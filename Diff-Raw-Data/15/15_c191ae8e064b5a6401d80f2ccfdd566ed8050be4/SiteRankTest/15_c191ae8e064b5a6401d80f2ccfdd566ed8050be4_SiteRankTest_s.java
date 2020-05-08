 package org.healthonnet.lucene.siterank;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class SiteRankTest {
 
     @Test
     public void testSiteRank1() {
         Assert.assertEquals(1, new MockSiteRankSource().getRankInfo("google.com").getRank());
         Assert.assertEquals(430, new MockSiteRankSource().getRankInfo("webmd.com").getRank());
         Assert.assertEquals(4706956, new MockSiteRankSource().getRankInfo("thebuttercompartment.com").getRank());
         
        // no foobar.com
        Assert.assertFalse(new MockSiteRankSource().getRankInfo("foobar.com").isSiteFound());
     }
 }
