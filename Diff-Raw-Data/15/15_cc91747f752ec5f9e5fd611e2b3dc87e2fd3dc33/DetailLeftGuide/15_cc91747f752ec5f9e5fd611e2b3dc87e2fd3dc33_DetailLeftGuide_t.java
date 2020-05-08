 package com.tools.tvguide.views;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.tools.tvguide.R;
 import com.tools.tvguide.adapters.DetailLeftGuideListAdapter;
 import com.tools.tvguide.data.Channel;
 
 import android.content.Context;
 import android.database.DataSetObserver;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class DetailLeftGuide extends LinearLayout implements OnItemClickListener 
 {
     private String mTitle;
     
     private Context mContext;
     private TextView mTitleTextView;
     private ListView mChannelListView;
     private DetailLeftGuideListAdapter mListAdapter;
     private OnChannelSelectListener mListener;
     
     public abstract interface OnChannelSelectListener
     {
         public abstract void onChannelSelect(Channel channel);
     }
     
     public DetailLeftGuide(Context context) 
     {
         super(context);
         LayoutInflater.from(context).inflate(R.layout.channel_detail_left, this);
         
         mContext = context;
         mTitleTextView = (TextView) findViewById(R.id.detail_left_title_tv);
         mChannelListView = (ListView) findViewById(R.id.detail_left_channel_lv);
     }
 
     public void setTitle(String title)
     {
         mTitle = title;
     }
     
     public void setChannelList(List<Channel> channeList)
     {
         assert(channeList != null);
        
         mListAdapter = new DetailLeftGuideListAdapter(mContext, channeList);
         mChannelListView.setAdapter(mListAdapter);
         mChannelListView.setOnItemClickListener(this);
     }
     
    public void setCurrentIndex(int position)
    {
    	mListAdapter.setCurrentIndex(position);
    }
    
     public void setOnChannelSelectListener(OnChannelSelectListener listener)
     {
         mListener = listener;
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, final int position, long id) 
     {
         mListAdapter.setCurrentIndex(position);
         if (mListener != null)
             mListener.onChannelSelect((Channel) mListAdapter.getItem(position));
     }
 }
