 /*
  * Copyright (c) 2011, dan.clark@nekocode.org
  *
  * Licensed under FreeBSD license.  See README for details.
  */
 
 package org.nekocode.itunes.remote;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.nekocode.itunes.remote.connection.ContentCode;
 import org.nekocode.itunes.remote.connection.ITunesRemoteResponse;
 import org.nekocode.nowplaying.objects.Track;
 
 import javax.swing.ImageIcon;
 import java.util.Collection;
 import java.util.Date;
 
 import static org.nekocode.itunes.remote.connection.ContentCode.*;
 
 /**
  * A Track that loads data from ITunesRemoteResponse objects
  */
 public class ITunesRemoteTrack implements Track {
     private static final Logger log = LogManager.getLogger(ITunesRemoteTrack.class);
 
     /**
      * These are the content codes expected in any response.
      */
     static final ContentCode[] DEFAULT_CONTENT_CODES = {
             DMAP_ITEM_ID,
             DMAP_ITEM_NAME,
             DAAP_SONG_ARTIST,
             DAAP_SONG_ALBUM,
             DAAP_SONG_GROUPING,
             DMAP_PERSISTENT_ID,
             DAAP_SONG_USER_RATING,
             DAAP_SONG_TIME,
             DAAP_SONG_TRACK_NUMBER,
             DAAP_SONG_TRACK_COUNT,
     };
 
     private String persistentId;
     private int trackId;
     private long databaseId;
     private String title;
     private String artist;
     private String album;
     private String grouping;
     private int rating;
     private Collection<ImageIcon> artwork;
     private double duration;
     private short trackNumber;
     private short trackCount;
 
     public ITunesRemoteTrack(long databaseId,
                              ITunesRemoteResponse response) {
         setValues(databaseId, response);
     }
 
     /**
      * Must call setValues() before this track can be used.
      */
     protected ITunesRemoteTrack() { }
 
     /**
      * Sets the internal values of this object.
      *
      * @param databaseId database id of track
      * @param response can be either a LIST_ITEM or a DAAP_DATABASE_SONGS
      */
     protected void setValues(long databaseId, ITunesRemoteResponse response) {
         this.databaseId = databaseId;
 
         ITunesRemoteResponse item;
         if (response.hasLeaf(DMAP_ITEM_ID)) {
             item = response;
         } else {
            ContentCode listType = response.hasLeaf(DAAP_DATABASE_SONGS) ? DAAP_DATABASE_SONGS : DAAP_PLAYLIST_SONGS;
             item = response.getMultiBranch(listType, DMAP_LIST, DMAP_LIST_ITEM).get(0);
         }
         trackId = item.getInt(DMAP_ITEM_ID);
         persistentId = item.hasLeaf(DMAP_PERSISTENT_ID) ? item.getHexNumber(DMAP_PERSISTENT_ID) : Integer.toString(trackId); // TODO MonkeyTunes doesn't seem to provide this
         title = item.getString(DMAP_ITEM_NAME);
         artist = item.getString(DAAP_SONG_ARTIST);
         album = item.getString(DAAP_SONG_ALBUM);
         grouping = item.getString(DAAP_SONG_GROUPING);
         rating = item.getInt(DAAP_SONG_USER_RATING);
         trackNumber = item.getShort(DAAP_SONG_TRACK_NUMBER, (short) 0);
         trackCount = item.getShort(DAAP_SONG_TRACK_COUNT, (short) 0);
         duration = item.getInt(DAAP_SONG_TIME) / 1000.0;
     }
 
     @Override
     public String getAlbum() {
         return album;
     }
 
     @Override
     public String getArtist() {
         return artist;
     }
 
     @Override
     public Collection<ImageIcon> getArtwork(int size) {
         return artwork;
     }
 
     public void setArtwork(Collection<ImageIcon> artwork) {
         this.artwork = artwork;
     }
 
     @Override
     public String getComment() {
         return null;
     }
 
     @Override
     public boolean isCompilation() {
         return false;
     }
 
     @Override
     public String getComposer() {
         return null;
     }
 
     @Override
     public int getDiscCount() {
         return 0;
     }
 
     @Override
     public int getDiscNumber() {
         return 0;
     }
 
     @Override
     public boolean isEnabled() {
         return false;
     }
 
     @Override
     public String getGenre() {
         return null;
     }
 
     @Override
     public String getGrouping() {
         return grouping;
     }
 
     @Override
     public int getPlayedCount() {
         return 0;
     }
 
     @Override
     public Date getPlayedDate() {
         return null;
     }
 
     @Override
     public int getRating() {
         return rating;
     }
 
     @Override
     public String getTitle() {
         return title;
     }
 
     @Override
     public int getTrackCount() {
         return trackCount;
     }
 
     @Override
     public int getTrackNumber() {
         return trackNumber;
     }
 
     @Override
     public int getVolumeAdjustment() {
         return 0;
     }
 
     @Override
     public int getYear() {
         return 0;
     }
 
     @Override
     public int getBitRate() {
         return 0;
     }
 
     @Override
     public int getBpm() {
         return 0;
     }
 
     @Override
     public Date getDateAdded() {
         return null;
     }
 
     @Override
     public double getDuration() {
         return duration;
     }
 
     @Override
     public Date getModificationDate() {
         return null;
     }
 
     @Override
     public int getSampleRate() {
         return 0;
     }
 
     @Override
     public int getSize() {
         return 0;
     }
 
     @Override
     public String getTime() {
         return null;
     }
 
     @Override
     public int getTrackId() {
         return trackId;
     }
 
     @Override
     public long getDatabaseId() {
         return databaseId;
     }
 
     @Override
     public Object getOriginal() {
         return null;
     }
 
     @Override
     public String getPersistentId() {
         return persistentId;
     }
 
     public String toString() {
         return getTitle() + '/' + getArtist() + '/' + getAlbum();
     }
 }
