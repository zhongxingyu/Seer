 package common_exceptions;
 
 // This is in an external jar that's added to the classpath.
 import hello.*;
 
 /**
  * Examples of commonly seen exception -NoClassDefFoundError.
  * This exception occurs when the JVM can't find a class it needs because of a 
  * command-line error, a classpath issue or a missing .class file.
  * 
 * NOTE:  Too see the exception run from the commandline 
  * i.e. in eclipse: 
  * 			1.  compile using the jar file that contains the hello package
  * 			2.  cd to the folder containing NoClassDefFound.class
  * 			3.  attempt to run without the jar file i.e. 
  * 				 	from the directory above the package with the .class file in it
  * 					java -cp . common_exceptions.NoClassDefFound
  * 			4.  Note that the following will run successfully. 
  * 			    java -cp .:/absolute/path/to/HelloWorld.jar common_exceptions.NoClassDefFound
  * @author Tonisha Whyte
  *
  */
 public class NoClassDefFound {
 
 	/**
 	 * Accesses a class from another package.  To see the exception compile and
 	 * run when the classpath includes the assertions package then remove the 
 	 * .class file for the class used below and re-run.
 	 * @param args
 	 */
 	public static void main(String[] args) {
       Moi fromAJar = new Moi();
       fromAJar.inEnglish();
 	}
 
 }
