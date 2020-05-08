 package com.quanleimu.view;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsDetail.EDATAKEYS;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ErrorHandler;
 import com.quanleimu.util.GoodsListLoader;
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
 	private final int MCMESSAGE_DELETE = 5;
 	private final int MSG_DELETE_POST_SUCCESS = 6;
 	private final int MSG_DELETE_POST_FAIL = 7;
 	private final int MSG_RESTORE_POST_SUCCESS = 8;
 	private final int MSG_RESTORE_POST_FAIL = 9;
 
 	public PullToRefreshListView lvGoodsList;
 	public ImageView ivMyads, ivMyfav, ivMyhistory;
 
 	private List<GoodsDetail> listMyPost = null;
 	private List<GoodsDetail> listInVerify = null;
 	private List<GoodsDetail> listDeleted = null;
 	
 	public GoodsListAdapter adapter = null;
 //	private String json;
 	UserBean user;
 	private int currentPage = -1;//-1:mypost, 0:inverify, 1:deleted
 	private Bundle bundle;
 	private int buttonStatus = -1;//-1:edit 0:finish
 	private GoodsListLoader glLoader = null;
 	
 	private String json = "";
 	
 	public PersonalPostView(Context context, Bundle bundle){
 		super(context, bundle);
 		this.bundle = bundle;
 		init();
 	}
 
 	private void rebuildPage(boolean onResult){
 		if(glLoader != null){
 			glLoader.setHandler(myHandler);
 		}
 		LinearLayout lView = (LinearLayout)this.findViewById(R.id.linearListView);
 		
 		if(-1 == currentPage){
 			ivMyads.setImageResource(R.drawable.bg_segment_sent_selected);
 			ivMyfav.setImageResource(R.drawable.bg_segment_approving);
 			ivMyhistory.setImageResource(R.drawable.bg_segment_deleted);
 			if(m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_title = "已发布的信息";
 				m_viewInfoListener.onTitleChanged(title);
 			}
 			GoodsList gl = new GoodsList();
 			gl.setData(listMyPost);
 			glLoader.setGoodsList(gl);
 			adapter.setList(listMyPost);
 			adapter.notifyDataSetChanged();	
 		}
 		else if(0 == currentPage){
 			lvGoodsList.setVisibility(View.VISIBLE);
 			ivMyads.setImageResource(R.drawable.bg_segment_sent);
 			ivMyfav.setImageResource(R.drawable.bg_segment_approving_selected);
 			ivMyhistory.setImageResource(R.drawable.bg_segment_deleted);
 			
 			if(m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_title = "审核中";
 				title.m_rightActionHint = "编辑";
 				m_viewInfoListener.onTitleChanged(title);
 			}
 			if(listInVerify == null){
 				pd = ProgressDialog.show(PersonalPostView.this.getContext(), "提示", "请稍候...");
 				pd.setCancelable(true);
 				pd.show();
 				this.onRefresh();
 			}
 			else{
 				adapter.setList(listInVerify);
 				adapter.notifyDataSetChanged();
 				GoodsList gl = new GoodsList();
 				gl.setData(listInVerify);
 				glLoader.setGoodsList(gl);
 
 			}
 		}
 		else{
 			lvGoodsList.setVisibility(View.VISIBLE);
 			ivMyads.setImageResource(R.drawable.bg_segment_sent);
 			ivMyfav.setImageResource(R.drawable.bg_segment_approving);
 			ivMyhistory.setImageResource(R.drawable.bg_segment_deleted_selected);
 			if(m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_title = "已删除";
 				title.m_rightActionHint = "编辑";
 				m_viewInfoListener.onTitleChanged(title);
 			}
 			if(listDeleted == null){
 				pd = ProgressDialog.show(PersonalPostView.this.getContext(), "提示", "请稍候...");
 				pd.setCancelable(true);
 				pd.show();
 				this.onRefresh();
 			}
 			else{
 				adapter.setList(listDeleted);
 				adapter.notifyDataSetChanged();
 			}
 		}		
 
 		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {			
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				final int index = arg2 - lvGoodsList.getHeaderViewsCount();
 				if(index < 0)
 					return;
 				
 				if(currentPage == -1 && null != listMyPost && index < listMyPost.size() ){
 					m_viewInfoListener.onNewView(new GoodDetailView(getContext(), bundle, glLoader, index, null));
 				}
 				else if(null !=  listInVerify && index < listInVerify.size() && 0 == currentPage){
 					m_viewInfoListener.onNewView(new GoodDetailView(getContext(), bundle, glLoader, index, null));
 					////TODO...... new verify view
 				}
 				else if(null != listDeleted && index < listDeleted.size() && 1 == currentPage){
 					final String[] names = {"彻底删除", "恢复"};
 					new AlertDialog.Builder(PersonalPostView.this.getContext()).setTitle("选择操作")
 					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO Auto-generated method stub
 							dialog.dismiss();
 						}
 					})
 					.setItems(names, new DialogInterface.OnClickListener(){
 						public void onClick(DialogInterface dialog, int which){
 							switch(which){
 								case 0:
 									String id = listDeleted.get(index).getValueByKey(EDATAKEYS.EDATAKEYS_ID);
 									pd = ProgressDialog.show(PersonalPostView.this.getContext(), "提示", "请稍候...");
 									pd.show();
 									(new Thread(new MyMessageDeleteThread(id))).start();
 									break;
 								case 1:
 									String id2 = listDeleted.get(index).getValueByKey(EDATAKEYS.EDATAKEYS_ID);
 									pd = ProgressDialog.show(PersonalPostView.this.getContext(), "提示", "请稍候...");
 									pd.show();
 									(new Thread(new MyMessageRestoreThread(id2))).start();
 									break;
 							}
 						}
 					}).show();
 
 				}
 			}
 		});
		
 		lvGoodsList.setOnRefreshListener(this);	
 		lvGoodsList.setSelectionFromHeader(glLoader.getSelection());
 	}
 	
 	@Override
 	protected void onAttachedToWindow(){
 		super.onAttachedToWindow();
 		user = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
 		this.rebuildPage(false);
 	}
 	
 	@Override
 	public void onPause(){
 		super.onPause();
 		
 		if(adapter != null){
 			adapter.setHasDelBtn(false);
 			adapter.notifyDataSetChanged();
 		}
 		
 		buttonStatus = -1;
 		
 		for(int i = 0; i < lvGoodsList.getChildCount(); ++i){
 			ImageView imageView = (ImageView)lvGoodsList.getChildAt(i).findViewById(R.id.ivInfo);
 			
 			if(	null != imageView	
 					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
 					/*&& null != imageView.getDrawable()
 					&& imageView.getDrawable() instanceof AnimationDrawable*/){
 				SimpleImageLoader.Cancel(imageView.getTag().toString(), imageView);
 			}
 		}		
 	}
 
 	@Override
 	public void onResume(){
 		super.onResume();
 		
 		for(int i = 0; i < lvGoodsList.getChildCount(); ++i){
 			ImageView imageView = (ImageView)lvGoodsList.getChildAt(i).findViewById(R.id.ivInfo);
 			
 			if(	null != imageView	
 					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
 					/*&& null != imageView.getDrawable()
 					&& imageView.getDrawable() instanceof AnimationDrawable*/){
 				SimpleImageLoader.showImg(imageView, imageView.getTag().toString(), getContext());
 			}
 		}
 		
 		lvGoodsList.setSelectionFromHeader(glLoader.getSelection());
 	}	
 	
 	private void init(){
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		View v = inflater.inflate(R.layout.personalcenterview, null);
 		this.addView(v);
 		
 		try {
 			if (Util.JadgeConnection(this.getContext()) == false) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
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
 
 		GoodsList gl = new GoodsList();
 		gl.setData(listMyPost == null ? new ArrayList<GoodsDetail>() : listMyPost);
 	
 		glLoader = new GoodsListLoader(null, myHandler, null, null);
 		glLoader.setHasMore(false);
 		glLoader.setGoodsList(gl);
 	}
 
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
 				if (gl == null || gl.getData().size() == 0) {
 					if(msg.what == MSG_MYPOST) {
 						if(null != listMyPost) listMyPost.clear();
 					}
 					else if(msg.what == MSG_INVERIFY) {
 						if(listInVerify == null){
 							listInVerify = new ArrayList<GoodsDetail>();
 						}
 						listInVerify.clear();
 					}
 					else if(msg.what == MSG_DELETED){
 						if(listDeleted == null){
 							listDeleted = new ArrayList<GoodsDetail>();
 						}
 						listDeleted.clear();
 					}
 					glLoader.setGoodsList(new GoodsList());
 				}
 				else{
 					if(msg.what == MSG_MYPOST){
 						listMyPost = gl.getData();
 						if(listMyPost != null){
 							for(int i = listMyPost.size() - 1; i >= 0; -- i){
 								if(!listMyPost.get(i).getValueByKey("status").equals("0")){
 									listMyPost.remove(i);
 								}
 							}
 						}
 						GoodsList gl2 = new GoodsList();
 						gl2.setData(listMyPost);
 						glLoader.setGoodsList(gl2);
 					}
 					else if(msg.what == MSG_INVERIFY) {
 						listInVerify = gl.getData();
 						if(listInVerify != null){
 							for(int i = listInVerify.size() - 1; i >= 0; -- i){
 								if(!listInVerify.get(i).getValueByKey("status").equals("4")){
 									listInVerify.remove(i);
 								}
 							}
 						}
 						GoodsList gl2 = new GoodsList();
 						gl2.setData(listInVerify);
 						glLoader.setGoodsList(gl2);
 					}
 					else if(msg.what == MSG_DELETED){
 						listDeleted = gl.getData();
 						glLoader.setGoodsList(gl);
 					}
 				}
 				if(msg.what == MSG_MYPOST){
 					QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 				}
 				rebuildPage(true);
 				lvGoodsList.onRefreshComplete();
 				break;
 			case GoodsListLoader.MSG_EXCEPTION:{
 				if(pd != null){
 					pd.dismiss();
 				}
 				lvGoodsList.onRefreshComplete();
 				break;
 			}
 
 			case MCMESSAGE_DELETE:
 				int pos = msg.arg2;
 //				pos = pos - lvGoodsList.getHeaderViewsCount();
 				String id = glLoader.getGoodsList().getData().get(pos).getValueByKey(EDATAKEYS.EDATAKEYS_ID);
 				pd = ProgressDialog.show(PersonalPostView.this.getContext(), "提示", "请稍候...");
 				new Thread(new MyMessageDeleteThread(id)).start();
 				break;
 			case MSG_DELETE_POST_FAIL:
 				if(pd != null){
 					pd.dismiss();
 				}
 				Toast.makeText(PersonalPostView.this.getContext(), "删除失败,请稍后重试！", 0).show();
 				break;
 			case MSG_DELETE_POST_SUCCESS:
 				if(pd != null){
 					pd.dismiss();
 				}
 				Object deletedId = msg.obj;
 				try {
 					JSONObject jb = new JSONObject(json);
 					JSONObject js = jb.getJSONObject("error");
 					String message = js.getString("message");
 					int code = js.getInt("code");
 					List<GoodsDetail> refList = null;
 					if(msg.arg1 == -1){
 						refList = listMyPost;
 					}
 					else if(msg.arg1 == 0){
 						refList = listInVerify;
 					}
 					else if(msg.arg1 == 1){
 						refList = listDeleted;
 					}
 					if(refList == null) break;
 					if (code == 0) {
 						for(int i = 0; i < refList.size(); ++ i){
 							if(refList.get(i).getValueByKey(EDATAKEYS.EDATAKEYS_ID).equals((String)deletedId)){
 								refList.remove(i);
 								break;
 							}
 						}
 						if(msg.arg1 == -1){
 							QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 						}
 						adapter.setList(refList);
 						adapter.notifyDataSetChanged();
 						Toast.makeText(PersonalPostView.this.getContext(), message, 0).show();
 					} else {
 						Toast.makeText(PersonalPostView.this.getContext(), "删除失败,请稍后重试！", 0).show();
 					}
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				adapter.setUiHold(false);
 				break;	
 			case MSG_RESTORE_POST_FAIL:
 				if(pd != null){
 					pd.dismiss();
 				}
 				Toast.makeText(PersonalPostView.this.getContext(), "恢复失败,请稍后重试！", 0).show();
 				break;
 			case MSG_RESTORE_POST_SUCCESS:
 				if(pd != null){
 					pd.dismiss();
 				}
 				if(listDeleted == null) break;
 				try{
 					JSONObject jb = new JSONObject(json);
 					JSONObject js = jb.getJSONObject("error");
 					String message = js.getString("message");
 					int code = js.getInt("code");
 					if(code == 0){
 						for(int i = 0; i < listDeleted.size(); ++ i){
 							if(listDeleted.get(i).getValueByKey(EDATAKEYS.EDATAKEYS_ID).equals((String)msg.obj)){
 								listDeleted.remove(i);
 								break;
 							}
 						}
 						if(currentPage == 1){
 							adapter.setList(listDeleted);
 							adapter.notifyDataSetChanged();
 						}
 						Toast.makeText(PersonalPostView.this.getContext(), message, 0).show();
 					}
 					else{
 						Toast.makeText(PersonalPostView.this.getContext(), "恢复失败,请稍后重试！", 0).show();
 					}
 				}catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				break;
 			case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				
 				Message msg2 = Message.obtain();
 				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 				
 				lvGoodsList.onFail();
 				
 				break;
 			}
 //			super.handleMessage(msg);
 		}
 	};
 	
 	class MyMessageRestoreThread implements Runnable{
 		private String id;
 		public MyMessageRestoreThread(String id){
 			this.id = id;
 		}
 		
 		@Override
 		public void run(){
 			if(user == null)return;
 			json = "";
 			String apiName = "ad_undelete";
 			ArrayList<String> list = new ArrayList<String>();
 			list.add("mobile=" + user.getPhone());
 			String password1 = Communication.getMD5(user.getPassword());
 			password1 += Communication.apiSecret;
 			String userToken = Communication.getMD5(password1);
 			list.add("userToken=" + userToken);
 			list.add("adId=" + id);
 			list.add("rt=1");
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url);
 				if (json != null) {
 					Message msg = myHandler.obtainMessage();
 					msg.obj = id;
 					msg.what = MSG_RESTORE_POST_SUCCESS;
 					myHandler.sendMessage(msg);
 				} else {
 					myHandler.sendEmptyMessage(MSG_RESTORE_POST_FAIL);
 				} 
 				return;
 
 			} catch (UnsupportedEncodingException e) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 			} catch (IOException e) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 			} catch (Communication.BXHttpException e){
 				
 			}
 			if(pd != null){
 				pd.dismiss();
 			}
 		}
 	}
 
 	class MyMessageDeleteThread implements Runnable {
 		private String id;
 		private int currengPage = -1;
 
 		public MyMessageDeleteThread(String id){
 			this.id = id;
 			this.currengPage = PersonalPostView.this.currentPage;
 		}
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			if(user == null)return;
 			json = "";
 			String apiName = "ad_delete";
 			ArrayList<String> list = new ArrayList<String>();
 			list.add("mobile=" + user.getPhone());
 			String password1 = Communication.getMD5(user.getPassword());
 			password1 += Communication.apiSecret;
 			String userToken = Communication.getMD5(password1);
 			list.add("userToken=" + userToken);
 			list.add("adId=" + id);
 			list.add("rt=1");
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url);
 				if (json != null) {
 					Message msg = myHandler.obtainMessage();
 					msg.obj = id;
 					msg.arg1 = currengPage;
 					msg.what = MSG_DELETE_POST_SUCCESS;
 					myHandler.sendMessage(msg);
 				} else {
 					myHandler.sendEmptyMessage(MSG_DELETE_POST_FAIL);
 				} 
 				return;
 
 			} catch (UnsupportedEncodingException e) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 			} catch (IOException e) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 			} catch (Communication.BXHttpException e){
 				
 			}
 			if(pd != null){
 				pd.dismiss();
 			}
 		}
 	}
 
 	@Override
 	public boolean onRightActionPressed(){
 		if(-1 == buttonStatus){
 //		btnEdit.setBackgroundResource(R.drawable.btn_clearall);
 			if(this.m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_rightActionHint = "完成";
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
 		title.m_leftActionHint = "返回";
 		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
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
 	
 	@Override
 	public void onPreviousViewBack(int message, Object obj){
 		if(GoodDetailView.MSG_ADINVERIFY_DELETED == message){
 			if(obj != null){
 				if(this.listInVerify != null){
 					for(int i = 0; i < listInVerify.size(); ++ i){
 						if(listInVerify.get(i).getValueByKey(EDATAKEYS.EDATAKEYS_ID).equals((String)obj)){
 							listInVerify.remove(i);
 							break;
 						}
 					}
 					if(currentPage == 0){
 						adapter.setList(listInVerify);
 						adapter.notifyDataSetChanged();
 					}
 				}
 			}
 		}else if(GoodDetailView.MSG_MYPOST_DELETED == message){
 			if(glLoader.getGoodsList() != null 
 					&& glLoader.getGoodsList().getData() != null 
 					&& glLoader.getGoodsList().getData().size() > 0){
 				if(QuanleimuApplication.getApplication().getListMyPost() == null ||
 						QuanleimuApplication.getApplication().getListMyPost().size() != glLoader.getGoodsList().getData().size()){
 					GoodsList gl = new GoodsList();
 					gl.setData(QuanleimuApplication.getApplication().getListMyPost());
 					glLoader.setGoodsList(gl);
 					adapter.setList(QuanleimuApplication.getApplication().getListMyPost());
 					adapter.notifyDataSetChanged();
 				}
 			}
 		}
 	}
 }
