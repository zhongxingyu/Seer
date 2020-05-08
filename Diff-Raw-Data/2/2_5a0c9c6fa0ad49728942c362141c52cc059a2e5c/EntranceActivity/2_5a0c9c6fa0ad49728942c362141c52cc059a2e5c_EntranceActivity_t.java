 package com.zrd.zr.letuwb;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.AlphaAnimation;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 import com.mobclick.android.MobclickAgent;
 import com.zrd.zr.letuwb.R;
 import com.zrd.zr.protos.WeibousersProtos.Weibousers;
 import com.zrd.zr.protos.WeibousersProtos.Weibouser;
 
 public class EntranceActivity extends Activity implements OnTouchListener {
 
 	final static String SERIAL_APP = "gbhytfvnjurdcmkiesx,lowaz.;p201108282317";
 	final static String TIMEZONE_SERVER = "Asia/Hong_Kong";
 	//final static String URL_SITE = "http://hot88.info/letmewb/";
 	final static String URL_SITE = "http://183.90.186.96/letmewb/";
 	//final static String URL_UPDATE = "http://az88.info/";
 	final static String URL_UPDATE = "http://183.90.186.96/";
 	//final static String URL_STATS = "http://az88.info/letmewb/";
 	final static String URL_STATS = "http://183.90.186.96/letmewb/";
 	final static String PATH_COLLECTION = "/letuwb/collection/";
 	final static String PATH_CACHE = "/letuwb/cache/";
 	final static Integer MAXSIZE_CACHE = 100;// in MB
 	final static Integer MAXPERCENTAGE_CACHE = 5; // with %
 	final static int REQUESTCODE_PICKFILE = 1001;
 	final static int REQUESTCODE_BACKFROM = 1002;
 	final static String CONFIG_ACCOUNTS = "Accounts";
 	final static String CONFIG_CLIENTKEY = "ClientKey";
 	final static String CONFIG_RANDOMKEY = "RandomKey";
 	final static String CONFIG_TOPICCHOICE = "TopicChoice";
 	final static String SYMBOL_FAILED = "~failed~";
 	final static String SYMBOL_SUCCESSFUL = "~successful~";
 	final static int PERIOD_VOTEAGAIN = 24;//in HOUR
 	private static String mClientKey = "";
 	private static String mRandomKey = "";
 	private static Integer mTopicChoice = 0;
 	GridView mGridPics;
 	private static final Integer mLimit = 28;//how many pictures should be passed to PicbrowActivity, actually multiple of mPageLimit is recommended
 	final Integer mPageLimit = 4;//how many pictures should be loaded into mGridPics.
 	private static Integer mCurPage = 1;
 	private Integer mPageBeforeBrow = 1;
 	private static Integer mCurParagraph = 1;
 	private static Integer mTotalPages = 0;
 	private static Integer mTotalPics = 0;
 	private static ArrayList<String> mCurTerms = new ArrayList<String>();
 	private static Integer mAccountId = 0;
 	private static ArrayList<WeibouserInfo> mUsrs = new ArrayList<WeibouserInfo>();
 	ArrayList<WeibouserInfo> mPageUsrs = new ArrayList<WeibouserInfo>();
 	OnClickListener listenerBtnView = null;
 	OnClickListener listenerBtnExit = null;
 	Dialog mPrgDlg;
 	private Dialog mDlgWaysToCheck;
 	AlertDialog mQuitDialog;
 	EditText mEditUsername;
 	EditText mEditPassword;
 	EditText mEditRepeat;
 	TableRow mRowRepeat;
 	CheckBox mCheckRemember;
 	TextView mTextPageInfo;
 	Button mBtnRandom;
 	Button mBtnLatest;
 	Button mBtnHottest;
 	Button mBtnUnhottest;
 	ImageButton mBtnExchange;
 	private ArrayList<Button> mTopicBtns = null;
 	SeekBar mSeekMain;
 	Button mBtnPre;
 	Button mBtnNext;
 	TextView mTextSeekPos;
 	LinearLayout mLinearMainBottom;
 	WebView mWebCount;
 	static DisplayMetrics mDisplayMetrics = new DisplayMetrics();
 	private static int mPrivilege = 1;//0 member, 1 guest
 	NotificationManager mNotificationManager;
 	GestureDetector mGestureDetector = null;
 	private String mOutText;//!!actually not used!!
 	static SharedPreferences mPreferences = null;
 	
     /* Called when the activity is firstly created. */
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
         setContentView(R.layout.main);
         mPreferences = getPreferences(EntranceActivity.MODE_PRIVATE);
         
         /**
          * Try to trace exceptions
          */
         //TraceDroid.init(this);
         //TraceDroidEmailSender.sendStackTraces("ralphchiu1@gmail.com", this);
         
         /**
          * Initialize the application
          */
         mClientKey = mPreferences.getString(CONFIG_CLIENTKEY, "");
         mRandomKey = mPreferences.getString(CONFIG_RANDOMKEY, "");
         mTopicChoice = mPreferences.getInt(CONFIG_TOPICCHOICE, 0);
         AsyncInit init = new AsyncInit();
         init.execute();
         
         /**
          * Clean cache if needed
          */
         AsyncCacheCleaner acc = new AsyncCacheCleaner(this);
         acc.execute(
         	AsyncSaver.getSdcardDir() + EntranceActivity.PATH_CACHE,
         	MAXSIZE_CACHE.toString(),		//in MB
         	MAXPERCENTAGE_CACHE.toString()	//in percentage
         );
         
         // Look up the AdView as a resource and load a request.
         AdView adView = (AdView)this.findViewById(R.id.adsMain);
         adView.loadAd(new AdRequest());
                 
         getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.exchangelist_title);
         RegLoginActivity.addContext(EntranceActivity.this);
         mBtnExchange = (ImageButton) findViewById(R.id.btnExchange);
         mGridPics = (GridView) findViewById(R.id.gridViewPics);
         mTextPageInfo = (TextView) findViewById(R.id.tvPageInfo);
         mBtnRandom = (Button) findViewById(R.id.btnRandom);
         mBtnLatest = (Button) findViewById(R.id.btnLatest);
         mBtnHottest = (Button) findViewById(R.id.btnHottest);
         mBtnUnhottest = (Button) findViewById(R.id.btnUnhottest);
         mTopicBtns = new ArrayList<Button>();
         mTopicBtns.add(mBtnLatest);
         mTopicBtns.add(mBtnHottest);
         mTopicBtns.add(mBtnRandom);
         mTopicBtns.add(mBtnUnhottest);
         mSeekMain = (SeekBar) findViewById(R.id.sbMain);
         mBtnPre = (Button) findViewById(R.id.btnPre);
         mBtnNext = (Button) findViewById(R.id.btnNext);
         mTextSeekPos = (TextView) findViewById(R.id.tvSeekPos);
         mLinearMainBottom = (LinearLayout) findViewById(R.id.linearLayoutMainBottom);
         mLinearMainBottom.setVisibility(LinearLayout.GONE);
         mWebCount = (WebView) findViewById(R.id.wvCount);
         getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
         mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         mGestureDetector = new GestureDetector(this, new LetuseeGestureListener());
         mGridPics.setOnTouchListener(this);
         
         mBtnExchange.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 Intent intent = new Intent();
                 intent.setClass(EntranceActivity.this, ExchangeListActivity.class);
                 startActivity(intent);
             }
         });
         
         mTextSeekPos.setVisibility(TextView.GONE);
         mSeekMain.setMax(0);
         mSeekMain.setOnSeekBarChangeListener(
         	new OnSeekBarChangeListener() {
 
 				@Override
 				public void onProgressChanged(SeekBar seekBar, int progress,
 						boolean fromUser) {
 					// TODO Auto-generated method stub
 					int p;
 					if (progress == 0) p = 1;
 					else p = progress;
 					mTextSeekPos.setText("" + ((p - 1) * mPageLimit + 1) + "~" + (p * mPageLimit));
 					mTextSeekPos.setVisibility(TextView.VISIBLE);
 					mTextPageInfo.setVisibility(TextView.GONE);
 					AlphaAnimation anim = new AlphaAnimation(0.1f, 1.0f);
 					mTextSeekPos.startAnimation(anim);
 				}
 
 				@Override
 				public void onStartTrackingTouch(SeekBar seekBar) {
 					// TODO Auto-generated method stub
 					
 				}
 
 				@Override
 				public void onStopTrackingTouch(SeekBar seekBar) {
 					// TODO Auto-generated method stub
 					mTextSeekPos.setVisibility(TextView.GONE);
 					mTextPageInfo.setVisibility(TextView.VISIBLE);
 					int progress = seekBar.getProgress();
 					if (progress == 0) progress = 1;
 					int idxPic = mPageLimit * progress;
 					Integer page;
 					if (idxPic % mLimit == 0) {
 						page = idxPic / mLimit;
 					} else {
 						page = (int) Math.ceil((double) idxPic / (double) mLimit);
 					}
 					int max = (int) Math.ceil((double) mLimit / (double) mPageLimit);
 					Integer paragraph = progress % max == 0 ? max : (progress % max);
 					
 					mPrgDlg.show();
 					AsyncGridLoader agl = new AsyncGridLoader(EntranceActivity.this);
 					int m = mCurTerms.size();
 					String[] args = new String[m + 4];
 					for (int i = 0; i < m; i++) {
 						args[i] = mCurTerms.get(i);
 					}
 					args[m] = "limit";
 					args[m + 1] = mLimit.toString();
 					args[m + 2] = "page";
 					args[m + 3] = page.toString();
 					mCurPage = page;
 					mCurParagraph = paragraph;
 					agl.execute(args);
 					
 					/*
 					Toast.makeText(
 						LetuseeActivity.this,
 						"" + ((progress - 1) * mPageLimit + 1) + "~" + (progress * mPageLimit),
 						Toast.LENGTH_SHORT
 					).show();
 					*/
 				}
         		
         	}
         );
         
         mBtnPre.setOnClickListener(new OnClickListener () {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				previous();
 			}
         	
         });
         
         mBtnNext.setOnClickListener(new OnClickListener () {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				next();
 			}
         	
         });
         
         mBtnRandom.setOnClickListener(new OnClickListener () {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				mBtnRandom.setSelected(true);
 				mBtnLatest.setSelected(false);
 				mBtnHottest.setSelected(false);
 				mBtnUnhottest.setSelected(false);
 				
 				AsyncGridLoader asyncGridLoader = new AsyncGridLoader(EntranceActivity.this);
 				mPrgDlg.show();
 				mCurTerms.clear();
 		        mCurTerms.add("top");
 		        mCurTerms.add("6");
 		        mCurPage = 1;
 		        mCurParagraph = 1;
 		        asyncGridLoader.execute(mCurTerms.get(0), mCurTerms.get(1), "limit", mLimit.toString(), "page", mCurPage.toString(), "pb", "1");
 			}
         	
         });
         
         mBtnLatest.setOnClickListener(new OnClickListener () {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				mBtnRandom.setSelected(false);
 				mBtnLatest.setSelected(true);
 				mBtnHottest.setSelected(false);
 				mBtnUnhottest.setSelected(false);
 				
 				AsyncGridLoader asyncGridLoader = new AsyncGridLoader(EntranceActivity.this);
 				mPrgDlg.show();
 				mCurTerms.clear();
 		        mCurTerms.add("top");
 		        mCurTerms.add("0");
 		        mCurPage = 1;
 		        mCurParagraph = 1;
 		        asyncGridLoader.execute(mCurTerms.get(0), mCurTerms.get(1), "limit", mLimit.toString(), "page", mCurPage.toString(), "pb", "1");
 			}
         	
         });
         
         mBtnHottest.setOnClickListener(new OnClickListener () {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				mBtnRandom.setSelected(false);
 				mBtnLatest.setSelected(false);
 				mBtnHottest.setSelected(true);
 				mBtnUnhottest.setSelected(false);
 				
 				Toast.makeText(
 					EntranceActivity.this,
 					R.string.tips_hottesttheweek,
 					Toast.LENGTH_LONG
 				).show();
 				
 				AsyncGridLoader asyncGridLoader = new AsyncGridLoader(EntranceActivity.this);
 				mPrgDlg.show();
 				mCurTerms.clear();
 		        mCurTerms.add("top");
 		        mCurTerms.add("4");
 		        mCurPage = 1;
 		        mCurParagraph = 1;
 		        asyncGridLoader.execute(mCurTerms.get(0), mCurTerms.get(1), "limit", mLimit.toString(), "page", mCurPage.toString(), "pb", "1");
 			}
         	
         });
         
         mBtnUnhottest.setOnClickListener(new OnClickListener () {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				mBtnRandom.setSelected(false);
 				mBtnLatest.setSelected(false);
 				mBtnHottest.setSelected(false);
 				mBtnUnhottest.setSelected(true);
 				
 				Toast.makeText(
 					EntranceActivity.this,
 					R.string.tips_unhottesttheweek,
 					Toast.LENGTH_LONG
 				).show();
 				
 				AsyncGridLoader asyncGridLoader = new AsyncGridLoader(EntranceActivity.this);
 				mPrgDlg.show();
 				mCurTerms.clear();
 		        mCurTerms.add("top");
 		        mCurTerms.add("5");
 		        mCurPage = 1;
 		        mCurParagraph = 1;
 		        asyncGridLoader.execute(mCurTerms.get(0), mCurTerms.get(1), "limit", mLimit.toString(), "page", mCurPage.toString(), "pb", "1");
 			}
         	
         });
         
         mPrgDlg = new Dialog(this, R.style.Dialog_Clean);
         mPrgDlg.setContentView(R.layout.custom_dialog_loading);
         WindowManager.LayoutParams lp = mPrgDlg.getWindow().getAttributes();
         lp.alpha = 1.0f;
         mPrgDlg.getWindow().setAttributes(lp);
         mPrgDlg.setCancelable(true);
 		
 		mQuitDialog = new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).create();
 		mQuitDialog.setTitle(getString(R.string.quit_title));
 		mQuitDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.label_yes),
 			new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					SharedPreferences.Editor edit = mPreferences.edit();
 					int i;
 					for (i = 0; i < mTopicBtns.size(); i++) {
 						if (mTopicBtns.get(i).isSelected()) break;
 					}
 					edit.putInt(CONFIG_TOPICCHOICE, i);
 					edit.commit();
 					android.os.Process.killProcess(android.os.Process.myPid());
 				}
 			
 			}
 		);
 		mQuitDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.label_no),
 			new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 				}
 			
 			}
 		);
 		
 		mDlgWaysToCheck = new Dialog(this, R.style.Dialog_Clean);
 		mDlgWaysToCheck.setContentView(R.layout.custom_dialog_list);
 		ListView lv = (ListView)mDlgWaysToCheck.findViewById(R.id.lvCustomList);
 		ArrayList<String> list = new ArrayList<String>();
 		list.add(getString(R.string.label_bigger_pic));
 		list.add(getString(R.string.label_microblogs));
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
 			this,
 			R.layout.item_custom_dialog_list,
 			list
 		);
 		lv.setAdapter(adapter);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long arg3) {
 				// TODO Auto-generated method stub
 				WeibouserInfo wi;
 				Intent intent = new Intent();
 				int idx = (Integer) mGridPics.getTag();
 				switch (position) {
 				case 0:
 					wi = (WeibouserInfo) mPageUsrs.get(idx);
 					intent.setClass(EntranceActivity.this, PicbrowActivity.class);
 					intent.putExtra("id", wi.id);
 					mPageBeforeBrow = mCurPage;
 					startActivityForResult(intent, REQUESTCODE_BACKFROM);
 					break;
 				case 1:
 					wi = (WeibouserInfo) mPageUsrs.get(idx);
 	                
 	                intent.putExtra("uid", wi.uid);
 					
 					intent.setClass(EntranceActivity.this, WeiboShowActivity.class);
 					startActivity(intent);
 					break;
 				}
 				mDlgWaysToCheck.dismiss();
 			}
 			
 		});
                 
 		mGridPics.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				// TODO Auto-generated method stub
 				mGridPics.setTag(position);
 				
 				mDlgWaysToCheck.show();
 			}
         });
         
         if (mClientKey.equals("")) {
         	mBtnHottest.performClick();
         } else {
         	if (mTopicChoice < mTopicBtns.size()) {
         		mTopicBtns.get(mTopicChoice).performClick();
         	} else {
         		mBtnLatest.performClick();
         	}
         }
     }
     
 	/*
 	 * get index of current usr in mUsrs by id
 	 */
 	public static int getUsrIndexFromId(long id, List<WeibouserInfo> usrs) {
 		if (usrs == null) return -1;
 		if (usrs.size() == 0) return -1;
 		int i;
 		for (i = 0; i < usrs.size(); i++) {
 			WeibouserInfo pi = (WeibouserInfo) usrs.get(i); 
 			if (id == pi.id) {
 				break;
 			}
 		}
 		if (i == usrs.size()) return -1;
 		return i;
 	}
 	
 	/*
 	 * get picfileInfo from mUsrs by id
 	 */
 	public static WeibouserInfo getPicFromId(long id, List<WeibouserInfo> pics) {
 		int idx = getUsrIndexFromId(id, pics);
 		if (idx < 0 || idx >= pics.size()) return null;
 		return pics.get(idx);
 	}
 
     
     public static String getClientKey() {
     	return mClientKey;
     }
     
     public static String getRandomKey() {
     	return mRandomKey;
     }
     
     public static void resetRandomKey(String key) {
     	mRandomKey = key;
     	if (mPreferences != null) {
 	    	SharedPreferences.Editor edit = mPreferences.edit();
 			edit.putString(CONFIG_RANDOMKEY, mRandomKey);
 			edit.commit();
     	}
     }
     
     public static ArrayList<String[]> getStoredAccounts() {
     	ArrayList<String[]> list = new ArrayList<String[]>();
     	if (mPreferences == null) return list;
     	String contents = mPreferences.getString(CONFIG_ACCOUNTS, "");
     	if (contents.equals("")) return list;
     	String[] pairs = contents.split(",");
     	if (pairs.length % 2 != 0) return list; 
     	
     	for (int i = 0; i < pairs.length; i += 2) {
     		list.add(new String[] {pairs[i], pairs[i + 1]});
     	}
     	return list;
     }
     
     public static void saveAccount(String usr, String pwd) {
     	ArrayList<String[]> list = getStoredAccounts();
     	int i;
     	for (i = 0; i < list.size(); i++) {
     		if (list.get(i)[0].equals(usr)) break;
     	}
     	if (i != list.size()) {
      		list.remove(i);
     	}
 		list.add(new String[] {usr, pwd});
 		String content = "";
 		for (i = 0; i < list.size(); i++) {
 			content += list.get(i)[0] + "," + list.get(i)[1];
 			if (i != list.size() - 1) content += ",";
 		}
 		SharedPreferences.Editor edit = mPreferences.edit();
 		edit.putString(CONFIG_ACCOUNTS, content);
 		edit.commit();
     }
     
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		MobclickAgent.onPause(this);
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		runOnUiThread(new Runnable() {
 			public void run() {
 				final String TAG = "Letusee Stats";
 				String url = URL_STATS + "count.html";
 				mWebCount.setWebViewClient(new WebViewClient() {
 					 public boolean shouldOverrideUrlLoading (WebView view, String url) {
 						 Log.v(TAG, "loading " + url);
 						 return false;
 					 }
 				});
 				WebSettings webSettings = mWebCount.getSettings();
 				webSettings.setJavaScriptEnabled(true);
 				webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
 				mWebCount.loadUrl(url);
 				Log.v(TAG, "send " + url);
 			}
 		});
 		
 		super.onResume();
 		
 		/*
 		 * for umeng.com
 		 */
 		MobclickAgent.onResume(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		menu.add(Menu.NONE, Menu.FIRST + 2, 2, getString(R.string.omenuitem_reglogin)).setIcon(R.drawable.ic_menu_login);
 		menu.add(Menu.NONE, Menu.FIRST + 1, 1, getString(R.string.omenuitem_upload)).setIcon(android.R.drawable.ic_menu_upload);
 		menu.add(Menu.NONE, Menu.FIRST + 3, 3, getString(R.string.omenuitem_quit)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
 		menu.add(Menu.NONE, Menu.FIRST + 4, 4, getString(R.string.omenuitem_about)).setIcon(android.R.drawable.ic_menu_help);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch (item.getItemId()) {
 		case Menu.FIRST + 1:
 			AsyncUploader.upload(mPrivilege, EntranceActivity.this);
 			break;
 		case Menu.FIRST + 2:
 			if (mPrivilege == 0) {
 				Toast.makeText(
 					this,
 					R.string.tips_alreadyloggedin,
 					Toast.LENGTH_SHORT
 				).show();
 			} else {
 				Intent intent = new Intent();
 				intent.setClass(EntranceActivity.this, RegLoginActivity.class);
 				startActivity(intent);
 			}
 			break;
 		case Menu.FIRST + 3:
 			mQuitDialog.show();
 			break;
 		case Menu.FIRST + 4:
 			Intent intent = new Intent();
 			intent.setClass(EntranceActivity.this, AboutActivity.class);
 			startActivity(intent);
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			mQuitDialog.show();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 		
 	/*
 	 * before called, mOutText should be evaluated.
 	 * !!actually not used!!
 	 */
 	public void saveText2Sd(String fpath, String fname) {
 		if(!AsyncSaver.getSdcardDir().equals("")) {
 			String path = AsyncSaver.getSdcardDir() + "/letusee/" + fpath;
 			File file = new File(path);
 			Boolean couldSave = false;
 			if (!file.exists()) {
 				if (file.mkdirs()) {
 					couldSave = true;
 				} else {
 					Toast.makeText(EntranceActivity.this,
 						String.format(getString(R.string.err_nopath), path),
 						Toast.LENGTH_LONG
 					).show();
 					return;
 				}
 			} else couldSave = true;
 			if (couldSave) {
 				//OK, now we could actually save the file, finally.
 				final File saveFile = new File(file, fname);
 				if (saveFile.exists()) {
 					//if there is already a file exists with same file name
 					AlertDialog alertDlg = new AlertDialog.Builder(EntranceActivity.this).create();
 					alertDlg.setTitle(String.format(getString(R.string.err_filealreadyexists), fname));
 					alertDlg.setButton(
 						DialogInterface.BUTTON_POSITIVE,
 						getString(R.string.label_ok),
 						new DialogInterface.OnClickListener () {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// TODO Auto-generated method stub
 								_saveFile(saveFile);
 							}
 							
 						}
 					);
 					alertDlg.setButton(
 						DialogInterface.BUTTON_NEGATIVE, 
 						getString(R.string.label_cancel), 
 						new DialogInterface.OnClickListener () {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// TODO Auto-generated method stub
 							}
 							
 						}
 					);
 					alertDlg.show();
 				} else {
 					_saveFile(saveFile);
 				}
 			}
 		} else {
 			Toast.makeText(EntranceActivity.this,
 				getString(R.string.err_sdcardnotmounted),
 				Toast.LENGTH_LONG
 			).show();
 		}
 	}
 	
 	/*
 	 * !!actually not used!!
 	 */	
 	private void _saveFile(File saveFile) {
 		FileOutputStream outStream;
 		try {
 			outStream = new FileOutputStream(saveFile);
 			outStream.write(mOutText.getBytes());
 			Toast.makeText(EntranceActivity.this,
 				"Saved.",
 				Toast.LENGTH_LONG
 			).show();
 			return;
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			Toast.makeText(EntranceActivity.this,
 				"File not found.",
 				Toast.LENGTH_LONG
 			).show();
 			return;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			Toast.makeText(EntranceActivity.this,
 				"Failed to do IO.",
 				Toast.LENGTH_LONG
 			).show();
 			return;
 		}
 	}
 
 	public static void setPrivilege(Integer privilege) {
 		if (privilege < 0 || privilege > 1) {
 			privilege = 1;
 		}
 		EntranceActivity.mPrivilege = privilege;
 		switch (privilege) {
 		case 0:
 			break;
 		case 1:
 			break;
 		default:
 			break;
 		}
 	}
 	
 	public static Integer getLimit() {
 		return mLimit;
 	}
 	
 	public static Integer getCurPage() {
 		return mCurPage;
 	}
 	
 	public static Integer getTotalPics() {
 		return mTotalPics;
 	}
 	
 	public static ArrayList<WeibouserInfo> getmUsrs() {
 		return mUsrs;
 	}
 	
 	public static void setmUsrs(ArrayList<WeibouserInfo> pics) {
 		mUsrs = pics;
 	}
 	
 	public static Integer getAccountId() {
 		return mAccountId;
 	}
 	
 	public static void setAccountId(Integer id) {
 		mAccountId = id;
 	}
 	
 	public static int getPrivilege() {
 		return mPrivilege;
 	}
 	
 	public static String getParamsAsStr(String... params) {
 		String sParams = "";
 		if (params.length >= 2 && (params.length % 2 == 0)) {
 			for (int i = 0; i < params.length; i += 2) {
 				sParams += (params[i] + "=" + params[i + 1]);
 				if (i != params.length - 2) {
 					sParams += "&";
 				}
 			}
 		}
 		return sParams;
 	}
 	
 	public static String getPhpContentByGet(String sPhp, String sParams) {
 		URL url;
 		try {
 			url = new URL(EntranceActivity.URL_SITE + sPhp + "?" + sParams);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.connect();
 			InputStream is = conn.getInputStream();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 			String line, content = "";
 			while ((line = reader.readLine()) != null) {
 				content += line;
 			}
 			return content;
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}		
 	}
 	
     public static ArrayList<WeibouserInfo> getPics(String... params) {
     	ArrayList<WeibouserInfo> usrs = new ArrayList<WeibouserInfo>();
     	
     	String sParams = getParamsAsStr(params);
     	SecureUrl su = new SecureUrl();
     	URLConnection conn = su.getConnection(URL_SITE + "picsinfo.php?" + sParams);
     	if (conn == null) return usrs;
     	try {
 	    	conn.connect();
 	    	InputStream is = conn.getInputStream();
 	    	Weibousers pbUsrs = Weibousers.parseFrom(is);
 	    	long id, uid;
 	    	for (Weibouser pbUsr: pbUsrs.getUsrList()) {
 	    		try {
 		    		id = Long.parseLong(pbUsr.getId());
 		    		uid = Long.parseLong(pbUsr.getUid());
 		    	} catch (NumberFormatException e) {
 		    		id = uid = 0;
 		    	}
 				WeibouserInfo wi = new WeibouserInfo(
 					id, uid, pbUsr.getScreenName(),
 					pbUsr.getName(), pbUsr.getProvince(), pbUsr.getCity(),
 					pbUsr.getLocation(), pbUsr.getDescription(), pbUsr.getUrl(),
 					pbUsr.getProfileImageUrl(), pbUsr.getDomain(), pbUsr.getGender(),
 					(long)pbUsr.getFollowersCount(), (long)pbUsr.getFriendsCount(), 
 					(long)pbUsr.getStatusesCount(), (long)pbUsr.getFavouritesCount(), 
 					pbUsr.getCreatedAt(), pbUsr.getFollowing(),
 					pbUsr.getAllowAllActMsg(), pbUsr.getGeoEnabled(), pbUsr.getVerified(), 
 					pbUsr.getStatusId(),
 					pbUsr.getClicks(), pbUsr.getLikes(), pbUsr.getDislikes());
 				usrs.add(wi);
 	    	}
     	} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return usrs;
     }
     
     /*
      * get the usable msg from post back text.
      * return value is in a string array:
      * the 1st one indicate whether it's successful or failed,
      * the 2nd one indicate the message body.
      */
     public static String[] getPhpMsg(String result) {
     	if (result == null) return null;
 		String msg = /*getString(R.string.tips_nothinghappened);*/result;//for debug
 		if (result.indexOf(SYMBOL_FAILED) != -1) {
 			msg = result.substring(
 				result.indexOf(SYMBOL_FAILED) + SYMBOL_FAILED.length(),
 				result.lastIndexOf(SYMBOL_FAILED)
 			);
 			return new String[] {SYMBOL_FAILED, msg};
 		}
 		if (result.indexOf(SYMBOL_SUCCESSFUL) != -1) {
 			msg = result.substring(
 				result.indexOf(SYMBOL_SUCCESSFUL) + SYMBOL_SUCCESSFUL.length(),
 				result.lastIndexOf(SYMBOL_SUCCESSFUL)
 			);
 			return new String[] {SYMBOL_SUCCESSFUL, msg};
 		}
 		return null;
     }
     
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		switch (requestCode) {
 		case REQUESTCODE_PICKFILE:
 			if (resultCode == RESULT_OK) {
 				AsyncUploader asyncUploader = new AsyncUploader(this, mAccountId);
 				asyncUploader.execute(data);
 			}
 			break;
 		case REQUESTCODE_BACKFROM:
 			if (resultCode == RESULT_OK) {
				long id = data.getLongExtra("id", 0);
 				int idx = getUsrIndexFromId(id, mUsrs);
 				int par = idx / mPageLimit + 1;
 				if (mPageBeforeBrow != mCurPage || par != mCurParagraph)
 				{
 					mPrgDlg.show();
 					mCurParagraph = par;
 					mPageUsrs.clear();
 					for (int i = (mCurParagraph - 1) * mPageLimit; i < mCurParagraph * mPageLimit; i++) {
 						mPageUsrs.add(mUsrs.get(i));
 					}
 					WeibouserInfoGridAdapter adapter = new WeibouserInfoGridAdapter(EntranceActivity.this, mPageUsrs, mGridPics);
 					mGridPics.setAdapter(adapter);
 					renewCurParagraphTitle();
 				}
 			}
 			break;
 		default:
 			break;
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
     
     /*
 	 * get total pages number
 	 */
     public int getTotalPagesNum() {
 		String sBackMsg = "";
 		sBackMsg = getPhpContentByGet(
 			"stats.php",
 			EntranceActivity.getParamsAsStr("total", "pages", "limit", mLimit.toString())
 		);
 		if (sBackMsg != null) {
 			String ss[] = getPhpMsg(sBackMsg);
 			if (ss != null && ss[0].equals(EntranceActivity.SYMBOL_SUCCESSFUL)) {
 				sBackMsg = ss[1];
 			} else {
 				sBackMsg = "-2";
 			}
 		} else {
 			sBackMsg = "0";
 		}
 		int i = 0;
 		try {
 			i = Integer.parseInt(sBackMsg);
 		} catch (NumberFormatException e) {
 			i = -1;
 		}
 		return i;
     }
     
     /*
      * get total pictures number
      */
     public int getTotalPicsNum() {
  		String sBackMsg = "";
 		sBackMsg = getPhpContentByGet(
 			"stats.php",
 			EntranceActivity.getParamsAsStr("total", "usrs")
 		);
 		if (sBackMsg != null) {
 			String ss[] = getPhpMsg(sBackMsg);
 			if (ss != null && ss[0].equals(EntranceActivity.SYMBOL_SUCCESSFUL)) {
 				sBackMsg = ss[1];
 			} else {
 				return mUsrs.size();
 			}
 		} else {
 			return mUsrs.size();
 		}
 		int i = 0;
 		try {
 			i = Integer.parseInt(sBackMsg);
 		} catch (NumberFormatException e) {
 			return mUsrs.size();
 		}
 		return i;
     }
     
     /*
      * renew the current paragraph informations
      */
     private void renewCurParagraphTitle() {
     	/*
     	String title = String.format(
 			getString(R.string.tips_pages),
 			(mCurParagraph - 1) * mPageLimit + (mCurPage - 1) * mLimit + 1,
 			(mCurParagraph - 1) * mPageLimit + (mCurPage - 1) * mLimit + mPageUsrs.size(),
 			(mCurPage - 1) * mLimit + 1,
 			mCurPage * mLimit > mTotalPics ? mTotalPics : mCurPage * mLimit,
 			mTotalPics
 		);
 		*/
     	String title = String.format(
 			getString(R.string.tips_pages),
 			(mCurParagraph - 1) * mPageLimit + (mCurPage - 1) * mLimit + 1,
 			(mCurParagraph - 1) * mPageLimit + (mCurPage - 1) * mLimit + mPageUsrs.size(),
 			mTotalPics
 		);
 
     	mSeekMain.setProgress(
     		(mCurPage - 1)
     		* (mLimit % mPageLimit == 0 ? mLimit / mPageLimit : mLimit / mPageLimit + 1)
     		+ mCurParagraph
     	);
     	mTextSeekPos.setVisibility(TextView.GONE);
     	mTextPageInfo.setVisibility(TextView.VISIBLE);
     	
     	/*
     	Toast.makeText(
 			this,
 			title,
 			Toast.LENGTH_LONG
 		).show();
     	*/
 			
 		mTextPageInfo.setText(title);
 		mLinearMainBottom.setVisibility(LinearLayout.VISIBLE);
 		AlphaAnimation anim = new AlphaAnimation(0.1f, 1.0f);
 		anim.setDuration(300);
 		mLinearMainBottom.startAnimation(anim);
     }
     
     /*
      * try to load pictures in mGridPics under background by using AsyncTask:
      * 1, get picture informations from remote DB
      * 2, get picture images from remote server and set to mGridPics
      */
     private class AsyncGridLoader extends AsyncTask <String, Object, WeibouserInfoGridAdapter> {
     	Context mContext;
     	
     	public AsyncGridLoader(Context c) {
     		this.mContext = c;
     	}
 
 		@Override
 		protected void onPostExecute(WeibouserInfoGridAdapter result) {
 			// TODO Auto-generated method stub
 			renewCurParagraphTitle();
 			if (result != null) {
 				mGridPics.setAdapter(result);
 			} else {
 				((EntranceActivity) mContext).mPrgDlg.dismiss();
 				AlertDialog alertDlg = new AlertDialog.Builder(mContext)
 					.setPositiveButton(R.string.label_ok, null)
 					.create();
 				alertDlg.setIcon(android.R.drawable.ic_dialog_info);
 				alertDlg.setTitle(getString(R.string.err_nopictures));
 				alertDlg.setMessage(getString(R.string.msg_nopictures));
 				WindowManager.LayoutParams lp = alertDlg.getWindow().getAttributes();
 		        lp.alpha = 0.9f;
 		        alertDlg.getWindow().setAttributes(lp);
 		        alertDlg.show();	
 			}
 			//super.onPostExecute(result);
 		}
 
 		@Override
 		protected WeibouserInfoGridAdapter doInBackground(String... params) {
 			// TODO Auto-generated method stub
 			if (mTotalPics <= 0) {
 				int i = getTotalPicsNum();
 				if (i < 0) {
 					Toast.makeText(
 						EntranceActivity.this,
 						getString(R.string.err_noconnection),
 						Toast.LENGTH_LONG
 					).show();
 				} else {
 					mTotalPics = i;
 					mTotalPages = (int) Math.ceil((float)mTotalPics / (float)mLimit);
 				}
 			}
 			mSeekMain.setMax(
 				mTotalPics == 0 ? 0 : ((int) Math.ceil((double)mTotalPics / (double)mPageLimit))
 			);
 			mUsrs = getPics(params);
 			mPageUsrs.clear();
 			for (int i = (mCurParagraph - 1) * mPageLimit; i < mUsrs.size() && i < mCurParagraph * mPageLimit; i++) {
 				mPageUsrs.add(mUsrs.get(i));
 			}
 			WeibouserInfoGridAdapter adapter = null;
 			if (mPageUsrs.size() != 0) {
 				adapter = new WeibouserInfoGridAdapter((EntranceActivity) mContext, mPageUsrs, mGridPics);
 			} 
 			return adapter;
 		}
     	
     }
         
     /*
      * Initialize stuff that the app needed, should include:
      * 1.try to get a local key
      * 2.automatically login if remembered
      */
     private class AsyncInit extends AsyncTask <Object, Object, Object>{
 
 		@Override
 		protected void onPostExecute(Object result) {
 			// TODO Auto-generated method stub
 			String[] msgs = (String[]) result;
 			/*
 			for (int i = 0; i < msgs.length; i++) {
 				if (!msgs[i].equals("")) {
 					Toast.makeText(
 						LetuseeActivity.this,
 						msgs[i],
 						Toast.LENGTH_SHORT
 					).show();
 				}
 			}
 			*/
 			if (!msgs[2].equals("")
 				&& !msgs[2].equals(getString(R.string.tips_alreadylast))
 				&& !msgs[2].equals(getString(R.string.err_wrongversioninfos))
 				&& !msgs[2].equals(getString(R.string.err_noversioninfos))
 				&& !msgs[2].equals(getString(R.string.err_noversion))
 			) {
 				String[] infos = msgs[2].split(",");
 				Intent intent = new Intent();
 				intent.putExtra("code", infos[0]);
 				intent.putExtra("name", infos[1]);
 				intent.putExtra("newname", infos[2]);
 				intent.setClass(EntranceActivity.this, UpdateActivity.class);
 				startActivity(intent);
 			}
 			super.onPostExecute(result);
 		}
 
 		@Override
 		protected Object doInBackground(Object... params) {
 			// TODO Auto-generated method stub
 			String[] msgs = {"", "", ""};
 			/*
 			 * try to get the client key
 			 */
 			SecureUrl su = new SecureUrl();
 			if (mClientKey.equals("")) {
 				String msg = getPhpContentByGet(
 					"key.php",
 					getParamsAsStr("serial", su.phpMd5(SERIAL_APP))
 				);
 				String ss[] = getPhpMsg(msg);
 				if (ss != null && ss[0].equals(SYMBOL_SUCCESSFUL)) {
 					mClientKey = ss[1];
 					SharedPreferences.Editor edit = mPreferences.edit();
 					edit.putString(CONFIG_CLIENTKEY, mClientKey);
 					edit.commit();
 					msgs[0] = getString(R.string.tips_succeededtogetserial);
 				} else {
 					msgs[0] = getString(R.string.err_failedtogetserial);
 				}
 			} else {
 				String msg = getPhpContentByGet(
 					"key.php",
 					getParamsAsStr("serial", su.phpMd5(SERIAL_APP), "key", mClientKey)
 				);
 				String[] ss = getPhpMsg(msg);
 				if (ss != null && ss[0].equals(SYMBOL_SUCCESSFUL)) {
 					msgs[0] = getString(R.string.tips_succeededtogetserial);
 				} else {
 					msgs[0] = getString(R.string.err_failedtoconfirmserial);
 				}
 			}
 			
 			/*
 	    	 * Auto login part begin
 	    	 */
 	        
 	        /*
 	    	 * Auto login part end
 	    	 */
 	        
 	        /*
 	         * check if update needed
 	         */
 	        try {
 				PackageInfo info = EntranceActivity.this.getPackageManager().getPackageInfo(EntranceActivity.this.getPackageName(), 0);
 				String content = getPhpContentByGet("ver.php", "");
 				if (content != null) {
 					String[] infos = content.split(",");
 					if (infos.length == 4) {
 						String sNewCode = infos[1];
 						String sNewName = infos[3];
 						Integer iNewCode = 0;
 						try {
 							iNewCode = Integer.parseInt(sNewCode);
 						} catch (NumberFormatException e) {
 							iNewCode = -1;
 						}
 						if (iNewCode > info.versionCode) {
 							msgs[2] = "" + info.versionCode + "," + info.versionName + "," + sNewName;
 						} else {
 							msgs[2] = getString(R.string.tips_alreadylast);
 						}
 					} else {
 						msgs[2] = getString(R.string.err_wrongversioninfos);
 					}
 				} else {
 					msgs[2] = getString(R.string.err_noversioninfos);
 				}
 			} catch (NameNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				msgs[2] = getString(R.string.err_noversion);
 			}
 			return msgs;
 		}
     	
     }
     
     public static String[] renewPageArgs(int direction) {
     	if (direction > 0) {
     		mCurPage++;
     		if (mCurPage > mTotalPages) {
 				mCurPage--;
 				return null;
 			}
     	} else {
     		mCurPage--;
     		if (mCurPage < 1) {
 				mCurPage++;
 				return null;
 			}
     	}
     	
 		int m = mCurTerms.size();
 		String[] args = new String[m + 6];
 		for (int i = 0; i < m; i++) {
 			args[i] = mCurTerms.get(i);
 		}
 		args[m] = "limit";
 		args[m + 1] = mLimit.toString();
 		args[m + 2] = "page";
 		args[m + 3] = mCurPage.toString();
 		args[m + 4] = "pb";
 		args[m + 5] = "1";
 		return args;
     }
     
     public void next() {
     	double maxParagraph = Math.ceil((float)mUsrs.size() / (float) mPageLimit);
     	mCurParagraph++;
 		if (mCurParagraph >  maxParagraph) {
 			mCurParagraph--;
 			String[] args = renewPageArgs(1);
 			if (args != null) {
 				mPrgDlg.show();
 				AsyncGridLoader agl = new AsyncGridLoader(EntranceActivity.this);
 				mCurParagraph = 1;
 				agl.execute(args);
 			}
 		} else {
 			mPrgDlg.show();
 			mPageUsrs.clear();
 			for (int i = (mCurParagraph -1) * mPageLimit; i < mCurParagraph * mPageLimit && i < mUsrs.size(); i++) {
 				mPageUsrs.add(mUsrs.get(i));
 			}
 			WeibouserInfoGridAdapter adapter = new WeibouserInfoGridAdapter(EntranceActivity.this, mPageUsrs, mGridPics);
 			mGridPics.setAdapter(adapter);
 			renewCurParagraphTitle();
 		}
     }
     
     public void previous() {
     	double maxParagraph = Math.ceil((float)mUsrs.size() / (float) mPageLimit);
     	mCurParagraph--;
 		if (mCurParagraph < 1) {
 			mCurParagraph++;
 			String[] args = renewPageArgs(-1);
 			if (args != null) {
 				mPrgDlg.show();
 				AsyncGridLoader agl = new AsyncGridLoader(EntranceActivity.this);
 				mCurParagraph = (int) maxParagraph;
 				agl.execute(args);
 			}
 		} else {
 			mPrgDlg.show();
 			mPageUsrs.clear();
 			for (int i = (mCurParagraph -1) * mPageLimit; i < mCurParagraph * mPageLimit && i < mUsrs.size(); i++) {
 				mPageUsrs.add(mUsrs.get(i));
 			}
 			WeibouserInfoGridAdapter adapter = new WeibouserInfoGridAdapter(EntranceActivity.this, mPageUsrs, mGridPics);
 			mGridPics.setAdapter(adapter);
 			renewCurParagraphTitle();
 		}
     }
     
     /*
      * GestureListsener zone begin
      */
     public boolean onTouch(View view, MotionEvent event) {
         return mGestureDetector.onTouchEvent(event);
     }
     
     class LetuseeGestureListener extends GestureDetector.SimpleOnGestureListener {
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			// TODO Auto-generated method stub
 			
 			if(e1.getX() > e2.getX()) {//move to left
 				next();
 			} else if (e1.getX() < e2.getX()) {
 				previous();
 			}
 			return super.onFling(e1, e2, velocityX, velocityY);
 		}
     	
     }
     /*
      * GestureListsener zone end
      */
 }
