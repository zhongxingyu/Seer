 package com.osastudio.newshub;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.code.microlog4android.Logger;
 import com.google.code.microlog4android.LoggerFactory;
 import com.google.code.microlog4android.config.PropertyConfigurator;
 import com.huadi.azker_phone.R;
 import com.osastudio.newshub.NewsApp.TempCacheData;
 import com.osastudio.newshub.cache.NewsAbstractCache;
 import com.osastudio.newshub.data.AppProperties;
 import com.osastudio.newshub.data.NewsAbstract;
 import com.osastudio.newshub.data.NewsAbstractList;
 import com.osastudio.newshub.data.NewsChannel;
 import com.osastudio.newshub.data.NewsChannelList;
 import com.osastudio.newshub.data.NewsResult;
 import com.osastudio.newshub.data.user.ValidateResult;
 import com.osastudio.newshub.library.PreferenceManager;
 import com.osastudio.newshub.library.PreferenceManager.PreferenceFiles;
 import com.osastudio.newshub.library.PreferenceManager.PreferenceItems;
 import com.osastudio.newshub.net.AppPropertiesApi;
 import com.osastudio.newshub.net.Net;
 import com.osastudio.newshub.net.NewsBaseApi;
 import com.osastudio.newshub.net.NewsChannelApi;
 import com.osastudio.newshub.net.UserApi;
 import com.osastudio.newshub.utils.NetworkHelper;
 import com.osastudio.newshub.utils.NewsResultAsyncTask;
 import com.osastudio.newshub.utils.Utils;
 import com.osastudio.newshub.utils.Utils.DialogConfirmCallback;
 import com.osastudio.newshub.widgets.AzkerGridLayout;
 import com.osastudio.newshub.widgets.AzkerGridLayout.OnGridItemClickListener;
 import com.osastudio.newshub.widgets.BaseAssistent;
 import com.osastudio.newshub.widgets.DivisionEditText;
 import com.osastudio.newshub.widgets.RegisterView;
 import com.osastudio.newshub.widgets.RegisterView.USER_TYPE;
 import com.osastudio.newshub.widgets.SlidePager;
 import com.osastudio.newshub.widgets.SlideSwitcher;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.RemoteException;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Rect;
 import android.graphics.drawable.BitmapDrawable;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.text.TextUtils;
 import android.util.DisplayMetrics;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.umeng.analytics.MobclickAgent;
 
 @SuppressLint("NewApi")
 public class CategoryActivity extends NewsBaseActivity {
    private static final String DEFAULT_BACKGROUND_FILE = "file:///android_asset/1.jpg";
 
    private static final float DEFAULT_WH_RATE = 5.0f / 8.2f;
 
    public static final String TYPE_NOTIFY_LIST = "1";
    public static final String TYPE_EXPERT_LIST = "3";
    public static final String TYPE_USETLSSUES_MOBILE = "5";
    public static final String TYPE_LESSON_LIST = "6";
 
    public static final int REQUEST_NOTIFY_LIST = 1;
    public static final int REQUEST_EXPERT_LIST = 3;
    public static final int REQUEST_USETLSSUES_MOBILE = 5;
    public static final int REQUEST_LESSON_LIST = 6;
    public static final int REQUEST_USER_INFO = 7;
    public static final int REQUEST_MESSAGE_JUMP = 100;
 
    public static final String LAUNCHER = "launcher";
    public static final String MESSAGE_SEND_TYPE = "message_send_type";
    public static final String MESSAGE_SERVICE_ID = "service_id";
    public static final String MESSAGE_SERVICE_TITLE = "service_title";
    public static final String MESSAGE_USER_ID = "student_id";
    public static final String MESSAGE_USER_NAME = "student_name";
 
    private AppProperties mAppProperties = null;
    private Bitmap mReceiveBmp = null;
    private Bitmap mCoverBmp = null;
    private RelativeLayout mRoot = null;
    private ImageView mCover = null;
    // private SlideSwitcher mSwitcher = null;
    // private Gallery mSwitcher = null;
    private SlidePager mSwitcher = null;
    private View mToolbar = null;
    private View mActivateLayout = null;
    private DivisionEditText mActivateEdit = null;
    private EditText mActivatePswd = null;
    private View mActivateBtn = null;
    private View mAccount_btn = null;
    private View mRecommend_btn = null;
    private View mExpertlist_btn = null;
    private View mFeedback_btn = null;
    private View mSetting_btn = null;
    private TextView mPage = null;
 
    // private ArrayList<CategoryData> mCategories = new
    // ArrayList<CategoryData>();
    private ArrayList<NewsChannel> mCategoryList = null;
    private int mTouchSlop;
    private int mBaseX, mBaseY;
    private int mDirection = -1; // 0 is preview; 1 is next;
    private int mInitX, mInitY;
    private boolean mbSwitchAble = true;
    private LayoutInflater mInflater = null;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private int mImageHeight = 0;
    private int mXMargin = 0;
    private int mYMargin = 0;
    private Display mDisplay;
    private float mdp = 1;
    private int mUserStatus = 3;
    private boolean mIsSplashShow = true;
 
    private LoadDataTask mTask = null;
    private ProgressDialog mDlg = null;
    private NewsApp mApp = null;
    private boolean mIsLoadFinish = false;
 
    private GalleryAdapter mGalleryAdapter = null;
    // private boolean mIsLauncher = true;
 
    private int mMessageType = -1;
    private String mServiceID = null;
    private String mServiceTitle;
    private boolean mNeedJump = false;
 
    private Handler mHandler = new Handler() {
 
       public void handleMessage(Message msg) {
          switch (msg.what) {
          case Net.NetIsOK:
 //            Utils.log("NetIsOK", "setupData");
 //            if (mNeedJump) {
 //               JumpToMessageTarget();
 //               mNeedJump = false;
 //            } else {
 //               setupData(0);
 //            }
             setupData(0);
             break;
          case Net.NetTipMessage_show:
         	if (!CategoryActivity.this.isFinishing()) {
 	            Utils.ShowConfirmDialog(
 	                  CategoryActivity.this,
 	                  CategoryActivity.this.getResources().getString(
 	                        R.string.net_isonline_tip_msg),
 	                  new DialogConfirmCallback() {
 	                     public void onConfirm(DialogInterface dialog) {
 	                        CategoryActivity.this.finish();
 	
 	                     }
 	                  });
         	 }
             break;
          }
          super.handleMessage(msg);
       }
    };
 
    protected ServiceConnection mNewsServiceConn = new ServiceConnection() {
       @Override
       public void onServiceDisconnected(ComponentName name) {
 
       }
 
       @Override
       public void onServiceConnected(ComponentName name, IBinder service) {
          setNewsService(INewsService.Stub.asInterface(service));
          try {
             getNewsService().checkAppDeadline();
          } catch (Exception e) {
             // e.printStackTrace();
          }
       }
    };
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       
       PropertyConfigurator.getConfigurator(this).configure();
 
       bindNewsService(mNewsServiceConn);
 
       mApp = (NewsApp) getApplication();
 
       Rect frame = new Rect();
       getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
       int statusBarHeight = frame.top;
 
       WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
       mDisplay = wm.getDefaultDisplay();
 
       mScreenWidth = mDisplay.getWidth();
       mScreenWidth = mScreenWidth > 0 ? mScreenWidth : 0;
       mScreenHeight = mDisplay.getHeight();
       mScreenHeight = mScreenHeight > 0 ? mScreenHeight : 0;
 
       DisplayMetrics dm = new DisplayMetrics();
       mDisplay.getMetrics(dm);
       mdp = dm.density;
 
       int top = getStatusHeight(this);
       mScreenHeight = (int) (mScreenHeight - top - 70 * mdp);
       if (mScreenWidth > 0 && mScreenHeight > 0) {
          if ((float) mScreenWidth / (float) mScreenHeight > DEFAULT_WH_RATE) {
             mScreenWidth = (int) (mScreenHeight * DEFAULT_WH_RATE + 0.5f);
             mXMargin = (int) ((mDisplay.getWidth() - mScreenWidth) / 2);
             mYMargin = (int) (35 * mdp);
          } else {
             mScreenHeight = (int) (mScreenWidth / DEFAULT_WH_RATE + 0.5f);
             mYMargin = (int) ((mDisplay.getHeight() - mScreenHeight) / 2);
          }
       }
 
       mImageHeight = mScreenHeight / 6;
 
       setContentView(R.layout.category_activity);
       findViews();
       
 
 
       Bundle extras = getIntent().getExtras();
       if (extras != null) {
          // mIsLauncher = extras.getBoolean(LAUNCHER);
          mMessageType = extras.getInt(MESSAGE_SEND_TYPE, -1);
          mServiceID = extras.getString(MESSAGE_SERVICE_ID);
          mServiceTitle = extras.getString(MESSAGE_SERVICE_TITLE);
          String userID = extras.getString(MESSAGE_USER_ID);
          
          if (userID != null && mMessageType >= 0 && mServiceID != null) {
             mNeedJump = true;
             mApp.setCurrentUserId(userID);
             View cover = findViewById(R.id.cover_layout);
             cover.setVisibility(View.GONE);
          }
       }
       
 //      mMessageType = Utils.MESSAGE_SEND_TYPE_RECOMMEND;
 //      mServiceID = "1";
 //      mNeedJump = true;
 //      View cover = findViewById(R.id.cover_layout);
 //      cover.setVisibility(View.GONE);
       
 
       ViewConfiguration configuration = ViewConfiguration.get(this);
       mTouchSlop = configuration.getScaledTouchSlop();
 
       mInflater = LayoutInflater.from(this);
 
 
       Utils.createLocalDiskPath(Utils.TEMP_FOLDER);
       Utils.createLocalDiskPath(Utils.TEMP_CACHE_FOLDER);
       Utils.createLocalFile(Utils.TEMP_FOLDER + ".nomedia");
       
 
       if (mNeedJump) {
          JumpToMessageTarget();
       } else {
          checkNetWork();
       }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
       super.onNewIntent(intent);
       Bundle extras = intent.getExtras();
       if (extras != null) {
          // mIsLauncher = extras.getBoolean(LAUNCHER);
          mMessageType = extras.getInt(MESSAGE_SEND_TYPE, -1);
          mServiceID = extras.getString(MESSAGE_SERVICE_ID);
          String userID = extras.getString(MESSAGE_USER_ID);
          
          if (userID != null && mMessageType >= 0 && mServiceID != null) {
             mNeedJump = true;
             mApp.setCurrentUserId(userID);
             View cover = findViewById(R.id.cover_layout);
             cover.setVisibility(View.GONE);
          }
       }
 
 //      Utils.log("category", "onNewIntent "+mNeedJump+ " type="+mMessageType);
       if (mNeedJump) {
          JumpToMessageTarget();
       } else {
          checkNetWork();
       }
    }
 
    private boolean mIsExit = false;
    private Toast mExitToast = null;
 
    @Override
    public void onBackPressed() {
 //      if (!mIsLoadFinish) {
 //         return;
 //      }
       View cover = findViewById(R.id.cover_layout);
       if (cover.getVisibility() == View.VISIBLE && mUserStatus == 3 && mIsLoadFinish) {
          hideCover();
       } else if (mIsExit) {
          if (mExitToast != null) {
             mExitToast.cancel();
          }
          super.onBackPressed();
       } else {
          mIsExit = true;
          mExitToast = Toast.makeText(this, R.string.exit_msg,
                Toast.LENGTH_SHORT);
          mExitToast.show();
          mHandler.postDelayed(new Runnable() {
             public void run() {
                mIsExit = false;
                if (mExitToast != null) {
                   mExitToast.cancel();
                }
             }
          }, 2000);
       }
    }
 
    private Net mNet = null;
 
    private void checkNetWork() {
       if (mNet == null) {
       mNet = new Net(this, mHandler);
       }
       if (mNet.PhoneIsOnLine()) {
          mIsLoadFinish = false;
          // mDlg = Utils.showProgressDlg(this, null);
          mNet.ExecutNetTask(this, NewsBaseApi.getWebServer(this));
       } else {
          Utils.ShowConfirmDialog(this,
                getString(R.string.phone_isonline_tip_msg),
                new DialogConfirmCallback() {
                   public void onConfirm(DialogInterface dialog) {
                      CategoryActivity.this.finish();
 
                   }
                });
       }
    }
 
    public static int getStatusHeight(Activity activity) {
       int statusHeight = 0;
       Rect localRect = new Rect();
       activity.getWindow().getDecorView()
             .getWindowVisibleDisplayFrame(localRect);
       statusHeight = localRect.top;
       if (0 == statusHeight) {
          Class<?> localClass;
          try {
             localClass = Class.forName("com.android.internal.R$dimen");
             Object localObject = localClass.newInstance();
             int i5 = Integer.parseInt(localClass.getField("status_bar_height")
                   .get(localObject).toString());
             statusHeight = activity.getResources().getDimensionPixelSize(i5);
          } catch (ClassNotFoundException e) {
             e.printStackTrace();
          } catch (IllegalAccessException e) {
             e.printStackTrace();
          } catch (InstantiationException e) {
             e.printStackTrace();
          } catch (NumberFormatException e) {
             e.printStackTrace();
          } catch (IllegalArgumentException e) {
             e.printStackTrace();
          } catch (SecurityException e) {
             e.printStackTrace();
          } catch (NoSuchFieldException e) {
             e.printStackTrace();
          }
       }
       return statusHeight;
    }
 
    private void findViews() {
       int top = getStatusHeight(this);
       int margin = mYMargin;// (int) ((mScreenHeight - mScreenWidth * 4 / 3 -
       // top) / 2 - 5 * mdp);
 
       mRoot = (RelativeLayout) findViewById(R.id.root);
       mToolbar = findViewById(R.id.tool_bar);
       
       Bitmap bg = getImageFromAssetsFile("1.jpg");
       if (bg != null) {
          mRoot.setBackgroundDrawable(new BitmapDrawable(bg));
       }
 
       if (mXMargin < 20 * mdp) {
          RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) mToolbar
                .getLayoutParams();
          rlp.rightMargin = mXMargin;
          mToolbar.setLayoutParams(rlp);
       }
 
       mCover = (ImageView) findViewById(R.id.cover);
 
       mCoverBmp = getImageFromAssetsFile("0.jpg");
       if (mCoverBmp != null) {
          mCover.setImageBitmap(mCoverBmp);
       }
       // mSwitcher = (SlideSwitcher) findViewById(R.id.switcher);
       // mSwitcher = (Gallery)findViewById(R.id.switcher);
       mSwitcher = (SlidePager) findViewById(R.id.switcher);
       mSwitcher.setVisibility(View.INVISIBLE);
       mToolbar.setVisibility(View.INVISIBLE);
       mSwitcher.setOnPageChangeListener(new OnPageChangeListener() {
 
          @Override
          public void onPageSelected(int arg0) {
             setPageText(arg0);
 
          }
 
          @Override
          public void onPageScrolled(int arg0, float arg1, int arg2) {
             // TODO Auto-generated method stub
 
          }
 
          @Override
          public void onPageScrollStateChanged(int arg0) {
             // TODO Auto-generated method stub
 
          }
       });
 
       mActivateLayout = findViewById(R.id.activite);
       mActivateLayout.setVisibility(View.GONE);
       mActivateEdit = (DivisionEditText) findViewById(R.id.activite_edit);
       mActivatePswd = (EditText)findViewById(R.id.password_edit);
       mActivateBtn = findViewById(R.id.activite_btn);
       mActivateBtn.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
             String activate_str = mActivateEdit.getResult();
             String pswd_str = mActivatePswd.getText().toString();
             if (activate_str == null || activate_str.equals("")) {
                Utils.ShowConfirmDialog(CategoryActivity.this, getString(R.string.phone_num_error), null);
             } else if (pswd_str == null || pswd_str.equals("")) {
                Utils.ShowConfirmDialog(CategoryActivity.this, getString(R.string.activite_msg_pswd), null);
             }
             if (activate_str != null && !activate_str.equals("") && 
                   pswd_str != null && !pswd_str.equals("")) {
                if (mDlg == null) {
                mDlg = Utils.showProgressDlg(CategoryActivity.this, null);
                }
                new ActivateTask(CategoryActivity.this).execute(activate_str, pswd_str);
             }
 
          }
       });
 
       mAccount_btn = findViewById(R.id.account_btn);
       RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) mAccount_btn
             .getLayoutParams();
       rlp.topMargin = margin;
       mAccount_btn.setLayoutParams(rlp);
       mAccount_btn.setOnClickListener(new OnClickListener() {
 
          public void onClick(View v) {
             startUserInfosActivity();
 
          }
       });
 
       mRecommend_btn = findViewById(R.id.recommend);
       mRecommend_btn.setOnClickListener(new OnClickListener() {
 
          public void onClick(View v) {
             startListActivity(Utils.RECOMMEND_LIST_TYPE, null);
 
          }
       });
 
       mExpertlist_btn = findViewById(R.id.expertlist);
       mExpertlist_btn.setOnClickListener(new OnClickListener() {
 
          public void onClick(View v) {
             startListActivity(Utils.EXPERT_LIST_TYPE, null);
 
          }
       });
 
       mFeedback_btn = findViewById(R.id.feedback);
       mFeedback_btn.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
             startFeedbackActivity();
          }
       });
 
       mSetting_btn = findViewById(R.id.settings);
       LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mSetting_btn
             .getLayoutParams();
       lp.bottomMargin = margin;
       mSetting_btn.setLayoutParams(lp);
       mSetting_btn.setOnClickListener(new OnClickListener() {
 
          @Override
          public void onClick(View v) {
             startSettingActivity();
 
          }
       });
 
       mPage = (TextView) findViewById(R.id.page);
       rlp = (RelativeLayout.LayoutParams) mPage.getLayoutParams();
       rlp.height = margin;
       mPage.setLayoutParams(rlp);
    }
 
    private void setPageText(int current) {
       int total;
       if (mCategoryList == null || mCategoryList.size() == 0) {
          return;
       }
       if (mCategoryList.size() % 8 == 0) {
          total = mCategoryList.size() / 8;
       } else {
          total = mCategoryList.size() / 8 + 1;
       }
       String page = String.valueOf(current + 1) + "/" + String.valueOf(total);
       mPage.setText(page);
    }
 
    @Override
    protected void onDestroy() {
       super.onDestroy();
 
       unbindNewService(mNewsServiceConn);
 
       if (mTask != null && !mTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
          mTask.cancel(true);
          mTask = null;
       }
 
       if (mLoadBitmapTask != null
             && !mLoadBitmapTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
          mLoadBitmapTask.cancel(true);
          mLoadBitmapTask = null;
       }
 
       if (mIconList != null) {
          for (int i = 0; i < mIconList.size(); i++) {
             if (mIconList.get(i) != null && mIconList.get(i).mBmp != null
                   && !mIconList.get(i).mBmp.isRecycled()) {
                mIconList.get(i).mBmp.recycle();
                mIconList.get(i).mBmp = null;
             }
          }
       }
    }
 
    private Bitmap getImageFromAssetsFile(String fileName) {
       Bitmap bmp = null;
       AssetManager am = getResources().getAssets();
       try {
          InputStream is = am.open(fileName);
          bmp = BitmapFactory.decodeStream(is);
          is.close();
       } catch (IOException e) {
          e.printStackTrace();
       }
 
       return bmp;
 
    }
 
    private void hideCover() {
       // hideSlideMsg();
       View cover = findViewById(R.id.cover_layout);
       if (cover.getVisibility() == View.VISIBLE && mUserStatus == 3) {
          mSwitcher.setVisibility(View.VISIBLE);
          mToolbar.setVisibility(View.VISIBLE);
          Animation anim = AnimationUtils.loadAnimation(this,
                R.anim.pull_out_to_top);
          cover.setVisibility(View.GONE);
          cover.setAnimation(anim);
       }
    }
 
    private void showCover() {
       View cover = findViewById(R.id.cover_layout);
       if (cover.getVisibility() != View.VISIBLE) {
          Animation anim = AnimationUtils.loadAnimation(this,
                R.anim.pull_in_from_top);
          anim.setAnimationListener(new AnimationListener() {
 
             @Override
             public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
 
             }
 
             @Override
             public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
 
             }
 
             @Override
             public void onAnimationEnd(Animation animation) {
                mSwitcher.setVisibility(View.INVISIBLE);
                mToolbar.setVisibility(View.INVISIBLE);
 
             }
          });
          cover.setAnimation(anim);
          cover.setVisibility(View.VISIBLE);
       }
 
    }
 
    private RegisterView mRegisterView = null;
 
    private void showRegisterView() {
       RegisterView registerDlg = new RegisterView(this,
             R.style.Theme_PageDialog, mDisplay.getWidth(),
             mDisplay.getHeight(), USER_TYPE.REGISTER);
       registerDlg.setDialogConfirmCallback(mRegisterCallback);
       registerDlg.show();
    }
 
    private AlphaAnimation mAlphaAnim = null;
 
    private void showSlideMsg() {
       final View view = findViewById(R.id.msg_text);
       if (view.getVisibility() != View.VISIBLE) {
          view.setVisibility(View.VISIBLE);
 
          mAlphaAnim = new AlphaAnimation(1.0f, 0);
          mAlphaAnim.setDuration(2000);
          mAlphaAnim.setRepeatCount(Animation.INFINITE);
          mAlphaAnim.setRepeatMode(Animation.REVERSE);
          view.setAnimation(mAlphaAnim);
          mAlphaAnim.start();
       }
    }
 
    private void hideSlideMsg() {
       final View view = findViewById(R.id.msg_text);
       if (view.getVisibility() == View.VISIBLE) {
          if (mAlphaAnim != null) {
             mAlphaAnim.cancel();
          }
          view.setVisibility(View.GONE);
 
       }
    }
 
    private void setupData(int stage) {
       SharedPreferences prefs = getSharedPreferences(
             PreferenceFiles.APP_SETTINGS, Context.MODE_PRIVATE);
       if (prefs != null) {
          String userId = prefs.getString(PreferenceItems.USER_ID, null);
          if (userId != null) {
             mApp.setCurrentUserId(userId);
          }
       }
 
       mTask = new LoadDataTask(this);
       mTask.execute(stage);
    }
 
    public boolean dispatchTouchEvent(MotionEvent event) {
       if (!mIsLoadFinish) {
          return true;
       }
       // mGd.onTouchEvent(event);
       int y = (int) event.getRawY();
       int x = (int) event.getRawX();
       switch (event.getAction()) {
       case MotionEvent.ACTION_DOWN:
          mInitX = x;
          mInitY = y;
          mDirection = -1;
          mbSwitchAble = true;
          break;
       case MotionEvent.ACTION_MOVE:
          if (y - mBaseY > mTouchSlop
                && Math.abs(mInitX - x) < Math.abs(mInitY - y)) {
             showCover();
             mbSwitchAble = false;
          } else if (mBaseY - y > mTouchSlop
                && Math.abs(mInitX - x) < Math.abs(mInitY - y)) {
             hideCover();
             mbSwitchAble = false;
          }
          break;
       case MotionEvent.ACTION_UP:
 
          break;
       }
       mBaseX = x;
       mBaseY = y;
       if (!mbSwitchAble) {
          return true;
       } else {
          return super.dispatchTouchEvent(event);
       }
    }
 
    private class ActivateTask extends NewsResultAsyncTask<String, Void, NewsResult> {
       public ActivateTask(Context context) {
          super(context);
          // TODO Auto-generated constructor stub
       }
 
       @Override
       protected NewsResult doInBackground(String... params) {
          return UserApi.validate(getApplicationContext(),
                params[0], params[1]);
       }
 
       @Override
       public void onPostExecute(NewsResult rtn) {
          super.onPostExecute(rtn);
          if (mDlg != null) {
             mDlg.dismiss();
             mDlg = null;
          }
          ValidateResult result = (ValidateResult)rtn;
          if (result != null && result.isSuccess()) {
             mActivateLayout.setVisibility(View.GONE);
             if (!result.hasUserIds()) {
                showRegisterView();
             } else {
                List<String> userIds = result.getUserIds();
                String curId = mApp.getCurrentUserId();
                int idIndex = -1;
                if (userIds != null && userIds.size() > 0) {
                   if (curId != null) {
                      for (int i = 0; i < userIds.size(); i++) {
                         if (curId.equals(userIds.get(i))) {
                            idIndex = i;
                         }
                      }
                   }
                   if (idIndex < 0) {
                      mApp.setCurrentUserId(userIds.get(0));
                      mUserStatus = 3;
                   }
                }
                mTask = new LoadDataTask(CategoryActivity.this);
                mTask.execute(0);
             }
          } else {
             mActivateEdit.init();
             mActivatePswd.setText(null);
          }
       }
    }
 
    private class BMP_Item {
       String mPath;
       Bitmap mBitmap;
    }
 
    // private class LoadImageTask extends AsyncTask<Void, BMP_Item, Void> {
    //
    // @Override
    // protected Void doInBackground(Void... params) {
    // BMP_Item item = new BMP_Item();
    // item.mPath = DEFAULT_SPLASH_FILE;
    // item.mBitmap = Utils.loadBitmap(DEFAULT_SPLASH_FILE, mScreenWidth,
    // 0, 0);
    // if (item.mBitmap != null) {
    // publishProgress(item);
    // }
    // item = new BMP_Item();
    // item.mPath = DEFAULT_BACKGROUND_FILE;
    // item.mBitmap = Utils.loadBitmap(DEFAULT_SPLASH_FILE, mScreenWidth,
    // 0, 0);
    // if (item.mBitmap != null) {
    // publishProgress(item);
    // }
    // return null;
    // }
    //
    // @Override
    // protected void onProgressUpdate(BMP_Item... values) {
    // BMP_Item item = values[0];
    // super.onProgressUpdate(values);
    // }
    //
    // }
 
    private class LoadDataTask extends NewsResultAsyncTask<Integer, Integer, NewsResult> {
 
       public LoadDataTask(Context context) {
          super(context);
       }
 
       @Override
       protected NewsResult doInBackground(Integer... params) {
          int startFlag = params[0];
 
          switch (startFlag) {
          case 0:
             mAppProperties = AppPropertiesApi
                   .getAppProperties(CategoryActivity.this);
             if (mAppProperties != null && mAppProperties .isSuccess()) {
                mUserStatus = mAppProperties.getUserStatus();
                if (mUserStatus == 3) {
                   List<String> userIds = mAppProperties.getUserIds();
                   String curId = mApp.getCurrentUserId();
                   int idIndex = -1;
                   if (userIds != null && userIds.size() > 0) {
                      if (curId != null) {
                         for (int i = 0; i < userIds.size(); i++) {
                            if (curId.equals(userIds.get(i))) {
                               idIndex = i;
                            }
                         }
                      }
                      if (idIndex < 0) {
                         mApp.setCurrentUserId(userIds.get(0));
                      }
                   }
 
                } else {
                   mApp.setCurrentUserId(null);
                   if (mUserStatus == 4 || mUserStatus == 5) {
                      return mAppProperties;
                   }
                }
                publishProgress(0);
 
             }
             else {
                return mAppProperties;
             }
          case 1:
             if (mApp.getCurrentUserId() != null && !mApp.getCurrentUserId().equals("")) {
                NewsChannelList channel_list = NewsChannelApi
                      .getNewsChannelList(getApplicationContext(),
                            mApp.getCurrentUserId());
                if (channel_list != null && channel_list .isSuccess()) {
                   mCategoryList = (ArrayList<NewsChannel>) channel_list
                         .getChannelList();
 //                  ArrayList<NewsChannel> list = (ArrayList<NewsChannel>) channel_list
 //                      .getChannelList();
 //                  if (mCategoryList == null) {
 //                     mCategoryList = new ArrayList<NewsChannel>();
 //                  } else {
 //                     mCategoryList.clear();
 //                  }
 //                  for (int i = 0; i < list.size(); i++) {
 //                     NewsChannel channel = list.get(i);
 //                     for(int j = 0; j < 4; j++) {
 //                        NewsChannel item = new NewsChannel();
 //                        item.setChannelId(channel.getChannelId());
 //                        item.setIconId(channel.getIconId());
 //                        item.setIconUrl(channel.getIconUrl());
 //                        item.setResultCode(channel.getResultCode());
 //                        item.setResultDescription(channel.getResultDescription());
 //                        item.setTitleColor(channel.getTitleColor());
 //                        item.setTitleId(channel.getTitleId());
 //                        item.setTitleName(channel.getTitleName());
 //                        item.setTitleType(item.getTitleType());
 //                        mCategoryList.add(item);
 //                     }
 //                  }
                } else {
                   return channel_list;
                }
             }
 
          case 2:
             if (mAppProperties != null) {
                mReceiveBmp = Utils.getBitmapFromUrl(
                      mAppProperties.getSplashImageUrl(), true);
 
 //               Utils.log("LoadDataTask", "get cover bmp=" + mCoverBmp + "// "
 //                     + mAppProperties.getSplashImageUrl());
                publishProgress(2);
             }
 
          }
 
          return null;
       }
 
       @Override
       protected void onProgressUpdate(Integer... values) {
          int status = values[0];
          switch (status) {
          case 0:
             if (mUserStatus == 1) {
                mActivateLayout.setVisibility(View.VISIBLE);
             }else {
                mActivateLayout.setVisibility(View.GONE);
                if (mUserStatus == 2) {
                   showRegisterView();
                }
             }
             break;
          case 1:
             break;
          case 2:
             if (mCover != null && mReceiveBmp != null) {
                mCover.setImageBitmap(mReceiveBmp);
                if (mCoverBmp != null && !mCoverBmp.isRecycled()) {
                   mCoverBmp.recycle();
                }
                mCoverBmp = mReceiveBmp;
                if (mUserStatus == 3) {
                   showSlideMsg();
                }
             }
             break;
          }
 
          super.onProgressUpdate(values);
       }
 
       @Override
       public void onPostExecute(NewsResult result) {
     	 super.onPostExecute(result);
 
          mIsLoadFinish = true;
          if (mDlg != null) {
             Utils.closeProgressDlg(mDlg);
             mDlg = null;
          }
 //         if (result == null || result.isSuccess()){
             if (mUserStatus == 4) {
                String msg = CategoryActivity.this.getString(R.string.msg_user_forbidden);
                Utils.ShowConfirmDialog(CategoryActivity.this, msg, new DialogConfirmCallback() {
                   @Override
                   public void onConfirm(DialogInterface dialog) {
                      CategoryActivity.this.finish();
                   }
                });
             } else if (mUserStatus == 5) {
                String msg = CategoryActivity.this.getString(R.string.msg_user_no_authority);
                Utils.ShowConfirmDialog(CategoryActivity.this, msg, new DialogConfirmCallback() {
                   @Override
                   public void onConfirm(DialogInterface dialog) {
                      CategoryActivity.this.finish();
                   }
                });
             }
             
             try {
                getNewsService().hasNewVersion(mAppProperties, false);
                getNewsService().checkNewsMessage();
             } catch (Exception e) {
                // e.printStackTrace();
             }
             if (mCategoryList != null && mCategoryList.size() > 0) {
                SwitchAssistent assistent = new SwitchAssistent();
                mSwitcher.setAssistant(assistent);
                setPageText(mSwitcher.getCurrentItem());
                mLoadBitmapTask = new LoadBitmapTask();
                mLoadBitmapTask.execute();
             }
 //         }
          mTask = null;
          
       }
 
       @Override
       protected void onCancelled() {
          if (mDlg != null) {
             Utils.closeProgressDlg(mDlg);
             mDlg = null;
          }
          mTask = null;
          super.onCancelled();
       }
 
    }
 
    private ArrayList<IconData> mIconList = null;
    private LoadBitmapTask mLoadBitmapTask = null;
 
    private class IconData {
       String mIconUrl = null;
       Bitmap mBmp = null;
    }
 
    private class LoadBitmapTask extends AsyncTask<Void, Void, Void> {
 
       @Override
       protected Void doInBackground(Void... params) {
          if (mIconList == null) {
             mIconList = new ArrayList<IconData>();
          }
          for (int i = 0; i < mCategoryList.size(); i++) {
             boolean bNeedDecode = true;
             NewsChannel channel = mCategoryList.get(i);
             for (int j = 0; i < mIconList.size(); i++) {
                IconData data = mIconList.get(j);
                if (channel.getIconUrl().equals(data.mIconUrl)) {
                   bNeedDecode = false;
                   break;
                }
             }
             if (bNeedDecode) {
                Bitmap bmp = Utils.getBitmapFromUrl(channel.getIconUrl(),
                      mScreenHeight / 12, true);
 
 //               Utils.log("LoadBitmapTask",
 //                     "decode icon " + channel.getIconUrl() + " " + bmp);
                if (bmp != null) {
                   IconData iconData = new IconData();
                   iconData.mIconUrl = channel.getIconUrl();
                   iconData.mBmp = bmp;
                   mIconList.add(iconData);
                   publishProgress();
                }
             }
 
          }
          return null;
       }
 
       @Override
       protected void onProgressUpdate(Void... values) {
          SwitchAssistent assistent = new SwitchAssistent();
          mSwitcher.setAssistant(assistent);
          // if (mGalleryAdapter == null) {
          // mGalleryAdapter = new GalleryAdapter();
          //
          // mSwitcher.setAdapter(mGalleryAdapter);
          // } else {
          // mGalleryAdapter.notifyDataSetChanged();
          // }
 
          super.onProgressUpdate(values);
       }
 
       @Override
       protected void onPostExecute(Void result) {
          mLoadBitmapTask = null;
          super.onPostExecute(result);
       }
 
    }
 
    private void setupGridLayout(AzkerGridLayout grid_layout, int page) {
       grid_layout.setAssistant(new GridLayoutAssistent(page));
    }
 
    private class GalleryAdapter extends BaseAdapter {
 
       @Override
       public int getCount() {
          if (mCategoryList.size() % 8 == 0) {
             return mCategoryList.size() / 8;
          } else {
             return mCategoryList.size() / 8 + 1;
          }
       }
 
       @Override
       public Object getItem(int position) {
          // TODO Auto-generated method stub
          return null;
       }
 
       @Override
       public long getItemId(int position) {
          // TODO Auto-generated method stub
          return position;
       }
 
       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
          if (convertView == null) {
             convertView = mInflater.inflate(R.layout.category_view, null);
             Gallery.LayoutParams glp = new Gallery.LayoutParams(
                   ViewGroup.LayoutParams.FILL_PARENT,
                   ViewGroup.LayoutParams.FILL_PARENT);
             convertView.setLayoutParams(glp);
          }
          AzkerGridLayout grid_layout = (AzkerGridLayout) convertView
                .findViewById(R.id.grid);
          RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) grid_layout
                .getLayoutParams();
          if (lp != null) {
             lp.leftMargin = mXMargin;
             lp.height = mScreenHeight;
             lp.width = mScreenHeight / 2;
             grid_layout.setLayoutParams(lp);
          }
 
          setupGridLayout(grid_layout, position);
 
          grid_layout.setGridItemClickListener(new GridItemClickListener());
          return convertView;
 
       }
 
    }
 
    private class SwitchAssistent extends BaseAssistent {
 
       @Override
       public int getCount() {
          if (mCategoryList.size() % 8 == 0) {
             return mCategoryList.size() / 8;
          } else {
             return mCategoryList.size() / 8 + 1;
          }
       }
 
       @Override
       public Object getItem(int position) {
          // TODO Auto-generated method stub
          return null;
       }
 
       @Override
       public View getView(int position, View convertView) {
          if (convertView == null) {
             convertView = mInflater.inflate(R.layout.category_view, null);
          }
          AzkerGridLayout grid_layout = (AzkerGridLayout) convertView
                .findViewById(R.id.grid);
          RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) grid_layout
                .getLayoutParams();
          if (lp != null) {
             lp.leftMargin = mXMargin;
             lp.height = mScreenHeight;
             lp.width = mScreenHeight / 2;
             // lp.width = (int) (mScreenWidth * 2 / 3 + 10 * mdp);
             // lp.height = lp.width * 2;
             grid_layout.setLayoutParams(lp);
          }
 
          setupGridLayout(grid_layout, position);
 
          grid_layout.setGridItemClickListener(new GridItemClickListener());
          return convertView;
 
       }
 
    }
 
    private class GridItemClickListener implements OnGridItemClickListener {
 
       @Override
       public void onClick(int position, View v) {
          // int page = mSwitcher.getCurrentIndex();
          // int page = mSwitcher.getSelectedItemPosition();
          int page = mSwitcher.getCurrentItem();
          int index = page * 8 + position;
          if (index < mCategoryList.size()) {
             NewsChannel data = mCategoryList.get(index);
             if (data.getTitleType() > 0) {
                startNextActivity(index);
             }
          }
 
       }
 
    }
 
    private class GridLayoutAssistent extends BaseAssistent {
       int mPage = 0;
 
       public GridLayoutAssistent(int thispage) {
          mPage = thispage;
       }
 
       @Override
       public int getCount() {
          int count = 0;
          // if ((mPage+1) * 8 <= mCategories.size()) {
          // count = 8;
          // } else {
          // count = 8 - ((mPage+1) * 8 - mCategories.size());
          // }
          if ((mPage + 1) * 8 <= mCategoryList.size()) {
             count = 8;
          } else {
             count = 8 - ((mPage + 1) * 8 - mCategoryList.size());
          }
          return count;
       }
 
       @Override
       public Object getItem(int position) {
          int index = mPage * 8 + position;
          // if (index < mCategories.size()) {
          // return mCategories.get(index);
          // } else {
          // return null;
          // }
          if (index < mCategoryList.size()) {
             return mCategoryList.get(index);
          } else {
             return null;
          }
       }
 
       @Override
       public View getView(int position, View convertView) {
          int index = mPage * 8 + position;
          // if (index < mCategories.size()) {
          if (index < mCategoryList.size()) {
             RelativeLayout category = (RelativeLayout) convertView;
             if (category == null) {
                LayoutInflater inflater = LayoutInflater
                      .from(CategoryActivity.this);
                category = (RelativeLayout) inflater.inflate(
                      R.layout.category_item, null);
             }
 
             View base = category.findViewById(R.id.base);
             ImageView iv = (ImageView) category.findViewById(R.id.image);
             LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) iv
                   .getLayoutParams();
             if (llp != null) {
                llp.height = mImageHeight;
                iv.setLayoutParams(llp);
             }
 
             TextView tv = (TextView) category.findViewById(R.id.name);
             tv.setEnabled(false);
             tv.setClickable(false);
             tv.setFocusable(false);
             tv.setFocusableInTouchMode(false);
 
             NewsChannel data = mCategoryList.get(index);
             base.setBackgroundColor(data.getTitleColor());
             tv.setText(data.getTitleName());
             if (mIconList != null) {
                for (int i = 0; i < mIconList.size(); i++) {
                   IconData icondata = mIconList.get(i);
                   if (icondata != null
                         && icondata.mIconUrl.equals(data.getIconUrl())) {
                      if (icondata.mBmp != null && !icondata.mBmp.isRecycled()) {
                         iv.setImageBitmap(icondata.mBmp);
                      }
                   }
                }
             }
             return category;
          } else {
             return null;
          }
       }
    }
 
    private void startNextActivity(int index) {
       if (index < mCategoryList.size()) {
          NewsChannel data = mCategoryList.get(index);
          if (data.getTitleType() > 0) {
             int type = data.getTitleType();
             switch (type) {
             case Utils.IMPORT_NOTIFY_TYPE:
             case Utils.IMPORT_EXPERT_TYPE:
                ArrayList<TempCacheData> cacheList = new ArrayList<TempCacheData>();
                cacheList.add(new TempCacheData(data.getChannelId()));
                mApp.setTempCache(cacheList);
 
                startPageActivity(type, data.getTitleName());
                break;
             case Utils.USER_ISSUES_TYPE:
             case Utils.DAILY_REMINDER_TYPE:
             case Utils.EXPERT_LIST_TYPE:
             case Utils.NOTIFY_LIST_TYPE:
             case Utils.RECOMMEND_LIST_TYPE:
                startListActivity(type, data.getTitleName());
                break;
             case Utils.LESSON_LIST_TYPE:
                startSummaryActivity(mCategoryList.get(index));
                break;
             case Utils.EXAM_TYPE:
                startExamActivity(data);
                break;
             }
          }
       }
 
    }
    
    private void startExamActivity(NewsChannel channel) {
       Intent intent = new Intent(this, ExamActivity.class);
       intent.putExtra(ExamActivity.EXTRA_EXAM_ID, channel.getChannelId());
       intent.putExtra(ExamActivity.EXTRA_EXAM_TITLE, channel.getTitleName());
       startActivity(intent);
    }
 
    private void startSummaryActivity(NewsChannel data) {
       int requestCode = REQUEST_LESSON_LIST;
 
       Intent it = new Intent(this, SummaryActivity.class);
       it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       it.putExtra(SummaryActivity.CHANNEL_TYPE, Utils.LESSON_LIST_TYPE);
       it.putExtra(SummaryActivity.CHANNEL_ID, data.getChannelId());
       it.putExtra(SummaryActivity.CHANNEL_TITLE, data.getTitleName());
       startActivity(it);
    }
 
    private void startUserInfosActivity() {
       Intent it = new Intent(this, UserInfosActivity.class);
       startActivityForResult(it, REQUEST_USER_INFO);
    }
 
    private void startPageActivity(int listType, String title) {
       Intent it = new Intent(this, PageActivity.class);
       it.putExtra(PageActivity.PAGE_TYPE, listType);
       it.putExtra(PageActivity.START_INDEX, 0);
       it.putExtra(PageActivity.CATEGORY_TITLE, title);
       startActivity(it);
    }
 
    private void startListActivity(int listtype, String title) {
       int title_resid = 0;
       if (title == null) {
          switch (listtype) {
          case Utils.RECOMMEND_LIST_TYPE:
             title_resid = R.string.recommend;
             break;
          case Utils.EXPERT_LIST_TYPE:
             title_resid = R.string.expertlist;
             break;
          case Utils.USER_ISSUES_TYPE:
             title_resid = R.string.user_issues_list;
             break;
          case Utils.NOTIFY_LIST_TYPE:
             title_resid = R.string.default_notice_title;
             break;
          case Utils.DAILY_REMINDER_TYPE:
             title_resid = R.string.default_daily_reminder_title;
             break;
          }
          if (title_resid > 0) {
             title = getString(title_resid);
          }
       }
       Intent it = new Intent(this, AzkerListActivity.class);
       it.putExtra(AzkerListActivity.LIST_TYPE, listtype);
       it.putExtra(AzkerListActivity.LIST_TITLE, title);
       startActivity(it);
    }
 
    private void startFeedbackActivity() {
 
       Intent it = new Intent(this, FeedbackActivity.class);
       startActivity(it);
    }
 
    private void startSettingActivity() {
 
       Intent it = new Intent(this, SettingActivity.class);
       startActivity(it);
    }
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
       switch (requestCode) {
       case REQUEST_USER_INFO:
          if (resultCode == RESULT_OK) {
             SharedPreferences prefs = getSharedPreferences(
                   PreferenceFiles.APP_SETTINGS, Context.MODE_PRIVATE);
             if (prefs != null) {
                String userId = prefs.getString(PreferenceItems.USER_ID, null);
 
                if (userId != null) {
                   if (!userId.equals(mApp.getCurrentUserId())) {
                      mApp.setCurrentUserId(userId);
                      mDlg = Utils.showProgressDlg(this, null);
                      mTask = new LoadDataTask(this);
                      mTask.execute(1);
                   }
                }
             }
          }
          break;
       case REQUEST_MESSAGE_JUMP:
          findViews();
          View cover = findViewById(R.id.cover_layout);
          cover.setVisibility(View.GONE);
          mSwitcher.setVisibility(View.VISIBLE);
          mToolbar.setVisibility(View.VISIBLE);
          
          String cur_id = mApp.getPrefsManager().getUserId();
          mApp.setCurrentUserId(cur_id);
          
          setupData(0);
          
          break;
       }
       super.onActivityResult(requestCode, resultCode, data);
    }
 
    private DialogConfirmCallback mRegisterCallback = new DialogConfirmCallback() {
 
       @Override
       public void onConfirm(DialogInterface dialog) {
          mDlg = Utils.showProgressDlg(CategoryActivity.this, null);
          mTask = new LoadDataTask(CategoryActivity.this);
          mTask.execute(0);
 
       }
 
    };
 
    private void JumpToMessageTarget() {
       mNeedJump = false;
       if (mNet == null) {
          mNet = new Net(this, mHandler);
       }
       if (!mNet.PhoneIsOnLine()) {
          Utils.ShowConfirmDialog(this,
                getString(R.string.phone_isonline_tip_msg),
                new DialogConfirmCallback() {
                   public void onConfirm(DialogInterface dialog) {
                      CategoryActivity.this.finish();
 
                   }
                });
          return;
       }
       Intent it = new Intent();
       switch (mMessageType) {
       case Utils.MESSAGE_SEND_TYPE_DAILY_REMINDER:
          it.setClass(this, AzkerListActivity.class);
          it.putExtra(AzkerListActivity.LIST_TYPE, Utils.DAILY_REMINDER_TYPE);
          it.putExtra(AzkerListActivity.LIST_TITLE,
                getString(R.string.default_daily_reminder_title));
          break;
       case Utils.MESSAGE_SEND_TYPE_NOTIFY:
       case Utils.MESSAGE_SEND_TYPE_NOTIFY_BACK:
       case Utils.MESSAGE_SEND_TYPE_RECOMMEND:
          ArrayList<TempCacheData> cacheList = new ArrayList<TempCacheData>();
          cacheList.add(new TempCacheData(mServiceID));
          mApp.setTempCache(cacheList);
          int type =  Utils.IMPORT_NOTIFY_TYPE;
          int title_src = R.string.default_notice_title;
          if (mMessageType == Utils.MESSAGE_SEND_TYPE_RECOMMEND) {
             type =  Utils.RECOMMEND_LIST_TYPE;
             title_src = R.string.recommend;
          }
          it.setClass(this, PageActivity.class);
          it.putExtra(PageActivity.PAGE_TYPE, type);
          it.putExtra(PageActivity.START_INDEX, 0);
          it.putExtra(PageActivity.CATEGORY_TITLE, getString(title_src));
          break;
       case Utils.MESSAGE_SEND_TYPE_LESSON:
          NewsAbstractCache cache = getNewsAbstractCache();
          NewsAbstractList abstractList = new NewsAbstractList();
          List<NewsAbstract> list = abstractList.getList();
          if (list != null) {
             list.clear();
             NewsAbstract abstractData = new NewsAbstract();
             abstractData.setId(mServiceID);
             list.add(abstractData);
          }
          
          cache.setAbstracts(abstractList);
          it.setClass(this, FileActivity.class);
          it.putExtra(FileActivity.START_INDEX, 0);
          
          break;
       case Utils.MESSAGE_SEND_TYPE_EXAM:
          it.setClass(this, ExamActivity.class);
          it.putExtra(ExamActivity.EXTRA_EXAM_ID, mServiceID);
          it.putExtra(ExamActivity.EXTRA_EXAM_TITLE, mServiceTitle);
          break;
 
       }
       startActivityForResult(it, REQUEST_MESSAGE_JUMP);
 
    }
 
 }
