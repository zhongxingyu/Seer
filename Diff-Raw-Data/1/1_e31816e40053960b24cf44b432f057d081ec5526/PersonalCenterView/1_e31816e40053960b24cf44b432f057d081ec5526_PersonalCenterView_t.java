 package com.quanleimu.view;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.Helper;
 import com.quanleimu.util.Util;
 import com.quanleimu.adapter.GoodsListAdapter;
 import com.quanleimu.view.BaseView;
 import com.quanleimu.widget.PullToRefreshListView;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import android.widget.LinearLayout;
 public class PersonalCenterView extends BaseView implements OnScrollListener, View.OnClickListener, PullToRefreshListView.OnRefreshListener, PullToRefreshListView.OnGetmoreListener{
 	private final int MCMESSAGE_MYPOST_SUCCESS = 0;
 	private final int MCMESSAGE_MYPOST_FAIL = 1;
 	private final int MCMESSAGE_DELETE = 2;
 	private final int MCMESSAGE_DELETE_SUCCESS = 3;
 	private final int MCMESSAGE_DELETE_FAIL = 4;
 	private final int MCMESSAGE_DELETEALL = 5;
 	private final int MCMESSAGE_MYHISTORY_UPDATE_SUCCESS = 6;
 	private final int MCMESSAGE_MYHISTORY_UPDATE_FAIL = 7;
 	private final int MCMESSAGE_MYFAV_UPDATE_SUCCESS = 8;
 	private final int MCMESSAGE_MYFAV_UPDATE_FAIL = 9;	
 	private final int MCMESSAGE_NETWORKERROR = 10;
 
 	
 	private final int MCMESSAGE_MYPOST_GETMORE_SUCCESS = 11;
 	private final int MCMESSAGE_MYPOST_GETMORE_FAIL = 12;
 	private final int MCMESSAGE_MYHISTORY_GETMORE_SUCCESS = 13;
 	private final int MCMESSAGE_MYHISTORY_GETMORE_FAIL = 14;
 	private final int MCMESSAGE_MYFAV_GETMORE_SUCCESS = 15;
 	private final int MCMESSAGE_MYFAV_GETMORE_FAIL = 16;
 	
 	private final int MCMESSAGE_MYFAV_UPDATE_NOTNECESSARY = 20;
 	private final int MCMESSAGE_MYFAV_GETMORE_NOTNECESSARY = 21;	
 	
 
 	public PullToRefreshListView lvGoodsList;
 	public ImageView ivMyads, ivMyfav, ivMyhistory;
 
 	private List<GoodsDetail> listMyPost = new ArrayList<GoodsDetail>();
 	private List<GoodsDetail> goodsList = new ArrayList<GoodsDetail>();
 	public GoodsListAdapter adapter = null;
 	private String mobile;
 	private String json;
 	private String password;
 	UserBean user;
 	private int currentPage = -1;//-1:mypost, 0:myfav, 1:history
 	private Bundle bundle;
 	private int buttonStatus = -1;//-1:edit 0:finish
 	private View loginItem = null;
 	
 	private boolean loginTried = false;
 	
 	public PersonalCenterView(Context context, Bundle bundle){
 		super(context, bundle);
 		this.bundle = bundle;
 		init();
 	}
 
 	private void rebuildPage(boolean onResult){
 		LinearLayout lView = (LinearLayout)this.findViewById(R.id.linearListView);
 		
 		if(-1 == currentPage){
 			ivMyads.setImageResource(R.drawable.btn_posted_press);
 			ivMyfav.setImageResource(R.drawable.btn_fav_normal);
 			ivMyhistory.setImageResource(R.drawable.btn_history_normal);
 			if(m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_title = "我发布的信息";
 				m_viewInfoListener.onTitleChanged(title);
 			}
 			UserBean tmpBean = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
 			if(onResult || 
 					(tmpBean != null 
 						&& user != null && tmpBean.getPhone().equals(user.getPhone()))){
 				adapter.setList(listMyPost);
 				adapter.notifyDataSetChanged();
 			}
 			else{
 				user = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
 						
 				if (user != null) {					
 					if(loginItem != null){
 						lView.removeView(loginItem);
 					}
 					lvGoodsList.setVisibility(View.VISIBLE);
 					mobile = user.getPhone();
 					password = user.getPassword();
 					pd = ProgressDialog.show(this.getContext(), "提示", "正在下载数据，请稍候...");
 					pd.setCancelable(true);
 					new Thread(new UpdateAndGetmoreThread(currentPage, true)).start();
 				} else {
 					if(null == loginItem){
 						LayoutInflater inflater = LayoutInflater.from(this.getContext());
 						loginItem = inflater.inflate(R.layout.item_post_select, null);
 						loginItem.setClickable(true);
 						TextView show = (TextView)loginItem.findViewById(R.id.postshow);
 						show.setText("登录或注册");
 						loginItem.setOnClickListener(new OnClickListener(){
 							@Override
 							public void onClick(View v) {
 								bundle.putInt("type", 1);
 								bundle.putString("backPageName", "");					
 								m_viewInfoListener.onNewView(new LoginView(getContext(), bundle));
 							}
 						});
 					}
 					if(loginItem.getParent() == null){
 						lView.addView(loginItem);
 					}
 					lvGoodsList.setVisibility(View.GONE);
 				}
 			}			
 			
 //			lvGoodsList.setPullToRefreshEnabled(true);
 			lvGoodsList.setAdapter(adapter);
 		}
 		else if(0 == currentPage){
 			if(loginItem != null){
 				lView.removeView(loginItem);
 			}
 			lvGoodsList.setVisibility(View.VISIBLE);
 			ivMyads.setImageResource(R.drawable.btn_posted_normal);
 			ivMyfav.setImageResource(R.drawable.btn_fav_press);
 			ivMyhistory.setImageResource(R.drawable.btn_history_normal);
 			
 			if(m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_title = "我的收藏";
 				title.m_rightActionHint = "编辑";
 //				title.m_leftActionHint = "更新";
 				m_viewInfoListener.onTitleChanged(title);
 			}
 			goodsList = QuanleimuApplication.getApplication().getListMyStore();
 			adapter.setList(goodsList);
 			adapter.notifyDataSetChanged();
 			
 //			lvGoodsList.setPullToRefreshEnabled(false);
 		}
 		else{
 			if(loginItem != null){
 				lView.removeView(loginItem);
 			}
 			lvGoodsList.setVisibility(View.VISIBLE);
 			ivMyads.setImageResource(R.drawable.btn_posted_normal);
 			ivMyfav.setImageResource(R.drawable.btn_fav_normal);
 			ivMyhistory.setImageResource(R.drawable.btn_history_press);
 			if(m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_title = "我的历史";
 				title.m_rightActionHint = "编辑";
 //				title.m_leftActionHint = "更新";
 				m_viewInfoListener.onTitleChanged(title);
 			}
 			goodsList = QuanleimuApplication.getApplication().getListLookHistory();
 			adapter.setList(goodsList);
 			adapter.notifyDataSetChanged();
 			
 //			lvGoodsList.setPullToRefreshEnabled(false);
 		}
 		
 
 		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {			
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				int index = arg2 - lvGoodsList.getHeaderViewsCount();
 				if(index < 0)
 					return;
 				
 				GoodsDetail detail = null;
 				if(currentPage == -1 && index < listMyPost.size() ){
 					detail = listMyPost.get(index);					
 				}
 				else if(index < goodsList.size() && (0 == currentPage || 1 == currentPage)){
 					detail = goodsList.get(index);
 				}
 				
 				if(null != detail){
 					GoodDetailView detailView = new GoodDetailView(detail, getContext(), bundle);
 					detailView.setInfoChangeListener(m_viewInfoListener);
 					m_viewInfoListener.onNewView(detailView);
 				}
 			}
 		});
 		
 		lvGoodsList.setOnRefreshListener(this);	
 		lvGoodsList.setOnGetMoreListener(this);
 	}
 	
 	@Override
 	protected void onAttachedToWindow(){
 		super.onAttachedToWindow();
 		
 		this.rebuildPage(false);
 	}
 	
 	@Override
 	public void onPause(){
 		if(adapter != null){
 			adapter.setHasDelBtn(false);
 			adapter.notifyDataSetChanged();
 		}
 		buttonStatus = -1;
 	}
 
 	private void init(){
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		View v = inflater.inflate(R.layout.personalcenterview, null);
 		this.addView(v);
 		
 		try {
 			if (Util.JadgeConnection(this.getContext()) == false) {
 				Toast.makeText(this.getContext(), "网络连接异常", 3).show();
 //				isConnect = 0;
 				myHandler.sendEmptyMessage(MCMESSAGE_NETWORKERROR);
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		lvGoodsList = (PullToRefreshListView) findViewById(R.id.lvGoodsList);
 
 		ivMyads = (ImageView) findViewById(R.id.ivMyads);
 		ivMyfav = (ImageView) findViewById(R.id.ivMyfav);
 		ivMyhistory = (ImageView) findViewById(R.id.ivMyhistory);
 
 //		lvGoodsList.setDivider(null);
 		lvGoodsList.setOnScrollListener(this);
 
 		ivMyads.setOnClickListener(this);
 		ivMyfav.setOnClickListener(this);
 		ivMyhistory.setOnClickListener(this);
 		adapter = new GoodsListAdapter(this.getContext(), this.listMyPost);
 		adapter.setMessageOutOnDelete(myHandler, MCMESSAGE_DELETE);
 		lvGoodsList.setAdapter(adapter);
 	}
 	
 	class UpdateAndGetmoreThread implements Runnable{
 		private int currentPage = -1;
 		private boolean update = false;
 		
 		public UpdateAndGetmoreThread(int currentPage, boolean update_){
 			this.currentPage = currentPage;
 			this.update = update_;
 		}
 		
 		@Override
 		public void run(){
 			String apiName = "ad_list";
 			ArrayList<String>list = new ArrayList<String>();
 			int msgToSend = -1;
 			int msgToSendOnFail = -1;
 			
 			boolean needUpdateOrGetmore = true;
 			if(currentPage == -1){
 				list.add("query=userId:" + user.getId() + " AND status:0");
 				if(update){
 					list.add("start=0");
 					msgToSend = MCMESSAGE_MYPOST_SUCCESS;
 					msgToSendOnFail = MCMESSAGE_MYPOST_FAIL;
 				}
 //				else{
 //					list.add("start="+PersonalCenterView.this.listMyPost.size());
 //					msgToSend = MCMESSAGE_MYPOST_GETMORE_SUCCESS;
 //					msgToSendOnFail = MCMESSAGE_MYPOST_GETMORE_FAIL;
 //				}
 			}
 			else{
 				List<GoodsDetail> details = null;
 				if(currentPage == 0){
 					details = QuanleimuApplication.getApplication().getListMyStore();
 					if(update){		
 						list.add("start=0");
 						msgToSend = MCMESSAGE_MYFAV_UPDATE_SUCCESS;
 						msgToSendOnFail = MCMESSAGE_MYFAV_UPDATE_FAIL;
 					}
 //					else{
 //						list.add("start="+PersonalCenterView.this.goodsList.size());
 //						msgToSend = MCMESSAGE_MYFAV_GETMORE_SUCCESS;
 //						msgToSendOnFail = MCMESSAGE_MYFAV_GETMORE_FAIL;						
 //					}
 				}
 				else if(currentPage == 1){
 					details = QuanleimuApplication.getApplication().getListLookHistory();
 					if(update){
 						list.add("start=0");
 						msgToSend = MCMESSAGE_MYHISTORY_UPDATE_SUCCESS;
 						msgToSendOnFail = MCMESSAGE_MYHISTORY_UPDATE_FAIL;	
 					}
 //					else{
 //						list.add("start="+PersonalCenterView.this.goodsList.size());
 //						msgToSend = MCMESSAGE_MYHISTORY_GETMORE_SUCCESS;
 //						msgToSendOnFail = MCMESSAGE_MYHISTORY_GETMORE_FAIL;								
 //					}
 				}
 				if(details != null && details.size() > 0){
 					String ids = "id:" + details.get(0).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID);
 					for(int i = 1; i < details.size(); ++ i){
 						ids += " OR " + "id:" + details.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID);  
 					}
 					list.add("query=(" + ids + ")");
 				}
 				else{
 					needUpdateOrGetmore = false;
 				}
 			}
 			
 			list.add("rt=1");	
 			list.add("rows=30");
 			
 			if(needUpdateOrGetmore){
 				String url = Communication.getApiUrl(apiName, list);
 				try {
 					json = Communication.getDataByUrl(url);
 					if (json != null) {
 						myHandler.sendEmptyMessage(msgToSend);
 					} else {
 						myHandler.sendEmptyMessage(msgToSendOnFail);
 					}
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 					myHandler.sendEmptyMessage(MCMESSAGE_NETWORKERROR);
 				}	
 			}else{
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				if(update){
 					myHandler.sendEmptyMessage(MCMESSAGE_MYFAV_UPDATE_NOTNECESSARY);
 				}else{
 					myHandler.sendEmptyMessage(MCMESSAGE_MYFAV_GETMORE_NOTNECESSARY);
 				}
 			}
 		}
 	}
 
 	Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MCMESSAGE_MYPOST_SUCCESS:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				GoodsList gl = JsonUtil.getGoodsListFromJson(json); 
 				if (gl == null || gl.getCount() == 0) {
 //					Toast.makeText(PersonalCenterView.this.getContext(), "您尚未发布信息，", 0).show();
 //					TODO:how to check if delay occurred or there's really no info
 					listMyPost.clear();
 				}
 				else{
 					listMyPost = gl.getData();
 				}
 				QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 				rebuildPage(true);
 				lvGoodsList.onRefreshComplete();
 				break;
 //			case MCMESSAGE_MYPOST_GETMORE_SUCCESS:
 //				if (pd != null) {
 //					pd.dismiss();
 //				}
 //				GoodsList glGetMore = JsonUtil.getGoodsListFromJson(json); 
 //				if (glGetMore == null || glGetMore.getCount() == 0) {
 //					lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_NO_MORE);
 //				}
 //				else{
 //					List<GoodsDetail> listData = glGetMore.getData();
 //					for (int i = 0; i < listData.size(); i++) {
 //						listMyPost.add(listData.get(i));
 //					}
 //					lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_OK);
 //					QuanleimuApplication.getApplication().setListMyPost(listMyPost);					
 //				}
 //				break;				
 			case MCMESSAGE_MYFAV_UPDATE_SUCCESS:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				GoodsList glFav = JsonUtil.getGoodsListFromJson(json); 
 				if (glFav != null && glFav.getCount() > 0) {
 					QuanleimuApplication.getApplication().setListMyStore(glFav.getData());
 					rebuildPage(true);
 				}
 				
 				lvGoodsList.onRefreshComplete();
 				
 				break;
 //			case MCMESSAGE_MYFAV_GETMORE_SUCCESS:
 //				if (pd != null) {
 //					pd.dismiss();
 //				}
 //				GoodsList glFavGetMore = JsonUtil.getGoodsListFromJson(json); 
 //				if (glFavGetMore != null && glFavGetMore.getCount() > 0) {
 //					List<GoodsDetail> listData = glFavGetMore.getData();
 //					for (int i = 0; i < listData.size(); i++) {
 //						goodsList.add(listData.get(i));
 //					}	
 //					lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_OK);
 //					QuanleimuApplication.getApplication().setListMyStore(goodsList);					
 //					
 //				}else{
 //					lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_NO_MORE);
 //				}
 //			
 //				break;
 			case MCMESSAGE_MYHISTORY_UPDATE_SUCCESS:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				
 				GoodsList glHistory = JsonUtil.getGoodsListFromJson(json); 
 				if (glHistory != null && glHistory.getCount() > 0) {
 					QuanleimuApplication.getApplication().setListLookHistory(glHistory.getData());
 					rebuildPage(true);
 				}	
 				
 				lvGoodsList.onRefreshComplete();
 				
 				break;
 //			case MCMESSAGE_MYHISTORY_GETMORE_SUCCESS:
 //				if (pd != null) {
 //					pd.dismiss();
 //				}
 //				
 //				GoodsList glHistoryGetmore = JsonUtil.getGoodsListFromJson(json); 
 //				if (glHistoryGetmore != null && glHistoryGetmore.getCount() > 0) {
 //					List<GoodsDetail> listData = glHistoryGetmore.getData();
 //					for (int i = 0; i < listData.size(); i++) {
 //						goodsList.add(listData.get(i));
 //					}	
 //					lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_OK);
 //					QuanleimuApplication.getApplication().setListLookHistory(goodsList);
 //				}else{
 //					lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_NO_MORE);
 //				}
 //				
 //				lvGoodsList.onRefreshComplete();
 //				
 //				break;
 			case MCMESSAGE_MYPOST_FAIL:
 			case MCMESSAGE_MYFAV_UPDATE_FAIL:
 			case MCMESSAGE_MYHISTORY_UPDATE_FAIL:
 //			case MCMESSAGE_MYPOST_GETMORE_FAIL:
 //			case MCMESSAGE_MYFAV_GETMORE_FAIL:
 //			case MCMESSAGE_MYHISTORY_GETMORE_FAIL:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				Toast.makeText(PersonalCenterView.this.getContext(), "数据获取失败，请检查网络连接后重试！", 3).show();
 				lvGoodsList.onRefreshComplete();
 				break;
 			case MCMESSAGE_MYFAV_UPDATE_NOTNECESSARY:
 				PersonalCenterView.this.lvGoodsList.onRefreshComplete();
 				break;
 			case MCMESSAGE_MYFAV_GETMORE_NOTNECESSARY:
 				PersonalCenterView.this.lvGoodsList.onGetMoreCompleted(PullToRefreshListView.E_GETMORE.E_GETMORE_NO_MORE);
 				break;
 			case MCMESSAGE_DELETE:
 				int pos = msg.arg2;
 				if(PersonalCenterView.this.currentPage == -1){
 					pd = ProgressDialog.show(PersonalCenterView.this.getContext(), "提示", "请稍候...");
 					pd.setCancelable(true);
 					pd.show();
 
 					new Thread(new MyMessageDeleteThread(pos)).start();
 				}
 				else if(0 == PersonalCenterView.this.currentPage){
 					goodsList.remove(pos);
 					QuanleimuApplication.getApplication().setListMyStore(goodsList);
 					Helper.saveDataToLocate(PersonalCenterView.this.getContext(), "listMyStore", goodsList);
 					adapter.setList(goodsList);
 					adapter.notifyDataSetChanged();
 					adapter.setUiHold(false);
 				}
 				else if(1 == PersonalCenterView.this.currentPage){
 					goodsList.remove(pos);
 					QuanleimuApplication.getApplication().setListLookHistory(goodsList);
 					Helper.saveDataToLocate(PersonalCenterView.this.getContext(), "listLookHistory", goodsList);
 					adapter.setList(goodsList);
 					adapter.notifyDataSetChanged();			
 					adapter.setUiHold(false);
 				}
 				break;
 			case MCMESSAGE_DELETEALL:
 				if(PersonalCenterView.this.currentPage != -1){
					if(null == goodsList) break;
 					goodsList.clear();
 					QuanleimuApplication.getApplication().setListMyStore(goodsList);
 					Helper.saveDataToLocate(PersonalCenterView.this.getContext(), "listMyStore", goodsList);
 					adapter.setList(goodsList);
 					adapter.notifyDataSetChanged();
 					
 					if(PersonalCenterView.this.m_viewInfoListener != null){
 						TitleDef title = getTitleDef();
 						title.m_rightActionHint = "编辑";
 						title.m_leftActionHint = "设置";
 						m_viewInfoListener.onTitleChanged(title);
 					}
 					adapter.setHasDelBtn(false);
 					buttonStatus = -1;
 				}
 				break;
 			case MCMESSAGE_DELETE_SUCCESS:
 				if(pd != null){
 					pd.dismiss();
 				}
 				int pos2 = msg.arg2;
 				try {
 					JSONObject jb = new JSONObject(json);
 					JSONObject js = jb.getJSONObject("error");
 					String message = js.getString("message");
 					int code = js.getInt("code");
 					if (code == 0) {
 						// 删除成功
 						listMyPost.remove(pos2);
 						QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 						adapter.setList(listMyPost);
 						adapter.notifyDataSetChanged();
 						Toast.makeText(PersonalCenterView.this.getContext(), message, 0).show();
 					} else {
 						// 删除失败
 						Toast.makeText(PersonalCenterView.this.getContext(), "删除失败,请稍后重试！", 0).show();
 					}
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				adapter.setUiHold(false);
 				break;
 			case MCMESSAGE_DELETE_FAIL:
 				if(pd != null){
 					pd.dismiss();
 				}
 
 				Toast.makeText(PersonalCenterView.this.getContext(), "删除失败，请稍后重试！", 0).show();
 				adapter.setUiHold(false);
 				break;
 			case MCMESSAGE_NETWORKERROR:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				Toast.makeText(PersonalCenterView.this.getContext(), "网络连接失败，请检查设置！", 3).show();
 				
 				lvGoodsList.onRefreshComplete();
 				
 				break;
 			}
 			super.handleMessage(msg);
 		}
 	};
 
 	class MyMessageDeleteThread implements Runnable {
 		private int position;
 
 		public MyMessageDeleteThread(int position) {
 			this.position = position;
 		}
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			json = "";
 			String apiName = "ad_delete";
 			ArrayList<String> list = new ArrayList<String>();
 			list.add("mobile=" + mobile);
 			String password1 = Communication.getMD5(password);
 			password1 += Communication.apiSecret;
 			String userToken = Communication.getMD5(password1);
 			list.add("userToken=" + userToken);
 			list.add("adId=" + listMyPost.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url);
 				if (json != null) {
 					Message msg = myHandler.obtainMessage();
 					msg.arg2 = position;
 					msg.what = MCMESSAGE_DELETE_SUCCESS;
 					myHandler.sendMessage(msg);
 					// myHandler.sendEmptyMessageDelayed(5, 3000);5
 				} else {
 					myHandler.sendEmptyMessage(MCMESSAGE_DELETE_FAIL);
 				} 
 
 			} catch (UnsupportedEncodingException e) {
 				myHandler.sendEmptyMessage(MCMESSAGE_NETWORKERROR);
 			} catch (IOException e) {
 				myHandler.sendEmptyMessage(MCMESSAGE_NETWORKERROR);
 			}
 		}
 	}
 	
 	@Override
 	public boolean onLeftActionPressed(){
 		if(currentPage != -1 && 0 == buttonStatus){
 			myHandler.sendEmptyMessage(MCMESSAGE_DELETEALL);
 		}else{
 			m_viewInfoListener.onNewView(new SetMainView(getContext()));
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onRightActionPressed(){
 		if(-1 == buttonStatus){
 //		btnEdit.setBackgroundResource(R.drawable.btn_clearall);
 			if(this.m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_rightActionHint = "完成";
 				if(currentPage != -1){
 					title.m_leftActionHint = "清空";
 				}
 				m_viewInfoListener.onTitleChanged(title);
 			}
 			if(adapter != null){
 				adapter.setHasDelBtn(true);
 			}
 			buttonStatus = 0;
 		}
 		else{
 //			btnEdit.setBackgroundResource(R.drawable.btn_search);
 			if(this.m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_rightActionHint = "编辑";
 //				title.m_leftActionHint = "更新";
 				m_viewInfoListener.onTitleChanged(title);
 			}
 			adapter.setHasDelBtn(false);
 			buttonStatus = -1;
 		}
 		if(adapter != null)
 		{
 			adapter.notifyDataSetChanged();
 		}		
 		return true;
 	}
 	
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.ivMyads:
 			this.currentPage = -1;
 			buttonStatus = -1;
 			adapter.setHasDelBtn(false);
 			rebuildPage(false);
 			break;
 		case R.id.ivMyfav:
 			buttonStatus = -1;
 			adapter.setHasDelBtn(false);
 			
 			this.currentPage = 0;
 			rebuildPage(false);
 			break;
 		case R.id.ivMyhistory:
 			buttonStatus = -1;
 			adapter.setHasDelBtn(false);
 			
 			this.currentPage = 1;
 			rebuildPage(false);
 			break;
 		default:
 			break;
 		}
 //		super.onClick(v);
 	}
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		if(scrollState == SCROLL_STATE_IDLE)
 		{
 //			LoadImage.doTask();
 		}
 		
 	}
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_leftActionHint = "设置";
 		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
 		title.m_rightActionHint = "编辑";
 		title.m_title = "个人中心";
 		title.m_visible = true;
 		return title;
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = true;
 		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MINE;
 		return tab;
 	}
 
 	@Override
 	public void onRefresh() {
 		new Thread(new UpdateAndGetmoreThread(currentPage, true)).start();		
 	}
 
 	@Override
 	public void onGetMore() {
 		// TODO Auto-generated method stub
 		//new Thread(new UpdateAndGetmoreThread(currentPage, false)).start();		
 	}
 	
 }
