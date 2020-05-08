 package com.dgsd.android.ShiftTracker.Adapter;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.text.TextUtils;
 import android.text.format.DateFormat;
 import android.text.format.DateUtils;
 import android.util.SparseArray;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.dgsd.android.ShiftTracker.Data.DbField;
 import com.dgsd.android.ShiftTracker.Data.Provider;
 import com.dgsd.android.ShiftTracker.Model.Shift;
 import com.dgsd.android.ShiftTracker.R;
 import com.dgsd.android.ShiftTracker.Service.DbService;
 import com.dgsd.android.ShiftTracker.Util.UIUtils;
 
import java.util.Date;
 
 public class TemplateAdapter extends SimpleCursorAdapter implements SimpleCursorAdapter.ViewBinder {
     private SparseArray<String> mIdToTimeArray;
    private java.text.DateFormat mTimeFormat;
 
     public TemplateAdapter(Context context) {
         super(context, R.layout.list_item_template, null,
                 new String[]{DbField.ID.name}, new int[]{R.id.container}, 0);
 
         this.setViewBinder(this);
        mTimeFormat = DateFormat.getTimeFormat(context);
 
         //Caching
         mIdToTimeArray = new SparseArray<String>();
     }
 
     @Override
     public boolean setViewValue(View view, Cursor cursor, int i) {
         ViewHolder holder = (ViewHolder) view.getTag();
         if(holder == null)
             holder = new ViewHolder(view);
 
         holder.shift = Shift.fromCursor(cursor);
         holder.name.setText(holder.shift.name);
         holder.time.setText(getTimeText(holder.shift));
 
         if(TextUtils.isEmpty(holder.shift.note)) {
             holder.note.setVisibility(View.GONE);
         } else {
             holder.note.setText(holder.shift.note);
             holder.note.setVisibility(View.VISIBLE);
         }
 
         final long shiftId = holder.shift.id;
         holder.delete.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 final ContentValues values = new ContentValues();
                 values.put(DbField.IS_TEMPLATE.name, 0);
                 DbService.async_update(mContext, Provider.SHIFTS_URI, DbField.ID + "=" + shiftId, values);
             }
         });
 
         return true;
     }
 
     private String getTimeText(Shift shift) {
         String time = mIdToTimeArray.get( (int) shift.id);
         if(!TextUtils.isEmpty(time))
             return time;
 
 
        time = mTimeFormat.format(new Date(shift.getStartTime())) + " - " + mTimeFormat.format(new Date(shift.getEndTime()));
 
         time += " (" + UIUtils.getDurationAsHours(shift.getDurationInMinutes()) + ")";
 
         mIdToTimeArray.put( (int) shift.id, time);
         return time;
     }
 
     @Override
     public Cursor swapCursor(Cursor c) {
         clearCaches();
         return super.swapCursor(c);
     }
 
     private void clearCaches() {
         mIdToTimeArray.clear();
     }
 
     public static class ViewHolder {
         TextView name;
         TextView time;
         TextView note;
         ImageView delete;
 
         public Shift shift;
 
         public ViewHolder(View view) {
             if(view == null)
                 return;
 
             name = (TextView) view.findViewById(R.id.name);
             time = (TextView) view.findViewById(R.id.time);
             note = (TextView) view.findViewById(R.id.note);
             delete = (ImageView) view.findViewById(R.id.delete);
 
             view.setTag(this);
         }
     }
 }
