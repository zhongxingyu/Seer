 package com.zrd.zr.letuwb;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.zrd.zr.pnj.PNJ;
 import com.zrd.zr.pnj.SecureURL;
 import com.zrd.zr.pnj.ThreadPNJDealer;
 import com.zrd.zr.protos.WeibousersProtos.UCMappings;
 import com.zrd.zr.protos.WeibousersProtos.Weibouser;
 import com.zrd.zr.protos.WeibousersProtos.Weibousers;
 import com.zrd.zr.weiboes.Sina;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.Message;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.animation.AlphaAnimation;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 public class MainPage {
 
 	private EntranceActivity parent;
 	/*
 	 * members for layout "main"
 	 * start
 	 */
 	//components
 	private GridView mGridPics;
 	private TextView mTextPageInfo;
 	private Button mBtnRandom;
 	private Button mBtnLatest;
 	private Button mBtnHottest;
 	private Button mBtnUnhottest;
 	private Button mBtnPossessions;
 	private SeekBar mSeekMain;
 	private ImageButton mBtnPrev;
 	private ImageButton mBtnNext;
 	private TextView mTextSeekPos;
 	private LinearLayout mLinearMainBottom;
 	//
 	private ArrayList<Button> mTopicBtns = null;
 	private GestureDetector mGestureDetector = null;
 	public static SharedPreferences mPreferences = null;
 	
 	private Handler mHandler = null;
 	
 	public Dialog mPrgDlg;
 	
 	private static Integer mTopicChoice = 0;
 	private static final Integer mLimit = 28;//how many pictures should be passed to PicbrowActivity, actually multiple of mPageLimit is recommended
 	private final Integer mPageLimit = 4;//how many pictures should be loaded into mGridPics.
 	private static Integer mCurPage = 1;
 	private Integer mPageBeforeBrow = 1;
 	private static Integer mCurParagraph = 1;
 	private static Integer mTotalPages = 0;
 	private static Integer mTotalPics = 0;
 	private static ArrayList<String> mCurTerms = new ArrayList<String>();
 	private static ArrayList<WeibouserInfo> mUsrs = new ArrayList<WeibouserInfo>();
 	ArrayList<WeibouserInfo> mPageUsrs = new ArrayList<WeibouserInfo>();
 	/*
 	 * members for layout "main"
 	 * end
 	 */
 	
 	MainPage(EntranceActivity activity) {
 		this.parent = activity;
         mGridPics = (GridView) activity.findViewById(R.id.gridViewPics);
         mTextPageInfo = (TextView) activity.findViewById(R.id.tvPageInfo);
         mBtnRandom = (Button) activity.findViewById(R.id.btnRandom);
         mBtnLatest = (Button) activity.findViewById(R.id.btnLatest);
         mBtnHottest = (Button) activity.findViewById(R.id.btnHottest);
         mBtnUnhottest = (Button) activity.findViewById(R.id.btnUnhottest);
         mBtnPossessions = (Button) activity.findViewById(R.id.btnPossessions);
         setTopicBtns(new ArrayList<Button>());
         getTopicBtns().add(mBtnLatest);
         getTopicBtns().add(mBtnHottest);
         getTopicBtns().add(mBtnRandom);
         getTopicBtns().add(mBtnUnhottest);
         getTopicBtns().add(mBtnPossessions);
         mSeekMain = (SeekBar) activity.findViewById(R.id.sbMain);
         mBtnPrev = (ImageButton) activity.findViewById(R.id.btnPrev);
         mBtnNext = (ImageButton) activity.findViewById(R.id.btnNext);
         mTextSeekPos = (TextView) activity.findViewById(R.id.tvSeekPos);
         mLinearMainBottom = (LinearLayout) activity.findViewById(R.id.linearLayoutMainBottom);
         mGestureDetector = new GestureDetector(activity, new MainGestureListener());
         
         __init();
         
         mHandler = new Handler() {
 
 			public void handleMessage(Message msg) {
 				switch (msg.what) {
 				case ThreadPNJDealer.DEL_POSSESSION:
 					UCMappings mappings = 
 						(UCMappings) msg.getData().getSerializable(ThreadPNJDealer.KEY_DATA);
 					if (mappings.getFlag() > 0) {
 						WeibouserInfo wi = (WeibouserInfo) mGridPics.getTag();
 						int idx = getUsrIndexFromId(wi.id, getUsrs());
 						getUsrs().remove(idx);
 						int totalPics = getTotalPics() - 1;
 						setTotalPics(totalPics);
 						if (mPageUsrs.contains(wi)) {
 							WeibouserInfoGridAdapter adapter = (WeibouserInfoGridAdapter) mGridPics.getAdapter();
 							adapter.remove(wi);
 							adapter.notifyDataSetChanged();
 						}
 						//mPageUsrs.remove(position);//kind of repeatedly doing the same thing with "adapter.remove(wi)", so it should not be called
 						renewCurParagraphTitle();
 						Toast.makeText(
 							parent,
 							R.string.tips_possessionremoved,
 							Toast.LENGTH_SHORT
 						).show();
 						return;
 					}
 					break;
 				}
 			}
         };
 
 	}
 	
 	private void __init() {
         mGridPics.setOnTouchListener((OnTouchListener) parent);
         mLinearMainBottom.setVisibility(LinearLayout.GONE);
 	    mTextSeekPos.setVisibility(TextView.GONE);
 	    mSeekMain.setMax(0);
 		/*
 		 * actions
 		 */    
 	    mSeekMain.setOnSeekBarChangeListener(
 	    	new OnSeekBarChangeListener() {
 
 				@Override
 				public void onProgressChanged(SeekBar seekBar, int progress,
 						boolean fromUser) {
 					// TODO Auto-generated method stub
 					int p;
 					if (progress == 0) p = 1;
 					else p = progress;
 					mTextSeekPos.setText("" + ((p - 1) * getPageLimit() + 1) + "~" + (p * getPageLimit()));
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
 					int idxPic = getPageLimit() * progress;
 					Integer page;
 					if (idxPic % mLimit == 0) {
 						page = idxPic / mLimit;
 					} else {
 						page = (int) Math.ceil((double) idxPic / (double) mLimit);
 					}
 					int max = (int) Math.ceil((double) mLimit / (double) getPageLimit());
 					Integer paragraph = progress % max == 0 ? max : (progress % max);
 					
 					mPrgDlg.show();
 					AsyncGridLoader agl = new AsyncGridLoader(parent);
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
 					setCurParagraph(paragraph);
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
 	    
 	    mBtnPrev.setOnClickListener(new OnClickListener () {
 
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
 				mBtnPossessions.setSelected(false);
 				
 				AsyncGridLoader asyncGridLoader = new AsyncGridLoader(parent);
 				mPrgDlg.show();
 				mCurTerms.clear();
 		        mCurTerms.add("top");
 		        mCurTerms.add("6");
 		        mCurPage = 1;
 		        setCurParagraph(1);
 		        asyncGridLoader.execute(mCurTerms.get(0), mCurTerms.get(1), "limit", mLimit.toString(), "page", mCurPage.toString());
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
 				mBtnPossessions.setSelected(false);
 				
 				AsyncGridLoader asyncGridLoader = new AsyncGridLoader(parent);
 				mPrgDlg.show();
 				mCurTerms.clear();
 		        mCurTerms.add("top");
 		        mCurTerms.add("0");
 		        mCurPage = 1;
 		        setCurParagraph(1);
 		        asyncGridLoader.execute(mCurTerms.get(0), mCurTerms.get(1), "limit", mLimit.toString(), "page", mCurPage.toString());
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
 				mBtnPossessions.setSelected(false);
 				
 				Toast.makeText(
 					parent,
 					R.string.tips_hottesttheweek,
 					Toast.LENGTH_LONG
 				).show();
 				
 				AsyncGridLoader asyncGridLoader = new AsyncGridLoader(parent);
 				mPrgDlg.show();
 				mCurTerms.clear();
 		        mCurTerms.add("top");
 		        mCurTerms.add("4");
 		        mCurPage = 1;
 		        setCurParagraph(1);
 		        asyncGridLoader.execute(mCurTerms.get(0), mCurTerms.get(1), "limit", mLimit.toString(), "page", mCurPage.toString());
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
 				mBtnPossessions.setSelected(false);
 				
 				Toast.makeText(
 					parent,
 					R.string.tips_unhottesttheweek,
 					Toast.LENGTH_LONG
 				).show();
 				
 				AsyncGridLoader asyncGridLoader = new AsyncGridLoader(parent);
 				mPrgDlg.show();
 				mCurTerms.clear();
 		        mCurTerms.add("top");
 		        mCurTerms.add("5");
 		        mCurPage = 1;
 		        setCurParagraph(1);
 		        asyncGridLoader.execute(mCurTerms.get(0), mCurTerms.get(1), "limit", mLimit.toString(), "page", mCurPage.toString());
 			}
 	    	
 	    });
 	    
 	    mBtnPossessions.setOnClickListener(new OnClickListener () {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				mBtnRandom.setSelected(false);
 				mBtnLatest.setSelected(false);
 				mBtnHottest.setSelected(false);
 				mBtnUnhottest.setSelected(false);
 				mBtnPossessions.setSelected(true);
 				
 				Toast.makeText(
 					parent,
 					R.string.tips_possessions,
 					Toast.LENGTH_LONG
 				).show();
 				
 				AsyncGridLoader asyncGridLoader = new AsyncGridLoader(parent);
 				mPrgDlg.show();
 				mCurTerms.clear();
 		        mCurTerms.add("clientkey");
 		        mCurTerms.add(EntranceActivity.getClientKey());
 		        mCurPage = 1;
 		        setCurParagraph(1);
 		        asyncGridLoader.execute(mCurTerms.get(0), mCurTerms.get(1), "limit", mLimit.toString(), "page", mCurPage.toString());
 			}
 	    	
 	    });
 	    
 	    mPrgDlg = new Dialog(parent, R.style.Dialog_Clean);
 	    mPrgDlg.setContentView(R.layout.custom_dialog_loading);
 	    WindowManager.LayoutParams lp = mPrgDlg.getWindow().getAttributes();
 	    lp.alpha = 1.0f;
 	    mPrgDlg.getWindow().setAttributes(lp);
 	    mPrgDlg.setCancelable(true);
 			            
 		mGridPics.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> pv, View v, int position, long id) {
 				// TODO Auto-generated method stub
 				Sina sina = WeiboPage.getSina();
 				if (EntranceActivity.isNowLoggingIn()) {
 					Toast.makeText(
 						parent,
 						R.string.tips_nowisloggingin,
 						Toast.LENGTH_LONG
 					).show();
 				} else {
 					if (sina != null && sina.isLoggedIn()) {
 						WeibouserInfo wi = (WeibouserInfo) mPageUsrs.get(position);
 						if (mBtnPossessions.isSelected()) {
 							/*
 							Intent intent = new Intent();
 							intent.putExtra("uid", wi.uid);
 			                intent.putExtra("id", wi.id);
 							intent.setClass(parent, WeiboShowActivity.class);
 							parent.startActivity(intent);
 							*/
 							
 							parent.getWeiboPage().setReferer(R.layout.main);
							parent.getWeiboPage().reloadLastUser(wi.uid);
 							parent.switchPage(R.layout.weibo_show, wi.uid, wi.id);
 						} else {
 							parent.getBrowPage().setReferer(R.layout.main);
 							parent.switchPage(R.layout.brow, wi.id);
 						}
 					} else {
 						Toast.makeText(
 							parent,
 							R.string.tips_havetologin,
 							Toast.LENGTH_LONG
 						).show();
 						Intent intent = new Intent();
 						intent.setClass(parent, RegLoginActivity.class);
 						parent.startActivity(intent);
 					}
 				}
 			}
 	    });
 		
 		mGridPics.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> pv, View v,
 					int position, long id) {
 				// TODO Auto-generated method stub
 				if (mBtnPossessions.isSelected()) {
 					mGridPics.setTag(position);
 					new AlertDialog.Builder(parent)
 						.setTitle(R.string.tips_confirmdelpossession)
 						.setPositiveButton(
 							R.string.label_ok,
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									// TODO Auto-generated method stub
 									int position = (Integer)mGridPics.getTag();
 									WeibouserInfo wi = mPageUsrs.get(position);
 									mGridPics.setTag(wi);
 									
 									new Thread(
 										new ThreadPNJDealer(
 											ThreadPNJDealer.DEL_POSSESSION,
 											EntranceActivity.URL_SITE
 												+ "delpzs.php?"
 												+ "clientkey=" + EntranceActivity.getClientKey()
 												+ "&channelid=0"
 												+ "&uid=" + wi.uid,
 											mHandler
 										)
 									).start();
 									Toast.makeText(
 										parent,
 										R.string.tips_possessioncanceling,
 										Toast.LENGTH_SHORT
 									).show();
 									dialog.dismiss();
 								}
 
 							}
 						)
 						.setNegativeButton(R.string.label_cancel, null)
 						.create()
 						.show();
 						
 				}
 				return false;
 			}
 			
 		});
 	}
 	
 	/*
 	 * methods
 	 */
 	public Integer getLimit() {
 		return mLimit;
 	}
 	
 	public Integer getCurPage() {
 		return mCurPage;
 	}
 	
 	public Integer getTotalPics() {
 		return mTotalPics;
 	}
 	
 	public void setTotalPics(int total) {
 		mTotalPics = total;
 	}
 	
 	public ArrayList<WeibouserInfo> getUsrs() {
 		return mUsrs;
 	}
 	
 	public void setUsrs(ArrayList<WeibouserInfo> pics) {
 		mUsrs = pics;
 	}
 		
 	/*
 	 * get index of current usr in mUsrs by id
 	 */
 	public int getUsrIndexFromId(long id, List<WeibouserInfo> usrs) {
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
 	public WeibouserInfo getPicFromId(long id, List<WeibouserInfo> pics) {
 		int idx = getUsrIndexFromId(id, pics);
 		if (idx < 0 || idx >= pics.size()) return null;
 		return pics.get(idx);
 	}
 	
     public ArrayList<WeibouserInfo> getPics(String... params) {
     	ArrayList<WeibouserInfo> usrs = new ArrayList<WeibouserInfo>();
     	
     	String sParams = PNJ.getParamsAsStr(params);
     	SecureURL su = new SecureURL();
     	URLConnection conn = su.getConnection(EntranceActivity.URL_SITE + "picsinfo.php?" + sParams);
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
      * using protobuf structure to get users information from DB
      */
     public UCMappings updateUser(String... params) {
     	SecureURL su = new SecureURL();
     	URLConnection conn = su.getConnection(
     			EntranceActivity.URL_SITE + "updusr.php"
     		+ PNJ.getParamsAsStr(params)
     	);
     	if (conn == null) return null;
     	try {
 			conn.connect();
 			InputStream is = conn.getInputStream();
 			return UCMappings.parseFrom(is);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
     }
     
     /*
      * renew the current paragraph informations
      */
     public void renewCurParagraphTitle() {
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
 			parent.getString(R.string.tips_pages),
 			(getCurParagraph() - 1) * getPageLimit() + (mCurPage - 1) * mLimit + 1,
 			(getCurParagraph() - 1) * getPageLimit() + (mCurPage - 1) * mLimit + mPageUsrs.size(),
 			mTotalPics
 		);
 
     	mSeekMain.setProgress(
     		(mCurPage - 1)
     		* (mLimit % getPageLimit() == 0 ? mLimit / getPageLimit() : mLimit / getPageLimit() + 1)
     		+ getCurParagraph()
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
 	
 	public String[] renewPageArgs(int direction) {
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
     	if (mUsrs.size() == 0) return;
     	double maxParagraph = Math.ceil((float)mUsrs.size() / (float) getPageLimit());
     	setCurParagraph(getCurParagraph() + 1);
 		if (getCurParagraph() >  maxParagraph) {
 			setCurParagraph(getCurParagraph() - 1);
 			String[] args = renewPageArgs(1);
 			if (args != null) {
 				mPrgDlg.show();
 				AsyncGridLoader agl = new AsyncGridLoader(parent);
 				setCurParagraph(1);
 				agl.execute(args);
 			}
 		} else {
 			mPrgDlg.show();
 			mPageUsrs.clear();
 			for (int i = (getCurParagraph() -1) * getPageLimit(); i < getCurParagraph() * getPageLimit() && i < mUsrs.size(); i++) {
 				mPageUsrs.add(mUsrs.get(i));
 			}
 			WeibouserInfoGridAdapter adapter = new WeibouserInfoGridAdapter(parent, mPageUsrs, mGridPics, mBtnPossessions.isSelected());
 			mGridPics.setAdapter(adapter);
 			renewCurParagraphTitle();
 		}
     }
     
     public void previous() {
     	double maxParagraph = Math.ceil((float)mUsrs.size() / (float) getPageLimit());
     	setCurParagraph(getCurParagraph() - 1);
 		if (getCurParagraph() < 1) {
 			setCurParagraph(getCurParagraph() + 1);
 			String[] args = renewPageArgs(-1);
 			if (args != null) {
 				mPrgDlg.show();
 				AsyncGridLoader agl = new AsyncGridLoader(parent);
 				setCurParagraph((int) maxParagraph);
 				agl.execute(args);
 			}
 		} else {
 			mPrgDlg.show();
 			mPageUsrs.clear();
 			for (int i = (getCurParagraph() -1) * getPageLimit(); i < getCurParagraph() * getPageLimit() && i < mUsrs.size(); i++) {
 				mPageUsrs.add(mUsrs.get(i));
 			}
 			WeibouserInfoGridAdapter adapter = new WeibouserInfoGridAdapter(parent, mPageUsrs, mGridPics, mBtnPossessions.isSelected());
 			mGridPics.setAdapter(adapter);
 			renewCurParagraphTitle();
 		}
     }
     
     public Handler getHandler() {
     	return mHandler;
     }
     
     public GridView getGridPics() {
 		return mGridPics;
 	}
 
 	public void setGridPics(GridView mGridPics) {
 		this.mGridPics = mGridPics;
 	}
 
 	public static Integer getTopicChoice() {
 		return mTopicChoice;
 	}
 
 	public void setTopicChoice(Integer mTopicChoice) {
 		MainPage.mTopicChoice = mTopicChoice;
 	}
 
 	public Integer getPageBeforeBrow() {
 		return mPageBeforeBrow;
 	}
 
 	public void setPageBeforeBrow(Integer mPageBeforeBrow) {
 		this.mPageBeforeBrow = mPageBeforeBrow;
 	}
 
 	public ArrayList<Button> getTopicBtns() {
 		return mTopicBtns;
 	}
 
 	public void setTopicBtns(ArrayList<Button> mTopicBtns) {
 		this.mTopicBtns = mTopicBtns;
 	}
 
 	public Integer getPageLimit() {
 		return mPageLimit;
 	}
 
 	public Integer getCurParagraph() {
 		return mCurParagraph;
 	}
 
 	public void setCurParagraph(Integer mCurParagraph) {
 		MainPage.mCurParagraph = mCurParagraph;
 	}
 	
     /*
 	 * get total pages number
 	 */
     public int getTotalPagesNum() {
 		String sBackMsg = "";
 		sBackMsg = PNJ.getResponseByGet(
 			EntranceActivity.URL_SITE + "stats.php",
 			PNJ.getParamsAsStr("total", "pages", "limit", mLimit.toString())
 		);
 		if (sBackMsg != null) {
 			String ss[] = EntranceActivity.getPhpMsg(sBackMsg);
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
     public static int getTotalPicsNum() {
  		String sBackMsg = "";
 		sBackMsg = PNJ.getResponseByGet(
 			EntranceActivity.URL_SITE + "stats.php",
 			PNJ.getParamsAsStr("total", "usrs")
 		);
 		if (sBackMsg != null) {
 			String ss[] = EntranceActivity.getPhpMsg(sBackMsg);
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
 
 	public GestureDetector getGestureDetector() {
 		return mGestureDetector;
 	}
 
 	public void setGestureDetector(GestureDetector mGestureDetector) {
 		this.mGestureDetector = mGestureDetector;
 	}
 
 	/*
      * classes
      */
 	class MainGestureListener extends GestureDetector.SimpleOnGestureListener {
 
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
 			mPrgDlg.dismiss();
 			if (mPageUsrs.size() == 0) {
 				AlertDialog alertDlg = new AlertDialog.Builder(mContext)
 					.setIcon(android.R.drawable.ic_dialog_info)
 					.setTitle(R.string.msg_nopictures)
 					//.setMessage(R.string.msg_nopictures)
 					.setPositiveButton(R.string.label_ok, null)
 					.create();
 				WindowManager.LayoutParams lp = alertDlg.getWindow().getAttributes();
 		        lp.alpha = 0.9f;
 				alertDlg.getWindow().setAttributes(lp);
 		        alertDlg.show();	
 			}
 			mGridPics.setAdapter(result);
 			//super.onPostExecute(result);
 		}
 
 		@Override
 		protected WeibouserInfoGridAdapter doInBackground(String... params) {
 			// TODO Auto-generated method stub
 			mUsrs = getPics(params);
 			
 			if (!mBtnPossessions.isSelected()) {
 				int num = getTotalPicsNum();
 				if (num < 0) {
 					Toast.makeText(
 						parent,
 						R.string.err_noconnection,
 						Toast.LENGTH_LONG
 					).show();
 					mTotalPics = 0;
 				} else {
 					mTotalPics = num;
 				}
 			} else {
 				mTotalPics = mUsrs.size();
 			}
 			mTotalPages = (int) Math.ceil((float)mTotalPics / (float)mLimit);
 			mSeekMain.setMax(
 				mTotalPics == 0 ? 0 : ((int) Math.ceil((double)mTotalPics / (double)getPageLimit()))
 			);
 			
 			mPageUsrs.clear();
 			for (int i = (getCurParagraph() - 1) * getPageLimit(); i < mUsrs.size() && i < getCurParagraph() * getPageLimit(); i++) {
 				mPageUsrs.add(mUsrs.get(i));
 			}
 			WeibouserInfoGridAdapter adapter = new WeibouserInfoGridAdapter(
 				(EntranceActivity) mContext, mPageUsrs, mGridPics,
 				mBtnPossessions.isSelected()
 			);
 			return adapter;
 		}
     	
     }
 }
