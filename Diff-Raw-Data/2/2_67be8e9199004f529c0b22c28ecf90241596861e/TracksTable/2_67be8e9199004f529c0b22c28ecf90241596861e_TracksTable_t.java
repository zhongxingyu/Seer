 package net.kokkeli.data.db;
 
 import java.io.File;
 import java.util.ArrayList;
 import com.almworks.sqlite4java.SQLiteConnection;
 import com.almworks.sqlite4java.SQLiteException;
 import com.almworks.sqlite4java.SQLiteStatement;
 
 import net.kokkeli.data.Role;
 import net.kokkeli.data.Track;
 import net.kokkeli.data.User;
 
 /**
  * Class representing Tracks-table in database. Used for operating the specific table only.
  * @author Hekku2
  *
  */
 public class TracksTable {
     private static final String TABLENAME = "tracks";
     private static final String ALLTRACKS = "SELECT * FROM " + TABLENAME;
     private static final String COLUMN_ID = "Id";
     private static final String COLUMN_TRACK = "Track";
     private static final String COLUMN_ARTIST = "Artist";
     private static final String COLUMN_LOCATION = "Location";
     private static final String COLUMN_UPLOADER = "Uploader";
     
     private static final String INSERT = "INSERT INTO "+ TABLENAME +" ("+ COLUMN_TRACK +","+ COLUMN_ARTIST+", "+ COLUMN_LOCATION +", "+ COLUMN_UPLOADER + ") VALUES ";
     
     private final String databaseLocation;
     
     /**
      * Creates TracksTable with given databaselocation
      * @param databaseLocation
      */
     public TracksTable(String databaseLocation) {
         this.databaseLocation = databaseLocation;
     }
 
     /**
      * Returns track with given id.
      * NOTE: User only contains id, username and role are not set.
      * @param id Id of track
      * @return Track Fetched track.
      * @throws DatabaseException Thrown if there is problem with database.
      * @throws NotFoundInDatabase Thrown if track is not in database.
      */
     public Track get(long id) throws DatabaseException, NotFoundInDatabase {
         SQLiteConnection db = new SQLiteConnection(new File(databaseLocation));
         Track track = null;
         
         try {
             db.open(false);
             SQLiteStatement st = db.prepare(getSingleItemQuery(id));
             try {
                 while (st.step()) {
                     track = new Track(st.columnLong(0));
                     track.setTrackName(st.columnString(1));
                     track.setArtist(st.columnString(2));
                     track.setLocation(st.columnString(3));
                     
                     User uploader = new User(st.columnLong(4), null, Role.NONE);
                     track.setUploader(uploader);
                 }
             } finally {
                 st.dispose();
             }
             db.dispose();
         } catch (SQLiteException e) {
             throw new DatabaseException("Unable to get tracks for playlist with Id: " + id, e);
         }
         
         if (track == null)
             throw new NotFoundInDatabase("No track with id: " + id + " in database.");
         
         return track;
     }
 
     /**
      * Returns all tracks from database. User only holds Id.
      * @return Collection of tracks
      * @throws DatabaseException Thrown if there is a problem with the database
      */
     public ArrayList<Track> get() throws DatabaseException {
         SQLiteConnection db = new SQLiteConnection(new File(databaseLocation));
         
         ArrayList<Track> tracks = new ArrayList<Track>();
         try {
             db.open(false);
             SQLiteStatement st = db.prepare(ALLTRACKS);
             try {
                 while (st.step()) {
                     Track track = new Track(st.columnLong(0));
                     track.setTrackName(st.columnString(1));
                     track.setArtist(st.columnString(2));
                     track.setLocation(st.columnString(3));
                     
                     User uploader = new User(st.columnLong(4), null, Role.NONE);
                     track.setUploader(uploader);
                     tracks.add(track);
                 }
             } finally {
                 st.dispose();
             }
             db.dispose();
         } catch (SQLiteException e) {
            throw new DatabaseException("Unabe to get tracks from database.", e);
         }
         
         return tracks;
     }
     
     /**
      * Inserts track to database.
      * @param newTrack Track to insert
      * @return Database id of added track.
      * @throws DatabaseException thrown if there is problem with the database.
      */
     public long insert(Track newTrack) throws DatabaseException {
         SQLiteConnection db = new SQLiteConnection(new File(databaseLocation));
         
         long id;
         try {
             db.open(false);
             SQLiteStatement st = db.prepare(insertItemRow(newTrack));
             try {
                 st.stepThrough();
             } finally {
                 st.dispose();
             }
             
             id = db.getLastInsertId();
             db.dispose();
         } catch (SQLiteException e) {
             throw new DatabaseException("Unabe to insert track to database.", e);
         }
         
         return id;
     }
     
     /**
      * Creates query selecting single user.
      * @param id Id of wanted user
      * @return Query for selecting single user.
      */
     private static String getSingleItemQuery(long id){
         return ALLTRACKS + " WHERE "+ COLUMN_ID+" = " + id;
     }
 
     /**
      * Creates insert statement
      * @param track Track to insert
      * @return Insert statement
      */
     private static String insertItemRow(Track track){
         return INSERT + "('" + track.getTrackName() + "','" + track.getArtist() + "','" + track.getLocation() + "',"+ track.getUploader().getId()+")";
     }
 }
