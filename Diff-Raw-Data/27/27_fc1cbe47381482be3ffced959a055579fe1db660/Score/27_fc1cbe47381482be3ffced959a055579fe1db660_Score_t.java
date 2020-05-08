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
 // Score.java
 // Since: 2011/08/08
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align.strategy;
 
 import org.utgenome.weaver.align.AlignmentScoreConfig;
 
 public class Score
 {
     public final int score;
     public final int numMismatches;
     public final int numGapOpens;
     public final int numGapExtend;
     public final int numSplit;
 
     public Score(int score, int numMismatches, int numGapOpens, int numGapExtend, int numSplit) {
         this.score = score;
         this.numMismatches = numMismatches;
         this.numGapOpens = numGapOpens;
         this.numGapExtend = numGapExtend;
         this.numSplit = numSplit;
     }
 
     @Override
     public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("%d:%d/%d/%d/%d", score, numMismatches, numGapOpens, numGapExtend, numSplit));
        return s.toString();
     }
 
     public int layer() {
         return numMismatches + numGapOpens + numGapExtend + numSplit;
     }
 
     public static Score initial() {
         return new Score(0, 0, 0, 0, 0);
     }
 
     public Score update(int newScore) {
         return new Score(newScore, numMismatches, numGapOpens, numGapExtend, numSplit);
     }
 
     public Score extendWithMatch(AlignmentScoreConfig config) {
         return new Score(score + config.matchScore, numMismatches, numGapOpens, numGapExtend, numSplit);
     }
 
     public Score extendWithMismatch(AlignmentScoreConfig config) {
         return new Score(score - config.mismatchPenalty, numMismatches + 1, numGapOpens, numGapExtend, numSplit);
     }
 
     public Score extendWithGapOpen(AlignmentScoreConfig config) {
         return new Score(score - config.gapOpenPenalty, numMismatches, numGapOpens + 1, numGapExtend, numSplit);
     }
 
     public Score extendWithSplit(AlignmentScoreConfig config) {
         return new Score(score - config.splitOpenPenalty, numMismatches, numGapOpens, numGapExtend, numSplit + 1);
     }
 
     public Score extendWithGapExtend(AlignmentScoreConfig config) {
         return new Score(score - config.gapExtensionPenalty, numMismatches, numGapOpens, numGapExtend + 1, numSplit);
     }
 
 }
