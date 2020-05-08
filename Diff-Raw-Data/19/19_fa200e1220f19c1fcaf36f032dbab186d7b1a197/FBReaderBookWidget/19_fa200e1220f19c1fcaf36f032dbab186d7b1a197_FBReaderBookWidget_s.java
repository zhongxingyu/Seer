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
 
 import android.appwidget.*;
 import android.content.*;
 import android.widget.RemoteViews;
 
 import org.geometerplus.fbreader.book.*;
 
 public class FBReaderBookWidget extends AppWidgetProvider {
 	static final String WIDGET_OPEN_BOOK_ACTION = "fbreader.widget.open_book";
 	static final String WIDGET_CONFIGURE_ACTION = "fbreader.widget.configure";
 	static final String FBREADER_OPEN_BOOK_ACTION = "android.fbreader.action.VIEW";
 	static final String FBREADER_BOOK_EVENT_ACTION = "fbreader.library_service.book_event";
 	static final String BOOK_KEY = "fbreader.book";
 	static final String SHARED_PREFERENCES_NAME = "fbreader.widget.";
 	
 	static final int SINGLE_CURRENT_COVER_VIEW_TYPE = 0;
 	static final int SINGLE_CHOSEN_COVER_VIEW_TYPE = 1;
 	
 	private Book myBookFromEvent;
 	
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
 		super.onUpdate(context, appWidgetManager, appWidgetIds);
 		for (int i : appWidgetIds) {
 			updateWidget(context, appWidgetManager, i);
 		}
 	}
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		final String action = intent.getAction();
 		
 		if (FBREADER_BOOK_EVENT_ACTION.equals(action)) {
 			final String type = intent.getStringExtra("type");
			if (BookEvent.valueOf(type) == BookEvent.Updated) {
				myBookFromEvent = SerializerUtil.deserializeBook(intent.getStringExtra("book"));
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			    ComponentName thisAppWidget = new ComponentName(context.getPackageName(), this.getClass().getName());
			    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
			    onUpdate(context, appWidgetManager, appWidgetIds);
 			}
 		}
 		
 		if (WIDGET_OPEN_BOOK_ACTION.equals(action)) {
 			final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);	
 			final String sharedPreferensesName = SHARED_PREFERENCES_NAME + appWidgetId;
 			final SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferensesName, Context.MODE_PRIVATE);
 			final String bookXML = sharedPreferences.getString("book", null);
 			if (bookXML == null) {
 				return;
 			}
 			final Book book = new XMLSerializer().deserializeBook(bookXML);
 			if (book == null) {
 				return;
 			}
 			context.startActivity(new Intent(FBREADER_OPEN_BOOK_ACTION).putExtra(
 					BOOK_KEY, SerializerUtil.serialize(book)).addFlags(
 					Intent.FLAG_ACTIVITY_NEW_TASK));
 		}
 
 		if (WIDGET_CONFIGURE_ACTION.equals(action)) {
 			final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
 			final Intent settings = new Intent(context, ConfigureActivity.class);
 			settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			settings.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
 			context.startActivity(settings);
 		}
 		super.onReceive(context, intent);
 	}
 
 	private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
 		final String sharedPreferensesName = SHARED_PREFERENCES_NAME + appWidgetId;
 		SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferensesName, Context.MODE_PRIVATE);
 		final String bookXML = sharedPreferences.getString("book", null);
 		final int type = sharedPreferences.getInt("type", 0);
 		if (bookXML == null) {
 			return;
 		}
 		final Book book = new XMLSerializer().deserializeBook(bookXML);
 		if (book == null) {
 			return;
 		}
 		final RemoteViews remoteViews = new WidgetRemoteViewsHolder().setRemoteViews(context, appWidgetId, book);
 		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
 	}
 }
