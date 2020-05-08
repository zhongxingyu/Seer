 package org.drools.planner.examples.ras2012.model;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class ScheduleAdherenceRequirementTest {
 
     @Test
     public void testEqualsObject() {
         final ScheduleAdherenceRequirement s1 = new ScheduleAdherenceRequirement(Node.getNode(0), 1);
         Assert.assertEquals("ScheduleAdherenceRequirement should equal itself.", s1, s1);
         final ScheduleAdherenceRequirement s2 = new ScheduleAdherenceRequirement(Node.getNode(0), 1);
         Assert.assertEquals("ScheduleAdherenceRequirement should equal others with the same data.",
                 s1, s2);
         final ScheduleAdherenceRequirement s3 = new ScheduleAdherenceRequirement(Node.getNode(1), 1);
         Assert.assertFalse(
                 "ScheduleAdherenceRequirement shouldn't equal others with different data.",
                 s1.equals(s3));
         final ScheduleAdherenceRequirement s4 = new ScheduleAdherenceRequirement(Node.getNode(0), 2);
         Assert.assertFalse(
                 "ScheduleAdherenceRequirement shouldn't equal others with different data.",
                 s1.equals(s4));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testScheduleAdherenceRequirementNullNode() {
         new ScheduleAdherenceRequirement(null, 1);
     }
 
 }
