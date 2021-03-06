 package com.mucommander.shell;
 
 import com.mucommander.Debug;
 import com.mucommander.PlatformManager;
 import com.mucommander.conf.*;
 import com.mucommander.file.AbstractFile;
import com.mucommander.file.impl.local.LocalFile;
 import com.mucommander.process.AbstractProcess;
 import com.mucommander.process.ProcessListener;
 import com.mucommander.process.ProcessRunner;
 import com.mucommander.command.CommandParser;
 
 import java.io.IOException;
 
 /**
  * @author Maxence Bernard, Nicolas Rinaudo
  */
 public class Shell implements ConfigurationListener {
     // - Class variables -----------------------------------------------------
     // -----------------------------------------------------------------------
     /** Tokens that compose the shell command. */
     private static String[] tokens;
    /** Tokens that compose remote shell commands. */
    private static String[] remoteTokens;
     /** Instance of configuration listener. */
     private static Shell    confListener;
 
 
 
     // - Initialisation ------------------------------------------------------
     // -----------------------------------------------------------------------
     /**
      * Initialises the shell.
      */
     static {
         ConfigurationManager.addConfigurationListener(confListener = new Shell());
 
         // This could in theory also be written without the confListener reference.
         // It turns out, however, that proGuard is a bit too keen when removing fields
         // he thinks are not used. This code is written that way to make sure
         // confListener is not taken out, and the ConfigurationListener instance removed
         // instantly as there is only a WeakReference on it.
         // The things we have to do...
         confListener.setShellCommand();

        remoteTokens = new String[1];
     }
 
     /**
      * Prevents instances of Shell from being created.
      */
     private Shell() {}
 
 
 
     // - Shell interaction ---------------------------------------------------
     // -----------------------------------------------------------------------
     /**
      * Executes the specified command in the specified folder.
      * <p>
      * The <code>currentFolder</code> folder parameter will only be used if it's neither a
      * remote directory nor an archive. Otherwise, the command will run from the user's
      * home directory.
      * </p>
      * @param     command       command to run.
      * @param     currentFolder where to run the command from.
      * @return                  the resulting process.
      * @exception IOException   thrown if any error occurs while trying to run the command.
      */
     public static AbstractProcess execute(String command, AbstractFile currentFolder) throws IOException {return execute(command, currentFolder, null);}
 
     /**
      * Executes the specified command in the specified folder.
      * <p>
      * The <code>currentFolder</code> folder parameter will only be used if it's neither a
      * remote directory nor an archive. Otherwise, the command will run from the user's
      * home directory.
      * </p>
      * <p>
      * Information about the resulting process will be sent to the specified <code>listener</code>.
      * </p>
      * @param     command       command to run.
      * @param     currentFolder where to run the command from.
      * @param     listener      where to send information about the resulting process.
      * @return                  the resulting process.
      * @exception IOException   thrown if any error occurs while trying to run the command.
      */
     public static synchronized AbstractProcess execute(String command, AbstractFile currentFolder, ProcessListener listener) throws IOException {
        String[] commandTokens;
 
        if(Debug.ON) Debug.trace("Executing " + command);
 
         // Adds the command to history.
         ShellHistoryManager.add(command);
 
        // Builds the shell command.
        // Local files use the configuration defined shell. Remote files
        // will execute the command as-is.
        if(currentFolder instanceof LocalFile) {
            tokens[tokens.length - 1] = command;
            commandTokens             = tokens;
        }
        else {
            remoteTokens[0] = command;
            commandTokens   = remoteTokens;
        }
         // Starts the process.
        return ProcessRunner.execute(commandTokens, currentFolder, listener);
     }
 
 
 
     // - Configuration management --------------------------------------------
     // -----------------------------------------------------------------------
     /**
      * Extracts the shell command from configuration.
      */
     private static synchronized void setShellCommand() {
         String command; // Shell command.
 
         // Retrieves the configuration defined shell command.
         if(ConfigurationManager.getVariableBoolean(ConfigurationVariables.USE_CUSTOM_SHELL, ConfigurationVariables.DEFAULT_USE_CUSTOM_SHELL))
             command = ConfigurationManager.getVariable(ConfigurationVariables.CUSTOM_SHELL, PlatformManager.getDefaultShellCommand());
         else
             command = PlatformManager.getDefaultShellCommand();
 
         // Splits the command into tokens, leaving room for the argument.
         tokens = CommandParser.getTokensWithParams(command, 1);
     }
 
     /**
      * Reacts to configuration changes.
      */
     public boolean configurationChanged(ConfigurationEvent event) {
         if(event.getVariable().startsWith(ConfigurationVariables.SHELL_SECTION))
             setShellCommand();
         return true;
     }
 }
