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
 package org.eclipse.jubula.autagent;
 
 import java.awt.EventQueue;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.OptionGroup;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.jubula.autagent.agent.AutAgent;
 import org.eclipse.jubula.autagent.desktop.DesktopIntegration;
 import org.eclipse.jubula.autagent.remote.dialogs.ChooseCheckModeDialogBP;
 import org.eclipse.jubula.autagent.remote.dialogs.ObservationConsoleBP;
 import org.eclipse.jubula.communication.Communicator;
 import org.eclipse.jubula.communication.IConnectionInitializer;
 import org.eclipse.jubula.communication.connection.ConnectionState;
 import org.eclipse.jubula.communication.listener.ICommunicationErrorListener;
 import org.eclipse.jubula.communication.message.AutRegisteredMessage;
 import org.eclipse.jubula.communication.message.Message;
 import org.eclipse.jubula.communication.message.StartAUTServerStateMessage;
 import org.eclipse.jubula.tools.constants.AUTServerExitConstants;
 import org.eclipse.jubula.tools.constants.ConfigurationConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.CommunicationException;
 import org.eclipse.jubula.tools.exception.JBVersionException;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.registration.AutIdentifier;
 import org.eclipse.jubula.tools.utils.DevNull;
 import org.eclipse.jubula.tools.utils.EnvironmentUtils;
 
 
 /**
  * The AutStarter for starting, watching and stopping the AUTServer.
  *
  * ExitCodes:
  * <ul>
  * <li>-1: invalid command line options</li>
  * <li>0: option -h(elp)</li>
  * <li>1: security violation when trying accepting connections</li>
  * <li>2: io exception when trying accepting connections</li>
  * <li>3: configuration error</li>
  * </ul>
  *
  * @author BREDEX GmbH
  * @created 26.07.2004
  *
  */
 public class AutStarter {
     /** constant for timeout when sending command to shutdown AUT Agent */
     private static final int TIMEOUT_SEND_STOP_CMD = 10000;
 
     /**
      * <code>COMMANDLINE_OPTION_STOP</code>
      */
     private static final String COMMANDLINE_OPTION_STOP = "stop"; //$NON-NLS-1$
 
     /** the logger */
     private static Log log = LogFactory.getLog(AutStarter.class);
 
     /** exit code in case of invalid options */
     private static final int EXIT_INVALID_OPTIONS = -1;
 
     /** exit code in case of option -h(elp) */
     private static final int EXIT_HELP_OPTION = 0;
 
     /** exit code in case of a security exception */
     private static final int EXIT_SECURITY_VIOLATION = 1;
 
     /** exit code in case of an io exception */
     private static final int EXIT_IO_EXCEPTION = 2;
 
     /** exit code in case of an configuration error */
     private static final int EXIT_CONFIGURATION_ERROR = 3;
 
     /** exit code in case of a version error between Client and AutStarter */
     private static final int EXIT_CLIENT_SERVER_VERSION_ERROR = 4;
 
     /** the instance */
     private static AutStarter instance = null;
 
     /** the command line */
     private CommandLine m_cmd = null;
 
     /** the communicator to use */
     private Communicator m_communicator;
 
     /** the communicator to use to communicate with AUTServer*/
     private Communicator m_autCommunicator;
 
     /**
      * the timeout for killing the autServerVM when the connection to the
      * JubulaClient was closed, defaults to 10 seconds
      */
     private int m_stopAUTServerTimeout = 10000;
 
     /** abort the current startup? */
     private volatile boolean m_abortRequested;
 
     /** the AUT Agent that is used for AUT registration and deregistration */
     private AutAgent m_agent;
     
     /** sends messages using the Agent's communicator(s) */
     private CommunicationHelper m_messenger;
     
     /**
      * private constructor
      */
     private AutStarter() {
         super();
 
         AutAgent agent = new AutAgent();
         m_messenger = new CommunicationHelper();
         // AUT Registration listener. Sends registration information to 
         // connected client.
         agent.addPropertyChangeListener(
                 AutAgent.PROP_NAME_AUTS, new PropertyChangeListener() {
 
                     public void propertyChange(PropertyChangeEvent evt) {
                         Communicator clientComm = 
                             AutStarter.getInstance().getCommunicator();
                         if (clientComm == null 
                                 || clientComm.getConnection() == null) {
                             // No connection. Do nothing.
                             return;
                         }
                         try {
                             Object newValue = evt.getNewValue();
                             if (newValue instanceof AutIdentifier) {
                                 clientComm.send(new AutRegisteredMessage(
                                         (AutIdentifier)newValue, true));
                             }
 
                             Object oldValue = evt.getOldValue();
                             if (oldValue instanceof AutIdentifier) {
                                 clientComm.send(new AutRegisteredMessage(
                                         (AutIdentifier)oldValue, false));
                             }
                         } catch (CommunicationException ce) {
                             log.error("Error occurred while sending AUT Registration Message.", ce); //$NON-NLS-1$
                         }
                     }
 
                 });
 
         m_agent = agent;
 
     }
 
     /**
      * method to create an options object, filled with all options
      *
      * @return the options
      */
     @SuppressWarnings("nls")
     private static Options createOptions() {
         Options options = new Options();
 
         Option portOption = new Option("p", true, "the port to listen to");
         portOption.setArgName("port");
         options.addOption(portOption);
         
         options.addOption("l", false, "lenient mode; does not shutdown AUTs " 
                 + "that try to register themselves using an already " 
                 + "registered AUT ID");
         options.addOption("h", false, "prints this help text and exits");
 
         OptionGroup verbosityOptions = new OptionGroup();
         verbosityOptions.addOption(new Option("q", false, "quiet mode"));
         verbosityOptions.addOption(new Option("v", false, "verbose mode"));
         options.addOptionGroup(verbosityOptions);
 
         OptionGroup startStopOptions = new OptionGroup();
         startStopOptions.addOption(new Option("start", false,
                 "startup mode"));
         
         OptionBuilder.hasOptionalArg();
         Option stopOption = OptionBuilder.create(COMMANDLINE_OPTION_STOP);
         stopOption.setDescription("stops a running AUT Agent instance "
                         + "for the given port "
                         + "on the given hostname (default \"localhost\")");
         stopOption.setArgName("hostname");
         startStopOptions.addOption(stopOption);
         options.addOptionGroup(startStopOptions);
         
         return options;
     }
 
     /**
      * main method
      *
      * @param args -
      *            the args, parsed with a PosixParser
      */
     public static void main(String[] args) {
         // create the single instance here
         final AutStarter server = AutStarter.getInstance();
 
         CommandLineParser parser = new PosixParser();
         try {
             server.setCmd(parser.parse(createOptions(), args));
         } catch (ParseException pe) {
             String message = "invalid option: "; //$NON-NLS-1$
             log.error(message, pe);
             server.printHelp();
             System.exit(EXIT_INVALID_OPTIONS);
         }
 
         server.start();
     }
 
     /**
      * starts watching the given process <br>
      * @param process the process representing an AutServer, must not be null
      * @param isAgentSet true if executable file and agent are set.
      * @throws IllegalArgumentException if the given process is null
      * @return false when no more server can watched (it's only one),
      *         true otherwise
      */
     public boolean watchAutServer(Process process, boolean isAgentSet)
         throws IllegalArgumentException {
         // check parameter
         if (process == null) {
             throw new IllegalArgumentException(
                     "process must not be null"); //$NON-NLS-1$
         }
 
         // start thread waiting for termination
         new AUTServerWatcher(process, isAgentSet, m_messenger).start();
         return true;
     }
 
     /**
      * Method to get the single instance of this class.
      *
      * @return the instance of this Singleton
      */
     public static AutStarter getInstance() {
         if (instance == null) {
             instance = new AutStarter();
         }
         return instance;
     }
 
     /**
      * @return Returns the communicator.
      */
     public synchronized Communicator getCommunicator() {
         return m_communicator;
     }
 
     /**
      * @param communicator
      *            The communicator to set.
      */
     public synchronized void setCommunicator(Communicator communicator) {
         m_communicator = communicator;
     }
 
     /**
      * @return Returns the communicator.
      */
     public synchronized Communicator getAutCommunicator() {
         return m_autCommunicator;
     }
 
     /**
      * @param communicator
      *            The communicator to set.
      */
     public synchronized void setAutCommunicator(Communicator communicator) {
         m_autCommunicator = communicator;
     }
 
     /**
      * @return Returns the stopAUTServerTimeout.
      */
     public int getStopAUTServerTimeout() {
         return m_stopAUTServerTimeout;
     }
 
     /**
      * sets the timeout used at killing the autServerProcess.
      *
      * @param stopAUTServerTimeout
      *            The stopAUTServerTimeout to set, for negative values zero is
      *            used.
      */
     public void setStopAUTServerTimeout(int stopAUTServerTimeout) {
         if (stopAUTServerTimeout < 0) {
             m_stopAUTServerTimeout = 0;
         } else {
             m_stopAUTServerTimeout = stopAUTServerTimeout;
         }
     }
 
     /**
      * start accepting connections
      */
     public void start() {
         int exitCode = 0;
        String infoMessage = I18n.getString("GuiDancerServer.StartErrorText"); //$NON-NLS-1$
         try {
             if (m_cmd.hasOption("h")) { //$NON-NLS-1$
                 printHelp();
                 System.exit(EXIT_HELP_OPTION);
             }
             boolean killDuplicateAuts = !m_cmd.hasOption("l");
             getAgent().setKillDuplicateAuts(killDuplicateAuts);
             int port = getPortNumber();
            infoMessage = I18n.getString("GuiDancerServer.StartCommErrorText",  //$NON-NLS-1$
                     new Object[] {StringConstants.EMPTY + port});
             if (!m_cmd.hasOption(COMMANDLINE_OPTION_STOP)) {
                 initClientConnectionSocket(port);
                 initAutConnectionSocket();
                 DesktopIntegration di = new DesktopIntegration(getAgent());
                 di.setPort(port);
                 m_agent.addPropertyChangeListener(AutAgent.PROP_NAME_AUTS, di);
                 if (m_cmd.hasOption("v") && !m_cmd.hasOption("q")) { //$NON-NLS-1$ //$NON-NLS-2$
                    infoMessage = I18n.getString("GuiDancerServer.StartSuccessText") + //$NON-NLS-1$
                         getCommunicator().getLocalPort() + "."; //$NON-NLS-1$
                 } else {
                     infoMessage = StringConstants.EMPTY;
                 }
             } else {
                 String hostname = "localhost"; //$NON-NLS-1$
                 if (m_cmd.getOptionValue(COMMANDLINE_OPTION_STOP) != null) {
                     hostname = m_cmd.getOptionValue(COMMANDLINE_OPTION_STOP);
                 }
                 infoMessage = StringConstants.EMPTY;
                 try {
                     Socket commandSocket = new Socket(hostname, port);
                     InputStream inputStream = commandSocket.getInputStream();
                     BufferedReader br = new BufferedReader(
                             new InputStreamReader(inputStream));
                     ConnectionState.respondToTypeRequest(TIMEOUT_SEND_STOP_CMD,
                             br, inputStream, new PrintStream(commandSocket
                                     .getOutputStream()),
                             ConnectionState.CLIENT_TYPE_COMMAND_SHUTDOWN);
                     waitForAgentToTerminate(br);
                 } catch (ConnectException ce) {
                     System.out.println("AUT Agent not found at " + hostname + ":" + port); //$NON-NLS-1$ //$NON-NLS-2$
                 }
             }
         } catch (SecurityException se) {
             log.error("security violation", se); //$NON-NLS-1$
             exitCode = EXIT_SECURITY_VIOLATION;
         } catch (IOException ioe) {
             String message = "could not open socket: "; //$NON-NLS-1$
             log.error(message, ioe);
             exitCode = EXIT_IO_EXCEPTION;
         } catch (NumberFormatException nfe) {
             String message = "invalid value for option port"; //$NON-NLS-1$
             log.error(message, nfe);
             exitCode = EXIT_INVALID_OPTIONS;
         } catch (NullPointerException npe) {
             log.error("no command line", npe); //$NON-NLS-1$
             printHelp();
             exitCode = EXIT_INVALID_OPTIONS;
         } catch (JBVersionException ve) {
             log.error(ve.getMessage(), ve);
             exitCode = EXIT_CLIENT_SERVER_VERSION_ERROR;
         } finally {
          // print information box to user
             if (infoMessage.length() > 0) {
                 showUserInfo(infoMessage);
             }
             if (exitCode != 0) {
                 System.exit(exitCode);
             }
         }
     }
 
     /**
      * @param br
      *            the buffered reader which is used to determine whether the
      *            agent has shutdown itself
      */
     private void waitForAgentToTerminate(BufferedReader br) {
         // keep process and socket alive till agent has read the shutdown command
         boolean socketAlive = true;
         while (socketAlive) {
             try {
                 if (br.readLine() == null) {
                     socketAlive = false;
                 }
             } catch (IOException e) {
                 // ok here --> autagent has shut down itself
                 socketAlive = false;
             }
         }
     }
 
     /**
      * @return the port number
      */
     private int getPortNumber() {
         int port = ConfigurationConstants.AUT_AGENT_DEFAULT_PORT;
         if (m_cmd.hasOption("p")) { //$NON-NLS-1$
             port = Integer.valueOf(m_cmd.getOptionValue("p")).intValue(); //$NON-NLS-1$
         } else {
             String portStr = EnvironmentUtils.getProcessEnvironment()
                 .getProperty(ConfigurationConstants.AUTSTARTER_PORT);
             if ((portStr != null) && (!portStr.trim()
                     .equals(StringConstants.EMPTY))) {
                 try {
                     port = Integer.valueOf(portStr).intValue();
                 } catch (NumberFormatException nfe) {
                     log.error("Format of portnumber in Environment-Variable '" //$NON-NLS-1$
                             + ConfigurationConstants.AUTSTARTER_PORT
                             + "' is not an integer", nfe); //$NON-NLS-1$
                 }
             }
             log.info("using default port " + String.valueOf(port)); //$NON-NLS-1$
         }
         return port;
     }
 
     /**
      * initializes the Socket for the client to connect.
      * 
      * @param port
      *            int
      * @throws IOException
      *             error
      * @throws JBVersionException
      *             in case of version error between Client and AutStarter
      */
     private void initClientConnectionSocket(int port) throws IOException,
     JBVersionException {
 
         Map<String, IConnectionInitializer> clientTypeToInitializer =
             new HashMap<String, IConnectionInitializer>();
         
         
         clientTypeToInitializer.putAll(m_agent.getConnectionInitializers());
         clientTypeToInitializer.put(
                 ConnectionState.CLIENT_TYPE_COMMAND_SHUTDOWN, 
                 new IConnectionInitializer() {
                     public void initConnection(Socket socket, 
                             BufferedReader reader) {
                         System.out.println("Shutdown requested. Shutting down..."); //$NON-NLS-1$
                         System.exit(0);
                     }
                 });
         // create a communicator
         setCommunicator(
                 new Communicator(port, this.getClass().getClassLoader(), 
                 clientTypeToInitializer));
         
         getCommunicator().addCommunicationErrorListener(
                 new CommunicationListener());
         logRunning();
         // start listening
         logStartListening();
         getCommunicator().run();
     }
 
     /**
      * initializes the Socket for the AUTServer to connect
      * 
      * @throws IOException
      *             error
      * @throws JBVersionException
      *             in case of a version error between Client and AutStarter
      */
     private void initAutConnectionSocket() throws IOException,
         JBVersionException {
 
         // create a communicator on any free port
         setAutCommunicator(new Communicator(0, this.getClass()
                 .getClassLoader()));
         getAutCommunicator()
                 .addCommunicationErrorListener(new CommunicationListener());
         getAutCommunicator().run();
     }
 
     /**
      * @param infoMessage message to show
      */
     private void showUserInfo(final String infoMessage) {
         if (m_cmd.hasOption("q")) { //$NON-NLS-1$
             System.out.println(infoMessage);
         } else {
             try {
                 UIManager.setLookAndFeel(UIManager.
                         getSystemLookAndFeelClassName());
             } catch (ClassNotFoundException e) { // NOPMD by al on 3/19/07 1:55 PM
               // as this is just for the information box, ignore exceptions.
             } catch (InstantiationException e) { // NOPMD by al on 3/19/07 1:55 PM
               // as this is just for the information box, ignore exceptions.
             } catch (IllegalAccessException e) { // NOPMD by al on 3/19/07 1:55 PM
               // as this is just for the information box, ignore exceptions.
             } catch (UnsupportedLookAndFeelException e) { // NOPMD by al on 3/19/07 1:55 PM
               // as this is just for the information box, ignore exceptions.
             }
             Thread t = new Thread() {
                 public void run() {
                     try {
                         sleep(10000);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
 
                     EventQueue.invokeLater(new Runnable() {
                         public void run() {
                             JOptionPane.getRootFrame().dispose();
                         }
                     });
                 }
             };
             t.start();
             JOptionPane.showMessageDialog(null, infoMessage
                + I18n.getString("GDServer.dialogClose"), //$NON-NLS-1$
                 I18n.getString("Constants.DefaultTextValue"), //$NON-NLS-1$
                 JOptionPane.INFORMATION_MESSAGE);
         }
     }
 
     /**
      * prints a formatted help text
      */
     private void printHelp() {
         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("autagent", //$NON-NLS-1$
             createOptions(), true);
     }
 
     /**
      * private method which prints a start message (java version)
      */
     private void logRunning() {
         if (log.isInfoEnabled()) {
             String message = "running VM with JRE version: " //$NON-NLS-1$
                     + System.getProperty("java.version"); //$NON-NLS-1$
             log.info(message);
         }
     }
 
     /**
      * private method which prints a 'listening' message (the port number
      * listening to)
      */
     private void logStartListening() {
 
         if (log.isInfoEnabled()) {
             String message = "listening to port " //$NON-NLS-1$
                     + getCommunicator().getLocalPort();
             log.info(message);
         }
     }
 
     /**
      * @return Returns the cmd.
      */
     public CommandLine getCmd() {
         return m_cmd;
     }
 
     /**
      * @param cmd
      *            The cmd to set.
      */
     public void setCmd(CommandLine cmd) {
         m_cmd = cmd;
     }
 
     /**
      * 
      * @return The AUT Agent responsible for managing running AUTs.
      */
     public AutAgent getAgent() {
         return m_agent;
     }
     
     /**
      * A thread waiting for termination of the autServerVM. Puts the exitcode
      * into m_autExitValue and handle the termination.
      *
      * @author BREDEX GmbH
      * @created 03.08.2004
      */
     private static class AUTServerWatcher extends Thread {
 
         /** lock for synchonizing on m_autServerVM */
         private final Object m_autServerLock = new Object();
         
         /** 
          * the started VM the AUTServer running in, it's null, when no AutServer
          * was started 
          */
         private Process m_autServerProcess;
 
         /** the exit value of the VM the AUTServer is running in */
         private int m_autExitValue;
 
         /** used to pick up the 'Unrecognized option' error stream */
         private String m_errorStream;
 
         /**
          * whether the server is expecting the AUT server to stop. Used for
          * deciding whether to report the stop as an error.
          */
         private boolean m_isExpectingAUTServerStop;
 
         /** whether the AUT was startet using the GDAgent mechanism */
         private boolean m_isAgentSet;
 
         /** sends messages concerning the AUT Server */
         private CommunicationHelper m_messenger;
         
         /**
          * Constructor
          * 
          * @param autServerProcess The process in which the AUT and AUT Server
          *                         are running.
          * @param isAgentSet Whether the AUT was startet using the GDAgent 
          *                   mechanism.
          * @param messenger Sends messages concerning the AUT Server.
          */
         public AUTServerWatcher(Process autServerProcess, boolean isAgentSet, 
                 CommunicationHelper messenger) {
             super("AUTServerWatcher"); //$NON-NLS-1$
             m_autServerProcess = autServerProcess;
             m_isAgentSet = isAgentSet;
             m_messenger = messenger;
         }
 
         /**
          * handles the termination of the AUTServer, uses m_autExitValue
          */
         private void handleStoppedAUTServer() {
             if (log.isInfoEnabled()) {
                 log.info("trying to send message with AUTServer exitcode '" //$NON-NLS-1$
                         + String.valueOf(m_autExitValue)
                         + "' to client"); //$NON-NLS-1$
             }
             StartAUTServerStateMessage message = null;
             ChooseCheckModeDialogBP.getInstance().closeDialog();
             ObservationConsoleBP.getInstance().closeShell();
             switch (m_autExitValue) {
                 case AUTServerExitConstants.EXIT_OK:
                     log.info("regular termination of AUTServer"); //$NON-NLS-1$
                     break;
                 case AUTServerExitConstants.AUT_START_ERROR:
                     message = new StartAUTServerStateMessage(
                         StartAUTServerStateMessage.ERROR, "Error while starting AUT!"); //$NON-NLS-1$
                     break;
                 case AUTServerExitConstants.EXIT_INVALID_ARGS:
                     if (m_isAgentSet && (m_errorStream != null)) {
                         message = new StartAUTServerStateMessage(
                                 StartAUTServerStateMessage.JDK_INVALID,
                                 "JDK 1.5 or higher is required to start your AUT" + //$NON-NLS-1$
                                 " via executable file."); //$NON-NLS-1$
                     } else {
                         message = new StartAUTServerStateMessage(
                                 StartAUTServerStateMessage.INVALID_ARGUMENTS,
                                 "invalid arguments"); //$NON-NLS-1$
                     }
                     break;
                 case AUTServerExitConstants.EXIT_INVALID_NUMBER_OF_ARGS:
                     message = new StartAUTServerStateMessage(
                             StartAUTServerStateMessage.INVALID_ARGUMENTS,
                             "invalid number of arguments"); //$NON-NLS-1$
                     break;
                 case AUTServerExitConstants.EXIT_UNKNOWN_GUIDANCERCLIENT:
                     message = new StartAUTServerStateMessage(
                             StartAUTServerStateMessage.COMMUNICATION,
                             "establishing communication failed: invalid client"); //$NON-NLS-1$
                     break;
                 case AUTServerExitConstants.EXIT_COMMUNICATION_ERROR:
                     message = new StartAUTServerStateMessage(
                             StartAUTServerStateMessage.COMMUNICATION,
                             "establishing communication failed"); //$NON-NLS-1$
                     break;
                 case AUTServerExitConstants
                     .EXIT_SECURITY_VIOLATION_AWT_EVENT_LISTENER:
                 case AUTServerExitConstants
                     .EXIT_SECURITY_VIOLATION_COMMUNICATION:
                 case AUTServerExitConstants.EXIT_SECURITY_VIOLATION_REFLECTION:
                 case AUTServerExitConstants.EXIT_SECURITY_VIOLATION_SHUTDOWN:
                     message = new StartAUTServerStateMessage(
                             StartAUTServerStateMessage.SECURITY,
                             "security violation"); //$NON-NLS-1$
                     break;
                 case AUTServerExitConstants.EXIT_AUT_NOT_FOUND:
                 case AUTServerExitConstants.EXIT_AUT_WRONG_CLASS_VERSION:
                 case AUTServerExitConstants.EXIT_MISSING_AGENT_INFO:
                     // do nothing : AUTServer sent already a message
                     break;
                 case AUTServerExitConstants.RESTART:
                     message = m_messenger.handleAutRestart();
                     break;
                 default:
                     log.error("unknown AUTServer exit code: " //$NON-NLS-1$
                             + m_autExitValue + "'"); //$NON-NLS-1$
                     message = new StartAUTServerStateMessage(
                         StartAUTServerStateMessage.ERROR,
                         "unknown AUTServer exit code: '" //$NON-NLS-1$
                         + m_autExitValue + "'"); //$NON-NLS-1$
             }
             m_messenger.sendStoppedAUTServerMessage(message);
         }
 
         /**
          * {@inheritDoc}
          */
         public void run() {
             DevNull dn;
             try {
                 // clear the streams of the autServerVM
                 synchronized (m_autServerLock) {
                     dn = new DevNull(m_autServerProcess.getErrorStream());
                     dn.start();
                     new DevNull(m_autServerProcess.getInputStream()).start();
                 }
                 // don't synchronized, catching NullPointerException which is
                 // raised if the process has already terminated
                 m_autExitValue = m_autServerProcess.waitFor();
                 // picking up the 'Unrecognized option' error stream
                 m_errorStream = dn.getLine();
 
                 synchronized (m_autServerLock) {
                     m_autServerProcess = null;
                 }
 
                 if (log.isInfoEnabled()) {
                     log.info("VM stopped with exitValue " //$NON-NLS-1$
                         + String.valueOf(m_autExitValue));
                 }
 
                 if (!m_isExpectingAUTServerStop) {
                     handleStoppedAUTServer();
                 }
 
                 m_isExpectingAUTServerStop = false;
 
             } catch (InterruptedException ie) {
                 log.info("thread observing autserver process interrupted", ie); //$NON-NLS-1$
             } catch (NullPointerException npe) {
                 log.debug("autserver process already terminated", npe); //$NON-NLS-1$
             }
         }
     }
 
     /**
      * Helper class for sending messages through the AutStarter's 
      * communicator(s).
      * 
      * @author BREDEX GmbH
      * @created Feb 25, 2010
      */
     private class CommunicationHelper {
         /**
          * sends a message via communicator if the AutServer has stopped
          * @param message the message to send
          */
         public void sendStoppedAUTServerMessage(
                 StartAUTServerStateMessage message) {
             if (message != null) {
                 try {
                     getCommunicator().send(message);
                 } catch (CommunicationException bce) { // NOPMD by al on 3/19/07 1:55 PM
                     // communication already closed, do nothing
                 } catch (NullPointerException npe) { // NOPMD by al on 3/19/07 1:56 PM
                     // communication already closed, do nothing
                 }
             }
         }
         
         /**
          * Handles the restart of the AUT(Server) while test execution
          * @return StartAUTServerStateMessage
          */
         public StartAUTServerStateMessage handleAutRestart() {
             StartAUTServerStateMessage message = null;
             getAutCommunicator().close();
             getAutCommunicator().getConnectionManager()
                 .remove(getAutCommunicator().getConnection());
             try {
                 initAutConnectionSocket();
             } catch (JBVersionException e) {
                 message = new StartAUTServerStateMessage(
                     StartAUTServerStateMessage.COMMUNICATION,
                     "version exception while restart AUT"); //$NON-NLS-1$
             } catch (IOException e) {
                 message = new StartAUTServerStateMessage(
                     StartAUTServerStateMessage.COMMUNICATION,
                     "io exception while restart AUT"); //$NON-NLS-1$
             }
             return message;
         }
     }
     
     /**
      * Inner class listening for closing connections. In case of a shutdown the
      * communicator is restarted.
      *
      * @author BREDEX GmbH
      * @created 26.07.2004
      */
     private class CommunicationListener
         implements ICommunicationErrorListener {
 
         /**
          * {@inheritDoc}
          */
         public void connectingFailed(InetAddress inetAddress, int port) {
             log.error("connectingFailed() called although this is a server"); //$NON-NLS-1$
         }
         /**
          * {@inheritDoc}
          */
         public void connectionGained(InetAddress inetAddress, int port) {
             if (log.isInfoEnabled()) {
                 try {
                     String message = "accepted connection from " + //$NON-NLS-1$
                         inetAddress.getHostName()
                         + ":" + String.valueOf(port); //$NON-NLS-1$
                     log.info(message);
                 } catch (SecurityException se) {
                     log.warn("security violation while getting the host name from ip address", //$NON-NLS-1$
                             se);
                 }
             }
         }
         /**
          * {@inheritDoc}
          */
         public void acceptingFailed(int port) {
             log.error("accepting failed on port: " + String.valueOf(port)); //$NON-NLS-1$
         }
 
         /**
          * {@inheritDoc}
          */
         public void sendFailed(Message message) {
             log.warn("sending message failed: " + String.valueOf(message)); //$NON-NLS-1$
         }
 
         /**
          * {@inheritDoc}
          */
         public void shutDown() {
             log.info("connection closed"); //$NON-NLS-1$
         }
     }
 
 }
