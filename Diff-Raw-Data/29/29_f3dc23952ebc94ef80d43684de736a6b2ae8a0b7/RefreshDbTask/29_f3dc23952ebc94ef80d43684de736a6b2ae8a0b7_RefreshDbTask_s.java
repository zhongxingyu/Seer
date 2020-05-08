 package no.ctryti.dagensatuio;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class RefreshDbTask extends AsyncTask<Void, Integer, Void>  {
 	
 	private static final String TAG = "RefreshDBTask";
 	private Activity mCtx;
 	private DatabaseAdapter mDbAdapter;
 	
 	private static final int DONE = 1;
 	private static final int DOWNLOADING = 2;
 	private static final int PARSING = 3;
 	
	
 	public RefreshDbTask(Activity ctx) {
 		this.mCtx = ctx;
		mDbAdapter = new DatabaseAdapter(ctx);
		
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		mCtx.setProgressBarVisibility(true);
 		mCtx.setProgressBarIndeterminateVisibility(true);
 		mCtx.setProgress(0);
 		mCtx.setTitle("Loading: " + Settings.Place.values()[0].getName());
 	}
 
 	@Override
 	protected void onPostExecute(Void result) {
 		mCtx.setProgress(10000);
 		mCtx.setTitle(R.string.app_name);
 		mDbAdapter.close();
 	}
 
 	@Override
 	protected void onProgressUpdate(Integer... values) {
 		switch (values[0]) {
 		case DONE:
 			mCtx.setTitle("Done!");
 			break;
 		case DOWNLOADING:
 			mCtx.setTitle("Downloading: " + Settings.Place.values()[values[1]].getName());
 			break;
 		case PARSING:
 			mCtx.setTitle("Parsing    : " + Settings.Place.values()[values[1]].getName());
 			break;
 		default:
 			if (values[1] != Settings.Place.values().length) {
 				mCtx.setTitle("Finished: " + Settings.Place.values()[values[1]].getName());
 			}
 			mCtx.setProgress(values[0]-1);
 			break;
 		}
 	}
 
 	@Override
 	protected Void doInBackground(Void... params) {
 
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpResponse response;
 		HttpEntity entity;
 		HttpGet get;
 		ArrayList<DinnerItem> dinnerItems = new ArrayList<DinnerItem>();
 
 		int progresStatus = 0;
 		int total = Settings.Place.values().length;
 
 		/* Each of the enum Place contains a name and URI. */
 		for (Settings.Place place : Settings.Place.values()) {
 			publishProgress(DOWNLOADING, progresStatus);
 			try {
 				get = new HttpGet(place.getURI());
 				response = client.execute(get);
 				entity = response.getEntity();
 				dinnerItems.clear();
 				dinnerItems.addAll(SiOParser.parse(entity.getContent(), place.getName()));
 			} catch (ClientProtocolException e) {
 				Log.w(TAG, "ClientProtocolException: " + place.getName());
 			} catch (IOException e) {
 				Log.w(TAG, "IOException: " + place.getName());
 			}
 			publishProgress(PARSING, progresStatus);
 			ContentValues v = new ContentValues();
 			for (DinnerItem t : dinnerItems) {
 				v.clear();
 				v.put(DatabaseAdapter.KEY_DAY, t.getDay());
 				v.put(DatabaseAdapter.KEY_DESC, t.getDescription());
 				v.put(DatabaseAdapter.KEY_PLACE, t.getPlace());
 				v.put(DatabaseAdapter.KEY_TYPE, t.getType());
 				int gluten = t.isGluten() ? 1 : 0;
 				int laktose = t.isLaktose() ? 1 : 0;
 				v.put(DatabaseAdapter.KEY_GLUTEN, gluten);
 				v.put(DatabaseAdapter.KEY_LAKTOSE, laktose);
 				mDbAdapter.insert(v);
 				
 			}
 			Log.i(TAG, "Finished adding " + place.getName());
 			progresStatus++;
 			publishProgress((int) (((double) progresStatus / total) * 10000), progresStatus);
 			
 		}
 		publishProgress(DONE);
 		try {
 			Thread.sleep(3000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 }
 
