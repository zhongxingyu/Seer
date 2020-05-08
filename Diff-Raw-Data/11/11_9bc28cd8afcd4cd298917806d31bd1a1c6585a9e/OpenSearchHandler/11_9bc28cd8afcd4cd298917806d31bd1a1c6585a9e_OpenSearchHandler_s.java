 /*******************************************************************************
  * Copyright 2010 Cameron McKay
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.cdmckay.android.provider;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class OpenSearchHandler extends DefaultHandler {
 
 	private static final String EMPTY_STRING = "";
 	
 	private boolean inItem = false;
 	private boolean inTitle = false;
 	private boolean inDescription = false;
 	private boolean inUrl = false;		
 	
 	private int id = 1;
 	private String title = EMPTY_STRING;
	private String description = EMPTY_STRING;
 	private String url = EMPTY_STRING;
 	
 	public static class Result {
 		public final int id;
 		public final String title;
 		public final String description;
 		public final String url;
 		
 		public Result(int id, String title, String description, String url) {
 			this.id = id;
 			this.title = title;
 			this.description = description;
 			this.url = url;
 		}
 	}
 	
 	private List<Result> results = new ArrayList<Result>();
 	
 	public List<Result> getResults() {		
 		return results;
 	}
 	
 	@Override
 	public void startElement(String uri, String localName, String qName, Attributes attributes)
 			throws SAXException {	
 		final String name = localName.trim().toLowerCase();
 		if (name.equals("item")) {
 			inItem = true;
 		} else if (name.equals("text")) {
 			inTitle = true;
 		} else if (name.equals("description")) {
 			inDescription = true;
 		} else if (name.equals("url")) {
 			inUrl = true;
 		}
 	}
 	
 	@Override
 	public void endElement(String uri, String localName, String qName) throws SAXException {
 		final String name = localName.trim().toLowerCase();
 		if (name.equals("item")) {
			results.add(new Result(id, title, description, url));
 			id++;
 			title = EMPTY_STRING;
			description = EMPTY_STRING;
 			url = EMPTY_STRING;
 			inItem = false;
 		} else if (name.equals("text")) {
 			inTitle = false;
 		} else if (name.equals("description")) {
 			inDescription = false;
 		} else if (name.equals("url")) {
 			inUrl = false;
 		}
 	}
 	
 	@Override
 	public void characters(char[] ch, int start, int length) throws SAXException {
 		final String str = new String(ch).substring(start, start + length);
 		if (inItem) {
 			if (inTitle) {
 				title = str;
 			} else if (inDescription) {				
				description = str.replaceAll(" \\(\\)", "");
 			} else if (inUrl) {
 				url = str;							
 			}
 		}
 	}
 	
 }
