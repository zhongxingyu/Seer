 package com.cocacola.climateambassador.adapters;
 
 import android.app.ActionBar;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.cocacola.climateambassador.R;
 import com.cocacola.climateambassador.ui.InternalTrainingOverview;
 import com.cocacola.climateambassador.ui.SupplierOverview;
 import com.cocacola.climateambassador.ui.ValueChainModule;
 
 /**
  * Created by Vinnie Vendemia on 8/27/13.
  */
 public class MenuListAdapter extends BaseAdapter {
 
     Context mContext;
     String[] mTitle;
     int[] mIcon;
     LayoutInflater mInflater;
 
    public MenuListAdapter(Context context, String[] title,
                           int[] icon) {
         mContext = context;
         mTitle = title;
         mIcon = icon;
         mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     @Override
     public int getCount() {
         return mTitle.length;
     }
 
     @Override
     public Object getItem(int position) {
         return mTitle[position];
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     public View getView(int position, View convertView, ViewGroup parent) {
 
         TextView txtTitle;
         TextView txtSubTitle;
         ImageView imgIcon;
 
         View v = mInflater.inflate(R.layout.drawer_list_item, parent, false);
 
         // Locate the TextViews in drawer_list_item.xml
         txtTitle = (TextView) v.findViewById(R.id.title);
 

         // Locate the ImageView in drawer_list_item.xml
         imgIcon = (ImageView) v.findViewById(R.id.icon);
 
         // Set the results into TextViews
         txtTitle.setText(mTitle[position]);
 
         // Set the results into ImageView
         imgIcon.setImageResource(mIcon[position]);
 
         return v;
     }
 
 
 
 }
