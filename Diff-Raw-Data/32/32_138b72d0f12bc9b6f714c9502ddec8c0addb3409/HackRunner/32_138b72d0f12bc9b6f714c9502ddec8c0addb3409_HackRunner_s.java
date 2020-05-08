 package se.atoulou.facebook.hackercup.common;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import se.atoulou.facebook.hackercup.beautifulstrings.BeautifulStringsApp;
 
 /**
  * To run, 'mvn clean compile' followed by 'mvn exec:java' should do the trick.
  */
 public class HackRunner {
     private static final Logger logger = LoggerFactory.getLogger(HackRunner.class);
 
     public static void main(String[] args) {
         logger.error("That problem is not recognized!");
 
        logger.debug("Locating input file.");
         String inputPath = "se/atoulou/facebook/hackercup/beautifulstrings/input.txt";

        logger.debug("Preparing input/output streams.");
         InputStream in = HackRunner.class.getClassLoader().getResourceAsStream(inputPath);
        OutputStream out = System.out;
 
         HackApp app = new BeautifulStringsApp();
 
         logger.debug("Running HackApp ({}).", app.getClass().getSimpleName());
         app.actualRun(in, out);
     }
 }
