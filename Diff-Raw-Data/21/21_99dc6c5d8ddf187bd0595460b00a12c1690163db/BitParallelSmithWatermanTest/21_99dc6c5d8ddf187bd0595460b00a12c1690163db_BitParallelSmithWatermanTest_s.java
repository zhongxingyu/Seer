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
 // BitParallelSmithWatermanTest.java
 // Since: 2011/07/29
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import org.junit.Test;
 
 public class BitParallelSmithWatermanTest
 {
     @Test
     public void align64() throws Exception {
 
         String ref = "ACGTGGTCTT";
         //BitParallelSmithWaterman.align64(new ACGTSequence(ref), new ACGTSequence("ACGTGGTCTT"));
        BitParallelSmithWaterman.align64(new ACGTSequence(ref), new ACGTSequence("ACGTGGTT"));
         //BitParallelSmithWaterman.align64(new ACGTSequence(ref), new ACGTSequence("ACGTGGT"));
         //BitParallelSmithWaterman.align64(new ACGTSequence(ref), new ACGTSequence("CGTGGTC"));
 
     }
 
 }
