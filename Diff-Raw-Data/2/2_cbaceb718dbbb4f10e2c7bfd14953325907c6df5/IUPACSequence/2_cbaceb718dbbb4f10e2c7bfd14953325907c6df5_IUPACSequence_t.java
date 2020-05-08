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
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 
 import org.utgenome.UTGBErrorCode;
 import org.utgenome.UTGBException;
 import org.utgenome.gwt.utgb.client.bio.IUPAC;
 import org.utgenome.weaver.align.SAIS.BaseArray;
 import org.xerial.lens.SilkLens;
 import org.xerial.util.FileType;
 
 /**
  * Reader of the IUPACSequence
  * 
  * @author leo
  * 
  */
 public class IUPACSequence implements BaseArray
 {
     public static class SequenceIndex
     {
         public String name;
         public String desc;
         public long   length;
         public long   offset;
 
         public SequenceIndex(String name, String desc, long length, long offset) {
             this.name = name;
             this.desc = desc;
             this.length = length;
             this.offset = offset;
         }
     }
 
     private static class Info
     {
         public List<SequenceIndex> index;
         public int                 totalSize;
 
         public static Info loadSilk(File silkFile) throws UTGBException {
             BufferedReader input = null;
             try {
                 try {
                     input = new BufferedReader(new FileReader(silkFile));
                     return SilkLens.loadSilk(Info.class, input);
                 }
                 finally {
                     if (input != null)
                         input.close();
                 }
             }
             catch (Exception e) {
                 throw UTGBException.convert(e);
             }
 
         }
     }
 
     private Info   info;
     private byte[] seq;
 
     public IUPACSequence(File iupacFile) throws UTGBException {
         String silkIndexFile = FileType.replaceFileExt(iupacFile.getPath(), "i.silk");
         info = Info.loadSilk(new File(silkIndexFile));
 
         if (info == null)
             throw new UTGBException(UTGBErrorCode.INVALID_INPUT, "failed to load index file");
 
         int byteSize = info.totalSize >> 1 + (info.totalSize & 0x01);
         this.seq = new byte[info.totalSize];
         try {
             FileInputStream seqIn = new FileInputStream(iupacFile);
             try {
                 int read = seqIn.read(seq, 0, byteSize);
             }
             finally {
                 seqIn.close();
             }
         }
         catch (IOException e) {
             throw UTGBException.convert(e);
         }
 
     }
 
     public int size() {
         return info.totalSize;
     }
 
     public IUPAC getIUPAC(int index) {
        byte code = (byte) ((seq[index >> 1] >>> (1 - (index & 1)) * 4) & 0x0F);
         return IUPAC.decode(code);
     }
 
     public void setIUPAC(int index, IUPAC val) {
         int pos = index / 2;
         int offset = index % 2;
         byte code = (byte) val.bitFlag;
 
     }
 
     public IUPAC[] toArray() {
         IUPAC[] result = new IUPAC[size()];
         for (int i = 0; i < result.length; ++i) {
             result[i] = getIUPAC(i);
         }
         return result;
     }
 
     public void reverse(OutputStream out) throws IOException {
         IUPACSequenceWriter encoder = new IUPACSequenceWriter(out);
         for (int i = info.totalSize - 1; i >= 0; --i) {
             encoder.append(this.getIUPAC(i));
         }
         encoder.close();
     }
 
     @Override
     public int get(int i) {
         return getIUPAC(i).bitFlag;
     }
 
     @Override
     public void set(int i, int val) {
         throw new UnsupportedOperationException("set");
     }
 
     @Override
     public int update(int i, int val) {
         throw new UnsupportedOperationException("update");
     }
 
 }
