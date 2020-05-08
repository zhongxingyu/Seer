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
 // BWFiles.java
 // Since: 2011/02/15
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import java.io.File;
 
 /**
  * This class defines BWT related file names
  * 
  * @author leo
  * 
  */
 public class BWTFiles
 {
     private String prefix;
     private String prefixWithStrand;
 
     public BWTFiles(String fastaFile, Strand strand) {
         if (fastaFile.endsWith("tar.gz"))
             this.prefix = fastaFile.substring(0, fastaFile.length() - "tar.gz".length() - 1);
         else
             this.prefix = fastaFile;
 
         switch (strand) {
         case FORWARD:
             prefixWithStrand = prefix + ".f";
             break;
         case REVERSE:
             prefixWithStrand = prefix + ".r";
             break;
         }
     }
 
     public File pacIndex() {
        return new File(prefix + ".silk");
     }
 
     public File iupac() {
         return new File(prefixWithStrand + ".iupac");
     }
 
     public File bwt() {
         return new File(prefixWithStrand + ".bwt");
     }
 
     public File sparseSuffixArray() {
         return new File(prefixWithStrand + ".ssa");
     }
 
     public File bwtWavelet() {
         return new File(prefixWithStrand + ".bwt.wv");
     }
 
 }
