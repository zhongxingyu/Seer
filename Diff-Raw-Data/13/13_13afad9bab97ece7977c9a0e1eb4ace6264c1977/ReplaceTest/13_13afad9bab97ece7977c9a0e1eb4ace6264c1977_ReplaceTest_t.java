 package org.playerbot.ai;
 
 import org.dbunit.Assertion;
 import org.dbunit.dataset.ITable;
 import org.junit.Test;
 
 public class ReplaceTest extends AbstractDbUnitTest {
 
     @Test
     public void r2ToZero() throws Exception {
         replace();
 
         ITable actualData = destinationConnection.createQueryTable("characters",
                 "SELECT * FROM characters WHERE name = 'TestChar'");
        Assertion.assertEqualsIgnoreCols(destinationDataSet.getTable("characters"), actualData, new String[] {"guid"});
     }
 
     @Override
     protected String getDestinationDataSetName() {
         return "zero.xml";
     }
 
     @Override
     protected String getSourceDataSetName() {
         return "r2.xml";
     }
 
     @Override
     protected String getDestinationConnectionName() {
         return "mangoszero_Crystalsong";
     }
 
     @Override
     protected String getSourceConnectionName() {
         return "r2_Eversong";
     }
 
     private void replace() throws Exception {
         Main main = new Main();
         main.characterName = "TestChar";
         main.sourceConnectionName = getSourceConnectionName();
         main.destinationConnectionName = getDestinationConnectionName();
         main.replace = true;
         main.configurationFileName = "src/main/resources/config.xml";
         main.run();
     }
 
 }
