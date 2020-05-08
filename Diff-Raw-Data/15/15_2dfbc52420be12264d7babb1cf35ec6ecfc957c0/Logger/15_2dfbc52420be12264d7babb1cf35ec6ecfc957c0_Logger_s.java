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
      * Prints exception stack trace.
      * @param   e   Exception
      */
     public static void debug_printExceptionMessage(Exception e) {
         e.printStackTrace();
     }
     
     /**
      * Prints bytecode count.
      */
    public static void debug_printBytecodeNumber() {
        System.out.println("bytecodes: " + String.valueOf(Clock.getBytecodeNum()));
     }
 
 }
