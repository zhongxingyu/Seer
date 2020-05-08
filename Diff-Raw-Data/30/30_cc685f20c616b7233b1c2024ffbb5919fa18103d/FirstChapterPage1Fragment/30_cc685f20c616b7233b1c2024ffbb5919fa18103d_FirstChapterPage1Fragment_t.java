package com.protech.ascension.gui.chapter1;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import com.protech.ascension.gui.PageFragment;
 import com.protech.ascension.R;
 import com.protech.ascension.gui.TouchImageView2;
 import com.protech.ascension.util.BitmapWorkerTask;
 
 /**
  * Created with IntelliJ IDEA.
  * User: BNengel
  * Date: 5/10/12
  * Time: 5:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class FirstChapterPage1Fragment extends PageFragment {
     TouchImageView2 view;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.ch1_page1, container, false);
 
 //        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.image8);
         view = ((TouchImageView2) rootView.findViewById(R.id.touch_view));
 
 //        view.setImageBitmap(
 //                decodeSampledBitmapFromResource(
 //                                getResources(), R.drawable.image8, 600, 600)
 //        );
         BitmapWorkerTask task = new BitmapWorkerTask(getResources(), view);
         task.doInBackground(R.drawable.image8);
         return rootView;
     }
 
     @Override
     protected int getTouchMode() {
         return view.getMode();
     }
 
     @Override
     protected int getCaptionListId() {
         return R.array.ch1_pg1_caption_list;
     }
 }
