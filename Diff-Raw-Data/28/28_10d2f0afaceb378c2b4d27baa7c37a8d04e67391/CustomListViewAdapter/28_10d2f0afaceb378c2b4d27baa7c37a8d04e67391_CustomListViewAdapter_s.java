 package com.uofa.adventure_app.activity;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.uofa.adventure_app.R;
 import com.uofa.adventure_app.model.Annotation;
 import com.uofa.adventure_app.model.Media;
 /**
  * This class populates the list view in the Annotation screen.
  * 
  * @author Ulvi Ibrahimov, Chris Pavlicek
  *
  */
 public class CustomListViewAdapter extends ArrayAdapter<Annotation> {
 	 
     Context context;
  
     public CustomListViewAdapter(Context context, int resourceId,
             ArrayList<Annotation> items) {
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
         Annotation rowItem = getItem(position);
  
         LayoutInflater mInflater = (LayoutInflater) context
                 .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
         if (convertView == null) {
             convertView = mInflater.inflate(R.layout.list_annotations, null);
             holder = new ViewHolder();
             holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);
             holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
             holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
             convertView.setTag(holder);
         } else
             holder = (ViewHolder) convertView.getTag();
         
         if(rowItem.annotationString() != null) {
         	holder.txtDesc.setText(rowItem.annotationString());
         }
         if(rowItem.user().getName() != null) {
         	holder.txtTitle.setText("By: " + rowItem.user().getName());
         }
         if(rowItem.media() != null) {
         	Bitmap bitmap = Media.decodeBase64(rowItem.media());
         	holder.imageView.setImageBitmap(bitmap);
         }
  
         return convertView;
     }
 }
