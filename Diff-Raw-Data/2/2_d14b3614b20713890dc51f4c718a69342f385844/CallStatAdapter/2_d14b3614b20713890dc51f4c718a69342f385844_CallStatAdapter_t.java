 package org.ktln2.android.callstat;
 
 import android.content.Context;
 import android.widget.ArrayAdapter;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.ImageView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.LayoutInflater;
 import android.text.SpannableString;
 import android.text.style.StyleSpan;
 import android.graphics.Typeface;
 import java.util.Comparator;
 
 
 /*
  * This adapter bind the data to the list view.
  *
  * There is only a problem: from the same data you can calculate several
  * different list ordering and visualization.
  *
  * The common point is the layout of the cell: a contact information
  * and a value side by side.
  */
 public class CallStatAdapter extends ArrayAdapter<CallStat> {
     public final static int CALL_STAT_ADAPTER_ORDERING_TOTAL_DURATION = 0;
     public final static int CALL_STAT_ADAPTER_ORDERING_TOTAL_CALLS    = 1;
     public final static int CALL_STAT_ADAPTER_ORDERING_MAX_DURATION   = 2;
     public final static int CALL_STAT_ADAPTER_ORDERING_MIN_DURATION   = 3;
     public final static int CALL_STAT_ADAPTER_ORDERING_AVG_DURATION   = 4;
     // the only role of this class is to maintain
     // the expensive information about list item
     // without quering everytime the layout
     private class Holder {
         public TextView numberView;
         public TextView contactView;
         public TextView contactTotalCallsView;
         public TextView contactTotalDurationView;
         public TextView contactAvgDurationView;
         public TextView contactMaxDurationView;
         public TextView contactMinDurationView;
         public ImageView photoView;
     }
 
     Context mContext;
     StatisticsMap mMap;
 
     /*
      * From default load the data by max duration.
      */
     public CallStatAdapter(Context context, StatisticsMap data) {
         super(
             context,
             R.layout.list_duration_item,
             data.getCallStatOrderedByMaxDuration()
         );
 
         mContext = context;
         mMap = data;
     }
 
     public View getView(int position, View view, ViewGroup viewGroup) {
         Holder holder;
         if (view == null) {
             LayoutInflater inflater =
                 (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_duration_item, viewGroup, false);
 
             holder = new Holder();
             holder.numberView  = (TextView)view.findViewById(R.id.number);
             holder.contactView = (TextView)view.findViewById(R.id.contact);
             holder.contactTotalCallsView = (TextView)view.findViewById(R.id.contact_total_calls);
             holder.contactAvgDurationView = (TextView)view.findViewById(R.id.contact_avg_duration);
             holder.contactMaxDurationView = (TextView)view.findViewById(R.id.contact_max_duration);
             holder.contactMinDurationView = (TextView)view.findViewById(R.id.contact_min_duration);
             holder.photoView   = (ImageView)view.findViewById(R.id.photo);
 
             view.setTag(holder);
 
         } else {
             holder = (Holder)view.getTag();
         }
 
         CallStat entry = getItem(position);
 
         String phonenumber = entry.getKey();
         float percent = new Float(entry.getTotalDuration())*100/new Float(mMap.getTotalDuration());
 
         holder.numberView.setText(phonenumber);
 
         // show contact name
         holder.contactView.setText(entry.getContactName());
 
         // fill various statistical data
         String text = new String(entry.getTotalCalls() + " calls for " + DateUtils.formatElapsedTimeNG(entry.getTotalDuration()));
         SpannableString ss = new SpannableString(text);
         ss.setSpan(new StyleSpan(Typeface.BOLD), 0, ss.length(), 0);
         holder.contactTotalCallsView.setText(ss);
 
         holder.contactAvgDurationView.setText("Average call: " + DateUtils.formatElapsedTimeNG(entry.getAverageDuration()));
         holder.contactMaxDurationView.setText("Max call: " + DateUtils.formatElapsedTimeNG(entry.getMaxDuration()));
         holder.contactMinDurationView.setText("Min call: " + DateUtils.formatElapsedTimeNG(entry.getMinDuration()));
 
 
         // show contact photo
         holder.photoView.setImageBitmap(entry.getContactPhoto());
 
         return view;
     }
 
     /*
      * Tell to the adapter to reorder its data with respect to
      * the quantity passed as argument.
      */
     public void order(int type) {
         Comparator<CallStat> comparator = null;
         switch(type) {
             case CALL_STAT_ADAPTER_ORDERING_TOTAL_DURATION:
                 comparator = new Comparator<CallStat>() {
                     public int compare(CallStat a, CallStat b) {
                         return (int)(-a.getTotalDuration() + b.getTotalDuration());
                     }
                 };
                 break;
             case CALL_STAT_ADAPTER_ORDERING_AVG_DURATION:
                 comparator = new Comparator<CallStat>() {
                     public int compare(CallStat a, CallStat b) {
                         return (int)(-a.getAverageDuration() + b.getAverageDuration());
                     }
                 };
                 break;
             case CALL_STAT_ADAPTER_ORDERING_MAX_DURATION:
                 comparator = new Comparator<CallStat>() {
                     public int compare(CallStat a, CallStat b) {
                         return (int)(-a.getMaxDuration() + b.getMaxDuration());
                     }
                 };
                 break;
             case CALL_STAT_ADAPTER_ORDERING_MIN_DURATION:
                 comparator = new Comparator<CallStat>() {
                     public int compare(CallStat a, CallStat b) {
                         return (int)(-a.getMinDuration() + b.getMinDuration());
                     }
                 };
                 break;
             default:
             case CALL_STAT_ADAPTER_ORDERING_TOTAL_CALLS:
                 comparator = new Comparator<CallStat>() {
                     public int compare(CallStat a, CallStat b) {
                         return (int)(-a.getTotalCalls() + b.getTotalCalls());
                     }
                 };
                 break;
         }
         sort(comparator);
     }
 }
