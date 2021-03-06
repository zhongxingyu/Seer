 /******************************************************************************
  * Copyright (c) 2005-2008 Whirlwind Match Limited. All rights reserved.
  *
  * This is open source software; you can use, redistribute and/or modify
  * it under the terms of the Open Software Licence v 3.0 as published by the 
  * Open Source Initiative.
  *
  * You should have received a copy of the Open Software Licence along with this
  * application. if not, contact the Open Source Initiative (www.opensource.org)
  *****************************************************************************/
 package com.wwm.postcode;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.util.Set;
 import java.util.TreeMap;
 import org.slf4j.Logger;
 
 import com.wwm.db.core.Settings;
 import com.wwm.util.FileUtils;
 import com.wwm.util.StringUtils;
 
 public class JibbleConvertor {
 	
 	private static final String jibbleFile = Settings.getInstance().getPostcodeRoot() + File.separatorChar + JibbleImporter.jibbleDataFile;
 	TreeMap<String, PostcodeResult> jibbleMap;
 
 	@SuppressWarnings("unchecked")
	public JibbleConvertor(Logger log) {
 		log.info("Starting up JibbleConvertor using data: " + jibbleFile);
 		try {
 			jibbleMap = (TreeMap<String, PostcodeResult>)FileUtils.readObjectFromGZip(jibbleFile);
 		} catch (Error e) {
 			log.error("Lookups disabled.  Failed to load Jibble data " + e.getCause().getMessage());
 		}
 	}
 	
 	public PostcodeResult lookupShort(String prefix) {
 		if (prefix == null || jibbleMap == null) return null;
 		prefix = StringUtils.stripSpaces(prefix.toUpperCase());
 		return jibbleMap.get(prefix);
 	}
 	
 	public byte[] getPrefixData() {
 		StringBuffer sb = new StringBuffer();
 		Set<String> set = jibbleMap.keySet();
 		for (String s : set) {
 			sb.append(s);
 			int l = s.length();
 			while (l < 4) {
 				l++;
 				sb.append(' ');
 			}
 		}
 		String data = sb.toString();
 		try {
 			return data.getBytes("UTF8");
 		} catch (UnsupportedEncodingException e) {
 			throw new Error("Internal Error. ", e);
 		}
 	}
 	
 }
