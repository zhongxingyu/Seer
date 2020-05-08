 package com.moac.android.soundmap.model;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Type;
 import java.util.Collection;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 @RunWith(JUnit4.class)
 public class ModelDeserializationTest {
 
     /**
      * Warning - the JSON returned from the SoundCloud API console
      * doesn't have the double quotes escaped! Keep that in mind when
      * copying test data from it - you may be better off getting it from the
      * API directly.
      */
 
     Gson gson;
 
     @Before
     public void setup() {
         GsonBuilder gsonBuilder = new GsonBuilder();
         gsonBuilder.registerTypeAdapter(GeoLocation.class, new GeoLocationDeserializer());
         gson = gsonBuilder.create();
     }
 
     @After
     public void tearDown() {
         gson = null;
     }
 
     @Test
     public void testTracksJsonDeserialisation() throws IOException {
         String json = readTestDataFile("tracks.json");
         Type collectionType = new TypeToken<Collection<Track>>() {
         }.getType();
         Collection<Track> tracks = gson.fromJson(json, collectionType);
         assertNotNull(tracks);
         assertEquals(4, tracks.size());
     }
 
     @Test
     public void testTrackSingleJsonDeserialisation() throws IOException {
         String json = readTestDataFile("track_single.json");
         Type trackType = new TypeToken<Track>() {
         }.getType();
 
         Track track = gson.fromJson(json, trackType);
         assertNotNull(track);
         assertEquals("99801677", track.getId());
         assertEquals("Dj Niko Force", track.getTitle());
         assertEquals("https://i1.sndcdn.com/artworks-000052237226-1guyjw-large.jpg?cc07a88", track.getArtworkUrl());
         assertEquals("https://w1.sndcdn.com/sPNv4LFoR9b7_m.png", track.getWaveformUrl());
         assertEquals("http://soundcloud.com/niko-nikiosdj/dj-niko-force", track.getPermalinkUrl());
 
         User user = track.getUser();
         assertNotNull(user);
         assertEquals("18402377", user.getId());
         assertEquals("NIV DJ [official]", user.getUsername());
         assertEquals("https://i1.sndcdn.com/avatars-000045447140-w7r3d2-large.jpg?cc07a88", user.getAvatarUrl());
         assertEquals("https://api.soundcloud.com/users/18402377", user.getUri());
     }
 
     @Test
     public void testTrackSingleGeoJsonDeserialisation() throws IOException {
         String json = readTestDataFile("track_single_geo.json");
         Type trackType = new TypeToken<Track>() {
         }.getType();
         Track track = gson.fromJson(json, trackType);
         assertNotNull(track);
        GeoLocation location = track.getGeoLocation();
         assertNotNull(location);
         assertEquals(52.527544, location.getLatitude(), 0.0);
         assertEquals(13.402905, location.getLongitude(), 0.0);
     }
 
     @Test
     public void testTrackSingleGeoQuotedJsonDeserialisation() throws IOException {
         String json = readTestDataFile("track_single_geo_quoted.json");
         Type trackType = new TypeToken<Track>() {
         }.getType();
         Track track = gson.fromJson(json, trackType);
         assertNotNull(track);
        GeoLocation location = track.getGeoLocation();
         assertNotNull(location);
         assertEquals(52.527544, location.getLatitude(), 0.0);
         assertEquals(13.402905, location.getLongitude(), 0.0);
     }
 
     @Test
     public void testTrackSingleGeoNegativeJsonDeserialisation() throws IOException {
         String json = readTestDataFile("track_single_geo_negative.json");
         Type trackType = new TypeToken<Track>() {
         }.getType();
         Track track = gson.fromJson(json, trackType);
         assertNotNull(track);
        GeoLocation location = track.getGeoLocation();
         assertNotNull(location);
         assertEquals(-52.527544, location.getLatitude(), 0.0);
         assertEquals(-13.402905, location.getLongitude(), 0.0);
     }
 
     @Test
     public void testTrackSingleGeoPositiveJsonDeserialisation() throws IOException {
         String json = readTestDataFile("track_single_geo_positive.json");
         Type trackType = new TypeToken<Track>() {
         }.getType();
         Track track = gson.fromJson(json, trackType);
         assertNotNull(track);
        GeoLocation location = track.getGeoLocation();
         assertNotNull(location);
         assertEquals(52.527544, location.getLatitude(), 0.0);
         assertEquals(13.402905, location.getLongitude(), 0.0);
     }
 
     @Test
     public void testTrackSingleGeoReversedOrderJsonDeserialisation() throws IOException {
         String json = readTestDataFile("track_single_geo_reversed_order.json");
         Type trackType = new TypeToken<Track>() {
         }.getType();
         Track track = gson.fromJson(json, trackType);
         assertNotNull(track);
        GeoLocation location = track.getGeoLocation();
         assertNotNull(location);
         assertEquals(52.527544, location.getLatitude(), 0.0);
         assertEquals(13.402905, location.getLongitude(), 0.0);
     }
 
     /**
      * FIXME Test the GeoLocation deserialization more directly.
      * Use only the required data rather than a whole track.
      * Using a full JSON Track makes it difficult to see what
      * the variance in the input data is and forces me to get an
      * entire Track dump just to test a single component.
      */
 
     private static String readTestDataFile(String _filename) {
         InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(_filename);
 
         if (inputStream == null)
             throw new IllegalArgumentException("Test data file not found on classpath: " + _filename);
 
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
         StringBuilder sb = new StringBuilder();
         String line;
         try {
             while ((line = reader.readLine()) != null) {
                 sb.append(line);
             }
             reader.close();
             return sb.toString();
         } catch (IOException ex) {
             throw new RuntimeException("Test data file could not be read: " + _filename, ex);
         }
     }
 }
