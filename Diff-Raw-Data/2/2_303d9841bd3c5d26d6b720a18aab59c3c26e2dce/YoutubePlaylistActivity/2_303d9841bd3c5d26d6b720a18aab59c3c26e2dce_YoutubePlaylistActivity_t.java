 package edu.stanford.junction.sample.partyware;
 
 import android.content.Context;
 import android.content.pm.ResolveInfo;
 import android.content.pm.PackageManager;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Bundle;
 import android.view.View;
 import android.view.LayoutInflater;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView;
 import edu.stanford.junction.props2.Prop;
 import edu.stanford.junction.props2.IPropChangeListener;
 
 import org.json.JSONObject;
 import java.util.*;
 
 
 public class YoutubePlaylistActivity extends RichListActivity implements OnItemClickListener{
 
     private VidAdapter mVids;
 	private IPropChangeListener mPropListener;
 
 
 	public final static int REQUEST_CODE_ADD_VIDEO = 0;
 
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.youtube);
 
 		mVids = new VidAdapter(this);
 		setListAdapter(mVids);
 		getListView().setTextFilterEnabled(true);
 		getListView().setOnItemClickListener(this); 
 
 		Button button = (Button)findViewById(R.id.add_video_button);
 		button.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					addVideo();
 				}
 			});
 
 		listenForAnyPropChange();
 		refresh();
 	}
 
 	protected void onAnyPropChange(){
 		refresh();
 	}
 
 	protected void addVideo(){
 		Intent intent = new Intent(YoutubeSearchActivity.LAUNCH_INTENT);
 		startActivityForResult(intent, REQUEST_CODE_ADD_VIDEO);
 	}
 
 
 	public void onItemClick(AdapterView<?> parent, View v, int position, long id){
 		JSONObject o = mVids.getItem(position);
 		String videoId = o.optString("videoId");
 		Intent i =  new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:"  + videoId));
 		List<ResolveInfo> list = getPackageManager().queryIntentActivities(
 			i, PackageManager.MATCH_DEFAULT_ONLY);
 		if(list.size() > 0) {
 			startActivity(i);
 		} 
 		else{
 			toastShort("Youtube player not available!");
 		}
 	}
 
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		switch(requestCode) {
 		case REQUEST_CODE_ADD_VIDEO:
 			if(resultCode == RESULT_OK){
 				String caption = intent.getStringExtra("title");
 				String videoId = intent.getStringExtra("video_id");
 				String thumbUrl = intent.getStringExtra("thumb_url");
 
 				JunctionApp app = (JunctionApp)getApplication();
 				PartyProp prop = app.getProp();
 				String userId = app.getUserId();
 				long time = (long)(System.currentTimeMillis()/1000.0);
 				prop.addYoutube(userId, videoId, thumbUrl, caption, time);
 
 			}
 			break;
 		}
 	}
 
 	private void refresh(){
 		JunctionApp app = (JunctionApp)getApplication();
 		PartyProp prop = app.getProp();
 		List<JSONObject> videos = prop.getYoutubeVids();
 		refreshVideos(videos);
 	}
 
 	private void refreshVideos(List<JSONObject> videos){
 		mVids.clear();
 		for(JSONObject a : videos){
 			mVids.add(a);
 		}
 	}
 
 
 	public void onDestroy(){
 		super.onDestroy();
 		JunctionApp app = (JunctionApp)getApplication();
 		Prop prop = app.getProp();
 		prop.removeChangeListener(mPropListener);
 		mVids.clear();
 		mVids.recycle();
 	}
 
 	class VidAdapter extends MediaListAdapter<JSONObject> {
 
 		public VidAdapter(Context context){
 			super(context, R.layout.youtube_item);
 		}
 
 		class UpVoteListener implements OnClickListener{
 			public String videoId;
 			public UpVoteListener(String vidId){
 				videoId = vidId;
 			}
 			public void onClick(View v) {
 				JunctionApp app = (JunctionApp)getApplication();
 				app.upvoteVideo(videoId);
 			}
 		}
 
 		class DownVoteListener implements OnClickListener{
 			public String videoId;
 			public DownVoteListener(String vidId){
 				videoId = vidId;
 			}
 			public void onClick(View v) {
 				JunctionApp app = (JunctionApp)getApplication();
 				app.downvoteVideo(videoId);
 			}
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater)(
 					getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
 				v = vi.inflate(R.layout.youtube_item, null);
 			}
 			JSONObject o = getItem(position);
 			if (o != null) {
 				TextView tt = (TextView) v.findViewById(R.id.toptext);
 				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
 
 				if(position == 0){
					tt.setText("Next to play");
 					tt.setTextColor(0xffffdd00);
 					v.setBackgroundColor(0xff222222);
 				}
 				else{
 					tt.setText(o.optString("caption"));
 					tt.setTextColor(0xffffffff);
 					v.setBackgroundColor(0xff000000);
 				}
 
 				bt.setText("Votes: " + o.optInt("votes"));
 
 				final ImageView icon = (ImageView)v.findViewById(R.id.icon);
 				final String url = o.optString("thumbUrl");
 				loadImage(icon, url);
 
 				String id = o.optString("id");
 
 				Button upvote = (Button) v.findViewById(R.id.upvote_button);
 				Button downvote = (Button) v.findViewById(R.id.downvote_button);
 
 				JunctionApp app = (JunctionApp)getApplication();
 				if(app.alreadyVotedFor(id)){
 					upvote.setVisibility(View.GONE);
 					downvote.setVisibility(View.GONE);
 				}
 				else{
 					upvote.setVisibility(View.VISIBLE);
 					downvote.setVisibility(View.VISIBLE);
 					upvote.setOnClickListener(new UpVoteListener(id));
 					downvote.setOnClickListener(new DownVoteListener(id));
 				}
 			}
 			return v;
 		}
 	}
 
 }
 
 
 
