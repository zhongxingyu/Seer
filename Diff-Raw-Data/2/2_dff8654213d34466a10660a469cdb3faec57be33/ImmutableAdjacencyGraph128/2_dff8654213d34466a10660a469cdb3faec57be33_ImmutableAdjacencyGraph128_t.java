 package it.unipd.dei.webqual.converter;
 
 import it.unimi.dsi.big.webgraph.ImmutableGraph;
 import it.unimi.dsi.big.webgraph.ImmutableSequentialGraph;
 import it.unimi.dsi.big.webgraph.NodeIterator;
 import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
 import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
 import it.unimi.dsi.fastutil.longs.LongBigArrays;
 import it.unimi.dsi.logging.ProgressLogger;
 
 import java.io.*;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 public class ImmutableAdjacencyGraph128 extends ImmutableSequentialGraph {
 
   /** Length in bytes of the IDs */
   public static final int ID_LEN = 16;
 
   /** `long` mask with the first bit set */
   public static final long HEAD_MASK_L = 1L << 63;
 
   /** `long` mask with all the bits other than the most significant set. */
   public static final long RESET_MASK = ~HEAD_MASK_L;
 
   /** `byte` mask with the most significant bit set. */
   public static final byte HEAD_MASK = (byte) (1 << 7);
 
   /** The name of the file storing the graph. */
   private final String filename;
 
   /** The number of nodes of the graph. */
   private final long numNodes;
 
   /** A map between the original IDs of the graph and IDs in the range `[0, numNodes]` */
   private final Map<Long, Integer> map;
 
   private ImmutableAdjacencyGraph128( final CharSequence filename ) throws IOException {
     System.out.println("Creating ImmutableAdjacencyGraph128");
     this.filename = filename.toString();
     this.map = new Long2IntOpenHashMap();
     this.numNodes = countNodes();
   }
 
   protected long countNodes() throws IOException {
     DataInputStream dis = new DataInputStream(
       new BufferedInputStream(new FileInputStream(this.filename)));
 
     int cnt = 0;
 
     int read;
     byte[] buf = new byte[ID_LEN];
 
     while (true) { // while true: it would be `while (read == 128)` but this way we make only one comparison
       read = dis.read(buf);
       if(read == ID_LEN){
         if(isHead(buf)) {
           long id = reset(getLong(buf));
           map.put(id, cnt);
           cnt++;
           if(cnt % 1000 == 0) {
            System.out.printf("%d - %d bytes left\r", cnt, dis.available());
           }
         }
       } else {
         break;
       }
     }
 
     System.out.println();
     dis.close();
     if(read != -1) { // -1 means stream exhausted
       throw new IllegalStateException("The last ID was not of " + ID_LEN + " bytes");
     }
     return cnt;
   }
 
   /**
    * Creates a `long` from the first 8 bytes of the 16 bytes ID.
    */
   protected static long getLong(byte[] id128) {
     long l = 0;
     // Loop manually unrolled
     // for (int i = 0; i < 8; i++)
     // {
     //   l = (l << 8) + (id128[i] & 0xff);
     // }
     l = (l << 8) + (id128[0] & 0xff);
     l = (l << 8) + (id128[1] & 0xff);
     l = (l << 8) + (id128[2] & 0xff);
     l = (l << 8) + (id128[3] & 0xff);
     l = (l << 8) + (id128[4] & 0xff);
     l = (l << 8) + (id128[5] & 0xff);
     l = (l << 8) + (id128[6] & 0xff);
     l = (l << 8) + (id128[7] & 0xff);
     return l;
   }
 
   protected static boolean isHead(byte[] id) {
     return (id[0] & HEAD_MASK) == HEAD_MASK;
   }
 
   protected static boolean isHead(long id) {
     return (id & HEAD_MASK_L) == HEAD_MASK_L;
   }
 
   protected static long reset(long id) {
     return id & RESET_MASK;
   }
 
   protected long resetMap(long id) {
     Integer l = map.get(reset(id));
     if (l == null) {
       return -1;
     } else {
       return l;
     }
   }
 
   @Override
   public long numNodes() {
     return numNodes;
   }
 
   public static ImmutableGraph load( final CharSequence basename, final ProgressLogger pl ) {
     throw new UnsupportedOperationException( "Graphs may be loaded offline only" );
   }
 
   public static ImmutableGraph load( final CharSequence basename ) {
     return load( basename, (ProgressLogger)null );
   }
 
   public static ImmutableGraph loadSequential( final CharSequence basename, final ProgressLogger pl ) {
     return load( basename, pl );
   }
 
   public static ImmutableGraph loadSequential( final CharSequence basename ) {
     return load( basename, (ProgressLogger)null );
   }
 
   public static ImmutableGraph loadOffline( final CharSequence basename, final ProgressLogger pl ) throws IOException {
     return new ImmutableAdjacencyGraph128( basename );
   }
 
   public static ImmutableGraph loadOffline( final CharSequence basename ) throws IOException {
     return loadOffline( basename, (ProgressLogger)null );
   }
 
   public NodeIterator nodeIterator() {
     try {
       return new NodeIterator() {
 
         final DataInputStream dis = new DataInputStream(
           new BufferedInputStream(new FileInputStream(filename)));
         long outdegree;
         long[][] successors = LongBigArrays.EMPTY_BIG_ARRAY;
         long nextId = -1;
 
         {
           byte[] firstId = new byte[ID_LEN];
           dis.read(firstId);
           nextId = resetMap(getLong(firstId));
         }
 
         @Override
         public long nextLong() {
           if(!hasNext()) throw new NoSuchElementException();
           successors = LongBigArrays.ensureCapacity(successors, 10000); // magic number! tweak for efficiency
           outdegree = 0;
           long currentId = nextId;
 
           try {
             // now read the adjacency
             byte[] buf = new byte[ID_LEN];
             while(dis.available() > 0) {
               dis.read(buf);
               // assign the next long if we are on a head
               if(isHead(buf)) {
                 nextId = resetMap(getLong(buf));
                 break;
               } else {
                 long mapped = resetMap(getLong(buf));
                 if(mapped >= 0) {
                   LongBigArrays.set(successors, outdegree++, mapped);
                 }
               }
             }
 
             successors = LongBigArrays.trim(successors, outdegree);
 
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
