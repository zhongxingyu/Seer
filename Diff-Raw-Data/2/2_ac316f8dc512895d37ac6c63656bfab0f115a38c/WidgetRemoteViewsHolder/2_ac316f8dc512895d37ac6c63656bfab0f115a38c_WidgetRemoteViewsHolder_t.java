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
 
 import android.annotation.TargetApi;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Build;
 import android.os.Environment;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.RemoteViews;
 
 import org.geometerplus.fbreader.book.Book;
 import org.geometerplus.zlibrary.core.util.RationalNumber;
 
 public class WidgetRemoteViewsHolder {
 	private RemoteViews myRemoteViews;
 	private Widget myWidget;
 	private Context myContext;
 	
 	RemoteViews setRemoteViews(Context context, MultiplyWidget widget) {
 		myContext = context;
 		myWidget = widget;
 		myRemoteViews = new RemoteViews(context.getPackageName(), R.layout.stack_view);
 		setIntents(false, -1);
 		return myRemoteViews;
 	}
 	
 	RemoteViews setRemoteViews(Context context, SingleWidget widget, int whatPartOfMultiply) {
 		myContext = context;
 		myRemoteViews = new RemoteViews(context.getPackageName(), R.layout.stack_view_item);
 		myWidget = widget;
 		setCover(widget.getBook());
 		RationalNumber rationalNumber = widget.getBook().getProgress();
 		if (rationalNumber == null) {
 			rationalNumber = RationalNumber.create(0, 1);
 		}
 		setProgress(rationalNumber);
 		setIntents(true, whatPartOfMultiply);
 		return myRemoteViews;
 	}
 
 	private void setProgress(RationalNumber rationalNumber) {
 		final int percent = Math.round(100 * rationalNumber.toFloat());
 		setBar(percent);
 		setTextView(percent);
 	}
 
 	private void setTextView(int percent) {
 		if (myWidget.getWithPercent()) {
 			myRemoteViews.setViewVisibility(R.id.percent_progress, View.VISIBLE);
 			myRemoteViews.setTextViewText(R.id.percent_progress, percent + "%");
 		} else {
 			myRemoteViews.setViewVisibility(R.id.percent_progress, View.GONE);
 		}
 	}
 
 	private void setBar(int percent) {
 		if (myWidget.getWithBar()) {
 			myRemoteViews.setViewVisibility(R.id.progress, View.VISIBLE);
 			myRemoteViews.setProgressBar(R.id.progress, 100, percent, false);
 		} else {
 			myRemoteViews.setViewVisibility(R.id.progress, View.GONE);
 		}
 	}
 
 	private void setCover(Book book) {
 		Bitmap cover = ImageManager.getImage(book);
 		if (cover == null) {
 			cover = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.null_cover);
 			myRemoteViews.setViewVisibility(R.id.title, View.VISIBLE);
 			myRemoteViews.setTextViewText(R.id.title, book.getTitle());
 		} else {
 			myRemoteViews.setViewVisibility(R.id.title, View.GONE);
 		}
 		myRemoteViews.setImageViewBitmap(R.id.cover, cover);
 	}
 	
 	private void setIntents(boolean isSingle, int whatPartOfMultiply) {
 		if (isSingle) {
 			setOpenBookIntent(whatPartOfMultiply);
 		} else {
 			setConfigureIntent();
 			setCommonIntent();
 		}
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setOpenBookIntent(int whatPartOfMultiply) {
 		final Intent intent = new Intent(myContext, WidgetProvider.class);
 		intent.setAction(WidgetAction.OPEN_BOOK);
 		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, myWidget.getId());
 		if (whatPartOfMultiply == -1) {
 			final PendingIntent pendingIntent = PendingIntent.getBroadcast(myContext, myWidget.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
 			myRemoteViews.setOnClickPendingIntent(R.id.cover, pendingIntent);
 		} else {
 			//@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 			intent.putExtra(PreferenceKey.EXTRA_ITEM, whatPartOfMultiply);
 			myRemoteViews.setOnClickFillInIntent(R.id.cover, intent);
 		}
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setCommonIntent() {
 		final Intent intent = new Intent(myContext, WidgetProvider.class);
 		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, myWidget.getId());
 		final PendingIntent pendingIntent = PendingIntent.getBroadcast(myContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		myRemoteViews.setPendingIntentTemplate(R.id.stack_widget_view, pendingIntent);
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setConfigureIntent() {
 		final Intent intent = new Intent(myContext, StackWidgetService.class);
 		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, myWidget.getId());
 		myRemoteViews.setRemoteAdapter(myWidget.getId(), R.id.stack_widget_view, intent);
 	}
 }
