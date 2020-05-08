 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.utility;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import java.util.ArrayList;
 import java.util.List;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.domain.Domain;
 
 public class ListAdapter extends ArrayAdapter {
     private LayoutInflater mInflater;
     private List items;
     private Context context;
     private int resource;
     private boolean showdetails;
     private List orig_items;
 
     public ListAdapter(Context context, int resource, int textViewResourceId, List items, boolean showdetails) {
         super(context, textViewResourceId, items);
         mInflater = LayoutInflater.from(context);
         this.orig_items = items;
         this.items = items;
         this.context = context;
         this.resource = resource;
         this.showdetails = showdetails;
     }
 
     @Override
     public int getCount() {
         return items.size();
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         ViewHolder holder;
 
         if (convertView == null) {
             convertView = mInflater.inflate(resource, parent, false);
             holder = new ViewHolder();
             holder.icon = (ImageView) convertView.findViewById(R.id.icon);
             holder.name = (TextView) convertView.findViewById(R.id.name);
             holder.detail = (TextView) convertView.findViewById(R.id.detail);
 
             convertView.setTag(holder);
         } else {
             holder = (ViewHolder) convertView.getTag();
         }
 
         Domain domain = (Domain)items.get(position);
 
         holder.name.setText(domain.getName());
         if(domain.getName().length() > 30) {
             holder.name.setTextSize(18);
         }
 
         if (showdetails) {
             holder.detail.setText(domain.getDetail().substring(0, 200) + " ...");
         }
 
         if (domain.getIcon() != 0) {
             holder.icon.setImageResource(domain.getIcon());
         } else {
             holder.icon.setImageResource(R.drawable.ic_launcher); // default app icon
         }
 
         return convertView;
     }
 
     static class ViewHolder {
         ImageView icon;
         TextView name;
         TextView detail;
     }
 
     public void filter(CharSequence filter) {
         ArrayList filtered = new ArrayList();
 
         for (Domain c : (ArrayList<Domain>)orig_items) {
             if (c.getName().toLowerCase().contains(filter.toString().toLowerCase()))
             {
                 filtered.add(c);
             }
         }
 
         this.items = filtered;
 
         if("".equals(filter.toString())) {
             this.items = this.orig_items;
         }
 
         notifyDataSetChanged();
     }
 }
