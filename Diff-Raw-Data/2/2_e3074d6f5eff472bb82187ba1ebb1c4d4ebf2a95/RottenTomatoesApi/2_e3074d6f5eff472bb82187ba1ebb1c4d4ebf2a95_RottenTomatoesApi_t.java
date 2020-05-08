 package com.criticcomrade.api.main;
 
 import java.io.IOException;
 import java.util.*;
 
 import com.criticcomrade.api.data.*;
 import com.google.gson.*;
 
 public class RottenTomatoesApi {
     
     private static final String URL_SEARCH_MOVIES = "http://api.rottentomatoes.com/api/public/v1.0/movies.json";
     private static final String URL_MOVIE = "http://api.rottentomatoes.com/api/public/v1.0/movies/<movie_id>.json";
     private static final String URL_MOVIE_REVIEWS = "http://api.rottentomatoes.com/api/public/v1.0/movies/<movie_id>/reviews.json";
     private static final String URL_MOVIE_BOX_OFFICE = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json";
     private static final String URL_MOVIE_IN_THEATERS = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/opening.json";
     private static final String URL_MOVIE_OPENING = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/upcoming.json";
     private static final String URL_MOVIE_UPCOMING = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/upcoming.json";
     
     public static final int PAGE_LIMIT = 50;
     
     /**
      * Example of querrying the Rotten Tomatoes public API.
      * 
      * @param args
      * @throws IOException
      */
     public static void main(String[] args) throws IOException {
 	
 	RottenTomatoesApi api = new RottenTomatoesApi();
 	
 	List<MovieShort> searchResults = api.searchMovies("dark knight");
 	
 	for (MovieShort ms : searchResults) {
 	    
 	    Movie m = api.getMovie(ms);
 	    
 	    System.out.println(String.format("%s (%s)", m.title, m.year));
 	    
 	    List<Review> reviews = api.getReviews(m);
 	    for (Review r : reviews) {
 		
 		System.out.println("\t" + r.critic + " at " + r.publication + " = " + r.original_score);
 		
 	    }
 	    
 	    System.out.println("\n");
 	}
 	
     }
     
     public List<MovieShort> searchMovies(String title) throws IOException {
 	
 	String url = URL_SEARCH_MOVIES;
 	
 	Map<String, String> params = new HashMap<String, String>();
 	params.put("q", title);
 	params.put("page_limit", String.format("%d", PAGE_LIMIT));
 	params.put("page", String.format("%d", 1));
 	
 	MovieSearchResults fullRet = (new Gson()).fromJson(WebCaller.doApiCall(url, params), MovieSearchResults.class);
 	
 	for (int page = 2; page * PAGE_LIMIT < fullRet.total; page++) {
 	    params.put("page", String.format("%d", page));
 	    MovieSearchResults pageResults = (new Gson()).fromJson(WebCaller.doApiCall(url, params), MovieSearchResults.class);
 	    fullRet.movies.addAll(pageResults.movies);
 	}
 	
 	return new ArrayList<MovieShort>(fullRet.movies);
 	
     }
     
     public Movie getMovie(MovieShort ms) throws JsonSyntaxException, IOException {
 	return getMovie(ms.id);
     }
     
     public Movie getMovie(String id) throws JsonSyntaxException, IOException {
 	
 	String url = URL_MOVIE.replaceAll("<movie_id>", id);
 	
 	Movie ret = (new Gson()).fromJson(WebCaller.doApiCall(url, new HashMap<String, String>()), Movie.class);
 	
 	return ret;
 	
     }
     
     public List<Review> getReviews(Movie m) throws JsonSyntaxException, IOException {
 	
 	String url = URL_MOVIE_REVIEWS.replace("<movie_id>", m.id);
 	
 	Map<String, String> params = new HashMap<String, String>();
 	params.put("page_limit", String.format("%d", PAGE_LIMIT));
 	params.put("page", String.format("%d", 1));
 	params.put("review_type", "all");
 	
 	ReviewList fullRet = (new Gson()).fromJson(WebCaller.doApiCall(url, params), ReviewList.class);
 	
	for (int page = 2; (page - 1) * PAGE_LIMIT < fullRet.total; page++) {
 	    params.put("page", String.format("%d", page));
 	    ReviewList pageResults = (new Gson()).fromJson(WebCaller.doApiCall(url, params), ReviewList.class);
 	    fullRet.reviews.addAll(pageResults.reviews);
 	}
 	
 	return new ArrayList<Review>(fullRet.reviews);
 	
     }
     
     public List<MovieShort> getBoxOfficeMovies() throws JsonSyntaxException, IOException {
 	String url = URL_MOVIE_BOX_OFFICE;
 	MovieSearchResults ret = (new Gson()).fromJson(WebCaller.doApiCall(url, new HashMap<String, String>()), MovieSearchResults.class);
 	return new ArrayList<MovieShort>(ret.movies);
     }
     
     public List<MovieShort> getInTheatersMovies() throws JsonSyntaxException, IOException {
 	String url = URL_MOVIE_IN_THEATERS;
 	MovieSearchResults ret = (new Gson()).fromJson(WebCaller.doApiCall(url, new HashMap<String, String>()), MovieSearchResults.class);
 	return new ArrayList<MovieShort>(ret.movies);
     }
     
     public List<MovieShort> getOpeningMovies() throws JsonSyntaxException, IOException {
 	String url = URL_MOVIE_OPENING;
 	MovieSearchResults ret = (new Gson()).fromJson(WebCaller.doApiCall(url, new HashMap<String, String>()), MovieSearchResults.class);
 	return new ArrayList<MovieShort>(ret.movies);
     }
     
     public List<MovieShort> getUpcomingMovies() throws JsonSyntaxException, IOException {
 	String url = URL_MOVIE_UPCOMING;
 	MovieSearchResults ret = (new Gson()).fromJson(WebCaller.doApiCall(url, new HashMap<String, String>()), MovieSearchResults.class);
 	return new ArrayList<MovieShort>(ret.movies);
     }
     
 }
