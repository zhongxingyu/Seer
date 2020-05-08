 /*
  *      Copyright (c) 2004-2011 YAMJ Members
  *      http://code.google.com/p/moviejukebox/people/list 
  *  
  *      Web: http://code.google.com/p/moviejukebox/
  *  
  *      This software is licensed under a Creative Commons License
  *      See this page: http://code.google.com/p/moviejukebox/wiki/License
  *  
  *      For any reuse or distribution, you must make clear to others the 
  *      license terms of this work.  
  */
 package com.moviejukebox.rottentomatoes.tools;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.moviejukebox.rottentomatoes.model.Cast;
 import com.moviejukebox.rottentomatoes.model.Link;
 import com.moviejukebox.rottentomatoes.model.Movie;
 import com.moviejukebox.rottentomatoes.model.ReleaseDate;
 import com.moviejukebox.rottentomatoes.model.Review;
 
 public class RTParser {
     private static Logger logger = Logger.getLogger("rottentomatoes");
 
     private final static String CAST_ABRIDGED = "abridged_cast";
     private final static String CAST_FULL = "cast";
     private final static String CHARACTERS = "characters";
     private final static String DIRECTORS_ABDRIDGED = "abridged_directors";
     private final static String GENRES = "genres";
     private final static String ID = "id";
     private final static String LINKS = "links";
     private final static String MOVIES = "movies";
     private final static String MPAA_RATING = "mpaa_rating";
     private final static String NAME = "name";
     private final static String POSTERS = "posters";
     private final static String RATINGS = "ratings"; 
     private final static String RELEASE_DATES = "release_dates";
     private final static String RUNTIME = "runtime"; 
     private final static String SYNOPSIS = "synopsis"; 
     private final static String TITLE = "title";
     private final static String YEAR = "year";
     private final static String CRITIC = "critic";
     private final static String DATE = "date";
     private final static String PUBLICATION = "publication";
     private final static String QUOTE = "quote";
     private final static String REVIEWS = "reviews";
     
     /**
      * Get multiple movies and process them
      * @param searchUrl
      * @return
      */
     public static HashSet<Movie> getMovies(String searchUrl) {
        JSONObject movieObject = new JSONObject();
         HashSet<Movie> movies = new HashSet<Movie>();
         
         try {
             String response = WebBrowser.request(searchUrl);
             movieObject = new JSONObject(response);
         } catch (IOException e) {
             e.printStackTrace();
             return movies;
         } catch (JSONException e) {
             e.printStackTrace();
             return movies;
         }
 
         try {
             if (movieObject.length() > 0) {
                 JSONArray jsonMovies = movieObject.getJSONArray(MOVIES);
                 
                 for (int loop = 0 ; loop < jsonMovies.length() ; loop++) {
                    Movie movie = parseMovie(jsonMovies.getJSONObject(loop));
                    movies.add(movie);
                 }
                 
                 return movies;
             } else {
                 return movies;
             }
         } catch (JSONException e) {
             throw new RuntimeException("RottenTomatoesAPI: Error getting list of movies - " + e.getMessage());
         }
     }
     
     /**
      * Process a single movie
      * @param searchUrl
      * @return
      */
     public static Movie getSingleMovie(String searchUrl) {
        JSONObject movieObject = new JSONObject();
        Movie movie = new Movie();
         
         try {
             String response = WebBrowser.request(searchUrl);
             movieObject = new JSONObject(response);
             
             movie = parseMovie(movieObject);
             
             return movie;
         } catch (IOException e) {
             throw new RuntimeException("RottenTomatoesAPI: Incorrect Movie ID - " + e.getMessage());
         } catch (JSONException e) {
             throw new RuntimeException("RottenTomatoesAPI: Incorrect Movie ID - " + e.getMessage());
         }
 }
 
     public static HashSet<Cast> getCastList(String searchUrl) {
        JSONObject castObject = new JSONObject();
         HashSet<Cast> castList = new HashSet<Cast>();
         
         try {
             String response = WebBrowser.request(searchUrl);
             castObject = new JSONObject(response);
             castList = parseCast(castObject, CAST_FULL);    
             return castList;
         } catch (IOException e) {
             e.printStackTrace();
         } catch (JSONException e) {
             e.printStackTrace();
         }
         
         return castList;
     }
     
     /**
      * Parse the movie JSON object for Cast information
      * @param jMovie
      * @return
      */
     private static HashSet<Cast> parseCast(JSONObject jMovie, String castType) {
         HashSet<Cast> response = new HashSet<Cast>();
         
         try {
             JSONArray jsonCast = jMovie.getJSONArray(castType);
             
             for (int loop = 0 ; loop < jsonCast.length() ; ++loop) {
                 JSONObject jCast = jsonCast.getJSONObject(loop);
                 Cast cast = new Cast();
                 cast.setCastName(readString(jCast, NAME));
     
                 // Sometimes the cast member doesn't have a character name.
                 if (jCast.length() > 1) {
                     JSONArray charList = jCast.getJSONArray(CHARACTERS);
                     for (int loop2 = 0; loop2 < charList.length(); loop2++) {
                         cast.addCharacter(charList.getString(loop2));
                     }
                 }
                 
                 response.add(cast);
             }
         } catch (JSONException e) {
             throw new RuntimeException("RottenTomatoesAPI: Error parsing cast - " + e.getMessage());
         }
         
         return response;
     }
 
     /**
      * Parse the movie JSON object for the director names
      * @param jMovie
      * @param directorsAbdridged 
      * @return
      */
     private static HashSet<String> parseDirectors(JSONObject jMovie, String directorType) {
         HashSet<String> response = new HashSet<String>();
         
         JSONArray jsonCast;
         try {
             jsonCast = jMovie.getJSONArray(directorType);
             for (int loop = 0 ; loop < jsonCast.length() ; ++loop) {
                 JSONObject jCast = jsonCast.getJSONObject(loop);
                 response.add(readString(jCast, NAME));
             }
         } catch (JSONException e) {
             // The GENRES weren't found, we can safely ignore this error
             // throw new RuntimeException("RottenTomatoesAPI: Error parsing directors - " + e.getMessage());
         }
         
         return response;
     }
     
     /**
      * Parse the movie JSON object for a link object
      * @param jMovie
      * @param linkType
      * @return
      */
     public static HashSet<Link> parseGenericLinks(JSONObject jMovie, String linkType) {
         HashSet<Link> response = new HashSet<Link>();
 
         JSONObject jObject;
         try {
             jObject = jMovie.getJSONObject(linkType);
         } catch (JSONException e) {
             logger.fine("No " + linkType + " in the object");
             return null;
         }
 
         @SuppressWarnings("unchecked")
         Iterator<String> linkList = jObject.keys();
         
         while (linkList.hasNext()) {
             String type = (String)linkList.next();
             
             Link link = new Link();
             link.setLinkType(type);
             link.setLinkUrl(readString(jObject, type));
             
             response.add(link);
         }
         
         return response;
     }
     
     private static HashSet<String> parseGenres(JSONObject jMovie) {
         HashSet<String> response = new HashSet<String>();
         
         try {
             JSONArray jGenres = jMovie.getJSONArray(GENRES);
             
             if (jGenres.length() > 1) {
                 for (int loop = 0; loop < jGenres.length(); loop++) {
                     response.add(jGenres.getString(loop));
                 }
             }
         } catch (JSONException e) {
             // The GENRES weren't found, we can safely ignore this error
             // throw new RuntimeException("RottenTomatoesAPI: Error parsing genres - " + e.getMessage());
         }
         
         return response;
 }
     
     /**
      * Retrieve the link list from the URL
      * @param linkSearchUrl
      * @param linkType
      * @return
      */
     public static HashSet<Link> parseLinks(String linkSearchUrl, String linkType) {
         JSONObject objMaster;
        HashSet<Link> response = new HashSet<Link>();
         
         try {
             objMaster = new JSONObject(WebBrowser.request(linkSearchUrl));
             response = RTParser.parseGenericLinks(objMaster, linkType);
         } catch (JSONException e) {
             throw new RuntimeException("RottenTomatoesAPI: Error parsing links - " + e.getMessage());
         } catch (IOException e) {
             throw new RuntimeException("RottenTomatoesAPI: Error parsing links - " + e.getMessage());
         }
         
         return response;
     }
     
     /**
      * Parse a JSON object for all the movie information
      * @param jMovie
      * @return
      */
     public static Movie parseMovie(JSONObject jMovie) {
         Movie movie = new Movie();
 
         movie.setId(readInt(jMovie, ID));
         movie.setTitle(readString(jMovie, TITLE));
         movie.setYear(readInt(jMovie, YEAR));
         movie.setGenres(parseGenres(jMovie));
         movie.setCertification(readString(jMovie, MPAA_RATING));
         movie.setRuntime(readInt(jMovie, RUNTIME));
         movie.setReleaseDates(parseReleaseDates(jMovie));
         movie.setRatings(parseRatings(jMovie));
         movie.setSynopsis(readString(jMovie, SYNOPSIS));
         movie.setArtwork(parseGenericLinks(jMovie, POSTERS));
         movie.setCast(parseCast(jMovie, CAST_ABRIDGED));
         movie.setDirectors(parseDirectors(jMovie, DIRECTORS_ABDRIDGED));
         movie.setLinks(parseGenericLinks(jMovie, LINKS));
         return movie;
     }
     
     /**
      * Parse the movie JSON object for the ratings.
      * The whole object is passed in so we can trap the JSON Exceptions
      * @param jMovie
      * @return
      */
     private static HashMap<String, Integer> parseRatings(JSONObject jMovie) {
         HashMap<String, Integer> response = new HashMap<String, Integer>();
 
         JSONObject jObject;
         try {
             jObject = jMovie.getJSONObject(RATINGS);
         } catch (JSONException e) {
             logger.fine("No ratings for the movie");
             return null;
         }
         
         @SuppressWarnings("unchecked")
         Iterator<String> ratingList = jObject.keys();
         
         String type = null;
         int score = 0;
         while (ratingList.hasNext()) {
             type = (String)ratingList.next();
             score =  readInt(jObject, type);
             response.put(type, score);
         }
         
         return response;
     }
     
     /**
      * Parse the movie JSON object for the release dates.
      * The whole object is passed in so we can trap the JSON Exceptions
      * @param jMovie
      * @return
      */
     private static HashSet<ReleaseDate> parseReleaseDates(JSONObject jMovie) {
         HashSet<ReleaseDate> response = new HashSet<ReleaseDate>();
 
         JSONObject jObject;
         try {
             jObject = jMovie.getJSONObject(RELEASE_DATES);
         } catch (JSONException e) {
             logger.fine("No release dates for the movie");
             return null;
         }
         
         @SuppressWarnings("unchecked")
         Iterator<String> releaseDateList = jObject.keys();
         
         while (releaseDateList.hasNext()) {
             String type = (String)releaseDateList.next();
             
             ReleaseDate rd = new ReleaseDate();
             rd.setReleaseType(type);
             rd.setReleaseDate(readString(jObject, type));
             
             response.add(rd);
         }
         
         return response;
     }
 
     /**
      * Get an integer from a JSON Object
      * @param jObject
      * @param item
      * @return
      */
     private static int readInt(JSONObject jObject, String item) {
         int response = 0;
         
         try {
             response = jObject.getInt(item);
             return response;
         } catch (JSONException ignore) {
             // We can't read this as an integer, so it's probably null or an empty string
             return 0;
         }
     }
     
     /**
      * Get a string from a JSON object
      * @param jObject
      * @param item
      * @return
      */
     private static String readString(JSONObject jObject, String item) {
         String response = "";
         
         if (jObject == null) {
             return response;
         }
         
         try {
             response = jObject.getString(item);
         } catch (JSONException error) {
             // The item wasn't found.
         }
         
         return response;
     }
 
     public static HashSet<Review> getReviews(String searchUrl) {
        JSONObject reviewListObject = new JSONObject();
         HashSet<Review> reviewList = new HashSet<Review>();
         
         try {
             reviewListObject = new JSONObject(WebBrowser.request(searchUrl));
             
             JSONArray array = reviewListObject.getJSONArray(REVIEWS);
 
             for (int loop = 0; loop < array.length(); loop++) {
                 reviewList.add(parseReview(array.getJSONObject(loop)));
             }
             
             return reviewList;
         } catch (IOException e) {
             throw new RuntimeException("RottenTomatoesAPI: Error getting reviews - " + e.getMessage());
         } catch (JSONException e) {
             throw new RuntimeException("RottenTomatoesAPI: Error getting reviews - " + e.getMessage());
         }
     }
     
     /**
      * Parse an individual review
      * @param jReview
      * @return
      */
     private static Review parseReview(JSONObject jReview) {
         Review review = new Review();
         
         review.setCritic(readString(jReview, CRITIC));
         review.setReviewDate(readString(jReview, DATE));
         review.setPublication(readString(jReview, PUBLICATION));
         review.setQuote(readString(jReview, QUOTE));
         review.setLinks(parseGenericLinks(jReview, LINKS));
         
         return review;
     }
     
 }
