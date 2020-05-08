 package com.github.tommywalsh.mcotp;
 
 import java.util.Vector;
 import java.util.TreeMap;
 import java.util.SortedMap;
 import java.util.Random;
 import java.io.Serializable;
 
 /////////////////////////////////////////////////////////////////////
 //
 // This class is in charge of sequencing songs and providing them
 // to the engine.  The public interface is documented below
 //
 /////////////////////////////////////////////////////////////////////
 
 
 public class SongProvider implements Serializable
 {
     // The next two functions give an iterator-like interface
     // and allow the engine to easily cycle over all applicable songs
     public Song getCurrentSong() {
 	String albumName = null;
 	if (m_currentAlbum != null) {
 	    albumName = m_currentAlbum.name();
 	}
 	return new Song(m_currentBand.name(), albumName, m_currentSong);
     }
 
     public void advanceSong() {
 	if (m_isRandom) {
 	    advanceToRandomSong();
 	} else {
 	    advanceToNextLinearSong();
 	}
     }
     
     //  Should the songs be provided in random order, or sequential?
     public void toggleRandom() {
 	m_isRandom = !m_isRandom;
     }
 
     public boolean isRandom() {
 	return m_isRandom;
     }
 
     // The "clamp" functions specify the "boundaries" of available songs.
     // These are currently unimplemented.  In future, this might be
     // expanded to allow arbitrary filters 
     // (e.g. "Allow only death metal or Elton John or Spirit in the Sky")
     
     // Only serve up songs of the given genre
     public void setGenreClamp(String genreClamp)
     {
         m_bandClamp = "";
         m_albumClamp = "";
     }
 
     // Only serve up songs from a single band
     public void setBandClamp(String band)
     {
         m_bandClamp = band;
         m_albumClamp = "";
     }
 
     // Only serve up songs from a single album
     public void setAlbumClamp(String band, String album)
     {
         m_bandClamp = band;
         m_albumClamp = album;
     }
 
     public boolean isBandClamped()
     {
 	return !isAlbumClamped() && (m_bandClamp != null && m_bandClamp != "");
     }
     public boolean isAlbumClamped()
     {
 	return (m_albumClamp != null && m_albumClamp != "");
     }
 
     public SongProvider(StorageProvider sp) {
 	m_storage = sp;
     }
 
     public void initAfterDeserialization(StorageProvider sp) {
 	m_storage = sp;
 	m_isRandom = false;
 	m_bandClamp = "";
 	m_albumClamp = "";
 	m_currentSong = null;
 	m_currentBand = null;
 	m_currentAlbum = null;
 	m_random = new Random();
 
 	buildIndex();
 
 	advanceToSpecificSong(m_currentSongNumber);
     }
 
     ///////// END OF PUBLIC INTERFACE //////////
 
 
 
 
 
 
 
 
 
 
 
 
     transient private StorageProvider m_storage;
     
     transient private TreeMap<Integer, Integer> m_indexToBand; 
     private Vector<Band> m_allBands;
     private int m_numSongs;
     transient private boolean m_isRandom = false;
     transient private String m_bandClamp = "";
     transient private String m_albumClamp = "";
     transient private String m_currentSong;
     private int m_currentSongNumber;
     
     transient private Band m_currentBand;
     transient private Album m_currentAlbum;
 
 
 
 
 
 
 
     ////////////////////////////////////////////////////////////
     // Convenience function wrapping storage provider methods //
     ////////////////////////////////////////////////////////////
     private Vector<String> getFiles(String path) {
 	return m_storage.getFilesOrDirs(path, true);
     }
 
     private Vector<String> getDirectories(String path) {
 	return m_storage.getFilesOrDirs(path, false);
     }
 
     private Vector<String> getAlbumSongs(String band, String album) {	
 	return getFiles(m_storage.getAlbumPath(band, album));
     }
 
     private Vector<String> getAlbums(String band) {
 	return getDirectories(m_storage.getBandPath(band));
     }
 
     private Vector<String> getLooseSongs(String band) {
 	return getFiles(m_storage.getBandPath(band));
     }
 
     private Vector<String> getAllBands() {
 	return getDirectories(m_storage.getLibraryPath());
     }
     ////////////////////////////////////////////////////////////
 
 
 
 
 
     
 
 
 
     ////////////////////////////////////////////////////////////
     // Classes and functions to keep track of music in library
     ////////////////////////////////////////////////////////////
     private Album constructAlbum(String band, String albumName, int startNum) {
 	Vector<String> allSongs = getAlbumSongs(band, albumName);
 	
 	return new Album(albumName, startNum, allSongs.size() );
     }
 
 
     
 
 
 
 
     private Band constructBand(String bandName, int startNum) {
 	Vector<String> looseSongs = getLooseSongs(bandName);
 	Vector<String> albumNames = getAlbums(bandName);
 
 	Vector<Album> albums = new Vector<Album>();
 	int albumStartNum = startNum + looseSongs.size();
 	int albumSongCount = 0;
 	for (String albumName : albumNames) {
 	    Album album = constructAlbum(bandName, albumName, albumStartNum);
 	    albums.add(album);
 	    albumSongCount += album.numSongs();
 	    albumStartNum += album.numSongs();
 	}
 	return new Band(bandName,
 			startNum,
 			albumSongCount + looseSongs.size(),
 			albums);
     }
     ////////////////////////////////////////////////////////////
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
     private void buildIndex()
     {
	m_indexToBand = new TreeMap<Integer, Integer>();
 	for (int i=0; i<m_allBands.size(); ++i) {
 	    m_indexToBand.put((Integer)(m_allBands.elementAt(i).firstSong()), i);
 	}
     }
     
     public void constructLibrary()
     {
 	int nextSongNum = 0;
 	m_allBands = new Vector<Band>();
 	m_indexToBand = new TreeMap<Integer, Integer>();
 
 	int i = 0;
 	for (String bandName : getAllBands()) {
 	    Band band = constructBand(bandName, nextSongNum);
 	    nextSongNum += band.numSongs();
 	    m_allBands.add(band);
 	    m_indexToBand.put((Integer)(band.firstSong()), i);
 	    ++i;
 	}
 	m_numSongs = nextSongNum;
 	advanceToSpecificBandSong(m_allBands.elementAt(0), 0);
     }
 
 
 
 
 
 
 
     /////////////////////////////////////////////////////////////////////
     // Functions that handle moving to the "next" song.
     // There's a few levels, with the more general ones redirecting to the
     // more specific ones, depending on the situation
     /////////////////////////////////////////////////////////////////////
     private void advanceToSpecificAlbumSong(Band band, Album album, int songNum) {
 	assert (album != null);
 	assert ( (songNum >= album.firstSong()) &&
 		 (songNum <= album.lastSong()));
 
 	Vector<String> albumSongs = getAlbumSongs(band.name(), album.name());
 	m_currentSongNumber = songNum;
 	m_currentSong = albumSongs.elementAt(songNum - album.firstSong());
 	m_currentAlbum = album;
 	m_currentBand = band;
     }
 
     private void advanceToNextAlbumSong() {
 	Album album = m_currentAlbum;
 	assert (album != null);
 
 	int songNum = m_currentSongNumber;
 	++songNum;
 	if (songNum > album.lastSong()) {
 	    songNum = album.firstSong();
 	}
 
 	advanceToSpecificAlbumSong(m_currentBand, album, songNum);
     }
 
     private void advanceToSpecificLooseSong(Band band, int songNum) 
     {
 	int index = songNum - band.firstSong();
 	assert index >= 0;
 	Vector<String> looseSongs = getLooseSongs(band.name());
 	assert index < looseSongs.size();
 	m_currentSongNumber = songNum;
 	m_currentSong = looseSongs.elementAt(index);
 	m_currentBand = band;
 	m_currentAlbum = null;
 
     }
 
     private void advanceToSpecificBandSong(Band band, int songNum) {
 	assert band != null;
 	assert songNum >= band.firstSong();
 	assert songNum <= band.lastSong();
 
 	Vector<Album> albums = band.albums();
 	if (albums.size() == 0 || songNum < albums.elementAt(0).firstSong()) {
 	    advanceToSpecificLooseSong(band, songNum);
 	} else {
 	    // Might be slightly better to do a binary search here
 	    // (or maybe not... there will be a lot of cases with a
 	    //  small number of albums)
 	    int i = 0;
 	    while(songNum > albums.elementAt(i).lastSong()) {
 		++i;
 	    }
 	    advanceToSpecificAlbumSong(band, albums.elementAt(i), songNum);
 	}	    	    
     }
 
     private void advanceToNextBandSong() {
 	Band band = m_currentBand;
 	assert band != null;
 	assert m_currentSongNumber >= band.firstSong();
 
 	int songNum = m_currentSongNumber+1;
 	if (songNum > band.lastSong()) {
 	    songNum = band.firstSong();
 	}
 
 	advanceToSpecificBandSong(band, songNum);
     }
 
     private void advanceToSpecificSong(int songNum) {
 	assert songNum >= 0;
 	assert songNum < m_numSongs;
 
 	SortedMap<Integer, Integer> headMap = m_indexToBand.headMap(songNum);
 	SortedMap<Integer, Integer> tailMap = m_indexToBand.tailMap(songNum);
 
 	int index;
 
 	// tailmap begins AFTER or AT our desired song.
 	// If it begins AT our desired song...
 	if (tailMap.size() != 0 && tailMap.firstKey() == songNum) {
 	    // ... then our band is the first one in the tail map...
 	    index = tailMap.get(tailMap.firstKey());
 	} else {
 	    // ... otherwise, our band is the last on the head map
 	    index = headMap.get(headMap.lastKey());
 	}
 	
 	Band band = m_allBands.elementAt(index);
 	advanceToSpecificBandSong(band, songNum);
     }
 
     private void advanceToNextLinearSong() {
 	if (m_albumClamp != null && m_albumClamp != "") {
 	    // Might wrap to beginning of album
 	    advanceToNextAlbumSong();
 	} else if (m_currentAlbum != null && m_currentSongNumber < m_currentAlbum.lastSong()) {
 	    advanceToNextAlbumSong();
 	} else if (m_bandClamp != null && m_bandClamp != "") {
 	    // Might wrap to beginning of band
 	    advanceToNextBandSong();
 	} else if (m_currentSongNumber < m_currentBand.lastSong()) {
 	    advanceToNextBandSong();
 	} else {
 	    int targetSong = m_currentSongNumber + 1;
 	    assert targetSong <= m_numSongs;
 	    if (targetSong == m_numSongs) {
 		targetSong = 0;
 	    }
 	    advanceToSpecificSong(targetSong);
 	}
     }
 
     transient Random m_random = new Random();
     private void advanceToRandomSong() {
         int firstValid = 0;
         int numValid = m_numSongs;
         if (m_albumClamp != null && m_albumClamp != "") {
             Album album = m_currentAlbum;
             assert (album.name().equals(m_albumClamp));  // implement random clamping later
             firstValid = album.firstSong();
             numValid = album.numSongs();
         } else if (m_bandClamp != null && m_bandClamp != "") {
             Band band = m_currentBand;
             assert (band.name().equals(m_bandClamp));  // implement random clamping later
             firstValid = band.firstSong();
             numValid = band.numSongs();
         }
 
 	advanceToSpecificSong(firstValid + m_random.nextInt(numValid));
     }
 
     /////////////////////////////////////////////////////////////////////
 
 
 }
 
