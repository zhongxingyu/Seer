 package com.abbyy.cloudocr;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 
 import com.abbyy.cloudocr.database.TasksContract;
 
 public class Task {
 	DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss'Z'",
 			Locale.GERMANY);
 
 	private Context mContext;
 	private boolean isInDb;
 
 	public String mTaskId;
 	public String mStatus;
 	public String mRegistrationTime;
 	public Date mRegistrationDate;
 	public String mStatusChangeTime;
 	public Date mStatusChangeDate;
 	public String mFilesCount;
 	public String mCredits;
 	public String mEstimatedProcessingTime;
 	public String mDescription;
 	public String mResultUrl;
 	public String mResultType;
 	public String mError;
 	public String mFileName;
 
 	public Task(Context context, String taskId, String status,
 			String registrationTime, String statusChangeTime,
 			String filesCount, String credits, String estimatedProcessingTime,
 			String description, String resultUrl, String error, boolean isInDb,
 			String fileName, String resultType) {
 		mContext = context;
 		this.isInDb = isInDb;
 		mTaskId = taskId;
 		mStatus = status;
 		mRegistrationTime = registrationTime;
 		mStatusChangeTime = statusChangeTime;
 		mFilesCount = filesCount;
 		mCredits = credits;
 		mEstimatedProcessingTime = estimatedProcessingTime;
 		mDescription = description;
 		mResultUrl = resultUrl;
 		mError = error;
 		mFileName = fileName;
 		mResultType = resultType;
 	}
 
 	public Task(Context context, HashMap<String, String> data, String fileName,
 			String exportFormat) {
 		mContext = context;
 		mTaskId = data.get(context.getString(R.string.field_id));
 		mStatus = data.get(context.getString(R.string.field_status));
 		mRegistrationTime = data.get(context
 				.getString(R.string.field_registration_time));
 		mStatusChangeTime = data.get(context
 				.getString(R.string.field_status_change_time));
 		mFilesCount = data.get(context.getString(R.string.field_files_count));
 		mCredits = data.get(context.getString(R.string.field_credits));
 		if (data.get(context
 				.getString(R.string.field_estimated_processing_time)) != null) {
 			mEstimatedProcessingTime = data.get(context
 					.getString(R.string.field_estimated_processing_time));
 		}
 		mDescription = data.get(context.getString(R.string.field_description));
 		if (data.get(context.getString(R.string.field_result_url)) != null) {
 			mResultUrl = data.get(context.getString(R.string.field_result_url));
 		}
 		if (data.get(context.getString(R.string.field_error)) != null) {
 			mError = data.get(context.getString(R.string.field_error));
 		}
 		mFileName = fileName;
 		mResultType = exportFormat;
 	}
 
 	public Task(Cursor cursor) {
 		if (cursor != null) {
 			if (cursor.moveToFirst()) {
 				mDescription = cursor.getString(cursor
 						.getColumnIndex(TasksContract.TasksTable.DESCRIPTION));
 				mStatus = cursor.getString(cursor
 						.getColumnIndex(TasksContract.TasksTable.STATUS));
 				mRegistrationTime = cursor
 						.getString(cursor
 								.getColumnIndex(TasksContract.TasksTable.REGISTRATION_TIME));
 				mStatusChangeTime = cursor
 						.getString(cursor
 								.getColumnIndex(TasksContract.TasksTable.STATUS_CHANGE_TIME));
 				mFilesCount = cursor.getString(cursor
 						.getColumnIndex(TasksContract.TasksTable.FILES_COUNT));
 				mCredits = cursor.getString(cursor
 						.getColumnIndex(TasksContract.TasksTable.CREDITS));
 				mEstimatedProcessingTime = cursor
 						.getString(cursor
 								.getColumnIndex(TasksContract.TasksTable.ESTIMATED_PROCESSING_TIME));
 				mResultUrl = cursor.getString(cursor
 						.getColumnIndex(TasksContract.TasksTable.RESULT_URL));
 				mError = cursor.getString(cursor
 						.getColumnIndex(TasksContract.TasksTable.ERROR));
 				mFileName = cursor.getString(cursor
 						.getColumnIndex(TasksContract.TasksTable.FILENAME));
 				mResultType = cursor.getString(cursor
 						.getColumnIndex(TasksContract.TasksTable.RESULT_TYPE));
 
 				try {
 					mRegistrationDate = dateFormat.parse(mRegistrationTime);
 					mStatusChangeDate = dateFormat.parse(mStatusChangeTime);
 				} catch (ParseException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public boolean writeTaskToDb() {
 		isInDb = checkDb();
 
 		Uri uri = TasksContract.CONTENT_TASKS;
 
 		if (isInDb) {
 			String where = TasksContract.TasksTable.TASK_ID + "=?";
 			String[] selectionArgs = { mTaskId };
 			int count = mContext.getContentResolver().update(uri, setValues(),
 					where, selectionArgs);
 			if (count < 1) {
 				return false;
 			}
 		} else {
 			mContext.getContentResolver().insert(uri, setValues());
 		}
 		return isInDb;
 	}
 
 	private boolean checkDb() {
 		Uri uri = TasksContract.CONTENT_TASKS;
 		String selection = TasksContract.TasksTable.TASK_ID + "=?";
 		String[] selectionArgs = { mTaskId };
 
 		Cursor cursor = mContext.getContentResolver().query(uri, null,
 				selection, selectionArgs, null);
		int count = cursor.getCount();
		cursor.close();
		return count >= 1;
 	}
 
 	private ContentValues setValues() {
 		ContentValues values = new ContentValues();
 		values.put(TasksContract.TasksTable.TASK_ID, mTaskId);
 		if (mCredits != null) {
 			values.put(TasksContract.TasksTable.CREDITS, mCredits);
 		}
 		if (mDescription != null) {
 			values.put(TasksContract.TasksTable.DESCRIPTION, mDescription);
 		}
 		if (mError != null) {
 			values.put(TasksContract.TasksTable.ERROR, mError);
 		}
 		if (mEstimatedProcessingTime != null) {
 			values.put(TasksContract.TasksTable.ESTIMATED_PROCESSING_TIME,
 					mEstimatedProcessingTime);
 		}
 		if (mFilesCount != null) {
 			values.put(TasksContract.TasksTable.FILES_COUNT, mFilesCount);
 		}
 		if (mRegistrationTime != null) {
 			values.put(TasksContract.TasksTable.REGISTRATION_TIME,
 					mRegistrationTime);
 		}
 		if (mResultUrl != null) {
 			values.put(TasksContract.TasksTable.RESULT_URL, mResultUrl);
 		}
 		if (mStatus != null) {
 			values.put(TasksContract.TasksTable.STATUS, mStatus);
 		}
 		if (mStatusChangeTime != null) {
 			values.put(TasksContract.TasksTable.STATUS_CHANGE_TIME,
 					mStatusChangeTime);
 		}
 		if (mFileName != null) {
 			values.put(TasksContract.TasksTable.FILENAME, mFileName);
 		}
 		if (mResultType != null) {
 			values.put(TasksContract.TasksTable.RESULT_TYPE, mResultType);
 		}
 		return values;
 	}
 }
