 package org.amplafi.flow.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.amplafi.flow.utils.AdminTool;
 import org.apache.log4j.AppenderSkeleton;
 import org.apache.log4j.Logger;
 import org.apache.log4j.spi.LoggingEvent;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * A class tests AdminTool.
  * @author Daisy
  */
 
 public class TestAdminTool {
     //The testBuffer is used to test if a script is opened by command.
     //Use a public static to allow the example script to communicate back to this test.
     public static final String TEST_REPORT_SYS_PROP = "test.report";
     private TestHelperAdminTool testHelpAdminTool = null;
     private static final String SEP = System.getProperty("file.separator");
 
     public static final String PATH = "src" + SEP +"test" + SEP +"resources" + SEP +"testscripts" + SEP +"junitTestScript";
     public static final String CONFIG_FILE_NAME = "fareaches.fadmin.test.properties";
     private String userInputKey;
     private String userInputHost;
     private String userInputPort;
     private String userInputApiv;
     private TestAppender testAppender;
 
     /**
      * Junit test sets up data.
      */
     @BeforeMethod
     public void setUp() throws Exception {
         testAppender = new TestAppender();
        Logger.getLogger(getClass()).addAppender(testAppender);
         userInputKey = "";
         userInputHost = "";
         userInputPort = "";
         userInputApiv = "";
         testHelpAdminTool = new TestHelperAdminTool();
         testHelpAdminTool.setComandScriptPath(PATH);
         testHelpAdminTool.setConfigFileName(CONFIG_FILE_NAME);
     }
 
     /**
      * Junit test tears down the data.
      */
     @AfterMethod
     public void tearDown() throws Exception {
        Logger.getLogger(getClass()).removeAllAppenders();
         testHelpAdminTool = null;
         userInputKey = null;
         userInputHost = null;
         userInputPort = null;
         userInputApiv = null;
         testAppender = null;
     }
 
     /**
      * This test tests command FAdmin -l.
      */
     @Test
     public void testProcessCommandLineList() throws SecurityException,
             NoSuchFieldException, Exception {
         String[] args = { "-l" };
         testHelpAdminTool.processCommandLine(args);
         List<String> logMsgList = testHelpAdminTool.getLogMsgList();
         //This is test that use our own script
         String excepted1 = "     testScript2       - Just an test2 script";
         String excepted2 = "     testScript       - Just an test script";
         String excepted3 = "     GetPermApiKey       - Generates a new Permanent API key";
         assertEquals(3,logMsgList.size());
         assertTrue(logMsgList.contains(excepted1));
         assertTrue(logMsgList.contains(excepted2));
         assertTrue(logMsgList.contains(excepted3));
     }
 
     /**
      * This test tests command FAdmin -L.
      */
     @Test
     public void testProcessCommandLineListdetailed() throws SecurityException,
             NoSuchFieldException, Exception {
         String[] args = { "-L" };
         testHelpAdminTool.processCommandLine(args);
         List<String> logMsgList = testHelpAdminTool.getLogMsgList();
         //This is test that use our own script
         String expected1 = "     GetPermApiKey       - Generates a new Permanent API key       - " + SEP + PATH + SEP + "GetNewPermanentKey.groovy";
         String expected2 = "     testScript       - Just an test script       - " + SEP + PATH + SEP + "testScript.groovy";
         String expected3 = "     testScript2       - Just an test2 script       - " + SEP + PATH + SEP + "testScript2.groovy";
         assertEquals(3,logMsgList.size());
         assertEquals(expected1,logMsgList.get(0));
         assertEquals(expected2,logMsgList.get(1));
         assertEquals(expected3,logMsgList.get(2));
     }
 
     /**
      * This test tests command FAdmin -help.
      */
     @Test
     public void testProcessCommandLineHelp() {
         String[] args = { "-help", "testScript2" };
         testHelpAdminTool.processCommandLine(args);
         String excepted = "usage test";
         List<String> logMsgList = testHelpAdminTool.getLogMsgList();
         assertEquals(1,logMsgList.size());
         assertTrue(logMsgList.contains(excepted));
     }
 
     /**
      * This test tests command FAdmin -help, but script does not have usage information .
      */
     @Test
     public void testProcessCommandLineHelpWithNoUsage() {
         String[] args = { "-help", "testScript" };
         testHelpAdminTool.processCommandLine(args);
         String excepted = "Script testScript does not have usage information";
         List<String> logMsgList = testHelpAdminTool.getLogMsgList();
         assertEquals(1, logMsgList.size());
         assertTrue(logMsgList.contains(excepted));
     }
 
     /**
      * This test tests command FAdmin -x,user enters port etc manually at prompt .
      */
     @Test
     public void testProcessCommandLineX() {
         userInputApiv = "apiv2";
         userInputHost = "http://apiv1.farreach.es";
         userInputKey = "ampcb_222df5d558173cc402f59913355e9f5f1fb8b444cd7d43be01c89fe216500469";
         userInputPort = "80";
         String[] args = { "-x" };
         testHelpAdminTool.processCommandLine(args);
         String keyValue = testHelpAdminTool.getConfigProperties().getProperty("key", "");
         String exceptedKey = userInputKey;
         String portValue = testHelpAdminTool.getConfigProperties().getProperty("port", "");
         String exceptedPort = userInputPort;
         String hostValue = testHelpAdminTool.getConfigProperties().getProperty("host", "");
         String exceptedHost = userInputHost;
         String apivValue = testHelpAdminTool.getConfigProperties().getProperty("apiv", "");
         String exceptedApiv = userInputApiv;
         assertEquals(exceptedKey, keyValue);
         assertEquals(exceptedPort, portValue);
         assertEquals(exceptedHost, hostValue);
         assertEquals(exceptedApiv, apivValue);
     }
 
     /**
      * This test tests command FAdmin -x , key, port etc passes in on command line
      */
     @Test
     public void testProcessCommandLineWithKeyEtc() {
         String[] args = { "-x", "-key", "ampcb_111df5d558173cc402f59913355e9f5f1fb8b444cd7d43be01c89fe216500469", "-port", "8080" };
         testHelpAdminTool.processCommandLine(args);
         String keyValue = testHelpAdminTool.getConfigProperties().getProperty("key", "");
         String exceptedKey = "ampcb_111df5d558173cc402f59913355e9f5f1fb8b444cd7d43be01c89fe216500469";
         String portValue = testHelpAdminTool.getConfigProperties().getProperty("port", "");
         String exceptedPort = "8080";
         String hostValue = testHelpAdminTool.getConfigProperties().getProperty("host", "");
         String exceptedHost = AdminTool.DEFAULT_HOST;
         String apivValue = testHelpAdminTool.getConfigProperties().getProperty("apiv", "");
         String exceptedApiv = AdminTool.DEFAULT_API_VERSION;
         assertEquals(exceptedKey,keyValue);
         assertEquals(exceptedPort,portValue);
         assertEquals(exceptedHost,hostValue);
         assertEquals(exceptedApiv,apivValue);
     }
 
     /**
      * This test tests command FAdmin -x , key, port etc are default
      */
     @Test
     public void testProcessCommandLineWithDefault() {
         userInputApiv = "";
         userInputHost = "";
         userInputKey = "";
         userInputPort = "";
         String[] args = { "-x" };
         testHelpAdminTool.processCommandLine(args);
         String keyValue = testHelpAdminTool.getConfigProperties().getProperty("key", "");
         String exceptedKey = "Auto obtain key";
         String portValue = testHelpAdminTool.getConfigProperties().getProperty("port", "");
         String exceptedPort = AdminTool.DEFAULT_PORT;
         String hostValue = testHelpAdminTool.getConfigProperties().getProperty("host", "");
         String exceptedHost = AdminTool.DEFAULT_HOST;
         String apivValue = testHelpAdminTool.getConfigProperties().getProperty("apiv", "");
         String exceptedApiv = AdminTool.DEFAULT_API_VERSION;
         // returned from ./src/test/resources/testscripts/junitTestScript/GetNewPermanentKey.groovy
         assertEquals("Dummy_key",keyValue);
         assertEquals(exceptedPort,portValue);
         assertEquals(exceptedHost,hostValue);
         assertEquals(exceptedApiv,apivValue);
     }
 
     /**
      * This test tests command FAdmin <script name> , key, port etc already in properties file
      */
     @Test
     public void testProcessCommandLineWithProExist() {
         userInputApiv = "";
         userInputHost = "";
         userInputKey = "";
         userInputPort = "";
         String[] args = { "testScript" };
 
         // Set up config properties.
         Properties configProperties = testHelpAdminTool.getConfigProperties();
         configProperties.setProperty("key", "ampcb_333df5d558173cc402f59913355e9f5f1fb8b444cd7d43be01c89fe216500469");
         configProperties.setProperty("host", AdminTool.DEFAULT_HOST);
         configProperties.setProperty("port", AdminTool.DEFAULT_PORT);
         configProperties.setProperty("apiv", AdminTool.DEFAULT_API_VERSION);
 
         // Run the command.
         testHelpAdminTool.processCommandLine(args);
 
         String keyValue = testHelpAdminTool.getConfigProperties().getProperty("key", "");
         String exceptedKey = "ampcb_333df5d558173cc402f59913355e9f5f1fb8b444cd7d43be01c89fe216500469";
         String portValue = testHelpAdminTool.getConfigProperties().getProperty("port", "");
         String exceptedPort = AdminTool.DEFAULT_PORT;
         String hostValue = testHelpAdminTool.getConfigProperties().getProperty("host", "");
         String exceptedHost = AdminTool.DEFAULT_HOST;
         String apivValue = testHelpAdminTool.getConfigProperties().getProperty("apiv", "");
         String exceptedApiv = AdminTool.DEFAULT_API_VERSION;
         assertEquals(exceptedKey,keyValue);
         assertEquals(exceptedPort,portValue);
         assertEquals(exceptedHost,hostValue);
         assertEquals(exceptedApiv,apivValue);
     }
 
     /**
      * This test tests command FAdmin <script name> .
      */
     @Test
     public void testProcessCommandLineRunScript() {
         String[] args = { "testScript" };
         testHelpAdminTool.processCommandLine(args);
         String exString = "This is testScript1";
         assertEquals(exString, testAppender.getLogs());
     }
 
     /**
      * This test tests command FAdmin <scriptName> <paramName>=<paramValue> .
      */
     @Test
     public void testProcessCommandLineRunScriptWithParam() {
         String[] args = { "testScript", "param1=dog" };
         testHelpAdminTool.processCommandLine(args);
         String exString = "This is testScript1 param1 : dog";
         assertEquals(exString, testAppender.getLogs());
     }
 
 
     /**
      * This class is AdminTool Mock, it extends AdminTool and override some method for test.
      * DAISY: there are functions in easyMock that can do this much quicker.
      */
     class TestHelperAdminTool extends AdminTool {
 
         private List<String> logMsgList = new ArrayList<String>();
         private Properties configProperties = new Properties();
 
         public TestHelperAdminTool(){
         }
 
         /**
          * @return the configProperties
          */
         public Properties getConfigProperties(){
             return configProperties;
         }
 
         /**
          * @return the log message list
          */
         public List<String> getLogMsgList() {
             return logMsgList;
         }
 
         /**
          * This method add the out put from adminTool to the message list
          */
         @Override
         public void emitOutput(String msg) {
             logMsgList.add(msg);
         }
 
         /**
          * This method mock user input
          * @return test data for user input
          */
         @Override
         public String getUserInput(String key){
             if(key.equals("host")){
                 return userInputHost;
             }else if(key.equals("port")){
                 return userInputPort;
             }else if(key.equals("apiv")){
                 return userInputApiv;
             }else if(key.equals("key")){
                 return userInputKey;
             }else{
                 return "";
             }
         }
 
         /**
          * This method mock get properties
          * @return configProperties
          */
         @Override
         public Properties getProperties(){
             return configProperties;
         }
 
         /**
          * This method override saveProperties method of AdminTool so that test can not create a properties file
          */
         @Override
         public void saveProperties(){
         }
     }
 
     private class TestAppender extends AppenderSkeleton {
         private StringBuffer logBuffer;
 
         public TestAppender(){
             logBuffer = new StringBuffer();
         }
 
         public void append(LoggingEvent event) {
             logBuffer.append(event.getMessage());
         }
 
         public boolean requiresLayout() {
             return false;
         }
 
         public void close() {
         }
 
         public String getLogs(){
             return logBuffer.toString();
         }
     }
 
 }
