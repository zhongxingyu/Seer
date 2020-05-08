 package navat.bgu.freeseat;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.text.format.Time;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class RoomsProvider {
 	ArrayList<Room> roomList;
 	DatabaseHelper db;
 	Context context;
 	MainActivity activity;
 	public static final String lastUpdatePrefKey = "LastDBUpdate";
	SharedPreferences settings;
 	
 	public RoomsProvider(MainActivity fromActivity, SharedPreferences ownerSettings) {
 		context = fromActivity;
 		activity = fromActivity;
 		settings = ownerSettings;
 		db = new DatabaseHelper(context);
 		loadFromDb();
 	}
 	public void loadFromDb() {
 		roomList = db.getAllRooms();
 		SharedPreferences.Editor editor = settings.edit();
	    editor.putLong(lastUpdatePrefKey, System.currentTimeMillis());
 	
 	    // Commit the edits!
 	    editor.commit();
 	}
 	public boolean isExpired() {
 		if (db.isRoomsTableEmpty()) {
 			return true;
 		}
		if (Math.abs(settings.getLong(lastUpdatePrefKey, System.currentTimeMillis()) - System.currentTimeMillis()) > 60 * 2) {
 			return true;
 		}
 		return false;
 	}
 	public void feedDatabase() {
 		new ParseSite().execute("http://gezer.bgu.ac.il/compclass/compclass.php");
 		//AsyncTask task = new ProgressTask(this.activity).execute();
 	}
 
 	private class ParseSite extends AsyncTask<String, Void, Boolean> {
 		private ProgressDialog pdia;
 
         protected Boolean doInBackground(String... arg) {
             try {
             	URL url = new URL(arg[0]);
         		//URLConnection urlConnection = url.openConnection();
         		//InputStream in = new BufferedInputStream(urlConnection.getInputStream());
             	RoomsHTMLParser parser = new RoomsHTMLParser(url);
         		
         		if (parser != null) {
     	        	parser.getRoomsStates();
     	            
     	            Iterator<Room> it = parser.roomsState.iterator();
     	            while (it.hasNext()) {
     	            	db.setRoom(it.next());
     	            }
             	} else {
             		// TODO error fetching url! Notify user and log!
             	}
             	loadFromDb();
             	activity.adapter = new ArrayAdapter<Room>(activity,
                         android.R.layout.simple_list_item_1, 
                         roomList);
             } catch(Exception e) {
                 e.printStackTrace();
             }
 
             return true;
         }
         
         @Override
         protected void onPreExecute(){ 
            super.onPreExecute();
                 pdia = new ProgressDialog(context);
                 pdia.setMessage("Loading...");
                 pdia.show();
                 db.onUpgrade(db.getWritableDatabase(), 1, 1);
         }
 
         protected void onPostExecute(Boolean result) {
         	activity.setListAdapter(activity.adapter);
         	activity.adapter.notifyDataSetChanged();
         	EditText filterText = (EditText) activity.findViewById(R.id.search_box);
         	activity.adapter.getFilter().filter(filterText.getText().toString());
         	pdia.dismiss();
         }
     }
 }
