 /*
  *      Copyright (C) 2005-2009 Team XBMC
  *      http://xbmc.org
  *
  *  This Program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2, or (at your option)
  *  any later version.
  *
  *  This Program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with XBMC Remote; see the file license.  If not, write to
  *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
  *  http://www.gnu.org/copyleft/gpl.html
  *
  */
 
 package org.xbmc.httpapi.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import org.xbmc.httpapi.Connection;
 import org.xbmc.httpapi.client.ControlClient.PlayStatus;
 import org.xbmc.httpapi.data.Album;
 import org.xbmc.httpapi.data.Artist;
 import org.xbmc.httpapi.data.Genre;
 import org.xbmc.httpapi.data.ICoverArt;
 import org.xbmc.httpapi.data.Song;
 import org.xbmc.httpapi.type.MediaType;
 
 import android.util.Log;
 
 /**
  * Takes care of every music related stuff, notably the music database.
  * 
  * @author Team XBMC
  */
 public class MusicClient {
 	
 	// those are the musicdb://n/ keys.
 	public static final int MUSICDB_GENRE           = 1;
 	public static final int MUSICDB_ARTIST          = 2;
 	public static final int MUSICDB_ALBUM           = 3;
 	public static final int MUSICDB_SONG            = 4;
 	public static final int MUSICDB_TOP100          = 5;
 	public static final int MUSICDB_RECENTLY_ADDED  = 6;
 	public static final int MUSICDB_RECENTLY_PLAYED = 7;
 	public static final int MUSICDB_COMPILATION     = 8;
 	public static final int MUSICDB_YEARS           = 9;
 	
 	public static final String PLAYLIST_ID = "0";
 	
 	private final Connection mConnection;
 
 	/**
 	 * Class constructor needs reference to HTTP client connection
 	 * @param connection
 	 */
 	public MusicClient(Connection connection) {
 		mConnection = connection;
 	}
 	
 	/**
 	 * Adds an album to the current playlist.
 	 * @param album
 	 * @return first song of the album
 	 */
 	public Song addToPlaylist(Album album) {
 		return addToPlaylist(getSongs(album));
 	}
 
 	/**
 	 * Adds all songs from an artist to the current playlist.
 	 * @param artist
 	 * @return first song of all added songs
 	 */
 	public Song addToPlaylist(Artist artist) {
 		return addToPlaylist(getSongs(artist));
 	}
 
 	/**
 	 * Adds all songs from a genre to the current playlist.
 	 * @param genre
 	 * @return first song of all added songs
 	 */
 	public Song addToPlaylist(Genre genre) {
 		return addToPlaylist(getSongs(genre));
 	}
 
 	/**
 	 * Adds songs of a genre from an artist to the current playlist.
 	 * @param artist
 	 * @return first song of all added songs
 	 */
 	public Song addToPlaylist(Artist artist, Genre genre) {
 		return addToPlaylist(getSongs(artist, genre));
 	}
 	
 	/**
 	 * Adds a list of songs to the current playlist.
 	 * @param artist
 	 * @return first song of all added songs
 	 */
 	public Song addToPlaylist(ArrayList<Song> songs) {
 		Song firstSong = null;
 		for (Song song : songs) {
 			if (firstSong == null) {
 				firstSong = song;
 			}
 			addToPlaylist(song);
 		}
 		return firstSong;
 	}
 	
 	/**
 	 * Adds a song to the current playlist.
 	 * @param song
 	 * @return true on success, false otherwise.
 	 */
 	public boolean addToPlaylist(Song song) {
 		return mConnection.getBoolean("AddToPlayList", song.path + ";" + PLAYLIST_ID);
 	}
 	
 	/**
 	 * Returns how many items are in the playlist.
 	 * @return
 	 */
 	public int getPlaylistSize() {
 		return mConnection.getInt("GetPlaylistLength", PLAYLIST_ID);
 	}
 	
 	/**
 	 * Clears current playlist
 	 * @return true on success, false otherwise.
 	 */
 	public boolean clearPlaylist() {
 		return mConnection.getBoolean("ClearPlayList", PLAYLIST_ID);
 	}
 	
 	/**
 	 * Plays an album. Playlist is previously cleared.
 	 * @param album
 	 * @return true on success, false otherwise.
 	 */
 	public boolean play(Album album) {
 		return play(getSongs(album));
 	}
 	
 	/**
 	 * Plays all songs of a genre. Playlist is previously cleared.
 	 * @param genre
 	 * @return true on success, false otherwise.
 	 */
 	public boolean play(Genre genre) {
 		return play(getSongs(genre));
 	}
 	
 	/**
 	 * Plays all songs from an artist. Playlist is previously cleared.
 	 * @param artist
 	 * @return true on success, false otherwise.
 	 */
 	public boolean play(Artist artist) {
 		return play(getSongs(artist));
 	}
 	
 	/**
 	 * Plays songs of a genre from an artist. Playlist is previously cleared.
 	 * @param artist
 	 * @param genre
 	 * @return true on success, false otherwise.
 	 */
 	public boolean play(Artist artist, Genre genre) {
 		return play(getSongs(artist, genre));
 	}
 
 	
 	/**
 	 * Plays all songs of this array.
 	 * @param artist
 	 * @param genre
 	 * @return true on success, false otherwise.
 	 */
 	public boolean play(ArrayList<Song> songs) {
 		clearPlaylist();
 		for (Song song : songs) {
 			addToPlaylist(song);
 		}
 		setCurrentPlaylist();
 		return playlistNext();
 	}
 	
 	/**
 	 * Adds a song to the current playlist and plays it.
 	 * @param song
 	 * @return true on success, false otherwise.
 	 */
 	public boolean play(Song song) {
 		return mConnection.getBoolean("PlayFile", song.path + ";" + PLAYLIST_ID);
 	}
 	
 	
 	/**
 	 * Starts playing the next media in the current playlist. 
 	 * @return true on success, false otherwise.
 	 */
 	public boolean playlistNext() {
 		return mConnection.getBoolean("PlayNext");
 	}
 	
 	/**
 	 * Sets the media at playlist position position to be the next item to be 
 	 * played. Position starts at 0, so SetPlaylistSong(5) sets the position
 	 * to the 6th song in the playlist.
 	 * @param pos Position
 	 * @return true on success, false otherwise.
 	 */
 	public boolean playlistSetSong(int pos) {
 		return mConnection.getBoolean("SetPlaylistSong", String.valueOf(pos));
 	}
 	
 	/**
 	 * Sets current playlist to "0"
 	 * @return true on success, false otherwise.
 	 */
 	public boolean setCurrentPlaylist() {
 		return mConnection.getBoolean("SetCurrentPlaylist", PLAYLIST_ID);
 	}
 	
 	/**
 	 * Gets all allbums with given artist IDs
 	 * @param artistIDs Array of artist IDs
 	 * @return All compilation albums
 	 */
 	public ArrayList<Album> getAlbums(ArrayList<Integer> artistIDs) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT idAlbum, strAlbum, strArtist, iYear, strThumb");
 		sb.append(" FROM albumview WHERE albumview.strAlbum <> ''");
 		sb.append(" AND idArtist IN (");
 		int n = 0;
 		for (Integer id : artistIDs) {
 			sb.append(id);
 			n++;
 			if (artistIDs.size() < n) {
 				sb.append(", ");
 			}
 		}
 		sb.append(")");
 		return parseAlbums(mConnection.query("QueryMusicDatabase", sb.toString()));
 	}
 	
 	/**
 	 * Gets all albums from database
 	 * @return All albums
 	 */
 	public ArrayList<Album> getAlbums() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT idAlbum, strAlbum, strArtist, iYear, strThumb");
 		sb.append(" FROM albumview WHERE albumview.strAlbum <> ''");
		sb.append(" ORDER BY upper(strAlbum), strAlbum");
 		
 /*		sb.append("SELECT a.idAlbum, a.strAlbum, i.strArtist, a.iYear, (");
 		sb.append("   SELECT p.strPath");
 		sb.append("   FROM song AS s, path AS p");
 		sb.append("   WHERE s.idPath = p.idPath");
 		sb.append("   AND s.idAlbum = a.idAlbum");
 		sb.append("   LIMIT 1");
 		sb.append("  )");
 		sb.append("  FROM album AS a, artist AS i");
 		sb.append("  WHERE a.idArtist = i.idArtist");
 		sb.append("  ORDER BY i.strArtist ASC");
 //		sb.append("  LIMIT 300"); // let's keep it at 300 for now*/
 		return parseAlbums(mConnection.query("QueryMusicDatabase", sb.toString()));
 	}
 
 	/**
 	 * Gets all albums of an artist from database
 	 * @return Albums with an artist
 	 */
 	public ArrayList<Album> getAlbums(Artist artist) {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("SELECT idAlbum, strAlbum, strArtist, iYear, strThumb");
 		sb.append(" FROM albumview");
 		sb.append(" WHERE albumview.strAlbum <> ''");
 		sb.append(" AND idArtist = " + artist.id);
		sb.append(" ORDER BY upper(strAlbum), strAlbum");
 		
 /*		sb.append("SELECT DISTINCT a.idAlbum, a.strAlbum, i.strArtist, a.iYear, (");
 		sb.append("   SELECT p.strPath");
 		sb.append("   FROM song AS s, path AS p");
 		sb.append("   WHERE s.idPath = p.idPath");
 		sb.append("   AND s.idAlbum = a.idAlbum");
 		sb.append("   LIMIT 1");
 		sb.append("  )");
 		sb.append("  FROM song AS s, album AS a, artist as i");
 		sb.append("  WHERE s.idArtist = " + artist.id);
 		sb.append("  AND s.idAlbum = a.idAlbum");
 		sb.append("  AND i.idArtist = s.idArtist");
 		sb.append("  ORDER BY a.strAlbum ASC");*/
 		return parseAlbums(mConnection.query("QueryMusicDatabase", sb.toString()));
 	}
 
 	/**
 	 * Gets all albums of with at least one song in a genre
 	 * @return Albums of a genre
 	 */
 	public ArrayList<Album> getAlbums(Genre genre) {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("SELECT idAlbum, strAlbum, strArtist, iYear, strThumb");
 		sb.append("  FROM albumview");
 		sb.append("  WHERE albumview.strAlbum <> ''");
 		sb.append("  AND (idAlbum IN ("); 
 		sb.append("        SELECT song.idAlbum FROM song"); 			
 		sb.append("        JOIN exgenresong ON song.idSong = exgenresong.idSong"); 			
 		sb.append("        WHERE exgenresong.idGenre =  " + genre.id);
 		sb.append("  ) OR idAlbum IN (");
 		sb.append("        SELECT DISTINCT idAlbum");
 		sb.append("        FROM song");
 		sb.append("        WHERE idGenre = " + genre.id);
 		sb.append("  ))");
		sb.append(" ORDER BY upper(strAlbum), strAlbum");
 		
 /*		sb.append("SELECT DISTINCT alb.idAlbum, alb.strAlbum, art.strArtist, alb.iYear, (");
 		sb.append("   SELECT p.strPath");
 		sb.append("   FROM song AS s, path AS p");
 		sb.append("   WHERE s.idPath = p.idPath");
 		sb.append("   AND s.idAlbum = alb.idAlbum");
 		sb.append("   LIMIT 1");
 		sb.append("  )");
 		sb.append("  FROM artist as art, album as alb");
 		sb.append("  WHERE art.idArtist = alb.idArtist");
 		sb.append("  AND (alb.idAlbum IN (");
 		sb.append("        SELECT DISTINCT s.idAlbum");
 		sb.append("        FROM exgenresong AS g, song AS s");
 		sb.append("        WHERE g.idGenre = " + genre.id);
 		sb.append("        AND g.idSong = s.idSong");
 		sb.append("  ) OR alb.idAlbum IN (");
 		sb.append("        SELECT DISTINCT idAlbum");
 		sb.append("        FROM song");
 		sb.append("        WHERE idGenre = " + genre.id);
 		sb.append("  ))");
 		sb.append("  ORDER BY alb.strAlbum");*/
 		return parseAlbums(mConnection.query("QueryMusicDatabase", sb.toString()));
 	}
 	
 	/**
 	 * Gets all albums from database
 	 * @param albumArtistsOnly If set to true, hide artists who appear only on compilations.
 	 * @return All albums
 	 */
 	public ArrayList<Artist> getArtists(boolean albumArtistsOnly) {
 		
 		StringBuilder sb = new StringBuilder();
 		if (albumArtistsOnly) {
 			sb.append("SELECT idArtist, strArtist ");
 			sb.append("  FROM artist");
 			sb.append("  WHERE (");
 			sb.append("    idArtist IN (");
 			sb.append("      SELECT album.idArtist");
 			sb.append("      FROM album");
 			sb.append("    ) OR idArtist IN (");
 			sb.append("      SELECT exartistalbum.idArtist");
 			sb.append("      FROM exartistalbum");
 			sb.append("      JOIN album ON album.idAlbum = exartistalbum.idAlbum");
 			sb.append("      WHERE album.strExtraArtists != ''");
 			sb.append("    )");
 			sb.append(") AND artist.strArtist != ''");
 		} else {
			sb.append("SELECT idArtist, strArtist FROM artist");
 		}
		sb.append(" ORDER BY upper(strArtist), strArtist");
 		return parseArtists(mConnection.query("QueryMusicDatabase", sb.toString()));
 	}
 
 	/**
 	 * Gets all artists with at least one song of a genre.
 	 * @return Albums with a genre
 	 */
 	public ArrayList<Artist> getArtists(Genre genre, boolean albumArtistsOnly) {
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT DISTINCT idArtist, strArtist ");
 		sb.append("  FROM artist");
 		sb.append("  WHERE (idArtist IN (");
 		sb.append("    SELECT DISTINCT s.idArtist");
 		sb.append("    FROM exgenresong AS g, song AS s");
 		sb.append("    WHERE g.idGenre = " + genre.id);
 		sb.append("    AND g.idSong = s.idSong");
 		sb.append("  ) OR idArtist IN (");
 		sb.append("    SELECT DISTINCT idArtist");
 		sb.append("     FROM song");
 		sb.append("     WHERE idGenre = " + genre.id);
 		sb.append("  ))");
 
 		if (albumArtistsOnly) {
 			sb.append("  AND (");
 			sb.append("    idArtist IN (");
 			sb.append("      SELECT album.idArtist");
 			sb.append("      FROM album");
 			sb.append("    ) OR idArtist IN (");
 			sb.append("      SELECT exartistalbum.idArtist");
 			sb.append("      FROM exartistalbum");
 			sb.append("      JOIN album ON album.idAlbum = exartistalbum.idAlbum");
 			sb.append("      WHERE album.strExtraArtists != ''");
 			sb.append("    )");
 			sb.append("  ) AND artist.strArtist != ''");
 		}		
		sb.append(" ORDER BY upper(strArtist), strArtist");
 		return parseArtists(mConnection.query("QueryMusicDatabase", sb.toString()));
 	}
 	
 	/**
 	 * Gets all genres from database
 	 * @return All genres
 	 */
 	public ArrayList<Genre> getGenres() {
		return parseGenres(mConnection.query("QueryMusicDatabase", "SELECT idGenre, strGenre FROM genre ORDER BY upper(strGenre), strGenre"));
 	}
 	
 	/**
 	 * Updates the album object with additional data from the albuminfo table
 	 * @param album
 	 * @return Updated album
 	 */
 	public Album updateAlbumInfo(Album album) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT g.strGenre, a.strExtraGenres, ai.strLabel, ai.iRating");
 		sb.append("  FROM album a, genre g");
 		sb.append("  LEFT JOIN albuminfo AS ai ON ai.idAlbumInfo = a.idAlbum");
 		sb.append("  WHERE a.idGenre = g.idGenre");
 		sb.append("  AND a.idAlbum = " + album.id);
 		return parseAlbumInfo(album, mConnection.query("QueryMusicDatabase", sb.toString()));
 	}
 	
 	/**
 	 * Returns a list containing all tracks of an album. The list is sorted by filename.
 	 * @param album
 	 * @return Tracks of an album
 	 */
 	public ArrayList<Song> getSongs(Album album) {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("SELECT idSong, strTitle, strArtist, strAlbum, iTrack, iDuration, strPath, strFileName, strThumb");
 		sb.append("  FROM songview");
 		sb.append("  WHERE idAlbum = " + album.id);
		sb.append(" ORDER BY iTrack, strFileName");
 		
 /*		sb.append("SELECT s.idSong, s.strTitle, art.strArtist, alb.strAlbum, albArtist.strArtist AS albumArtist, s.iTrack, s.iDuration, p.strPath, s.strFileName");
 		sb.append("  FROM song AS s, path AS p, artist AS art, album AS alb, artist as albArtist");
 		sb.append("  WHERE s.idPath = p.idPath");
 		sb.append("  AND s.idArtist = art.idArtist");
 		sb.append("  AND s.idAlbum = alb.idAlbum");
 		sb.append("  AND albArtist.idArtist = alb.idArtist");
 		sb.append("  AND s.idAlbum = " + album.id);
 		sb.append("  ORDER BY s.iTrack, s.strFileName");*/
 		return parseSongs(mConnection.query("QueryMusicDatabase", sb.toString()));
 	}
 	
 	/**
 	 * Returns a list containing all tracks of an artist. The list is sorted by album name, filename.
 	 * @param artist Artist
 	 * @return All tracks of the artist
 	 */
 	public ArrayList<Song> getSongs(Artist artist) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT idSong, strTitle, strArtist, strAlbum, iTrack, iDuration, strPath, strFileName, strThumb ");
 		sb.append("  FROM songview");
 		sb.append("  WHERE (");
 		sb.append("    idArtist = " + artist.id);
 		sb.append("    OR idSong IN (");
 		sb.append("       SELECT exartistsong.idSong");
 		sb.append("       FROM exartistsong");
 		sb.append("       WHERE exartistsong.idArtist = " + artist.id);
 		sb.append("    ) OR idSong IN (");
 		sb.append("       SELECT song.idSong");
 		sb.append("       FROM song");
 		sb.append("       JOIN album ON song.idAlbum = album.idAlbum");
 		sb.append("       WHERE album.idArtist = " + artist.id);
 		sb.append("    ) OR idSong IN (");
 		sb.append("       SELECT song.idSong");
 		sb.append("       FROM song");
 		sb.append("       JOIN exartistalbum ON song.idAlbum = exartistalbum.idAlbum");
 		sb.append("       JOIN album ON song.idAlbum = album.idAlbum");
 		sb.append("       WHERE exartistalbum.idArtist = " + artist.id);
 		sb.append("       AND album.strExtraArtists != ''");
 		sb.append("    )");
 		sb.append("  )");
 		
 /*		sb.append("SELECT s.idSong, s.strTitle, art.StrArtist, alb.strAlbum, albArtist.strArtist AS albumArtist, s.iTrack, s.iDuration, p.strPath, s.strFileName");
 		sb.append("  FROM song AS s, path AS p, artist art, album AS alb, artist as albArtist");
 		sb.append("  WHERE s.idPath = p.idPath");
 		sb.append("  AND s.idArtist = art.idArtist");
 		sb.append("  AND s.idAlbum = alb.idAlbum");
 		sb.append("  AND albArtist.idArtist = alb.idArtist");
 		sb.append("  AND s.idArtist = " + artist.id);
 		sb.append("  ORDER BY alb.strAlbum, s.iTrack, s.strFileName");*/
 		return parseSongs(mConnection.query("QueryMusicDatabase", sb.toString()));
 	}
 	
 	/**
 	 * Returns a list containing all tracks of a genre. The list is sorted by artist, album name, filename.
 	 * @param genre Genre
 	 * @return All tracks of the genre
 	 */
 	public ArrayList<Song> getSongs(Genre genre) {
 		StringBuilder sb = new StringBuilder();
 
 		sb.append("SELECT idSong, strTitle, strArtist, strAlbum, iTrack, iDuration, strPath, strFileName, strThumb");
 		sb.append("  FROM songview");
 		sb.append("  WHERE idGenre = " + genre.id);
 		sb.append("  OR idSong IN (");
 		sb.append("     SELECT exgenresong.idSong FROM exgenresong WHERE exgenresong.idGenre = " + genre.id);
 		sb.append("  )");
 		
 /*		sb.append("SELECT s.idSong, s.strTitle, art.StrArtist, alb.strAlbum, albArtist.strArtist AS albumArtist, s.iTrack, s.iDuration, p.strPath, s.strFileName");
 		sb.append("  FROM song AS s, path AS p, artist art, album AS alb, artist as albArtist");
 		sb.append("  WHERE s.idPath = p.idPath");
 		sb.append("  AND s.idArtist = art.idArtist");
 		sb.append("  AND s.idAlbum = alb.idAlbum");
 		sb.append("  AND albArtist.idArtist = alb.idArtist");
 		sb.append("  AND s.idGenre = " + genre.id);
 		sb.append("  ORDER BY art.StrArtist, alb.strAlbum, s.iTrack, s.strFileName");*/
 		return parseSongs(mConnection.query("QueryMusicDatabase", sb.toString()));
 	}
 	
 	/**
 	 * Returns a list containing all tracks of a genre AND and artist. The list is sorted by 
 	 * artist, album name, filename.
 	 * @param genre Genre
 	 * @return All tracks of the genre
 	 */
 	public ArrayList<Song> getSongs(Artist artist, Genre genre) {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("SELECT idSong, strTitle, strArtist, strAlbum, iTrack, iDuration, strPath, strFileName, strThumb ");
 		sb.append("  FROM songview");
 		sb.append("  WHERE (");
 		sb.append("    idArtist = " + artist.id);
 		sb.append("    OR idSong IN (");
 		sb.append("       SELECT exartistsong.idSong");
 		sb.append("       FROM exartistsong");
 		sb.append("       WHERE exartistsong.idArtist = " + artist.id);
 		sb.append("    ) OR idSong IN (");
 		sb.append("       SELECT song.idSong");
 		sb.append("       FROM song");
 		sb.append("       JOIN album ON song.idAlbum = album.idAlbum");
 		sb.append("       WHERE album.idArtist = " + artist.id);
 		sb.append("    ) OR idSong IN (");
 		sb.append("       SELECT song.idSong");
 		sb.append("       FROM song");
 		sb.append("       JOIN exartistalbum ON song.idAlbum = exartistalbum.idAlbum");
 		sb.append("       JOIN album ON song.idAlbum = album.idAlbum");
 		sb.append("       WHERE exartistalbum.idArtist = " + artist.id);
 		sb.append("       AND album.strExtraArtists != ''");
 		sb.append("    )");
 		sb.append("  ) AND (");
 		sb.append("    idGenre = " + genre.id);
 		sb.append("    OR idSong IN (");
 		sb.append("       SELECT exgenresong.idSong FROM exgenresong WHERE exgenresong.idGenre = " + genre.id);
 		sb.append("    )");
 		sb.append("  )");
 		
 		/*
 		sb.append("SELECT s.idSong, s.strTitle, art.StrArtist, alb.strAlbum, albArtist.strArtist AS albumArtist, s.iTrack, s.iDuration, p.strPath, s.strFileName");
 		sb.append("  FROM song AS s, path AS p, artist art, album AS alb, artist as albArtist");
 		sb.append("  WHERE s.idPath = p.idPath");
 		sb.append("  AND s.idArtist = art.idArtist");
 		sb.append("  AND s.idAlbum = alb.idAlbum");
 		sb.append("  AND albArtist.idArtist = alb.idArtist");
 		sb.append("  AND s.idGenre = " + genre.id);
 		sb.append("  AND s.idArtist = " + artist.id);
 		sb.append("  ORDER BY art.StrArtist, alb.strAlbum, s.iTrack, s.strFileName");*/
 		return parseSongs(mConnection.query("QueryMusicDatabase", sb.toString()));
 	}
 	
 	/**
 	 * Returns album thumbnail as base64-encoded string
 	 * @param album
 	 * @return Base64-encoded content of thumb
 	 */
 	public String getAlbumThumb(ICoverArt art) {
 		final String data = mConnection.query("FileDownload", Album.getThumbUri(art));
 		if (data.length() > 0) {
 			return data;
 		} else {
 			Log.i("MusicClient", "*** Downloaded cover has size null, retrying with fallback:");
 			return mConnection.query("FileDownload", Album.getFallbackThumbUri(art));
 		}
 	}
 	
 	/**
 	 * Returns a list containing all artist IDs that stand for "compilation".
 	 * Best case scenario would be only one ID for "Various Artists", though
 	 * there are also just "V.A." or "VA" naming conventions.
 	 * @return List of compilation artist IDs
 	 */
 	public ArrayList<Integer> getCompilationArtistIDs() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT idArtist");
 		sb.append("  FROM artist");
 		sb.append("  WHERE lower(strArtist) LIKE 'various artists%'");
 		sb.append("  OR lower(strArtist) LIKE 'v.a.%'");
 		sb.append("  OR lower(strArtist) = 'va'");
 		return parseIntArray(mConnection.query("QueryMusicDatabase", sb.toString()));
 		
 	}
 
 	/**
 	 * Converts query response from HTTP API to a list of Album objects. Each
 	 * row must return the following attributes in the following order:
 	 * <ol>
 	 * 	<li><code>idAlbum</code></li>
 	 * 	<li><code>strAlbum</code></li>
 	 * 	<li><code>strArtist</code></li>
 	 * 	<li><code>path to album</code></li>
 	 * </ol>
 	 * @param response
 	 * @return List of albums
 	 */
 	private ArrayList<Album> parseAlbums(String response) {
 		ArrayList<Album> albums = new ArrayList<Album>();
 		String[] fields = response.split("<field>");
 		try {
 			for (int row = 1; row < fields.length; row += 5) {
 				albums.add(new Album(
 						Connection.trimInt(fields[row]), 
 						Connection.trim(fields[row + 1]), 
 						Connection.trim(fields[row + 2]),
 						Connection.trimInt(fields[row + 3]),
 						Connection.trim(fields[row + 4])
 				));
 			}
 		} catch (Exception e) {
 			System.err.println("ERROR: " + e.getMessage());
 			System.err.println("response = " + response);
 			e.printStackTrace();
 		}
 		return albums;
 	}
 	
 	/**
 	 * Updates an album with info from HTTP API query response. One row is 
 	 * expected, with the following columns:
 	 * <ol>
 	 * 	<li><code>strGenre</code></li>
 	 * 	<li><code>strExtraGenres</code></li>
 	 * 	<li><code>iYear</code></li>
 	 * 	<li><code>strLabel</code></li>
 	 * 	<li><code>iRating</code></li>
 	 * </ol>  
 	 * @param album
 	 * @param response
 	 * @return Updated album
 	 */
 	private Album parseAlbumInfo(Album album, String response) {
 		String[] fields = response.split("<field>");
 		try {
 			if (Connection.trim(fields[2]).length() > 0) {
 				album.genres = Connection.trim(fields[1]) + Connection.trim(fields[2]);
 			}	
 			if (Connection.trim(fields[3]).length() > 0) {
 				album.label = Connection.trim(fields[4]);
 			}
 			if (Connection.trim(fields[4]).length() > 0) {
 				album.rating = Connection.trimInt(fields[5]);
 			}
 		} catch (Exception e) {
 			System.err.println("ERROR: " + e.getMessage());
 			System.err.println("response = " + response);
 			e.printStackTrace();
 		}
 		return album;
 	}
 	
 	/**
 	 * Converts query response from HTTP API to a list of Song objects. Each
 	 * row must return the following columns in the following order:
 	 * <ol>
 	 * 	<li><code>strTitle</code></li>
 	 * 	<li><code>strArtist</code></li>
 	 * 	<li><code>iTrack</code></li>
 	 * 	<li><code>iDuration</code></li>
 	 * 	<li><code>strPath</code></li>
 	 * 	<li><code>strFileName</code></li>
 	 * </ol>
 	 * @param response
 	 * @return List of Songs
 	 */
 	private ArrayList<Song> parseSongs(String response) {
 		ArrayList<Song> songs = new ArrayList<Song>();
 		String[] fields = response.split("<field>");
 		try { 
 			for (int row = 1; row < fields.length; row += 9) { 
 				songs.add(new Song( // int id, String title, String artist, String album, int track, int duration, String path, String filename, String thumbPath
 						Connection.trimInt(fields[row]),
 						Connection.trim(fields[row + 1]), 
 						Connection.trim(fields[row + 2]), 
 						Connection.trim(fields[row + 3]), 
 						Connection.trimInt(fields[row + 4]), 
 						Connection.trimInt(fields[row + 5]), 
 						Connection.trim(fields[row + 6]),
 						Connection.trim(fields[row + 7]), 
 						Connection.trim(fields[row + 8]) 
 				));
 			}
 		} catch (Exception e) {
 			System.err.println("ERROR: " + e.getMessage());
 			System.err.println("response = " + response);
 			e.printStackTrace();
 		}
 		Collections.sort(songs);
 		return songs;		
 	}
 	
 	/**
 	 * Converts query response from HTTP API to a list of integer values.
 	 * @param response
 	 * @return
 	 */
 	private ArrayList<Integer> parseIntArray(String response) {
 		ArrayList<Integer> array = new ArrayList<Integer>();
 		String[] fields = response.split("<field>");
 		try { 
 			for (int row = 1; row < fields.length; row += 9) {
 				array.add(Connection.trimInt(fields[row]));
 			}
 		} catch (Exception e) {
 			System.err.println("ERROR: " + e.getMessage());
 			System.err.println("response = " + response);
 			e.printStackTrace();
 		}
 		return array;
 	}
 	
 	/**
 	 * Converts query response from HTTP API to a list of Artist objects. Each
 	 * row must return the following columns in the following order:
 	 * <ol>
 	 * 	<li><code>idArtist</code></li>
 	 * 	<li><code>strArtist</code></li>
 	 * </ol>
 	 * @param response
 	 * @return List of Artists
 	 */
 	private ArrayList<Artist> parseArtists(String response) {
 		ArrayList<Artist> artists = new ArrayList<Artist>();
 		String[] fields = response.split("<field>");
 		try { 
 			for (int row = 1; row < fields.length; row += 2) { 
 				artists.add(new Artist(
 						Connection.trimInt(fields[row]), 
 						Connection.trim(fields[row + 1])
 				));
 			}
 		} catch (Exception e) {
 			System.err.println("ERROR: " + e.getMessage());
 			System.err.println("response = " + response);
 			e.printStackTrace();
 		}
 		return artists;		
 	}
 	
 	/**
 	 * Converts query response from HTTP API to a list of Genre objects. Each
 	 * row must return the following columns in the following order:
 	 * <ol>
 	 * 	<li><code>idGenre</code></li>
 	 * 	<li><code>strGenre</code></li>
 	 * </ol>
 	 * @param response
 	 * @return List of Genres
 	 */
 	private ArrayList<Genre> parseGenres(String response) {
 		ArrayList<Genre> genres = new ArrayList<Genre>();
 		String[] fields = response.split("<field>");
 		try { 
 			for (int row = 1; row < fields.length; row += 2) { 
 				genres.add(new Genre(
 						Connection.trimInt(fields[row]), 
 						Connection.trim(fields[row + 1])
 				));
 			}
 		} catch (Exception e) {
 			System.err.println("ERROR: " + e.getMessage());
 			System.err.println("response = " + response);
 			e.printStackTrace();
 		}
 		return genres;		
 	}
 	
 	public static ControlClient.ICurrentlyPlaying getCurrentlyPlaying(final HashMap<String, String> map) {
 		return new ControlClient.ICurrentlyPlaying() {
 			private static final long serialVersionUID = 5036994329211476713L;
 			public String getTitle() {
 				return map.get("Title");
 			}
 			public int getTime() {
 				return parseTime(map.get("Time"));
 			}
 			public PlayStatus getPlayStatus() {
 				return PlayStatus.parse(map.get("PlayStatus"));
 			}
 			public float getPercentage() {
 				return Float.valueOf(map.get("Percentage"));
 			}
 			public String getFilename() {
 				return map.get("Filename");
 			}
 			public int getDuration() {
 				return parseTime(map.get("Duration"));
 			}
 			public String getArtist() {
 				return map.get("Artist");
 			}
 			public String getAlbum() {
 				return map.get("Album");
 			}
 			public MediaType getType() {
 				return MediaType.music;
 			}
 			public boolean isPlaying() {
 				return PlayStatus.parse(map.get("PlayStatus")).equals(PlayStatus.Playing);
 			}
 			private int parseTime(String time) {
 				String[] s = time.split(":");
 				if (s.length == 2) {
 					return Integer.parseInt(s[0]) * 60 + Integer.parseInt(s[1]);
 				} else if (s.length == 3) {
 					return Integer.parseInt(s[0]) * 3600 + Integer.parseInt(s[1]) * 60 + Integer.parseInt(s[2]);
 				} else {
 					return 0;
 				}
 			}
 		};
 	}
 	
 }
