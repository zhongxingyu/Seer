 package com.mobileobservinglog.tests;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.Instrumentation;
 import android.app.ActivityManager.RunningTaskInfo;
 import android.test.SingleLaunchActivityTestCase;
 import android.util.Log;
 import android.widget.Button;
 
 import com.mobileobservinglog.HomeScreen;
 import com.mobileobservinglog.SettingsContainer;
 import com.mobileobservinglog.R;
 
 public class HomeScreenTests extends SingleLaunchActivityTestCase<HomeScreen>{
 
 	HomeScreen mAut = null;
 	Instrumentation mInstrumentation = null;
 	SettingsContainer mSettings = null;
 	
 	public HomeScreenTests(){
 		super("com.mobileobservinglog", HomeScreen.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception{
 		super.setUp();
 		mAut = getActivity();
 		mInstrumentation = getInstrumentation();
 		mSettings = SettingsContainer.getSettingsContainer();
 	}
 	
 	@Override
 	protected void tearDown() throws Exception{
 		super.tearDown();
 		mAut.finish();
 	}
 	
 	/**
 	 * this test will press the View Catalogs button and check to see that the screen displays
 	 * 
 	 * @throws Throwable
 	 */
 	public void testPressViewCatalogs() throws Throwable
 	{
 		Log.d("JoeTest", "Find the View Catalog Button");
 		final Button viewCatalogs = (Button)mAut.findViewById(R.id.navToCatalogsButton);
 		
 		//Set up the activity manager
 		final ActivityManager am = (ActivityManager)mAut.getApplication().getSystemService(Activity.ACTIVITY_SERVICE);
     	
     	Log.d("JoeTest", "Starting testPressViewCatalogs");
 		runTestOnUiThread(new Runnable()
 		{
 			public void run()
 			{
 				Log.d("JoeTest", "Inside run()");
 	        	
 	        	//Press the button
 				Log.d("JoeTest", "Clicking the view catalogs button");
 				viewCatalogs.performClick();
 				
 				//sleep so the activity can launch
 				try
 				{
 					Thread.sleep(2000);
 				}
 				catch (InterruptedException e)
 				{
 					Log.d("JoeTest", "Caught interrupted exception");
 				}
 			}
 		});
 		
 		//Check for the screen display
 		List<RunningTaskInfo> tasks = am.getRunningTasks(3);
 		Log.d("JoeTest", "Top task is " + tasks.get(0).topActivity.toString());
		assertEquals("Home screen was not at the top of the stack", com.mobileobservinglog.CatalogsScreen.class.toString(), "class " + tasks.get(0).topActivity.getClassName().toString());
 	}
 	
 	/**
 	 * this test will press the Target Lists button and check to see that the screen displays
 	 * 
 	 * @throws Throwable
 	 */
 	public void testPressTargetLists() throws Throwable
 	{
 		Log.d("JoeTest", "Find the Target Lists Button");
 		final Button targetLists = (Button)mAut.findViewById(R.id.navToTargetsButton);
 		
 		//Set up the activity manager
 		final ActivityManager am = (ActivityManager)mAut.getApplication().getSystemService(Activity.ACTIVITY_SERVICE);
     	
     	Log.d("JoeTest", "Starting testPressTargetLists");
 		runTestOnUiThread(new Runnable()
 		{
 			public void run()
 			{
 				Log.d("JoeTest", "Inside run()");
 
 				//Press the button
 				Log.d("JoeTest", "Clicking the Target Lists button");
 				targetLists.performClick();
 				
 				//sleep to let the activity launch
 				try
 				{
 					Thread.sleep(2000);
 				}
 				catch (InterruptedException e)
 				{
 					Log.d("JoeTest", "Caught interrupted exception");
 				}
 			}
 		});
 		
 		//Check for the screen display
 		List<RunningTaskInfo> tasks = am.getRunningTasks(3);
 		Log.d("JoeTest", "Top task is " + tasks.get(0).topActivity.toString());
 		assertEquals("Home screen was not at the top of the stack", com.mobileobservinglog.TargetListsScreen.class.toString(), "class " + tasks.get(0).topActivity.getClassName().toString());
 	}
 	
 	/**
 	 * this test will press the Add Catalogs button and check to see that the screen displays
 	 * 
 	 * @throws Throwable
 	 */
 	public void testPressAddCatalogs() throws Throwable
 	{
 		Log.d("JoeTest", "Find the Add Catalog Button");
 		final Button addCatalogs = (Button)mAut.findViewById(R.id.navToAddCatalogsButton);
 		
 		//Set up the activity manager
 		final ActivityManager am = (ActivityManager)mAut.getApplication().getSystemService(Activity.ACTIVITY_SERVICE);
     	
     	Log.d("JoeTest", "Starting testPressAddCatalogs");
 		runTestOnUiThread(new Runnable()
 		{
 			public void run()
 			{
 				Log.d("JoeTest", "Inside run()");
 
 				//Press the normal mode button
 				Log.d("JoeTest", "Clicking the add catalogs button");
 				addCatalogs.performClick();
 				
 				//sleep so the activity can launch
 				try
 				{
 					Thread.sleep(2000);
 				}
 				catch (InterruptedException e)
 				{
 					Log.d("JoeTest", "Caught interrupted exception");
 				}
 			}
 		});
 		
 		//Check for the screen display
 		List<RunningTaskInfo> tasks = am.getRunningTasks(3);
 		Log.d("JoeTest", "Top task is " + tasks.get(0).topActivity.toString());
 		assertEquals("Home screen was not at the top of the stack", com.mobileobservinglog.AddCatalogsScreen.class.toString(), "class " + tasks.get(0).topActivity.getClassName().toString());
 	}
 	
 	/**
 	 * this test will press the Backup/Restore button and check to see that the screen displays
 	 * 
 	 * @throws Throwable
 	 */
 	public void testPressBackupRestore() throws Throwable
 	{
 		Log.d("JoeTest", "Find the Backup/Restore Button");
 		final Button backupRestore = (Button)mAut.findViewById(R.id.navToBackupButton);
 		
 		//Set up the activity manager
 		final ActivityManager am = (ActivityManager)mAut.getApplication().getSystemService(Activity.ACTIVITY_SERVICE);
     	
     	Log.d("JoeTest", "Starting testPressBackupRestore");
 		runTestOnUiThread(new Runnable()
 		{
 			public void run()
 			{
 				Log.d("JoeTest", "Inside run()");
 
 				//Press the Backup/Restore button
 				Log.d("JoeTest", "Clicking the Backup/Restore button");
 				backupRestore.performClick();
 				
 				//sleep so the activity can launch
 				try
 				{
 					Thread.sleep(2000);
 				}
 				catch (InterruptedException e)
 				{
 					Log.d("JoeTest", "Caught interrupted exception");
 				}
 			}
 		});
 		
 		//Check for the screen display
 		List<RunningTaskInfo> tasks = am.getRunningTasks(3);
 		Log.d("JoeTest", "Top task is " + tasks.get(0).topActivity.toString());
 		assertEquals("Home screen was not at the top of the stack", com.mobileobservinglog.BackupRestoreScreen.class.toString(), "class " + tasks.get(0).topActivity.getClassName().toString());
 	}
 	
 	/**
 	 * this test will press the Settings button and check to see that the screen displays
 	 * 
 	 * @throws Throwable
 	 */
 	public void testPressSettings() throws Throwable
 	{
 		Log.d("JoeTest", "Find the Settings Button");
 		final Button settings = (Button)mAut.findViewById(R.id.navToSettingsButton);
 		
 		//Set up the activity manager
 		final ActivityManager am = (ActivityManager)mAut.getApplication().getSystemService(Activity.ACTIVITY_SERVICE);
     	
     	Log.d("JoeTest", "Starting testPressSettings");
 		runTestOnUiThread(new Runnable()
 		{
 			public void run()
 			{
 				Log.d("JoeTest", "Inside run()");
 
 				//Press the Settings button
 				Log.d("JoeTest", "Clicking the Settings button");
 				settings.performClick();
 				
 				//sleep so the activity can launch
 				try
 				{
 					Thread.sleep(2000);
 				}
 				catch (InterruptedException e)
 				{
 					Log.d("JoeTest", "Caught interrupted exception");
 				}
 			}
 		});
 		
 		//Check for the screen display
 		List<RunningTaskInfo> tasks = am.getRunningTasks(3);
 		Log.d("JoeTest", "Top task is " + tasks.get(0).topActivity.toString());
 		assertEquals("Home screen was not at the top of the stack", com.mobileobservinglog.SettingsScreen.class.toString(), "class " + tasks.get(0).topActivity.getClassName().toString());
 	}
 }
