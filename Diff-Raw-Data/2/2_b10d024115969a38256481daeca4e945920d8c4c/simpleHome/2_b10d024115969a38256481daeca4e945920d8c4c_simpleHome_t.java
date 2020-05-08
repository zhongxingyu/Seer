 package simple.home.jtbuaa;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import base.lib.HanziToPinyin;
 import base.lib.WrapInterstitialAd;
 import base.lib.util;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.WallpaperManager;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.IPackageStatsObserver;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageStats;
 import android.content.pm.ResolveInfo;
 import android.content.res.Configuration;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.ColorFilter;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Parcelable;
 import android.os.RemoteException;
 import android.preference.PreferenceManager;
 import android.provider.CallLog.Calls;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 
 public class simpleHome extends Activity implements SensorEventListener, sizedRelativeLayout.OnResizeChangeListener {
 
 	int homeTab = 1;
 	
 	AlertDialog restartDialog = null;
 	AlertDialog hintDialog = null;
 	
 	//wall paper related
 	String downloadPath;
 	SensorManager sensorMgr;
 	Sensor mSensor;
 	float last_x, last_y, last_z;
 	long lastUpdate, lastSet;
 	ArrayList<String> picList, picList_selected;
 	boolean shakeWallpaper = false;
 	boolean busy;
 	SharedPreferences perferences;
 	SharedPreferences.Editor editor;
 	String wallpaperFile = "";
 	int rotateMode = 1;
 	boolean systemIsGrid = true;
 	boolean userIsGrid = false;
 
 
 	AppAlphaList sysAlphaList, userAlphaList;
 	//alpha list related
     TextView radioText;
     RadioGroup radioGroup;
 
 	//app list related
 	private List<View> mListViews;
 	GridView favoAppList;
 	ListView sysAppList, userAppList, shortAppList;
 	ImageView homeBar, shortBar;
 	String version, myPackageName;
 	ViewPager mainlayout;
 	MyPagerAdapter myPagerAdapter;
 	RelativeLayout home;
 	ResolveInfo appDetail;
 	List<ResolveInfo> mAllApps, mFavoApps, mSysApps, mUserApps, mShortApps;
 	PackageManager pm;
 	favoAppAdapter favoAdapter;
 	shortAppAdapter shortAdapter;
 	ResolveInfo ri_phone, ri_sms, ri_contact;
 	CallObserver callObserver;
 	SmsChangeObserver smsObserver;
 	ImageView shortcut_phone, shortcut_sms, shortcut_contact;
 	sizedRelativeLayout base;
 	RelativeLayout apps;
     appHandler mAppHandler = new appHandler();
 	final static int UPDATE_RI_PHONE = 0, UPDATE_RI_SMS = 1, UPDATE_RI_CONTACT = 2, UPDATE_USER = 3, UPDATE_SPLASH = 4; 
 	ContextMenu mMenu;
 	
 	ricase selected_case;
 	
 	boolean canRoot;
 	
 	DisplayMetrics dm;
 	
 	IBinder token;
 
 	//package size related
 	HashMap<String, Object> packagesSize;
 	Method getPackageSizeInfo;
 	IPackageStatsObserver sizeObserver;
 	static int sizeM = 1024*1024; 
 
 	WallpaperManager mWallpaperManager;
 	
 	class MyPagerAdapter extends PagerAdapter{
 	    @Override
 	    public void destroyItem(View collection, int arg1, Object view) {
 	        ((ViewPager) collection).removeView((View) view);
 	    }
 	    
 	    @Override
 	    public void finishUpdate(View arg0) {
 	    }
 
 	    @Override
 	    public int getCount() {
 	        return mListViews.size();
 	    }
 
 	    @Override
 	    public Object instantiateItem(View arg0, int arg1) {
 	        ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
 	        return mListViews.get(arg1);
 	    }
 
 	    @Override
 	    public boolean isViewFromObject(View arg0, Object arg1) {
 	        return arg0==(arg1);
 	    }
 
 	    @Override
 	    public void restoreState(Parcelable arg0, ClassLoader arg1) {
 	    }
 
 	    @Override
 	    public Parcelable saveState() {
 	        return null;
 	    }
 
 	    @Override
 	    public void startUpdate(View arg0) {
 	    }
 	    
 	    @Override
 	    public int getItemPosition(Object object) {
 	        return POSITION_NONE;
 	    }
 	}
 
 	@Override
 	protected void onResume() {
 		if (sysAlphaList.appToDel != null) {
 			String apkToDel = sysAlphaList.appToDel.activityInfo.applicationInfo.sourceDir;
 			String res = ShellInterface.doExec(new String[] {"ls /data/data/" + sysAlphaList.appToDel.activityInfo.packageName}, true);
 			if (res.contains("No such file or directory")) {//uninstalled
 				String[] cmds = {
 						"rm " + apkToDel + ".bak",
 						"rm " + apkToDel.replace(".apk", ".odex")};
 				//ShellInterface.doExec(cmds);// not really delete.
 			}
 			else ShellInterface.doExec(new String[] {"mv " + apkToDel + ".bak " + apkToDel});
 			
 			sysAlphaList.appToDel = null;
 		}
 		
 		shakeWallpaper = perferences.getBoolean("shake", false);
 		if (shakeWallpaper) { 
 	    	sensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
 	    	busy = false;
 		}
 		else sensorMgr.unregisterListener(this);
 
 		int tmpMode = perferences.getInt("rotate_mode", 1);
 		if (rotateMode != tmpMode) {
 			rotateMode = tmpMode;
 			if (rotateMode == 1) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
 			else if (rotateMode == 2) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 			else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		}
 
 		boolean isGrid = perferences.getBoolean("system", true);
 		if (systemIsGrid != isGrid) {
 			systemIsGrid = isGrid;
 			if (systemIsGrid) ;
 			else ;
 		}
 
 		isGrid = perferences.getBoolean("user", false);
 		if (userIsGrid != isGrid) {
 			userIsGrid = isGrid;
 			if (userIsGrid) ;
 			else ;
 		}
 
 		int m = (int) (5*dm.density);
 		int mTop = (int) (35*dm.density);
 		boolean showTitle = perferences.getBoolean("title", true);
 		if (showTitle) {
 			radioGroup.setVisibility(View.VISIBLE);
 			LayoutParams lp = (LayoutParams) sysAlphaList.AppList.getLayoutParams();
 			lp.setMargins(m, mTop, m, m);
 			lp = (LayoutParams) userAlphaList.AppList.getLayoutParams();
 			lp.setMargins(m, mTop, m, m);
 		}
 		else {
 			radioGroup.setVisibility(View.GONE);
 			LayoutParams lp = (LayoutParams) sysAlphaList.AppList.getLayoutParams();
 			lp.setMargins(m, m, m, m);
 			lp = (LayoutParams) userAlphaList.AppList.getLayoutParams();
 			lp.setMargins(m, m, m, m);
 		}
 
 		boolean showAlpha = perferences.getBoolean("alpha", true);
 		if (showAlpha) {
 			sysAlphaList.AlphaGrid.setVisibility(View.VISIBLE);
 			userAlphaList.AlphaGrid.setVisibility(View.VISIBLE);
 		}
 		else {
 			sysAlphaList.AlphaGrid.setVisibility(View.GONE);
 			userAlphaList.AlphaGrid.setVisibility(View.GONE);
 		}
 
 		super.onResume();
 	}
 	
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	menu.add(0, 0, 0, R.string.wallpaper).setIcon(android.R.drawable.ic_menu_gallery).setAlphabeticShortcut('W');
     	menu.add(0, 1, 0, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences)
     		.setIntent(new Intent(android.provider.Settings.ACTION_SETTINGS));
     	menu.add(0, 2, 0, R.string.help).setIcon(android.R.drawable.ic_menu_help).setAlphabeticShortcut('H');
     	return true;
     }
 	
 	public boolean onOptionsItemSelected(MenuItem item){
 		switch (item.getItemId()) {
 		case 0://wallpaper
 	        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
 	        util.startActivity(Intent.createChooser(pickWallpaper, getString(R.string.wallpaper)), true, getBaseContext());
 			break;
 		case 1://settings
 			return super.onOptionsItemSelected(item);
 		case 2://help dialog
 			Intent intent = new Intent("about");
 			intent.setClassName(getPackageName(), About.class.getName());
 			intent.putExtra("version", version);
 			intent.putExtra("filename", wallpaperFile);
 			util.startActivity(intent, false, getBaseContext());
 			break;
 		}
 		return true;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		selected_case = (ricase) v.getTag();
 		
 		switch (selected_case.mCase) {
 		case 0://on home
 			menu.add(0, 0, 0, getString(R.string.removeFromFavo));
 			break;
 		case 1://on shortcut
 			menu.add(0, 1, 0, getString(R.string.removeFromShort));
 			break;
 		case 2://on app list
 			if (mainlayout.getCurrentItem() == 0) {
 				if (sysAlphaList.mIsGrid)
 					menu.add(0, 8, 0, getString(R.string.list_view));
 				else
 					menu.add(0, 8, 0, getString(R.string.grid_view));
 			}
 			else if (mainlayout.getCurrentItem() == 2) {
 				if (userAlphaList.mIsGrid)
 					menu.add(0, 8, 0, getString(R.string.list_view));
 				else
 					menu.add(0, 8, 0, getString(R.string.grid_view));
 			}
 			menu.add(0, 7, 0, getString(R.string.hideapp));
 			menu.add(0, 4, 0, getString(R.string.appdetail));
 			menu.add(0, 5, 0, getString(R.string.addtoFavo));
 			menu.add(0, 6, 0, getString(R.string.addtoShort));
 			mMenu = menu; 
 			break;
 		}
 	}
 	
 	void writeFile(String name) {
 		try {
 			FileOutputStream fo = this.openFileOutput(name, 0);
 			ObjectOutputStream oos = new ObjectOutputStream(fo);
 			if (name.equals("short")) {
 				for (int i = 0; i < mShortApps.size(); i++)
 					oos.writeObject(((ResolveInfo)mShortApps.get(i)).activityInfo.name);
 			}
 			else if (name.equals("favo")) {
 				for (int i = 0; i < mFavoApps.size(); i++)
 					oos.writeObject(((ResolveInfo)mFavoApps.get(i)).activityInfo.name);
 			}
 			oos.flush();
 			oos.close();
 			fo.close();
 		} catch (Exception e) {}
 	}
 	
 	boolean backup(String sourceDir) {//copy file to sdcard
 		String apk = sourceDir.split("/")[sourceDir.split("/").length-1];
 		FileOutputStream fos;
 		FileInputStream fis;
 		String filename = downloadPath + "apk/" + apk;
 		try {
 			File target = new File(filename);
 			fos = new FileOutputStream(target, false);
 			fis = new FileInputStream(sourceDir);
 			byte buf[] = new byte[10240];
 			int readLength = 0;
         	while((readLength = fis.read(buf))>0){
        			fos.write(buf, 0, readLength);
         	}
 			fos.close();
 			fis.close();
 		} catch (Exception e) {
 			hintDialog.setMessage(e.toString());
 			hintDialog.show();
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean onContextItemSelected(MenuItem item){
 		super.onContextItemSelected(item);
 		ResolveInfo info = null;
 		if (item.getItemId() < 8) info = (ResolveInfo) selected_case.mRi;
 		switch (item.getItemId()) {
 		case 0://remove from home
 			favoAdapter.remove(info);
 			writeFile("favo");
 			break;
 		case 1://remove from shortcut
 			shortAdapter.remove(info);
 			writeFile("short");	//save shortcut to file
 			break;
 		case 4://get app detail info
 			showDetail(info.activityInfo.applicationInfo, info);
 			break;
 		case 5://add to home
 			if (favoAdapter.getPosition(info) < 0) { 
 				favoAdapter.add(info);
 				writeFile("favo");
 			}
 			break;
 		case 6://add to shortcut
 			if (shortAdapter.getPosition(info) < 0) {
 				shortAdapter.insert(info, 0);
 				writeFile("short");	//save shortcut to file
 			}
 			break;
 		case 7://hide the ri
 			if (mainlayout.getCurrentItem() == 0) {
 				sysAlphaList.remove(info.activityInfo.packageName);
 			}
 			else {
 				userAlphaList.remove(info.activityInfo.packageName);
 	    		editor.putString("hide_package", info.activityInfo.packageName);
 	    		editor.commit();
 			}
 			refreshRadioButton();
 			break;
 		case 8://switch view
     		restartDialog.show();
     		return true;//break can't finish on some device?
 		case 9://get package detail info
 			PackageInfo pi = (PackageInfo) selected_case.mRi; 
 			showDetail(pi.applicationInfo, null);
 			break;
 		}
 		return false;
 	}
 
 	void showDetail(final ApplicationInfo info, ResolveInfo aInfo) {
 		final String sourceDir = info.sourceDir;
 		final String label = (String) info.loadLabel(pm);
 		final String packageName = info.packageName;
 		String shortName = packageName;
 		if (aInfo != null) shortName += "/" + new ComponentName(packageName, aInfo.activityInfo.name).getShortClassName();
 		AlertDialog detailDlg = new AlertDialog.Builder(this).
 				setTitle(label).
 				setIcon(info.loadIcon(pm)).
 				setMessage(shortName + "\n\n" + sourceDir).
 				setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 				        Intent intent = new Intent(Intent.ACTION_SEND);
 				        intent.setType("text/plain");  
 				        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
 				        String text = label + getString(R.string.app_share_text) 
 				        	+ "http://bpc.borqs.com/market.html?id=" + packageName;
 			    		intent.putExtra(Intent.EXTRA_TEXT, text);
 			   			util.startActivity(Intent.createChooser(intent, getString(R.string.sharemode)), true, getBaseContext());
 					}
 				}).setNeutralButton(R.string.backapp, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						String apk = sourceDir.split("/")[sourceDir.split("/").length-1];
 						if (backup(sourceDir)) {
 							String odex = sourceDir.replace(".apk", ".odex");
 							File target = new File(odex);
 							boolean backupOdex = true;
 							if (target.exists()) 
 								if (!backup(odex)) backupOdex = false;//backup odex if any
 							
 							if (backupOdex) {
 								hintDialog.setMessage(getString(R.string.backapp) +	" " + 
 										label + " to " + 
 										downloadPath + "apk/" + apk);
 								hintDialog.show();
 							}
 						}
 					}
 				}).
 				setNegativeButton(R.string.more, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface arg0, int arg1) {
 			        	Intent intent;
 						if (appDetail != null) {
 							intent = new Intent(Intent.ACTION_VIEW);
 							intent.setClassName(appDetail.activityInfo.packageName, appDetail.activityInfo.name);
 							intent.putExtra("pkg", packageName);
 							intent.putExtra("com.android.settings.ApplicationPkgName", packageName);
 						}
 						else {//2.6 tahiti change the action.
 							intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", packageName, null));
 						}
 						util.startActivity(intent, true, getBaseContext());
 					}
 				}).create();
 		boolean canBackup = !downloadPath.startsWith(getFilesDir().getPath());
 		detailDlg.show();
 		detailDlg.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(canBackup);//not backup if no SDcard.
 	}
 	
 	WrapInterstitialAd interstitialAd = null;
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         myPackageName = this.getPackageName();
     	pm = getPackageManager();
     	version = util.getVersion(this);
 
         try {
         	getPackageSizeInfo = PackageManager.class.getMethod(
         		    "getPackageSizeInfo", new Class[] {String.class, IPackageStatsObserver.class});
         } catch (Exception e) {
         	e.printStackTrace();
         }
         
     	sizeObserver = new IPackageStatsObserver.Stub() {
 			@Override
 			public void onGetStatsCompleted(PackageStats pStats,
 					boolean succeeded) throws RemoteException {
 				
 				long size = pStats.codeSize;
 		        String ssize = new String();
 		        if (size > 10 * sizeM) 		ssize = size / sizeM + "M";
 		        else if (size > 10 * 1024)	ssize = size / 1024 + "K";
 		        else if (size > 0)			ssize = size + "B";
 		        else 						ssize = "";
 				packagesSize.put(pStats.packageName, ssize);
 			}
 		};
 		packagesSize = new HashMap<String, Object>();
     	
     	sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
     	mSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		perferences = PreferenceManager.getDefaultSharedPreferences(this);
 		editor = perferences.edit();
 
 		dm = new DisplayMetrics();  
 		getWindowManager().getDefaultDisplay().getMetrics(dm);
 		
     	//1,2,3,4 are integer value of small, normal, large and XLARGE screen respectively.
     	//int screen_size = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK; 
     	//if (screen_size < Configuration.SCREENLAYOUT_SIZE_LARGE)//disable auto rotate screen for small and normal screen.
     		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		
     	requestWindowFeature(Window.FEATURE_NO_TITLE); //hide titlebar of application, must be before setting the layout
     	setContentView(R.layout.ads);
     	    	
         home = (RelativeLayout) getLayoutInflater().inflate(R.layout.home, null);
         
         
     	//favorite app tab
     	favoAppList = (GridView) home.findViewById(R.id.favos);
     	favoAppList.setVerticalScrollBarEnabled(false);
     	favoAppList.inflate(this, R.layout.app_list, null);
     	favoAppList.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View arg0, MotionEvent arg1) {
 		    	shortAppList.setVisibility(View.INVISIBLE);
 				return false;
 			}
     	});
         
     	shortAppList = (ListView) home.findViewById(R.id.business);
     	shortAppList.bringToFront();
     	shortAppList.setVisibility(View.INVISIBLE);
 
     	
 		boolean isGrid = perferences.getBoolean("system", true);
     	sysAlphaList = new AppAlphaList(this, pm, packagesSize, isGrid, dm.widthPixels > 480);
     	
 		isGrid = perferences.getBoolean("user", false);
     	userAlphaList = new AppAlphaList(this, pm, packagesSize, isGrid, dm.widthPixels > 480);
     	
     	
     	mListViews = new ArrayList<View>();
         mListViews.add(sysAlphaList.view);
         mListViews.add(home);
         mListViews.add(userAlphaList.view);
 
         radioText = (TextView) findViewById(R.id.radio_text);
         radioGroup = (RadioGroup) findViewById(R.id.radio_hint);
         radioGroup.removeViewAt(0);
         
 		mWallpaperManager = WallpaperManager.getInstance(this);
         
 		if (interstitialAd == null) interstitialAd = new WrapInterstitialAd(this, "a152395b161f6e6", mAppHandler);
 		interstitialAd.loadAd();
         mainlayout = (ViewPager)findViewById(R.id.mainFrame);
         mainlayout.setLongClickable(true);
         myPagerAdapter = new MyPagerAdapter();
         mainlayout.setAdapter(myPagerAdapter);
         mainlayout.setOnPageChangeListener(new OnPageChangeListener() {
 			@Override
 			public void onPageScrollStateChanged(int state) {
 				if (state == ViewPager.SCROLL_STATE_SETTLING) {
 					refreshRadioButton();
 				}
 			}
 
 			int edgePressure = 0;
 			@Override
 			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
 				if (positionOffsetPixels == 0) {
 					if ((position == 0) || (position == myPagerAdapter.getCount()-1))
 						edgePressure += 1;
 					else edgePressure = 0;
 				}
 				else edgePressure = 0;
 				if (edgePressure > 3 && interstitialAd != null) {
 					if (interstitialAd.isReady()) {
 						interstitialAd.show();
 						interstitialAd.loadAd();
 					}
 					else interstitialAd.loadAd();
 				}
 
 				if (!shakeWallpaper) {//don't move wallpaper if change wallpaper by shake
 					if (token == null) token = mainlayout.getWindowToken();//any token from a component is ok
 					mWallpaperManager.setWallpaperOffsets(token, 
 				            //when slide from home to systems or from systems to home, the "position" is 0,
 				            //when slide from home to users or from users to home, it is 1.
 				            //positionOffset is from 0 to 1. sometime it will jump from 1 to 0, we just omit it if it is 0.
 				            //so we can unify it to (0, 1) by (positionOffset+position)/2
 		                    (positionOffset+position)/(mListViews.size()-1), 0);
 				}
 			}
 
 			@Override
 			public void onPageSelected(int arg0) {
 			}
         });
         mainlayout.setCurrentItem(homeTab);
 
 		mSysApps = new ArrayList<ResolveInfo>();
 		mUserApps = new ArrayList<ResolveInfo>();
 		mFavoApps = new ArrayList<ResolveInfo>();
 		mShortApps = new ArrayList<ResolveInfo>();
 		
 		favoAdapter = new favoAppAdapter(getBaseContext(), mFavoApps);
 		favoAppList.setAdapter(favoAdapter);
 		
         base = (sizedRelativeLayout) home.findViewById(R.id.base); 
         base.setResizeListener(this);
         homeBar = (ImageView) home.findViewById(R.id.home_bar);
         homeBar.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent(Intent.ACTION_MAIN);
 				intent.setPackage("harley.browsers");
 				if (!util.startActivity(intent, false, getBaseContext())) {
 					intent.setPackage("easy.browser.classic");
 					if (!util.startActivity(intent, false, getBaseContext())) {
 						intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=harley.browsers"));
 						util.startActivity(intent, true, getBaseContext());
 					}
 				}
 			}
         });
         
         shortBar = (ImageView) home.findViewById(R.id.business_bar);
         shortBar.setOnClickListener(new OnClickListener() {//by click this bar to show/hide mainlayout
 			@Override
 			public void onClick(View arg0) {
 				if ((shortAppList.getVisibility() == View.INVISIBLE) && !mShortApps.isEmpty()) 
 					shortAppList.setVisibility(View.VISIBLE);
 				else shortAppList.setVisibility(View.INVISIBLE);
 			}
         });
         
 		shortcut_phone = (ImageView) home.findViewById(R.id.shortcut_phone);
 		shortcut_sms = (ImageView) home.findViewById(R.id.shortcut_sms);
 		//shortcut_contact = (ImageView) findViewById(R.id.shortcut_contact);
 		
 		//for package add/remove
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
 		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
 		filter.addDataScheme("package");
 		registerReceiver(packageReceiver, filter);
 		
 		//for wall paper changed
 		filter = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);
 		registerReceiver(wallpaperReceiver, filter);
 		
 		filter = new IntentFilter("simpleHome.action.HOME_CHANGED");
         registerReceiver(homeChangeReceiver, filter);
 
 		filter = new IntentFilter("simpleHome.action.PIC_ADDED");
         registerReceiver(picAddReceiver, filter);
         
 		filter = new IntentFilter("simpleHome.action.SHARE_DESKTOP");
         registerReceiver(deskShareReceiver, filter);
         
         filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);  
         filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);  
         filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);  
         filter.addAction(Intent.ACTION_MEDIA_REMOVED);  
         filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);  
         filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);  
         filter.addDataScheme("file");  
         registerReceiver(sdcardListener, filter);  
             	
         apps = (RelativeLayout) findViewById(R.id.apps);
         //mWallpaperManager.setWallpaperOffsets(apps.getWindowToken(), 0.5f, 0);//move wallpaper to center, but null pointer? 
         
     	//TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
     	//getITelephony(tm).getActivePhoneType();
     	
     	ContentResolver cr = getContentResolver();
     	callObserver = new CallObserver(cr, mAppHandler);
     	smsObserver = new SmsChangeObserver(cr, mAppHandler);
     	getContentResolver().registerContentObserver(Calls.CONTENT_URI, true, callObserver);
     	cr.registerContentObserver(Uri.parse("content://mms-sms/"), true, smsObserver);
         
     	//task for init, such as load webview, load package list
 		InitTask initTask = new InitTask();
         initTask.execute("");
         
 		restartDialog = new AlertDialog.Builder(this).
 				setTitle(R.string.app_name).
 				setIcon(R.drawable.icon).
 				setMessage(R.string.restart).
 				setPositiveButton("OK", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						//save the layout
 						if (mainlayout.getCurrentItem() == 0) 
 				    		editor.putBoolean("system", !sysAlphaList.mIsGrid);
 						else if (mainlayout.getCurrentItem() == 2) {
 				    		editor.putBoolean("user", !userAlphaList.mIsGrid);
 				    		editor.putString("hide_package", "");
 						}
 			    		editor.commit();
 			    		
 			    		//restart the activity. note if set singleinstance or singletask of activity, below will not work on some device.
 						Intent intent = getIntent();
 						overridePendingTransition(0, 0);
 						intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
 						finish();
 						overridePendingTransition(0, 0);
 						startActivity(intent);
 					}
 				}).
 				setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				}).create();
 		
 		hintDialog = new AlertDialog.Builder(this).
 				setNegativeButton("OK", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				}).create();
     }
 
     @Override 
     protected void onDestroy() {
     	unregisterReceiver(packageReceiver);
     	unregisterReceiver(wallpaperReceiver);
     	unregisterReceiver(picAddReceiver);
     	unregisterReceiver(deskShareReceiver);
     	unregisterReceiver(homeChangeReceiver);
     	unregisterReceiver(sdcardListener);
     	
     	super.onDestroy();
     }
     
     BroadcastReceiver sdcardListener = new BroadcastReceiver() {  
         @Override  
         public void onReceive(Context context, Intent intent) {  
               
             String action = intent.getAction();  
             if(Intent.ACTION_MEDIA_MOUNTED.equals(action)  
                     || Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)  
                     || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)  
                     ){// mount sd card success
             	if (downloadPath != null) 
             		downloadPath = Environment.getExternalStorageDirectory() + "/simpleHome/"; 
             	if (mMenu != null) mMenu.getItem(3).setEnabled(true);
             } else if(Intent.ACTION_MEDIA_REMOVED.equals(action)  
                     || Intent.ACTION_MEDIA_UNMOUNTED.equals(action)  
                     || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)  
                     ){// fail to mount sd card
             	if (downloadPath != null) 
             		downloadPath = getFilesDir().getPath() + "/";
             	if (mMenu != null) mMenu.getItem(3).setEnabled(false);
             }  
         }  
     };  
     
  	BroadcastReceiver wallpaperReceiver = new BroadcastReceiver() {
  		@Override
  		public void onReceive(Context arg0, Intent arg1) {
  			apps.setBackgroundColor(0);//set back ground to transparent to show wallpaper
  			wallpaperFile = "";
  		}
  	};
  	
     BroadcastReceiver picAddReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             final String picName = intent.getStringExtra("picFile");
 			picList.add(picName);//add to picture list
 			
     		editor.putBoolean("shake_enabled", true);
     		editor.commit();
         }
     };
     
     BroadcastReceiver deskShareReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
 			String snap = downloadPath + "snap/snap.png";
 			FileOutputStream fos;
 			try {//prepare for share desktop
 				fos = new FileOutputStream(snap);
 				apps.setDrawingCacheEnabled(true);
 				Bitmap bmp = apps.getDrawingCache(); 
 		        bmp.compress(Bitmap.CompressFormat.PNG, 90, fos); 
 		        fos.close();
 		        apps.destroyDrawingCache();
 			} catch (Exception e) {
 			} 
 
 	        Intent intentSend = new Intent(Intent.ACTION_SEND);
 	        intentSend.setType("image/*");  
 	        intentSend.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
 	        intentSend.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(downloadPath + "snap/snap.png")));
 	        util.startActivity(Intent.createChooser(intentSend, getString(R.string.sharemode)), true, getBaseContext());
         }
     };
     
     BroadcastReceiver homeChangeReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             final String homeName = intent.getStringExtra("old_home");
             if(homeName.equals(myPackageName)) finish();
         }
     };
 
     void refreshRadioButton() {
 		int current = mainlayout.getCurrentItem();
 		radioGroup.clearCheck();
 		if (current < radioGroup.getChildCount()) ((RadioButton) radioGroup.getChildAt(current)).setChecked(true);// crash once for C lassCastException.
 		
 		if (current == 0) 
 			radioText.setText(getString(R.string.systemapps) + "(" + sysAlphaList.getCount() + ")");
 		else if (current == 2) 
 			radioText.setText(getString(R.string.userapps) + "(" + userAlphaList.getCount() + ")");
 		else radioText.setText("Home");
     }
 
     
 	BroadcastReceiver packageReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             String packageName = intent.getDataString().split(":")[1];//it always in the format of package:x.y.z
             if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
             	ResolveInfo info = userAlphaList.remove(packageName);
             	if (info == null) info = sysAlphaList.remove(packageName);
             	refreshRadioButton();
     			
             	if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {//not remove shortcut if it is just replace 
             		for (int i = 0; i < favoAdapter.getCount(); i++) {
             			info = favoAdapter.getItem(i);
             			if (info.activityInfo.packageName.equals(packageName)) {
             				favoAdapter.remove(info);
             				writeFile("favo");
             				break;
             			}
             		}
             		for (int i = 0; i < shortAdapter.getCount(); i++) {
             			info = shortAdapter.getItem(i);
             			if (info.activityInfo.packageName.equals(packageName)) {
             				shortAdapter.remove(info);
             				writeFile("short");
             				break;
             			}
             		}
             		
             	}
             }
             else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
 	    		try {//get size of new installed package
 					getPackageSizeInfo.invoke(pm, packageName, sizeObserver);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
             	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
             	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
             	mainIntent.setPackage(packageName);
             	List<ResolveInfo> targetApps = pm.queryIntentActivities(mainIntent, 0);
 
             	for (int i = 0; i < targetApps.size(); i++) {
                 	if (targetApps.get(i).activityInfo.packageName.equals(packageName) ) {//the new package may not support Launcher category, we will omit it.
                 		ResolveInfo ri = targetApps.get(i);
         	    	    CharSequence  sa = ri.loadLabel(pm);
         	    	    if (sa == null) sa = ri.activityInfo.name;
         	    		ri.activityInfo.applicationInfo.dataDir = getToken(sa);//we borrow dataDir to store the Pinyin of the label.
         	    		String tmp = ri.activityInfo.applicationInfo.dataDir.substring(0, 1);
         	    		
                     	if ((ri.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                     		sysAlphaList.add(ri);
             		    	break;
                     	}
                     	else {
                     		userAlphaList.add(ri);
             		    	break;
                     	}
                 	}
             	}
             	refreshRadioButton();
             }
 		}
 		
 	};
 
     
     private class favoAppAdapter extends ArrayAdapter<ResolveInfo> {
     	ArrayList<ResolveInfo> localApplist;
         public favoAppAdapter(Context context, List<ResolveInfo> apps) {
             super(context, 0, apps);
             localApplist = (ArrayList<ResolveInfo>) apps;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             final ResolveInfo info = (ResolveInfo) localApplist.get(position);
 
             if (convertView == null) {
                 final LayoutInflater inflater = getLayoutInflater();
                 convertView = inflater.inflate(R.layout.favo_list, parent, false);
             }
             
             convertView.setBackgroundColor(0);
             
             final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.favoappicon);
             
             btnIcon.setImageDrawable(info.loadIcon(pm));
     		btnIcon.setOnClickListener(new OnClickListener() {//start app
 				@Override
 				public void onClick(View arg0) {
 					util.startApp(info, getBaseContext());
 				}
     		});
     		btnIcon.setTag(new ricase(info, 0));
             registerForContextMenu(btnIcon);
             
             return convertView;
         }
     }
     
     private class shortAppAdapter extends ArrayAdapter<ResolveInfo> {
     	ArrayList<ResolveInfo> localApplist;
         public shortAppAdapter(Context context, List<ResolveInfo> apps) {
             super(context, 0, apps);
             localApplist = (ArrayList<ResolveInfo>) apps;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             final ResolveInfo info = (ResolveInfo) localApplist.get(position);
 
             if (convertView == null) {
                 final LayoutInflater inflater = getLayoutInflater();
                 convertView = inflater.inflate(R.layout.favo_list, parent, false);
             }
             
             final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.favoappicon);
             btnIcon.setImageDrawable(info.loadIcon(pm));
             
             TextView appname = (TextView) convertView.findViewById(R.id.favoappname);
             appname.setText(info.loadLabel(pm));
             
             convertView.setOnClickListener(new OnClickListener() {//launch app
 				@Override
 				public void onClick(View arg0) {
 					util.startApp(info, getBaseContext());
 					shortAppList.setVisibility(View.INVISIBLE);
 				}
     		});
     		convertView.setTag(new ricase(info, 1));
             registerForContextMenu(convertView);
             
             return convertView;
         }
     }
     
     void readFile(String name) 
     {
     	FileInputStream fi = null;
     	ObjectInputStream ois = null;
 		try {//read favorite or shortcut data
 			fi = openFileInput(name);
 			ois = new ObjectInputStream(fi);
 			String activityName;
 			while ((activityName = (String) ois.readObject()) != null) {
 				for (int i = 0; i < mAllApps.size(); i++)
 					if (mAllApps.get(i).activityInfo.name.equals(activityName)) {
 						if (name.equals("favo")) mFavoApps.add(mAllApps.get(i));
 						else if (name.equals("short")) mShortApps.add(mAllApps.get(i));
 						break;
 					}
 			}
 		} catch (EOFException e) {//only when read eof need send out msg.
 			try {
 				ois.close();
 				fi.close();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     public String getToken(CharSequence sa) {
 	    String sa1 = sa.toString().trim();
 	    String sa2 = sa1;
 	    if (sa1.length() > 0) {
     	    try {//this is to fix a bug report by market
     	    	sa2 = HanziToPinyin.getInstance().getToken(sa1.charAt(0)).target.trim();
     	    	if (sa2.length() > 1) sa2 = sa2.substring(0, 1);
     	    } catch(Exception e) {
     	    	e.printStackTrace();
     	    }
 	    }
 	    sa2 = sa2.toUpperCase();
 	    if ((sa2.compareTo("A") < 0) || (sa2.compareTo("Z") > 0)) sa2 = "#";//for space or number, we change to #
 	    return sa2;
     }
     
 	class InitTask extends AsyncTask<String, Integer, String> {
 		@Override
 		protected String doInBackground(String... params) {//do all time consuming work here
 			canRoot = false;
 	    	String res = ShellInterface.doExec(new String[] {"id"}, true);
 	    	if (res.contains("root")) {
 	    		res = ShellInterface.doExec(new String[] {"mount -o rw,remount -t yaffs2 /dev/block/mtdblock3 /system"}, true);
 	    		if (res.contains("Error")) canRoot = false;
 	    		else canRoot = true;
 	    	}
 
 	    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
 	    	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 	    	mAllApps = pm.queryIntentActivities(mainIntent, 0);
 				    	
             readFile("favo");
             readFile("short");
             boolean shortEmpty = mShortApps.isEmpty();
             
             String hidePackage = perferences.getString("hide_package", "");
 	    	//read all resolveinfo
 	    	String label_sms = "簡訊 Messaging Messages メッセージ 信息 消息 短信 메시지  Mensajes Messaggi Berichten SMS a MMS SMS/MMS"; //use label name to get short cut
 	    	String label_phone = "電話 Phone 电话 电话和联系人 拨号键盘 키패드  Telefon Teléfono Téléphone Telefono Telefoon Телефон 휴대전화  Dialer";
 	    	String label_contact = "聯絡人 联系人 Contacts People 連絡先 通讯录 전화번호부  Kontakty Kontakte Contactos Contatti Contacten Контакты 주소록";
 	    	int match = 0;
 	    	for (int i = 0; i < mAllApps.size(); i++) {
 	    		ResolveInfo ri = mAllApps.get(i);
 	    	    CharSequence  sa = ri.loadLabel(pm);
 	    	    if (sa == null) sa = ri.activityInfo.name;
 	    		ri.activityInfo.applicationInfo.dataDir = getToken(sa);//we borrow dataDir to store the Pinyin of the label.
 	    		
 	    		if ((ri.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
 	    			sysAlphaList.add(ri, false, false);
 	    			
 	    			if (match < 3) {//only find 3 match: sms, phone, contact
 		    			String name = sa.toString() ; 
 		    			//Log.d("===============", name);
 		    			if (label_phone.contains(name)) {
 		    				if (ri_phone == null) {
 		    					ri_phone = ri;
 		    		        	Message msgphone = mAppHandler.obtainMessage();
 		    		        	msgphone.what = UPDATE_RI_PHONE;
 		    		        	mAppHandler.sendMessage(msgphone);//inform UI thread to update UI.
 		    		        	match += 1;
 		    				}
 		    			} 
 		    			else if (label_sms.contains(name)) {
 		    				if ((ri_sms == null) && (!name.equals("MM"))) {
 		    					ri_sms = ri;
 		    		        	Message msgsms = mAppHandler.obtainMessage();
 		    		        	msgsms.what = UPDATE_RI_SMS;
 		    		        	mAppHandler.sendMessage(msgsms);//inform UI thread to update UI.
 		    		        	match += 1;
 		    				}
 		    			}
 		    			else if ((shortEmpty) && label_contact.contains(name)) {//only add contact to shortcut if shortcut is empty.
 		    				if (ri_contact == null) {
 		    					mShortApps.add(ri);
 		    					/*ri_contact = ri;
 		    		        	Message msgcontact = mAppHandler.obtainMessage();
 		    		        	msgcontact.what = UPDATE_RI_CONTACT;
 		    		        	mAppHandler.sendMessage(msgcontact);//inform UI thread to update UI.*/
 		    		        	match += 1;
 			    			}
 		    			}
 	    			}
 	    		}
 	    		else {
 	    			if (hidePackage.equals("") || !hidePackage.equals(ri.activityInfo.packageName))
 	    				userAlphaList.add(ri, false, false);
 	    		}
 		    	
 	    		try {
 					getPackageSizeInfo.invoke(pm, ri.activityInfo.packageName, sizeObserver);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 	    	}
 	    	sysAlphaList.sortAlpha();
 	    	userAlphaList.sortAlpha();
 
 	    	
         	Message msguser = mAppHandler.obtainMessage();
         	msguser.what = UPDATE_USER;
         	mAppHandler.sendMessage(msguser);//inform UI thread to update UI.
 	    	
         	downloadPath = util.preparePath(getBaseContext());
         	
         	picList = new ArrayList();
         	picList_selected = new ArrayList();
         	new File(downloadPath).list(new OnlyPic());
         	if (picList.size() > 0) {
         		editor.putBoolean("shake_enabled", true);
         		editor.commit();
         	}
 			   
 	    	mainIntent = new Intent(Intent.ACTION_VIEW, null);
 	    	mainIntent.addCategory(Intent.CATEGORY_DEFAULT);
 	    	List<ResolveInfo> viewApps = pm.queryIntentActivities(mainIntent, 0);
 	    	appDetail = null;
 	    	for (int i = 0; i < viewApps.size(); i++) {
 	    		if (viewApps.get(i).activityInfo.name.contains("InstalledAppDetails")) {
 	    			appDetail = viewApps.get(i);//get the activity for app detail setting
 	    			break;
 	    		}
 	    	}
 
 			return null;
 		}
 	}
 
 	class OnlyPic implements FilenameFilter { 
 	    public boolean accept(File dir, String s) {
 	    	 String name = s.toLowerCase();
 	         if (s.endsWith(".png") || s.endsWith(".jpg")) {
 	        	 picList.add(s);
 	        	 return true;
 	         }
 	         else return false;
 	    } 
 	} 
 	
     class appHandler extends Handler {
 
         public void handleMessage(Message msg) {
         	switch (msg.what) {
         	case UPDATE_USER:
         		sysAlphaList.setAdapter();
         		userAlphaList.setAdapter();
         		refreshRadioButton();//this will update the radio button with correct app number. only for very slow phone
         		
         		shortAdapter = new shortAppAdapter(getBaseContext(), mShortApps);
         		shortAppList.setAdapter(shortAdapter);
         		
         		break;
         	case UPDATE_RI_PHONE:
         		int missCallCount = callObserver.countUnread();
         		if (missCallCount > 0) {
         			Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.phone);
         			shortcut_phone.setImageBitmap(util.generatorCountIcon(bm, missCallCount, 1, 1, getBaseContext()));
         		}
         		else shortcut_phone.setImageResource(R.drawable.phone);
         		
     			shortcut_phone.setOnClickListener(new OnClickListener() {//start app
     				@Override
     				public void onClick(View arg0) {
     					util.startApp(ri_phone, getBaseContext());
     				}
     			});
         		break;
         	case UPDATE_RI_SMS:
         		int unreadCount = smsObserver.countUnread();
         		if (unreadCount > 0) {
         			Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.sms);
             		shortcut_sms.setImageBitmap(util.generatorCountIcon(bm, unreadCount, 1, 1, getBaseContext()));
         		}
         		else shortcut_sms.setImageResource(R.drawable.sms);
         		
     			shortcut_sms.setOnClickListener(new OnClickListener() {//start app
     				@Override
     				public void onClick(View arg0) {
     					util.startApp(ri_sms, getBaseContext());
     				}
     			});
         		break;
         	case UPDATE_RI_CONTACT:
     			shortcut_contact.setImageDrawable(ri_contact.loadIcon(pm));
     			shortcut_contact.setOnClickListener(new OnClickListener() {//start app
     				@Override
     				public void onClick(View arg0) {
     					util.startApp(ri_contact, getBaseContext());
     				}
     			});
         		break;
         	}
         }
     };
 
 	
 	@Override
 	protected void onNewIntent(Intent intent) {//go back to home if press Home key.
		if ((Intent.ACTION_MAIN.equals(intent.getAction())) && (intent.hasCategory(Intent.CATEGORY_HOME))) {
 			if (mainlayout.getCurrentItem() != homeTab) mainlayout.setCurrentItem(homeTab);
 			else if (shortAppList.getVisibility() == View.VISIBLE) shortBar.performClick();
 		}
 		super.onNewIntent(intent); 
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (event.getRepeatCount() == 0) {
 			if (keyCode == KeyEvent.KEYCODE_BACK) {//press Back key in webview will go backword.
 				if (mainlayout.getCurrentItem() != homeTab) mainlayout.setCurrentItem(homeTab);
 				else if (shortAppList.getVisibility() == View.VISIBLE) shortBar.performClick();
 				else this.openOptionsMenu();
 				return true;
 			}	
 		}
 		return false;
 	}
 	
 	@Override
     public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig); //not restart activity each time screen orientation changes
 		getWindowManager().getDefaultDisplay().getMetrics(dm);
 		
 		if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO)
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
     }
 
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent arg0) {
 		if (arg0.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {  
 			long curTime = System.currentTimeMillis();  
 			// detect every 100ms
 			if ((curTime - lastUpdate) > 100) {  
 				long timeInterval = (curTime - lastUpdate);  
 				lastUpdate = curTime;  
 				  
 				float x = arg0.values[SensorManager.DATA_X];  
 				float y = arg0.values[SensorManager.DATA_Y];  
 				float z = arg0.values[SensorManager.DATA_Z];  
 				  
 				float deltaX = x - last_x;
 				float deltaY = y - last_y;
 				float deltaZ = z - last_z;
 				  
 				double speed = Math.sqrt(deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ)/timeInterval * 100;
 				//condition to change wallpaper: speed is enough; frequency is not too high; picList is not empty. 
 				if ((!busy) && (speed > 8) && (curTime - lastSet > 500) && (picList != null) && (picList.size() > 0)) {
 					busy = true;
 			    	Random random = new Random();
 			    	int id = random.nextInt(picList.size());
 				    try {
 				    	wallpaperFile = downloadPath + picList.get(id);
 				    	BitmapDrawable bd = (BitmapDrawable) BitmapDrawable.createFromPath(wallpaperFile);
 				        double factor = 1.0 * bd.getIntrinsicWidth() / bd.getIntrinsicHeight();
 				        if (factor >= 1.2) {//if too wide, we want use setWallpaperOffsets to move it, so we need set it to wallpaper
 				        	int tmpWidth = (int) (dm.heightPixels * factor);
 				        	mWallpaperManager.setBitmap(Bitmap.createScaledBitmap(bd.getBitmap(), tmpWidth, dm.heightPixels, false));
 				        	mWallpaperManager.suggestDesiredDimensions(tmpWidth, dm.heightPixels);
 					    	
 							sensorMgr.unregisterListener(this);
 			        		shakeWallpaper = false;
 			        		editor.putBoolean("shake", false);
 			        		editor.commit();
 				        }
 				    	else {//otherwise just change the background is ok.				    		
 					    	ClippedDrawable cd = new ClippedDrawable(bd, apps.getWidth(), apps.getHeight());
 					    	apps.setBackgroundDrawable(cd);
 				        }
 				        picList_selected.add(picList.get(id));
 				        picList.remove(id);//prevent it be selected again before a full cycle
 				    	lastSet = System.currentTimeMillis();
 					} catch (Exception e) {
 						e.printStackTrace();
 						picList.remove(id);
 						wallpaperFile = "";
 					}
 					
 					if (picList.isEmpty()) {
 						if (picList_selected.isEmpty()) {
 							sensorMgr.unregisterListener(this);
 							
 			        		editor.putBoolean("shake_enabled", false);
 			        		shakeWallpaper = false;
 			        		editor.putBoolean("shake", false);
 			        		editor.commit();
 						}
 						else {
 							picList = (ArrayList<String>) picList_selected.clone();
 							picList_selected.clear();
 						}
 					}
 					
 					busy = false;
 				}  
 				last_x = x;  
 				last_y = y;  
 				last_z = z;  
 			}  
 		}  
 	}
 
 	@Override
 	public void onSizeChanged(int w, int h, int oldW, int oldH) {
 		//setLayout(oldW);
 	}
 }
 
 /** from Android Home sample
  * When a drawable is attached to a View, the View gives the Drawable its dimensions
  * by calling Drawable.setBounds(). In this application, the View that draws the
  * wallpaper has the same size as the screen. However, the wallpaper might be larger
  * that the screen which means it will be automatically stretched. Because stretching
  * a bitmap while drawing it is very expensive, we use a ClippedDrawable instead.
  * This drawable simply draws another wallpaper but makes sure it is not stretched
  * by always giving it its intrinsic dimensions. If the wallpaper is larger than the
  * screen, it will simply get clipped but it won't impact performance.
  */
 class ClippedDrawable extends Drawable {
     private final Drawable mWallpaper;
     int screenWidth, screenHeight;
     boolean tooWide = false;
 
     public ClippedDrawable(Drawable wallpaper, int sw, int sh) {
         mWallpaper = wallpaper; 
         screenWidth = sw;
         screenHeight = sh;
     }
 
     @Override
     public void setBounds(int left, int top, int right, int bottom) {
         super.setBounds(left, top, right, bottom);
         // Ensure the wallpaper is as large as it really is, to avoid stretching it at drawing time
         int tmpHeight = mWallpaper.getIntrinsicHeight() * screenWidth / mWallpaper.getIntrinsicWidth();
         int tmpWidth = mWallpaper.getIntrinsicWidth() * screenHeight / mWallpaper.getIntrinsicHeight();
         if (tmpHeight >= screenHeight) { 
         	top -= (tmpHeight - screenHeight)/2;
         	mWallpaper.setBounds(left, top, left + screenWidth, top + tmpHeight);
         }
         else {//if the pic width is wider than screen width, then we need show part of the pic.
         	tooWide = true;
            	left -= (tmpWidth - screenWidth)/2;
         	mWallpaper.setBounds(left, top, left + tmpWidth, top + screenHeight);
         }
     }
 
     public void draw(Canvas canvas) {
         mWallpaper.draw(canvas);
     }
 
     public void setAlpha(int alpha) {
         mWallpaper.setAlpha(alpha);
     }
 
     public void setColorFilter(ColorFilter cf) {
         mWallpaper.setColorFilter(cf);
     }
 
     public int getOpacity() {
         return mWallpaper.getOpacity();
     }
 }
 
 class sizedRelativeLayout extends RelativeLayout {
 
 	public sizedRelativeLayout(Context context) {
 		super(context);
 	}
 	
     public sizedRelativeLayout(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 	
 	private OnResizeChangeListener mOnResizeChangeListener;
 	
     protected void onSizeChanged(int w, int h, int oldW, int oldH) {
         if(mOnResizeChangeListener!=null){
             mOnResizeChangeListener.onSizeChanged(w,h,oldW,oldH);
         }
         super.onSizeChanged(w,h,oldW,oldH);
     }
 	
     public void setResizeListener(OnResizeChangeListener l) {
         mOnResizeChangeListener = l;
     }
 	    
     public interface OnResizeChangeListener{
         void onSizeChanged(int w,int h,int oldW,int oldH);
     }
 	
 }
 
 class SmsChangeObserver extends ContentObserver {
 	ContentResolver mCR;
 	Handler mHandler;
 	
 	public SmsChangeObserver(ContentResolver cr, Handler handler) {
 		super(handler);
 		mCR = cr;
 		mHandler = handler;
 	}
 	
 	public int countUnread() {
     	//get sms unread count
 		int ret = 0;
     	Cursor csr = mCR.query(Uri.parse("content://sms"),
     	                new String[] {"thread_id"},
     	                "read=0",
     	                null,
     	                null);
     	if (csr != null) ret = csr.getCount();
     	
     	//get mms unread count
     	csr = mCR.query(Uri.parse("content://mms"),
     	                new String[] {"thread_id"},
     	                "read=0",
     	                null,
     	                null);
     	if (csr != null) ret += csr.getCount();
     	return ret;
 	}
 	
 	@Override
 	public void onChange(boolean selfChange) {
 		super.onChange(selfChange);
     	Message msgsms = mHandler.obtainMessage();
     	msgsms.what = 1;//UPDATE_RI_SMS;
     	mHandler.sendMessage(msgsms);//inform UI thread to update UI.
 	}
 }
 
 
 class CallObserver extends ContentObserver {
 	ContentResolver mCR;
 	Handler mHandler;
 	
 	public CallObserver(ContentResolver cr, Handler handler) {
 		super(handler);
 		mHandler = handler;
 		mCR = cr;
 	}
 	
 	public int countUnread() {
     	//get missed call number
     	Cursor csr = mCR.query(Calls.CONTENT_URI, 
     			new String[] {Calls.NUMBER, Calls.TYPE, Calls.NEW}, 
     			Calls.TYPE + "=" + Calls.MISSED_TYPE + " AND " + Calls.NEW + "=1", 
     			null, Calls.DEFAULT_SORT_ORDER);
     	if (csr != null) return csr.getCount();
     	else return 0;
 	}
 	
 	@Override
 	public void onChange(boolean selfChange) {
 		super.onChange(selfChange);
     	Message msgphone = mHandler.obtainMessage();
     	msgphone.what = 0;//UPDATE_RI_PHONE;
     	mHandler.sendMessage(msgphone);//inform UI thread to update UI.
 	}
 }
 
