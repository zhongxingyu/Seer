 package it.unipd.dei.webqual.converter.merge;
 
 import it.unipd.dei.webqual.converter.Utils;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * This class takes as input a set of adjacency list files and merges them
  * removing duplicates
  */
 public class GraphMerger {
 
   public static void sort(String inPath, String outPath, int idLen) throws IOException {
     List<Pair> pairs = new LinkedList<>();
 
     // the `true` parameter is for resetting the first bit of the IDs
     LazyFilePairIterator it = new LazyFilePairIterator(inPath, idLen);
     while(it.hasNext()) {
       pairs.add(it.next());
     }
 
     Collections.sort(pairs);
     writePairs(outPath, pairs);
   }
 
   private static void writePairs(String outPath, List<Pair> pairs)
     throws IOException {
 
     OutputStream out = new BufferedOutputStream(new FileOutputStream(outPath));
     for(Pair pair : pairs) {
       out.write(Utils.setHead(pair.head));
       for(byte[] neigh : pair.neighbours) {
         out.write(neigh);
       }
     }
     out.close();
   }
 
   private static final int GROUP_BY = 4;
   private static final int ID_LEN = 16;
   private static final String tmpDir = "/tmp/graph-merger/";
   private static final String sortedTmpPrefix = "sorted-";
   private static final PairComparator PAIR_COMPARATOR = new PairComparator();
   private static final PairMerger PAIR_MERGER = new PairMerger();
 
   public static File[] sortFiles(File[] inFiles) throws IOException {
     File[] sortedFiles = new File[inFiles.length];
     for(int i = 0; i<inFiles.length; i++) {
       String sortedName = tmpDir + sortedTmpPrefix + inFiles[i].getName();
       sortedFiles[i] = new File(sortedName);
       sort(inFiles[i].getCanonicalPath(), sortedName, ID_LEN);
     }
     return sortedFiles;
   }
 
   public static void main(String[] args) throws IOException {
 
     if(args.length != 2) {
       System.err.println("USAGE: graph-merger input_dir output_file");
      System.exit(1);
     }
 
     String inputDir = args[0];
     String outputName = args[1];
 
     new File(tmpDir).mkdir();
 
     File[] inFiles = new File(inputDir).listFiles();
 
     File[] sortedFiles = sortFiles(inFiles);
 
     mergeFiles(sortedFiles, outputName, GROUP_BY);
   }
 
   private static void mergeFiles(File[] sortedFiles, String outputName, int groupBy) throws IOException {
     if(groupBy < 2) {
       throw new IllegalArgumentException("groupBy should be >= 2");
     }
     if(sortedFiles.length <= groupBy) {
       mergeFiles(sortedFiles, outputName);
     }
 
     File[] tmpFiles = new File[sortedFiles.length / groupBy];
 
     for(int i = 0; i < tmpFiles.length; i++) {
       tmpFiles[i] = File.createTempFile("graph-merger", "merging");
 
       File[] group = Arrays.copyOfRange(
         sortedFiles, i*groupBy, Math.min((i + 1)*groupBy, sortedFiles.length));
 
       mergeFiles(group, tmpFiles[i].getCanonicalPath());
     }
   }
 
   /**
    * Creates a single lazy iterator over the merged files and uses it to create
    * the real merge file.
    * @param sortedFiles
    * @param outputName
    */
   private static void mergeFiles(File[] sortedFiles, String outputName) throws IOException {
     LazyMergeIterator<Pair>[] iterators = new LazyMergeIterator[sortedFiles.length/2];
     for(int i=0; i<iterators.length; i++) {
       if((2*i+1) < sortedFiles.length) {
         iterators[i] = new LazyMergeIterator<>(
           new LazyFilePairIterator(sortedFiles[2*i].getCanonicalPath(), ID_LEN),
           new LazyFilePairIterator(sortedFiles[2*i+1].getCanonicalPath(), ID_LEN),
           PAIR_COMPARATOR, PAIR_MERGER);
       } else {
         iterators[i] = new LazyMergeIterator<>(
           new LazyFilePairIterator(sortedFiles[2*i].getCanonicalPath(), ID_LEN),
           PAIR_COMPARATOR, PAIR_MERGER);
       }
     }
 
     LazyMergeIterator<Pair> it = LazyMergeIterator.compose(PAIR_COMPARATOR, PAIR_MERGER, iterators);
 
     OutputStream out = new BufferedOutputStream(new FileOutputStream(outputName));
     while(it.hasNext()) {
       Pair pair = it.next();
       out.write(Utils.setHead(pair.head));
       for(byte[] neigh : pair.neighbours) {
         out.write(neigh);
       }
     }
     out.close();
   }
 
 }
