 package de.hanneseilers.mensa_sh.loader;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.hanneseilers.mensa_sh.CacheManager;
 import de.hanneseilers.mensa_sh.activities.ActivityMain;
 import de.hanneseilers.mensa_sh.enums.LoadingProgress;
 import de.mensa.sh.core.Mensa;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 
 /**
  * Class for async loading of cities
  * @author hannes
  *
  */
 public class AsyncCitiesLoader extends AsyncTask<Void, Integer, List<String>> {
 	
 	private ActivityMain ctx;
 	public static String cachedFileName = "saved_city";
 	
 	public AsyncCitiesLoader(ActivityMain ctx){
 		super();
 		this.ctx = ctx;
 	}
 	
 	/**
 	 * Gets cities
 	 */
 	@Override
 	protected List<String> doInBackground(Void... params) {		
 		// check if data is cache
 		String ret;
 		List<String> retList = new ArrayList<String>();
 
 		if( (ret = CacheManager.readCachedFile(ctx, "cities")) != null ){
 			String retArray[] = ret.split(";");
 			for( String city : retArray ){
 				retList.add(city);
 			}
 		}
 		
 		if( retList.size() == 0 ){			
 			// cache file
 			retList = Mensa.getCities();
 			ret = "";
 			for( String city : retList ){
 				ret += city + ";";
 			}
 			CacheManager.writeChachedFile(ctx, "cities", ret);
 		}
 		
 		return retList;
 	}
 	
 	/**
 	 * Adds cities to list
 	 */
 	@Override
 	protected void onPostExecute(List<String> result) {
 		ctx.clearCitiesAdapter();
 		for(String c : result){
 			ctx.addCity(c);
 		}
 		ctx.notifyCityAdapter();
 		ctx.getSpinnerCity().setSelection(getSelection(result));
 		ctx.setLoadingProgress(LoadingProgress.CITIES_LOADED);
 	}
 	
 	/**
 	 * Returns the position to select
 	 * @return
 	 */
 	private int getSelection(List<String> result){
 		// get settings
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
 		
 		// check if to load position
 		if( sharedPref.getBoolean("SAVE_LAST_MENSA", false) && ctx.isFirstSelection() ){
 			String city;
			if( (city = CacheManager.readCachedFile(ctx, cachedFileName, false)) != null ){
 				return result.indexOf(city);
 			}
 		}
 		
 		return 0;
 	}
 
 }
