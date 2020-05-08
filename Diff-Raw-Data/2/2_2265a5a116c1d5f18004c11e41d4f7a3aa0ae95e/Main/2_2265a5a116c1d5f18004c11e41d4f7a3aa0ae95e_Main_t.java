 package it.unipd.dei.webqual.converter;
 
 import it.unimi.dsi.big.webgraph.*;
 import it.unimi.dsi.fastutil.Function;
 import it.unimi.dsi.logging.ProgressLogger;
 import it.unipd.dei.webqual.converter.merge.ArrayComparator;
 import it.unipd.dei.webqual.converter.merge.ArrayLongComparator;
 import it.unipd.dei.webqual.converter.merge.GraphMerger;
 import it.unipd.dei.webqual.converter.sort.GraphSorter;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 import java.io.*;
 import java.util.Comparator;
 
 public class Main {
 
   public static void main(String[] args) throws IOException {
     Options opts = new Options();
     CmdLineParser parser = new CmdLineParser(opts);
     try {
       parser.parseArgument(args);
     } catch (CmdLineException e) {
       System.err.println(e.getMessage());
       parser.printUsage(System.err);
       System.exit(1);
     }
 
     Comparator<byte[]> comparator = new ArrayLongComparator();
 
     ProgressLogger pl = new ProgressLogger();
     pl.displayFreeMemory = true;
 
     File[] sortedChunks;
    if(!opts.skipSplitting){
 
       pl.logger().info("Building hash function");
       Function<byte[], Long> mapFunc =
         FunctionFactory.buildDeterministicMap(opts.inputGraph, opts.idLen, pl);
   //    String mphSerializedName = opts.inputGraph + "-mph";
   //    pl.logger().info("Storing hash function to {}", mphSerializedName);
   //    serialize(mphSerializedName, mapFunc);
 
       if(opts.intermediateChecks)
         Checks.checkMap(opts.inputGraph, opts.idLen, mapFunc, pl);
 
       pl.logger().info("Loading graph from {}", opts.inputGraph);
       ImmutableGraph originalGraph =
         ImmutableAdjacencyGraph.loadOffline(opts.inputGraph.getCanonicalPath(), opts.idLen, mapFunc, pl);
 
       pl.logger().info("Sorting graph");
       File[] chunks = GraphSplitter.split(originalGraph, opts.outputDir, opts.chunkSize, pl);
 
       sortedChunks = GraphMerger.sortFiles(chunks, 8, comparator);
     } else {
       sortedChunks = opts.outputDir.listFiles();
     }
 
     pl.logger().info("Merging files");
     File mergedFile;
     if(sortedChunks.length < 2) {
       mergedFile = sortedChunks[0];
     } else {
       mergedFile = GraphMerger.mergeFiles(sortedChunks, opts.outputFile, sortedChunks.length, 8, comparator, 0);
     }
 
     if(opts.intermediateChecks) {
       Checks.checkSorted(mergedFile, 8, comparator, pl);
       Checks.checkDuplicates(mergedFile, 8, pl);
     }
 
     pl.start("==== Loading graph from " + mergedFile);
     Function<byte[], Long> map =
       FunctionFactory.buildIdentity(mergedFile, pl);
     ImmutableGraph iag =
       ImmutableAdjacencyGraph.loadOffline(mergedFile.getCanonicalPath(), 8, map, pl);
     pl.stop("Loaded graph with " + iag.numNodes() + " nodes");
 
     if (opts.intermediateChecks)
       Checks.checkIncreasing(iag, pl);
 
     String efOut = opts.outputFile + "-ef";
 
     ImmutableGraph efGraph = Conversions.toBVGraph(iag, efOut, pl);
 
     if(opts.intermediateChecks)
       Checks.checkPositiveIDs(iag, pl);
 
     if(opts.intermediateChecks || opts.finalCheck)
       Checks.checkEquality(iag, efGraph, pl);
 
     pl.logger().info("All done");
 
 //    Metrics.report();
     Metrics.reset();
   }
 
   private static void serialize(String name, Object o) throws IOException {
     ObjectOutputStream oos =
       new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(name)));
     oos.writeObject(o);
     oos.close();
   }
 
   public static class Options {
 
     @Option(name = "-i", metaVar = "FILE", required = true, usage = "the input graph")
     public File inputGraph;
 
     @Option(name = "--id-len", metaVar = "N", usage = "the input ID length, defaults to 16")
     public int idLen = 16;
 
     @Option(name = "-d", metaVar = "OUTPUT_DIR", required = true, usage = "Output directory")
     public File outputDir;
 
     @Option(name = "-o", metaVar = "FILE", required = true, usage = "Output file")
     public File outputFile;
 
     @Option(name = "-c", metaVar = "CHUNK_SIZE", usage = "Chunk size for intermediate sorted files. Defaults to 300'000")
     public int chunkSize = 300_000;
 
     @Option(name = "--checks", usage = "Perform intermediate checks")
     public boolean intermediateChecks = false;
 
     @Option(name = "--final-check", usage = "Perform final equality checks")
     public boolean finalCheck = false;
 
     @Option(name = "--skip-splitting", usage = "Skips the splitting phase")
     public boolean skipSplitting = false;
 
   }
 
 }
