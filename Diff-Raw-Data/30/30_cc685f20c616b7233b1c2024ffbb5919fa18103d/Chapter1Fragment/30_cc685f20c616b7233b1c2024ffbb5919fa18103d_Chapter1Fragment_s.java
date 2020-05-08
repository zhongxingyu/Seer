package com.protech.ascension.chapter1;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import com.protech.ascension.R;
 import com.protech.ascension.gui.TouchImageView2;
 
 /**
  * Created with IntelliJ IDEA.
  * User: BNengel
  * Date: 5/30/12
  * Time: 10:40 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Chapter1Fragment extends Fragment {
     private static final String IMAGE_DATA_EXTRA = "resId";
     private int mPageNum;
     private TouchImageView2 mImageView;
 
     public Chapter1Fragment() { }
 
     public static Chapter1Fragment newInstance(int pageNum) {
         final Chapter1Fragment f = new Chapter1Fragment();
         final Bundle args = new Bundle();
         args.putInt(IMAGE_DATA_EXTRA, pageNum);
         f.setArguments(args);
         return f;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mPageNum = getArguments() != null ? getArguments().getInt(IMAGE_DATA_EXTRA) : -1;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         final View v = inflater.inflate(R.layout.ch1_page1, container, false);
         mImageView = (TouchImageView2) v.findViewById(R.id.touch_view);
         return v;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         if (Chapter1FragmentActivity.class.isInstance(getActivity())) {
             final int resId = Chapter1FragmentActivity.PAGE_LIST[mPageNum];
             // Call out to Chapter1FragmentActivity to load the bitmap in a background thread
             ((Chapter1FragmentActivity) getActivity()).loadBitmap(resId, mImageView);
         }
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         final int resId = Chapter1FragmentActivity.PAGE_LIST[mPageNum];
 //        mImageView.setImageResource(resId);
         Bitmap bm = BitmapFactory.decodeResource(getResources(), resId);
         mImageView.setImageBitmap(bm);
     }
 }
