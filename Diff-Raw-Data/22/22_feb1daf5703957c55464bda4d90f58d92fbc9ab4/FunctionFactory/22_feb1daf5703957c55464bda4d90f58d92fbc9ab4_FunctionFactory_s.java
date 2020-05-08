 package it.unipd.dei.webqual.converter;
 
 import it.unimi.dsi.fastutil.Function;
 import it.unimi.dsi.logging.ProgressLogger;
 import it.unimi.dsi.sux4j.mph.HollowTrieDistributorMonotoneMinimalPerfectHashFunction;
 import it.unimi.dsi.sux4j.mph.HollowTrieMonotoneMinimalPerfectHashFunction;
 import it.unimi.dsi.sux4j.mph.LcpMonotoneMinimalPerfectHashFunction;
 import it.unimi.dsi.sux4j.mph.MinimalPerfectHashFunction;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Contains factory methods for various types of functions
  */
 public class FunctionFactory {
 
   public static Function<byte[], Long> buildIdentity(String file, ProgressLogger pl) throws IOException {
     AdjacencyHeads heads = new AdjacencyHeads(file, 8); // read longs
     final long size = heads.getCount();
 
     return new Function<byte[], Long>() {
       @Override
       public Long put(byte[] key, Long value) {
         throw new UnsupportedOperationException();
       }
 
       @Override
       public Long get(Object key) {
         byte[] bytes = (byte[]) key;
        return Utils.getLong(bytes);
       }
 
       @Override
       public boolean containsKey(Object key) {
         return false;
       }
 
       @Override
       public Long remove(Object key) {
         throw new UnsupportedOperationException();
       }
 
       @Override
       public int size() {
         return (int) size;
       }
 
       @Override
       public void clear() {
         //To change body of implemented methods use File | Settings | File Templates.
       }
     };
   }
 
   public static MinimalPerfectHashFunction<byte[]> buildMphf( String file,
                                                               int idLen,
                                                               ProgressLogger pl) throws IOException {
     if(pl != null)
       pl.start("Counting nodes and creating perfect hash function");
     AdjacencyHeads heads = new AdjacencyHeads(file, idLen, true);
     ByteArrayTransformationStrategy trStrat =
       new ByteArrayTransformationStrategy(idLen);
 
     MinimalPerfectHashFunction mph =
       new MinimalPerfectHashFunction<byte[]>(heads, trStrat);
 
     if(mph.size64() != heads.getCount()) {
       throw new IllegalStateException(
         "Number of elements counted by the iterator is different than the number " +
           "of elements counted by the  minimal perfect hash function");
     }
 
     if(pl != null)
       pl.logger().info("The perfect hash function is using {} bits", mph.numBits());
     return mph;
   }
 
   public static HollowTrieMonotoneMinimalPerfectHashFunction<byte[]> buildHollowTrieMonotoneMph( String file,
                                                                                                  int idLen,
                                                                                                  ProgressLogger pl) throws IOException {
     if(pl != null)
       pl.start("Counting nodes and creating perfect hash function");
     AdjacencyHeads heads = new AdjacencyHeads(file, idLen, true);
     ByteArrayTransformationStrategy trStrat =
       new ByteArrayTransformationStrategy(idLen);
 
     HollowTrieMonotoneMinimalPerfectHashFunction mph =
       new HollowTrieMonotoneMinimalPerfectHashFunction<byte[]>(heads, trStrat);
 
     if(mph.size64() != heads.getCount()) {
       throw new IllegalStateException(
         "Number of elements counted by the iterator is different than the number " +
           "of elements counted by the  minimal perfect hash function");
     }
 
     if(pl != null)
       pl.logger().info("The perfect hash function is using {} bits", mph.numBits());
     return mph;
   }
 
   public static HollowTrieDistributorMonotoneMinimalPerfectHashFunction<byte[]> buildHollowTrieDistributorMonotoneMph( String file,
                                                                                                  int idLen,
                                                                                                  ProgressLogger pl) throws IOException {
     if(pl != null)
       pl.start("Counting nodes and creating perfect hash function");
     AdjacencyHeads heads = new AdjacencyHeads(file, idLen, true);
     ByteArrayTransformationStrategy trStrat =
       new ByteArrayTransformationStrategy(idLen);
 
     HollowTrieDistributorMonotoneMinimalPerfectHashFunction mph =
       new HollowTrieDistributorMonotoneMinimalPerfectHashFunction<byte[]>(heads, trStrat);
 
     if(mph.size64() != heads.getCount()) {
       throw new IllegalStateException(
         "Number of elements counted by the iterator is different than the number " +
           "of elements counted by the  minimal perfect hash function");
     }
 
     if(pl != null)
       pl.logger().info("The perfect hash function is using {} bits", mph.numBits());
     return mph;
   }
 
   public static LcpMonotoneMinimalPerfectHashFunction<byte[]> buildLcpMonotoneMph( String file,
                                                                                    int idLen,
                                                                                    ProgressLogger pl) throws IOException {
     if(pl != null)
       pl.start("Counting nodes and creating perfect hash function");
     AdjacencyHeads heads = new AdjacencyHeads(file, idLen, true);
     ByteArrayTransformationStrategy trStrat =
       new ByteArrayTransformationStrategy(idLen);
 
     LcpMonotoneMinimalPerfectHashFunction mph =
       new LcpMonotoneMinimalPerfectHashFunction<byte[]>(heads, trStrat);
 
     if(mph.size64() != heads.getCount()) {
       throw new IllegalStateException(
         "Number of elements counted by the iterator is different than the number " +
           "of elements counted by the  minimal perfect hash function");
     }
 
     if(pl != null)
       pl.logger().info("The perfect hash function is using {} bits", mph.numBits());
     return mph;
   }
 
   public static ByteArray2LongFunction buildDeterministicMap( String file,
                                                               int idLen,
                                                               ProgressLogger pl) throws IOException {
     if(pl != null)
       pl.displayFreeMemory = true;
     if(pl != null)
       pl.start("Building the deterministic map, with increasing IDs");
 
     AdjacencyHeads heads = new AdjacencyHeads(file, idLen, true);
     ByteArray2LongFunction map = new ByteArray2LongFunction();
 
     long cnt = 0;
     for(byte[] head : heads) {
       map.put(head, cnt);
       cnt++;
     }
 
     if(map.size() != heads.getCount()) {
       throw new IllegalStateException(
         "Number of elements counted by the iterator is different than the number " +
           "of elements counted by the  minimal perfect hash function");
     }
 
     if(pl != null)
       pl.stop("Populated map with " +  map.size() +" elements");
 
     return map;
   }
 
   public static void main(String[] args) throws IOException {
     Function<byte[], Long> map = buildDeterministicMap("merged", 16, new ProgressLogger());
     AdjacencyHeadIterator it = new AdjacencyHeadIterator("merged", 16, true);
 
     int errCnt = 0;
     int totArcs = 0;
     while(it.hasNext()) {
       byte[] elem = it.next();
       long id = map.get(elem);
       if(id < 0)
         throw new RuntimeException(Arrays.toString(elem) + " has no associated id");
       List<byte[]> neighs = it.neighbours();
       totArcs += neighs.size();
       int neighCount = 0;
       for(byte[] neigh : neighs) {
         long nId = map.get(neigh);
         if(nId < 0) {
 //          throw new RuntimeException("\n"+Arrays.toString(neigh) + ", neighbour " + neighCount + "/" + neighs.size() + " of\n" +
 //            Arrays.toString(elem) + " : " + id + " has no associated id");
           errCnt++;
         }
         neighCount++;
       }
     }
     System.out.println("Error count: " + errCnt + " over " + totArcs + " = " + ((double)errCnt/totArcs*100) + "%");
   }
 
 }
