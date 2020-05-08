 package com.oshmidt;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.log4j.Logger;
 
 /**
  * @author oshmidt
  *         <p>
  *         Main class. Application start point. After start parsed options and
  *         send them to GistManager.
  */
 public final class App {
 
     /** private constructor. */
     private App() {
     }
 
     private static final String HELP_SHORT = Messages
             .getString("com.oshmidt.cli.short.help");
     private static final String HELP_LONG = Messages
             .getString("com.oshmidt.cli.long.help");
     private static final String HELP_DESCRIPTION = Messages
             .getString("com.oshmidt.cli.description.help");
 
     private static final String ALL_KEY = Messages
             .getString("com.oshmidt.cli.allKey");
 
     private static final String USERNAME_SHORT = Messages
             .getString("com.oshmidt.cli.short.username");
     private static final String USERNAME_DESCRIPTION = Messages
             .getString("com.oshmidt.cli.description.username");
 
     private static final String PASSWORD_SHORT = Messages
             .getString("com.oshmidt.cli.short.password");
     private static final String PASSWORD_DESCRIPTION = Messages
             .getString("com.oshmidt.cli.description.password");
 
     private static final String DOWNLOAD_GISTS_SHORT = Messages
             .getString("com.oshmidt.cli.short.downloadGists");
     private static final String DOWNLOAD_GISTS_DESCRIPTION = Messages
             .getString("com.oshmidt.cli.description.downloadGists");
 
     private static final String SHOW_LOCAL_GISTS_LONG = Messages
             .getString("com.oshmidt.cli.long.showLocalGists");
     private static final String SHOW_LOCAL_GISTS_DESCRIPTION = Messages
             .getString("com.oshmidt.cli.description.showLocalGists");
 
     private static final String DOWNLOAD_FILES_LONG = Messages
             .getString("com.oshmidt.cli.long.downloadFiles");
     private static final String DOWNLOAD_FILES_DESCRIPTION = Messages
             .getString("com.oshmidt.cli.description.downloadFiles");
 
     private static final String SHORT_DESCRIPTION = Messages
             .getString("com.oshmidt.cli.short.Description");
 
     private static final String HELP_TITLE = Messages
             .getString("com.oshmidt.cli.helpTitle");
 
     private static final String WRONG_COMMAND = Messages
             .getString("com.oshmidt.cli.wrongCommand");
 
     private static final String HELP_DEVELOPED_BY = Messages
             .getString("com.oshmidt.cli.helpDevelopedBy");
 
     /**
      * GistManager instance.
      */
     private static GistManager gistManager = new GistManager();
 
     /**
      * Logger instance.
      */
     private static Logger logger = Logger.getLogger(App.class);
 
     /**
      * Application start point.
      *
      * @param args
      *            - start arguments
      */
     public static void main(final String[] args) {
         logger.info(Messages.getString("com.oshmidt.cli.aplicationStartOption",
                 StringUtils.convertToString(args, " ")));
 
         CommandLineParser parser = new PosixParser();
         CommandLine cmd = null;
 
         try {
             cmd = parser.parse(initOptions(), args);
         } catch (ParseException e1) {
             logger.error(WRONG_COMMAND + e1);
             System.out.println(WRONG_COMMAND);
             return;
         }
         if (cmd.hasOption(USERNAME_SHORT) && cmd.hasOption(PASSWORD_SHORT)) {
 
             gistManager.initUser(cmd.getOptionValue(USERNAME_SHORT),
                     cmd.getOptionValue(PASSWORD_SHORT));
             gistManager.loadAndSaveRemoteGists();
         } else if (cmd.hasOption(DOWNLOAD_GISTS_SHORT)) {
             gistManager.importUser();
             gistManager.loadAndSaveRemoteGists();
         } else {
             gistManager.readLocalGists();
         }
 
         if (cmd.hasOption(DOWNLOAD_FILES_LONG)) {
             gistManager.downloadGists(cmd.getOptionValue(DOWNLOAD_FILES_LONG));
         }
 
        if (cmd.hasOption(HELP_SHORT)) {
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp(SHORT_DESCRIPTION, HELP_TITLE, initOptions(),
                     HELP_DEVELOPED_BY);
         }
 
         if (cmd.hasOption(SHOW_LOCAL_GISTS_LONG)) {
             if (cmd.getOptionValue(SHOW_LOCAL_GISTS_LONG).equals(ALL_KEY)) {
                 gistManager.showGists();
             }
         }
 
     }
 
     /**
      * Method create and return options for parsing input parameters.
      *
      * @return Options - {@link org.apache.commons.cli.Option}
      */
     private static Options initOptions() {
         Options options = new Options();
         options.addOption(USERNAME_SHORT, true, USERNAME_DESCRIPTION);
         options.addOption(PASSWORD_SHORT, true, PASSWORD_DESCRIPTION);
         options.addOption(DOWNLOAD_GISTS_SHORT, false,
                 DOWNLOAD_GISTS_DESCRIPTION);
         options.addOption(SHOW_LOCAL_GISTS_LONG, true,
                 SHOW_LOCAL_GISTS_DESCRIPTION);
         options.addOption(DOWNLOAD_FILES_LONG, true, DOWNLOAD_FILES_DESCRIPTION);
         options.addOption(HELP_SHORT, HELP_LONG, false, HELP_DESCRIPTION);
         return options;
 
     }
 
 }
