 package org.csie.mpp.buku.view;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.csie.mpp.buku.App;
 import org.csie.mpp.buku.R;
 import org.csie.mpp.buku.Util;
 import org.csie.mpp.buku.db.DBHelper;
 import org.csie.mpp.buku.db.FriendEntry;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class FriendsManager extends ViewManager {
 	public static interface OnListLoadListener {
 		public void onListLoaded(List<FriendEntry> entries);
 	}
 	
 	private List<FriendEntry> friends;
 	private FriendEntryAdapter adapter;
 	
 	private Updater updater;
 
 	public static class FriendEntryAdapter extends ArrayAdapter<FriendEntry> {
 		private LayoutInflater inflater;
 		private int resourceId;
 		private List<FriendEntry> entries;
 		
 		public FriendEntryAdapter(Activity activity, int resource, List<FriendEntry> list) {
 			super(activity, resource, list);
 			
 			inflater = activity.getLayoutInflater();
 			resourceId = resource;
 			entries = list;
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			FriendEntry friend = entries.get(position);
 			View view = inflater.inflate(resourceId, parent, false);
 			((ImageView)view.findViewById(R.id.list_image)).setImageBitmap(friend.icon);
 			((TextView)view.findViewById(R.id.list_name)).setText(friend.name);
 			return view;
 		}
 	}
 	
 	protected class Updater extends AsyncTask<Integer, Integer, Boolean> {
 		private TextView info;
 		
 		public Updater() {
 			info = (TextView)getFrame().findViewById(R.id.text);
 			info.setBackgroundColor(0xaaffffff);
 			info.setText(R.string.msg_updating);
 		}
 		
 		@Override
 		protected Boolean doInBackground(Integer... args) {
 			Bundle params = new Bundle();
 			params.putString("q", "SELECT uid,first_name,name,is_app_user FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) AND is_app_user = 1");
 			
 			try {
 				String response = App.fb.request("fql", params);
 				
 				int counter = 0;
 				while(response != null) {
 					JSONObject json = new JSONObject(response);
 					JSONArray data = json.getJSONArray("data");
 					for(int i = 0; i < data.length(); i++) {
 						publishProgress(++counter);
 						
 						JSONObject item = data.getJSONObject(i);		
 						if (item != JSONObject.NULL) {
 							String id = item.getString("uid");
 							if(FriendEntry.exists(rdb, id))
 								continue;
 							
 							FriendEntry friend = new FriendEntry();
 							friend.id = id;
 							friend.name = item.getString("name");
 							friend.firstname = item.getString("first_name");
 							friend.icon = Util.urlToImage(new URL("http://graph.facebook.com/" + friend.id + "/picture"));
 							friend.insert(rdb);
 							
 							friends.add(friend);
 						}
 					}
 					response = null;
 					
 					if(json.has("paging")) {
 						JSONObject paging = json.getJSONObject("paging");
 						if(paging.has("next")) {
 							URL url = new URL(paging.getString("next"));
 							response = Util.urlToString(url);
 						}
 					}
 				}
 			}
 			catch(Exception e) {
 				Log.e(App.TAG, e.toString());
 				return false;
 			}
 			
 			return true;
 		}
 		
 		@Override
 		protected void onProgressUpdate(Integer... progresses) {
 			adapter.notifyDataSetChanged();
 		}
 		
 		@Override
 		protected void onPostExecute(Boolean result) {
 			info.setBackgroundColor(0x00ffffff);
 			info.setText("");
 			
 			if(result) {
 				if(friends.size() == 0)
 					createNoFriendView();
 				else
 					adapter.notifyDataSetChanged();
 			}
 			
 			listener.onListLoaded(friends);
 		}
 	}
 	
 	public FriendsManager(Activity activity, DBHelper helper) {
 		super(activity, helper);
 		
 		friends = new ArrayList<FriendEntry>();
 		adapter = new FriendEntryAdapter(activity, R.layout.list_item_friend, friends);
 	}
 	
 	private OnListLoadListener listener;
 	
 	public FriendsManager(Activity activity, DBHelper helper, OnListLoadListener callback) {
 		this(activity, helper);
 		
 		listener = callback;
 	}
 	
 	public void update() {
 		updater.execute(0);
 	}
 
 	private void updateFriendList() {
 		friends.clear();
 		
 		FriendEntry[] entries = FriendEntry.queryAll(rdb);
 		for(FriendEntry entry: entries)
 			friends.add(entry);
 		
 		adapter.notifyDataSetChanged();
 	}
 	
 	@Override
 	protected void updateView() {
 		if(FriendEntry.count(rdb) == 0)
 			createNoFriendView();
 		else {
 			createFriendView();
 			updateFriendList();
 		}
 		
 		if(updater == null) {
 			updater = new Updater();
 			update();
 		}
 	}
 	
 	private void createFriendView() {
 		LinearLayout frame = getFrame();
 		
 		TextView text = (TextView)frame.findViewById(R.id.text);
 		text.setText("");
 		
 		ListView list = (ListView)frame.findViewById(R.id.list);
 		list.setAdapter(adapter);
 	}
 
 	private void createNoFriendView() {
 		LinearLayout frame = getFrame();
 		
 		TextView text = (TextView)frame.findViewById(R.id.text);
		text.setText("You have no friends. QQ"); // TODO: change to strings.xml
 	}
 }
