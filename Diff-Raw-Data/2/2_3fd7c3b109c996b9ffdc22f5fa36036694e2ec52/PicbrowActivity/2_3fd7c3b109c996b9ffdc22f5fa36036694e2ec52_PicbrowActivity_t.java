 package com.zrd.zr.letuwb;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URLConnection;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import weibo4android.User;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.AlphaAnimation;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageSwitcher;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewSwitcher.ViewFactory;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 import com.mobclick.android.MobclickAgent;
 import com.sonyericsson.zoom.DynamicZoomControl;
 import com.sonyericsson.zoom.ImageZoomView;
 import com.sonyericsson.zoom.LongPressZoomListener;
 import com.zrd.zr.letuwb.R;
 import com.zrd.zr.weiboes.Sina;
 import com.zrd.zr.weiboes.ThreadSinaDealer;
 
 public class PicbrowActivity extends Activity implements ViewFactory, OnTouchListener {
 
 	private FrameLayout mFrameBackground;
 	private RelativeLayout rlCtrl;
 	private LinearLayout llVoteInfo;
 	private LinearLayout mLayoutTop;
 	private TextView tvNums;
 	private TextView mTextScreenName;
 	private TextView mTextUpup;
 	private TextView mTextDwdw;
 	private TextView mTextVoteRating;
 	private ProgressBar mProgressVote;
 	private ImageZoomView mBrow;
 	private Button btnSave;
 	private Button btnUpload;
 	private Button btnPlay;
 	private Button btnPause;
 	private ImageButton btnUpup;
 	private ImageButton btnDwdw;
 	private ImageButton btnZoomIn;
 	private ImageButton btnZoomOut;
 	private ImageButton mBtnExchange;
 	private Button mBtnShare;
 	private Button mBtnWeiboShow;
 	private Button mBtnWeiboFriend;
 	private static Boolean mIsLoading = false;
 	private Boolean mWasPlaying = false;
 	public Boolean mIsDooming = false;
 	Long mId = (long)0;
 	ArrayList<WeibouserInfo> mUsrs = null;
 	Bitmap bdPicFailed;
 	private GestureDetector mGestureDetector = null;
 	//private Vibrator mVibrator;
 	AlphaAnimation fadeinAnim = new AlphaAnimation(0.1f, 1.0f);
 	AlphaAnimation fadeoutAnim = new AlphaAnimation(1.0f, 0.1f);
 	
 	NotificationManager mNotificationManager;
 	Timer mTimer = null;
 	Handler mHandler = null;
 	
     /** Zoom control */
     private DynamicZoomControl mZoomControl;
     /** On touch listener for zoom view */
     private LongPressZoomListener mZoomListener;
     
 	private File mSaveFile = null;
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		setContentView(R.layout.brow);
 		
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.exchangelist_title);
 		RegLoginActivity.addContext(PicbrowActivity.this);
 		mBtnExchange = (ImageButton) findViewById(R.id.btnExchange);
 		mFrameBackground = (FrameLayout) findViewById(R.id.flBackground);
 		rlCtrl = (RelativeLayout) findViewById(R.id.rlControl);
 		llVoteInfo = (LinearLayout) findViewById(R.id.llVoteInfo);
 		mLayoutTop = (LinearLayout) findViewById(R.id.llBrowTop);
 		tvNums = (TextView) findViewById(R.id.textViewNums);
 		mTextScreenName = (TextView) findViewById(R.id.tvScreenNameAbovePic);
 		mTextUpup = (TextView) findViewById(R.id.tvUpup);
 		mTextDwdw = (TextView) findViewById(R.id.tvDwdw);
 		mTextVoteRating = (TextView) findViewById(R.id.tvVoteRating);
 		mProgressVote = (ProgressBar) findViewById(R.id.pbVote);
 		mBrow = (ImageZoomView) findViewById(R.id.imageSwitcher);
 		btnSave = (Button) findViewById(R.id.btnSave);
 		btnPlay = (Button) findViewById(R.id.btnPlay);
 		btnPause = (Button) findViewById(R.id.btnPause);
 		btnUpload = (Button) findViewById(R.id.btnUpload);
 		btnUpup = (ImageButton) findViewById(R.id.imageButton4);
 		btnDwdw = (ImageButton) findViewById(R.id.imageButton3);
 		btnZoomIn = (ImageButton) findViewById(R.id.btnZoomin);
 		btnZoomOut = (ImageButton) findViewById(R.id.btnZoomout);
 		mBtnShare = (Button) findViewById(R.id.btnShare);
 		mBtnWeiboShow = (Button) findViewById(R.id.btnWeiboShow);
 		mBtnWeiboFriend = (Button) findViewById(R.id.btnMakeFriendsFromBrow);
 		bdPicFailed = BitmapFactory.decodeResource(this.getResources(), R.drawable.broken);
 		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		mGestureDetector = new GestureDetector(this, new PicbrowGestureListener());
 		//mVibrator = ( Vibrator )getApplication().getSystemService(Service.VIBRATOR_SERVICE);
 		mHandler = new Handler() {
 
 			public void handleMessage(Message msg) {
 				switch (msg.what) {
 					case 1:
 						if (mUsrs.size() == 0) {
 							Toast.makeText(PicbrowActivity.this, getString(R.string.tips_nopictures), Toast.LENGTH_SHORT).show();
 						} else if (btnPause.getVisibility() == ImageButton.VISIBLE && !PicbrowActivity.mIsLoading) {
 	                		Toast.makeText(PicbrowActivity.this, getString(R.string.tips_playing), Toast.LENGTH_SHORT).show();
 	                		if (EntranceActivity.getUsrIndexFromId(mId, mUsrs) < mUsrs.size() - 1) {
 	                			zrAsyncShowPic(mId, 2);
 	                		} else {
 	                			zrAsyncShowPic(mUsrs.get(0).id, 0);
 	                		}
 	                	}
 						break;
 					case ThreadSinaDealer.CREATE_FRIENDSHIP:
 						User user = (User)msg.getData().getSerializable(ThreadSinaDealer.KEY_DATA);
 						if (user != null) {
 							if (!user.equals(WeiboShowActivity.getSina().getLoggedInUser())) {
 								Toast.makeText(
 									PicbrowActivity.this,
 									"Friends made.",
 									Toast.LENGTH_LONG
 								).show();
 							} else {
 								Toast.makeText(
 									PicbrowActivity.this,
 									"Friends already.",
 									Toast.LENGTH_LONG
 								).show();
 							}
 						} else {
 							//deal with failing to make friends
 						}
 						break;
 				}    
 				super.handleMessage(msg);
 			}
 			
 		};
 		
 		rlCtrl.setOnTouchListener(this);
 		mZoomControl = new DynamicZoomControl();
 		//mZoomListener = new LongPressZoomListener(getApplicationContext());
 		mZoomListener = new LongPressZoomListener(this);
         mZoomListener.setZoomControl(mZoomControl);
         mBrow.setZoomState(mZoomControl.getZoomState());
         mZoomControl.setAspectQuotient(mBrow.getAspectQuotient());
 		mBrow.setOnTouchListener(this);
         //mBrow.setOnTouchListener(mZoomListener);
 		
 		mLayoutTop.setVisibility(LinearLayout.INVISIBLE);
 		llVoteInfo.setVisibility(LinearLayout.INVISIBLE);
 		mTextScreenName.setVisibility(TextView.GONE);
 		
 		// Look up the AdView as a resource and load a request.
         AdView adView = (AdView)this.findViewById(R.id.adsBrow);
         adView.loadAd(new AdRequest());
 		
 		//mBrow.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
 		//mBrow.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
 		Intent intent = getIntent();
 		mUsrs = EntranceActivity.getmUsrs();
 		mId = intent.getLongExtra("id", 0);
 		zrAsyncShowPic(mId, 0);
 		
 		tvNums.setText("0/" + mUsrs.size());
 		
 		/*
 		 * initialize the title bar
 		 */
 		Sina sina = WeiboShowActivity.getSina();
 		if (sina != null && sina.isLoggedIn()) {
 			RegLoginActivity.updateTitle(
 				R.id.ivTitleIcon, R.id.tvTitleName,
 				sina.getLoggedInUser()
 			);
 		}
 		
 		mBtnShare.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                     // TODO Auto-generated method stub
             	WeibouserInfo wi = EntranceActivity.getPicFromId(mId, mUsrs);
             	if (wi != null) {
             		String sCacheFile = 
             			AsyncSaver.getSdcardDir() + EntranceActivity.PATH_CACHE 
             			+ wi.uid + ".jg";
             		String sFile =
             			AsyncSaver.getSdcardDir() + EntranceActivity.PATH_CACHE 
             			+ wi.uid + ".jpg";
             		File file = new File(sCacheFile);
             		file.renameTo(new File(sFile));
             		Uri uri = Uri.parse("file:///" + sFile);
             		Intent intent = new Intent(Intent.ACTION_SEND);
                 	intent.setType("image/*");
                     intent.putExtra(Intent.EXTRA_STREAM, uri);
                     
                     intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.sharing_title));
                     intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sharing_content));
                     
                     startActivity(Intent.createChooser(intent, getTitle()));
             	}
             }
         });
 		
 		mBtnExchange.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
             	Intent intent = new Intent();
                 intent.setClass(PicbrowActivity.this, ExchangeListActivity.class);
                 startActivity(intent);
             }
         });
 		
 		mBtnWeiboShow.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				//get current weibo user's information
             	WeibouserInfo wi = EntranceActivity.getPicFromId(mId, mUsrs);
             	
                 Intent intent = new Intent();
                 
                intent.putExtra("uid", wi.uid);
 				
 				intent.setClass(PicbrowActivity.this, WeiboShowActivity.class);
 				startActivity(intent);
 			}
 			
 		});
 		
 		mBtnWeiboFriend.setOnClickListener(new OnClickListener () {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Sina sina = WeiboShowActivity.getSina();
 				if (sina != null && sina.isLoggedIn()) {
 					new Thread(
 						new ThreadSinaDealer(
 							sina,
 							ThreadSinaDealer.CREATE_FRIENDSHIP,
 							new String[] {"" + EntranceActivity.getPicFromId(mId, mUsrs).uid},
 							mHandler
 						)
 					).start();
 				} else {
 					RegLoginActivity.shallWeLogin(R.string.title_loginfirst, PicbrowActivity.this);
 				}
 			}
 			
 		});
 		
 		btnZoomOut.setOnClickListener(new OnClickListener () {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (!mIsDooming) {
 					Toast.makeText(
 						PicbrowActivity.this,
 						R.string.tips_howtobacktobrowse,
 						Toast.LENGTH_SHORT
 					).show();
 					mIsDooming = true;
 				}
 				//ZoomState state = mZoomControl.getZoomState();
 				//state.setZoom(state.getZoom() * (float)Math.pow(20, -0.1));
 				//state.notifyObservers();
 				mZoomControl.zoom((float)Math.pow(20, -0.1), 0, 0);
 				mBrow.setOnTouchListener(mZoomListener);
 				rlCtrl.setOnTouchListener(mZoomListener);
 			}
 			
 		});
 		
 		btnZoomIn.setOnClickListener(new OnClickListener () {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (!mIsDooming) {
 					Toast.makeText(
 						PicbrowActivity.this,
 						R.string.tips_howtobacktobrowse,
 						Toast.LENGTH_SHORT
 					).show();
 					mIsDooming = true;
 				}
 				//ZoomState state = mZoomControl.getZoomState();
 				//state.setZoom(state.getZoom() * (float)Math.pow(20, 0.1));
 				//state.notifyObservers();
 				mZoomControl.zoom((float)Math.pow(20, 0.1), 0, 0);
 				mBrow.setOnTouchListener(mZoomListener);
 				rlCtrl.setOnTouchListener(mZoomListener);
 			}
 			
 		});
 				
 		btnUpload.setOnClickListener(new OnClickListener () {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				AlertDialog dlg = new AlertDialog.Builder(PicbrowActivity.this)
 					.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface arg0, int arg1) {
 							// TODO Auto-generated method stub
 							AsyncUploader.upload(EntranceActivity.getPrivilege(), PicbrowActivity.this);
 						}
 						
 					})
 					.setNegativeButton(R.string.label_cancel, null)
 					.setTitle(R.string.tips_noadultstuff)
 					.create();
 				dlg.show();
 			}
 			
 		});
 		
 		btnUpup.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (EntranceActivity.getClientKey().equals("")) {
 					Toast.makeText(
 						PicbrowActivity.this,
 						R.string.tips_notgetserialyet,
 						Toast.LENGTH_SHORT
 					).show();
 				} else {
 					WeibouserInfo wi = EntranceActivity.getPicFromId(mId, mUsrs);
 					Calendar now = Calendar.getInstance();
 					now.setTimeZone(TimeZone.getTimeZone(EntranceActivity.TIMEZONE_SERVER));
 					
 					if ((wi.mLastVoteTime != null
 						&& now.getTime().getTime() - wi.mLastVoteTime.getTime() > EntranceActivity.PERIOD_VOTEAGAIN * 3600000)
 						|| wi.mLastVoteTime == null) {
 						/**
 						 * we let the voters think they'll see the result immediately,
 						 * and we actually do the voting at background and it'll refresh
 						 * the real result lately.
 						 */
 						//mVibrator.vibrate( new long[]{50, 400, 30, 800},-1);
 						wi.likes++;
 						wi.mLastVote = 1;
 						zrRenewCurFileInfo();
 						AsyncVoter asyncVoter = new AsyncVoter();
 						asyncVoter.execute("weibouserid", mId.toString(), "clientkey", EntranceActivity.getClientKey(), "vote", "1");
 					} else {
 						Toast.makeText(
 							PicbrowActivity.this, 
 							String.format(getString(R.string.err_voted), wi.mLastVote == 1 ? getString(R.string.label_upup) : getString(R.string.label_dwdw), EntranceActivity.PERIOD_VOTEAGAIN),
 							Toast.LENGTH_SHORT
 						).show();
 					}
 				}
 			}
 			
 		});
 		
 		btnDwdw.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (EntranceActivity.getClientKey().equals("")) {
 					Toast.makeText(
 						PicbrowActivity.this,
 						R.string.tips_notgetserialyet,
 						Toast.LENGTH_SHORT
 					).show();
 				} else {
 					WeibouserInfo wi = EntranceActivity.getPicFromId(mId, mUsrs);
 					Date now = Calendar.getInstance(TimeZone.getTimeZone(EntranceActivity.TIMEZONE_SERVER)).getTime();
 					
 					if ((wi.mLastVoteTime != null
 						&& now.getTime() - wi.mLastVoteTime.getTime() > EntranceActivity.PERIOD_VOTEAGAIN * 3600000)
 						|| wi.mLastVoteTime == null) {
 						/**
 						 * we let the voters think they'll see the result immediately,
 						 * and we actually do the voting at background and it'll refresh
 						 * the real result lately.
 						 */
 						//mVibrator.vibrate( new long[]{100,10,100,10},-1);
 						wi.dislikes++;
 						wi.mLastVote = -1;
 						zrRenewCurFileInfo();
 						AsyncVoter asyncVoter = new AsyncVoter();
 						asyncVoter.execute("weibouserid", mId.toString(), "clientkey", EntranceActivity.getClientKey(), "vote", "-1");
 					} else {
 						Toast.makeText(
 							PicbrowActivity.this, 
 							String.format(getString(R.string.err_voted), wi.mLastVote == 1 ? getString(R.string.label_upup) : getString(R.string.label_dwdw), EntranceActivity.PERIOD_VOTEAGAIN),
 							Toast.LENGTH_SHORT
 						).show();
 					}
 				}
 			}
 			
 		});
 		
 		btnSave.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Context context = getApplicationContext();
 				// TODO Auto-generated method stub
 				if(!AsyncSaver.getSdcardDir().equals("")) {
 					String path = AsyncSaver.getSdcardDir() + EntranceActivity.PATH_COLLECTION;
 					File file = new File(path);
 					Boolean couldSave = false;
 					if (!file.exists()) {
 						if (file.mkdirs()) {
 							couldSave = true;
 						} else {
 							Toast.makeText(context,
 								String.format(getString(R.string.err_nopath), path),
 								Toast.LENGTH_LONG
 							).show();
 							return;
 						}
 					} else couldSave = true;
 					if (couldSave) {
 						//OK, now we could actually save the file, finally.
 						String fn = getSaveFileName(EntranceActivity.getPicFromId(mId, mUsrs).uid + ".xxx");
 						mSaveFile = new File(file, fn);
 						if (mSaveFile.exists()) {
 							//if there is already a file exists with same file name
 							AlertDialog alertDlg = new AlertDialog.Builder(PicbrowActivity.this).create();
 							alertDlg.setTitle(R.string.title_warning);
 							alertDlg.setMessage(String.format(getString(R.string.err_filealreadyexists), fn));
 							alertDlg.setIcon(android.R.drawable.ic_dialog_alert);
 							alertDlg.setButton(
 								DialogInterface.BUTTON_POSITIVE,
 								getString(R.string.label_ok),
 								new DialogInterface.OnClickListener () {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										// TODO Auto-generated method stub
 										Toast.makeText(
 											PicbrowActivity.this,
 											R.string.tips_saving,
 											Toast.LENGTH_SHORT
 										).show();
 										AsyncSaver asyncSaver = new AsyncSaver(PicbrowActivity.this, mBrow.getImage());
 										asyncSaver.execute(mSaveFile);
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
 							Toast.makeText(
 								PicbrowActivity.this,
 								R.string.tips_saving,
 								Toast.LENGTH_SHORT
 							).show();
 							AsyncSaver asyncSaver = new AsyncSaver(PicbrowActivity.this, mBrow.getImage());
 							asyncSaver.execute(mSaveFile);
 						}
 					}
 				} else {
 					Toast.makeText(context,
 						getString(R.string.err_sdcardnotmounted),
 						Toast.LENGTH_LONG
 					).show();
 				}
 			}
 			
 		});
 		
 		btnPlay.setOnClickListener(new OnClickListener () {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				btnPause.setVisibility(ImageButton.VISIBLE);
 				btnPlay.setVisibility(ImageButton.GONE);
 				
 				mTimer  = new Timer();
 				
 				mTimer.schedule(new TimerTask(){
 
 					public void run() {
 						Message message = new Message();    
 						message.what = 1;    
 						mHandler.sendMessage(message);  
 					}
 					
 				} , 0, 9000);
 			}
 			
 		});
 		
 		btnPause.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (mTimer != null) {
 					mTimer.cancel();
 				}
 				btnPlay.setVisibility(ImageButton.VISIBLE);
 				btnPause.setVisibility(ImageButton.GONE);
 			}
 			
 		});
 	}
 	
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		
 		/*
 		 * for playing
 		 */
 		if (btnPause.getVisibility() == ImageButton.VISIBLE) {
 			mWasPlaying = true;
 			btnPause.performClick();
 			mIsLoading = false;
 		}
 		
 		/*
 		 * for umeng.com
 		 */
 		MobclickAgent.onPause(this);
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		
 		/*
 		 * for playing
 		 */
 		if (mWasPlaying) {
 			mWasPlaying = false;
 			btnPlay.performClick();
 		}
 		
 		/*
 		 * for umeng.com
 		 */
 		MobclickAgent.onResume(this);
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		// TODO Auto-generated method stub
 		super.onConfigurationChanged(newConfig);
 		
 		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			mFrameBackground.setBackgroundResource(R.drawable.bg_h);
 		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
 			mFrameBackground.setBackgroundResource(R.drawable.bg);
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		switch (requestCode) {
 		case EntranceActivity.REQUESTCODE_PICKFILE:
 			if (resultCode == RESULT_OK) {
 				AsyncUploader asyncUploader = new AsyncUploader(this, EntranceActivity.getAccountId());
 				asyncUploader.execute(data);
 			}
 			break;
 		default:
 			break;
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (mIsDooming) {
 				mBrow.setOnTouchListener(this);
 				rlCtrl.setOnTouchListener(this);
 				mIsDooming = false;
 				resetZoomState();
 				Toast.makeText(
 					this,
 					getString(R.string.label_browse),
 					Toast.LENGTH_LONG
 				).show();
 				return true;
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		menu.clear();
 		if (mIsDooming) {
 			menu.add(Menu.NONE, Menu.FIRST + 1, 1, R.string.label_browse).setIcon(android.R.drawable.ic_menu_zoom);
 		} else {
 			menu.add(Menu.NONE, Menu.FIRST + 1, 1, R.string.label_zoom).setIcon(android.R.drawable.ic_menu_zoom);
 		}
 		menu.add(Menu.NONE, Menu.FIRST + 2, 1, R.string.label_reset).setIcon(android.R.drawable.ic_menu_revert);
 		menu.add(Menu.NONE, Menu.FIRST + 3, 1, R.string.label_refresh).setIcon(R.drawable.ic_menu_refresh);
 		menu.add(Menu.NONE, Menu.FIRST + 4, 4, getString(R.string.omenuitem_about)).setIcon(android.R.drawable.ic_menu_help);
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch (item.getItemId()) {
 		case Menu.FIRST + 1:
 			if (getString(R.string.label_zoom).equals(item.getTitle())) {
 				if (mLayoutTop.getVisibility() == LinearLayout.VISIBLE) {
 					fadeoutAnim.setDuration(300);
 					mLayoutTop.startAnimation(fadeoutAnim);
 					mLayoutTop.setVisibility(LinearLayout.INVISIBLE);
 				}
 				item.setTitle(getString(R.string.label_browse));
 				mBrow.setOnTouchListener(mZoomListener);
 				rlCtrl.setOnTouchListener(mZoomListener);
 				mIsDooming = true;
 			} else {
 				if (mLayoutTop.getVisibility() == LinearLayout.INVISIBLE) {
 					mLayoutTop.setVisibility(LinearLayout.VISIBLE);
 					fadeinAnim.setDuration(500);
 					mLayoutTop.startAnimation(fadeinAnim);
 				}
 				item.setTitle(getString(R.string.label_zoom));
 				mBrow.setOnTouchListener(this);
 				rlCtrl.setOnTouchListener(this);
 				mIsDooming = false;
 			}
 	        
 			break;
 		case Menu.FIRST + 2:
 			resetZoomState();
 			break;
 		case Menu.FIRST + 3:
 			zrAsyncShowPic(mId, 0);
 			break;
 		case Menu.FIRST + 4:
 			Intent intent = new Intent();
 			intent.setClass(PicbrowActivity.this, AboutActivity.class);
 			startActivity(intent);
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	/**
 	 * getters here
 	 */
 	public ImageZoomView getBrow() {
 		return mBrow;
 	}
 	
 	public RelativeLayout getCtrlLayout() {
 		return rlCtrl;
 	}
 	
 	/**
      * Reset zoom state and notify observers
      */
     public void resetZoomState() {
     	mZoomControl.getZoomState().setPanX(0.5f);
         mZoomControl.getZoomState().setPanY(0.5f);
         mZoomControl.getZoomState().setZoom(1f);
         mZoomControl.getZoomState().notifyObservers();
     }
 		
 	/**
 	 * we save all file with JPEG format
 	 */
 	public String getSaveFileName(String sOriginalFileName) {
 		String fn = sOriginalFileName;
 		int i = fn.lastIndexOf(".");
 		if (i > 0 && i < (fn.length() - 1)) {
 			fn = fn.substring(0, i);
 		}
 		fn += ".jpg";
 		return fn;
 	}
 	
 	/*
 	 * tell mBrow which picture to show at the very first time
 	 */
 	@Override
 	public View makeView() {
 		// TODO Auto-generated method stub
 		ImageView iv = new ImageView(this);
 		iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
 		iv.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 		iv.setImageResource(R.drawable.broken);
 		return iv;
 	}
 	
 	/*
 	 * set current file information on mTextScreenName
 	 */
 	public void zrRenewCurFileInfo() {
 		WeibouserInfo wi = EntranceActivity.getPicFromId(mId, mUsrs);
 		if (wi == null) return;
 		//tvNums.setText((LetuseeActivity.getPicIndexFromId(mId, mUsrs) + 1) + "/" + mUsrs.size());
 		tvNums.setText(
 			(EntranceActivity.getUsrIndexFromId(mId, mUsrs)
 			+ EntranceActivity.getLimit() * (EntranceActivity.getCurPage() -1)
 			+ 1)
 			+ "/"
 			+ EntranceActivity.getTotalPics()
 		);
 		if (wi.screen_name.trim().equals("")) {
 			mTextScreenName.setVisibility(TextView.GONE);
 		} else {
 			mTextScreenName.setVisibility(TextView.VISIBLE);
 			mTextScreenName.setText(String.format(getString(R.string.info_picture), wi.screen_name + (wi.verified == 1 ? " (V)" : "")));
 		}
 		mTextUpup.setText(wi.likes.toString());
 		mTextDwdw.setText(wi.dislikes.toString());
 		int iTotalVotes = wi.likes + wi.dislikes;
 		int iPercentage = iTotalVotes <= 0 ? 0 : (wi.likes * 100 / iTotalVotes);
 		if (iTotalVotes <= 0) {
 			mProgressVote.setSecondaryProgress(0);
 			mProgressVote.setProgress(0);
 		} else {
 			mProgressVote.setProgress(iPercentage);
 			mProgressVote.setSecondaryProgress(100);
 		}
 		mTextVoteRating.setText(
 			String.format(
 				getString(R.string.tips_voterating), 
 				iPercentage, 
 				iTotalVotes
 			)
 		);
 		if (wi.mLastVote != 0) {
 			llVoteInfo.setVisibility(LinearLayout.VISIBLE);
 		} else {
 			llVoteInfo.setVisibility(LinearLayout.INVISIBLE);
 		}
 	}
 	
 	/*
 	 * show the next, previous or current picture following 2 parameters:
 	 * id, direction
 	 */
 	public void zrAsyncShowPic(long id, int direction) {
 		if (mUsrs == null) return;
 		if (mUsrs.size() == 0) return;
 		AsyncPicLoader asyncPicLoader = new AsyncPicLoader(this);
 		asyncPicLoader.execute(id, direction);
 	}
 	
 	@Override
 	public void finish() {
 		// TODO Auto-generated method stub
 		if (mTimer != null) {
 			mTimer.cancel();
 		}
 		Intent intent = new Intent();
 		intent.putExtra("id", mId);
 		if (this.getParent() == null) {
 			this.setResult(Activity.RESULT_OK, intent);
 		} else {
 			this.getParent().setResult(Activity.RESULT_OK, intent);
 		}
 		super.finish();
 	}
 	
 	/*
 	 * implement asynchronized picture loading by using AsyncTask
 	 */
 	private class AsyncPicLoader extends AsyncTask<Object, Object, Bitmap> {
 		Context mContext;
 		Dialog mPrgDialog;
 		
 		public AsyncPicLoader(Context c) {
 			super();
 			mContext = c;
 			mPrgDialog = new Dialog(mContext, R.style.Dialog_Clean);
 			mPrgDialog.setContentView(R.layout.custom_dialog_loading);
 			((TextView) mPrgDialog.findViewById(R.id.tvCustomDialogTitle)).setText(getString(R.string.msg_loading));
 			mPrgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
 				public void onCancel(DialogInterface dialog) {
 					if (mTimer != null) {
 						mTimer.cancel();
 					}
 					btnPlay.setVisibility(ImageButton.VISIBLE);
 					btnPause.setVisibility(ImageButton.GONE);
 					PicbrowActivity.mIsLoading = false;
 					dialog.cancel();
 				}
         	});
 			mPrgDialog.setCancelable(true);
 			WindowManager.LayoutParams lp = mPrgDialog.getWindow().getAttributes();
 	        lp.alpha = 1.0f;
 	        mPrgDialog.getWindow().setAttributes(lp);
 	        mPrgDialog.show();
 		}
 		
 		@Override
 		protected Bitmap doInBackground(Object... params) {
 			System.gc();
 			System.runFinalization();
 			System.gc();
 			
 			// TODO Auto-generated method stub
 			mIsLoading = true;
 			
 			if (params.length != 2) return bdPicFailed;
 			
 			Long id = (Long) params[0];
 			Integer direction = (Integer) params[1];
 			int idx = EntranceActivity.getUsrIndexFromId(id, mUsrs);
 			if (idx == -1) return bdPicFailed;
 			if (mUsrs.size() == 0) return bdPicFailed;
 			switch (direction) {
 			default:
 			case 0:
 				mId = id;
 				break;
 			case 1:
 				if (idx == 0) {
 					String[] args = EntranceActivity.renewPageArgs(-1);
 					if (args != null) {
 						mUsrs = EntranceActivity.getPics(args);
 						EntranceActivity.setmUsrs(mUsrs);
 					}
 					mId = mUsrs.get(mUsrs.size() - 1).id;
 				}
 				else mId = mUsrs.get(idx - 1).id;
 				break;
 			case 2:
 				if (idx == mUsrs.size() -1) {
 					String[] args = EntranceActivity.renewPageArgs(1);
 					if (args != null) {
 						mUsrs = EntranceActivity.getPics(args);
 						EntranceActivity.setmUsrs(mUsrs);
 					}
 					mId = mUsrs.get(0).id;
 				}
 				else mId = mUsrs.get(idx + 1).id;
 				break;
 			}
 			
 			WeibouserInfo wi = EntranceActivity.getPicFromId(mId, mUsrs);
 			String sPath, sFname;
 			sPath = AsyncSaver.getSdcardDir() + EntranceActivity.PATH_CACHE;
 			sFname = wi.uid + ".jg";
 			if (AsyncSaver.probeFile(sPath, sFname) == -2) {
 				vote("weibouserid", mId.toString(), "clientkey", EntranceActivity.getClientKey(), "vote", "2");
 				Bitmap bmp = BitmapFactory.decodeFile(sPath + "/" + sFname);
 		    	return bmp == null ? bdPicFailed : bmp;
 			} else {
 				SecureUrl su = new SecureUrl();
 				String sUrl = new String(wi.profile_image_url);
 				sUrl = sUrl.replace("/50/", "/180/");
 				URLConnection conn = su.getConnection(sUrl);
 				InputStream is;
 				
 				if (conn == null) return bdPicFailed;
 				try {
 					is = conn.getInputStream();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					Log.e("DEBUGTAG", "Remtoe Image Exception", e);
 					e.printStackTrace();
 					return bdPicFailed;
 				}
 				
 				if (is == null) return bdPicFailed;
 				BufferedInputStream bis = new BufferedInputStream(is, 8192);
 				Bitmap bmp = BitmapFactory.decodeStream(bis);
 				if (bmp == null) return bdPicFailed;
 				try {
 					bis.close();
 					is.close();
 					bis = null;
 					is = null;
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					Log.e("DEBUGTAG", "Remtoe Image Exception", e);
 					e.printStackTrace();
 					bis = null;
 					is = null;
 					return bdPicFailed;
 				}
 				File file = AsyncSaver.getSilentFile(sPath, sFname);
 				if (file != null) {
 					AsyncSaver saver = new AsyncSaver(PicbrowActivity.this, bmp);
 					saver.saveImage(file);
 					saver = null;
 				}
 				String sLastVote = conn.getHeaderField("lastvote");
 				String sLastVoteTime = conn.getHeaderField("lastvotetime");
 				String sClicks = conn.getHeaderField("clicks");
 				String sLikes = conn.getHeaderField("likes");
 				String sDislikes = conn.getHeaderField("dislikes");
 				if (sLastVote != null && sLastVoteTime != null
 					&& sClicks != null && sLikes != null && sDislikes != null) {
 					wi.mLastVote = Integer.parseInt(sLastVote);
 					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 					ParsePosition pp = new ParsePosition(0);
 					wi.mLastVoteTime = sdf.parse(sLastVoteTime, pp);
 					wi.clicks = Integer.parseInt(sClicks);
 					wi.likes = Integer.parseInt(sLikes);
 					wi.dislikes = Integer.parseInt(sDislikes);
 				}
 				return bmp;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Bitmap result) {
 			// TODO Auto-generated method stub
 			mBrow.setImage(result);
 			resetZoomState();
 			fadeinAnim.setDuration(300);
 			mBrow.startAnimation(fadeinAnim);
 			//mBrow.setTag(result);
 			mPrgDialog.dismiss();
 			PicbrowActivity.mIsLoading = false;
 			zrRenewCurFileInfo();
 			super.onPostExecute(result);
 		}
 
 		@Override
 		protected void onProgressUpdate(Object... values) {
 			// TODO Auto-generated method stub
 			super.onProgressUpdate(values);
 		}
 		
 		@Override
         protected void onCancelled() {
 			if (mTimer != null) {
 				mTimer.cancel();
 			}
 			btnPause.setVisibility(ImageButton.GONE);
 			btnPlay.setVisibility(ImageButton.VISIBLE);
 			PicbrowActivity.mIsLoading = false;
             //super.onCancelled();
         }
 	}
 	
 	/*
 	 * GestureListener zone
 	 */
 	public boolean onTouch(View view, MotionEvent event) {
 		if (mIsDooming) {
 			return mZoomListener.getGestureDetector().onTouchEvent(event);
 		} else {
 			return mGestureDetector.onTouchEvent(event);
 		}
     }
 	
 	class PicbrowGestureListener extends GestureDetector.SimpleOnGestureListener {
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			// TODO Auto-generated method stub
 			if (mUsrs.size() == 0) {
 				Toast.makeText(PicbrowActivity.this, getString(R.string.tips_nopictures), Toast.LENGTH_SHORT).show();
 			} else {
 				if (!PicbrowActivity.mIsLoading) {
 					if(e1.getX() > e2.getX()) {//move to left
 						/*
 						if (LetuseeActivity.getPicIndexFromId(mId, mUsrs) == mUsrs.size() - 1) {
 							Toast.makeText(PicbrowActivity.this, getString(R.string.tips_lastone), Toast.LENGTH_SHORT).show();
 						}
 						*/
 						zrAsyncShowPic(mId, 2);
 					} else if (e1.getX() < e2.getX()) {
 						/*
 						if (LetuseeActivity.getPicIndexFromId(mId, mUsrs) == 0) {
 							Toast.makeText(PicbrowActivity.this, getString(R.string.tips_firstone), Toast.LENGTH_SHORT).show();
 						}
 						*/
 						zrAsyncShowPic(mId, 1);
 					}
 				}
 			}
 			return super.onFling(e1, e2, velocityX, velocityY);
 		}
 
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			// TODO Auto-generated method stub
 			if (!mIsDooming) {
 				Toast.makeText(
 					PicbrowActivity.this,
 					R.string.tips_howtobacktobrowse,
 					Toast.LENGTH_SHORT
 				).show();
 				
 				/*
 				ZoomState state = mZoomControl.getZoomState();
 				state.setZoom(state.getZoom() * (float)1.8);
 				state.notifyObservers();
 				mBrow.setOnTouchListener(mZoomListener);
 				rlCtrl.setOnTouchListener(mZoomListener);
 				*/
 				
 				mZoomControl.zoom((float)Math.pow(20, 0.1), 0, 0);
 				mBrow.setOnTouchListener(mZoomListener);
 				rlCtrl.setOnTouchListener(mZoomListener);
 				
 				mIsDooming = true;
 			}
 			
 			//return super.onDoubleTap(e);
 			return true;
 		}
 
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			// TODO Auto-generated method stub
 			//return super.onSingleTapConfirmed(e);
 			if (mLayoutTop.getVisibility() == LinearLayout.VISIBLE) {
 				fadeoutAnim.setDuration(300);
 				mLayoutTop.startAnimation(fadeoutAnim);
 				mLayoutTop.setVisibility(LinearLayout.INVISIBLE);
 			} else {
 				mLayoutTop.setVisibility(LinearLayout.VISIBLE);
 				fadeinAnim.setDuration(500);
 				mLayoutTop.startAnimation(fadeinAnim);
 			}
 			return true;
 		}
 
 		@Override
 		public boolean onSingleTapUp(MotionEvent e) {
 			// TODO Auto-generated method stub
 			
 			//return super.onSingleTapUp(e);
 			return false;
 		}
 		
 	}
 	
 	private boolean vote(String... params) {
 		WeibouserInfo pi = EntranceActivity.getPicFromId(mId, mUsrs);
 				
 		String msg = EntranceActivity.getPhpContentByGet(
 			"vote.php",
 			EntranceActivity.getParamsAsStr(params)
 		);
 		if (msg != null) {
 			String ss[] = EntranceActivity.getPhpMsg(msg);
 			if (ss != null && ss[0].equals(EntranceActivity.SYMBOL_SUCCESSFUL)) {
 				if (ss[1].equals("")) {// means it's never voted
 					// do nothing
 				} else {
 					String[] sRecs = ss[1].split(","); 
 					if (sRecs.length == 8) {
 						pi.mLastVote = Integer.parseInt(sRecs[0]);
 						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 						ParsePosition pp = new ParsePosition(0);
 						pi.mLastVoteTime = sdf.parse(sRecs[1], pp);
 						pi.clicks = Integer.parseInt(sRecs[2]);
 						pi.likes = Integer.parseInt(sRecs[3]);
 						pi.dislikes = Integer.parseInt(sRecs[4]);
 					}
 				}
 				return true;
 			} else return false;
 		} else return false;
 	}
 	
 	/*
 	 * try to vote under background by using AsyncTask
 	 */
 	private class AsyncVoter extends AsyncTask <String, Object, Boolean> {
 		
 		@Override
 		protected void onPostExecute(Boolean result) {
 			// TODO Auto-generated method stub
 			super.onPostExecute(result);
 		}
 
 		@Override
 		protected Boolean doInBackground(String... params) {
 			// TODO Auto-generated method stub
 			return vote(params);
 		}
 		
 	}
 }
