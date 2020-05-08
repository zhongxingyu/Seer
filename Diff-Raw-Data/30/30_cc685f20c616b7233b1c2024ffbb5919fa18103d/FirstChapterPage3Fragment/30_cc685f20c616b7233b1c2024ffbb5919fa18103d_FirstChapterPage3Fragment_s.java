package com.protech.ascension.chapter1;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import com.protech.ascension.gui.PageFragment;
 import com.protech.ascension.R;
 import com.protech.ascension.gui.TouchImageView2;
 
 /**
  * Created with IntelliJ IDEA.
  * User: BNengel
  * Date: 5/10/12
  * Time: 5:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class FirstChapterPage3Fragment extends PageFragment {
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.ch1_page3, container, false);
         return rootView;
     }
 
     @Override
     protected int getTouchMode() {
         return TouchImageView2.NONE;
     }
 
     @Override
     protected int getCaptionListId() {
         return R.array.ch1_pg3_caption_list;
     }
 }
