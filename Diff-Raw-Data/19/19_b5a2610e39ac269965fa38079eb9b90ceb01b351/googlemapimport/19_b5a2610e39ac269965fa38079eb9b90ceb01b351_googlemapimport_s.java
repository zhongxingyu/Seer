 import java.io.*;
 import java.net.*;
 import java.security.Principal;
 import java.util.*;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.InetAddress;
 import javax.xml.*;
 import java.io.File;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import java.net.URLEncoder;
 
 public class googlemapimport {
 	public static void main(String[] args) {
 		String addr = "http://maps.google.com/maps/ms?ie=UTF8&hl=en&msa=0&output=georss&msid=116968845691849104850.00048835577f696c2984d";
		BufferedReader reader;
 		String MeetupURL = "http://api.meetup.com/ew/event/";
 		URL url;
 		HttpURLConnection conn;
 		String params = "";
 		String [] LatLon = new String[2];
		Date time = new Date("Mon, 20 Sep 2010 11:00:00 EDT");
 
 		//load data from google
 		try{
 			url = new URL(addr);
 			conn = (HttpURLConnection) url.openConnection();
 
 
 			conn.setRequestMethod("GET");
 
 			conn.connect();
 			InputStream in = conn.getInputStream();
 			reader = new BufferedReader(new InputStreamReader(in));
 
 
 
    			try{
     				// Create file 
    				FileWriter fstream = new FileWriter("test.xml");
         			BufferedWriter out = new BufferedWriter(fstream);
 				String text = reader.readLine();
 				while (text != null){		
 					out.write(text + "\n");
 					text = reader.readLine();
 				}
     				//Close the output stream
     				out.close();
     			}catch (Exception e){//Catch exception if any
       				System.err.println("Error: " + e.getMessage());
     			}
 
 			conn.disconnect();
 
 		} catch(IOException ex) {
 			ex.printStackTrace();
 			System.out.println("made it here");
 		}
 
 		//parse xml
 		try {
 			File file = new File("test.xml");
   			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
   			DocumentBuilder db = dbf.newDocumentBuilder();
   			Document doc = db.parse(file);
   			doc.getDocumentElement().normalize();
 
   			NodeList nodeLst = doc.getElementsByTagName("item");
 
 
 			for (int s = 0; s < nodeLst.getLength(); s++) {
 
     				Node fstNode = nodeLst.item(s);
     
     				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
  
					params = "urlname=TEDTESTMIKE2";
 
            				Element fstElmnt = (Element) fstNode;
 					NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("title");
 					Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
 					NodeList fstNm = fstNmElmnt.getChildNodes();
 					params = params + "&title=" + URLEncoder.encode(((Node) fstNm.item(0)).getNodeValue());
 
 					fstNmElmntLst = fstElmnt.getElementsByTagName("georss:point");
 					fstNmElmnt = (Element) fstNmElmntLst.item(0);
 					fstNm = fstNmElmnt.getChildNodes();
 					LatLon = ((Node) fstNm.item(0)).getNodeValue().trim().split(" ", 2);
 
 					fstElmnt = (Element) fstNode;
 					fstNmElmntLst = fstElmnt.getElementsByTagName("description");
 					fstNmElmnt = (Element) fstNmElmntLst.item(0);
 					fstNm = fstNmElmnt.getChildNodes();
 					params = params + "&description=" + URLEncoder.encode(((Node) fstNm.item(0)).getNodeValue().replaceAll("\\<.*?>",""));
 
 					params = params + "&lat=" + LatLon[0] + "&lon=" + LatLon[1];
 
 					params = params + "&time=" + time.getTime();
 
					params = params + "&key=2b2b21f3a32d2a5b4c6d3c8015746";
 					
 
 					try{
 						url = new URL(MeetupURL);
 						conn = (HttpURLConnection) url.openConnection();
 
 						conn.setDoOutput(true);
 						conn.setRequestMethod("POST");
 
 						conn.connect();
 
 
 						OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); 
 						wr.write(params); 
 						wr.flush(); 
 
 						BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
 						String line; 
 						while ((line = rd.readLine()) != null) { 
 							// Process line... 
 						} 
 						wr.close(); 
 						rd.close(); 
 			   		
 
 						conn.disconnect();
 
 					} catch(IOException ex) {
 						ex.printStackTrace();
 						System.out.println();
 						System.out.println(params);
 						System.out.println();
 					}
    				}
 
  			}
   		} catch (Exception e) {
     			e.printStackTrace();
   		}
 
 	}
 
 } 
 
