 /*
  * Copyright 2006-2010 Amazon Technologies, Inc. or its affiliates.
  * Amazon, Amazon.com and Carbonado are trademarks or registered trademarks
  * of Amazon Technologies, Inc. or its affiliates.  All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.amazon.carbonado.cursor;
 
 import java.lang.reflect.UndeclaredThrowableException;
 
 import java.util.Comparator;
 import java.util.NoSuchElementException;
 import java.util.Random;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.cojen.util.BeanComparator;
 
 import com.amazon.carbonado.*;
 import com.amazon.carbonado.stored.*;
 import com.amazon.carbonado.lob.ByteArrayBlob;
 
 /**
  * Test case for {@link MergeSortBuffer}.
  *
  * @author Brian S O'Neill
  */
 public class TestMergeSortBuffer extends TestCase {
     private static final int SMALL_BUFFER_SIZE = 10;
     private static final int MEDIUM_BUFFER_SIZE = 500;
     private static final int LARGE_BUFFER_SIZE = 20000;
     private static final int HUGE_BUFFER_SIZE = 4000000;
 
     public static void main(String[] args) {
         junit.textui.TestRunner.run(suite());
     }
 
     public static TestSuite suite() {
         return new TestSuite(TestMergeSortBuffer.class);
     }
 
     private final Comparator<StorableTestBasic> mComparator;
 
     private Repository mRepository;
 
     public TestMergeSortBuffer(String name) {
         super(name);
         mComparator = BeanComparator.forClass(StorableTestBasic.class)
             .orderBy("stringProp")
             .caseSensitive()
             .orderBy("intProp");
     }
 
     protected void setUp() {
         mRepository = TestUtilities.buildTempRepository();
     }
 
     protected void tearDown() {
         mRepository.close();
         mRepository = null;
     }
 
     public void testEmptyBuffer() throws Exception {
         testEmptyBuffer(mRepository.storageFor(StorableTestBasic.class));
     }
 
     public void testEmptyBufferAutoStorage() throws Exception {
         testEmptyBuffer(null);
     }
 
     private void testEmptyBuffer(Storage<StorableTestBasic> storage) throws Exception {
         SortBuffer<StorableTestBasic> buffer = new MergeSortBuffer<StorableTestBasic>(storage);
         buffer.prepare(mComparator);
 
         assertEquals(0, buffer.size());
         assertEquals(false, buffer.iterator().hasNext());
         try {
             buffer.iterator().next();
             fail();
         } catch (NoSuchElementException e) {
         }
 
         buffer.sort();
 
         assertEquals(0, buffer.size());
         assertEquals(false, buffer.iterator().hasNext());
         try {
             buffer.iterator().next();
             fail();
         } catch (NoSuchElementException e) {
         }
 
         buffer.close();
     }
 
     public void testSmallBuffer() throws Exception {
         testBuffer(mRepository.storageFor(StorableTestBasic.class), SMALL_BUFFER_SIZE);
     }
 
     public void testSmallBufferAutoStorage() throws Exception {
         testBuffer(null, SMALL_BUFFER_SIZE);
     }
 
     public void testMedium() throws Exception {
         testBuffer(mRepository.storageFor(StorableTestBasic.class), MEDIUM_BUFFER_SIZE);
     }
 
     public void testMediumAutoStorage() throws Exception {
         testBuffer(null, MEDIUM_BUFFER_SIZE);
     }
 
     public void testMediumOdd() throws Exception {
         testBuffer(mRepository.storageFor(StorableTestBasic.class), MEDIUM_BUFFER_SIZE + 13);
     }
 
     public void testMediumOddAutoStorage() throws Exception {
         testBuffer(null, MEDIUM_BUFFER_SIZE + 13);
     }
 
     public void testLarge() throws Exception {
         testBuffer(mRepository.storageFor(StorableTestBasic.class), LARGE_BUFFER_SIZE);
     }
 
     public void testLargeAutoStorage() throws Exception {
         testBuffer(null, LARGE_BUFFER_SIZE);
     }
 
     public void testLargeOdd() throws Exception {
         testBuffer(mRepository.storageFor(StorableTestBasic.class), LARGE_BUFFER_SIZE + 13);
     }
 
     public void testLargeOddAutoStorage() throws Exception {
         testBuffer(null, LARGE_BUFFER_SIZE + 13);
     }
 
     public void testHugeAutoStorage() throws Exception {
         testBuffer(null, HUGE_BUFFER_SIZE);
     }
 
     public void testHugeWithTimeout() throws Exception {
         try {
             testBuffer(null, HUGE_BUFFER_SIZE, Query.Timeout.millis(100));
             fail();
         } catch (UndeclaredThrowableException e) {
            assertTrue(e.getCause() instanceof FetchTimeoutException);
         }
     }
 
     public void testLobs() throws Exception {
         Comparator<StorableWithLobs> c = BeanComparator.forClass(StorableWithLobs.class)
             .orderBy("-id");
 
         SortBuffer<StorableWithLobs> buffer =
             new MergeSortBuffer<StorableWithLobs>(null, null, 100);
         buffer.prepare(c);
 
         Storage<StorableWithLobs> storage = mRepository.storageFor(StorableWithLobs.class);
 
         for (int i=0; i<5000; i++) {
             StorableWithLobs s = storage.prepare();
             s.setBlobValue(new ByteArrayBlob(("hello " + i).getBytes()));
             s.insert();
             buffer.add(s);
         }
 
         buffer.sort();
 
         int lastId = Integer.MAX_VALUE;
         for (StorableWithLobs s : buffer) {
             assertTrue(s.getId() < lastId);
             String str = s.getBlobValue().asString();
             assertTrue(str.startsWith("hello "));
             assertTrue((Integer.parseInt(str.substring(6)) + 1) == s.getId());
             lastId = s.getId();
         }
 
         buffer.close();
     }
 
     private void testBuffer(Storage<StorableTestBasic> storage, int size)
         throws Exception
     {
         testBuffer(storage, size, null);
     }
 
     private void testBuffer(Storage<StorableTestBasic> storage, int size,
                             Query.Controller controller)
         throws Exception
     {
         SortBuffer<StorableTestBasic> buffer =
             new MergeSortBuffer<StorableTestBasic>(storage, controller);
         buffer.prepare(mComparator);
 
         if (storage == null) {
             storage = mRepository.storageFor(StorableTestBasic.class);
         }
 
         final long seed = 345891237L;
 
         Random rnd = new Random(seed);
 
         for (int i=0; i<size; i++) {
             buffer.add(generateRandomStorable(storage, rnd));
         }
 
         assertEquals(size, buffer.size());
 
         if (size <= SMALL_BUFFER_SIZE) {
             // Verify everything is in the buffer, and in the correct
             // order. Cannot guarantee this if buffer spilled over into files.
             rnd = new Random(seed);
             int count = 0;
             for (StorableTestBasic stb : buffer) {
                 assertEquals(generateRandomStorable(storage, rnd), stb);
                 count++;
             }
             assertEquals(size, count);
         }
 
         buffer.sort();
         assertEquals(size, buffer.size());
 
         assertSortedResults(buffer);
 
         buffer.close();
 
         assertEquals(0, buffer.size());
     }
 
     private void assertSortedResults(SortBuffer<StorableTestBasic> buffer) {
         int count = 0;
         StorableTestBasic last = null;
         for (StorableTestBasic stb : buffer) {
             if (last != null) {
                 int comparatorResult = mComparator.compare(last, stb);
                 if (comparatorResult > 0) {
                     System.out.println(last);
                     System.out.println(stb);
                     System.out.println(count);
                 }
                 assertTrue(comparatorResult <= 0);
             }
             last = stb;
             count++;
         }
         assertEquals(buffer.size(), count);
     }
 
     private StorableTestBasic generateRandomStorable(Storage<StorableTestBasic> storage,
                                                      Random rnd)
     {
         StorableTestBasic stb = storage.prepare();
         stb.setId(rnd.nextInt());
         stb.setStringProp(TestUtilities.sRandomText(0, 100, rnd));
         stb.setIntProp(rnd.nextInt());
         stb.setLongProp(rnd.nextLong());
         stb.setDoubleProp(rnd.nextDouble());
         return stb;
     }
 }
