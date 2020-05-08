 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.app.testexec.batch;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jubula.app.testexec.i18n.Messages;
 import org.eclipse.jubula.autagent.AutStarter;
 import org.eclipse.jubula.autagent.AutStarter.Verbosity;
 import org.eclipse.jubula.client.cmd.AbstractCmdlineClient;
 import org.eclipse.jubula.client.cmd.JobConfiguration;
 import org.eclipse.jubula.client.cmd.controller.IClcServer;
 import org.eclipse.jubula.client.cmd.controller.intern.RmiBase;
 import org.eclipse.jubula.client.core.ClientTest;
 import org.eclipse.jubula.client.core.IClientTest;
 import org.eclipse.jubula.client.core.agent.AutAgentRegistration;
 import org.eclipse.jubula.client.core.agent.AutRegistrationEvent;
 import org.eclipse.jubula.client.core.agent.AutRegistrationEvent.RegistrationStatus;
 import org.eclipse.jubula.client.core.agent.IAutRegistrationListener;
 import org.eclipse.jubula.client.core.businessprocess.ExternalTestDataBP;
 import org.eclipse.jubula.client.core.businessprocess.ITestExecutionEventListener;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution.PauseMode;
 import org.eclipse.jubula.client.core.businessprocess.TestExecutionEvent;
 import org.eclipse.jubula.client.core.businessprocess.compcheck.CompletenessGuard;
 import org.eclipse.jubula.client.core.businessprocess.db.TestSuiteBP;
 import org.eclipse.jubula.client.core.businessprocess.problems.IProblem;
 import org.eclipse.jubula.client.core.businessprocess.problems.ProblemFactory;
 import org.eclipse.jubula.client.core.communication.AutAgentConnection;
 import org.eclipse.jubula.client.core.communication.ConnectionException;
 import org.eclipse.jubula.client.core.events.AUTEvent;
 import org.eclipse.jubula.client.core.events.AUTServerEvent;
 import org.eclipse.jubula.client.core.events.AutAgentEvent;
 import org.eclipse.jubula.client.core.events.IAUTEventListener;
 import org.eclipse.jubula.client.core.events.IAUTServerEventListener;
 import org.eclipse.jubula.client.core.events.IServerEventListener;
 import org.eclipse.jubula.client.core.events.ServerEvent;
 import org.eclipse.jubula.client.core.model.IAUTConfigPO;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IExecStackModificationListener;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.model.ReentryProperty;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.core.persistence.ProjectPM;
 import org.eclipse.jubula.client.core.utils.AbstractNonPostOperatingTreeNodeOperation;
 import org.eclipse.jubula.client.core.utils.ITreeTraverserContext;
 import org.eclipse.jubula.client.core.utils.TreeTraverser;
 import org.eclipse.jubula.toolkit.common.exception.ToolkitPluginException;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.CommunicationException;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.registration.AutIdentifier;
 import org.eclipse.jubula.tools.utils.FileUtils;
 import org.eclipse.jubula.tools.utils.NetUtil;
 import org.eclipse.jubula.tools.utils.TimeUtil;
 import org.eclipse.osgi.util.NLS;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * This controller offers methods to create batch jobs from file
  * and to execute jobs.
  *
  * @author BREDEX GmbH
  * @created Mar 29, 2006
  */
 public class ExecutionController implements IAUTServerEventListener,
         IServerEventListener, IAUTEventListener, ITestExecutionEventListener, 
         IAutRegistrationListener {
     /**
      * @author BREDEX GmbH
      * @created Oct 13, 2010
      */
     private class ClcService implements IClcServer {
 
         /** indicates if a Test Suite is running */
         private boolean m_tsRunning;
         /** result for caller */
         private int m_result;
 
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         public int runTestSuite(String tsName, int timeout, 
                 Map<String, String> variables) {
             m_tsRunning = false;
             m_stopProcessing = false; // bugfix for ticket #3501: 1+n test
                                       // execution is otherwise not correctly
                                       // synchronized for the clcserver
             setNoErrorWhileExecution(true); // bugfix for ticket #3501: m_result
                                             // is otherwise not resettet
             WatchdogTimer timer = null;
             if (timeout > 0) {
                 timer = new WatchdogTimer(timeout);
             }
             m_result = 0;
             IProjectPO project = m_job.getProject();
             ITestSuitePO workUnit = null;
             for (ITestSuitePO ts : TestSuiteBP.getListOfTestSuites(project)) {
                 if (ts.getName().equals(tsName)) {
                     workUnit = ts;
                     break;
                 }
             }
             if (workUnit == null) {
                 m_result = -1;
             } else {
                 ClientTest.instance().startTestSuite(
                         workUnit,
                         m_job.getLanguage(),
                         m_startedAutId,
                         m_job.isAutoScreenshot(),
                         variables);
                 m_tsRunning = true;
                 timer.start();
             }
             while (!m_stopProcessing && m_tsRunning) {
                 synchronized (m_rmiBase) {
                     try {
                         m_rmiBase.wait();
                     } catch (InterruptedException e) {
                         // just check
                     }
                 }
             }
             if (timer != null) {
                 timer.abort();
             }
             return m_result;
         }
         
         /**
          * Notify the client that the TS has completed
          * @param result result of the TS run
          */
         @SuppressWarnings("synthetic-access")
         public void tsDone(int result) {
             m_result = result;
             m_tsRunning = false;
             if (m_rmiBase != null) {
                 synchronized (m_rmiBase) {
                     m_rmiBase.notifyAll();
                 }
             }
         }
 
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         public void shutdown() {
             m_clientActive = false;
             stopProcessing();
         }
 
     }
 
     /**
      * @author BREDEX GmbH
      * @created Oct 16, 2009
      */
     private final class WatchdogTimer extends Thread {
 
         /** when should the run be finished? */
         private long m_stoptime;
         
         /** should the time stop */
         private boolean m_abort = false;
         
         /**
          * @param timeout Time in seconds the watchdog should wait before
          * aborting the run.
          */
         public WatchdogTimer(int timeout) {
             super(Messages.WatchdogTimer);
             setDaemon(true);
             m_stoptime = new Date().getTime();
             m_stoptime += timeout * 1000;
         }
 
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         @Override
         public void run() {
             do {
                 TimeUtil.delay(1000);
                 if (m_abort) {
                     return;
                 }
             } while (new Date().getTime() < m_stoptime);
 
             sysErr(Messages.ExecutionControllerAbort);
 
             ClientTest.instance().stopTestExecution();
             stopProcessing();
             
             // wait 30 seconds, then exit the whole program
             TimeUtil.delay(30000);
             if (!m_abort) {
                 System.exit(1);
             }
         }
 
         /**
          * abort this watchdog
          */
         public void abort() {
             m_abort = true;
             this.interrupt();
         }
     }
 
     /**
      * @author BREDEX GmbH
      */
     private final class CollectAllErrorsOperation 
         extends AbstractNonPostOperatingTreeNodeOperation<INodePO> {
         /** the errors to show */
         private Set<IProblem> m_errorsToShow = new HashSet<IProblem>();
 
         /** {@inheritDoc} */
         public boolean operate(ITreeTraverserContext<INodePO> ctx,
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             if (ProblemFactory.hasProblem(node)) {
                 for (IProblem problem : node.getProblems()) {
                     if (problem.getStatus().getSeverity() == IStatus.ERROR) {
                         getErrorsToShow().add(problem);
                     }
                 }
             }
             return node.isActive();
         }
         
         /** @return the m_errorsToShow */
         public Set<IProblem> getErrorsToShow() {
             return m_errorsToShow;
         }
     }
     
     /** 
      * Name of the environment variable that defines the time the client should
      * wait during AUT startup process.
      */
     private static final String AUT_STARTUP_DELAY_VAR = "TEST_AUT_STARTUP_DELAY"; //$NON-NLS-1$
     
     /**
      * default time to wait during startup process 
      */
     private static final int AUT_STARTUP_DELAY_DEFAULT = 5000;
     
     /** the logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(ExecutionController.class);
     
     /** instance of controller */
     private static ExecutionController instance;
 
     /** configuration of desired job */
     private JobConfiguration m_job;
     
     /** true if client is processing a job */
     private boolean m_idle = false;
 
     /** true if ITE is currently executing a test suite */
     private boolean m_isTestSuiteRunning = false;
     
     /** true if this is the first time the AUT is being started for the current test suite */
     private boolean m_isFirstAutStart = true;
     
     /** true if test should be reported as successful */
     private boolean m_noErrorWhileExecution = true;
     
     /** true if client sends a shutdown command to end test execution */
     private boolean m_shutdown = false;
 
     /** 
      * true if fatal error occurred, and processing of the batch process must
      * be stopped
      */
     private boolean m_stopProcessing = false;
     
     /** process for watching test execution */
     private TestExecutionWatcher m_progress = new TestExecutionWatcher();
 
     /** the ID of the AUT that was started for test execution */
     private AutIdentifier m_startedAutId = null;
     
     /** the RMI service registry */
     private RmiBase m_rmiBase;
     /** the implementation of the service for internal use */
     @SuppressWarnings("synthetic-access")
     private final ClcService m_clcServiceImpl = new ClcService();
     /** is there a CLC client connection */
     private boolean m_clientActive = false;
 
     /** private constructor */
     private ExecutionController() {
         IClientTest clientTest = ClientTest.instance();
         clientTest.addAUTServerEventListener(this);
         clientTest.addAutAgentEventListener(this);
         clientTest.addTestEventListener(this);
         clientTest.addTestExecutionEventListener(this);
         AutAgentRegistration.getInstance().addListener(this);
     }
     
     /**
      * Method to get the single instance of this class.
      * @return the instance of this Singleton
      */
     public static ExecutionController getInstance() {
         if (instance == null) {
             instance = new ExecutionController();
         }
         return instance;
     }
     
     /**
      * creates the job passed to command Line client
      * 
      * @param configFile
      *            File
      * @throws IOException
      *             Error
      * @return a job configuration
      */
     public JobConfiguration initJob(File configFile) throws IOException {
         if (configFile != null) {
             // Create JobConfiguration from XMl
             BufferedReader in = null;
             StringWriter writer = new StringWriter();
             try {
                 in = new BufferedReader(new FileReader(configFile));
                 String line = null;
                 while ((line = in.readLine()) != null) {
                     writer.write(line);
                 }
             } finally {
                 if (in != null) {
                     in.close();
                 }
             }
             String xml = writer.toString();    
             m_job = JobConfiguration.readFromXML(xml);
         } else {
             // or create an empty JobConfiguration
             m_job = new JobConfiguration();
         }
         return m_job;
     }
     
     /**
      * only loads projects and do a completeness check
      */
     public void simulateJob() {
         prepareExecution();
     }
     
     /**
      * executes the complete test
      * @throws CommunicationException Error
      * @return boolean true if all testsuites completed successfully
      */
     public boolean executeJob() throws CommunicationException {
         // start the watchdog timer
         WatchdogTimer timer = null;
         if (m_job.getTimeout() > 0) {        
             timer = new WatchdogTimer(m_job.getTimeout());
             timer.start();
         }
         int autAgentPortNumber = m_job.getPort();
         if (StringUtils.isEmpty(m_job.getServer())) {
             // if "port" parameter for testexec was not given (in command line and/or in configuration file) 
             // port number equals 0 by default and any free port should be used for embedded AUT Agent in this case
             if (autAgentPortNumber == 0) {
                 autAgentPortNumber = NetUtil.getFreePort();
             }
             //the "server" parameter (autAgentHostName) for testexec is set to "localhost"
             m_job.setEmbeddedAutAgentHostName();
             if (!startEmbeddedAutAgent(autAgentPortNumber)) {
                 endTestExecution();
                 return false;
             } 
         } 
         String autAgentHostName = m_job.getServer();
         sysOut(NLS.bind(Messages.ConnectingToAUTAgent,
             new Object[] { autAgentHostName, autAgentPortNumber }));
         // init ClientTestImpl
         IClientTest clientTest = ClientTest.instance();
         clientTest.connectToAutAgent(autAgentHostName, 
                 String.valueOf(autAgentPortNumber));
         if (!AutAgentConnection.getInstance().isConnected()) {
             throw new CommunicationException(
                     Messages.ConnectionToAUTAgentFailed,
                     MessageIDs.E_COMMUNICATOR_CONNECTION);
         }
         clientTest.setRelevantFlag(m_job.isRelevant());
         clientTest.setScreenshotXMLFlag(m_job.isXMLScreenshot());
         prepareExecution();
         // start AUT, working will be set false, after AUT started
         m_idle = true;
         // ends testexecution if shutdown command was received from the client
         if (m_shutdown) {
             sysOut(Messages.ReceivedShutdownCommand);
             endTestExecution();
         }
         try {
             
             if (m_rmiBase != null) { // run as a CLS server
                 doClcService();
             } else if (m_job.getTestJob() != null) {
                 ensureAutIsStarted(m_job.getActualTestSuite(), 
                         m_job.getAutConfig());
                 doTestJob();
             } else {
                 ensureAutIsStarted(m_job.getActualTestSuite(), 
                         m_job.getAutConfig());
                 doTestSuite();
             }
         } catch (ToolkitPluginException e1) {
             sysErr(NLS.bind(Messages.ExecutionControllerAUT,
                 Messages.ErrorMessageAUT_TOOLKIT_NOT_AVAILABLE));
         }
         if (timer != null) {
             timer.abort();
         }
         
         return isNoErrorWhileExecution();
     }
 
     /**
      * starts the embedded AUT Agent
      * @param port the port number of the embedded AUT Agent
      * @throws CommunicationException in case of failure to connect to embedded AUT Agent
      * @return true if embedded Agent was started successfully and false otherwise
      */
     private boolean startEmbeddedAutAgent(int port)
         throws CommunicationException {
         AutStarter autAgentInstance = AutStarter.getInstance();
         if (autAgentInstance.getCommunicator() == null) {
             // Embedded Agent is not running. We need to start it before
             // trying to connect to it.
             try {
                sysOut(I18n.getString("AUTAgent.EmbeddedAUTAgentStart",  //$NON-NLS-1$
                        new String[] {String.valueOf(port)}));
                 autAgentInstance.start(
                         port, false, Verbosity.QUIET, false);
                 return true;
             } catch (Exception e) {
                 LOG.error(e.getLocalizedMessage(), e);
                 return false;
             }
         }
         return false;
     }
 
     /**
      * execute a test suite
      */
     private void doTestSuite() {
         // executing batch of test suites
         while (m_job.getActualTestSuite() != null 
                 && !m_stopProcessing) {
             while (m_idle && !m_stopProcessing) {
                 TimeUtil.delay(50);
             }
             if (m_job.getActualTestSuite() != null 
                     && !m_stopProcessing
                     && !m_idle && !m_isFirstAutStart) {
    
                 m_idle = true;
                 sysOut(StringConstants.TAB
                     + NLS.bind(Messages.ExecutionControllerTestSuiteBegin,
                         m_job.getActualTestSuite().getName()));
                 ClientTest.instance().startTestSuite(
                     m_job.getActualTestSuite(),
                     m_job.getLanguage(),
                     m_startedAutId != null ? m_startedAutId : m_job
                         .getAutId(), m_job.isAutoScreenshot(), null);
             } 
         }
     }
 
     /**
      * run a test job
      */
     private void doTestJob() {
         sysOut(StringConstants.TAB
                 + NLS.bind(Messages.ExecutionControllerTestJobBegin, m_job
                         .getTestJob().getName()));
         ClientTest.instance().startTestJob(
                 m_job.getTestJob(), m_job.getLanguage(),
                 m_job.isAutoScreenshot());
     }
 
     /**
      * wait for the CLC service to receive a shutdown from the client
      */
     private void doClcService() throws ToolkitPluginException {
         IProjectPO project = m_job.getProject();
         String autConfigName = m_job.getAutConfigName();
         
         ITestSuitePO workUnit = null;
         IAUTConfigPO autConfig = null;
         
         for (ITestSuitePO ts : TestSuiteBP.getListOfTestSuites(project)) {
             for (IAUTConfigPO cfg : ts.getAut().getAutConfigSet()) {
                 if (autConfigName.equals(cfg.getName())) {
                     workUnit = ts;
                     autConfig = cfg;
                     break;
                 }
             }
         }
         if (workUnit != null && autConfig != null) {
             ensureAutIsStarted(workUnit, autConfig);
         }
         m_clientActive = true;
         do {
             synchronized (m_rmiBase) {
                 try {
                     m_rmiBase.wait();
                 } catch (InterruptedException e) {
                     // just check if we should stop
                 }
             }
         } while (m_clientActive);
         if (autConfig != null) {
             try {
                 AutIdentifier startedAutId = new AutIdentifier(
                         autConfig.getConfigMap().get(
                                 AutConfigConstants.AUT_ID));
                 if (AutAgentConnection.getInstance().isConnected()) {
                     ClientTest.instance().stopAut(startedAutId);
                 }
             } catch (ConnectionException e) {
                 LOG.info(Messages.ErrorWhileStoppingAUT, e);
             }
         }
 
     }
         
     /**
      * end processing and notify any waiting CLC service threads
      */
     private void stopProcessing() {
         m_stopProcessing = true;
         if (m_rmiBase != null) {
             synchronized (m_rmiBase) {
                 m_rmiBase.notifyAll();
             }
         }
     }
     /**
      * prepares the test execution by:
      *   <p> * initializing database connection
      *   <p> * loading the project
      *   <p> * validating project
      */
     private void prepareExecution() {
         // setting LogDir , resource/html must be in classpath
         setLogDir();
         // set data dir for external data
         ExternalTestDataBP.setDataDir(new File(m_job.getDataDir()));
         // init Persistor
         // Persistence (JPA / EclipseLink).properties and mapping files
         // have to be in classpath
         Persistor.setDbConnectionName(m_job.getDbscheme());
         Persistor.setUser(m_job.getDbuser());
         Persistor.setPw(m_job.getDbpw());
         Persistor.setUrl(m_job.getDb());
         if (!Persistor.init()) {
             throw new IllegalArgumentException(Messages.
                     ExecutionControllerInvalidDBDataError, null);
         }
         // load project
         loadProject();
     }
 
     /**
      * sets the log Directory
      */
     private void setLogDir() {
         if (StringUtils.isNotEmpty(m_job.getResultDir())) {
             Validate.isTrue(FileUtils.isValidPath(m_job.getResultDir()),
                     Messages.ExecutionControllerLogPathError);
             ClientTest.instance().setLogPath(m_job.getResultDir());
         }
     }
 
     /**
      * starts the AUT
      * @param ts the Test Suite which will be started
      * @param autConf configuration for this AUT
      */
     private void ensureAutIsStarted(ITestSuitePO ts, IAUTConfigPO autConf) 
         throws ToolkitPluginException {
         if (ts != null && autConf != null) {
             final IAUTMainPO aut = ts.getAut();
             
             if (ts != null) {
                 AutIdentifier autToStart = new AutIdentifier(autConf
                         .getConfigMap().get(AutConfigConstants.AUT_ID));
                 AUTStartListener asl = new AUTStartListener(autToStart);
                 IClientTest clientTest = ClientTest.instance();
                 clientTest.addTestEventListener(asl);
                 clientTest.addAUTServerEventListener(asl);
                 AutAgentRegistration.getInstance().addListener(asl);
                 sysOut(NLS.bind(Messages.ExecutionControllerAUT,
                         NLS.bind(Messages.ExecutionControllerAUTStart,
                                 aut.getName(), autConf.getName())));
                 clientTest.startAut(aut, autConf, m_job.getLanguage());
                 m_startedAutId = autToStart;
                 while (!asl.autStarted() && !asl.hasAutStartFailed()) {
                     TimeUtil.delay(500);
                 }
                 waitExternalTime();
             }
         } else {
             // assume that the AUT has already been started via e.g. autrun
             m_idle = false;
             m_isFirstAutStart = false;
         }
     }
 
     /**
      * this method delays the test execution start during AUT startup
      */
     private void waitExternalTime() {
         TimeUtil.delayDefaultOrExternalTime(AUT_STARTUP_DELAY_DEFAULT,
                 AUT_STARTUP_DELAY_VAR);
     }
 
     /**
      * @author BREDEX GmbH
      * @created 24.08.2009
      */
     public class AUTStartListener implements IAUTEventListener, 
         IAUTServerEventListener, IAutRegistrationListener {
         /** flag to indicate that the AUT has been successfully started */
         private boolean m_autStarted = false;
         
         /** flag to indicate that the AUT start has failed*/
         private boolean m_autStartFailed = false;
         
         /** timer to set autStartFailed to true after a certain amount of time */
         private Timer m_startFailedTimer = new Timer();
         
         /** startup timeout: 5 minutes */
         private long m_autStartTimeout = 5 * 60 * 1000;
 
         /** ID of the AUT that should be started */
         private AutIdentifier m_autToStart;
         
         /** 
          * Constructor
          * 
          * @param autToStart ID of the AUT that should be started.
          */
         public AUTStartListener(AutIdentifier autToStart) {
             m_autToStart = autToStart;
             m_startFailedTimer.schedule(new TimerTask() {
                 public void run() {
                     setAutStartFailed(true);
                     removeListener();
                 }
             }, m_autStartTimeout);
         }
         
         /** @return the hasBeenNotified */
         public synchronized boolean autStarted() {
             return m_autStarted;
         }
 
         /** {@inheritDoc} */
         public synchronized void stateChanged(AUTEvent event) {
             switch (event.getState()) {
                 case AUTEvent.AUT_STARTED:
                     m_autStarted = true;
                     dispose();
                     break;
                 default:
                     break;
             }
         }
 
         /** @return the autStartFailed */
         public synchronized boolean hasAutStartFailed() {
             return m_autStartFailed;
         }
 
         /** {@inheritDoc} */
         public void stateChanged(AUTServerEvent event) {
             switch (event.getState()) {
                 case AUTServerEvent.COMMUNICATION:
                 case AUTServerEvent.COULD_NOT_ACCEPTING:
                 case AUTServerEvent.DOTNET_INSTALL_INVALID:
                 case AUTServerEvent.INVALID_JAR:
                 case AUTServerEvent.INVALID_JAVA:
                 case AUTServerEvent.JDK_INVALID:
                 case AUTServerEvent.NO_MAIN_IN_JAR:
                 case AUTServerEvent.SERVER_NOT_INSTANTIATED:
                 case ServerEvent.CONNECTION_CLOSED:
                     setAutStartFailed(true);
                     dispose();
                     break;
                 default:
                     break;
             }
         }
 
         /**
          * @param autStartFailed the autStartFailed to set
          */
         protected synchronized void setAutStartFailed(boolean autStartFailed) {
             m_autStartFailed = autStartFailed;
         }
         
         /** dispose this listener and stop all running tasks */
         private void dispose () {
             m_startFailedTimer.cancel();
             removeListener();
         }
         
         /** remove listener */
         protected void removeListener() {
             IClientTest clientTest = ClientTest.instance();
             clientTest.removeTestEventListener(this);
             clientTest.removeAUTServerEventListener(this);
             AutAgentRegistration.getInstance().removeListener(this);
         }
 
         /**
          * {@inheritDoc}
          */
         public void handleAutRegistration(AutRegistrationEvent event) {
             if (event.getAutId().equals(m_autToStart)
                     && event.getStatus() == RegistrationStatus.Register) {
                 m_autStarted = true;
                 dispose();
             }
         }
     }
     
     /**
      * loads a project
      */
     private void loadProject() {
         sysOut(Messages.ExecutionControllerDatabase
                 + NLS.bind(Messages.ExecutionControllerLoadingProject,
                     new Object[] { m_job.getProjectName(),
                                    m_job.getProjectMajor(),
                                    m_job.getProjectMinor() }));
         try {
             IProjectPO actualProject = 
                 ProjectPM.loadProjectByNameAndVersion(m_job.getProjectName(), 
                     m_job.getProjectMajor(), m_job.getProjectMinor());
             if (actualProject != null) {
                 ProjectPM.loadProjectInROSession(actualProject);
                 final IProjectPO currentProject = GeneralStorage.getInstance()
                     .getProject();
                 m_job.setProject(currentProject);
                 sysOut(Messages.ExecutionControllerDatabase
                     + NLS.bind(Messages.ExecutionControllerProjectLoaded,
                             m_job.getProjectName()));
             }
         } catch (JBException e1) {
             /* An exception was thrown while loading data or closing a session
              * using Persistence (JPA / EclipseLink). The project is never set. This is detected
              * during job validation (initAndValidate). */
         }
         
         sysOut(Messages.ExecutionControllerProjectCompleteness);
         m_job.initAndValidate();
         boolean noErrors = true;
         for (ITestSuitePO ts : m_job.getTestSuites()) {
             CompletenessGuard.checkAll(m_job.getLanguage(), ts);
             sysOut(NLS.bind(Messages.ExecutionControllerTestSuiteCompleteness,
                     ts.getName()));
             final CollectAllErrorsOperation op = 
                     new CollectAllErrorsOperation();
             TreeTraverser traverser = new TreeTraverser(ts, op);
             traverser.traverse(true); 
             for (IProblem problem : op.getErrorsToShow()) {
                 if (problem.hasUserMessage()) {
                     sysOut(problem.getUserMessage());
                 }
                 noErrors = false;
             }
         }
         Validate.isTrue(noErrors, 
                 Messages.ExecutionControllerProjectCompletenessFailed);
     }
 
     /**
      * {@inheritDoc}
      */
     public void stateChanged(AUTServerEvent event) {
         switch (event.getState()) {
             case AUTServerEvent.INVALID_JAR:
                 sysErr(Messages.ExecutionControllerInvalidJarError);
                 stopProcessing();
                 m_idle = false;
                 break;
             case AUTServerEvent.INVALID_JAVA:
                 sysErr(Messages.ExecutionControllerInvalidJREError);
                 stopProcessing();
                 m_idle = false;
                 break;
             case AUTServerEvent.SERVER_NOT_INSTANTIATED:
                 sysErr(Messages.ExecutionControllerServerNotInstantiated);
                 stopProcessing();
                 m_idle = false;
                 break;
             case AUTServerEvent.NO_MAIN_IN_JAR:
                 sysErr(Messages.ExecutionControllerInvalidMainError);
                 stopProcessing();
                 m_idle = false;
                 break;
             case AUTServerEvent.COULD_NOT_ACCEPTING:
                 sysErr(Messages.ExecutionControllerAUTStartError);
                 stopProcessing();
                 m_idle = false;
                 break;
             case ServerEvent.CONNECTION_CLOSED:
                 stopProcessing();
                 m_idle = false;
                 break;
             case AUTServerEvent.DOTNET_INSTALL_INVALID:
                 sysErr(Messages.ExecutionControllerDotNetInstallProblem);
                 stopProcessing();
                 m_idle = false;
                 break;
                 
             default:
                 break;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void stateChanged(AutAgentEvent event) {
         sysOut(NLS.bind(Messages.ExecutionControllerServer, event));
         switch (event.getState()) {
             case ServerEvent.CONNECTION_CLOSED:
                 break;
             default:
                 break;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void stateChanged(AUTEvent event) {
         switch (event.getState()) {
             case AUTEvent.AUT_STARTED:
                 sysOut(NLS.bind(Messages.ExecutionControllerAUT,
                     Messages.ExecutionControllerAUTConnectionEstablished));
                 break;
             case AUTEvent.AUT_CLASS_VERSION_ERROR:
             case AUTEvent.AUT_MAIN_NOT_FOUND:
             case AUTEvent.AUT_NOT_FOUND:
             case AUTEvent.AUT_ABORTED:
             case AUTEvent.AUT_START_FAILED:
                 sysErr(Messages.ExecutionControllerAUTStartError);
                 stopProcessing();
                 break;
             case AUTEvent.AUT_STOPPED:
                 if (m_isTestSuiteRunning) {
                     sysErr(Messages.ExecutionControllerAUTConnectionLost);
                     ClientTest.instance().stopTestExecution();
                 } else {
                     sysOut(NLS.bind(Messages.ExecutionControllerAUT,
                         Messages.ExecutionControllerAUTDisconnected));
                 }
                 stopProcessing();
                 break;
             case AUTEvent.AUT_RESTARTED:
                 return;
             default:
                 break;
         }
         // generally do not do this, if AUT-Restart-Action is executed!
         if (m_isFirstAutStart) {
             m_idle = false;
             m_isFirstAutStart = false;
         }
 
     }
 
     /**
      * Writes an output to console
      * 
      * @param text
      *            the text to write
      */
     private void sysOut(String text) {
         AbstractCmdlineClient.printConsoleLn(text, true);
     }
     
     /**
      * Writes an output to console
      * 
      * @param text
      *            the message to log and println to sys.err
      */
     private void sysErr(String text) {
         AbstractCmdlineClient.printlnConsoleError(text);
     }
     
     /**
      * {@inheritDoc}
      */
     public void stateChanged(TestExecutionEvent event) {
         Exception exception = event.getException();
         if (exception instanceof JBException) {
             String errorMsg = I18n.getString(exception.getMessage(), true);
             sysErr(errorMsg);
             stopProcessing();
         }
 
         switch (event.getState()) {
             case TEST_EXEC_RESULT_TREE_READY:
                 TestExecution.getInstance().getTrav()
                     .addExecStackModificationListener(m_progress);
                 break;
             case TEST_EXEC_START:
             case TEST_EXEC_RESTART:
                 m_isTestSuiteRunning = true;
                 break;
             case TEST_EXEC_FINISHED:
                 sysOut(Messages.ExecutionControllerTestSuiteEnd);
                 m_job.getNextTestSuite();
                 m_clcServiceImpl.tsDone(isNoErrorWhileExecution() ? 0 : 1);
                 m_isTestSuiteRunning = false;
                 break;
             case TEST_EXEC_PAUSED:
                 TestExecution.getInstance().pauseExecution(PauseMode.UNPAUSE);
                 break;
             case TEST_EXEC_ERROR:
             case TEST_EXEC_FAILED:
             case TEST_EXEC_STOP:
                 m_job.getNextTestSuite();
                 m_isTestSuiteRunning = false;
                 break;
             default:
                 break;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void endTestExecution() {
         m_idle = false;
     }
 
     /**
      * @author BREDEX GmbH
      * @created Dec 3, 2010
      */
     protected class TestExecutionWatcher 
             implements IExecStackModificationListener {
 
         /**
          * {@inheritDoc}
          */
         public void stackIncremented(INodePO node) {
             String nodeType = StringConstants.EMPTY;
             if (node instanceof IEventExecTestCasePO) {
                 IEventExecTestCasePO evPo = (IEventExecTestCasePO)node;
                 if (evPo.getReentryProp() != ReentryProperty.RETRY) {
                     setNoErrorWhileExecution(false);
                 }
                 nodeType = Messages.EventHandler;
             } else if (node instanceof ITestSuitePO) {
                 nodeType = Messages.TestSuite;
             } else if (node instanceof IExecTestCasePO) {
                 nodeType = Messages.TestCase;
             }
             StringBuilder sb = new StringBuilder(nodeType);
             sb.append(StringConstants.COLON);
             sb.append(StringConstants.SPACE);
             sb.append(String.valueOf(node.getName()));
             sysOut(sb.toString());
         }
 
         /**
          * {@inheritDoc}
          */
         public void stackDecremented() {
             // do nothing
         }
 
         /**
          * {@inheritDoc}
          */
         public void nextDataSetIteration() {
             // do nothing
         }
 
         /**
          * {@inheritDoc}
          */
         public void nextCap(ICapPO cap) {
             sysOut(StringConstants.TAB
                     + Messages.Step 
                     + StringConstants.COLON
                     + StringConstants.SPACE
                     + String.valueOf(cap.getName()));
         }
 
         /**
          * {@inheritDoc}
          */
         public void retryCap(ICapPO cap) {
             sysOut(StringConstants.TAB
                     + Messages.RetryStep 
                     + StringConstants.COLON
                     + StringConstants.SPACE
                     + String.valueOf(cap.getName()));
         }
     }
 
     /**
      * @param job the job to set
      */
     public void setJob(JobConfiguration job) {
         m_job = job;
         if (m_job.getServerPort() != null) {
             Integer port = Integer.parseInt(m_job.getServerPort());
             m_rmiBase = new RmiBase(port, m_clcServiceImpl);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void handleAutRegistration(AutRegistrationEvent event) {
         if ((event.getAutId().equals(m_startedAutId)
                 || event.getAutId().equals(m_job.getAutId()))
                 && event.getStatus() == RegistrationStatus.Register) {
             // generally do not do this, if AUT-Restart-Action is executed!
             if (m_isFirstAutStart) {
                 m_idle = false;
                 m_isFirstAutStart = false;
             }
         }
     }
 
     /**
      * @param noErrorWhileExecution the noErrorWhileExecution to set
      */
     protected void setNoErrorWhileExecution(boolean noErrorWhileExecution) {
         m_noErrorWhileExecution = noErrorWhileExecution;
     }
 
     /**
      * @return the noErrorWhileExecution
      */
     protected boolean isNoErrorWhileExecution() {
         return m_noErrorWhileExecution;
     }
 }
