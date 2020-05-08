 package it.unipd.dei.webqual.converter;
 
 import com.codahale.metrics.Counter;
 import com.codahale.metrics.Histogram;
 import it.unimi.dsi.big.webgraph.ImmutableGraph;
 import it.unimi.dsi.big.webgraph.ImmutableSequentialGraph;
 import it.unimi.dsi.big.webgraph.NodeIterator;
 import it.unimi.dsi.fastutil.Function;
 import it.unimi.dsi.fastutil.longs.LongBigArrays;
 import it.unimi.dsi.logging.ProgressLogger;
 
 import java.io.*;
 import java.util.NoSuchElementException;
 
 import static it.unipd.dei.webqual.converter.Utils.isHead;
 
 public class ImmutableAdjacencyGraph extends ImmutableSequentialGraph {
 
   /** Used to keep measures with the registry unique across calls */
   private int iteratorId = -1;
 
   /** Length in bytes of the IDs */
   private final int idLen;
 
   /** The name of the file storing the graph. */
   private final String filename;
 
   /** The number of nodes of the graph. */
   private final long numNodes;
 
   private final Function<byte[], Long> map;
 
   private final ProgressLogger pl;
 
   private ImmutableAdjacencyGraph( CharSequence filename,
                                    int idLen,
                                    Function<byte[], Long> mapFunction,
                                    ProgressLogger pl) {
     this.idLen = idLen;
     this.filename = filename.toString();
     this.pl = pl;
     this.map = mapFunction;
     this.numNodes = mapFunction.size();
 
     pl.logger().info("Loaded graph with {} nodes.", this.numNodes);
   }
 
   @Override
   public long numNodes() {
     return numNodes;
   }
 
   public static ImmutableGraph load( final CharSequence basename, final ProgressLogger pl ) {
     throw new UnsupportedOperationException( "Graphs may be loaded offline only" );
   }
 
   public static ImmutableGraph load( final CharSequence basename ) {
     return load( basename, null );
   }
 
   public static ImmutableGraph loadSequential( final CharSequence basename, final ProgressLogger pl ) {
     return load( basename, pl );
   }
 
   public static ImmutableGraph loadSequential( final CharSequence basename ) {
     return load( basename, null );
   }
 
   public static ImmutableGraph loadOffline( final CharSequence basename, final int idLen, final Function<byte[], Long> mapFunction, final ProgressLogger pl ) throws IOException {
     return new ImmutableAdjacencyGraph( basename, idLen, mapFunction, pl );
   }
 
   public static ImmutableGraph loadOffline( final CharSequence basename ) throws IOException {
     return loadOffline( basename, 16, FunctionFactory.buildMphf(new File(basename.toString()), 16, null), null );
   }
 
   public NodeIterator nodeIterator() {
     // increase count of iterators to keep measures unique across calls
     iteratorId++;
     try {
       return new NodeIterator() {
 
         Counter missingItems = Metrics.missingItemsCounter(filename, iteratorId);
         Histogram degreesDist = Metrics.degreeHistogram(filename, iteratorId);
 
         final DataInputStream dis = new DataInputStream(
           new BufferedInputStream(new FileInputStream(filename)));
         long outdegree;
         long[][] successors = LongBigArrays.EMPTY_BIG_ARRAY;
         long nextId = -1;
 
         {
           byte[] firstId = new byte[idLen];
           dis.read(firstId);
           if(!isHead(firstId)) {
             throw new NoSuchElementException(
               "The first element of the file is not a head");
           }
           nextId = map.get(firstId);
         }
 
         @Override
         public long nextLong() {
           if(!hasNext()) throw new NoSuchElementException();
           successors = LongBigArrays.EMPTY_BIG_ARRAY;
           successors = LongBigArrays.ensureCapacity(successors, 10000); // magic number! tweak for efficiency
           outdegree = 0;
           long currentId = nextId;
 
           try {
             // now read the adjacency
             byte[] buf = new byte[idLen];
             while(dis.available() > 0) {
               dis.read(buf);
               // assign the next long if we are on a head
               if(isHead(buf)) {
                 nextId = map.get(buf);
                 break;
               } else {
                 long mapped = map.get(buf);
                 if(mapped >= 0 && mapped < numNodes) {
                   LongBigArrays.set(successors, outdegree++, mapped);
                 } else {
                   missingItems.inc();
                   pl.logger().trace("Neighbour " + mapped + " of " + currentId + " out of range, skipping");
                 }
               }
             }
 
             successors = LongBigArrays.trim(successors, outdegree);
             degreesDist.update(outdegree);
 
           } catch (IOException e) {
             throw new RuntimeException(e);
           }
 
           return currentId;
         }
 
         @Override
         public long[][] successorBigArray() {
           return successors;
         }
 
         @Override
         public long outdegree() {
           return outdegree;
         }
 
         @Override
         public boolean hasNext() {
           try {
             return dis.available() > 0;
           } catch (IOException e) {
             return false;
           }
         }
 
         @Override
         protected void finalize() throws Throwable {
           dis.close();
           super.finalize();
         }
       };
     } catch (FileNotFoundException e) {
       throw new RuntimeException(e);
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
 
 }
