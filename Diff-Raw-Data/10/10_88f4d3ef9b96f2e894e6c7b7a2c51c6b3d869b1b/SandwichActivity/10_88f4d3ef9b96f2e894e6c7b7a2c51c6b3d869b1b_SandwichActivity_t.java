 package com.mustangexchange.polymeal;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.view.PagerTabStrip;
 import android.support.v4.view.ViewPager;
 import android.support.v4.widget.DrawerLayout;
 import android.view.*;
 import android.widget.*;
 import org.jsoup.Connection;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 
 public class SandwichActivity extends FragmentActivity {
 
     Parser parseHtml;
     Handler uiUpdate= new Handler();
     private ListView lv;
     private TextView moneyView;
     private String money;
     private int tempIndex;
     private ProgressDialog status;
 
     private ViewPager vp;
     private PagerTabStrip myPagerTabStrip;
     private static ArrayList<SandwichActivity.FoodItemAdapter> foodAdapterList = new ArrayList<FoodItemAdapter>();
     public static ActionBar mActionBar;
     public static BigDecimal totalAmount;                   public final Activity activity = this;
 
     private static Context mContext;
 
     private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
     private ActionBarDrawerToggle mDrawerToggle;
     private String[]  mDrawerItems;
     public static Activity mActivity;
     public static boolean clear;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_sandwich);
 
         mContext = this;
         mActivity = this;
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
         mDrawerItems = getResources().getStringArray(R.array.drawerItems);
 
         // set a custom shadow that overlays the main content when the drawer opens
         mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
         // set up the drawer's list view with items and click listener
         mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                 R.layout.drawer_list_item, mDrawerItems));
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
         for(int i = 0;i<MainActivity.sandItems.size();i++)
         {
             if(MainActivity.sandItems.get(i).getNames().size() != 0) //check in case it's not the right time of day
             {
             /* Each of the components of sandItems is passed in INSTEAD of the actual list itself that way we only
                get what is applicable at this time period rather than the whole thing. This prevents
                ArrayOutOfBoundsExceptions later on.
              */
             foodAdapterList.add(new FoodItemAdapter(this, MainActivity.sandItems.get(i).getTitle(), MainActivity.sandItems.get(i).getNames(),
                     MainActivity.sandItems.get(i).getPrices()));
             }
 
         }
 
         vp = (ViewPager) findViewById(R.id.pager);
         vp.setAdapter(new SandwichPagerAdapter(getSupportFragmentManager(), foodAdapterList));
         vp.getAdapter().notifyDataSetChanged();
 
         myPagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
         myPagerTabStrip.setTabIndicatorColor(0xC6930A);
 
         mActionBar = getActionBar();
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
 
     /**
      * When using the ActionBarDrawerToggle, you must call it during
      * onPostCreate() and onConfigurationChanged()...
      */
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         mDrawerToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         // Pass any configuration change to the drawer toggls
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     public void setSubtitleColor() {
         int titleId = Resources.getSystem().getIdentifier("action_bar_subtitle", "id", "android");
         TextView yourTextView = (TextView)findViewById(titleId);
         if(totalAmount.compareTo(BigDecimal.ZERO) < 0)
         {
             yourTextView.setTextColor(Color.RED);
         }
         else
         {
             yourTextView.setTextColor(Color.WHITE);
         }
     }
 
     public void updateBalance() {
         totalAmount = MoneyTime.calcTotalMoney();
         setSubtitleColor();
         mActionBar.setSubtitle("$" + totalAmount + " Remaining");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
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
             case R.id.refresh:
                 status = new ProgressDialog(SandwichActivity.this,ProgressDialog.STYLE_SPINNER);
                 status.setMessage("Downloading and Parsing...");
                 status.setTitle("Refreshing Menu Data");
                 status.setIndeterminate(true);
                 new Thread(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             uiUpdate.post(new Runnable() {
                                 @Override
                                 public void run() {
                                     status.show();
                                 }
                             });
                             Connection one = Jsoup.connect("http://dining.calpoly.edu/menus/?lid=1014&name=VG%20Cafe").timeout(10000);
                             Connection two = Jsoup.connect("http://dining.calpoly.edu/menus/?lid=1012&name=Sandwich%20Factory").timeout(10000);
                             Document docVg = one.get();
                             try
                             {
                                 parseHtml = new Parser(MainActivity.vgItems);
                                 parseHtml.parse(docVg,false);
                             }
                             catch(Exception e)
                             {
                                 uiUpdate.post(new Runnable() {
                                     @Override
                                     public void run() {
                                         AlertDialog.Builder onErrorConn= new AlertDialog.Builder(SandwichActivity.this);
                                         onErrorConn.setTitle("Error Parsing!");
                                         onErrorConn.setMessage("There was an error parsing menu data. Please relaunch the app and try again. If the issue persists contact the developer.");
                                         onErrorConn.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                             public void onClick(DialogInterface dialog, int button) {
                                                 finish();
                                             }
                                         });
                                         onErrorConn.show();
                                     }
                                 });
                             }
                             Document docSand = two.get();
                             try
                             {
                                 parseHtml = new Parser(MainActivity.sandItems);
                                 parseHtml.parse(docSand,true);
                             }
                             catch(Exception e)
                             {
                                 uiUpdate.post(new Runnable() {
                                     @Override
                                     public void run() {
                                         AlertDialog.Builder onErrorConn= new AlertDialog.Builder(SandwichActivity.this);
                                         onErrorConn.setTitle("Error Parsing!");
                                         onErrorConn.setMessage("There was an error parsing menu data. Please relaunch the app and try again. If the issue persists contact the developer.");
                                         onErrorConn.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                             public void onClick(DialogInterface dialog, int button) {
                                                 finish();
                                             }
                                         });
                                         onErrorConn.show();
                                     }
                                 });
                             }
                         }
                         catch (Exception e){
                             uiUpdate.post(new Runnable() {
                                 @Override
                                 public void run() {
 
                                     AlertDialog.Builder onErrorConn= new AlertDialog.Builder(SandwichActivity.this);
                                     onErrorConn.setTitle("Error Connecting!");
                                     onErrorConn.setMessage("There was an error connecting to the website to download the menu. Please check your data connection and try again.");
                                     onErrorConn.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                         public void onClick(DialogInterface dialog, int button) {
                                             finish();
                                         }
                                     });
                                     onErrorConn.show();
                                 }
                             });
                         }
                         uiUpdate.post(new Runnable() {
                             @Override
                             public void run() {
                                 status.dismiss();
                                 vp.getAdapter().notifyDataSetChanged();
                             }
                         });
                     }
                 }).start();
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
                 Intent intentHome = new Intent(mContext, MainActivity.class);
                 intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intentHome);
             }
             else if(parent.getPositionForView(view)==1)
             {
                 MainActivity.vgOrSand = 2;
                 mDrawerLayout.closeDrawer(mDrawerList);
             }
             else if(parent.getPositionForView(view)==2)
             {
                 MainActivity.vgOrSand = 1;
                 mDrawerLayout.closeDrawer(mDrawerList);
                VistaActivity.clear = true;
                 SandwichActivity.mActivity.finish();
                 Intent intentVG = new Intent(mContext, VistaActivity.class);
                 startActivity(intentVG);
             }
             else if(parent.getPositionForView(view)==3)
             {
                 mDrawerLayout.closeDrawer(mDrawerList);
                 Intent intentCP = new Intent(mContext, CompleteorActivity.class);
                 startActivity(intentCP);
             }
             else
             {
                 Toast.makeText(mContext, "Invalid Selection!", Toast.LENGTH_SHORT);
             }
         }
     }
 
     public class FoodItemAdapter extends BaseAdapter implements View.OnClickListener {
         private Context context;
 
         private ArrayList<String> names;
         private ArrayList<String> prices;
         private String title;
 
         public FoodItemAdapter(Context context, String title, ArrayList<String> names, ArrayList<String> prices) {
             this.context = context;
             this.names = names;
             this.prices = prices;
             this.title = title;
         }
 
         public String getTitle() {
             return title;
         }
 
         public int getCount() {
             return names.size();
         }
 
         public Object getItem(int position) {
             return names.get(position);
         }
 
         public long getItemId(int position) {
             return position;
         }
 
         public View getView(int position, View convertView, ViewGroup viewGroup) {
             //ItemSet entry = setList.get(position);
             if (convertView == null) {
                 LayoutInflater inflater = (LayoutInflater) context
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 convertView = inflater.inflate(R.layout.row_item, null);
             }
             TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
             tvName.setText(names.get(position));
 
             TextView tvPrice = (TextView) convertView.findViewById(R.id.tv_price);
             if(prices.size() != 0)
             {
                 tvPrice.setText("$" + prices.get(position));
             }
 
             //Set the onClick Listener on this button
             ImageButton btnAdd = (ImageButton) convertView.findViewById(R.id.btn_add);
             btnAdd.setFocusableInTouchMode(false);
             btnAdd.setFocusable(false);
             btnAdd.setOnClickListener(this);
             btnAdd.setTag(new Integer(position));
 
             return convertView;
         }
 
         @Override
         public void onClick(View view) {
             final int position = (Integer) view.getTag();
             AlertDialog.Builder onListClick= new AlertDialog.Builder(SandwichActivity.this);
             onListClick.setTitle("Add to Cart?");
             onListClick.setMessage("Would you like to add " + names.get(position) + " to your cart? Price: " + "$" + prices.get(position));
             onListClick.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int button) {
                     //money = boundPrices.get(tempIndex);
                    if (names.get(position).contains("@#$")) {
                         AlertDialog.Builder onYes = new AlertDialog.Builder(SandwichActivity.this);
                         onYes.setTitle("How much?");
                         onYes.setMessage("Estimated Number of Ounces: ");
                         LayoutInflater inflater = SandwichActivity.this.getLayoutInflater();
                         View DialogView = inflater.inflate(R.layout.number_picker, null);
                         final NumberPicker np = (NumberPicker) DialogView.findViewById(R.id.numberPicker);
                         np.setMinValue(1);
                         np.setMaxValue(50);
                         np.setWrapSelectorWheel(false);
                         np.setValue(1);
                         onYes.setView(DialogView);
                         onYes.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int button) {
                                 //MoneyTime.moneySpent = MoneyTime.moneySpent + (np.getValue()*new Double(money));
                                 Cart.add(names.get(position), prices.get(position));
                                 updateBalance();
                             }
                         });
                         onYes.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int button) {
                             }
                         });
                         onYes.show();
                     } else {
                         //MoneyTime.moneySpent = MoneyTime.moneySpent + (new Double(money));
                         Cart.add(names.get(position), prices.get(position));
                         updateBalance();
                     }
                 }
             });
             onListClick.setNegativeButton("No", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int button) {
                 }
             });
             onListClick.show();
         }
     }
 }
