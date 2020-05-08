 package com.quanleimu.view;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ErrorHandler;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.GoodsListAdapter;
 import com.quanleimu.view.BaseView;
 import com.quanleimu.widget.PullToRefreshListView;
 
 public class GetGoodsView extends BaseView implements OnScrollListener, PullToRefreshListView.OnRefreshListener, PullToRefreshListView.OnGetmoreListener{
 
 	private PullToRefreshListView lvGoodsList;
 	private ProgressDialog pd;
 	private ProgressBar progressBar;
 	//private LinearLayout loadingLayout;
 
 	private String categoryEnglishName = "";
 	private String siftResult = "";
 
 	private String json = "";
 	private int startRow = 0;
 	//private List<Bitmap> listBm = new ArrayList<Bitmap>();
 	private List<GoodsDetail> listGoods = new ArrayList<GoodsDetail>();
 	private List<GoodsDetail> listCommonGoods = new ArrayList<GoodsDetail>();
 	private GoodsList goodsList = new GoodsList();
 	private GoodsListAdapter adapter;
 	private boolean isFirst = true;
 	private String mUrl = "";
 	
 	private final static int ERROR_FIRST = 0;
 	private final static int ERROR_MORE = 1;
 	private final static int ERROR_NOMORE = 2;
 	
 	//TextView tvAddMore;
 
 	Bundle bundle;
 	
	
	
	@Override
	public void onResume(){
		this.lvGoodsList.requestFocus();//force cate view has focus
	}
	
 		
 	public GetGoodsView(Context context, Bundle bundle, String categoryEnglishName){
 		super(context, bundle);
 		this.categoryEnglishName = categoryEnglishName;
 
 		this.bundle = bundle;
 		
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
 		title.m_leftActionHint = bundle.getString("backPageName");
 		title.m_title = bundle.getString("name");
 		title.m_rightActionHint = "筛选";
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
 //        loadingLayout = new LinearLayout(this.getContext());  
 //        loadingLayout.setBackgroundResource(R.drawable.alpha_bg);
 //        loadingLayout.addView(layout, WClayoutParams);  
 //        loadingLayout.setGravity(Gravity.CENTER); 
         
 //        tvAddMore.setOnClickListener(new View.OnClickListener() {
 //			
 //			@Override
 //			public void onClick(View v) {
 //				progressBar.setVisibility(View.VISIBLE);
 //				tvAddMore.setText("加载中...");
 //				
 //				//点击获取更多 按钮布局消失
 //				isFirst = false;
 //				startRow = listGoods.size();
 //				new Thread(new GetGoodsListThread()).start();
 //			}
 //		});
 		
 //        lvGoodsList.setDivider(null);
 //		lvGoodsList.addFooterView(loadingLayout);
 		
 		lvGoodsList.setOnScrollListener(this);
 
 		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				
 				int index = arg2 - lvGoodsList.getHeaderViewsCount();
 				if(index < 0 || index > listGoods.size() - 1)
 					return;
 
 				if(GetGoodsView.this.m_viewInfoListener != null){
 					bundle.putSerializable("currentGoodsDetail", listGoods.get(index));
 					bundle.putString("detail_type", "getgoods");
 					m_viewInfoListener.onNewView(new GoodDetailView(listGoods.get(index), getContext(), bundle));
 				}				
 			}
 		});
 
 		pd = ProgressDialog.show(this.getContext(), "提示", "请稍候...");
 		pd.setCancelable(true);
 
 		if (siftResult != null && !siftResult.equals("")) {
 			mUrl = "query="
 					+ "cityEnglishName:"+QuanleimuApplication.getApplication().getCityEnglishName()+" AND categoryEnglishName:"
 					+ categoryEnglishName + " " + siftResult;
 		} else {
 			mUrl = "query="
 					+ "cityEnglishName:"+QuanleimuApplication.getApplication().getCityEnglishName()+" AND categoryEnglishName:"
 					+ categoryEnglishName + " AND status:0";
 		}
 
 		new Thread(new GetGoodsListThread()).start();
 
 	}
 
 		// 管理线程的Handler
 	Handler myHandler = new Handler() {
 		
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case GetGoodsView.ERROR_FIRST:				 
 				goodsList = JsonUtil.getGoodsListFromJson(json);
 				if (goodsList == null || goodsList.getCount() == 0) {
 					Message msg1 = Message.obtain();
 					msg1.what = ErrorHandler.ERROR_COMMON_FAILURE;
 					Bundle bundle = new Bundle();
 					bundle.putString("popup_message", "没有符合的结果，请更改条件并重试！");
 					msg1.setData(bundle);
 					QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg1);
 				} else {
 					listGoods = goodsList.getData();
 					QuanleimuApplication.getApplication().setListGoods(listGoods);
 					
 					adapter = new GoodsListAdapter(GetGoodsView.this.getContext(), listGoods);
 					lvGoodsList.setAdapter(adapter);
 				}
 				
 				lvGoodsList.onRefreshComplete();
 				
 				break;
 			case GetGoodsView.ERROR_NOMORE:
 				progressBar.setVisibility(View.GONE);
 //				tvAddMore.setText("更多...");
 //				loadingLayout.setVisibility(View.GONE);
 				
 				Message msg1 = Message.obtain();
 				msg1.what = ErrorHandler.ERROR_COMMON_FAILURE;
 				Bundle bundle = new Bundle();
 				bundle.putString("popup_message", "数据下载失败，请重试！");
 				msg1.setData(bundle);
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg1);
 				
 				lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_NO_MORE);
 				
 				break;
 			case GetGoodsView.ERROR_MORE:
 				progressBar.setVisibility(View.GONE);
 //				tvAddMore.setText("更多...");
 //				loadingLayout.setVisibility(View.GONE);
 				
 				goodsList = JsonUtil.getGoodsListFromJson(json);
 				if (goodsList == null || goodsList.getCount() == 0) {
 					Message msg2 = Message.obtain();
 					msg2.what = ErrorHandler.ERROR_COMMON_WARNING;
 					Bundle bundle1 = new Bundle();
 					bundle1.putString("popup_message", "没有更多啦！");
 					msg2.setData(bundle1);
 					QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 				} else {
 					listCommonGoods =  goodsList.getData();
 					for(int i=0;i<listCommonGoods.size();i++)
 					{
 						listGoods.add(listCommonGoods.get(i));
 					}
 					QuanleimuApplication.getApplication().setListGoods(listGoods);
 					
 					adapter.setList(listGoods);
 					adapter.notifyDataSetChanged();					
 				}
 				
 				lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_OK);
 				
 				break;
 			case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
 				progressBar.setVisibility(View.GONE);
 //				tvAddMore.setText("更多...");
 //				loadingLayout.setVisibility(View.GONE);
 				
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 				break;
 			}
 			
 			if (pd != null) {
 				pd.dismiss();
 			}
 			
 //			// 判断总数是不是已经超出当前集合长度
 //			if (goodsList.getCount() > listGoods.size()) {
 //				loadingLayout.setVisibility(View.VISIBLE);
 //			} else {
 //				loadingLayout.setVisibility(View.GONE);
 //			}
 			
 			super.handleMessage(msg);
 		}
 	};
 
 	class GetGoodsListThread implements Runnable {
 		@Override
 		public void run() {
 			String apiName = "ad_list";
 			ArrayList<String> list = new ArrayList<String>();
 
 //			list.add("fields=" + URLEncoder.encode(fields));
 			list.add(mUrl);
 			list.add("start=" + startRow);
 			list.add("rows=" + 30);
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url);
 
 				if (json != null) {
 					if (isFirst == false) {
 						//isFirst = 0;
 						myHandler.sendEmptyMessage(GetGoodsView.ERROR_MORE);
 					} else {
 						isFirst = false;
 						myHandler.sendEmptyMessage(GetGoodsView.ERROR_FIRST);
 					}
 
 				} else {
 					myHandler.sendEmptyMessage(GetGoodsView.ERROR_NOMORE);
 				}
 			} catch (UnsupportedEncodingException e) {
 				myHandler.sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 			} catch (IOException e) {
 				myHandler.sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 			} 
 
 		}
 	}
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 		//Log.d("GetGoodsView: ", "on scroll called!");
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
 		// TODO Auto-generated method stub
 		isFirst = false;
 		startRow = listGoods.size();
 		new Thread(new GetGoodsListThread()).start();
 	}
 
 	@Override
 	public void onRefresh() {
 		// TODO Auto-generated method stub
 		isFirst = true;
 		startRow = 0;
 		new Thread(new GetGoodsListThread()).start();		
 	}
 }
