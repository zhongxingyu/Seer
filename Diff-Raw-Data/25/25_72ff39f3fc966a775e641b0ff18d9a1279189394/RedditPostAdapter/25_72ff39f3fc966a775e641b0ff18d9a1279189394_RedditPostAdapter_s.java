 package org.sgnn7.ourobo.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.sgnn7.ourobo.BrowserViewActivity;
 import org.sgnn7.ourobo.R;
 import org.sgnn7.ourobo.eventing.IChangeEventListener;
 import org.sgnn7.ourobo.util.HttpUtils;
 import org.sgnn7.ourobo.util.ImageCacheManager;
 import org.sgnn7.ourobo.util.LogMe;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewSwitcher;
 
 public class RedditPostAdapter extends BaseAdapter {
 	private static final String PARAMETER_SEPARATOR = "&";
 
 	private static final int DEFAULT_POST_COUNT = 24;
 	private static final String DEFAULT_SORTING_TYPE = "new";
 
 	private final List<RedditPost> redditPosts = new ArrayList<RedditPost>();
 
 	private final DownloadTaskFactory downloadTaskFactory;
 	private final Activity activity;
 
 	private final String baseUrl;
 	private final String dataLocationUrl;
 	private final String mobileBaseUrl;
	private final String mobileUrl;
 
 	public RedditPostAdapter(Activity activity, String baseUrl, String dataLocationUri, String mobileBaseUrl,
			String mobileUrl, IChangeEventListener finishedDownloadingListener) {
 		this.activity = activity;
 		this.downloadTaskFactory = createDownloadTaskFactory(finishedDownloadingListener);
 		this.baseUrl = baseUrl;
 		this.dataLocationUrl = dataLocationUri;
 		this.mobileBaseUrl = mobileBaseUrl;
		this.mobileUrl = mobileUrl;
 	}
 
 	private DownloadTaskFactory createDownloadTaskFactory(final IChangeEventListener finishedDownloadingListener) {
 		DownloadTaskFactory downloadTaskFactory = new DownloadTaskFactory() {
 			@Override
 			protected void onPostExecuteDownloadTask(List<RedditPost> results) {
 				if (!results.isEmpty()) {
 					addPosts(results);
 				} else {
 					Toast.makeText(activity, "Could not retrieve json data", Toast.LENGTH_LONG).show();
 				}
 				finishedDownloadingListener.handle();
 			}
 		};
 
 		return downloadTaskFactory;
 	}
 
 	public int getCount() {
 		int numberOfPosts;
 		numberOfPosts = redditPosts.size();
 		return numberOfPosts;
 	}
 
 	public Object getItem(int position) {
 		return redditPosts.get(position);
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		RedditPost redditPost = redditPosts.get(position);
 
 		View redditPostHolder = null;
 		// Does not work for some reason :(
 		// if (convertView == null) {
 		LogMe.i("Creating view: " + redditPost.getTitle());
 		redditPostHolder = activity.getLayoutInflater().inflate(R.layout.post_layout, parent, false);
 		// } else {
 		// redditPostHolder = convertView;
 		// }
 
 		setPostHolderValues(position, redditPost, redditPostHolder);
 		redditPostHolder.invalidate();
 		return redditPostHolder;
 	}
 
 	public void addPosts(final List<RedditPost> newRedditPosts) {
 		redditPosts.addAll(newRedditPosts);
 		LogMe.e("Posts set. Add Size: " + newRedditPosts.size() + ". Total: " + redditPosts.size());
 		notifyDataSetChanged();
 	}
 
 	public void refreshViews() {
 		LogMe.e("Clearing view");
 		redditPosts.clear();
 
 		downloadTaskFactory.newDownloadTask().execute(dataLocationUrl, getParameterString());
 	}
 
 	public void downloadMoreContent() {
 		LogMe.i("Lazy loading more content...");
 		downloadTaskFactory.newDownloadTask().execute(dataLocationUrl, getParameterString());
 	}
 
 	public String getLastPostId() {
 		return redditPosts.get(redditPosts.size() - 1).getName();
 	}
 
 	public void stopAllDownloads() {
 		ImageCacheManager.stopDownloads();
 	}
 
 	private void setPostHolderValues(int index, final RedditPost redditPost, View postHolder) {
 		TextView titleView = (TextView) postHolder.findViewById(R.id.post_title);
 		String title = sanitizeString(redditPost.getTitle().trim());
 		titleView.setText(title);
 
 		final UrlFileType fileType = HttpUtils.getFileType(redditPost.getUrl());
 
 		postHolder.setBackgroundDrawable(activity.getResources().getDrawable(
 				getBackgroundIdBasedOnTypeAndIndex(fileType, index)));
 		postHolder.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				stopAllDownloads();
 				activity.startActivity(getBrowserViewIntent(redditPost, fileType));
 			}
 		});
 
 		boolean isImageUrl = fileType.equals(UrlFileType.IMAGE);
 
 		ViewSwitcher thumbnailHolder = (ViewSwitcher) postHolder.findViewById(R.id.post_thumbnail_holder);
 		final ImageView thumbnail = (ImageView) thumbnailHolder.findViewById(R.id.post_thumbnail);
 
 		AsyncThumbnailLoader thumbnailLazyLoader = new AsyncThumbnailLoader(activity, postHolder, thumbnailHolder,
 				thumbnail, baseUrl);
 		if (isImageUrl) {
 			thumbnailLazyLoader.loadImage(redditPost.getUrl());
 		} else {
 			thumbnailLazyLoader.loadImage(redditPost.getThumbnail());
 		}
 
 		RelativeLayout scoreHolder = (RelativeLayout) postHolder.findViewById(R.id.post_score_holder);
 		scoreHolder.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				stopAllDownloads();
 
				String commentsUrl = mobileUrl + redditPost.getPermalink();
 				LogMe.e("Opening comments at: " + commentsUrl);
 
 				Intent targetIntent = new Intent(activity, BrowserViewActivity.class);
 				targetIntent.putExtra(BrowserViewActivity.LOCATION, commentsUrl);
 				activity.startActivity(targetIntent);
 			}
 		});
 
 		TextView scoreView = (TextView) scoreHolder.findViewById(R.id.post_score);
 		scoreView.setText("" + redditPost.getScore());
 	}
 
 	private String sanitizeString(String text) {
 		String sanitizedText = "NULL";
 		if (text != null && text.length() != 0) {
 			sanitizedText = text;
 		}
 		return sanitizedText;
 	}
 
 	private String getParameterString() {
 		Map<String, String> parameters = new HashMap<String, String>();
 		parameters.put("limit", "" + DEFAULT_POST_COUNT);
 		parameters.put("sort", DEFAULT_SORTING_TYPE);
 
 		if (getCount() != 0) {
 			parameters.put("after", getLastPostId());
 		}
 
 		String parameterString = "";
 		for (String parameterKey : parameters.keySet()) {
 			parameterString += PARAMETER_SEPARATOR + parameterKey + "=" + parameters.get(parameterKey);
 		}
 		parameterString = "?" + parameterString.substring(1);
 
 		LogMe.d("Parameters: " + parameterString);
 
 		return parameterString;
 	}
 
 	private Intent getBrowserViewIntent(final RedditPost redditPost, final UrlFileType fileType) {
 		Intent targetIntent = new Intent(activity, BrowserViewActivity.class);
 		targetIntent.putExtra(BrowserViewActivity.LOCATION, injectRedditMobileUrls(redditPost.getUrl()));
 		return targetIntent;
 	}
 
 	private String injectRedditMobileUrls(String url) {
 		return url.replace(baseUrl, mobileBaseUrl);
 	}
 
 	private int getBackgroundIdBasedOnTypeAndIndex(UrlFileType fileType, int index) {
 		int backdgoundId = R.drawable.gray_post_style;
 		if (index % 2 != 0) {
 			backdgoundId = R.drawable.blue_post_style;
 		}
 		return backdgoundId;
 	}
 }
