 package com.r0adkll.sparc.pillalarm.adapters;
 
 import android.content.Context;
 import android.view.View;
 import android.widget.TextView;
 
 import com.r0adkll.deadskunk.adapters.BetterListAdapter;
 import com.r0adkll.deadskunk.utils.Utils;
 import com.r0adkll.sparc.pillalarm.R;
 import com.r0adkll.sparc.pillalarm.server.model.Prescription;
 import com.r0adkll.sparc.pillalarm.server.model.Schedule;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 /**
  * Created by r0adkll on 8/24/13.
  */
 public class PrescriptionListAdapter extends BetterListAdapter<Prescription> {
 
 
     /**
      * Constructor
      *
      * @param context            application context
      * @param textViewResourceId the item view id
      * @param objects            the list of objects
      */
     public PrescriptionListAdapter(Context context, int textViewResourceId, List<Prescription> objects) {
         super(context, textViewResourceId, objects);
     }
 
     @Override
     public ViewHolder createHolder(View view) {
         PrescriptViewHolder holder = new PrescriptViewHolder();
         holder.title = (TextView) view.findViewById(R.id.title);
         holder.desc = (TextView) view.findViewById(R.id.description);
         holder.tag = (TextView) view.findViewById(R.id.tag);
         holder.nextUpdate = (TextView) view.findViewById(R.id.next_update);
         return holder;
     }
 
     @Override
     public void bindHolder(ViewHolder holder, int position) {
         PrescriptViewHolder pvh = (PrescriptViewHolder) holder;
         Prescription data = getItem(position);
 
         // Compute next update time
         Calendar today = Calendar.getInstance();
         Calendar cal = Calendar.getInstance();
 
 
         // Get frequency
         if(!data.getScheduling().isEmpty()){
             Schedule sched = data.getScheduling().get(0);
             float rate = (24f / (float) sched.getFrequency());
             int modifier = (int) (10 / rate);
 
             cal.set(Calendar.HOUR_OF_DAY, 3 + modifier);
 
            while(cal.before(today) || (cal.getTime().before(data.getStartDate()))){
                 cal.add(Calendar.HOUR_OF_DAY, sched.getFrequency());
 
                 String dayTime = android.text.format.DateFormat.format("hh:mm a", cal.getTime()).toString();
                 String dateTime = android.text.format.DateFormat.format("MM-dd-yyyy", cal.getTime()).toString();
 
                 Utils.log("PLA", "NEXT UPDATE INCREMENT[" + dayTime + "][" + dateTime + "]");
             }
 
             String dayTime = android.text.format.DateFormat.format("hh:mm a", cal.getTime()).toString();
             String dateTime = android.text.format.DateFormat.format("MM-dd-yyyy", cal.getTime()).toString();
 
             pvh.nextUpdate.setText("Next alarm at " + dayTime + " on " + dateTime);
 
         }
 
         // Bind data
         pvh.title.setText(data.getName() + " - " + data.getDose() + " mg");
         pvh.desc.setText(data.getQuantity() + " pills ");
         if(!data.getTag().isEmpty()){
             pvh.tag.setVisibility(View.VISIBLE);
             pvh.tag.setText("#"+data.getTag());
         }else
             pvh.tag.setVisibility(View.GONE);
 
     }
 
     public static class PrescriptViewHolder extends ViewHolder{
         TextView title, desc, tag, nextUpdate;
     }
 }
