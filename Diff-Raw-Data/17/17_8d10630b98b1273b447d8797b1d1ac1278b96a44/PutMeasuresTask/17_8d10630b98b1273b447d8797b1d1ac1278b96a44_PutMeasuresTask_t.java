 package org.sensapp.android.sensappdroid.restrequests;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.sensapp.android.sensappdroid.R;
 import org.sensapp.android.sensappdroid.activities.SensorsActivity;
 import org.sensapp.android.sensappdroid.contentprovider.SensAppCPContract;
 import org.sensapp.android.sensappdroid.datarequests.DatabaseRequest;
 import org.sensapp.android.sensappdroid.json.JsonPrinter;
 import org.sensapp.android.sensappdroid.json.MeasureJsonModel;
 import org.sensapp.android.sensappdroid.json.NumericalMeasureJsonModel;
 import org.sensapp.android.sensappdroid.json.StringMeasureJsonModel;
 import org.sensapp.android.sensappdroid.models.Sensor;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 public class PutMeasuresTask extends AsyncTask<Integer, Integer, Integer> {
 	
 	private static final String TAG = PutMeasuresTask.class.getSimpleName();
 	private static final int INTEGER_SIZE = 4;
 	private static final int LONG_SIZE = 12;
 	private static final int DEFAULT_SIZE_LIMIT = 200000;
 	private static final int NOTIFICATION_ID = 10;
 	private static final int NOTIFICATION_FINAL_ID = 20;
 	
 	private Context context;
 	private Uri uri;
 	private NotificationManager notificationManager;
 	private Notification notification;
 	
 	public PutMeasuresTask(Context context, Uri uri) {
 		super();
 		this.context = context;
 		this.uri = uri;
 		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 	}
 	
 	private boolean sensorExists(String sensorName) {
 		String[] projection = {SensAppCPContract.Sensor.NAME};
 		Cursor cursor = context.getContentResolver().query(Uri.parse(SensAppCPContract.Sensor.CONTENT_URI + "/" + sensorName), projection, null, null, null); 
 		if (cursor != null) {
 			boolean exists =  cursor.getCount() > 0;
 			cursor.close();
 			return exists;
 		}
 		return false;
 	}
 	
 	private List<Long> getBasetimes(String sensorName) {
 		List<Long> basetimes = new ArrayList<Long>();
 		String[] projection = {"DISTINCT " + SensAppCPContract.Measure.BASETIME};
 		Cursor cursor = context.getContentResolver().query(Uri.parse(SensAppCPContract.Measure.CONTENT_URI + "/" + sensorName), projection, null, null, null);
 		if (cursor != null) {
 			while (cursor.moveToNext()) {
 				basetimes.add(cursor.getLong(cursor.getColumnIndexOrThrow(SensAppCPContract.Measure.BASETIME)));
 			}
 			cursor.close();
 		}
 		return basetimes;
 	}
 	
 	@Override
 	protected void onPreExecute() {
 	        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, SensorsActivity.class), 0);
 	        notification = new Notification(R.drawable.ic_launcher, "Starting upload", System.currentTimeMillis());
 	        notification.contentIntent = pendingIntent;
 	        notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
 	        notification.contentView = new RemoteViews(context.getPackageName(), R.layout.upload_notification_layout);
 	        notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_launcher);
 	        notification.contentView.setTextViewText(R.id.status_text, "Uploading measures...");
 	        notification.contentView.setProgressBar(R.id.status_progress, 100, 0, false);
 	        notificationManager.notify(NOTIFICATION_ID, notification);
 	}
 	
 	@Override
 	protected Integer doInBackground(Integer... params) {
 		
 		int rowTotal = 0;
 		
 		Cursor cursor = context.getContentResolver().query(uri, new String[]{SensAppCPContract.Measure.ID}, SensAppCPContract.Measure.UPLOADED + " = 0", null, null);
 		if (cursor != null) {
 			rowTotal = cursor.getCount();
 			cursor.close();
 		}
 		
 		notification.contentView.setTextViewText(R.id.status_text, "Uploading " + rowTotal + " measures...");
 		notificationManager.notify(NOTIFICATION_ID, notification);
 		
 		int rowsUploaded = 0;
		int progress = 0;
 		int sizeLimit = DEFAULT_SIZE_LIMIT;
 		if (params.length > 0) {
 			sizeLimit = params[0];
 		}
 		
 		ArrayList<String> sensorNames = new ArrayList<String>();
 		cursor = context.getContentResolver().query(uri, new String[]{"DISTINCT " + SensAppCPContract.Measure.SENSOR}, SensAppCPContract.Measure.UPLOADED + " = 0", null, null);
 		if (cursor != null) {
 			while (cursor.moveToNext()) {
 				sensorNames.add(cursor.getString(cursor.getColumnIndexOrThrow(SensAppCPContract.Measure.SENSOR)));
 			}
 			cursor.close();
 		}
 		
 		Sensor sensor;
 		for (String sensorName : sensorNames) {
 			
 			if (!sensorExists(sensorName)) {
 				Log.e(TAG, "Incorrect database: sensor " + sensorName + " does not exit");
 				return null;
 			}
 		
 			sensor = DatabaseRequest.SensorRQ.getSensor(context, sensorName);
 			
 			if (!sensor.isUploaded()) {
 				Uri postSensorResult = null;
 				try {
 					postSensorResult = new PostSensorRestTask(context, sensorName).executeOnExecutor(THREAD_POOL_EXECUTOR).get();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} catch (ExecutionException e) {
 					e.printStackTrace();
 				}
 				if (postSensorResult == null) {
 					Log.e(TAG, "Post sensor failed");
 					return null;
 				}
 			}
 			
 			MeasureJsonModel model = null;
 			if (sensor.getTemplate().equals("Numerical")) {
 				model = new NumericalMeasureJsonModel(sensorName, sensor.getUnit());
 			} else if (sensor.getTemplate().equals("String")) {
 				model = new StringMeasureJsonModel(sensorName, sensor.getUnit());
 			} else {
 				Log.e(TAG, "Incorrect sensor template");
 				return null;
 			}
 				
 			List<Integer> ids = new ArrayList<Integer>();
 			for (Long basetime : getBasetimes(sensorName)) {
 				model.setBt(basetime);	
 				String[] projection = {SensAppCPContract.Measure.ID, SensAppCPContract.Measure.VALUE, SensAppCPContract.Measure.TIME};
 				String selection = SensAppCPContract.Measure.SENSOR + " = \"" + model.getBn() + "\" AND " + SensAppCPContract.Measure.BASETIME + " = " + model.getBt() + " AND " + SensAppCPContract.Measure.UPLOADED + " = 0";
 				cursor = context.getContentResolver().query(uri, projection, selection, null, null);
 				if (cursor != null) {
 					if (cursor.getCount() > 0) {
 						int size = 0;
 						while (size == 0) {
 							while (cursor.moveToNext()) {
 								ids.add(cursor.getInt(cursor.getColumnIndexOrThrow(SensAppCPContract.Measure.ID)));
 								long time = cursor.getLong(cursor.getColumnIndexOrThrow(SensAppCPContract.Measure.TIME));
 								if (model instanceof NumericalMeasureJsonModel) {
 									int value = cursor.getInt(cursor.getColumnIndexOrThrow(SensAppCPContract.Measure.VALUE));
 									((NumericalMeasureJsonModel) model).appendMeasure(value, time);
 									size += INTEGER_SIZE;
 								} else if (model instanceof StringMeasureJsonModel) {
 									String value = cursor.getString(cursor.getColumnIndexOrThrow(SensAppCPContract.Measure.VALUE));
 									((StringMeasureJsonModel) model).appendMeasure(value, time);
 									size += value.length();
 								}
 								size += LONG_SIZE;
 								if (size > sizeLimit && !cursor.isLast()) {
 									size = 0;
 									break;
 								}
 							}
 
 							try {
 								RestRequest.putData(sensor.getUri(), JsonPrinter.measuresToJson(model));
 							} catch (RequestErrorException e) {
 								Log.e(TAG, e.getMessage());
 								if (e.getCause() != null) {
 									Log.e(TAG, e.getCause().getMessage());
 								}
 								return null;
 							}
 							model.clearValues();
							publishProgress((int) ((float) (progress + ids.size()) / rowTotal * 100));
 						}
						ContentValues values = new ContentValues();
						values.put(SensAppCPContract.Measure.UPLOADED, 1);
						selection = SensAppCPContract.Measure.ID + " IN " + ids.toString().replace('[', '(').replace(']', ')');
						rowsUploaded += context.getContentResolver().update(uri, values, selection, null);
						progress += ids.size();
						ids.clear();
 					}
 					cursor.close();
 				}
 			}
 		}
 		return rowsUploaded;
 	}
 
 	@Override
 	protected void onProgressUpdate(Integer... values) {
 		notification.contentView.setProgressBar(R.id.status_progress, 100, values[0], false);
 		notificationManager.notify(NOTIFICATION_ID, notification);
 	}
 
 	@Override
 	protected void onPostExecute(Integer result) {
 		notificationManager.cancel(NOTIFICATION_ID);
 		if (result == null) {
 			Log.e(TAG, "Put data error");
 			Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show();
 		} else {
 			Log.i(TAG, "Put data succed: " + result + " measures uploaded");
 			final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, SensorsActivity.class), 0);
 			Notification notificationFinal = new Notification(R.drawable.ic_launcher, "Upload finished", System.currentTimeMillis());
 			notificationFinal.setLatestEventInfo(context, "Upload succeed", result + " measures uploaded", pendingIntent);
 			notificationManager.notify(NOTIFICATION_FINAL_ID, notificationFinal);
 			Toast.makeText(context, "Upload succeed: " + result + " measures uploaded", Toast.LENGTH_LONG).show();
 		}
 	}
 }
