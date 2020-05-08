 package com.tools.tvguide.activities;
 
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.mobeta.android.dslv.DragSortController;
 import com.mobeta.android.dslv.DragSortListView;
 import com.mobeta.android.dslv.DragSortListView.DragSortListener;
 import com.tools.tvguide.R;
 import com.tools.tvguide.components.DefaultNetDataGetter;
 import com.tools.tvguide.data.Channel;
 import com.tools.tvguide.managers.AppEngine;
 import com.tools.tvguide.managers.CollectManager;
 import com.tools.tvguide.managers.UrlManager;
 import com.tools.tvguide.utils.NetDataGetter;
 import com.tools.tvguide.utils.NetworkManager;
 import com.tools.tvguide.utils.Utility;
 import com.tools.tvguide.utils.XmlParser;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.SimpleAdapter.ViewBinder;
 import android.widget.Toast;
 
 public class CollectActivity extends Activity implements DragSortListener
 {
     private DragSortListView mChannelListView;
     private DragSortController mController;
     private MySimpleAdapter mListViewAdapter;
     private ArrayList<HashMap<String, Object>> mItemList;
     private List<Channel> mChannelList;
     private HashMap<String, HashMap<String, Object>> mXmlChannelInfo;
     private final String XML_ELEMENT_LOGO = "logo";
     private Handler mUpdateHandler;
     private List<Pair<String, String>> mOnPlayingProgramList;           // List of "id"-"title" pair
     private final int MSG_REFRESH_ON_PLAYING_PROGRAM_LIST   = 1;
     private ArrayList<String> mReportList;
     private LayoutInflater mInflater;
     private LinearLayout mContentLayout;
     private LinearLayout mNoCollectLayout;
     private LinearLayout.LayoutParams mCenterLayoutParams;
         
     private class MySimpleAdapter extends SimpleAdapter
     {
         public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) 
         {
             super(context, data, resource, from, to);
         }
         
         @Override
         public View getView(int position, View convertView, ViewGroup parent) 
         {
             View view = super.getView(position, convertView, parent);
             Button rmBtn = (Button)view.findViewById(R.id.collect_item_del_btn);
             if (rmBtn != null)
             {
             	assert (mChannelList != null);
                 rmBtn.setTag(mChannelList.get(position));
                 rmBtn.setOnClickListener(new OnClickListener() 
                 {
                     @Override
                     public void onClick(View v) 
                     {
                     	Channel bindChannel = (Channel) v.getTag();
                         
                         for (int i=0; i<mChannelList.size(); ++i)
                         {
                             if (mChannelList.get(i).id.equals(bindChannel.id))
                             {
                                 AppEngine.getInstance().getCollectManager().removeCollectChannel(mChannelList.get(i).id);
                                 mChannelList.remove(i);
                                 mItemList.remove(i);
                             }
                         }
                         mListViewAdapter.notifyDataSetChanged();
                     }
                 });
             }
             return view;
         }
         
         @Override
         public Object getItem(int position)
         {
         	return mItemList.get(position);
         }
         
         public Object remove(int position)
         {
         	if (position < 0 || position >= getCount())
         		return null;
         	
         	Object removeObject = getItem(position);
         	mChannelList.remove(position);
         	mItemList.remove(position);
         	notifyDataSetChanged();
         	
         	return removeObject;
         }
         
         @SuppressWarnings("unchecked")
 		public void add(int position, Object item)
         {
         	if (position < 0 || position > getCount() || item == null)
         		return;
         	
         	HashMap<String, Object> hashItem = (HashMap<String, Object>) item;
         	Channel channel = new Channel();
         	channel.id = (String) hashItem.get("id");
         	channel.name = (String) hashItem.get("name");
         	
         	mChannelList.add(position, channel);
         	mItemList.add(position, hashItem);
         	notifyDataSetChanged();
         }
     }
     
     private class MyViewBinder implements ViewBinder
     {
         public boolean setViewValue(View view, Object data,
                 String textRepresentation)
         {
             if((view instanceof ImageView) && (data instanceof Bitmap))
             {
                 ImageView iv = (ImageView)view;
                 Bitmap bm = (Bitmap)data;
                 iv.setImageBitmap(bm);
                 return true;
             }
             else if (view instanceof Button)
             {
                 Button btn = (Button)view;
                 btn.setText(getResources().getString(R.string.delete));
                 return true;
             }
             
             return false;
         }
     }
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_collect);
         
         mChannelListView = (DragSortListView)findViewById(R.id.collect_channel_list_view);
         mXmlChannelInfo = XmlParser.parseChannelInfo(this);
         mChannelList = new ArrayList<Channel>();
         mOnPlayingProgramList = new ArrayList<Pair<String,String>>();
         mReportList = new ArrayList<String>();
         mInflater = LayoutInflater.from(this);
         mContentLayout = (LinearLayout)findViewById(R.id.collect_content_layout);
         mNoCollectLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips, null);
         mCenterLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
         ((TextView) mNoCollectLayout.findViewById(R.id.center_tips_text_view)).setText(getResources().getString(R.string.promote_collect_tips));
         
         mController = buildController(mChannelListView);
         mChannelListView.setFloatViewManager(mController);
         mChannelListView.setOnTouchListener(mController);
         mChannelListView.setDragSortListener(this);
         mChannelListView.setRemoveListener(this);
         mChannelListView.setDragEnabled(true);
         
         mChannelListView.setOnItemClickListener(new OnItemClickListener() 
         {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
             {
                 String channelId = mChannelList.get(position).id;
                 String channelName = mChannelList.get(position).name;
                 ArrayList<Channel> channelList = new ArrayList<Channel>();
                 for (int i=0; i<mChannelList.size(); ++i)
                 {
                     Channel channel = new Channel();
                     channel.id = mChannelList.get(i).id;
                     channel.name = mChannelList.get(i).name;
                     channelList.add(channel);
                 }
                 
                 Intent intent = new Intent(CollectActivity.this, ChannelDetailActivity.class);
                 intent.putExtra("id", channelId);
                 intent.putExtra("name", channelName);
                 intent.putExtra("channel_list", (Serializable) channelList);
                 startActivity(intent);
             }
         });
         
         createAndSetListViewAdapter();
         createUpdateThreadAndHandler();
         report();

        // Show tips: long press to sort
        Toast.makeText(this, getResources().getString(R.string.long_press_sort_tips), Toast.LENGTH_LONG).show();
     }
     
     @Override
     protected void onResume() 
     {
         super.onResume();
         updateChannelListView();
         updateOnPlayingProgramList();
         
         if (mItemList.isEmpty())
         {
             mContentLayout.removeAllViews();
             mContentLayout.addView(mNoCollectLayout, mCenterLayoutParams);
         }
         else
         {
             mContentLayout.removeAllViews();
             mContentLayout.addView(mChannelListView);
         }
     };
 
     private void createAndSetListViewAdapter()
     {
         mItemList = new ArrayList<HashMap<String, Object>>();
         mListViewAdapter = new MySimpleAdapter(CollectActivity.this, mItemList, R.layout.collect_list_item,
                 new String[]{"image", "name", "program", "button"}, 
                 new int[]{R.id.collect_item_logo, R.id.collect_item_channel, R.id.collect_item_program, R.id.collect_item_del_btn});
         mListViewAdapter.setViewBinder(new MyViewBinder());
         mChannelListView.setAdapter(mListViewAdapter);
     }
     
     private void initChannelList()
     {
         mChannelList.clear();
         Iterator<Entry<String, HashMap<String, Object>>> iter = AppEngine.getInstance().getCollectManager().getCollectChannels().entrySet().iterator();
         while (iter.hasNext())
         {
             Map.Entry<String, HashMap<String, Object>> entry = (Map.Entry<String, HashMap<String,Object>>)iter.next();
             Channel channel = new Channel();
             channel.id = entry.getKey();
             channel.name = (String) entry.getValue().get("name");
             mChannelList.add(channel);
         }
     }
     
     private void updateChannelListView()
     {
         initChannelList();
         mItemList.clear();
         for (int i=0; i<mChannelList.size(); ++i)
         { 
             String id = mChannelList.get(i).id;
             String name = mChannelList.get(i).name;
             
             HashMap<String, Object> item = new HashMap<String, Object>();            
             item.put("id", id);
             item.put("name", name);
             if (mXmlChannelInfo.get(id) != null)
             {
                 item.put("image", Utility.getImage(CollectActivity.this, (String) mXmlChannelInfo.get(id).get(XML_ELEMENT_LOGO)));                        
             }
             mItemList.add(item);
         }
         mListViewAdapter.notifyDataSetChanged();
     }
     
     private void createUpdateThreadAndHandler()
     {
         mUpdateHandler = new Handler(NetworkManager.getInstance().getNetworkThreadLooper());
     }
     
     private void updateOnPlayingProgramList()
     {
         mUpdateHandler.post(new Runnable()
         {
             public void run()
             {
                 assert(mChannelList != null);
                 String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_ON_PLAYING_PROGRAMS);
                 try 
                 {
                     NetDataGetter getter = new DefaultNetDataGetter(url);
                     List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
                     //String test = "{\"channels\":[\"cctv1\", \"cctv3\"]}";
                     String idArray = "[";
                     for (int i=0; i<mChannelList.size(); ++i)
                     {
                         idArray += "\"" + mChannelList.get(i).id + "\"";
                         if (i < (mChannelList.size() - 1))
                         {
                             idArray += ",";
                         }
                     }
                     idArray += "]";
                     
                     pairs.add(new BasicNameValuePair("channels", "{\"channels\":" + idArray + "}"));
                     JSONObject jsonRoot = getter.getJSONsObject(pairs);
                     mOnPlayingProgramList.clear();
                     if (jsonRoot != null)
                     {
                         JSONArray resultArray = jsonRoot.getJSONArray("result");
                         if (resultArray != null)
                         {
                             for (int i=0; i<resultArray.length(); ++i)
                             {
                                 Pair<String, String> pair = new Pair<String, String>(resultArray.getJSONObject(i).getString("id"), 
                                         resultArray.getJSONObject(i).getString("title"));
                                 mOnPlayingProgramList.add(pair);
                             }
                         }
                     }
                     uiHandler.sendEmptyMessage(MSG_REFRESH_ON_PLAYING_PROGRAM_LIST);
                 }
                 catch (MalformedURLException e) 
                 {
                     e.printStackTrace();
                 }
                 catch (JSONException e) 
                 {
                     e.printStackTrace();
                 }
             }
         });
     }
     
     private void report()
     {
         mReportList.clear();
         Iterator<Entry<String, HashMap<String, Object>>> iter = AppEngine.getInstance().getCollectManager().getCollectChannels().entrySet().iterator();
         while (iter.hasNext())
         {
             Map.Entry<String, HashMap<String, Object>> entry = (Map.Entry<String, HashMap<String,Object>>)iter.next();
             mReportList.add(entry.getKey());
         }
         if (mReportList.size() == 0)
             return;
         new Thread(new Runnable() 
         {
             @Override
             public void run() 
             {
                 String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_REPORT) + "?type=collect";
                 try 
                 {
                     NetDataGetter getter = new DefaultNetDataGetter(url);
                     List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
                     //String test = "{\"channels\":[\"cctv1\", \"cctv3\"]}";
                     String idArray = "[";
                     for (int i=0; i<mReportList.size(); ++i)
                     {
                         idArray += "\"" + mReportList.get(i) + "\"";
                         if (i < (mReportList.size() - 1))
                         {
                             idArray += ",";
                         }
                     }
                     idArray += "]";
                     pairs.add(new BasicNameValuePair("channels", "{\"channels\":" + idArray + "}"));
                     getter.getJSONsObject(pairs);
                 } 
                 catch (MalformedURLException e) 
                 {
                     e.printStackTrace();
                 }
             }
         }).start();
     }
     
     private DragSortController buildController(DragSortListView dslv) 
 	{
         DragSortController controller = new DragSortController(dslv);
         controller.setRemoveEnabled(false);
         controller.setSortEnabled(true);
         controller.setDragInitMode(DragSortController.ON_LONG_PRESS);
 //        controller.setRemoveMode(DragSortController.FLING_REMOVE);
         return controller;
     }
     
     private Handler uiHandler = new Handler()
     {
         public void handleMessage(Message msg)
         {
             super.handleMessage(msg);
             switch (msg.what) 
             {
                 case MSG_REFRESH_ON_PLAYING_PROGRAM_LIST:
                     if (mOnPlayingProgramList != null)
                     {
                         for (int i=0; i<mOnPlayingProgramList.size(); ++i)
                         {
                             mItemList.get(i).put("program", "正在播放： " +  mOnPlayingProgramList.get(i).second);
                         }
                         mListViewAdapter.notifyDataSetChanged();
                     }
                     break;
                 default:
                     break;
             }
         }
     };
 
 	@Override
 	public void drop(int from, int to) 
 	{
 		CollectManager manager = AppEngine.getInstance().getCollectManager();
 		String dragChannelId = mChannelList.get(from).id;
 		if (dragChannelId != null)
 		{
 			HashMap<String, Object> dragChannelInfo = manager.removeCollectChannel(dragChannelId);
 			manager.addCollectChannel(to, dragChannelId, dragChannelInfo);
 		}
 		
 		Object dragObject = mListViewAdapter.remove(from);
 		if (dragObject != null)
 			mListViewAdapter.add(to, dragObject);
 	}
 
 	@Override
 	public void drag(int from, int to) 
 	{
 	}
 
 	@Override
 	public void remove(int which) 
 	{
 	}
 }
