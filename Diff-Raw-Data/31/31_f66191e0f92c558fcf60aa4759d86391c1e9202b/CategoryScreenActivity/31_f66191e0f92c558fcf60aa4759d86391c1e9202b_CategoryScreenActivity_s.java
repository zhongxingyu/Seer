 /*
  * Copyright (C) 2013 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.morphoss.jumble.frontend;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 
 import org.json.JSONException;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.NavUtils;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.GridLayout;
 import android.widget.ImageView;
 import android.widget.PopupWindow;
 
 import com.morphoss.jumble.BaseActivity;
 import com.morphoss.jumble.R;
 import com.morphoss.jumble.Util;
 import com.morphoss.jumble.database.JumbleCategoryTable;
 import com.morphoss.jumble.database.JumbleProvider;
 import com.morphoss.jumble.models.Category;
 import com.morphoss.jumble.models.ModelParser;
 
 public class CategoryScreenActivity extends BaseActivity {
 
 	/**
 	 * This class displays the categories and load them from the JSON file
 	 */
 	private static final String TAG = "CategoryActivity";
 	private GridLayout gridLayout;
 	private static CategoryGridAdapter pga;
 	private PopupWindow pwindowLevel;
 	private ImageView btnClosePopupLevel;
 	
 
 	/**
 	 * 
 	 * @return the category gridadapter
 	 */
 	public static int getCount() {
 		return pga.getCount();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_category);
 		gridLayout = (GridLayout) findViewById(R.id.gridView1);
 		gridLayout.setRowCount(2);
 		DisplayMetrics displaymetrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
 		int height = displaymetrics.heightPixels;
 		int width = displaymetrics.widthPixels;
 		pga = new CategoryGridAdapter(this, width, height);
 
 		new LoadCategoryTask().execute();
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		Intent intent = new Intent(this, MainActivity.class);
 		startActivity(intent);
 		finish();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		myApp.pauseMusic();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		myApp.resumeMusic();
 		final Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				// Show the popup window of an unlocked level after 0.5s = 500ms
				if (pga.newLevel) {
					PopupWindowLevel();
 				}
 
 			}
 		}, 500);
 	}
 
 	/**
 	 * This method creates a popup window when a level is unlocked
 	 */
 	public void PopupWindowLevel() {
 
 		try {
 
 			LayoutInflater inflater = (LayoutInflater) CategoryScreenActivity.this
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View layout = inflater.inflate(R.layout.popup_level,
 					(ViewGroup) findViewById(R.id.popup_element));
 			DisplayMetrics metrics = new DisplayMetrics();
 			getWindowManager().getDefaultDisplay().getMetrics(metrics);
 			pwindowLevel = new PopupWindow(layout, metrics.widthPixels,
 					metrics.heightPixels, true);
 			pwindowLevel.showAtLocation(layout, Gravity.CENTER, 0, 0);
 			btnClosePopupLevel = (ImageView) layout
 					.findViewById(R.id.btn_close_popup);
 			btnClosePopupLevel.setOnClickListener(cancel_popup_level);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private final OnClickListener cancel_popup_level = new OnClickListener() {
 		public void onClick(View v) {
			pga.newLevel = false;
 			pwindowLevel.dismiss();
 
 		}
 	};
 	@Override
 	protected void onStart() {
 		super.onStart();
 		myApp.playMusic("generale.ogg");
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private class LoadCategoryTask extends
 			AsyncTask<Void, Void, Collection<Category>> {
 
 		@Override
 		protected Collection<Category> doInBackground(Void... params) {
 
 			File file = new File(
 					Util.createInternalStorage(CategoryScreenActivity.this)
 							+ File.separator + "words.json");
 
 			try {
 				String json = Util.getStringFromFile(file);
 				Log.d(TAG, "Loaded json: " + json);
 				return ModelParser.readJsonData(json);
 
 			} catch (IOException e) {
 				Log.e(TAG, "Error reading file: " + e.getMessage());
 
 				return null;
 			} catch (JSONException e) {
 				Log.e(TAG, "Error reading file: " + e.getMessage());
 				return null;
 			}
 
 		}
 
 		@Override
 		public void onPostExecute(Collection<Category> result) {
 			if (result == null) {
 				Log.e(TAG, "Could not load categories!!!!!");
 				finish();
 				return;
 			}
 
 			try {
 				pga.setCategories(result);
 				gridLayout.removeAllViews();
 				for (int i = 0; i < pga.getCount(); i++) {
 					final int num = i;
 					View v = pga.getView(i, null, null);
 					gridLayout.addView(v, i);
 					pga.setLayout(v);
 					if (CategoryGridAdapter.getCategory(i).unlocked()) {
 						v.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								Intent intent = new Intent(
 										CategoryScreenActivity.this,
 										JumbleActivity.class);
 								intent.putExtra(JumbleActivity.CATEGORY_KEY,
 										num);
 								intent.putExtra(WinningActivity.AVATAR_ID,
 										AvatarActivity.id);
 								startActivity(intent);
 								finish();
 							}
 						});
 					}
 
 				}
 			} catch (Exception e) {
 				Log.w(TAG, "Exception", e);
 			}
 		}
 	}
 
 }
