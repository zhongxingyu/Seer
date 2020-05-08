 package com.quanleimu.view;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.entity.ImageList;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ErrorHandler;
 import com.quanleimu.util.GoodsListLoader;
 import com.quanleimu.activity.BaseActivity;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.BXLocation;
 import com.quanleimu.adapter.GoodsListAdapter;
 import com.quanleimu.view.BaseView;
 import com.quanleimu.widget.PullToRefreshListView;
 import com.quanleimu.widget.PullToRefreshListView.E_GETMORE;
 
 public class GetGoodsView extends BaseView implements View.OnClickListener, OnScrollListener, PullToRefreshListView.OnRefreshListener, PullToRefreshListView.OnGetmoreListener{
 
 	private PullToRefreshListView lvGoodsList;
 	private ProgressDialog pd;
 	private ProgressBar progressBar;
 
 	private String categoryEnglishName = "";
 	private String siftResult = "";
 
 	private GoodsListAdapter adapter;
 
 	Bundle bundle;
 	
 	private GoodsListLoader goodsListLoader = null;
 	
 	private View titleControl = null;
 	
 	private List<String> basicParams = null;
 	
 //	private int titleControlStatus = 0;//0:Left(Recent), 1: Right(Nearby)
 	private int titleControlStatus = 1;//0:Right(Recent), 1: Left(Nearby)
 	
 	private BXLocation curLocation = null;
 
     private static final int HOUR_MS = 60*60*1000;
     private static final int MINUTE_MS = 60*1000;
     
 	private boolean mRefreshUsingLocal = false;
 	
 	@Override
 	public void onResume(){
 		this.lvGoodsList.requestFocus();//force cate view has focus
 		
 		for(int i = 0; i < lvGoodsList.getChildCount(); ++i){
 			ImageView imageView = (ImageView)lvGoodsList.getChildAt(i).findViewById(R.id.ivInfo);
 			
 			if(	null != imageView	
 					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
 					/*&& null != imageView.getDrawable()
 					&& imageView.getDrawable() instanceof AnimationDrawable*/){
 				SimpleImageLoader.showImg(imageView, imageView.getTag().toString(), null, getContext());
 			}
 		}
 		
 		if(null != goodsListLoader){
 			goodsListLoader.setHasMoreListener(null);
 			goodsListLoader.setHandler(myHandler);
 			if(null != adapter){
 				adapter.setList(goodsListLoader.getGoodsList().getData());
 			}
 			lvGoodsList.setSelectionFromHeader(goodsListLoader.getSelection());
 		}
 		
 	}
 	
 	@Override
 	public void onPause(){
 		Log.d("onpause of getgoods", "hahaha  ohnoooooooooooooooooooooooo!!!!!");
 		super.onPause();
 		
 		for(int i = 0; i < lvGoodsList.getChildCount(); ++i){
 			ImageView imageView = (ImageView)lvGoodsList.getChildAt(i).findViewById(R.id.ivInfo);
 			
 			if(	null != imageView){	
 				if(null != imageView.getTag() && imageView.getTag().toString().length() > 0
 				/*&& null != imageView.getDrawable()
 				&& imageView.getDrawable() instanceof AnimationDrawable*/){
 					SimpleImageLoader.Cancel(imageView.getTag().toString(), imageView);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void onDestroy(){		
 		this.lvGoodsList = null;
 		this.adapter = null;
 		if(goodsListLoader != null && goodsListLoader.getGoodsList() != null){
 			 List<GoodsDetail> list = goodsListLoader.getGoodsList().getData();
 			 if(list != null){
 				 for(int i = 0; i < list.size(); ++ i){
 					 GoodsDetail gd = list.get(i);
 					 if(gd != null){
 						 ImageList il = gd.getImageList();
 						 if(il != null){
 							 if(il.getResize180() != null){
 								 String b = il.getResize180();
 								 if (b.contains(",")) {
 									String[] c = b.split(",");
 									if (c[0] != null && !c[0].equals("")) {
 //										Log.d("ondestroy of getgoodsview", "hahahaha recycle in getgoodsview ondestroy");
 										QuanleimuApplication.lazyImageLoader.forceRecycle(c[0]);
 //										Log.d("ondestroy of getgoodsview", "hahahaha end recycle in getgoodsview ondestroy");
 									}
 								 }
 							 }
 						 }
 					 }
 				 }
 			 }
 		}
 		this.goodsListLoader.reset();
 		this.goodsListLoader = null;
 		System.gc();
 		super.onDestroy();
 	}
 	
 		
 	public GetGoodsView(Context context, Bundle bundle, String categoryEnglishName){
 		super(context, bundle);
 		this.categoryEnglishName = categoryEnglishName;
 
 		this.bundle = bundle;
 //		Debug.startMethodTracing();
 		init();
 	}
 
 	public GetGoodsView(Context context, Bundle bundle, String categoryEnglishName, String siftResult){
 		super(context, bundle);
 		this.categoryEnglishName = categoryEnglishName;
 		this.siftResult = siftResult;
 		this.bundle = bundle;	
 		
 		init();
 	}
 	
 	public boolean onRightActionPressed(){
 	
 		bundle.putString("backPageName", bundle.getString("backPageName"));
 		bundle.putString("searchType", "goodslist");
 		bundle.putString("categoryEnglishName", categoryEnglishName);
 
 		if(null != m_viewInfoListener){
 			m_viewInfoListener.onNewView(new SiftView(getContext(), bundle));
 		}
 		
 		return true;
 	}//called when right button on title bar pressed, return true if handled already, false otherwise
 	
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
		title.m_leftActionHint = "返回";//bundle.getString("backPageName");
 		title.m_title = bundle.getString("name");
 		title.m_rightActionHint = "筛选";
 		
 //		if(null == titleControl){
 //		}
 		title.m_titleControls = titleControl;
 		
 		return title;
 	}
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = false;
 		return tab;}
 
 	protected void init() {
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		View v = inflater.inflate(R.layout.goodslist, null);
 		this.addView(v);
 		
 		titleControl = inflater.inflate(R.layout.recent_or_nearby, null);
 		titleControl.findViewById(R.id.btnNearby).setOnClickListener(this);
 		titleControl.findViewById(R.id.btnRecent).setOnClickListener(this);
 
 
 		lvGoodsList = (PullToRefreshListView) findViewById(R.id.lvGoodsList);
 		lvGoodsList.setOnRefreshListener(this);
 		lvGoodsList.setOnGetMoreListener(this);
 
         LinearLayout layout = new LinearLayout(this.getContext());  
         layout.setOrientation(LinearLayout.HORIZONTAL);  
         progressBar = new ProgressBar(this.getContext(), null, android.R.attr.progressBarStyleSmall);
          //进度条显示位置  
         progressBar.setVisibility(View.GONE);
         
         LayoutParams WClayoutParams =
         		new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
         layout.addView(progressBar, WClayoutParams);  
         layout.setGravity(Gravity.CENTER);  
 		
 		lvGoodsList.setOnScrollListener(this);
 
 		pd = ProgressDialog.show(this.getContext(), "提示", "请稍候...");
 		pd.setCancelable(true);
 
 		basicParams = new ArrayList<String>();
 		if (siftResult != null && !siftResult.equals("")) {
 			basicParams.add("query="
 					+ "cityEnglishName:"+QuanleimuApplication.getApplication().getCityEnglishName()+" AND categoryEnglishName:"
 					+ categoryEnglishName + " " + siftResult);
 		} else {
 			basicParams.add("query="
 					+ "cityEnglishName:"+QuanleimuApplication.getApplication().getCityEnglishName()+" AND categoryEnglishName:"
 					+ categoryEnglishName + " AND status:0");
 		}
 
 		
 		goodsListLoader = new GoodsListLoader(basicParams, myHandler, null, new GoodsList());
 		
 		curLocation = QuanleimuApplication.getApplication().getCurrentPosition(true);
 		if(curLocation == null){
 			((Button)titleControl.findViewById(R.id.btnNearby)).setBackgroundResource(R.drawable.bg_nav_seg_left_normal);
 			((Button)titleControl.findViewById(R.id.btnRecent)).setBackgroundResource(R.drawable.bg_nav_seg_right_pressed);
 			this.titleControlStatus = 0;
 		}else{
 			basicParams.add("lat="+curLocation.fLat);
 			basicParams.add("lng="+curLocation.fLon);
 			goodsListLoader.setNearby(true);
 		}
 		
 		goodsListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL);
 		
 		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				
 				int index = arg2 - lvGoodsList.getHeaderViewsCount();
 				if(index < 0 || index > goodsListLoader.getGoodsList().getData().size() - 1)
 					return;
 
 				if(GetGoodsView.this.m_viewInfoListener != null){
 					bundle.putSerializable("currentGoodsDetail", goodsListLoader.getGoodsList().getData().get(index));
 					bundle.putString("detail_type", "getgoods");
 					m_viewInfoListener.onNewView(new GoodDetailView(getContext(), bundle, goodsListLoader, index, null));
 				}				
 			}
 		});
 		
 		((TextView)findViewById(R.id.tvSubCateName)).setText(bundle.getString("name"));
 
 		String categoryName = bundle.getString("categoryName");
 		if(categoryName == null || categoryName.equals("") || categoryEnglishName == null || categoryEnglishName.equals("")){
 			findViewById(R.id.publishBtn).setVisibility(View.GONE);
 		}else{
 			findViewById(R.id.publishBtn).setOnClickListener(this);
 			findViewById(R.id.publishBtn).setVisibility(View.VISIBLE);
 		}
 
 	}
 
 		// 管理线程的Handler
 	Handler myHandler = new Handler() {
 		
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case GoodsListLoader.MSG_FIRST_FAIL:
 				if(goodsListLoader == null) break;
 				if(GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE == goodsListLoader.getRequestDataStatus())
 					goodsListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE);
 				else{
 					Toast.makeText(getContext(), "没有符合条件的结果，请重新输入！", Toast.LENGTH_LONG).show();
 					
 					if (pd != null) {
 						pd.dismiss();
 					}
 				}
 				break;
 			case GoodsListLoader.MSG_FINISH_GET_FIRST:
 				if(goodsListLoader == null) break;
 				GoodsList goodsList = JsonUtil.getGoodsListFromJson(goodsListLoader.getLastJson());
 				goodsListLoader.setGoodsList(goodsList);
 				if (goodsList == null || goodsList.getData() == null || goodsList.getData().size() == 0) {
 					Message msg1 = Message.obtain();
 					msg1.what = ErrorHandler.ERROR_COMMON_FAILURE;
 					Bundle bundle = new Bundle();
 					bundle.putString("popup_message", "没有符合的结果，请更改条件并重试！");
 					msg1.setData(bundle);
 					QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg1);
 				} else {
 					//QuanleimuApplication.getApplication().setListGoods(goodsListLoader.getGoodsList().getData());
 					
 					adapter = new GoodsListAdapter(GetGoodsView.this.getContext(), goodsListLoader.getGoodsList().getData());
 					lvGoodsList.setAdapter(adapter);
 					
 					goodsListLoader.setHasMore(true);
 				}
 				
 				lvGoodsList.onRefreshComplete();
 				
 				//if currently using offline data, start fetching online data
 				if(GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE == goodsListLoader.getDataStatus())
 					lvGoodsList.fireRefresh();
 					//goodsListLoader.startFetching(true, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE);
 				
 				if (pd != null) {
 					pd.dismiss();
 				}
 				
 				break;
 			case GoodsListLoader.MSG_NO_MORE:
 				if(goodsListLoader == null) break;
 				progressBar.setVisibility(View.GONE);
 				
 //				Message msg1 = Message.obtain();
 //				msg1.what = ErrorHandler.ERROR_COMMON_FAILURE;
 //				Bundle bundle = new Bundle();
 //				bundle.putString("popup_message", "数据下载失败，请重试！");
 //				msg1.setData(bundle);
 //				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg1);
 				
 				lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_NO_MORE);
 				goodsListLoader.setHasMore(false);
 				
 				if (pd != null) {
 					pd.dismiss();
 				}
 				
 				break;
 			case GoodsListLoader.MSG_FINISH_GET_MORE:
 				if(goodsListLoader == null) break;
 				progressBar.setVisibility(View.GONE);
 				
 				GoodsList moreGoodsList = JsonUtil.getGoodsListFromJson(goodsListLoader.getLastJson());
 				if (moreGoodsList == null || moreGoodsList.getData() == null || moreGoodsList.getData().size() == 0) {
 //					Message msg2 = Message.obtain();
 //					msg2.what = ErrorHandler.ERROR_COMMON_WARNING;
 //					Bundle bundle1 = new Bundle();
 //					bundle1.putString("popup_message", "没有更多啦！");
 //					msg2.setData(bundle1);
 //					QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 					
 					lvGoodsList.onGetMoreCompleted(E_GETMORE.E_GETMORE_NO_MORE);
 					goodsListLoader.setHasMore(false);
 				} else {
 					List<GoodsDetail> listCommonGoods =  moreGoodsList.getData();
 					for(int i=0;i<listCommonGoods.size();i++)
 					{
 						goodsListLoader.getGoodsList().getData().add(listCommonGoods.get(i));
 					}
 					//QuanleimuApplication.getApplication().setListGoods(goodsListLoader.getGoodsList().getData());
 					
 					adapter.setList(goodsListLoader.getGoodsList().getData());
 					adapter.notifyDataSetChanged();		
 					
 					lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_OK);
 					goodsListLoader.setHasMore(true);
 				}
 				
 				if (pd != null) {
 					pd.dismiss();
 				}
 				
 				break;
 			case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
 				if(goodsListLoader == null) break;
 				progressBar.setVisibility(View.GONE);
 
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 				
 				lvGoodsList.onFail();
 				
 				if (pd != null) {
 					pd.dismiss();
 				}
 				
 				break;
 			}
 			
 			super.handleMessage(msg);
 		}
 	};
 	
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 		if(		null == goodsListLoader || 
 				null == goodsListLoader.getGoodsList() || 
 				null == goodsListLoader.getGoodsList().getData() || 
 				goodsListLoader.getGoodsList().getData().size() <= firstVisibleItem){
 			return;
 		}
 		
     	String number = "";
     	String unit = "";
     	
     	firstVisibleItem -= ((PullToRefreshListView)view).getHeaderViewsCount();
     	if(firstVisibleItem < 0)	firstVisibleItem = 0;
     	
 		if(0 == titleControlStatus){//time-sequenced
 			Date date = new Date(Long.parseLong(goodsListLoader.getGoodsList().getData().get(firstVisibleItem).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DATE)) * 1000);
 			long time_first_item = date.getTime();
 			//Log.d("kkkkkk", "item:"+firstVisibleItem+", dateTime: "+time_first_item);
 
 	    	long time_diff = System.currentTimeMillis() - time_first_item;
 	    	
 	    	long nHours = time_diff / HOUR_MS;
 	    	time_diff %= HOUR_MS;
 	    	long nMinutes = time_diff / MINUTE_MS;
 	    	time_diff %= MINUTE_MS;
 
 	    	if(nHours > 0){
 	    		unit = "小时";
 	    		number += nHours;
 	    		int fractorHours = (int)(nMinutes/6.0f);
 	    		if(fractorHours > 0){
 	    			number += "."+fractorHours;
 	    		}
 	    	}else{
 	    		unit = "分钟";
 	    		number += nMinutes;
 	    		if(number.contains("-")){
 	    			number = "1";
 	    		}
 	    		else if(number.equals("0")){
 	    			number = "1";
 	    		}
 	    	}
 		}else{
 			GoodsDetail detail = goodsListLoader.getGoodsList().getData().get(firstVisibleItem);
 			String lat = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT);
 			String lon = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON);
 			
 			double latD = 0;
 			double lonD = 0;
 			try
 			{
 				latD = Double.parseDouble(lat);
 				lonD = Double.parseDouble(lon);
 			}
 			catch(Throwable t)
 			{
 				Log.d("GetGoodView", "ad nearby lacks lat & lon");
 			}
 			
 			if(latD == 0  || lonD == 0){
 				unit = "米";
 				number = "0";
 			}else{
 
 				float results[] = {0.0f, 0.0f, 0.0f};
 				Location.distanceBetween(latD, lonD, curLocation.fLat, curLocation.fLon, results);
 				
 				if(results[0] < 1000){
 					unit = "米";
 					number += (int)(results[0]);
 				}else{
 					unit = "公里";
 					int kilo_number = (int)(results[0]/1000);
 					int fractor_kilo_number = (int)((results[0]-(kilo_number*1000))/100);
 					number = ""+kilo_number+"."+fractor_kilo_number;
 				}
 			}
 		}
 		
 		((TextView)findViewById(R.id.tvSpaceOrTimeNumber)).setText(number);
 		((TextView)findViewById(R.id.tvSpaceOrTimeUnit)).setText(unit);
 		
 		//Log.d("kkkkkk", "first visible item: "+firstVisibleItem+", visibleItemCount: "+visibleItemCount+", totalItemCount: "+totalItemCount);
 	}
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		if(scrollState == SCROLL_STATE_IDLE)
 		{
 			ArrayList<String> urls = new ArrayList<String>();
 			for(int index = 0; index < view.getChildCount(); ++index){
 				View curView = view.getChildAt(+index);
 				if(null != curView){
 					View curIv = curView.findViewById(R.id.ivInfo);
 					
 					if(null != curIv && null != curIv.getTag())	urls.add(curIv.getTag().toString());
 				}			
 			}
 			
 			SimpleImageLoader.AdjustPriority(urls);			
 		}		
 	}
 
 	@Override
 	public void onGetMore() {
 		goodsListLoader.startFetching(false, ((GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE == goodsListLoader.getDataStatus()) ? 
 												Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE :
 												Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL));
 	}
 
 	@Override
 	public void onRefresh() {
 		goodsListLoader.startFetching(true, mRefreshUsingLocal ? Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL : Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE);	
 		mRefreshUsingLocal = false;
 	}
 	
 	
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()){
 		case R.id.publishBtn:
 			String categoryName = bundle.getString("categoryName");
 			categoryName = categoryEnglishName + "," + categoryName;
 			m_viewInfoListener.onNewView(new PostGoodsView((BaseActivity)this.getContext(), this.bundle, categoryName));
 			break;
 		case R.id.btnRecent:
 			if(titleControlStatus != 0){
 				View btnNearBy = titleControl.findViewById(R.id.btnNearby);
 				int paddingLeft = btnNearBy.getPaddingLeft(), paddingRight = btnNearBy.getPaddingRight(), paddingTop=btnNearBy.getPaddingTop(), paddingBottom=btnNearBy.getPaddingBottom();
 				btnNearBy.setBackgroundResource(R.drawable.bg_nav_seg_left_normal);
 				btnNearBy.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
 				
 				View btnRecent = titleControl.findViewById(R.id.btnRecent);
 				paddingLeft = btnRecent.getPaddingLeft(); paddingRight = btnRecent.getPaddingRight(); paddingTop=btnRecent.getPaddingTop();paddingBottom=btnRecent.getPaddingBottom();
 				btnRecent.setBackgroundResource(R.drawable.bg_nav_seg_right_pressed);
 				btnRecent.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
 				
 				((TextView)findViewById(R.id.tvSpaceOrTimeNumber)).setText("0");
 				((TextView)findViewById(R.id.tvSpaceOrTimeUnit)).setText("小时");
 				
 				goodsListLoader.cancelFetching();
 				goodsListLoader.setNearby(false);
 				goodsListLoader.setParams(basicParams);
 				goodsListLoader.setRuntime(true);
 				
 				mRefreshUsingLocal = true;
 				lvGoodsList.onFail();
 				lvGoodsList.fireRefresh();
 				
 				titleControlStatus = 0;
 			}
 			break;
 		case R.id.btnNearby:
 			if(titleControlStatus != 1){
 				curLocation = QuanleimuApplication.getApplication().getCurrentPosition(true);
 				if(curLocation == null){
 					new AlertDialog.Builder(this.getContext()).setTitle("提醒").setMessage("无法确定当前位置")
 					.setPositiveButton("确定", new DialogInterface.OnClickListener(){
 						@Override
 						public void onClick(DialogInterface dialog, int which){
 							dialog.dismiss();
 						}
 					}).show();
 					return;
 				}
 				View btnNearBy = titleControl.findViewById(R.id.btnNearby);
 				int paddingLeft = btnNearBy.getPaddingLeft(), paddingRight = btnNearBy.getPaddingRight(), paddingTop=btnNearBy.getPaddingTop(), paddingBottom=btnNearBy.getPaddingBottom();
 				btnNearBy.setBackgroundResource(R.drawable.bg_nav_seg_left_pressed);
 				btnNearBy.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
 				
 				View btnRecent = titleControl.findViewById(R.id.btnRecent);
 				paddingLeft = btnRecent.getPaddingLeft(); paddingRight = btnRecent.getPaddingRight(); paddingTop=btnRecent.getPaddingTop();paddingBottom=btnRecent.getPaddingBottom();
 				btnRecent.setBackgroundResource(R.drawable.bg_nav_seg_right_normal);
 				btnRecent.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
 				
 				((TextView)findViewById(R.id.tvSpaceOrTimeNumber)).setText("0");
 				((TextView)findViewById(R.id.tvSpaceOrTimeUnit)).setText("米");
 				
 				goodsListLoader.cancelFetching();
 				
 				List<String> params = new ArrayList<String>();
 				params.addAll(basicParams);
 //				params.add("nearby=true");
 				goodsListLoader.setNearby(true);
 
 				//Log.d("kkkkkk", "get goods nearby: ("+curLocation.fLat+", "+curLocation.fLon+") !!");
 				params.add("lat="+curLocation.fLat);
 				params.add("lng="+curLocation.fLon);
 				goodsListLoader.setParams(params);
 				goodsListLoader.setRuntime(false);
 				
 				mRefreshUsingLocal = true;
 				lvGoodsList.onFail();
 				lvGoodsList.fireRefresh();
 				
 				titleControlStatus = 1;
 			}
 			break;
 		}
 	}
 }
