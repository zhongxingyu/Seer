 package cn.edu.zju.plex.tdd.dao;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import weibo4j.model.Status;
 
 import cn.edu.zju.plex.tdd.entity.ParsedStatus;
 import cn.edu.zju.plex.tdd.entity.RssFeed;
 import cn.edu.zju.plex.tdd.entity.RssNews;
 import cn.edu.zju.plex.tdd.entity.TvShows;
 import cn.edu.zju.plex.tdd.tools.CharsetUtil;
 
 /**
  * TDD的数据库操作类. TODO 以表分割该类
  * 
  * @author WangFengwei
  */
 public final class DB4Tdd {
 
 	/** 数据库链接. */
 	// TODO 需要加锁么？ createStatement操作能否并发
 	private static Connection con = MySqlDB.getCon();
 
 	/** 日志. */
 	// TODO 需要加锁么？
 	private static final Logger LOG = Logger.getLogger(DB4Tdd.class);
 
 	/**
 	 */
 	public static List<RssNews> getRssNewsToParse(int count) {
 		String sql = "select id, title, link, category, description, pubDate, feed, page, status from rss_news where status = "
 				+ RssNews.ST_READY + " limit 0," + count;
 		List<RssNews> res = new ArrayList<RssNews>();
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			while (rs.next()) {
 				RssNews rssnews = new RssNews();
 				rssnews.setId(rs.getLong(1));
 				rssnews.setTitle(rs.getString(2));
 				rssnews.setLink(rs.getString(3));
 				rssnews.setCategory(rs.getString(4));
 				rssnews.setDescription(rs.getString(5));
 				try {
 					rssnews.setPubDate(new SimpleDateFormat(
 							"yyyy-MM-dd HH:mm:ss").parse(rs.getString(6)));
 				} catch (ParseException e) {
 					e.printStackTrace();
 				}
 				rssnews.setFeed(rs.getLong(7));
 				rssnews.setPage(rs.getString(8));
 				rssnews.setStatus(rs.getInt(9));
 				res.add(rssnews);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return res;
 	}
 
 	/**
 	 * @param count
 	 *            一次获取的数量
 	 * @param linkReg
 	 *            link的正则表达式
 	 * @return
 	 */
 	public static List<RssNews> getRssNewsToSplit(int count, String linkReg) {
 		String sql = "select id, title, link, category, description, pubDate, feed, page, status from rss_news where link regexp '"
 				+ linkReg + "' limit 0," + count;
 		List<RssNews> res = new ArrayList<RssNews>();
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			while (rs.next()) {
 				RssNews rssnews = new RssNews();
 				rssnews.setId(rs.getLong(1));
 				rssnews.setTitle(rs.getString(2));
 				rssnews.setLink(rs.getString(3));
 				rssnews.setCategory(rs.getString(4));
 				rssnews.setDescription(rs.getString(5));
 				rssnews.setPubDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
 						.parse(rs.getString(6)));
 				rssnews.setFeed(rs.getLong(7));
 				rssnews.setPage(rs.getString(8));
 				rssnews.setStatus(rs.getInt(9));
 				res.add(rssnews);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			LOG.error(e.getMessage());
 			LOG.warn("sql:\t" + sql);
 		}
 		return res;
 	}
 
 	/**
 	 * 从rssnews表中得到要索引的rssnews条目. TODO table has changed!!!
 	 * 
 	 * @return 成功则返回得到的rssnews(id, title, description, summary, status)，失败返回null
 	 */
 	public static List<RssNews> getItemToIndex(int startIdx, int count) {
 		String sql = String
 				.format("select id, title, summary, status, description from rssnews where status=%d order by id limit %d,%d",
 						RssNews.ST_FINISHED, startIdx, count);
 		List<RssNews> res = new ArrayList<RssNews>();
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			while (rs.next()) {
 				RssNews ritem = new RssNews();
 				ritem.setId(rs.getLong(1));
 				ritem.setTitle(rs.getString(2));
 				ritem.setDescription(rs.getString(3).replaceAll("<[^>]+>", "")
 						.replaceAll("[\\s\\t\\r\\n]+", ""));
 				ritem.setSummary(rs.getString(4).replaceAll("<[^>]+>", "")
 						.replaceAll("[\\s\\t\\r\\n]+", ""));
 				ritem.setStatus((int) rs.getLong(5));
 				res.add(ritem);
 			}
 		} catch (SQLException e) {
 			LOG.warn(e.toString());
 		}
 		return res;
 	}
 
 	/**
 	 * 替换不合法的字符，将'''替换成'’'.
 	 * 
 	 * @param input
 	 *            输入字符串
 	 * @return output 替换后的字符串
 	 */
 	private static String rmInvalidChar(final String input) {
 		String output = null;
 		if (input != null) {
 			output = input.trim().replaceAll("'", "’");
 		}
 		return output;
 	}
 
 	/**
 	 * @return
 	 */
 	public static ArrayList<RssFeed> getRssFeedList() {
 		String sql = "select id, title, feed, link, count, first_update, last_update from rss_feeds";
 		ArrayList<RssFeed> res = new ArrayList<RssFeed>();
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			while (rs.next()) {
 				RssFeed rf = new RssFeed();
 				rf.setId(rs.getLong(1));
 				rf.setTitle(rs.getString(2));
 				rf.setFeed(rs.getString(3));
 				rf.setLink(rs.getString(4));
 				rf.setCount(rs.getInt(5));
				rf.setFirstUpdate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.parse(rs.getString(6)));
				rf.setLastUpdate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.parse(rs.getString(7)));
 				res.add(rf);
 			}
		} catch (Exception e) {
 			e.printStackTrace();
 			LOG.warn(e.toString());
 		}
 		return res;
 	}
 
 	/**
 	 * 插入rssnews，目前直插入了rss和news部分字段
 	 * <p>
 	 * (title, link, category, description, pubDate, feed, page, status)
 	 * <p>
 	 * TODO insert sql will be too long???
 	 */
 	public static void insertRssNews(RssNews rssnews) {
 
 		String sql = String
 				.format("insert into rss_news(title, link, category, description, pubDate, feed, page, status) values('%s', '%s', '%s', '%s', '%s', %d, '%s', %d);",
 						rmInvalidChar(rssnews.getTitle()),
 						rmInvalidChar(rssnews.getLink()), rmInvalidChar(rssnews
 								.getCategory()), rmInvalidChar(rssnews
 								.getDescription()), new SimpleDateFormat(
 								"yyyy-MM-dd HH:mm:ss").format(rssnews
 								.getPubDate()), rssnews.getFeed(),
 						rmInvalidChar(rssnews.getPage()), rssnews.getStatus());
 		try {
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql);
 			stmt.close();
 		} catch (SQLException e) {
 			// ignore duplicated entry warning
 			if (!e.getMessage().contains("Duplicate")) {
 				e.printStackTrace();
 				LOG.warn(e.getMessage());
 				LOG.warn("sql is:\t" + sql);
 			} else
 				LOG.info("duplicated entry:" + rssnews);
 		}
 	}
 
 	/**
 	 * update RssNews(content, images, videos, words, status)
 	 * <p>
 	 * 
 	 * @param rssNews
 	 */
 	public static void updateParsedRssNews(RssNews rssNews) {
 
 		String sql = String
 				.format("update rss_news set content='%s', images='%s', videos='%s', words='%s', meiju_id='%s', meiju_cname='%s', meiju_ename='%s', status=%d where id=%d",
 						rssNews.getContent(), rssNews.getImages(), rssNews
 								.getVideos(), rssNews.getWords(), rssNews
 								.getTvShows().getSid(), rssNews.getTvShows()
 								.getCname(), rssNews.getTvShows().getEname(),
 						rssNews.getStatus(), rssNews.getId());
 		try {
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql);
 			stmt.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static List<RssNews> getRssNewsForTest(int feed, int count) {
 		String sql = "select id, title, link, feed, page from rss_news where feed = "
 				+ feed + " limit 0," + count;
 		List<RssNews> res = new ArrayList<RssNews>();
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			while (rs.next()) {
 				RssNews rssnews = new RssNews();
 				rssnews.setId(rs.getLong(1));
 				rssnews.setTitle(rs.getString(2));
 				rssnews.setLink(rs.getString(3));
 				rssnews.setFeed(rs.getLong(4));
 				rssnews.setPage(rs.getString(5));
 				res.add(rssnews);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return res;
 	}
 
 	/**
 	 * @deprecated 这是老本版的获取方法，现在只需要access token
 	 */
 	public static String[] getWeiboOAuth(String wuid) {
 		String sql = "select accesstoken, accesstokensecret from weibo_auth where wuid like '"
 				+ wuid + "'";
 		String[] accessTokenSecret = new String[2];
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			if (rs.next()) {
 				accessTokenSecret[0] = rs.getString(1);
 				accessTokenSecret[1] = rs.getString(2);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			accessTokenSecret = null;
 			LOG.warn("fail to get weibo auth:" + wuid);
 			LOG.error(e.getMessage());
 			LOG.warn("sql is:\t" + sql);
 		}
 		return accessTokenSecret;
 	}
 
 	/**
 	 * 获取目标用户的user iｄ和上次上次收录的weibo id
 	 */
 	public static HashMap<String, String> getWeiboTargets() {
 		HashMap<String, String> targets = new HashMap<String, String>();
 		String sql = "select wuid, lastwid from meiju_wusers";
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			while (rs.next()) {
 				targets.put(rs.getString(1), rs.getString(2));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			targets = null;
 			LOG.warn("fail to get target weibo users");
 			LOG.error(e.getMessage());
 			LOG.warn("sql is:\t" + sql);
 		}
 		return targets;
 	}
 
 	public static void updateWeiboTargets(String wuid, String lastUpdateWeibo) {
 		String sql = "update meiju_wusers set lastwid = '" + lastUpdateWeibo
 				+ "' where wuid like '" + wuid + "'";
 		try {
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			LOG.warn("fail to update target weibo users");
 			LOG.error(e.getMessage());
 			LOG.warn("sql is:\t" + sql);
 		}
 	}
 
 	/**
 	 * 插入新微薄(wid, wmid, wsmallimages, wmiddleimage, woriginalimage, wgeo,
 	 * wuname, wuid, wtime, wtext, wreply2wid, wreply2uid, wrepost_count,
 	 * wcomment_count, wattitudes_count) TODO java.sql.SQLException: Incorrect
 	 * string value: '\xF0\x9F\x94\x83' for column 'wtext' at row 1
 	 * 
 	 * @param s
 	 */
 	public static void insertWeibo(Status s) {
 		String sql = String
 				.format("insert into meiju_weibo(wid, wmid, wsmallimage, wmiddleimage, "
 						+ "woriginalimage, wgeo, wuname, wuid, wtime, wtext, wreply2wid, wreply2uid, "
 						+ "wrepost_count, wcomment_count, wattitudes_count) values('%s', '%s', '%s', '%s', '%s', '%s',"
 						+ " '%s', '%s', '%s', '%s', '%s', '%s', %d, %d, %d)", s
 						.getId(), s.getMid(), s.getThumbnailPic(), s
 						.getBmiddlePic(), s.getOriginalPic(), s.getGeo(), s
 						.getUser().getName().replace('\'', '’'), s.getUser()
 						.getId(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
 						.format(s.getCreatedAt()),
 						s.getText().replace('\'', '’'), s
 								.getInReplyToStatusId(),
 						s.getInReplyToUserId(), s.getRepostsCount(), s
 								.getCommentsCount(), 0);
 		try {
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql);
 		} catch (SQLException e) {
 			// ignore duplicated entry warning
 			if (!e.getMessage().contains("Duplicate")) {
 				e.printStackTrace();
 				LOG.warn(e.getMessage());
 				LOG.warn("sql is:\t" + sql);
 			} else
 				LOG.debug("duplicated entry:" + s);
 		}
 
 	}
 
 	public static List<ParsedStatus> getWeiboToParse(int count) {
 		String sql = "select wid, wmid, wsmallimage, wmiddleimage, "
 				+ "woriginalimage, wgeo, wuname, wuid, wtime, wtext, "
 				+ "wreply2wid, wreply2uid, wrepost_count, wcomment_count "
 				+ "from meiju_weibo where status = " + RssNews.ST_READY
 				+ " limit 0," + count;
 		List<ParsedStatus> res = new ArrayList<ParsedStatus>();
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			while (rs.next()) {
 				ParsedStatus ps = new ParsedStatus();
 				ps.setId(rs.getString(1));
 				ps.setMid(rs.getString(2));
 				ps.setThumbnailPic(rs.getString(3));
 				ps.setBmiddlePic(rs.getString(4));
 				ps.setOriginalPic(rs.getString(5));
 				ps.setGeo(rs.getString(6));
 				ps.setUname(rs.getString(7));
 				ps.setUid(rs.getString(8));
				ps.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.parse(rs.getString(9)));
 				ps.setText(rs.getString(10));
 				ps.setInReplyToStatusId(rs.getLong(11));
 				ps.setInReplyToUserId(rs.getLong(12));
 				ps.setRepostsCount(rs.getInt(13));
 				ps.setCommentsCount(rs.getInt(14));
 
 				ps.setStatus(RssNews.ST_READY);
 				res.add(ps);
 			}
		} catch (Exception e) {
 			e.printStackTrace();
 			LOG.warn("error occured in getWeiboToParse: " + e.getMessage());
 			LOG.warn(sql);
 		}
 		return res;
 	}
 
 	public static void updateParsedStatus(ParsedStatus st) {
 		String sql = String
 				.format("update meiju_weibo set "
 						+ "content='%s', video='%s', url='%s', topic='%s', words='%s', meiju_id='%s', meiju_cname='%s', meiju_ename='%s', "
 						+ "at_unames='%s', status=%d where wid='%s'",
 						st.getContent(), st.getVideo(), st.getUrl(),
 						st.getTopic(), st.getWords(), st.getTvShow().getSid(),
 						st.getTvShow().getCname(), st.getTvShow().getEname(),
 						st.getUname(), st.getStatus(), st.getId());
 		try {
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql);
 			stmt.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			LOG.warn("error occured in updateParsedStatus:" + e.getMessage());
 			LOG.warn(sql);
 		}
 	}
 
 	/**
 	 * 返回美剧名(aka),英文名去掉中间空格，并且小写
 	 * 
 	 * @return
 	 */
 	public static HashMap<String, TvShows> getMeijuTvs() {
 		HashMap<String, TvShows> tvs = new HashMap<String, TvShows>();
 		String sql = "select sid, cname, ename, aka_original, aka from tvshows";
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			String aka, id;
 			while (rs.next()) {
 				id = rs.getString(1);
 				String cname = rs.getString(2);
 				String ename = rs.getString(3);
 				TvShows tvShow = new TvShows(id, cname, ename);
 				tvs.put(cname, tvShow);
 				tvs.put(ename.toLowerCase().replace(" ", ""), tvShow);
 				for (int i = 4; i < 6; i++) {
 					aka = rs.getString(i).toLowerCase()
 							.replaceAll("[:：，-；;]", ",");
 					for (String name : aka.split(",")) {
 						if (tvs.get(name) != null && tvs.get(name) != tvShow) {
 							LOG.debug(tvs.get(name) + " and " + id
 									+ " conflicted by name:" + name);
 						} else {
 							name = name.trim();
 							if (name.length() > 1) {
 								if (!CharsetUtil.containChinese(name))
 									name = name.replace(" ", "");
 								tvs.put(name, tvShow);
 								// LOG.info("put in name:\t" + name);
 							}
 						}
 					}
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			tvs = null;
 			LOG.warn("fail to get target weibo users");
 			LOG.error(e.getMessage());
 			LOG.warn("sql is:\t" + sql);
 		}
 		LOG.info("init Meiju Tv Over");
 		return tvs;
 
 	}
 
 	public static void delete(RssNews rssNews) {
 		String sql = "delete from rss_news where id=" + rssNews.getId();
 		try {
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql);
 			stmt.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			LOG.warn("error occured in updateParsedStatus:" + e.getMessage());
 			LOG.warn(sql);
 		}
 	}
 
 	/**
 	 * @param timeLen
 	 * @return
 	 */
 	public static List<RssNews> getRssNewsToMerge(int timeLen) {
 		String presql = "select pubDate from rss_news where delegate=0 order by pubDate limit 0, 1";
 		String sql = "select id, title, link, category, description, pubDate, "
 				+ "feed, words, meiju_id, meiju_cname, meiju_ename, delegate, status "
 				+ "from rss_news where delegate=0 and pubDate<='%s' order by pubDate";
 		List<RssNews> res = new ArrayList<RssNews>();
 		Date endDate = null;
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(presql);
 			if (rs.next()) {
 				Date start = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
 						.parse(rs.getString(1));
 				endDate = new Date(start.getTime() + timeLen);
 			} else {
 				LOG.warn("no more dates...");
 				endDate = new Date();
 			}
 			sql = String
 					.format(sql, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
 							.format(endDate));
 			rs = stmt.executeQuery(sql);
 
 			while (rs.next()) {
 				RssNews rssnews = new RssNews();
 				rssnews.setId(rs.getLong(1));
 				rssnews.setTitle(rs.getString(2));
 				rssnews.setLink(rs.getString(3));
 				rssnews.setCategory(rs.getString(4));
 				rssnews.setDescription(rs.getString(5));
 				rssnews.setPubDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
 						.parse(rs.getString(6)));
 				rssnews.setFeed(rs.getLong(7));
 				rssnews.setWords(rs.getString(8));
 				rssnews.setTvShows(new TvShows(rs.getString(9), rs
 						.getString(10), rs.getString(11)));
 				rssnews.setDelegate(rs.getLong(12));
 				rssnews.setStatus(rs.getInt(13));
 				res.add(rssnews);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			LOG.error(e.getMessage());
 			LOG.warn("sql:\t" + sql);
 		}
 		return res;
 	}
 
 	public static void updateDelegate(RssNews rssNews) {
 		String sql = String.format(
 				"update rss_news set delegate=%d where id=%d",
 				rssNews.getDelegate(), rssNews.getId());
 		try {
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql);
 			stmt.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			LOG.error(e.getMessage());
 			LOG.warn("sql:\t" + sql);
 		}
 	}
 
 	public static void updateRssFeedLastUpdateTime(RssFeed rf) {
 		String sql = String.format(
 				"update rss_feeds set last_update='%s' where id=%d",
 				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rf
 						.getLastUpdate()), rf.getId());
 		try {
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql);
 			stmt.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			LOG.error(e.getMessage());
 			LOG.warn("sql:\t" + sql);
 		}
 
 	}
 
 	// images_count = -1是还没有获取的，没有是0
 	public static List<RssNews> getRssNewsToDownloadImages() {
 		String sql = "select id, images from rss_news where image_count = -1 limit 0, 100";
 		List<RssNews> res = new ArrayList<RssNews>();
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			while (rs.next()) {
 				RssNews rssnews = new RssNews();
 				rssnews.setId(rs.getLong(1));
 				rssnews.setImages(rs.getString(2));
 				res.add(rssnews);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			LOG.error(e.getMessage());
 			LOG.warn("sql:\t" + sql);
 		}
 		return res;
 	}
 
 	public static void updateRssNewsImageCount(long rssNewsId, int count) {
 		String sql = "update rss_news set image_count = " + count
 				+ " where id=" + rssNewsId;
 		try {
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			LOG.error(e.getMessage());
 			LOG.warn("sql:\t" + sql);
 		}
 
 	}
 
 	public static List<ParsedStatus> getParsedStatusToDownloadImages() {
 		String sql = "select wid, wsmallimage, wmiddleimage, woriginalimage from meiju_weibo where image_count = -1 limit 0, 100";
 		List<ParsedStatus> res = new ArrayList<ParsedStatus>();
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			while (rs.next()) {
 				ParsedStatus st = new ParsedStatus();
 				st.setId(rs.getString(1));
 				st.setThumbnailPic(rs.getString(2));
 				st.setBmiddlePic(rs.getString(3));
 				st.setOriginalPic(rs.getString(4));
 				res.add(st);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			LOG.error(e.getMessage());
 			LOG.warn("sql:\t" + sql);
 		}
 		return res;
 	}
 
 	public static void updateParsedStatusImageCount(String statusId, int count) {
 		String sql = "update meiju_weibo set image_count = " + count
 				+ " where wid='" + statusId + "'";
 		try {
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			LOG.error(e.getMessage());
 			LOG.warn("sql:\t" + sql);
 		}
 
 	}
 
 }
