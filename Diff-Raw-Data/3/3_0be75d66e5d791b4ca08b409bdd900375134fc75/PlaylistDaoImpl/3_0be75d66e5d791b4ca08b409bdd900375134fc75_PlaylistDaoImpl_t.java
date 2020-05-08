 package org.mashupmedia.dao;
 
 import java.util.List;
 
 import org.hibernate.Query;
 import org.mashupmedia.exception.MashupMediaException;
 import org.mashupmedia.model.media.MediaItem;
 import org.mashupmedia.model.playlist.Playlist;
 import org.mashupmedia.model.playlist.Playlist.PlaylistType;
 import org.mashupmedia.model.playlist.PlaylistMediaItem;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class PlaylistDaoImpl extends BaseDaoImpl implements PlaylistDao {
 
 	@Override
 	public List<Playlist> getPlaylists() {
 		Query query = sessionFactory.getCurrentSession().createQuery("from Playlist");
 		query.setCacheable(true);
 		@SuppressWarnings("unchecked")
 		List<Playlist> playlists = (List<Playlist>) query.list();
 		return playlists;
 	}
 
 	@Override
 	public Playlist getPlaylist(long id) {
 		Query query = sessionFactory.getCurrentSession().createQuery("from Playlist where id = :id");
 		query.setCacheable(true);
 		query.setLong("id", id);
 		Playlist playlist = (Playlist) query.uniqueResult();
 		return playlist;
 	}
 
 	@Override
 	public Playlist getLastAccessedPlaylist(long userId, PlaylistType playlistType) {
 		Query query = sessionFactory
 				.getCurrentSession()
 				.createQuery(
 						"from Playlist as p where p.updatedBy.id = :userId and p.playlistTypeValue = :playlistTypeValue and p.updatedOn = (select max(tmp.updatedOn) from Playlist as tmp)");
 		query.setCacheable(true);
 		query.setLong("userId", userId);
 		query.setString("playlistTypeValue", PlaylistType.MUSIC.getValue());
 		@SuppressWarnings("unchecked")
 		List<Playlist> playlists = query.list();
 		if (playlists == null || playlists.isEmpty()) {
 			return null;
 		}
 		
 		return playlists.get(0);
 	}
 
 	@Override
 	public Playlist getDefaultPlaylistForUser(long userId, PlaylistType playlistType) {
 		Query query = sessionFactory.getCurrentSession().createQuery(
 				"from Playlist where createdBy.id = :userId and isUserDefault = true and playlistTypeValue = :playlistTypeValue");
 		query.setCacheable(true);
 		query.setLong("userId", userId);
 		query.setString("playlistTypeValue", PlaylistType.MUSIC.getValue());
 		@SuppressWarnings("unchecked")
 		List<Playlist> playlists = query.list();
 		if (playlists == null || playlists.isEmpty()) {
 			return null;
 		}
 		
 		if (playlists.size() > 1) {
 			throw new MashupMediaException("Error, more than one default playlist found for user id: " + userId);
 		}
 				
 		return playlists.get(0);
 	}
 
 	@Override
 	public void savePlaylist(Playlist playlist) {
 		long playlistId = playlist.getId();
 		deletePlaylistMediaItems(playlistId);
 		if (playlistId == 0) {
 			sessionFactory.getCurrentSession().save(playlist);
 		} else {
 			sessionFactory.getCurrentSession().merge(playlist);
 		}
 		
 		List<PlaylistMediaItem> playlistMediaItems = playlist.getPlaylistMediaItems();
 		if (playlistMediaItems == null || playlistMediaItems.isEmpty()) {
 			return;
 		}
 		
 		for (PlaylistMediaItem playlistMediaItem : playlistMediaItems) {
 			playlistMediaItem.setId(0);
 			saveOrUpdate(playlistMediaItem);
 		}
 	}
 
 	protected void deletePlaylistMediaItems(long playlistId) {
 		if (playlistId < 1) {
 			return;
 		}
 		
 		Query query = sessionFactory.getCurrentSession().createQuery("from PlaylistMediaItem where playlist.id = :playlistId");
 		query.setLong("playlistId", playlistId);
 		query.setCacheable(true);
 		@SuppressWarnings("unchecked")
 		List<PlaylistMediaItem> playlistMediaItems = query.list();
 		if (playlistMediaItems == null || playlistMediaItems.isEmpty()) {
 			return;
 		}
 
 		int deletedItems = playlistMediaItems.size();
 		
 		for (PlaylistMediaItem playlistMediaItem : playlistMediaItems) {
 			sessionFactory.getCurrentSession().delete(playlistMediaItem);
 		}
 						
 		logger.info("Deleted " + deletedItems + " playlistMediaItems for playlist id: " + playlistId);
 	}
 
 	@Override
 	public List<Playlist> getPlaylists(long userId, PlaylistType playlistType) {
 		StringBuilder hqlBuilder  = new StringBuilder();
 		hqlBuilder.append("from Playlist");
 		hqlBuilder.append(" where createdBy.id = :userId ");
 		hqlBuilder.append(" and playlistTypeValue = :playlistType");
 				
 		Query query = sessionFactory.getCurrentSession().createQuery(hqlBuilder.toString());
 		query.setCacheable(true);
 		query.setLong("userId", userId);
 		query.setString("playlistType", playlistType.getValue());
 		@SuppressWarnings("unchecked")
 		List<Playlist> playlists = (List<Playlist>) query.list();
 		return playlists;
 	}
 
 	@Override
 	public void deletePlaylist(Playlist playlist) {
		Query query = sessionFactory.getCurrentSession().createQuery("delete PlaylistMediaItem pmi where pmi.playlist.id = :playlistId");
		query.setLong("playlistId", playlist.getId());
		query.executeUpdate();		
 		sessionFactory.getCurrentSession().delete(playlist);
 	}
 
 	@Override
 	public void deletePlaylistMediaItems(List<? extends MediaItem> mediaItems) {
 		for (MediaItem mediaItem : mediaItems) {
 			long mediaItemId = mediaItem.getId();
 			Query query = sessionFactory.getCurrentSession().createQuery("delete PlaylistMediaItem pmi where pmi.mediaItem.id = :mediaItemId");
 			query.setLong("mediaItemId", mediaItemId);
 			int deletedItems = query.executeUpdate();
 			logger.info("Deleted " + deletedItems + " playlistMediaItems");
 		}
 	}
 
 	@Override
 	public void deleteLibrary(long libraryId) {
 		Query query = sessionFactory.getCurrentSession().createQuery("from PlaylistMediaItem pmi where pmi.mediaItem.library.id = :libraryId");
 		query.setLong("libraryId", libraryId);		
 		@SuppressWarnings("unchecked")
 		List<PlaylistMediaItem> playlistMediaItems = query.list();
 		int deletedItems = playlistMediaItems.size();
 		for (PlaylistMediaItem playlistMediaItem : playlistMediaItems) {
 			sessionFactory.getCurrentSession().delete(playlistMediaItem);
 		}
 		logger.info("Deleted " + deletedItems + " playlistMediaItems");
 		
 //		Query query = sessionFactory.getCurrentSession().createQuery("delete PlaylistMediaItem pmi where pmi.mediaItem.library.id = :libraryId");
 //		query.setLong("libraryId", libraryId);
 //		int deletedItems = query.executeUpdate();
 //		logger.info("Deleted " + deletedItems + " playlistMediaItems");
 		
 	}
 	
 	// protected void deleteMediaItem(Playlist playlist, long mediaItemId) {
 	// List<PlaylistMediaItem> playlistMediaItems =
 	// playlist.getPlaylistMediaItems();
 	// if (playlistMediaItems == null || playlistMediaItems.isEmpty()) {
 	// return;
 	// }
 	//
 	// List<PlaylistMediaItem> playlistMediaItemsToDelete = new
 	// ArrayList<PlaylistMediaItem>();
 	//
 	// for (PlaylistMediaItem playlistMediaItem : playlistMediaItems) {
 	// MediaItem mediaItem = playlistMediaItem.getMediaItem();
 	// if (mediaItem.getId() == mediaItemId) {
 	// playlistMediaItemsToDelete.add(playlistMediaItem);
 	// }
 	// }
 	//
 	// if (playlistMediaItems.isEmpty()) {
 	// return;
 	// }
 	//
 	// playlistMediaItems.removeAll(playlistMediaItemsToDelete);
 	// sessionFactory.getCurrentSession().merge(playlist);
 	// }
 
 	// @Override
 	// public Playlist getPlaylistFromPlaylistMediaItemId(long
 	// playlistMediaItemId) {
 	// Query query = sessionFactory
 	// .getCurrentSession()
 	// .createQuery(
 	// "select playlist from Playlist playlist inner join playlist.playlistMediaItems pmi where pmi.id = :playlistMediaItemId");
 	// query.setCacheable(true);
 	// query.setLong("playlistMediaItemId", playlistMediaItemId);
 	// Playlist musicPlaylist = (Playlist) query.uniqueResult();
 	// return musicPlaylist;
 	// }
 
 }
