 package com.taperay.android.preview;
 
 import android.app.AlertDialog;
 import android.app.SearchManager;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class ShowArtworksActivity extends TapeRayActivity {
 
 	private ContentManager contentManager;
 	private String[] artworkTitles;
 
 	private void showArtworks() {
 		artworkTitles = contentManager.getArtworkTitles();
 		setTitle(contentManager.getCurrentTitle());
 
 		if (artworkTitles.length == 0) {
 			runOnUiThread(new Runnable() {
 				public void run() {
 					AlertDialog.Builder builder = new AlertDialog.Builder(ShowArtworksActivity.this);  
 					builder.setMessage(getResources().getString(R.string.no_results));  
 					builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {  
 						@Override
 						public void onClick(DialogInterface dialog, int which) {  
 							dialog.cancel();  
 							finish();
 						}  
 					});
 
 					AlertDialog alert = builder.create(); 
 					alert.show();
 				}
 			});
 		}
 
 		final ListView listView = (ListView) findViewById(R.id.list);
 		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(ShowArtworksActivity.this,
 				android.R.layout.simple_list_item_1, android.R.id.text1, artworkTitles);
 
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				Intent i = new Intent(ShowArtworksActivity.this, ShowPreviewActivity.class);
 				contentManager.selectArtwork(position);
 				startActivity(i);
 			}
 		});
 
 		listView.setAdapter(adapter);
 	}
 
	/** Called when the activity is started or restarted. */
 	@Override
	protected void onStart() {
		super.onStart();
 
 		setContentView(R.layout.artworks);
 
 		TapeRayApplication app = (TapeRayApplication) getApplication();
 		contentManager = app.getContentManager();
 
 		Intent intent = getIntent();
 		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 			final String query = intent.getStringExtra(SearchManager.QUERY);
 
 			new Thread(new Runnable() {
 				public void run() {
 					try {
 						contentManager.performSearch(query);
 					} catch (Exception e) {
 						e.printStackTrace();
 						displayNetworkErrorAndFinish();
 						return;
 					}
 
 					runOnUiThread(new Runnable() {
 						public void run() {
 							showArtworks();
 						}
 					});					
 				}
 			}).start();
 		} else {
 			showArtworks();
 		}
 	}
 }
