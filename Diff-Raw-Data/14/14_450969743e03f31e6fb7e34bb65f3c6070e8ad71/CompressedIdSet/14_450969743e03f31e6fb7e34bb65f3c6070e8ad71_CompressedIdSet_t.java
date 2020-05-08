 package org.apache.lucene.util.packed;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Random;
 
 import com.senseidb.compressor.idset.IdSet;
 import com.senseidb.compressor.idset.LongArrayIdSet;
 import com.senseidb.compressor.util.CompressorUtil;
 
 public class CompressedIdSet extends IdSet {
   private static class ValSeg {
     long minVal;
     Packed64 valSet;
 
     ValSeg() {
     }
 
     public long sizeInBytes() {
       return 8 + 4 + valSet.ramBytesUsed();
     }
   }
 
   private static class CompressedLongIterator implements
       LongRandomAccessIterator {
 
     private Iterator<ValSeg> iter;
     private ValSeg cs = null;
     private int readCursor;
     private long lastVal;
 
     private final long[] currentSeg;
     private final int currentCount;
     private final ArrayList<ValSeg> segList;
     private final int size;
 
     CompressedLongIterator(long[] currentSeg, int currentCount,
         LinkedList<ValSeg> segList, int size) {
       this.currentCount = currentCount;
       this.currentSeg = currentSeg;
       this.segList = new ArrayList<ValSeg>(segList);
       this.size = size;
       reset();
     }
 
     @Override
     public long get(int idx) {
       assert idx >= 0 && idx < size;
       int i = idx / currentSeg.length;
       int m = idx % currentSeg.length;
 
       if (i >= segList.size()) {
         // last block
         return currentSeg[m];
       } else {
         ValSeg seg = this.segList.get(i);
         long start = seg.minVal;
         for (int l = 0; l <= m; ++l) {
           start += seg.valSet.get(l);
         }
         return start;
       }
     }
 
     @Override
     public boolean hasNext() {
       if (cs == null) {
         return readCursor < currentCount;
       }
       return true;
     }
 
     @Override
     public long next() {
       if (cs == null) {
         long val = currentSeg[readCursor];
         readCursor++;
         return val;
       }
       if (readCursor < cs.valSet.size()) {
         long val = cs.valSet.get(readCursor);
         val += lastVal;
         lastVal = val;
         if (readCursor == cs.valSet.size() - 1) {
           readCursor = 0;
           if (iter.hasNext()) {
             cs = iter.next();
             lastVal = cs.minVal;
           } else {
             cs = null;
             lastVal = 0;
           }
         } else {
           readCursor++;
         }
         return val;
       }
       return -1;
     }
 
     @Override
     public void reset() {
       iter = segList.iterator();
       readCursor = 0;
       if (iter.hasNext()) {
         cs = iter.next();
         lastVal = cs.minVal;
       } else {
         cs = null;
         lastVal = 0;
       }
     }
 
     @Override
     public long numElems() {
       return size;
     }
   }
 
   private long maxDelta;
   private final long[] currentSeg;
   private int currentCount;
   private final LinkedList<ValSeg> segList = new LinkedList<ValSeg>();
  private int size = 0;
 
   public CompressedIdSet(int blockSize) {
     currentSeg = new long[blockSize];
     init();
   }
 
   public long sizeInBytes() {
     long size = currentCount * 8;
     for (ValSeg seg : segList) {
       size += seg.sizeInBytes();
     }
     return size;
   }
 
   void init() {
     currentCount = 0;
     maxDelta = -1;
   }
 
   @Override
   public void addID(long val) {
     if (currentCount == 0) {
       currentSeg[currentCount++] = val;
     } else {
       long delta = val - currentSeg[currentCount];
       if (maxDelta < delta) {
         maxDelta = delta;
       }
       currentSeg[currentCount++] = val;
     }
     if (currentCount == currentSeg.length) {
       compressBlock();
     }
     size++;
   }
 
   private void compressBlock() {
     int nBits = CompressorUtil.getNumBits(maxDelta);
     ValSeg seg = new ValSeg();
     seg.minVal = currentSeg[0];
     seg.valSet = new Packed64(currentSeg.length, nBits);
     for (int i = 0; i < currentSeg.length; ++i) {
       if (i > 0) {
         long val = currentSeg[i] - currentSeg[i - 1];
         seg.valSet.set(i, val);
       } else {
         seg.valSet.set(i, 0);
       }
     }
     segList.add(seg);
     init();
   }
 
   @Override
   public LongRandomAccessIterator iterator() {
     return new CompressedLongIterator(currentSeg, currentCount, segList, size);
   }
 
   public static void main(String[] args) throws Exception {
     int count = 5 * 1024; // 100M longs
     // int maxVal = 1000000;
     Random rand = new Random();
     LongArrayIdSet set2 = new LongArrayIdSet(count);
     long s1, e1;
 
     s1 = System.currentTimeMillis();
     for (int i = 0; i < count; ++i) {
       long v = Math.abs(rand.nextInt());
       set2.addID(v);
     }
     e1 = System.currentTimeMillis();
     System.out.println(" uncompressed size: " + set2.sizeInBytes() + ", took: "
         + (e1 - s1));
 
     int blockSize = 256;
     long[] data = set2.vals;
     long[] copy = new long[data.length];
     System.arraycopy(data, 0, copy, 0, data.length);
 
     s1 = System.currentTimeMillis();
     Arrays.sort(copy);
     CompressedIdSet set = new CompressedIdSet(blockSize);
     for (long v : copy) {
       set.addID(v);
     }
     e1 = System.currentTimeMillis();
     System.out.println("compressed size: " + set.sizeInBytes() + ", took: "
         + (e1 - s1));
 
     LongRandomAccessIterator iter1 = set.iterator();
     LongRandomAccessIterator iter2 = set2.iterator();
     ;
 
     long start, end;
 
     start = System.currentTimeMillis();
     long tmp = 0;
     while (iter1.hasNext()) {
       long v = iter1.next();
       tmp += v;
     }
     end = System.currentTimeMillis();
     System.out.println("compressed time1: " + (end - start));
 
     start = System.currentTimeMillis();
     while (iter2.hasNext()) {
       long v = iter2.next();
       tmp += v;
     }
     end = System.currentTimeMillis();
     System.out.println("uncompresed time2: " + (end - start));
 
     start = System.currentTimeMillis();
 
     for (int i = 0; i < iter1.numElems(); ++i) {
       long v = iter1.get(i);
       tmp += v;
     }
     end = System.currentTimeMillis();
     System.out.println("compressed ra time1: " + (end - start));
 
     start = System.currentTimeMillis();
     for (int i = 0; i < iter2.numElems(); ++i) {
       long v = iter2.get(i);
       tmp += v;
     }
     end = System.currentTimeMillis();
     System.out.println("uncompressed ra time2: " + (end - start));
 
   }
 }
