 package it.unipd.dei.webqual.converter.merge;
 
 import com.codahale.metrics.Timer;
 import it.unimi.dsi.logging.ProgressLogger;
 import it.unipd.dei.webqual.converter.Checks;
 import it.unipd.dei.webqual.converter.Metrics;
 import it.unipd.dei.webqual.converter.Utils;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * This class takes as input a set of adjacency list files and merges them
  * removing duplicates
  */
 public class GraphMerger {
 
   private static final Logger log = LoggerFactory.getLogger(GraphMerger.class);
 
   private static final PairComparator PAIR_COMPARATOR = new PairComparator();
 
   public static void sort(File inPath, File outPath, Comparator<byte[]> comparator, int idLen) throws IOException {
     log.info("Sorting {}", inPath);
     Timer timer = Metrics.fileSortingTimer;
     Timer.Context context = timer.time();
     List<Pair> pairs = new LinkedList<>();
 
     // the `true` parameter is for resetting the first bit of the IDs
     LazyFilePairIterator it = new LazyFilePairIterator(inPath, idLen, comparator);
     while(it.hasNext()) {
       pairs.add(it.next());
     }
 
     Collections.sort(pairs, PAIR_COMPARATOR);
 
     // remove duplicates from the collection itself
     Iterator<Pair> deduplicated = removeLocalDuplicates(pairs, comparator);
 
     writeIterator(outPath, deduplicated);
     long time = context.stop();
     log.info("{} sorted, elapsed time: {} seconds", inPath, time / 1000000000);
   }
 
   private static Iterator<Pair> removeLocalDuplicates(List<Pair> pairs, Comparator<byte[]> comparator) {
     Iterator<Pair>
       first = pairs.iterator(),
       second = pairs.iterator();
 
     return new LazyMergeIterator<>(first, second, PAIR_COMPARATOR, new PairMerger(comparator));
   }
 
   private static void writeIterator(File outPath, Iterator<Pair> pairs) throws IOException {
     OutputStream out = new BufferedOutputStream(new FileOutputStream(outPath));
     while(pairs.hasNext()) {
       Pair pair = pairs.next();
       out.write(Utils.setHead(pair.head));
       for(byte[] neigh : pair.neighbours) {
         out.write(neigh);
       }
     }
     out.close();
   }
 
   public static File[] sortFiles(File[] inFiles, int idLen, Comparator<byte[]> comparator) throws IOException {
     log.info("============= Sorting files ===============");
     Timer timer = Metrics.totalSortingTimer;
     Timer.Context context = timer.time();
     File[] sortedFiles = new File[inFiles.length];
     for(int i = 0; i<inFiles.length; i++) {
       sortedFiles[i] = File.createTempFile("graph-merger", "sorting"+inFiles[i].getName());
       sort(inFiles[i], sortedFiles[i], comparator, idLen);
     }
     long time = context.stop();
     log.info("====== Files sorted, elapsed time: {} seconds", time / 1000000000);
     return sortedFiles;
   }
 
   public static File mergeFiles( final File[] sortedFiles,
                                           final File outputName,
                                           final int groupBy,
                                           final int idLen,
                                           final Comparator<byte[]> comparator,
                                           final int recursionLevel) throws IOException {
     if(groupBy < 2) {
       throw new IllegalArgumentException("groupBy should be >= 2");
     }
     if(sortedFiles.length <= groupBy) {
       log.info("Recursion level {}. Merging {} files.",
         recursionLevel, sortedFiles.length);
       return mergeFiles(sortedFiles, outputName, idLen, comparator);
     }
 
     File[] tmpFiles = new File[sortedFiles.length / groupBy];
 
     for(int i = 0; i < tmpFiles.length; i++) {
       tmpFiles[i] = File.createTempFile("graph-merger", "merging");
 
       File[] group = Arrays.copyOfRange(
         sortedFiles, i*groupBy, Math.min((i + 1)*groupBy, sortedFiles.length));
 
       log.info("Recursion level {}. Merging {} out of {} files.",
         recursionLevel, group.length, sortedFiles.length);
       tmpFiles[i] = mergeFiles(group, tmpFiles[i], groupBy, idLen, comparator, recursionLevel+1);
     }
 
     log.info("Recursion level {}. Merging temporary files together", recursionLevel);
     return mergeFiles(tmpFiles, outputName, groupBy, idLen, comparator, recursionLevel+1);
   }
 
   /**
    * Creates a single lazy iterator over the merged files and uses it to create
    * the real merge file.
    * @param sortedFiles
    * @param output
    */
   private static File mergeFiles(File[] sortedFiles, File output, int idLen, Comparator<byte[]> comparator) throws IOException {
     Timer timer = Metrics.fileMergingTimer;
     Timer.Context context = timer.time();
     LazyMergeIterator<Pair>[] iterators = new LazyMergeIterator[sortedFiles.length/2];
     for(int i=0; i<iterators.length; i++) {
       if((2*i+1) < sortedFiles.length) {
         iterators[i] = new LazyMergeIterator<>(
           new LazyFilePairIterator(sortedFiles[2*i], idLen, comparator),
           new LazyFilePairIterator(sortedFiles[2*i+1], idLen, comparator),
           PAIR_COMPARATOR, new PairMerger(comparator));
       } else {
         iterators[i] = new LazyMergeIterator<>(
           new LazyFilePairIterator(sortedFiles[2*i], idLen, comparator),
           PAIR_COMPARATOR, new PairMerger(comparator));
       }
     }
 
     LazyMergeIterator<Pair> it = LazyMergeIterator.compose(PAIR_COMPARATOR, new PairMerger(comparator), iterators);
     writeIterator(output, it);
 
     long time = context.stop();
     log.info("{} files merged, elapsed time: {} seconds", sortedFiles.length, time/1000000000);
 
     return output;
   }
 
   public static void main(String[] args) throws IOException, InterruptedException {
 
     Options opts = new Options();
     CmdLineParser parser = new CmdLineParser(opts);
     try {
       parser.parseArgument(args);
     } catch (CmdLineException e) {
       System.err.println(e.getMessage());
       parser.printUsage(System.err);
       System.exit(1);
     }
 
     File[] inFiles = opts.inputDir.listFiles();
 
     File[] sortedFiles = (opts.noSort)? inFiles : sortFiles(inFiles, opts.idLen, new ArrayComparator());
 
     if(!opts.noCheckSort) {
      Checks.checkSorted(sortedFiles, opts.idLen, new ArrayComparator(), new ProgressLogger());
     }
 
     log.info("============= Merging files ===============");
     Timer timer = Metrics.totalMergingTimer;
     Timer.Context context = timer.time();
     File outFile = mergeFiles(sortedFiles, new File(opts.outputName), opts.groupBy, opts.idLen, new ArrayComparator(), 0);
     long time = context.stop();
     log.info("====== Files merged, elapsed time: {} seconds", time/1000000000);
 
 
     log.info("{} duplicates have been merged", Metrics.duplicatesCounter.getCount());
 
     if(!opts.noCheckDup) {
       Checks.checkDuplicates(outFile, opts.idLen, new ProgressLogger());
     }
 
     Metrics.report();
     Metrics.reset();
   }
 
   public static class Options {
 
     @Option(aliases="--input-dir", name="-i", usage="The input directory", required=true, metaVar="FILE")
     File inputDir;
 
     @Option(aliases="--out", name="-o", usage="The output file", required=true, metaVar="FILE")
     String outputName;
 
     @Option(aliases="--group-by", name="-g", usage="Merge files in chunks of N files", metaVar="N")
     int groupBy = 2;
 
     @Option(aliases="--id-len", name="-l", usage="The length of the IDs in bytes", metaVar="N")
     int idLen = 16;
 
     @Option(name="--no-sort", usage="Skip sorting phase. Input must be already sorted")
     boolean noSort = false;
 
     @Option(name="--no-check-sort", usage="Skip checking of input files")
     boolean noCheckSort = false;
 
     @Option(name="--no-check-duplicate", usage="Skip checking of input files")
     boolean noCheckDup = false;
 
   }
 
 }
