 /*
  * Copyright (C) 2013 Geometer Plus <contact@geometerplus.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  */
 
 package org.geometerplus.fbreader.widget;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.view.*;
 import android.widget.*;
 
 import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
 import org.geometerplus.fbreader.book.*;
 
 public class ConfigureActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
 	private final IBookCollection myCollection = new BookCollectionShadow();
 	
 	private static final int CHOOSE_BOOK = 0;
 	
 	private boolean withBar = true;
 	private boolean withPercent = false;
 	private boolean withCurrentBook = true;
 	
 	private int myAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
 	private Book myBook;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preferences);
 		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
 		
 		final Intent intent = getIntent();
 		final Bundle extras = intent.getExtras();
 		if (extras != null) {
 			myAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
 		}
 		if (myAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
             finish();
         }
 		
 		((BookCollectionShadow)myCollection).bindToService(this, new Runnable() {
 
 			@Override
 			public void run() {
 				myCollection.saveCovers();
				myBook = myCollection.getRecentBook(0);
 				setWidgetConf();
 			}
 
 		});	
 	}
 	
 	@Override
 	protected void onDestroy() {
 		((BookCollectionShadow)myCollection).unbind();
 		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
 		super.onDestroy();
 	}
 	
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		final SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
 		if (key.equals("with_bar")) {
 			withBar = preference.getBoolean("with_bar", false);
 		}
 		if (key.equals("with_percent")) {
 			withPercent = preference.getBoolean("with_percent", false);
 		}
 		if (key.equals("listpref")) {
 			String listPrefs = preference.getString("listpref", "Not set");
 			withCurrentBook = listPrefs.equalsIgnoreCase(getString(R.string.current));
 			if (withCurrentBook) {
 			} else {
 				Intent intent = new Intent(this, ChooseBookActivity.class);
 				startActivityForResult(intent, CHOOSE_BOOK);
 			}
 		}
		setWidgetConf();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_OK && requestCode == CHOOSE_BOOK) {
 			myBook = new XMLSerializer().deserializeBook(data.getStringExtra("book"));
 		}
 	}
 
 	private void setWidgetConf() {
 		final SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
 		withBar = preference.getBoolean("with_bar", false);
 		withPercent = preference.getBoolean("with_percent", false);
 
 		String imageInSD = Environment.getExternalStorageDirectory().toString() +
 				"/FBReaderJ/Covers/" + myBook.getId() + ".PNG";
 		final Bitmap cover = BitmapFactory.decodeFile(imageInSD);
 		
 		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
 		final RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget);
 
 		final int percent = 50;
 		if (null != cover) {
 			remoteViews.setBitmap(R.id.cover, "setImageBitmap", cover);
 		}
 		if (withBar) {
 			remoteViews.setViewVisibility(R.id.progress, View.VISIBLE);
 			remoteViews.setProgressBar(R.id.progress, 100, percent, false);
 		} else {
 			remoteViews.setViewVisibility(R.id.progress, View.GONE);
 		}
 		if (withPercent) {
 			remoteViews.setViewVisibility(R.id.percent_progress, View.VISIBLE);
 			remoteViews.setTextViewText(R.id.percent_progress, percent + "%");
 		} else {
 			remoteViews.setViewVisibility(R.id.percent_progress, View.GONE);
 		}
 
 		final Intent active = new Intent(this, FBReaderBookWidget.class);
 		active.setAction(FBReaderBookWidget.ACTION_WIDGET_RECEIVER);
 		active.putExtra("book", new XMLSerializer().serialize(myBook));
 		PendingIntent actionPendingIntent = PendingIntent.getBroadcast(this, myAppWidgetId, active, PendingIntent.FLAG_UPDATE_CURRENT);
 		remoteViews.setOnClickPendingIntent(R.id.Layout, actionPendingIntent);
 		
 		final Intent settings = new Intent(this, FBReaderBookWidget.class);
 		settings.setAction(FBReaderBookWidget.ACTION_WIDGET_CONFIGURE);
 		settings.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, myAppWidgetId);
 		PendingIntent actionPendingIntent2 = PendingIntent.getBroadcast(this, myAppWidgetId, settings, PendingIntent.FLAG_UPDATE_CURRENT);
 		remoteViews.setOnClickPendingIntent(R.id.config, actionPendingIntent2);
 		
 		appWidgetManager.updateAppWidget(myAppWidgetId, remoteViews);
 		
 		final Intent resultValue = new Intent();
 		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, myAppWidgetId);
 		setResult(RESULT_OK, resultValue);
 	}
 	
 	@Override
 	public void onBackPressed() {
 		finish();
 	}
 }
