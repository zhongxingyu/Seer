 package com.quizz.places.db;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.lang.reflect.Type;
 import java.util.Collection;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.quizz.core.application.BaseQuizzApplication;
 import com.quizz.core.db.QuizzDAO;
 import com.quizz.core.managers.DataManager;
 import com.quizz.core.models.Section;
 import com.quizz.places.application.QuizzPlacesApplication;
 
 public class GameDataLoading {
 
 	public static final String DEBUG_TAG = GameDataLoading.class.getSimpleName();
 
 	private Context mContext;
 	private GameDataLoadingListener mGameDataLoadingListener;
 	
 	public interface GameDataLoadingListener {
 		public void onGameLoadingStart();
 		public void onGameLoadingProgress(int progress);
 		public void onGameLoadingSuccess(List<Section> sections);
 		public void onGameLoadingFailure(Exception e);
     }
 	
 	public GameDataLoading(Context pContext) {
 		if (pContext instanceof GameDataLoadingListener) {
 			mContext = pContext;
 			mGameDataLoadingListener = (GameDataLoadingListener) mContext;
 		} else {
 			throw new ClassCastException(pContext.toString() + " must implement GameDataLoadingListener");
 		}
 	}
 
 	private boolean initVersionInPreferences() {
 		SharedPreferences sharedPreferences = ((Activity) mContext).getPreferences(Application.MODE_PRIVATE);
 		Editor editor = sharedPreferences.edit();
 		if (!sharedPreferences.contains(BaseQuizzApplication.PREF_VERSION_KEY)) {
 			editor.putInt(BaseQuizzApplication.PREF_VERSION_KEY, BaseQuizzApplication.PREF_VERSION_VALUE);
 		}
 		return editor.commit();
 	}
 	
 	private boolean initNbHintsAvailableInPreferences() {
 		SharedPreferences sharedPreferences = ((Activity) mContext).getPreferences(Application.MODE_PRIVATE);
 		Editor editor = sharedPreferences.edit();
 		editor.putInt(BaseQuizzApplication.PREF_UNLOCKED_HINTS_COUNT_KEY, 
 				BaseQuizzApplication.PREF_DEFAULT_UNLOCKED_HINTS_COUNT_VALUE);
 		return editor.commit();
 	}
 
 	public boolean upgradeVersionInPreferences() {
 		SharedPreferences sharedPreferences = ((Activity) mContext).getPreferences(Application.MODE_PRIVATE);
 		Editor editor = sharedPreferences.edit();
 		editor.putInt(BaseQuizzApplication.PREF_VERSION_KEY, BaseQuizzApplication.PREF_VERSION_VALUE);
 		return editor.commit();
 	}
 
 	public void initPreferences() {
 		initVersionInPreferences();
 		initNbHintsAvailableInPreferences();
 	}
 	
 	public boolean isDbUpgradeNeeded() {
 		SharedPreferences sharedPreferences = ((Activity) mContext).getPreferences(Application.MODE_PRIVATE);
 		if (sharedPreferences.getInt(BaseQuizzApplication.PREF_VERSION_KEY, 0) 
 				< BaseQuizzApplication.PREF_VERSION_VALUE) {
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isFirstLaunch() {
 		SharedPreferences sharedPreferences = ((Activity) mContext).getPreferences(Application.MODE_PRIVATE);
 		if (!sharedPreferences.contains(BaseQuizzApplication.PREF_VERSION_KEY)) {
 			return true;
 		}
 		return false;
 	}
 	
 	public void executeAsyncGameLoading(boolean loadFromJson) {
 		new AsyncDataLoader(loadFromJson).execute();
 	}
 
 	// ===========================================================
 	// Inner classes
 	// ===========================================================
 
 	/**
 	 * First launch asyncTask<br />
 	 * Initiates database and fill it with json file content
 	 * 
 	 */
 	private class AsyncDataLoader extends AsyncTask<Void, Integer, List<Section>> {
 
 		private boolean mLoadFromJson = false;
 		private Exception mException = null;
 		
 		public AsyncDataLoader(boolean loadFromJson) {
 			mLoadFromJson = loadFromJson;
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			mGameDataLoadingListener.onGameLoadingStart();
 			initPreferences();
 			Log.e("ASYNC", "onPreExecute: "+System.currentTimeMillis());
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			if (progress[0] <= 100) {
 				mGameDataLoadingListener.onGameLoadingProgress(progress[0]);
 			}
 		}
 
 		@Override
 		protected void onPostExecute(List<Section> result) {
 			Log.e("ASYNC", "onPostExecute: "+System.currentTimeMillis());
 			if (mException != null) {
 				mGameDataLoadingListener.onGameLoadingFailure(mException);
 			} else {
 				mGameDataLoadingListener.onGameLoadingSuccess(result);
 			}
 		}
 
 		@Override
 		protected List<Section> doInBackground(Void... arg0) {
 			Log.e("ASYNC", "doInBackground: "+System.currentTimeMillis());
 			List<Section> sections = null;
 			if (mLoadFromJson) {
 				Gson gson = new Gson();
 				Type type = new TypeToken<Collection<Section>>() {}.getType();
 				try {
 					Log.e("ASYNC", "InputStream: "+System.currentTimeMillis());
 					InputStream is = mContext.getResources().getAssets().open(QuizzPlacesApplication.JSON_FILE);
 					Log.e("ASYNC", "reader: "+System.currentTimeMillis());
 					Reader reader = new InputStreamReader(is);
 					Log.e("ASYNC", "sections: "+System.currentTimeMillis());
 					sections = gson.fromJson(reader, type);
 					Log.e("ASYNC", "setSections: "+System.currentTimeMillis());
 					DataManager.setSections(sections);
 					Log.e("ASYNC", "for: "+System.currentTimeMillis());
 					if (sections.size() > 0) {
 						int progress = 0;
						int ratio = 100 / sections.size();
 						Log.e("ASYNC", "before inserts: "+System.currentTimeMillis());
 						for (Section section : sections) {
 							section.status = (section.number == 1) ? Section.SECTION_UNLOCKED : Section.SECTION_LOCKED;
 							QuizzDAO.INSTANCE.insertSection(section);
							int progressTmp = ++progress * ratio;
 							Log.e("ASYNC", "progress: "+progressTmp+", time: "+System.currentTimeMillis());
 							publishProgress(progressTmp);
 						}
 						Log.e("ASYNC", "after inserts: "+System.currentTimeMillis());
 					} else {
 						publishProgress(100);
 					}
 				} catch (IOException e) {
 					Log.e(DEBUG_TAG, e.getMessage(), e);
 					mException = e;
 				}
 			} else {
 				DataManager.getSections();
 			}
 			DataManager.dataLoaded = true;
 			return sections;
 		}
 	}
 	
 }
