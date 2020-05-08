 package openagendamail;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import openagendamail.file.LogFile;
 import openagendamail.util.OpenAgendaMailTools;
 
 /**
  * The main class for AgendaMail.
  *
  * @author adam
  * @date Jan 1, 2013
 * Last Updated March 15, 2013
  */
 public class OpenAgendaMail {
 
     /** The properties for this program. */
     private static Properties m_props;
 
     /** A version string. */
     static final String VERSION = "v1.7";
 
     /** The date of the last update to the system. */
    private static final String LAST_UPDATED = "March 15th, 2013";
 
 
     /**
      * The Main method.
      * @param args the command line arguments
      */
     public static void main(String[] args) {
 
         // print out software version info
         StringBuilder bldr = new StringBuilder(System.lineSeparator());
        bldr.append("OpenAgendaMail:  A program by Adam Anderson");
         bldr.append(System.lineSeparator());
         bldr.append("Version:  " + VERSION);
         bldr.append(System.lineSeparator());
         bldr.append("Last Updated:  " + LAST_UPDATED);
         System.out.println(bldr.toString());
         LogFile.getLogFile().log(bldr.toString());
 
         // VALIDATE ARGUMENTS
         if (args.length != 1){
             printUsage();
             System.exit(0);
         }
         if (!(args[0].toLowerCase().equals("1stand3rd") || args[0].toLowerCase().equals("week-based") ||
                 args[0].toLowerCase().equals("-h") || args[0].toLowerCase().equals("--help"))) {
             printUsage();
             System.exit(0);
         }
 
         // BEGIN EXECUTION
         loadProperties();
         switch (args[0].toLowerCase()) {
             case "1stand3rd":
                 // This block is called when an agenda is required for the 1st and 3rd Sundays of the month.
                 LogFile.getLogFile().log("Application Started in 1stAndThird mode.");
                 executeFirstAndThirdMode();
                 break;
 
             case "week-based":
                 // This block is called when an agenda is required on a weekly recurring basis on the same day (ie
                 // every week or every two weeks etc.)
                 LogFile.getLogFile().log("Application Started in week-based mode.");
                 executeWeekBasedMode();
                 break;
 
             default:
                 System.exit(1);
                 break;
         }
     }
 
     /** Helper method that loads the properties file into the application's {@link Properties} object. */
     private static void loadProperties(){
         m_props = new Properties();
         try {
             FileInputStream inStream = new FileInputStream("agendamail.properties");
             m_props.load(inStream);
         } catch (FileNotFoundException ex) {
             LogFile.getLogFile().log("Properties file not found.", ex);
         } catch (IOException ioex){
             LogFile.getLogFile().log("Failed to read in properties.", ioex);
         }
     }
 
     /** Starts the application in week-based mode. */
     private static void executeWeekBasedMode(){
         long frequencyInSeconds = Integer.valueOf(m_props.getProperty("weeks.between.meetings", "1")) * OpenAgendaMailTools.ONE_WEEK_IN_SECONDS;
         long secondsUntilAgendaIsDue = OpenAgendaMailTools.getSecondsUntilSpecifiedDay(OpenAgendaMailTools.getDayOfWeek(m_props.getProperty("send.day", "tue")));
 
         // Build the agenda.
         BuildAgendaRunnable builder = new BuildAgendaRunnable(m_props, true);
         ScheduledExecutorService buildExecutor = Executors.newSingleThreadScheduledExecutor();
         if (m_props.getProperty("debug", "false").equals("true")){
             buildExecutor.scheduleWithFixedDelay(builder, 0, frequencyInSeconds, TimeUnit.SECONDS);
         } else {
             buildExecutor.scheduleWithFixedDelay(builder, secondsUntilAgendaIsDue, frequencyInSeconds, TimeUnit.SECONDS);
         }
 
         // Send the agenda.
         EmailSenderRunnable sender = OpenAgendaMailTools.buildAgendaEmailSender(m_props, null);
         ScheduledExecutorService sendExecutor = Executors.newSingleThreadScheduledExecutor();
         if (m_props.getProperty("debug", "false").equals("true")){
             sendExecutor.scheduleWithFixedDelay(sender, 60, frequencyInSeconds, TimeUnit.SECONDS);
         } else {
             sendExecutor.scheduleWithFixedDelay(sender, secondsUntilAgendaIsDue + OpenAgendaMailTools.SECONDS_IN_FOUR_HOURS, frequencyInSeconds, TimeUnit.SECONDS);
         }
 
         // if enabled, schedule the reminder email
         if ((m_props.getProperty("reminders.on", "false")).toLowerCase().equals("true")){
             long secondsUntilReminder = OpenAgendaMailTools.getSecondsUntilSpecifiedDay(OpenAgendaMailTools.getDayOfWeek(m_props.getProperty("reminder.day", "mon")));
             EmailSenderRunnable reminder = OpenAgendaMailTools.buildReminderSender(m_props);
             ScheduledExecutorService reminderExecutor = Executors.newSingleThreadScheduledExecutor();
 
             if (m_props.getProperty("debug", "false").equals("true")){
                 System.out.println("Debug scheduling reminder...");
                 reminderExecutor.scheduleWithFixedDelay(reminder, 30, frequencyInSeconds, TimeUnit.SECONDS);
             } else {
                 reminderExecutor.scheduleWithFixedDelay(reminder, secondsUntilReminder, frequencyInSeconds, TimeUnit.SECONDS);
             }
         }
     }
 
     /** Starts the scheduling for meetings that are on the 1st and 3rd of a given day of the week within a month. */
     private static void executeFirstAndThirdMode(){
         long secondsUntilSendDay = OpenAgendaMailTools.getSecondsUntilSpecifiedDay(OpenAgendaMailTools.getDayOfWeek(m_props.getProperty("send.day", "tue")));
 
         FirstAndThirdRunnable firstAndThird = new FirstAndThirdRunnable(m_props);
         ScheduledExecutorService checkerExecutor = Executors.newSingleThreadScheduledExecutor();
         if (m_props.getProperty("debug", "false").equals("true")){
             checkerExecutor.scheduleWithFixedDelay(firstAndThird, 15, secondsUntilSendDay, TimeUnit.SECONDS);
         } else {
             checkerExecutor.scheduleWithFixedDelay(firstAndThird, secondsUntilSendDay, OpenAgendaMailTools.ONE_WEEK_IN_SECONDS, TimeUnit.SECONDS);
         }
     }
 
     /** Prints out the proper usage of the application to the command prompt. */
     private static void printUsage(){
         System.out.println("\nUsage:");
 
         System.out.println("   AgendaMail requires a flag at the command line.  To run in");
         System.out.println("   week-based mode at the command prompt type:");
         System.out.println("      'java -jar AgendaMail.jar week-based' but without quotes.\n\n");
 
         System.out.println("   To run in 1st and 3rd week mode, at the command prompt type:");
         System.out.println("      'java -jar AgendaMail.jar 1stand3rd' but without quotes.\n\n");
 
         System.out.println("   To show this help type:");
         System.out.println("      'java -jar AgendaMail.jar -h' but without quotes.\n");
         System.out.println("   or type:");
         System.out.println("      'java -jar AgendaMail.jar --help' but without quotes.\n\n");
     }
 }
