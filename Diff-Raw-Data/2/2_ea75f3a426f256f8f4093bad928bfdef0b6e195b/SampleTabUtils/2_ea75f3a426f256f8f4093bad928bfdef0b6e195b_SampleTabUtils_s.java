 package uk.ac.ebi.fgpt.sampletab.utils;
 
 import java.io.File;
 
 public class SampleTabUtils {
 
     public static File submissionDir;
 
     public static String getPathPrefix(String submissionId){
         if (submissionId.startsWith("GMS-")) return "imsr";
         else if (submissionId.startsWith("GAE-")) return "ae";
         else if (submissionId.startsWith("GRP-")) return "pride";
         else if (submissionId.startsWith("GVA-")) return "dgva";
         else if (submissionId.startsWith("GCR-")) return "coriell";
         else if (submissionId.startsWith("GEN-")) return "sra";
         else if (submissionId.equals("GEN")) return "encode";
         else if (submissionId.equals("G1K")) return "g1k";
        else if (submissionId.equals("GHM")) return "hapmap";
         else throw new IllegalArgumentException("Unable to get path prefix for "+submissionId);
     }
     
     public static File getSubmissionFile(String submissionId){
         File subdir = new File(submissionDir, getPathPrefix(submissionId));
         File subsubdir = new File(subdir, submissionId);
         File sampletabFile = new File(subsubdir, "sampletab.txt");
         return sampletabFile;
     }
     
 }
