 package se.atoulou.facebook.hackercup.common;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 import com.google.common.io.CharStreams;
 
 public abstract class HackApp {
     private static final Logger logger = LoggerFactory.getLogger(HackApp.class);
 
     public final void actualRun(InputStream inputStream, OutputStream outputStream) {
         PrintWriter writer = new PrintWriter(new BufferedOutputStream(outputStream));
         List<String> inputLines = inputStreamToList(inputStream);
         run(writer, inputLines);
     }
 
     /**
      * Run the problem.
      * 
      * @param writer
      *            A {@link PrintWriter} to print output to.
      * @param inputLines
      *            A list of strings representing the input lines.
      */
     abstract protected void run(PrintWriter writer, List<String> inputLines);
 
     private List<String> inputStreamToList(InputStream inputStream) {
         String inputString;
         try {
             inputString = CharStreams.toString(new InputStreamReader(inputStream));
             logger.trace("Input String:" + inputString);
         } catch (IOException e) {
             logger.error("Error loading input file into string", e);
             throw new RuntimeException(e);
         }
 
        String[] rawLines = inputString.split("\\r?\\n"); // Alternatively
                                                          // (untested),
                                                          // "[\\r\\n]+" to skip
                                                          // empty lines
 
         int expectedInputLength = Integer.parseInt(rawLines[0]);
         final List<String> inputLines = Lists.newArrayListWithExpectedSize(expectedInputLength);
 
         // Not using String line : lines because I treat line #1 differently
         for (int i = 1; i < rawLines.length; i++) {
             // What if the input ends with an empty line?
             String line = rawLines[i];
             if (!line.isEmpty()) {
                 inputLines.add(line);
             }
 
            // Error if validation failed. Seems to be a pretty universal
            // constraint.
            if (i <= expectedInputLength) {
                logger.error("The number of input lines read doesn't match the expected number given in the data file!");
            }
         }
         return inputLines;
     }
 }
