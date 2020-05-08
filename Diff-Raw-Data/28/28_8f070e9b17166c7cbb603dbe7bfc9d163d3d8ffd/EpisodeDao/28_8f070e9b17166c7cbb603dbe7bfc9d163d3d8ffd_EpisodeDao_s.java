 package com.steto.diaw.dao;
 
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.List;
 
 import roboguice.util.Ln;
 
 import com.j256.ormlite.dao.BaseDaoImpl;
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.stmt.QueryBuilder;
 import com.j256.ormlite.support.ConnectionSource;
 import com.steto.diaw.model.Episode;
 import com.steto.diaw.model.Show;
 
 public class EpisodeDao extends BaseDaoImpl<Episode, Integer> {
 	
 	public EpisodeDao(ConnectionSource connection) throws SQLException {
 		super(connection, Episode.class);
 	}
 	
 	@Override
 	public List<Episode> queryForAll() throws SQLException {
 		List<Episode> episodeList = super.queryForAll();
 		Collections.sort(episodeList);
 		return episodeList;
 	}
 
 	public List<Episode> queryWithLimit(int number) throws SQLException {
 		QueryBuilder<Episode, Integer> queryBuilder = queryBuilder();
 		queryBuilder.orderBy(Episode.COLUMN_UPDATED_AT, false);
 		queryBuilder.limit(Long.valueOf(number));
 		queryBuilder.prepare();
 		return queryBuilder.query();
 	}
 
 
 	/**
 	 * @param allEp la liste des episodes à ajouter
 	 * @return le nombre d'épisode ajouté en base et pas updaté
 	 */
 	public int createOrUpdate(List<Episode> allEp) throws SQLException {
 		int nbCreated = 0;
 
 		for (Episode episode : allEp) {
 			CreateOrUpdateStatus ret = createOrUpdate(episode);
 			if (ret.isCreated()) {
 				nbCreated++;
 			} else {
 				Ln.d("episodeDAO", "Episode : " + episode.getShowName() + " " + episode.getSeasonNumber() + " " + episode.getEpisodeNumber() + " déjà present en base");
 			}
 		}
 		return nbCreated;
 	}
 
 	public Show getShowFromEpisode(Episode ep) {
 		ShowDao myDAO = null;
 		Show associated = new Show(ep.getShowName());
 		try {
 			myDAO = DaoManager.createDao(getConnectionSource(), Show.class);
 			List<Show> allShow = myDAO.queryForAll();
 			for (Show show : allShow) {
 				if (show.equals(associated)) {
 					return show;
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return associated;
 	}
 }
