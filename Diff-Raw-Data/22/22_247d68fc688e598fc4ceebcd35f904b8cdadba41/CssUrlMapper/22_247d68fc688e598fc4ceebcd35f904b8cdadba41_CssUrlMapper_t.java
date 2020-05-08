 
 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.util.cssurlmapper;
 
 import java.io.*;
 
 public class CssUrlMapper {
 
 	private File file;
 	private String libraryName;
 	private String relativePathDirectory = null;
 	private File outputDir;
 	
 	public CssUrlMapper(File file, String libraryName, File relativePathDirectory, File outputDir) {
 	
 		this.file = file;
 		this.libraryName = libraryName;
 		this.outputDir = outputDir;
 		
 		try {
 			if (relativePathDirectory != null) {
 				this.relativePathDirectory = relativePathDirectory.getCanonicalPath();
 			}
 		} catch (Exception e) {
 			this.relativePathDirectory = null;
 		}
 	}
 	
 	public void run() {
 	
 		try {
 			System.out.println("Reading file " + this.file.getCanonicalPath());
 			FileReader reader = new FileReader(this.file);
 			
 			File outputFile = new File(this.outputDir, this.file.getName());
 			FileWriter writer = new FileWriter(outputFile);
 			
 			boolean inComments = false;
 			int lastChar = 0;
 			UrlOccurrence currentUrl = null;
 			for (int c = reader.read(); c != -1; c = reader.read()) {
 				if ((c == 'u' || c == 'U') && currentUrl == null && !inComments) { // url occurrence starting point
 					currentUrl = new UrlOccurrence();
 					currentUrl.feedCharacter(c);
 				} else if (currentUrl != null) { // feed characters to url occurrence
 					currentUrl.feedCharacter(c);
 					// if url format is no longer valid, flush characters and discard url
 					if (!currentUrl.isValid()) {
 						writer.write(currentUrl.toString());
 						currentUrl = null;
 					// url is complete and valid, transform it
 					} else if (currentUrl.isComplete()) {
 						writer.write(transformUrl(currentUrl));
 						currentUrl = null;
 					}
 				} else {
 					// write characters to output file normally
 					writer.write(c);
 					
 					// keep track of comments
 					if (c == '*' && lastChar == '/' && !inComments) {
 						inComments = true;
 						lastChar = 0;
 					} else if (c == '/' && lastChar == '*' && inComments) {
 						inComments = false;
 						lastChar = 0;
 					} else {
 						lastChar = c;
 					}
 				}
 			}
 			
 			// special case when we reach EOF and currentUrl is not null,
 			//  meaning that there was no closing quote or parenthesis...
 			if (currentUrl != null) {
 				writer.write(currentUrl.toString()); // flush buffers
 			}
 			
 			System.out.println("Writing file " + outputFile.getCanonicalPath());
 			writer.close();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private String transformUrl(UrlOccurrence url) {
 
 		String path = url.getUrlChars().trim();
		String query = null;
		int queryIndex = path.indexOf("?");
		if( queryIndex > -1 ){
		    query = path.substring(queryIndex);
		    path = path.substring(0,queryIndex);
		}
 		
 		if (path.startsWith("http://") || path.startsWith("data:") || path.startsWith("#{resource")) { 
 			return url.toString(); // not a local resource or already in JSF format, do not modify
 		}
 		
 		String resourcePath = "";
 		
 		try {
 			// trick to get a complete path and without ..'s
 			File tempFile = new File(this.file.getCanonicalPath());
 			File file = new File(tempFile.getParentFile(), path);
			System.out.println("file: " + file + ", path: " + path);
			System.out.println("canonical path: " + file.getCanonicalPath());
 			file = new File(file.getCanonicalPath());
 			
 			if (this.relativePathDirectory == null) {
 				resourcePath = "/" + file.getName();
 			} else {
 			
 				// get file name and add separator going upward until file path is the same as relative path	
 				while (file != null && !this.relativePathDirectory.equals(file.getCanonicalPath())) {
 					resourcePath = "/" + file.getName() + resourcePath;
 					file = file.getParentFile();
 				}
 			}
 
 		} catch (Exception e) {
 			System.out.println("ERROR: problem while mapping URL in file " + this.file);
 			e.printStackTrace();
 		}
 		
		return url.getLeadingChars() + "#{resource['" + this.libraryName + resourcePath + "']}"
		    + (query != null ? query : "") + url.getTrailingChars();
 	}
 }
