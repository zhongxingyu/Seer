 package edu.uib.info310.search.builder;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Locale;
 
 import javax.xml.transform.TransformerException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.stereotype.Component;
 
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 
 import edu.uib.info310.exception.ArtistNotFoundException;
 import edu.uib.info310.exception.MasterNotFoundException;
 import edu.uib.info310.search.builder.ontology.BBCOntology;
 import edu.uib.info310.search.builder.ontology.DBPediaOntology;
 import edu.uib.info310.search.builder.ontology.DiscogOntology;
 import edu.uib.info310.search.builder.ontology.ITunesOntology;
 import edu.uib.info310.search.builder.ontology.LastFMOntology;
 import edu.uib.info310.transformation.XslTransformer;
 
 @Component
 public class OntologyBuilderImpl implements OntologyBuilder {
 
 	@Autowired
 	private LastFMOntology search;
 
 	@Autowired
 	private XslTransformer transformer;
 
 	@Autowired
 	private ITunesOntology itunes;
 
 	@Autowired
 	private DiscogOntology discog;
 
 	@Autowired
 	private BBCOntology bbc;
 
 	@Autowired
 	private DBPediaOntology dbp;
 
 	private static final String SIMILAR_XSL = "src/main/resources/XSL/SimilarArtistLastFM.xsl";
 	private static final String ARTIST_EVENTS_XSL = "src/main/resources/XSL/Events.xsl";
 	private static final Logger LOGGER = LoggerFactory.getLogger(OntologyBuilderImpl.class);
 
 
 	/* (non-Javadoc)
 	 * @see edu.uib.info310.search.builder.OntologyBuilder#createArtistOntology(java.lang.String)
 	 */
 	public Model createArtistOntology(String search_string) throws ArtistNotFoundException {
 		Model model = ModelFactory.createDefaultModel();
 		String correctName = search.correctArtist(search_string);
 
 		try {
 			transformer.setXml(search.getSimilarArtist(correctName));
 			transformer.setXsl(new File(SIMILAR_XSL));
 
 			InputStream in = new ByteArrayInputStream(transformer.transform().toByteArray());
 
 			model.read(in, null);
 			LOGGER.debug("Model size after getting similar artists: " + model.size());
 			transformer.setXml(search.getArtistEvents(correctName));
 			transformer.setXsl(new File(ARTIST_EVENTS_XSL));
 
 			in = new ByteArrayInputStream(transformer.transform().toByteArray());
 			model.read(in, null);
 			LOGGER.debug("Model size after getting artist events: " + model.size());
 		} catch(Exception e) {
 			LOGGER.error("Threw exception" + e);
 		}
 
 		String id = "";
 		String artistName = "";
 		String getIdQuery = "PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX mo:<http://purl.org/ontology/mo/> SELECT ?id ?name WHERE {?id mo:similar-to ?something; foaf:name ?name . }";
 		QueryExecution exec = QueryExecutionFactory.create(getIdQuery, model);
 		ResultSet ids = exec.execSelect();
 		if(ids.hasNext()){
 			QuerySolution sol = ids.next(); 
 			id = sol.get("id").asResource().getURI();
 			artistName = sol.get("name").asLiteral().getString();
 			LOGGER.debug("Sending id:" + id + ", and name: " + artistName + "  to iTunes, BBC and DBPedia");
 		}else{
 			LOGGER.debug("Did not find id for artist with name " + correctName);
 		}
 
 		if(artistName.isEmpty() || artistName == null || model.size() < 3) {
 			LOGGER.debug("Artist not found");
 			throw new ArtistNotFoundException("Last.FM found name, but no data.");
 		}
 
 
 		model.add(itunes.getRecordsByArtistName(artistName, id));
 		LOGGER.debug("Model size after adding record info from iTunes: " + model.size());
 		try{
 			model.add(bbc.getArtistModel(artistName, id));
 			LOGGER.debug("Model size after adding artist info from BBC: " + model.size());
 		}
 		catch (Exception e) {
 			LOGGER.error("Error retreiving model from BBC, maybe the server is down: " + e.toString());
 		}
 		try{
 			model.add(dbp.getArtistModel(artistName, id));
 			LOGGER.debug("Model size after adding artist info from DBPedia: " + model.size());
 		} 
 		catch (Exception e) {
 			LOGGER.error("Error retreiving model from DBPedia, maybe the server is down: " + e.toString());
 		}
 
 		try {
 			FileOutputStream out = new FileOutputStream(new File("log/ontout.ttl"));
 			model.write(out,"TURTLE");
 		} catch (FileNotFoundException e) {
 			LOGGER.error("Error writing to file");
 		}
 
 
 		return  model;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.uib.info310.search.builder.OntologyBuilder#createRecordOntology(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	public Model createRecordOntology(String releaseId, String record_name, String artist_name){
 
 		LOGGER.debug("Got releaseID: " + releaseId);
 		File xsl = new File("src/main/resources/XSL/AlbumXSLT.xsl");
 
 		XslTransformer transform = new XslTransformer();
 
 		try {
 			transform.setXml(discog.getAlbumURI(releaseId));
 		} catch (MasterNotFoundException e1) {
 			LOGGER.error(e1.toString());
 		}
 		transform.setXsl(xsl);
 
 		Model model = ModelFactory.createDefaultModel();
 		InputStream in = null;
 		try {
 			in = new ByteArrayInputStream(transform.transform().toByteArray());
 		} catch (TransformerException e1) {
 			LOGGER.error(e1.toString());
 		}
 		model.read(in,null);
 		LOGGER.debug("Model size after getting album info: " + model.size());
 
		String newUriQuery = "PREFIX mo: <http://purl.org/ontology/mo/> SELECT ?discogs WHERE {?o mo:discogs ?discogs.}";
		QueryExecution execution = QueryExecutionFactory.create(newUriQuery, model);
		ResultSet albumResults = execution.execSelect();
		QuerySolution uriResult = albumResults.next();
		String albumUri = "http://api.discogs.com/release/" + uriResult.get("discogs").toString();
 		Model itunesModel = itunes.getRecordWithNameAndArtist(albumUri, record_name, artist_name);
 		model.add(itunesModel);
 		LOGGER.debug("Model size after getting iTunes info: " + model.size());
 		try {
 			FileOutputStream out = new FileOutputStream(new File("log/albumout.ttl"));
 			model.write(out,"TURTLE");
 		} catch (FileNotFoundException e) {
 			LOGGER.error(e.toString());
 		}
 
 		return model;
 	}
 
 	public static void main(String[] args) throws ArtistNotFoundException {
 		ApplicationContext context = new ClassPathXmlApplicationContext("main-context.xml");
 		OntologyBuilder builder = (OntologyBuilder) context.getBean("ontologyBuilderImpl");
 		builder.createArtistOntology("Bjrk");
 	}
 
 }
