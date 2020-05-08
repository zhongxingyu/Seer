 package com.fennyfatal.atoruploader;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class DirectoryPicker extends ListActivity{
 	public static final String START_DIR = "startDir";
 	public static final String DIR_LIST = "dirList";
 	private String curPath = ""; 
 	private String server = "";
 	private Boolean working = false;
 	private ArrayList<String> dirs;
 	public static final String CHOSEN_DIRECTORY = "chosenDir";
 	public static final int PICK_DIRECTORY = 2432;
 	
 	//TODO: Add instance for picking the default download directory. Users have a tendency to fail at typing their paths.
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_directory_picker);
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		String rawServer = prefs.getString("server_url", "fail");
 		//Get rid of trailing slashes. TODO: Factor this out into a function.
        server = TorrentUploader.removeTrailingSlash(rawServer);
         String rawCurPath = prefs.getString("server_path", "fail");
         //Because windows users are all noobs!!!
         rawCurPath = rawCurPath.replace('\\', '/');
         curPath = TorrentUploader.removeTrailingSlash(rawCurPath);
 		setTitle(curPath);
 		Button btnChoose = (Button) findViewById(R.id.btnChoose);
 		btnChoose.setText("Choose " + "'" + curPath.substring(curPath.lastIndexOf('/')+1).replace("\"", "") + "'");
 		btnChoose.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				returnDir(curPath);				
 			}
 		});
 		ListView lv = this.getListView();
         lv.setTextFilterEnabled(true);
         getDirs(server, curPath);
         lv.setOnItemClickListener(new OnItemClickListener() {
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         		//callback.pickString(dirs.get(position));
         		//returnDir(dirs.get(position));
         		switch (position)
         		{
         		case 0:
         		return;
         		case 1:
         			curPath = curPath.substring(0,curPath.lastIndexOf('/'));
         		break;
         		default:
         			curPath = curPath + '/' + dirs.get(position);
         		}
         		getDirs(server,curPath);
         	}
         });
 	}
 
 	private void returnDir(String path) {
     	Intent result = new Intent();
     	result.putExtra(CHOSEN_DIRECTORY, path);
     	result.setData(getIntent().getData());
         setResult(RESULT_OK, result);
         //if (path.equalsIgnoreCase("."))
         	finish();
     }
 	@SuppressWarnings("deprecation")
 	private ArrayList<String> getDirs(String Domain, String Path)
 	{
 		//Check if we are already waiting for a dir listing.
 		//TODO: add some sort of visual indication that we are working...
 		if (working)
 			return null;
 		working = true;
 		AsyncHttpClient client = new AsyncHttpClient();
 		//Ask for directory listing in JSON. Doesn't matter THAT much if the directory the user typed in is wrong, the WebUI will give us the default.
 		client.get(Domain + "/plugins/_getdir/info.php?mode=dirlist;&basedir=" + URLEncoder.encode(Path), new AsyncHttpResponseHandler() {
 		    @Override
 		    public void onSuccess(String response) {
 		    	try{
 		    		//YAY we got a response from the server, let's try and process it.
 			    	ArrayList<String> Mine = new ArrayList<String>();
 					JSONObject obj = new JSONObject(response);
 					curPath = obj.getString("basedir");
 					setTitle(curPath);
 					Button btnChoose = (Button) findViewById(R.id.btnChoose);
 					btnChoose.setText("Choose " + "'" + curPath.substring(curPath.lastIndexOf('/')+1).replace("\"", "") + "'");
 					JSONArray array = obj.getJSONArray("dirlist");
 					for (int i = 0; i < array.length();i++)
 						{
 							Mine.add(array.getString(i));
 						}
 					dirs = Mine;
 					setListAdapter(new ArrayAdapter<String>(DirectoryPicker.this, R.layout.list_item, dirs));
 			    	} 
 		    		catch (JSONException e) {
 		    		e.printStackTrace();
 		    	}
 		    	finally {} //TODO add failure messages here.
 		    	//Okay, we can let the user pick more directories.
 		    	working = false;
 		    }
 		});
 		return null;
 	 }
 }
