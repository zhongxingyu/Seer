 package com.beef.homework;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 
 public class CustomAdapter extends ArrayAdapter<String>{
     private ArrayList<String> entries;
     private Activity activity;
  
     public CustomAdapter(Activity a, int textViewResourceId, ArrayList<String> entries) {
         super(a, textViewResourceId, entries);
         this.entries = entries;
         this.activity = a;
     }
     public static class ViewHolder{
     	public TextView item1;
     	public TextView item2;
     }
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View v = convertView;
         ViewHolder holder;
         if (v == null) {
             LayoutInflater vi =
                 (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.grid_item, null);
             holder = new ViewHolder();
             holder.item1 = (TextView) v.findViewById(R.id.big);
             holder.item2 = (TextView) v.findViewById(R.id.small);
             v.setTag(holder);
         }
         else
             holder=(ViewHolder)v.getTag();
        String[] values = new String[entries.size()];
        values = entries.toArray(values);
         final String custom = entries.get(position);
        if (custom != null) {
        	for(int i = 0;i<entries.size();i++ ){
        		holder.item1.setText(values[i]);
        	}
            
            holder.item2.setText("Nothing");
        }
         return v;
     }
 
 }
