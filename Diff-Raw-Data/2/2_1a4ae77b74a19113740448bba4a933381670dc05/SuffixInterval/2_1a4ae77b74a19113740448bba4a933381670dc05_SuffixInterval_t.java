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
 // SuffixInterval.java
 // Since: 2011/02/12
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 /**
 * A range in suffix array [lowerBound, upperBound)
  * 
  * @author leo
  * 
  */
 public class SuffixInterval implements SARange
 {
     //public final static SuffixInterval NULL = new SuffixInterval(-1, -1);
 
     public final long lowerBound;
     public final long upperBound;
 
     public SuffixInterval(long lowerBound, long upperBound) {
         this.lowerBound = lowerBound;
         this.upperBound = upperBound;
     }
 
     public SuffixInterval(SuffixInterval other) {
         this.lowerBound = other.lowerBound;
         this.upperBound = other.lowerBound;
     }
 
     public long range() {
         return upperBound - lowerBound;
     }
 
     public boolean isUniqueHit() {
         return range() == 1;
     }
 
     public boolean isEmpty() {
         return lowerBound >= upperBound;
     }
 
     public boolean hasEntry() {
         return lowerBound < upperBound;
     }
 
     @Override
     public String toString() {
         return String.format("[%d,%d)%s", lowerBound, upperBound, isUniqueHit() ? "!" : "");
     }
 
     @Override
     public int hashCode() {
         int h = 3;
         h += lowerBound * 17;
         h += upperBound * 17;
         return h % 1973;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof SuffixInterval) {
             SuffixInterval other = SuffixInterval.class.cast(obj);
             return lowerBound == other.lowerBound && upperBound == other.upperBound;
         }
         else
             return false;
     }
 
     public SuffixInterval getReverseSi() {
         return null;
     }
 
     @Override
     public SuffixInterval forwardSi() {
         return this;
     }
 
     @Override
     public SuffixInterval backwardSi() {
         return null;
     }
 
 }
