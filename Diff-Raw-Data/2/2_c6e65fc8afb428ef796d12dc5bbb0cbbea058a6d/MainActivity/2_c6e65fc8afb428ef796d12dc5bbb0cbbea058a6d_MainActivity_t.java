 package com.shj00007.activity;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.support.v4.widget.CursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.text.Html;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.shj00007.R;
 import com.shj00007.activity.base.BaseActivity;
 import com.shj00007.adapter.ExpandableAdapter;
 import com.shj00007.adapter.MidListViewAdapter;
 import com.shj00007.business.BusinessRss;
 import com.shj00007.cache.AsyncImageGetter;
 import com.shj00007.cache.ImageCache;
 import com.shj00007.touch.ctrl.GestureListenerImpl;
 import com.shj00007.utility.DownFile;
 
 public class MainActivity extends BaseActivity implements OnTouchListener {
 	AsyncImageGetter mAsyncImageGetter = null;
 	private LinearLayout layout = null;
 
 	private GestureDetector gestureDetector = null;
 
 	private BusinessRss mBusinessRss = null;
 	private ExpandableListView mExpandableListView = null;
 	private ExpandableAdapter mExpandableAdapter = null;
 
 	private SimpleCursorAdapter mSimpleCursorAdapter = null;
 
 	private ListView mMidListView = null;
 	private ImageView mMidUnreadImage = null;
 	private ImageView mRightUnreadImage = null;
 	private TextView tvrighttext = null;
 	private ScrollView svrightscroll = null;
 
 	private ListView mMid_starr_listview = null;
 
 	private ImageView mSetAllRead = null;
 	private ImageView mUnread = null;
 	private ImageView mAddFeed = null;
 	private ImageView mViewStarr = null;
 	private ImageView mSetStarr = null;
 	private ImageView mUpdateRss = null;
 
 	DisplayMetrics dm = null;
 	private Cursor _Cursor = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		initView();
 
 		initTools();
 
 		setListener();
 
 		bindData();
 		
 		if(mExpandableAdapter.getGroupCount()==0){
 			addNorRss();
 		}
 
 	}
 
 	public void initTools() {
 		mBusinessRss = new BusinessRss(this);
 		mAsyncImageGetter = new AsyncImageGetter();
 		dm = getResources().getDisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(dm);
 		gestureDetector = new GestureDetector(this, new GestureListenerImpl(
 				this, layout, dm));
 	}
 
 	public void initView() {
 		layout = (LinearLayout) findViewById(R.id.homelayout);
 		mExpandableListView = (ExpandableListView) findViewById(R.id.expandableListleft);
 		mMidListView = (ListView) findViewById(R.id.mid_listview);
 		tvrighttext = (TextView) findViewById(R.id.tvright_text_up);
 		svrightscroll = (ScrollView) findViewById(R.id.svrightscrool);
 		mMidUnreadImage = (ImageView) findViewById(R.id.ivmidunreadimage);
 		mRightUnreadImage = (ImageView) findViewById(R.id.ivrightunreadimage);
 		mSetAllRead = (ImageView) findViewById(R.id.ivsetallread);
 		mUnread = (ImageView) findViewById(R.id.ivonlyunread);
 		mAddFeed = (ImageView) findViewById(R.id.ivaddfeed);
 		mViewStarr = (ImageView) findViewById(R.id.ivviewstarred);
 		mSetStarr = (ImageView) findViewById(R.id.ivsetstarr);
 		mMid_starr_listview = (ListView) findViewById(R.id.mid_starr_listview);
 		mUpdateRss = (ImageView) findViewById(R.id.ivupdate);
 	}
 
 	public void setListener() {
 		mExpandableListView.setOnTouchListener(this);
 		mMidListView.setOnTouchListener(this);
 		mMidListView.setOverscrollHeader(getResources().getDrawable(
 				R.drawable.choose_background));
 		tvrighttext.setOnTouchListener(this);
 		svrightscroll.setOnTouchListener(this);
 		mMid_starr_listview.setOnTouchListener(this);
 
 		mSetStarr.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				showToast("请选中Item");
 			}
 		});
 
 		mExpandableListView.setOnChildClickListener(new OnChildClickListener() {
 
 			@Override
 			public boolean onChildClick(ExpandableListView parent, View v,
 					int groupPosition, int childPosition, long id) {
 				// TODO Auto-generated method stub
 				mMid_starr_listview.setVisibility(View.GONE);
 				return false;
 			}
 		});
 
 		mAddFeed.setOnClickListener(new OnClickListener() {
 			private EditText etaddfeedname = null;
 			private AutoCompleteTextView etaddcategoryname = null;
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				View _v = getLayoutInflater().inflate(R.layout.addfeeddialog,
 						null);
 				ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,
 						android.R.layout.simple_dropdown_item_1line,
 						getResources().getStringArray(R.array.category));
 				etaddfeedname = (EditText) _v.findViewById(R.id.etaddfeedname);
 				etaddcategoryname = (AutoCompleteTextView) _v
 						.findViewById(R.id.etaddcategory);
 				etaddcategoryname.setAdapter(adapter);
 
 				new AlertDialog.Builder(MainActivity.this)
 						.setTitle("请输入")
 						.setView(_v)
 						.setPositiveButton("确定",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										// TODO Auto-generated method
 										// stub
 										InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
 										final String link = etaddfeedname
 												.getText().toString();
 										if (etaddfeedname.getText().toString()
 												.equals("")) {
 
 											showToast("请输入地址");
 											return;
 										}
 										if (etaddcategoryname.getText()
 												.toString().equals("")) {
 											showToast("请输入类别");
 											return;
 										}
 										final String category = etaddcategoryname
 												.getText().toString();
 
 										if (mBusinessRss.isRssFeedExist(link)) {
 											showToast("rss已存在");
 											return;
 										}
 
 										imm.hideSoftInputFromWindow(
 												etaddcategoryname
 														.getWindowToken(), 0);
 										showProgressDialog("正在加载rss", "loading");
 
 										new Thread(new Runnable() {
 
 											@Override
 											public void run() {
 
 												if (mBusinessRss
 														.downloadRSS(link)) {
 													mBusinessRss.addRssFeed(
 															category, link);
 													mBusinessRss.updateRss();
 													Handler myhandler = new Handler(
 															Looper.getMainLooper()) {
 
 														@Override
 														public void handleMessage(
 																Message msg) {
 															bindData();
 															dismissProgressDialog();
 														}
 													};
 													myhandler.removeMessages(0);
 													myhandler
 															.sendEmptyMessage(0);
 												} else {
 													Handler handler = new Handler(
 															Looper.getMainLooper()) {
 
 														@Override
 														public void handleMessage(
 																Message msg) {
 															dismissProgressDialog();
 															showToast("网络异常，请检查你的feed地址是否正确");
 														}
 													};
 													handler.removeMessages(0);
 													handler.sendEmptyMessage(0);
 
 												}
 
 											}
 										}).start();
 
 									}
 								}).setNegativeButton("取消", null).show();
 
 			}
 
 		});
 
 		mSetAllRead.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				String _RssName = "";
 				try {
 					_RssName = ((MidListViewAdapter) mMidListView.getAdapter())
 							.getRssName();
 				} catch (Exception e) {
 					_RssName = null;
 				}
 				if (_RssName != null) {
 					showToast("将rss所有条目设置为已阅读");
 					mBusinessRss.setRssIsread(_RssName);
 					mExpandableAdapter.notifyDataSetChanged();
 					((MidListViewAdapter) mMidListView.getAdapter())
 							.notifyDataSetChanged();
 				} else {
 					showToast("请先选中rss");
 				}
 			}
 		});
 
 		mUnread.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 
 				if (mMidListView.getAdapter() == null) {
 					showToast("请选择rss");
 					return;
 				}
 				mMid_starr_listview.setVisibility(View.GONE);
 				mMidListView.setVisibility(View.VISIBLE);
 				MidListViewAdapter _Adapter = (MidListViewAdapter) mMidListView
 						.getAdapter();
 				if (_Adapter.getOnlyViewUnRead()) {
 					_Adapter.setOnlyViewUnRead(false);
 					showToast("显示所有条目");
 				} else {
 					_Adapter.setOnlyViewUnRead(true);
 					showToast("只显示未读条目");
 				}
 				_Adapter.notifyDataSetChanged();
 			}
 		});
 
 		mViewStarr.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				showToast("显示所有星标条目");
 				_Cursor = mBusinessRss.getStarredCursor();
 
 				mSimpleCursorAdapter = new SimpleCursorAdapter(
 						MainActivity.this,
 						R.layout.mid_listview,
 						_Cursor,
 						new String[] { "rssname", "title", "pubdate" },
 						new int[] { R.id.midname, R.id.midtitle, R.id.middate },
 						CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
 				mMid_starr_listview.setAdapter(mSimpleCursorAdapter);
 				mMidUnreadImage.setVisibility(View.GONE);
 				mMidListView.setVisibility(View.GONE);
 				mMid_starr_listview.setVisibility(View.VISIBLE);
 			}
 		});
 
 		mMid_starr_listview
 				.setOnItemClickListener(new OnItemClickListenerImpl());
 		mMidListView.setOnItemClickListener(new OnItemClickListenerImpl());
 
 		mUpdateRss.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				showProgressDialog("正在更新rss", "loading");
 				svrightscroll.scrollTo(0, 0);
 				new Thread(new Runnable() {
 
 					@Override
 					public void run() {
 
 						try {
 							mBusinessRss.updateAllRss();
 						} catch (Exception e) {
 							e.printStackTrace();
 
 							Handler myhandler = new Handler(Looper
 									.getMainLooper()) {
 								@Override
 								public void handleMessage(Message msg) {
 									// TODO Auto-generated method stub
 									showToast("网络异常");
 								}
 							};
 							myhandler.removeMessages(0);
 							myhandler.sendEmptyMessage(0);
 						}
 						Handler myhandler = new Handler(Looper.getMainLooper()) {
 							@Override
 							public void handleMessage(Message msg) {
 								// TODO Auto-generated method stub
 								bindData();
 								dismissProgressDialog();
 							}
 						};
 						myhandler.removeMessages(0);
 						myhandler.sendEmptyMessage(0);
 					}
 				}).start();
 
 			}
 		});
 	}
 
 	private class OnItemClickListenerImpl implements OnItemClickListener {
 
 		String _RssName = "";
 		String _ItemTitle = "";
 		String _Pubdate = "";
 		String _Description = null;
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 				long arg3) {
 			// TODO Auto-generated method stub
 
 			_RssName = ((TextView) arg1.findViewById(R.id.midname)).getText()
 					.toString();
 			_ItemTitle = ((TextView) arg1.findViewById(R.id.midtitle))
 					.getText().toString();
 			_Pubdate = ((TextView) arg1.findViewById(R.id.middate)).getText()
 					.toString();
 
 			_Description = "<h1>" + _ItemTitle + "</h1>\n\n<hr />"
 					+ mBusinessRss.getDescription(_RssName, _ItemTitle);
 
 			tvrighttext.setText(Html.fromHtml(_Description, new DownFile(
 					tvrighttext), null));
 			mRightUnreadImage.setVisibility(View.GONE);
 			svrightscroll.setVisibility(View.VISIBLE);
 			if (!mBusinessRss.isRead(_RssName, _ItemTitle)) {
 
 				mBusinessRss.setHasRead(_RssName, _ItemTitle);
 				mExpandableAdapter.notifyDataSetChanged();
 				((MidListViewAdapter) mMidListView.getAdapter())
 						.notifyDataSetChanged();
 			}
 
 			mSetStarr.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					if (mBusinessRss.isItemStarred(_ItemTitle)) {
 						mBusinessRss.setItemUnstarr(_ItemTitle);
 						showToast("set unstarr");
 					} else {
 						mBusinessRss.setItemStarr(_RssName, _ItemTitle,
 								_Pubdate);
 						showToast("set starr");
 					}
 				}
 			});
 		}
 	}
 
 	public void bindData() {
 		mExpandableAdapter = new ExpandableAdapter(this, mBusinessRss,
 				mMidListView, mMidUnreadImage);
 		mExpandableListView.setAdapter(mExpandableAdapter);
 	}
 	
 	public void addNorRss(){
 		new AlertDialog.Builder(MainActivity.this)
 		.setTitle("rss为空,是否自动加入rss测试")
 		.setPositiveButton("确定",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog,
 							int which) {
 						
 						showProgressDialog("正在加载rss", "loading");
 
 						new Thread(new Runnable() {
 
 							@Override
 							public void run() {
 								String testlinks[]=getResources().getStringArray(R.array.testlinks);
 								Log.i("test", "count="+testlinks.length);
 								String category="";
 								for (int i = 0; i < testlinks.length; i++) {
 									
 									if(i<3){
 										category="资讯1";
 									}else{
 										category="资讯2";
 									}
 									Log.i("test", testlinks[i]);
 									Log.i("test", category);
 									
 									if (mBusinessRss
 											.downloadRSS(testlinks[i])) {
 										mBusinessRss.addRssFeed(
 												category, testlinks[i]);
 										mBusinessRss.updateRss();
 										
 									} else {
 										Handler handler = new Handler(
 												Looper.getMainLooper()) {
 
 											@Override
 											public void handleMessage(
 													Message msg) {
 												dismissProgressDialog();
												showToast("网络错误，添加失败，请链接网络");
 											}
 										};
 										handler.removeMessages(0);
 										handler.sendEmptyMessage(0);
 
 									}
 								}
 								
 								Handler myhandler = new Handler(
 										Looper.getMainLooper()) {
 
 									@Override
 									public void handleMessage(
 											Message msg) {
 										bindData();
 										dismissProgressDialog();
 									}
 								};
 								myhandler.removeMessages(0);
 								myhandler
 										.sendEmptyMessage(0);
 
 							}
 						}).start();
 
 					}
 				}).setNegativeButton("取消", null).show();
 	}
 	
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		// TODO Auto-generated method stub
 		return gestureDetector.onTouchEvent(event);
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		// TODO Auto-generated method stub
 		return gestureDetector.onTouchEvent(event);
 	}
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		if (_Cursor != null) {
 			_Cursor.close();
 		}
 		ImageCache.getInstance().clearCache();
 		mBusinessRss.closeDatabase();
 		super.onDestroy();
 	}
 }
