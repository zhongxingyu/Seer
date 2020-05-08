 package grepJar;
 
 import java.io.File;
 import java.util.regex.Pattern;
 
 /**
  * Entry point for the utility
  * @author Luke Sandberg
  * Oct 28, 2010
  */
 public class Main {
     public static void main(String[] args) {
         if (args.length > 2 || args.length < 1) {
             System.out.println("Usage: java -jar jarFinder.jar <regex> [directory]\n\t[Directory] defaults to the Current Dir if not provided.");
             return;
         }
 
         Pattern pat = Pattern.compile(args[0], Pattern.CASE_INSENSITIVE);
         File dir = new File(".");
         if (args.length == 2) {
            dir = new File(args[1]);
         }
 
         for (File jar : new JarFinder(dir)) {
             String sn;
             if ((sn = JarSearcher.search(jar, pat)) != null) {
                 System.out.println(jar.getPath() + "\t" + sn);
             }
 
         }
     }
 }
