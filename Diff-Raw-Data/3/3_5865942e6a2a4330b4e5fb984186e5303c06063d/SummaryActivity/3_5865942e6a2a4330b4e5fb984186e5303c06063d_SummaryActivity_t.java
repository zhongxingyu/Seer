 package com.osastudio.newshub;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.huadi.azker_phone.R;
 import com.osastudio.newshub.data.NewsAbstractList;
 import com.osastudio.newshub.data.SubscriptionAbstractList;
 import com.osastudio.newshub.data.base.NewsBaseAbstract;
 import com.osastudio.newshub.net.NewsAbstractApi;
 import com.osastudio.newshub.net.SubscriptionApi;
 import com.osastudio.newshub.utils.Utils;
 import com.osastudio.newshub.widgets.BaseAssistent;
 import com.osastudio.newshub.widgets.SlideSwitcher;
 import com.osastudio.newshub.widgets.SummaryGrid;
 import com.osastudio.newshub.widgets.SummaryGrid.OnGridItemClickListener;
 
 import android.R.color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Color;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.widget.TextView;
 
 public class SummaryActivity extends NewsBaseActivity {
 	public static final String CHANNEL_TYPE = "Channel_type";
 	public static final String CHANNEL_ID = "Channel_id";
 	public static final String CHANNEL_TITLE = "Channel_title";
 
 	private NewsApp mApp = null;
 	private int mChannelType;
 	private String mChannelId = null;
 	private String mChannelTitle = null;
 	private String mChannelDsip = null;
 	private SlideSwitcher mSwitcher = null;
 	private List<NewsBaseAbstract> mSummaries = new ArrayList<NewsBaseAbstract>();
 	private int mTotalPage = 0;
 	private int mTouchSlop;
 	private int mDirection = -1; // 0 is preview; 1 is next;
 	private int mInitX, mInitY;
 	private int mBaseX, mBaseY;
 	private boolean mbSwitchAble = true;
 	
 	private LoadDataTask mLoadDataTask = null;
 	private ProgressDialog mDlg = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_switcher);
 		mApp = (NewsApp) getApplication();
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			mChannelType = extras.getInt(CHANNEL_TYPE);
 			mChannelId = extras.getString(CHANNEL_ID);
 			mChannelTitle = extras.getString(CHANNEL_TITLE);
 		}
 
 		ViewConfiguration configuration = ViewConfiguration.get(this);
 		mTouchSlop = configuration.getScaledTouchSlop();
 		mSwitcher = (SlideSwitcher) findViewById(R.id.switcher);
 		setupData();
 	}
 
 	private void setupData() {
 		mDlg = Utils.showProgressDlg(this, null);
 		mLoadDataTask = new LoadDataTask();
 		mLoadDataTask.execute();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		if (mDlg != null) {
 			Utils.closeProgressDlg(mDlg);
 			mDlg = null;
 		}
 		if (mLoadDataTask != null) {
 			mLoadDataTask.cancel(true);
 			mLoadDataTask = null;
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent event) {
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
 					Utils.logd("FileActivity", "switch scroll " + mDirection);
 					mbSwitchAble = false;
 					break;
 				}
 			}
 			if (y - mBaseY > mTouchSlop
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
 			return true;//super.dispatchTouchEvent(event);
 		} else {
 			return super.dispatchTouchEvent(event);
 		}
 	}
 
 
 	private class LoadDataTask extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			NewsAbstractList summary_list = null;
 			switch (mChannelType) {
 			case Utils.USER_ISSUES_TYPE:
 				SubscriptionAbstractList userIssueList = SubscriptionApi
 						.getSubscriptionAbstractList(getApplicationContext(),
 								mApp.getCurrentUserId(), mChannelId);
 				if (userIssueList != null) {
 					mSummaries = userIssueList.asNewsBaseAbstractList();
               mChannelTitle = userIssueList.getChannelName();
               mChannelDsip = userIssueList.getChannelDescription();
 				}
 				break;
 			case Utils.LESSON_LIST_TYPE:
 			case Utils.DAILY_REMINDER_TYPE:
 				summary_list = NewsAbstractApi.getNewsAbstractList(
 						getApplicationContext(), mChannelId);
 				if (summary_list != null) {
 					mSummaries = summary_list.asNewsBaseAbstractList();
 					mChannelTitle = summary_list.getChannelName();
 					mChannelDsip = summary_list.getChannelDescription();
 				}
 				break;
 //			case Utils.DAILY_REMINDER_TYPE:
 //				break;
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if (mDlg != null) {
 				Utils.closeProgressDlg(mDlg);
 				mDlg = null;
 			}
 			if (mSummaries != null && mSummaries.size() > 0) {
 				if (mSummaries.size() % 4 == 0) {
 					mTotalPage = mSummaries.size() / 4;
 				} else {
 					mTotalPage = mSummaries.size() / 4 + 1;
 				}
 			}
 			mLoadDataTask = null;
 			switch (mChannelType) {
 			case Utils.USER_ISSUES_TYPE:
 			case Utils.LESSON_LIST_TYPE:
 			case Utils.DAILY_REMINDER_TYPE:
 				SwitchAssistent assistent = new SwitchAssistent();
 				mSwitcher.setAssistant(assistent);
 				break;
 //				break;
 			}
 			super.onPostExecute(result);
 		}
 		
 		@Override
 		protected void onCancelled() {
 			if (mDlg != null) {
 				Utils.closeProgressDlg(mDlg);
 				mDlg = null;
 			}
 			mLoadDataTask = null;
 			super.onCancelled();
 		}
 
 	}
 
 	private void setupGridLayout(SummaryGrid grid_layout, int page) {
 //		TextView title = (TextView)grid_layout.findViewById(R.id.title_text);
 //		title.setText(mChannelTitle);
 
 		TextView pageTv = (TextView)grid_layout.findViewById(R.id.page);
 		pageTv.setText(String.valueOf(page+1)+"/"+String.valueOf(mTotalPage));
 		grid_layout.setAssistant(new GridLayoutAssistent(page));
 	}
 
 	private void startFileActivity(int index) {
 		if (mChannelType == Utils.USER_ISSUES_TYPE) {
 			Intent it = new Intent(this, PageActivity.class);
 			it.putExtra(PageActivity.PAGE_TYPE, mChannelType);
 			it.putExtra(PageActivity.START_INDEX, index);
 			it.putExtra(PageActivity.CATEGORY_TITLE, mChannelTitle);
 			startActivity(it);
 		} else {
 			Intent it = new Intent(this, FileActivity.class);
 			it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			it.putExtra(FileActivity.START_INDEX, index);
 			it.putExtra(FileActivity.CATEGORY_TITLE, mChannelTitle);
 			startActivityForResult(it, 1);
 		}
 	}
 
 	private class SummaryItemClickListener implements OnGridItemClickListener {
 		@Override
 		public void onClick(int position, View v) {
 			int page = mSwitcher.getCurrentIndex();
 			int index = page * 4 + position-1;
 			if (index < mSummaries.size()) {
 				startFileActivity(index);
 			}
 
 		}
 
 	}
 
 	private class SwitchAssistent extends BaseAssistent {
 
 		@Override
 		public int getCount() {
 //			if (mSummaries.size() % 4 == 0) {
 //				return mSummaries.size() / 4;
 //			} else {
 //				return mSummaries.size() / 4 + 1;
 //			}
 			return mTotalPage;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public View getView(int position, View convertView) {
 			SummaryGrid grid_layout = (SummaryGrid) convertView;
 			if (grid_layout == null) {
 				grid_layout = new SummaryGrid(SummaryActivity.this);
 			}
 			setupGridLayout(grid_layout, position);
 			grid_layout
 					.setGridItemClickListener(new SummaryItemClickListener());
 			return grid_layout;
 
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
 			if ((mPage + 1) * 4 <= mSummaries.size()) {
 				count = 4;
 			} else {
 				count = 4 - ((mPage + 1) * 4 - mSummaries.size());
 			}
 			return count;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			int index = mPage * 4 + position;
 			if (index < mSummaries.size()) {
 				return mSummaries.get(index);
 			} else {
 				return null;
 			}
 		}
 
 		@Override
 		public View getView(int position, View convertView) {
 			TextView tv = null;
 			TextView sub = null;
 			View summary = convertView;
 			if (summary == null) {
 				LayoutInflater inflater = LayoutInflater
 						.from(SummaryActivity.this);
 				summary = inflater.inflate(R.layout.summary_item, null);
 			}
 			tv = (TextView) summary.findViewById(R.id.title);
 			sub = (TextView) summary
 					.findViewById(R.id.expert_name);
 			if (position == 0) {
 				tv.setTextColor(Color.WHITE);
 				tv.setTextSize(24);
 				tv.setText(Html.fromHtml(mChannelTitle));
 				
 				sub.setTextColor(Color.WHITE);
 				sub.setText(Html.fromHtml(mChannelDsip));
 				summary.setBackgroundColor(color.transparent);
 			} else {
 				int index = mPage * 4 + position-1;
 				if (index < mSummaries.size()) {
 					
 					NewsBaseAbstract data = mSummaries.get(index);
 					tv.setTextColor(Color.BLACK);
 					tv.setTextSize(16);
 					tv.setText(Html.fromHtml(data.getTitle()));
 
 					sub.setTextColor(SummaryActivity.this.getResources().getColor(R.color.text_gray));
 					sub.setText(Html.fromHtml(data.getAuthor()));
 					summary.setBackgroundColor(color.transparent);
 					
 				} else {
 					return null;
 				}
 			}
 			return summary;
 		}
 
 	}
 
 }
