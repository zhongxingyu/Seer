 package com.steto.diaw.dao;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.j256.ormlite.dao.BaseDaoImpl;
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.stmt.PreparedQuery;
 import com.j256.ormlite.stmt.QueryBuilder;
 import com.j256.ormlite.stmt.SelectArg;
 import com.j256.ormlite.support.ConnectionSource;
 import com.steto.diaw.model.Episode;
 import com.steto.diaw.model.Season;
 import com.steto.diaw.model.Show;
 
 public class ShowDao extends BaseDaoImpl<Show, Integer> {
 
 	public ShowDao(ConnectionSource connectionSource) throws SQLException {
 		super(connectionSource, Show.class);
 	}
 
 	@Override
 	public List<Show> queryForAll() throws SQLException {
 		List<Show> showList = super.queryForAll();
 		Collections.sort(showList);
 		return showList;
 	}
 
 	public Show queryFromName(String name) throws SQLException {
 		SelectArg nameArg = new SelectArg();
 		QueryBuilder<Show, Integer> queryBuilder = queryBuilder();
 		queryBuilder.where().eq(Show.COLUMN_SHOWNAME, nameArg);
 		PreparedQuery<Show> preparedQuery = queryBuilder.prepare();
 		nameArg.setValue(name);
 		return queryForFirst(preparedQuery);
 	}
 
 	public List<Episode> getEpisodeFromShow(Show show) throws SQLException {
 		EpisodeDao episodesDao = DaoManager.createDao(getConnectionSource(), Episode.class);
		return episodesDao.queryForName(show.getShowName());
 	}
 
 	public List<Season> getSeasonsFromShow(Show show) throws SQLException {
 		List<Episode> episodes = null;
 		List<Season> seasons = new ArrayList<Season>();
 		episodes = getEpisodeFromShow(show);
 		if (episodes == null || episodes.isEmpty()) {
 			return new ArrayList<Season>();
 		}
 
 		Collections.sort(episodes, new Episode.OrderShowComparator());
 
 		Season season = new Season();
 		List<Episode> episodesToAdd = new ArrayList<Episode>();
 
 		season.setNumber(episodes.get(0).getSeasonNumber());
 		episodesToAdd.add(episodes.get(0));
 
 		for (int i = 1; i < episodes.size(); i++) {
 			if (episodes.get(i).getSeasonNumber() == season.getNumber()) {
 				episodesToAdd.add(episodes.get(i));
 			} else {
 				// change of season
 				season.setEpisodes(episodesToAdd);
 				seasons.add(season);
 
 				season = new Season();
 				episodesToAdd = new ArrayList<Episode>();
 				season.setNumber(episodes.get(i).getSeasonNumber());
 				episodesToAdd.add(episodes.get(i));
 			}
 		}
 		season.setEpisodes(episodesToAdd);
 		seasons.add(season);
 		return seasons;
 	}
 }
