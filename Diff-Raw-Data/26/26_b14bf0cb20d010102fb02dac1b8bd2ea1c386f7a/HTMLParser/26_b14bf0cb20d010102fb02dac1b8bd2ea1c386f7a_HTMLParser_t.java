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
 
 package edu.uci.ics.crawler4j.extractor;
 
 import it.unimi.dsi.parser.BulletParser;
 import it.unimi.dsi.parser.callback.TextExtractor;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import edu.uci.ics.crawler4j.crawler.Page;
 import edu.uci.ics.crawler4j.crawler.configuration.ICrawlerSettings;
 import edu.uci.ics.crawler4j.url.URLCanonicalizer;
 
 /**
  * @author Yasser Ganjisaffar <yganjisa at uci dot edu>
  */
 
 public class HTMLParser implements IPageParser {
 	private final int maxOutLinks;
	private final ICrawlerSettings config;
 	private Map<String, String> urls;
 
 	public HTMLParser(ICrawlerSettings config) {
		this.config = config;
 		maxOutLinks = config.getMaxOutlinks();
 	}
 
 	@Override
 	public void parse(Page page) {
		final BulletParser bulletParser = new BulletParser();
		final TextExtractor textExtractor = new TextExtractor();
		final HTMLLinkExtractor linkExtractor = new HTMLLinkExtractor();
		linkExtractor.setIncludeImagesSources(config.getIncludeImages());
		
 		urls = new HashMap<>();
 		String htmlContent = page.getHTML();
 		String contextURL = page.getWebURL().getURL();
 		char[] chars = htmlContent.toCharArray();
 
 		bulletParser.setCallback(textExtractor);
 		bulletParser.parse(chars);
 		page.setText(textExtractor.text.toString().trim());
 		page.setTitle(textExtractor.title.toString().trim());
 
 		bulletParser.setCallback(linkExtractor);
 		bulletParser.parse(chars);
 		
 		String href;
 		String baseURL = linkExtractor.base();
 		if (baseURL != null) {
 			contextURL = baseURL;
 		}
 
 		int urlCount = 0;
 		if (linkExtractor.metaRefresh() != null) {
 			href = parseLink(linkExtractor.metaRefresh(), contextURL);
 			if (href != null) {
 				urls.put(href, null);
 				urlCount++;
 			}
 		}
 		
 		for(Map.Entry<String, String> link : linkExtractor.urls.entrySet()) {
 			href = parseLink(link.getKey(), contextURL);
 			if (href != null) {
 				urls.put(href, link.getValue());
 			
 				if (++urlCount > maxOutLinks) {
 					break;
 				}
 			}
 		}
 	}
 	
 	private String parseLink(String href, String contextURL) {
 		href = href.trim();
 		if (href.length() > 0 && href.indexOf("javascript:") < 0
 				&& href.indexOf("@") < 0) {
 			URL url = URLCanonicalizer.getCanonicalURL(href, contextURL);
 			if (url != null) {
 				return url.toExternalForm();
 			}				
 		}
 		
 		return null;
 	}
 
 	@Override
 	public Map<String, String> getLinks() {
 		return urls;
 	}
 }
