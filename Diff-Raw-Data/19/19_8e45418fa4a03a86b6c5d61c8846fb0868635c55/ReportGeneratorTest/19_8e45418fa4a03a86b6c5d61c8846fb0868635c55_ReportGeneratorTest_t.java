 package org.investovator.controller.config;
 
 import org.junit.Test;
 
 /**
  * @author Amila Surendra
  * @version $Revision
  */
 public class ReportGeneratorTest {
     @Test
     public void testGenerateXML() throws Exception {
 
        ReportGenerator gen = new ReportGenerator();
 
         gen.generateXML("IBM");
 
 
     }
 
     @Test
     public void testGetSupportedReports() throws Exception{
 
        ReportGenerator gen = new ReportGenerator();
 
         String[] result = gen.getSupportedReports();
 
         for (int i = 0; i < result.length; i++) {
             System.out.println(result[i]);
         }
     }
 
 
     @Test
     public void testGetDependencyReportBeans() throws Exception{
 
        ReportGenerator gen = new ReportGenerator();
 
         String[] types = gen.getSupportedReports();
 
         String[] result = gen.getDependencyReportBeans(types[0]);
 
         for (int i = 0; i < result.length; i++) {
             System.out.println(result[i]);
         }
     }
 }
