 /* Copyright (c) 2007 Bug Labs, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.buglabs.osgi.concierge.core.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Vector;
 import java.util.jar.Attributes;
 import java.util.jar.Manifest;
 
 /**
  * 
  * @author Angel Roman - roman@mdesystems.com
  *
  */
 public class ManifestUtils {
 
 	public static List getImportedPackages(InputStream in) throws IOException {
 		return getCSV("Import-Package", in);
 	}
 
 	public static List getExportedPackages(InputStream in) throws IOException {
 		return getCSV("Export-Package", in);
 	}
 
 
 	public static List getBundleClassPath(InputStream in) throws IOException {
 		return getCSV("Bundle-ClassPath", in);
 	}
 	
 	public static String getVersion(InputStream in) throws IOException {
 		String version = "";
 		
 		List values =  getCSV("Bundle-Version", in);
 		
 		if(values.size() > 0) {
 			version = (String) values.get(0);
 		}
 		
 		return version;
 	}
 	
 	private static List getCSV(String name, InputStream in) throws IOException {
 		Manifest mf = new Manifest(in);
 		
 		Attributes mainAttributes = mf.getMainAttributes();
 		Vector packages = new Vector();
 
 		String result = mainAttributes.getValue(name);
 
 		if(result != null) {
 			if(result.length() > 0) {
 				packages.addAll(Arrays.asList(result.split(",")));
 			}
 		}
 
 		return packages;
 	}
 }
