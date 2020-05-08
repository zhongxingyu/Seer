 /*
  * Copyright 2013 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.log.api.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 
 /**
  * @since 1.7.0
  * @author xeraph
  * 
  */
 public class FileUtils {
 	private FileUtils() {
 	}
 
 	public static List<String> matchFiles(String basePath, Pattern fileNamePattern) {
 		File[] files = new File(basePath).listFiles();
 
 		ArrayList<String> logFiles = new ArrayList<String>();
 		if (files != null) {
 			for (File f : files) {
				if (fileNamePattern.matcher(f.getName()).matches())
 					logFiles.add(f.getAbsolutePath());
 			}
 		}
 
 		Collections.sort(logFiles);
 
 		return logFiles;
 	}
 
 	public static void ensureClose(BufferedReader br) {
 		try {
 			if (br != null)
 				br.close();
 		} catch (IOException e) {
 		}
 	}
 
 	public static void ensureClose(InputStream is) {
 		try {
 			if (is != null)
 				is.close();
 		} catch (IOException e) {
 		}
 	}
 }
