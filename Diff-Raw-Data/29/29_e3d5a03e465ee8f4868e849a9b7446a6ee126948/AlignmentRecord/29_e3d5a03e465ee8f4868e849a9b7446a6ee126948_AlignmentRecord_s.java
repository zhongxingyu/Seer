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
 
 import org.utgenome.UTGBException;
 import org.utgenome.gwt.utgb.client.bio.SAMReadFlag;
 import org.utgenome.weaver.align.ACGTSequence;
 import org.utgenome.weaver.align.CIGAR;
 import org.utgenome.weaver.align.CIGAR.Type;
 import org.utgenome.weaver.align.Strand;
 import org.xerial.util.StringUtil;
 
 /**
  * Holds an alignment result in a form that can be converted into SAM format
  * 
  * @author leo
  * 
  */
 public class AlignmentRecord
 {
     public String          readName;
     public String          chr;
     public Strand          strand;
     public int             start;
     public int             end;
     public int             numMismatches = 0;
     public CIGAR           cigar;
     public String          querySeq;
     public String          qual;
     public int             score;
     public int             numBestHits;
     public String          alignmentState;
     public AlignmentRecord split         = null;
 
     public AlignmentRecord() {
 
     }
 
     public AlignmentRecord(String readName, String chr, Strand strand, int start, int end, int numMismatches,
             CIGAR cigar, String querySeq, String qual, int score, int numBestHists, String alignmentState,
             AlignmentRecord split) {
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
         this.numBestHits = numBestHists;
         this.alignmentState = alignmentState;
         this.split = split;
     }
 
     public CIGAR getCigar() {
         if (cigar != null)
             return cigar;
         else
             return new CIGAR();
     }
 
     public void setCIGAR(String cigarStr) throws UTGBException {
         this.cigar = new CIGAR(cigarStr);
     }
 
     public String toSAMLine() {
        return toSAMLine(true, true);
     }
 
    protected String toSAMLine(boolean isFirst, boolean eachFragmentIsMapped) {
         ArrayList<Object> column = new ArrayList<Object>();
         column.add(readName);
         int flag = 0;
         if (strand == Strand.REVERSE)
             flag |= SAMReadFlag.FLAG_STRAND_OF_QUERY;
         if (isFirst) {
             flag |= SAMReadFlag.FLAG_IS_FIRST_READ;
 
             for (AlignmentRecord r = this; r != null; r = r.split) {
                 if (r.numBestHits <= 0) {
                     eachFragmentIsMapped = false;
                     break;
                 }
             }
         }
         else if (split == null) {
             // last read
             flag |= SAMReadFlag.FLAG_IS_SECOND_READ;
         }
 
         if (eachFragmentIsMapped)
             flag |= SAMReadFlag.FLAG_MAPPED_IN_A_PROPER_PAIR;
 
         // when the next fragment is unmapped
         {
             if (numBestHits <= 0) {
                 flag |= SAMReadFlag.FLAG_QUERY_IS_UNMAPPED;
             }
             if (split != null && split.numBestHits <= 0) {
                 flag |= SAMReadFlag.FLAG_MATE_IS_UNMAPPED;
             }
         }
 
         // Add SAM format columns
         column.add(flag);
         column.add(chr);
         column.add(start);
         column.add(score);
         column.add(getCigar());
         if (split == null) {
             column.add("*"); // pair chr
             column.add(0); // pair start
             column.add(0); // insert size
         }
         else {
             if (!"*".equals(this.chr) && this.chr.equals(split.chr))
                 column.add("=");
             else
                 column.add(split.chr);
             column.add(split.start);
             column.add(split.end - start);
         }
         column.add(querySeq);
         column.add(qual == null ? "*" : qual); // quality value
         if (numBestHits > 0) {
             if (numMismatches >= 0)
                 column.add("NM:i:" + numMismatches);
             if (alignmentState != null)
                 column.add("XP:Z:" + alignmentState);
             column.add(String.format("X0:i:%d", numBestHits));
         }
         String line = StringUtil.join(column, "\t");
         if (split != null)
            line += "\n" + split.toSAMLine(false, eachFragmentIsMapped);
 
         return line;
     }
 
     private static String reverse(String s) {
         if (s == null)
             return null;
         StringBuilder out = new StringBuilder(s.length());
         for (int i = s.length() - 1; i >= 0; --i)
             out.append(s.charAt(i));
         return out.toString();
     }
 
     public static AlignmentRecord convert(ReadHit hit, Read read, int numOtherBestHits) throws UTGBException {
 
         final int numHits = hit.numHits + numOtherBestHits;
         ACGTSequence query = read.getRead(0);
         final int m = (int) query.textSize();
 
         String qual = read.getQual(0);
         if (!hit.strand.isForward()) {
             query = query.reverseComplement();
             if (qual != null)
                 qual = reverse(qual);
         }
 
         if (hit.nextSplit == null) {
             // TODO alignment score
             AlignmentRecord rec = new AlignmentRecord(read.name(), hit.chr, hit.strand, (int) hit.pos, (int) hit.pos
                     + hit.matchLength, hit.getTotalDifferences(), hit.cigar, query.toString(), qual, 1, numHits,
                     hit.getAlignmentState(), null);
             return rec;
         }
         else {
 
             // TODO generalize this part to two or more fragments
             int qualLen = qual != null ? qual.length() : hit.matchLength;
             ReadHit split = hit.nextSplit;
             ACGTSequence s1 = query.subString(0, hit.matchLength);
             ACGTSequence s2 = query.subString(hit.matchLength, m);
             int b1 = Math.min(qualLen, hit.matchLength);
             int b2 = Math.min(qualLen, m);
             String q1 = qual != null ? qual.substring(0, b1) : null;
             String q2 = qual != null ? qual.substring(b1, b2) : null;
 
             if (hit.isUnique()) {
                 if (split.isUnique()) {
                     if (hit.chr.equals(split.chr)) {
                         // Both reads are unique and in the same chromosome
                         // TODO score, quality value trimming
                         AlignmentRecord rec = new AlignmentRecord(read.name(), hit.chr, hit.strand, (int) hit.pos,
                                (int) hit.pos + hit.matchLength, hit.diff, new CIGAR(hit.cigar).add(split.matchLength,
                                        CIGAR.Type.HardClip), s1.toString(), q1, 1, 1, hit.getAlignmentState(hit), null);
                         AlignmentRecord splitRec = new AlignmentRecord(read.name(), split.chr, split.strand,
                                (int) split.pos, (int) split.pos + split.matchLength, split.diff, new CIGAR().add(
                                        hit.matchLength, CIGAR.Type.HardClip).add(split.cigar), s2.toString(), q2, 1,
                                1, split.getAlignmentState(hit), null);
                         rec.split = splitRec;
                         return rec;
                     }
                     else {
                         // use longer alignment as a base
                         if (hit.matchLength >= split.matchLength) {
                             // first split is longer than split
                             CIGAR cigar = new CIGAR(hit.cigar);
                             cigar.add(split.matchLength, Type.SoftClip);
                             AlignmentRecord rec = new AlignmentRecord(read.name(), hit.chr, hit.strand, (int) hit.pos,
                                     (int) hit.pos + m, hit.diff, cigar, query.toString(), qual, 1, numHits,
                                     hit.getAlignmentState(hit), null);
                             return rec;
                         }
                         else {
                             // next split is longer then head
                             CIGAR cigar = new CIGAR(hit.cigar);
                             cigar.add(split.cigar);
                             AlignmentRecord rec = new AlignmentRecord(read.name(), split.chr, split.strand,
                                     (int) split.pos - hit.matchLength, (int) split.pos - hit.matchLength + m,
                                     split.diff, cigar, query.toString(), qual, 1, numHits,
                                     split.getAlignmentState(hit), null);
                             return rec;
                         }
                     }
                 }
                 else {
                     // head is unique but split is repeat
                     CIGAR cigar = new CIGAR(hit.cigar);
                     cigar.add(split.matchLength, Type.SoftClip);
                     AlignmentRecord rec = new AlignmentRecord(read.name(), hit.chr, hit.strand, (int) hit.pos,
                             (int) hit.pos + m, hit.diff, cigar, query.toString(), qual, 1, numHits,
                             hit.getAlignmentState(hit), null);
                     return rec;
                 }
             }
             else {
                 if (split.isUnique()) {
                     // hed is repeat but split is unique
                     CIGAR cigar = new CIGAR();
                     cigar.add(hit.matchLength, Type.SoftClip);
                     cigar.add(split.cigar);
                     AlignmentRecord rec = new AlignmentRecord(read.name(), split.chr, split.strand, (int) split.pos
                             - hit.matchLength, (int) split.pos - hit.matchLength + m, split.diff, cigar,
                             query.toString(), qual, 1, numHits, split.getAlignmentState(hit), null);
                     return rec;
                 }
 
             }
 
         }
         return null;
     }

 }
