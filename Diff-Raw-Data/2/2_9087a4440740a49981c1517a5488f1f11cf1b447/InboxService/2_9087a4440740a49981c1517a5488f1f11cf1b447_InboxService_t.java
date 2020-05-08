 package beast.tutorial.stories.system;
 
 import es.upm.dit.gsi.beast.story.logging.LogActivator;
 import es.upm.dit.gsi.beast.story.BeastTestCaseRunner;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.Properties;
 import org.junit.Test;
 
 /**
  * Main class to translate plain text into code, following the Given-When-Then
  * language. In the GIVEN part it launchs the platform In the WHEN part it
  * configures the state of its agents. In the THEN part it checks the correct
  * behaviour. The main purpose of it consists of knowing agents' state/properties
  * without changing its code.
  * 
  * @author es.upm.dit.gsi.beast
  */
 public class InboxService{
 
     public Logger logger = Logger.getLogger(InboxService.class.getName());
 
     /**
      * Constructor to configure logging
      */
     public InboxService() {
          Properties preferences = new Properties();
          try {
               FileInputStream configFile = new FileInputStream("src/test/resources/beast-conf/logger.properties");
               preferences.load(configFile);
               LogManager.getLogManager().readConfiguration(configFile);
               LogActivator.logToFile(logger, InboxService.class.getName(), Level.ALL);
          } catch (IOException ex) {
               logger.severe("WARNING: Could not open configuration file");
          }
     }
 
   /**
    * This is the scenario: PassingIncomingCall,
    * where the GIVEN is described as: a customer is calling,,
    * the WHEN is described as: the voice recognition system does not understand to the customer,
    * and the THEN is described as: the call is attended by a helpdesk operator.
    */
     @Test
     public void passingIncomingCall(){
         // Here you must call the MAS tests for this scenario
         // or the scenarios themselves
         // EXAMPLE:
         // BeastTestCaseRunner.executeBeastTestCase("es.upm.dit.gsi.beast.reader.mas.test.MASTestScenario");
         // JUnitCore.runClasses(es.upm.dit.gsi.beast.reader.mas.test.MASTestStory.class");
     }
 
   /**
    * This is the scenario: ReportCreation,
    * where the GIVEN is described as: a customer has a problem,,
   * the WHEN is described as: a phone call is received and the voice recognition system understands to the customer,
    * and the THEN is described as: the system records the message and a new issue report is created.
    */
     @Test
     public void reportCreation(){
         // Here you must call the MAS tests for this scenario
         // or the scenarios themselves
         // EXAMPLE:
         // BeastTestCaseRunner.executeBeastTestCase("es.upm.dit.gsi.beast.reader.mas.test.MASTestScenario");
         // JUnitCore.runClasses(es.upm.dit.gsi.beast.reader.mas.test.MASTestStory.class");
     }
 
 }
 
