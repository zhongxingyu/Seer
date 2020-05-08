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
 
 package de.dhbw_mannheim.cloudraid.jni;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.NoSuchElementException;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import de.dhbw_mannheim.cloudraid.config.ICloudRAIDConfig;
 import de.dhbw_mannheim.cloudraid.config.exceptions.InvalidConfigValueException;
 import de.dhbw_mannheim.cloudraid.config.impl.Config;
 import de.dhbw_mannheim.cloudraid.core.impl.jni.RaidAccessInterface;
 
 /**
  * @author Markus Holtermann
  */
 public class TestRaidAccessInterface {
 
 	private static File in, out;
 	private static String hash;
 	private static char[] content_array;
 	private static String content;
 
 	private static final int CONTENT_LENGTH = 1024 * 20;
 	private static final String KEY = "CloudRAID";
 	private static String mergeInPath;
 	private static String mergeOutPath;
 	private static String splitInPath;
 	private static String splitOutPath;
 	private static ICloudRAIDConfig config;
 
 	@BeforeClass
 	public static void oneTimeSetUp() throws NoSuchElementException,
 			InvalidConfigValueException, IOException {
 		int i;
 		config = new Config();
 		config.setCloudRAIDHome(System.getProperty("java.io.tmpdir")
 				+ File.separator + "cloudraid");
 		config.init("CloudRAID-unitTests");
 		mergeInPath = config.getString("split.input.dir", null);
 		mergeOutPath = config.getString("split.output.dir", null);
 
 		splitInPath = config.getString("split.input.dir", null);
 		splitOutPath = config.getString("split.output.dir", null);
 
 		new File(mergeInPath).mkdirs();
 		new File(mergeOutPath).mkdirs();
 		new File(splitInPath).mkdirs();
 		new File(splitOutPath).mkdirs();
 
 		in = new File(splitInPath, "TestRaidAccessInterface.in");
 		out = new File(mergeOutPath, "TestRaidAccessInterface.out");
 
 		FileWriter fw = new FileWriter(in);
 		content_array = new char[CONTENT_LENGTH];
 
 		for (i = 0; i < CONTENT_LENGTH; i++) {
 			content_array[i] = (char) (i % 256);
 		}
 
 		content = new String(content_array);
 
 		fw.write(content);
 		fw.close();
 	}
 
 	@AfterClass
 	public static void oneTimeTearDown() {
 		in.delete();
 		new File(mergeInPath, hash + ".0").delete();
 		new File(mergeInPath, hash + ".1").delete();
 		new File(mergeInPath, hash + ".2").delete();
 		new File(mergeInPath, hash + ".m").delete();
 		out.delete();
 		config.delete();
 	}
 
 	@Test
 	public void testSplit() throws NoSuchAlgorithmException,
 			UnsupportedEncodingException {
 
 		hash = RaidAccessInterface.splitInterface(splitInPath, in
 				.getAbsolutePath().substring(splitInPath.length()),
 				splitOutPath, KEY);
 
 		MessageDigest digest = MessageDigest.getInstance("SHA-256");
 		digest.reset();
 		byte[] expected = digest.digest(in.getAbsolutePath()
 				.substring(splitInPath.length()).getBytes("UTF-8"));
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < expected.length; i++) {
 			sb.append(Integer.toString((expected[i] & 0xff) + 0x100, 16)
 					.substring(1));
 		}
 		assertEquals(sb.toString(), hash);
 	}
 
 	@Test
 	public void testMerge() {
 
 		new File(splitOutPath, hash + ".0").renameTo(new File(mergeInPath, hash
 				+ ".0"));
 		new File(splitOutPath, hash + ".1").renameTo(new File(mergeInPath, hash
 				+ ".1"));
 		new File(splitOutPath, hash + ".2").renameTo(new File(mergeInPath, hash
 				+ ".2"));
 		new File(splitOutPath, hash + ".m").renameTo(new File(mergeInPath, hash
 				+ ".m"));
 
 		int i = RaidAccessInterface.mergeInterface(mergeInPath, hash,
 				out.getAbsolutePath(), KEY);
		assertEquals(0x02, i);
 	}
 
 	@Ignore
 	@Test
 	public void testContent() throws IOException {
 		FileReader fr;
 		char[] buff;
 		fr = new FileReader(in);
 		buff = new char[CONTENT_LENGTH];
 		fr.read(buff, 0, CONTENT_LENGTH);
 		assertEquals(content, new String(buff));
 		fr.close();
 
 		fr = new FileReader(out);
 		buff = new char[CONTENT_LENGTH];
 		fr.read(buff, 0, CONTENT_LENGTH);
 		assertEquals(content, new String(buff));
 		fr.close();
 	}
 }
