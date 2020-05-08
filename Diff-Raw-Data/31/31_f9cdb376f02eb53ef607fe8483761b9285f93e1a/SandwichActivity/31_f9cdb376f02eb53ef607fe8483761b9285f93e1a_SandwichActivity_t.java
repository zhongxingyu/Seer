 package com.mustangexchange.polymeal;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.view.PagerTabStrip;
 import android.support.v4.view.ViewPager;
 import android.support.v4.widget.DrawerLayout;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.google.gson.reflect.TypeToken;
 
 import java.lang.reflect.Type;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class SandwichActivity extends FragmentActivity {
 
     private ViewPager vp;
     private PagerTabStrip myPagerTabStrip;
     private static ArrayList<FoodItemAdapter> foodAdapterList = new ArrayList<FoodItemAdapter>();
     public static ActionBar mActionBar;
     public static BigDecimal totalAmount;
     public static Activity mActivity;
 
     public static Context mContext;
 
     private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
     private ActionBarDrawerToggle mDrawerToggle;
     private String[]  mDrawerItems;
     public static boolean clear;
     private Type gsonType = new TypeToken<ArrayList<ItemSet>>() {}.getType();
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_sandwich);
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
         mDrawerItems = getResources().getStringArray(R.array.drawerItems);
         mContext = this;
         mActivity = this;
         mActionBar = getActionBar();
         // set a custom shadow that overlays the main content when the drawer opens
         mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
         // set up the drawer's list view with items and click listener
         /*mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                 R.layout.drawer_list_item, mDrawerItems));*/
         mDrawerList.setAdapter(new ListViewArrayAdapter(this, new ArrayList(Arrays.asList(mDrawerItems))));
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
         // enable ActionBar app icon to behave as action to toggle nav drawer
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         // ActionBarDrawerToggle ties together the the proper interactions
         // between the sliding drawer and the action bar app icon
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,                  /* host Activity */
                 mDrawerLayout,         /* DrawerLayout object */
                 R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                 R.string.drawer_open,  /* "open drawer" description for accessibility */
                 R.string.drawer_close  /* "close drawer" description for accessibility */
         ) {
             public void onDrawerClosed(View view) {
                 //getActionBar().setTitle(mTitle);
                 //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
 
             public void onDrawerOpened(View drawerView) {
                 //getActionBar().setTitle(mDrawerTitle);
                 //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
         };
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
 
         /* The next couple lines of code dynamically sets up an ArrayList of FoodItemAdapters.
            One for each tab in the ViewPager. FoodItemAdapters are Adapters for the Card ListViews
            of each ViewPager Fragment. It gets passed in with the ViewPager adapter because the ViewPager Adapter
            will draw each fragment. MyPagerAdapter is the single adapter for the ViewPager which also uses a custom
            Fragment inner class called MyFragment.
          */
 
         foodAdapterList.clear();
         System.out.println(ItemSetContainer.sandItems.get(0).size());
         for(int i = 0;i<ItemSetContainer.sandItems.size();i++)
         {
             for(int j = 0;j<ItemSetContainer.sandItems.get(i).size();j++) {
                System.out.println(ItemSetContainer.sandItems.get(i).getItem(j).getName() + " " + ItemSetContainer.sandItems.get(i).getItem(j).getPriceString() + " " + ItemSetContainer.sandItems.get(i).getItem(j).getValid());
             }
         }
         for(int i = 0;i<ItemSetContainer.sandItems.size();i++)
         {
             if(ItemSetContainer.sandItems.get(i).size() != 0) //check in case it's not the right time of day
             {
             /* Each of the components of sandItems is passed in INSTEAD of the actual list itself that way we only
                get what is applicable at this time period rather than the whole thing. This prevents
                ArrayOutOfBoundsExceptions later on.
              */
                 foodAdapterList.add(new FoodItemAdapter(this, ItemSetContainer.sandItems.get(i)));
             }
 
         }
         vp = (ViewPager) findViewById(R.id.pager);
         vp.setAdapter(new PagerAdapter(this, getSupportFragmentManager(), foodAdapterList));
         vp.getAdapter().notifyDataSetChanged();
 
         myPagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
         myPagerTabStrip.setTabIndicatorColor(0xC6930A);
 
         updateBalance();
         if(clear)
         {
             Cart.clear();
         }
     }
 
     public void onResume()
     {
         super.onResume();
         updateBalance();
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState)
     {
         super.onPostCreate(savedInstanceState);
         mDrawerToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig)
     {
         super.onConfigurationChanged(newConfig);
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     public static void setSubtitleColor()
     {
         int titleId = Resources.getSystem().getIdentifier("action_bar_subtitle", "id", "android");
         TextView yourTextView = (TextView) mActivity.findViewById(titleId);
         if(totalAmount.compareTo(BigDecimal.ZERO) < 0)
         {
             yourTextView.setTextColor(Color.RED);
         }
         else
         {
             yourTextView.setTextColor(Color.WHITE);
         }
     }
 
     public static void updateBalance() {
         totalAmount = MoneyTime.calcTotalMoney();
         setSubtitleColor();
         mActionBar.setSubtitle("$" + totalAmount + " Remaining");
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return super.onCreateOptionsMenu(menu);
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         if (mDrawerToggle.onOptionsItemSelected(item)) {
             return true;
         }
         switch (item.getItemId())
         {
             case R.id.cart:
                 Intent intent = new Intent(this, CartActivity.class);
                 intent.putExtra("PARENT", "SandwichActivity.class");
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     /* The click listner for ListView in the navigation drawer */
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             if(parent.getPositionForView(view)==0)
             {
                 mDrawerLayout.closeDrawer(mDrawerList);
                 Thread threadHome = new Thread(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             Thread.sleep(400);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                         Intent intentHome = new Intent(mContext, MainActivity.class);
                         intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                         mContext.startActivity(intentHome);
                     }
                 });
                 threadHome.start();
             }
             else if(parent.getPositionForView(view)==1)
             {
                 MainActivity.vgOrSand = 2;
                 mDrawerLayout.closeDrawer(mDrawerList);
             }
             else if(parent.getPositionForView(view)==2)
             {
                 final Thread threadVG = new Thread(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             Thread.sleep(400);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                         final Intent intentVG = new Intent(mContext, VistaActivity.class);
                         intentVG.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                         mContext.startActivity(intentVG);
                         SandwichActivity.mActivity.finish();
                     }
                 });
                 mDrawerLayout.closeDrawer(mDrawerList);
                 if(Cart.getCart().size()>0)
                 {
                     AlertDialog.Builder notifyClear = new AlertDialog.Builder(mContext);
                     notifyClear.setTitle("Warning!");
                     notifyClear.setMessage("Your cart contains Sandwich Factory items. If you continue the cart will be cleared and these items will be removed. Do you want to continue?");
                     notifyClear.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int button) {
                             Cart.clear();
                             Toast.makeText(mContext,"Cart Cleared!",Toast.LENGTH_SHORT).show();
                             MainActivity.vgOrSand = 1;
                             VistaActivity.clear = true;
                             threadVG.start();
                         }
                     });
                     notifyClear.setNegativeButton("No", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int button) {
                         }
                     });
                     notifyClear.show();
                 }
                 else
                 {
                     MainActivity.vgOrSand = 1;
                     mDrawerLayout.closeDrawer(mDrawerList);
                     VistaActivity.clear = true;
                     threadVG.start();
                 }
             }
             else if(parent.getPositionForView(view)==3)
             {
                 mDrawerLayout.closeDrawer(mDrawerList);
                 final Thread threadCP = new Thread(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             Thread.sleep(400);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                         final Intent intentCP = new Intent(mContext, CompleteorActivity.class);
                         intentCP.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                         mContext.startActivity(intentCP);
                     }
                 });
                 threadCP.start();
             }
             else if(parent.getPositionForView(view)==4)
             {
                 final Thread threadST = new Thread(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             Thread.sleep(400);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                         final Intent intentST = new Intent(mContext, SettingsActivity.class);
                         intentST.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                         mContext.startActivity(intentST);
                     }
                 });
                 mDrawerLayout.closeDrawer(mDrawerList);
                 threadST.start();
             }
             else
             {
                 Toast.makeText(mContext, "Invalid Selection!", Toast.LENGTH_SHORT);
             }
         }
     }
 }
