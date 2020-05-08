 package com.marklogic.training;
 
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 
 import com.marklogic.client.document.BinaryDocumentManager;
 import com.marklogic.client.document.XMLDocumentManager;
 import com.marklogic.client.io.DOMHandle;
 import com.marklogic.client.io.InputStreamHandle;
 import com.marklogic.client.io.SearchHandle;
 import com.marklogic.client.query.MatchDocumentSummary;
 import com.marklogic.client.query.MatchLocation;
 import com.marklogic.client.query.MatchSnippet;
 import com.marklogic.client.query.QueryManager;
 import com.marklogic.client.query.StringQueryDefinition;
 
 /*
  * this class encapsulates the MarkLogic Search API
  */
 public class Search {
 
 	private static final String OPTIONS_NAME = "full-options";
 	private static final Logger logger = LoggerFactory.getLogger(Search.class);
 	private static MarkLogicConnection conn = null;
 	
 	public Search() {
 		logger.info("initialising MarkLogic Search...");
 		conn = MarkLogicConnection.getInstance();
 		
 	}
 	/*
 	 * main search function
 	 * @arg is the search argument passed from the browser
 	 * 
 	 * 1. set the search options
 	 * 2. execute the search
 	 * 3. parse the results into a tree
 	 * 4. create a POJO with the results for processing in the view.
 	 */
 	public Song[] search(String arg) {
 		
 		logger.info("Performing search() with arg = "+(arg == ""?" EMPTY_STRING":arg) );
 		
 		
 		// create a manager for searching
 		QueryManager queryMgr = conn.getClient().newQueryManager();
 
 		// create a search definition
 		StringQueryDefinition querydef = queryMgr.newStringDefinition(OPTIONS_NAME);
 		
 		// set the search argument
 		querydef.setCriteria(arg);
 
 		// create a handle for the search results
 		SearchHandle resultsHandle = new SearchHandle();
 
 		// set the page length
 		queryMgr.setPageLength(10);
 		// run the search
 		queryMgr.search(querydef, resultsHandle);
 		
 		logger.info("Matched "+resultsHandle.getTotalResults()+
 				" documents with '"+querydef.getCriteria()+"'\n");
 
 		// iterate over the result documents
 		MatchDocumentSummary[] docSummaries = resultsHandle.getMatchResults();
 		logger.info("Listing "+docSummaries.length+" documents:\n");
 		Song[] songs = new Song[docSummaries.length];
 		int i = 0;
 		for (MatchDocumentSummary docSummary: docSummaries) {
 			
 			MatchLocation[] matches = docSummary.getMatchLocations();
 			
 			
 			List<Snippet> snippetList = new ArrayList<Snippet>();
 			
 			for (MatchLocation match: matches) {
 				
 				MatchSnippet[] snippets = match.getSnippets();
 				
 				for (MatchSnippet snippet: snippets) {
 					
 					snippetList.add(new Snippet(snippet.getText(), snippet.isHighlighted()) );
 					
 					logger.info(" snippet text is "+(snippet.isHighlighted()?" highlighted ":" NOT highlighted ")+" text value: "+snippet.getText());
 				}
 				
 			}
 			
 			logger.info("gathered number of snippets = "+snippetList.size());
			Object[] objs = snippetList.toArray();
			Snippet[] snips = new Snippet[objs.length];
			for (int k=0; k<objs.length; k++) {
				if (objs[k] instanceof Snippet ) {
					snips[k] = (Snippet) objs[k];
				} else {
					logger.error("cast from Object to Snippet not fucking worky");
				}
			}

 			
 			// read constituent documents
 			// read the document from the db and pass back the DOM for that doc.
 			Document doc = getDOMDocument(docSummary.getUri());
 
 			Song song = buildSong(docSummary.getUri(), doc );
 			logger.info("about to set snippets in song ");
 
 			song.setSnippets(snips);
 			songs[i] = song;
 			i++;
 		}
 
 		return songs;
 		
 	}
 	/*
 	 * retrieve the details for one song
 	 */
 	public Song getSongDetails(String uri) {
 		return buildSongFullDetails(uri);
 	}
 	/*
 	 * serve images from MarkLogic
 	 */
 	public InputStream serveImage(String uri) {
 		
 		// Read Document into an InputStream
 		// create  handle
 		InputStreamHandle handle = new InputStreamHandle();  
 
 		try {				
 			// create a manager for binary documents
 			BinaryDocumentManager docMgr = conn.getClient().newBinaryDocumentManager();			
 			
 			// read document
 			docMgr.read(uri,handle);
 			// output the string
 			logger.info("loaded binary document from db  - mimetype" + handle.getMimetype() );
 				  
 		} catch (Exception e) {
 			logger.error("Exception : " + e.toString() );
 		} 
 		return  handle.get();
 	}
 	public void stop() {
 		logger.info("releasing MarkLogic Search...");
 		if (conn != null) {
 			conn.release();			
 		}
 	}  
 	private Song buildSongFullDetails(String uri) {
 
 		// read the document from the db and pass back the DOM for that doc.
 		Document doc = getDOMDocument(uri);
 		
 		// build the display details for a song
 		Song song = buildSong(uri,doc);
 		
 		song.setAlbum(SongHelper.getTagValue("album", doc));
 		logger.debug(" Song album " + song.getAlbum() );
 		
 		song.setLabel(SongHelper.getTagValue("label", doc));
 		logger.debug(" Song label " + song.getLabel() );
 		
 		song.setWriters(SongHelper.get2ndLevelChildren("writers", doc));
 		logger.debug(" Song writers " + song.getWriters() );
 		
 		song.setProducers(SongHelper.get2ndLevelChildren("producers", doc));
 		logger.debug(" Song producers " + song.getProducers() );
 		
 		song.setFormats(SongHelper.get2ndLevelChildren("formats", doc));
 		logger.debug(" Song formats " + song.getFormats() );
 		
 		song.setLengths(SongHelper.get2ndLevelChildren("lengths", doc));
 		logger.debug(" Song lengths " + song.getLengths() );
 		
 		song.setDescription(SongHelper.getSongDescription(doc,99999)); 
 		logger.debug(" Song description " + song.getDescription() );
 
 		song.setWeeks(SongHelper.get2ndLevelChildren("weeks", doc));
 		logger.debug(" Song actual weeks at #1 " + song.getWeeks() );
 		
 		song.setAlbumimage(SongHelper.getAlbumURI(doc));
 		logger.debug(" Song album image uri " + song.getAlbumimage() );
 		
 		return song;
 	}
 	private Document getDOMDocument(String uri) {
 		// set up the read
 		XMLDocumentManager docMgr = conn.getClient().newXMLDocumentManager();
 		// create the handle
 		DOMHandle readHandle = new DOMHandle();
 		// read the document into the handle
 		logger.debug("About to read document "+uri);
 		docMgr.read(uri, readHandle);
 		// access and return the document content
 		return readHandle.get();
 		
 	}
 	private Song buildSong(String uri, Document doc) {
 		// create the POJO
 		Song song = new Song();
 		
 		song.setTitle(SongHelper.getTagValue("title", doc));
 		logger.debug("song title is " + song.getTitle() );
 
 		song.setArtist(SongHelper.getTagValue("artist", doc));
 		logger.debug("song artist is " + song.getArtist() );
 
 		song.setUri(uri);
 		logger.debug("song uri is " + song.getPlainTextUri() );
 		
 		song.setGenres(SongHelper.getGenres(doc));
 		logger.debug("song genres is/are " + song.getGenres() );
 		 
 		song.setWeekending(SongHelper.getWeekLastAttr(doc));
 		logger.debug("song last week in charts was " + song.getWeekending() );
 
 		song.setTotalweeks(SongHelper.getNumberOfWeeks(doc));
 		logger.debug(" number of weeks at #1 was " + song.getTotalweeks() );
 		
 		song.setDescription(SongHelper.getSongDescription(doc,40)); 
 		logger.debug(" Song description " + song.getDescription() );
 
 		return song; 
 	}
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
