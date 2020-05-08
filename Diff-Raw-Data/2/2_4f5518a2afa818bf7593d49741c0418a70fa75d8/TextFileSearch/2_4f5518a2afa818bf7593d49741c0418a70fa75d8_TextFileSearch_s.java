 package com.tritowntim.ga;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 
 /**
  *
  * @author Tim Dussinger
  */
 public class TextFileSearch {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws IOException {
         InputValidator inputValidator = new InputValidator();
         inputValidator.validateInput(args);
         
         FileReader fileReader = new FileReader();
         String fileContents = fileReader.readFile(args[0]);
         
         WordSearcher wordSearcher = new WordSearcher();
         ArrayList<Integer> instances = wordSearcher.findInstances(fileContents, args[1]);
         int instanceCount = instances.size();
         
         BigDecimal avgWordsBtwn = wordSearcher.countAvgWordsBtwnInstances(instances, fileContents, args[1]);
         
         System.out.println("Found " + instanceCount + " occurrence" + (instanceCount == 1 ? "" : "s") + " of '" + args[1] + "' within " + args[0]);
        System.out.println("Average of " + avgWordsBtwn + " word" + (avgWordsBtwn.equals(BigDecimal.ONE) ? "" : "s") + " between each occurrence " + " of '" + args[1] + "' within " + args[0]);
         
     }
 }
