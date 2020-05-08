 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
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
 // utgb-shell Project
 //
 // QSeqToFastqTest.java
 // Since: Jul 20, 2010
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.shell;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.FileReader;
 
 import org.junit.Test;
 import org.utgenome.format.fastq.FastqRead;
 import org.utgenome.format.fastq.FastqReader;
 import org.xerial.util.FileResource;
 import org.xerial.util.FileUtil;
 import org.xerial.util.log.Logger;
 
 public class QSeqToFastqTest {
 	private static Logger _logger = Logger.getLogger(QSeqToFastqTest.class);
 
 	@Test
 	public void convert() throws Exception {
 
 		File in = FileUtil.createTempFile(new File("target"), "sample_qseq", ".txt");
 		in.deleteOnExit();
 		FileUtil.copy(FileResource.openByteStream(QSeqToFastqTest.class, "qseq_sample.txt"), in);
 
 		File out = FileUtil.createTempFile(new File("target"), "sample_qseq", ".fastq");
 		out.deleteOnExit();
 
 		final String prefix = "HG0001:L2";
 
 		UTGBShell.runCommand(String.format("qseq2fastq -g %s %s %s", prefix, in.getPath(), out.getPath()));
 
 		FastqReader fr = new FastqReader(new FileReader(out));
 		int readCount = 0;
 		for (FastqRead read; (read = fr.next()) != null; readCount++) {
 			assertTrue(read.seqname.startsWith(prefix));
 		}
 
		assertEquals(4, readCount);
 
 	}
 }
