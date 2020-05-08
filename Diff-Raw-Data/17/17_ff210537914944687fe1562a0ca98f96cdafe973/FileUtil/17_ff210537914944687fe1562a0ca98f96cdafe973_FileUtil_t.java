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
 
 /**
  * A utility class for file operations.
  * 
  * @author Ketan Padegaonkar
  */
 public class FileUtil {
 
 	private static final String	JCCTRAY_HOME		= System.getProperty("user.dir");
 	private static final String	USER_HOME_JCCTRAY	= System.getProperty("user.home") + File.separator + ".jcctray";
 
 	/**
 	 * Returns the location of the given config file. Looks for the file in the
 	 * user home dir first, if the file does not exist in the home dir, then
 	 * looks in the program install dir, or <code>null</code> if the file
 	 * could not be found.
 	 * 
 	 * @return the location of the given config file in the following order: (1)
 	 *         the home dir (2) the program install dir (3) <code>null</code>
 	 *         if the file cannot be found.
 	 */
 	public static String findConfigFile(String fileName) {
 		File file = new File(getFileNameInUserHome(fileName));
 		if (file.exists())
 			return file.getAbsolutePath();
 		file = new File(getFileNameInJCCTrayHome(fileName));
 		if (file.exists())
 			return file.getAbsolutePath();
		
		File jcctrayDir = new File(getFileNameInUserHome(""));
		jcctrayDir.mkdirs();
		
	return new File(getFileNameInUserHome(fileName)).getAbsolutePath();
		
//		return null;
 	}
 
 	public static String getFileNameInJCCTrayHome(String fileName) {
 		return getFileName(JCCTRAY_HOME, fileName);
 	}
 
 	public static String getFileNameInUserHome(String fileName) {
 		return getFileName(USER_HOME_JCCTRAY, fileName);
 	}
 
 	private static String getFileName(String baseDir, String fileName) {
 		return baseDir + File.separator + fileName;
 	}
 }
