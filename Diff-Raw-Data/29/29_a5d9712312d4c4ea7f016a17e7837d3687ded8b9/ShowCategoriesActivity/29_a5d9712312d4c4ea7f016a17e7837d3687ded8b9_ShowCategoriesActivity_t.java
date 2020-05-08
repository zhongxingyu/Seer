 package com.taperay.android.preview;
 
 import com.taperay.android.preview.R;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.view.View;
 
 public class ShowCategoriesActivity extends TapeRayActivity {
 	private static ContentManager contentManager;
 	private String[] categoryTitles;
 	private ProgressDialog dialog;
 
 	private void displayCategories() {
 		final ListView listView = (ListView) findViewById(R.id.list);
 		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(ShowCategoriesActivity.this,
 				android.R.layout.simple_list_item_1, android.R.id.text1, categoryTitles);
 
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				dialog = ProgressDialog.show(ShowCategoriesActivity.this,
 						getResources().getString(R.string.progress_dialog_header),
 						getResources().getString(R.string.loading_artworks),
 						true);
 				final int index = position;
 
 				new Thread(new Runnable() {
 					public void run() {
 						TapeRayApplication app = (TapeRayApplication) getApplication();
 						ContentManager contentManager = app.getContentManager();
 
 						try {
 							contentManager.selectCategory(index);
 						} catch (Exception e) {
 							displayNetworkErrorAndFinish();
 						}
 
 						if (dialog != null)
 							dialog.dismiss();
 
 						Intent i = new Intent(ShowCategoriesActivity.this, ShowArtworksActivity.class);
 						startActivity(i);
 					}
 				}).start();
 
 			}
 		});
 
 		listView.post(new Runnable() {
 			public void run() {
 				listView.setAdapter(adapter);
 			}
 		});
 	}
 
	/** Called when the activity is started or restarted. */
 	@Override
	protected void onStart() {
		super.onStart();
 
 		setContentView(R.layout.categories);
 		setTitle("TapeRay > " + getResources().getString(R.string.categories));
 
 		dialog = ProgressDialog.show(this,
 				getResources().getString(R.string.progress_dialog_header), 
 				getResources().getString(R.string.loading_data), 
 				true);
 
 		new Thread(new Runnable() {
 			public void run() {
 				TapeRayApplication app = (TapeRayApplication) getApplication();
 				contentManager = app.getContentManager();
 				try {
 					contentManager.loadData();
 				} catch (Exception e) {
 					displayNetworkErrorAndFinish();
 					return;
 				}
 
 				if (dialog != null) {
 					categoryTitles = contentManager.getCategoryTitles();
 					dialog.dismiss();
 					displayCategories();
 				}
 			}
 		}).start();
 	}
 
 }
