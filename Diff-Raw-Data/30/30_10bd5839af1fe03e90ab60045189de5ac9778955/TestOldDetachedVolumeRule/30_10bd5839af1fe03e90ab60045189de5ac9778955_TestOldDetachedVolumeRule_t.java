 //CHECKSTYLE IGNORE Javadoc
 //CHECKSTYLE IGNORE MagicNumberCheck
 /*
  *
  *  Copyright 2012 Netflix, Inc.
  *
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  *
  */
 
 package com.netflix.simianarmy.aws.janitor.rule.volume;
 
 import java.util.Date;
 
 import org.joda.time.DateTime;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import com.netflix.simianarmy.Resource;
 import com.netflix.simianarmy.aws.AWSResource;
 import com.netflix.simianarmy.aws.AWSResourceType;
 import com.netflix.simianarmy.aws.janitor.VolumeTaggingMonkey;
 import com.netflix.simianarmy.aws.janitor.rule.TestMonkeyCalendar;
 import com.netflix.simianarmy.janitor.JanitorMonkey;
 
 import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;
 import static org.joda.time.DateTimeConstants.MILLIS_PER_HOUR;
 
 
 public class TestOldDetachedVolumeRule {
 
     @Test
     public void testNonVolumeResource() {
         Resource resource = new AWSResource().withId("asg1").withResourceType(AWSResourceType.ASG);
         ((AWSResource) resource).setAWSResourceState("available");
         OldDetachedVolumeRule rule = new OldDetachedVolumeRule(new TestMonkeyCalendar(), 0, 0);
         Assert.assertTrue(rule.isValid(resource));
         Assert.assertNull(resource.getExpectedTerminationTime());
     }
 
     @Test
     public void testUnavailableVolume() {
         Resource resource = new AWSResource().withId("vol-123").withResourceType(AWSResourceType.EBS_VOLUME);
         ((AWSResource) resource).setAWSResourceState("stopped");
         OldDetachedVolumeRule rule = new OldDetachedVolumeRule(new TestMonkeyCalendar(), 0, 0);
         Assert.assertTrue(rule.isValid(resource));
         Assert.assertNull(resource.getExpectedTerminationTime());
     }
 
     @Test
     public void testTaggedAsNotMark() {
         int ageThreshold = 5;
         DateTime now = DateTime.now();
         Resource resource = new AWSResource().withId("vol-123").withResourceType(AWSResourceType.EBS_VOLUME)
                 .withLaunchTime(new Date(now.minusDays(ageThreshold + 1).getMillis()));
         ((AWSResource) resource).setAWSResourceState("available");
         Date lastDetachTime = new Date(now.minusDays(ageThreshold + 1).getMillis());
         String metaTag = VolumeTaggingMonkey.makeMetaTag(null, null, lastDetachTime);
         resource.setTag(JanitorMonkey.JANITOR_META_TAG, metaTag);
         int retentionDays = 4;
         OldDetachedVolumeRule rule = new OldDetachedVolumeRule(new TestMonkeyCalendar(),
                 ageThreshold, retentionDays);
         resource.setTag(JanitorMonkey.JANITOR_TAG, "donotmark");
         Assert.assertTrue(rule.isValid(resource));
         Assert.assertNull(resource.getExpectedTerminationTime());
     }
 
     @Test
     public void testNoMetaTag() {
         int ageThreshold = 5;
         DateTime now = DateTime.now();
         Resource resource = new AWSResource().withId("vol-123").withResourceType(AWSResourceType.EBS_VOLUME)
                 .withLaunchTime(new Date(now.minusDays(ageThreshold + 1).getMillis()));
         ((AWSResource) resource).setAWSResourceState("available");
         int retentionDays = 4;
         OldDetachedVolumeRule rule = new OldDetachedVolumeRule(new TestMonkeyCalendar(),
                 ageThreshold, retentionDays);
         resource.setTag(JanitorMonkey.JANITOR_TAG, "donotmark");
         Assert.assertTrue(rule.isValid(resource));
         Assert.assertNull(resource.getExpectedTerminationTime());
     }
 
     @Test
     public void testUserSpecifiedTerminationDate() {
         int ageThreshold = 5;
         DateTime now = DateTime.now();
         Resource resource = new AWSResource().withId("vol-123").withResourceType(AWSResourceType.EBS_VOLUME)
                 .withLaunchTime(new Date(now.minusDays(ageThreshold + 1).getMillis()));
         ((AWSResource) resource).setAWSResourceState("available");
         int retentionDays = 4;
         DateTime userDate = new DateTime(now.plusDays(3).withTimeAtStartOfDay());
         resource.setTag(JanitorMonkey.JANITOR_TAG,
                 OldDetachedVolumeRule.TERMINATION_DATE_FORMATTER.print(userDate));
         OldDetachedVolumeRule rule = new OldDetachedVolumeRule(new TestMonkeyCalendar(),
                 ageThreshold, retentionDays);
         Assert.assertFalse(rule.isValid(resource));
         Assert.assertEquals(resource.getExpectedTerminationTime().getTime(), userDate.getMillis());
     }
 
     @Test
     public void testOldDetachedVolume() {
         int ageThreshold = 5;
         DateTime now = DateTime.now();
         Resource resource = new AWSResource().withId("vol-123").withResourceType(AWSResourceType.EBS_VOLUME)
                 .withLaunchTime(new Date(now.minusDays(ageThreshold + 1).getMillis()));
         ((AWSResource) resource).setAWSResourceState("available");
         Date lastDetachTime = new Date(now.minusDays(ageThreshold + 1).getMillis());
         String metaTag = VolumeTaggingMonkey.makeMetaTag(null, null, lastDetachTime);
         resource.setTag(JanitorMonkey.JANITOR_META_TAG, metaTag);
         int retentionDays = 4;
         OldDetachedVolumeRule rule = new OldDetachedVolumeRule(new TestMonkeyCalendar(),
                 ageThreshold, retentionDays);
         Assert.assertFalse(rule.isValid(resource));
         verifyTerminationTime(resource, retentionDays, now);
     }
 
     @Test
     public void testDetachedVolumeNotOld() {
         int ageThreshold = 5;
         DateTime now = DateTime.now();
         Resource resource = new AWSResource().withId("vol-123").withResourceType(AWSResourceType.EBS_VOLUME)
                 .withLaunchTime(new Date(now.minusDays(ageThreshold + 1).getMillis()));
         ((AWSResource) resource).setAWSResourceState("available");
         Date lastDetachTime = new Date(now.minusDays(ageThreshold - 1).getMillis());
         String metaTag = VolumeTaggingMonkey.makeMetaTag(null, null, lastDetachTime);
         resource.setTag(JanitorMonkey.JANITOR_META_TAG, metaTag);
         int retentionDays = 4;
         OldDetachedVolumeRule rule = new OldDetachedVolumeRule(new TestMonkeyCalendar(),
                 ageThreshold, retentionDays);
         Assert.assertTrue(rule.isValid(resource));
         Assert.assertNull(resource.getExpectedTerminationTime());
     }
 
     @Test
     public void testAchedVolume() {
         int ageThreshold = 5;
         DateTime now = DateTime.now();
         Resource resource = new AWSResource().withId("vol-123").withResourceType(AWSResourceType.EBS_VOLUME)
                 .withLaunchTime(new Date(now.minusDays(ageThreshold + 1).getMillis()));
         ((AWSResource) resource).setAWSResourceState("available");
         String metaTag = VolumeTaggingMonkey.makeMetaTag("i-123", "owner", null);
         resource.setTag(JanitorMonkey.JANITOR_META_TAG, metaTag);
         int retentionDays = 4;
         OldDetachedVolumeRule rule = new OldDetachedVolumeRule(new TestMonkeyCalendar(),
                 ageThreshold, retentionDays);
         Assert.assertTrue(rule.isValid(resource));
         Assert.assertNull(resource.getExpectedTerminationTime());
     }
 
 
     @Test
     public void testResourceWithExpectedTerminationTimeSet() {
         DateTime now = DateTime.now();
         Date oldTermDate = new Date(now.plusDays(10).getMillis());
         String oldTermReason = "Foo";
         int ageThreshold = 5;
         Resource resource = new AWSResource().withId("vol-123").withResourceType(AWSResourceType.EBS_VOLUME)
                 .withLaunchTime(new Date(now.minusDays(ageThreshold + 1).getMillis()));
         ((AWSResource) resource).setAWSResourceState("available");
         Date lastDetachTime = new Date(now.minusDays(ageThreshold + 1).getMillis());
         String metaTag = VolumeTaggingMonkey.makeMetaTag(null, null, lastDetachTime);
         resource.setTag(JanitorMonkey.JANITOR_META_TAG, metaTag);
         int retentionDays = 4;
         OldDetachedVolumeRule rule = new OldDetachedVolumeRule(new TestMonkeyCalendar(),
                 ageThreshold, retentionDays);
         resource.setExpectedTerminationTime(oldTermDate);
         resource.setTerminationReason(oldTermReason);
         Assert.assertFalse(rule.isValid(resource));
         Assert.assertEquals(oldTermDate, resource.getExpectedTerminationTime());
         Assert.assertEquals(oldTermReason, resource.getTerminationReason());
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void testNullResource() {
         OldDetachedVolumeRule rule = new OldDetachedVolumeRule(new TestMonkeyCalendar(), 5, 4);
         rule.isValid(null);
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void testNgativeAgeThreshold() {
         new OldDetachedVolumeRule(new TestMonkeyCalendar(), -1, 4);
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void testNgativeRetentionDaysWithOwner() {
         new OldDetachedVolumeRule(new TestMonkeyCalendar(), 5, -4);
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void testNullCalendar() {
         new OldDetachedVolumeRule(null, 5, 4);
     }
 
    /** Verify that the termination date is roughly retentionDays from now **/
     private void verifyTerminationTime(Resource resource, int retentionDays, DateTime now) {
         long timeDifference = (resource.getExpectedTerminationTime().getTime() - now.getMillis());
         //use floating point, allow for a one hour diff on either side due to DST cutover
         double retentionMillis = (double) retentionDays * MILLIS_PER_DAY;
         double actualTerminationDays = (double) timeDifference / (double) MILLIS_PER_DAY;
         double allowableLowerBound = (retentionMillis - (double) MILLIS_PER_HOUR) / (double) MILLIS_PER_DAY;
         double allowableUpperBound = (retentionMillis + (double) MILLIS_PER_HOUR) / (double) MILLIS_PER_DAY;
 
         Assert.assertTrue(allowableLowerBound <= actualTerminationDays && actualTerminationDays <= allowableUpperBound);
     }
 }
