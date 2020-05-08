 package org.d1sturbed.ww;
 
 import java.util.ArrayList;
 import java.util.Random;
 import android.graphics.PorterDuff.Mode;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 public abstract class WWBaseWidget extends AppWidgetProvider implements LocationListener,Runnable {
 	
 	//debugging
 	public static final boolean DEBUG = true;
 	public static final String TAG = "WWBaseWidget";
 	//intent actions
 	public static final String ACTION_WIDGET_SWITCH = "WW.ACTION_WIDGET_SWITCH";
 	public static String ACTION_START_ACTIVITY = "WW.ACTION_START_ACTIVITY";
 	
 	//application context
 	protected Context context;
 	//weather handler data
 	protected WWBaseHandler h;
 	//layout of the widget
 	protected int l;
 	//location
 	ArrayList<Bitmap> ba;
 	protected Location lo;
 
 
 	
 	@Override
 	public void onEnabled(Context context) {
 		debug("onEnabled-: " + context.getPackageName());
 		super.onEnabled(context);
 	}
 
 	protected void debug(String msg) {
 		if (DEBUG) {
 			Log.d(TAG, msg);
 		}
 	}
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
         Canvas canvas = new Canvas(output);
 
        final int color = 0xff424242;
         final Paint paint = new Paint();
         final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
         final RectF rectF = new RectF(rect);
         final float roundPx = pixels;
 
         paint.setAntiAlias(true);
         canvas.drawARGB(0, 0, 0, 0);
         paint.setColor(color);
         canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
 
         paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
         canvas.drawBitmap(bitmap, rect, rect, paint);
 
        return Bitmap.createScaledBitmap(output, 70, 70, false);
     }
    
     
 	//update the widget data
 	protected void setWidget(Bitmap b) {
 		RemoteViews updateViews;
 		AppWidgetManager manager;
 		ComponentName widget;
 		
 		updateViews = new RemoteViews(context.getPackageName(), l);
 		debug("Base" + getClass().getName());
 		manager = AppWidgetManager.getInstance(context);
 		widget = new ComponentName(context, getClass());
 		
 		//start WWActivity onclick
 		PendingIntent pendingShowActivityIntent;
 		Intent intent = new Intent(context, getClass());
 		intent.setAction(WWBaseWidget.ACTION_START_ACTIVITY);
 		if(h!=null) {
 			intent.putExtra("h", h);
 		}
 		if(b!=null) {
 			intent.putExtra("b", b);	
 		}
 		
 		pendingShowActivityIntent = PendingIntent.getBroadcast(context, new Random().nextInt(),	intent, 0);
 		updateViews.setOnClickPendingIntent(R.id.widget_textview, pendingShowActivityIntent);
 		
 		//set Widget
 		try {
 			if(b!=null) {
 				updateViews.setImageViewBitmap(R.id.widget_imageview, getRoundedCornerBitmap(b, 15));
 				updateViews.setInt(R.id.widget_imageview, "setAlpha", 50);
 			}
 			updateViews.setTextViewText(R.id.widget_textview, h.getShortString());
 		} catch (Exception e) {
 			debug("Fehler: " + e.toString());
 			updateViews.setTextViewText(R.id.widget_textview, "Fehler:" + e.toString());
 		}
 		
 		//update View
 		manager.updateAppWidget(widget, updateViews);
 	}
 
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds) {
 		super.onUpdate(context, appWidgetManager, appWidgetIds);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		updateWeather(location);
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		debug(provider);
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	//get Weather icon and update Widget
 	public void run() {
 		try {
 			if(h!=null) {
 				h.getImages(context);
 				Bitmap b=h.fromCache(context, h.getPic());
 				if(b!=null) {
 					setWidget(b);
 				} else {
 					setWidget(null);					
 				}
 			}
 		} catch(Exception e) {
 			setWidget(null);
 			debug("Could not get:" + e.toString());
 		}
 	}
 	
 	
 
 	//send update intent to update service
 	private void updateWeather(Location myl) {
 		Intent mintent = new Intent(this.context, WWUpdate.class);
 		mintent.setAction(WWUpdate.UPDATE);
 		mintent.putExtra("location", myl);
 		mintent.putExtra("className", this.getClass().getName());
 		context.startService(mintent);		
 	}
 	
 
 	//receive incoming intents
 	public void onReceive(Context context, Intent intent) {
 		debug(getClass().getName()+":"+intent.getAction());
 		WWBaseWidget w=null;
 		WWBaseHandler h=null;
 		//get Handler Extra, contains weather data
 		if(intent.hasExtra("h")) {
 			h=(WWBaseHandler)intent.getExtras().getSerializable("h");
 		}
 		
 		//which widget are we? (ugly)
 		switch(l) {
 			case R.layout.widget:
 				w=new WW(context,h);
 				break;
 			case R.layout.widget2x1:
 				w=new WW2x1(context,h);
 				break;
 			default:
 				w=null;
 				break;						
 		}
 		
 		//weather data update 
 		if(intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE")) {
 			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 			Criteria c=new Criteria();
 			c.setAccuracy(Criteria.POWER_LOW);
 			Location myl=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 			
 				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000000, 1000, w);
 			if(myl!=null) {
 				debug("updating widget");
 				w.updateWeather(myl);
 			}
 		//widget view update
 		} else if (intent.getAction().equals(WW.ACTION_WIDGET_SWITCH)) {
 			if(w!=null) {
 				new Thread(w).start();
 			}
 		//start application with containing contents
 		} else if (intent.getAction().equals(WW.ACTION_START_ACTIVITY)) {
 			Intent mintent = new Intent(context, WWActivity.class);
 			mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			if(h!=null) {
 				mintent.putExtra("h", h);
 			}
 			if(intent.hasExtra("b")) {
 				mintent.putExtra("b", (Bitmap) intent.getParcelableExtra("b"));
 			}
 			context.startActivity(mintent);
 		}
 	}
 
 }
