 package org.sensapp.android.sensappdroid.restservice;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.sensapp.android.sensappdroid.contentprovider.SensAppCPContract;
 import org.sensapp.android.sensappdroid.datarequests.DatabaseRequest;
 import org.sensapp.android.sensappdroid.json.JsonPrinter;
 import org.sensapp.android.sensappdroid.json.MeasureJsonModel;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 public class PutMeasuresTask extends AsyncTask<Void, Integer, Integer> {
 	
 	private static final String TAG = PutMeasuresTask.class.getSimpleName();
 	
 	private Context context;
 	private Uri uri;
 	
 	public PutMeasuresTask(Context context, Uri uri) {
 		super();
 		this.context = context;
 		this.uri = uri;
 	}
 
 	private String getUnit(String sensorName) {
 		String[] projection = {SensAppCPContract.Sensor.UNIT};
 		Cursor cursor = context.getContentResolver().query(Uri.parse(SensAppCPContract.Sensor.CONTENT_URI + "/" + sensorName), projection, null, null, null); 
 		if (cursor != null) {
 			cursor.moveToFirst();
 			String unit = cursor.getString(cursor.getColumnIndexOrThrow(SensAppCPContract.Sensor.UNIT));
 			cursor.close();
 			return unit;
 		} 
 		return null;
 	}
 	
 	private Uri getUri(String sensorName) {
 		String[] projection = {SensAppCPContract.Sensor.URI};
 		Cursor cursor = context.getContentResolver().query(Uri.parse(SensAppCPContract.Sensor.CONTENT_URI + "/" + sensorName), projection, null, null, null); 
 		if (cursor != null) {
 			cursor.moveToFirst();
 			String uri = cursor.getString(cursor.getColumnIndexOrThrow(SensAppCPContract.Sensor.URI));
 			cursor.close();
 			return Uri.parse(uri);
 		} 
 		return null;
 	}
 	
 	private boolean isSensorUploaded(String sensorName) {
 		String[] projection = {SensAppCPContract.Sensor.NAME};
 		String selection = SensAppCPContract.Sensor.UPLOADED + " = 1";
 		Cursor cursor = context.getContentResolver().query(Uri.parse(SensAppCPContract.Sensor.CONTENT_URI + "/" + sensorName), projection, selection, null, null); 
 		if (cursor != null) {
 			boolean uploaded = cursor.getCount() > 0;
 			cursor.close();
 			return uploaded;
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
 	
 	private List<Integer> fillMeasureJsonModel(MeasureJsonModel model) {
 		List<Integer> ids = new ArrayList<Integer>();
 		String[] projection = {SensAppCPContract.Measure.ID, SensAppCPContract.Measure.VALUE, SensAppCPContract.Measure.TIME};
 		String selection = SensAppCPContract.Measure.BASETIME + " = " + model.getBt() + " AND " + SensAppCPContract.Measure.UPLOADED + " = 0";
 		Cursor cursor = context.getContentResolver().query(Uri.parse(SensAppCPContract.Measure.CONTENT_URI + "/" + model.getBn()), projection, selection, null, null);
 		if (cursor != null) {
 			while (cursor.moveToNext()) {
 				ids.add(cursor.getInt(cursor.getColumnIndexOrThrow(SensAppCPContract.Measure.ID)));
 				int value = cursor.getInt(cursor.getColumnIndexOrThrow(SensAppCPContract.Measure.VALUE));
 				long time = cursor.getLong(cursor.getColumnIndexOrThrow(SensAppCPContract.Measure.TIME));
 				model.appendMeasure(value, time);
 			}
 			cursor.close();
 		}
 		return ids;
 	}
 	
 	@Override
 	protected Integer doInBackground(Void... params) {
 		int rowsUploaded = 0;
 		
 		ArrayList<String> sensorNames = new ArrayList<String>();
 		Cursor cursor = context.getContentResolver().query(uri, new String[]{"DISTINCT " + SensAppCPContract.Measure.SENSOR}, null, null, null);
 		if (cursor != null) {
 			while (cursor.moveToNext()) {
 				sensorNames.add(cursor.getString(cursor.getColumnIndexOrThrow(SensAppCPContract.Measure.SENSOR)));
 			}
 			cursor.close();
 		}
 		
 		for (String sensorName : sensorNames) { 
 			
 			if (!isSensorUploaded(sensorName)) {
 				Uri postSensorResult = null;
 				try {
 					postSensorResult = new PostSensorRestTask(context).executeOnExecutor(THREAD_POOL_EXECUTOR, sensorName).get();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} catch (ExecutionException e) {
 					e.printStackTrace();
 				}
 				if (postSensorResult == null) {
 					Log.e(TAG, "Post sensor failed");
 					return null;
 				}
 				ContentValues values = new ContentValues();
 				values.put(SensAppCPContract.Sensor.UPLOADED, 1);
 				DatabaseRequest.SensorRQ.updateSensor(context, sensorName, values);
 			}
 			
 			Uri uri = getUri(sensorName);
 			List<Integer> ids = new ArrayList<Integer>();
 			for (Long basetime : getBasetimes(sensorName)) {
 				MeasureJsonModel model = new MeasureJsonModel(sensorName, basetime, getUnit(sensorName));
				ids.addAll(fillMeasureJsonModel(model));
 				if (ids.size() > 0) {
 					try {
 						RestRequest.putData(uri, JsonPrinter.measuresToJson(model));
 					} catch (RequestErrorException e) {
 						Log.e(TAG, e.getMessage());
 						if (e.getCause() != null) {
 							Log.e(TAG, e.getCause().getMessage());
 						}
 						return null;
 					}
 				}
 			}
 			ContentValues values = new ContentValues();
 			values.put(SensAppCPContract.Measure.UPLOADED, 1);
 			String selection = SensAppCPContract.Measure.ID + " IN " + ids.toString().replace('[', '(').replace(']', ')');
 			rowsUploaded += context.getContentResolver().update(Uri.parse(SensAppCPContract.Measure.CONTENT_URI + "/" +  sensorName), values, selection, null);
 		}
 		return rowsUploaded;
 	}
 
 	@Override
 	protected void onPostExecute(Integer result) {
 		super.onPostExecute(result);
 		if (result == null) {
 			Log.e(TAG, "Put data error");
 			Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show();
 		} else {
 			Log.i(TAG, "Put data succed: " + result + " measures uploaded");
 			Toast.makeText(context, "Upload succeed", Toast.LENGTH_LONG).show();
 		}
 	}
 }
