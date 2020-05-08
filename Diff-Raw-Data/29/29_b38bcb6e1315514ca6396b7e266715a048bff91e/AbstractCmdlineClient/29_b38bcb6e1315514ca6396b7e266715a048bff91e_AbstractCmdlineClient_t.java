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
 package org.eclipse.jubula.client.cmd;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.jubula.client.cmd.i18n.Messages;
 import org.eclipse.jubula.client.core.ClientTestFactory;
 import org.eclipse.jubula.client.core.businessprocess.ClientTestStrings;
 import org.eclipse.jubula.client.core.businessprocess.JobConfiguration;
 import org.eclipse.jubula.client.core.communication.ConnectionException;
 import org.eclipse.jubula.client.core.communication.ServerConnection;
 import org.eclipse.jubula.client.core.errorhandling.ErrorMessagePresenter;
 import org.eclipse.jubula.client.core.errorhandling.IErrorMessagePresenter;
 import org.eclipse.jubula.client.core.model.IAUTConfigPO;
 import org.eclipse.jubula.client.core.persistence.locking.LockManager;
 import org.eclipse.jubula.client.core.preferences.database.DatabaseConnection;
 import org.eclipse.jubula.client.core.preferences.database.DatabaseConnectionConverter;
 import org.eclipse.jubula.client.core.progress.IProgressConsole;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.messagehandling.Message;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.registration.AutIdentifier;
 import org.eclipse.osgi.util.NLS;
 
 
 /**
  * @author BREDEX GmbH
  * @created Mar 12, 2009
  */
 public abstract class AbstractCmdlineClient implements IProgressConsole {
     /** <code>EXIT_CODE_ERROR</code> */
     protected static final int EXIT_CODE_ERROR = 1;
     /** <code>EXIT_CODE_OK</code> */
     protected static final int EXIT_CODE_OK = 0;
     
     /** error message */
     protected static final String OPT_NO_VAL = 
         Messages.NoArgumentFor + StringConstants.COLON;
     /** error message */
     protected static final String OPT_UNKNOWN = 
         Messages.UnrecognizedOption + StringConstants.COLON 
         + StringConstants.SPACE;
     /** separator character */
     protected static final char COLON = ':';
     /** log facility */
     private static Log log = LogFactory.getLog(AbstractCmdlineClient.class);
     /** be quiet during processing */
     private static boolean quiet = false;
     /** did an error occur during processing */
     private static boolean errorOccured = false;
     /** is this a dry run* */
     private boolean m_noRun = false;
     /** the command line representation */
     private CommandLine m_cmd = null;
 
     /** external configuation file with parameters */
     private File m_configFile;
     /** JobConfiguration created from configFile */
     private JobConfiguration m_job;
 
     /**
      * @param name name of optione
      * @param hasArg option has an argument
      * @param argname name of the argument
      * @param text Text for help
      * @param isReq option sis required
      * @return Option opt 
      */
     protected static Option createOption(String name, boolean hasArg,
             String argname, String text, boolean isReq) {
         Option opt = new Option(name, hasArg, text);
         opt.setRequired(isReq);
         opt.setArgName(argname);
         return opt;
     }
 
 
     /**
      * cleanup of connection
      */
     protected void shutdown() {
         try {
             if (!ServerConnection.getInstance().isConnected()) {
                 printlnConsoleError(Messages.ConnectionToAutUnexpectedly);
             }
         } catch (ConnectionException e) {
             log.info(Messages.ConnectionToAutUnexpectedly, e);
         }
         IAUTConfigPO startedConfig = m_job.getAutConfig();
         if (startedConfig != null) {
             try {
                 AutIdentifier startedAutId = new AutIdentifier(
                         startedConfig.getConfigMap().get(
                                 AutConfigConstants.AUT_ID));
                 if (ServerConnection.getInstance().isConnected()) {
                     ClientTestFactory.getClientTest().stopAut(startedAutId);
                 }
             } catch (ConnectionException e) {
                 log.info(Messages.ErrorWhileShuttingDownStopping, e);
             }
         }
         
         try {
             while (ServerConnection.getInstance().isConnected()) {
                 ClientTestFactory.getClientTest().disconnectFromServer();
                 try {
                     Thread.sleep(200);
                 } catch (InterruptedException e) {
                     // do nothing
                 }
             }
         } catch (ConnectionException e) {
             log.info(Messages.ErrorWhileShuttingDownDisconnecting, e);
         }
         // cleanup after connections closed
         if (LockManager.isRunning()) {
             LockManager.instance().dispose();
         }
     }
 
     /**
      * 
      * @param args
     *            the command line
     * @throws FileNotFoundException
     *             if config file is missing
     * @throws ParseException
     *             if wrong options are present
     * @throws IOException
     *             if io error
     * @return true to continue processing the commandline run; false to stop
     *         processing further execution.
      */
    protected boolean parseCommandLine(String[] args)
         throws FileNotFoundException, ParseException, IOException {
         String[] cloneArgs = args.clone();
         Options options = createOptions(false);
         // Command line arguments parser
         CommandLineParser parser = new BasicParser();
         try {
             // we will parse the command line until there are no
             // (more) errors
             int maxTrys = 5;
             Boolean parseNotOK = true;
             while (parseNotOK) {
                 try {
                     m_cmd = parser.parse(options, cloneArgs);
                     parseNotOK = false;
                 } catch (ParseException exp) {
                     cloneArgs = handleParseException(args, exp);
                     if (maxTrys-- < 0) {
                         throw new ParseException(StringConstants.EMPTY);
                     }
                 }
             }
 
            if (m_cmd.hasOption(ClientStrings.HELP)) {
                printUsage();
                return false;
            }
            
             // The first thing to check is, if there is a config file
             // if there is a config file we read this first,
             if (m_cmd.hasOption(ClientStrings.CONFIG)) {
                 m_configFile = new File(m_cmd
                         .getOptionValue(ClientStrings.CONFIG));
                 if (m_configFile.exists() && m_configFile.canRead()) {
                     printConsoleLn(Messages.ClientConfigFile
                             + m_configFile.getAbsolutePath(), true);
                     m_job = JobConfiguration.initJob(m_configFile);
 
                 } else {
                     throw new FileNotFoundException(StringConstants.EMPTY);
                 }
             } else {
                 m_job = JobConfiguration.initJob(null);
             }
             // now we should have all arguments, either from file or
             // from commandline
             if (m_cmd.hasOption(ClientStrings.QUIET)) {
                 quiet = true;
             }
             if (m_cmd.hasOption(ClientStrings.NORUN)) {
                 m_noRun = true;
             }
             // then set attributes from command Line and check if parameter -startserver was called
             if (m_cmd.hasOption(ClientTestStrings.STARTSERVER)) {
                 m_job.parseOptionsWithServer(m_cmd);
             } else {
                 m_job.parseJobOptions(m_cmd);
             }
             // check if all needed attributes are set
             preValidate(m_job);
 
         } catch (PreValidateException exp) {
             String message = exp.getLocalizedMessage();
             if (message != null && message.length() > 0) {
                 printlnConsoleError(message);
             }
             printUsage();
             throw new ParseException(StringConstants.EMPTY);
         }
        return true;
     }
 
     /**
      * method to create an options object, filled with all options
      * @param req
      *      boolean flag must be true for an required option
      *      this is only used for printing the correct usage
      * @return the options
      */
     private Options createOptions(boolean req) {
         Options options = new Options();
         options.addOption(createOption(ClientStrings.HELP, false, 
                 StringConstants.EMPTY, 
                 Messages.ClientHelpOpt, false));
         options.addOption(createOption(ClientStrings.QUIET, false, 
                 StringConstants.EMPTY, 
                 Messages.ClientQuietOpt, false));
         options.addOption(createOption(ClientStrings.CONFIG, true, 
                 ClientStrings.CONFIGFILE,
                 Messages.ClientConfigOpt, false));
         options.addOption(createOption(ClientTestStrings.DBURL, true, 
                 ClientTestStrings.DATABASE,
                 Messages.ClientDburlOpt, false));
         options.addOption(createOption(ClientTestStrings.DB_SCHEME, true, 
                 ClientTestStrings.SCHEME, 
                 Messages.ClientDbschemeOpt, false));
         options.addOption(createOption(ClientTestStrings.DB_USER, true, 
                 ClientTestStrings.USER, 
                 Messages.ClientDbuserOpt, false));
         options.addOption(createOption(ClientTestStrings.DB_PW, true, 
                 ClientTestStrings.PASSWORD, 
                 Messages.ClientDbpwOpt, false));
         extendOptions(options, req);
         return options;
     }
     
     /**
      * method to extend an options object, filled with all options
      * @param opt Predefined options. This options will be extended
      * during the method call.
      * @param req
      *      boolean flag must be true for an required option
      *      this is only used for printing the correct usage
      */
 
     protected abstract void extendOptions(Options opt, boolean req);
     
     /**
      * Do any final work required before actually running the client
      */
     protected abstract void preRun();
 
     /**
      * writes an output to console
      * @param text
      *      Message
      *      @param printTimestamp should a timestamp be printed
      */
 
     public static void printConsoleLn(String text, boolean printTimestamp) {
         if (printTimestamp) {
             Date now = new Date();
             String time = now.toString();
             printConsole(time);
             printConsole(StringConstants.TAB);
         }
         printConsole(StringUtils.chomp(text));
         printConsole(StringConstants.NEWLINE);
     }
     
     /**
      * {@inheritDoc}
      */
     public void writeErrorLine(String line) {
         printlnConsoleError(line);
     }
 
     /**
      * {@inheritDoc}
      */
     public void writeLine(String line) {
         printConsole(line + StringConstants.NEWLINE);
     }
 
     /**
      * writes an output to console
      * @param text
      *      Message
      */
     public static void printConsole(String text) {
         if (!quiet) {
             System.out.print(text);
         }
     }
 
     /**
      * writes an output to console
      * @param text
      *      the message to log and println to sys.err
      */
     public static void printlnConsoleError(String text) {
         errorOccured = true;
         log.error(Messages.AnErrorOcurred + StringConstants.COLON
                 + StringConstants.SPACE + text);
         System.err.println(Messages.ClientError + StringConstants.NEWLINE
                 + StringConstants.TAB
                 + text); 
     }
     
     /**
      * excute a job
      * @param args
      *      Command Line Parameter
      * @return int
      *      Exit Code
      */
     public int run(String[] args) {
         ErrorMessagePresenter.setPresenter(new IErrorMessagePresenter() {
             public void showErrorMessage(JBException ex, Object[] params,
                     String[] details) {
                 
                 log.error(ex + StringConstants.COLON + StringConstants.SPACE
                         + ex.getMessage());
                 Integer messageID = ex.getErrorId();
                 showErrorMessage(messageID, params, details);
             }
 
             public void showErrorMessage(Integer messageID, Object[] params,
                     String[] details) {
 
                 Message m = MessageIDs.getMessageObject(messageID);
                 if (m == null) {
                     log.error(Messages.NoCorrespondingMessage 
                             + StringConstants.COLON + StringConstants.SPACE 
                             + messageID);
                 } else {
                     String msgString = m.getMessage(params);
                     if (m.getSeverity() == Message.ERROR) {
                         printlnConsoleError(msgString);
                     } else {
                         printConsole(msgString);
                     }
                 }
             }
         });
         try {
            if (!parseCommandLine(args)) {
                return EXIT_CODE_ERROR;
            }
         } catch (ParseException e) {
             log.error(e);
             return EXIT_CODE_ERROR;
         } catch (IOException e) {
             log.error(e);
             return EXIT_CODE_ERROR;
         }
         preRun();
         try {
             int exitCode = doRun();
 
             if (isErrorOccured()) {
                 exitCode = EXIT_CODE_ERROR;
             }
             
             printConsoleLn(Messages.ClientExitCode + exitCode, true);
 
             return exitCode;
         } catch (Throwable t) {
             // Assume that, if an exception has bubbled up this far, then it is 
             // a big enough problem to warrant telling the user and returning a
             // generic error exit code.
             log.error(t);
             printlnConsoleError(t.getLocalizedMessage());
             return EXIT_CODE_ERROR;
         }
     }
 
 
     /**
      * runs the job
      * @return int
      *      Exit Code
      */
     protected abstract int doRun();
 
 
     /**
      * checks if all job arguments are present
      * @param job
      *      contains the job configuration
      * @throws PreValidateException is arguments are missing
      */
     private void preValidate(JobConfiguration job) throws PreValidateException {
         StringBuilder errorMsg = new StringBuilder();
         errorMsg.append(Messages.ClientMissingArgs);
         if (job.getDbConnectionName() == null) {
             appendError(errorMsg, ClientTestStrings.DB_SCHEME, 
                     ClientTestStrings.SCHEME);
         }
         if (job.getDbuser() == null) {
             appendError(errorMsg, ClientTestStrings.DB_USER, 
                     ClientTestStrings.USER);
         }
         if (job.getDbpw() == null) {
             appendError(errorMsg, ClientTestStrings.DB_PW, 
                     ClientTestStrings.PASSWORD);
         }
         extendValidate(job, errorMsg);
         if (errorOccured) {
             throw new PreValidateException(errorMsg.toString());
         }
         if (job.getDbscheme() == null) {
             List<DatabaseConnection> availableConnections = 
                 DatabaseConnectionConverter.computeAvailableConnections();
             List<String> connectionNames = new ArrayList<String>();
             for (DatabaseConnection conn : availableConnections) {
                 connectionNames.add(conn.getName());
             }
             throw new PreValidateException(NLS.bind(
                     Messages.NoSuchDatabaseConnection, 
                     new String[] {job.getDbConnectionName(), 
                             StringUtils.join(connectionNames, ", ")})); //$NON-NLS-1$
         }
     }
 
 
     /**
      * Do validation beyonf the baisc parameters
      * @param job configuration to check
      * @param errorMsgs storage for error messages from validation
      */
     protected abstract void extendValidate(JobConfiguration job, 
             StringBuilder errorMsgs);
 
 
     /**
      * 
      * @param args
      *          commandline
      * @param exp
      *          exception
      * @return args  modified
      */
     public String[] handleParseException(String [] args, ParseException exp) {
         // if there is an error we will remove that token
         // and try it again
         int idx;
         String message = exp.getLocalizedMessage();
         if (message != null && message.length() > 0) {
             printlnConsoleError(message);
         }
         if (message.startsWith(OPT_NO_VAL)) {
             idx = message.indexOf(COLON);
             message = message.substring(idx + 1);
         } else if (message.startsWith(OPT_UNKNOWN)) {
             idx = message.indexOf(COLON);
             message = message.substring(idx + 2);
         }
         for (int i = 0; i < args.length; i++) {
             if (args[i].endsWith(message)) {
                 args[i] = StringConstants.EMPTY;
             }
         }
         return args;
     }
 
     /**
      * 
      * @param errorMsg Stringbuilder with message
      * @param msg1 the missing option
      * @param msg2 the missing option argument
      */
     protected void appendError(StringBuilder errorMsg, String msg1, 
             String msg2) {
         errorOccured = true;
         errorMsg.append(StringConstants.TAB);
         errorMsg.append(StringConstants.MINUS);
         errorMsg.append(msg1);
         errorMsg.append(StringConstants.SPACE);
         errorMsg.append(msg2);
         errorMsg.append(StringConstants.NEWLINE);   
     }
 
     /** 
      * printusage prints the command line syntax
      *
      */
     private void printUsage() {
         Options options = createOptions(true);
         HelpFormatter formatter = new HelpFormatter();
     
         formatter.printHelp(getCmdlineClientExecName(), options, true);
     }
 
     /** @return the name of the executable for the commandline Client */
     public abstract String getCmdlineClientExecName();
 
     /**
      * @return the noRun
      */
     public boolean isNoRun() {
         return m_noRun;
     }
 
 
     /**
      * @return the quiet
      */
     public boolean isQuiet() {
         return quiet;
     }
 
 
     /**
      * @return the errorOccured
      */
     public static boolean isErrorOccured() {
         return errorOccured;
     }
 
 
     /**
      * 
      * @return CommandLine
      *      the command Line the client was started with
      */
     public CommandLine getCmdLine() {
         return m_cmd;
     }
 
     /**
      * initializes the client
      * @param args
      *      Command Line Parameter
      * @return int
      *      ExitCode
      */
     public int runAntTask(String[] args) {
         int exitCode = run(args);
         return exitCode;
     }
 
 
     /**
      * @return the job
      */
     public JobConfiguration getJob() {
         return m_job;
     }
 
 }
