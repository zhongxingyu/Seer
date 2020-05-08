 /**
  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  This code is licensed
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  */
 package edu.rit.csh.androidwebnews;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Rect;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.animation.TranslateAnimation;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 
 import java.util.ArrayList;
 
 
 //Code is from Scirocco on Stack Overflow
 //http://stackoverflow.com/questions/11234375/how-did-google-manage-to-do-this-slide-actionbar-in-android-application
 
 /**
  * Displays a ListView on the left side of the screen
  * that can be hidden.
  *
  * @author Scirocco, modified by Derek Gonyeo
  */
 public class NewsgroupListMenu {
 
     public static boolean menuShown = false;
     private static View menu;
     private static LinearLayout content;
     private static FrameLayout parent;
     private static int menuSize;
     private static int statusHeight = 0;
     private final Activity act;
     private ArrayList<Newsgroup> newsgroupList;
     private final NewsgroupsListAdapter newsgroupAdapter;
 
     NewsgroupListMenu(Activity act) {
         this.act = act;
         newsgroupList = new ArrayList<Newsgroup>();
         newsgroupAdapter = new NewsgroupsListAdapter(act, newsgroupList);
     }
 
     //call this in your onCreate() for screen rotation
     public void checkEnabled() {
         if (menuShown)
             this.show(false);
     }
 
     public void show() {
         //get the height of the status bar
         if (statusHeight == 0) {
             Rect rectgle = new Rect();
             Window window = act.getWindow();
             window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
             statusHeight = rectgle.top;
         }
         this.show(true);
     }
 
     public void update(ArrayList<Newsgroup> newsgroupList) {
         this.newsgroupList = newsgroupList;
         newsgroupAdapter.clear();
         newsgroupAdapter.addAll(newsgroupList);
         newsgroupAdapter.notifyDataSetChanged();
     }
 
     void show(boolean animate) {
         menuSize = dpToPx(250, act);

        if (act.findViewById(R.id.recent_fragment) == null) {
            content = ((LinearLayout) act.findViewById(R.id.threadsfragment).getParent());
        } else {
            content = ((LinearLayout) act.findViewById(R.id.recent_fragment).getParent());
        }
         FrameLayout.LayoutParams parm = (FrameLayout.LayoutParams) content.getLayoutParams();
         parm.gravity = Gravity.TOP;
         statusHeight = 0;
         parm.setMargins(menuSize, 0, -menuSize, 0);
         content.setLayoutParams(parm);
         //animation for smooth slide-out
         TranslateAnimation ta = new TranslateAnimation(-menuSize, 0, 0, 0);
         ta.setDuration(500);
         if (animate)
             content.startAnimation(ta);
         parent = (FrameLayout) content.getParent();
         LayoutInflater inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         menu = inflater.inflate(R.layout.menu, null);
         FrameLayout.LayoutParams lays = new FrameLayout.LayoutParams(-1, -1, 3);
         // lays.setMargins(0, -35, 0, 0);
 
         menu.setLayoutParams(lays);
         parent.addView(menu);
         ListView list = (ListView) act.findViewById(R.id.menu_listview);
         //list.setBackgroundResource(R.drawable.shadow);
         list.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 //handle your menu-click
             }
         });
         if (animate)
             menu.startAnimation(ta);
         menu.findViewById(R.id.overlay).setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 NewsgroupListMenu.this.hide();
             }
         });
         enableDisableViewGroup((LinearLayout) parent.findViewById(android.R.id.content).getParent(), false);
         //((ExtendedViewPager) act.findViewById(R.id.viewpager)).setPagingEnabled(false);
         //((ExtendedPagerTabStrip) act.findViewById(R.id.viewpager_tabs)).setNavEnabled(false);
         menuShown = true;
         this.fill();
     }
 
     void fill() {
         ListView list = (ListView) act.findViewById(R.id.menu_listview);
         list.setOnItemClickListener(new OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> adapter, View arg1, int position,
                                     long id) {
                 Newsgroup newsgroup = (Newsgroup) adapter.getItemAtPosition(position);
                 String value = newsgroup.getName();
                 ((BaseActivity) act).onNewsgroupSelected(value);
                 hide();
             }
 
         });
 
         list.setAdapter(newsgroupAdapter);
     }
 
     void hide() {
         TranslateAnimation ta = new TranslateAnimation(0, -menuSize, 0, 0);
         ta.setDuration(500);
         menu.startAnimation(ta);
         parent.removeView(menu);
 
         TranslateAnimation tra = new TranslateAnimation(menuSize, 0, 0, 0);
         tra.setDuration(500);
         content.startAnimation(tra);
         FrameLayout.LayoutParams parm = (FrameLayout.LayoutParams) content.getLayoutParams();
         parm.setMargins(0, 0, 0, 0);
         content.setLayoutParams(parm);
         enableDisableViewGroup((LinearLayout) parent.findViewById(android.R.id.content).getParent(), true);
         //((ExtendedViewPager) act.findViewById(R.id.viewpager)).setPagingEnabled(true);
         //((ExtendedPagerTabStrip) act.findViewById(R.id.viewpager_tabs)).setNavEnabled(true);
         menuShown = false;
     }
 
     private static int dpToPx(int dp, Context ctx) {
         Resources r = ctx.getResources();
         return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
     }
 
     //originally: http://stackoverflow.com/questions/5418510/disable-the-touch-events-for-all-the-views
     //modified for the needs here
     private static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
         int childCount = viewGroup.getChildCount();
         for (int i = 0; i < childCount; i++) {
             View view = viewGroup.getChildAt(i);
             if (view.isFocusable())
                 view.setEnabled(enabled);
             if (view instanceof ViewGroup) {
                 enableDisableViewGroup((ViewGroup) view, enabled);
             } else if (view instanceof ListView) {
                 if (view.isFocusable())
                     view.setEnabled(enabled);
                 ListView listView = (ListView) view;
                 int listChildCount = listView.getChildCount();
                 for (int j = 0; j < listChildCount; j++) {
                     if (view.isFocusable())
                         listView.getChildAt(j).setEnabled(false);
                 }
             }
         }
     }
 }
