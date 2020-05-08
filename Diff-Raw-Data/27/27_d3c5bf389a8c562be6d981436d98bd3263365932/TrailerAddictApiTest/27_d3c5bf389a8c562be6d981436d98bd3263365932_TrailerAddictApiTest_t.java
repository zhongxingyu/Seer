 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.moviejukebox.traileraddictapi;
 
 import com.moviejukebox.traileraddictapi.model.Trailer;
 import com.moviejukebox.traileraddictapi.model.TrailerSize;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  * @author stuart.boston
  */
 public class TrailerAddictApiTest {
 
     private static final Logger LOGGER = Logger.getLogger(TrailerAddictApiTest.class);
     private static final List<String> FILM_IDS = new ArrayList<String>();
     private static final List<String> IMDB_IDS = new ArrayList<String>();
     private static final List<String> ACTOR_IDS = new ArrayList<String>();
     private static final List<String> FEATURED_IDS = new ArrayList<String>();
     private static final int NUMBER_OF_TRAILERS = 4;
     private static final int REQUIRED_WIDTH = 720;
     private static final String TEST_WIDTH_COMPARE = "width=\"" + REQUIRED_WIDTH + "\"";
     private static final String TEST_TRAILER_URL = "http://www.traileraddict.com/trailer/avatar/trailer-b";
 
     public TrailerAddictApiTest() {
     }
 
     @BeforeClass
     public static void setUpClass() {
         // Set the logger level to TRACE
         Logger.getRootLogger().setLevel(Level.TRACE);
         // Add the film IDs
         FILM_IDS.add("the-hobbit");
 
         IMDB_IDS.add("tt0903624");  // The Hobbit
         IMDB_IDS.add("0903624");    // The Hobbit
 
         ACTOR_IDS.add("brad-pitt");
 
         FEATURED_IDS.add("yes");
         FEATURED_IDS.add("no");
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of getFilm method, of class TrailerAddictApi.
      */
     @Test
     public void testGetFilm() throws TrailerAddictException {
         LOGGER.info("getFilm");
 
         for (String id : FILM_IDS) {
             List<Trailer> trailers = TrailerAddictApi.getFilm(id, NUMBER_OF_TRAILERS);
             assertNotNull("List of trailers is null for " + id, trailers);
             assertFalse("List of trailers is empty for " + id, trailers.isEmpty());
             assertTrue("Incorrect number of trailers for " + id, trailers.size() == NUMBER_OF_TRAILERS);
         }
     }
 
     /**
      * Test of getFilm method, of class TrailerAddictApi.
      */
     @Test
    public void testGetFilmWidth() throws TrailerAddictException {
         LOGGER.info("getFilmWidth");
 
         String id = FILM_IDS.get(0);
         List<Trailer> trailers = TrailerAddictApi.getFilm(id, NUMBER_OF_TRAILERS, REQUIRED_WIDTH);
         assertNotNull("List of trailers is null for " + id, trailers);
         assertFalse("List of trailers is empty for " + id, trailers.isEmpty());
         Trailer t = trailers.get(0);
         String embed = t.getEmbed(TrailerSize.custom);
         assertNotNull("No custom trailer size found for " + id, embed);
         assertTrue("Incorrect width found in results for " + id, embed.contains(TEST_WIDTH_COMPARE));
     }
 
     /**
      * Test of getActor method, of class TrailerAddictApi.
      */
     @Test
    public void testGetActor() throws TrailerAddictException {
         LOGGER.info("getActor");
 
         for (String id : ACTOR_IDS) {
             LOGGER.info("Actor: " + id);
             List<Trailer> trailers = TrailerAddictApi.getActor(id, NUMBER_OF_TRAILERS);
             assertNotNull("List of trailers is null for " + id, trailers);
             assertFalse("List of trailers is empty for " + id, trailers.isEmpty());
             assertTrue("Incorrect number of trailers for " + id, trailers.size() == NUMBER_OF_TRAILERS);
         }
     }
 
     /**
      * Test of getActor method, of class TrailerAddictApi.
      */
     @Test
    public void testGetActorWidth() throws TrailerAddictException {
         LOGGER.info("getActorWidth");
 
         String id = ACTOR_IDS.get(0);
         List<Trailer> trailers = TrailerAddictApi.getActor(id, NUMBER_OF_TRAILERS, REQUIRED_WIDTH);
         assertNotNull("List of trailers is null for " + id, trailers);
         assertFalse("List of trailers is empty for " + id, trailers.isEmpty());
         Trailer t = trailers.get(0);
         String embed = t.getEmbed(TrailerSize.custom);
         assertNotNull("No custom trailer size found for " + id, embed);
         assertTrue("Incorrect width found in results for " + id, embed.contains(TEST_WIDTH_COMPARE));
     }
 
     /**
      * Test of getFilmImdb method, of class TrailerAddictApi.
      */
     @Test
    public void testGetFilmImdb() throws TrailerAddictException {
         LOGGER.info("getFilmImdb");
 
         for (String id : IMDB_IDS) {
             List<Trailer> trailers = TrailerAddictApi.getFilmImdb(id, NUMBER_OF_TRAILERS);
             assertNotNull("List of trailers is null for " + id, trailers);
             assertFalse("List of trailers is empty for " + id, trailers.isEmpty());
             assertTrue("Incorrect number of trailers for " + id, trailers.size() == NUMBER_OF_TRAILERS);
         }
     }
 
     /**
      * Test of getFilmImdb method, of class TrailerAddictApi.
      */
     @Test
    public void testGetFilmImdbWidth() throws TrailerAddictException {
         LOGGER.info("getFilmImdbWidth");
 
         String id = IMDB_IDS.get(0);
         List<Trailer> trailers = TrailerAddictApi.getFilmImdb(id, NUMBER_OF_TRAILERS, REQUIRED_WIDTH);
         assertNotNull("List of trailers is null for " + id, trailers);
         assertFalse("List of trailers is empty for " + id, trailers.isEmpty());
         Trailer t = trailers.get(0);
         String embed = t.getEmbed(TrailerSize.custom);
         assertNotNull("No custom trailer size found for " + id, embed);
         assertTrue("Incorrect width found in results for " + id, embed.contains(TEST_WIDTH_COMPARE));
     }
 
     /**
      * Test of getFeatured method, of class TrailerAddictApi.
      */
     @Test
    public void testGetFeatured() throws TrailerAddictException {
         LOGGER.info("getFeatured");
 
         for (String id : FEATURED_IDS) {
             List<Trailer> trailers = TrailerAddictApi.getFeatured(id, NUMBER_OF_TRAILERS);
             assertNotNull("List of trailers is null for " + id, trailers);
             assertFalse("List of trailers is empty for " + id, trailers.isEmpty());
             assertTrue("Incorrect number of trailers for " + id, trailers.size() == NUMBER_OF_TRAILERS);
         }
     }
 
     /**
      * Test of getFeatured method, of class TrailerAddictApi.
      */
     @Test
    public void testGetFeaturedWidth() throws TrailerAddictException {
         LOGGER.info("getFeatured");
 
         for (String id : FEATURED_IDS) {
             List<Trailer> trailers = TrailerAddictApi.getFeatured(id, NUMBER_OF_TRAILERS, REQUIRED_WIDTH);
             assertNotNull("List of trailers is null for " + id, trailers);
             assertFalse("List of trailers is empty for " + id, trailers.isEmpty());
             Trailer t = trailers.get(0);
             String embed = t.getEmbed(TrailerSize.custom);
             assertNotNull("No custom trailer size found for " + id, embed);
             assertTrue("Incorrect width found in results for " + id, embed.contains(TEST_WIDTH_COMPARE));
         }
     }
 
     /**
      * Test of getSimpleApi method, of class TrailerAddictApi.
      */
     @Test
    public void testGetSimpleApi_String() throws TrailerAddictException {
         LOGGER.info("getSimpleApi (Via link string)");
         Trailer trailer = TrailerAddictApi.getSimpleApi(TEST_TRAILER_URL);
         assertNotNull("Simple trailer is null", trailer);
         assertFalse("Simple trailer is empty", trailer.getEmbed().isEmpty());
     }
 
     /**
      * Test of getSimpleApi method, of class TrailerAddictApi.
      */
     @Test
     public void testGetSimpleApi_Trailer() throws TrailerAddictException {
         LOGGER.info("getSimpleApi (via Trailer)");
 
         for (String id : FILM_IDS) {
             List<Trailer> trailers = TrailerAddictApi.getFilm(id, NUMBER_OF_TRAILERS);
             assertNotNull("List of trailers is null for " + id, trailers);
             assertFalse("List of trailers is empty for " + id, trailers.isEmpty());
 
             // Just test the first trailer
             Trailer trailer = TrailerAddictApi.getSimpleApi(trailers.get(0));
             assertNotNull("Simple trailer is null for " + id, trailer);
             assertFalse("Simple trailer is empty", trailer.getEmbed().isEmpty());
 
             // Just test the first movie
             break;
         }
 
     }
 }
