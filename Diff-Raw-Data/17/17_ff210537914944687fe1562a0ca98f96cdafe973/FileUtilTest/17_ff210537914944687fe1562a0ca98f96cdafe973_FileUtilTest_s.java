 /*******************************************************************************
  *  Copyright 2007 Ketan Padegaonkar http://ketan.padegaonkar.name
  *  
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *  
  *      http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  ******************************************************************************/
 package net.sourceforge.jcctray.utils;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 
 /**
  * @author Ketan Padegaonkar
  */
 public class FileUtilTest extends TestCase {
 
 	private static final String	USER_HOME_JCCTRAY		= System.getProperty("user.home") + File.separator + ".jcctray";
 	private static final String	MY_CONFIG				= "my.config";
 	private static final String	FILEPATH_IN_USER_HOME	= USER_HOME_JCCTRAY + File.separator + MY_CONFIG;
 	private static final String	FILEPATH_IN_USER_DIR	= System.getProperty("user.dir") + File.separator + MY_CONFIG;
 
 	private File				fileInUserHome;
 	private File				fileInUserDir;
 
 	protected void setUp() throws Exception {
 		new File(USER_HOME_JCCTRAY).mkdirs();
 		super.setUp();
 	}
 
 	public void testGetsSettingsFromUserHomeDirFirst() throws Exception {
 		fileInUserHome = new File(FILEPATH_IN_USER_HOME);
 		fileInUserHome.createNewFile();
 		assertEquals(FILEPATH_IN_USER_HOME, FileUtil.findConfigFile(MY_CONFIG));
 	}
 
 	public void testGetsSettingsUserDirIfNoFileInHome() throws Exception {
 		fileInUserDir = new File(FILEPATH_IN_USER_DIR);
 		fileInUserDir.createNewFile();
 		assertEquals(FILEPATH_IN_USER_DIR, FileUtil.findConfigFile(MY_CONFIG));
 	}
 
	public void testGetsNullIfNoFileInUserHomeAndUserDir() throws Exception {
		assertNull(FileUtil.findConfigFile(MY_CONFIG));
 	}
 
 	protected void tearDown() throws Exception {
 		if (fileInUserHome != null)
 			fileInUserHome.delete();
 
 		if (fileInUserDir != null)
 			fileInUserDir.delete();
 	}
 
 }
