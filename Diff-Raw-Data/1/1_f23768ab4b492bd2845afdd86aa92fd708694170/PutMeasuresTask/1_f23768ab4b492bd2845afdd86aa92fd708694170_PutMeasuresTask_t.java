 package org.sensapp.android.sensappdroid.restrequests;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.sensapp.android.sensappdroid.R;
 import org.sensapp.android.sensappdroid.activities.TabsActivity;
 import org.sensapp.android.sensappdroid.contract.SensAppContract;
 import org.sensapp.android.sensappdroid.datarequests.DatabaseRequest;
 import org.sensapp.android.sensappdroid.json.JsonPrinter;
 import org.sensapp.android.sensappdroid.json.MeasureJsonModel;
 import org.sensapp.android.sensappdroid.json.NumericalMeasureJsonModel;
 import org.sensapp.android.sensappdroid.json.StringMeasureJsonModel;
 import org.sensapp.android.sensappdroid.models.Sensor;
 import org.sensapp.android.sensappdroid.preferences.GeneralPrefFragment;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 public class PutMeasuresTask extends AsyncTask<Integer, Integer, Integer> {
 	
 	public static final int FLAG_DEFAULT = 0x79;
 	public static final int FLAG_SILENT = 0x07;
 	
 	private static final String TAG = PutMeasuresTask.class.getSimpleName();
 	private static final int INTEGER_SIZE = 4;
 	private static final int LONG_SIZE = 12;
 	private static final int DEFAULT_SIZE_LIMIT = 200000;
 	private static final int NOTIFICATION_ID = 10;
 	private static final int NOTIFICATION_FINAL_ID = 20;
 	
 	private PutMeasureCallback listenner;
 	private Context context;
 	private int flag;
 	private int id;
 	private Uri uri;
 	private NotificationManager notificationManager;
 	private Notification notification;
 	private String errorMessage;
 	
 	public interface PutMeasureCallback {
 		public void onTaskFinished(int id);
 	}
 	
 	public PutMeasuresTask(Context context, Uri uri) {
 		super();
 		this.flag = FLAG_DEFAULT;
 		this.context = context;
 		this.uri = uri;
 		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 	}
 	
 	public PutMeasuresTask(PutMeasureCallback listenner, int id, Context context, Uri uri) {
 		super();
 		this.listenner = listenner;
 		this.id = id;
 		this.flag = FLAG_DEFAULT;
 		this.context = context;
 		this.uri = uri;
 		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 	}
 	
 	public PutMeasuresTask(PutMeasureCallback listenner, int id, Context context, Uri uri, int flag) {
 		super();
 		this.listenner = listenner;
 		this.id = id;
 		this.flag = flag;
 		this.context = context;
 		this.uri = uri;
 		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 	}
 	
 	private boolean sensorExists(String sensorName) {
 		String[] projection = {SensAppContract.Sensor.NAME};
 		Cursor cursor = context.getContentResolver().query(Uri.parse(SensAppContract.Sensor.CONTENT_URI + "/" + sensorName), projection, null, null, null); 
 		if (cursor != null) {
 			boolean exists =  cursor.getCount() > 0;
 			cursor.close();
 			return exists;
 		}
 		return false;
 	}
 	
 	private List<Long> getBasetimes(String sensorName) {
 		List<Long> basetimes = new ArrayList<Long>();
 		String[] projection = {"DISTINCT " + SensAppContract.Measure.BASETIME};
 		Cursor cursor = context.getContentResolver().query(Uri.parse(SensAppContract.Measure.CONTENT_URI + "/" + sensorName), projection, null, null, null);
 		if (cursor != null) {
 			while (cursor.moveToNext()) {
 				basetimes.add(cursor.getLong(cursor.getColumnIndexOrThrow(SensAppContract.Measure.BASETIME)));
 			}
 			cursor.close();
 		}
 		return basetimes;
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		if (flag != FLAG_SILENT) {
 			final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, TabsActivity.class), 0);
 			notification = new Notification(R.drawable.ic_launcher, "Starting upload", System.currentTimeMillis());
 			notification.contentIntent = pendingIntent;
 			notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
 			notification.contentView = new RemoteViews(context.getPackageName(), R.layout.upload_notification_layout);
 			notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_launcher);
 			notification.contentView.setTextViewText(R.id.status_text, "Uploading measures...");
 			notification.contentView.setProgressBar(R.id.status_progress, 100, 0, false);
 			notificationManager.notify(NOTIFICATION_ID, notification);
 		}
 	}
 
 	@Override
 	protected Integer doInBackground(Integer... params) {
 
 		int rowTotal = 0;
 
 		Cursor cursor = context.getContentResolver().query(uri, new String[]{SensAppContract.Measure.ID}, SensAppContract.Measure.UPLOADED + " = 0", null, null);
 		if (cursor != null) {
 			rowTotal = cursor.getCount();
 			cursor.close();
 		}
 
 		if (flag != FLAG_SILENT) {
 			notification.contentView.setTextViewText(R.id.status_text, "Uploading " + rowTotal + " measures...");
 			notificationManager.notify(NOTIFICATION_ID, notification);
 		}
 		
 		int rowsUploaded = 0;
 		int progress = 0;
 		int sizeLimit = DEFAULT_SIZE_LIMIT;
 		if (params.length > 0) {
 			sizeLimit = params[0];
 		}
 
 		ArrayList<String> sensorNames = new ArrayList<String>();
 		cursor = context.getContentResolver().query(uri, new String[]{"DISTINCT " + SensAppContract.Measure.SENSOR}, SensAppContract.Measure.UPLOADED + " = 0", null, null);
 		if (cursor != null) {
 			while (cursor.moveToNext()) {
 				sensorNames.add(cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Measure.SENSOR)));
 			}
 			cursor.close();
 		}
 
 		Sensor sensor;
 		for (String sensorName : sensorNames) {
 
 			if (!sensorExists(sensorName)) {
 				Log.e(TAG, "Incorrect database: sensor " + sensorName + " does not exit");
 				return null;
 			}
 
 			// Update uri with current preference
 			try {
 				ContentValues values = new ContentValues();
 				values.put(SensAppContract.Sensor.URI, GeneralPrefFragment.buildUri(PreferenceManager.getDefaultSharedPreferences(context), context.getResources()));
 				context.getContentResolver().update(Uri.parse(SensAppContract.Sensor.CONTENT_URI + "/" + sensorName), values, null, null);
 			} catch (IllegalStateException e) {
 				errorMessage = e.getMessage();
 				Log.e(TAG, errorMessage);
 				return null;
 			}
 
 			sensor = DatabaseRequest.SensorRQ.getSensor(context, sensorName);
 
 			try {
 				if (!RestRequest.isSensorRegistred(sensor)) {
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
 			} catch (RequestErrorException e1) {
 				errorMessage = e1.getMessage();
 				Log.e(TAG, errorMessage);
 				return null;
 			}
 
 			MeasureJsonModel model = null;
 			if (sensor.getTemplate().equals("Numerical")) {
 				model = new NumericalMeasureJsonModel(sensorName, sensor.getUnit());
 			} else if (sensor.getTemplate().equals("String")) {
 				model = new StringMeasureJsonModel(sensorName, sensor.getUnit());
 			} else {
 				errorMessage = "Incorrect sensor template";
 				Log.e(TAG, errorMessage);
 				return null;
 			}
 
 			List<Integer> ids = new ArrayList<Integer>();
 			for (Long basetime : getBasetimes(sensorName)) {
 				model.setBt(basetime);	
 				String[] projection = {SensAppContract.Measure.ID, SensAppContract.Measure.VALUE, SensAppContract.Measure.TIME};
 				String selection = SensAppContract.Measure.SENSOR + " = \"" + model.getBn() + "\" AND " + SensAppContract.Measure.BASETIME + " = " + model.getBt() + " AND " + SensAppContract.Measure.UPLOADED + " = 0";
 				cursor = context.getContentResolver().query(uri, projection, selection, null, null);
 				if (cursor != null) {
 					if (cursor.getCount() > 0) {
 						int size = 0;
 						while (size == 0) {
 							while (cursor.moveToNext()) {
 								ids.add(cursor.getInt(cursor.getColumnIndexOrThrow(SensAppContract.Measure.ID)));
 								long time = cursor.getLong(cursor.getColumnIndexOrThrow(SensAppContract.Measure.TIME));
 								if (model instanceof NumericalMeasureJsonModel) {
 									float value = cursor.getFloat(cursor.getColumnIndexOrThrow(SensAppContract.Measure.VALUE));
 									((NumericalMeasureJsonModel) model).appendMeasure(value, time);
 									size += INTEGER_SIZE;
 								} else if (model instanceof StringMeasureJsonModel) {
 									String value = cursor.getString(cursor.getColumnIndexOrThrow(SensAppContract.Measure.VALUE));
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
 								errorMessage = e.getMessage();
 								Log.e(TAG, errorMessage);
 								if (e.getCause() != null) {
 									Log.e(TAG, e.getCause().getMessage());
 								}
								cursor.close();
 								return null;
 							}
 							model.clearValues();
 							publishProgress((int) ((float) (progress + ids.size()) / rowTotal * 100));
 						}
 						ContentValues values = new ContentValues();
 						values.put(SensAppContract.Measure.UPLOADED, 1);
 						selection = SensAppContract.Measure.ID + " IN " + ids.toString().replace('[', '(').replace(']', ')');
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
 		if (flag != FLAG_SILENT) {
 			notification.contentView.setProgressBar(R.id.status_progress, 100, values[0], false);
 			notificationManager.notify(NOTIFICATION_ID, notification);
 		}
 	}
 
 	@Override
 	protected void onPostExecute(Integer result) {
 		notificationManager.cancel(NOTIFICATION_ID);
 		if (result == null) {
 			Log.e(TAG, "Put data error");
 			if (flag != FLAG_SILENT) {
 				if (errorMessage == null) {
 					Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show();
 				} else {
 					Toast.makeText(context, "Upload failed:\n" + errorMessage, Toast.LENGTH_LONG).show();
 				}
 			}
 		} else {
 			Log.i(TAG, "Put data succed: " + result + " measures uploaded");
 			if (flag != FLAG_SILENT) {
 				final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, TabsActivity.class), 0);
 				Notification notificationFinal = new Notification(R.drawable.ic_launcher, "Upload finished", System.currentTimeMillis());
 				notificationFinal.flags |= Notification.FLAG_AUTO_CANCEL;
 				notificationFinal.setLatestEventInfo(context, "Upload succeed", result + " measures uploaded", pendingIntent);
 				notificationManager.notify(NOTIFICATION_FINAL_ID, notificationFinal);
 				if (result == 0) {
 					Toast.makeText(context, "No measures to upload", Toast.LENGTH_LONG).show();
 				} else {
 					Toast.makeText(context, "Upload succeed: " + result + " measures uploaded", Toast.LENGTH_LONG).show();
 				}
 			}
 		}
 		if (listenner != null) {
 			listenner.onTaskFinished(id);
 		}
 	}
 }
