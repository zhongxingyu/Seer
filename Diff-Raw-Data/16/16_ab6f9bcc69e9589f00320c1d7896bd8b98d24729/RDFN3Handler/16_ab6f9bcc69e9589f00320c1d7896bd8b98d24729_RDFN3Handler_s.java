 package com.github.egonw.moldbvalid;
 
 import java.io.*;
 import java.util.*;
 
 import org.openscience.cdk.validate.*;
 
 public class RDFN3Handler implements IReportHandler {
 
   private PrintStream output;
 
   private int fileCounter = 0;
   private int molCounter = 0;
   private int errorCounter = 0;
   private int typeCounter = 0;
   private String currentSubject = "";
 
   private Map<IValidationTestType,Integer> typesAlreadyProcessed;
 
   public RDFN3Handler(OutputStream output) {
     if (output instanceof PrintStream) {
       this.output = (PrintStream)output;
     } else {
       this.output = new PrintStream(output);
     }
     typesAlreadyProcessed = new HashMap<IValidationTestType,Integer>();
   }
 
   public void setFile(String filename) {
     fileCounter++;
     output.println(":file" + fileCounter + " rdfs:label \"" + filename + "\" .");
   }
 
   public void setSubject(String subject) {
     this.molCounter++;
     this.currentSubject = ":mol" + molCounter;
     output.println(":file" + fileCounter + " :describes " + currentSubject + " .");
     output.println(currentSubject + "  rdfs:label \"" + subject + "\" .");
   };
 
   public void handleError(String type, ValidationTest test) {
     errorCounter++;
     String error = ":hasError" + errorCounter;
     output.println(currentSubject + " " + type + " " + error + " .");
     int currentType = 0;
     IValidationTestType testType = test.getType();
     if (typesAlreadyProcessed.containsKey(testType)) {
       currentType = typesAlreadyProcessed.get(testType);
     } else {
       typeCounter++;
       currentType = typeCounter;
       typesAlreadyProcessed.put(testType, currentType);
       // and also output to N3
       String typeType = ":ValidationTestType" + typeCounter;
       output.println(typeType + " rdfs:label \"" + testType.getError() + "\" ;");
     }
     String typeType = ":ValidationTestType" + typeCounter;
     output.print(error + " a " + typeType);
     if (test.getDetails().length() > 0) {
       output.println(" .");
      output.println(" :details \"" + test.getDetails());
     }
     output.println(" .");
   }
 
 }
 
