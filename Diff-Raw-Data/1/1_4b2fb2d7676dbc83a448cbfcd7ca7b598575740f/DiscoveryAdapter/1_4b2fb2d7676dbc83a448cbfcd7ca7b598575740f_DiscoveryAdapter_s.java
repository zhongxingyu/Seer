 package com.github.alexesprit.chatlogs.adapter;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.TextView;
 import com.github.alexesprit.chatlogs.R;
 import com.github.alexesprit.chatlogs.item.DiscoveryItem;
 import com.github.alexesprit.chatlogs.loader.LogLoaderFactory;
 import com.github.alexesprit.chatlogs.util.Theme;
 
 import java.util.ArrayList;
 
 public class DiscoveryAdapter extends AbstactAdapter<DiscoveryItem> {
     public DiscoveryAdapter(Context context, ArrayList<DiscoveryItem> bookmarks) {
         super(context, bookmarks);
     }
 
     @Override
     public View getView(int i, View view, ViewGroup viewGroup) {
         ViewHolder vh;
         if (null == view) {
             view = inflater.inflate(R.layout.discovery_item, null);
             vh = new ViewHolder(view);
             view.setTag(vh);
         } else {
             vh = (ViewHolder)view.getTag();
         }
         vh.populateFrom(getItem(i));
         return view;
     }
 
     private static class ViewHolder implements CompoundButton.OnCheckedChangeListener {
         private TextView addressView;
         private TextView sourceView;
         private CheckBox checkBox;
         private DiscoveryItem item;
 
         private ViewHolder(View view) {
             addressView = (TextView)view.findViewById(R.id.discovery_item_label);
             sourceView = (TextView)view.findViewById(R.id.discovery_item_type);
             checkBox = (CheckBox)view.findViewById(R.id.discovery_item_checkbox);
             checkBox.setOnCheckedChangeListener(this);
         }
 
         private void populateFrom(DiscoveryItem item) {
             this.item = item;
             addressView.setText(item.address);
             addressView.setTextColor(Theme.getTextColor());
             sourceView.setText(LogLoaderFactory.getLoaderTypeName(item.source));
             sourceView.setTextColor(Theme.getTextColor());
         }
 
         @Override
         public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
             item.checked = b;
         }
     }
 }
