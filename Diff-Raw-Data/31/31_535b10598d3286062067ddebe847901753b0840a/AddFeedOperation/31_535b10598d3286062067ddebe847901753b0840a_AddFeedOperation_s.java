 package com.serb.podpamp.model.operations;
 
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.net.Uri;
 import android.os.Bundle;
 import com.foxykeep.datadroid.exception.ConnectionException;
 import com.foxykeep.datadroid.exception.CustomRequestException;
 import com.foxykeep.datadroid.exception.DataException;
 import com.foxykeep.datadroid.requestmanager.Request;
 import com.foxykeep.datadroid.service.RequestService;
 import com.serb.podpamp.model.managers.DownloadManager;
 import com.serb.podpamp.model.managers.FeedsManager;
 import com.serb.podpamp.model.provider.Contract;
 import com.serb.podpamp.model.request.RequestFactory;
 import com.serb.podpamp.utils.Utils;
 import org.mcsoxford.rss.*;
 
 import java.util.List;
 
 public class AddFeedOperation implements RequestService.Operation {
 	@Override
 	public Bundle execute(Context context, Request request)
 		throws ConnectionException, DataException, CustomRequestException {
 		String url = request.getString(RequestFactory.FEED_URL);
 
 		if (FeedsManager.isFeedAdded(context, url))
 			return null;
 
 		RSSReader reader = new RSSReader();
 
 		try {
 			RSSFeed rss_feed = reader.load(url);
 			List<RSSItem> items = rss_feed.getItems();
 
 			int unreadCount = Utils.getNewFeedKeepUnreadCount(context);
 
 			ContentValues values = new ContentValues();
 			values.put(Contract.FeedsColumns.TITLE, rss_feed.getTitle());
 			values.put(Contract.FeedsColumns.SUBTITLE, rss_feed.getSubtitle());
			values.put(Contract.FeedsColumns.ICON_URL, rss_feed.getIconUrl().toString());
			values.put(Contract.FeedsColumns.ICON, DownloadManager.downloadImage(rss_feed.getIconUrl().toString()));
 			values.put(Contract.FeedsColumns.URL, url);
 			values.put(Contract.FeedsColumns.UNREAD_ITEMS_COUNT, items.size() < unreadCount ? items.size() : unreadCount);
 
 			Uri feedUri = context.getContentResolver().insert(Contract.Feeds.CONTENT_URI, values);
 
 			long feedId = ContentUris.parseId(feedUri);
 			for (RSSItem item : items) {
 				FeedsManager.addFeedItem(context, feedId, item, unreadCount <= 0);
 				unreadCount--;
 			}
 		} catch (RSSReaderException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
