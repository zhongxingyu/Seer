 package edu.stanford.junction.sample.partyware;
 
 import android.app.Activity;
 import android.app.NotificationManager;
 import android.app.Service;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.ServiceConnection;
 import android.content.Intent;
 import android.content.ComponentName;
 import android.net.Uri;
 import android.os.IBinder;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ArrayAdapter;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView;
 
 import edu.stanford.junction.extra.JSONObjWrapper;
 import edu.stanford.junction.props2.Prop;
 import edu.stanford.junction.props2.IPropChangeListener;
 
 import org.json.JSONObject;
 
 import java.net.URI;
 import java.util.*;
 import java.text.DateFormat;
 
 
 public class YoutubePlaylistActivity extends RichListActivity implements OnItemClickListener{
 
     private Handler mMainHandler;
     private ArrayAdapter<JSONObject> mVideos;
 
 	public final static int REQUEST_CODE_ADD_VIDEO = 0;
 
 	private final DateFormat dateFormat = DateFormat.getDateTimeInstance();
 
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.youtube);
 
 		mVideos = new ArrayAdapter<JSONObject>(
 			this, 
 			android.R.layout.simple_list_item_1,
 			new ArrayList<JSONObject>());
 		setListAdapter(mVideos);
 		getListView().setTextFilterEnabled(true);
 		getListView().setOnItemClickListener(this); 
 
 		Button button = (Button)findViewById(R.id.add_video_button);
 		button.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					addVideo();
 				}
 			});
 
		final Handler refreshHandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					refresh();
				}
			};

 		try{
 			Prop prop = JunctionService.getProp();
 			prop.addChangeListener(new IPropChangeListener(){
 					public String getType(){ return Prop.EVT_CHANGE; }
 					public void onChange(Object data){
						refreshHandler.sendEmptyMessage(0);
 					}
 				});
 
 			prop.addChangeListener(new IPropChangeListener(){
 					public String getType(){ return Prop.EVT_SYNC; }
 					public void onChange(Object data){
						refreshHandler.sendEmptyMessage(0);
 					}
 				});
 		}
 		catch(IllegalStateException e){
 			toastShort("Failed to get info from service! See debug log.");
 			e.printStackTrace(System.err);
 		}
 
 		refresh();
 	}
 
 	protected void addVideo(){
 		Intent intent = new Intent(AddYoutubeVideoActivity.LAUNCH_INTENT);
 		startActivityForResult(intent, REQUEST_CODE_ADD_VIDEO);
 	}
 
 	private JSONObject randomTestObj(){
 		try{
 			JSONObject o = new JSONObject();
 			o.put("name", "dudeface");
 			o.put("time", (new Date()).getTime());
 			return o;
 		}
 		catch(Exception e){
 			return new JSONObject();
 		}
 	}
 
 	public void onItemClick(AdapterView parent, View v, int position, long id){
 		// Intent intent = new Intent();
 		// JSONObject advert = (JSONObject)mVideos.getItem(position);
 		// String url = advert.optString("url");
 		// intent.putExtra(WhiteboardIntents.EXTRA_SESSION_URL, url);
 		// setResult(RESULT_OK, intent);
 		// finish();
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		switch(requestCode) {
 		case REQUEST_CODE_ADD_VIDEO:
 			if(resultCode == RESULT_OK){
 				String caption = intent.getStringExtra(AddYoutubeVideoActivity.EXTRA_CAPTION);
 				String videoId = intent.getStringExtra(AddYoutubeVideoActivity.EXTRA_VIDEO_ID);
 				try{
 					PartyProp prop = JunctionService.getProp();
 					String userId = JunctionService.getUserId();
 					long time = (new Date()).getTime();
 					prop.addYoutube(userId, videoId, caption, time);
 				}
 				catch(IllegalStateException e){
 					toastShort("Failed to get info from service! See debug log.");
 					e.printStackTrace(System.err);
 				}
 				
 			}
 			break;
 		}
 	}
 
 	private void refresh(){
 		try{
 			PartyProp prop = JunctionService.getProp();
 			List<JSONObject> videos = prop.getYoutubeVids();
 			refreshVideos(videos);
 		}
 		catch(IllegalStateException e){
 			toastShort("Failed to get info from service! See debug log.");
 			e.printStackTrace(System.err);
 		}
 	}
 
 	private void refreshVideos(List<JSONObject> videos){
 		mVideos.setNotifyOnChange(false);
 		mVideos.clear();
 		for(JSONObject a : videos){
 			JSONObject video = new JSONObjWrapper(a){
 					public String toString(){
 						return optString("caption"); 
 					}
 				};
 			mVideos.add(video);
 		}
 		mVideos.setNotifyOnChange(true);
 		mVideos.notifyDataSetChanged();
 	}
 
 
 	public void onDestroy(){
 		super.onDestroy();
 	}
 
 }
 
 
 
