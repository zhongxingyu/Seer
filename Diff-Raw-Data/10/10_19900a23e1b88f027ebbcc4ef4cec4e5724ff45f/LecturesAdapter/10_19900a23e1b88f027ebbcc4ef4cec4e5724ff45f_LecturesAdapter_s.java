 package com.gdg.andconlab.ui;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.support.v4.widget.CursorAdapter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import com.gdg.andconlab.R;
 import com.gdg.andconlab.models.Lecture;
 
 public class LecturesAdapter extends CursorAdapter {
     private int idx_name;
     private int idx_description;
 
     public LecturesAdapter(Context context, Cursor c) {
         super(context, c, 0);
 
         idx_name = c.getColumnIndex(Lecture.COLUMN_NAME_NAME);
         idx_description = c.getColumnIndex(Lecture.COLUMN_NAME_DESCRIPTION);
     }
 
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         ViewHolder holder = (ViewHolder) view.getTag();
         holder.name.setText(cursor.getString(idx_name));
        holder.description.setText(cursor.getString(idx_description));
     }
 
     @Override
     public View newView(Context context, Cursor cursor, ViewGroup list) {
         LayoutInflater inflater = LayoutInflater.from(mContext);
         View view = inflater.inflate(R.layout.lecture_list_item, null);
 
         ViewHolder holder = new ViewHolder();
         holder.name = (TextView) view.findViewById(R.id.lecture_name);
         holder.description = (TextView) view.findViewById(R.id.lecture_description);
 
         view.setTag(holder);
 
         return view;
     }
 
 
     private class ViewHolder {
         public TextView name;
         public TextView description;
     }
 }
