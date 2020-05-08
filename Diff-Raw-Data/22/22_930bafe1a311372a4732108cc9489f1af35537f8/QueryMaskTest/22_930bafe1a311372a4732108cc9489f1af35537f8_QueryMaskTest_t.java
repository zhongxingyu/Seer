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
 // QueryMaskTest.java
 // Since: 2011/08/31
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.xerial.util.log.Logger;
 
 public class QueryMaskTest
 {
     private static Logger _logger = Logger.getLogger(QueryMaskTest.class);
 
     public void check(String answer, ACGT ch, int offset, int boundary) {
         ACGTSequence query = new ACGTSequence("AAGATTGC");
         int m = query.length();
         QueryMask qm = new QueryMask(query);
         _logger.debug("ans:%s, offset:%d, boundary:%d", answer, offset, boundary);
         assertEquals(BitVector.parseString(answer),
                 BitVector.parseLong(qm.getPatternMaskIn64bitForBidirectionalSearch(ch, offset, boundary), m));
     }
 
     @Test
     public void mask64() throws Exception {
 
         // AAG|ATTGC
         //     ATTGC|GAA
         //      TTGC|GAA
         //       TGC|GAA
         check("00001011", ACGT.A, 0, 0);
         check("00000101", ACGT.A, 1, 0);
         check("11000001", ACGT.A, 3, 3);
         check("01100000", ACGT.A, 4, 3);
        check("00110000", ACGT.A, 5, 3);
     }
 }
