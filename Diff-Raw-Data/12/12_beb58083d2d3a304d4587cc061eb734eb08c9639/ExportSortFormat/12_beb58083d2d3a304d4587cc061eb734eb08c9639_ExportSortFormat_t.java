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
            if(ss[ss.length-1].length() == 0) {
                 continue;
             }
            for(int i = 0; i < ss.length - 1; i++) {
                out.print(ss[0]);
                out.print(":");
            }
            final String[] tkIds = ss[ss.length-1].split(" ");
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
