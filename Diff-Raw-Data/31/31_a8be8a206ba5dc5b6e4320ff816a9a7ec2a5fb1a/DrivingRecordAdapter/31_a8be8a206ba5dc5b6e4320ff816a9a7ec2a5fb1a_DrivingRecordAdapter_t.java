 package io.ehdev.android.drivingtime.adapter;
 
 import android.R;
 import android.content.Context;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import io.ehdev.android.drivingtime.backend.model.Record;
 import io.ehdev.android.drivingtime.view.entry.DisplayRecordRow;
 
 import java.util.Date;
 import java.util.List;
 
 public class DrivingRecordAdapter extends BaseAdapter{
 
     public static final String TAG = DrivingRecordAdapter.class.getName();
 
    public static final int NO_VALUE_SELECTED = -1;

     private Context context;
     private List<Record> drivingRecordList;
     private int selected = -1;
 
     public DrivingRecordAdapter(Context context, List<Record> drivingRecordList){
         this.context = context;
 
         this.drivingRecordList = drivingRecordList;
     }
 
     @Override
     public int getCount() {
         return drivingRecordList.size();
     }
 
     @Override
     public Record getItem(int position) {
         return drivingRecordList.get(position);
     }
 
     @Override
     public long getItemId(int position) {
         return drivingRecordList.get(position).getId();
     }
 
     @Override
     public View getView(int position, View view, ViewGroup parent) {
 
         if(null == view || !(view instanceof DisplayRecordRow))
             view = new DisplayRecordRow(context);
 
         java.text.DateFormat timeFormatter = DateFormat.getTimeFormat(context);
         Date startTime = getItem(position).getStartTime().toDate();
 
         java.text.DateFormat dateFormatter= DateFormat.getLongDateFormat(context);
         String timeString = String.format("%s %s", dateFormatter.format(startTime), timeFormatter.format(startTime));
 
         DisplayRecordRow displayRecordRow = (DisplayRecordRow) view;
         displayRecordRow.setRightText(timeString);
         displayRecordRow.setLeftText(getItem(position).getDrivingTask().getTaskName());
         displayRecordRow.setCenterText(getItem(position).getDurationAsString());
         displayRecordRow.requestLayout();
         Log.i(TAG, getItem(position).getDurationAsString());
         if(position == selected)
             displayRecordRow.setBackgroundResource(R.color.holo_orange_dark);
         else
             displayRecordRow.setBackgroundResource(R.color.transparent);
 
         return displayRecordRow;
     }
 
     public void setSelected(int index){
         this.selected = index;
         notifyDataSetChanged();
     }
 
    public boolean isIndexSelected(int index){
        return index == selected;
    }

     public void clearSelected(){
        setSelected(NO_VALUE_SELECTED);
     }
 }
