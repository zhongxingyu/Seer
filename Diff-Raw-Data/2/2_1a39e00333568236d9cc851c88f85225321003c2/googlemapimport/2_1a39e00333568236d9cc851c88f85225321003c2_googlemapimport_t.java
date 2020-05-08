 import java.io.*;
 import java.net.*;
 import java.util.*;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import java.security.Principal;
 
 public class googlemapimport {
 	public static void main(String[] args) {
 
 		
 		String URLNAME = "";
 String APIKEY = "";
 String addr = "";
 String time = "";
 			BufferedReader keyboard = new BufferedReader( new InputStreamReader( System.in ) );
 			String MeetupURL = "http://api.meetup.com/ew/event/";
 			try{
 			System.out.println("Please enter the Url Name: ");
 			URLNAME = keyboard.readLine();
 			System.out.println("Please enter your api key: ");
 			APIKEY = keyboard.readLine();
 			System.out.println("Please enter the google map rss url: ");
 			addr = keyboard.readLine();
 			System.out.println("Please enter the time (millisecs from the epoch): ");
 			time = keyboard.readLine();
 			} catch (Exception e){}
 
 			BufferedReader reader;
 			URL url;
 			HttpURLConnection conn;
 			String params = "";
 			String [] LatLon = new String[2];
 
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
 	    				FileWriter fstream = new FileWriter("temp.xml");
 					BufferedWriter out = new BufferedWriter(fstream);
 					String text = reader.readLine();
 					while (text != null){		
 						out.write(text + "\n");
 						text = reader.readLine();
 					}
 
 	    				//Close the output stream
 	    				out.close();
 
 	    			} catch (Exception e){
 
 					//Catch exception if any
 	      				System.err.println("Error: " + e.getMessage());
 	    			}
 				
 				//disconect from url
 				conn.disconnect();
 
 			} catch(IOException ex) {
 
 				//catch exception
 				ex.printStackTrace();
 			}
 
 			//parse xml
 			try {
				File file = new File("temp.xml");
 	  			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	  			DocumentBuilder db = dbf.newDocumentBuilder();
 	  			Document doc = db.parse(file);
 	  			doc.getDocumentElement().normalize();
 
 	  			NodeList nodeLst = doc.getElementsByTagName("item");
 
 				for (int s = 0; s < nodeLst.getLength(); s++) {
 
 	    				Node fstNode = nodeLst.item(s);
 	    
 	    				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
 
 						//start building paramater list from xml and user data	 
 						params = "urlname=" + URLNAME;
 
 						//add title
 		   				Element fstElmnt = (Element) fstNode;
 						NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("title");
 						Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
 						NodeList fstNm = fstNmElmnt.getChildNodes();
 						params = params + "&title=" + URLEncoder.encode(((Node) fstNm.item(0)).getNodeValue());
 
 						//get lat / lon then split them
 						fstNmElmntLst = fstElmnt.getElementsByTagName("georss:point");
 						fstNmElmnt = (Element) fstNmElmntLst.item(0);
 						fstNm = fstNmElmnt.getChildNodes();
 						LatLon = ((Node) fstNm.item(0)).getNodeValue().trim().split(" ", 2);
 						params = params + "&lat=" + LatLon[0] + "&lon=" + LatLon[1];
 
 						//get description and strip it of html
 						fstElmnt = (Element) fstNode;
 						fstNmElmntLst = fstElmnt.getElementsByTagName("description");
 						fstNmElmnt = (Element) fstNmElmntLst.item(0);
 						fstNm = fstNmElmnt.getChildNodes();
 						params = params + "&description=" + URLEncoder.encode(((Node) fstNm.item(0)).getNodeValue().replaceAll("\\<.*?>",""));
 
 						//add time and api key
 						params = params + "&time=" + time;
 						params = params + "&key=" + APIKEY;
 					
 
 						try{
 							//connect to meetup api
 							url = new URL(MeetupURL);
 							conn = (HttpURLConnection) url.openConnection();
 							conn.setDoOutput(true);
 							conn.setRequestMethod("POST");
 							conn.connect();
 
 							//write the params list
 							OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); 
 							wr.write(params); 
 							wr.flush(); 
 
 							//read back response
 							BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
 							String line; 
 							while ((line = rd.readLine()) != null) { 
 								System.out.println(line);
 							}
 							System.out.println();
 							wr.close(); 
 							rd.close(); 
 				   		
 							//close connection
 							conn.disconnect();
 
 						} catch(IOException ex) {
 	
 							//catch exception and print it out, also print out the params
 							System.out.println();
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
 
