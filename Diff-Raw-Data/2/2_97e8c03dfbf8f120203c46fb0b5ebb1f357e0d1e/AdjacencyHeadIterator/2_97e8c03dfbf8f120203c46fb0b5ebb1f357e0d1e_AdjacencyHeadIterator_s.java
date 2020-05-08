 package it.unipd.dei.webqual.converter;
 
 import java.io.*;
 import java.math.BigInteger;
 import java.util.*;
 
 import static it.unipd.dei.webqual.converter.Utils.*;
 
 public class AdjacencyHeadIterator implements Iterator<byte[]> {
 
   private final String fileName;
   private final int idLen;
   private final boolean reset;
   private final DataInputStream dis;
 
   private boolean hasNext;
   private byte[] next;
 
   private List<byte[]> neighbours;
 
   private long count;
 
   public AdjacencyHeadIterator(String fileName, int idLen, boolean reset) throws IOException {
     this.fileName = fileName;
     this.idLen = idLen;
     this.reset = reset;
     this.dis = new DataInputStream(
       new BufferedInputStream(new FileInputStream(this.fileName)));
 
     this.hasNext = true;
     byte[] firstNode = new byte[idLen];
     int read = dis.read(firstNode);
     if (read != idLen || !isHead(firstNode)) {
       throw new NoSuchElementException(
        "The first id is not the head of an adjacency list");
     }
     if(reset) {
       this.next = reset(firstNode);
     } else {
       this.next = new byte[idLen];
       System.arraycopy(firstNode, 0, next, 0, idLen);
     }
 
     this.count = 1;
   }
 
   @Override
   public boolean hasNext() {
     return hasNext;
   }
 
   public List<byte[]> neighbours() {
     return neighbours;
   }
 
   @Override
   public byte[] next() {
     final byte[] cur = next;
     // advance to the next head
     try {
       hasNext = false;
       neighbours = new ArrayList<>();
       while (dis.available() > 0) {
         byte[] buf = new byte[idLen];
         dis.read(buf);
 //        System.out.print("Analyzing " + new BigInteger(buf).toString(16));
         if(isHead(buf)) {
 //          System.out.println("<<--------- Found a head");
           count++;
           if(reset) {
             next = reset(buf);
           } else {
             next = new byte[idLen];
             System.arraycopy(buf, 0, next, 0, idLen);
           }
           hasNext = true;
           break;
         } else {
           neighbours.add(buf);
 //          System.out.println();
         }
       }
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
     return cur;
   }
 
   @Override
   public void remove() {
     throw new UnsupportedOperationException();
   }
 
   public long getCount() {
     return count;
   }
 
   @Override
   protected void finalize() throws Throwable {
     try {
       dis.close();
     }
     finally {
       super.finalize();
     }
   }
 
   public static void main(String[] args) throws IOException {
 //    AdjacencyHeadIterator it = new AdjacencyHeadIterator(
 //      args[0], 16, false);
 //
 //    Map<BigInteger, List<BigInteger>> heads = new HashMap<>();
 //    long duplicates = 0;
 //    long diffNeighsDuplicates = 0;
 //    long edges = 0;
 //
 //    while(it.hasNext()) {
 //      byte[] head = it.next();
 //      BigInteger bi = new BigInteger(head);
 //      edges += it.neighbours().size();
 //      if(bi.signum() >= 0) {
 //        System.out.println("Head with first bit set to 0: " + bi.toString(16));
 //        System.out.println("The first byte is: " + head[0]);
 //        System.out.println("And isHead returns " + isHead(head));
 //        System.exit(1);
 //      }
 //      List<BigInteger> contained = heads.get(bi);
 //
 //      if(contained != null) {
 //        duplicates++;
 //
 //        Collections.sort(contained);
 //        Collections.sort(it.neighbours());
 //
 //        if(it.neighbours().equals(contained)) {
 //          diffNeighsDuplicates++;
 //        }
 //      } else {
 //        heads.put(bi, it.neighbours());
 //      }
 //    }
 //
 //    System.out.printf("%s  %d  %d  %f   %f\n",
 //      args[0],
 //      it.getCount(),
 //      edges,
 //      (((double) duplicates) / it.getCount()) * 100,
 //      (((double) diffNeighsDuplicates) / duplicates) * 100);
 
   }
 
 }
