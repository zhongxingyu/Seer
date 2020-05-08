 /**Copyright (c) 2013 Durgesh Trivedi
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.durgesh.quick.squick;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.OrientationEventListener;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 
 import com.durgesh.R;
 import com.durgesh.pref.SQPrefs;
 import com.durgesh.service.SQService;
 import com.durgesh.util.Constants;
 import com.sileria.android.view.HorzListView;
 import com.sileria.android.view.SlidingTray;
 
 /**
  * Create the LEFT ,BOTTOM, RIGHT and TOP drawer all drawer must be in the same XML file
  * 
  * @author durgesht
  */
 public abstract class SQDrawers extends Activity {
     // Represent on which sqbar is swap and what shortcut it has directdial,directmessage,app or contact
     public int selector;
     protected SQTapListener sqTapListener;
     private LinearLayout leftDrawerContent, rightDrawerContent, topDrawerContent, bottomDrawerContent;
     private CustomAdapter leftDrawerAdapter, rightDrawerAdapter, topDrawerAdapter, bottomDrawerAdapter;
     private List<View> leftAdapterList, rightAdapterList, topAdapterList, bottomAdapterList;
     private FrameLayout leftDrawerContainer, rightDrawerContainer, topDrawerContainer, bottomDrawerContainer;
     private SlidingTray leftDrawer, bottomDrawer, rightDrawer, topDrawer;
     protected ListView leftDraweritemList, rightDraweritemList;
     protected HorzListView topDraweritemList, bottomDraweritemList;
     // Position of the item in the main list which is represent by adapter passed from subclass
     protected View currentItem;
     protected String PREFIX;
     protected int shortcutCount;
     private int tbDrawerWidth;
     private int tbDrawerHeight;
     private int lrDrawerHeight;
     private int lrDrawerWidth;
     private OrientationEventListener sqOrientationListener;
     private int noLeftDrawerItem;
     private int noBottomDrawerItem;
     
     public static int MAXDRAWERITEMS=22;
     public static final int MAXDRAWERITEMSSMALLSCREEN=18;
     public static  int LAGERSCREENITEMCOUNT=11;
     public static final int SMALLSCREENITEMCOUNT=10;
     public static  int NOLRDRAWERITEM  =6;
     public static  int NOTBDRAWERITEM  =5;
     public static final int NOLRDRAWERITEMSMALSCRN  =5;
     public static final int NOTBDRAWERITEMSMALLSCRN  =4;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.drawerscontainer);
         init();
         stopService(new Intent(this, SQService.class));
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         startService(new Intent(this, SQService.class));
     }
 
     /**
      * Fill the drawers the item
      */
     protected void fillAllDrawerItem(ItemClickListener listener, int position) {
         int size = getShortCutsCount();
         int listItem = position != 0 ? position : getCurrentPosition(currentItem);
         for (; listItem <= size; listItem++) {
             if (listItem <= noLeftDrawerItem) {
                 if (leftDrawerContent == null) {
                     initLeftDrawerContent();
                 }
                 setItem(getView(Tag(listItem, leftAdapterList, leftDrawerAdapter)));
             } else if (listItem > noLeftDrawerItem && listItem <= LAGERSCREENITEMCOUNT) {
                 if (bottomDrawerContent == null) {
                     initBottomDrawerContent();
                 }
                 setItem(getView(Tag(listItem, bottomAdapterList, bottomDrawerAdapter)));
             } else if (listItem > LAGERSCREENITEMCOUNT && listItem <= LAGERSCREENITEMCOUNT + noLeftDrawerItem) {
                 if (rightDrawerContent == null) {
                     initRightDrawerContent();
                 }
                 setItem(getView(Tag(listItem, rightAdapterList, rightDrawerAdapter)));
             } else if (listItem > LAGERSCREENITEMCOUNT + noLeftDrawerItem && listItem <= MAXDRAWERITEMS) {
                 if (topDrawerContent == null) {
                     initTopDrawerContent();
                 }
                 setItem(getView(Tag(listItem, topAdapterList, topDrawerAdapter)));
             }
         }
         openDrawer();
         setOnItemListener(listener);
 
     }
 
     private void init() {
         selector = getIntent().getIntExtra(Constants.SUPERQUICK, Constants.DO_NOTHING);
         PREFIX = selector == Constants.PHONE_CALL ? Intent.ACTION_CALL : Intent.ACTION_SENDTO;
         sqTapListener = new SQTapListener(this);
         getShortCutsCount();
         sqOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
             @Override
             public void onOrientationChanged(int orientation) {
                 initOrientation();
             }
         };
         sqOrientationListener.enable();
         initOrientation();
     }
 
     private void initOrientation() {
         WindowManager windowsmanger = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
         Display display = windowsmanger.getDefaultDisplay();
         int screenHeight = display.getHeight();
         
         int screenWidth = display.getWidth();
         
         // change the number of drawer item as per screen size
         if (screenHeight < 600 && screenWidth < 300) {
             NOLRDRAWERITEM = NOLRDRAWERITEMSMALSCRN;
             NOTBDRAWERITEM = NOTBDRAWERITEMSMALLSCRN;
             MAXDRAWERITEMS = MAXDRAWERITEMSSMALLSCREEN;
             LAGERSCREENITEMCOUNT = SMALLSCREENITEMCOUNT;
         }
         if (screenHeight > screenWidth) {
             noLeftDrawerItem = NOLRDRAWERITEM;
             noBottomDrawerItem = NOTBDRAWERITEM;
         } else {
             noLeftDrawerItem = NOTBDRAWERITEM;
             noBottomDrawerItem = NOLRDRAWERITEM;
         }
         tbDrawerWidth = (int) getDrawerHeighWidth(noBottomDrawerItem);
         lrDrawerHeight = (int) getDrawerHeighWidth(noLeftDrawerItem);
         tbDrawerHeight = lrDrawerWidth = getResources().getDimensionPixelSize(R.dimen.drawer_item_size) + 2
                 * getResources().getDimensionPixelSize(R.dimen.drawer_item_padding);
     }
 
     /**
      * Initialize the content of the left drawer
      */
     private void initLeftDrawerContent() {
         leftDrawerContent = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.leftright_drawer_content, null);
         leftAdapterList = new ArrayList<View>();
         leftDrawerAdapter = new CustomAdapter(this, R.layout.drawer_item, leftAdapterList);
         leftDraweritemList = (ListView) leftDrawerContent.findViewById(R.id.leftright_drawer_list);
         leftDraweritemList.setAdapter(leftDrawerAdapter);
         leftDraweritemList.setScrollContainer(false);
 
     }
 
     /**
      * Initialize the content of the right drawer
      */
     private void initRightDrawerContent() {
         rightDrawerContent = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.leftright_drawer_content, null);
         rightAdapterList = new ArrayList<View>();
         rightDrawerAdapter = new CustomAdapter(this, R.layout.drawer_item, rightAdapterList);
         rightDraweritemList = (ListView) rightDrawerContent.findViewById(R.id.leftright_drawer_list);
         rightDraweritemList.setAdapter(rightDrawerAdapter);
     }
 
     /**
      * Initialize the content of the top drawer
      */
     private void initTopDrawerContent() {
         topDrawerContent = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.topbottom_drawer_content, null);
         topAdapterList = new ArrayList<View>();
         topDrawerAdapter = new CustomAdapter(this, R.layout.drawer_item, topAdapterList);
         topDraweritemList = (HorzListView) topDrawerContent.findViewById(R.id.topbottom_drawer_list);
         topDraweritemList.setAdapter(topDrawerAdapter);
     }
 
     /**
      * Initialize the content of the bottom drawer
      */
     private void initBottomDrawerContent() {
         bottomDrawerContent = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.topbottom_drawer_content, null);
         bottomAdapterList = new ArrayList<View>();
         bottomDrawerAdapter = new CustomAdapter(this, R.layout.drawer_item, bottomAdapterList);
         bottomDraweritemList = (HorzListView) bottomDrawerContent.findViewById(R.id.topbottom_drawer_list);
         bottomDraweritemList.setAdapter(bottomDrawerAdapter);
     }
 
     /**
      * @param drawer
      *            drawer id to be layout
      * @param position
      *            position of the drawer on screen
      * @param conten
      *            content layout for the drawer items
      */
     private SlidingTray initDrawer(int position, View drawer) {
         LinearLayout drawerhandle = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.drawerhandle, null);
         SlidingTray slidedrawer = new SlidingTray(this, drawerhandle, drawer, position);
         return slidedrawer;
     }
 
     private void openDrawer() {
         openLefDrawer();
         openRightDrawer();
         openTopDrawer();
         openBottomDrawer();
 
     }
 
     // Left drawer
     private void openLefDrawer() {
         if (leftDrawerContainer == null && leftDrawerContent != null) {
             leftDrawerContainer = (FrameLayout) findViewById(R.id.left_drawer_container);
             FrameLayout.LayoutParams pram = new FrameLayout.LayoutParams(lrDrawerWidth, lrDrawerHeight);
             pram.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
             leftDrawerContainer.setLayoutParams(pram);
             leftDrawer = initDrawer(SlidingTray.LEFT, leftDrawerContent);
             leftDrawerContainer.addView(leftDrawer);
             leftDrawer.animateOpen();
         }
     }
 
     // Right drawer
     private void openRightDrawer() {
         if (rightDrawerContainer == null && rightDrawerContent != null) {
             rightDrawerContainer = (FrameLayout) findViewById(R.id.right_drawer_container);
             FrameLayout.LayoutParams pram = new FrameLayout.LayoutParams(lrDrawerWidth, lrDrawerHeight);
             pram.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
             rightDrawerContainer.setLayoutParams(pram);
             // Currently the drawer not open right to left need some changes to be made in SlidingTray
             // so keeping the drawer direction as LEFT in place of right
             rightDrawer = initDrawer(SlidingTray.LEFT, rightDrawerContent);
             rightDrawerContainer.addView(rightDrawer);
             rightDrawer.animateOpen();
         }
 
     }
 
     // Bottom drawer
     private void openBottomDrawer() {
         if (bottomDrawerContainer == null && bottomDrawerContent != null) {
             bottomDrawerContainer = (FrameLayout) findViewById(R.id.bottom_drawer_container);
             FrameLayout.LayoutParams pram = new FrameLayout.LayoutParams(tbDrawerWidth, tbDrawerHeight);
             pram.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
             bottomDrawerContainer.setLayoutParams(pram);
             bottomDrawer = initDrawer(SlidingTray.TOP, bottomDrawerContent);
             bottomDrawerContainer.addView(bottomDrawer);
             bottomDrawer.animateOpen();
         }
     }
 
     // Top drawer
     private void openTopDrawer() {
         if (topDrawerContainer == null && topDrawerContent != null) {
             topDrawerContainer = (FrameLayout) findViewById(R.id.top_drawer_container);
             FrameLayout.LayoutParams pram = new FrameLayout.LayoutParams(tbDrawerWidth, tbDrawerHeight);
             pram.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
             topDrawerContainer.setLayoutParams(pram);
             // Currently the drawer not open bottom to top need some changes to be made in SlidingTray
             // so keeping the drawer direction as TOP in place of bottom
             topDrawer = initDrawer(SlidingTray.TOP, topDrawerContent);
             topDrawerContainer.addView(topDrawer);
             topDrawer.animateOpen();
         }
     }
 
     /**
      * Custom adapter class to represent an item in a drawer
      * 
      * @author durgesht
      */
     class CustomAdapter extends ArrayAdapter<View> {
         Context context;
         int layoutResourceId;
         List<View> objects = null;
 
         public CustomAdapter(Context context, int textViewResourceId, List<View> objects) {
             super(context, textViewResourceId, objects);
             this.context = context;
             layoutResourceId = textViewResourceId;
             this.objects = objects;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             return objects.get(position);
         }
     }
 
     /**
      * Set the listener for all the list items
      * 
      * @param itemClick
      */
     private void setOnItemListener(ItemClickListener itemClick) {
         if (leftDraweritemList != null) {
             leftDraweritemList.setOnItemClickListener(itemClick);
             leftDraweritemList.setOnItemLongClickListener(itemClick);
         }
         if (rightDraweritemList != null) {
             rightDraweritemList.setOnItemClickListener(itemClick);
             rightDraweritemList.setOnItemLongClickListener(itemClick);
         }
         if (topDraweritemList != null) {
             topDraweritemList.setOnItemClickListener(itemClick);
             topDraweritemList.setOnItemLongClickListener(itemClick);
         }
         if (bottomDraweritemList != null) {
             bottomDraweritemList.setOnItemClickListener(itemClick);
             bottomDraweritemList.setOnItemLongClickListener(itemClick);
         }
 
     }
 
     protected abstract View getView(Object[] tag);
 
     /**
      * Represent a object[] which is stored as a tag to a drawer view item.Store information of its position,list and adapter for its drawer
      * 
      * @param itemno
      *            position of item in the list
      * @param list
      *            in a drawer to which the item belong
      * @param adapter
      *            adapter for the listview to which the item belong
      * @return
      */
     private Object[] Tag(Integer itemno, List<View> list, CustomAdapter adapter) {
         Object itemTag[] = new Object[5];
         itemTag[0] = itemno;
         itemTag[1] = list;
         itemTag[2] = adapter;
         itemTag[3] = null;
         itemTag[4] = null;// hold intent data to start the activity
         // At array position 4 store info for already existing drawer item
         // It will helpful to update a existing item without updating total number of item in the list
         return itemTag;
     }
 
     /**
      * Set the item into the list
      * 
      * @param view
      */
     private void setItem(View view) {
         Object[] itemTag = (Object[]) view.getTag();
         List<View> list = (List<View>) itemTag[1];
         list.add(view);
         CustomAdapter adapter = (CustomAdapter) itemTag[2];
         adapter.notifyDataSetChanged();
 
     }
 
     protected void addItem(ItemClickListener listener, Intent intent) {
         Object[] tag = (Object[]) currentItem.getTag();
         //update the new intent from old 
         tag[4]=intent;
         // notify the change into adapter
         CustomAdapter adapter = (CustomAdapter) tag[2];
         adapter.notifyDataSetChanged();
        if (shortcutCount < MAXDRAWERITEMS && tag[3] == null) {
             SQPrefs.setSharedPreferenceInt(this, PREFIX, shortcutCount + 1);
             // update the add button to new position in the list
             int position = (Integer) tag[0] +1;
             // Represent a existing drawer item help in update a item in drawer
             tag[3] = Constants.DRAWERITEM;
             tag[4] = intent;// hold intent to start a shortcut Activity
             fillAllDrawerItem(listener,position);
         }
     }
 
     /**
      * Get the current position of item in the drawer
      * 
      * @param view
      * @return postion of item
      */
     protected int getCurrentPosition(View view) {
         if (view != null) {
             Object itemTag[] = (Object[]) view.getTag();
             return (Integer) itemTag[0];
         }
         return 1;
     }
 
     /**
      * Interface to be implemented for OnItemLongClickListener and OnItemClickListener
      * 
      * @author durgesht
      */
     interface ItemClickListener extends OnItemLongClickListener, OnItemClickListener {
 
     }
 
     /**
      * Get the total number of shortcut created
      * 
      * @return count
      */
     private int getShortCutsCount() {
         shortcutCount = SQPrefs.getSharedPreferenceAsInt(this, PREFIX, 1);
         return shortcutCount;
     }
 
     /**
      * Set animation for the drawer item
      * 
      * @param view
      */
     protected void setAnimation(View view) {
         TranslateAnimation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                 Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
         anim.setDuration(1500);
         view.startAnimation(anim);
     }
 
     private float getDrawerHeighWidth(int noofItem) {
         WindowManager windowsmanger = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
         Display display = windowsmanger.getDefaultDisplay();
         float drawerHeight = 0;
         int screenHeight;
         if (display.getHeight() > display.getWidth()) {
             screenHeight = display.getHeight();
         } else {
             screenHeight = display.getWidth();
         }
         float itemHeighWidth = 0;
         Resources res = getResources();
         itemHeighWidth = res.getDimensionPixelSize(R.dimen.drawer_item_size) + 3 * res.getDimensionPixelSize(R.dimen.drawer_item_padding);
         for (int item = 0; item < noofItem; item++) {
             if (drawerHeight < screenHeight) {
                 drawerHeight += itemHeighWidth;
             }
         }
         return drawerHeight;
     }
 
     /**
      * Add a default image for the drawer Item
      * 
      * @param view
      *            imageView
      */
     protected void addDefaultImage(View view) {
         ImageView imageView = (ImageView) view.findViewById(R.id.shortcut_item_img);
         imageView.setBackgroundResource(R.drawable.addshortcuts);
         imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
     }
 
 }
