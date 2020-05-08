 package com.timvdalen.gizmooi;
 
 import java.io.IOException;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.annotation.SuppressLint;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProviderInfo;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.WindowManager;
 import android.widget.RemoteViews;
 
 /**
  * Runnable that loads a Photo and places it in a widget
  */
 public class PhotoLoaderRunnable implements Runnable{
 	//* The Context to use for creating Intents
 	private Context context;
 	
 	//* The ID of the widget to update
 	private int appWidgetId;
 	
 	//* The AppWidgetManager to update the widget on
 	private AppWidgetManager appWidgetManager;
 	
 	//* Photo to display
 	private Photo photo;
 	
 	//* If this is >1, use sampling for the Photo
 	private int sample;
 
 	public PhotoLoaderRunnable(Context context, int appWidgetId, AppWidgetManager appWidgetManager){
 		this.context = context;
 		this.appWidgetId = appWidgetId;
 		this.appWidgetManager = appWidgetManager;
 		
 		this.photo = null;
 		this.sample = 1;
 	}
 
 	@Override
 	public void run(){
 		// Determine screen size in pixels
 		DisplayMetrics dm = new DisplayMetrics();
 		((WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
 		
 		RemoteViews views = new RemoteViews(context.getPackageName(), this.getBaseLayout());
 		
 		try{
 			if(this.photo == null){
 				// Grab a Photo
 				this.photo = FlickrAPIHandler.getPhoto();
 			}
 			
 			// Download the image
 			Bitmap bmpPhoto = this.photo.getBitmap(dm, this.sample);
 			// Set the image
 			views.setImageViewBitmap(R.id.imgMooi, bmpPhoto);
 			
 			// Setup click handler
 			Intent intent = new Intent(this.context, ToastShowActivity.class);
 			intent.putExtra("title", this.photo.getTitle());
 			intent.putExtra("owner", this.photo.getOwner());
			PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, intent, 0);
 			
 			views.setOnClickPendingIntent(R.id.imgMooi, pendingIntent);
 			
 			// Update the widget
 			this.appWidgetManager.updateAppWidget(this.appWidgetId, views);
 		}catch(java.lang.IllegalArgumentException e){
 			// We're out of memory, try it again with a higher sampling value!
 			// To anyone reading this: I realize this is horrible but I can't find a way of detecting if this will happen.
 			// I'm open to suggestions
 			this.sample *= 2;
 			
 			this.run();
 		}catch(IOException e){
 			// We'll try again tomorrow, just use the Photo that's currently set
 			return;
 		}catch(XmlPullParserException e){
 			// We'll try again tomorrow, just use the Photo that's currently set
 			return;
 		}
 	}
 	
 	@SuppressLint("NewApi")
 	private int getBaseLayout(){
 		//Decide whether or not we're running on the lockscreen and set the base layout accordingly
 		boolean isKeyguard;
 		if(Build.VERSION.SDK_INT < 17){
 			// Lockscreen widgets aren't supported so this is a homescreen widget
 			isKeyguard = false;
 		}else{
 			Bundle myOptions = this.appWidgetManager.getAppWidgetOptions(appWidgetId);
 			int category = myOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);
 			isKeyguard = (category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD);
 		}
 
 		return isKeyguard ? R.layout.lockscreen_layout : R.layout.widget_layout;
 	}
 }
 
