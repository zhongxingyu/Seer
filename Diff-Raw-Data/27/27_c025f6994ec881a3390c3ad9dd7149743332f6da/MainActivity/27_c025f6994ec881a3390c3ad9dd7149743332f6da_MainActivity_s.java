 package com.annotations.client;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import models.Video;
 
 import org.bson.types.MinKey;
 import org.bson.types.ObjectId;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import dialogs.NameDialog;
 import dialogs.TraitsDialog;
 import dialogs.TraitsDialog.OnTraitsDialogResult;
 
 import restclient.AnnotationsRESTClientUsage;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	public final static String PATH = "com.annotations.client.PATH";
 	public final static String NAME = "com.annotations.client.NAME";
 	public final static String ID = "com.annotations.client.ID";
 	private static ProgressDialog progressDialog;
 
 
 	public AnnotationsRESTClientUsage client = new AnnotationsRESTClientUsage(this);
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		//Disable NetworkOnMainThreadException
 		//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 		//StrictMode.setThreadPolicy(policy);
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			/*
 			 * Mode connect
 			 */
 			
			listViewInit();
 		} else {
 			/*
 			 * Mode hors connexion
 			 */
 			Toast.makeText(this, "This apps requires an internet connection.", Toast.LENGTH_LONG).show();
 		}
 
 
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(requestCode == 1) {
 			if(resultCode == RESULT_OK) {
 
 				Uri contactUri = data.getData();
 				String path = getRealPathFromURI(contactUri);
 				String name = getRealNameFromURI(contactUri);
 
 				Video video = new Video(name, path);
 				video.setStream(new File(path));
 
 				NameDialog nameDialog = new NameDialog();
 				nameDialog.setVideo(video);
 				nameDialog.show(getFragmentManager(), "salut");
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.nouvelle_video:
 			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 			intent.setType("video/*");
 			startActivityForResult(intent, 1);
 			return true;
 		case R.id.action_settings:
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	//http://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore
 	public String getRealPathFromURI(Uri contentUri) {
 		String[] proj = { MediaStore.Images.Media.DATA };    
 		Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
 		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 		cursor.moveToFirst();
 		return cursor.getString(column_index);
 	}
 
 	public String getRealNameFromURI(Uri contentUri) {
 		String[] proj = { MediaStore.Images.Media.DISPLAY_NAME };
 		Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
 		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
 		cursor.moveToFirst();
 		return cursor.getString(column_index);
 	}
 
 	public void listViewInit() {
 
 		progressDialog = ProgressDialog.show(MainActivity.this, "", "Loading videos...", true);
 		progressDialog.setCancelable(false);  
 
 		final ListView listView = (ListView)findViewById(R.id.list_main_activity);
 		@SuppressWarnings("unused")
 		String result = client.listVideo();
 		JSONArray array = client.getJsonArray();
 		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
 		HashMap<String, String> map;
 
 		for(int i = 0; i < array.length(); i++) {
 			try {
 				JSONObject obj = array.getJSONObject(i);
 				map = new HashMap<String, String>();
 				map.put("img", ""+i);
 				map.put("nom", obj.getString("nom"));
 				JSONObject vId = (JSONObject) obj.get("id");
 				ObjectId videoId = new ObjectId(Integer.parseInt(vId.getString("timeSecond")), Integer.parseInt(vId.getString("machine")), Integer.parseInt(vId.getString("inc")));
 				map.put("id", videoId.toString());
 				map.put("path", obj.getString("path"));
 				listItem.add(map);	      
 				progressDialog.dismiss(); 
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 
 
 		SimpleAdapter mSchedule = new SimpleAdapter (this.getBaseContext(), listItem, R.layout.video_item,
 				new String[] {"img", "nom"}, new int[] {R.id.img_list_video, R.id.nom_list_video});
 		listView.setAdapter(mSchedule);
 
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			@SuppressWarnings("unchecked")
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int position, long arg3) {
 				HashMap<String, String> map = (HashMap<String, String>) listView.getItemAtPosition(position);
 				Intent i = new Intent(MainActivity.this, AnnotationActivityStreaming.class);
 				i.putExtra(MainActivity.ID, map.get("id"));
 				i.putExtra(MainActivity.PATH, map.get("path"));
 				i.putExtra(MainActivity.NAME, map.get("nom"));
 				startActivity(i);
 			}
 		});
 	}
 
 
 
 }
