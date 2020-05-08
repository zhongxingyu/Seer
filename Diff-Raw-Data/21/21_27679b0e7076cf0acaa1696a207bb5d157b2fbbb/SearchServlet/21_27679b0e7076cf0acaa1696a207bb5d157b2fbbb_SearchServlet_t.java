 package it.polimi.chansonnier.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.eclipse.smila.lucene.LuceneSearchService;
 import org.eclipse.smila.processing.ProcessingException;
 import org.eclipse.smila.processing.parameters.SearchParameters;
 import org.eclipse.smila.search.api.SearchResult;
 import org.eclipse.smila.blackboard.Blackboard;
 import org.eclipse.smila.blackboard.BlackboardAccessException;
 import org.eclipse.smila.blackboard.path.Path;
 import org.eclipse.smila.datamodel.id.Id;
 import org.eclipse.smila.datamodel.record.Annotation;
 import org.eclipse.smila.datamodel.record.Record;
 import org.eclipse.smila.datamodel.record.RecordFactory;;
 
 public class SearchServlet extends HttpServlet {
 	public static final String DEFAULT_PIPELINE = "SearchPipeline";
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response) 
 		throws ServletException, IOException {
 	    String lyrics = request.getParameter("lyrics");
 	    if (lyrics != null) {
 	    	Record queryRecord = RecordFactory.DEFAULT_INSTANCE.createRecord();
 	    	queryRecord.setMetadata(RecordFactory.DEFAULT_INSTANCE.createMetadataObject());
 	        final Annotation annotation = RecordFactory.DEFAULT_INSTANCE.createAnnotation();
 	        queryRecord.getMetadata().addAnnotation(SearchParameters.PARAMETERS, annotation);
 	        annotation.setNamedValue(SearchParameters.QUERY, lyrics);
 	        annotation.setNamedValue(LuceneSearchService.SEARCH_ANNOTATION_QUERY_ATTRIBUTE, "Lyrics");
 	    	try {
 				SearchResult result = Activator.getSearchService().search(DEFAULT_PIPELINE, queryRecord);
 				Blackboard blackboard = Activator.getBlackboardFactory().createPersistingBlackboard();
 				System.out.println(result.getRecords());
				if (result.getRecords() != null && result.getRecords().length > 0) {
 					for (Record r : result.getRecords()) {
 						Id id = r.getId();
                         String link = id.toString();
 						String title = blackboard.getLiteral(id, new Path("Title")).toString();
 						String artist = blackboard.getLiteral(id, new Path("Artist")).toString();
 						String fullLyrics = blackboard.getLiteral(id, new Path("Lyrics")).toString();
 						response.getWriter().write("<p>Song found: " + title + " (artist: " + artist + ", lyrics: " + fullLyrics + ")<br /><a href=\"" + link + "\">" + link + "</a></p>");
 					}
 				} else {
 					response.getWriter().write("No results.");
 				}
 		        response.setContentType("text/html;charset=UTF-8");
 			} catch (ProcessingException e) {
 				response.getWriter().write(e.getMessage());
 			} catch (BlackboardAccessException e) {
 				response.getWriter().write(e.getMessage());
 			}
 	    }
 	}
 }
