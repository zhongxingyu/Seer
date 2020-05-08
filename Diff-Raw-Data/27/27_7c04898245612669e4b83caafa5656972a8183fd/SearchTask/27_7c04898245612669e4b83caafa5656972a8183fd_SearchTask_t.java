 package wei.mark.tabletennis.util;
 
 import java.util.ArrayList;
 
 import wei.mark.tabletennis.model.PlayerModel;
 import android.os.AsyncTask;
 
 public class SearchTask extends AsyncTask<String, Void, ArrayList<PlayerModel>> {
 	SearchCallback callback;
 	String provider, query, id;
	boolean user, hasSavedResult;
 	ArrayList<PlayerModel> savedResult;
 
 	public SearchTask(SearchCallback searchCallback) {
 		callback = searchCallback;
 	}
 
 	@Override
 	protected ArrayList<PlayerModel> doInBackground(String... params) {
 		try {
 			id = params[0];
 			provider = params[1];
 			query = params[2];
 			user = Boolean.parseBoolean(params[3]);
 
 			AppEngineParser parser = AppEngineParser.getParser();
 
 			return parser.execute(id, provider, query);
 		} catch (Exception ex) {
 			return null;
 		}
 	}
 
 	@Override
 	protected void onPostExecute(ArrayList<PlayerModel> result) {
		hasSavedResult = true;
 		savedResult = result;
 
 		if (callback != null)
 			callback.searchCompleted(savedResult);
 	}
 
 	public void setSearchCallback(SearchCallback searchCallback) {
 		callback = searchCallback;
 
		if (callback != null && hasSavedResult)
 			callback.searchCompleted(savedResult);
 	}
 }
