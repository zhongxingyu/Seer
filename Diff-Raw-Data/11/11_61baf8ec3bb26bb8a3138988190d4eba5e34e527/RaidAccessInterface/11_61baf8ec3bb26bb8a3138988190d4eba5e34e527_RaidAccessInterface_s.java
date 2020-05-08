 /*
  * Copyright 2011 by the CloudRAID Team, see AUTHORS for more details.
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
 
  * http://www.apache.org/licenses/LICENSE-2.0
 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package de.dhbw.mannheim.cloudraid.jni;
 
 /**
  * @author Florian Bausch, Markus Holtermann
  */
 public class RaidAccessInterface {
 
 	static {
 		System.loadLibrary("cloudraid");
 	}
 
 	/**
 	 * @param tempInputDirPath
 	 *            The complete, absolute path to the temporary input directory,
 	 *            e.g. <code>/tmp/CloudRAID</code>. The path must end with the
 	 *            path separator!
 	 * @param hash
	 *            The hash / base name of the RAID files with 40 characters, e.g.
 	 *            <code>4cf17f73ea0316baffbe8f5eae2451b0f245a5f098378e283acf39143e1c69b3</code>
 	 * @param outputFilePath
 	 *            The complete, absolute path to the original file (
 	 *            <code>/home/user/CloudRAID/test/file.txt</code>). An existing
 	 *            file will be overwritten!
 	 * @param key
 	 *            The key for file decryption. The decryption is done after the
 	 *            input files are merged into the original file. (E.g.
 	 *            <code>eph3Oodotah0peiy</code>)
 	 * @param keyLength
 	 *            The number of bytes the key has, referring to the example: 16.
 	 * @return Return the success and error code of the merge.
 	 */
	public native int mergeInterface(String tempInputDirPath,
 			String hash, String outputFilePath, String key,
 			int keyLength);
 
 	/**
 	 * @param inputFilePath
 	 *            The complete, absolute path to the original file (
 	 *            <code>/home/user/CloudRAID/test/file.txt</code>)
 	 * @param tempOutputDirPath
 	 *            The complete, absolute path to the temporary output directory.
 	 *            The directory MUST exist and end with the path separator
 	 *            (<code>/tmp/CloudRAID/</code>)!
 	 * @param key
 	 *            The key for file encryption. The encryption is done before the
 	 *            file inputFilePath is splitted into the chunks. (E.g.
 	 *            <code>eph3Oodotah0peiy</code>)
 	 * @param keyLength
 	 *            The number of bytes the key has, referring to the example: 16.
	 * @return The base name of the RAID files with 40 characters, or an integer
 	 *         that specifies the return value if something went wrong.
 	 *         Referring to the example:
 	 *         <code>4cf17f73ea0316baffbe8f5eae2451b0f245a5f098378e283acf39143e1c69b3</code>
 	 */
	public native String splitInterface(String inputFilePath,
 			String tempOutputDirPath, String key, int keyLength);
 }
