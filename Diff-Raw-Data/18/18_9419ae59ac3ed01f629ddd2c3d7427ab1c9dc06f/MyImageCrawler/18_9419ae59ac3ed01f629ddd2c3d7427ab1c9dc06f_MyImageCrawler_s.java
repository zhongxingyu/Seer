 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package edu.uci.ics.crawler4j.example.imagecrawler;
 
 import java.io.File;
 import java.util.regex.Pattern;
 import edu.uci.ics.crawler4j.crawler.IPageVisitValidator;
 import edu.uci.ics.crawler4j.crawler.IPageVisited;
 import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;
 import edu.uci.ics.crawler4j.util.IO;
 
 /**
  * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
  */
 
 /*
  * This class shows how you can crawl images on the web and store them in a
  * folder. This is just for demonstration purposes and doesn't scale for large
  * number of images. For crawling millions of images you would need to store
  * downloaded images in a hierarchy of folders
  * 
  * IMPORTANT: Make sure that you update crawler4j.properties file and 
  *            set crawler.include_images to true
  */
 public class MyImageCrawler implements IPageVisited, IPageVisitValidator {
 
 	private final Pattern filters = Pattern
 			.compile(".*(\\.(css|js|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf"
 					+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
 
 	private final Pattern imgPatterns = Pattern
 			.compile(".*(\\.(bmp|gif|jpe?g|png|tiff?))$");
 
 	private File storageFolder;
 	private String[] crawlDomains;
 
 	public MyImageCrawler(String[] crawlDomains, String storageFolderName) {
 		this.crawlDomains = crawlDomains;
 
 		storageFolder = new File(storageFolderName);
 		if (!storageFolder.exists()) {
 			storageFolder.mkdirs();
 		}
 	}
 
 	public boolean canVisit(String url) {
 		String href = url.toLowerCase();
 		if (filters.matcher(href).matches()) {
 			return false;
 		}
 		
 		if (imgPatterns.matcher(href).matches()) {
 			return true;
 		}
 
 		for (String domain : crawlDomains) {
 			if (href.startsWith(domain)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void visited(Page page) {
 		String url = page.getWebURL().getURL();
 
 		// We are only interested in processing images
 		if (!page.isBinary() || !imgPatterns.matcher(url).matches()) {
 			return;
 		}
 		
 		// Not interested in very small images
 		if (page.getBinaryData().length < 10 * 1024) {
 			return;
 		}
 
 		// get a unique name for storing this image
 		String extension = url.substring(url.lastIndexOf("."));
 		String hashedName = Cryptography.MD5(url) + extension;
 
 		// store image
 		IO.writeBytesToFile(page.getBinaryData(), storageFolder
 				.getAbsolutePath()
 				+ "/" + hashedName);
 
 		System.out.println("Stored: " + url);
 	}
 }
