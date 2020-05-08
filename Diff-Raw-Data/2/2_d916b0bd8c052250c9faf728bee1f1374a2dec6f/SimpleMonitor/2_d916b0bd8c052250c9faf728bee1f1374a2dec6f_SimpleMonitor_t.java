 import java.text.SimpleDateFormat;
 import java.text.DateFormat;
 import java.util.Date;
 
 public class SimpleMonitor {
 
     public static boolean out = true;
     public static boolean printTimes = true;
     public static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
     
     /**
      * Constructor to Monitor
      *
      * @param       outBoolean              boolean         true if you want to output to stdout
      * @param       timeBoolean             boolean         true if you want to record timestamp to stdout
      */
     public SimpleMonitor(boolean outBoolean, boolean timeBoolean) {
     	out = outBoolean;
     	printTimes = timeBoolean;
     }
 
     /**
      * An interface to print to stdout
      *
      * @param   o    Object  Object to print to stdout
      */
     public static void out(Object o) {
         if(out) {
             if(printTimes) {
             	Date date = new Date();
                 System.out.print( dateFormat.format(date) + "  " );
             }
             System.out.println(o);
         }
     }
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		int outn = 1;
 		out("Testing " + outn );
         printTimes = false;
         out("Testing " + outn++ );
     }
 }
