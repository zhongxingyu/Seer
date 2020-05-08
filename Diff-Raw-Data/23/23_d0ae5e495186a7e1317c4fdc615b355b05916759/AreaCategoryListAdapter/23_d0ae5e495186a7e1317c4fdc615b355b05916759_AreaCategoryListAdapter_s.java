 package com.restaurant.adapter;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.restaurant.collection.CategoryActivity;
 import com.restaurant.collection.R;
 import com.restaurant.collection.entity.Area;
 import com.restaurant.collection.entity.Category;
 
 public class AreaCategoryListAdapter extends BaseAdapter {
 
     private final Activity        activity;
     private final  ArrayList<Category>       data;
 	private int area_id;
     private static LayoutInflater inflater = null;
 
     public AreaCategoryListAdapter(Activity a, ArrayList<Category> cs, int a_id) {
         activity = a;
         data = cs;
         area_id = a_id;
         inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
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
 
     public View getView(final int position, View convertView, ViewGroup parent) {
         View vi = convertView;
         if (convertView == null)
             vi = inflater.inflate(R.layout.item_listview_category, null);
         TextView text = (TextView) vi.findViewById(R.id.text_list);
         text.setText(data.get(position).getName());
         
         vi.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
             	
             	Intent intent = new Intent(activity, CategoryActivity.class);
             	Bundle bundle = new Bundle();
             	bundle.putInt("SecondCategoryId", data.get(position).getId());
             	bundle.putInt("AreaId", area_id);
                 intent.putExtras(bundle);
             	activity.startActivity(intent);
 
             }
 
         });
         
        vi.setBackground(activity.getResources().getDrawable(R.drawable.restaurant_list_selector));
 
         return vi;
     }
 }
