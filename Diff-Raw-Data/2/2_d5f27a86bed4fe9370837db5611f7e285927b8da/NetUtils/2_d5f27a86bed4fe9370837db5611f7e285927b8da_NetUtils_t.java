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
 
 package de.dhbw.mannheim.cloudraid.net.util;
 
 import org.scribe.utils.OAuthEncoder;
 
 /**
  * @author Markus Holtermann
  */
 public class NetUtils {
 
 	/**
 	 * This function safely encodes the the given path spec of an URL, splits it
 	 * at the <code>/</code>, encodes each single part and finally joins them
 	 * with the <code>/</code> again.
 	 * 
 	 * From<br>
	 * <code>/this is/a/t&#8364;&#167;t</code><br>
 	 * to<br>
 	 * <code>/this%20is/a/t%E2%82%AC%C2%A7t</code>.
 	 * 
 	 * @param url
 	 *            The URL that should be encoded
 	 * @return Returns the safe URL
 	 */
 	public static String safeURLPercentEncode(String url) {
 		String[] splits = url.split("/");
 		StringBuffer out = new StringBuffer();
 
 		out.append(OAuthEncoder.encode(splits[0]));
 		for (int i = 1; i < splits.length; i++) {
 			out.append("/");
 			out.append(OAuthEncoder.encode(splits[i]));
 		}
 		return out.toString();
 	}
 
 }
