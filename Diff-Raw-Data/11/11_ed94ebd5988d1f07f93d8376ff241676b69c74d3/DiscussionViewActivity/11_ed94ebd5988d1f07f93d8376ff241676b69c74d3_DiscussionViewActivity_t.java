 package com.ph.tymyreader;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.SimpleAdapter;
 
 import com.ph.tymyreader.model.DiscussionPref;
 import com.ph.tymyreader.model.DsItem;
 
 // TODO Zobecnit nacitaki stranek, aby umely pracovat s vice strankami a nebylo jen na tahani diskuse
 
 // TODO Osetrit vypnuty internet
 
 
 public class DiscussionViewActivity extends ListActivity {
 
 	private static final int NEW_POST_ACTIVITY = 1;
 	private DiscussionPref dsPref;
 	final String CAP = "caption";
 	final String TEXT = "text";
 	private String[] from = new String[] {CAP, TEXT};
 	private int[] to = new int[] {R.id.text1, R.id.text2};
 	List<HashMap<String, String>> itemsList = new ArrayList<HashMap<String,String>>();
 	private SimpleAdapter adapter;
 	private TymyReader app;
 	private DownloadWebpageText loader;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.custom_list_activity_view);
 		app = (TymyReader) getApplication();
 		dsPref = app.getDsPref();
 		app.clearDsPref();
 
 		//dsPref = (DiscussionPref) getIntent().getSerializableExtra("dsPref");
 
 		final DiscussionPref data = (DiscussionPref) getLastNonConfigurationInstance();
 		if (data == null) {
 
 			dsPref.setDsItems(itemsList);
 			adapter = new SimpleAdapter(this, itemsList, R.layout.two_line_list_item, from, to);
 			setListAdapter(adapter);
 
 			loader = new DownloadWebpageText();
 			loader.execute(dsPref);
 		} else {
 			// after configuration change
 			dsPref = data;
 			itemsList = dsPref.getDsItems();
 			adapter = new SimpleAdapter(this, itemsList, R.layout.two_line_list_item, from, to);
 			setListAdapter(adapter);
 		}
 		setTitle( dsPref.getUrl() + " / " + dsPref.getName());
 
 	}
 
 	/**
 	 * Add caption-text pair into itemsList, if clear is true then before 
 	 * add the list is cleared.
 	 * 
 	 * @param clear true for clear itemsList before add
 	 * @param caption item caption
 	 * @param text item text 
 	 */
 	private void addItemsList(boolean clear, String caption, String text) {
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put(CAP, caption);
 		map.put(TEXT, text);
 		if (clear) { itemsList.clear(); }
 		itemsList.add(map);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_discussion_view, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_post:
 			showNewPost();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		final DiscussionPref data = dsPref;
 		return data;
 	}
 
 	private void showNewPost() {
 		app.setDsPref(dsPref);
 		Intent intent = new Intent(this, PostActivity.class);
 		startActivityForResult(intent, NEW_POST_ACTIVITY);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case NEW_POST_ACTIVITY:
 			if (resultCode == RESULT_OK) {
 				if (loader != null) loader.cancel(true);
 				loader = new DownloadWebpageText(); 
 				loader.execute(dsPref);
 			}
 		}
 	}
 
 	protected class DownloadWebpageText extends
 	AsyncTask<DiscussionPref, Integer, String> {
 		private ProgressDialog dialog = new ProgressDialog(DiscussionViewActivity.this);
 
 		@Override
 		protected void onPreExecute() {
 			dialog.setMessage(getString(R.string.loading));
 			dialog.show();
 		}
 		
 		@Override
 		protected String doInBackground(DiscussionPref... dsPref) {
 			TymyPageLoader page = new TymyPageLoader();
 			return page.loadDsPage(dsPref[0].getUrl(), dsPref[0].getId(), dsPref[0].getUser(), dsPref[0].getPass(), dsPref[0].getHttpContext());
 		}
 
 		protected void onProgressUpdate(Integer... progress) {
 			setProgress(progress[0]);
 		}
 
 		// onPostExecute displays the results of the AsyncTask.
 		@Override
 		protected void onPostExecute(String input) {
 			showList(input);
 			if(dialog.isShowing())
 			{
 				dialog.dismiss();
 			}
 		}
 
 		private void showList(String input) {
 			if ((input == null) || (input.length()) == 0 ) {
 				input = getString(R.string.empty_discussion);
 				addItemsList(true, input, "");	
 			} else {
 				DsItem dsItem = new DsItem();
 				TymyParser tParser = new TymyParser(input);
 				itemsList.clear();
 				int countNew = dsPref.getNewItems();
 				while ((dsItem = tParser.getDsItem()) != null) {
 					//Log.v(TAG, name + " " + post);
 					if (countNew > 0) {
 						addItemsList(false, "" + getString(R.string.new_item) + " " +dsItem.getDsCaption(), dsItem.getDsItemText());
 						countNew = countNew - 1;
 					} else {
 						addItemsList(false, dsItem.getDsCaption(), dsItem.getDsItemText());	
 					}
 				}
				dsPref.clearNewItems();
 			}
 			adapter.notifyDataSetChanged();
 
 			if (itemsList != null){
 				dsPref.setDsItems(itemsList);
 			}
 		}
 	}
 }
 
