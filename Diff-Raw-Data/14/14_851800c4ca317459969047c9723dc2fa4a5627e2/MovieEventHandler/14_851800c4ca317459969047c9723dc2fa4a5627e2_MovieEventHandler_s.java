 package androidnfc.movieapp;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.helpers.DefaultHandler;
 
 import androidnfc.movieapp.models.Movie;
 
 public class MovieEventHandler extends DefaultHandler {
 	
 	private List<Movie> movieList;
 	private Movie tempMovie;
 	private String tempValue;
 	
 	public List<Movie> getParsedMovies() {
 		return this.movieList;
 	}
 
 	@Override
 	public void startDocument() {
 		// Do nothing.
 	}
 	
 	@Override
 	public void endDocument() {
 		// Do nothing.
 	}
 	
 	@Override
 	public void startElement(String namespaceURI, String localName,
 							 String qName, Attributes attrs) {
 		
 		if (qName.equals("Events")) {
 			
 			this.movieList = new LinkedList<Movie>();
 			
 		} else if (qName.equals("Event")) {
 			
 			this.tempMovie = new Movie();
 			
 		}
 		
 	}
 	
 	@Override
 	public void characters(char[] ch, int start, int length) {
 		tempValue = new String(ch, start, length);
 	}
 	
 	@Override
 	public void endElement(String namespaceURI, String localName, String qName) {
 		
		if (qName.equals("Movie")) {
 			
 			this.movieList.add(tempMovie);
 			
 		} else if (qName.equals("Title")) {
 			
 			this.tempMovie.setTitle(tempValue);
 			
 		}
 		
 		
 	}
 	
 }
