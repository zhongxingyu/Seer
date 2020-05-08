 package playlist.model;
 
 import com.datastax.driver.core.BoundStatement;
 import com.datastax.driver.core.PreparedStatement;
 import com.datastax.driver.core.ResultSet;
 import com.datastax.driver.core.Row;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * DataStax Academy Sample Application
  *
  * Copyright 2013 DataStax
  *
  */
 
 
 public class TracksDAO extends CassandraData {
 
   // Hard Coded Genres for now
 
   private final UUID track_id;
   private final String artist;
   private final String track;
   private final String genre;
   private Boolean starred;
   private final String music_file;
   private final int track_length_in_seconds;
 
   /**
   * Constructor to create a TrackDAO object when given a single Cassandra Row object
    *
    * @param row - a single Cassandra Java Driver Row
    *
    */
 
   private TracksDAO(Row row) {
     track_id = row.getUUID("track_id");
     artist = row.getString("artist");
     track = row.getString("track");
     genre = row.getString("genre");
     music_file = row.getString("music_file");
     track_length_in_seconds = row.getInt("track_length_in_seconds");
 
     try {
       starred = row.getBool("starred");
     } catch (Exception e) {
       starred = false;  // If the field doesn't exist or is null we set it to false
     }
   }
 
   public TracksDAO(String artist, String track, String genre, String music_file, int track_length_in_seconds) {
     this.track_id = UUID.randomUUID();  // We can generate the new UUID right here in the constructor
     this.artist = artist;
     this.track = track;
     this.genre = genre;
     this.music_file = music_file;
     this.track_length_in_seconds = track_length_in_seconds;
     starred = false;  // We never set this when adding a track, so leave this one alone
   }
 
   // Static finder method
 
   public static List<TracksDAO> listSongsByArtist(String artist) {
 
     String queryText = "SELECT * FROM track_by_artist WHERE artist = ?";
     PreparedStatement preparedStatement = getSession().prepare(queryText);
     BoundStatement boundStatement = preparedStatement.bind(artist);
     ResultSet results = getSession().execute(boundStatement);
 
     List<TracksDAO> tracks = new ArrayList<>();
 
     for (Row row : results) {
       tracks.add(new TracksDAO(row));
     }
 
     return tracks;
   }
 
   public static List<TracksDAO> listSongsByGenre(String genre, int num_tracks) {
 
     String queryText = "SELECT * FROM track_by_genre WHERE genre = ? LIMIT ?";
     PreparedStatement preparedStatement = getSession().prepare(queryText);
     BoundStatement boundStatement = preparedStatement.bind(genre, num_tracks);
     ResultSet results = getSession().execute(boundStatement);
 
 
     List<TracksDAO> tracks = new ArrayList<>();
 
     for (Row row : results) {
       tracks.add(new TracksDAO(row));
     }
 
     return tracks;
   }
 
   public static TracksDAO getTrackById(UUID track_id) {
     PreparedStatement preparedStatement = getSession().prepare("SELECT * FROM track_by_id WHERE track_id = ?");
     BoundStatement boundStatement = preparedStatement.bind(track_id);
     ResultSet resultSet = getSession().execute(boundStatement);
 
     // Return null if there is no track found
 
     if (resultSet.isExhausted()) {
       return null;
     }
 
     return new TracksDAO(resultSet.one());
   }
 
   /**
    * Add this track to the database
    */
 
   public void add() {
 
     // Compute the first letter of the artists name for the artists_by_first_letter table
     String artist_first_letter = this.artist.substring(0,1).toUpperCase();
 
     PreparedStatement preparedStatement =
             getSession().prepare("INSERT INTO artists_by_first_letter (first_letter, artist) VALUES (?, ?)");
     BoundStatement boundStatement = preparedStatement.bind(artist_first_letter, this.artist);
     getSession().execute(boundStatement);
 
     preparedStatement = getSession().prepare("INSERT INTO track_by_id (genre, track_id, artist, track, track_length_in_seconds) VALUES (?, ?, ?, ?, ?)");
     boundStatement = preparedStatement.bind(this.genre, this.track_id, this.artist, this.track, this.track_length_in_seconds);
     getSession().execute(boundStatement);
 
     preparedStatement = getSession().prepare("INSERT INTO track_by_genre (genre, track_id, artist, track, track_length_in_seconds) VALUES (?, ?, ?, ?, ?)");
     boundStatement = preparedStatement.bind(this.genre, this.track_id, this.artist, this.track, this.track_length_in_seconds);
     getSession().execute(boundStatement);
 
     preparedStatement = getSession().prepare("INSERT INTO track_by_artist (genre, track_id, artist, track, track_length_in_seconds) VALUES (?, ?, ?, ?, ?)");
     boundStatement = preparedStatement.bind(this.genre, this.track_id, this.artist, this.track, this.track_length_in_seconds);
     getSession().execute(boundStatement);
 
   }
 
   /**
    *  Set the track as being starred
    */
   public void star() {
 
     PreparedStatement preparedStatement = getSession().prepare("UPDATE track_by_artist  USING TTL 30 SET starred = true where artist = ? and track = ? and track_id = ?");
     BoundStatement boundStatement = preparedStatement.bind(artist, track, track_id);
     getSession().execute(boundStatement);
 
     preparedStatement = getSession().prepare("UPDATE track_by_genre  USING TTL 30 SET starred = true where genre = ? and artist = ? and track = ? and track_id = ?");
     boundStatement = preparedStatement.bind(genre, artist, track, track_id);
     getSession().execute(boundStatement);
 
   }
 
 
   public UUID getTrack_id() {
     return track_id;
   }
 
   public String getArtist() {
     return artist;
   }
 
   public String getTrack() {
     return track;
   }
 
   public String getGenre() {
     return genre;
   }
 
   public String getMusic_file() {
     return music_file;
   }
 
   public int getTrack_length_in_seconds() {
     return track_length_in_seconds;
   }
 
   public Boolean getStarred() {
     return starred;
   }
 }
