 package nl.oxanvanleeuwen.android.livetv.activities;
 
 import java.util.ArrayList;
 import nl.oxanvanleeuwen.android.livetv.R;
 import nl.oxanvanleeuwen.android.livetv.service.Channel;
 import nl.oxanvanleeuwen.android.livetv.service.MediaStreamService;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class ChannelList extends Activity {
 	private static final String TAG = "ChannelListActivity";
 	
 	private MediaStreamService service;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	// init
         super.onCreate(savedInstanceState);
         setContentView(R.layout.channellist);
        setTitle(getResources().getText(R.string.availablechannellist));
         ListView lv = (ListView)findViewById(R.id.channellist);
         
         // set up service
         service = new MediaStreamService("http://mediastreamer.lan/MPWebStream/MediaStream.svc");
         
         // show all channels
         try {
             Log.v(TAG, "Started loading channels");
             ArrayList<String> channels = new ArrayList<String>();
 			for(Channel channel : service.getChannels()) {
 				channels.add(channel.name);
 			}
 			String[] strlist = new String[]{};
 			ArrayAdapter<String> items = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, channels.toArray(strlist));
 			lv.setAdapter(items);
 			Log.v(TAG, "Loaded channels");
 		} catch (Exception e) {
 			Log.e(TAG, "Failed to show channel list", e);
 			Toast.makeText(ChannelList.this, getString(R.string.channelfailed), Toast.LENGTH_SHORT);
 		}
 		
 		// register callback
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Channel channel = service.getChannelsCached().get(position);
 				Intent intent = new Intent(ChannelList.this, ViewStream.class);
 				try {
 					intent.putExtra("url", service.getTvStreamUrl(service.getTranscoder(), channel, "admin", "admin"));
 				} catch (Exception e) {
 					Log.e(TAG, "Failed to start stream", e);
 					Toast.makeText(ChannelList.this, getString(R.string.streamfailed), Toast.LENGTH_SHORT);
 				}
 				startActivity(intent);
 			}
 		});
     }
 }
