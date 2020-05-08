 package com.serb.podpamp.model.operations;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.os.Bundle;
 import com.foxykeep.datadroid.exception.ConnectionException;
 import com.foxykeep.datadroid.exception.CustomRequestException;
 import com.foxykeep.datadroid.exception.DataException;
 import com.foxykeep.datadroid.requestmanager.Request;
 import com.foxykeep.datadroid.service.RequestService;
 import com.serb.podpamp.model.managers.FeedsManager;
 import com.serb.podpamp.model.provider.Contract;
 import org.mcsoxford.rss.RSSFeed;
 import org.mcsoxford.rss.RSSItem;
 import org.mcsoxford.rss.RSSReader;
 import org.mcsoxford.rss.RSSReaderException;
 
 import java.util.List;
 
 public class RefreshFeedsOperation implements RequestService.Operation {
 	@Override
 	public Bundle execute(Context context, Request request)
 		throws ConnectionException, DataException, CustomRequestException {
 
 		final String[] projection = {
 			Contract.Feeds._ID,
 			Contract.Feeds.TITLE,
 			Contract.Feeds.URL
 		};
 
 		Cursor cursor = context.getContentResolver().query(Contract.Feeds.CONTENT_URI,
 			projection, null, null, null);
 
 		if (cursor != null)
 		{
 			while (cursor.moveToNext())
 			{
 				//String title = cursor.getString(cursor.getColumnIndex(Contract.Feeds.TITLE));
 				//todo notify progress
 
 				long feedId = cursor.getLong(cursor.getColumnIndex(Contract.Feeds._ID));
 				String url = cursor.getString(cursor.getColumnIndex(Contract.Feeds.URL));
 				updateFeed(context, feedId, url);
 			}
 			cursor.close();
 		}
 
 		return null;
 	}
 
 	//region Private Methods.
 
 	private void updateFeed(Context context, long feedId, String url)
 	{
 		RSSReader reader = new RSSReader();
 
 		try {
 			RSSFeed rss_feed = reader.load(url);
 			List<RSSItem> items = rss_feed.getItems();
 
 			int newItemsCount = 0;
 			for (RSSItem item : items) {
 				if (exists(context, feedId, item))
 					break;
 				//todo remove reference to RSSItem from FeedsManager
 				FeedsManager.addFeedItem(context, feedId, item, false);
 				newItemsCount++;
 			}
 
 			if (newItemsCount > 0)
 			{
 				FeedsManager.updateUnreadFeedItemsCount(context, feedId, newItemsCount);
 			}
 		} catch (RSSReaderException e) {
 			e.printStackTrace();
 		}
		catch (Exception e) {
			e.printStackTrace();
		}
 	}
 
 
 
 	private boolean exists(Context context, long feedId, RSSItem item) {
 		final String[] projection = {
 			Contract.FeedItems._ID
 		};
 
 		final String selection = Contract.FeedItems.FEED_ID + " = ? and " +  Contract.FeedItems.GUID + " = ? and " + Contract.FeedItems.PUBLISHED + " = ?";
 		final String[] selectionArgs = { String.valueOf(feedId), item.getGuid(), String.valueOf(item.getPubDate().getTime()) };
 
 		Cursor cursor = context.getContentResolver().query(Contract.FeedItems.CONTENT_URI,
 			projection, selection, selectionArgs, null);
 
 		boolean result = false;
 		if (cursor != null)
 		{
 			if (cursor.moveToNext())
 			{
 				result = true;
 			}
 			cursor.close();
 		}
 		return result;
 	}
 
 	//endregion
 }
