 /* 
  * This file is part of PS2YT
  *
  * Copyright (C) 2013 Frédéric Bertolus (Niavok)
  * 
  * PS2YT is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.niavok.youtube;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.niavok.podcast.Podcast;
 import com.niavok.podcast.RessourceLoadingException;
 
 public class YoutubeFeed {
 
 	private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 	
 	List<YoutubeEntry> entryList = new CopyOnWriteArrayList<YoutubeEntry>();
 
 	
 	public static int load(YoutubeFeed feed, String feedXml) {
 		System.out.println(feedXml);
 		
 		try {
             DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 
             Document doc;
             doc = docBuilder.parse(new InputSource(new StringReader(feedXml)));
 
             Element root = doc.getDocumentElement();
 
             if (root.getNodeName().contains("feed")) {
             	System.out.println("feed found");
             	return feed.add(root);
             } else {
                 throw new RessourceLoadingException("Unknown tag '"+root.getNodeName()+"'for root");
             }
 
         } catch (ParserConfigurationException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (SAXException e) {
             throw new RessourceLoadingException("Failed to parse feed ", e);
         } catch (IOException e) {
             throw new RessourceLoadingException("Failed to load feed", e);
         }	
 		return 0;
 	}
 	
 	
 	public YoutubeFeed() {
 	}
 	
 	public int add(Element element) {
 		
 		int addCount = 0;
 		
 		NodeList childNodes = element.getChildNodes();
         for (int i = 0; i < childNodes.getLength(); i++) {
             Node node = childNodes.item(i);
             if (node.getNodeType() != Node.ELEMENT_NODE) {
                 // TODO error
                 continue;
             }
             Element subElement = (Element) node;
             if (subElement.getNodeName().equals("entry")) {
                 YoutubeEntry entry = new YoutubeEntry(subElement);
                 if(entry != null) {
                 	entryList.add(entry);
                 	addCount++;
                 }
             	
             }
         }
 	    
         return addCount;
 		
 	}
 
 	public Map<String, YoutubeEntry> getEntryList(Podcast podcast) {
 		Map<String, YoutubeEntry> map = new HashMap<String, YoutubeEntry>();
 
 		
 		for(YoutubeEntry entry: entryList) {
 			
 			String number = entry.getNumber(podcast);
 			if(number != null) {
 				map.put(number, entry);
 			}	
 			
 			
 		}
 		
 		return map;
 	}
 
 
 	public void add(YoutubeEntry entry) {
 		entryList.add(entry);
 	}
 	
 	
 }
