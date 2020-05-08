 package com.foreveross.chameleon.activity;
 
 import java.io.IOException;
 import java.lang.ref.SoftReference;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.cordova.CallbackContext;
 import org.apache.cordova.Config;
 import org.apache.cordova.CordovaInterface;
 import org.apache.cordova.CordovaPlugin;
 import org.apache.cordova.CordovaWebView;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.ActivityInfo;
 import android.content.res.AssetManager;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ResizeLayout;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewTreeObserver;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationSet;
 import android.view.animation.TranslateAnimation;
 import android.webkit.WebView;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.csair.impc.R;
 import com.foreveross.chameleon.Application;
 import com.foreveross.chameleon.BroadcastConstans;
 import com.foreveross.chameleon.TmpConstants;
 import com.foreveross.chameleon.URL;
 import com.foreveross.chameleon.event.EventBus;
 import com.foreveross.chameleon.event.MessageCountChangeEvent;
 import com.foreveross.chameleon.event.PresenceEvent;
 import com.foreveross.chameleon.pad.component.NativeViewFragment;
 import com.foreveross.chameleon.pad.fragment.ChatRoomFragment;
 import com.foreveross.chameleon.pad.fragment.DroidGapFragment;
 import com.foreveross.chameleon.pad.fragment.GroupFragment;
 import com.foreveross.chameleon.pad.fragment.ModuleDetailFragment;
 import com.foreveross.chameleon.pad.fragment.MucAddFirendFragment;
 import com.foreveross.chameleon.pad.fragment.PageFinishListener;
 import com.foreveross.chameleon.pad.fragment.PageStartListener;
 import com.foreveross.chameleon.pad.fragment.ParentDroidFragment;
 import com.foreveross.chameleon.pad.fragment.ViewCreateCallBack;
 import com.foreveross.chameleon.pad.modle.SkinModel;
 import com.foreveross.chameleon.phone.modules.CubeModule;
 import com.foreveross.chameleon.phone.modules.CubeModuleManager;
 import com.foreveross.chameleon.phone.modules.task.ThreadPlatformUtils;
 import com.foreveross.chameleon.phone.muc.MucManagerFragment;
 import com.foreveross.chameleon.store.core.ModelCreator;
 import com.foreveross.chameleon.store.core.ModelFinder;
 import com.foreveross.chameleon.store.core.StaticReference;
 import com.foreveross.chameleon.store.model.MultiUserInfoModel;
 import com.foreveross.chameleon.store.model.SystemInfoModel;
 import com.foreveross.chameleon.update.AutoCheckUpdateListener;
 import com.foreveross.chameleon.update.CheckUpdateTask;
 import com.foreveross.chameleon.util.FileCopeTool;
 import com.foreveross.chameleon.util.PadUtils;
 import com.foreveross.chameleon.util.Pool;
 import com.foreveross.chameleon.util.Preferences;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.squareup.otto.Subscribe;
 
 import common.extras.plugins.CubeLoginPlugin;
 import common.extras.plugins.ExtroSystem;
 
 public class FacadeActivity extends FragmentActivity implements
 		CordovaInterface {
 	private AutoCheckUpdateListener acuListener;
 	private CheckUpdateTask updateTask;
 	private ExecutorService pool = Executors.newCachedThreadPool();
 	private Dialog splashDialog;
 	private int splashscreenTime;
 	private int xMove = 0;
 
 	private TextView tv;
 	/**
 	 * 底层fragment
 	 */
 	private ParentDroidFragment parentFragment;
 	/**
 	 * 
 	 * 右侧fragment容器
 	 */
 	private FrameLayout detailContainer;
 	private NativeViewFragment nativeViewFragment;
 	private String url = null;
 	private BroadcastReceiver broadcastReceiver;
 	private Dialog skinDialog;
 	private View skinView;
 	private int temp;
 	private View layout;
 	private View contentView;
 	private Handler handler;
 	private LinearLayout skinLinearlayout;
 	// 窗口移动的速度
 	private final static int MOVEMENT_SPEED = 16;
 	private Stack<Fragment> stack = new Stack<Fragment>();
 	private boolean isPad = false;
 	private Application application = null;
 	private List<SoftReference<Bitmap>> bitmapRefList = new ArrayList<SoftReference<Bitmap>>();
 
 	private WindowManager wm = null;
 	private WindowManager.LayoutParams wmParams = null;
 	private ImageView myFV = null;
 
 	private ProgressBar autoDownload;
 	private RelativeLayout autoDownloadlayout;
 
 	private View views;
 
 	public final static int SYSTEMDIALOG = 300;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.components_test);
 		Config.init(this);
 		init();
 		application.showTopWindow(this);
 		checkUpdate(url);
 		loadUrl(url);
 		registerReceiver();
 	}

 	public void checkUpdate(String url) {
		if (url != null && url.contains("login")) {
			acuListener = new AutoCheckUpdateListener(this);
			updateTask = new CheckUpdateTask(Application.class.cast(
					FacadeActivity.this.getApplication()).getCubeApplication(),
					acuListener);
			updateTask.execute();
 		}
 	}
 
 	private boolean hasShowTheme = false;
 
 	boolean popRight = false;
 	boolean pending = false;
 
 	private static final int BIGGER = 1;
 	private static final int SMALLER = 2;
 	private static final int MSG_RESIZE = 1;
 	private static final int HEIGHT_THREADHOLD = 30;
 
 	class InputHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_RESIZE: {
 				if (msg.arg1 == BIGGER) {
 					Intent i = new Intent(BroadcastConstans.KEYBOARDSHOW);
 					i.putExtra("show", false);
 					FacadeActivity.this.sendBroadcast(i);
 
 					/*
 					 * findViewById(R.id.bottom_layout)
 					 * .setVisibility(View.VISIBLE);
 					 */} else {
 					Intent i = new Intent(BroadcastConstans.KEYBOARDSHOW);
 					i.putExtra("show", true);
 					FacadeActivity.this.sendBroadcast(i);/*
 														 * findViewById(R.id.
 														 * bottom_layout
 														 * ).setVisibility
 														 * (View.GONE);
 														 */
 				}
 			}
 				break;
 			default:
 				break;
 			}
 			super.handleMessage(msg);
 		}
 	}
 
 	private InputHandler mHandler = new InputHandler();
 
 	/**
 	 * [一句话功能简述]<BR>
 	 * [功能详细描述] 2013-10-12 下午5:01:20
 	 */
 	@Override
 	protected void onStart() {
 		super.onStart();
 	}
 
 	public void registerReceiver() {
 		hasShowTheme = false;
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(BroadcastConstans.PUSH_MODULE_MESSAGE);
 		filter.addAction(BroadcastConstans.MODULE_WEB);
 		filter.addAction(BroadcastConstans.MODULE_PROGRESS);
 		filter.addAction("com.xmpp.mutipleAccount");
 		filter.addAction(BroadcastConstans.CHANGE_SKIN);
 		filter.addAction(BroadcastConstans.KEYBOARDSHOW);
 		filter.addAction(BroadcastConstans.RefreshMainPage);
 		filter.addAction(BroadcastConstans.SHOWDIOLOG);
 		filter.addAction(BroadcastConstans.CANCEELDIOLOG);
 		filter.addAction(BroadcastConstans.MODULE_AUTODOWNLOAD_PROGERSS);
 		filter.addAction(BroadcastConstans.JumpToCubeManager);
 		broadcastReceiver = new BroadcastReceiver() {
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				// 换肤功能
 				Log.i("AAA", "ACTION = " + intent.getAction());
 				if (intent.getAction()
 						.equals(BroadcastConstans.MODULE_PROGRESS)) {
 					String identifier = intent.getStringExtra("identifier");
 					int progress = intent.getIntExtra("progress", 0);
 					sendProcessToJs(identifier, progress);
 				} else if (intent.getAction().equals(
 						BroadcastConstans.CHANGE_SKIN)) {
 					// parentFragment.enableAccelerate();
 					if (hasShowTheme && skinDialog != null
 							&& !skinDialog.isShowing()) {
 						skinDialog.show();
 						return;
 					}
 					String tempurl = "www/pad";
 					FileCopeTool tool = new FileCopeTool(context);
 					String temp = tool.getFromAssets(tempurl
 							+ "/theme/theme.json");
 					Gson gson = new Gson();
 					final List<SkinModel> retList = gson.fromJson(temp,
 							new TypeToken<List<SkinModel>>() {
 							}.getType());
 					LayoutInflater inflater = LayoutInflater
 							.from(FacadeActivity.this);
 					skinView = inflater.inflate(R.layout.skin_dialog_layout,
 							null);
 					skinLinearlayout = (LinearLayout) skinView
 							.findViewById(R.id.skin_layout);
 					for (int i = 0; i < retList.size(); i++) {
 						final int temps = i;
 						View v = LayoutInflater.from(FacadeActivity.this)
 								.inflate(R.layout.skin_view, null);
 						final ImageView iv = (ImageView) v
 								.findViewById(R.id.skin_iv);
 						AssetManager asm = getAssets();
 						java.io.InputStream inputStream = null;
 						String url = retList.get(i).getImgThum();
 						try {
 							inputStream = asm.open(tempurl + url);// 根据URL
 																	// 获取资源的流
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 
 						BitmapFactory.Options opts = new BitmapFactory.Options();
 						opts.inSampleSize = 4;
 						// 把流转正BITMAP对象
 						Bitmap bitmap = BitmapFactory.decodeStream(inputStream,
 								null, opts);
 						bitmapRefList.add(new SoftReference<Bitmap>(bitmap));
 						iv.setImageBitmap(bitmap);
 
 						iv.setScaleType(ImageView.ScaleType.FIT_XY);
 						v.setLayoutParams(new LayoutParams(260, 170));
 						skinLinearlayout.addView(v);// 把带有按钮的VIEW 增加到视图当中
 
 						ininDialog();
 
 						iv.setOnClickListener(new OnClickListener() {
 							@Override
 							public void onClick(View arg0) {
 								changeSkin(retList.get(temps).getName());
 								cancelDialog();
 								// skinView.postDelayed(new Runnable() {
 								//
 								// @Override
 								// public void run() {
 								// parentFragment.disableAccelerate();
 								// }
 								// }, 4000);
 
 							}
 						});
 						hasShowTheme = true;
 					}
 				} else if (intent.getAction().equals(
 						BroadcastConstans.PUSH_MODULE_MESSAGE)) {
 					String identifier = intent.getStringExtra("identifier");
 					int count = intent.getIntExtra("count", 0);
 					Log.i("AAA", "count = " + count);
 					receiveMessage(identifier, count);
 				} else if (intent.getAction().equals(
 						BroadcastConstans.RefreshMainPage)) {
 
 					String identifier = intent.getStringExtra("identifier");
 					String type = intent.getStringExtra("type");
 					CubeModule module = CubeModuleManager.getInstance()
 							.getIdentifier_new_version_map().get(identifier);
 					if (module == null) {
 						module = CubeModuleManager.getInstance()
 								.getCubeModuleByIdentifier(identifier);
 						if (module.isAutoDownload()) {
 							endAnimAutoDownload();
 						}
 					}
 					refreshMainPage(identifier, type, module);
 				} else if (intent.getAction().equals(
 						BroadcastConstans.MODULE_WEB)) {
 					String identifier = intent.getStringExtra("identifier");
 					String type = intent.getStringExtra("type");
 					CubeModule module = CubeModuleManager.getInstance()
 							.getIdentifier_new_version_map().get(identifier);
 					if (module == null) {
 						module = CubeModuleManager.getInstance()
 								.getCubeModuleByIdentifier(identifier);
 					}
 
 					refreshModule(identifier, type, module);
 				} else if (intent.getAction().equals("com.xmpp.mutipleAccount")) {
 					sendBroadcast(new Intent("push.model.change"));
 					// 被逼下线
 					Dialog dialog = new AlertDialog.Builder(FacadeActivity.this)
 							.setCancelable(false)
 							.setTitle("提示")
 							.setMessage("你的账号已在别处被登录，请重启应用")
 							.setPositiveButton("确定",
 									new DialogInterface.OnClickListener() {
 										@Override
 										public void onClick(
 												DialogInterface dialog,
 												int which) {
 											popRightAll();
 											findViewById(R.id.child_frame)
 													.setVisibility(
 															View.INVISIBLE);
 											Application.class
 													.cast(FacadeActivity.this
 															.getApplicationContext())
 													.logOff();
 										}
 									}).create();
 					dialog.show();
 				} else if (intent.getAction().equals(
 						BroadcastConstans.SHOWDIOLOG)) {
 					// showAuotdownloadDialog(false);
 					startAnimAutoDownload();
 					// autoDownloadlayout.setVisibility(View.VISIBLE);
 				} else if (intent.getAction().equals(
 						BroadcastConstans.CANCEELDIOLOG)) {
 					// cancelAuotdownloadDialog();
 					endAnimAutoDownload();
 
 				} else if (intent.getAction().equals(
 						BroadcastConstans.MODULE_AUTODOWNLOAD_PROGERSS)) {
 					if (tv == null) {
 						return;
 					}
 					tv.setText("正在下载... "
 							+ (ThreadPlatformUtils.getAutodownLoadallcount() - ThreadPlatformUtils
 									.getAutodownLoadTaskCout()) + "/"
 							+ ThreadPlatformUtils.getAutodownLoadallcount());
 				} else if (intent.getAction().equals(
 						BroadcastConstans.KEYBOARDSHOW)) {
 					Boolean b = intent.getBooleanExtra("show", false);
 					sentKeyboardShow(b);
 				}
 				// 处理管理页面的返回问题
 				else if (intent.getAction().equals(
 						BroadcastConstans.JumpToCubeManager)) {
 					if (url != null) {
 						url = url + "#manager";
 					}
 				}
 
 			}
 		};
 		registerReceiver(broadcastReceiver, filter);
 	}
 
 	/**
 	 * 换肤弹窗的大小设置
 	 */
 	public void ininDialog() {
 		if (skinDialog == null) {
 			skinDialog = new Dialog(FacadeActivity.this, R.style.skin_dialog);
 			Window w = skinDialog.getWindow();
 			WindowManager.LayoutParams lp = w.getAttributes();
 			lp.x = 1024;
 			lp.y = 1024;
 			skinDialog.setContentView(skinView);
 		}
 
 		if (skinDialog.isShowing()) {
 			cancelDialog();
 		}
 		skinDialog.setCancelable(true);
 		skinDialog.show();
 	}
 
 	public void startAnimAutoDownload() {
 
 		autoDownloadlayout.setVisibility(View.VISIBLE);
 		TranslateAnimation translateAnimation = new TranslateAnimation(0f, 0f,
 				40f, 0f);// 动画效果
 		translateAnimation.setDuration(1000);
 		autoDownloadlayout.startAnimation(translateAnimation);// 调用开始动画
 	}
 
 	public void endAnimAutoDownload() {
 
 		TranslateAnimation translateAnimations = new TranslateAnimation(0f, 0f,
 				0f, 80f);// 动画效果
 		translateAnimations.setDuration(1000);
 
 		AnimationSet animationSet = new AnimationSet(true);
 		animationSet.addAnimation(translateAnimations);
 		animationSet.setAnimationListener(new AnimationListener() {
 			public void onAnimationStart(Animation animation) {
 
 			}
 
 			public void onAnimationRepeat(Animation animation) {
 			}
 
 			public void onAnimationEnd(Animation animation) {
 				autoDownloadlayout.clearAnimation();
 				if (autoDownloadlayout.isShown())
 					autoDownloadlayout.setVisibility(View.GONE);
 			}
 		});
 		autoDownloadlayout.startAnimation(animationSet);// 调用开始动画
 
 	}
 
 	public void cancelDialog() {
 		if (skinDialog == null) {
 			return;
 		}
 		if (skinDialog.isShowing()) {
 			skinDialog.cancel();
 		}
 	}
 
 	public Dialog progressDialog;
 
 	public void showAuotdownloadDialog(boolean cancelable) {
 		if (progressDialog == null) {
 
 			progressDialog = new Dialog(this, R.style.dialog);
 			LayoutInflater mInflater = (LayoutInflater) getActivity()
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View v = mInflater.inflate(R.layout.autodownload_dialog_layout,
 					null);
 			tv = (TextView) v.findViewById(R.id.dialog_text);
 			tv.setText("正在下载...");
 			progressDialog.setContentView(v);
 
 		}
 
 		if (progressDialog.isShowing()) {
 			return;
 		}
 		progressDialog.setCancelable(cancelable);
 		progressDialog.show();
 	}
 
 	public void cancelAuotdownloadDialog() {
 		if (progressDialog == null) {
 			return;
 		}
 		if (progressDialog.isShowing()) {
 			progressDialog.cancel();
 		}
 	}
 
 	/**
 	 * 组装handler信息
 	 * 
 	 * @param padding
 	 * @param toX
 	 */
 	@SuppressLint("NewApi")
 	public void sendHandlerMessage(int padding, int toX) {
 		detailContainer.setPadding(padding, 0, 0, 0);
 		Message m = new Message();
 		m.arg2 = toX;
 		m.arg1 = (int) detailContainer.getX();
 		handler.sendMessage(m);
 	}
 
 	public void refreshMainPage(String identifier, String type,
 			CubeModule module) {
 		String moduleMessage = new Gson().toJson(module);
 		parentFragment.sendJavascript("refreshMainPage('" + identifier + "','"
 				+ type + "','" + moduleMessage + "')");
 	}
 
 	// 只做一次业务
 	Boolean firstTime = true;
 	int touchX = 0;
 
 	/**
 	 * 
 	 */
 	@SuppressLint("NewApi")
 	public void init() {
 		application = Application.class.cast(FacadeActivity.this
 				.getApplication());
 		sendChatNotification = application.getShouldSendChatNotification();
 		sendMessageNotification = application
 				.getShouldSendMessageNotification();
 		sendNoticeNotification = application.getShouldSendNoticeNotification();
 		EventBus.getEventBus(TmpConstants.EVENTBUS_MESSAGE_COUNT)
 				.register(this);
 		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).register(this);
 		url = getIntent().getStringExtra("url");
 		isPad = getIntent().getBooleanExtra("isPad", false);
 		if (isPad) {
 			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		} else {
 			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		}
 		layout = this.getActivity().findViewById(R.id.content_window_id);
 		autoDownloadlayout = (RelativeLayout) findViewById(R.id.autodownloadlayout);
 		autoDownload = (ProgressBar) findViewById(R.id.progressBar1);
 		tv = (TextView) findViewById(R.id.dialog_text);
 
 		parentFragment = (ParentDroidFragment) this.getSupportFragmentManager()
 				.findFragmentById(R.id.parent_fragment);
 		parentFragment.addPageStartListener(new PageStartListener() {
 
 			@Override
 			public void onPageStarted(WebView view, String url, Bitmap favicon) {
 				if (url.contains(URL.PAD_LOGIN_URL)
 						|| url.contains(URL.PHONE_LOGIN_URL)) {
 					detailContainer.setVisibility(View.GONE);
 				} else {
 					detailContainer.setVisibility(View.GONE);
 					Pool.getPool().execute(new Runnable() {
 						@Override
 						public void run() {
 							// try {
 							// long noticeCount =
 							// StaticReference.userMf.queryBuilder(NoticeModuleMessage.class).where().eq("hasRead",
 							// '1').countOf();
 							// long messageCount =
 							// StaticReference.userMf.queryBuilder(NoticeModuleMessage.class).where().eq("hasRead",
 							// '1').countOf();
 							// long chatCount =
 							// StaticReference.userMf.queryBuilder(NoticeModuleMessage.class).where().eq("hasRead",
 							// '1').countOf();
 							// } catch (SQLException e) {
 							// e.printStackTrace();
 							// }
 						}
 					});
 				}
 
 			}
 		});
 
 		parentFragment.addPageFinishListener(new PageFinishListener() {
 
 			@Override
 			public void onPageFinished(WebView view, String url) {
 
 				finishOp(url, TmpConstants.VIEW_MESSAGE_COUNT_PRESENCE);
 				finishOp(url, TmpConstants.VIEW_ANNOUNCE_COUNT_PRESENCE);
 				finishOp(url, TmpConstants.VIEW_CHAT_COUNT_PRESENCE);
 
 				if (url.contains(URL.PAD_MAIN_URL)
 						|| url.contains(URL.PHONE_MAIN_URL)) {
 					application.setShouldSendChatNotification(false);
 					application.setShouldSendMessageNotification(false);
 					application.setShouldSendNoticeNotification(false);
 				} else {
 					application.setShouldSendChatNotification(true);
 					application.setShouldSendMessageNotification(true);
 					application.setShouldSendNoticeNotification(true);
 				}
 			}
 		});
 
 		// nativeViewFragment = new NativeViewFragment();
 
 		detailContainer = (FrameLayout) this.findViewById(R.id.child_frame);
 		if (isPad) {
 			makeContentHalf();
 		} else {
 			detailContainer.setVisibility(View.GONE);
 		}
 		// 动态拖拉窗口效果
 		detailContainer.setOnTouchListener(new View.OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				System.out.println("event  yy ==  " + event.getY());
 
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					// 向右弹
 					if (v.getX() < parentFragment.getWidth() / 4
 							&& v.getX() > 0) {
 						sendHandlerMessage(0, 0);//
 						// 向左弹
 					} else if (v.getX() > parentFragment.getWidth() / 4
 							&& v.getX() < parentFragment.getWidth() / 2) {
 						sendHandlerMessage(21,
 								parentFragment.getWidth() / 2 - 24);
 						//
 					} else if (v.getX() < 0) {
 						sendHandlerMessage(0, 0);
 
 					} else if (v.getX() > parentFragment.getWidth() / 2
 							&& v.getX() < (parentFragment.getWidth() / 4) * 3) {
 						sendHandlerMessage(21,
 								parentFragment.getWidth() / 2 - 24);
 					} else if (v.getX() > (parentFragment.getWidth() / 3)) {
 
 						sendHandlerMessage(0, parentFragment.getWidth());
 						popRight();
 
 					}
 					firstTime = true;
 				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 
 					if (firstTime) {// 记录第一次点下的坐标
 						touchX = (int) event.getRawX();
 						xMove = (int) v.getX();
 						firstTime = false;
 					} else {
 						int x = (int) event.getRawX();
 						temp = x - touchX;
 						v.setX(xMove + temp);// 通过设备view X坐标的值 达到移动空窗口的目的
 					}
 				}
 				return true;
 			}
 		});
 
 		// handler接收信息,主要为了窗口移动动画
 		handler = new Handler() {
 			@SuppressLint("NewApi")
 			public void handleMessage(Message msg) {
 				int toX = msg.arg2;
 				int distance = msg.arg1;
 				int animRate = 0;
 				// 判断是向左移 还是向右移
 				if (toX < detailContainer.getX()) {
 					animRate = -MOVEMENT_SPEED;
 					detailContainer.setX(distance);
 					// 判断是否移到指定位置
 					if (detailContainer.getX() > toX) {
 						Message m = new Message();
 						m.arg2 = toX;
 						m.arg1 = (int) detailContainer.getX() + animRate;// 循环累加
 						handler.sendMessageDelayed(m, 0);// 自己发信息 自己接收,0秒延时,实现循环
 					} else {
 						detailContainer.setX(toX);
 					}
 				} else if (toX > detailContainer.getX()) {
 					animRate = MOVEMENT_SPEED;
 					detailContainer.setX(distance);
 
 					if (detailContainer.getX() < toX) {
 						Message m = new Message();
 						m.arg2 = toX;
 						m.arg1 = (int) detailContainer.getX() + animRate;
 						handler.sendMessageDelayed(m, 0);
 					} else {
 						detailContainer.setX(toX);
 					}
 				}
 			}
 		};
 
 		ResizeLayout layout = (ResizeLayout) findViewById(R.id.layouts);
 		layout.setOnResizeListener(new ResizeLayout.OnResizeListener() {
 			public void OnResize(int w, int h, int oldw, int oldh) {
 				if (PadUtils.isPad(FacadeActivity.this)) {
 					int change = BIGGER;
 					if (h < oldh) {
 						change = SMALLER;
 					}
 					Message msg = new Message();
 					msg.what = 1;
 					msg.arg1 = change;
 					mHandler.sendMessage(msg);
 				}
 			}
 		});
 	}
 
 	public void finishOp(String url, String countView) {
 		if (url.contains(URL.PAD_MAIN_URL) || url.contains(URL.PHONE_MAIN_URL)) {
 			EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).post(
 					new PresenceEvent(countView, true));
 		} else {
 			EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).post(
 					new PresenceEvent(countView, false));
 		}
 	}
 
 	/**
 	 * 计算窗口大小 ，占其空间一半
 	 */
 	@SuppressLint("NewApi")
 	public void makeContentHalf() {
 		contentView = this.getActivity().findViewById(R.id.content_window_id);
 		contentView.getViewTreeObserver().addOnPreDrawListener(
 				new ViewTreeObserver.OnPreDrawListener() {
 					public boolean onPreDraw() {
 						int contentViewWidth = contentView.getWidth();
 						parentFragment.setWidth(contentViewWidth);
 						RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
 								contentViewWidth / 2 + 24,
 								LayoutParams.MATCH_PARENT);
 						layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
 								RelativeLayout.TRUE);
 						detailContainer.setLayoutParams(layoutParams);
 						return true;
 					}
 				});
 		detailContainer.setVisibility(View.GONE);
 
 	}
 
 	/**
 	 * 
 	 * [加载底层页面]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param parentUrl
 	 *            2013-8-15 上午10:02:07
 	 */
 	public void loadUrl(String parentUrl) {
 
 		if (parentUrl == null) {
 			if (isPad) {
 				parentUrl = URL.PAD_LOGIN_URL;
 			} else {
 				parentUrl = URL.PHONE_LOGIN_URL;
 			}
 		}
 
 		parentFragment.setIsVisible(View.GONE);
 		parentFragment.loadUrl(parentUrl);
 	}
 
 	/**
 	 * 从webViewUrl加载详细到右侧
 	 */
 	@SuppressLint("NewApi")
 	public void setByContentUrl(final String url) {
 		if (!stack.isEmpty()) {
 			Fragment topFragment = stack.peek();
 			if (topFragment != null
 					&& DroidGapFragment.class.isAssignableFrom(topFragment
 							.getClass())) {
 				DroidGapFragment fragment = DroidGapFragment.class
 						.cast(topFragment);
 				if (url.equals(fragment.getNatvieUrl())) {
 					return;
 				}
 			}
 		}
 		final DroidGapFragment fragment = new DroidGapFragment() {
 
 			@Override
 			public FragmentActivity getActivity() {
 				return FacadeActivity.this;
 			}
 		};
 		fragment.setViewCreateCallBack(new ViewCreateCallBack() {
 
 			@Override
 			public void viewCreated(CordovaWebView cordovaWebView) {
 				fragment.setIsVisible(View.VISIBLE);
 				fragment.setOpenLayer(true);
 				fragment.loadUrl(url);
 			}
 		});
 		setByFragment(fragment);
 	}
 
 	public void makeNativehalf() {
 		final View contentView = this.getActivity().findViewById(
 				R.id.content_window_id);
 		contentView.getViewTreeObserver().addOnPreDrawListener(
 				new ViewTreeObserver.OnPreDrawListener() {
 					public boolean onPreDraw() {
 						int contentViewWidth = contentView.getWidth();
 						RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
 								contentViewWidth / 2 + 24,
 								LayoutParams.MATCH_PARENT);
 						layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
 								RelativeLayout.TRUE);
 						detailContainer.setLayoutParams(layoutParams);
 						return true;
 					}
 				});
 	}
 
 	/**
 	 * 换肤
 	 * 
 	 * @param name
 	 */
 	public void changeSkin(String name) {
 		parentFragment.sendJavascript("changeTheme('" + name + "')");
 	}
 
 	public void sentKeyboardShow(Boolean b) {
 		parentFragment.sendJavascript("isKeyboardShow('" + b + "')");
 	}
 
 	/**
 	 * 加载本地组件
 	 */
 	public void setByNativeView(View contentView) {
 		detailContainer.setVisibility(View.VISIBLE);
 		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
 				.beginTransaction();
 		// nativeViewFragment.init(contentView);
 		fragmentTransaction.replace(R.id.child_frame, nativeViewFragment);
 		fragmentTransaction.commitAllowingStateLoss();
 	}
 
 	@SuppressLint("NewApi")
 	public void recompute() {
 		if (detailContainer.getX() == 0 && !detailContainer.isShown()) {
 			detailContainer.setX(0);
 		} else {
 			detailContainer.setPadding(21, 0, 0, 0);
 			detailContainer.setBackgroundResource(R.drawable.shadow);
 			detailContainer.setX(parentFragment.getWidth() / 2 - 24);
 		}
 		if (detailContainer.getX() == 0 && !detailContainer.isShown()) {
 			detailContainer.setX(0);
 			detailContainer.setBackgroundColor(0x000000);
 		} else {
 			detailContainer.setPadding(21, 0, 0, 0);
 			detailContainer.setBackgroundResource(R.drawable.shadow);
 			detailContainer.setX(parentFragment.getWidth() / 2 - 24);
 		}
 
 		if (!stack.isEmpty() && stack.peek() instanceof GroupFragment) {
 			detailContainer.setVisibility(View.VISIBLE);
 		}
 		//
 	}
 
 	/**
 	 * 加载本地Fragment
 	 */
 	@SuppressLint("NewApi")
 	public void setByFragment(Fragment fragment) {
 		makeNativehalf();
 		if (!stack.isEmpty()) {
 			if (stack.contains(fragment)) {
 				return;
 			}
 
 			Fragment topFragment = stack.peek();
 			if (topFragment != null
 					&& topFragment.toString().equals(fragment.toString())) {
 				recompute();
 				return;
 			}
 		}
 		if ((!(fragment instanceof ChatRoomFragment)
 				&& !(fragment instanceof MucAddFirendFragment) && !(fragment instanceof MucManagerFragment))) {
 			popRight();
 		} else {
 
 			pending = true;
 		}
 
 		stack.push(fragment);
 		if (detailContainer.getX() == 0 && !detailContainer.isShown()) {
 			detailContainer.setX(0);
 		} else {
 			detailContainer.setPadding(21, 0, 0, 0);
 			detailContainer.setBackgroundResource(R.drawable.shadow);
 			detailContainer.setX(parentFragment.getWidth() / 2 - 24);
 		}
 		detailContainer.setVisibility(View.VISIBLE);
 		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
 				.beginTransaction();
 		fragmentTransaction.add(R.id.child_frame, fragment);
 		// fragmentTransaction.setCustomAnimations(android.R.anim.slide_out_right,
 		// android.R.anim.slide_in_left);
 		fragmentTransaction.addToBackStack(null);
 		// fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
 		fragmentTransaction.commit();
 	}
 
 	public void setParentFragment(Fragment fragment) {
 		detailContainer.setVisibility(View.VISIBLE);
 		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
 				.beginTransaction();
 		fragmentTransaction.add(R.id.parent_fragment, fragment);
 		fragmentTransaction.addToBackStack(null);
 		fragmentTransaction.commit();
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		boolean isPad = intent.getBooleanExtra("isPad", true);
 		if (isPad) {
 			doPadNewIntent(intent);
 		} else {
 			doPhoneNewIntent(intent);
 		}
 
 	}
 
 	public void doPadNewIntent(Intent intent) {
 		// popRight();
 		int direction = intent.getIntExtra("direction", 1);
 		String type = intent.getStringExtra("type");
 		String value = intent.getStringExtra("value");
 		if (value.endsWith("login.html") && !stack.isEmpty()) {
 			stack.pop();
 		}
 		setIntent(intent);
 		if ("web".equals(type)) {
 			String url = value;
 			intent.putExtra("url", url);
 			// 1.表示父层,2表示子层
 			if (1 == direction) {
 				this.url = url;
 				parentFragment.setWidth(contentView.getWidth());
 				parentFragment.loadUrl(url);
 			} else if (2 == direction) {
 				setByContentUrl(url);
 			}
 		} else if ("fragment".equals(type)) {
 			String fragmentClassName = value;
 			if (!stack.isEmpty()) {
 				Fragment top = stack.peek();
 				if (ModuleDetailFragment.class.isAssignableFrom(top.getClass())) {
 					String newIdentifier = intent.getStringExtra("identifier");
 					String version = intent.getStringExtra("version");
 					int bulid = intent.getIntExtra("build", 1);
 					String newIden = newIdentifier + "_" + version + "_"
 							+ bulid;
 					String iden = ModuleDetailFragment.class.cast(top)
 							.toString();
 					if (iden.equals(newIden)) {
 						return;
 					}
 				}
 			}
 			Fragment fragment = Fragment.instantiate(this, fragmentClassName);
 
 			if (1 == direction) {
 				setParentFragment(fragment);
 			} else if (2 == direction) {
 				setByFragment(fragment);
 			}
 		}
 	}
 
 	public void doPhoneNewIntent(Intent intent) {
 		setIntent(intent);
 		String url = intent.getStringExtra("value");
 		this.url = url;
 		loadUrl(url);
 	}
 
 	/*****************************************************************************
 	 * 		
 	 * 					
 	 * 
 	 *****************************************************************************/
 
 	class DetailModel {
 		private String name;
 
 		public String getName() {
 			return name;
 		}
 
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		public Object getOtherData() {
 			return otherData;
 		}
 
 		public void setOtherData(Object otherData) {
 			this.otherData = otherData;
 		}
 
 		private Object otherData;
 	}
 
 	/*****************************************************************************
 	 * 
 	 * 
 	 *****************************************************************************/
 	@Override
 	public ExecutorService getThreadPool() {
 		return pool;
 	}
 
 	// Plugin to call when activity result is received
 	protected CordovaPlugin activityResultCallback = null;
 
 	@Override
 	public void setActivityResultCallback(CordovaPlugin plugin) {
 		this.activityResultCallback = plugin;
 	}
 
 	/**
 	 * Launch an activity for which you would like a result when it finished.
 	 * When this activity exits, your onActivityResult() method will be called.
 	 * 
 	 * @param command
 	 *            The command object
 	 * @param intent
 	 *            The intent to start
 	 * @param requestCode
 	 *            The request code that is passed to callback to identify the
 	 *            activity
 	 */
 	public void startActivityForResult(CordovaPlugin command, Intent intent,
 			int requestCode) {
 		this.activityResultCallback = command;
 		super.startActivityForResult(intent, requestCode);
 	}
 
 	public void removeSplashScreen() {
 		if ((this.splashDialog != null) && (this.splashDialog.isShowing())) {
 			this.splashDialog.dismiss();
 			this.splashDialog = null;
 		}
 	}
 
 	protected void showSplashScreen(int time) {
 
 		Runnable runnable = new Runnable() {
 			public void run() {
 				Display display = getActivity().getWindowManager()
 						.getDefaultDisplay();
 
 				LinearLayout root = new LinearLayout(getActivity());
 				root.setMinimumHeight(display.getHeight());
 				root.setMinimumWidth(display.getWidth());
 				root.setOrientation(1);
 				// root.setBackgroundColor(getActivity().
 				// .getIntegerProperty("backgroundColor", -16777216));
 				root.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 0.0F));
 				ImageView splashscreenView = new ImageView(getActivity());
 				// splashscreenView.setImageResource(R.drawable.bg_main);
 				splashscreenView.setScaleType(ImageView.ScaleType.CENTER_CROP);
 				splashscreenView.setLayoutParams(new LinearLayout.LayoutParams(
 						-1, -1));
 				root.addView(splashscreenView);
 
 				splashDialog = new Dialog(getActivity(), 16973840);
 
 				if ((getActivity().getWindow().getAttributes().flags & 0x400) == 1024) {
 					splashDialog.getWindow().setFlags(1024, 1024);
 				}
 
 				splashDialog.setContentView(root);
 				splashDialog.setCancelable(false);
 				splashDialog.show();
 
 				Handler handler = new Handler();
 				handler.postDelayed(new Runnable() {
 					public void run() {
 						removeSplashScreen();
 					}
 
 				}, splashscreenTime);
 			}
 
 		};
 		getActivity().runOnUiThread(runnable);
 	}
 
 	public int getIntegerProperty(String name, int defaultValue) {
 		Bundle bundle = getActivity().getIntent().getExtras();
 		if (bundle == null)
 			return defaultValue;
 		Integer p;
 		try {
 			p = (Integer) bundle.get(name);
 		} catch (ClassCastException e) {
 			p = Integer.valueOf(Integer.parseInt(bundle.get(name).toString()));
 		}
 		if (p == null) {
 			return defaultValue;
 		}
 		return p.intValue();
 	}
 
 	@Override
 	public Object onMessage(String arg0, Object arg1) {
 		return null;
 	}
 
 	@Override
 	public Activity getActivity() {
 		return this;
 	}
 
 	public void receiveMessage(final String identifier, final int count) {
 		Log.i("AAA", "进入receiveMessage");
 		receiveMessage(identifier, count, true);
 	}
 
 	public void receiveMessage(final String identifier, final int count,
 			final boolean display) {
 		Log.i("AAA", "进入receiveMessage");
 		runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				parentFragment.sendJavascript("receiveMessage('" + identifier
 						+ "'," + count + "," + display + ")");
 				Log.d(FacadeActivity.class.getName(), "receiveMessage('"
 						+ identifier + "'," + count + "," + display + ")");
 			}
 		});
 	}
 
 	public void refreshModule(String identifier, String type, CubeModule module) {
 		String moduleMessage = new Gson().toJson(module);
 		parentFragment.sendJavascript("refreshModule('" + identifier + "','"
 				+ type + "','" + moduleMessage + "')");
 	}
 
 	public void sendProcessToJs(String identifier, int progress) {
 		Log.v("CordovaWebView", "CordovaWebView send Progress" + progress);
 		parentFragment.sendJavascript("updateProgress('" + identifier + "','"
 				+ progress + "')");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
 	 */
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
 	 */
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
 	 */
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent event) {
 		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && url != null
 		/* && !url.contains("login") */) {
 			if (event.getAction() == KeyEvent.ACTION_UP) {
 				Dialog dialog = new AlertDialog.Builder(this)
 						.setTitle("提示")
 						.setMessage("确定退出 ？")
 						.setNegativeButton("取消",
 								new DialogInterface.OnClickListener() {
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										dialog.dismiss();
 									}
 								})
 						.setPositiveButton("确定",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										dialog.dismiss();
 										Application.class
 												.cast(FacadeActivity.this
 														.getApplicationContext())
 												.exitApp();
 										FacadeActivity.this.finish();
 									}
 								}).create();
 
 				if (!PadUtils.isPad(this)) {
 					if (this.url.endsWith("phone/index.html")) {
 						dialog.show();
 						return true;
 					} else if (this.url.endsWith("#manager")) {
 						parentFragment.sendJavascript("backToMain()");
 						int index = url.indexOf("#manager");
 						url = url.substring(0, index);
 						return true;
 					} else if (this.url.endsWith("phone/login.html")) {
 						dialog.show();
 						return true;
 					}
 				}
 
 				else {
 					dialog.show();
 					return true;
 				}
 
 			}
 		}
 
 		return super.dispatchKeyEvent(event);
 	}
 
 	@SuppressLint("NewApi")
 	public void popRight() {
 		getSupportFragmentManager().popBackStack();
 		if (!stack.isEmpty()) {
 			stack.pop();
 			if (stack.isEmpty()) {
 				detailContainer.setVisibility(View.GONE);
 				detailContainer.setX(parentFragment.getWidth() / 2);
 				makeContentHalf();
 				recompute();
 			}
 
 		}
 
 	}
 
 	public void popRightAll() {
 		// getSupportFragmentManager().p
 		stack.empty();
 	}
 
 	@Subscribe
 	public void onMessageCountChnage(
 			MessageCountChangeEvent messageCountChangeEvent) {
 		receiveMessage(messageCountChangeEvent.getIdentifier(),
 				messageCountChangeEvent.getCount(),
 				messageCountChangeEvent.isDisplayBadge());
 	}
 
 	@Subscribe
 	public void onMuliSystem(HashMap<String, Object> b) {
 		SystemInfoModel model = (SystemInfoModel) b.get("systemmodel");
 		model.setCurr(true);
 		String userName = (String) b.get("username");
 		String passWord = (String) b.get("password");
 		boolean isremember = (Boolean) b.get("isremember");
 		boolean isoutline = (Boolean) b.get("isoutline");
 		// 保存当前的系统ID
 		Preferences.saveSytemId(model.getSysId(), Application.sharePref);
 		if (activityResultCallback instanceof CubeLoginPlugin) {
 			CubeLoginPlugin plugin = (CubeLoginPlugin) activityResultCallback;
 			plugin.processLogined(isremember, userName, passWord,isoutline,
 					plugin.getCallback());
 		} else if (activityResultCallback instanceof ExtroSystem){
 			MultiUserInfoModel multiUserInfoModel = new MultiUserInfoModel();
 			multiUserInfoModel.setUserName(userName);
 			multiUserInfoModel.setSystemId(model.getSysId());
 			List<MultiUserInfoModel> list = StaticReference.userMf
 					.queryForMatching(multiUserInfoModel);
 			if (list.size() > 0){
 				MultiUserInfoModel multiModel = list.get(0);
 				passWord = multiModel.getPassWord();
 			}
 			ExtroSystem plugin = (ExtroSystem) activityResultCallback;
 			plugin.processLogined(userName, passWord , model.getSysId() , model ,
 					plugin.getCallback());
 		}
 
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(broadcastReceiver);
 		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).unregister(this);
 		for (SoftReference<Bitmap> softReference : bitmapRefList) {
 			if (softReference.get() != null && softReference.get() != null
 					&& !softReference.get().isRecycled()) {
 				softReference.get().recycle();
 
 			}
 		}
 
 	}
 
 	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
 
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			Log.i("AAA", "按下back");
 			String extraMessage = "";
 			Set<CubeModule> allSet = CubeModuleManager.getInstance()
 					.getAllSet();
 			StringBuilder builder = new StringBuilder();
 			for (CubeModule cubeModule : allSet) {
 				switch (cubeModule.getModuleType()) {
 				case CubeModule.DELETING: {
 					builder.append(" 删除");
 					break;
 				}
 				case CubeModule.INSTALLING: {
 					builder.append(" 安装");
 					break;
 				}
 				case CubeModule.UPGRADING: {
 					builder.append(" 升级");
 					break;
 				}
 				}
 			}
 			if (builder.length() > 1) {
 				// builder.append("正在进行中，退出可能导致操作失败，");
 				// extraMessage = builder.toString();
 				extraMessage = "有模块正在下载或更新中，退出可能导致操作失败";
 			}
 			// Dialog dialog = new AlertDialog.Builder(this)
 			// .setTitle("提示")
 			// .setMessage(extraMessage + " 确定退出?")
 			// .setNegativeButton("取消",
 			// new DialogInterface.OnClickListener() {
 			// @Override
 			// public void onClick(DialogInterface dialog,
 			// int which) {
 			// dialog.dismiss();
 			// }
 			// })
 			// .setPositiveButton("确定",
 			// new DialogInterface.OnClickListener() {
 			//
 			// @Override
 			// public void onClick(DialogInterface dialog,
 			// int which) {
 			// try {
 			// dialog.dismiss();
 			// finish();
 			// Application.class.cast(FacadeActivity.this).exitApp();
 			// } catch (Exception e) {
 			// e.printStackTrace();
 			// Log.e("exit", "退出应用异常");
 			// }
 			// }
 			// }).create();
 			// dialog.show();
 			// return true;
 		}
 		return false;
 	};
 
 	/**
 	 * [一句话功能简述]<BR>
 	 * [功能详细描述] 2013-9-2 上午11:53:29
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (sendChatNotification != null) {
 			application.setShouldSendChatNotification(sendChatNotification);
 		}
 		if (sendMessageNotification != null) {
 			application
 					.setShouldSendMessageNotification(sendMessageNotification);
 		}
 		if (sendNoticeNotification != null) {
 			application.setShouldSendNoticeNotification(sendNoticeNotification);
 		}
 
 	}
 
 	/**
 	 * [一句话功能简述]<BR>
 	 * [功能详细描述] 2013-9-3 上午11:26:48
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		sendChatNotification = application.shouldSendChatNotification();
 		sendMessageNotification = application.shouldSendMessageNotification();
 		sendNoticeNotification = application.shouldSendNoticeNotification();
 		application.setShouldSendChatNotification(true);
 		application.setShouldSendMessageNotification(true);
 		application.setShouldSendNoticeNotification(true);
 	}
 
 	/**
 	 * [一句话功能简述]<BR>
 	 * [功能详细描述] 2013-9-2 上午11:53:38
 	 */
 
 	private Boolean sendChatNotification = null;
 	private Boolean sendMessageNotification = null;
 	private Boolean sendNoticeNotification = null;
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		popRight = false;
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		// TODO Auto-generated method stub
 		super.onConfigurationChanged(newConfig);
 		Log.i("test", "test");
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode != android.app.Activity.RESULT_OK) {
 			return;
 		}
 	}
 }
