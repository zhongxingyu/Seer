 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eu.monnetproject.bliss.experiments;
 
 import eu.monnetproject.bliss.CLIOpts;
 import eu.monnetproject.bliss.WordMap;
 import java.io.File;
 import java.io.PrintStream;
 import java.util.Scanner;
 
 /**
  *
  * @author jmccrae
  */
 public class ExportSortFormat {
     public static void main(String[] args) throws Exception {
         final CLIOpts opts = new CLIOpts(args);
         final File wordMapFile = opts.roFile("wordMap", "The word map");
         final File corpusFile = opts.roFile("corpus", "The corpus");
         final PrintStream out = opts.outFileOrStdout();
         if(!opts.verify(ExportSortFormat.class)) {
             return;
         }
         final int W = WordMap.calcW(wordMapFile);
         final String[] wordMap = WordMap.inverseFromFile(wordMapFile, W, true);
         final Scanner in = new Scanner(CLIOpts.openInputAsMaybeZipped(corpusFile));
         while(in.hasNextLine()) {
             final String line = in.nextLine();
             final String[] ss = line.split(":");
            if(ss.length != 2) {
                System.err.println("Bad line: " + line);
                 continue;
             }
            out.print(ss[0]);
            out.print(":");
            final String[] tkIds = ss[1].split(" ");
             for(String tkId : tkIds) {
                 out.print(wordMap[Integer.parseInt(tkId)]);
                 out.print(" ");
             }
             out.println();
         }
         out.flush();
         out.close();
         in.close();
     }
 }
