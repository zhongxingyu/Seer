 package com.zrd.zr.letuwb;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
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
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.view.animation.AlphaAnimation;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 import com.zrd.zr.customctrls.ZRImageView;
 import com.zrd.zr.customctrls.ZRScrollView;
 import com.zrd.zr.customctrls.ZRScrollView.OnScrollStoppedListener;
 
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainPage {
 
 	private EntranceActivity parent;
 	/*
 	 * members for layout "main"
 	 * start
 	 */
 	//components
 	private Button mBtnRandom;
 	private Button mBtnLatest;
 	private Button mBtnHottest;
 	private Button mBtnUnhottest;
 	private Button mBtnPossessions;
 	private LinearLayout mLinearMainBottom;
 	private TextView mTextMsgMain;
 	
 	private ZRScrollView mScrollMain;
 	private LinearLayout mLinearLeft;
 	private LinearLayout mLinearMid;
 	private LinearLayout mLinearRight;
 	//assistants
 	private ArrayList<Button> mTopicBtns = null;
 	public static SharedPreferences mPreferences = null;
 	private int _index = 0;
 	private WeibouserInfo _wi;
 	
 	private Handler mHandler = null;
 	
 	private static Integer mTopicChoice = 0;
 	private static final Integer mLimit = 12;//how many pictures should be get in an expanding page
 	private static Integer mCurPage = 1;
 	private Integer mPageBeforeBrow = 1;
 	private static Integer mTotalPics = 0;
 	private static ArrayList<String> mCurTerms = new ArrayList<String>();
 	private static ArrayList<WeibouserInfo> mUsrs = new ArrayList<WeibouserInfo>();
 	/*
 	 * members for layout "main"
 	 * end
 	 */
 	
 	MainPage(EntranceActivity activity) {
 		this.parent = activity;
         mBtnRandom = (Button) activity.findViewById(R.id.btnRandom);
         mBtnLatest = (Button) activity.findViewById(R.id.btnLatest);
         mBtnHottest = (Button) activity.findViewById(R.id.btnHottest);
         mBtnUnhottest = (Button) activity.findViewById(R.id.btnUnhottest);
         mBtnPossessions = (Button) activity.findViewById(R.id.btnPossessions);
         mTopicBtns = new ArrayList<Button>();
         getTopicBtns().add(mBtnLatest);
         getTopicBtns().add(mBtnHottest);
         getTopicBtns().add(mBtnRandom);
         getTopicBtns().add(mBtnUnhottest);
         getTopicBtns().add(mBtnPossessions);
         mLinearMainBottom = (LinearLayout) activity.findViewById(R.id.linearLayoutMainBottom);
         mTextMsgMain = (TextView) activity.findViewById(R.id.tvMsgMain);
         
         mScrollMain = (ZRScrollView) activity.findViewById(R.id.svMain);
         mLinearLeft = (LinearLayout) activity.findViewById(R.id.llLeft);
         mLinearMid = (LinearLayout) activity.findViewById(R.id.llMid);
         mLinearRight = (LinearLayout) activity.findViewById(R.id.llRight);
         
         __init();
         
         mHandler = new Handler() {
 
 			public void handleMessage(Message msg) {
 				switch (msg.what) {
 				case ThreadPNJDealer.DEL_POSSESSION:
 					UCMappings mappings = 
 						(UCMappings) msg.getData().getSerializable(ThreadPNJDealer.KEY_DATA);
 					if (mappings.getFlag() > 0) {
 						//TODO: get rid of the selected possession here
 						int idx = getUsrIndexFromId(_wi.id, mUsrs);
 						mUsrs.remove(idx);
 						mTotalPics--;
 						switch (idx % 3) {
 						case 0:
 							mLinearLeft.removeViewAt(idx / 3);
 							break;
 						case 1:
 							mLinearMid.removeViewAt(idx / 3);
 							break;
 						case 2:
 							mLinearRight.removeViewAt(idx / 3);
 							break;
 						}
 						
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
         mLinearMainBottom.setVisibility(LinearLayout.GONE);
 		/*
 		 * actions
 		 */
 
 	    mBtnRandom.setOnClickListener(new OnClickListener () {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				mBtnRandom.setSelected(true);
 				mBtnLatest.setSelected(false);
 				mBtnHottest.setSelected(false);
 				mBtnUnhottest.setSelected(false);
 				mBtnPossessions.setSelected(false);
 				
 				Toast.makeText(
 					parent,
 					R.string.tips_therandom,
 					Toast.LENGTH_LONG
 				).show();
 				
 				mCurTerms.clear();
 		        mCurTerms.add("top");
 		        mCurTerms.add("6");
 		        mCurPage = 1;
 		        initShow(mCurTerms.get(0), mCurTerms.get(1));
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
 				
 				Toast.makeText(
 					parent,
 					R.string.tips_thelatest,
 					Toast.LENGTH_LONG
 				).show();
 				
 				mCurTerms.clear();
 		        mCurTerms.add("top");
 		        mCurTerms.add("0");
 		        mCurPage = 1;
 		        initShow(mCurTerms.get(0), mCurTerms.get(1));
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
 				
 				mCurTerms.clear();
 		        mCurTerms.add("top");
 		        mCurTerms.add("4");
 		        mCurPage = 1;
 		        initShow(mCurTerms.get(0), mCurTerms.get(1));
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
 				
 				mCurTerms.clear();
 		        mCurTerms.add("top");
 		        mCurTerms.add("5");
 		        mCurPage = 1;
 		        initShow(mCurTerms.get(0), mCurTerms.get(1));
 			}
 	    	
 	    });
 	    
 	    mBtnPossessions.setOnClickListener(new OnClickListener() {
 
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
 				
 				mCurTerms.clear();
 		        mCurTerms.add("clientkey");
 		        mCurTerms.add(EntranceActivity.getClientKey());
 		        mCurPage = 1;
 		        initShow(mCurTerms.get(0), mCurTerms.get(1));
 			}
 	    	
 	    });
 	    
 		mScrollMain.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			//public boolean onTouch(View arg0, MotionEvent arg1) {
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_UP:
 					mScrollMain.startScrollerTask();
 				case MotionEvent.ACTION_DOWN :
 					break;
 				case MotionEvent.ACTION_MOVE :
 					_index++;
 					break;
 				default :
 					break;
 			}
 			if (event.getAction() == MotionEvent.ACTION_UP &&  _index > 0) {
 				_index = 0;
 				View view = ((ZRScrollView) v).getChildAt(0);
 				if (view.getMeasuredHeight() <= v.getScrollY() + v.getHeight()) {
 					/*
 					 * put the process codes of "scroll to the end" bellow.
 					 */
 					if (mUsrs.size() == mTotalPics) {
 						
 					} else {
 						Toast.makeText(
 							parent,
 							R.string.tips_loading,
 							Toast.LENGTH_SHORT
 						).show();
 						expandShow();
 					}
 				}
 			}
 			return false;
 			}
         	
         });
 		
 		mScrollMain.setOnScrollStoppedListener(new OnScrollStoppedListener() {
 
 	        public void onScrollStopped() {
 	        	//TODO: put codes below to deal with the stop of the scrolling
 	        	ArrayList<View> views = mScrollMain.getTouchables();
 	        	/*
 	             * step 1.
 	             * find the first "visible" view, and put the position in
 	             * mUsrs with it into variable "idxUsr"
 	             */
 	            int idxUsr = -1;
 	            for (int i  = 0; i < views.size(); i++) {
 	            	if (views.get(i).getClass().equals(LinearLayout.class)
 	            		&& views.get(i).getTag() != null) {
 	            		Rect rect = new Rect();
 	            		Point point = new Point();
 	            		mScrollMain.getChildVisibleRect(views.get(i), rect, point);
 	            		if (point.y >= 0 
 	            			&& point.y <= (mScrollMain.getBottom() - mScrollMain.getTop())) {
 	            			idxUsr = (Integer) views.get(i).getTag();
 	            			break;
 	            		}
 	            	}
 	            }
 	            /*
 	             * step 2.
 	             * get the part of mUsrs that from (idxUsr - 12) to (idxUsr + 15),
 	             * and get the part of mUsrs that's the rest of the above part, too.
 	             * and then release the images outside the range above, and reload
 	             * the images (if they had been reset before) in the range above.
 	             */
 	            if (idxUsr != -1) {
 	            	int start = idxUsr - 12;
 	            	if (start < 0) start = 0;
 	            	int end = idxUsr + 15;
 	            	if (end >= mUsrs.size()) end = mUsrs.size() - 1;
 	            	for (int i = 0; i < mUsrs.size(); i++) {
 	            		WeibouserInfo wi = mUsrs.get(i);
 	            		LinearLayout ll = (LinearLayout) wi.getTag();
 	            		if (ll != null) {
 		            		ZRImageView v = (ZRImageView) ll.findViewById(R.id.ivPinterest);
 	            			if (!(i >= start && i <= end)) {
 	            				/* 
 	            				 * we don't want to deal with too many images,
 	            				 * we just deal with about 18 pieces here
 	            				 */
 	            				if ((i < start && i >= (start - 9)) 
 	            					|| (i > end && i <= (i + 9))) {
 		            				if (!v.isLoading()) {
 		            					AsyncImageLoader loader = (AsyncImageLoader) v.getTag();
 		            					if (loader != null) {
 		            						loader.cancel(true);
 		            					}
 		            					v.setTag(null);
 		            				}
 		            				v.reset();
 	            				}
 	            			} else {
 	            				if (v.hasBeenReset()) {
 	            					AsyncImageLoader imgLoader = new AsyncImageLoader(
 	        							parent, 
 	        							v, 
 	        							R.drawable.icon_gray
 	        						);
 	            					URL url;
 	            					try {
 	            						url = new URL(wi.getBigger_profile_image_url());
 	            						imgLoader.execute(url);
 	            					} catch (MalformedURLException e) {
 	            						// TODO Auto-generated catch block
 	            						e.printStackTrace();
 	            					}
 	            				}
 	            			}
 	            		}
 	            	}
 	            }
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
 			WeibouserInfo wi = (WeibouserInfo) usrs.get(i); 
 			if (id == wi.id) {
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
     	if (mUsrs.size() != 0) {
     		if (!mBtnPossessions.isSelected()) {
 	    		mTextMsgMain.setText(
 	    			parent.getString(R.string.label_alreadyloaded) 
 	    			+ mUsrs.size()
 	    		);
     		} else {
     			mTextMsgMain.setText(
 	    			parent.getString(R.string.label_alreadyloaded) 
 	    			+ mUsrs.size()
 	    			+ "/" + mTotalPics
 	    			+ parent.getString(R.string.label_possessiondeltips)
 	    		);
     		}
     	}
 		mLinearMainBottom.setVisibility(LinearLayout.VISIBLE);
 		AlphaAnimation anim = new AlphaAnimation(0.1f, 1.0f);
 		anim.setDuration(300);
 		mLinearMainBottom.startAnimation(anim);
     }
     
     public Handler getHandler() {
     	return mHandler;
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
     public static int getTotalPicsNum(String... params) {
  		String sBackMsg = "";
  		if (params.length == 0) {
 			sBackMsg = PNJ.getResponseByGet(
 				EntranceActivity.URL_SITE + "stats.php",
 				PNJ.getParamsAsStr("total", "usrs")
 			);
  		} else {
  			sBackMsg = PNJ.getResponseByGet(
 				EntranceActivity.URL_SITE + "stats.php",
 				PNJ.getParamsAsStr("total", "usrs", "clientkey", params[0])
 			);
  		}
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
 	
 	/*
 	 * expand a "pinterest" liked view without loading the content
 	 */
 	public LinearLayout expandPinterestView(LinearLayout linear) {
 		LayoutInflater inflater = parent.getLayoutInflater();
 		LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.pinterest_item, null);
 
 		TextView text = (TextView) ll.findViewById(R.id.tvPinterest);
 		text.setText(R.string.msg_loading);
 		
 		linear.addView(ll);
 		return ll;
 	}
 	
 	/*
 	 * expand new users to mUsrs
 	 */
 	public ArrayList<WeibouserInfo> expandUsrs(String... params) {
 		ArrayList<WeibouserInfo> usrs = getPics(params);
 		
 		/*
 		 * check the returned usrs to see if mUsrs contains some of it,
 		 * if it does, then get rid of the ones which already exist in
 		 * mUsrs from usrs.
 		 */
 		for (int i = 0; i < usrs.size(); i++) {
 			if (mUsrs.indexOf(usrs.get(i)) != -1) {
 				usrs.remove(i);
 				i--;
 			}
 		}
 		
 		int num;
 		if (!mBtnPossessions.isSelected()) {
 			num = getTotalPicsNum();
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
 			num = getTotalPicsNum(EntranceActivity.getClientKey());
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
 		}
 
 		for (int i = 0; i < usrs.size(); i++) {
 			mUsrs.add(usrs.get(i));
 		}
 		
 		return usrs;
 	}
 	
 	/*
 	 * show the very first pinterest pieces
 	 */
 	public void initShow(String termName, String termValue) {
 		mLinearLeft.removeAllViews();
 		mLinearMid.removeAllViews();
 		mLinearRight.removeAllViews();
 		mScrollMain.scrollTo(0, 0);
 		AsyncExpand expand = new AsyncExpand(expandPinterests());
 		expand.execute(termName, termValue);
 	}
 	
 	public void expandShow() {
 		AsyncExpand expand = new AsyncExpand(expandPinterests());
 		expand.execute();
 	}
 	
 	/*
 	 * expand the views in pinterest view:
 	 * we will add "mLimit" numbers of pieces into the pinterest view 
 	 * at once without loading the contents.
 	 */
 	public ArrayList<LinearLayout> expandPinterests() {
 		ArrayList<LinearLayout> list = new ArrayList<LinearLayout>();
 		for (int i = 0; i < mLimit; i += 3) {
 			list.add(expandPinterestView(mLinearLeft));
 			if (i + 1 < mLimit) {
 				list.add(expandPinterestView(mLinearMid));
 			}
 			if (i + 2 < mLimit) {
 				list.add(expandPinterestView(mLinearRight));
 			}
 		}
 		return list;
 	}
 	
 	/*
 	 * load the contents for the views in pinterest
 	 */
 	public void loadPinterests(ArrayList<LinearLayout> views, ArrayList<WeibouserInfo> usrs) {
 		if (usrs.size() <= views.size()) {
 			for (int i = 0; i < usrs.size(); i++) {
 				LinearLayout ll = views.get(i);
 				WeibouserInfo wi = usrs.get(i);
 				
 				ZRImageView zrImg = (ZRImageView) ll.findViewById(R.id.ivPinterest);
 				TextView text = (TextView) ll.findViewById(R.id.tvPinterest);
 				
 				AsyncImageLoader imgLoader = new AsyncImageLoader(
 					parent, 
 					zrImg
 				);
 				zrImg.setTag(imgLoader);
 				URL url;
 				try {
 					url = new URL(wi.getBigger_profile_image_url());
 					//imgLoader.execute(url, true);//load into memory
 					imgLoader.execute(url);
 				} catch (MalformedURLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				text.setText(
 					Html.fromHtml(
 						"<font color='#1da7ef'>"
 						+ wi.description
 						+ "</font>"
 					)
 				);
 				
 				ll.setTag(getUsrIndexFromId(wi.id, mUsrs));
 				wi.setTag(ll);
 				ll.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View arg0) {
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
 								WeibouserInfo wi = mUsrs.get((Integer) arg0.getTag());
								if (mBtnPossessions.isSelected()) {
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
 				
 				ll.setOnLongClickListener(new OnLongClickListener(){
 
 					@Override
 					public boolean onLongClick(View arg0) {
 						// TODO Auto-generated method stub
 						if (mBtnPossessions.isSelected()) {
 							_wi = mUsrs.get((Integer) arg0.getTag());
 							new AlertDialog.Builder(parent)
 								.setTitle(R.string.tips_confirmdelpossession)
 								.setIcon(android.R.drawable.ic_dialog_alert)
 								.setPositiveButton(
 									R.string.label_ok,
 									new DialogInterface.OnClickListener() {
 				
 										@Override
 										public void onClick(DialogInterface dialog,
 												int which) {
 											// TODO Auto-generated method stub
 											new Thread(
 												new ThreadPNJDealer(
 													ThreadPNJDealer.DEL_POSSESSION,
 													EntranceActivity.URL_SITE
 														+ "delpzs.php?"
 														+ "clientkey=" + EntranceActivity.getClientKey()
 														+ "&channelid=0"
 														+ "&uid=" + _wi.uid,
 													mHandler
 												)
 											).start();
 											Toast.makeText(
 												parent,
 												R.string.tips_possessioncanceling,
 												Toast.LENGTH_SHORT
 											).show();
 										}
 										
 									}
 								)
 								.setNegativeButton(R.string.label_cancel, null)
 								.create()
 								.show();
 						} else {
 							
 						}
 						
 						return false;
 					}
 					
 				});
 			}
 		} else {
 			//TODO: put the cutting codes below
 		}
 	}
 
 	/*
      * classes
      */
 	class AsyncExpand extends AsyncTask<String, Object, ArrayList<WeibouserInfo>> {
 		private ArrayList<LinearLayout> mViews;
 		
 		AsyncExpand(ArrayList<LinearLayout> views) {
 			this.mViews = views;
 		}
 		
 		@Override
 		protected void onPostExecute(ArrayList<WeibouserInfo> usrs) {
 			// TODO Auto-generated method stub
 			
 			if (mViews != null) {
 				loadPinterests(mViews, usrs);
 				renewCurParagraphTitle();
 			}
 			
 			super.onPostExecute(usrs);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see android.os.AsyncTask#doInBackground(Params[])
 		 * if no params at all, it'll only expand a number (of mLimit) of
 		 * weibousers to the pinterest; if there are 2 params, it'll clear
 		 * the pinterest and expand a number (of mLimit) of weibousers at
 		 * the very beginning.
 		 */
 		@Override
 		protected ArrayList<WeibouserInfo> doInBackground(String... params) {
 			// TODO Auto-generated method stub
 			ArrayList<WeibouserInfo> usrs = new ArrayList<WeibouserInfo>();
 			if (params.length == 0) {
 				mCurPage++;
 				usrs = expandUsrs(
 					mCurTerms.get(0), mCurTerms.get(1),
 					"limit", mLimit.toString(),
 					"page", mCurPage.toString()
 				);
 			} else if (params.length == 2){
 				String termName = params[0];
 				String termValue = params[1];
 				mUsrs.clear();
 				mCurPage = 1;
 				usrs = expandUsrs(
 					termName, termValue,
 					"limit", mLimit.toString(), 
 					"page", mCurPage.toString()
 				);
 			}
 			
 			return usrs;
 		}
 		
 	}
 }
