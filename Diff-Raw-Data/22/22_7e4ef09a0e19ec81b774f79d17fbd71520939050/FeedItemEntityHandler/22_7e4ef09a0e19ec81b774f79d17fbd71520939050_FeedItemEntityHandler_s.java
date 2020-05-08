 package feedreader.persist;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.inject.Singleton;
 
 import com.google.common.base.Preconditions;
 
 import common.persist.DbUtils;
 import common.persist.RowMapper;
 import feedreader.FeedItem;
 
 @Singleton
 public class FeedItemEntityHandler {
 	private static final String SELECT_SQL_FRAGMENT;
 	private static final RowMapper<FeedItem> ROW_MAPPER;
 	
 	static {
 		SELECT_SQL_FRAGMENT = "select "
 			+ "i.id feedItem_id, "
 			+ "i.title feedItem_title, "
 			+ "i.description feedItem_description, "
 			+ "i.link feedItem_link, "
 			+ "i.pubDate feedItem_pubDate, "
 			+ "i.guid feedItem_guid, "
 			+ "i.created feedItem_created, "
 			+ "i.feedId feedItem_feedId, "
 			
 			//feed
 			+ "if.id feed_id, "
 			+ "if.url feed_url, "
 			+ "if.lastUpdated feed_lastUpdated, "
 			+ "if.title feed_title, "
 			+ "if.created feed_created, "
 			+ "if.createdBy feed_createdBy "
 			
 			+ "from feedreader.FeedItems i "
 			+ "inner join feedreader.Feeds if on i.feedId = if.id "
 			+ "inner join feedreader.Users ifu on if.createdBy = ifu.id ";
 		ROW_MAPPER = new FeedItemRowMapper();
 	}
 	
 	/**
 	 * Inserts a new feed item into the database
 	 * @return the ID of the new feed item.
 	 */
 	public int insert(Connection cnn, int feedId, String title, String description, String link, Date pubDate, String guid) throws SQLException {
 		final PreparedStatement stmt = cnn.prepareStatement("insert into feedreader.FeedItems (feedId, title, description, link, pubDate, guid) values (?, ?, ?, ?, ?, ?) returning id");
 		try {
 			stmt.setInt(1, feedId);
 			stmt.setString(2, title);
 			stmt.setString(3, description);
 			stmt.setString(4, link);
			DbUtils.setDate(stmt, 5, pubDate);
 			stmt.setString(6, guid);
 			
 			if (stmt.execute()) {
				ResultSet rst = null;
 				try {
					rst = stmt.getResultSet();
 					if (rst.next()) {
 						return rst.getInt("id");
 					} else {
 						throw new IllegalStateException("Unable to create FeedItem");
 					}
 				} finally {
 					DbUtils.close(rst);
 				}
 			} else {
 				throw new IllegalStateException("Unable to create FeedItem");
 			}
 			
 		} finally {
 			DbUtils.close(stmt);
 		}
 	}
 	
 	/**
 	 * Gets the feed items for the specified feed ID.
 	 * @return the feed items for the specified feed ID.
 	 */
 	public @Nonnull List<FeedItem> getFeedItemsForFeed(Connection cnn, int feedId) throws SQLException {
 		return getFeedItemsForFeed(cnn, feedId, null);
 	}
 	
 	/**
 	 * Gets the feed items for the specified feed ID.
 	 * @return the feed items for the specified feed ID.
 	 */
 	public @Nonnull List<FeedItem> getFeedItemsForFeed(Connection cnn, int feedId, @Nullable Integer limit) throws SQLException {
 		Preconditions.checkArgument(cnn != null, "cnn should not be null");
 		if (limit != null && limit < 1) {
 			return Collections.emptyList();
 		}
 		
 		final List<FeedItem> feedItems = new ArrayList<FeedItem>();
 		final PreparedStatement stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT + "where if.id = ? " + (limit != null ? " limit " + limit : ""));
 		try {
 			stmt.setInt(1, feedId);
 			
 			final ResultSet rst = stmt.executeQuery();
 			try {
 				
 				while(rst.next()) {
 					feedItems.add(ROW_MAPPER.mapRow(rst));
 				}
 				
 			} finally {
 				DbUtils.close(rst);
 			}
 			
 		} finally {
 			DbUtils.close(stmt);
 		}
 		return feedItems;
 	}
 	
 	/**
 	 * Gets all of the feed items the user is subscribed to.
 	 */
 	public @Nonnull List<FeedItem> getFeedItemsForStream(@Nonnull Connection cnn, int userId, int size, int offset) throws SQLException {
 		Preconditions.checkArgument(cnn != null, "cnn should not be null");
 
 		//get all of the feed item ids
 		final List<Integer> feedItemIds = getSubscribedFeedItemIds(cnn, userId, size, offset);
 		
 		//get all of the feed items that match
 		return getFeedItems(cnn, feedItemIds);
 		
 	}
 	
 	/**
 	 * Gets the feed item IDs for all of the subscriptions for the given user
 	 */
 	private List<Integer> getSubscribedFeedItemIds(Connection cnn, int userId, int size, int offset) throws SQLException {
 		final ArrayList<Integer> feedItemIds = new ArrayList<Integer>(size);
 		PreparedStatement stmt = null;
 		try {
 			stmt = cnn.prepareStatement("select fi.id feedItemId "
 					+ "from feedreader.FeedSubscriptions fs inner join feedreader.FeedItems fi on fs.feedId = fi.feedId "
 					+ "where fs.subscriber = ? "
 					+ "order by fi.pubDate desc limit " + size + " offset " + offset);
 			stmt.setInt(1, userId);
 			
 			ResultSet rst = null;
 			try {
 				rst = stmt.executeQuery();
 				while(rst.next()) {
 					feedItemIds.add(rst.getInt("feedItemId"));
 				}
 			} finally {
 				DbUtils.close(rst);
 			}
 		} finally {
 			DbUtils.close(stmt);
 		}
 		return feedItemIds;
 	}
 	
 	/**
 	 * Gets all of the specified feed items corresponding to the specified IDs
 	 */
 	private List<FeedItem> getFeedItems(final Connection cnn, final List<Integer> feedItemIds) throws SQLException {
 		final ArrayList<FeedItem> feedItems = new ArrayList<FeedItem>(feedItemIds.size());
 		PreparedStatement stmt = null;
 		try {
 			stmt = cnn.prepareStatement(SELECT_SQL_FRAGMENT
 					+ "where i.id = ? "
 					+ "limit 1");
 			
 			//TODO: create a procedure to do all of the looping in Postgresql instead of doing in JDBC
 			for(Integer feedItemId : feedItemIds) {
 				stmt.setInt(1, feedItemId);
 				
 				ResultSet rst = null;
 				try {
 					rst = stmt.executeQuery();
 					
 					//limit 1 clause used in query so there is no need to check anything after the first
 					if(rst.next()) { 
 						FeedItem feedItem = ROW_MAPPER.mapRow(rst);
 						feedItems.add(feedItem);
 					}
 					
 				} finally {
 					DbUtils.close(rst);
 				}
 			}
 			
 		} finally {
 			DbUtils.close(stmt);
 		}
 		return feedItems;
 	}
 }
