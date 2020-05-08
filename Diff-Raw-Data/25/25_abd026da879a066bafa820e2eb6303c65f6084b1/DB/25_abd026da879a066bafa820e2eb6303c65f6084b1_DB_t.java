 /**
  *  This file is part of MediaDaemon.
  *
  *  MediaDaemon is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  MediaDaemon is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with MediaDaemon.  If not, see <http://www.gnu.org/licenses/>.
  */
 package ws.ip4u.mediadaemon;
 
 import com.google.common.collect.Lists;
 import java.io.IOException;
 import java.sql.*;
 import java.util.List;
 
 /**
  *
  * @author jalsk
  */
 public class DB
 {
 	private Connection conn;
 //	private Log log = LogFactory.getLog(DB.class);
 	private static final String DB_TBL_EPISODES = "episodes";
 	private static final String DB_TBL_SEASON = "season";
 	private static final String DB_TBL_SERIES = "series";
 
 	public DB(String dbPath) throws SQLException
 	{
 		conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
 		createTables();
 	}
 
 	private void createTables() throws SQLException
 	{
 		Statement stmt = null;
 
 		try
 		{
 			stmt = conn.createStatement();
 
 			createIfNotExists(stmt, DB_TBL_SERIES, "id, name, path, rename");
 			createIfNotExists(stmt, DB_TBL_SEASON, "id, seriesid, number, path");
 			createIfNotExists(stmt, DB_TBL_EPISODES, "id, seasonid, name, number, season, path, filename");
 		}
 		finally
 		{
 			if(stmt != null)
 			{
 				stmt.close();
 			}
 		}
 	}
 
 	private void createIfNotExists(Statement stmt, String tableName, String columns) throws SQLException
 	{
		if(stmt.executeQuery(String.format("select name from sqlite_master where type='table' and name='%s';", tableName)).getString("name") != null)
 		{
			stmt.executeUpdate(String.format("create table %s (%s);", tableName, columns));
 		}
 	}
 
 	public boolean upsertSeries(Series series) throws SQLException
 	{
 		return upsertSeries(Lists.newArrayList(series));
 	}
 
 	public boolean upsertSeries(List<Series> series) throws SQLException
 	{
 		PreparedStatement stmt = null;
 
 		try
 		{
 			stmt = conn.prepareStatement("insert into " + DB_TBL_SERIES + " values (?, ?, ?, ?);");
 
 			for(Series s : series)
 			{
 				stmt.setInt(1, s.getSeriesId());
 				stmt.setString(2, s.getShowName());
 				stmt.setString(3, s.getPath());
 				stmt.setBoolean(4, s.getRename());
 
 				stmt.addBatch();
 			}
 
 			return stmt.execute();
 		}
 		finally
 		{
 			if(stmt != null)
 				stmt.close();
 		}
 	}
 
 	public boolean upsertSeason(Season season) throws SQLException
 	{
 		return upsertSeason(Lists.newArrayList(season));
 	}
 
 	public boolean upsertSeason(List<Season> seasons) throws SQLException
 	{
 		PreparedStatement stmt = null;
 
 		try
 		{
 			stmt = conn.prepareStatement("insert into " + DB_TBL_SEASON + " values (?, ?, ?, ?);");
 
 			for(Season s : seasons)
 			{
 				stmt.setInt(1, s.getSeasonId());
 				stmt.setInt(2, s.getSeriesId());
 				stmt.setInt(3, s.getSeasonNumber());
 				stmt.setString(4, ""); // TODO: pull in the path for the season
 
 				stmt.addBatch();
 			}
 
 			return stmt.execute();
 		}
 		finally
 		{
 			if(stmt != null)
 				stmt.close();
 		}
 	}
 
 	public boolean upsertEpisode(Episode episode) throws SQLException, IOException
 	{
 		return upsertEpisodes(Lists.newArrayList(episode));
 	}
 
 	public boolean upsertEpisodes(List<Episode> episodes) throws SQLException, IOException
 	{
 		PreparedStatement stmt = null;
 
 		try
 		{
 			stmt = conn.prepareStatement("insert into " + DB_TBL_EPISODES + " values (?, ?, ?, ?, ?, ?, ?);");
 
 			for(Episode episode : episodes)
 			{
 				stmt.setInt(1, episode.getId());
 				stmt.setInt(2, episode.getSeriesId());
 				stmt.setString(3, episode.getEpisodeName());
 				stmt.setInt(4, episode.getEpisodeNumber());
 				stmt.setInt(5, episode.getSeasonId());
 				stmt.setString(6, episode.getParentDir().getCanonicalPath());
 				stmt.setString(7, episode.getFilename());
 
 				stmt.addBatch();
 			}
 			return stmt.execute();
 		}
 		finally
 		{
 			if(stmt != null)
 				stmt.close();
 		}
 	}
 
 	// Grab all series from the database, pull it into a cache
 	public List<Series> getAllSeries() throws SQLException
 	{
 		return Lists.newArrayList();
 	}
 
 	// Grab existing data from database
 	public Series getSeries(int seriesNumber) throws SQLException
 	{
 		PreparedStatement stmt = null;
 		Series series = null;
 		ResultSet rs = null;
 
 		try
 		{
 			SeriesFactory factory = new SeriesFactory();
 
			stmt = conn.prepareCall(String.format("select * from %s where seriesId = ?;", DB_TBL_SERIES));
			stmt.setInt(1, seriesNumber);

			rs = stmt.executeQuery();
 			series = factory.withBasePath(rs.getString("basePath"))
 							.withSeriesId(seriesNumber)
 							.withShowName("showName")
 							.build();
 		}
 		finally
 		{
			rs.close();
			stmt.close();
 		}
 
 		return series;
 	}
 
 	public List<Season> getAllSeasons(int seriesId) throws SQLException
 	{
 		return Lists.newArrayList();
 	}
 
 	public Season getSeason(int seriesId, int seasonNumber) throws SQLException
 	{
 		return null;
 	}
 
 	public List<Episode> getEpisodes() throws SQLException
 	{
 		return Lists.newArrayList();
 	}
 
 	public Episode getEpisode() throws SQLException
 	{
 		return null;
 	}
 
 	public void updateAllSeries(List<Series> series) throws SQLException
 	{
 		for(Series s : series)
 		{
 			upsertSeries(s);
 		}
 	}
 }
