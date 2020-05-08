 /*
  * #%L
  * Bitrepository Common
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.protocol;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.core.util.StatusPrinter;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import javax.jms.JMSException;
 import javax.swing.JFrame;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.TestSettingsProvider;
 import org.bitrepository.common.utils.TestFileHelper;
 import org.bitrepository.protocol.bus.LocalActiveMQBroker;
 import org.bitrepository.protocol.bus.MessageBusWrapper;
 import org.bitrepository.protocol.bus.MessageReceiver;
 import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;
 import org.bitrepository.protocol.fileexchange.HttpServerConnector;
 import org.bitrepository.protocol.http.EmbeddedHttpServer;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.bitrepository.protocol.security.DummySecurityManager;
 import org.bitrepository.protocol.security.SecurityManager;
 import org.jaccept.TestEventManager;
 import org.jaccept.gui.ComponentTestFrame;
 import org.jaccept.structure.ExtendedTestCase;
 import org.jaccept.testreport.ReportGenerator;
 import org.slf4j.LoggerFactory;
 import org.testng.ITestResult;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.BeforeTest;
 
 /**
  * Contains the generic parts for integration testing.
  */
 public abstract class IntegrationTest extends ExtendedTestCase {
     protected static TestEventManager testEventManager = TestEventManager.getInstance();
     private static ReportGenerator reportGenerator;
     public static LocalActiveMQBroker broker;
     public static EmbeddedHttpServer server;
     public static HttpServerConnector httpServer;
     public static HttpServerConfiguration httpServerConfiguration;
     public static MessageBus messageBus;
 
     private MessageReceiverManager receiverManager;
     protected static String alarmDestinationID;
     protected static MessageReceiver alarmReceiver;
 
     protected static SecurityManager securityManager;
 
     /** Settings for the Component-Under-Test */
     protected static Settings settingsForCUT;
     /** Settings for the Test components */
     protected static Settings settingsForTestClient;
 
     protected String NON_DEFAULT_FILE_ID;
     protected static String DEFAULT_FILE_ID;
     protected static URL DEFAULT_FILE_URL;
     protected static String DEFAULT_FILE_ADDRESS;
     protected String DEFAULT_AUDITINFORMATION;
 
     protected String testMethodName;
 
     @BeforeSuite(alwaysRun = true)
     public void initializeSuite() {
         settingsForCUT = loadSettings(getComponentID());
         settingsForTestClient = loadSettings("TestSuiteInitialiser");
         makeUserSpecificSettings(settingsForCUT);
         makeUserSpecificSettings(settingsForTestClient);
 
         securityManager = createSecurityManager();
         setupMessageBus();
         setupHttpServer();
         startReportGenerator();
         DEFAULT_FILE_ID = "DefaultFile-" + createDate();
         try {
             DEFAULT_FILE_URL = httpServer.getURL(TestFileHelper.DEFAULT_FILE_ID);
             DEFAULT_FILE_ADDRESS = DEFAULT_FILE_URL.toExternalForm();
         } catch (MalformedURLException e) {
             throw new RuntimeException("Never happens");
         }
     }
 
     /**
      * May be extended by subclasses needing to have their receivers managed. Remember to still call
      * <code>super.registerReceivers()</code> when overriding
      */
     protected void registerMessageReceivers() {
         alarmReceiver = new MessageReceiver(settingsForCUT.getAlarmDestination(), testEventManager);
         addReceiver(alarmReceiver);
     }
     protected void addReceiver(MessageReceiver receiver) {
         receiverManager.addReceiver(receiver);
     }
 
     @AfterSuite(alwaysRun = true)
     public void shutdownSuite() {
         teardownMessageBus();
         teardownHttpServer();
     }
 
     /**
      * Defines the standard BitRepositoryCollection configuration
      */
     @BeforeMethod(alwaysRun = true)
     public final void beforeMethod(Method method) {
         testMethodName = method.getName();
         setupSettings();
         NON_DEFAULT_FILE_ID = TestFileHelper.createUniquePrefix(testMethodName);
         DEFAULT_AUDITINFORMATION = testMethodName;
         receiverManager = new MessageReceiverManager(messageBus);
         registerMessageReceivers();
         receiverManager.startListeners();
         initializeCUT();
     }
     /**
      *  To be overridden by concrete tests wishing to do stuff. Remember to call super if this is overridden.
      */
     protected void initializeCUT() {}
 
     @AfterMethod(alwaysRun = true)
     public final void afterMethod(ITestResult testResult) {
        if ( receiverManager != null ) {
            receiverManager.stopListeners();
        }
         if (testResult.isSuccess()) {
             afterMethodVerification();
         }
         shutdownCUT();
     }
 
     /**
      * May be used by by concrete tests for general verification when the test method has finished. Will only be run
      * if the test has passed (so far).
      */
     protected void afterMethodVerification() {
         receiverManager.checkNoMessagesRemainInReceivers();
     }
     /**
      *  May be overridden by concrete tests wishing to do stuff. Remember to call super if this is overridden.
      */
     protected void shutdownCUT() {}
 
     /**
      * Initializes the settings. Will postfix the alarm and collection topics with '-${user.name}
      */
     protected void setupSettings() {
         settingsForCUT = loadSettings(getComponentID());
         makeUserSpecificSettings(settingsForCUT);
 
         alarmDestinationID = settingsForCUT.getCollectionSettings().getProtocolSettings().getAlarmDestination();
 
         settingsForTestClient = loadSettings(testMethodName);
         makeUserSpecificSettings(settingsForTestClient);
     }
 
     /** Can be overloaded by tests needing to load custom settings */
     protected Settings loadSettings(String componentID) {
         return TestSettingsProvider.reloadSettings(componentID);
     }
 
     private void makeUserSpecificSettings(Settings settings) {
         settings.getCollectionSettings().getProtocolSettings().setCollectionDestination(
                 settings.getCollectionDestination() + getTopicPostfix());
         settings.getCollectionSettings().getProtocolSettings().setAlarmDestination(
                 settings.getAlarmDestination() + getTopicPostfix());
     }
 
     // Experimental, use at own risk.
     @BeforeTest (alwaysRun = true)
     public void startTestGUI() {
         if (System.getProperty("enableTestGUI", "false").equals("true") ) {
             JFrame hmi = new ComponentTestFrame();
             hmi.pack();
             hmi.setVisible(true);
         }
     }
     // Experimental, use at own risk.
     @BeforeSuite (alwaysRun = true)
     public void startReportGenerator() {
         if (System.getProperty("enableTestReport", "false").equals("true") ) {
             reportGenerator = new ReportGenerator();
             reportGenerator.projectStarted("Bitrepository test");
             //ToBeFinished
         }
     }
     @BeforeTest (alwaysRun = true)
     public void writeLogStatus() {
         if (System.getProperty("enableLogStatus", "false").equals("true")) {
             LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
             StatusPrinter.print(lc);
         }
     }
 
     /** Indicated whether an embedded active MQ should be started and used */
     public boolean useEmbeddedMessageBus() {
         return System.getProperty("useEmbeddedMessageBus", "true").equals("true");
     }
 
     /** Indicated whether an embedded http server should be started and used */
     public boolean useEmbeddedHttpServer() {
         return System.getProperty("useEmbeddedHttpServer", "false").equals("true");
     }
 
     /**
      * Hooks up the message bus.
      */
     private void setupMessageBus() {
         if (useEmbeddedMessageBus() && broker == null) {
             broker = new LocalActiveMQBroker(settingsForCUT.getMessageBusConfiguration());
             broker.start();
         }
         messageBus = new MessageBusWrapper(
                 ProtocolComponentFactory.getInstance().getMessageBus(settingsForCUT, securityManager), testEventManager);
     }
 
     /**
      * Shutdown the message bus.
      */
     private void teardownMessageBus() {
         try {
             messageBus.close();
         } catch (JMSException e) {
             throw new RuntimeException(e);
         }
 
         if(broker != null) {
             try {
                 broker.stop();
             } catch (Exception e) {
                 // No reason to pollute the test output with this
             }
         }
     }
 
     /**
      * Initialises the connection to the file exchange server. Also starts an embedded http server 
      * if this is going to be used, eg. if useEmbeddedHttpServer is true. 
      * @throws Exception
      */
     protected void setupHttpServer() {
         httpServerConfiguration =
                 new HttpServerConfiguration(settingsForTestClient.getReferenceSettings().getFileExchangeSettings());
         if (useEmbeddedHttpServer() && server == null) { // Note that the embedded server isn't fully functional yet
             server = new EmbeddedHttpServer();
             server.start();
         }
         // The commented line below is meant to separate different test runs into separate folders on the server. 
         // The current challenge is how to do this programatically. MSS tried Apache JackRabbit, but this involved a 
         // lot of additional dependencies which caused inconsistencies in the dependency model (usage of incompatible 
         // SLJ4J API 1.5 and 1.6)
         //config.setHttpServerPath("/dav/" + System.getProperty("user.name") + "/");
         httpServer = new HttpServerConnector(httpServerConfiguration, testEventManager);
         httpServer.clearFiles();
     }
 
     /**
      * Shutdown the embedded http server if any. 
      * @throws Exception
      */
     protected void teardownHttpServer() {
         if (useEmbeddedHttpServer()) {
             server.stop();
         }
     }
 
     /**
      * Returns the postfix string to use when accessing user specific topics, which is the mechanism we use in the 
      * bit repository tests.
      * @return The string to postfix all topix names with.
      */
     protected String getTopicPostfix() {
         return "-" + System.getProperty("user.name");
     }
 
     protected String getComponentID() {
         return getClass().getSimpleName();
     }
 
     protected void addFixtureSetup(String setupDescription) {
         addStep(setupDescription, "");
     }
 
     protected String createDate() {
         return Long.toString(System.currentTimeMillis());
     }
 
     protected SecurityManager createSecurityManager() {
         return new DummySecurityManager();
     }
 }
