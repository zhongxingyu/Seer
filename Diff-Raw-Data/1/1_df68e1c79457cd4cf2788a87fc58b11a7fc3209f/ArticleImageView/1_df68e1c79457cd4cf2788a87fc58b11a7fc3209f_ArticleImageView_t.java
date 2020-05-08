 package org.gnuton.newshub.view;
 
 import android.content.Context;
 import android.database.DataSetObserver;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 
 import org.gnuton.newshub.R;
 import org.gnuton.newshub.adapters.ImageAdapter;
 import org.gnuton.newshub.utils.MyApp;
 
 public class ArticleImageView extends ImageView{
     private observer mObserver;
     private ImageAdapter mAdapter;
     private final static String TAG = ArticleImageView.class.getName();
 
     public ArticleImageView(Context context, AttributeSet attrs) {
         super(context, attrs);
         mObserver = new observer();
     }
 
     public void setAdapter(ImageAdapter adapter){
 
         //Unregister previous adapter
         if (mAdapter!= null)
             mAdapter.unregisterDataSetObserver(mObserver);
 
         //register new one
         mAdapter = adapter;
         if (mAdapter != null)
             mAdapter.registerDataSetObserver(mObserver);
     }
 
     private class observer extends DataSetObserver{
         @Override
         public void onChanged(){
             Log.d(TAG, "onChanged");
             Drawable repr = mAdapter.getRepresentativeDrawable();
             if (repr == null){
                 repr = mAdapter.getDefaultDrawable();
             } else {
                 scheduleAnimationForView();
             }
             setImageDrawable(repr);
         }
     }
 
     private void scheduleAnimationForView(){
         Animation fadeInAnimation = AnimationUtils.loadAnimation(MyApp.getContext(), R.animator.fadein);
         setAnimation(fadeInAnimation);
     }
 }
