 package com.tools.tvguide.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.tools.tvguide.R;
 import com.tools.tvguide.adapters.ChannellistAdapter;
 import com.tools.tvguide.components.MyProgressDialog;
 import com.tools.tvguide.data.Category;
 import com.tools.tvguide.data.Channel;
 import com.tools.tvguide.managers.AppEngine;
 import com.tools.tvguide.managers.AdManager.AdSize;
 import com.tools.tvguide.managers.OnPlayingHtmlManager.OnPlayingCallback;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class ChannellistActivity extends Activity implements Callback 
 {
     private ListView mChannelListView;
     private ChannellistAdapter mListAdapter;
     private TextView mTitltTextView;
     private List<Channel> mChannelList;
     private HashMap<String, String> mOnPlayingPrograms = new HashMap<String, String>();
     private ArrayList<HashMap<String, Object>> mItemList;
     private Category mCurrentCategory;
     private MyProgressDialog mProgressDialog;
     private Handler mUiHandler;
     private enum SelfMessage { Refresh_Channel_List, Refresh_On_Playing_Program_List };
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_channellist);
         mChannelListView = (ListView)findViewById(R.id.channel_list);
         mTitltTextView = (TextView)findViewById(R.id.channellist_text_title);
         mChannelList = new ArrayList<Channel>();
         mItemList = new ArrayList<HashMap<String, Object>>();
         mListAdapter = new ChannellistAdapter(this, mItemList);
         mChannelListView.setAdapter(mListAdapter);
         mProgressDialog = new MyProgressDialog(this);
         mUiHandler = new Handler(this);
         mCurrentCategory = (Category) getIntent().getSerializableExtra("category");
         if (mCurrentCategory == null)
             return;
         
         mTitltTextView.setText(mCurrentCategory.name);
         updateChannelList();
         mChannelListView.setOnItemClickListener(new OnItemClickListener() 
         {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
             {
 //                String channelId = mChannelList.get(position).id;
 //                String channelName = mChannelList.get(position).name;
 //                Intent intent = new Intent(ChannellistActivity.this, ChannelDetailActivity.class);
 //                intent.putExtra("id", channelId);
 //                intent.putExtra("name", channelName);
 //                
 //                ArrayList<Channel> channelList = new ArrayList<Channel>();
 //                for (int i=0; i<mChannelList.size(); ++i)
 //                {
 //                    Channel channel = new Channel();
 //                    channel.id = mChannelList.get(i).id;
 //                    channel.name = mChannelList.get(i).name;
 //                    channelList.add(channel);
 //                }
 //                intent.putExtra("channel_list", (Serializable) channelList);
 //                startActivity(intent);
             }
         });
         
         AppEngine.getInstance().getAdManager().addAdView(ChannellistActivity.this, R.id.adLayout, AdSize.NORMAL_SIZE);
     }
 
     public void back(View view)
     {
         if (view instanceof Button)
         {
             // The same effect with press back key
             finish();
         }
     }
        
     private void updateChannelList()
     {
         AppEngine.getInstance().getOnPlayingHtmlManager().getOnPlayingChannels(0, mCurrentCategory, new OnPlayingCallback() 
         {
             @Override
             public void onPlayingChannelsLoaded(int requestId, List<Channel> channels, HashMap<String, String> programs) 
             {
                 if (channels != null)
                 {
                     mChannelList.clear();
                     mChannelList.addAll(channels);
                     mUiHandler.obtainMessage(SelfMessage.Refresh_Channel_List.ordinal()).sendToTarget();
                 }
                 if (programs != null)
                 {
                     mOnPlayingPrograms.clear();
                     mOnPlayingPrograms.putAll(programs);
                 }
             }
         });
     }
     
     private void updateOnPlayingProgramList()
     {
         mUiHandler.obtainMessage(SelfMessage.Refresh_On_Playing_Program_List.ordinal()).sendToTarget();
     }
 
     @Override
     public boolean handleMessage(Message msg) 
     {
         SelfMessage selfMsg = SelfMessage.values()[msg.what];
         switch (selfMsg)
         {
             case Refresh_Channel_List:
                 if (mChannelList != null)
                 {
                     mProgressDialog.dismiss();
                     mItemList.clear();
                     for(int i=0; i<mChannelList.size(); ++i)
                     {
                         HashMap<String, Object> item = new HashMap<String, Object>();
                         item.put("tvmao_id", mChannelList.get(i).tvmaoId);
                         item.put("name", mChannelList.get(i).name);
                         mItemList.add(item);
                     }
                     mListAdapter.notifyDataSetChanged();
                     updateOnPlayingProgramList();
                 }
                 break;
             case Refresh_On_Playing_Program_List:
                 for (int i=0; i<mItemList.size(); ++i)
                 {
                     String channelId = (String) mItemList.get(i).get("tvmao_id");
                     if (channelId == null)
                         continue;
                     String onPlayingString = mOnPlayingPrograms.get(channelId);
                     if (onPlayingString != null)
                     {
                         mItemList.get(i).put("program", "正在播出：" + onPlayingString);
                     }
                 }
                 mListAdapter.notifyDataSetChanged();
                 break;
         }
         return true;
     }
 }
