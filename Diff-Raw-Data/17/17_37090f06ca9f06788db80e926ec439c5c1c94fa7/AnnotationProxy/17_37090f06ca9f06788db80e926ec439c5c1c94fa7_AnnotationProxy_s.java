 /**
  * Appcelerator Titanium Mobile
  * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
  * Licensed under the terms of the Apache Public License
  * Please see the LICENSE included with this distribution for details.
  */
 package ti.map;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.appcelerator.kroll.KrollDict;
 import org.appcelerator.kroll.KrollProxy;
 import org.appcelerator.kroll.annotations.Kroll;
 import org.appcelerator.kroll.common.AsyncResult;
 import org.appcelerator.kroll.common.Log;
 import org.appcelerator.kroll.common.TiMessenger;
 import org.appcelerator.titanium.TiC;
 import org.appcelerator.titanium.TiContext;
 import org.appcelerator.titanium.util.TiConvert;
 
 import android.app.Activity;
 import android.os.Message;
 
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 @Kroll.proxy(creatableInModule=MapModule.class, propertyAccessors = {
 	TiC.PROPERTY_SUBTITLE,
 	TiC.PROPERTY_TITLE,
 	TiC.PROPERTY_LATITUDE,
 	TiC.PROPERTY_LONGITUDE,
 	MapModule.PROPERTY_DRAGGABLE,
 	TiC.PROPERTY_IMAGE,
 	TiC.PROPERTY_PINCOLOR
 })
 public class AnnotationProxy extends KrollProxy
 {
 	private static final String TAG = "AnnotationProxy";
 
 	private MarkerOptions markerOptions;
 	private TiMarker marker;
 	
 	private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;
 	
 	private static final int MSG_SET_LONG = MSG_FIRST_ID + 300;
 	private static final int MSG_SET_LAT = MSG_FIRST_ID + 301;
 	private static final int MSG_SET_TITLE = MSG_FIRST_ID + 302;
 	private static final int MSG_SET_SUBTITLE = MSG_FIRST_ID + 303;
 	private static final int MSG_SET_DRAGGABLE= MSG_FIRST_ID + 304;
 
 
 
 	public AnnotationProxy()
 	{
 		super();
 		markerOptions = new MarkerOptions();
 	}
 
 	public AnnotationProxy(TiContext tiContext)
 	{
 		this();
 	}
 
 	@Override
 	protected KrollDict getLangConversionTable() {
 		KrollDict table = new KrollDict();
 		table.put(TiC.PROPERTY_SUBTITLE, TiC.PROPERTY_SUBTITLEID);
 		table.put(TiC.PROPERTY_TITLE, TiC.PROPERTY_TITLEID);
 		return table;
 	}
 	
 	@Override
 	public boolean handleMessage(Message msg) 
 	{
 		AsyncResult result = null;
 		switch (msg.what) {
 
 		case MSG_SET_LONG: {
 			result = (AsyncResult) msg.obj;
 			setPosition(TiConvert.toDouble(getProperty(TiC.PROPERTY_LATITUDE)), (Double)result.getArg());
 			result.setResult(null);
 			return true;
 		}
 		
 		case MSG_SET_LAT: {
 			result = (AsyncResult) msg.obj;
 			setPosition((Double)result.getArg(), TiConvert.toDouble(getProperty(TiC.PROPERTY_LONGITUDE)));
 			result.setResult(null);
 			return true;
 		}
 		
 		case MSG_SET_TITLE: {
 			result = (AsyncResult) msg.obj;
 			marker.getMarker().setTitle((String)result.getArg());
 			result.setResult(null);
 			return true;
 		}
 		
 		case MSG_SET_SUBTITLE: {
 			result = (AsyncResult) msg.obj;
 			marker.getMarker().setSnippet((String)result.getArg());
 			result.setResult(null);
 			return true;
 		}
 		
 		case MSG_SET_DRAGGABLE: {
 			result = (AsyncResult) msg.obj;
 			marker.getMarker().setDraggable((Boolean)result.getArg());
 			result.setResult(null);
 			return true;
 		}
 		
 		default : {
 			return super.handleMessage(msg);
 		}
 		}
 	}
 
 	public void setPosition(double latitude, double longitude) {
 		LatLng position = new LatLng(latitude, longitude);
 		marker.getMarker().setPosition(position);
 	}
 
 	public void processOptions() {
 		double longitude = 0;
 		double latitude = 0;
 		if (hasProperty(TiC.PROPERTY_LONGITUDE)) {
 			longitude = TiConvert.toDouble(getProperty(TiC.PROPERTY_LONGITUDE));
 		}
 		if (hasProperty(TiC.PROPERTY_LATITUDE)) {
 			latitude = TiConvert.toDouble(getProperty(TiC.PROPERTY_LATITUDE));
 		}
 
 		LatLng position = new LatLng(latitude, longitude);
 		markerOptions.position(position);
 		
 		if (hasProperty(TiC.PROPERTY_TITLE)) {
 			markerOptions.title(TiConvert.toString(getProperty(TiC.PROPERTY_TITLE)));
 		}
 		if (hasProperty(TiC.PROPERTY_SUBTITLE)) {
 			markerOptions.snippet(TiConvert.toString(getProperty(TiC.PROPERTY_SUBTITLE)));
 		}
 		if (hasProperty(MapModule.PROPERTY_DRAGGABLE)) {
 			markerOptions.draggable(TiConvert.toBoolean(getProperty(MapModule.PROPERTY_DRAGGABLE)));
 		}
 		//image and pincolor must be defined before adding to mapview. Once added, their values are final.
 		if (hasProperty(TiC.PROPERTY_IMAGE)) {
 			handleImage(getProperty(TiC.PROPERTY_IMAGE));
 		}
 		else if (hasProperty(TiC.PROPERTY_PINCOLOR)) {
 			markerOptions.icon(BitmapDescriptorFactory.defaultMarker(TiConvert.toFloat(getProperty(TiC.PROPERTY_PINCOLOR))));
 		}
 	}
 	
 	private void handleImage(Object image) {
 		//image path 
 		if (image instanceof String) {
 			String path = "Resources/" + image;
 			Activity activity = getActivity();
 			if (activity != null) {
 				try {
 					//check if asset exists
 					InputStream in = activity.getAssets().open(path);
 					in.close();
 					markerOptions.icon(BitmapDescriptorFactory.fromAsset(path));
 				} catch (IOException e) {
 					Log.w(TAG, "Image does not exist");
 				}
 			}
 		}
 	}
 
 	public MarkerOptions getMarkerOptions() {
 		return markerOptions;
 	}
 	
 	public void setTiMarker(TiMarker m) {
 		marker = m;
 	}
 	
 	public TiMarker getTiMarker() {
 		return marker;
 	}
 	
 	public void showInfo() {
 		Marker m = marker.getMarker();
 		if (m != null) {
 			m.showInfoWindow();
 		}
 	}
 	
 	public void hideInfo() {
 		Marker m = marker.getMarker();
 		if (m != null) {
 			m.hideInfoWindow();
 		}
 	}
 	
 	@Override
 	public void onPropertyChanged(String name, Object value) {
 		super.onPropertyChanged(name, value);
 		
 		if (marker == null) {
 			return;
 		}
 		
 		if (name.equals(TiC.PROPERTY_LONGITUDE)) {
 			TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_SET_LONG), TiConvert.toDouble(value));
 		}
 		if (name.equals(TiC.PROPERTY_LATITUDE)) {
 			TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_SET_LAT), TiConvert.toDouble(value));
 		}
 		if (name.equals(TiC.PROPERTY_TITLE)) {
 			TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_SET_TITLE), TiConvert.toString(value));
 		}
 		if (name.equals(TiC.PROPERTY_SUBTITLE)) {
 			TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_SET_SUBTITLE), TiConvert.toString(value));
 		}
 		if (name.equals(MapModule.PROPERTY_DRAGGABLE)) {
 			TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_SET_DRAGGABLE), TiConvert.toBoolean(value));
 		}
 		
 	}
 	
 }
