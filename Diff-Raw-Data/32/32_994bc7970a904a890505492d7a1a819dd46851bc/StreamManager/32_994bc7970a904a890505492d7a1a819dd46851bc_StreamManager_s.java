 package org.csie.mpp.buku.view;
 
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.csie.mpp.buku.App;
 import org.csie.mpp.buku.R;
 import org.csie.mpp.buku.Util;
 import org.csie.mpp.buku.db.DBHelper;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.facebook.android.BaseRequestListener;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
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
 
 public class StreamManager extends ViewManager {
 	private List<Stream> streams;
 	private ArrayAdapter<Stream> adapter;
 	
 	public StreamManager(Activity activity, DBHelper helper) {
 		super(activity, helper);
 	}
 	
 	private static class Stream {
 		private String id;
 		private String message;
 		private String book;
 		private String link;
 		private Bitmap pic;
 		private Date time;
 		
 		public Stream(String id) {
 			this.id = id;
 		}
 		public void setDate(String dateString){
 			dateString = dateString.replace("T", " ");
 			dateString = dateString.replace("+0000", "");
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			try {
 				time = sdf.parse(dateString);
 			}
 			catch(Exception e){
 				Log.e(App.TAG, e.toString());
 			}
 		}
 	}
 
 	private void createView(LinearLayout frame) {
 		if(streams.size() == 0) {
 			TextView text = (TextView)frame.findViewById(R.id.text);
 			text.setText("You have no streams. QQ"); // TODO: change to strings.xml
 		}
 		else {
 			TextView text = (TextView)frame.findViewById(R.id.text);
 			text.setText("");
 			
 			adapter = new ArrayAdapter<Stream>(activity, R.layout.list_item_stream, streams) {
 				@Override
 				public View getView(int position, View convertView, ViewGroup parent) {
 					Stream stream = streams.get(position);
 					LayoutInflater inflater = activity.getLayoutInflater();
 					View view = inflater.inflate(R.layout.list_item_stream, null);
 					if(stream.message != null)
 						((TextView)view.findViewById(R.id.list_message)).setText(stream.message);
 					if(stream.pic != null)
 						((ImageView)view.findViewById(R.id.list_image)).setImageBitmap(stream.pic);
					if(stream.book != null)
						((TextView)view.findViewById(R.id.list_name)).setText(stream.book);
 					((TextView)view.findViewById(R.id.list_time)).setText(stream.time.toLocaleString());
 					return view;
 				}
 			};
 			
 			ListView list = (ListView)frame.findViewById(R.id.list);
 			list.setAdapter(adapter);
 		}
 	}
 
 	@Override
 	protected void updateView() {
 		final LinearLayout frame = getFrame();
 		
 		if(streams != null)
 			createView(frame);
 		else {
 			Bundle params = new Bundle();
			params.putString("fields", "id,message,name,picture,link,application,created_time");
 			
 			// TODO: change to AsyncTask
 			App.fb_runner.request("me/feed", params, new BaseRequestListener() {
 				@Override
 				public void onComplete(String response, Object state) {
 					streams = new ArrayList<Stream>();
 					
 					try {
 						JSONObject json = new JSONObject(response);
 						JSONArray data = json.getJSONArray("data");
 						for(int i = 0; i < data.length(); i++) {
 							try {
 								JSONObject item = data.getJSONObject(i);
 								if(item.has("application")) {
 									if(item.get("application") == JSONObject.NULL)
 										continue;
 									if(!item.getJSONObject("application").getString("id").equals(App.FB_APP_ID))
 										continue;
 									Stream stream = new Stream(item.getString("id"));
									if(item.has("name"))
										stream.book = item.getString("name");
 									if(item.has("message"))
 										stream.message = item.getString("message");
									stream.pic = Util.urlToImage(new URL(item.getString("picture")));
									stream.link = item.getString("link");
 									stream.setDate(item.getString("created_time"));
 									streams.add(stream);
 								}
 							}
 							catch(Exception e) {
 								Log.e(App.TAG, e.toString());
 							}
 						}
 						// TODO: parse next page
 					}
 					catch(Exception e) {
 						Log.e(App.TAG, e.toString());
 					}
 					
 					activity.runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							createView(frame);
 						}
 					});
 				}
 			});
 		}
 	}
 }
