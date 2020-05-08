 /*
  * Copyright 2011-2013 Jeroen Meetsma - IJsberg
  *
  * This file is part of Iglu.
  *
  * Iglu is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Iglu is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ijsberg.iglu.util.io;
 
import static junit.framework.Assert.assertEquals;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
 /**
  */
 public class StreamSupportTest {
 
 	private File tempDir;
 	public static final String TEST_FILE = "org/ijsberg/iglu/util/io/directory structure/root/WWW/_visie/screendump.gif";
 	private String testFileName;
 
 	@Before
 	public void setUp() throws Exception {
 		tempDir = FileSupport.createTmpDir();
 		testFileName = tempDir.getPath() + "/screendump.gif"; 
 		FileSupport.copyClassLoadableResourceToFileSystem(TEST_FILE, testFileName);
 	}
 
 	@After
 	public void tearDown () {
 		FileSupport.deleteFile(tempDir);
 	}
 
 	@Test
 	public void testAbsorbInputStream() throws Exception {
 
 		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 		InputStream input = new FileInputStream(testFileName);
 		StreamSupport.absorbInputStream(input, buffer);
 		input.close();
 		
 		assertEquals(103499, buffer.size());
 	}
 
 }
