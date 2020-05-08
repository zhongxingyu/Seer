 package it.unipd.dei.webqual.converter;
 
 import it.unimi.dsi.big.webgraph.ImmutableGraph;
 import it.unimi.dsi.big.webgraph.LazyLongIterator;
 import it.unimi.dsi.big.webgraph.NodeIterator;
 import it.unimi.dsi.logging.ProgressLogger;
 import it.unipd.dei.webqual.converter.merge.ArrayComparator;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 
 public class Checks {
 
   public static boolean checkEquality(ImmutableGraph a, ImmutableGraph b, ProgressLogger pl) {
     pl.start("==== Checking for equality ====");
     if(!a.equals(b)) {
       pl.logger().error("Graphs are not equal!!!");
       pl.logger().info(a.numNodes() + " ?= " + b.numNodes());
       return false;
     }
     pl.stop("Check completed, no errors found, the graphs are equals :-)");
     return true;
   }
 
   public static boolean checkPositiveIDs(ImmutableGraph g, ProgressLogger pl) {
     pl.start("==== Checking for positive IDs ====");
     NodeIterator it = g.nodeIterator();
     while(it.hasNext()) {
       pl.update();
       long node = it.next();
       if(node < 0) {
        pl.logger().error("ID {} is negative", node);
         pl.stop();
        return false;
       }
       long outdeg = it.outdegree();
       LazyLongIterator succs = it.successors();
       while(outdeg-- != 0) {
         long s = succs.nextLong();
         if(s < 0) {
          pl.logger().error("ID {}, neighbour of {}, is negative", s, node);
           pl.stop();
          return false;
         }
       }
     }
     pl.stop("Check completed, no errors found, the graph has only positive IDs :-)");
     return true;
   }
 
   public static boolean checkDuplicates(File f, int idLen, ProgressLogger pl) throws IOException {
     pl.start("==== Checking for duplicates ====");
     ArrayComparator cmp = new ArrayComparator();
     AdjacencyHeadIterator it =
       new AdjacencyHeadIterator(f.getCanonicalPath(), idLen, true);
     byte[] last = it.next();
     while(it.hasNext()) {
       pl.update();
       byte[] cur = it.next();
       if(cmp.compare(cur, last) == 0) {
         throw new IllegalArgumentException(
           "File " + f + " contains duplicates: \n\t"+ Arrays.toString(last) +
             "\n\t" + Arrays.toString(cur));
       }
       last = cur;
     }
     pl.stop("Check completed, no errors found, the file contains no duplicate IDs :-)");
     return true;
   }
 
   public static boolean checkSorted(File[] sortedFiles, int idLen, ProgressLogger pl) throws IOException {
     for(int i=0; i<sortedFiles.length; i++) {
       System.out.printf("%f%%\r", ((double) i)/sortedFiles.length * 100);
       checkSorted(sortedFiles[i], idLen, pl);
     }
     return true;
   }
 
   public static boolean checkSorted(File f, int idLen, ProgressLogger pl) throws IOException {
     pl.start("Checking file " + f.getName());
     ArrayComparator cmp = new ArrayComparator();
     AdjacencyHeadIterator it =
       new AdjacencyHeadIterator(f.getCanonicalPath(), idLen, true);
     byte[] last = it.next();
     while(it.hasNext()) {
       pl.update();
       byte[] cur = it.next();
       if(cmp.compare(cur, last) < 0) {
         throw new IllegalArgumentException("File " + f + " is not sorted");
       }
       last = cur;
     }
     pl.stop(f.getName() + " is sorted :-)");
     return true;
   }
 
 }
