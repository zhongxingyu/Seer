 package com.goodow.drive.android.activity;
 
 import com.goodow.android.drive.R;
 import com.goodow.api.services.account.Account;
 import com.goodow.drive.android.Interface.ILocalFragment;
 import com.goodow.drive.android.Interface.IRemoteControl;
 import com.goodow.drive.android.fragment.DataDetailFragment;
 import com.goodow.drive.android.fragment.DataListFragment;
 import com.goodow.drive.android.fragment.LeftMenuFragment;
 import com.goodow.drive.android.fragment.LessonListFragment;
 import com.goodow.drive.android.fragment.LocalResFragment;
 import com.goodow.drive.android.fragment.OfflineListFragment;
 import com.goodow.drive.android.global_data_cache.GlobalConstant;
 import com.goodow.drive.android.global_data_cache.GlobalConstant.DocumentIdAndDataKey;
 import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
 import com.goodow.drive.android.toolutils.LoginNetRequestTask;
 import com.goodow.drive.android.toolutils.OverallUncaughtException;
 import com.goodow.drive.android.toolutils.OverallUncaughtException.LoginAgain;
 import com.goodow.drive.android.toolutils.RemoteControlObserver;
 import com.goodow.drive.android.toolutils.RemoteControlObserver.SwitchFragment;
 import com.goodow.drive.android.toolutils.SimpleProgressDialog;
 import com.goodow.drive.android.toolutils.Tools;
 import com.goodow.drive.android.toolutils.ToolsFunctionForThisProgect;
 import com.goodow.realtime.CollaborativeMap;
 import com.goodow.realtime.Model;
 import com.goodow.realtime.android.CloudEndpointUtils;
 import com.goodow.realtime.android.RealtimeModule;
 import com.goodow.realtime.android.ServerAddress;
 
 import com.google.api.client.extensions.android.http.AndroidHttp;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.json.jackson2.JacksonFactory;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 
 import android.content.Intent;
 
 import android.app.Activity;
 
 import android.content.SharedPreferences;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.animation.ObjectAnimator;
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.res.ColorStateList;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.TextUtils;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.view.animation.DecelerateInterpolator;
 import android.view.animation.Interpolator;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import elemental.json.JsonArray;
 import roboguice.activity.RoboActivity;
 import roboguice.inject.ContentView;
 import roboguice.inject.InjectView;
 
 @ContentView(R.layout.activity_main)
 public class MainActivity extends RoboActivity {
   private final String TAG = this.getClass().getSimpleName();
 
   private RemoteControlObserver remoteControlObserver;
   private ActionBar actionBar;
   private String title;
 
   @InjectView(R.id.leftMenuLayout)
   private LinearLayout leftMenu;
   @InjectView(R.id.middleLayout)
   private LinearLayout middleLayout;
   @InjectView(R.id.dataDetailLayout)
   private LinearLayout dataDetailLayout;
   @InjectView(R.id.contentLayout)
   private LinearLayout contentLayout;
 
   private TextView openFailure_text;
   private ImageView openFailure_img;
 
   private FragmentManager fragmentManager;
 
   private ILocalFragment currentFragment;
   private ILocalFragment lastFragment;
 
   private final LeftMenuFragment leftMenuFragment = new LeftMenuFragment();
   private final DataListFragment dataListFragment = new DataListFragment();
   private final LocalResFragment localResFragment = new LocalResFragment();
   private final OfflineListFragment offlineListFragment = new OfflineListFragment();
   private final DataDetailFragment dataDetailFragment = new DataDetailFragment();
   private final LessonListFragment lessonListFragment = new LessonListFragment();
   @InjectView(R.id.pb_indeterminate)
   private ProgressBar pbIndeterminate;
 
   @SuppressLint("HandlerLeak")
   private final Handler handler = new Handler() {
     @Override
     public void handleMessage(Message msg) {
       currentFragment.backFragment();
     }
   };
 
   private float startPoint = 0;
 
   private boolean isShow = false;
 
   public DataDetailFragment getDataDetailFragment() {
     return dataDetailFragment;
   }
 
   public ILocalFragment getLastiRemoteDataFragment() {
     return lastFragment;
   }
 
   public LocalResFragment getLocalResFragment() {
     return localResFragment;
   }
 
   public IRemoteControl getRemoteControlObserver() {
     return remoteControlObserver;
   }
 
   public void goObservation() {
     if (null != remoteControlObserver) {
       String docId =
           "@tmp/" + GlobalDataCacheForMemorySingleton.getInstance().getUserId() + "/"
               + GlobalConstant.DocumentIdAndDataKey.REMOTECONTROLDOCID.getValue();
 
       remoteControlObserver.startObservation(docId, pbIndeterminate);
     }
   }
 
   public void hideLeftMenuLayout() {
     if (null != leftMenu && null != middleLayout) {
       Animation out = AnimationUtils.makeOutAnimation(this, false);
       out.setAnimationListener(new AnimationListener() {
         @Override
         public void onAnimationEnd(Animation animation) {
           leftMenuFragment.hiddenView();
 
           leftMenu.setVisibility(LinearLayout.INVISIBLE);
           middleLayout.setVisibility(LinearLayout.INVISIBLE);
           setLeftMenuLayoutX(0);// 重置其位置,防止负数循环叠加
           setLeftMenuLayoutX(-leftMenu.getWidth());
         }
 
         @Override
         public void onAnimationRepeat(Animation animation) {
         }
 
         @Override
         public void onAnimationStart(Animation animation) {
         }
       });
 
       leftMenu.startAnimation(out);
     }
   }
 
   public void notifyFragment() {
     leftMenuFragment.notifyData();
     dataListFragment.loadDocument();
     lessonListFragment.loadDocument();
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     super.onCreateOptionsMenu(menu);
 
     MenuItem back2Login = menu.add(0, 0, 0, R.string.ds_dialog_exit_button_text);
     back2Login.setIcon(R.drawable.exit_program);
     back2Login.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
     return true;
   }
 
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
     switch (keyCode) {
     case KeyEvent.KEYCODE_BACK:
       if (null != currentFragment) {
         currentFragment.backFragment();
 
         return true;
       }
     case KeyEvent.KEYCODE_HOME:
 
       return true;
     default:
       break;
     }
 
     return super.onKeyDown(keyCode, event);
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     super.onOptionsItemSelected(item);
 
     if (item.getItemId() == android.R.id.home) {
       if (leftMenu.getVisibility() == LinearLayout.VISIBLE) {
         hideLeftMenuLayout();
       } else {
         setLeftMenuLayoutX(0);
         showLeftMenuLayout();
 
         middleLayout.setVisibility(LinearLayout.VISIBLE);
       }
     } else if (item.getItemId() == 0) {
       // 退出程序 取消
       new AlertDialog.Builder(this).setPositiveButton(R.string.unsaved_dialog_cancel, new DialogInterface.OnClickListener() {
 
         @Override
         public void onClick(DialogInterface dialog, int which) {
 
         }
         // 确定
       }).setNegativeButton(R.string.trix_sheets_tab_menu_ok, new DialogInterface.OnClickListener() {
 
         @Override
         public void onClick(DialogInterface dialog, int which) {
           GlobalDataCacheForMemorySingleton.getInstance.setUserId(null);
           GlobalDataCacheForMemorySingleton.getInstance.setAccess_token(null);
 
           ToolsFunctionForThisProgect.quitApp(MainActivity.this);
         }
       }).setTitle("温馨提示").setMessage(R.string.back_dailogMessage).create().show();
     }
 
     return true;
   }
 
   @Override
   public boolean onTouchEvent(MotionEvent event) {
     return touchEvent(event);
   }
 
   public void openState(int visibility) {
     if (null != openFailure_text) {
       openFailure_text.setVisibility(visibility);
       openFailure_text.invalidate();
     }
 
     if (null != openFailure_img) {
       openFailure_img.setVisibility(visibility);
       openFailure_img.invalidate();
     }
   }
 
   public void restActionBarTitle() {
     actionBar.setTitle(R.string.app_name);
   }
 
   public void setActionBarContent(JsonArray currentPathList, Model model, final String docId) {
     ActionBar actionBar = getActionBar();
 
     actionBar.setDisplayShowCustomEnabled(true);
     actionBar.setDisplayShowTitleEnabled(false);
 
     LayoutInflater layoutInflater = LayoutInflater.from(this);
 
     View view = layoutInflater.inflate(R.layout.actionbar_view, null);
 
     LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.actionbar_view);
 
     for (int i = 0; i < currentPathList.length(); i++) {
       final CollaborativeMap currentMap = model.getObject(currentPathList.get(i).asString());
 
       if (null != currentMap) {
         final TextView newTextView = new TextView(this);
 
         newTextView.setId(i);
 
         newTextView.setText((String) currentMap.get("label") + " > ");
 
         ColorStateList colorStateList = getResources().getColorStateList(R.color.white);
         newTextView.setTextColor(colorStateList);
 
         newTextView.setBackgroundResource(R.drawable.selector_actionbar_text_color);
 
         newTextView.setOnClickListener(new OnClickListener() {
           @Override
           public void onClick(View v) {
             remoteControlObserver.changePath(currentMap.getId(), docId);
           }
         });
 
         linearLayout.addView(newTextView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
             LinearLayout.LayoutParams.WRAP_CONTENT));
       }
     }
 
     actionBar.setCustomView(view);
   }
 
   public void setActionBarTitle(String title) {
     actionBar.setTitle(title);
   }
 
   public void setDataDetailLayoutState(final int state) {
     if (dataDetailLayout.getVisibility() != state) {
       Interpolator accelerator = new AccelerateInterpolator();
       Interpolator decelerator = new DecelerateInterpolator();
 
       ObjectAnimator visToInvis = ObjectAnimator.ofFloat(dataDetailLayout, "rotationY", 0f, 90f);
       visToInvis.setDuration(500);
       visToInvis.setInterpolator(accelerator);
 
       final ObjectAnimator invisToVis = ObjectAnimator.ofFloat(dataDetailLayout, "rotationY", -90f, 0f);
       invisToVis.setDuration(500);
       invisToVis.setInterpolator(decelerator);
       visToInvis.addListener(new AnimatorListenerAdapter() {
         @Override
         public void onAnimationEnd(Animator anim) {
           invisToVis.start();
           dataDetailLayout.setVisibility(state);
         }
       });
       visToInvis.start();
     }
   }
 
   public void setLastiRemoteDataFragment(ILocalFragment lastiRemoteDataFragment) {
     this.lastFragment = lastiRemoteDataFragment;
   }
 
   public void setLocalFragment(ILocalFragment iRemoteDataFragment) {
     if (dataDetailLayout.getVisibility() == View.INVISIBLE) {
       this.currentFragment = iRemoteDataFragment;
     }
   }
 
   public void setLocalFragmentForDetail(ILocalFragment iRemoteDataFragment) {
     this.currentFragment = iRemoteDataFragment;
   }
 
   public void setOpenStateView(TextView textView, ImageView imageView) {
     openFailure_text = textView;
     openFailure_img = imageView;
   }
 
   public void showChangeUserDialog() {
     final View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_user_layout, null);
     final Dialog dialog = new Dialog(this, R.style.AlertDialog);
     dialog.show();
     Window window = dialog.getWindow();
     window.setContentView(dialogView);
 
     final EditText userNameEditText = (EditText) dialogView.findViewById(R.id.username_editText);
     final EditText passwordEditText = (EditText) dialogView.findViewById(R.id.password_editText);
 
     // 登录 按钮
     final Button loginButton = (Button) dialogView.findViewById(R.id.login_button);
     loginButton.setOnClickListener(new View.OnClickListener() {
 
       @Override
       public void onClick(View v) {
         String errorMessageString = "";
         String username = "";
         String password = "";
 
         do {
           username = userNameEditText.getText().toString();
           if (TextUtils.isEmpty(username)) {
             errorMessageString = "用户名不能为空";
 
             break;
           }
 
           password = passwordEditText.getText().toString();
           if (TextUtils.isEmpty(password)) {
             errorMessageString = "密码不能为空";
 
             break;
           }
 
           // 一切OK
           String[] params = { username, password };
           Account account = provideDevice(GlobalConstant.REALTIME_SERVER);
           final LoginNetRequestTask loginNetRequestTask = new LoginNetRequestTask(MainActivity.this, dialog, account);
           SimpleProgressDialog.show(MainActivity.this, new OnCancelListener() {
             @Override
             public void onCancel(DialogInterface dialog) {
               loginNetRequestTask.cancel(true);
             }
           });
           loginNetRequestTask.execute(params);
 
           return;
         } while (false);
 
         // 用户输入的信息错误
         Toast.makeText(MainActivity.this, errorMessageString, Toast.LENGTH_LONG).show();
       }
     });
 
     // 取消 按钮
     final Button cancelButton = (Button) dialogView.findViewById(R.id.cancel_button);
     cancelButton.setOnClickListener(new View.OnClickListener() {
 
       @Override
       public void onClick(View v) {
         // 点击取消按钮对话框消失
         dialog.dismiss();
       }
     });
   }
 
   public void switchFragment(DocumentIdAndDataKey doc) {
     Fragment newFragment = null;
 
     do {
       if (null == doc) {
 
         break;
       }
 
       switch (doc) {
       case LESSONDOCID:
         newFragment = lessonListFragment;
 
         break;
       case FAVORITESDOCID:
         newFragment = dataListFragment;
 
         break;
       case OFFLINEDOCID:
         newFragment = offlineListFragment;
 
         break;
       default:
         newFragment = dataListFragment;
 
         break;
       }
 
       if (null == newFragment) {
 
         break;
       }
 
       FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
       fragmentTransaction.replace(R.id.contentLayout, newFragment);
       fragmentTransaction.commitAllowingStateLoss();
 
       if (null != currentFragment) {
         currentFragment.loadDocument();
       }
     } while (false);
 
   }
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     // 添加捕获全局异常的处理方案
     Thread.currentThread().setUncaughtExceptionHandler(OverallUncaughtException.OVERALLUNCAUGHTEXCEPTION);
 
     OverallUncaughtException.OVERALLUNCAUGHTEXCEPTION.setLoginAgain(new LoginAgain() {
       @Override
      public void login(String errorinfo) {
         SharedPreferences sharedPreferences = getSharedPreferences(LoginNetRequestTask.LOGINPREFERENCESNAME, Activity.MODE_PRIVATE);
         String userName = sharedPreferences.getString(LoginNetRequestTask.USERNAME, "");
         String passWord = sharedPreferences.getString(LoginNetRequestTask.PASSWORD, "");
 
        Log.e(TAG, errorinfo);
 
         Intent intent = new Intent(MainActivity.this, MainActivity.class);
         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         intent.putExtra(LoginNetRequestTask.USERNAME, userName);
         intent.putExtra(LoginNetRequestTask.PASSWORD, passWord);
 
         MainActivity.this.startActivity(intent);
 
         android.os.Process.killProcess(android.os.Process.myPid());
       }
     });
 
     actionBar = getActionBar();
 
     if (null != title) {
       actionBar.setTitle(title);
     }
     actionBar.setDisplayHomeAsUpEnabled(true);
 
     fragmentManager = getFragmentManager();
     FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
     fragmentTransaction.replace(R.id.dataDetailLayout, dataDetailFragment);
     fragmentTransaction.replace(R.id.leftMenuLayout, leftMenuFragment);
 
     fragmentTransaction.commitAllowingStateLoss();
 
     // 捕获异常时,重新登录
     Bundle extras = getIntent().getExtras();
     if (null != extras) {
       String userName = extras.getString(LoginNetRequestTask.USERNAME);
       String passWord = extras.getString(LoginNetRequestTask.PASSWORD);
 
       String[] params = { userName, passWord };
       Account account = provideDevice(GlobalConstant.REALTIME_SERVER);
       final LoginNetRequestTask loginNetRequestTask = new LoginNetRequestTask(MainActivity.this, null, account);
       SimpleProgressDialog.show(MainActivity.this, new OnCancelListener() {
         @Override
         public void onCancel(DialogInterface dialog) {
           loginNetRequestTask.cancel(true);
         }
       });
       loginNetRequestTask.execute(params);
     }
   }
 
   @Override
   protected void onDestroy() {
     Log.i(TAG, "onDestroy");
     super.onDestroy();
   }
 
   @Override
   protected void onPause() {
     super.onPause();
 
     if (null != getActionBar().getTitle()) {
       title = (String) getActionBar().getTitle();
     }
   }
 
   @Override
   protected void onRestart() {
     Log.i(TAG, "onRestart");
     super.onRestart();
   }
 
   @Override
   protected void onResume() {
     Log.i(TAG, "onResume");
     super.onResume();
 
     contentLayout.setOnTouchListener(new OnTouchListener() {
 
       @Override
       public boolean onTouch(View v, MotionEvent event) {
         return touchEvent(event);
       }
     });
 
     middleLayout.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View v) {
         hideLeftMenuLayout();
       }
     });
 
     final GestureDetector gt = new GestureDetector(this, new SimpleOnGestureListener() {
       private final int FLING_MIN_DISTANCE = 10;// X或者y轴上移动的距离(像素)
       private final int FLING_MIN_VELOCITY = 20;// x或者y轴上的移动速度(像素/秒)
 
       @Override
       public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
         if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
           handler.sendEmptyMessage(1);
         }
 
         return true;
       }
     });
     dataDetailLayout.setLongClickable(true);
     dataDetailLayout.setOnTouchListener(new OnTouchListener() {
       @Override
       public boolean onTouch(View v, MotionEvent event) {
         return gt.onTouchEvent(event);
       }
     });
   }
 
   @Override
   protected void onSaveInstanceState(Bundle outState) {
 
   }
 
   @Override
   protected void onStart() {
     Log.i(TAG, "onStart");
     super.onStart();
 
     remoteControlObserver = new RemoteControlObserver(this, new SwitchFragment() {
       @Override
       public void switchFragment(DocumentIdAndDataKey doc) {
         MainActivity.this.switchFragment(doc);
       }
     });
     goObservation();
   }
 
   @Override
   protected void onStop() {
     Log.i(TAG, "onStop");
     super.onStop();
 
     remoteControlObserver.removeHandler();
   }
 
   @Provides
   @Singleton
   private Account provideDevice(@ServerAddress String serverAddress) {
     Account.Builder endpointBuilder =
         new Account.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), new HttpRequestInitializer() {
           @Override
           public void initialize(HttpRequest httpRequest) {
 
           }
         });
     endpointBuilder.setRootUrl(RealtimeModule.getEndpointRootUrl(serverAddress));
     return CloudEndpointUtils.updateBuilder(endpointBuilder).build();
   }
 
   private void setLeftMenuLayoutX(int x) {
     leftMenuFragment.setViewLayout(x);
     leftMenu.layout(x, leftMenu.getTop(), leftMenu.getRight(), leftMenu.getBottom());
   }
 
   private void showLeftMenuLayout() {
     Animation in = AnimationUtils.makeInAnimation(this, true);
     leftMenu.startAnimation(in);
     leftMenu.setVisibility(LinearLayout.VISIBLE);
 
     leftMenuFragment.showView();
 
     setLocalFragment(leftMenuFragment);
   }
 
   private boolean touchEvent(MotionEvent event) {
     switch (event.getAction()) {
     case MotionEvent.ACTION_DOWN:
       setLeftMenuLayoutX(0);
       setLeftMenuLayoutX(-leftMenu.getWidth());
 
       if (event.getX() < 40) {
         showLeftMenuLayout();
 
         startPoint = event.getX();
         isShow = true;
       }
 
       break;
     case MotionEvent.ACTION_UP:
       if ((Math.abs(leftMenu.getLeft()) <= leftMenu.getWidth() / 3) && leftMenu.getVisibility() == View.VISIBLE) {
         setLeftMenuLayoutX(0);
         middleLayout.setVisibility(View.VISIBLE);
       } else {
         hideLeftMenuLayout();
       }
 
       startPoint = 0;
       isShow = false;
 
       break;
     case MotionEvent.ACTION_MOVE:
       do {
         if (!isShow) {
 
           break;
         }
 
         if (Math.abs(event.getX() - startPoint) < 3) {
 
           break;
         }
 
         if (leftMenu.getLeft() >= 0) {
 
           break;
         }
 
         if (startPoint < event.getX()) {
           int add = leftMenu.getLeft() + (int) Tools.getRawSize(TypedValue.COMPLEX_UNIT_DIP, 6);
           if (add < 0) {
             setLeftMenuLayoutX(add);
           } else {
             setLeftMenuLayoutX(0);
             middleLayout.setVisibility(View.VISIBLE);
           }
         } else if (startPoint > event.getX()) {
           int reduce = leftMenu.getLeft() - (int) Tools.getRawSize(TypedValue.COMPLEX_UNIT_DIP, 6);
           if (Math.abs(reduce) < leftMenu.getWidth()) {
             setLeftMenuLayoutX(reduce);
           }
         }
 
         startPoint = event.getX();
       } while (false);
 
       break;
     default:
 
       break;
     }
 
     return true;
   }
 }
