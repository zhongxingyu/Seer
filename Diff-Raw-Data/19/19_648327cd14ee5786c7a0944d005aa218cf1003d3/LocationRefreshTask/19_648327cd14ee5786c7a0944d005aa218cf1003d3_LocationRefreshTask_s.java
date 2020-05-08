 package uk.co.mentalspace.android.bustimes;
 
 import uk.co.mentalspace.android.bustimes.db.LocationsDBAdapter;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public abstract class LocationRefreshTask extends AsyncTask<Void, Integer, String> {
 	private static final String LOGNAME = "LocationRefreshTask";
 	
 	protected static final int PROGRESS_POSITION_CONTACTING_SERVER = 0;
 	protected static final int PROGRESS_POSITION_DOWNLOADING_DATA = 1;
 	protected static final int PROGRESS_POSITION_PROCESSING_DATA = 2;
 	
 	protected static final int PROGRESS_INDEX_PROGRESS_LABEL = 0;
 	protected static final int PROGRESS_INDEX_PROGRESS_VALUE = 1;
 	
 	protected boolean isFinished = false;
 	protected Context ctx = null;
 	protected LocationsDBAdapter ldba = null;	
 	protected Exception failure = null;
 	
 	protected int maxProgress = 0;
 	protected int currentProgress = 0;
 	protected String currentProgressLabel = "";
 	
 	protected String[] progressLabels = new String[] {"Contacting server", "Downloading data", "Processing records"};
 	
 	public void init(Context ctx) {
 		ldba = new LocationsDBAdapter(ctx);
 		this.ctx = ctx; 
 	}
 	
 	public int getMaxProgress() {
 		return maxProgress;
 	}
 	
 	public int getCurrentProgress() {
 		return currentProgress;
 	}
 	
 	public String getCurrentProgressLabel() {
 		return currentProgressLabel;
 	}
 	
 	public Exception getFailure() {
 		return failure;
 	}
 	
 	public boolean isFinished() {
 		return isFinished;
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		maxProgress = 20000; //TODO remove hard-coded value
 		currentProgress = 0;
 		currentProgressLabel = "";
 		triggerProgressUpdate();
 	}
 	
 	protected void publishProgress(int labelIndex, int value) {
		Log.d(LOGNAME, "Updating progress values");
 		String progressLabel = progressLabels[labelIndex];
 		if (PROGRESS_POSITION_PROCESSING_DATA == labelIndex) progressLabel += " ("+value+" / "+maxProgress+")";
 
 		currentProgress = value;
 		currentProgressLabel = progressLabel;
 
 		triggerProgressUpdate();
 	}
 	
 	protected void triggerProgressUpdate() {
		Log.d(LOGNAME, "Sending intent to trigger progress update");
 		Intent intent = new Intent(ctx, LocationRefreshService.class);
 		intent.setAction(LocationRefreshService.ACTION_GET_REFRESH_PROGRESS);
 		intent.putExtra(LocationRefreshService.EXTRA_SOURCE_NAME, getSourceId());
 		ctx.startService(intent);
 	}
 
 	@Override
 	protected void onProgressUpdate(Integer... progress) {		
 		Log.d(LOGNAME, "onProgressUpdate called - transferring to progressUpdate");
 		int labelIndex = progress[PROGRESS_INDEX_PROGRESS_LABEL];
 		int value = progress[PROGRESS_INDEX_PROGRESS_VALUE];
 		publishProgress(labelIndex, value);
 	}
 	
 	protected void finish() {
 		isFinished = true;
 		
 		if (null != ldba) {
 			ldba.close();
 			ldba = null;
 		}
 		
 		//notify the service that this task is complete
 		Intent intent = new Intent(ctx, LocationRefreshService.class);
 		intent.setAction(LocationRefreshService.ACTION_LOCATION_REFRESH_TASK_COMPLETE);
 		intent.putExtra(LocationRefreshService.EXTRA_SOURCE_NAME, getSourceId());
 		ctx.startService(intent);
 	}
 
 	protected abstract String doInBackground(Void... strings);
 
 	public abstract String getSourceId();
 	
 }
