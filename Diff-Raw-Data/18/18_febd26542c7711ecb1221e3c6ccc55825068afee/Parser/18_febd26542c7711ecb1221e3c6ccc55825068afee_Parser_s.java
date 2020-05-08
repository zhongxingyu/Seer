 package org.openntf.news.http.core;
 
 /*
  * � Copyright IBM, 2011, 2012
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at:
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing 
  * permissions and limitations under the License.
  * 
  * Author: Niklas Heidloff - niklas_heidloff@de.ibm.com
  */
 
 import lotus.domino.*;
 import net.htmlparser.jericho.*;
 import java.util.*;
 import java.net.*;
 import com.ibm.xsp.extlib.util.ExtLibUtil;
 
 public class Parser {
 	// No need to instantiate this class
 	private Parser() { }
 
 	static public String getOutput(String urlToIndex) {
 
 		String unid = "";
 		Document newsEntry = null;
 		try {
 			MicrosoftTagTypes.register();
 			PHPTagTypes.register();
 			PHPTagTypes.PHP_SHORT.deregister(); 
 			MasonTagTypes.register();
			Source source = new Source(new URL(urlToIndex));
 			source.fullSequentialParse();
 
 			String title = getTitle(source);
 			String description = getMetaValue(source, "description");
 			if (description == null || description.isEmpty()) {
 				description = title;
 			}
 
 			Session session = ExtLibUtil.getCurrentSession();
 			Database db = ExtLibUtil.getCurrentDatabase();
 			newsEntry = db.createDocument();
 
 			newsEntry.appendItemValue("Form", "News");
 			newsEntry.appendItemValue("NState", "queued");
 			newsEntry.appendItemValue("NID", generateUniqueId());
 			newsEntry.appendItemValue("NLink", urlToIndex);
 			newsEntry.appendItemValue("PID", "unknown");
 			newsEntry.appendItemValue("NTitle", title);
 			//newsEntry.appendItemValue("NAbstract", description + "\n\n\n" + source.getTextExtractor().setIncludeAttributes(true).toString());
 			newsEntry.appendItemValue("NAbstract", "");
 			Date date = new Date();
 			newsEntry.appendItemValue("NCreationDate", session.createDateTime(date));
 
 			newsEntry.save();
 
 			unid = newsEntry.getUniversalID();
 		} catch(Exception err) {
 			err.printStackTrace();
 		} finally {
 			MiscUtils.incinerate(newsEntry);
 		}
 		return unid;
 	}
 
 	private static String generateUniqueId() {
 		String allowed= "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 		StringBuilder sb = new StringBuilder(6);
 		Random random = new Random();
 		for (int i=0; i < 6; i++) {
 			sb.append(allowed.charAt(random.nextInt(allowed.length())));
 		}
 		return sb.toString().toLowerCase();
 	}
 
 	private static String getTitle(Source s) {
 		Element e = s.getFirstElement(HTMLElementName.TITLE);
 		if (e == null) return null;
 		return CharacterReference.decodeCollapseWhiteSpace(e.getContent());
 	}
 
 	private static String getMetaValue(Source s, String k) {
 		for (int p = 0; p < s.length();) {
 			StartTag startTag = s.getNextStartTag(p, "name" , k , false);
 			if (startTag == null) return null;
 			if (startTag.getName() == HTMLElementName.META)
 				return startTag.getAttributeValue("content");
 			p = startTag.getEnd();
 		}
 		return null;
 	}
 }
