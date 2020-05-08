 package org.sensapp.android.sensappdroid.api;
 
 import org.sensapp.android.sensappdroid.contract.SensAppContract;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ActivityNotFoundException;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 import android.net.Uri;
 
 public class SensAppHelper {
 	
 	public static Uri insertMeasure(Context context, String sensor, int value) throws IllegalArgumentException {
 		return insertMeasure(context, sensor, String.valueOf(value));
 	}
 	 
 	public static Uri insertMeasure(Context context, String sensor, float value) throws IllegalArgumentException {
 		return insertMeasure(context, sensor, String.valueOf(value));
 	}
 	
 	public static Uri insertMeasure(Context context, String sensor, String value) throws IllegalArgumentException {
 		if (context == null) {
 			throw new IllegalArgumentException("The context is null");
 		} 
 		if (sensor == null) {
 			throw new IllegalArgumentException("The sensor is null");
 		} else if (!isSensorRegistered(context, sensor)) {
 			throw new IllegalArgumentException(sensor + " is not maintained");
 		}
 		if (value == null) {
 			throw new IllegalArgumentException("The value is null");
 		}
 		ContentValues values = new ContentValues();
 		values.put(SensAppContract.Measure.SENSOR, sensor);
 		values.put(SensAppContract.Measure.VALUE, value);
 		values.put(SensAppContract.Measure.BASETIME, 0);
 		values.put(SensAppContract.Measure.TIME, System.currentTimeMillis() / 1000);
 		return context.getContentResolver().insert(SensAppContract.Measure.CONTENT_URI, values);
 	}
 	
 	public static Uri insertMeasure(Context context, String sensor, int value, long basetime, long time) throws IllegalArgumentException {
 		return insertMeasure(context, sensor, String.valueOf(value), basetime, time);
 	}
 	 
 	public static Uri insertMeasure(Context context, String sensor, float value, long basetime, long time) throws IllegalArgumentException {
 		return insertMeasure(context, sensor, String.valueOf(value), basetime, time);
 	}
 	
 	public static Uri insertMeasure(Context context, String sensor, String value, long basetime, long time) throws IllegalArgumentException {
 		if (context == null) {
 			throw new IllegalArgumentException("The context is null");
 		} 
 		if (sensor == null) {
 			throw new IllegalArgumentException("The sensor is null");
 		} else if (!isSensorRegistered(context, sensor)) {
 			throw new IllegalArgumentException(sensor + " is not maintained");
 		}
 		if (value == null) {
 			throw new IllegalArgumentException("The value is null");
 		}
 		ContentValues values = new ContentValues();
 		values.put(SensAppContract.Measure.SENSOR, sensor);
 		values.put(SensAppContract.Measure.VALUE, value);
 		values.put(SensAppContract.Measure.BASETIME, basetime);
 		values.put(SensAppContract.Measure.TIME, time);
 		return context.getContentResolver().insert(SensAppContract.Measure.CONTENT_URI, values);
 	}
 	
 	public static Uri registerNumericalSensor(Context context, String name, String description, SensAppUnit unit) throws IllegalArgumentException {
 		return registerSensor(context, name, description, unit, SensAppTemplate.numerical);
 	}
 	
 	public static Uri registerStringSensor(Context context, String name, String description, SensAppUnit unit) throws IllegalArgumentException {
 		return registerSensor(context, name, description, unit, SensAppTemplate.string);
 	}
 	
 	public static boolean isSensAppInstalled(Context context) {
 		try{
 			context.getPackageManager().getApplicationInfo("org.sensapp.android.sensappdroid", 0);
 		} catch (PackageManager.NameNotFoundException e ) {
 			return false;
 		}
 		return true;
 	}
 	
 	public static Dialog getInstallationDialog(final Context context) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setMessage("SensApp application is needed. Would you like install it now?");
 		builder.setCancelable(false);
 		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				Uri uri = Uri.parse("market://details?id=" + "org.sensapp.android.sensappdroid");
 				try {
 					context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
 				} catch (ActivityNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			}
 		});
 		return builder.create();
 	}
 	
 	private static Uri registerSensor(Context context, String name, String description, SensAppUnit unit, SensAppTemplate template) throws IllegalArgumentException {
 		if (context == null) {
 			throw new IllegalArgumentException("The context is null");
 		} 
 		if (name == null) {
 			throw new IllegalArgumentException("The sensor name is null");
 		} else if (isSensorRegistered(context, name)) {
 			return null;
 		}
 		if (unit == null) {
 			throw new IllegalArgumentException("The sensor unit is null");
 		}
 		ContentValues values = new ContentValues();
 		values.put(SensAppContract.Sensor.NAME, name);
 		if (description == null) {
 			values.put(SensAppContract.Sensor.DESCRIPTION, "");
		} else {
			values.put(SensAppContract.Sensor.DESCRIPTION, description);
 		}
 		values.put(SensAppContract.Sensor.UNIT, unit.getIANAUnit());
 		values.put(SensAppContract.Sensor.BACKEND, SensAppBackend.raw.getBackend());
 		values.put(SensAppContract.Sensor.TEMPLATE, template.getTemplate());
 		return context.getContentResolver().insert(SensAppContract.Sensor.CONTENT_URI, values);
 	}
 	
 	private static boolean isSensorRegistered(Context context, String name) {
 		Cursor c = context.getContentResolver().query(Uri.parse(SensAppContract.Sensor.CONTENT_URI + "/" + name), null, null, null, null);
 		boolean isRegistered = c.getCount() > 0;
 		c.close();
 		return isRegistered;
 	}
 	
 	private static enum SensAppBackend {
 		raw("raw");
 		private String backend;
 		private SensAppBackend(String backend) {
 			this.backend = backend;
 		}
 		public String getBackend() {
 			return backend;
 		}
 	}
 
 	private static enum SensAppTemplate {
 		string("String"), 
 		numerical("Numerical");
 		private String template;
 		private SensAppTemplate(String template) {
 			this.template = template;
 		}
 		public String getTemplate() {
 			return template;
 		}
 	}
 }
