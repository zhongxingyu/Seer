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
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
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
     private static boolean sHasShownTips = false;
     private DragSortListView mChannelListView;
     private DragSortController mController;
     private MySimpleAdapter mListViewAdapter;
     private ArrayList<HashMap<String, Object>> mItemList;
     private List<Channel> mChannelList;
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
             Button rmBtn = (Button)view.findViewById(R.id.del_btn);
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
                             if (mChannelList.get(i).tvmaoId.equals(bindChannel.tvmaoId))
                             {
                                 AppEngine.getInstance().getCollectManager().removeCollectChannel(mChannelList.get(i).tvmaoId);
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
         	channel.tvmaoId = (String) hashItem.get("tvmao_id");
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
         mChannelList = new ArrayList<Channel>();
         mReportList = new ArrayList<String>();
         mInflater = LayoutInflater.from(this);
         mContentLayout = (LinearLayout)findViewById(R.id.collect_content_layout);
         mNoCollectLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips_layout, null);
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
                 Intent intent = new Intent(CollectActivity.this, ChannelDetailActivity.class);
                 intent.putExtra("tvmao_id", mChannelList.get(position).tvmaoId);
                 intent.putExtra("name", mChannelList.get(position).name);
                 intent.putExtra("channel_list", (Serializable) mChannelList);
                 startActivity(intent);
             }
         });
         
         createAndSetListViewAdapter();
         report();
     }
     
     @Override
     protected void onResume() 
     {
         super.onResume();
         updateChannelListView();
         
         if (mItemList.isEmpty())
         {
             mContentLayout.removeAllViews();
             mContentLayout.addView(mNoCollectLayout, mCenterLayoutParams);
         }
         else
         {
             mContentLayout.removeAllViews();
             mContentLayout.addView(mChannelListView);
             // Show tips: long press to sort
             if (mItemList.size() > 1 && sHasShownTips == false)
             {
                 Toast.makeText(this, getResources().getString(R.string.long_press_sort_tips), Toast.LENGTH_LONG).show();
                 sHasShownTips = true;
             }
         }
     };
 
     private void createAndSetListViewAdapter()
     {
         mItemList = new ArrayList<HashMap<String, Object>>();
         mListViewAdapter = new MySimpleAdapter(CollectActivity.this, mItemList, R.layout.collect_list_item,
                 new String[]{"image", "name", "button"}, 
                 new int[]{R.id.channel_logo_niv, R.id.channel_name_tv, R.id.del_btn});
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
             channel.tvmaoId = entry.getKey();
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
             String id = mChannelList.get(i).tvmaoId;
             String name = mChannelList.get(i).name;
             
             HashMap<String, Object> item = new HashMap<String, Object>();            
            item.put("id", id);
             item.put("name", name);
             mItemList.add(item);
         }
         mListViewAdapter.notifyDataSetChanged();
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
                 String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.ProxyUrl.Report) + "?type=collect";
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
 
 	@Override
 	public void drop(int from, int to) 
 	{
 		CollectManager manager = AppEngine.getInstance().getCollectManager();
 		String dragChannelId = mChannelList.get(from).tvmaoId;
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
