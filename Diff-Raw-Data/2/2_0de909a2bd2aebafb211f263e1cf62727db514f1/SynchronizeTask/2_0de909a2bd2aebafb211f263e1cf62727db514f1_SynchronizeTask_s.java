 package ch.hsr.traildevil.sync;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import ch.hsr.traildevil.TraillistActivity;
 import ch.hsr.traildevil.domain.Trail;
 import ch.hsr.traildevil.util.Constants;
 import ch.hsr.traildevil.util.network.HttpHandler;
 import ch.hsr.traildevil.util.persistence.TrailProvider;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 
 public class SynchronizeTask extends AsyncTask<String, String, Long> {
 	
 	private static final String TAG_PREFIX = SynchronizeTask.class.getSimpleName() + ": ";
 	
 	private final TraillistActivity activity;
 	private final TrailProvider trailProvider;
 
 	public SynchronizeTask(TraillistActivity activity) {
 		this.activity = activity;
 		this.trailProvider = TrailProvider.getInstance(Constants.DB_LOCATION);
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		activity.showDialog(TraillistActivity.DIALOG_PROGRESS_ID);
 	}
 
 	@Override
 	protected Long doInBackground(String... params) {
 		
 		Log.i(Constants.TAG, TAG_PREFIX + "start synchronizing");
 		
 		String updateUrl = params[0];
 		boolean firstDownload = Boolean.valueOf(params[1]);
 		
 		publishProgress("get updates from server");
 		List<Trail> trails = loadTrailsData(updateUrl);
 		
 		if(isCancelled())
 			return null;
 		
 		
 		publishProgress("save data");
 		long result = firstDownload ? fillDb(trails) : updateDb(trails);
 		
 		if(isCancelled())
 			return null;
 		
 		return result;
 	}
 	
 
 	/**
 	 * This method is invoked from the UI Thread and is therefore safe to update the
 	 * UI.
 	 */
 	@Override
 	protected void onProgressUpdate(String... values) {
 		// TODO update progress dialog
 	}	
 	
 	/**
 	 * Is invoked when the doInBackground() method has finished and AsyncTask.cancel() has not been invoked. 
 	 * This method is invoked from the UI Thread and is therefore safe to update the UI.
 	 */
 	@Override
 	protected void onPostExecute(Long result) {
 		// commit db changes
 		Log.i(Constants.TAG, TAG_PREFIX + "Start commiting data");
 		trailProvider.commit();
 		Log.i(Constants.TAG, TAG_PREFIX + "data commited");
 		
 		// store new timestamp (Note that when no update was found, 0 is passed as result)
 		long lastModifiedTimestamp = Math.max(result, activity.getLastModifiedTimestamp());
 		activity.setLastModifiedTimestamp(lastModifiedTimestamp);
 		Log.i(Constants.TAG, TAG_PREFIX + "synchronizing completed.");
 		
 		activity.syncCompleted();
 	}
 
 	/**
 	 * This method is invoked when the async task is canceled. This is the case when
 	 * the user presses the "cancel" button on the progress dialog. 
 	 */
 	@Override
 	protected void onCancelled() {
 		Log.i(Constants.TAG, TAG_PREFIX + "synchronizing cancelled. Data is rolled back.");
 		
 		trailProvider.rollback();
 		activity.syncAborted();
 	}
 	
 	/**
 	 * Fills the Db for the first time. It should just be used for this purpose. 
 	 * It is just done in a second method, for better maintainability. 
 	 * Note that the data is not yet persited after this method call!
 	 * 
 	 * @param trails The trails to fill.
 	 * @return the latest modified timestamp
 	 */
 	private long fillDb(List<Trail> trails){
 		Log.i(Constants.TAG, TAG_PREFIX + "Start filling the db for the first time");
 		long lastModifiedTimestamp = 0;
 		
 		for(Trail newTrail : trails){
 			
 			if(isCancelled()){
 				Log.i(Constants.TAG, TAG_PREFIX + "Stop filling the db, since cancel was invoked");
 				return -1;
 			}
 			
 			lastModifiedTimestamp = Math.max(lastModifiedTimestamp, newTrail.getModifiedUnixTs());
 			
			if(isDeletedTrail(newTrail)){ 
 				trailProvider.store(newTrail);
 			}
 		}	
 		
 		Log.i(Constants.TAG, TAG_PREFIX + "Db fill complete, but commit is outstanding");
 		return lastModifiedTimestamp;		
 	}
 	
 	/**
 	 * Updates the DB with the new data. Note that the data is not yet persited after this method call!
 	 * 
 	 * @param trails A list of Trails to update
 	 * @return The max value of last modified timestamps or 0 if no updates were found
 	 */
 	private long updateDb(List<Trail> trails) {
 		Log.i(Constants.TAG, TAG_PREFIX + "Start updating the db");
 		long lastModifiedTimestamp = 0;
 		
 		//TODO Handle if data has already been persisted, but last modified timestamp has not been updated!
 		//	   Check if updatedTrail has status deleted, if so and if existingTrail is null, it means that
 		//	   we deleted the data before, but didn't set the lastmodified timestamp!
 		for(Trail updatedTrail : trails){
 			
 			if(isCancelled()){
 				Log.i(Constants.TAG, TAG_PREFIX + "Stop updating the db, since cancel was invoked");
 				return -1;
 			}
 			
 			lastModifiedTimestamp = Math.max(lastModifiedTimestamp, updatedTrail.getModifiedUnixTs());
 
 			Log.i(Constants.TAG, TAG_PREFIX + "Find trail with id = " + updatedTrail.getId());
 			Trail existingTrail = trailProvider.find(updatedTrail.getId());
 			Log.i(Constants.TAG, TAG_PREFIX + "Trail found");
 			
 			if(isNewTrail(existingTrail)){
 				if(!isDeletedTrail(updatedTrail)){
 					trailProvider.store(updatedTrail);
 				}else{
 					// do nothing, since trail is not in local db, 
 					// but has already been deleted on server side
 				}
 			}else{
 				if(isDeletedTrail(updatedTrail)){ // deleted
 					trailProvider.delete(existingTrail);
 				}else{ // modified
 					trailProvider.delete(existingTrail);
 					trailProvider.store(updatedTrail);
 				}
 			}
 			Log.i(Constants.TAG, TAG_PREFIX + "Trail updated");
 		}	
 		
 		Log.i(Constants.TAG, TAG_PREFIX + "Db update complete, but commit is outstanding");
 		return lastModifiedTimestamp;
 	}
 
 	private boolean isDeletedTrail(Trail updatedTrail) {
 		return updatedTrail.getDeletedUnixTs() > 0;
 	}
 
 	/**
 	 * It is a new trail if it doesn't exist on the db yet.
 	 * 
 	 * @param existingTrail The trail to check
 	 * @return true if it 
 	 */
 	private boolean isNewTrail(Trail existingTrail) {
 		return existingTrail == null;
 	}
 	
 	private List<Trail> loadTrailsData(String url) {
 		Log.i(Constants.TAG, TAG_PREFIX + "Start downloading new data from the web");
 		HttpHandler httpHandler = new HttpHandler();
 		httpHandler.connectTo(url, HttpHandler.TYPE_JSON);
 		
 		List<Trail> trails = new ArrayList<Trail>(100);
 
 		try{
 			Gson gson = new Gson();
 			JsonElement json = new JsonParser().parse(httpHandler.getReader());
 			for (JsonElement element : json.getAsJsonArray()) {
 				
 				if(isCancelled()){
 					Log.i(Constants.TAG, TAG_PREFIX + "Stop downloading data, since cancel was invoked");
 					return null;
 				}
 				
 				trails.add(gson.fromJson(element, Trail.class));
 			}
 		}finally{
 			httpHandler.resetStream(); // ensure that the stream is closed
 		}
 		
 		Log.i(Constants.TAG, TAG_PREFIX + "Data downloaded");
 		return trails;
 	}
 }
