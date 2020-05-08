 package com.tools.tvguide.adapters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.tools.tvguide.R;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class DateAdapter extends BaseAdapter
 {
     public static class DateData
     {
         public String mDate;
         public DateData(String date)
         {
             mDate = date;
         }
     }
     
     private Context mContext;
     private List<DateData> mDataList;
     private int mCurIndex;
     
     public DateAdapter(Context context, int maxDays)
     {
         assert (maxDays >=1 && maxDays <= 14);
         mContext = context;
         mCurIndex = 0;
         mDataList = new ArrayList<DateAdapter.DateData>();
         
         String[] weekdays = mContext.getResources().getStringArray(R.array.weekdays);
         for (int i=0; i<maxDays; ++i)
         {
             DateData data = new DateData(weekdays[i]);
             mDataList.add(data);
         }
     }
     
     public void resetMaxDays(int maxDays)
     {
     	assert (maxDays >=1 && maxDays <= 14);
     	if (maxDays == mDataList.size())
     	    return;
     	
     	mDataList.clear();
     	String[] weekdays = mContext.getResources().getStringArray(R.array.weekdays);
         for (int i=0; i<maxDays; ++i)
         {
             DateData data = new DateData(weekdays[i]);
             mDataList.add(data);
         }
         notifyDataSetChanged();
     }
     
     public void setCurrentIndex(int position)
     {
         assert(position >= 0);
         if (position >= mDataList.size())
             return;
         mCurIndex = position;
         notifyDataSetChanged();
     }
     
     @Override
     public int getCount() 
     {
         assert(mDataList != null);
         return mDataList.size();
     }
 
     @Override
     public Object getItem(int position) 
     {
         assert(mDataList != null);
         return mDataList.get(position);
     }
 
     @Override
     public long getItemId(int position) 
     {
         return position;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) 
     {
         if (convertView == null)
         {
             convertView = LayoutInflater.from(mContext).inflate(R.layout.row, parent, false);
         }
         
         ImageView iconImageView = ((ImageView) convertView.findViewById(R.id.row_icon));
         TextView titleTextView = ((TextView) convertView.findViewById(R.id.row_title));
         titleTextView.setText(mDataList.get(position).mDate);
 
         if (position == mCurIndex)
         {
             iconImageView.setVisibility(View.VISIBLE);
             titleTextView.setTextColor(mContext.getResources().getColor(R.color.dark_blue));
         }
         else
         {
             iconImageView.setVisibility(View.GONE);
             titleTextView.setTextColor(mContext.getResources().getColor(R.color.darkgray));
         }
         
         return convertView;
     }
 }
