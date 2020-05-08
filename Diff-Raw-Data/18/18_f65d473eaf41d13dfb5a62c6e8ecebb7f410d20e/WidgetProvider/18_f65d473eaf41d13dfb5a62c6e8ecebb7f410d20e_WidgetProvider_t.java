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
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Build;
 import android.view.View;
 import android.widget.RemoteViews;
 
 import org.geometerplus.fbreader.book.*;
 
 public class WidgetProvider extends AppWidgetProvider {
 	static final String FBREADER_BOOK_EVENT = "fbreader.library_service.book_event";
 	protected Book myBookFromEvent;
 	protected boolean myUpdateWithoutForce;
 	protected boolean myBookRemoved;
 
 	@Override
 	public void onEnabled(Context context) {
 		super.onEnabled(context);
 		startUpdate(context);
 	}
 
 	@Override
 	public void onDeleted(Context context, int[] appWidgetIds) {
 		for (int id : appWidgetIds) {
 			SharedPreferencesManager.deleteSharedPreferences(context, id);
 		}
 		super.onDeleted(context, appWidgetIds);
 	}
 
 	@Override
 	public void onDisabled(Context context) {
 		ImageManager.deleteAllCovers();
 		super.onDisabled(context);
 	}
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
 		super.onUpdate(context, appWidgetManager, appWidgetIds);
 		for (int appWidgetId : appWidgetIds) {
 			updateWidget(context, appWidgetManager, appWidgetId);
 		}
 	}
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		super.onReceive(context, intent);
 		if (intent.getAction().equals(FBREADER_BOOK_EVENT)) {
 			if (prepareToUpdate(intent)) {
 				startUpdate(context);
 			}
 		} else {
 			WidgetAction.makeAction(context, intent);
 		}
 	}
 	
 	protected boolean prepareToUpdate(Intent intent) {
 		final String type = intent.getStringExtra("type");
 		final BookEvent bookEvent = BookEvent.valueOf(type);
 		if (bookEvent != BookEvent.Updated && bookEvent != BookEvent.Opened) {
 			if (bookEvent == BookEvent.Removed) {
 				myBookRemoved = true;
 			} else {
 				return false;
 			}
 		}
 		myBookFromEvent = SerializerUtil.deserializeBook(intent.getStringExtra("book"));
 		myUpdateWithoutForce = true;
 		return true;
 	}
 
 	protected void startUpdate(Context context) {
 		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
 		final ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
 		final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
 		onUpdate(context, appWidgetManager, appWidgetIds);
 	}
 
 	protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
 		final Widget widget;
 		final int viewType = SharedPreferencesManager.getType(context, appWidgetId);
 		if (viewType != Widget.MULTIPLY) {
 			widget = SingleWidget.create(context, appWidgetId);
 		} else {
 			widget = MultiplyWidget.create(context, appWidgetId);
 		}
 		if (widget == null) {
 			return;
 		}
 		if (myUpdateWithoutForce) {
 			if (myBookRemoved) {
 				if (widget instanceof MultiplyWidget) {
 					if (!((MultiplyWidget)widget).deleteOneOfBooks(myBookFromEvent)) {
 					return;
 					}
 				} else if (widget.getViewType() == Widget.CHOSEN_BOOK) {
					//TODO: say somehow that book has gone
 					RemoteViews rm = new RemoteViews(context.getPackageName(), R.layout.stack_view_item);
 					final Bitmap cover = BitmapFactory.decodeResource(context.getResources(), R.drawable.null_cover);
 					rm.setImageViewBitmap(R.id.cover, cover);
 					rm.setViewVisibility(R.id.percent_progress, View.GONE);
 					rm.setViewVisibility(R.id.progress, View.GONE);
 					appWidgetManager.updateAppWidget(appWidgetId, rm);
 					return;
 				}
 			} else if (!widget.updateBook(myBookFromEvent)) {
 				return;
 			}
 		}
 		widget.writeDataToFile(context);
 		final RemoteViews remoteViews = widget.getRemoteViews(context);
 		if (remoteViews == null) {
 			return;
 		}
 		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
 		if (context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.HONEYCOMB) {
 			appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.stack_widget_view);
 		}
 	}
 }
