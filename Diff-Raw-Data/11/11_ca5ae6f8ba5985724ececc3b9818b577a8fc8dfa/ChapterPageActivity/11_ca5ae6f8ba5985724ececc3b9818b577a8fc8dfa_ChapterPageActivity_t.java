 package org.ags.lparchive.page;
 
 import org.ags.lparchive.DataHelper;
 import org.ags.lparchive.LPArchiveApplication;
 import org.ags.lparchive.R;
 import org.ags.lparchive.RetCode;
 import org.ags.lparchive.model.Chapter;
 import org.ags.lparchive.task.PageFetchTask;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 /**
  * Loads a page of a LP with chapter specific navigation menu options.
  */
 public class ChapterPageActivity extends PageActivity {
 	public static final int MENU_PREVIOUS = 0;
 	public static final int MENU_NEXT = 0;
 	
 	private Cursor cursor;
 	private String lpUrl;
 	private long lpId;
 	private int position;
 	private DataHelper dh;
 	
 	/**
 	 * Creates an Intent to launch a ChapterPageActivity for a LP.
 	 * 
 	 * @param lpUrl
 	 *            The URL for the LP. eg.
 	 *            http://lparchive.org/Final-Fantasy-IX/
 	 * @param lpId
 	 *            The Let's Play ID in the DB.
 	 * @return The created intent.
 	 */
 	public static Intent newInstance(Context context, String lpUrl, long lpId) {
 		return newInstance(context, lpUrl, lpId, 0);
     }
 	
 	/**
 	 * Creates an Intent to launch a ChapterPageActivity for a LP.
 	 * 
 	 * @param lpUrl
 	 *            The URL for the LP. eg. http://lparchive.org/Final-Fantasy-IX/
 	 * @param lpId
 	 *            The Let's Play ID in the DB.
 	 * @param position
 	 *            Chapter number to view.
 	 * @return The created intent.
 	 */
 	public static Intent newInstance(Context context, String lpUrl, long lpId, int position) {
 		Intent i = new Intent(context, ChapterPageActivity.class);
         i.putExtra("lpUrl", lpUrl);
         i.putExtra("position", position);
         i.putExtra("lpId", lpId);
         return i;
     }
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (savedInstanceState == null) {
 			Bundle extras = getIntent().getExtras();
 			lpUrl = extras.getString("lpUrl");
 			position = extras.getInt("position");
 			lpId = extras.getLong("lpId");
 			
 			dh = ((LPArchiveApplication) getApplicationContext())
 					.getDataHelper();
 			
 			cursor = dh.getChapters(lpId);
 
 			loadPage();
 		}
 	}
 	
 	protected void loadPage() {
 		// ensure position points to somewhere valid
 		if (cursor.moveToPosition(position)) {
 			long id = cursor.getLong(0);
 			Chapter c = dh.getChapter(id);
 			boolean isIntro = c.isIntro();
 			String pageUrl = (isIntro) ? lpUrl : lpUrl + c.getUrl();
			Log.d("LPA", "pageURL" + pageUrl);
 			new LoadPageFetchTask(this, pageUrl, isIntro).execute();
 		} else {
 			Log.e("LPA", "error moving to position");
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options_menu_page_chapter, menu);
 		return true;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// enable/disable the previous/next buttons depending on chapter
 		menu.getItem(MENU_PREVIOUS).setEnabled(position != 0);
 		menu.getItem(MENU_NEXT).setEnabled(cursor.getCount() - 1 != position);
 
 		return super.onPrepareOptionsMenu(menu);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    // reload current chapter
 	    case R.id.refresh_page:
 	    	loadPage();
 	    	return true;
     	// load previous chapter
 	    case R.id.prev_chapter:
 	    	position--;
 	    	loadPage();
 	    	return true;
     	// load next chapter
 	    case R.id.next_chapter:
 	    	position++;
 	    	loadPage();
 	    	return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 
 	/** Loads page into this activities Webview. */
 	class LoadPageFetchTask extends PageFetchTask {
 		public LoadPageFetchTask(Activity activity, String url, boolean isIntro) {
 			super(activity, url, isIntro);
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		protected void onPostExecute(RetCode result) {
 			super.onPostExecute(result);
 			if (result.equals(RetCode.SUCCESS)) {
 				webview.loadDataWithBaseURL(url, html, "text/html", "utf-8",
 						null);
 			}
 		}
 	}
 }
