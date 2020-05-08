 package com.senseidb.compressor.test;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Random;
 
 import junit.framework.TestCase;
 
 import org.apache.lucene.search.DocIdSetIterator;
 import org.apache.lucene.util.packed.CompressedIdSet;
 import org.junit.Test;
 
 import com.senseidb.compressor.idset.IdSet;
 import com.senseidb.compressor.idset.IdSet.LongRandomAccessIterator;
 import com.senseidb.compressor.idset.IntArrayDocIdSetIterator;
 import com.senseidb.compressor.idset.LongArrayIdSet;
 
 public class IntArrayDSITest {
 
   @Test
   public void testEmptyArray() throws Exception {
     int[] arr = new int[] {};
     IntArrayDocIdSetIterator iter = new IntArrayDocIdSetIterator(arr);
     int doc = iter.nextDoc();
     TestCase.assertEquals(DocIdSetIterator.NO_MORE_DOCS, doc);
 
     doc = iter.advance(1);
     TestCase.assertEquals(DocIdSetIterator.NO_MORE_DOCS, doc);
   }
 
   @Test
   public void testIntArrayDSI() throws Exception {
     int[] arr = new int[] { -1, -1, 1, 3, 5, 7, 9 };
     IntArrayDocIdSetIterator iter = new IntArrayDocIdSetIterator(arr);
     int doc = iter.nextDoc();
     TestCase.assertEquals(1, doc);
     doc = iter.nextDoc();
     TestCase.assertEquals(3, doc);
 
     iter.reset();
     for (int i = 0; i < 5; ++i) {
       doc = iter.nextDoc();
     }
     TestCase.assertEquals(9, doc);
     doc = iter.nextDoc();
     TestCase.assertEquals(DocIdSetIterator.NO_MORE_DOCS, doc);
 
     iter.reset();
     doc = iter.advance(6);
     TestCase.assertEquals(7, doc);
 
     doc = iter.advance(7);
     TestCase.assertEquals(9, doc);
 
     iter.reset();
     doc = iter.advance(9);
     TestCase.assertEquals(9, doc);
     doc = iter.nextDoc();
     TestCase.assertEquals(DocIdSetIterator.NO_MORE_DOCS, doc);
 
     iter.reset();
     doc = iter.advance(10);
     TestCase.assertEquals(DocIdSetIterator.NO_MORE_DOCS, doc);
 
     arr = new int[] { 1, 3, 5, 7, 9 };
     iter = new IntArrayDocIdSetIterator(arr);
     doc = iter.nextDoc();
     doc = iter.nextDoc();
     doc = iter.nextDoc();
     doc = iter.advance(1);
     TestCase.assertEquals(5, doc);
     arr = new int[] { 1, 3, 5, 7, 9 };
     iter = new IntArrayDocIdSetIterator(arr);
     doc = iter.advance(1);
     TestCase.assertEquals(1, doc);
     doc = iter.advance(1);
     TestCase.assertEquals(3, doc);
   }
 
   private void compare(long[] expected, IdSet idset) throws IOException {
     int i = 0;
     LongRandomAccessIterator iter = idset.iterator();
     while (iter.hasNext()) {
       long v = iter.next();
       long v2 = iter.get(i);
       TestCase.assertEquals(expected[i], v);
       TestCase.assertEquals(expected[i], v2);
       i++;
     }
     TestCase.assertEquals(expected.length, i);
   }
 
   @Test
   public void testIdSet() throws Exception {
    int num = 10;
     int maxVal = 100000;
     // LongSet data = new LongOpenHashSet();
     long[] longarr = new long[num];
     Random rand = new Random();
     for (int i = 0; i < num; ++i) {
 
       long n = Math.abs(rand.nextInt(maxVal));
       longarr[i] = n;
     }
 
     LongArrayIdSet longSet = new LongArrayIdSet(longarr.length);
     for (long val : longarr) {
       longSet.addID(val);
     }
 
     compare(longarr, longSet);
     Arrays.sort(longarr);
 
    CompressedIdSet cset = new CompressedIdSet(3);
     for (long val : longarr) {
       cset.addID(val);
     }
 
     compare(longarr, cset);
   }
 }
