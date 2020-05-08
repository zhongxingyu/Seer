 package org.investovator.controller.config;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * @author Amila Surendra
  * @version $Revision
  */
 public class ModelGeneratorTest {
 
     ModelGenerator generator;
 
     @Before
     public void setUp() throws Exception {
         generator = new ModelGenerator(getClass().getResource("model_template.xml").getPath());
 
     }
 
     @Test
     public void testGetSupportedAgentTypes() throws Exception {
 
         String[] result = generator.getSupportedAgentTypes();
 
         for (int i = 0; i < result.length; i++) {
             System.out.println(result[i]);
         }
     }
 
     @Test
     public void testCreateModelConfig() throws Exception{
 
         generator.addAgent(generator.getSupportedAgentTypes()[0],120);
         generator.addAgent(generator.getSupportedAgentTypes()[1],100);
 
         ReportGenerator gen = new ReportGenerator(getClass().getResource("report_template.xml").getPath());
         String[] types = gen.getSupportedReports();
         String[] result = gen.getDependencyReportBeans(types[0]);
 
         generator.addDependencyReportBean(result);
 
         generator.addSimulationProperty("$slowSleepInterval","100");
         generator.addSimulationProperty("$maximumDays","1");
         generator.addSimulationProperty("$lengthOfDay","200000");
         generator.addSimulationProperty("$initialPrice","500");
 


         generator.createModelConfig();
 
     }
 
 
 
 }
