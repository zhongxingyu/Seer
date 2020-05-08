 package edu.uib.info310.search;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.xml.transform.TransformerException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.stereotype.Component;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
 
 import edu.uib.info310.exception.ArtistNotFoundException;
 import edu.uib.info310.exception.MasterNotFoundException;
 import edu.uib.info310.model.Artist;
 import edu.uib.info310.model.Event;
 import edu.uib.info310.model.Record;
 import edu.uib.info310.model.Track;
 import edu.uib.info310.model.factory.ModelFactory;
 import edu.uib.info310.model.factory.ModelFactoryImpl;
 import edu.uib.info310.model.imp.RecordImp;
 import edu.uib.info310.model.mock.MockRecord;
 import edu.uib.info310.search.builder.OntologyBuilder;
 import edu.uib.info310.search.builder.ontology.DiscogOntology;
 
 @Component
 public class SearcherImpl implements Searcher {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(SearcherImpl.class);
 	@Autowired
 	private OntologyBuilder builder;
 	@Autowired
 	private ModelFactory modelFactory;
 	@Autowired
 	private DiscogOntology discog;
 	private Model model;
 	private Artist artist;
 	private Record record;
 
 	public Artist searchArtist(String search_string) throws ArtistNotFoundException {
 		this.artist = modelFactory.createArtist();
 		this.model = builder.createArtistOntology(search_string);
 		LOGGER.debug("Size of infered model: " + model.size());
 
 		setArtistIdAndName();
 
 		setSimilarArtist();
 
 		setArtistEvents();
 
 		setArtistDiscography();
 
 		setArtistInfo();
 
 		return this.artist;
 	}
 
 
 	public List<Record> searchRecords(String albumName) {
 		List<Record> records= new LinkedList<Record>();
 		
 		String safe_search = "";
 		try {
 			safe_search = URLEncoder.encode(albumName, "UTF-8");
 		} catch (UnsupportedEncodingException e) {/*ignore*/}
 		
 		String prefix = 
 				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
 				"PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
 				"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
 				"PREFIX mo: <http://purl.org/ontology/mo/>" +
 				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
 				"PREFIX dc: <http://purl.org/dc/terms/>" +
 				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>";
 		
 		String albums =  "SELECT ?artistName ?albumName ?year "+
 				" WHERE {	?record dc:title \""+ safe_search + "\" ;" +
 									"rdf:type mo:Record ;" +
 									"foaf:maker ?artist ;" +
 									"mo:discogs ?discogs ;" +
 									"dc:issued ?year ;" +
 									"dc:title ?albumName." +
 									"?artist foaf:name ?artistName}" +
 									"ORDER BY ?year";
 
 		Query query = QueryFactory.create(prefix + albums);
 		QueryEngineHTTP queryExecution = QueryExecutionFactory.createServiceRequest("http://api.kasabi.com/dataset/discogs/apis/sparql", query);
 		queryExecution.addParam("apikey", "fe29b8c58180640f6db16b9cd3bce37c872c2036");
 		ResultSet recordResults = queryExecution.execSelect();
 		
 		while(recordResults.hasNext()){
 			List<Artist> artists = new LinkedList<Artist>();
 			Record record = modelFactory.createRecord();
 			QuerySolution querySol = recordResults.next();
 			record.setName(querySol.get("albumName").toString());
 			Artist artist = modelFactory.createArtist();
 			artist.setName(querySol.get("artistName").toString());
 			artists.add(artist);
 			record.setArtist(artists);
 			
 //			Date date = this.makeDate(querySol.get("year").toString());	
 
 			record.setYear(querySol.get("year").toString().substring(0,4));
 //			recordResult.setDiscogId(querySol.get("discogs").toString());
 			if(!records.contains(record)){
 				records.add(record);
 			}
 		}
 
 		return records; 
 	}
 
 	private void setArtistIdAndName() {
 		String getIdStr = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX mo:<http://purl.org/ontology/mo/> PREFIX foaf:<http://xmlns.com/foaf/0.1/>  SELECT ?id ?name WHERE {?id foaf:name ?name; mo:similar-to ?something.}";
 		QueryExecution execution = QueryExecutionFactory.create(getIdStr, model);
 		ResultSet similarResults = execution.execSelect();
 		if(similarResults.hasNext()){
 			QuerySolution solution = similarResults.next();
 			this.artist.setId(solution.get("id").toString());
 			this.artist.setName(solution.get("name").toString());
 		}
 		LOGGER.debug("Artist id set to: " + this.artist.getId() + ", Artist name set to: " + this.artist.getName());
 
 	}
 
 	private void setArtistDiscography() {
 		List<Record> discog = new LinkedList<Record>();
 		Map<String,Record> uniqueRecord = new HashMap<String, Record>();
 
 		String getDiscographyStr = 	"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
 				"PREFIX mo: <http://purl.org/ontology/mo/> " +
 				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
 				"PREFIX dc: <http://purl.org/dc/terms/> " + 
 				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
 				"SELECT DISTINCT " +
 				" ?artistId ?albumId ?release ?title ?image ?year ?labelId ?labelName ?track ?artist  "+
 				" WHERE { " + 
 				"?artistId foaf:made ?albumId. " +
 				"?albumId dc:title ?title." +
 				"OPTIONAL {?albumId mo:publisher ?labelId. } "+
 				"OPTIONAL {?albumId dc:issued ?year. }" +
 				"OPTIONAL {?albumId foaf:depiction ?image. }" +
 				"}";
 
 
 		LOGGER.debug("Search for albums for artist with name: " + this.artist.getName() + ", with query:" + getDiscographyStr);
 		QueryExecution execution = QueryExecutionFactory.create(getDiscographyStr, model);
 		ResultSet albums = execution.execSelect();
 
 		LOGGER.debug("Found records? " + albums.hasNext());
 		while(albums.hasNext()){
 
 			Record recordResult = modelFactory.createRecord();
 			QuerySolution queryAlbum = albums.next();
 			recordResult.setId(queryAlbum.get("albumId").toString());
 			recordResult.setName(queryAlbum.get("title").toString());
 			if(queryAlbum.get("image") != null) {
 				recordResult.setImage(queryAlbum.get("image").toString());
 			}
 			if(queryAlbum.get("year") != null) {
 				SimpleDateFormat format = new SimpleDateFormat("yyyy",Locale.US);
 				recordResult.setYear(format.format(makeDate(queryAlbum.get("year").toString())));
 			}
 			if(recordResult.getImage() != null){
 				uniqueRecord.put(recordResult.getName(), recordResult);
 			}
 		}
 		for(Record record : uniqueRecord.values()){
 			discog.add(record);
 		}
 
 		this.artist.setDiscography(discog);
 		LOGGER.debug("Found "+ artist.getDiscography().size() +"  artist records");
 	}
 
 	private void setSimilarArtist() {
 		List<Artist> similar = new LinkedList<Artist>();
 		String similarStr = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
 				"PREFIX mo:<http://purl.org/ontology/mo/>  " +
 				"PREFIX foaf:<http://xmlns.com/foaf/0.1/> " +
 				"SELECT ?name ?id ?image " +
 				" WHERE { <" + this.artist.getId() + "> mo:similar-to ?id . " +
 				"?id foaf:name ?name; " +
 				" mo:image ?image } ";
 
 		QueryExecution execution = QueryExecutionFactory.create(similarStr, model);
 		ResultSet similarResults = execution.execSelect();
 
 		while(similarResults.hasNext()){
 			Artist similarArtist = modelFactory.createArtist();
 			QuerySolution queryArtist = similarResults.next();
 			similarArtist.setName(queryArtist.get("name").toString());
 			similarArtist.setId(queryArtist.get("id").toString());
 			similarArtist.setImage(queryArtist.get("image").toString());
 			similar.add(similarArtist);
 		}
 		artist.setSimilar(similar);
 		LOGGER.debug("Found " + this.artist.getSimilar().size() +" similar artists");
 	}
 
 	private void setArtistEvents(){
 		List<Event> events = new LinkedList<Event>();
 		String getArtistEventsStr = " PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX event: <http://purl.org/NET/c4dm/event.owl#> PREFIX v: <http://www.w3.org/2006/vcard/ns#> PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>" +
 				"SELECT ?venueId ?venueName ?date ?lng ?lat ?location ?preformance " +
 				" WHERE {?preformance foaf:hasAgent <" + this.artist.getId() + ">; event:place ?venueId; event:time ?date. ?venueId v:organisation-name ?venueName; geo:lat ?lat; geo:long ?lng; v:locality ?location}";
 		QueryExecution execution = QueryExecutionFactory.create(getArtistEventsStr, model);
 		ResultSet eventResults = execution.execSelect();
 		while(eventResults.hasNext()){
 			Event event = modelFactory.createEvent();
 			QuerySolution queryEvent = eventResults.next();
 			event.setId(queryEvent.get("venueId").toString());
 			event.setVenue(queryEvent.get("venueName").toString());
 			event.setLat(queryEvent.get("lat").toString());
 			event.setLng(queryEvent.get("lng").toString());
 			
 			String dateString = queryEvent.get("date").toString();
 			Date date = new Date();
 			SimpleDateFormat stf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss",Locale.US);
 			try {
 				date = stf.parse(dateString);
 				
 			} catch (ParseException e) {
 				LOGGER.error("Couldnt parse date");
 			}
 			
 			event.setDate(date);
 			event.setLocation(queryEvent.get("location").toString());
 			event.setWebsite(queryEvent.get("preformance").toString());
 			events.add(event);
 		}
 		this.artist.setEvents(events);
 		LOGGER.debug("Found "+ artist.getEvents().size() +"  artist events");
 	}
 
 	private void setArtistInfo() {
 		String id = "<" +  artist.getId() + ">";
 		LOGGER.debug("Artisturi " + artist.getName());
 		String getArtistInfoStr = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
 				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
 				"PREFIX mo: <http://purl.org/ontology/mo/> " +
 				"PREFIX dbpedia: <http://dbpedia.org/property/> " +
 				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
 				"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
 				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
 				"PREFIX dbont: <http://dbpedia.org/ontology/> " +
 				"SELECT DISTINCT * WHERE {" +
 				"OPTIONAL { "+ id +" mo:fanpage ?sFanpage. BIND(str(?sFanpage) AS ?fanpage)} " +
 				"OPTIONAL { "+ id +" mo:imdb ?imdb. }" +
 				"OPTIONAL { "+ id +" foaf:name ?name. }"  +
 				"OPTIONAL { "+ id +" mo:myspace ?myspace. } " +
 				"OPTIONAL { "+ id +" mo:homepage ?homepage. } " +
 				"OPTIONAL { "+ id +" rdfs:comment ?sShortDesc. BIND(str(?sShortDesc) AS ?shortDesc)}  " +
 				"OPTIONAL { "+ id +" mo:image ?image}" +
 				"OPTIONAL { "+ id +" mo:biography ?sBio. BIND(str(?sBio) AS ?bio) } " +
 				"OPTIONAL { "+ id +" dbont:birthname ?birthname} " +
 				"OPTIONAL { "+ id +" dbont:hometown ?hometown. } " +
 				"OPTIONAL { "+ id +" mo:origin ?sOrigin. BIND(str(?sOrigin) AS ?origin)} " +
 				"OPTIONAL { "+ id +" mo:activity_start ?start. } " +
 				"OPTIONAL { "+ id +" mo:activity_end ?end. } " +
 				"OPTIONAL { "+ id +" dbont:birthDate ?birthdate. } " +
 				"OPTIONAL { "+ id +" dbont:deathDate ?deathdate. } " +
 				"OPTIONAL { "+ id +" mo:wikipedia ?wikipedia. } " +
 				"OPTIONAL { "+ id +" foaf:page ?bbcpage. }" +
 				"OPTIONAL { "+ id +" dbont:bandMember ?memberOf. ?memberOf rdfs:label ?sName1. BIND(str(?sName1) AS ?name1)}" +
 				"OPTIONAL { "+ id +" dbont:formerBandMember ?pastMemberOf. ?pastMemberOf rdfs:label ?sName2. BIND(str(?sName2) AS ?name2) }" +
 				"OPTIONAL { "+ id +" dbpedia:currentMembers ?currentMembers. ?currentMembers rdfs:label ?sName3. BIND(str(?sName3) AS ?name3) }" +
 				"OPTIONAL { "+ id +" dbpedia:pastMembers ?pastMembers. ?pastMembers rdfs:label ?sName4. BIND(str(?sName4) AS ?name4) }}" ;
 
 
 		
 		QueryExecution ex = QueryExecutionFactory.create(getArtistInfoStr, model);
 		ResultSet results = ex.execSelect();
 		HashMap<String,Object> metaMap = new HashMap<String,Object>();
 		List<String> fanpages = new LinkedList<String>();
 		List<String> bands = new LinkedList<String>();
 		List<String> formerBands = new LinkedList<String>();
 		List<String> currentMembers = new LinkedList<String>();
 		List<String> pastMembers = new LinkedList<String>();
 		while(results.hasNext()) {
 			
 			// TODO: optimize (e.g storing in variables instead of performing query.get several times?)
 			QuerySolution query = results.next();
 			if(query.get("name") != null){
 				LOGGER.debug("Artistname " + query.get("name").toString());
 			}
 			if(query.get("image") != null){
 				artist.setImage(query.get("image").toString());
 			}
 			if(query.get("fanpage") != null){
 				String fanpage = "<a href=\"" + query.get("fanpage").toString() + "\">" + query.get("fanpage").toString() + "</a>";
 
 				if(!fanpages.contains(fanpage)) {
 					fanpages.add(fanpage);
 				}
 			}
 			if(query.get("memberOf") != null){
 				String test2 = query.get("name1").toString();
 				String test = null;
 				try {
 					test = URLEncoder.encode(test2, "UTF-8");
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				String memberOf = "<a href=\"artist?q=" + test + "\">" + test2 + "</a>";
 
 				if(!bands.contains(memberOf)) {
 					bands.add(memberOf);
 				}
 			}
 			if(query.get("pastMemberOf") != null){
 				String test2 = query.get("name2").toString();
 				String test = null;
 				try {
 					test = URLEncoder.encode(test2, "UTF-8");
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				String pastMemberOf = "<a href=\"artist?q=" + test + "\">" + test2 + "</a>";
 
 				if(!formerBands.contains(pastMemberOf)) {
 					formerBands.add(pastMemberOf);
 				}
 			}
 			if(query.get("currentMembers") != null){
 				String test2 = query.get("name3").toString();
 				String test = null;
 				try {
 					test = URLEncoder.encode(test2, "UTF-8");
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				String currentMember = "<a href=\"artist?q=" + test + "\">" + test2 + "</a>";
 
 				if(!currentMembers.contains(currentMember)) {
 					currentMembers.add(currentMember);
 				}
 			}
 			if(query.get("pastMembers") != null){
 				String test2 = query.get("name4").toString();
 				String test = null;
 				try {
 					test = URLEncoder.encode(test2, "UTF-8");
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				String pastMember = "<a href=\"artist?q=" + test + "\">" + test2 + "</a>";
 
 				if(!pastMembers.contains(pastMember)) {
 					pastMembers.add(pastMember);
 				}
 			}
 
 			if(query.get("bio") != null) {
 				artist.setBio(query.get("bio").toString());
 			}
 
 			if(query.get("wikipedia") != null) {
 				metaMap.put("Wikipedia", ("<a href=\"" + query.get("wikipedia").toString() + "\">" + query.get("wikipedia").toString() + "</a>"));
 			}
 
 			if(query.get("bbcpage") != null) {
 				metaMap.put("BBC Music", ("<a href=\"" + query.get("bbcpage").toString() + "\">" + query.get("bbcpage").toString() + "</a>"));
 			}
 
 			if(query.get("birthdate") != null) {
 				SimpleDateFormat format = new SimpleDateFormat("EEE dd. MMM yyyy",Locale.US);	
 				metaMap.put("Born", (format.format(makeDate(query.get("birthdate").toString()))));
 			}
 
 			if(query.get("homepage") != null) {
 				metaMap.put("Homepage", ("<a href=\"" + query.get("homepage").toString() + "\">" + query.get("homepage").toString() + "</a>"));
 			}
 
 			if(query.get("imdb") != null) {
 				metaMap.put("IMDB", ("<a href=\"" + query.get("imdb").toString() + "\">" + query.get("imdb").toString() + "</a>"));
 			}
 
 			if(query.get("myspace") != null) {
 				metaMap.put("MySpace", ("<a href=\"" + query.get("myspace").toString() + "\">" + query.get("myspace").toString() + "</a>"));
 			}
 
 			if(query.get("shortDesc") != null) {
 				artist.setShortDescription(query.get("shortDesc").toString());
 			}
 
 			if(query.get("birthname") != null) {
 				metaMap.put("Name", (query.get("birthname").toString()));
 			}
 			
 			
 			if(query.get("deathdate") != null) {
 SimpleDateFormat format = new SimpleDateFormat("EEE dd. MMM yyyy",Locale.US);	
 					
 				metaMap.put("Died", (format.format(makeDate(query.get("deathdate").toString()))));
 				
 			}
 
 			if(query.get("origin") != null) {
 				metaMap.put("From", (query.get("origin").toString()));
 			}
 
 			if(query.get("hometown") != null) {
 				metaMap.put("Living", (query.get("hometown").toString()));
 			}
 			if(query.get("start") != null) {
 			SimpleDateFormat format = new SimpleDateFormat("yyyy",Locale.US);	
 				
 				String activityStart = format.format(makeDate(query.get("start").toString()));
 				if(query.get("end") != null) {
 					
 					
 					activityStart += "-" + format.format(makeDate(query.get("end").toString()));
 					
 				}
 				metaMap.put("Active",activityStart);
 			}
 			
 		}
 
 		if(!fanpages.isEmpty()) {
 			metaMap.put("Fanpages", fanpages.toString());
 		}
 		if(!bands.isEmpty()) {
 			metaMap.put("Bandmembers", bands);
 		}
 		if(!formerBands.isEmpty()) {
 			metaMap.put("Former bandmembers", formerBands);
 		}
 		if(!currentMembers.isEmpty()) {
 			metaMap.put("Member of", currentMembers);
 		}
 		if(!pastMembers.isEmpty()) {
 			metaMap.put("Past member of", pastMembers);
 		}
 		artist.setMeta(metaMap);
 		LOGGER.debug("Found " + artist.getMeta().size() + " fun facts.");
 	}
 
 	public Event searchEvent(String search_string) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Record searchRecord(String record_name, String artist_name) throws MasterNotFoundException {
 		this.record = modelFactory.createRecord();
 		String release = discog.getRecordReleaseId(record_name, artist_name);
 		this.model = builder.createRecordOntology(release, record_name, artist_name);
 		try {	
 	    setRecordInfo(release);
 		} catch (MasterNotFoundException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		}
 		return record;
 	}
 	
 	public Track searchTrack(String search_string) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public void setRecordInfo(String search_string) throws MasterNotFoundException, TransformerException{
 		
 		
 		
 		String release = "<http://api.discogs.com/release/" + search_string + ">";
 		LOGGER.debug("This is the search_string "+ release);
 		String albumStr =  "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
 				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
 				"PREFIX mo: <http://purl.org/ontology/mo/> " +
 				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
 				"PREFIX dc: <http://purl.org/dc/terms/> " +
 				"PREFIX time: <http://www.w3.org/2006/time#> " + 
 				"SELECT DISTINCT * WHERE { " +
 				"?discogs mo:discogs ?id." +
 				"OPTIONAL { ?discogs foaf:name ?name.}" +
 				"OPTIONAL { ?discogs rdfs:comment ?comment. }" +
 				"OPTIONAL { ?discogs foaf:hasAgent ?artist. }" +
 				"OPTIONAL { ?discogs mo:genre ?genre. }" +
 				"OPTIONAL { ?discogs mo:catalogue_number ?catalogueNumber. }" +
 				"OPTIONAL { ?discogs mo:label ?publisher . }" +
				"OPTIONAL { " + release + " mo:image ?image . }" +
 				"OPTIONAL { ?discogs dc:issued ?year. }" +
 				"OPTIONAL { ?trackid foaf:name ?trackName. }" +
 				"OPTIONAL { ?trackid mo:track_number ?trackNumber. }" +
 				"OPTIONAL { ?trackid mo:preview ?preview. } " +
 				"OPTIONAL { ?trackid time:duration ?trackDuration. }" +
 				"}";
 		QueryExecution execution = QueryExecutionFactory.create(albumStr, model);
 		ResultSet albumResults = execution.execSelect();
 		
 		HashMap<String,String> genre = new HashMap<String,String>();
 		HashMap<String,Object> meta = new HashMap<String,Object>();
 		HashMap<String, Track> tracks = new HashMap<String, Track>();
 		HashMap<String, Artist> artists = new HashMap<String, Artist>();
 		
 		while(albumResults.hasNext()){
 			QuerySolution queryAlbum = albumResults.next();
 //			LOGGER.debug(queryAlbum.get("label").toString());
 //			LOGGER.debug(queryAlbum.get("comment").toString());
 //			LOGGER.debug(queryAlbum.get("genre").toString());
 //			LOGGER.debug(queryAlbum.get("year").toString());
 			
 			record.setId(release);
 			
 			if(queryAlbum.get("name") != null)
 			record.setName(queryAlbum.get("name").toString());
 			
 			if(queryAlbum.get("image") != null)
 			record.setImage(queryAlbum.get("image").toString());
 			
 			Track track = modelFactory.createTrack();
 			if(queryAlbum.get("trackName") != null) {
 				track.setName(queryAlbum.get("trackName").toString());
 			}
 			
 			if(queryAlbum.get("trackNumber") != null)
 			track.setTrackNr(queryAlbum.get("trackNumber").toString());
 			
 			if(queryAlbum.get("preview") != null)
 			track.setPreview(queryAlbum.get("preview").toString());
 			
 			if(queryAlbum.get("trackDuration") != null) {
 				track.setLength(queryAlbum.get("trackDuration").toString());
 			}
 			
 			if(queryAlbum.get("trackNumber") != null)
 			tracks.put(queryAlbum.get("trackNumber").toString(), track);
 			
 			Artist artist = modelFactory.createArtist();
 			
 			if(queryAlbum.get("artist") != null)
 			artist.setName(queryAlbum.get("artist").toString());
 			
 			if(queryAlbum.get("artist") != null)
 			artists.put(queryAlbum.get("artist").toString(), artist);
 			
 			if(queryAlbum.get("genre") != null)
 			genre.put(queryAlbum.get("genre").toString(),queryAlbum.get("genre").toString());
 			
 			if(queryAlbum.get("year") != null)
 			meta.put("Released", queryAlbum.get("year").toString());
 			
 			if(queryAlbum.get("catalogueNumber").toString() != "none") {
 				meta.put("Catalogue Number", queryAlbum.get("catalogueNumber").toString());
 			}
 			
 			if(queryAlbum.get("publisher") != null)
 			meta.put("Label", queryAlbum.get("publisher").toString());
 			
 			if(queryAlbum.get("year") != null)
 			record.setYear(queryAlbum.get("year").toString());
 			
 			if(queryAlbum.get("comment") != null)
 			record.setDescription(queryAlbum.get("comment").toString());
 		}
 		
 		record.setGenres(genre);
 		record.setTracks(new LinkedList<Track>(tracks.values()));
 		record.setArtist(new LinkedList<Artist>(artists.values()));
 		record.setMeta(meta);
 			
 	}
 		
 
 	public static void main(String[] args) throws MasterNotFoundException {		
 		ApplicationContext context = new  ClassPathXmlApplicationContext("main-context.xml");
 //		System.out.println(context);
 		Searcher searcher = (Searcher) context.getBean("searcherImpl");
 ////		searcher.searchArtist("Guns N Roses");
 //		Map<String, Record> records = searcher.searchRecords("Thriller");
 //		for(Record record:records.values()){
 //			System.out.println(record.getName()+", " + record.getDiscogId()+ ", " + record.getArtist().get(0).getName());
 //		}
 //		Searcher searcher = new SearcherImpl();
 		
 		
 		searcher.searchRecord("Rated R","Rihanna");
 		
 		
 	}
 	
 	private Date makeDate(String dateString){
 		Date date = new Date();
 		SimpleDateFormat stf = new SimpleDateFormat("yyyy-mm-dd",Locale.US);
 		try {
 			date = stf.parse(dateString);
 			
 			
 		} catch (ParseException e) {
 			LOGGER.error("Couldnt parse date");
 		}
 		return date;
 	}
 
 }
