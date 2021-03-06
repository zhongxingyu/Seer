 /*
  * Copyright (C) 2010 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package libcore.java.io;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import junit.framework.TestCase;
 
 public final class RandomAccessFileTest extends TestCase {
 
     private File file;
 
     @Override protected void setUp() throws Exception {
         file = File.createTempFile("RandomAccessFileTest", "tmp");
     }
 
     @Override protected void tearDown() throws Exception {
         file.delete();
     }
 
     public void testSeekTooLarge() throws Exception {
         RandomAccessFile raf = new RandomAccessFile(file, "rw");
         try {
             raf.seek(Long.MAX_VALUE);
             fail();
         } catch (IOException expected) {
         }
     }
 
     public void testSetLengthTooLarge() throws Exception {
         RandomAccessFile raf = new RandomAccessFile(file, "rw");
         try {
             raf.setLength(Long.MAX_VALUE);
             fail();
         } catch (IOException expected) {
         }
     }
 
     public void testSetLength64() throws Exception {
         RandomAccessFile raf = new RandomAccessFile(file, "rw");
         raf.setLength(0);
         assertEquals(0, file.length());
         long moreThanFourGig = ((long) Integer.MAX_VALUE) + 1L;
         raf.setLength(moreThanFourGig);
         assertEquals(moreThanFourGig, file.length());
     }
 
     // http://b/3015023
     public void testRandomAccessFileHasCleanupFinalizer() throws Exception {
         int tooManyOpenFiles = 2000;
         File file = File.createTempFile("RandomAccessFileTest", "tmp");
         for (int i = 0; i < tooManyOpenFiles; i++) {
            new RandomAccessFile(file, "rw");
             System.gc();
             System.runFinalization();
         }
     }
 
     public void testDirectories() throws Exception {
         try {
             new RandomAccessFile(".", "r");
             fail();
         } catch (FileNotFoundException expected) {
         }
         try {
             new RandomAccessFile(".", "rw");
             fail();
         } catch (FileNotFoundException expected) {
         }
     }
 }
