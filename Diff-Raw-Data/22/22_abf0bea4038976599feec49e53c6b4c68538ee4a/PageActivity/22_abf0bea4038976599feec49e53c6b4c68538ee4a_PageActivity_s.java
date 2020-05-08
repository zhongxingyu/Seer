 package com.osastudio.newshub;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xmlpull.v1.XmlSerializer;
 
 import com.huadi.azker_phone.R;
 import com.osastudio.newshub.NewsApp.TempCacheData;
 import com.osastudio.newshub.cache.NewsAbstractCache;
 import com.osastudio.newshub.cache.SubscriptionAbstractCache;
 import com.osastudio.newshub.data.NewsAbstract;
 import com.osastudio.newshub.data.NewsAbstractList;
 import com.osastudio.newshub.data.NewsArticle;
 import com.osastudio.newshub.data.NewsChannel;
 import com.osastudio.newshub.data.NewsColumnistInfo;
 import com.osastudio.newshub.data.NewsNoticeArticle;
 import com.osastudio.newshub.data.NewsResult;
 import com.osastudio.newshub.data.RecommendedTopicIntro;
 import com.osastudio.newshub.data.SubscriptionAbstract;
 import com.osastudio.newshub.data.SubscriptionArticle;
 import com.osastudio.newshub.net.NewsArticleApi;
 import com.osastudio.newshub.net.NewsColumnistApi;
 import com.osastudio.newshub.net.NewsNoticeApi;
 import com.osastudio.newshub.net.RecommendApi;
 import com.osastudio.newshub.net.SubscriptionApi;
 import com.osastudio.newshub.utils.NetworkHelper;
 import com.osastudio.newshub.utils.NewsResultAsyncTask;
 import com.osastudio.newshub.utils.Utils;
 import com.osastudio.newshub.widgets.BaseAssistent;
 import com.osastudio.newshub.widgets.FileView;
 import com.osastudio.newshub.widgets.SlideSwitcher;
 import com.osastudio.newshub.widgets.SummaryGrid;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.text.Html;
 import android.util.Xml;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class PageActivity extends NewsBaseActivity {
 	public static final String DIRECT_ENTER="direct_enter";
 	public static final String PAGE_TYPE = "page_type";
 	public static final String START_INDEX = "Start_index";
 	public static final String CATEGORY_TITLE = "Category_title";
 	public static final String PAGE_ID = "page_id";
 
 	public int mPageType;
 	public int mCurrentId = 0;
 	public String mCategoryTitle = null;
 	public int mCurrentShowId = -1;
 	public String mPageId = null;
 
 	private NewsApp mApp = null;
 	private LayoutInflater mInflater = null;
 
 	private List<SubscriptionAbstract> mUserIssueList = null;
 	private ArrayList<TempCacheData> mCacheList = null;
 
 	private SlideSwitcher mSwitcher = null;
 	private int mTouchSlop;
 	private int mDirection = -1; // 0 is preview; 1 is next;
 	private int mInitX, mInitY;
 	private int mBaseX, mBaseY;
 	private boolean mbSwitchAble = true;
 
 	private String mHtmlCotent = null;
 	private String mTitle = null;
 	private boolean mNeedFeedback; // for notice
 	private String mNoticeId = null;
 
 	private String mSummary = null; // simple//for expert
 	private String mResume = null; // for expert
 	private String mIconUrl = null; // for expert
 
 	private LoadDataTask mTask = null;
 
 	private int mTextSize = 18;
 	private boolean mIsWIFI = true;
 	private boolean mDirectEnter = false;
 	private boolean mIsDisplayTop = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_switcher);
 
 		mIsWIFI = NetworkHelper.isWifiEnabled(this);
 		mApp = (NewsApp) getApplication();
 		mInflater = LayoutInflater.from(this);
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			mPageType = extras.getInt(PAGE_TYPE);
 			mCurrentId = extras.getInt(START_INDEX);
 			mCategoryTitle = extras.getString(CATEGORY_TITLE);
 			if (mCurrentId < 0) {
 				mDirectEnter = extras.getBoolean(DIRECT_ENTER, false);
 				mPageId = extras.getString(PAGE_ID);
 				ArrayList<TempCacheData> cacheList = new ArrayList<TempCacheData>();
 				cacheList.add(new TempCacheData(mPageId));
 				mApp.setTempCache(cacheList);
 				mCurrentId = 0;
 
 			}
 		}
 
 		ViewConfiguration configuration = ViewConfiguration.get(this);
 		mTouchSlop = configuration.getScaledTouchSlop();
 		mSwitcher = (SlideSwitcher) findViewById(R.id.switcher);
 
 		setupData();
 	}
 	
 	@Override
 	protected void onNewIntent(Intent intent) {
 	   super.onNewIntent(intent);
 	   Bundle extras = intent.getExtras();
       if (extras != null) {
          mPageType = extras.getInt(PAGE_TYPE);
          mCurrentId = extras.getInt(START_INDEX);
          mCategoryTitle = extras.getString(CATEGORY_TITLE);
          if (mCurrentId < 0) {
             mDirectEnter = extras.getBoolean(DIRECT_ENTER, false);
             mPageId = extras.getString(PAGE_ID);
             ArrayList<TempCacheData> cacheList = new ArrayList<TempCacheData>();
             cacheList.add(new TempCacheData(mPageId));
             mApp.setTempCache(cacheList);
             mCurrentId = 0;
 
          }
       }
 
       Utils.logd("Page", "onNewIntent type"+mPageType);
       setupData();
 	}
 	
 
 	@Override
 	protected void onResume() {
 		mTextSize = getPrefsManager().getFontSize();
 		mIsWIFI = NetworkHelper.isWifiEnabled(this);
 		super.onResume();
 	}
 
 	private void setupData() {
 		if (mPageType == Utils.USER_ISSUES_TYPE) {
 			SubscriptionAbstractCache cache = getSubscriptionAbstractCache();
 			mUserIssueList = cache.getAbstracts().getList();
 		} else {
 			mCacheList = mApp.getTempCache();
 			if (mPageType == Utils.IMPORT_NOTIFY_TYPE) {
 				mCategoryTitle = getString(R.string.default_notice_title);
 			}
 		}
 
 		SwitchAssistent assistent = new SwitchAssistent();
 		mSwitcher.setAssistant(assistent, mCurrentId);
 	}
 
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent event){
 		// mGd.onTouchEvent(event);
 		int y = (int) event.getRawY();
 		int x = (int) event.getRawX();
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			mInitX = x;
 			mInitY = y;
 			mDirection = -1;
 			mbSwitchAble = true;
 			mIsDisplayTop = false;
 			View content = mSwitcher.getCurrentView();
 			if (content != null && content instanceof RelativeLayout
 					&& ((RelativeLayout) content).getChildCount() > 0) {
 				View child = ((RelativeLayout) mSwitcher.getCurrentView())
 						.getChildAt(0);
 				if (child instanceof FileView) {
 					mIsDisplayTop = ((FileView)child).isDisplayTop();
 				} else {
 					ScrollView scroll = (ScrollView)child.findViewById(R.id.scroll_layout);
 					if (scroll != null) {
 						if (scroll.getScrollY() <= 0) {
 							mIsDisplayTop = true;
 						}
 					}
 				}
 			}
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if (mbSwitchAble) {
 				if (Math.abs(mBaseX - x) > mTouchSlop
 						&& Math.abs(mBaseX - x) > Math.abs(mBaseY - y)) {
 					if (mInitX > x) {
 						mDirection = 1;
 					} else {
 						mDirection = 0;
 					}
 
 					int lastIndex = mSwitcher.getCurrentIndex();
 					mSwitcher.SwitcherOnScroll(mDirection);
 					mLastIndex = lastIndex;
 					Utils.logd("FileActivity", "switch scroll " + mDirection);
 					mbSwitchAble = false;
 					break;
 				}
 			}
 			if (mIsDisplayTop && y - mBaseY > mTouchSlop
 					&& Math.abs(mInitX - x) < Math.abs(mInitY - y)) {
 				onBackPressed();
 				mbSwitchAble = false;
 			}
 			break;
 		case MotionEvent.ACTION_UP:
 
 			break;
 		}
 		mBaseX = x;
 		mBaseY = y;
 		if (!mbSwitchAble || Math.abs(mBaseX - x) > Math.abs(mBaseY - y)) {
 			return true;// super.dispatchTouchEvent(event);
 		} else {
 			return super.dispatchTouchEvent(event);
 		}
 	
 		
 	}
 //	public boolean dispatchTouchEvent(MotionEvent event) {
 //		// mGd.onTouchEvent(event);
 //		int y = (int) event.getRawY();
 //		int x = (int) event.getRawX();
 //		switch (event.getAction()) {
 //		case MotionEvent.ACTION_DOWN:
 //			mInitX = x;
 //			mInitY = y;
 //			mDirection = -1;
 //			mbSwitchAble = true;
 //			break;
 //		case MotionEvent.ACTION_MOVE:
 //			if (mbSwitchAble) {
 //				if (Math.abs(mBaseX - x) > mTouchSlop
 //						&& Math.abs(mBaseX - x) > Math.abs(mBaseY - y)) {
 //					if (mInitX > x) {
 //						mDirection = 1;
 //					} else {
 //						mDirection = 0;
 //					}
 //
 //					int lastIndex = mSwitcher.getCurrentIndex();
 //					mSwitcher.SwitcherOnScroll(mDirection);
 //					mLastIndex = lastIndex;
 //					Utils.logd("FileActivity", "switch scroll " + mDirection);
 //					mbSwitchAble = false;
 //					break;
 //				}
 //			}
 //			break;
 //		case MotionEvent.ACTION_UP:
 //
 //			break;
 //		}
 //		mBaseX = x;
 //		mBaseY = y;
 //		if (!mbSwitchAble || Math.abs(mBaseX - x) > Math.abs(mBaseY - y)) {
 //			return true;// super.dispatchTouchEvent(event);
 //		} else {
 //			return super.dispatchTouchEvent(event);
 //		}
 //	}
 
 	private SubscriptionArticle loadUserIssue(int page) {
 		SubscriptionArticle userIssue = SubscriptionApi.getSubscriptionArticle(
 				this, mApp.getCurrentUserId(), mUserIssueList.get(page));
 		if (userIssue != null && userIssue.isSuccess()) {
 			mHtmlCotent = userIssue.getContent();
 			mTitle = userIssue.getTitle();
 			AddTitleToHtml();
 		}
 		return userIssue;
 	}
 
 	private RecommendedTopicIntro loadRecommendPage(int page) {
 		RecommendedTopicIntro recommend = RecommendApi
 				.getRecommendedTopicIntro(this, mApp.getCurrentUserId(),
 						mCacheList.get(page).mId);
 		if (recommend != null && recommend.isSuccess()) {
 			mHtmlCotent = recommend.getContent();
 			mTitle = recommend.getTitle();
 			AddTitleToHtml();
 		}
 		return recommend;
 	}
 
 	private NewsNoticeArticle loadNoticePage(int page) {
 		NewsNoticeArticle notice = NewsNoticeApi.getNewsNoticeArticle(this,
 				mApp.getCurrentUserId(), mCacheList.get(page).mId);
 		if (notice != null && notice.isSuccess()) {
 			mHtmlCotent = notice.getContent();
 			mTitle = notice.getTitle();
 			mNeedFeedback = notice.isFeedbackRequired();
 			if (mNeedFeedback) {
 				mNoticeId = notice.getId();
 			}
 			AddTitleToHtml();
 		}
 		return notice;
 	}
 
 	private NewsColumnistInfo loadExpertPage(int page) {
 		NewsColumnistInfo expert = NewsColumnistApi.getNewsColumnistInfo(this,
 				mApp.getCurrentUserId(), mCacheList.get(page).mId);
 		if (expert != null && expert.isSuccess()) {
 			mTitle = expert.getName();
 			mSummary = expert.getSummary();
 			mResume = expert.getResume();
 			mIconUrl = expert.getPortraitUrl();
 	
 			boolean bGetIcon = false;
 			for (int i = 0; i < mIconList.size(); i++) {
 				IconData data = mIconList.get(i);
 				if (data.mPage == page) {
 					if (data.mBmp == null || data.mBmp.isRecycled()) {
 						mIconList.remove(data);
 						mLoadBitmapTask = new LoadBitmapTask();
 						mLoadBitmapTask.execute(data);
 	
 					}
 					bGetIcon = true;
 					break;
 				}
 			}
 			if (!bGetIcon) {
 				IconData data = new IconData();
 				data.mPage = page;
 				data.mIconUrl = mIconUrl;
 	
 				mLoadBitmapTask = new LoadBitmapTask();
 				mLoadBitmapTask.execute(data);
 			}
 		}
 		return expert;
 	}
 
 	private class LoadDataTask extends NewsResultAsyncTask<Integer, Void, NewsResult> {
 		public LoadDataTask(Context context) {
 			super(context);
 			// TODO Auto-generated constructor stub
 		}
 
 		int mIndex = -1;
 		@Override
 		protected NewsResult doInBackground(Integer... params) {
 			mIndex = params[0];
 			if (mPageType == Utils.USER_ISSUES_TYPE) {
 				if (mIndex < 0 || mIndex >= mUserIssueList.size()) {
 					return null;
 				}
 			} else if (mIndex < 0 || mIndex >= mCacheList.size()) {
 				return null;
 			}
 			NewsResult result = null;
 			mNeedFeedback = false;
 			mNoticeId = null;
 			switch (mPageType) {
 			case Utils.NOTIFY_LIST_TYPE:
 			case Utils.IMPORT_NOTIFY_TYPE:
 				result = loadNoticePage(mIndex);
 				break;
 			case Utils.EXPERT_LIST_TYPE:
 			case Utils.IMPORT_EXPERT_TYPE:
 				result = loadExpertPage(mIndex);
 				break;
 			case Utils.RECOMMEND_LIST_TYPE:
 				result = loadRecommendPage(mIndex);
 				break;
 			case Utils.USER_ISSUES_TYPE:
 				result = loadUserIssue(mIndex);
 				break;
 
 			}
 			return result;
 		}
 
 		@Override
 		public void onPostExecute(NewsResult result) {
 			super.onPostExecute(result);
 			if (result != null && result.isSuccess() && mIndex >= 0) {
 				mCurrentShowId = mIndex;
 				Utils.log("LoadDataTask", "update switch");
 
 				switch (mPageType) {
 				case Utils.NOTIFY_LIST_TYPE:
 				case Utils.RECOMMEND_LIST_TYPE:
 				case Utils.IMPORT_NOTIFY_TYPE:
 				case Utils.USER_ISSUES_TYPE:
 					SwitchAssistent assistent = new SwitchAssistent();
 					mSwitcher.setAssistant(assistent);
 					break;
 				case Utils.EXPERT_LIST_TYPE:
 				case Utils.IMPORT_EXPERT_TYPE:
 					ExpertAssistent expertAssistent = new ExpertAssistent();
 					mSwitcher.setAssistant(expertAssistent);
 					break;
 
 				}
 
 			} else {
 
 			}
 			mTask = null;
 		}
 
 	}
 	
 	private class UserIssueTask extends NewsResultAsyncTask<Integer, Void, NewsResult> {
 		String mPageId = null;
 		public UserIssueTask(Context context) {
 			super(context);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		protected NewsResult doInBackground(Integer... params) {
 			int page = params[0];
 			mPageId = mCacheList.get(page).mId;
 			NewsResult result = RecommendApi.subscribeRecommendedTopic(PageActivity.this, mApp.getCurrentUserId(),
 					mPageId);
 			return result;
 		}
 		
 		@Override
 		public void onPostExecute(NewsResult result) {
 			super.onPostExecute(result);
 			if (result != null && result.isSuccess()) {
				Intent intent = new Intent();
				intent.putExtra(PAGE_ID, mPageId);
				setResult(Activity.RESULT_OK, intent);
				finish();
 			}
 		}
 		
 	}
 
 	final static private int MAX_DATA_SIZE = 5;
 	private ArrayList<IconData> mIconList = new ArrayList<IconData>();
 	private LoadBitmapTask mLoadBitmapTask = null;
 
 	private class IconData {
 		int mPage;
 		String mIconUrl = null;
 		Bitmap mBmp = null;
 	}
 
 	private class LoadBitmapTask extends AsyncTask<IconData, Void, Void> {
 
 		@Override
 		protected Void doInBackground(IconData... params) {
 			IconData iconData = params[0];
 			if (iconData.mBmp == null || !iconData.mBmp.isRecycled()) {
 				iconData.mBmp = Utils.getBitmapFromUrl(iconData.mIconUrl);
 				mIconList.add(iconData);
 			}
 
 			if (mIconList.size() > MAX_DATA_SIZE) {
 				int position = mSwitcher.getCurrentIndex();
 				for (int i = 0; i < mIconList.size(); i++) {
 					IconData temp = mIconList.get(i);
 					if (temp != null
 							&& Math.abs(temp.mPage - position) > MAX_DATA_SIZE / 2) {
 						if (temp.mBmp != null && !temp.mBmp.isRecycled()) {
 							temp.mBmp.recycle();
 						}
 						mIconList.remove(i);
 						i--;
 					}
 				}
 			}
 			return null;
 		}
 
 		// @Override
 		// protected void onProgressUpdate(Void... values) {
 		// ExpertAssistent assistent = new ExpertAssistent();
 		// mSwitcher.setAssistant(assistent);
 		//
 		// Utils.logd("LoadBitmapTask", "update icon ui");
 		// super.onProgressUpdate(values);
 		// }
 
 		@Override
 		protected void onPostExecute(Void result) {
 
 			ExpertAssistent assistent = new ExpertAssistent();
 			mSwitcher.setAssistant(assistent);
 			mLoadBitmapTask = null;
 			super.onPostExecute(result);
 		}
 
 	}
 
 	private void AddTitleToHtml() {
 		if (mTitle == null) {
 			return;
 		}
 		XmlSerializer serializer = Xml.newSerializer();
 		StringWriter writer = new StringWriter();
 		String xmlString = null;
 
 		try {
 			serializer.setOutput(writer);
 			serializer.startTag("", "h2");
 			serializer.text(mTitle);
 			serializer.endTag("", "h2");
 
 			serializer.startTag("", "hr");
 			serializer.endTag("", "hr");
 			serializer.flush();
 			xmlString = writer.toString();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		mHtmlCotent = xmlString + mHtmlCotent;
 	}
 	
 	private void userIssue(int position) {
 		new UserIssueTask(this).execute(position);
 	}
 
 	private class ExpertAssistent extends BaseAssistent {
 
 		@Override
 		public int getCount() {
 			return mCacheList.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public View getView(int position, View convertView) {
 		   if (convertView instanceof ProgressBar) {
             convertView = null;
          }
 			mCurrentId = position;
 			Utils.log("getView", "mLastIndex=" + mLastIndex + " mCurrentId="
 					+ position + " convertView=" + convertView);
 			if (mLastIndex == position && convertView != null) {
 				Utils.log("getView", " last data");
 				ScrollView scrollview = (ScrollView) convertView
 						.findViewById(R.id.scroll_layout);
 				scrollview.scrollTo(scrollview.getScrollX(), 0);
 				return convertView;
 			} else if (mCurrentId == mCurrentShowId) {
 				View fileview = convertView;
 				if (fileview == null) {
 					fileview = mInflater.inflate(R.layout.expert_page_view,
 							null);
 				}
 //				TextView title = (TextView) fileview.findViewById(R.id.title);
 				ImageView iv = (ImageView) fileview.findViewById(R.id.image);
 				TextView summary = (TextView) fileview
 						.findViewById(R.id.summary);
 				summary.setTextSize(mTextSize-2);
 				summary.setLineSpacing(0, 1.6f);
 				TextView resume = (TextView) fileview.findViewById(R.id.resume);
 				resume.setTextSize(mTextSize);
 				resume.setLineSpacing(0, 1.6f);
 
 				for (int i = 0; i < mIconList.size(); i++) {
 					IconData data = mIconList.get(i);
 					if (data.mPage == position) {
 						if (data.mBmp != null && !data.mBmp.isRecycled()) {
 							iv.setImageBitmap(data.mBmp);
 						}
 					}
 				}
 //				if (mCategoryTitle != null) {
 //					title.setVisibility(View.VISIBLE);
 //					title.setText(mCategoryTitle);
 //				} else {
 //					title.setVisibility(View.GONE);
 //				}
 				if (mSummary != null) {
 				   summary.setText(Html.fromHtml(mSummary));//(mSummary);
 				}
 				if (mResume != null) {
 				   resume.setText(Html.fromHtml(mResume));//(mResume);
 				}
 				Utils.log("getView", " real data");
 				return fileview;
 			} else {
 				if (mTask != null) {
 					mTask.cancel(true);
 					mTask = null;
 				}
 				mTask = new LoadDataTask(PageActivity.this);
 				mTask.execute(position);
 				Utils.log("getView", " no data");
 				return createPogress();
 			}
 
 		}
 
 	}
 
 	private class SwitchAssistent extends BaseAssistent {
 
 		@Override
 		public int getCount() {
 			if (mPageType == Utils.USER_ISSUES_TYPE) {
 				return mUserIssueList.size();
 			} else {
 				return mCacheList.size();
 			}
 		}
 
 		@Override
 		public Object getItem(int position) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public View getView(final int position, View convertView) {
 		   if (convertView instanceof ProgressBar) {
             convertView = null;
          }
 
 			mCurrentId = position;
 			Utils.log("getView", "mLastIndex=" + mLastIndex + " mCurrentId="
 					+ position + " convertView=" + convertView);
 			if (mLastIndex == position && convertView != null) {
 				Utils.log("getView", " last data");
 				((FileView) convertView).displayTop();
 				return convertView;
 			} else if (mCurrentId == mCurrentShowId) {
 				FileView fileview = (FileView) convertView;
 				
 				if (fileview == null) {
 					fileview = new FileView(PageActivity.this);
 				}
 				View btn = fileview.findViewById(R.id.btn);
 				btn.setOnClickListener(new View.OnClickListener() {
 					
 					public void onClick(View v) {
 						userIssue(position);
 					}
 				});
 				if (mPageType == Utils.RECOMMEND_LIST_TYPE) {
 					btn.setVisibility(View.VISIBLE);
 				} else {
 					btn.setVisibility(View.GONE);
 				}
 				
 				TextView title = (TextView) fileview.findViewById(R.id.title);
 				if (title != null && mCategoryTitle != null) {
 					title.setText(mCategoryTitle);
 				}
 				fileview.setData(mPageType, mHtmlCotent, mTextSize, mNoticeId, mIsWIFI);
 				Utils.log("getView", " real data");
 				
 				return fileview;
 			} else {
 				if (mTask != null) {
 					mTask.cancel(true);
 					mTask = null;
 				}
 				mTask = new LoadDataTask(PageActivity.this);
 				mTask.execute(position);
 				Utils.log("getView", " no data");
 				return createPogress();
 			}
 
 		}
 	}
 
 	private int mLastIndex = -1;
 	
 	private View createPogress() {
       ProgressBar bar = new ProgressBar(this);
       bar.setMax(100);
       bar.setProgress(50);
 
       return bar;
 
    }
 
 }
