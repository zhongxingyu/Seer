 package com.feiying.impl;
 
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 
 import com.feiying.db.DBHelper;
 
 public class Series {
 
 	/**
 	 * 得到电视剧列表
 	 * 
 	 * @param currPage
 	 * @param pageSize
 	 * @return
 	 * @throws SQLException
 	 */
 	public static List<Map<String, Object>> getList(int currPage, int pageSize)
 			throws SQLException {
 		String sql = "SELECT t.actor," 
 				+ "t.release_date," 
 				+ "t.origin," 
 				+ "t.episode_count,"
 				+ "t.episode_all," 
 				+ "v.source_id," 
 				+ "v.title,"
 				+ "v.image_url," 
 				+ "v.channel," 
 				+ "v.play_count,"
 				+ "v.fav_count," 
 				+ "v.share_count " 
 				+ "FROM fy_tv_series t "
 				+ "LEFT JOIN fy_video v ON t.source_id = v.source_id "
 				+ "WHERE v.status >= 100 "
 				+ "ORDER BY t.release_date DESC";
 		return DBHelper.getInstance().queryPager(sql, currPage, pageSize);
 	}
 
 	/**
 	 * 得到电视剧数量
 	 * 
 	 * @return
 	 * @throws SQLException
 	 */
 	public static int getListCount() throws SQLException {
 		String sql = "SELECT count(t.source_id) " +
 				"FROM fy_tv_series AS t LEFT JOIN fy_video AS v ON t.source_id = v.source_id "
 				+ "WHERE v.status >= 100 ";
 		return DBHelper.getInstance().count(sql);
 	}
 
 	/**
 	 * 得到电视剧根据sourceId
 	 * 
 	 * @param sourceId
 	 * @return
 	 * @throws SQLException
 	 */
 	public static Map<String, Object> getSeriesBySourceId(String sourceId)
 			throws SQLException {
 		Map<String, Object> bean = null;
 		String sql = "SELECT t.director," 
 				+ "t.actor," 
 				+ "t.release_date,"
 				+ "t.origin," 
 				+ "t.description," 
 				+ "t.episode_count,"
 				+ "t.episode_all," 
 				+ "v.source_id," 
 				+ "v.title,"
 				+ "v.image_url," 
 				+ "v.channel," 
 				+ "v.play_count,"
 				+ "v.fav_count," 
 				+ "v.share_count " 
 				+ "FROM fy_tv_series t "
 				+ "LEFT JOIN fy_video v ON t.source_id = v.source_id "
 				+ "WHERE t.source_id=?";
 		Object[] params = new Object[] { sourceId };
 		List<Map<String, Object>> list = DBHelper.getInstance().query(sql,
 				params);
 		if (list.size() > 0) {
 			bean = list.get(0);
 		}
 		return bean;
 	}
 
 	/**
 	 * 得到电视剧剧集列表
 	 * 
 	 * @param sourceId
 	 * @return
 	 * @throws SQLException
 	 */
 	public static List<Map<String, Object>> getEpisodeList(String sourceId)
 			throws SQLException {
 		String sql = "SELECT source_id," 
 				+ "time," 
 				+ "size," 
 				+ "episode_index,"
 				+ "image_url," 
 				+ "video_url " 
 				+ "FROM fy_tv_episode "
 				+ "WHERE source_id = ? " 
				+ "AND episode_index BETWEEN 1 AND "
				+ "( "
				+ "SELECT episode_count FROM fy_tv_series "
				+ "WHERE source_id = ? "
				+ ") "
 				+ "ORDER BY episode_index ";
		Object[] params = new Object[] { sourceId, sourceId };
 		return DBHelper.getInstance().query(sql, params);
 	}
 
 	/**
 	 * 该用户收藏下的所有电视剧
 	 * 
 	 * @param username
 	 * @param channel
 	 * @param currPage
 	 * @param pageSize
 	 * @return
 	 * @throws SQLException
 	 */
 	public static List<Map<String, Object>> getFavList(String username,
 			int channel, int currPage, int pageSize) throws SQLException {
 		String sql = "SELECT " 
 				+ "t.actor," 
 				+ "t.release_date,"
 				+ "t.origin," 
 				+ "t.episode_count," 
 				+ "t.episode_all," 
 				+ "v.source_id,"
 				+ "v.title," 
 				+ "v.image_url," 
 				+ "v.channel," 
 				+ "v.play_count,"
 				+ "v.fav_count," 
 				+ "v.share_count " 
 				+ "FROM fy_fav f "
 				+ "LEFT JOIN fy_tv_series t ON f.source_id = t.source_id "
 				+ "LEFT JOIN fy_video v ON f.source_id = v.source_id "
 				+ "WHERE f.username = ? AND v.channel = ? "
 				+ "ORDER BY f.created_time DESC";
 		Object[] params = new Object[] { Long.parseLong(username), channel };
 		return DBHelper.getInstance().queryPager(sql, params, currPage,
 				pageSize);
 	}
 }
