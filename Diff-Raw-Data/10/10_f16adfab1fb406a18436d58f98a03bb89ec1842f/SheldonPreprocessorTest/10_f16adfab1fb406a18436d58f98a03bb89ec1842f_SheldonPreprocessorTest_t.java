 package com.moviepilot.sheldon.compactor.custom;/*
 
 /**
  * SheldonPreprocessorTest
  *
  * @author stefanp
  */
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class SheldonPreprocessorTest {
     final SheldonNodePreprocessor prep = new SheldonNodePreprocessor();
 
     @Test
     public void testParse() {
         Assert.assertEquals(prep.parseRubyTimeLong("2011-06-17T17:08:58+02:00"), 1308323338L);
         Assert.assertEquals("2011-06-17T17:08:58+02:00", prep.formatRubyTime(1308323338L));
         Assert.assertEquals("2012-08-23T16:56:13+02:00", prep.formatRubyTime(1345733773L));
         Assert.assertEquals("2012-08-23T16:25:02+02:00", prep.formatRubyTime(1345731902L));
        Assert.assertEquals(prep.parseRubyTimeLong("2011-08-09T00:58:13+02:00"), 1312844293L);
     }

 }
