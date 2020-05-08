 package at.ac.tuwien.sportmate;
 
 import java.util.ArrayList;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 
 public class MainActivity extends FragmentActivity implements
 ActionBar.TabListener {
 	private static String myTab = "";
 	private final static String TAG = "MainActivity";
 	//private static MenuItem item;
 	//private static MenuItem item2;
 	private static Fragment myCurrentFragment = null;
 	private static ActionBar actionBar;
 
 	private static Tab activityStart;
 	private static Tab start;
 	private static Tab user; // my profile
 	private static boolean showMember; //true if user selects a member from GroupFragment 
 	private static boolean running;
 	private static ArrayList<BoGroupMember> notificationList;
 	public static boolean targetNotified = false;
 
 	private static ViewPager mViewPager;
 
 	AppData data;
 
 	NotificationManager mNotificationManager;
 
 	static AppSectionsPagerAdapter mAppSectionsPagerAdapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// load all weekly data From DB
 		data = AppData.getInstance();
		data.setCurrentMember(DBHandler.getGroupMember(2));
 		data.loadData();
 
 		// check DB for active members and write into appData
 		this.countActiveGroupMembers();
 		//check for Target Changes
 		this.checkForTargetChanges();
 
 		notificationList = new ArrayList<BoGroupMember>();
 		
 		mNotificationManager = (NotificationManager) this
 				.getSystemService(Context.NOTIFICATION_SERVICE);
 
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections
 		// of the app.
 		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		// setup action bar for tabs
 		actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setDisplayShowTitleEnabled(false);
 		actionBar.setDisplayShowHomeEnabled(false);
 
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mAppSectionsPagerAdapter);
 		mViewPager
 		.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				// When swiping between pages, select the
 				// corresponding tab.
 				getActionBar().setSelectedNavigationItem(position);
 			}
 		});
 		
 		//This means that the ViewPager will keep the Fragments in memory as long as they are less than three tabs away from the currently selected one
 		mViewPager.setOffscreenPageLimit(4);
 
 		start = actionBar.newTab().setText("Start").setTabListener(this);
 		actionBar.addTab(start);
 
 		user = actionBar.newTab().setText("Profil").setTabListener(this);
 		actionBar.addTab(user);
 
 		Tab tab = actionBar.newTab().setText("Gruppe").setTabListener(this);
 		actionBar.addTab(tab);
 		
 		tab = actionBar.newTab().setText("Hilfe").setTabListener(this);
 		actionBar.addTab(tab);
 
 
 		myTab = "Start";
 
 		/*
 		 * tab = actionBar .newTab() .setText("Target") .setTabListener( new
 		 * MyTabListener<SelectTargetFragment>(this, "Target",
 		 * SelectTargetFragment.class)); actionBar.addTab(tab);
 		 */
 		
 		//Alles LEDs ausschalten
 		SportMateApplication.getApplication().setGroupNotificatinoLight(0);
 		SportMateApplication.getApplication().setMyNotificatinoLight(0);
 		SportMateApplication.getApplication().setProgresses(0, 0);
 
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, switch to the corresponding page in
 		// the ViewPager.
 		mViewPager.setCurrentItem(tab.getPosition());
 
 		// MenuItem handling
 		myTab = tab.getText().toString();
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	public static void selectUser() {
 		showMember = true;
 		SingleStatistic single = (SingleStatistic) mAppSectionsPagerAdapter
 				.getActiveFragment(mViewPager, 1);
 		single.updateView();
 		mViewPager.setCurrentItem(1);
 	}
 	
 	public static void startActivity()
 	{
 		StartFragment start = (StartFragment) mAppSectionsPagerAdapter
 				.getActiveFragment(mViewPager, 0);
 		mViewPager.setCurrentItem(0);
 		start.startActivity();
 	}
 	
 	public static void updateAllViews(){
 		
 		AppData.getInstance().loadData();
 		
 		SingleStatistic singleFragement = (SingleStatistic) mAppSectionsPagerAdapter
 			.getActiveFragment(mViewPager, 1);
 		if(singleFragement != null){
 			singleFragement.updateView();
 		}
 		
 		GroupFragment groupFragment = (GroupFragment) mAppSectionsPagerAdapter
 			.getActiveFragment(mViewPager, 2);
 		if(groupFragment != null){
 			groupFragment.updateView();
 			}
 	}
 
 	public static void showActivityStart() {
 		actionBar.selectTab(activityStart);
 	}
 
 	public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
 		FragmentManager mFragmentManager;
 
 		public AppSectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 			mFragmentManager = fm;
 		}
 
 		public FragmentManager getFragmentManager() {
 			return mFragmentManager;
 		}
 
 		public android.support.v4.app.Fragment getItem(int i) {
 			switch (i) {
 			case 0:
 				return new StartFragment();
 			case 1:
 				return new SingleStatistic();
 			case 2:
 				return new GroupFragment();
 			case 3:
 				return new HelpFragment();
 			default:
 				return new StartFragment();
 			}
 		}
 
 		@Override
 		public int getCount() {
 			return 4;
 		}
 
 		public Fragment getActiveFragment(ViewPager container, int position) {
 			String name = makeFragmentName(container.getId(), position);
 			return mFragmentManager.findFragmentByTag(name);
 		}
 
 		private static String makeFragmentName(int viewId, int index) {
 			return "android:switcher:" + viewId + ":" + index;
 		}
 	}
 
 	public void countActiveGroupMembers() {
 
 		Runnable runnable = new Runnable() 
 		{
 			@Override
 			public void run() {
 				while (true) {
 
 					// da DB-fkt aufrufen
 					BoGroupMember member = AppData.getInstance()
 							.getCurrentMember();
 					if (member != null) {
 						//System.out.println(member.toString());
 
 						ArrayList<BoGroupMember> activeMembers = DBHandler.getActiveGroupMembers(
 								member.user_id, member.group_id);
 
 						boolean notify = false;
 						if (activeMembers.size() != AppData.getInstance()
 								.getActiveGroupMemberCount() && activeMembers.size()>0) {
 							notify = true;
 						}
 
 						if (activeMembers.size() == 0) 
 						{
 							mNotificationManager.cancel(1);
 							SportMateApplication.getApplication().setGroupNotificatinoLight(0);
 						}
 
 						AppData.getInstance().setActiveMembers(activeMembers);
 						AppData.getInstance().setActiveGroupMemberCount(activeMembers.size());
 
 						if (notify) 
 						{
 							showNotificationForActiveMembers();
 							SportMateApplication.getApplication().setGroupNotificatinoLight(1);
 							
 						}
 
 
 
 					}
 
 					try {
 						// refresh intervall
 						Thread.sleep(5000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 
 			}
 		};
 		new Thread(runnable).start();
 
 	}
 	
 	public void checkForTargetChanges() {
 
 		Runnable runnable = new Runnable() 
 		{
 			@Override
 			public void run() {
 				while (!targetNotified) 
 				{
 					ArrayList<BoGroupMember> group = AppData.getInstance().getCurrentGroup().groupMembers;
 					for (int i = 0; i < group.size(); i++)
 					{
 						if(group.get(i).user_id != AppData.getInstance().getCurrentMember().getUser_id())
 						{
 							group.get(i).weeklyTargets = DBHandler.getWeeklyTargetsFromUser(group.get(i).user_id);
 							if(group.get(i).seeIfWeeklyTargetChanged())
 							{
 								notificationList.add(group.get(i));
 							}
 						}
 					}
 					showNotificationForTargetChanges();
 
 					try {
 						// refresh intervall
 						Thread.sleep(5000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					targetNotified = true;
 				}
 
 			}
 		};
 		new Thread(runnable).start();
 
 	}
 
 	public void showNotificationForActiveMembers() {
 		int mId = 1;
 
 		String s = "";
 		for(BoGroupMember m: AppData.getInstance().getActiveMembers()){
 			s += " " + m.user_name;
 		}
 		if (AppData.getInstance().getActiveMembers().size()>1){
 			s += " machen gerade Sport";
 		} else {
 			s += " macht gerade Sport";
 		}
 
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
 				this)
 		.setSmallIcon(R.drawable.logo_app)
 		.setContentTitle(s)
 		.setContentText("Mach jetzt mit und sicher dir Bonuspunkte");
 
 		// Creates an explicit intent for an Activity in your app
 		Intent resultIntent = new Intent(this, MainActivity.class);
 
 		// The stack builder object will contain an artificial back stack for
 		// the
 		// started Activity.
 		// This ensures that navigating backward from the Activity leads out of
 		// your application to the Home screen.
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 		// Adds the back stack for the Intent (but not the Intent itself)
 		stackBuilder.addParentStack(MainActivity.class);
 		// Adds the Intent that starts the Activity to the top of the stack
 		stackBuilder.addNextIntent(resultIntent);
 		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
 		PendingIntent.FLAG_UPDATE_CURRENT);
 		//mBuilder.setContentIntent(resultPendingIntent);
 
 
 
 		// added:
 		Notification notification = mBuilder.build();
 		// Will show lights and make the notification disappear when the presses
 		// it
 		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
 
 		// mId allows you to update the notification later on.
 		mNotificationManager.notify(mId, notification);
 
 	}
 	
 	public void showNotificationForTargetChanges() 
 	{
 		int mId = 2;
 		String s = "";
 		if(notificationList.size() == 0)
 		{
 			return;
 		}
 		
 		if (notificationList.size() >= 1)
 		{
 			s = notificationList.get(0).getUser_name() + " hat sein Ziel auf " + notificationList.get(0).getWeeklyTargetMinutes() + " gendert"; 
 		}
 		if (notificationList.size() > 1)
 		{
 			for(int i = 1; i < notificationList.size(); i++)
 			{// maxi  =.. 
 				s += ", " + notificationList.get(i).getUser_name() + " hat sein Ziel auf " + notificationList.get(i).getWeeklyTargetMinutes() + " gendert,"; 
 			}
 		} 
 
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
 				this)
 		.setSmallIcon(R.drawable.logo_app)
 		.setContentTitle("Jemand hat sein Wochenziel gendert")
 		.setContentText(s);
 
 		// Creates an explicit intent for an Activity in your app
 		Intent resultIntent = new Intent(this, MainActivity.class);
 
 		// The stack builder object will contain an artificial back stack for
 		// the
 		// started Activity.
 		// This ensures that navigating backward from the Activity leads out of
 		// your application to the Home screen.
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 		// Adds the back stack for the Intent (but not the Intent itself)
 		stackBuilder.addParentStack(MainActivity.class);
 		// Adds the Intent that starts the Activity to the top of the stack
 		stackBuilder.addNextIntent(resultIntent);
 		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
 		PendingIntent.FLAG_UPDATE_CURRENT);
 		//mBuilder.setContentIntent(resultPendingIntent);
 
 
 
 		// added:
 		Notification notification = mBuilder.build();
 		// Will show lights and make the notification disappear when the presses
 		// it
 		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
 
 		// mId allows you to update the notification later on.
 		mNotificationManager.notify(mId, notification);
 
 	}
 	
 	@Override
 	public void onStop(){
 		DBHandler.setActive(AppData.getInstance().getCurrentMember().getUser_id(), 0);
 		super.onStop();
 		
 	}
 }
