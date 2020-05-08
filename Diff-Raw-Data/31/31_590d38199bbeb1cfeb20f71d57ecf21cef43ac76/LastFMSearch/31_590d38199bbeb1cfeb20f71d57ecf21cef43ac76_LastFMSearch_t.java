 package edu.uib.info310.search;
 
 import java.io.File;
 import java.io.FileOutputStream;
import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import edu.uib.info310.transformation.XslTransformer;
 
 @Component
 public class LastFMSearch {
 	private static final String similarArtistRequest = "http://ws.audioscrobbler.com/2.0/?method=artist.getsimilar&artist=";
 	private static final String artistEvents = "http://ws.audioscrobbler.com/2.0/?method=artist.getevents&artist=";
 	private static final String artistCorrection = "http://ws.audioscrobbler.com/2.0/?method=artist.getcorrection&artist=";
 	private static final String apiKey = "&api_key=a7123248beb0bbcb90a2e3a9ced3bee9";
 	private static final Logger LOGGER = LoggerFactory.getLogger(LastFMSearch.class);
 	
	public InputStream artistCorrection(String search_string)  {
	    try{ 

 		URL lastFMRequest = new URL(artistCorrection + search_string + apiKey);
		System.out.println(lastFMRequest);
         URLConnection lastFMConnection = lastFMRequest.openConnection();
		System.out.println("Test1");
 		return lastFMConnection.getInputStream();
    } catch(IOException ioexc){ 
        System.out.println("Unavailable: "+ioexc.getMessage()); 
    }
		return null; 

 	}
 	
 	private Document docBuilder(String artist) throws Exception{
 	    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
 	    domFactory.setNamespaceAware(true);
 	    DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = null;
		String safe_search = "";
		try {
			safe_search = URLEncoder.encode(artist, "UTF-8");
		} catch (UnsupportedEncodingException e) {/*ignore*/}
	    doc = builder.parse(artistCorrection(safe_search));  


		System.out.println("Test2");
 	    return doc;
 }
 	public String correctArtist(String artist) throws Exception {
 		Document correction = docBuilder(artist);
 		
 		NodeList nameList = correction.getElementsByTagName("name");
 		Node nameNode = nameList.item(0);
 		Element element = (Element) nameNode;
 		NodeList name = element.getChildNodes();
 		return (name.item(0)).getNodeValue();
 	}
 	
 	
 	public InputStream getArtistEvents (String artist) throws Exception {
 		String safeArtist = makeWebSafeString(artist);
         URL lastFMRequest = new URL(artistEvents + safeArtist + apiKey);
         URLConnection lastFMConnection = lastFMRequest.openConnection();
         		
 		return lastFMConnection.getInputStream();
     }
 	
 	public InputStream getSimilarArtist(String artist) throws Exception {
 		String safeArtist = makeWebSafeString(artist);
         URL lastFMRequest = new URL(similarArtistRequest + safeArtist + apiKey);
         LOGGER.debug("LastFM request URL: " + lastFMRequest.toExternalForm());
         URLConnection lastFMConnection = lastFMRequest.openConnection();
         		
 		return lastFMConnection.getInputStream();
     }
 	
 	
 	public static void main(String[] args) throws Exception {
 		
 		File xsl = new File("src/main/resources/XSL/SimilarArtistLastFM.xsl");
 		
 		LastFMSearch search = new LastFMSearch();
 		XslTransformer transform = new XslTransformer();
 		
 		transform.setXml(search.getSimilarArtist("Iron & Wine"));
 		transform.setXsl(xsl);
 		
 		File file = new File("log/rdf-artist.xml");
 		
 		FileOutputStream fileOutputStream = new FileOutputStream(file);
 		transform.transform().writeTo(fileOutputStream);
 //		System.out.println(transform.transform());
		System.out.println(search.correctArtist("ryksopp"));
 		
 	}
 	
 	
 	public String makeWebSafeString(String unsafe){
 		try {
 			return URLEncoder.encode(unsafe, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			return unsafe;
 		}
 	}
 	
 	
 
 }
