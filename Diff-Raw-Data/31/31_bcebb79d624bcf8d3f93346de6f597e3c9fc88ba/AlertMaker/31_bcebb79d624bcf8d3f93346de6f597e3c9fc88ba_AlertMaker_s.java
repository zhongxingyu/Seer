 package com.macaroon.piztor;
 
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.baidu.location.BDLocation;
 import com.baidu.location.BDLocationListener;
 import com.baidu.location.LocationClient;
 import com.baidu.location.LocationClientOption;
 import com.baidu.mapapi.map.LocationData;
 import com.baidu.mapapi.map.MKMapTouchListener;
 import com.baidu.mapapi.map.MapView;
 import com.baidu.mapapi.map.OverlayItem;
 import com.baidu.mapapi.map.PopupOverlay;
 import com.baidu.platform.comapi.basestruct.GeoPoint;
 
 public class AlertMaker {
 
 	private Calendar calendar;
 	private Context context;
 	private GeoPoint markerPoint;
 	private MapMaker mapMaker;
 
 	public AlertMaker(Context cc, MapMaker mm) {
 		context =cc;
 		mapMaker = mm;
 	}
 		
 	public static void closeBoard(Context cc) {
 		InputMethodManager imm = (InputMethodManager) cc
 				.getSystemService(Context.INPUT_METHOD_SERVICE);
 		  if (imm.isActive())
 			  imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
 		 InputMethodManager.HIDE_NOT_ALWAYS);
 		 }
 
 	public void showSettingsAlert() {
 		
 		closeBoard(context);
 		AlertDialog.Builder gpsDialog = new AlertDialog.Builder(context);
 		gpsDialog.setTitle("GPS settings");
 		gpsDialog.setMessage("GPS is not enabled. Please turn it on.");
 		gpsDialog.setPositiveButton("Settings",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 						context.startActivity(intent);
 					}
 				});
 		gpsDialog.setNegativeButton("Go without GPS",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 					}
 				});
 		gpsDialog.show();
 	}
 
 	public void showMarkerAlert(GeoPoint point) {
 		
 		closeBoard(context);
 		if (point == null) return;
 		markerPoint = point;
 		calendar = Calendar.getInstance();
 		TimePickerDialog markerDialog = new TimePickerDialog(context
 				, new TimePickerDialog.OnTimeSetListener() {
 					
 					@Override
 					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 						////// at least 2 minutes
 						if ((hourOfDay >= calendar.get(Calendar.HOUR_OF_DAY) && minute >= calendar.get(Calendar.MINUTE))
 								|| hourOfDay > calendar.get(Calendar.HOUR_OF_DAY))
 							mapMaker.DrawMarker(markerPoint);
 						else {
 							Toast toast = Toast.makeText(context,
									"Too early！ Give me at least 2 minutes!", Toast.LENGTH_LONG);
 							toast.show();
 							closeBoard(context);
 							showMarkerAlert(markerPoint);
 						}
 					}
 				}
 				, calendar.get(Calendar.HOUR_OF_DAY)
 				, calendar.get(Calendar.MINUTE), false);
 		markerDialog.show();
 	}
 	
 }
