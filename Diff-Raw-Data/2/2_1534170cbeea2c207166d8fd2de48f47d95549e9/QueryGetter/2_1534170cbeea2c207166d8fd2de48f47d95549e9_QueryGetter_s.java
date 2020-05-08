 package query;
 
 import java.io.*;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.*;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 
 public class QueryGetter {
 
     public static void main(String[] args) throws Exception {
         List<String> pageIds = getRandomQueryIds();
         /* To get more random articles, go to
          * http://en.wikipedia.org/w/api.php?action=query&list=random&rnlimit=5&rnnamespace=0 */
         DownloadRevHistories(pageIds);
     }
     
     
     public static List<String> getRandomQueryIds() {
     	List<String> idList = new ArrayList<String>();
     	try {
     		for (int i = 0; i < 10; i++) {
     			URL url = new URL("http://en.wikipedia.org/w/api.php?action=query&list=random&rnlimit=10&rnnamespace=0&format=xml");
     			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     			DocumentBuilder builder = factory.newDocumentBuilder();
     			Document xmlDocument = builder.parse(url.openConnection().getInputStream());
     			NodeList pages = xmlDocument.getElementsByTagName("page");
     			for (int j = 0; j < pages.getLength(); j++) {
     				NamedNodeMap attributes = pages.item(j).getAttributes();
     				idList.add(attributes.getNamedItem("id").getNodeValue());
     			}
     		}
     	} catch (Exception ex) {
     		ex.printStackTrace();
     		// do nothing
     	}
     	return idList;
     }
     
     public static BufferedReader GetBufferedReader(URL u) throws Exception {
     	URLConnection connection = u.openConnection();
     	BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
     	return rd;
     }
     
     private static Map<String, String> GetQueryParams(String pageIds, String revStartId) {
     	Map<String, String> queryMap = new HashMap<String, String>();
     	queryMap.put("action", "query");
     	queryMap.put("format", "xml");
     	queryMap.put("prop", "revisions");
     	queryMap.put("pageids", pageIds);
     	queryMap.put("rvlimit", "max");
     	queryMap.put("rvprop", "flags|timestamp|user|comment|size|tags");
     	if (revStartId != null) {
     		queryMap.put("rvstartid", revStartId);
     	}
     	return queryMap;
     	
     }
     
     public static void DownloadRevHistories(List<String> pageIds) throws Exception{
     	for (String pageId : pageIds) {
     		System.out.println("BEGIN DOWNLOAD FOR PAGE ID: " + pageId);
     		URLQuery query = new URLQuery("http://en.wikipedia.org/w/api.php");
     		String revStartId = null;
     		int xmlDocNum = 1;
     		while (true) {
     			Map<String, String> params = GetQueryParams(pageId, revStartId);
     			URL u = query.withQueryParams(params);
    			String filename = "revhistories/" + pageId + "-" + xmlDocNum + ".xml";
     			revStartId = DownloadToFile(u, filename);
     			if (revStartId == null) break;
     			xmlDocNum++;
     		}
     		System.out.println("END DOWNLOAD FOR PAGE ID: " + pageId);
     	}
     }
     
     public static String DownloadToFile(URL u, String filename) throws Exception {
     	BufferedReader rd = GetBufferedReader(u);
     	System.out.println("Downloading: " + u.toString());
     	PrintWriter pw = new PrintWriter(new FileWriter(filename));
     	String inputLine;
     	String revStartId = null;
     	String toFind = "<revisions rvstartid=\"";
     	while ((inputLine = rd.readLine()) != null) {
     		int revStartIdAttrIndex = inputLine.indexOf(toFind);
     		if (revStartIdAttrIndex != -1) {
     			int nextQuoteIndex = inputLine.indexOf("\"", revStartIdAttrIndex + toFind.length());
     			revStartId = inputLine.substring(revStartIdAttrIndex + toFind.length(), nextQuoteIndex);
     		}
     		pw.println(inputLine);
     		//System.out.println("input " + inputLine);
     	}
     	pw.close();
     	rd.close();
     	System.out.println("Saved to: " + filename);
     	return revStartId;
     }
     
 }
