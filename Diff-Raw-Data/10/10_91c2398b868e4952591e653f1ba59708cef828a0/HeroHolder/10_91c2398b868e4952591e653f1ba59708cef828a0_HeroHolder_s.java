 package com.ouchadam.fang.presentation.item;
 
 import android.content.Context;
 import android.widget.ImageView;
 
 import com.squareup.picasso.Picasso;
 
 public class HeroHolder {
 
     private static final int INVALID = -1;
 
     int width;
     int height;
     String url;
 
     public HeroHolder() {
         this.width = INVALID;
         this.height = INVALID;
         this.url = null;
     }
 
     void tryLoad(Context context, ImageView imageView) {
         if (isValid(width) && isValid(height) && isValid(url)) {
             // TODO blur or not to blur? setting perhaps?
             Picasso.with(context).load(url).centerCrop().resize(width, height).into(imageView);
 //            Picasso.with(context).load(url).transform(new BlurTransformation(url, context)).centerCrop().resize(width, height).into(imageView);
         }
     }
 
     private boolean isValid(int dimen) {
         return dimen != INVALID;
     }
 

     private boolean isValid(String url) {
         return url != null && !url.isEmpty();
     }
 }
