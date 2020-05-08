 package com.steto.diaw.dao;
 
 import android.util.Log;
 import com.j256.ormlite.dao.BaseDaoImpl;
 import com.j256.ormlite.support.ConnectionSource;
 import com.steto.diaw.model.Episode;
 
 import java.sql.SQLException;
 import java.util.List;
 
 public class EpisodeDao extends BaseDaoImpl<Episode, String> {
 
 
 	private static final String TAG = "EpisodeDao";
 
 	public EpisodeDao(ConnectionSource connectionSource) throws SQLException {
 		super(connectionSource, Episode.class);
 	}
 
 	@Override
 	public CreateOrUpdateStatus createOrUpdate(Episode data) throws SQLException {
 		List<Episode> listEpisodes = queryForAll();
 
 		if (listEpisodes.isEmpty()) {
 			Log.d(TAG, "createOrUpdate liste vide");
 			create(data);
 			return new CreateOrUpdateStatus(true, false, 0);
 		}
 
 		boolean create = true;
 		for (int i = 0; i < listEpisodes.size() && create; i++) {
 			Episode episode = listEpisodes.get(i);
 			if (episode.getShowName().equalsIgnoreCase(data.getShowName())
 					&& episode.getSeasonNumber() == data.getSeasonNumber()
 					&& episode.getEpisodeNumber() == data.getEpisodeNumber()) {
 				Log.d(TAG, "createOrUpdate episode already in base");
 				create = false;
 			}
 		}
 		if (create) {
 			create(data);
 			return new CreateOrUpdateStatus(true, false, 0);
 		} else {
 			return new CreateOrUpdateStatus(false, false, 0);
 		}
 	}
 }
