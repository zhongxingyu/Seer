 package newTeam.common.util;
 
 import battlecode.common.*;
 
 public class Logger {
     
     /**
      * Prints Sashko's raw strings
      * @param s     string to be printed.
      */
     public static void debug_printSashko(String s) {
         if(LoggerConfigs.SASHKO_STRINGS) {
             System.out.println(s);
         }
     }
 
     /**
      * Prints Hocho's raw strings
      * @param s     string to be printed.
      */
     public static void debug_printHocho(String s) {
         if(LoggerConfigs.HOCHO_STRINGS) {
             System.out.println(s);
         }
     }
 
     /**
      * Prints Antony's raw strings
      * @param s     string to be printed.
      */
     public static void debug_printAntony(String s) {
         if(LoggerConfigs.ANTONY_STRINGS) {
             System.out.println(s);
         }
     }
     
     /**
     * For precautionary use - if this method is being called, something has gone wrong
     * @param errorMessage
     * @param errorAuthor
     */
    public static void debug_printCustomErrorMessage(String errorMessage, String errorAuthor) {
        System.out.println("According to " + errorAuthor + ", the following has gone wrong:");
        System.out.println(errorMessage);
    }
    
    /**
      * Prints exception stack trace.
      * @param   e   Exception
      */
     public static void debug_printExceptionMessage(Exception e) {
         e.printStackTrace();
     }
     
     /**
      * Prints bytecode count.
      */
    public static void debug_printRemainingBytecodes() {
        System.out.println("remaining bytecodes: " + String.valueOf(Clock.getBytecodeNum()));
     }
 
 }
