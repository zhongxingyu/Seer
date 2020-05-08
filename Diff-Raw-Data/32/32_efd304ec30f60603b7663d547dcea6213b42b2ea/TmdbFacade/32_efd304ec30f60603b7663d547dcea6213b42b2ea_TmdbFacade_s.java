 package amo.randomFilm.datasource.tmdb;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.Priority;
 
 import sun.net.www.protocol.http.HttpURLConnection;
 import amo.randomFilm.configuration.Configuration;
 import amo.randomFilm.datasource.exception.MovieDataProviderException;
 import amo.randomFilm.datasource.tmdb.data.TmdbImage;
 import amo.randomFilm.datasource.tmdb.data.TmdbMovie;
 import amo.randomFilm.datasource.tmdb.data.TmdbMovieExtendedInfo;
 import amo.randomFilm.model.MovieDataProvider;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 
 /**
  * Offers Convenient Access to the TMDB Database.
  * 
  * @author Andreas Monger (andreas.monger@gmail.com)
  */
 public class TmdbFacade implements MovieDataProvider {
     
     /** the base URL for Requests to TMDB */
     private static final String BASE_URL = Configuration.getInstance().getProperty("tmdb.baseurl");
     
     /** the current API-KEY to TMDB */
     private static final String API_KEY = Configuration.getInstance().getProperty("tmdb.apikey");
     
     private static final String METHOD_GETAUTH = BASE_URL + "Auth.getToken/json/" + API_KEY;
     
     private static final String METHOD_SEARCH = BASE_URL + "Movie.search/de/json/" + API_KEY + "/";
     
     private static final String METHOD_INFO = BASE_URL + "Movie.getInfo/de/json/" + API_KEY + "/";
     
     private static final String METHOD_IMAGES = BASE_URL + "Movie.getImages/de/json/" + API_KEY + "/";
     
     /** Reference to this Object Instance */
     private static TmdbFacade instance = null;
     
     /** Logger Object for this Class */
     private static final Logger logger = Logger.getLogger(TmdbFacade.class);
     
     /** This System's file Encoding */
     private static final String encoding = System.getProperty("file.encoding");
     
     /**
      * Hidden Constructor
      */
     private TmdbFacade() {
     }
     
     /**
      * @return Reference to the unique {@link TmdbFacade} instance.
      */
     public static TmdbFacade getInstance() {
         if (instance == null) {
             instance = new TmdbFacade();
         }
         return instance;
     }
     
     /**
      * Fetches a Authorization Token from TMDB for write Access. A Token lasts for 1 hour!
      * 
      * @throws MovieDataProviderException
      *             in case the request was not successful.
      */
     private void getAuthToken() throws MovieDataProviderException {
         doRequest(METHOD_GETAUTH);
     }
     
     /**
      * Returns a list of movies that match the given title or <code>null</code>, of nothing could be
      * found.
      * 
      * @param title
      *            the title to look for
      * @return A List of movies that match the given title.
      * @throws MovieDataProviderException
      *             in case something unexpected happened
      */
     @Override
     public List<TmdbMovie> searchMovie(String title) throws MovieDataProviderException {
         String urlEncodedTitle = urlEncodeParameter(title);
         
         String jsonResult = doRequest(METHOD_SEARCH + urlEncodedTitle);
         logger.log(Level.DEBUG, "Got Search-Result: " + jsonResult);
         
         if (jsonResult.equals("[\"Nothing found.\"]")) {
             throw new MovieDataProviderException("Nothing Found.");
         }
         
         List<TmdbMovie> movies = extractMoviesFromJson(jsonResult);
         
         return movies;
     }
     
     /**
      * Extracts Movies from JSON text data.
      * 
      * @param jsonResult
      *            the JSON data to extract
      * @return the List of Movies found
      */
     private static List<TmdbMovie> extractMoviesFromJson(String jsonResult) {
         List<TmdbMovie> movies = new ArrayList<TmdbMovie>();
         // parse result
         JsonParser parser = new JsonParser();
         JsonArray array = parser.parse(jsonResult).getAsJsonArray();
         Gson gson = new Gson();
         Iterator<JsonElement> iterator = array.iterator();
         while (iterator.hasNext()) {
             TmdbMovie movie = gson.fromJson(iterator.next(), TmdbMovie.class);
             if (movie != null) {
                 movies.add(movie);
                 logger.log(Level.DEBUG, "Found Movie: " + movie);
             }
         }
         return movies;
     }
     
     /**
      * URL-Encodes a given Parameter.
      * 
      * @throws MovieDataProviderException
      *             in case the parameter is null
      */
     private static String urlEncodeParameter(String parameter) throws MovieDataProviderException {
         if (parameter == null || parameter.isEmpty()) {
             throw new MovieDataProviderException("Input is empty or null and cannot be URL-encoded!");
         }
         String urlEncodedParam = null;
         try {
             urlEncodedParam = URLEncoder.encode(parameter, encoding);
         } catch (UnsupportedEncodingException e) {
             throw new MovieDataProviderException("Could not URL-Encode: " + parameter, e);
         }
         return urlEncodedParam;
     }
     
     /**
      * Fetches a Movie-Info identified by a given TMDB-ID.
      * 
      * @param tmdb_id
      *            the TMDB-ID of the Movie
      * @throws MovieDataProviderException
      *             in case the Request was not successful
      */
     public static TmdbMovieExtendedInfo getInfo(String tmdb_id) throws MovieDataProviderException {
         String result = doRequest(METHOD_INFO + tmdb_id);
         logger.log(Priority.DEBUG, "Got EXT TMDB-Movie Info: " + result);
         JsonParser parser = new JsonParser();
         JsonArray array = parser.parse(result).getAsJsonArray();
         Gson gson = new Gson();
         return gson.fromJson(array.get(0), TmdbMovieExtendedInfo.class);
     }
     
     /**
      * Fetches a Movie-Image identified by a given TMDB-ID.
      * 
      * @param tmdb_id
      *            the TMDB-ID of the Movie
      * @throws MovieDataProviderException
      *             in case the Request was not successful
      */
     public static TmdbImage getImage(String tmdb_id) throws MovieDataProviderException {
         String result = doRequest(METHOD_IMAGES + tmdb_id);
         logger.debug("Got TMDB Image Info: " + result);
         JsonParser parser = new JsonParser();
         JsonArray array = parser.parse(result).getAsJsonArray();
         Gson gson = new Gson();
         return gson.fromJson(array.get(0), TmdbImage.class);
     }
     
     /**
      * Convenience Method for HTTP-GET requests to TMDB.
      * 
      * @param method
      *            the method to append to the base URL
      * @return the result returned by TMDB
      * @throws MovieDataProviderException
      *             in case the request was not successful
      */
     private static String doRequest(String method) throws MovieDataProviderException {
         URL url = null;
         try {
             url = new URL(method);
         } catch (MalformedURLException e) {
             throw new MovieDataProviderException("Could not generate URL for Method: " + method, e);
         }
         HttpURLConnection conn = new HttpURLConnection(url, null);
         logger.info("Connecting to URL: " + conn);
         try {
             conn.setConnectTimeout(3000);
             conn.setRequestMethod("GET");
             conn.connect();
         } catch (IOException e) {
             throw new MovieDataProviderException("Could not connect to URL for Method: " + method, e);
         }
         
         try {
             int responseCode = conn.getResponseCode();
             if (responseCode != 200) {
                 throw new MovieDataProviderException("Server returned HTTP-Status: " + responseCode + " for Method: "
                         + method);
             }
         }
         
         catch (IOException e) {
             throw new MovieDataProviderException("Could not get Response Code from URL for Method: " + method, e);
         }
         
         InputStream inputStream = null;
         try {
             inputStream = conn.getInputStream();
         } catch (IOException e) {
             throw new MovieDataProviderException("Could not read from URL for Method: " + method, e);
         }
         
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
         
         StringBuilder msg = new StringBuilder();
         String line = null;
         try {
             while ((line = reader.readLine()) != null) {
                 msg.append(line);
             }
         } catch (IOException e) {
             throw new MovieDataProviderException("Could not read from InputStream of Method: " + method, e);
         }
         
         logger.log(Level.INFO, "Received Message: " + msg);
         
         String response = msg.toString();
         logger.debug("Response is: '" + response + "'");
         if (!response.trim().matches("\\[.*\\]")) { // test if result is json
             throw new MovieDataProviderException("Result is not JSON as expected: " + response);
         }
         
         return response;
     }
 }
