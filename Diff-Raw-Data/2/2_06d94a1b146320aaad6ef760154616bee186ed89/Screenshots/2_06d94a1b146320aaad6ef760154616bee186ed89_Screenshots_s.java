 package bzb.se.update;
 
 import java.io.File;
 import org.w3c.dom.Document;
 import org.w3c.dom.*;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException; 
 import net.dapper.scrender.Scrender;
 
 /*
  *	Looks up web content and creates screenshots
  */
 
 public class Screenshots {
 
 	public static void main(String[] args) {
 	
 		try {
 
 		        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 	        	Document doc = docBuilder.parse (new File("res/markers.xml"));
 
 		        // normalize text representation
   		      	doc.getDocumentElement ().normalize ();
         	
         		NodeList markers = doc.getElementsByTagName("marker");
 		        int total = markers.getLength();
         		System.out.println("Total markers: " + total);
 
 	        	for(int s = 0; s < markers.getLength(); s++){
 
 	            		Node firstNode = markers.item(s);
         	    		if(firstNode.getNodeType() == Node.ELEMENT_NODE){
 
 	                		Element firstElement = (Element)firstNode;
 					if (firstElement.hasAttribute("markerSiteURL")) {
 						String mediaURL = firstElement.getAttribute("markerSiteURL").trim();
 						if (mediaURL.length() > 0) {
         		       		 		System.out.println("Media URL: " + mediaURL);
 		
 							try {		
 								Scrender scrender = new Scrender();
 								scrender.init();
								scrender.render(mediaURL, new File("./screenshots/" + firstElement.getAttribute("markerRecord").trim() + ".jpg"));
 							} catch (Exception e) {
 								e.printStackTrace();
 							}
 						} else {
 							System.out.println("No media URL");
 						}
 					} else {
 						System.out.println("No media URL");
 					}
         	    		}//end of if clause
 
         		}//end of for loop with s var
 
     		}catch (SAXParseException err) {
     			System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
  	   		System.out.println(" " + err.getMessage ());
    	 	}catch (SAXException e) {
    	 		Exception x = e.getException ();
     			((x == null) ? e : x).printStackTrace ();
 		}catch (Throwable t) {
     			t.printStackTrace ();
     		}
 	}
 
 }
