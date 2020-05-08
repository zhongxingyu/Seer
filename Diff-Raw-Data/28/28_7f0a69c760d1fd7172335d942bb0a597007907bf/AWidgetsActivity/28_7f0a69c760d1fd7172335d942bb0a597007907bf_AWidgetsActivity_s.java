 /*
  *  This program is a widget library base on Android platform
  *  Copyright (C) 2012  Xu Zhengyang
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package xzy.android.awidgets;
 
 import xzy.android.awidgets.widget.AHorizontalListView;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 /**
  * @author zhengyang.xu
  * @version 0.9
  * @since Mar 27, 2012 1:11:30 PM
  * @project AWidgets
  */
 public class AWidgetsActivity extends Activity {
 
     private AHorizontalListView mHListView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.listviewdemo);
         ScrollView scrollView = (ScrollView) this.findViewById(R.id.scrollview);
 //        scrollView.
         mHListView = (AHorizontalListView) findViewById(R.id.listview);
         TextView textView = new TextView(this);
         textView.setText("footer");
         TextView header = new TextView(this);
         header.layout(0, 0, 200, 200);
         header.setBackgroundColor(Color.RED);
         header.setText("header");
         // mHListView.addHeaderView(header);
         // mHListView.addFooterView(textView);
         // mHListView.addFooterView(header);
         mHListView.setAdapter(mAdapter);
         mHListView.setOnScrollEndListener(new AHorizontalListView.OnScrollEndListener() {
 
             @Override
             public void onScrollEnd() {
                 // Log.i("xzy", "on Scroll End");
             }
 
             @Override
             public void onScrollFinish() {
                 Log.i("xzy", "current is : " + mHListView.getCurrentX());
                 // Log.i("xzy", "on Scroll Finish");
 
             }
         });
         // listview.removeFooterView(textView);
 
     }
 
     private static String[] dataObjects = new String[] {
             "Text #1", "Text #2", "Text #3", "Text #4", "Text #5", "Text #6", "Text #7", "Text #8",
             "Text #9", "Text #10", "Text #11", "Text #12", "Text #13", "Text #14", "Text #15",
             "Text #16", "Text #17", "Text #18", "Text #19", "Text #20", "Text #21", "Text #22",
             "Text #23", "Text #24", "Text #25", "Text #26", "Text #27", "Text #28", "Text #29",
             "Text #30"
     };
 
     private BaseAdapter mAdapter = new BaseAdapter() {
 
        private OnClickListener mOnButtonClicked = new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 AlertDialog.Builder builder = new AlertDialog.Builder(AWidgetsActivity.this);
                 builder.setMessage("hello from " + v);
                 builder.setPositiveButton("Cool", null);
                 builder.show();
                 // mAdapter.notifyDataSetChanged();
                 // mHListView.setSelection(10);
                 // mHListView.setSelectionFromLeft(10, 40);
                mHListView.scrollTo(mHListView.getCurrentX() + 100);
 
             }
         };
 
         @Override
         public int getCount() {
             return dataObjects.length;
         }
 
         @Override
         public Object getItem(int position) {
             return null;
         }
 
         @Override
         public long getItemId(int position) {
             return 0;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             Log.i("xzy", "position: " + position);
             View retval = LayoutInflater.from(AWidgetsActivity.this).inflate(R.layout.viewitem,
                     null);
             TextView title = (TextView) retval.findViewById(R.id.title);
             title.setOnTouchListener(new View.OnTouchListener() {
 
                 @Override
                 public boolean onTouch(View v, MotionEvent event) {
                     switch (event.getAction()) {
                         case MotionEvent.ACTION_DOWN:
                             Log.i("xzy", "-----------down--------------");
                             break;
                         case MotionEvent.ACTION_MOVE:
                             break;
                         case MotionEvent.ACTION_UP:
                             break;
                         default:
                             break;
                     }
 
                     return false;
                 }
             });
             Button button = (Button) retval.findViewById(R.id.clickbutton);
 
             // button.setClickable(false);
            button.setOnClickListener(mOnButtonClicked);
             if (position % 2 == 0) {
                 title.setText(dataObjects[position]);
             } else {
                 title.setText("ABCDEFGHI");
 
             }
             return retval;
         }
 
         @Override
         public int getItemViewType(int position) {
 
             if (position % 2 == 0) {
                 return 0;
             } else {
                 return 1;
             }
         }
 
         @Override
         public int getViewTypeCount() {
             return 2;
         }
 
     };
 
 }
