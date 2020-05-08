 package net.acuttone.reddimg.views;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.acuttone.reddimg.R;
 import net.acuttone.reddimg.core.ReddimgApp;
 import net.acuttone.reddimg.core.RedditLink;
 import net.acuttone.reddimg.prefs.PrefsActivity;
 import net.acuttone.reddimg.prefs.SubredditsPickerActivity;
 import net.acuttone.reddimg.prefs.SubredditsPrefsTab;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.AnimationSet;
 import android.view.animation.TranslateAnimation;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class GalleryActivity extends Activity implements OnSharedPreferenceChangeListener {
 	private static final String CURRENT = "CURRENT";
 
 	private int current;
 	private int thumbSize;
 	private boolean doReload;
 	private LoadThumbsTask loadThumbsTask;
 	private ImageAdapter imageAdapter;
 	private GridView gridView;
 	private Button btnLoadMore;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.gallery);
 		
 		current = savedInstanceState == null ? 0 : savedInstanceState.getInt(CURRENT);
 		
 		gridView = (GridView) findViewById(R.id.gridview_gallery);
 		gridView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				int linkIndex = imageAdapter.getItem(position).getLinkIndex();
 				Intent i = new Intent(getApplicationContext(), LinkViewerActivity.class);
 				i.putExtra(LinkViewerActivity.LINK_INDEX, linkIndex);
 				startActivity(i);
 			}
 		});
 		
 		int cols = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 5 : 3; 
 		thumbSize = ReddimgApp.instance().getScreenW() / cols;
 		gridView.setNumColumns(cols);
 		imageAdapter = new ImageAdapter(getBaseContext());
 		gridView.setAdapter(imageAdapter);		
 		btnLoadMore = (Button) findViewById(R.id.gridview_loadmore);
 		btnLoadMore.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				loadMore();
 			}
 		});
 		ReddimgApp.instance().getPrefs().registerOnSharedPreferenceChangeListener(this);
 		loadMore();
 	}
 
 	private void loadMore() {
 		btnLoadMore.setText("Loading...");
 		btnLoadMore.setEnabled(false);
 		if(loadThumbsTask != null) {
 			loadThumbsTask.cancel(true);
 		}
 		loadThumbsTask = new LoadThumbsTask();
 		loadThumbsTask.execute(current);
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		ReddimgApp.instance().stopLinksQueueTimer();
 		if(loadThumbsTask != null) {
 			loadThumbsTask.cancel(true);
 			loadThumbsTask = null;
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		ReddimgApp.instance().startLinksQueueTimer();
 		if(doReload) {
 			current = 0;
 			imageAdapter.clearItems();
 			loadMore();
 			doReload = false;
 		}
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		ReddimgApp.instance().getPrefs().unregisterOnSharedPreferenceChangeListener(this);
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putInt(CURRENT, current);
 		super.onSaveInstanceState(outState);
 	}
 	
 	private class LoadThumbsTask extends AsyncTask<Integer, GridItem, Integer> {
 
 		@Override
 		protected Integer doInBackground(Integer... arg0) {
 			int index = arg0[0];
 			for (int i = 0; i < 15 && isCancelled() == false; i++) {
 				try {
 					RedditLink link = null;
 					while (link == null) {
 						link = ReddimgApp.instance().getLinksQueue().get(index);
 						if(link == null) {
 							try { Thread.sleep(300); } catch (InterruptedException e) { }
 						}								
 					}
 					Bitmap thumb = ReddimgApp.instance().getImageCache().getImage(link.getThumbUrl());
 					Bitmap resized = resizeThumb(thumb);
 					if (resized != null) {
 						publishProgress(new GridItem(index, link, resized));
 					}
 				} catch (IOException e) {
 					Log.e(ReddimgApp.APP_NAME, e.toString());
 				}
 				index++;
 			}
 			return index;
 		}
 
 		@Override
 		protected void onProgressUpdate(GridItem... values) {
 			super.onProgressUpdate(values);
 			imageAdapter.addItem(values[0]);
 		}
 
 		@Override
 		protected void onPostExecute(Integer result) {
 			super.onPostExecute(result);
			current = result;
 			btnLoadMore.setText("Load more");
 			btnLoadMore.setEnabled(true);
 		}
 		
 		@Override
 		protected void onCancelled() {
 			super.onCancelled();
 			btnLoadMore.setText("Load more");
 			btnLoadMore.setEnabled(true);
 		}
 		
 		private Bitmap resizeThumb(Bitmap thumbBmp) {
 			if (thumbBmp != null && thumbBmp.getWidth() != thumbSize) {
 				try {
 					Bitmap bmpSquared = thumbBmp;
 					if (thumbBmp.getWidth() != thumbBmp.getHeight()) {
 						int minDim = Math.min(thumbBmp.getWidth(), thumbBmp.getHeight());
 						int x0 = (thumbBmp.getWidth() - minDim) / 2;
 						int x1 = (thumbBmp.getWidth() + minDim) / 2;
 						int y0 = (thumbBmp.getHeight() - minDim) / 2;
 						int y1 = (thumbBmp.getHeight() + minDim) / 2;
 						bmpSquared = Bitmap.createBitmap(thumbBmp, x0, y0, x1, y1);
 					}
 					return Bitmap.createScaledBitmap(bmpSquared, thumbSize, thumbSize, true);
 				} catch (Throwable e) {
 					Log.w(ReddimgApp.APP_NAME, e.toString());
 				}
 			}
 			return null;
 		}
 	}
 
 	private class GridItem {
 		
 		private RedditLink redditLink;
 		private Bitmap thumb;
 		private int linkIndex;
 		private boolean isNew;
 		
 		public GridItem(int linkIndex, RedditLink link, Bitmap thumb) {
 			this.linkIndex = linkIndex;
 			this.redditLink = link;
 			this.thumb = thumb;
 			this.isNew = true;
 		}
 		
 		public boolean isNew() {
 			return isNew;
 		}
 		
 		public void setNew(boolean isNew) {
 			this.isNew = isNew;
 		}
 		
 		public int getLinkIndex() {
 			return linkIndex;
 		}
 		
 		public RedditLink getRedditLink() {
 			return redditLink;
 		}
 
 		public Bitmap getThumb() {
 			return thumb;
 		}
 
 	}
 	
 	private class ImageAdapter extends BaseAdapter {
 		private List<GridItem> items;
 
 		public ImageAdapter(Context context) {
 			this.items = new ArrayList<GalleryActivity.GridItem>();
 		}
 
 		public void clearItems() {
 			items.clear();
 			notifyDataSetChanged();
 		}
 
 		public void addItem(GridItem itm) {
 			items.add(itm);
 			notifyDataSetChanged();
 		}
 		
 		@Override
 		public int getCount() {
 			return items.size();
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater li = getLayoutInflater();
 			View view = li.inflate(R.layout.grid_item, null);
 			ImageView iv = (ImageView) view.findViewById(R.id.grid_item_image);
 			Bitmap thumb = items.get(position).getThumb();
 			if(thumb != null) {
 				iv.setImageBitmap(thumb);
 			}
 			TextView tv = (TextView) view.findViewById(R.id.grid_item_text);
 			String title  = items.get(position).getRedditLink().getTitle();
 			if(title.length() > 30) {
 				title = title.substring(0, 30) + "...";
 			}
 			tv.setText(title);
 			if(items.get(position).isNew()) {
 				Animation tranAnim = new TranslateAnimation(0, 0f, 50f, 0f);
 				AlphaAnimation alphaAnim = new AlphaAnimation(0.5f, 1f);
 				AnimationSet as = new AnimationSet(true);
 				as.addAnimation(alphaAnim);
 				as.addAnimation(tranAnim);
 				as.setDuration(700);
 				view.startAnimation(as);
 				items.get(position).setNew(false);
 			}
 			return view;
 		}
 
 		@Override
 		public GridItem getItem(int pos) {
 			return items.get(pos);
 		}
 
 		@Override
 		public long getItemId(int pos) {
 			return items.get(pos).getLinkIndex();
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = new MenuInflater(this);
 		inflater.inflate(R.menu.menu_gallery, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		MenuItem loginItem = menu.findItem(R.id.menuitem_login);
 		if(ReddimgApp.instance().getRedditClient().isLoggedIn()) {
 			loginItem.setTitle("Logout");
 		} else {
 			loginItem.setTitle("Login");
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent = null;
 		switch (item.getItemId()) {
 		case R.id.menuitem_reload:
 			current = 0;
 			ReddimgApp.instance().getLinksQueue().initSubreddits();
 			imageAdapter.clearItems();
 			loadMore();
 			return true;
 		case R.id.menuitem_subreddits:
 			intent = new Intent(getBaseContext(), SubredditsPrefsTab.class);
 			startActivity(intent);
 			return true;
 		case R.id.menuitem_login:
 			if (ReddimgApp.instance().getRedditClient().isLoggedIn()) {
 				ReddimgApp.instance().getRedditClient().doLogout();
 			} else {
 				intent = new Intent(getBaseContext(), LoginActivity.class);
 				startActivity(intent);
 			}
 			return true;
 		case R.id.menuitem_settings:
 			intent = new Intent(this, PrefsActivity.class);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 	
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		if (SubredditsPickerActivity.SUBREDDITS_LIST_KEY.equals(key)) {
 			doReload = true;
 		}
 	}
 	
 }
