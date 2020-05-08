 package org.sensapp.android.sensappdroid.api;
 
 import java.io.ByteArrayOutputStream;
 
 import org.sensapp.android.sensappdroid.contract.SensAppContract;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ActivityNotFoundException;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.res.Resources.NotFoundException;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 
 public class SensAppHelper {
 	
 	private static ContentValues buildMeasure(Context context, String sensor, String value, long basetime, long time) throws IllegalArgumentException {
 		if (context == null) {
 			throw new IllegalArgumentException("The context is null");
 		} else if (sensor == null) {
 			throw new IllegalArgumentException("The sensor is null");
 		} else if (!isSensorRegistered(context, sensor)) {
 			throw new IllegalArgumentException(sensor + " is not maintained");
 		} else if (value == null) {
 			throw new IllegalArgumentException("The value is null");
 		}
 		ContentValues values = new ContentValues();
 		values.put(SensAppContract.Measure.SENSOR, sensor);
 		values.put(SensAppContract.Measure.VALUE, value);
 		values.put(SensAppContract.Measure.BASETIME, basetime);
 		values.put(SensAppContract.Measure.TIME, time);
 		return values;
 	}
 
 	/**
 	 * Insert measure with default basetime and time.
 	 * Same as 
 	 * @link insertMeasure(Context context, String sensor, int value, long basetime, long time) 
 	 * with basetime = 0 and current time.
 	 * @param context the context used for the content resolver
 	 * @param sensor the name of the associated sensor
 	 * @param value the value of the measure
 	 * @return the uri of the newly inserted measure, the measure id is -1 in case of failure
 	 * @throws IllegalArgumentException
 	 * @see insertMeasure(Context context, String sensor, int value)
 	 * @see insertMeasure(Context context, String sensor, float value) 
 	 */
 	public static Uri insertMeasure(Context context, String sensor, String value) throws IllegalArgumentException {
 		ContentValues values = buildMeasure(context, sensor, value, 0, System.currentTimeMillis() / 1000);
 		return context.getContentResolver().insert(SensAppContract.Measure.CONTENT_URI, values);
 	}
 	
 	public static Uri insertMeasure(Context context, String sensor, int value) throws IllegalArgumentException {
 		return insertMeasure(context, sensor, String.valueOf(value));
 	}
 	 
 	public static Uri insertMeasure(Context context, String sensor, float value) throws IllegalArgumentException {
 		return insertMeasure(context, sensor, String.valueOf(value));
 	}
 	
 	public static Uri insertMeasure(Context context, String sensor, String value, long basetime, long time) throws IllegalArgumentException {
 		ContentValues values = buildMeasure(context, sensor, value, basetime, time);
 		return context.getContentResolver().insert(SensAppContract.Measure.CONTENT_URI, values);
 	}
 	
 	public static Uri insertMeasure(Context context, String sensor, int value, long basetime, long time) throws IllegalArgumentException {
 		return insertMeasure(context, sensor, String.valueOf(value), basetime, time);
 	}
 	 
 	public static Uri insertMeasure(Context context, String sensor, float value, long basetime, long time) throws IllegalArgumentException {
 		return insertMeasure(context, sensor, String.valueOf(value), basetime, time);
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
 	
 	private static ContentValues buildSensor(Context context, String name, String description, SensAppUnit unit, SensAppBackend backend, SensAppTemplate template, Drawable icon) throws IllegalArgumentException {
 		if (context == null) {
 			throw new IllegalArgumentException("The context is null");
 		} else if (name == null) {
 			throw new IllegalArgumentException("The sensor name is null");
 		} else if (isSensorRegistered(context, name)) {
 			return null;
 		} else if (unit == null) {
 			throw new IllegalArgumentException("The sensor unit is null");
 		} else if (backend == null) {
 			throw new IllegalArgumentException("The sensor backend is null");
 		} else if (template == null) {
 			throw new IllegalArgumentException("The sensor template is null");
 		}
 		ContentValues values = new ContentValues();
 		values.put(SensAppContract.Sensor.NAME, name);
 		if (description == null) {
 			values.put(SensAppContract.Sensor.DESCRIPTION, "");
 		} else {
 			values.put(SensAppContract.Sensor.DESCRIPTION, description);
 		}
 		values.put(SensAppContract.Sensor.UNIT, unit.getIANAUnit());
 		values.put(SensAppContract.Sensor.BACKEND, backend.getBackend());
 		values.put(SensAppContract.Sensor.TEMPLATE, template.getTemplate());
 		if (icon != null) {
 			Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
 			ByteArrayOutputStream stream = new ByteArrayOutputStream();
 			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
 			values.put(SensAppContract.Sensor.ICON, stream.toByteArray());
 		}
 		return values;
 	}
 	
 	private static Uri registerSensor(Context context, String name, String description, SensAppUnit unit, SensAppBackend backend, SensAppTemplate template, Drawable icon) throws IllegalArgumentException {
 		ContentValues values = buildSensor(context, name, description, unit, backend, template, icon);
 		if (values == null) {
 			return null;
 		} 
 		return context.getContentResolver().insert(SensAppContract.Sensor.CONTENT_URI, values);
 	}
 	
 	public static Uri registerNumericalSensor(Context context, String name, String description, SensAppUnit unit, Drawable icon) throws IllegalArgumentException {
 		return registerSensor(context, name, description, unit, SensAppBackend.raw, SensAppTemplate.numerical, icon);
 	}
 	
 	public static Uri registerStringSensor(Context context, String name, String description, SensAppUnit unit, Drawable icon) throws IllegalArgumentException {
 		return registerSensor(context, name, description, unit, SensAppBackend.raw, SensAppTemplate.string, icon);
 	}
 	
 	public static Uri registerNumericalSensor(Context context, String name, String description, SensAppUnit unit, int iconResourceId) throws IllegalArgumentException, NotFoundException {
 		Drawable icon = context.getResources().getDrawable(iconResourceId);
 		return registerSensor(context, name, description, unit, SensAppBackend.raw, SensAppTemplate.numerical, icon);
 	}
 	
 	public static Uri registerStringSensor(Context context, String name, String description, SensAppUnit unit, int iconResourceId) throws IllegalArgumentException, NotFoundException {
 		Drawable icon = context.getResources().getDrawable(iconResourceId);
 		return registerSensor(context, name, description, unit, SensAppBackend.raw, SensAppTemplate.string, icon);
 	}
 	
 	private static boolean isSensorRegistered(Context context, String name) {
		Cursor c = context.getContentResolver().query(Uri.parse(SensAppContract.Sensor.CONTENT_URI + "/" + name), new String[]{SensAppContract.Sensor.NAME}, null, null, null);
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
