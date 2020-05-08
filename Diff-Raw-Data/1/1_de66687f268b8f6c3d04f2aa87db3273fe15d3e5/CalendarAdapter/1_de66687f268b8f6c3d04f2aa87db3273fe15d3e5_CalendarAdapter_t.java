 /*
  * Copyright 2011 Lauri Nevala.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.cesar.yourlifealbum.components.adapters;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.cesar.yourlifealbum.R;
 import com.cesar.yourlifealbum.data.db.models.Photo;
 
 public class CalendarAdapter extends BaseAdapter {
 
     static final int FIRST_DAY_OF_WEEK = 0; // Sunday = 0, Monday = 1
     private Context mContext;
     private Calendar mMonth;
     private Calendar mSelectedDate;
     private List<Photo> mPhotoList;
     public String[] mDays;
     private List<Photo> mPhotoSetDay;
 
     public CalendarAdapter(final Context c, final Calendar monthCalendar) {
 
         mMonth = monthCalendar;
         mSelectedDate = (Calendar) monthCalendar.clone();
         mContext = c;
         mMonth.set(Calendar.DAY_OF_MONTH, 1);
         refreshDays();
     }
 
     public void setItems(final List<Photo> items) {
         mPhotoList = items;
     }
 
     @Override
     public int getCount() {
         return mDays.length;
     }
 
     @Override
     public Object getItem(final int position) {
         if (mDays != null && mDays.length > position) {
             return mDays[position];
         } else {
             return null;
         }
     }
 
     @Override
     public long getItemId(final int position) {
         return 0;
     }
 
     public class ViewHolder {
 
         public LinearLayout dateLayout;
         public TextView dateText;
         public ImageView dayImage;
 
         public ViewHolder(final View view) {
 
             dateLayout = (LinearLayout) view
                     .findViewById(R.id.calendar_date_layout);
             dateText = (TextView) view.findViewById(R.id.calendar_date);
             dayImage = (ImageView) view.findViewById(R.id.calendar_date_icon);
         }
 
     }
 
     // create a new view for each item referenced by the Adapter
     @Override
     public View getView(final int position, View convertView,
             final ViewGroup parent) {
 
         ViewHolder viewHolder = null;
 
         if (convertView == null
                 || (viewHolder = (ViewHolder) convertView.getTag()) == null) {
 
             LayoutInflater inflater = (LayoutInflater) mContext
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             convertView = inflater.inflate(R.layout.calendar_item, null, false);
             viewHolder = new ViewHolder(convertView);
             convertView.setTag(viewHolder);
         }
 
         // disable empty days from the beginning
         if (mDays[position].equals("")) {
             viewHolder.dateText.setClickable(false);
             viewHolder.dateText.setFocusable(false);
         } else {
             // mark current day as focused
             if (mMonth.get(Calendar.YEAR) == mSelectedDate.get(Calendar.YEAR)
                     && mMonth.get(Calendar.MONTH) == mSelectedDate
                             .get(Calendar.MONTH)
                     && mDays[position].equals(""
                             + mSelectedDate.get(Calendar.DAY_OF_MONTH))) {
                 viewHolder.dateLayout
                         .setBackgroundResource(R.drawable.item_background_focused);
             } else {
                 viewHolder.dateLayout
                         .setBackgroundResource(R.drawable.list_item_background);
             }
         }
         viewHolder.dateText.setText(mDays[position]);
 
         // create date string for comparison
         String date = mDays[position];
 
         if (date.length() == 1) {
             date = "0" + date;
         }
         String monthStr = "" + (mMonth.get(Calendar.MONTH) + 1);
         if (monthStr.length() == 1) {
             monthStr = "0" + monthStr;
         }
 
         // show icon if date is not empty and it exists in the items array
         if (date.length() > 0 && mPhotoList != null
                 && isPhotoDay(Integer.parseInt(date))) {
             viewHolder.dayImage.setVisibility(View.VISIBLE);
             viewHolder.dateLayout.setOnClickListener(new OnClickListener() {
 
                 @Override
                 public void onClick(final View v) {
                     // TODO Auto-generated method stub
 
                 }
             });
         } else {
             viewHolder.dayImage.setVisibility(View.INVISIBLE);
         }
         return convertView;
     }
 
     public void refreshDays() {
 
         int lastDay = mMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
         int firstDay = mMonth.get(Calendar.DAY_OF_WEEK);
 
         // figure size of the array
         if (firstDay == 1) {
             mDays = new String[lastDay + (FIRST_DAY_OF_WEEK * 6)];
         } else {
             mDays = new String[lastDay + firstDay - (FIRST_DAY_OF_WEEK + 1)];
         }
 
         int j = FIRST_DAY_OF_WEEK;
 
         // populate empty days before first real day
         if (firstDay > 1) {
             for (j = 0; j < firstDay - FIRST_DAY_OF_WEEK; j++) {
                 mDays[j] = "";
             }
         } else {
             for (j = 0; j < FIRST_DAY_OF_WEEK * 6; j++) {
                 mDays[j] = "";
             }
             j = FIRST_DAY_OF_WEEK * 6 + 1; // sunday => 1, monday => 7
         }
 
         // populate days
         int dayNumber = 1;
         for (int i = j - 1; i < mDays.length; i++) {
             mDays[i] = "" + dayNumber;
             dayNumber++;
         }
     }
 
     private boolean isPhotoDay(final int day) {
 
         if (mPhotoList != null && mPhotoList.size() > 0) {
             for (Photo item : mPhotoList) {
                 if (item.getDay() == day) {
                     if (mPhotoSetDay == null) {
                         mPhotoSetDay = new ArrayList<Photo>();
                     }
                     mPhotoSetDay.add(item);
                 }
             }
         }
         return mPhotoSetDay != null ? true : false;
     }
 }
