 package com.timgroup.tucker.info;
 
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
 
 public class ReportTest {
     
     @Test
     public void worstStatusCanBeFound() throws Exception {
         assertEquals(Status.OK, Report.worstStatus(Collections.<Report> emptySet()));
         
         assertEquals(Status.OK, Report.worstStatus(Collections.singleton(new Report(Status.INFO, null))));
         assertEquals(Status.OK, Report.worstStatus(Collections.singleton(new Report(Status.OK, null))));
         assertEquals(Status.CRITICAL, Report.worstStatus(Collections.singleton(new Report(Status.CRITICAL, null))));
         
         assertEquals(Status.OK, Report.worstStatus(Arrays.asList(new Report(Status.INFO, null), new Report(Status.INFO, null))));
         assertEquals(Status.OK, Report.worstStatus(Arrays.asList(new Report(Status.INFO, null), new Report(Status.OK, null))));
         assertEquals(Status.CRITICAL, Report.worstStatus(Arrays.asList(new Report(Status.OK, null), new Report(Status.CRITICAL, null))));
     }
     
     @Test
     public void reportMightNotHaveAValue() throws Exception {
         assertTrue(new Report(Status.INFO, "foo").hasValue());
         assertFalse(new Report(Status.INFO, null).hasValue());
         assertFalse(new Report(Status.INFO).hasValue());
     }
     
     @Test
     public void reportMightFail() throws Exception {
         assertTrue(new Report(Status.INFO, "foo").isSuccessful());
         assertTrue(new Report(Status.INFO).isSuccessful());
         
         assertFalse(new Report(Status.INFO, new Error()).isSuccessful());
         
         assertFalse(new Report(new Error()).isSuccessful());
         assertEquals(Status.CRITICAL, new Report(new Error()).getStatus());
     }
     
 }
