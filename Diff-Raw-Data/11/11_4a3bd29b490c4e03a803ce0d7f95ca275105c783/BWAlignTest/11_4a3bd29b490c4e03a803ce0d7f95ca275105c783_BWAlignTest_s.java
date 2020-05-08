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
 // BurrowsWheelerAlignmentTest.java
 // Since: 2011/02/10
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.utgenome.util.TestHelper;
 import org.utgenome.weaver.GenomeWeaver;
 import org.utgenome.weaver.align.record.AlignmentRecord;
 import org.utgenome.weaver.parallel.Reporter;
 import org.xerial.lens.SilkLens;
 import org.xerial.util.log.Logger;
 
 public class BWAlignTest
 {
     private static Logger _logger = Logger.getLogger(BWAlignTest.class);
 
     File                  tmpDir;
 
     @Before
     public void setUp() throws Exception {
         tmpDir = new File("target", "bwa");
         if (!tmpDir.exists())
             tmpDir.mkdirs();
     }
 
     @After
     public void tearDown() throws Exception {
         if (tmpDir != null && tmpDir.exists()) {
             //FileUtil.rmdir(tmpDir);
         }
     }
 
     @Test
     public void align() throws Exception {
 
         File fastaArchive = TestHelper.createTempFileFrom(BWAlignTest.class, "test.fa", new File(tmpDir, "test.fa"));
         GenomeWeaver.execute(String.format("bwt %s", fastaArchive));
         GenomeWeaver.execute(String.format("align -m bwa %s -q TATAA", fastaArchive.getPath()));
 
     }
 
     @Test
     public void align2() throws Exception {
         File fastaArchive = TestHelper.createTempFileFrom(BWAlignTest.class, "test2.fa", new File(tmpDir, "test.fa"));
         GenomeWeaver.execute(String.format("bwt %s", fastaArchive));
         GenomeWeaver.execute(String.format("align -m bwa %s -q ATCTCATGGGA", fastaArchive.getPath()));
     }
 
     @Test
     public void align3() throws Exception {
 
         File fastaArchive = TestHelper.createTempFileFrom(BWAlignTest.class, "test2.fa", new File(tmpDir, "test2.fa"));
         GenomeWeaver.execute(String.format("bwt %s", fastaArchive));
         FMIndexOnGenome fmIndex = FMIndexOnGenome.load(fastaArchive.getPath());
 
        Align.querySingle(fmIndex, "TAAAGTAT", new Reporter() {
             @Override
             public void emit(Object result) throws Exception {
                 AlignmentRecord input = (AlignmentRecord) result;
                 _logger.debug(SilkLens.toSilk(input));
                 assertEquals("seq2", input.chr);
                 assertEquals(Strand.REVERSE, input.strand);
                 assertEquals(9, input.start);
                 assertEquals(17, input.end); // 1-origin
             }
         });
 
     }
     //
     //    @Test
     //    public void forwardMatch() throws Exception {
     //
     //        File fastaArchive = TestHelper.createTempFileFrom(BWAlignTest.class, "test2.fa", new File(tmpDir, "test2.fa"));
     //        GenomeWeaver.execute(String.format("bwt %s", fastaArchive));
     //        FMIndexOnGenome fmIndex = new FMIndexOnGenome(fastaArchive.getPath());
     //
     //        Align.querySingle(fastaArchive.getPath(), "ATACTTTA", new ObjectHandlerBase<AlignmentRecord>() {
     //            @Override
     //            public void handle(AlignmentRecord input) throws Exception {
     //                _logger.debug(SilkLens.toSilk(input));
     //                assertEquals("seq2", input.chr);
     //                assertEquals(Strand.FORWARD, input.strand);
     //                assertEquals(9, input.start); // 1-origin
     //                assertEquals(17, input.end); // 1-origin
     //            }
     //        });
     //    }
     //
     //    @Test
     //    public void fastqGZ() throws Exception {
     //        File fastaArchive = TestHelper.createTempFileFrom(BWAlignTest.class, "test2.fa", new File(tmpDir, "test2.fa"));
     //        GenomeWeaver.execute(String.format("bwt %s", fastaArchive));
     //        File fastqArchive = TestHelper.createTempFileFrom(BWAlignTest.class, "record/sample.fastq.gz", new File(tmpDir,
     //                "sample.fastq.gz"));
     //        GenomeWeaver.execute(String.format("align -m bwa %s %s", fastaArchive, fastqArchive));
     //    }
     //
     //    @Test
     //    public void sample() throws Exception {
     //        File fastaArchive = TestHelper
     //                .createTempFileFrom(BWAlignTest.class, "sample.fa", new File(tmpDir, "sample.fa"));
     //        GenomeWeaver.execute(String.format("bwt %s", fastaArchive));
     //
     //        FASTAPullParser fa = new FASTAPullParser(fastaArchive);
     //        final FASTASequence seq = fa.nextSequence();
     //        fa.close();
     //
     //        BWAlign.querySingle(fastaArchive.getPath(), "TTTCAG", new ObjectHandlerBase<AlignmentRecord>() {
     //            @Override
     //            public void handle(AlignmentRecord input) throws Exception {
     //                _logger.debug(SilkLens.toSilk(input));
     //                String s = seq.getSequence().substring(input.start - 1, input.end - 1);
     //                if (input.numMismatches == 0)
     //                    assertEquals(String.format("strand:%s query:%s ref:%s", input.strand, input.querySeq, s), s,
     //                            input.querySeq);
     //            }
     //        });
     //
     //    }
     //
     //    @Test
     //    public void sample2() throws Exception {
     //        File fastaArchive = TestHelper.createTempFileFrom(BWAlignTest.class, "sample2.fa", new File(tmpDir,
     //                "sample2.fa"));
     //        GenomeWeaver.execute(String.format("bwt %s", fastaArchive));
     //
     //        FASTAPullParser fa = new FASTAPullParser(fastaArchive);
     //        FASTASequence seq;
     //        final HashMap<String, String> seqMap = new HashMap<String, String>();
     //        while ((seq = fa.nextSequence()) != null) {
     //            seqMap.put(seq.getSequenceName(), seq.getSequence());
     //        }
     //
     //        fa.close();
     //
     //        BWAlign.querySingle(fastaArchive.getPath(), "TTTCAG", new ObjectHandlerBase<AlignmentRecord>() {
     //            @Override
     //            public void handle(AlignmentRecord input) throws Exception {
     //                _logger.debug(SilkLens.toSilk(input));
     //                String s = seqMap.get(input.chr).substring(input.start - 1, input.end - 1);
     //                if (input.numMismatches == 0)
     //                    assertEquals(String.format("strand:%s query:%s ref:%s", input.strand, input.querySeq, s), s,
     //                            input.querySeq);
     //            }
     //        });
     //
     //    }
 
 }
