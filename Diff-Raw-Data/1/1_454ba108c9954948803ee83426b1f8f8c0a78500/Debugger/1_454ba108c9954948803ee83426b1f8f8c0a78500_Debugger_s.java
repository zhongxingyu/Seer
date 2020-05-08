 package parameters;
 
 /**
  * <b>Debugger</b><br>
  * Provide methods for printing messages 
  * accordingly to a flag.
  *
  * @author Renato Cordeiro Ferreira
  */
 public class Debugger
 {
     /** 
      * Variable to indicate if the Debugger should 
      * print it's info.
      */
     public static boolean info = false;
     
     /** 
      * Print without a terminal newline.
      * @param strings Variable size list of objects,
      *                which will have their 'toString()'
      *                method used for being printed.
      */
     public static void print(Object ... strings)
     {
         if(!info) return;
         for(Object s: strings) System.out.print(s.toString());
     }
 
     /** 
      * Print with a terminal newline.
      * @param strings Variable size list of objects,
      *                which will have their 'toString()'
      *                method used for being printed.
      */
     public static void say(Object ... strings)
     {
         if(!info) return;
         print(strings);
         System.out.println();
     }
     
     /** 
      * Print formatted.
      * @param format String with sequences of formats to
      *               be printed.
      * @param args   Variable size list of objects,
      *               which will have their 'toString()'
      *               method used for being printed.
     * @see   java.lang.System.out.printf
      */
     public static void printf(String format, Object ... args)
     {
         if(!info) return;
         System.out.printf(format, args);
     }
 }
