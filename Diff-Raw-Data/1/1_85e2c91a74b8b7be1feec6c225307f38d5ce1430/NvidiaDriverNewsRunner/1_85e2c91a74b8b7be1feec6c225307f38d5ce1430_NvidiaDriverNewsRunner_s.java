 /*
  	org.manalith.ircbot.plugin.nvidiadrivernews/NvidiaDriverNewsRunner.java
  	ManalithBot - An open source IRC bot based on the PircBot Framework.
  	Copyright (C) 2011, 2012  Seong-ho, Cho <darkcircle.0426@gmail.com>
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package org.manalith.ircbot.plugin.nvidiadrivernews;
 
 // import java.net.URL; // this is for the first way
 import java.io.IOException;
 
 import org.jsoup.Connection;
 import org.jsoup.Jsoup;
 
 // need to get org.jdom:jdom:1.1 from maven-repo
 /*
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.jdom.JDOMException;
 */ // is for the first way.
 
 import org.jsoup.select.Elements;
 
 public class NvidiaDriverNewsRunner {
 	public NvidiaDriverNewsRunner ( )
 	{
 		;
 	}
 	
 	public String run()
 	{
 		StringBuilder result = new StringBuilder();
 		
 		/*
 		String ProductSeriesType = "3"; // Geforce
 		String ProductSeries = ""; // reserved to get from xml
 		// String ddlDownloadType = "3"; // Driver : useless
 		String ddlOperatingSystem = "11"; // Linux:ia32 , 12 => amd64
 		String hidPageLanguage = "kr"; // hidden page language value = "kr"
 		String ddlLanguage = "8"; // Korean
 		
 		
 		try
 		{
 			URL url = new URL("http", "www.nvidia.com", "/Download/API/lookupValueSearch.aspx?TypeID=2&ParentID=1");
 			SAXBuilder simpleAPIforXMLBuilder = new SAXBuilder(); 
 			ProductSeries = ((Element)(simpleAPIforXMLBuilder.build(url)).getRootElement().getChild("LookupValues").getChildren("LookupValue").get(0)).getChild("Value").getValue(); 
 		}
 		catch ( IOException ioe )
 		{
 			result = ioe.getMessage();
 		}
 		catch ( JDOMException jde )
 		{
 			result = jde.getMessage();
 		}
 
 		String baseurl = "http://www.nvidia.com/Download/processDriver.aspx";
 		baseurl += "?psid=" + ProductSeries;
 		baseurl += "&pfid=" + ProductSeriesType;
 		baseurl += "&rpf=1";
 		//baseurl += "&dtid=" + ddlDownloadType; 
 		baseurl += "&osid=" + ddlOperatingSystem;
 		baseurl += "&lid=" + ddlLanguage;
 		baseurl += "&lang=" + hidPageLanguage;
 		
 		// http://www.nvidia.com/Download/processDriver.aspx?psid=76&pfid=3&rpf=1&osid=11&lid=8&lang=kr
 			
 		try
 		{
 			String [] urlsplit = (new StreamDownloader ( baseurl )).downloadDataStream().split("\\/");
 			String [] versplit = urlsplit[urlsplit.length-1].split("\\-");
 			result = versplit[0] + "-" + versplit[2] + "-" + versplit[3];
 			
 			// amd64
 			ddlOperatingSystem = "12";
 			
 			baseurl = "http://www.nvidia.co.kr/Download/processDriver.aspx";
 			baseurl += "?psid=" + ProductSeries;
 			baseurl += "&pfid=" + ProductSeriesType;
 			baseurl += "&rpf=1";
 			// baseurl += "&dtid=" + ddlDownloadType; 
 			baseurl += "&osid=" + ddlOperatingSystem;
 			baseurl += "&lid=" + ddlLanguage;
 			baseurl += "&lang=kr" + hidPageLanguage;
 			
 			urlsplit = (new StreamDownloader ( baseurl )).downloadDataStream().split("\\/");
 			versplit = urlsplit[urlsplit.length-1].split("\\-");
 			
 			result += ", " + versplit[0] + "-" + versplit[2] + "-" + versplit[3];			
 		}
 		catch ( IOException ioe )
 		{
 			result = ioe.getMessage();
 		}
 		*/ // The First method is the way to get from the official nvidia download site.
 		
 		try
 		{
 			
 			Connection conn = Jsoup.connect("http://www.nvnews.net/vbulletin/showthread.php?t=122606");
 			conn.timeout(10000);
 			
 			Elements e = conn.get().select("div#post_message_1836667").get(0).select("a");
 			result.append("Current long-lived branch release: ");
 			result.append(e.get(0).text());
 			result.append(", Current official release: ");
 			result.append(", Current beta release: ");
 			result.append(e.get(2).text());
 			
 		}
 		catch ( IOException ioe )
 		{
 			result.append(ioe.getMessage());
 		}
 		
 		return result.toString();
 	}
 }
