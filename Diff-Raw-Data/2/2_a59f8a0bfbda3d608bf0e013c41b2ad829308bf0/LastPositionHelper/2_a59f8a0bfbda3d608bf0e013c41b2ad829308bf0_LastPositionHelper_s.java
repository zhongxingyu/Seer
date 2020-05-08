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
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class LastPositionHelper {
 	private LastPositionHelper() {
 	}
 
 	public static Map<String, String> readLastPositions(File f) {
 		Logger logger = LoggerFactory.getLogger(LastPositionHelper.class);
 
 		Map<String, String> lastPositions = new HashMap<String, String>();
 		try {
 			if (f.exists()) {
 				FileInputStream is = null;
 				BufferedReader br = null;
 				try {
 					is = new FileInputStream(f);
 					br = new BufferedReader(new InputStreamReader(is, "utf-8"));
 					while (true) {
 						String line = br.readLine();
 						if (line == null || line.trim().isEmpty())
 							break;
 
 						String[] tokens = line.split(" ");
 						lastPositions.put(tokens[0], tokens[1]);
 					}
 				} finally {
 					ensureClose(is);
 					ensureClose(br);
 				}
 			}
 		} catch (IOException e) {
 			logger.error("araqne log api: apache logger cannot read last positions", e);
 		}
 
 		return lastPositions;
 	}
 
 	public static void updateLastPositionFile(File f, Map<String, String> lastPositions) {
 		Logger logger = LoggerFactory.getLogger(LastPositionHelper.class);
 
 		// write last positions
 		FileOutputStream os = null;
 
 		try {
 			os = new FileOutputStream(f);
 
 			for (String path : lastPositions.keySet()) {
 				String position = lastPositions.get(path);
 				String line = path + " " + position + "\n";
				os.write(line.getBytes());
 			}
 		} catch (IOException e) {
 			logger.error("araqne log api: cannot write last position file", e);
 		} finally {
 			if (os != null) {
 				try {
 					os.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	private static void ensureClose(BufferedReader br) {
 		try {
 			if (br != null)
 				br.close();
 		} catch (IOException e) {
 		}
 	}
 
 	private static void ensureClose(InputStream is) {
 		try {
 			if (is != null)
 				is.close();
 		} catch (IOException e) {
 		}
 	}
 }
