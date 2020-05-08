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
 // IUPACSequenceReader.java
 // Since: 2011/02/10
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.utgenome.gwt.utgb.client.bio.IUPAC;
 import org.xerial.util.log.Logger;
 
 /**
  * Reader of the IUPACSequence
  * 
  * @author leo
  * 
  */
 public class IUPACSequence implements LSeq
 {
     private static Logger _logger = Logger.getLogger(IUPACSequence.class);
 
     private byte[]        seq;
     private long          numBases;
 
     public IUPACSequence(String s) {
         this.numBases = s.length() + 1; // add 1 for sentinel
         long byteSize = (numBases / 2) + (numBases & 0x01);
         seq = new byte[(int) byteSize];
         for (int i = 0; i < seq.length; ++i)
             seq[i] = 0;
 
         int cursor = 0;
         for (; cursor < s.length(); ++cursor) {
             char c = Character.toUpperCase(s.charAt(cursor));
             setIUPAC(cursor, IUPAC.encode(c));
         }
         // ensure the sequence size is 2n
         if (cursor % 2 == 0)
             setIUPAC(cursor++, IUPAC.N);
         // append a sentinal
         setIUPAC(cursor++, IUPAC.None);
 
         numBases = cursor;
     }
 
     public IUPACSequence(long numBases) {
         this.numBases = numBases;
         long byteSize = (numBases / 2) + (numBases & 0x01);
         if (byteSize > Integer.MAX_VALUE)
             throw new IllegalArgumentException(String.format("iupac sequence cannot be larger than 4GB: %,d", numBases));
 
         this.seq = new byte[(int) byteSize];
 
         for (int i = 0; i < seq.length; ++i)
             seq[i] = 0;
     }
 
     private IUPACSequence(long numBases, byte[] seq) {
         this.numBases = numBases;
         this.seq = seq;
     }
 
     public IUPACSequence reverse() {
         IUPACSequence rev = new IUPACSequence(numBases);
         long cursor = 0;
         for (long i = numBases - 2; i >= 0; --i) {
             rev.setIUPAC(cursor++, getIUPAC(i));
         }
         rev.setIUPAC(cursor, IUPAC.None);
         return rev;
     }
 
     public static IUPACSequence loadFrom(File f) throws IOException {
         long numBases = f.length() * 2;
         DataInputStream d = new DataInputStream(new BufferedInputStream(new FileInputStream(f), 4 * 1024 * 1024));
         try {
             return IUPACSequence.loadFrom(d, numBases);
         }
         finally {
             d.close();
         }
     }
 
     public static IUPACSequence loadFrom(DataInputStream in, long numBases) throws IOException {
         // The num bases must be always 2
         int byteSize = (int) (numBases / 2);
         byte[] seq = new byte[byteSize];
         in.read(seq, 0, byteSize);
 
         return new IUPACSequence(numBases, seq);
     }
 
     public void saveTo(DataOutputStream out) throws IOException {
         out.write(seq, 0, seq.length);
     }
 
     public void saveTo(File file) throws IOException {
         DataOutputStream d = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
         try {
             saveTo(d);
         }
         finally {
             d.close();
         }
     }
 
     public IUPAC getIUPAC(long index) {
         byte code = (byte) ((seq[(int) (index >>> 1)] >>> (1 - (index & 1)) * 4) & 0x0F);
         return IUPAC.decode(code);
     }
 
     public void setIUPAC(long index, IUPAC val) {
         int pos = (int) (index / 2);
         int offset = (int) (index % 2);
         byte code = (byte) val.bitFlag;
 
         byte mask = (byte) ~(0xF0 >>> (offset * 4));
         seq[pos] &= mask;
         seq[pos] |= (byte) (code << (1 - offset) * 4);
     }
 
     @Override
     public String toString() {
         StringBuilder b = new StringBuilder();
         //b.append("[");
         for (int i = 0; i < numBases; ++i) {
             //            if (i != 0)
             //                b.append(", ");
             IUPAC base = getIUPAC(i);
            b.append(base == IUPAC.None ? "$" : base.name());
         }
         return b.toString();
     }
 
     @Override
     public long lookup(long index) {
         return ((seq[(int) (index >>> 1)] >>> (1 - (index & 1)) * 4) & 0x0F);
     }
 
     @Override
     public long textSize() {
         return numBases;
     }
 
     @Override
     public void set(long i, long val) {
         throw new UnsupportedOperationException("set");
     }
 
     @Override
     public long update(long i, long val) {
         throw new UnsupportedOperationException("update");
     }
 
 }
