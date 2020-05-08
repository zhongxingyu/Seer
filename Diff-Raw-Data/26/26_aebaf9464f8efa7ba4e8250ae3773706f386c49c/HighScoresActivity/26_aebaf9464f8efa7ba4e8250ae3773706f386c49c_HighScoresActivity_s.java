 package labs.brian.brianstetris;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 
 import labs.brian.adapters.HighScoreAdapter;
 import labs.brian.beans.HighScoreInfo;
 import labs.brian.database.DatabaseHelper;
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ListView;
 
 public class HighScoresActivity extends Activity {
 
 	private ListView list;
 	private DatabaseHelper databaseHelper = null;
 	private HighScoreAdapter adapter;
 	
 	@Override
 	public void onCreate(Bundle saved){
 		super.onCreate(saved);
 		setContentView(R.layout.highscores_screen);
 		
 		list = (ListView)findViewById(R.id.listView1);
 		
 		adapter = new HighScoreAdapter(this);
 		list.setAdapter(adapter);
 		
 		List<HighScoreInfo> list;
 		try {
 			list = getHelper().getSimpleDataDao().queryForAll();
			adapter.setData(list);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		/*
 		 * You'll need this in your class to release the helper when done.
 		 */
 		if (databaseHelper != null) {
 			OpenHelperManager.releaseHelper();
 			databaseHelper = null;
 		}
 	}
 	
 	/**
 	 * You'll need this in your class to get the helper from the manager once per class.
 	 */
 	private DatabaseHelper getHelper() {
 		if (databaseHelper == null) {
 			databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
 		}
 		return databaseHelper;
 	}
 }
