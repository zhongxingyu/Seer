 /*
  * Copyright 2011 - 2012 by the CloudRAID Team
  * see AUTHORS for more details
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
 
 package de.dhbw_mannheim.cloudraid.core.impl.jni;
 
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
 	 *            e.g. <code>/tmp/cloudraid</code>. The path must end with the
 	 *            path separator!
 	 * @param hash
 	 *            The hash / base name of the RAID files with 64 characters,
 	 *            e.g.
 	 *            <code>4cf17f73ea0316baffbe8f5eae2451b0f245a5f098378e283acf39143e1c69b3</code>
 	 * @param outputFilePath
 	 *            The complete, absolute path to the original file (
 	 *            <code>/home/user/CloudRAID/test/file.txt</code>). An existing
 	 *            file will be overwritten!
 	 * @param key
 	 *            The key for file decryption. The decryption is done after the
 	 *            input files are merged into the original file. (E.g.
 	 *            <code>eph3Oodotah0peiy</code>)
 	 * @return Return the success and error code of the merge.
 	 */
 	public static native int mergeInterface(String tempInputDirPath,
 			String hash, String outputFilePath, String key);
 
 	/**
 	 * <p>
 	 * This function splits the file <code>inputBasePath</code> +
 	 * <code>inputFilePath</code> to the directory
 	 * <code>tempOutputDirPath</code> with the <code>return value</code> as
 	 * name. The output files will be encrypted by <code>key</code>.
 	 * </p>
 	 * 
 	 * <p>
 	 * <b>Example:</b> A file <code>file.ext</code> is uploaded to the virtual
 	 * storage location <code>virtual/storage/location/</code>. Hence the
 	 * <i>inputFilePath</i> is <code>virtual/storage/location/file.ext</code>.
 	 * The CloudRAID service is configured to use
 	 * <code>/tmp/cloudraid-upload/</code> as the directory that contains the
 	 * files uploaded by the users (<i>inputbasePath</i>). Thus the file that
 	 * will be split is
 	 * <code>/tmp/cloudraid-upload/virtual/storage/location/file.ext</code>.<br>
 	 * The output files will be stored inside the directory
 	 * <i>tempOutputDirPath</i> <code>/tmp/cloudraid/</code> with the <i>return
 	 * values</i> appended by <code>.0</code>, <code>.1</code>, <code>.2</code>
 	 * or <code>.m</code> as filename.<br>
 	 * The raid files are encrypted by the given <i>key</i>.
 	 * </p>
 	 * 
 	 * @param inputBasePath
 	 *            The base path of all uploads ().
 	 * @param inputFilePath
 	 *            The relative path to the original file (
 	 *            <code>/tmp/cloudraid-upload/</code>
 	 *            <code>CloudRAID/test/file.txt</code>). Relative to the
 	 *            inputBasePath.
 	 * @param tempOutputDirPath
 	 *            The complete, absolute path to the temporary output directory.
 	 *            The directory MUST exist and end with the path separator (
 	 *            <code>/tmp/cloudraid/</code>)!
 	 * @param key
 	 *            The key for file encryption. The encryption is done before the
 	 *            file inputFilePath is split into the chunks. (E.g.
 	 *            <code>eph3Oodotah0peiy</code>)
 	 * @return The base name of the RAID files with 64 characters, or an integer
 	 *         that specifies the return value if something went wrong.
 	 *         Referring to the example:
 	 *         <code>4cf17f73ea0316baffbe8f5eae2451b0f245a5f098378e283acf39143e1c69b3</code>
 	 */
 	public static native String splitInterface(String inputBasePath,
 			String inputFilePath, String tempOutputDirPath, String key);
 }
