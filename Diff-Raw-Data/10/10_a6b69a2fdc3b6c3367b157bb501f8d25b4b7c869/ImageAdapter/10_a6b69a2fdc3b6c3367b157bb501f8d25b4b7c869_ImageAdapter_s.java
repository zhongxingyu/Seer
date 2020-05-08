 package org.gnuton.newshub.adapters;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.support.v4.view.PagerAdapter;
 import android.view.View;
 import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 
import org.gnuton.newshub.R;

 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * Created by gnuton on 6/18/13.
  */
 
 public class ImageAdapter extends PagerAdapter {
     Context mContext;
     private List<Drawable> mImages = new ArrayList<Drawable>();
     private List<ImageView> mImageViews = new ArrayList<ImageView>();
 
     public ImageAdapter(Context context){
         this.mContext=context;
 
         // Initialize the 4 delegates which will host images
         while (mImageViews.size() < 4) {
             ImageView imageView = new ImageView(mContext);
             imageView.setMinimumHeight(240);
             imageView.setBackgroundColor(Color.parseColor("#000000"));
             imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);  // FIT_CENTER
 
             this.mImageViews.add(imageView);
         }
     }
 
     @Override
     public int getCount() {
         return mImages.size();
     }
 
     public void clear(){
         mImages.clear();
         notifyDataSetChanged();
 
     }
     public void addImage(Drawable image){
         mImages.add(image);
         notifyDataSetChanged();
     }
 
     @Override
     public boolean isViewFromObject(View view, Object object) {
         return view == object;
     }
 
     @Override
     public Object instantiateItem(ViewGroup container, int position) {
         ImageView imageView = mImageViews.get(position % 3);
         imageView.setImageDrawable(mImages.get(position));
         if (imageView.getParent() == null)
             container.addView(imageView, 0);
         //if (position == 0)
         //    scheduleAnimationForView(imageView);
         return imageView;
     }
 
     @Override
     public void destroyItem(ViewGroup container, int position, Object object) {
         ImageView imageView = (ImageView) object;
         container.removeView(imageView);
     }
 
     @Override
     public int getItemPosition(Object object) {
         return POSITION_NONE;
     }
 
     public void scheduleAnimationForView(ImageView imageView){
         Animation fadeInAnimation = AnimationUtils.loadAnimation(mContext, R.animator.fadein);
         imageView.setAnimation(fadeInAnimation);
    }
 }
