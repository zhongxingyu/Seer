 package uk.ac.ids.linker.impl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.restlet.data.Reference;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import uk.ac.ids.linker.Linker;
 import uk.ac.ids.linker.LinkerParameters;
 
 /**
  * @author Christophe Gueret <christophe.gueret@gmail.com>
  * @author Victor de Boer <v.de.boer@vu.nl>
  * 
  */
 public class DBpedia extends Linker {
 	// Logger instance
 	protected static final Logger logger = Logger.getLogger(DBpedia.class.getName());
 
 	// Parameters
 	public static final String THEME_TITLE = "title";
 
 	// API to query geoname
 	private final static String API = "http://dbpedia.org/sparql?";
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * uk.ac.ids.linker.Linker#getFromService(uk.ac.ids.linker.LinkerParameters)
 	 */
 	@Override
 	protected List<Reference> getFromService(LinkerParameters parameters) {
 		if (!parameters.containsKey(THEME_TITLE))
 			return null;
 
 		String themeTitle = parameters.get(THEME_TITLE);
 
 		// Build the sparql query: limit to 5 (could be one)
 		String sparqlQuery = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
 		sparqlQuery += "prefix skos: <http://www.w3.org/2004/02/skos/core#>";
 		sparqlQuery += "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
 		sparqlQuery += "select distinct ?Concept where  {?Concept rdfs:label \"";
 		sparqlQuery += themeTitle;
 		sparqlQuery += "\"@en .}  LIMIT 5";
 
 		// ?Concept rdf:type skos:Concept.
 
 		try {
 			// Compose the URL
 			sparqlQuery = URLEncoder.encode(sparqlQuery.toString(), "utf-8");
 			StringBuffer urlString = new StringBuffer(API);
 			urlString.append("query=").append(sparqlQuery);
 			URL url = new URL(urlString.toString());
 
 			// Issue the request
 			StringBuffer response = new StringBuffer();
 
 			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			String line;
 			while ((line = reader.readLine()) != null) {
 				response.append(line);
 			}
 			reader.close();
 
 			// Parse the response: return only the first URI. If there are nu
 			// URIs found, return null
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
 			InputSource is = new InputSource();
 			is.setCharacterStream(new StringReader(response.toString()));
 			Document doc = db.parse(is);
 			NodeList results = doc.getElementsByTagName("result");
 			if (results.getLength() > 0) {
				Element element = (Element) results.item(0);
				String uri = element.getElementsByTagName("uri").item(0).getTextContent();

 				List<Reference> res = new ArrayList<Reference>();
				res.add(new Reference(uri));
 				return res;
			} else {
 				return null;
 			}
 
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 			return null;
 		} catch (SAXException e) {
 			e.printStackTrace();
 			return null;
 
 		}
 	}
 }
