 package org.mashupmedia.dao;
 
 import java.util.List;
 
 import org.mashupmedia.model.media.MediaItem;
 import org.mashupmedia.model.playlist.Playlist;
 
 public interface PlaylistDao {
 
 	public List<Playlist> getPlaylists();
 
 	public Playlist getPlaylist(long id);
 
	public Playlist getLastAccessedMusicPlaylist(long userId);
 
 	public void savePlaylist(Playlist playlist);
 
 	public List<Playlist> getPlaylists(long userId);
 
 	public void deletePlaylist(Playlist playlist);
 
 	public Playlist getDefaultMusicPlaylistForUser(long userId);
 
 	public void deletePlaylistMediaItems(List<? extends MediaItem> mediaItems);
 
 	public void deleteLibrary(long libraryId);
 	
 
 }
