 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.HashSet;
 
 class diff {
     // quick ref: java diff sents.ans sents.out model_file report
 	public static void main(String args[]) {
 	    
 	    if (args.length != 4) {
 	        System.out.println("error: Wrong number of arguments.");
 	        System.out.println("usage: java diff <sents.ans> <sents.out> <model_file> <report>");
 	        System.exit(1);
         }
         // take in params
         String ansFile = args[0];
         String outFile = args[1];
         String modelFile = args[2];
         String reportFile = args[3];
         
         try {
             // evaluate output against answers
             FileReader outReader = new FileReader(outFile);
             BufferedReader outBr = new BufferedReader(outReader);
 
             FileReader ansReader = new FileReader(ansFile);
             BufferedReader ansBr = new BufferedReader(ansReader);
             
             FileReader modelReader = new FileReader(modelFile);
             BufferedReader modelBr = new BufferedReader(modelReader);
             
             FileWriter reportWriter = new FileWriter(reportFile);
             BufferedWriter reportBw = new BufferedWriter(reportWriter);
             
             String modelLine;
             int lineCount = 1;
             Set<String> wordSet = new HashSet<String>();
             while ((modelLine = modelBr.readLine()) != null) {
                 if (lineCount == 2) {
                     String[] modelTokens = modelLine.trim().split("\\s+");
                     for (int i = 0; i < modelTokens.length; i++)
                         wordSet.add(modelTokens[i]);
                 }   
                 lineCount ++;
             }
             
             String outLine;
             String ansLine;
             int correctCount = 0;
             int totalCount = 0;
             while ((outLine = outBr.readLine()) != null) {
                 ansLine = ansBr.readLine();
                String oov = "";
 	            String[] outTokens = outLine.trim().split("\\s+");
 	            String[] ansTokens = ansLine.trim().split("\\s+");
                 for (int k = 0; k < outTokens.length; k++) {
                     // for each "word/tag" token, break it by "/"
                     // the last entity will be the tag
                     // everything before the last "/" will be the word
                     String[] wordAndTag = outTokens[k].split("/");
                     String word = wordAndTag[0];
                     String tag = wordAndTag[wordAndTag.length - 1];
                     for (int j = 0; j < wordAndTag.length; j++) {
                         if (j != 0 && j < wordAndTag.length - 1) {
                             word += "/" + wordAndTag[j];
                         }
                     }
                     if (!wordSet.contains(word)) {
                         oov = "[OOV]";
                     }
                     totalCount += 1;
                     if (outTokens[k].equals(ansTokens[k])) {
                         correctCount += 1;
                     }
                     else {
                         reportBw.write( ansTokens[k] + " <=> " + outTokens[k] + " " + oov );
                         reportBw.newLine();
                     }
                 }
             }
             outBr.close();
             ansBr.close();
             modelBr.close();
             reportBw.close();
             
             double acc = correctCount / (double) totalCount;
             System.out.println("Acc = " + String.format("%.2f", acc * 100) + "%");
         } catch (Exception e) {
             System.err.println("Error: " + e.getMessage());
         }
 	}
 }
