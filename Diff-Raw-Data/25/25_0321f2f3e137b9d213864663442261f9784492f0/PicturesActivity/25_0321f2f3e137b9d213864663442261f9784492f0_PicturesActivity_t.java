 package edu.stanford.junction.sample.partyware;
 
 import edu.stanford.junction.sample.partyware.util.BitmapManager;
 
 import android.app.Activity;
 import android.app.NotificationManager;
 import android.app.Service;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.ServiceConnection;
 import android.content.Intent;
 import android.content.ComponentName;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.IBinder;
 import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.LayoutInflater;
 import android.widget.Button;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.BaseAdapter;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView;
 
 import edu.stanford.junction.extra.JSONObjWrapper;
 import edu.stanford.junction.props2.Prop;
 import edu.stanford.junction.props2.IPropChangeListener;
 
 import org.json.JSONObject;
 
 import java.net.*;
 import java.io.*;
 import java.util.*;
 import java.text.DateFormat;
 
 
 
 public class PicturesActivity extends RichListActivity implements OnItemClickListener{
 
 	public final static int REQUEST_CODE_ADD_PIC = 0;
 	private ImageAdapter mPics;
 	private final DateFormat dateFormat = DateFormat.getTimeInstance();
 
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.pictures);
 
 		mPics = new ImageAdapter(this, 
 								 R.layout.picture_item, 
 								 new ArrayList<JSONObject>());
 		setListAdapter(mPics);
 		getListView().setTextFilterEnabled(true);
 		getListView().setOnItemClickListener(this); 
 
 		Button button = (Button)findViewById(R.id.add_picture_button);
 		button.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					addPic();
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
 
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
 		JSONObject o = mPics.getItem(position);
 		Intent intent = new Intent();
 		intent.setAction(ImageViewerActivity.LAUNCH_INTENT);
 		String url = o.optString("url");
 		intent.putExtra("image_url", url);
 		startActivity(intent);
 	}
 
 	protected void addPic(){
 		Intent intent = new Intent(AddPictureActivity.LAUNCH_INTENT);
 		startActivityForResult(intent, REQUEST_CODE_ADD_PIC);
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		switch(requestCode) {
 		case REQUEST_CODE_ADD_PIC:
 			if(resultCode == RESULT_OK){
 				String url = intent.getStringExtra(AddPictureActivity.EXTRA_URL);
 				String thumbUrl = intent.getStringExtra(AddPictureActivity.EXTRA_THUMB_URL);
 				String caption = intent.getStringExtra(AddPictureActivity.EXTRA_CAPTION);
 				try{
 					PartyProp prop = JunctionService.getProp();
 					String userId = JunctionService.getUserId();
 					long time = (new Date()).getTime();
 					prop.addImage(userId, url, thumbUrl, caption, time);
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
			toastShort("Refreshing....");
 			PartyProp prop = JunctionService.getProp();
 			List<JSONObject> images = prop.getImages();
 			refreshImages(images);
 		}
 		catch(IllegalStateException e){
 			toastShort("Failed to get info from service! See debug log.");
 			e.printStackTrace(System.err);
 		}
 	}
 
 	private void refreshImages(List<JSONObject> images){
 		mPics.clear();
 		for(JSONObject a : images){
 			mPics.add(a);
 		}
 	}
 
 
 	public void onDestroy(){
 		super.onDestroy();
 	}
 
 	class ImageAdapter extends ArrayAdapter<JSONObject> {
 
 		BitmapManager mgr = new BitmapManager(10);
 
 		public ImageAdapter(Context context, int resource, List<JSONObject> objects){
 			super(context, resource, objects);
 		}
 
 		/** 
 		 *  Returns a new ImageView to be displayed, depending on 
 		 *	the position passed. 
 		 */
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater)getSystemService(
 					Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.picture_item, null);
 			}
 			JSONObject o = getItem(position);
 			if (o != null) {
 				TextView tt = (TextView) v.findViewById(R.id.toptext);
 				String caption = o.optString("caption");
 				tt.setText(caption);
 
 				Date d = new Date(o.optLong("time"));
 				String time = dateFormat.format(d); 
 				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
 				bt.setText(" " + time);
 
 				ImageView icon = (ImageView)v.findViewById(R.id.icon);
 				String url = o.optString("thumbUrl");
 				Bitmap bm = mgr.getBitmap(url);
 				if(bm == null){
 					icon.setImageResource(R.drawable.shopper_icon);
 				}
 				else{
 					icon.setImageBitmap(bm);
 				}
 				icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
 			}
 			return v;
 
 		}
 
 
 	}
 
 
 }
 
 
 
