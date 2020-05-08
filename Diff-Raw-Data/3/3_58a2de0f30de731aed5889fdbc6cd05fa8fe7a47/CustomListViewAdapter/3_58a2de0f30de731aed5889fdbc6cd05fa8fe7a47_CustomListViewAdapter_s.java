 package ar.com.nivel7.kernelgesturesbuilder;
 
 import java.util.List;
 import ar.com.nivel7.kernelgesturesbuilder.R;
 import ar.com.nivel7.kernelgesturesbuilder.ActionsRowItem;
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
  
 public class CustomListViewAdapter extends ArrayAdapter<ActionsRowItem> {
  
     Context context;
  
     public CustomListViewAdapter(Context context, int resourceId,
             List<ActionsRowItem> items) {
         super(context, resourceId, items);
         this.context = context;
     }
  
     /*private view holder class*/
     private class ViewHolder {
         ImageView imageView;
         TextView txtTitle;
        TextView txtDesc;
     }
  
     public View getView(int position, View convertView, ViewGroup parent) {
         ViewHolder holder = null;
         ActionsRowItem rowItem = getItem(position);
  
         LayoutInflater mInflater = (LayoutInflater) context
                 .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
         if (convertView == null) {
             convertView = mInflater.inflate(R.layout.launchactivities, null);
             holder = new ViewHolder();
             holder.txtTitle = (TextView) convertView.findViewById(R.id.actionlabel);
             holder.imageView = (ImageView) convertView.findViewById(R.id.actionicon);
             convertView.setTag(holder);
         } else
             holder = (ViewHolder) convertView.getTag();
  
        holder.txtDesc.setText(rowItem.getDesc());
         holder.txtTitle.setText(rowItem.getTitle());
         holder.imageView.setImageResource(rowItem.getImageId());
  
         return convertView;
     }
 }
