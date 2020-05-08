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
 // BidirectionalCursor.java
 // Since: 2011/08/08
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align.strategy;
 
 import org.utgenome.weaver.align.ACGT;
 import org.utgenome.weaver.align.ACGTSequence;
 import org.utgenome.weaver.align.AlignmentScoreConfig;
 import org.utgenome.weaver.align.Strand;
 import org.utgenome.weaver.align.SuffixInterval;
 
 /**
  * cursor holding an alignment state of the bidirectional search
  * 
  * @author leo
  * 
  */
 public class BidirectionalCursor
 {
     public final CursorBase          cursor;
     public final Score               score;
     public final ExtensionType       extensionType;
     public final SuffixInterval      siF;
     public final SuffixInterval      siB;
     public final BidirectionalCursor split;
 
     public BidirectionalCursor(Score score, ACGTSequence read, Strand strand, SearchDirection searchDirection,
             ExtensionType extensionType, SuffixInterval siF, SuffixInterval siB, int cursorF, int cursorB,
             BidirectionalCursor split) {
         this.score = score;
         this.cursor = new CursorBase(read, strand, searchDirection, cursorF, cursorB);
         this.extensionType = extensionType;
         this.siF = siF;
         this.siB = siB;
         this.split = split;
     }
 
     public BidirectionalCursor(Score score, CursorBase cursor, ExtensionType extensionType, SuffixInterval siF,
             SuffixInterval siB, BidirectionalCursor split) {
         this.score = score;
         this.cursor = cursor;
         this.extensionType = extensionType;
         this.siF = siF;
         this.siB = siB;
         this.split = split;
     }
 
     public int getUpperBoundOfScore(AlignmentScoreConfig config) {
         return score.score + getRemainingBases() * config.matchScore;
     }
 
     public boolean reachedForwardEnd() {
         return cursor.cursorF >= cursor.read.textSize();
     }
 
     public Strand strand() {
         return cursor.strand;
     }
 
     public ACGT nextACGT() {
         return cursor.nextACGT();
     }
 
     public boolean isForwardSearch() {
         return cursor.isForwardSearch();
     }
 
     public int getRemainingBases() {
         return cursor.getRemainingBases();
     }
 
     @Override
     public String toString() {
         StringBuilder s = new StringBuilder();
        s.append(String.format("%s%s%2d:%d/%d:%s", cursor.strand.symbol, cursor.searchDirection.symbol, score.score,
                 cursor.cursorF, cursor.cursorB, siF));
         if (siB != null)
             s.append(String.format(" %s", siB));
         if (split != null)
             s.append(String.format(" split(%s)", split));
         return s.toString();
     }
 
 }
