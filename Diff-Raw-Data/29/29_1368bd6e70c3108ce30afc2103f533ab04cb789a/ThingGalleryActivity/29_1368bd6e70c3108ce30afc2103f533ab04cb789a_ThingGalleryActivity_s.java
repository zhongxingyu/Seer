 /*   Copyright 2012 Mario Bhmer
  *
  *   Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0) 
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://creativecommons.org/licenses/by-nc-sa/3.0/
  */
 package com.blogspot.marioboehmer.thingibrowse;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Gallery;
 import android.widget.ProgressBar;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.blogspot.marioboehmer.thingibrowse.adapter.ThingGalleryAdapter;
 import com.blogspot.marioboehmer.thingibrowse.network.ThingRequester;
 import com.blogspot.marioboehmer.thingibrowse.network.ThingiverseHTMLParser;
 
 /**
  * Used to display a gallery view of all attached thing images.
  * 
  * @author Mario Bhmer
  */
 public class ThingGalleryActivity extends SherlockActivity {
 
 	private Gallery gallery;
 	private ProgressBar progressBar;
 	private static final String TAG = ThingGalleryActivity.class
 			.getSimpleName();
 	private String[] imageDetailPageUrls;
 	private String[] imageUrls;
 	private boolean loadLargeimages = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getSupportActionBar().setDisplayShowTitleEnabled(false);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		setContentView(R.layout.thing_gallery_activity);
 		progressBar = (ProgressBar) findViewById(android.R.id.empty);
 		gallery = (Gallery) findViewById(R.id.gallery);
 
 		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 		NetworkInfo wifiNetworkInfo = connManager
 				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		int currentScreenWidth = getWindowManager().getDefaultDisplay()
 				.getWidth();
 		loadLargeimages = (!wifiNetworkInfo.isConnected() || currentScreenWidth < 640) ? false
 				: true;
 
 		if (savedInstanceState == null) {
 			savedInstanceState = getIntent().getExtras();
 		}
 		if (savedInstanceState != null) {
 			imageUrls = savedInstanceState.getStringArray("imageUrls");
 			imageDetailPageUrls = savedInstanceState
 					.getStringArray("imageDetailPageUrls");
 			
 			if (imageUrls != null) {
 				showImages(Arrays.asList(imageUrls));
 			} else {
 				new AsyncTask<String, Void, List<String>>() {
 
 					@Override
 					protected List<String> doInBackground(String... params) {
 						List<String> imageUrls = new ArrayList<String>();
 						for (int x = 0; x < params.length; x++) {
 							String imageResponseHtml = null;
 							try {
 								imageResponseHtml = ThingRequester.getInstance(
 										getApplicationContext())
 										.getResponseHtml(params[x]);
 							} catch (IOException e) {
								Log.e(TAG, e.getMessage());
 							} catch (Exception e) {
								Log.e(TAG, e.getMessage());
 							}
 							if (imageResponseHtml != null) {
 								String imageUrl = null;
 								if (loadLargeimages) {
 									imageUrl = ThingiverseHTMLParser
 											.getLargeImageUrl(imageResponseHtml);
 								} else {
 									imageUrl = ThingiverseHTMLParser
 											.getMediumImageUrl(imageResponseHtml);
 								}
 								if (imageUrl != null) {
 									imageUrls.add(imageUrl);
 								}
 							}
 						}
 						return imageUrls;
 					}
 
 					protected void onPostExecute(List<String> result) {
 						imageUrls = result.toArray(new String[result.size()]);
 						showImages(result);
 					};
 
 				}.execute(imageDetailPageUrls);
 			}
 		}
 	}
 
 	private void showImages(List<String> imageUrls) {
 		ThingGalleryAdapter adapter = new ThingGalleryAdapter(
 				getApplicationContext());
 		adapter.notifyDataSetInvalidated();
 		adapter.setImageUrls(imageUrls);
 		adapter.notifyDataSetChanged();
 		gallery.setAdapter(adapter);
 		progressBar.setVisibility(View.GONE);
 		gallery.setVisibility(View.VISIBLE);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putStringArray("imageDetailPageUrls", imageDetailPageUrls);
 		outState.putStringArray("imageUrls", imageUrls);
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getSupportMenuInflater().inflate(R.menu.menu, menu);
 		menu.findItem(R.id.refresh).setVisible(false);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			startActivity(ActionBarHelper.getInstance().getHomeIntent(
 					getApplicationContext()));
 			return true;
 		case R.id.feedback:
 			startActivity(ActionBarHelper.getInstance().getFeedbackIntent(
 					getApplicationContext()));
 			return true;
 		case R.id.info:
 			startActivity(ActionBarHelper.getInstance().getInfoIntent(
 					getApplicationContext()));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
