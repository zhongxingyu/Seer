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
 import org.utgenome.weaver.align.strategy.SearchDirection;
 import org.xerial.util.log.Logger;
 
 public class QueryMaskTest
 {
     private static Logger _logger = Logger.getLogger(QueryMaskTest.class);
 
     public void check(ACGTSequence query, String answer, SearchDirection d, ACGT ch, int offset, int margin,
             int boundary) {
         int m = query.length();
         QueryMask qm = new QueryMask(query);
         _logger.debug("ans:%s, offset:%d, boundary:%d", answer, offset, boundary);
         assertEquals(BitVector.parseString(answer),
                 BitVector.parseLong(qm.getBidirectionalPatternMask64(d, offset, boundary, offset, ch, margin), m));
 
     }
 
     @Test
     public void mask64() throws Exception {
 
         ACGTSequence query = new ACGTSequence("AAGATTGC");
         //   765 01234
         // F:AAG|ATTGC
         // R:CGTTAGAA
         //
         // 
         // GAA
         // AA
         // A
         check(query, "00001011", SearchDirection.Forward, ACGT.A, 0, 0, 0);
         check(query, "00000101", SearchDirection.Forward, ACGT.A, 1, 0, 0);
         check(query, "00000001", SearchDirection.Forward, ACGT.A, 3, 0, 3);
         check(query, "00011000", SearchDirection.Backward, ACGT.A, 3, 2, 3);
         check(query, "00000110", SearchDirection.Backward, ACGT.A, 3, 0, 3);
         check(query, "00000011", SearchDirection.Backward, ACGT.A, 2, 0, 3);
         check(query, "00000001", SearchDirection.Backward, ACGT.A, 1, 0, 3);
         check(query, "00000000", SearchDirection.Backward, ACGT.A, 0, 0, 3);
     }
 
 }
