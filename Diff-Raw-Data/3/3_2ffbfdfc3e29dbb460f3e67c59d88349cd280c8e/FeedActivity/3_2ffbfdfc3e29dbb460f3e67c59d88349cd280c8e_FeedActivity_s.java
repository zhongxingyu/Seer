 package com.saranomy.popconn;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.jinstagram.entity.comments.CommentData;
 import org.jinstagram.entity.users.feed.MediaFeed;
 import org.jinstagram.entity.users.feed.MediaFeedData;
 import org.jinstagram.exceptions.InstagramException;
 
 import twitter4j.Paging;
 import twitter4j.TwitterException;
 import android.app.Activity;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 import com.saranomy.popconn.core.InstagramCore;
 import com.saranomy.popconn.core.TwitterCore;
 import com.saranomy.popconn.item.Item;
 import com.saranomy.popconn.item.ItemAdapter;
 
 public class FeedActivity extends Activity {
 	private ListView activity_feed_listview;
 	private ProgressBar activity_feed_refresh;
 	private TwitterCore twitterCore;
 	private InstagramCore instagramCore;
 
 	private List<Item> items;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_feed);
 		syncViewById();
 		init();
 	}
 
 	private void syncViewById() {
 		activity_feed_listview = (ListView) findViewById(R.id.activity_feed_listview);
 		// activity_feed_listview.setOnRefreshListener(new OnRefreshListener() {
 		// @Override
 		// public void onRefresh() {
 		// init();
 		// }
 		// });
 		activity_feed_refresh = (ProgressBar) findViewById(R.id.activity_feed_refresh);
 	}
 
 	private void init() {
 		items = new ArrayList<Item>();
 		twitterCore = TwitterCore.getInstance();
 		instagramCore = InstagramCore.getInstance();
 
 		if (twitterCore.active) {
 			new LoadTwitterItem().execute();
 		} else {
 			new LoadInstagramItem().execute();
 		}
 	}
 
 	public class LoadTwitterItem extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			try {
 				getHomeTimeline(new Paging(1));
 			} catch (TwitterException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		private void getHomeTimeline(Paging paging) throws TwitterException {
 			List<twitter4j.Status> statuses = twitterCore.twitter
 					.getHomeTimeline(paging);
 			for (twitter4j.Status status : statuses) {
 				Item item = new Item();
 				item.socialId = 1;
 				item.name = status.getUser().getScreenName();
 				item.action = "@" + status.getUser().getName();
 				item.thumbnail_url = status.getUser().getProfileImageURL();
 				item.date = status.getCreatedAt().getTime() / 1000L;
 				item.time = countDown(item.date);
 				item.content = status.getText();
 
 				String comment = "";
 				if (status.isRetweet()) {
 					comment = status.getRetweetCount() + " Retweets ";
 				}
 				item.comment = comment;
 				items.add(item);
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			new LoadInstagramItem().execute();
 			super.onPostExecute(result);
 		}
 	}
 
 	public class LoadInstagramItem extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			if (instagramCore.active) {
 				try {
 					MediaFeed mediaFeed = instagramCore.instagram
 							.getUserFeeds();
 					List<MediaFeedData> mediaFeedData = mediaFeed.getData();
					for (int i = 0; i < 20; i++) {
 						MediaFeedData media = mediaFeedData.get(i);
 						Item item = new Item();
 						item.socialId = 2;
 						item.name = media.getUser().getUserName();
 						item.action = "";
 						if (media.getLocation() != null)
 							item.action = media.getLocation().getName();
 						item.thumbnail_url = media.getUser()
 								.getProfilePictureUrl();
 						item.date = Long.parseLong(media.getCreatedTime());
 						item.time = countDown(item.date);
 						item.image_url = media.getImages()
 								.getStandardResolution().getImageUrl();
 
 						StringBuffer sb = new StringBuffer();
 						sb.append(media.getLikes().getCount()).append(" likes\n");
 						List<CommentData> comments = media.getComments()
 								.getComments();
 						
 						for (CommentData comment : comments) {
 							sb.append(comment.getCommentFrom().getUsername())
 									.append(": ").append(comment.getText())
 									.append("\n");
 						}
 						item.comment = sb.toString();
 						items.add(item);
 					}
 				} catch (InstagramException e) {
 					e.printStackTrace();
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			ItemAdapter adapter = new ItemAdapter(inflater, items);
 			Collections.sort(items, Item.comparator);
 			activity_feed_listview.setAdapter(adapter);
 
 			activity_feed_refresh.setVisibility(View.GONE);
 			activity_feed_listview.setVisibility(View.VISIBLE);
 			// activity_feed_listview.onRefreshComplete();
 			super.onPostExecute(result);
 		}
 	}
 
 	public static String countDown(long time) {
 		long delta = (System.currentTimeMillis() / 60000) - (time / 60);
 		if (delta < 60)
 			return delta + "m";
 		else if (delta < 1440)
 			return Math.round(delta / 60) + "h";
 		else
 			return Math.round(delta / 1440) + "d";
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_feed, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		// TODO Auto-generated method stub
 		return super.onMenuItemSelected(featureId, item);
 	}
 }
