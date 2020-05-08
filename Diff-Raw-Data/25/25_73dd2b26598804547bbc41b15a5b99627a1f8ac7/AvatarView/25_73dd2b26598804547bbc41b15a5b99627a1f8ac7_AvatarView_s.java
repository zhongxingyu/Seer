 package ru.rutube.RutubeFeed.views;
 
 import android.content.Context;
 import android.util.AttributeSet;
 
 import com.android.volley.toolbox.NetworkImageView;
 
 import ru.rutube.RutubeFeed.R;
 
 /**
  * Created by tumbler on 06.11.13.
  */
 public class AvatarView extends NetworkImageView {
     public int[] sStubs;
 
     public AvatarView(Context context) {
         super(context);
         initStubs();
     }
 
     private void initStubs() {
         if (!isInEditMode()) {
             sStubs = new int[]{R.drawable._thumb_default_00,
                      R.drawable._thumb_default_01,
                      R.drawable._thumb_default_02,
                      R.drawable._thumb_default_03,
                      R.drawable._thumb_default_04,
                      R.drawable._thumb_default_05,
                      R.drawable._thumb_default_06,
                      R.drawable._thumb_default_07,
              };
         }
     }
 
     public AvatarView(Context context, AttributeSet attrs) {
         super(context, attrs);
         initStubs();
     }
 
     public AvatarView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         initStubs();
     }
 
     public void setDefaultImageRes(int author_id) {
         super.setDefaultImageResId(sStubs[author_id % 8]);
     }
 }
