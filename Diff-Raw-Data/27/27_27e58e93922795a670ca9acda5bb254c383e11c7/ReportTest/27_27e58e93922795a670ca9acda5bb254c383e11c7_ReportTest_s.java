 package com.timgroup.status;
 
 import java.util.Arrays;
 import java.util.Collections;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 public class ReportTest {
     
     @Test
     public void worstStatusCanBeFound() throws Exception {
         assertEquals(Status.OK, Report.worstStatus(Collections.<Report> emptySet()));
         
         assertEquals(Status.OK, Report.worstStatus(Collections.singleton(new Report(Status.INFO, null))));
         assertEquals(Status.OK, Report.worstStatus(Collections.singleton(new Report(Status.OK, null))));
        assertEquals(Status.WARN, Report.worstStatus(Collections.singleton(new Report(Status.WARN, null))));
         
         assertEquals(Status.OK, Report.worstStatus(Arrays.asList(new Report(Status.INFO, null), new Report(Status.INFO, null))));
         assertEquals(Status.OK, Report.worstStatus(Arrays.asList(new Report(Status.INFO, null), new Report(Status.OK, null))));
        assertEquals(Status.WARN, Report.worstStatus(Arrays.asList(new Report(Status.OK, null), new Report(Status.WARN, null))));
     }
     
 }
