 package com.isd.isd;
 
 import java.util.ArrayList;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 
 public class NewsActivity extends ListActivity {
 	private String category;
 	private NewsData data;
 	private NewsAdapter adapter;
 	
	private final int LOADING_DIALOG_ID = 0;
	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		category = getIntent().getCharSequenceExtra("category").toString();
 		loadData();
 		
 		// Fill data
     	adapter = new NewsAdapter(this, new ArrayList<Story>());
     	setListAdapter(adapter);
     	getListView().setScrollbarFadingEnabled(true);
 	}
     
 	@Override
 	protected Dialog onCreateDialog(int id) {
		if (id == LOADING_DIALOG_ID) {
			ProgressDialog pd = new ProgressDialog(this);
			pd.setTitle("Loading");
			pd.setMessage("Just a second...");
			pd.setIndeterminate(true);
			
			return pd;
		}
		else
			return null;
 	}
 	
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
     	Intent i = new Intent(this, StoryActivity.class);
     	i.putExtra("category", category);
     	i.putExtra("index", position);
        
         startActivity(i);
     }
     
     public void loadData() {
     	LoadDataTask ldt = new LoadDataTask();
     	ldt.execute();
     }
     
 	
 	private class LoadDataTask extends AsyncTask<Void, Void, Void> {
 		
 		protected void onPreExecute() {
			showDialog(LOADING_DIALOG_ID);
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			data = NewsData.getNewsData(category, true);
 			// Load the first full story, so we have the image
 			data.loadedStoryAtIndex(0);
 			return null;
 		}
 		
 		protected void onPostExecute(Void v) {
 	    	adapter.setStories(data.getStories());
 	    	adapter.notifyDataSetChanged();
	    	try {
	    		dismissDialog(LOADING_DIALOG_ID);
	    	} catch (IllegalArgumentException e) {
	    		Log.d("isd", "Problem dismissing dialog: " + e.getMessage());
	    	}
 		}
 	}
 
 }
