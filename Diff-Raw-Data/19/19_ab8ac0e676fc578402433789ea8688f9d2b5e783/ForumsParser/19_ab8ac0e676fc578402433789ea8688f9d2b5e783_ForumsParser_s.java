 /* Copyright (C) 2012 Mark
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package com.nguyenmp.gauchospace.parser;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXNotRecognizedException;
 import org.xml.sax.SAXNotSupportedException;
 
 import com.nguyenmp.gauchospace.thing.Forum;
 
 public class ForumsParser {
 	public static List<Forum> getForums(String html) throws SAXNotRecognizedException, SAXNotSupportedException, IOException, TransformerFactoryConfigurationError, TransformerException {
 		Document doc = XMLParser.getDocumentFromString(html);
 		
 		Element body = (Element) XMLParser.getChildFromName(doc.getDocumentElement(), "body");
 		Element page = XMLParser.getChildFromAttribute(body, "id", "page");
 		Element content = XMLParser.getChildFromAttribute(page, "id", "content");
 		Element table = (Element) XMLParser.getChildFromName(content, "table");
 		
 		return getListFromTable(table);
 	}
 
 	private static List<Forum> getListFromTable(Element table) throws TransformerException {
 		List<Forum> list = new ArrayList<Forum>();
 		
 		NodeList children = table.getChildNodes();
 		for (int i = 0; i < children.getLength(); i++) {
 			Element child = (Element) children.item(i);
 			
 			if (child.getAttribute("class") != null && !child.getAttribute("class").equals("")) {
 				list.add(getForumFromRow(child));
 			}
 		}
 		
 		return list;
 	}
 
 	private static Forum getForumFromRow(Element child) throws TransformerException {
 		String string = XMLParser.nodeToString(child);
		
 		int start, end;
 		
 		//Get the forum id
		start = string.indexOf("<a href=\"view.php?f=") + "<a href=\"view.php?f=".length();
 		end = string.indexOf("\"", start);
 		int forumID = Integer.parseInt(string.substring(start, end));
 		
 		//Get the forum's name
 		start = string.indexOf(">", end) + ">".length();
 		end = string.indexOf("</a>", start);
 		String forumName = string.substring(start, end);
 		
 		//Get the forum's description
 		start = string.indexOf("\">", end) + "\">".length();
 		end = string.indexOf("</td>", start);
 		System.out.println(string.substring(start, end));
		String forumDescription = "";
 		
 		//Get the number of discussions
 		end = string.indexOf("</a>", end);
 		start = string.substring(0, end).lastIndexOf(">") + ">".length();
 		int discussions = Integer.parseInt(string.substring(start, end));
 		
 		Forum forum = new Forum();
 		forum.setDescription(forumDescription);
 		forum.setID(forumID);
 		forum.setName(forumName);
 		forum.setNumberOfDiscussions(discussions);
 		forum.setSubscribed(false);
 		return forum;
 	}
 }
