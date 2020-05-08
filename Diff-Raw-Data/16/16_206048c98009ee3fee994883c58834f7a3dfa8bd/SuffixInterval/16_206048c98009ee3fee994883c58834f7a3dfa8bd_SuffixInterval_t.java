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
 
 public class SuffixInterval
 {
     public final long lowerBound;
     public final long upperBound;
 
     public SuffixInterval(long lowerBound, long upperBound) {
         this.lowerBound = lowerBound;
         this.upperBound = upperBound;
     }
 
     public boolean isValidRange() {
        return lowerBound <= upperBound;
     }
 
     @Override
     public String toString() {
         return String.format("[%,d, %,d]", lowerBound, upperBound);
     }
 }
