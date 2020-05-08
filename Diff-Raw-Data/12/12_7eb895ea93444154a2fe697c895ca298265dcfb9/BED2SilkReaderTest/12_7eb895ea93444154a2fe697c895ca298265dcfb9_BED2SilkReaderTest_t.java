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
 // BED2SilkReaderTest.java
 // Since: May 26, 2009
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.shell.db.bed;
 
 import java.io.BufferedReader;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.utgenome.format.bed.BED2SilkReader;
 import org.xerial.util.FileResource;
 import org.xerial.util.log.Logger;
 
 public class BED2SilkReaderTest {
 
 	private static Logger _logger = Logger.getLogger(BED2SilkReaderTest.class);
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testReader() throws Exception {
 		BED2SilkReader reader = new BED2SilkReader(FileResource.open(BED2SilkReaderTest.class, "sample.bed"));
 
 		BufferedReader buf = new BufferedReader(reader);
 		for (String line; (line = buf.readLine()) != null;) {
 			_logger.info(line);
 		}
 
 		buf.close();
 	}
 
	@Test
	public void invalidBED() throws Exception {
		BED2SilkReader reader = new BED2SilkReader(FileResource.open(BED2SilkReaderTest.class, "test_for_error.bed"));

		BufferedReader buf = new BufferedReader(reader);
		for (String line; (line = buf.readLine()) != null;) {
			_logger.info(line);
		}

		buf.close();

	}
 }
