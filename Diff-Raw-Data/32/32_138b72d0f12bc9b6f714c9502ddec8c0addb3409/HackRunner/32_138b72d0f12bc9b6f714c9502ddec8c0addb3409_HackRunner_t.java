 package se.atoulou.facebook.hackercup.common;
 
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
 
        logger.debug("Preparing input file.");
         String inputPath = "se/atoulou/facebook/hackercup/beautifulstrings/input.txt";
         InputStream in = HackRunner.class.getClassLoader().getResourceAsStream(inputPath);

        logger.debug("Preparing output file.");
        OutputStream out;
        try {
            out = new FileOutputStream("output.txt");
        } catch (FileNotFoundException e) {
            logger.error("Error creating output file.", e);
            throw new RuntimeException(e);
        }
 
         HackApp app = new BeautifulStringsApp();
 
         logger.debug("Running HackApp ({}).", app.getClass().getSimpleName());
         app.actualRun(in, out);
     }
 }
