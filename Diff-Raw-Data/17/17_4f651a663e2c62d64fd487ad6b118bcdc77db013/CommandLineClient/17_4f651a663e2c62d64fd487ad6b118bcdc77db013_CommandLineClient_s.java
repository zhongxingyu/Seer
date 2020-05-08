 package de.fuberlin.wiwiss.ng4j.semwebclient;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.ResultSetFormatter;
 
 public class CommandLineClient {
 	
 	private SemanticWebClient client = null;
 	
 	private String sparqlQuery = null;
 	
 	private String sparqlQueryFromFile = null;
 	
 	private Triple queryTriple = null;
 	
 	private boolean outputRetrievedURIs = false;
 	
 	private boolean outputFailedURIs = false;
 	
 	private List graphsToAdd = new ArrayList();
 	
 	private String writeGraphSetDestination = null;
 	
 	private String writeGraphSetFormat = null;
 	
 	private String loadGraphSetSource = null;
 	
 	private String loadGraphSetFormat = null;
 	
 	private int maxsteps = -1;
 	
 	private long timeout = -1;
 	
 	private int maxthreads = -1;
 	
 	
 	
 	public CommandLineClient(){
 		this.client = new SemanticWebClientImpl();
 	}
 
 	/**
 	 * Sets a SPARQL query to be executed against the Semantic Web.
 	 */
 	public void setSPARQLQuery(String query) {
 		this.sparqlQuery = query;
 		
 		
 	}
 
 	/**
 	 * A SPARQL query is loaded from a file and executed against the Semantic Web.
 	 */
 	public void setSPARQLFile(String filename) {
 		this.sparqlQueryFromFile = filename;
 	}
 	
 	/**
 	 * Sets a triple pattern to be used as a query against the Semantic Web. 
 	 * @param triple An RDF triple, may contain Node.ANY wildcards
 	 */
 	public void setFindTriple(Triple triple) {
 		this.queryTriple = triple;
 	}
 	
 	/**
 	 * Sets the maximal number of iterations of the retrieval algorithm. The default value is 3.
 	 */
 	public void setMaxSteps(int maxSteps) {
 		this.maxsteps = maxSteps;
 	}
 	
 	/**
 	 * Sets the timeout of the query in milliseconds. The default value is 10000.
 	 */
 	public void setTimeout(long timeoutMilliseconds) {
 		this.timeout = timeoutMilliseconds;
 	}
 
 	/**
 	 * Sets the maximal number of parallel threads for retrieving URIs. The default is 10.
 	 */
 	public void setMaxThreads(int maxThreads) {
 		this.maxthreads = maxThreads;
 	}
 	
 	/**
 	 * Loads a file from the Web before the query is executed.
 	 */
 	public void addSourceURI(String uri) {	// Can be called multiple times
 		this.graphsToAdd.add(uri);
 	}
 	
 	/**
 	 * Loads a set of graphs from a file before the query is executed.
 	 * @param file Filename of the graph set file
 	 * @param format "TRIG" or "TRIX"
 	 */
 	public void setLoadGraphSet(String file, String format) {	// format is "TRIG" or "TRIX"
 		this.loadGraphSetSource = file;
 		this.loadGraphSetSource = format;	
 	}
 	
 	/**
 	 * Saves all graphs that have been retrieved into a file after query execution has finished.
 	 * @param file Filename of the destination file
 	 * @param format "TRIG" or "TRIX"
 	 */
 	public void setWriteGraphSet(String file, String format) {	// format is "TRIG" or "TRIX"
 		this.writeGraphSetDestination = file;
 		this.writeGraphSetFormat = format;
 	}
 	
 	/**
 	 * Output a list of all successfully retrieved URIs?
 	 */
 	public void setOutputRetrievedURIs(boolean outputRetrievedURIs) {
 		this.outputRetrievedURIs = outputRetrievedURIs;
 	}
 	
 	/**
 	 * Output a list of all URIs that could not be retrieved?
 	 */
 	public void setOutputFailedURIs(boolean outputFailedURIs) {
 		this.outputFailedURIs = outputFailedURIs;
 	}
 	
 	/**
 	 * Executes the query specified using the other options.
 	 * @throws Exception Indicates an error
 	 */
 	public void run() throws Exception {
 		executeConfigure();
 		executeLoadNamendGraphSet();
 		executeAddGraphs();
 		executeSparqlFromFile();
 		executeSparqlQuery();
 		executeFindQuery();
 		executeOutput();
 		executeWriteGraphset();
 		this.client.close();
 	}
 	
 	private void executeAddGraphs()throws MalformedURLException,IOException{
 		Iterator it = this.graphsToAdd.iterator();
 		while (it.hasNext()) {
 			String graphuri = (String) it.next();
 			URL url = null;
 			HttpURLConnection connection = null;
 			url = new URL(graphuri);
 			if(url !=null){
 				connection = (HttpURLConnection) url.openConnection();	
 				this.client.read(connection.getInputStream(), guessLanguage(connection), url.toString());	
 			}
 		}
 	}
 	private void executeLoadNamendGraphSet() throws Exception{
 		if(this.loadGraphSetSource != null){
 			this.client.read(this.loadGraphSetSource,this.loadGraphSetFormat);
 		}
 	}
 	
 	private void executeSparqlFromFile() throws FileNotFoundException, IOException{
 		if(this.sparqlQueryFromFile != null){
 			BufferedReader in = new BufferedReader(
 					new InputStreamReader(new FileInputStream(this.sparqlQueryFromFile) ) );
			this.sparqlQuery = in.readLine();
 			in.close();
 		}
 	}
 	
 	private void executeSparqlQuery()throws Exception{
 		if(this.sparqlQuery != null){
 			Query query;
 			query = QueryFactory.create(this.sparqlQuery); 
 			QueryExecution qe = QueryExecutionFactory.create(query, this.client.asJenaModel("default")); 
 			ResultSet results = qe.execSelect(); 
 			ResultSetFormatter.out(System.out, results, query); 
 		}
 		
 	}
 	
 	private void executeFindQuery()throws Exception{
 		if(this.queryTriple != null){
 			SemWebIterator iter = this.client.find(this.queryTriple);
 			while (iter.hasNext()) {
 				SemWebTriple triple = (SemWebTriple) iter.next();
 				System.out.println(triple.toString());	
 			}
 		}
 	}
 	
 	private void executeOutput()throws Exception{
 		if(this.outputRetrievedURIs){
 			System.out.println("Successfully dereferenced URIs: ");
 			Iterator it = this.client.successfullyDereferencedURIs();
 			while (it.hasNext()) {
 				String uri = (String) it.next();
 				System.out.println(uri);
 			}
 		}
 		if(this.outputFailedURIs){
 			System.out.println("Unsuccessfully dereferenced URIs: ");
 			Iterator it = this.client.unsuccessfullyDereferencedURIs();
 			while (it.hasNext()) {
 				String uri = (String) it.next();
 				System.out.println(uri);
 			}
 		}	
 	}
 	
	private void executeWriteGraphset()throws FileNotFoundException{
 		if(this.writeGraphSetDestination != null){
			OutputStream out;
			out = new FileOutputStream(this.writeGraphSetDestination); 
 			this.client.write(out,this.writeGraphSetFormat,null);
 		}
 	}
 	
 	private String guessLanguage(HttpURLConnection con){
 		String type = con.getContentType();
 		if(type == null)
 			return null;
 		
 		if(type.startsWith("application/rdf+xml")||type.startsWith("text/xml")||
 				type.startsWith("application/xml")||type.startsWith("application/rss+xml")||
 				type.startsWith("text/plain"))
 				return "RDF/XML";
 		if(type.startsWith("application/n3")||type.startsWith("application/x-turtle")||
 				type.startsWith("text/rdf+n3"))
 				return "N3";
 	
 		return type;
 	}
 	
 	private void executeConfigure()throws Exception{
 		if(this.maxsteps != -1)
 			this.client.setConfig("maxsteps",Integer.toString(this.maxsteps));
 		if(this.timeout != -1)
 			this.client.setConfig("timeout",Long.toString(this.timeout));
 		if(this.maxthreads != -1)
 			this.client.setConfig("maxthreads",Long.toString(this.maxthreads));
 	}
 }
