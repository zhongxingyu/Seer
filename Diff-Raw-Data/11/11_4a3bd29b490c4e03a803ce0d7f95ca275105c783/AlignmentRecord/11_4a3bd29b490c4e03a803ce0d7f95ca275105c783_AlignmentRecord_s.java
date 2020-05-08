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
 // AlignmentRecord.java
 // Since: 2011/04/25
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align.record;
 
 import java.util.ArrayList;
 
 import org.utgenome.gwt.utgb.client.UTGBClientException;
 import org.utgenome.gwt.utgb.client.bio.CIGAR;
 import org.utgenome.gwt.utgb.client.bio.SAMReadFlag;
 import org.utgenome.weaver.align.Strand;
 import org.xerial.util.StringUtil;
 
 public class AlignmentRecord
 {
     public String          readName;
     public String          chr;
     public Strand          strand;
     public int             start;
     public int             end;
     public int             numMismatches = 0;
     private CIGAR          cigar;
     public String          querySeq;
     public String          qual;
     public int             score;
     public AlignmentRecord split         = null;
 
     public AlignmentRecord(String readName, String chr, Strand strand, int start, int end, int numMismatches,
             CIGAR cigar, String querySeq, String qual, int score, AlignmentRecord split) {
         this.readName = readName;
         this.chr = chr;
         this.strand = strand;
         this.start = start;
         this.end = end;
         this.numMismatches = numMismatches;
         this.cigar = cigar;
         this.querySeq = querySeq;
         this.qual = qual;
         this.score = score;
         this.split = split;
     }
 
     public CIGAR getCigar() {
         if (cigar != null)
             return cigar;
         else
             return new CIGAR();
     }
 
     public void setCIGAR(String cigarStr) throws UTGBClientException {
         this.cigar = new CIGAR(cigarStr);
     }
 
     public String toSAMLine() {
         ArrayList<Object> rec = new ArrayList<Object>();
         rec.add(readName);
         int flag = 0;
         if (strand == Strand.REVERSE)
             flag |= SAMReadFlag.FLAG_STRAND_OF_QUERY;
 
         rec.add(flag);
         rec.add(chr);
         rec.add(start);
         rec.add(score);
         rec.add(getCigar());
         rec.add("*"); // pair chr
         rec.add(0); // pair start
         rec.add(0); // insert size
         rec.add(querySeq);
         rec.add("*"); // quality value
         rec.add("NM:i:" + numMismatches);
         return StringUtil.join(rec, "\t");
     }
 
 }
