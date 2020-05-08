 package edu.uib.info310.search.builder.ontology.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.TransformerException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
 
 import edu.uib.info310.exception.MasterNotFoundException;
 import edu.uib.info310.search.builder.ontology.DiscogOntology;
 import edu.uib.info310.transformation.XslTransformer;
 
 @Component
 public class DiscogOntologyImpl implements DiscogOntology {
 	//	private static final String searchAlbum = "http://api.discogs.com/search?q=";
 	//	private static final String searchEnd = "&f=xml";
 	//	private static final String kasabi = "http://api.kasabi.com/dataset/discogs/apis/sparql?apikey=fe29b8c58180640f6db16b9cd3bce37c872c2036&output=xml&query=";
 	private static final String release = "http://api.discogs.com/release/";
 	private static final String master =  "http://api.discogs.com/master/";
 	private static final Logger LOGGER = LoggerFactory.getLogger(DiscogOntologyImpl.class);
 	private static final String format = "?f=xml";
 	private static String PREFIX = 
 			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
 					"PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
 					"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
 					"PREFIX mo: <http://purl.org/ontology/mo/>" +
 					"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
 					"PREFIX dc: <http://purl.org/dc/terms/>" +
 					"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>";
 
 
 	/* (non-Javadoc)
 	 * @see edu.uib.info310.search.builder.ontology.DiscogOntology#getDiscography(java.lang.String)
 	 */
 	public Model getDiscography(String search_string){
 		String safe_search = "";
 		try {
 			safe_search = URLEncoder.encode(search_string, "UTF-8");
 		} catch (UnsupportedEncodingException e) {/*ignore*/}
 
 		String searchString =" PREFIX INPUT: <http://www.discogs.com/artist/" + safe_search + ">" +
 				"DESCRIBE * where{ ?artist1 mo:discogs INPUT:." +
 				"?artist1 foaf:made ?allReleases. ?allReleases rdf:type mo:Record.}";
 
 		Query query = QueryFactory.create(PREFIX + searchString);
 		QueryEngineHTTP queryExecution = QueryExecutionFactory.createServiceRequest("http://api.kasabi.com/dataset/discogs/apis/sparql", query);
 
 		queryExecution.addParam("apikey", "fe29b8c58180640f6db16b9cd3bce37c872c2036");
 
 
 
 		return queryExecution.execDescribe();
 	}
 	/* (non-Javadoc)
 	 * @see edu.uib.info310.search.builder.ontology.DiscogOntology#getTracks(java.lang.String)
 	 */
 	public Model getTracks(String search_string){
 		String safe_search = "";
 		try {
 			safe_search = URLEncoder.encode(search_string, "UTF-8");
 		} catch (UnsupportedEncodingException e) {/*ignore*/}
 
 		String searchString =" PREFIX INPUT: <http://data.kasabi.com/dataset/discogs/artist/" + safe_search.toLowerCase().replace("+", "-") + ">" +
 
 	 			"DESCRIBE * where{ ?release foaf:maker INPUT:." +
 	 			"?release rdf:type mo:Track.}";
 
 		Query query = QueryFactory.create(PREFIX + searchString);
 		QueryEngineHTTP queryExecution = QueryExecutionFactory.createServiceRequest("http://api.kasabi.com/dataset/discogs/apis/sparql", query);
 
 		queryExecution.addParam("apikey", "fe29b8c58180640f6db16b9cd3bce37c872c2036");
 
 		return queryExecution.execDescribe();
 	}
 
 
 	/* (non-Javadoc)
 	 * @see edu.uib.info310.search.builder.ontology.DiscogOntology#getAlbum(java.lang.String)
 	 */
 	public Model getAlbum(String search_string){
 		String safe_search = "";
 		try {
 			safe_search = URLEncoder.encode(search_string, "UTF-8");
 		} catch (UnsupportedEncodingException e) {/*ignore*/}
 		
 		
 
 		String searchString =
 				"DESCRIBE ?album WHERE{?album dc:title \""+ search_string + "\" ; rdf:type mo:Record; mo:publisher ?publisher. ?maker foaf:name 'Michael Jackson'. ?album foaf:maker ?maker}";
 		String constructStr = "CONSTRUCT {?artist foaf:made ?record;" +
 				"foaf:name ?artistName;" +
 				"rdf:type mo:MusicArtist ." +
 				"?record rdfs:comment ?comment;" +
 				"dc:issued ?issued;" +
 				"rdf:type mo:Record;" +
 				"mo:discogs ?discogs;" +
 				"foaf:name ?recordName;" +
 				"mo:publisher ?publisher;" +
 				"mo:track ?track";
 
 
 		String whereStr = "} WHERE {?record dc:title \""+ search_string + "\" ;" +
 				"rdf:type mo:Record;" +
 				"foaf:maker ?artist;" +
 				"dc:issued ?issued;" +
 				"mo:discogs ?discogs;" +
 				"dc:title ?recordName;" +
 				"mo:publisher ?publisher;" +
 				"mo:track ?track." +
 				"?artist foaf:name ?artistName}";
 
 
 		Query query = QueryFactory.create(PREFIX + constructStr + whereStr);
 		QueryEngineHTTP queryExecution = QueryExecutionFactory.createServiceRequest("http://api.kasabi.com/dataset/discogs/apis/sparql", query);
 
 		queryExecution.addParam("apikey", "fe29b8c58180640f6db16b9cd3bce37c872c2036");
 
 		return queryExecution.execConstruct();
 	}
 
 
 	/* (non-Javadoc)
 	 * @see edu.uib.info310.search.builder.ontology.DiscogOntology#getAlbums(java.lang.String)
 	 */
 	public Model getAlbums(String search_string){
 		String safe_search = "";
 		try {
 			safe_search = URLEncoder.encode(search_string, "UTF-8");
 		} catch (UnsupportedEncodingException e) {/*ignore*/}
 
 		String constructStr = "CONSTRUCT {?artist foaf:made ?record;" +
 				"foaf:name ?artistName;" +
 				"rdf:type mo:MusicArtist ." +
 				"?record rdf:type mo:Record;" +
 				"mo:discogs ?discogs;" +
 				"dc:title ?record.";
 
 
 		String whereStr = "} WHERE {?record dc:title \""+ safe_search + "\" ;" +
 				"rdf:type mo:Record;" +
 				"foaf:maker ?artist;" +
 				"mo:discogs ?discogs;" +
 				"dc:title ?recordName." +
 				"?artist foaf:name ?artistName}";
 
 
 		Query query = QueryFactory.create(PREFIX + constructStr + whereStr);
 		QueryEngineHTTP queryExecution = QueryExecutionFactory.createServiceRequest("http://api.kasabi.com/dataset/discogs/apis/sparql", query);
 
 		queryExecution.addParam("apikey", "fe29b8c58180640f6db16b9cd3bce37c872c2036");
 
 
 
 		return queryExecution.execConstruct();
 	}
 
 	private InputStream getAlbumInputStream(String uri, String releaseId) throws MasterNotFoundException{
 		InputStream in = null;
 		try{ 
 			URL discogRequest = new URL(uri + releaseId + format);
 			LOGGER.debug("Discogs request URL: " + discogRequest.toExternalForm());
 
 			URLConnection discogConnection = discogRequest.openConnection();
 			discogConnection.setRequestProperty("Accept-Encoding", "gzip,deflate");
 			in = discogConnection.getInputStream();
 		} catch(IOException ioexc){ 
 			ioexc.printStackTrace();
 			throw new MasterNotFoundException("No master found");
 		}
 
 		return in;
 	}
 
 	private Document docBuilder(String uri, String releaseId) throws MasterNotFoundException {
 		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
 		domFactory.setNamespaceAware(true);
 		Document doc = null;
 		DocumentBuilder builder = null;
 		try {
 			builder = domFactory.newDocumentBuilder();
 		} catch (Exception e) {/*ignore*/}
 		try {
 			doc = builder.parse(getAlbumInputStream(uri,releaseId));
 		} catch (Exception a) {
 			a.printStackTrace();
 			throw new MasterNotFoundException("Doc couldn't be built");
 		}
 		return doc;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.uib.info310.search.builder.ontology.DiscogOntology#getAlbumURI(java.lang.String)
 	 */
 	public InputStream getAlbumURI(String releaseId) throws MasterNotFoundException {
 		LOGGER.debug("Trying to find master for  " + releaseId);
 		try{
 			Document released = docBuilder(release, releaseId);
 			LOGGER.debug("Doc working");
 			NodeList nameList = released.getElementsByTagName("master_id");
 			LOGGER.debug("Master_id found.	");
 			Node nameNode = nameList.item(0);
 			Element element = (Element) nameNode;
 			NodeList name = element.getChildNodes();
 			Document mainrelease = docBuilder(master, (name.item(0).getNodeValue()));
 			NodeList mainList = mainrelease.getElementsByTagName("main_release");
 			LOGGER.debug("Main release found.");
 			Node mainNode = mainList.item(0);
 			Element elementm = (Element) mainNode;
 			NodeList main = elementm.getChildNodes();
 
			LOGGER.debug("Get node value of correct artist: " + (name.item(0)).getNodeValue());
 			return getAlbumInputStream(release, (main.item(0)).getNodeValue());
 		}
 		catch (NullPointerException e) {
 			LOGGER.debug("Something went wrong " + releaseId);
 			return getAlbumInputStream(release,releaseId);
 
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.uib.info310.search.builder.ontology.DiscogOntology#getRecordReleaseId(java.lang.String, java.lang.String)
 	 */
 	public String getRecordReleaseId(String record_name, String artist_name) throws MasterNotFoundException{
 		
 
 		String selectString =
 				"SELECT ?album WHERE{" +
 						"?album foaf:maker ?artist;" +
 						"dc:title ?title;" +
 						"rdf:type ?type ." +
 						"?artist foaf:name \""+ artist_name + "\"." +
 
 						" FILTER (?type != mo:Track)." +
 						" FILTER regex(?title, \""+ record_name + "\", \"i\")" +
 						"}";
 
 		Query query = QueryFactory.create(PREFIX + selectString);
 		QueryEngineHTTP queryExecution = QueryExecutionFactory.createServiceRequest("http://api.kasabi.com/dataset/discogs/apis/sparql", query);
 		queryExecution.addParam("apikey", "fe29b8c58180640f6db16b9cd3bce37c872c2036");
 		try{
 
 			ResultSet releaseIdResult = queryExecution.execSelect();
 			String releaseId ="";
 
 
 			QuerySolution queryRelease = releaseIdResult.next();
 			//		LOGGER.debug("" + queryRelease.get("type").toString());
 			String releaseUri = queryRelease.get("album").toString();
 			releaseId = releaseUri.replace("http://data.kasabi.com/dataset/discogs/release/", "");
 
 			return releaseId;
 		}
 		catch (Exception e) {
 			LOGGER.debug("Didn't find artist with name " + artist_name);
 			final StringBuilder result = new StringBuilder(artist_name.length());
 			String[] words = artist_name.split("\\s");
 			for(int i=0,l=words.length;i<l;++i) {
 				if(i>0) result.append(" ");      
 				result.append(Character.toUpperCase(words[i].charAt(0)))
 				.append(words[i].substring(1));
 
 			}
 			artist_name = result.toString();
 			selectString =
 					"SELECT ?album WHERE{" +
 							"?album foaf:maker ?artist;" +
 							"dc:title ?title;" +
 							"rdf:type ?type ." +
 							"?artist foaf:name \""+ artist_name + "\"." +
 
 							" FILTER (?type != mo:Track)." +
 							" FILTER regex(?title, \""+ record_name + "\", \"i\")" +
 							"}";
 
 			query = QueryFactory.create(PREFIX + selectString);
 			queryExecution = QueryExecutionFactory.createServiceRequest("http://api.kasabi.com/dataset/discogs/apis/sparql", query);
 			queryExecution.addParam("apikey", "fe29b8c58180640f6db16b9cd3bce37c872c2036");
 			try{
 
 				ResultSet releaseIdResult = queryExecution.execSelect();
 				String releaseId ="";
 
 
 				QuerySolution queryRelease = releaseIdResult.next();
 				//		LOGGER.debug("" + queryRelease.get("type").toString());
 				String releaseUri = queryRelease.get("album").toString();
 				releaseId = releaseUri.replace("http://data.kasabi.com/dataset/discogs/release/", "");
 
 				return releaseId;
 			}
 			catch (Exception ee) {
 				LOGGER.debug("Didn't find artist with name " + artist_name);
 				throw new MasterNotFoundException("Did not find release for record \"" + record_name + "\" by : " + artist_name );
 			}
 
 		}
 	}
 
 	public static void main(String[] args) throws MasterNotFoundException {
 		ApplicationContext context = new ClassPathXmlApplicationContext("main-context.xml");
 		DiscogOntology search = (DiscogOntology) context.getBean("discogSearch");
 		System.out.println(search.getRecordReleaseId("If It's Lovin' That You Want","Rihanna"));
 	}
 }
 
