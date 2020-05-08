 package com.github.tommywalsh.mcotp;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.content.ContentResolver;
 import android.provider.BaseColumns;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Audio;
 import java.util.Random;
 
 import android.util.Log;
 
 
 public class AndroidSongProvider implements SongProvider
 {
     private ContentResolver m_queryProvider;
     private Cursor m_cursor = null;
     private Random m_random = new Random();
     private boolean m_isRandom = false;
     private Song m_song;
 
     private enum Clamp { NONE, BAND, ALBUM}
 
     private Clamp m_clamp = Clamp.NONE;
 
     private void loadQuery() {
 	String[] proj = {MediaStore.Audio.AudioColumns.ALBUM, 
 			 MediaStore.Audio.AudioColumns.ALBUM_ID, 
 			 MediaStore.Audio.AudioColumns.ARTIST, 
 			 MediaStore.Audio.AudioColumns.ARTIST_ID, 
 			 MediaStore.MediaColumns.DATA,
 			 BaseColumns._ID};
 	
 	android.net.Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
 	
 	
 	String whereClause = null;
 	String[] whereArgs = null;
 
 	// don't lock on a non-existant band or album!
 	if (m_cursor != null) {
 	    if (m_clamp == Clamp.BAND) {
 		whereClause = MediaStore.Audio.AudioColumns.ARTIST_ID + "=?";
 		String[] tmp = {new Integer(m_cursor.getInt(3)).toString()};
 		whereArgs = tmp;
 	    } else if (m_clamp == Clamp.ALBUM) {
 		whereClause = MediaStore.Audio.AudioColumns.ALBUM_ID + "=?";
 		String[] tmp = {new Integer(m_cursor.getInt(1)).toString()};
 		whereArgs = tmp;
 	    }
 	}
 
 
 	m_cursor = m_queryProvider.query(uri, proj, whereClause, whereArgs, null);
 	
 	Log.d("MCOTP", new Integer(m_cursor.getCount()).toString() + " songs in DB");
     }
     
     public AndroidSongProvider(ContentResolver queryProvider) {
 	m_queryProvider = queryProvider;
 	loadQuery();
 	advanceSong();
     }
 
 
     // The next two functions give an iterator-like interface
     // and allow the engine to easily cycle over all applicable songs
     public Song getCurrentSong() {
 	return m_song;
     }
     private void storeSong() {
 	m_song = new Song(m_cursor.getString(2),
 			m_cursor.getString(0),
 			m_cursor.getString(4));
     }
     private void advanceSequential() {
 	m_cursor.moveToNext();
 	if (m_cursor.isAfterLast()) {
 	    m_cursor.moveToFirst();
 	}
     }
     private void advanceRandom() {
 	m_cursor.moveToPosition(m_random.nextInt(m_cursor.getCount()));
     }
 
     public void advanceSong() {
 	if (isRandom()) {
 	    advanceRandom();
 	} else {
 	    advanceSequential();
 	}
 	storeSong();
     }
     public void toggleRandom() { m_isRandom = !m_isRandom;}
     public boolean isRandom() {return m_isRandom;}
     public void setGenreClamp(String genreClamp) {}
 
     private void toggleClamp(Clamp cType) {
 	if (m_clamp == cType) {
 	    m_clamp = Clamp.NONE;
 	} else {
 	    m_clamp = cType;
 	}
 	loadQuery();
     }
 	
     public void toggleBandClamp() {
 	toggleClamp(Clamp.BAND);
     }
     public void toggleAlbumClamp() {
 	toggleClamp(Clamp.ALBUM);
     }
 
     public boolean isBandClamped() {return (m_clamp == Clamp.BAND);}
    public boolean isAlbumClamped() {return (m_clamp == Clamp.ALBUM);}
 }
