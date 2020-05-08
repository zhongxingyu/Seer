 package cz.stoupa.showtimes.imports.mat.moviedetail;
 
 import com.google.inject.Inject;
 
 import cz.stoupa.showtimes.imports.StringMovieId;
 
 public class MatMovieDetailPageUrlGenerator {
 
 	public static final String QUERY_STRING_BASE = "?movie-id=";
 	
 	private final String movieDetailUrl;
 	
 	@Inject
	public MatMovieDetailPageUrlGenerator( String movieDetailUrl ) {
 		this.movieDetailUrl = movieDetailUrl;
 	}
 
 	public String prepareUrl( StringMovieId matMovieId ) {
 		return movieDetailUrl + QUERY_STRING_BASE + matMovieId;
 	}
 
 }
