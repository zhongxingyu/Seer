 package net.acuttone.reddimg.views;
 
 import net.acuttone.reddimg.R;
 import net.acuttone.reddimg.R.id;
 import net.acuttone.reddimg.R.layout;
 import net.acuttone.reddimg.R.menu;
 import net.acuttone.reddimg.core.ReddimgApp;
 import net.acuttone.reddimg.core.RedditClient;
 import net.acuttone.reddimg.core.RedditLink;
 import net.acuttone.reddimg.prefs.PrefsActivity;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class LinkViewerActivity extends Activity {
 
 	public static final String LINK_INDEX = "LINK_INDEX";
 	private int currentLinkIndex;
 	private ImageView viewBitmap;
 	private ImageView viewLeftArrow;
 	private ImageView viewRightArrow;
 	private TextView textViewTitle;
 	private AsyncTask<Void,Integer,Void> fadeTask;
 	private ImageView viewUpvote;
 	private ImageView viewDownvote;
 	private TextView textviewLoading;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.linkviewer);
 		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
 		textviewLoading = (TextView) findViewById(R.id.textviewLoading);
 		textviewLoading.setText("");
 		viewBitmap = (ImageView) findViewById(R.id.scrollViewLink).findViewById(R.id.imageViewLink);
 		viewUpvote = (ImageView) findViewById(R.id.imageupvote);
 		viewUpvote.setAlpha(0);
 		viewDownvote = (ImageView) findViewById(R.id.imagedownvote);
 		viewDownvote.setAlpha(0);
 		viewLeftArrow = (ImageView) findViewById(R.id.imageleftarrow);
 		viewRightArrow = (ImageView) findViewById(R.id.imagerightarrow);
 		viewLeftArrow.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(currentLinkIndex > 0) {
 					currentLinkIndex--;
 					loadImage();
 				}
 			}
 
 		});
 		viewRightArrow.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				currentLinkIndex++;
 				loadImage();
 			}
 		});
 		currentLinkIndex = getIntent().getExtras().getInt(LINK_INDEX);
 		loadImage();
 	}
 	
 	private void loadImage() {
 		AsyncTask<Integer, RedditLink, Object[]> loadTask = new AsyncTask<Integer, RedditLink, Object[]>() {
 
 			@Override
 			protected void onPreExecute() {
 				super.onPreExecute();
 				if(fadeTask != null) {
 					fadeTask.cancel(true);
 				}
				textviewLoading.setText("Loading links from "
						+ ReddimgApp.instance().getLinksQueue().getCurrentSubreddit());
 				viewLeftArrow.setAlpha(0);
 				viewRightArrow.setAlpha(0);
 				recycleBitmap();
 			}
 
 			@Override
 			protected Object[] doInBackground(Integer... params) {
 				RedditLink redditLink = ReddimgApp.instance().getLinksQueue().get(params[0]);
 				publishProgress(redditLink);
 				Bitmap bitmap = ReddimgApp.instance().getImageCache().getImage(redditLink.getUrl());
 				Object [] result = new Object[2];
 				result[0] = bitmap;
 				result[1] = redditLink;
 				return result;
 			}
 			
 			@Override
 			protected void onProgressUpdate(RedditLink... values) {
 				super.onProgressUpdate(values);
 				RedditLink link = values[0];
 				updateTitle(link);
 				textviewLoading.setText("Loading " + link.getUrl());
 			}
 
 			@Override
 			protected void onPostExecute(Object[] result) {
 				super.onPostExecute(result);
 				textviewLoading.setText("");
 				Bitmap bitmap = (Bitmap) ((Object []) result)[0];
 				RedditLink redditLink = (RedditLink) ((Object []) result)[1];
 				applyImage(bitmap, redditLink);
 			}
 		};
 		loadTask.execute(currentLinkIndex);
 	}
 
 	private void applyImage(Bitmap bitmap, RedditLink redditLink) {
 		viewBitmap.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
 		viewBitmap.setAdjustViewBounds(true);
 		viewBitmap.setImageBitmap(bitmap);
 		refreshVoteIndicators(redditLink); 
 		
 		fadeTask = new AsyncTask<Void, Integer, Void>() {
 
 			@Override
 			protected Void doInBackground(Void... params) {
 				for(int alpha = 256; alpha >= 0 && isCancelled() == false; alpha -= 4) {
 					try {
 						Thread.sleep(10);
 					} catch (InterruptedException e) { }
 					publishProgress(alpha);
 				}
 				return null;
 			}
 			
 			@Override
 			protected void onProgressUpdate(Integer... values) {
 				if(isCancelled() == false) {
 					viewLeftArrow.setAlpha(values[0]);
 					viewRightArrow.setAlpha(values[0]);
 				}
 				super.onProgressUpdate(values);
 			}
 			
 		};
 		fadeTask.execute(null);
 	}
 
 	private void refreshVoteIndicators(RedditLink redditLink) {
 		if(RedditClient.UPVOTE.equals(redditLink.getVoteStatus())) {
 			viewUpvote.setAlpha(255);
 			viewDownvote.setAlpha(0);
 		} else if(RedditClient.DOWNVOTE.equals(redditLink.getVoteStatus())) {
 			viewUpvote.setAlpha(0);
 			viewDownvote.setAlpha(255);
 		} else {
 			viewUpvote.setAlpha(0);
 			viewDownvote.setAlpha(0);
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		recycleBitmap();
 		
 		if(fadeTask != null) {
 			fadeTask.cancel(true);
 		}
 	}
 	
 	private void recycleBitmap() {
 		Drawable drawable = viewBitmap.getDrawable();
 		if (drawable instanceof BitmapDrawable) {
 		    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
 		    Bitmap bitmap = bitmapDrawable.getBitmap();
 		    viewBitmap.setImageBitmap(null);
 		    if(bitmap != null && bitmap.isRecycled() == false) {
 		    	bitmap.recycle();
 		    }
 		}
 	}
 
 	public void updateTitle(RedditLink link) {
 		StringBuilder sb = new StringBuilder();
 		SharedPreferences sp = ReddimgApp.instance().getPrefs();
 		if(sp.getBoolean("showScore", false)) {
 			sb.append("[" + link.getScore() + "] ");
 		}
 		sb.append(link.getTitle());
 		if(sp.getBoolean("showAuthor", false)) {
 			sb.append(" | by " + link.getAuthor());
 		}		
 		if(sp.getBoolean("showSubreddit", false)) {
 			sb.append(" in " + link.getSubreddit());
 		}
 		textViewTitle.setText(sb.toString());
 		int size = Integer.parseInt(sp.getString(PrefsActivity.TITLE_SIZE_KEY, "24"));
 		textViewTitle.setTextSize(size);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = new MenuInflater(this);
 		inflater.inflate(R.menu.menu_linkviewer, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		MenuItem upvoteItem = menu.findItem(R.id.menuitem_upvote);
 		MenuItem downvoteItem = menu.findItem(R.id.menuitem_downvote);
 		if(ReddimgApp.instance().getRedditClient().isLoggedIn()) {
 			upvoteItem.setEnabled(true);
 			downvoteItem.setEnabled(true);
 		} else {
 			upvoteItem.setEnabled(false);
 			downvoteItem.setEnabled(false);
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		RedditLink currentLink = null;
 		Intent intent = null;
 		switch (item.getItemId()) {
 		case R.id.menuitem_upvote:
 			currentLink = ReddimgApp.instance().getLinksQueue().get(currentLinkIndex);
 			ReddimgApp.instance().getRedditClient().vote(currentLink, RedditClient.UPVOTE);
 			refreshVoteIndicators(currentLink);
 			return true;
 		case R.id.menuitem_downvote:
 			currentLink = ReddimgApp.instance().getLinksQueue().get(currentLinkIndex);
 			ReddimgApp.instance().getRedditClient().vote(currentLink, RedditClient.DOWNVOTE);
 			refreshVoteIndicators(currentLink);
 			return true;
 		case R.id.menuitem_openimg:
 			currentLink = ReddimgApp.instance().getLinksQueue().get(currentLinkIndex);
 			String imageDiskPath = ReddimgApp.instance().getImageCache().getImageDiskPath(currentLink.getUrl());
 			Uri uri = Uri.parse("file://" + imageDiskPath);
 			intent = new Intent();
 			intent.setAction(Intent.ACTION_VIEW);
 			intent.setDataAndType(uri, "image/*");
 			startActivity(intent);
 			return true;
 		case R.id.menuitem_opencomments:
 			currentLink = ReddimgApp.instance().getLinksQueue().get(currentLinkIndex);
 			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentLink.getCommentUrl() + ".compact"));
 			startActivity(intent);
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 }
