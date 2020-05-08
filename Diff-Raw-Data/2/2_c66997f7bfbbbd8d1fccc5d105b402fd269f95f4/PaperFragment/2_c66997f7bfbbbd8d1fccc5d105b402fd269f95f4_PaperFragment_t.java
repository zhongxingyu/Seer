 package com.cultureshock.buskingbook.page;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.cultureshock.buskingbook.R;
 import com.cultureshock.buskingbook.component.ViewPagerAdapter;
 import com.cultureshock.buskingbook.main.MainActivity;
 
 public class PaperFragment extends Fragment implements OnClickListener{
     private FragmentActivity mContext;
     private static PaperFragment mInstance;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         return inflater.inflate(R.layout.main_paper, container, false);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         mContext = getActivity();
         mInstance = this;

        mContext.findViewById(R.id.paper_btn_sticker).requestFocus();
     }
 
     public static PaperFragment getInstance() {
         return mInstance;
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
     }
 
     @Override
     public synchronized void onClick(View v) {
     }
 }
