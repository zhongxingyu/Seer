 package org.amplafi.flow.test;
 
 import static org.junit.Assert.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import org.amplafi.flow.utils.AdminTool;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import sun.reflect.*;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.lang.reflect.*;
 
 /**
  * A class tests AdminTool.
  * @author Daisy
  *
  */
 public class AdminToolTest {
     //the testBuffer is used to test if a script is opend by command
     public static StringBuffer testBuffer = new StringBuffer();
     TestHelperAdminTool testHelpAdminTool = null;
     public static final String PATH = "src\\test\\resources\\testscripts\\junitTestScript";
     public static final String CONFIG_FILE_NAME = "fareaches.fadmin.test.properties";
     String userInputKey;
     String userInputHost;
     String userInputPort;
     String userInputApiv;
 
     /**
      * Junit test sets up data.
      */
     @Before
     public void setUp() throws Exception {
         userInputKey = "";
         userInputHost = "";
         userInputPort = "";
         userInputApiv = "";
         testHelpAdminTool = new TestHelperAdminTool();
 //        testBuffer = new StringBuffer();
         testHelpAdminTool.setComandScriptPath(PATH);
         testHelpAdminTool.setConfigFileName(CONFIG_FILE_NAME);
     }
 
     /**
      * Junit test tears down the data.
      */
     @After
     public void tearDown() throws Exception {
         testHelpAdminTool = null;
 //        testBuffer = null;
         userInputKey = null;
         userInputHost = null;
         userInputPort = null;
         userInputApiv = null;
         if(testBuffer.length()>0){
         	testBuffer.delete(0, testBuffer.length());
         }
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
         assertEquals(2,logMsgList.size());
         assertTrue(logMsgList.contains(excepted1));
         assertTrue(logMsgList.contains(excepted2));
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
         String excepted1 = "     testScript       - Just an test script       - \\src\\test\\resources\\testscripts\\junitTestScript\\testScript.groovy";
         String excepted2 = "     testScript2       - Just an test2 script       - \\src\\test\\resources\\testscripts\\junitTestScript\\testScript2.groovy";
         assertEquals(2,logMsgList.size());
         assertTrue(logMsgList.contains(excepted1));
         assertTrue(logMsgList.contains(excepted2));
     }
 
     /**
      * This test tests command FAdmin -help.
      */
     @Test
     public void testProcessCommandLineHelp() {
         String[] args = { "-help", "testScript2" };
         testHelpAdminTool.processCommandLine(args);
         String excepted = "Script Usage: usage test";
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
         assertEquals(1,logMsgList.size());
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
         assertEquals(exceptedKey,keyValue);
         assertEquals(exceptedPort,portValue);
         assertEquals(exceptedHost,hostValue);
         assertEquals(exceptedApiv,apivValue);
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
         String exceptedKey = "";
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
      * This test tests command FAdmin <script name> , key, port etc already in properties file
      */
     @Test
     public void testProcessCommandLineWithProExist() {
         userInputApiv = "";
         userInputHost = "";
         userInputKey = "";
         userInputPort = "";
         String[] args = { "testScript" };
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
         String exString = "This is testScript";
         assertEquals(exString,AdminToolTest.testBuffer.toString());
     }
 
     /**
      * This test tests command FAdmin <scriptName> <paramName>=<paramValue> .
      */
     @Test
     public void testProcessCommandLineRunScriptWithParam() {
         String[] args = { "testScript", "param1=dog" };
         testHelpAdminTool.processCommandLine(args);
         String exString = "This is testScript param1 : dog";
         assertEquals(exString,AdminToolTest.testBuffer.toString());
     }
 
     /**
      * This class is AdminTool Mock, it extends AdminTool and override some method for test.
      */
     class TestHelperAdminTool extends AdminTool {
         List<String> logMsgList = new ArrayList();
         Properties configProperties = new Properties();
         
         public TestHelperAdminTool(){
             configProperties.setProperty("key", "ampcb_333df5d558173cc402f59913355e9f5f1fb8b444cd7d43be01c89fe216500469");
             configProperties.setProperty("host", AdminTool.DEFAULT_HOST);
             configProperties.setProperty("port", AdminTool.DEFAULT_PORT);
             configProperties.setProperty("apiv", AdminTool.DEFAULT_API_VERSION);
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
 }
