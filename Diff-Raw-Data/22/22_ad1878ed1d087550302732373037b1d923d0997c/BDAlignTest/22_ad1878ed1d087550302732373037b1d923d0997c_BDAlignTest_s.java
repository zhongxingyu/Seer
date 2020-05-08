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
 // BDAlignTest.java
 // Since: Jul 14, 2011
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import java.io.File;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.utgenome.util.TestHelper;
 import org.utgenome.weaver.GenomeWeaver;
 import org.xerial.util.log.Logger;
 
 public class BDAlignTest
 {
     private static Logger _logger = Logger.getLogger(BDAlignTest.class);
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
         File fastaArchive = TestHelper.createTempFileFrom(BWAlignTest.class, "test2.fa", new File(tmpDir, "test2.fa"));
         GenomeWeaver.execute(String.format("bwt %s", fastaArchive));
         String query = "ATCTCATGGGA";
         GenomeWeaver.execute(String.format("BDAlign %s -q %s", fastaArchive, query));
 
         String q2 = "ACCTCATGGTA";
        GenomeWeaver.execute(String.format("BDAlign %s -q %s", fastaArchive, query));
     }
 
     @Test
     public void bidirectional() throws Exception {
         File fastaArchive = TestHelper.createTempFileFrom(BWAlignTest.class, "test2.fa", new File(tmpDir, "test2.fa"));
         GenomeWeaver.execute(String.format("bwt %s", fastaArchive));
 
         String q = "TACTTATACTTTATCTCCT";
         GenomeWeaver.execute(String.format("BDAlign %s -q %s", fastaArchive, q));
     }
 
     @Test
     public void forwardSearch() throws Exception {
         File fastaArchive = TestHelper.createTempFileFrom(BWAlignTest.class, "test2.fa", new File(tmpDir, "test2.fa"));
         GenomeWeaver.execute(String.format("bwt %s", fastaArchive));
 
         String q = "TCTATACCCCGGTGAA";
         GenomeWeaver.execute(String.format("BDAlign %s -q %s", fastaArchive, q));
     }
 
     @Test
     public void backwardSearch() throws Exception {
         File fastaArchive = TestHelper.createTempFileFrom(BWAlignTest.class, "test3.fa", new File(tmpDir, "test3.fa"));
         GenomeWeaver.execute(String.format("bwt %s", fastaArchive));
 
         String q = "CCGATCTATACCCCGGGGAA";
         GenomeWeaver.execute(String.format("BDAlign %s -q %s", fastaArchive, q));
     }
 
    @Test
    public void forwardSearchInsertion() throws Exception {
        File fastaArchive = TestHelper.createTempFileFrom(BWAlignTest.class, "test2.fa", new File(tmpDir, "test2.fa"));
        GenomeWeaver.execute(String.format("bwt %s", fastaArchive));

        String q = "TCTATACCCCGGGAA";
        GenomeWeaver.execute(String.format("BDAlign %s -q %s", fastaArchive, q));
    }

    @Test
    public void split() throws Exception {
        File fastaArchive = TestHelper.createTempFileFrom(BWAlignTest.class, "test3.fa", new File(tmpDir, "test3.fa"));
        GenomeWeaver.execute(String.format("bwt %s", fastaArchive));

        String q = "CTATCTATAGGGGAATTAT";
        GenomeWeaver.execute(String.format("BDAlign %s -q %s", fastaArchive, q));

    }

 }
