 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.io;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.TestUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 
 public class PilatusEdfLoaderThreadTest extends LoaderThreadTestBase {
 	
 	static String testScratchDirectoryName = null;
 	final static String testFileFolder = "testfiles/gda/analysis/io/EdfLoaderTest/";
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(PilatusEdfLoader.class.getCanonicalName());
 
 	    TestUtils.makeScratchDirectory(testScratchDirectoryName);
 		
 	}
 	
 	@Override
 	@Test
 	public void testInTestThread() throws Exception{
 		super.testInTestThread();
 	}
 	
 	@Override
 	@Test
 	public void testWithTenThreads() throws Exception{
 		super.testWithTenThreads();
 	}
 
 	@Override
 	public void doTestOfDataSet(int threadIndex) throws Exception {
 
 		DataHolder dataHolder = LoaderFactory.getData(testFileFolder + "diff6105.edf", null);
 
		AbstractDataset data = dataHolder.getDataset(PilatusEdfLoader.DATA_NAME);
 		assertEquals(data.getDouble(0, 0), 98.0, 0.0);
 		assertEquals(data.getDouble(2047, 2047), 199.0, 0.0);
 	}
 }
