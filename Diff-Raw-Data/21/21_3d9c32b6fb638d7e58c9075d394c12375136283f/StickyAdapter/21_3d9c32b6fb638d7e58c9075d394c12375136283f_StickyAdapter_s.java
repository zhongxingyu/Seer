 package com.github.remomueller.tasktracker.android;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import android.graphics.Paint;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 
 public class StickyAdapter extends BaseAdapter {
 
     private static final String TAG = "TaskTrackerAndroid";
 
     private Activity activity;
     private ArrayList<Sticky> data;
     private static LayoutInflater inflater=null;
 
     public StickyAdapter(Activity a, ArrayList<Sticky> d) {
         activity = a;
         data=d;
         inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     public int getCount() {
         return data.size();
     }
 
     public Object getItem(int position) {
        return position;
     }
 
     public long getItemId(int position) {
         return position;
     }
 
     public View getView(int position, View convertView, ViewGroup parent) {
         View vi=convertView;
        if(convertView==null)
             vi = inflater.inflate(R.layout.stickies_index_item, null);
 
         TextView sticky_id = (TextView) vi.findViewById(R.id.sticky_id);
         TextView description = (TextView) vi.findViewById(R.id.description);
         TextView due_date = (TextView) vi.findViewById(R.id.due_date);
         // RelativeLayout tags_container = (RelativeLayout) vi.findViewById(R.id.tags_container);
 
         TextView single_tag = (TextView) vi.findViewById(R.id.single_tag);
 
         Sticky sticky = new Sticky();
         sticky = data.get(position);
 
 
 
         sticky_id.setText(Integer.toString(sticky.id));
         if(sticky.completed == true){
             sticky_id.setPaintFlags(sticky_id.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
         }
         description.setText(sticky.full_description());
         due_date.setText(sticky.short_due_date());
 
         // final TextView tagtext = new TextView(vi);
        int i;
 
        if(sticky.tags.length != 0){ // Not sure why this line is necessary, but it keeps stickies with no tags from showing tags from other stickies...
            for(i = 0; i < sticky.tags.length; i++){
                 // final TextView tagtext = new TextView(vi);
                 if(i == 0){
                     single_tag.setText(sticky.tags[i].name);
                     single_tag.setBackgroundColor(Color.parseColor(sticky.tags[i].color));
                 }
                 // tagtext.setText("This is: " + sticky.tags[i].name);
                 // tags_container.addView(tagtext);
             }
         }else{
             single_tag.setText("");
             single_tag.setVisibility(View.GONE);
         }
 
         return vi;
     }
 }
