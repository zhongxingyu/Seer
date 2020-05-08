 package net.acuttone.reddimg;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
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
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 
 public class GalleryActivity extends Activity {
 	public static final int PICS_PER_PAGE = 12;
 	private static final String PAGE_NUMBER = "PAGE_NUMBER";
 
 	private int thumbSize;
 	private int page;
 	private Paint paint;
 	private Random rnd;
 	private GridView gridView;
 	private AsyncTask<Integer, Void, Void> loadLinksTask;
 	private ImageView imgviewLeft;
 	private ImageView imgviewRight;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(R.layout.gallery);
 		
 		rnd = new Random();
 		page = savedInstanceState == null ? 0 : savedInstanceState.getInt(PAGE_NUMBER);
 		
 		gridView = (GridView) findViewById(R.id.gridview_gallery);
 		gridView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				Log.d(ReddimgApp.APP_NAME, "" + position);
 				Intent i = new Intent(getApplicationContext(), LinkViewerActivity.class);
 				i.putExtra(LinkViewerActivity.LINK_INDEX, page * PICS_PER_PAGE + position);
 				startActivity(i);
 			}
 		});
 		
 		imgviewLeft = (ImageView) findViewById(R.id.imageview_leftarrow);
 		imgviewLeft.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(page > 0) {
 					page--;
 					loadLinks();
 				}
 			}
 		});
 		imgviewRight = (ImageView) findViewById(R.id.imageview_rightarrow);
 		imgviewRight.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				page++;
 				loadLinks();
 			}
 		});
 		
 		loadLinks();
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putInt(PAGE_NUMBER, page);
 		super.onSaveInstanceState(outState);
 	}
 
 	public void loadLinks() {
 		int numCols = 2;
 		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			numCols = 4;
 		}
 		gridView.setNumColumns(numCols);
 		thumbSize = ReddimgApp.instance().getScreenW() / numCols;
 		
 		if(page == 0) {
 			imgviewLeft.setAlpha(0);
 		} else {
 			imgviewLeft.setAlpha(255);
 		}
 		
 		final List<GridItem> items = new ArrayList<GridItem>();
 		final ImageAdapter imageAdapter = new ImageAdapter(GalleryActivity.this, items);
 		gridView.setAdapter(imageAdapter);
 		cleanup();
 		loadLinksTask = new AsyncTask<Integer, Void, Void>() {
 
 			private ProgressDialog progressDialog;
 
 			@Override
 			protected void onPreExecute() {
 				progressDialog = ProgressDialog.show(GalleryActivity.this, "Reddimg", "Fetching links...");
 				super.onPreExecute();
 			}
 			@Override
 			protected Void doInBackground(Integer... params) {
 				int page = params[0];
 				int startPos = page * PICS_PER_PAGE;
 				int endPos = (page + 1) * PICS_PER_PAGE;
 				for (int i = startPos; i < endPos; i++) {
 					items.add(new GridItem(ReddimgApp.instance().getLinksQueue().get(i)));
 				}
 				publishProgress(null);
 				for (GridItem item : items) {
 					if(isCancelled()) {
 						break;
 					}
 					if(item != null && item.getRedditLink() != null) {
 						item.getRedditLink().prepareThumb(thumbSize);
 						publishProgress(null);
 					}
 				}
 				return null;
 			}
 
 			@Override
 			protected void onProgressUpdate(Void... values) {
 				super.onProgressUpdate(values);
 				if(isCancelled()) {
 					return;
 				}
 				if(progressDialog.isShowing()) {
 					progressDialog.dismiss();
 				}
 				imageAdapter.notifyDataSetChanged();
 			}
 		};
 		loadLinksTask.execute(page);
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
 		case R.id.menuitem_login:
 			if (ReddimgApp.instance().getRedditClient().isLoggedIn()) {
 				ReddimgApp.instance().getRedditClient().doLogout();
 			} else {
 				intent = new Intent(this, LoginActivity.class);
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
 	
 	private void cleanup() {
 		if(loadLinksTask != null) {
 			loadLinksTask.cancel(true);
 			int startPos = (page-1) * PICS_PER_PAGE;
 			int endPos = page * PICS_PER_PAGE;
			for (int i = startPos; i < endPos && i >= 0; i++) {
 				ReddimgApp.instance().getLinksQueue().get(i).setThumb(null);
 			}
 			((ImageAdapter) gridView.getAdapter()).notifyDataSetChanged();
 		}
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		cleanup();
 	}
 	
 	private class GridItem {
 		
 		private RedditLink redditLink;
 		private Bitmap placeholder;
 		public GridItem(RedditLink link) {
 			this.redditLink = link;
 		    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)); 
 		    placeholder = Bitmap.createBitmap(thumbSize, thumbSize, Bitmap.Config.ARGB_4444);
 		    paint = new Paint();
 		    paint.setColor(color);
 		    new Canvas(placeholder).drawRect(new Rect(0, 0, thumbSize, thumbSize), paint);
 		}
 		
 		public Bitmap getThumb() {
 			if(redditLink.getThumb() != null && redditLink.getThumb().isRecycled() == false) {
 				return redditLink.getThumb();
 			} else {
 				return placeholder;
 			}
 		}
 
 		public RedditLink getRedditLink() {
 			return redditLink;
 		}
 
 		public void setRedditLink(RedditLink redditLink) {
 			this.redditLink = redditLink;
 		}
 		
 		public void dispose() {
 			// TODO
 		}
 	}
 	
 	private class ImageAdapter extends BaseAdapter {
 		private Context context;
 		private List<GridItem> items;
 
 		public ImageAdapter(Context context, List<GridItem> items) {
 			this.context = context;
 			this.items = items;
 		}
 
 		@Override
 		public void notifyDataSetChanged() {
 			super.notifyDataSetChanged();
 		}
 
 		@Override
 		public int getCount() {
 			return items.size();
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view = convertView;
 			// todo: attempt to reuse convertView
 			LayoutInflater li = getLayoutInflater();
 			view = li.inflate(R.layout.grid_item, null);
 			ImageView iv = (ImageView) view.findViewById(R.id.grid_item_image);
 			iv.setImageBitmap(items.get(position).getThumb());
 			return view;
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int arg0) {
 			return 0;
 		}
 	}
 	
 }
