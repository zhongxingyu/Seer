 /*--------------------------------------------------------------------------
  *  Copyright 2011 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // genome-weaver Project
 //
 // IUPACSequenceWriter.java
 // Since: 2011/02/10
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.utgenome.gwt.utgb.client.bio.IUPAC;
 
 /**
  * 4-bit encoder for IUPAC sequences
  * 
  * @author leo
  * 
  */
 public class IUPACSequenceWriter
 {
     private final OutputStream out;
     private byte               cache  = 0;
    private long               cursor = 0;
 
     public IUPACSequenceWriter(OutputStream out) {
         this.out = out;
     }
 
     public void append(IUPAC code) throws IOException {
        long offset = cursor % 2;
 
         if (offset == 0) {
             cache = 0;
             cache |= (byte) (0x0F & code.bitFlag);
         }
         else {
             cache <<= 4;
             cache |= (byte) (0x0F & code.bitFlag);
             out.write(cache);
         }
 
         cursor++;
     }
 
    public long size() {
         return cursor;
     }
 
     public void close() throws IOException {
         if (cursor % 2 == 1) {
             cache <<= 4;
             out.write(cache);
         }
         out.flush();
         out.close();
     }
 
 }
