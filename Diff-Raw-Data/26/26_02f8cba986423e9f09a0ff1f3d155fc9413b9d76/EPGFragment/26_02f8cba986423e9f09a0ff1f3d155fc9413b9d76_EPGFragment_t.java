 package de.bennir.DVBViewerController;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
import android.widget.FrameLayout;
 import android.widget.HorizontalScrollView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import de.bennir.DVBViewerController.view.TwoDScrollView;
 
 public class EPGFragment extends Fragment {
     private static final String TAG = EPGFragment.class.toString();
     HorizontalScrollView header;
     ScrollView side;
     RelativeLayout content;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         return inflater.inflate(R.layout.epg_fragment, container, false);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         setHasOptionsMenu(true);
 
         header = (HorizontalScrollView) getActivity().findViewById(R.id.epg_header);
         side = (ScrollView) getActivity().findViewById(R.id.epg_side);
         content = (RelativeLayout) getActivity().findViewById(R.id.epg_content);
 
         header.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View view, MotionEvent motionEvent) {
                 return true;
             }
         });
 
         side.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View view, MotionEvent motionEvent) {
                 return true;
             }
         });
 
         TwoDScrollView container = (TwoDScrollView) getActivity().findViewById(R.id.epg_twodscroll);
         container.setOnScrollChangedCallback(new TwoDScrollView.OnScrollChangedCallback() {
             @Override
             public void onScroll(int l, int t, int oldl, int oldt) {
                 header.scrollTo(l, 0);
                 side.scrollTo(0, t);
             }
         });
 
         // Fake Test Content
         LinearLayout col = (LinearLayout) getActivity().findViewById(R.id.epg_side_col);
 
 
         int width = 100;
         int height = 50;
         float d = getActivity().getResources().getDisplayMetrics().density;
         int pxWidth = Math.round(d * width);
         int pxHeight = Math.round(d * height);
         for (int i = 0; i < 20; i++) {
 
            int top = (int) (d * (i * height)) + 5 + (i*10);
             for (int j = 0; j < 10; j++) {
                 RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(pxWidth, pxHeight);
                 TextView item = new TextView(getActivity().getApplicationContext());
 
                int left = (int) (d * (j * width)) + 5 + (j*10);
                params.setMargins(left, top, 0, 0);
                 item.setLayoutParams(params);
 
 
                 item.setText("ARD" + i + "-" + j);
                item.setGravity(Gravity.CENTER);
                 item.setBackgroundResource(R.drawable.list_selector);
 
                 item.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         Toast.makeText(getActivity().getApplicationContext(), "Asd", Toast.LENGTH_SHORT).show();
                     }
                 });
 
                 content.addView(item);
             }
 
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, pxHeight);
            params.setMargins(0, 5, 0, 5);

             TextView chan = new TextView(getActivity().getApplicationContext());
            chan.setLayoutParams(params);
             chan.setText("ARD" + i);
             chan.setHeight(pxHeight);
            chan.setGravity(Gravity.CENTER);
             col.addView(chan);
 
         }
 
 
     }
 }
