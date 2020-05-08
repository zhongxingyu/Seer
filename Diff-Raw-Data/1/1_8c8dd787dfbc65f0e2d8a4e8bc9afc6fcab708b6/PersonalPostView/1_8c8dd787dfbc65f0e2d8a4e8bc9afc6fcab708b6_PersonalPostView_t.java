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
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.GoodsListLoader;
 import com.quanleimu.util.Helper;
 import com.quanleimu.util.Util;
 import com.quanleimu.adapter.GoodsListAdapter;
 import com.quanleimu.view.BaseView;
 import com.quanleimu.widget.PullToRefreshListView;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import android.widget.LinearLayout;
 public class PersonalPostView extends BaseView implements View.OnClickListener, PullToRefreshListView.OnRefreshListener{
 	private final int MSG_MYPOST = 1;
 	private final int MSG_INVERIFY = 2;
 	private final int MSG_DELETED = 3;
 	private final int MCMESSAGE_NETWORKERROR = 4;
 	private final int MCMESSAGE_DELETE = 5;
 
 	public PullToRefreshListView lvGoodsList;
 	public ImageView ivMyads, ivMyfav, ivMyhistory;
 
 	private List<GoodsDetail> listMyPost = new ArrayList<GoodsDetail>();
 	private List<GoodsDetail> listInVerify = new ArrayList<GoodsDetail>();
 	private List<GoodsDetail> listDeleted = new ArrayList<GoodsDetail>();
 	
 	public GoodsListAdapter adapter = null;
 //	private String json;
 	UserBean user;
 	private int currentPage = -1;//-1:mypost, 0:inverify, 1:deleted
 	private Bundle bundle;
 	private int buttonStatus = -1;//-1:edit 0:finish
 	private GoodsListLoader glLoader = null;
 	
 	public PersonalPostView(Context context, Bundle bundle){
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
 				title.m_title = "已发布的信息";
 				m_viewInfoListener.onTitleChanged(title);
 			}
 			adapter.setList(listMyPost);
 			adapter.notifyDataSetChanged();	
 		}
 		else if(0 == currentPage){
 			lvGoodsList.setVisibility(View.VISIBLE);
 			ivMyads.setImageResource(R.drawable.btn_posted_normal);
 			ivMyfav.setImageResource(R.drawable.btn_fav_press);
 			ivMyhistory.setImageResource(R.drawable.btn_history_normal);
 			
 			if(m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_title = "审核中";
 				title.m_rightActionHint = "编辑";
 				m_viewInfoListener.onTitleChanged(title);
 			}
 			adapter.setList(listInVerify);
 			adapter.notifyDataSetChanged();
 		}
 		else{
 			lvGoodsList.setVisibility(View.VISIBLE);
 			ivMyads.setImageResource(R.drawable.btn_posted_normal);
 			ivMyfav.setImageResource(R.drawable.btn_fav_normal);
 			ivMyhistory.setImageResource(R.drawable.btn_history_press);
 			if(m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_title = "已删除";
 				title.m_rightActionHint = "编辑";
 				m_viewInfoListener.onTitleChanged(title);
 			}
 			adapter.setList(listDeleted);
 			adapter.notifyDataSetChanged();
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
 					m_viewInfoListener.onNewView(new GoodDetailView(getContext(), bundle, glLoader, index));
 				}
 				else if(null !=  listInVerify && index < listInVerify.size() && 0 == currentPage){
 //					detail = goodsList.get(index);
 					////TODO...... new verify view
 				}
 			}
 		});
 		
 		lvGoodsList.setOnRefreshListener(this);	
 	}
 	
 	@Override
 	protected void onAttachedToWindow(){
 		super.onAttachedToWindow();
 		user = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
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
 
 	@Override
 	public void onResume(){
 		for(int i = 0; i < lvGoodsList.getChildCount(); ++i){
 			ImageView imageView = (ImageView)lvGoodsList.getChildAt(i).findViewById(R.id.ivInfo);
 			
 			if(	null != imageView	
 					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
 					/*&& null != imageView.getDrawable()
 					&& imageView.getDrawable() instanceof AnimationDrawable*/){
 				SimpleImageLoader.showImg(imageView, imageView.getTag().toString(), getContext());
 			}
 		}
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
 
 		ivMyads.setOnClickListener(this);
 		ivMyfav.setOnClickListener(this);
 		ivMyhistory.setOnClickListener(this);
		listMyPost = QuanleimuApplication.getApplication().getListMyPost();
 		adapter = new GoodsListAdapter(this.getContext(), this.listMyPost);
 		adapter.setMessageOutOnDelete(myHandler, MCMESSAGE_DELETE);
 		lvGoodsList.setAdapter(adapter);
 		
 		glLoader = new GoodsListLoader(null, myHandler, null, null);
 		glLoader.setHasMore(false);
 	}
 	
 //	class GetPersonalAds implements Runnable{
 //		private int currentPage = -1;
 //		public GetPersonalAds(int currentPage){
 //			this.currentPage = currentPage;
 //		}
 //		
 //		@Override
 //		public void run(){
 //			String apiName = "ad_list";
 //			ArrayList<String>list = new ArrayList<String>();
 //			list.add("start=0");
 //			if(currentPage == -1){
 //				list.add("query=userId:" + user.getId() + " AND status:0");
 //			}
 //			else if(currentPage == 0){
 //				list.add("query=userId:" + user.getId() + " AND status:4");
 //			}
 //			else if(currentPage == 1){
 //				list.add("query=userId:" + user.getId() + " AND status:3");
 //			}
 //			list.add("rt=1");	
 //			
 //			String url = Communication.getApiUrl(apiName, list);
 //			glLoader.setUrl(url);
 //			int msg = (currentPage == -1) ? MSG_MYPOST : (this.currentPage == 0 ? MSG_INVERIFY : MSG_DELETED);
 //			glLoader.startFetching(true, msg, msg, msg);
 //		}
 //	}
 
 	Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_MYPOST:
 			case MSG_INVERIFY:
 			case MSG_DELETED:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				GoodsList gl = JsonUtil.getGoodsListFromJson(glLoader.getLastJson()); 
 				if (gl == null || gl.getCount() == 0) {
 					if(msg.what == MSG_MYPOST) listMyPost.clear();
 					else if(msg.what == MSG_INVERIFY) listInVerify.clear();
 					else if(msg.what == MSG_DELETED) listDeleted.clear();
 					glLoader.setGoodsList(new GoodsList());
 				}
 				else{
 					if(msg.what == MSG_MYPOST) listMyPost = gl.getData();
 					else if(msg.what == MSG_INVERIFY) listInVerify = gl.getData();
 					else if(msg.what == MSG_DELETED) listDeleted = gl.getData();
 					glLoader.setGoodsList(gl);
 				}
 				QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 				rebuildPage(true);
 				lvGoodsList.onRefreshComplete();
 				break;
 			case GoodsListLoader.MSG_EXCEPTION:{
 				if(pd != null){
 					pd.dismiss();
 				}
 			}
 
 //			case MCMESSAGE_DELETE:
 //				int pos = msg.arg2;
 //				if(PersonalPostView.this.currentPage == -1){
 //					pd = ProgressDialog.show(PersonalCenterView.this.getContext(), "提示", "请稍候...");
 //					pd.setCancelable(true);
 //					pd.show();
 //
 //					new Thread(new MyMessageDeleteThread(pos)).start();
 //				}
 //				else if(0 == PersonalCenterView.this.currentPage){
 //					goodsList.remove(pos);
 //					QuanleimuApplication.getApplication().setListMyStore(goodsList);
 //					Helper.saveDataToLocate(PersonalCenterView.this.getContext(), "listMyStore", goodsList);
 //					adapter.setList(goodsList);
 //					adapter.notifyDataSetChanged();
 //					adapter.setUiHold(false);
 //				}
 //				else if(1 == PersonalCenterView.this.currentPage){
 //					goodsList.remove(pos);
 //					QuanleimuApplication.getApplication().setListLookHistory(goodsList);
 //					Helper.saveDataToLocate(PersonalCenterView.this.getContext(), "listLookHistory", goodsList);
 //					adapter.setList(goodsList);
 //					adapter.notifyDataSetChanged();			
 //					adapter.setUiHold(false);
 //				}
 //				break;
 //
 //			case MCMESSAGE_DELETE_SUCCESS:
 //				if(pd != null){
 //					pd.dismiss();
 //				}
 //				int pos2 = msg.arg2;
 //				try {
 //					JSONObject jb = new JSONObject(json);
 //					JSONObject js = jb.getJSONObject("error");
 //					String message = js.getString("message");
 //					int code = js.getInt("code");
 //					if (code == 0) {
 //						// 删除成功
 //						listMyPost.remove(pos2);
 //						QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 //						adapter.setList(listMyPost);
 //						adapter.notifyDataSetChanged();
 //						Toast.makeText(PersonalCenterView.this.getContext(), message, 0).show();
 //					} else {
 //						// 删除失败
 //						Toast.makeText(PersonalCenterView.this.getContext(), "删除失败,请稍后重试！", 0).show();
 //					}
 //				} catch (JSONException e) {
 //					// TODO Auto-generated catch block
 //					e.printStackTrace();
 //				}
 //				adapter.setUiHold(false);
 //				break;			
 //			case MCMESSAGE_NETWORKERROR:
 //				if (pd != null) {
 //					pd.dismiss();
 //				}
 //				Toast.makeText(PersonalPostView.this.getContext(), "网络连接失败，请检查设置！", 3).show();
 //				
 //				lvGoodsList.onRefreshComplete();
 //				
 //				break;
 			}
 			super.handleMessage(msg);
 		}
 	};
 
 //	class MyMessageDeleteThread implements Runnable {
 //		private int position;
 //
 //		public MyMessageDeleteThread(int position) {
 //			this.position = position;
 //		}
 //
 //		@Override
 //		public void run() {
 //			// TODO Auto-generated method stub
 //			json = "";
 //			String apiName = "ad_delete";
 //			ArrayList<String> list = new ArrayList<String>();
 //			list.add("mobile=" + mobile);
 //			String password1 = Communication.getMD5(password);
 //			password1 += Communication.apiSecret;
 //			String userToken = Communication.getMD5(password1);
 //			list.add("userToken=" + userToken);
 //			list.add("adId=" + listMyPost.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 //
 //			String url = Communication.getApiUrl(apiName, list);
 //			try {
 //				json = Communication.getDataByUrl(url);
 //				if (json != null) {
 //					Message msg = myHandler.obtainMessage();
 //					msg.arg2 = position;
 //					msg.what = MCMESSAGE_DELETE_SUCCESS;
 //					myHandler.sendMessage(msg);
 //					// myHandler.sendEmptyMessageDelayed(5, 3000);5
 //				} else {
 //					myHandler.sendEmptyMessage(MCMESSAGE_DELETE_FAIL);
 //				} 
 //
 //			} catch (UnsupportedEncodingException e) {
 //				myHandler.sendEmptyMessage(MCMESSAGE_NETWORKERROR);
 //			} catch (IOException e) {
 //				myHandler.sendEmptyMessage(MCMESSAGE_NETWORKERROR);
 //			} catch (Communication.BXHttpException e){
 //				
 //			}
 //		}
 //	}
 //	
 	@Override
 	public boolean onLeftActionPressed(){
 		if(currentPage != -1 && 0 == buttonStatus){
 //			myHandler.sendEmptyMessage(MCMESSAGE_DELETEALL);
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
 		List<String> params = new ArrayList<String>();
 		if(currentPage == -1){
 			params.add("query=userId:" + user.getId() + " AND status:0");
 		}
 		else if(currentPage == 0){
 			params.add("query=userId:" + user.getId() + " AND status:4");
 			params.add("activeOnly=0");
 		}
 		else if(currentPage == 1){
 			params.add("query=userId:" + user.getId() + " AND status:3");
 			params.add("activeOnly=0");
 		}
 //		list.add("rt=1");	
 		
 		glLoader.setParams(params);
 		int msg = (currentPage == -1) ? MSG_MYPOST : (this.currentPage == 0 ? MSG_INVERIFY : MSG_DELETED);
 		glLoader.startFetching(true, msg, msg, msg);
 	}
 }
