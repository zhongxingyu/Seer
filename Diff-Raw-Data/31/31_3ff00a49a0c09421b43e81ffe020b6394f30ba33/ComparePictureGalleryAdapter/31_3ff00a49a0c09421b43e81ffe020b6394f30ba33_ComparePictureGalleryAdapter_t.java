 package com.cs301w01.meatload.adapters;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.drawable.Drawable;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 
 import com.cs301w01.meatload.model.Picture;
 
 public class ComparePictureGalleryAdapter extends BaseAdapter {
 
     int mGalleryItemBackground;
     private Context mContext;
     private ArrayList<Picture> pictures;
 
     public ComparePictureGalleryAdapter(Context c, Collection<Picture> pictures, int[] r_Styleable, int r_Styleable_bg) {
 
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
 
     public View getView(int position, View convertView, ViewGroup parent) {
         LinearLayout ll = new LinearLayout(mContext);
         ll.setOrientation(LinearLayout.VERTICAL);
 
         ImageView imageView = new ImageView(mContext);
 
         imageView.setImageDrawable(Drawable.createFromPath(pictures.get(position).getPath()));
         imageView.setLayoutParams(new Gallery.LayoutParams(300, 200));
         imageView.setScaleType(ImageView.ScaleType.FIT_XY);
         imageView.setBackgroundResource(mGalleryItemBackground);
 
         ll.addView(imageView);
 
         TextView tv = new TextView(mContext);
        tv.setText(pictures.get(position).getName());
         tv.setGravity(Gravity.CENTER_HORIZONTAL);
 
         ll.addView(tv);
 
         return ll;
     }
 
 }
