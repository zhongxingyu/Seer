 import java.util.*;
 import java.io.*;
 import static java.lang.Math.*;
 
 /**
  * Just do a find replact on $1 and the name of your problem, then you'll be good to go.
  */
 public class $1 {
     
     /**
      * Do your settings configuration in static context (change settings to 'true' or 'false') (it's faster because of the compiler)
      */
     private static final boolean PRINT_NULLS_AS_BLANKS = false;
     private static final boolean TRIM_ON_PRINT = false;
 
     /**
      * Do your object configuration in the constructor (uncomment the resources you need.)
      */    
    private Scanner scan;
    private BufferedReader read;
 
    private test() {
	//scan = new Scanner("test.in");
	//read = new BufferedReader(new FileReader("test.in"));
     }
 
     public void doWorkHere() {
 	// |------------
 	// |
 	// | Replace me.
 	// |
 	// |------------
     }
 
     public static void main(String[] args) throws Exception {
 	new $1().doWorkHere();
     }
 
     public void print(Object o) {
 	if (null == o) { o = (PRINT_NULLS_AS_BLANKS ? "" : "null"); }
 	else if (TRIM_ON_PRINT) { o = o.toString().trim(); }
 	System.out.print(o);
     }
 
     public void println(Object o) {
 	if (null == o) { o = (PRINT_NULLS_AS_BLANKS ? "" : "null"); }
 	else if (TRIM_ON_PRINT) { o = o.toString().trim(); }
 	System.out.println(o);
     }
 
     public void println() {
 	System.out.println();
     }
 
     public void log(Object o) {
 	if (null == o) { o = (PRINT_NULLS_AS_BLANKS ? "" : "null"); }
 	else if (TRIM_ON_PRINT) { o = o.toString().trim(); }
 	System.err.print(o);
     }
 
     public void logln(Object o) {
 	if (null == o) { o = (PRINT_NULLS_AS_BLANKS ? "" : "null"); }
 	else if (TRIM_ON_PRINT) { o = o.toString().trim(); }
 	System.err.println(o);
     }
 
     public void logln() {
 	System.err.println();
     }
 }
