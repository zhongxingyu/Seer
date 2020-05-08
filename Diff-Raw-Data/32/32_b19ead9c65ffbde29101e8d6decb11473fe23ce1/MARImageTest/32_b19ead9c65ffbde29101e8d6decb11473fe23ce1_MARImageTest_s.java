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
 import static org.junit.Assert.assertTrue;
 import junit.framework.Assert;
 
 import org.apache.commons.lang.SerializationUtils;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.TestUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 /**
  */
 public class MARImageTest {
 	static String TestFileFolder;
 
 	static String testfile1, testfile2, testfile3, testfile4;
 
 	@BeforeClass
 	static public void setUpClass() {
 		TestFileFolder = TestUtils.getGDALargeTestFilesLocation();
 		if( TestFileFolder == null){
 			Assert.fail("TestUtils.getGDALargeTestFilesLocation() returned null - test aborted");
 		}
 		TestFileFolder += "MARImageTest/";
 		testfile1 = TestFileFolder + "in1187_sample1.mccd";
 		testfile2 = TestFileFolder + "mar225_001.mccd";
 		testfile3 = TestFileFolder + "mar165_001.mccd";
 		testfile4 = TestFileFolder + "ins2-foc_MS_2_001.mccd";
 	}
 
 	@Test
 	public void testLoaderFactory() throws Exception {
 		DataHolder dh = LoaderFactory.getData(testfile1, null);
         if (dh==null || dh.getNames().length<1) throw new Exception();
         		
         dh = LoaderFactory.getData(testfile2, null);
         if (dh==null || dh.getNames().length<1) throw new Exception();
         
         dh = LoaderFactory.getData(testfile3, null);
         if (dh==null || dh.getNames().length<1) throw new Exception();
         
         dh = LoaderFactory.getData(testfile4, null);
         if (dh==null || dh.getNames().length<1) throw new Exception();
         
		assertTrue(dh.getName(0).contains("ins2-foc_MS_2_001.mccd"));
 		
 		AbstractDataset data = dh.getDataset(0);
 		assertEquals(299, data.getDouble(1500, 1514), 1);
 		assertEquals(89, data.getDouble(3009, 2168), 1);
 	}
 
 	/**
 	 * Test Loading
 	 * 
 	 * @throws Exception
 	 *             if the test fails
 	 */
 	@Test
 	public void testLoadFile() throws Exception {
 		new MARLoader(testfile1).loadFile();
 	}
 
 	/**
 	 * Test Loading
 	 * 
 	 * @throws Exception
 	 *             if the test fails
 	 */
 	@Test
 	public void testLoadFile2() throws Exception {
 		new MARLoader(testfile2).loadFile();
 	}
 
 	/**
 	 * Test Loading
 	 * 
 	 * @throws Exception
 	 *             if the test fails
 	 */
 	@Test
 	public void testLoadFile3() throws Exception {
 		new MARLoader(testfile3).loadFile();
 	}
 
 	/**
 	 * Test Loading
 	 * 
 	 * @throws Exception
 	 *             if the test fails
 	 */
 	@Test
 	public void testLoadFile4() throws Exception {
 		new MARLoader(testfile4).loadFile();
 	}
 
 	@Test
 	public void testSerializability() throws Exception {
 		DataHolder loader = new MARLoader(testfile2).loadFile();
 		AbstractDataset data = loader.getDataset(0);
 		SerializationUtils.serialize(data.getMetadata());
 	}
 }
