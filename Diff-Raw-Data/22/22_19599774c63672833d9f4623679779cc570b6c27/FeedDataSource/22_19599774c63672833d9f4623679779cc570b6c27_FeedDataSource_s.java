 package me.hujin.rss.storage;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.hujin.rss.parser.RSSFeed;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 
 public class FeedDataSource extends DataSourceBase{
 	
 	public FeedDataSource(Context context) {
 		super(context);
 	}
 
 	public Feed addFeed(RSSFeed rssFeed) {
 		Feed newFeed = new Feed();
 		ContentValues values = new ContentValues();
 		values.put("title", rssFeed.title.trim());
 		values.put("description", rssFeed.description);
 		values.put("link", rssFeed.link);
 		values.put("last_build_date", rssFeed.getLastBuildTimestamp());
 		
 		long fid = db.insert(RssRoidSqliteHelper.TB_FEED, null, values);
 		
 		newFeed.setFid(fid);
 		newFeed.setTitle(rssFeed.title);
 		newFeed.setDescription(rssFeed.description);
 		newFeed.setLastBuildDate(rssFeed.getLastBuildTimestamp());
 		newFeed.setLink(rssFeed.link);
 		
 		return newFeed;
 	}
 	
 	/**
 	 * delete a feed and all its items form database.
 	 * @param fid
 	 * @return
 	 */
 	public boolean delete(long fid) {
 		db.delete(RssRoidSqliteHelper.TB_FEED, "fid=" + fid, null);
 		db.delete(RssRoidSqliteHelper.TB_ITEM, "fid=" + fid, null);
 		return true;
 	}
 	
 	/**
 	 * rename a feed by fid.
 	 * 
 	 * @param fid
 	 * @param newName
 	 * @return
 	 */
 	public boolean rename(long fid, String newName) {
 		ContentValues values = new ContentValues();
 		values.put("title", newName);
 		db.update(RssRoidSqliteHelper.TB_FEED, values, "fid=" + fid, null);
 		return true;
 	}
 	
 	
 	public void updateTimestamp(long fid, long lastBuildDate, long lastItemDate) {
 		ContentValues values = new ContentValues();
 		if(lastBuildDate > 0) {
 			values.put("last_build_date", lastBuildDate);
 		}
 		if(lastItemDate > 0) {
 			values.put("last_item_date", lastItemDate);
 		}
 		db.update(RssRoidSqliteHelper.TB_FEED, values, "fid=" + fid, null);
 	}
 	
 	public List<Feed> getAllFeeds() {
 		List<Feed> feeds = new ArrayList<Feed>();
 		Cursor cursor = db.query(
 				RssRoidSqliteHelper.TB_FEED, 
 				new String[]{"fid", "title", "description", "link", "last_build_date", "last_item_date"}, 
 				null, null, null, null, null, null);
 		
 		cursor.moveToFirst();
 		
 		while(!cursor.isAfterLast()) {
 			Feed newFeed = cursorToFeed(cursor);
 			feeds.add(newFeed);
 			cursor.moveToNext();
 		}
 		
 		cursor.close();
 		return feeds;
 	}
 	
 	/**
 	 * helper method for getAllFeeds()
 	 * @param cursor
 	 * @return
 	 */
 	public Feed cursorToFeed(Cursor cursor) {
 		Feed feed = new Feed();
 		feed.setFid(cursor.getLong(0));
 		feed.setTitle(cursor.getString(1));
 		feed.setDescription(cursor.getString(2));
 		feed.setLink(cursor.getString(3));
 		feed.setLastBuildDate(cursor.getLong(4));
 		feed.setLastItemDate(cursor.getLong(5));
 		
 		return feed;
 	}
 	
 	/**
 	 * get feed items by feed_id.
 	 * @param fid
 	 * @return
 	 */
 	public List<Item> getFeedItems(long fid) {
 		List<Item> items = new ArrayList<Item>();
 		
 		Cursor cursor = db.query(RssRoidSqliteHelper.TB_ITEM, 
 				new String[]{"id", "title", "author"}, 
				"fid=" + fid, null, null, null, "pub_date DESC");
 		
 		cursor.moveToFirst();
 		
 		while(!cursor.isAfterLast()) {
 			Item item = new Item();
 			item.setId(cursor.getLong(0));
 			item.setTitle(cursor.getString(1));
 			item.setAuthor(cursor.getString(2));
 			
 			items.add(item);
 			cursor.moveToNext();
 		}
 		cursor.close();
 		return items;
 	}
 	
 	/**
 	 * Checking for whether the given link of feed is exist.
 	 * 
 	 * @param link
 	 * @return
 	 */
 	public boolean isExist(String link) {
 		Cursor cursor = db.query("feed", new String[]{"count(*)"}, 
 				"link='" + link + "'", null, null, null, null);
 		long count = 0;
 		cursor.moveToFirst();
 		if(!cursor.isAfterLast()) {
 			count = cursor.getLong(0);
 		}
 		return count > 0 ? true : false;
 	}
 }
