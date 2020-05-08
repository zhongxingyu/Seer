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
  * Used in conjunction with a view to create a timeline of pictures built from the paths given in
  * the pictures array passed in the constructor. This adapter is used to create a gallery that scrolls
  * from horizontally.
  * @author Derek Dowling
  */
 public class HorizontalGalleryAdapter extends BaseAdapter {
 
     int mGalleryItemBackground;
     private Context mContext;
     private ArrayList<Picture> pictures;
 
     public HorizontalGalleryAdapter(Context c, Collection<Picture> pictures, int[] r_Styleable, int r_Styleable_bg) {
 
         mContext = c;
         TypedArray attr = mContext.obtainStyledAttributes(r_Styleable);
         mGalleryItemBackground = attr.getResourceId(
                 r_Styleable_bg, 0);
         this.pictures = new ArrayList<Picture>(pictures);
         attr.recycle();
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
 
     public LinearLayout getView(int position, View convertView, ViewGroup parent) {
 
         LinearLayout ll = new LinearLayout(mContext);
         ll.setOrientation(LinearLayout.VERTICAL);
 
         ImageView imageView = new ImageView(mContext);
 
         imageView.setImageDrawable(Drawable.createFromPath(pictures.get(position).getPath()));
         imageView.setLayoutParams(new Gallery.LayoutParams(150, 100));
         imageView.setScaleType(ImageView.ScaleType.FIT_XY);
         imageView.setBackgroundResource(mGalleryItemBackground);
 
         ll.addView(imageView);
 
         TextView tv = new TextView(mContext);
        tv.setText(pictures.get(position).getDate().toString() + " - " + pictures.get(position).getName());
         tv.setGravity(Gravity.CENTER_HORIZONTAL);
 
         ll.addView(tv);
 
         return ll;
     }
 }
