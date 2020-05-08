 package com.cs301w01.meatload.adapters;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.drawable.Drawable;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.model.Picture;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * Use this adapter when you want to create a vertical photo gallery that slides up and down.
  * @author Derek Dowling
  */
 public class GridViewGalleryAdapter extends BaseAdapter{
 
     private Context mContext;
     private ArrayList<Picture> pictures;
 
     public GridViewGalleryAdapter(Context c, Collection<Picture> pictures) {
 
         mContext = c;
         this.pictures = new ArrayList<Picture>(pictures);
     }
 
     public int getCount() {
         return pictures.size();
     }
 
     public Picture getItem(int position) {
         return pictures.get(position);
     }
 
     public long getItemId(int position) {
         return pictures.get(position).getPictureID();
     }
 
     public View getView(int position, View convertView, ViewGroup parent) {
 
         LinearLayout ll;
 
 //        if (convertView == null) {  // if it's not recycled, initialize some attributes
 //            imageView = new ImageView(mContext);
 //            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
 //            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
 //            imageView.setPadding(8, 8, 8, 8);
 
             ll = new LinearLayout(mContext);
             ll.setOrientation(LinearLayout.VERTICAL);
 
             ImageView imageView = new ImageView(mContext);
 
             imageView.setImageDrawable(Drawable.createFromPath(pictures.get(position).getPath()));
             imageView.setLayoutParams(new Gallery.LayoutParams(85, 85));
             imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
 
             ll.addView(imageView);
 
             TextView tv = new TextView(mContext);
            tv.setText(pictures.get(position).getDate().toString() + " - " + pictures.get(position).getName());
             tv.setGravity(Gravity.CENTER_HORIZONTAL);
 
             ll.addView(tv);
 
 //        } else {
 //            ll = (LinearLayout) convertView;
 //            imageView = ll.get
 //        }
 
 //        imageView.setImageDrawable(Drawable.createFromPath(pictures.get(position).getPath()));
 
         return ll;
     }
 }
