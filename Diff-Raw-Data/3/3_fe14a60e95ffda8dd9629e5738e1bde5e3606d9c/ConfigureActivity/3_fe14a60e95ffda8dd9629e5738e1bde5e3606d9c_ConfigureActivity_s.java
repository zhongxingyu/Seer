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
 
 import android.app.Activity;
 import android.app.Application;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.*;
 import android.widget.*;
 
 import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
 import org.geometerplus.fbreader.book.*;
 
 public class ConfigureActivity extends Activity {
 	private final IBookCollection myCollection = new BookCollectionShadow();
 	
 	private static final int CHOOSE_BOOK = 0;
 	
 	private boolean withBar;
 	private boolean withPercent;
 	private boolean withCurrentBook;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.widget_configure);
 		((BookCollectionShadow)myCollection).bindToService(this, null);
 	}
 	
 	@Override
 	protected void onDestroy() {
 		((BookCollectionShadow)myCollection).unbind();
 		super.onDestroy();
 	}
 	
 	public void onClick(View view) {
 		RadioButton rb = (RadioButton)findViewById(R.id.current_book);
 		withCurrentBook = rb.isChecked();
 		if (withCurrentBook) {
 			setWidgetConf(myCollection.getRecentBook(0));
 		} else {
 			Intent intent = new Intent(this, ChooseBookActivity.class);
 			startActivityForResult(intent, CHOOSE_BOOK);
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_OK && requestCode == CHOOSE_BOOK) {
			myCollection.saveCovers();
 			Book book = new XMLSerializer().deserializeBook(data.getStringExtra("book"));
 			setWidgetConf(book);
 		}
 	}
 
 	private void setWidgetConf(Book book) {
 		withBar = ((CheckBox)findViewById(R.id.with_bar)).isChecked();
 		withPercent = ((CheckBox)findViewById(R.id.with_percent)).isChecked();
 		
 		String imageInSD = Environment.getExternalStorageDirectory().toString() +
 				"/FBReaderJ/Covers/" + book.getId() + ".PNG";
 		final Bitmap cover = BitmapFactory.decodeFile(imageInSD);
 		
 		if(cover == null) {
 			System.out.println("Cover not found");
 			finish();
 		}
 		
 		final Intent intent = getIntent();
 		final Bundle extras = intent.getExtras();
 		
 		int appWidgetId = 0;
 		if (extras != null) {
 			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
 		}
 		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
 		final RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget);
 
 		final int percent = 50;
 		
 		if (withBar) {
 			remoteViews.setViewVisibility(R.id.progress, View.VISIBLE);
 			remoteViews.setProgressBar(R.id.progress, 100, percent, false);
 		}
 		
 		if (withPercent) {
 			remoteViews.setViewVisibility(R.id.percent_progress, View.VISIBLE);
 			remoteViews.setTextViewText(R.id.percent_progress, percent + "%");
 		}
 		
 		final Intent active = new Intent(this, FBReaderBookWidget.class);
 		active.setAction(FBReaderBookWidget.ACTION_WIDGET_RECEIVER);
 		active.putExtra("book", new XMLSerializer().serialize(book));
 		remoteViews.setBitmap(R.id.cover, "setImageBitmap", cover);
 
 		final PendingIntent actionPendingIntent = PendingIntent.getBroadcast(this, appWidgetId, active, PendingIntent.FLAG_UPDATE_CURRENT);
 		remoteViews.setOnClickPendingIntent(R.id.Layout, actionPendingIntent);
 		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
 		
 		final Intent resultValue = new Intent();
 		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
 		setResult(RESULT_OK, resultValue);
 		finish();
 	}
 }
