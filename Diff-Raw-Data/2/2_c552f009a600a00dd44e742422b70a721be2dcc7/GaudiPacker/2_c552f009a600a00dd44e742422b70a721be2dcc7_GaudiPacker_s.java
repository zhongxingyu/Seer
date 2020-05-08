ing/*
 Gaudi platform agnostic build tool
 Copyright 2010-2011 Sam Saint-Pettersen.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 For dependencies, please see LICENSE file.
 */
 package org.stpettersens.gaudi;
 import java.io.*;
 import java.util.Enumeration;
 import java.util.zip.*;
 
 /* Written in Java for compatibility.
 Zip extraction method is based on code at 
 http://www.devx.com/getHelpOn/10MinuteSolution/20447 
 */
 @SuppressWarnings("unchecked")
 public class GaudiPacker {
 	
 	int bufSize = 1024;
 	byte[] buffer = new byte[bufSize];
 	
 	Enumeration entries;
 	ZipFile zipFile;
 	String archive;
 	GaudiLogger logger;
 
 	GaudiPacker(String arch, boolean logging) {
 		archive = arch;
 		logger = new GaudiLogger(logging);
 	}
 	
 	private void copyStream(InputStream in, OutputStream out) throws IOException {
 		int len;
 		while((len = in.read(buffer)) >= 0) {
 			out.write(buffer, 0, len);
 		}
 		in.close();
 		out.close();
 	}
 	
 	// Extract a zip archive
 	public String extrZipFile() {
 		try {
 			zipFile = new ZipFile(archive);
 			entries = zipFile.entries();
 			ZipEntry entry = null;
 			
 			while(entries.hasMoreElements()) {
 				entry = (ZipEntry) entries.nextElement();
 				logger.dump(String.format("Extracted file <%s>/%s", archive, entry.getName()));
 				copyStream(zipFile.getInputStream(entry), new BufferedOutputStream
 				(new FileOutputStream(entry.getName())));
 			}
 			zipFile.close();
 			return entry.getName();
 		}
 		catch(IOException ioe) {
 			GaudiApp.displayError(ioe);
 		}
 		return null;
 	}
 }
