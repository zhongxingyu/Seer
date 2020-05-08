 package it.unipd.dei.webqual.converter;
 
 import it.unimi.dsi.big.webgraph.ImmutableGraph;
 import it.unimi.dsi.big.webgraph.LazyLongIterator;
 import it.unimi.dsi.big.webgraph.NodeIterator;
 import it.unimi.dsi.logging.ProgressLogger;
 
 import java.io.*;
 import java.util.ArrayList;
 
 public class GraphSplitter {
 
   public static File[] split(ImmutableGraph graph, File dir, int chunkSize, ProgressLogger pl) throws IOException {
     pl.start("==== Splitting " + graph.basename());
 
     ArrayList<File> files = new ArrayList<>();
     File currentFile = File.createTempFile("chunk-", "", dir);
     DataOutputStream dos = new DataOutputStream(
       new BufferedOutputStream(new FileOutputStream(currentFile)));
 
     NodeIterator it = graph.nodeIterator();
     int i = 1;
     while(it.hasNext()) {
       pl.update();
       long node = it.nextLong();
       dos.writeLong((Utils.setHead(node)));
       long outDegree = it.outdegree();
       LazyLongIterator succs = it.successors();
       while (outDegree-- != 0) {
         long succ = succs.nextLong();
         dos.writeLong(Utils.reset(succ));
       }
 
       if(i % chunkSize == 0) {
         dos.close();
         files.add(currentFile);
         currentFile = File.createTempFile("chunk-", "", dir);
         dos = new DataOutputStream(
           new BufferedOutputStream(new FileOutputStream(currentFile)));
       }
       i++;
     }
 
     pl.stop("Done splitting");
 
     return files.toArray(new File[files.size()]);
   }
 
 }
