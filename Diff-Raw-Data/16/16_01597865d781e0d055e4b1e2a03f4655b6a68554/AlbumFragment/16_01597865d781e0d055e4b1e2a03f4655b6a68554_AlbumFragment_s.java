 package com.dabing.emoj.fragment;
 
 import com.dabing.emoj.R;
 import com.dabing.emoj.activity.UserDefineAddActivity;
 import com.dabing.emoj.adpater.AlbumCusorAdapter;
 import com.dabing.emoj.db.UserDefineContentProvider;
 import com.dabing.emoj.db.UserDefineDataBaseHelper;
 import com.dabing.emoj.service.EmojScanService;
 import com.dabing.emoj.utils.AppConstant;
 import com.dabing.emoj.utils.FileInfo;
 import com.dabing.emoj.widget.Album;
 import com.dabing.emoj.widget.Album.AlbumClickListener;
 import com.dabing.emoj.widget.AddImageButton;
 import com.dabing.emoj.widget.AlbumImageView;
 import com.dabing.emoj.widget.CustomGridLayout;
 import com.handmark.pulltorefresh.library.PullToRefreshBase;
 import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.OnPullEventListener;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.ScrollView;
 
 public class AlbumFragment extends UserDefineFragment implements LoaderCallbacks<Cursor>{
	boolean mChanged = false;//cusor是否应该刷新
 	int mWidth = 80;
 	IEmojScanCallBack mCallBack;
 	Messenger client;
 	Messenger mService;
 	boolean mBound = false;
 	String[] PULL_MSG_ARRAY;
 	int PULL_MSG_ARRAY_INDEX = 0;
 	CustomGridLayout gridLayout;
 	PullToRefreshScrollView mScrollView;
 	ImageView mSdcardNoAviaible;
 	AlbumCusorAdapter adapter;
 	UserDefineDataBaseHelper mHelper;
	boolean IsScaned = false;//是否扫描过
 	boolean delMode = false;//删除模式
 	static final int COLUM_NUM = 3;
 	static final int COLUM_PADDING = 0;
 	static final String TAG = AlbumFragment.class.getSimpleName();	
 	static AlbumFragment instance;
 	public static AlbumFragment getInstance(){
		if(instance == null){
			instance = new AlbumFragment();
		}
		return instance;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
 	 */
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onActivityCreated(savedInstanceState);
 		Log.d(TAG, "onActivityCreated");
 		mScrollView = (PullToRefreshScrollView) getView().findViewById(
 				R.id.pull_refresh_scrollview);
 		gridLayout = (CustomGridLayout) getView().findViewById(
 				R.id.gridContainer);
 		gridLayout.setColumnCount(COLUM_NUM);
 		mSdcardNoAviaible = (ImageView) getView().findViewById(R.id.user_define_fragment1_nosdcard);
 		PULL_MSG_ARRAY = getResources().getStringArray(R.array.pull_msg_array);
 		PULL_MSG_ARRAY_INDEX = 0;
 		mScrollView.getLoadingLayoutProxy().setLoadingDrawable(null);
 		mScrollView.getLoadingLayoutProxy().setPullLabel(
 				PULL_MSG_ARRAY[PULL_MSG_ARRAY_INDEX]);
 		mScrollView.getLoadingLayoutProxy().setReleaseLabel(
 				PULL_MSG_ARRAY[PULL_MSG_ARRAY_INDEX]);
 		mScrollView.setOnPullEventListener(pullEventListener);
 		
 		bindGridLayout(null);
 		if(!mChanged){
 			getLoaderManager().initLoader(0, null, this);
 		}else {
 			getLoaderManager().restartLoader(0, null, this);
 			mChanged = false;
 		}
 		//通知父activity初始化完成
 		if(mCallBack != null){
 			mCallBack.onInit(TAG,null);
 		}
 		//扫描图片
 		if(!IsScaned){
 			IsScaned = true;
 			scanFilesDelayed(500);
 		}
 		registerSdcardReceiver();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		client = new Messenger(mHandler);
 		mHelper = new UserDefineDataBaseHelper(getActivity());
 		BindService();
 		calculateAlbumWidth();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
 	 * android.view.ViewGroup, android.os.Bundle)
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		return inflater.inflate(R.layout.user_define_fragment1, container,
 				false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onDestroy()
 	 */
 	@Override
 	public void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		Log.d(TAG, "onDestroy()");
 		getLoaderManager().destroyLoader(0);
 		UnBindService();
 		unregisterSdcardReceiver();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onPause()
 	 */
 	@Override
 	public void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.Fragment#onResume()
 	 */
 	@Override
 	public void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		Log.d(TAG, "onResume");	
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.Fragment#onDestroyView()
 	 */
 	@Override
 	public void onDestroyView() {
 		// TODO Auto-generated method stub
 		super.onDestroyView();
 		Log.d(TAG, "onDestroyView");
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.Fragment#onDetach()
 	 */
 	@Override
 	public void onDetach() {
 		// TODO Auto-generated method stub
 		super.onDetach();
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.Fragment#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		Log.d(TAG, "onActivityResult:"+requestCode + " " + resultCode);
 		if(requestCode == AppConstant.REQUEST_ADD_FILE){
 			if(resultCode == Activity.RESULT_OK){
 				long id = data.getLongExtra(AppConstant.INTENT_USER_DEFINE_ADD_FILE_ID, -1);
 				Log.d(TAG, "onActivityResult id:"+id);
 				getLoaderManager().restartLoader(0, null, this);
 				
 			}
 		}
 	}
 	// *********函数**************
 	
 	protected void refleshGridLayout() {
 		if (adapter != null) {
 			adapter.reflesh();
 		}
 	}
 	
 	protected void bindGridLayout(Cursor cursor){
 		AddImageButton addImageButton = new AddImageButton(getActivity().getParent());
 		addImageButton.setWidth(mWidth);
 		addImageButton.setOnClick(addClickListener);
 		gridLayout.setFirstView(addImageButton);
 		adapter = new AlbumCusorAdapter(getActivity().getParent(), cursor,COLUM_NUM,albumClickListener);
 		gridLayout.setAdapter(adapter);		
 		
 		
 	}
 	
 	// 计算相册宽度
 	private void calculateAlbumWidth() {
 		WindowManager windowManager = (WindowManager) getActivity()
 				.getApplication().getSystemService(Context.WINDOW_SERVICE);
 		int screenWidth = windowManager.getDefaultDisplay().getWidth();
 		mWidth = (screenWidth - (COLUM_NUM + 1) * COLUM_PADDING) / COLUM_NUM;
 		//Log.d(TAG, "width:" + mWidth);
 
 	}
 		
 	/**
 	 * 设置与activity的回调监听
 	 * @param callBack
 	 */
 	public void setCallBack(IEmojScanCallBack callBack){
 		mCallBack = callBack;
 	}
 	
 	public void scanFilesDelayed(long delay){
 		mHandler.postDelayed(new Runnable() {
 			
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				scanfiles();
 			}
 		}, delay);
 	}
 	
 	public void recycle(){
 		gridLayout.removeAllViews();
 	}
 	
 	public void edit(){
 		delMode = !delMode;
 		setDelMode(delMode);
 	}
 	/**
 	 * 获取是否是删除模式
 	 * @return
 	 */
 	public boolean getDelMode(){
 		return delMode;
 	}
 	/**
 	 * 取消删除模式
 	 */
 	public void cancelDelMode(){
 		delMode = false;
 		setDelMode(false);
 	}
 	/**
 	 * 设置相册是否可删除
 	 * @param b
 	 */
 	protected void setDelMode(boolean b){
 		for (int i = 0; i < gridLayout.getChildCount(); i++) {
 			View view = gridLayout.getChildAt(i);
 			if (view instanceof Album) {
 				((Album) view).setDelMode(b);
 			}
 		}
 	}
 	/**
 	 * 检测SD卡状态，更新UI
 	 */
 	protected void checkSdcardAvavible(){
 		//sdcard弹出
 		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
 			Log.d(TAG, "sdcard not avaible");
 			mSdcardNoAviaible.setVisibility(View.VISIBLE);
 			mScrollView.setVisibility(View.GONE);
 		}else {
 			Log.d(TAG, "sdcard avaible");
 			if(mScrollView.getVisibility() != View.VISIBLE){
 				mScrollView.setVisibility(View.VISIBLE);
 			}
 			mSdcardNoAviaible.setVisibility(View.GONE);			
 		}
 	}
 	
 	protected void registerSdcardReceiver(){
 		IntentFilter filter = new IntentFilter();
 		filter.addAction("android.intent.action.MEDIA_MOUNTED");
 		filter.addAction("android.intent.action.MEDIA_UNMOUNTED");
 		filter.addAction("android.intent.action.MEDIA_REMOVED");
 		filter.addAction("android.intent.action.ACTION_MEDIA_BAD_REMOVAL");
 		try {
 			getActivity().registerReceiver(mSdcardReceiver, filter);
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 	}
 	protected void unregisterSdcardReceiver(){
 		try {
 			getActivity().unregisterReceiver(mSdcardReceiver);
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 	}
 	// *********成员*********	
 	private BroadcastReceiver mSdcardReceiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			// TODO Auto-generated method stub
 			if (intent.getAction()
 					.equals("android.intent.action.MEDIA_MOUNTED"))// SD卡已经成功挂载
 			{
 				Log.d(TAG, "sdcard MEDIA_MOUNTED");
 			} else if (intent.getAction().equals(
 					"android.intent.action.MEDIA_REMOVED")
 					|| intent.getAction().equals(
 							"android.intent.action.MEDIA_UNMOUNTED")
 					|| intent.getAction().equals(
 							"android.intent.action.ACTION_MEDIA_BAD_REMOVAL")) {
 				Log.d(TAG, "sdcard not MEDIA_MOUNTED");
 
 			}
 		}
 	};
 	/**
 	 * 添加文件夹
 	 */
 	private OnClickListener addClickListener = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			Intent intent = new Intent(getActivity().getApplicationContext(), UserDefineAddActivity.class);
 			startActivityForResult(intent, AppConstant.REQUEST_ADD_FILE);			
 			
 		}
 	};
 	//点击相册
 	private AlbumClickListener albumClickListener = new AlbumClickListener() {
 		
 		@Override
 		public void click(FileInfo fileInfo) {
 			// TODO Auto-generated method stub
 			if(mCallBack != null){
 				mCallBack.onClick(TAG, fileInfo);
 			}
 		}
 
 		@Override
 		public void del(Album view, FileInfo fileInfo) {
 			// TODO 删除相册
 			try {
 				gridLayout.removeView(view);
 				mHelper.remove((int)fileInfo.dbId);
 				//getLoaderManager().restartLoader(0, null, AlbumFragment.this);
 				mChanged = true;
 			} catch (Exception e) {
 				// TODO: handle exception
 				Log.e(TAG, e.toString());
 			}
 		}
 	};
 	private OnPullEventListener<ScrollView> pullEventListener = new OnPullEventListener<ScrollView>() {
 
 		@Override
 		public void onPullEvent(PullToRefreshBase<ScrollView> refreshView,
 				State state, Mode direction) {
 			// TODO Auto-generated method stub
 			if (state == State.RESET) {
 				Log.d(TAG, "reset");
 				PULL_MSG_ARRAY_INDEX = PULL_MSG_ARRAY_INDEX < PULL_MSG_ARRAY.length - 1 ? PULL_MSG_ARRAY_INDEX + 1
 						: 0;
 				refreshView.getLoadingLayoutProxy().setPullLabel(
 						PULL_MSG_ARRAY[PULL_MSG_ARRAY_INDEX]);
 				refreshView.getLoadingLayoutProxy().setReleaseLabel(
 						PULL_MSG_ARRAY[PULL_MSG_ARRAY_INDEX]);
 			}
 
 		}
 	};
 
 	private  Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			// TODO Auto-generated method stub
 			switch (msg.what) {
 			case EmojScanService.CLIENT_SCAN_GET_FILE:
 				FileInfo file = (FileInfo) msg.obj;
 				Log.d(TAG, "get:" + file.toString());
 
 				break;
 			case EmojScanService.CLIENT_ALBUM_CHECK:
 
 				break;
 			case EmojScanService.CLIENT_ACTION_END:
 				if(mCallBack != null){
 					mCallBack.onEnd();
 				}
 				refleshGridLayout();
 				break;
 			case EmojScanService.CLINET_REFLESH_UI:
 				Log.d(TAG, "reflesh UI");
 				break;
 			case EmojScanService.CLIENT_ACTION_START:
 				if(mCallBack != null){
 					mCallBack.onLoading();
 				}
 				break;
 			default:
 				break;
 			}
 		}
 	};
 
 	// *******bind service 相关*********
 	private void BindService() {
 		Intent intent = new Intent(getActivity().getParent().getApplicationContext(),
 				EmojScanService.class);
 		getActivity().getParent().getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 		mBound = true;
 	}
 
 	private void UnBindService() {
 		try {
 			getActivity().getParent().getApplicationContext().unbindService(mConnection);
 			mBound = false;
 		} catch (Exception e) {
 			// TODO: handle exception
 			Log.e(TAG, e.toString());
 		}
 		
 	}
 
 	// 注册
 	protected void register() {
 		if (mService != null) {
 			try {
 				Message message = Message.obtain(null,
 						EmojScanService.CLIENT_REGISTER);
 				message.replyTo = client;
 				mService.send(message);
 			} catch (Exception e) {
 				// TODO: handle exception
 				Log.e(TAG, e.toString());
 			}
 
 		}
 	}
 
 	protected void unregister() {
 		try {
 			Message message = Message.obtain(null,
 					EmojScanService.CLIENT_UNREGISTER);
 			message.replyTo = client;
 			mService.send(message);
 		} catch (Exception e) {
 			// TODO: handle exception
 			Log.e(TAG, e.toString());
 		}
 	}
 
 	// 开始扫描
 	public void scanfiles() {
 		try {
 			Log.d(TAG, "scanfiles()");
 			Message message = Message.obtain(null, EmojScanService.CLIENT_SCAN);
 			message.replyTo = client;
 			mService.send(message);
 		} catch (Exception e) {
 			// TODO: handle exception
 			Log.e(TAG, "11:"+e.toString());
 		}
 	}
 
 	private final ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			// TODO Auto-generated method stub
 			Log.d(TAG, "disconnect");
 			mService = null;
 		}
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			// TODO Auto-generated method stub
 			Log.d(TAG, "onServiceConnected");
 			try {
 				mService = new Messenger(service);
 				register();
 			} catch (Exception e) {
 				// TODO: handle exception
 				Log.e(TAG, e.toString());
 			}
 		}
 	};
 	
 	//*****CusorLoader事件*******
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		// TODO Auto-generated method stub
 		Log.d(TAG, "onCreateLoader");
 		CursorLoader loader = new CursorLoader(getActivity(), UserDefineContentProvider.getUri(), null, null, null, null);
 		return loader;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
 		// TODO Auto-generated method stub
 		Log.d(TAG, "onLoadFinished");
 		try {
 			adapter.changeCursor(cursor);
 			//bindGridLayout(cursor);
 		} catch (Exception e) {
 			// TODO: handle exception
 			Log.e(TAG, e.toString());
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		// TODO Auto-generated method stub
 		Log.d(TAG, "onLoaderReset");
 		
 	}
 }
