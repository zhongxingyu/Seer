 package ch.hsr.hsrlunch;
 
 import java.util.ArrayList;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.widget.Toast;
 
 import ch.hsr.hsrlunch.controller.WeekDataSource;
 import ch.hsr.hsrlunch.model.Offer;
 import ch.hsr.hsrlunch.util.DBOpenHelper;
 import ch.hsr.hsrlunch.util.TabPageAdapter;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.coboltforge.slidemenu.SlideMenu;
 import com.coboltforge.slidemenu.SlideMenuInterface.OnSlideMenuItemClickListener;
 import com.viewpagerindicator.TabPageIndicator;
 
 public class MainActivity extends SherlockFragmentActivity implements OnSlideMenuItemClickListener {
 	
 	public final static String OFFER_DAILY_TITLE = "Tagesteller";
 	public final static String OFFER_VEGI_TITLE = "Vegetarisch";
 	public final static String OFFER_WEEK_TITLE = "Wochen-Hit";
 	
     private List<Offer> offerList;
     public static List<WorkDay> dayList;
     public static List<String> tabTitleList;
     
     public static WorkDay selectedDay;
 	
     ViewPager mViewPager;
     TabPageAdapter mAdapter;
     long WEEK_IN_MILLISECONDS = 7 * 24 * 60 * 60 * 1000;
 	
 	private SlideMenu slidemenu;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         getSupportActionBar().setHomeButtonEnabled(true);       
         	
         init();
         
         FragmentPagerAdapter mAdapter = new TabPageAdapter(getSupportFragmentManager());
         mViewPager = (ViewPager) findViewById(R.id.viewpager);
         mViewPager.setAdapter(mAdapter);
         
         TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
         indicator.setViewPager(mViewPager);
         
 		slidemenu = (SlideMenu) findViewById(R.id.slideMenu);
 		slidemenu.init(this, R.menu.slide, this, 333);
 		
 		
 //		slidemenu.setAsShown(); 		
 //		slidemenu.setHeaderImage(getResources().getDrawable(R.drawable.hsrlunch));
     }
     
 	@Override
 	public void onSlideMenuItemClick(int itemId) {
 		switch(itemId) {
 		case R.id.item_one:
 			Toast.makeText(this, "Montag markiert", Toast.LENGTH_SHORT).show();
 			selectedDay = dayList.get(0);
 			break;
 		case R.id.item_two:
 			Toast.makeText(this, "Dienstag markiert", Toast.LENGTH_SHORT).show();
 			selectedDay = dayList.get(1);
 			break;
 		case R.id.item_three:
 			Toast.makeText(this, "Mittwoch markiert", Toast.LENGTH_SHORT).show();
 			selectedDay = dayList.get(2);
 			break;
 		case R.id.item_four:
 			Toast.makeText(this, "Donnerstag markiert", Toast.LENGTH_SHORT).show();
 			selectedDay = dayList.get(3);
 			break;
 		case R.id.item_five:
 			Toast.makeText(this, "Freitag markiert", Toast.LENGTH_SHORT).show();
 			selectedDay = dayList.get(4);
 			break;
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
 		System.out.println("Option Printed!");
 		switch(item.getItemId()) {
 		case android.R.id.home: // this is the app icon of the actionbar
 			slidemenu.show();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	private void init() {
 		
 		Offer m1 = new Offer(0,	"Fischtäbli\nSauce Tatar\nBlattspinat\nSalzkartoffeln", "INT 8.00 EXT 10.60");
 		Offer m2 = new Offer(1, "Gemüseteigtaschen\nTomatensauce\nSalzkartoffeln\nBuntersalat", "INT 8.00 EXT 10.60");
 		Offer m3 = new Offer(2, "Schweinefilet im Speckmantel\nTomatensauce\nBuntersalat", "INT 14.50 EXT 15.50");
         offerList = new ArrayList<Offer>();
         offerList.add(m1);
         offerList.add(m2);
         offerList.add(m3);
         
         GregorianCalendar cal1 = new GregorianCalendar(2012, 10,22);
         GregorianCalendar cal2 = new GregorianCalendar(2012, 10,23);
         GregorianCalendar cal3 = new GregorianCalendar(2012, 10,24);
         GregorianCalendar cal4 = new GregorianCalendar(2012, 10,25);
         GregorianCalendar cal5 = new GregorianCalendar(2012, 10,26);
         
 		dayList = new ArrayList<WorkDay>();
 		dayList.add(new WorkDay(0,new Date(cal1.getTimeInMillis()), offerList));
 		dayList.add(new WorkDay(1,new Date(cal2.getTimeInMillis()), offerList));
 		dayList.add(new WorkDay(2,new Date(cal3.getTimeInMillis()), offerList));
 		dayList.add(new WorkDay(3,new Date(cal4.getTimeInMillis()), offerList));
 		dayList.add(new WorkDay(4,new Date(cal5.getTimeInMillis()), offerList));
 		
         tabTitleList = new ArrayList<String>(3);
         tabTitleList.add("tages");
         tabTitleList.add("vegi");
         tabTitleList.add("woche");
         
         GregorianCalendar cal = new GregorianCalendar();   
         System.out.println("dayselection: "+dayList.get( (cal.get(Calendar.DAY_OF_WEEK)+5) % 7));
         selectedDay = dayList.get(0);
 	} 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
       
         menu.add("refresh").setIcon(R.drawable.ic_menu_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         menu.add("share").setIcon(R.drawable.ic_menu_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         return true;
     }
     
     public boolean DBUpdateNeeded() {
     	long dbage = new WeekDataSource(new DBOpenHelper(this)).getWeekLastUpdate();
     	long actday = new Date().getTime();
     	long difference = 4 * 24 * 60 * 60 * 1000; // Weil der 1.1.1970 ein Donnerstag war
     	
     	
     	if (actday - ((actday+difference)%WEEK_IN_MILLISECONDS) > dbage)
     		return true; // dbage ist aus letzer Woche
     	return false; // dbage ist neuer als der letzte Montag
     		
     }
     
 }
