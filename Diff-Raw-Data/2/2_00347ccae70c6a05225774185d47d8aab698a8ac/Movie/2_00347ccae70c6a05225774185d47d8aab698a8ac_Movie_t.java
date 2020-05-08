 package com.dedaulus.cinematty.framework;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.*;
 
 /**
  * User: Dedaulus
  * Date: 13.03.11
  * Time: 21:58
  */
 public class Movie implements Comparable<Movie> {
     private static final List<String> BLANK_COUNTRIES             = new ArrayList<String>();
     private static final List<String> BLANK_DIRECTORS             = new ArrayList<String>();
     private static final Map<String, Cinema> BLANK_CINEMAS_IN_DAY = new HashMap<String, Cinema>();
     private static final Map<String, MovieActor> BLANK_ACTORS     = new HashMap<String, MovieActor>();
     private static final Map<String, MovieGenre> BLANK_GENRES     = new HashMap<String, MovieGenre>();
     private static final List<MovieReview> BLANK_REVIEWS          = new ArrayList<MovieReview>();
 
     private static String sharedUrl = "http://gdekino.net/cinematty/shared.php";
 
     private String name;
     private String id;
     private String picId;
     private MovieFrameIdsStore frameIdsStore;
     private int length; // in minutes
     private int year;
     private List<String> countries;
     private List<String> directors;
     private String description;
     private Map<String, MovieActor> actors;
     private Map<String, MovieGenre> genres;
     private float imdb;
     private List<MovieReview> reviews;
     private Map<Integer, Map<String, Cinema>> cinemas;
 
     {
         countries = BLANK_COUNTRIES;
         directors = BLANK_DIRECTORS;
         genres = BLANK_GENRES;
         actors = BLANK_ACTORS;
         reviews = BLANK_REVIEWS;
         cinemas = new HashMap<Integer, Map<String, Cinema>>();
     }
     
     public Movie(
             String name, 
             String id, 
             String picId, 
             MovieFrameIdsStore frameIdsStore, 
             int length, 
             int year, 
             List<String> countries, 
             List<String> directors,
             String description,
             Map<String, MovieActor> actors,
             Map<String, MovieGenre> genres,
             float imdb) {
         this.name = name;
         this.id = id;
         this.picId = picId;
         this.frameIdsStore = frameIdsStore;
         this.length = length;
         this.year = year;
         
         if (countries != null) {
             this.countries = countries;
         }
         
         if (directors != null) {
             this.directors = directors;
         }
         
         this.description = description;
         
         if (actors != null) {
             this.actors = actors;
             for (MovieActor actor : this.actors.values()) {
                 actor.addMovie(this);
             }
         }
         
         if (genres != null) {
             this.genres = genres;
             for (MovieGenre genre : this.genres.values()) {
                 genre.addMovie(this);
             }
         }
         
         this.imdb = imdb;
     }
 
     public String getName() {
         return name;
     }
 
     public String getId() {
         return id;
     }
 
     public String getPicId() {
         return picId;
     }
     
     public MovieFrameIdsStore getFrameIdsStore() {
         return frameIdsStore;
     }
 
     public int getLength() {
         return length;
     }
     
     public int getYear() {
         return year;
     }
     
     public List<String> getCountries() {
         return countries;
     }
     
     public List<String> getDirectors() {
         return directors;
     }
 
     public String getDescription() {
         return description;
     }
 
     public Map<String, MovieActor> getActors() {
         return actors;
     }
 
     public Map<String, MovieGenre> getGenres() {
         return genres;
     }
 
     public float getImdb() {
         return imdb;
     }
     
     public void addReview(MovieReview review) {
         if (reviews == BLANK_REVIEWS) {
             reviews = new ArrayList<MovieReview>();
         }
         reviews.add(review);
     }
 
     public List<MovieReview> getReviews() {
         return reviews;
     }
 
     public void addCinema(Cinema cinema, int day) {
         if (!cinemas.containsKey(day)) {
             cinemas.put(day, new HashMap<String, Cinema>());
         }
         
         cinemas.get(day).put(cinema.getName(), cinema);
     }
 
     public Map<String, Cinema> getCinemas(int day) {
         if (cinemas.containsKey(day)) {
             return cinemas.get(day);
         } else {
             cinemas.put(day, BLANK_CINEMAS_IN_DAY);
             return BLANK_CINEMAS_IN_DAY;
         }
     }
 
     public static void setSharedUrl(String url) {
         sharedUrl = url;
     }
 
     public String getSharedPageUrl(City city, Cinema cinema, int day) {
         return createSharedPageUrl(city, cinema, day);
     }
     
     public String getSharedPageUrl(City city) {
         return createSharedPageUrl(city, null, null);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         Movie other = (Movie)o;
         return name.equals(other.name);
     }
 
     @Override
     public int hashCode() {
         return name.hashCode();
     }
 
     public int compareTo(Movie o) {
         return name.compareTo(o.name);
     }
     
     private String createSharedPageUrl(City city, Cinema cinema, Integer day) {
         try {
             // JSON data:
             JSONObject json = new JSONObject();
            json.put("city", city.getFileName());
             json.put("movie", id);
             if (cinema != null && day != null) {
                 json.put("cinema", cinema.getId());
                 json.put("day", day.intValue());
             }
 
             JSONArray postJson = new JSONArray();
             postJson.put(json);
 
             // Post the data:
             HttpPost httpPost = new HttpPost(sharedUrl);
             httpPost.setHeader("json", json.toString());
             httpPost.getParams().setParameter("jsonpost", postJson);
 
             // Execute HTTP Post Request
             HttpClient httpClient = new DefaultHttpClient();
             HttpResponse response = httpClient.execute(httpPost);
 
             // for JSON:
             if(response != null) {
                 InputStream is = response.getEntity().getContent();
 
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                 StringBuilder sb = new StringBuilder();
                 String line = null;
                 while ((line = reader.readLine()) != null) {
                     sb.append(line);
                 }
                 is.close();
 
                 return new JSONObject(sb.toString()).getString("url");
             }
         } catch (Exception e) {
             // TODO something
         }
 
         return null;
     }
 }
