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
 // OccurrenceTable.java
 // Since: 2011/02/10
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import java.util.ArrayList;
 
 import org.utgenome.gwt.utgb.client.bio.IUPAC;
 import org.xerial.util.log.Logger;
 
 /**
  * Occurrence count table for FM-index
  * 
  * @author leo
  * 
  */
 public class OccurrenceCountTable
 {
     private static Logger       _logger = Logger.getLogger(OccurrenceCountTable.class);
 
     private ArrayList<int[]>    occTable;
     private final IUPACSequence seq;
     private int                 W;
 
     /**
      * Create a character occurrence count table. This class saves the
      * occurrence counts of each character for every W-bp block.
      * 
      * @param seq
      * @param windowSize
      */
     public OccurrenceCountTable(IUPACSequence seq, int windowSize) {
         this.seq = seq;
         this.W = windowSize;
 
         _logger.info("preparing occurrence count table...");
         final int K = IUPAC.values().length;
         final int tableSize = (seq.size() + W) / W;
 
         occTable = new ArrayList<int[]>(K);
         for (int k = 0; k < K; ++k) {
             int[] occ = new int[tableSize];
             for (int i = 0; i < occ.length; ++i)
                 occ[i] = 0;
             occTable.add(occ);
         }
 
         for (int i = 0; i < seq.size(); ++i) {
             IUPAC code = seq.getIUPAC(i);
             int codeIndex = code.bitFlag;
             final int blockIndex = i / W;
 
             if (i % W == 0 && blockIndex > 0) {
                for (IUPAC eachCode : IUPAC.values()) {
                    int[] occ = occTable.get(eachCode.bitFlag);
                    occ[blockIndex] = occ[blockIndex - 1];
                }
             }
            occTable.get(codeIndex)[blockIndex]++;
         }
         _logger.info("done.");
     }
 
     /**
      * Get the occurrence count of the specified character in seq[0..index]
      * 
      * @param code
      * @param index
      * @return
      */
     public int getOcc(IUPAC code, int index) {
         int blockPos = index / W;
         int occ = blockPos <= 0 ? 0 : occTable.get(code.bitFlag)[blockPos - 1];
         final int upperLimit = Math.min(index, seq.size() - 1);
         for (int i = blockPos * W; i <= upperLimit; i++) {
             if (seq.getIUPAC(i) == code) {
                 occ++;
             }
         }
         return occ;
     }
 
 }
