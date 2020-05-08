 package net.aborigeno.android.json;
 
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import net.aborigeno.android.json.dupal.App;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonSyntaxException;
 
 public class JSONNodesActivity extends Activity {
 	/** Called when the activity is first created. */
 
 	private static final String publicUrl = "Your public dir  url";
 	private static final String TAG = "Node";
 
 	AppService mService;
 	TextView tv;
 	Button mVai;
 	Activity activity;
 	App n;
 	Boolean mBound = false;
 
 	/** Defines callbacks for service binding, passed to bindService() */
 	private ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// We've bound to LocalService, cast the IBinder and get
 			// LocalService instance
 			AppService.AppBinder binder = (AppService.AppBinder) service;
 			mService = binder.getService();
 			mBound = true;
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
 			mBound = false;
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		activity = this;
 		setContentView(R.layout.main);
 		ExpandableListView tl = (ExpandableListView) findViewById(R.id.extList);
 
 		// tl.setAdapter(new TabAdapter(this));
 
 		tl.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 				Toast.makeText(JSONNodesActivity.this, "" + position,
 						Toast.LENGTH_SHORT).show();
 			}
 		});
 		tv = (TextView) findViewById(R.id.testo);
 		mVai = (Button) findViewById(R.id.vai);
 
 		mVai.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
				
 				vai();
 				// openGrid();
 			}
 		});
 
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		// Bind to LocalService
 		Intent intent = new Intent(this, AppService.class);
 		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		// Unbind from the service
 		if (mBound) {
 			unbindService(mConnection);
 			mBound = false;
 		}
 	}
 
 	public static String convertStreamToString(InputStream is) throws Exception {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		StringBuilder sb = new StringBuilder();
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 			sb.append(line + "\n");
 		}
 		is.close();
 		return sb.toString();
 	}
 
 	public static InputStream getInputStreamFromUrl(String url) {
 		InputStream content = null;
 		try {
 			HttpClient httpclient = new DefaultHttpClient();
 
 			HttpResponse response = httpclient.execute(new HttpGet(url));
 			content = response.getEntity().getContent();
 		} catch (Exception e) {
 			Log.d("[GET REQUEST]", "Network exception " + e.getMessage());
 			e.printStackTrace();
 		}
 		return content;
 	}
 
 	private void vai() {
 		 if (mBound) {
 	            // Call a method from the LocalService.
 	            // However, if this call were something that might hang, then this request should
 	            // occur in a separate thread to avoid slowing down the activity performance.
 	            App a = mService.getApp(1);
 	            if (a!=null)
 	            Toast.makeText(this, "Title: " + a.getTitle(), Toast.LENGTH_SHORT).show();
 	        }
 	}
 
 	private void vaiold() {
 		Gson gson = new Gson();
 		// JsonParser parser = new JsonParser();
 
 		try {
 			final String jsonString = convertStreamToString(getInputStreamFromUrl("url"));
 			n = gson.fromJson(jsonString, App.class);
 
 			Log.d(TAG, n.getField_version().getUnd()[0].getValue());
 			Log.d(TAG, n.getField_apk().getUnd().getUri());
 			// JsonObject jo = parser.parse(jsonString).getAsJsonObject();
 
 			// LocalizedField f = gson.fromJson(getFieldArray(jo,
 			// "field_version"), LocalizedField.class);
 			// FileField ff = gson.fromJson(getFieldArray(jo, "field_apk"),
 			// FileField.class);
 			// FileField fi = gson.fromJson(getFieldArray(jo, "field_image"),
 			// FileField.class);
 			//
 			// n.setField_version(f);
 			// n.setField_apk(ff);
 			// n.setField_image(fi);
 			// //tv.setText(n.getTitle());
 		} catch (JsonSyntaxException je) {
 			je.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private JsonElement getFieldArray(JsonObject jo, String fieldName) {
 		return jo.get(fieldName).getAsJsonObject().get("und").getAsJsonArray()
 				.get(0);
 	}
 
 	public class TabAdapter extends BaseExpandableListAdapter {
 		private Context mContext;
 
 		public TabAdapter(Context c) {
 			mContext = c;
 		}
 
 		public int getCount() {
 			return 1;
 		}
 
 		public Object getItem(int position) {
 			return null;
 		}
 
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		// create a new ImageView for each item referenced by the Adapter
 		public View getView(int position, View convertView, ViewGroup parent) {
 			AppView mAppView;
 			// String geturl = n.getField_image().getFullUri(publicUrl);
 			// Bitmap bm =
 			// BitmapFactory.decodeStream(getInputStreamFromUrl(geturl));
 			if (convertView == null) { // if it's not recycled, initialize some
 										// attributes
 				// tabrowView = (LinearLayout) findViewById(R.layout.approw);
 				mAppView = new AppView(mContext, n, publicUrl);
 			} else {
 				mAppView = (AppView) convertView;
 			}
 
 			return mAppView;
 		}
 
 		@Override
 		public Object getChild(int groupPosition, int childPosition) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public long getChildId(int groupPosition, int childPosition) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getChildView(int groupPosition, int childPosition,
 				boolean isLastChild, View convertView, ViewGroup parent) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public int getChildrenCount(int groupPosition) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public Object getGroup(int groupPosition) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public int getGroupCount() {
 			// TODO Auto-generated method stub
 			return 1;
 		}
 
 		@Override
 		public long getGroupId(int groupPosition) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getGroupView(int groupPosition, boolean isExpanded,
 				View convertView, ViewGroup parent) {
 			// TODO Auto-generated method stub
 			return getView(groupPosition, convertView, parent);
 		}
 
 		@Override
 		public boolean hasStableIds() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isChildSelectable(int groupPosition, int childPosition) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 	}
 
 }
