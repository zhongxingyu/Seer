 package com.dedaulus.cinematty.framework.tools;
 
 import com.dedaulus.cinematty.framework.Cinema;
 import com.dedaulus.cinematty.framework.Movie;
 import com.dedaulus.cinematty.framework.MovieActor;
 import com.dedaulus.cinematty.framework.MovieGenre;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import java.util.*;
 
 /**
  * User: Dedaulus
  * Date: 13.03.11
  * Time: 22:50
  */
 public class ScheduleHandler extends DefaultHandler {
     private static final String CINEMA_TAG          = "theater";
     private static final String CINEMA_ID_ATTR      = "id";
     private static final String CINEMA_TITLE_ATTR   = "title";
     private static final String CINEMA_ADDRESS_ATTR = "address";
     private static final String CINEMA_METRO_ATTR   = "metro";
     private static final String CINEMA_PHONE_ATTR   = "phone";
     private static final String CINEMA_URL_ATTR     = "www";
 
     private static final String MOVIE_TAG = "movie";
     private static final String SHOWTIME_TAG = "showtime";
 
     private static final String ACTORS_BUG_SUFFIX = " - ;";
 
     private HashMap<String, Cinema> mCinemaIds;
     private HashMap<String, Movie> mMovieIds;
     private HashMap<String, MovieActor> mActors;
     private HashMap<String, MovieGenre> mGenres;
     private StringBuilder mBuffer;
     private Cinema mCurrentCinema;
     private Movie mCurrentMovie;
 
     public void getSchedule(UniqueSortedList<Cinema> cinemas,
                             UniqueSortedList<Movie> movies,
                             UniqueSortedList<MovieActor> actors,
                             UniqueSortedList<MovieGenre> genres) {
         cinemas.clear();
         movies.clear();
         actors.clear();
         genres.clear();
 
         cinemas.addAll(mCinemaIds.values());
         movies.addAll(mMovieIds.values());
         actors.addAll(mActors.values());
         genres.addAll(mGenres.values());
     }
 
     @Override
     public void startDocument() throws SAXException {
         super.startDocument();
 
         mCinemaIds = new HashMap<String, Cinema>();
         mMovieIds = new HashMap<String, Movie>();
         mActors = new HashMap<String, MovieActor>();
         mGenres = new HashMap<String, MovieGenre>();
         mBuffer = new StringBuilder();
     }
 
     @Override
     public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         super.startElement(uri, localName, qName, attributes);
 
         if (qName.equalsIgnoreCase(CINEMA_TAG)) {
             Cinema cinema = new Cinema(attributes.getValue(CINEMA_TITLE_ATTR));
             cinema.setAddress(attributes.getValue(CINEMA_ADDRESS_ATTR));
             cinema.setMetro(attributes.getValue(CINEMA_METRO_ATTR));
             cinema.setPhone(attributes.getValue(CINEMA_PHONE_ATTR));
             cinema.setUrl(attributes.getValue(CINEMA_URL_ATTR));
 
             mCinemaIds.put(attributes.getValue(CINEMA_ID_ATTR), cinema);
         }
         else if (qName.equalsIgnoreCase(MOVIE_TAG)) {
             Movie movie = new Movie(attributes.getValue("title"));
 
             movie.setLengthInMinutes(parseLength(attributes.getValue("length")));
 
             // Get movie genres
             List<String> genres = parseGenres(attributes.getValue("type"));
             for (String genre : genres) {
                 MovieGenre movieGenre = mGenres.get(genre);
                 if (movieGenre == null) {
                     movieGenre = new MovieGenre(genre);
                     movie.addGenre(movieGenre);
                     mGenres.put(genre, movieGenre);
                 }
                 else {
                     movie.addGenre(movieGenre);
                 }
             }
             ///
 
             mMovieIds.put(attributes.getValue("id"), movie);
             mCurrentMovie = movie;
         }
         else if (qName.equalsIgnoreCase(SHOWTIME_TAG)) {
             mCurrentCinema = mCinemaIds.get(attributes.getValue("theater_id"));
             mCurrentMovie = mMovieIds.get(attributes.getValue("movie_id"));
         }
     }
 
     @Override
     public void endElement(String uri, String localName, String qName) throws SAXException {
         super.endElement(uri, localName, qName);
 
         if (qName.equalsIgnoreCase("actors") && mBuffer.length() > 0) {
             List<String> actors = parseActors(mBuffer.toString());
             for (String actor : actors) {
                 MovieActor movieActor = mActors.get(actor);
                 if (movieActor == null) {
                     movieActor = new MovieActor(actor);
                     mCurrentMovie.addActor(movieActor);
                     mActors.put(actor, movieActor);
                 }
                 else {
                     mCurrentMovie.addActor(movieActor);
                 }
             }
         }
         else if (qName.equalsIgnoreCase("description") && mBuffer.length() > 0) {
             mCurrentMovie.setDescription(mBuffer.toString());
         }
         else if (qName.equalsIgnoreCase(SHOWTIME_TAG) && mBuffer.length() > 0) {
             mCurrentCinema.addShowTime(mCurrentMovie, parseTimes(mBuffer.toString()));
         }
 
         mBuffer.setLength(0);
     }
 
     @Override
     public void characters(char[] ch, int start, int length) throws SAXException {
         super.characters(ch, start, length);
 
         mBuffer.append(ch, start, length);
     }
 
     private List<String> parseGenres(String genres) {
         List<String> list = new ArrayList<String>();
         StringTokenizer st = new StringTokenizer(genres, "/");
 
         while (st.hasMoreTokens()) {
             list.add(st.nextToken());
         }
 
         return list;
     }
 
     private List<String> parseActors(String actors) {
         if (actors.endsWith(ACTORS_BUG_SUFFIX)) {
             actors = actors.substring(0, actors.length() - ACTORS_BUG_SUFFIX.length());
         }
         List<String> list = new ArrayList<String>();
         StringTokenizer st = new StringTokenizer(actors, ";");
 
         while (st.hasMoreTokens()) {
             list.add(st.nextToken());
         }
 
         return list;
     }
 
     private int parseLength(String time) {
         if (time == null || time.length() == 0) {
             return 0;
         }
         else if (time.contains(":")) {
             StringTokenizer st = new StringTokenizer(time, ":");
             return Integer.parseInt(st.nextToken()) * 60 + Integer.parseInt(st.nextToken());
         }
         else return Integer.parseInt(time);
     }
 
     private List<Calendar> parseTimes(String times) {
        List<Calendar> list = new UniqueSortedList<Calendar>(new DefaultComparator<Calendar>());
         StringTokenizer st = new StringTokenizer(times, ";");
 
         while (st.hasMoreTokens()) {
             StringTokenizer hoursAndMinutes = new StringTokenizer(st.nextToken(), ":");
 
             if (hoursAndMinutes.hasMoreTokens()) {
                 int hours = Integer.parseInt(hoursAndMinutes.nextToken());
                 int minutes = Integer.parseInt(hoursAndMinutes.nextToken());
 
                 Calendar now = Calendar.getInstance();
                int hourNow = now.get(Calendar.HOUR_OF_DAY);

                 now.set(Calendar.HOUR_OF_DAY, hours);
                 now.set(Calendar.MINUTE, minutes);
 
                if (hourNow < 5) {
                    now.add(Calendar.DAY_OF_MONTH, -1);
                }

                 if (hours < 5) { /* holly fuck!*/
                     now.add(Calendar.DAY_OF_MONTH, 1);
                 }
 
                 list.add(now);
             }
         }
 
         return list;
     }
 }
