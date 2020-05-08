 /*******************************************************************************
  * Copyright (c) 2013 - 2014 Maksym Barvinskyi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  *     Maksym Barvinskyi - initial API and implementation
  ******************************************************************************/
 package org.grible.adaptor.helpers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 
 import org.grible.adaptor.json.TableJson;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 
 public class IOHelper {
 
 	public static File searchFile(File dir, String fileName) {
 		for (File temp : dir.listFiles()) {
 			if (temp.isDirectory()) {
 				File result = searchFile(temp, fileName);
 				if (result != null) {
 					return result;
 				}
 			} else if (fileName.equalsIgnoreCase(temp.getName())) {
 				return temp;
 			}
 		}
 		return null;
 	}
 
 	public static File searchFileByClassName(File dir, String className) throws Exception {
 		for (File temp : dir.listFiles()) {
 			if (temp.isDirectory()) {
				File result = searchFileByClassName(temp, className);
 				if (result != null) {
 					return result;
 				}
 			} else {
 				try {
 					TableJson tableJson = parseTableJson(temp);
 					if (tableJson.getClassName().equals(className)) {
 						return temp;
 					}
 				} catch (JsonSyntaxException e) {
 				}
 			}
 		}
 		return null;
 	}
 
 	public static TableJson parseTableJson(File file) throws Exception {
 		FileReader fr = new FileReader(file);
 		BufferedReader br = new BufferedReader(fr);
 		TableJson tableJson = new Gson().fromJson(br, TableJson.class);
 		br.close();
 		return tableJson;
 	}
 }
