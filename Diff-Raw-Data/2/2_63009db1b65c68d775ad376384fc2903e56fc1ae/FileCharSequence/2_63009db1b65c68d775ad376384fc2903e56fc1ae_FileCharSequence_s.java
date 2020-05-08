 /**
  * Copyright (C) 2009 Jesse Wilson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.publicobject.io;
 
 import java.io.RandomAccessFile;
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Adapts a text file as a character sequence so that it can be directly manipulated by regular
  * expressions and other character utilities. The file may be at most 2 GB in size and encoded with
  * {@code ISO-8859-1}; otherwise behaviour is undefined.
  *
  * @author jessewilson@google.com (Jesse Wilson)
  */
 public final class FileCharSequence implements CharSequence {
 
   private final RandomAccessFile randomAccess;
   private final long start;
   private final long end;
 
   public FileCharSequence(File file) throws IOException {
     randomAccess = new RandomAccessFile(file, "r");
     start = 0;
     end = randomAccess.length();
   }
 
   private FileCharSequence(FileCharSequence prototype, long start, long end) {
     this.randomAccess = prototype.randomAccess;
     this.start = start;
     this.end = end;
   }
 
   public int length() {
     return (int) (end - start);
   }
 
   public char charAt(int index) {
     try {
       randomAccess.seek(start + index);
       return (char) randomAccess.read();
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
 
   public CharSequence subSequence(int start, int end) {
    return new FileCharSequence(this, start, end);
   }
 
   public void close() throws IOException {
     randomAccess.close();
   }
 
   @Override
   public String toString() {
     try {
       byte[] bytes = new byte[length()];
       randomAccess.seek(start);
       randomAccess.readFully(bytes);
       return new String(bytes, "ISO-8859-1");
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
 }
 
