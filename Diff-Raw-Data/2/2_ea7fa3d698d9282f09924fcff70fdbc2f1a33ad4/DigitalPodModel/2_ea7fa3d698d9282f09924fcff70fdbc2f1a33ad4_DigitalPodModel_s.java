 package pod_search.model;
 
 import java.net.*;
 import java.io.*;
 
 //Adding this for XML parser
import javax.xml.parser.*;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Element;
 
 public class DigitalPodModel {
 
     private String _searchGenre;
     private String _prefix;
     private String _searchResult;
     private URL _url;
 
     public DigitalPodModel() {
         _searchGenre = "";
         _prefix = "http://www.digitalpodcast.com//podcastsearchservice/v2b/search/?appid=podsearch&keywords=";
     }
 	
 
     public String search(String searchGenre) {
         _searchGenre = searchGenre;
         String combined = _prefix.concat(_searchGenre);
         try {
             URL url = new URL(combined);  
             _url = url; 
             _searchResult = readFromURL(url);
             return _searchResult; 
         }   catch (MalformedURLException e) {
             return "Error!";
         }
         
         
     }
 
     public String getSearchResult(){
         return _searchResult;
     }
 
     public void getSearchResult1(){
         try{
             DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
             Document doc = docBuilder.parse(_url.openStream());
                 
             doc.getDocumentElement().normalize();
             NodeList nList = doc.getElementsByTagName("outline");
             //System.out.println("-----------------------");
                 
             for (int temp = 0; temp < nList.getLength(); temp++) {
                   
                 Node nNode = nList.item(temp);
                 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
             
                     Element eElement = (Element) nNode;
                           
                     String[] podName = new String[nList.getLength()];
                     String[] podUrl = new String[nList.getLength()];
                           
                     podName[temp]= getPodName("outline",eElement);
                     podUrl[temp]= getPodUrl("outline",eElement);
              
                     //System.out.println(podName[temp]+", "+podUrl[temp]);
              
                 }
             }
         }
         catch(Exception e){
             e.printStackTrace();
         }
     }
 
     private String getPodName(String sTag, Element eElement){
         String podName = eElement.getAttribute("text");
 
         return podName;
     }
 
     private String getPodUrl(String sTag, Element eElement){
         String url = eElement.getAttribute("url");
 
         return url;
     }
 
 	private String readFromURL(URL url)
     {
         try
         {
             //Authenticator.setDefault(new ProxyAuthenticator("students\0708886f", "8712276152081"));  
             System.setProperty("http.proxyHost", "127.0.0.1");  
             System.setProperty("http.proxyPort", "3127");  
             URLConnection connection = url.openConnection();
             connection.setDoInput(true);
             InputStream inStream = connection.getInputStream();
             BufferedReader input =
             new BufferedReader(new InputStreamReader(inStream));
     
             String line = "";
             String result = "";
             while ((line = input.readLine()) != null){
                 //System.out.println(line);
                 result += line;
             }
         return result;
         }
         catch (IOException e) {
             return "Error!";
         }
     }
 	
 	// public static void main(String[] args) {
  //    	try {
     		
     		
  //    		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
  //    		String searchGenre = bufferRead.readLine();
 
  //    	    // With a single string.
  //    		String prefix = "http://www.digitalpodcast.com//podcastsearchservice/v2b/search/?appid=podsearch&keywords=";
  //    		String combined = prefix.concat(searchGenre);
  //    	    URL url = new URL(combined);
  //    	    System.out.println(url);
     	    
  //    	    readFromURL(url);
     	    
  //    	} catch (MalformedURLException e) {
  //    	} catch (IOException e) {
 	// 		// TODO Auto-generated catch block
 	// 		e.printStackTrace();
 	// 	}
 //}
 }
