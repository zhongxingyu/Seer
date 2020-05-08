 package com.osastudio.newshub;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.huadi.azker_phone.R;
 import com.osastudio.newshub.data.NewsAbstract;
 import com.osastudio.newshub.data.NewsAbstractList;
 import com.osastudio.newshub.data.NewsResult;
 import com.osastudio.newshub.data.SubscriptionAbstractList;
 import com.osastudio.newshub.data.base.NewsBaseAbstract;
 import com.osastudio.newshub.net.NewsAbstractApi;
 import com.osastudio.newshub.net.SubscriptionApi;
 import com.osastudio.newshub.utils.NewsResultAsyncTask;
 import com.osastudio.newshub.utils.Utils;
 import com.osastudio.newshub.widgets.BaseAssistent;
 import com.osastudio.newshub.widgets.SlideSwitcher;
 import com.osastudio.newshub.widgets.SummaryGrid;
 import com.osastudio.newshub.widgets.SummaryGrid.OnGridItemClickListener;
 
 import android.R.color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.widget.TextView;
 
 /**
  * The Activity to display lesson list,
  * lessons list, user issue lessons list 
  * and daily reminder lessons list all user this activity
  * 
  * @author pengyue
  *
  */
 public class SummaryActivity extends NewsBaseActivity {
 	public static final String CHANNEL_TYPE = "Channel_type";
 	public static final String CHANNEL_ID = "Channel_id";
 	public static final String CHANNEL_TITLE = "Channel_title";
 	public static final String TITLE_TYPE = "title_type";
 	
 
 	private NewsApp mApp = null;
 	private int mChannelType;
 	private String mChannelId = null;
 	private String mChannelTitle = null;
 	private String mChannelDsip = null;
 	private int mTitleType = 1;
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
 			mTitleType = extras.getInt(TITLE_TYPE);
 		}
 
 		ViewConfiguration configuration = ViewConfiguration.get(this);
 		mTouchSlop = configuration.getScaledTouchSlop();
 		mSwitcher = (SlideSwitcher) findViewById(R.id.switcher);
 		setupData();
 	}
 
 	private void setupData() {
 		mDlg = Utils.showProgressDlg(this, null);
 		mLoadDataTask = new LoadDataTask(this);
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
 //					Utils.log("FileActivity", "switch scroll " + mDirection);
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
 
 
 	private class LoadDataTask extends NewsResultAsyncTask<Void, Void, NewsResult> {
 
 		public LoadDataTask(Context context) {
 			super(context);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		protected NewsResult doInBackground(Void... params) {
 			NewsResult result = null;
 			switch (mChannelType) {
 			case Utils.USER_ISSUES_TYPE:
 			   if (mTitleType == 1) {
 					result = SubscriptionApi
 							.getSubscriptionAbstractList(getApplicationContext(),
 									mApp.getCurrentUserId(), mChannelId);
 			   } else if (mTitleType == 2) {
 					result = NewsAbstractApi.getNewsAbstractList(
 							getApplicationContext(), mApp.getCurrentUserId(), mChannelId);
 			   }
 				
 				break;
 			case Utils.LESSON_LIST_TYPE:
 			case Utils.DAILY_REMINDER_TYPE:
 				result = NewsAbstractApi.getNewsAbstractList(
 						getApplicationContext(), mApp.getCurrentUserId(), mChannelId);
 				
 				break;
 			}
 			return result;
 		}
 
 		@Override
 		public void onPostExecute(NewsResult result) {
 			super.onPostExecute(result);
 
 			mLoadDataTask = null;
 			if (mDlg != null) {
 				Utils.closeProgressDlg(mDlg);
 				mDlg = null;
 			}
 
 			if(result != null && result.isSuccess()) {
 				if (mChannelType == Utils.USER_ISSUES_TYPE && mTitleType == 1) {
 					SubscriptionAbstractList userIssueList = (SubscriptionAbstractList)result;
 					mSummaries = userIssueList.asNewsBaseAbstractList();
 					mChannelTitle = userIssueList.getChannelName();
 					mChannelDsip = userIssueList.getChannelDescription();
 				} else if ((mChannelType == Utils.USER_ISSUES_TYPE && mTitleType == 2)
 				      || mChannelType == Utils.LESSON_LIST_TYPE 
 				      || mChannelType == Utils.DAILY_REMINDER_TYPE) {
 					NewsAbstractList summary_list = (NewsAbstractList)result;
 					mSummaries = summary_list.asNewsBaseAbstractList();
 					mChannelTitle = summary_list.getChannelName();
 					mChannelDsip = summary_list.getChannelDescription();
 				}
 				if (mSummaries != null && mSummaries.size() > 0) {
 					if (mSummaries.size() % 4 == 0) {
 						mTotalPage = mSummaries.size() / 4;
 					} else {
 						mTotalPage = mSummaries.size() / 4 + 1;
 					}
 				}
 				switch (mChannelType) {
 				case Utils.USER_ISSUES_TYPE:
 				case Utils.LESSON_LIST_TYPE:
 				case Utils.DAILY_REMINDER_TYPE:
 					SwitchAssistent assistent = new SwitchAssistent();
 					mSwitcher.setAssistant(assistent);
 					break;
 				}
 			}
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
 		if (mChannelType == Utils.USER_ISSUES_TYPE && mTitleType == 1) {
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
    
 	private void startExamActivity(NewsAbstract abs) {
       Intent intent = new Intent(this, ExamActivity.class);
       intent.putExtra(ExamActivity.EXTRA_EXAM_ID, abs.getId());
       intent.putExtra(ExamActivity.EXTRA_EXAM_TITLE, abs.getTitle());
       startActivity(intent);
 	}
 
 	private class SummaryItemClickListener implements OnGridItemClickListener {
 		@Override
 		public void onClick(int position, View v) {
 			int page = mSwitcher.getCurrentIndex();
 			int index = page * 4 + position-1;
 			if (index < mSummaries.size()) {
             NewsBaseAbstract baseAbstract = mSummaries.get(index);
             if (mChannelType == Utils.LESSON_LIST_TYPE
                  || mChannelType == Utils.DAILY_REMINDER_TYPE
                  || mChannelType == Utils.USER_ISSUES_TYPE) {
                if (baseAbstract instanceof NewsAbstract) {
                   NewsAbstract abs = (NewsAbstract) baseAbstract;
                   if (abs.getType() == NewsAbstract.EXAM_TYPE) {
                      startExamActivity(abs);
                      return;
                   }
                }
             }
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
 				if (mChannelTitle != null) {
 				   tv.setText(Html.fromHtml(mChannelTitle));
 				} else {
 				   tv.setText(null);
 				}
 				tv.setShadowLayer(2, 2, 2, Utils.COLOT_TEXT_GRAY);
 				sub.setTextColor(Color.WHITE);
 				if (mChannelDsip != null) {
 				   sub.setText(Html.fromHtml(mChannelDsip));
 				} else {
 				   sub.setText(null);
 				}
 				sub.setShadowLayer(1, 1, 1, Utils.COLOT_TEXT_GRAY);
 			} else {
 				int index = mPage * 4 + position-1;
 				if (index < mSummaries.size()) {
 					
 					NewsBaseAbstract data = mSummaries.get(index);
 					tv.setTextColor(Color.BLACK);
 					tv.setTextSize(16);
 					if (data.getTitle() != null) {
 					   tv.setText(Html.fromHtml(data.getTitle()));
 					}
 					sub.setTextColor(SummaryActivity.this.getResources().getColor(R.color.text_gray));
 					if (data.getAuthor() != null) {
 					   sub.setText(Html.fromHtml(data.getAuthor()));
 					}
 					summary.setBackgroundColor(color.transparent);
 					
 				} else {
 					return null;
 				}
 			}
 			return summary;
 		}
 
 	}
 
 }
