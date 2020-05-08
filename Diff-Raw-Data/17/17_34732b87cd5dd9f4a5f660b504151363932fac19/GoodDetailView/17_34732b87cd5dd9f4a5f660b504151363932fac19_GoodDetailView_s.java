 package com.quanleimu.view;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter; 
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.Helper;
 import com.quanleimu.util.Util;
 import com.quanleimu.view.BaseView;
 import com.quanleimu.activity.BaseActivity;
 import com.quanleimu.activity.R;
 import com.quanleimu.activity.BaiduMapActivity;
 
 import android.net.Uri;
 import android.content.Intent;
 
 public class GoodDetailView extends BaseView implements DialogInterface.OnClickListener, View.OnClickListener{
 	final private String strCollect = "收藏";
 	final private String strCancelCollect = "取消收藏";
 	final private String strManager = "管理";
 //	final private int msgShowMap = 1;
 	final private int msgCancelMap = 2;
 	final private int msgRefresh = 5;
 	final private int msgUpdate = 6;
 	final private int msgDelete = 7;
 
 	// 定义控件
 	public MainAdapter adapter;
 
 	// 定义变量
 	private LinearLayout ll_meta;
 	private TextView txt_tittle;
 	private TextView txt_message1;
 	private RelativeLayout rl_phone, rl_address, llgl;
 	private TextView txt_phone, txt_address;
 	private ImageView im_x;
 
 	public GoodsDetail detail = new GoodsDetail();
 	public Gallery glDetail;
 	public List<Bitmap> listBm = new ArrayList<Bitmap>();
 	public List<Bitmap> listBigBm = new ArrayList<Bitmap>();
 	public String mycenter_type = "";
 	
 	private String json = "";
 	
 	enum REQUEST_TYPE{
 		REQUEST_TYPE_REFRESH,
 		REQUEST_TYPE_UPDATE,
 		REQUEST_TYPE_DELETE
 	}
 	
 	public GoodDetailView(GoodsDetail detail, Context content, Bundle bundle){
 		super(content, bundle);
 		this.detail = detail;
 		init();
 	}
 	
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 	
 	@Override
 	protected void onAttachedToWindow(){
 		if(isMyAd()){
 			if(this.m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_rightActionHint = strManager;
 				m_viewInfoListener.onTitleChanged(title);
 				btnStatus = 1;
 			}
 		}
 		else{
 			if(isInMyStore()){
 				if(this.m_viewInfoListener != null){
 					TitleDef title = getTitleDef();
 					title.m_rightActionHint = strCancelCollect;
 					m_viewInfoListener.onTitleChanged(title);
 					btnStatus = 0;
 				}
 			}
 			else{
 				if(this.m_viewInfoListener != null){
 					TitleDef title = getTitleDef();
 					title.m_rightActionHint = strCollect;
 					m_viewInfoListener.onTitleChanged(title);
 					btnStatus = -1;
 				}
 			}
 		}
 		this.saveToHistory();
 		super.onAttachedToWindow();
 	}
 	
 	private void saveToHistory(){
 		List<GoodsDetail> listLookHistory = QuanleimuApplication.getApplication().getListLookHistory();
 		if(listLookHistory != null){
 			for(int i=0;i<listLookHistory.size();i++)
 			{
 				if(listLookHistory.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 						.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)))
 				{
 					return;
 				}
 			}
 		}
 		if(null == listLookHistory){
 			listLookHistory = new ArrayList<GoodsDetail>();
 		}
 		listLookHistory.add(detail);
 		QuanleimuApplication.getApplication().setListLookHistory(listLookHistory);
 		Helper.saveDataToLocate(this.getContext(), "listLookHistory", listLookHistory);		
 	}
 
 	private boolean isMyAd(){
 		if(detail == null) return false;
 		List<GoodsDetail> myPost = QuanleimuApplication.getApplication().getListMyPost();
 		for(int i = 0; i < myPost.size(); ++ i){
 			if(myPost.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 					.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
 				return true;
 			}
 		}
 		return false;
 	}
 	private boolean isInMyStore(){
 		if(detail == null) return false;
 		List<GoodsDetail> myStore = QuanleimuApplication.getApplication().getListMyStore();
 		if(myStore == null) return false;
 		for(int i = 0; i < myStore.size(); ++ i){
 			if(myStore.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 					.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
 				return true;
 			}
 		}
 		return false;		
 	}
 	
 	protected void init() {
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		View v = inflater.inflate(R.layout.gooddetailview, null);
 		this.addView(v);
 		
 		if(detail.getImageList() != null){
 			String b = (detail.getImageList().getResize180()).substring(1, (detail.getImageList().getResize180()).length()-1);
 			b = Communication.replace(b);
 			List<String> listUrl = new ArrayList<String>();
 			String[] c = b.split(",");
 			for(int i=0;i<c.length;i++) 
 			{
 				listUrl.add(c[i]);
 			}
 			if(listUrl.size() == 0){
 				llgl = (RelativeLayout) findViewById(R.id.llgl);
 				llgl.setVisibility(View.GONE);
 			}else{
 				glDetail = (Gallery) findViewById(R.id.glDetail);
 				glDetail.setFadingEdgeLength(10);
 				glDetail.setSpacing(40);
 				
 				adapter = new MainAdapter(this.getContext(), listUrl);
 				glDetail.setAdapter(adapter);
 				
 				glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 					@Override
 					public void onItemClick(AdapterView<?> arg0, View arg1,
 							int arg2, long arg3) {
 						Bundle bundle = new Bundle();
 						bundle.putInt("postIndex", arg2);
 						bundle.putSerializable("goodsDetail", detail);
 						
 						if(null != m_viewInfoListener){
 							m_viewInfoListener.onNewView(new BigGalleryView(getContext(), bundle));
 						}
 					}
 				});
 			}
 		}else{
 			llgl = (RelativeLayout) findViewById(R.id.llgl);
 			llgl.setVisibility(View.GONE);
 		}
 //		rl_test = (RelativeLayout) findViewById(R.id.detailLayout);
 		llgl = (RelativeLayout) findViewById(R.id.llgl);
 
 		txt_tittle = (TextView) findViewById(R.id.goods_tittle);
 		txt_message1 = (TextView) findViewById(R.id.sendmess1);
 		txt_phone = (TextView) findViewById(R.id.address1);
 		txt_address = (TextView) findViewById(R.id.address2);
 		rl_phone = (RelativeLayout) findViewById(R.id.showphone);
 		rl_address = (RelativeLayout) findViewById(R.id.showmap);
 		im_x = (ImageView) findViewById(R.id.ivCancel);
 
 		ll_meta = (LinearLayout) findViewById(R.id.meta);
 
 
 
 		im_x.setOnClickListener(this);
 
 		this.setMetaObject();
 		
 		txt_message1.setText(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DESCRIPTION));
 		txt_tittle.setText(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_TITLE));
 
 		String areaNamesV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
 		if (areaNamesV != null && !areaNamesV.equals("")) 
 		{
 			txt_address.setText(areaNamesV);
 			
 			String latV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT);
 			String lonV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON);
 			if(latV != null && !latV.equals("false") && !latV.equals("") && lonV != null && !lonV.equals("false") && !lonV.equals(""))
 			{
 				rl_address.setOnClickListener(this);
 			}
 			else
 			{
 				rl_address.setBackgroundResource(R.drawable.iv_bg_unclickable);
 			}
 		} 
 		else 
 		{
 			txt_address.setText("无");
 			rl_address.setBackgroundResource(R.drawable.iv_bg_unclickable);
 		}
 
 		String mobileV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_MOBILE);
 		if (mobileV != null
 				&& !mobileV.equals("")
 				&& !mobileV.equals("无")) {
 			txt_phone.setText(mobileV);
 			rl_phone.setOnClickListener(this);
 		} else {
 			rl_phone.setVisibility(View.GONE);
 //			txt_phone.setText("无");
 //			rl_phone.setBackgroundResource(R.drawable.iv_bg_unclickable);
 		}
 	}
 	private int btnStatus = -1;//-1:strCollect, 0: strCancelCollect, 1:strManager
 	private void handleStoreBtnClicked(){
 		if(-1 == btnStatus){
 			btnStatus = 0;
 			List<GoodsDetail> myStore = QuanleimuApplication.getApplication().getListMyStore();
 			
 			TitleDef title = getTitleDef();
 			title.m_rightActionHint = strCancelCollect;
 			m_viewInfoListener.onTitleChanged(title);
 			
 			if (myStore == null){
 				myStore = new ArrayList<GoodsDetail>();
 				myStore.add(detail);
 			} else {
 				if (myStore.size() >= 100) {
 					myStore.remove(0);
 				}
 				myStore.add(detail);
 			}		
 			QuanleimuApplication.getApplication().setListMyStore(myStore);
 			Helper.saveDataToLocate(this.getContext(), "listMyStore", myStore);
 			Toast.makeText(this.getContext(), "收藏成功", 3).show();
 		}
 		else if (0 == btnStatus) {
 			btnStatus = -1;
 			List<GoodsDetail> myStore = QuanleimuApplication.getApplication().getListMyStore();
 			for (int i = 0; i < myStore.size(); i++) {
 				if (detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 						.equals(myStore.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))) {
 					myStore.remove(i);
 					break;
 				}
 			}
 			QuanleimuApplication.getApplication().setListMyStore(myStore);
 			Helper.saveDataToLocate(this.getContext(), "listMyStore", myStore);
 			TitleDef title = getTitleDef();
 			title.m_rightActionHint = strCollect;
 			m_viewInfoListener.onTitleChanged(title);
 			Toast.makeText(this.getContext(), "取消收藏", 3).show();
 		}
 		else if(1 == btnStatus){
 			final String[] names = {"编辑","刷新","删除"};
 			new AlertDialog.Builder(this.getContext()).setTitle("选择操作")
 					.setItems(names, new DialogInterface.OnClickListener(){
 						public void onClick(DialogInterface dialog, int which){
 							switch(which){
 								case 0:
 //									Bundle bundle = new Bundle();
 //									bundle.putSerializable("goodsDetail", detail);
 //									bundle.putString("categoryEnglishName",detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME));
 //									intent.putExtras(bundle);									
 //									intent.setClass(GoodDetail.this, PostGoods.class);
 //									startActivity(intent);									
 //									dialog.dismiss();
 									break;
 								case 1:
 									pd = ProgressDialog.show(GoodDetailView.this.getContext(), "提示", "请稍候...");
 									pd.setCancelable(true);
 									new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_REFRESH)).start();
 									dialog.dismiss();
 									break;									
 								case 2:
 									pd = ProgressDialog.show(GoodDetailView.this.getContext(), "提示", "请稍候...");
 									pd.setCancelable(true);
 									new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_DELETE)).start();
 									dialog.dismiss();
 									break;
 								default:
 									break;
 							}
 						}
 					})
 					.setNegativeButton(
 				     "取消", new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.cancel();							
 						}
 					}).show();
 		}
 	}
 	
 	@Override
 	public boolean onRightActionPressed(){
 		handleStoreBtnClicked();
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.showphone:
 			final String[] names = {"打电话","发短信"};
 			new AlertDialog.Builder(this.getContext()).setTitle("选择联系方式")
 					.setItems(names, this)
 					.setNegativeButton(
 				     "取消", new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.cancel();							
 						}
 					})
 				     .show();
 			break;
 		case R.id.showmap:
 			String latV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT);
 			String lonV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON);
 			if(latV != null && !latV.equals("false") && !latV.equals("") && lonV != null && !lonV.equals("false") && !lonV.equals(""))
 			{
 				double lat = Double.valueOf(latV);
 				double lon = Double.valueOf(lonV);
 				String positions = Integer.toString((int)(lat*1E6)) + "," + Integer.toString((int)(lon*1E6));
 				Bundle bundle = new Bundle();
 				bundle.putString("detailPosition", positions);
 				
 				//TODO:
 				BaseActivity baseActivity = (BaseActivity)getContext();
 				baseActivity.getIntent().putExtras(bundle);
 				
 				baseActivity.getIntent().setClass(baseActivity, BaiduMapActivity.class);
 				baseActivity.startActivity(baseActivity.getIntent());
 			}
 			else
 			{
 				rl_address.setBackgroundResource(R.drawable.iv_bg_unclickable);
 			}
 			break;
 		case R.id.ivCancel:
 			myHandler.sendEmptyMessage(msgCancelMap);
 			break;
 		}
 //		super.onClick(v);
 	}
 	
 	private void setMetaObject(){
 		if(ll_meta == null) return;
 		ll_meta.removeAllViews();
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		for (int i = 0; i < detail.getMetaData().size(); i++) {
 			View v = null;
 			v = inflater.inflate(R.layout.item_meta, null);
 
 			TextView tvmetatxt = (TextView) v.findViewById(R.id.tvmetatxt);
 			TextView tvmeta = (TextView) v.findViewById(R.id.tvmeta);
 
 			tvmetatxt.setText(detail.getMetaData().get(i).split(" ")[0].toString() + "：");
 			tvmeta.setText(detail.getMetaData().get(i).split(" ")[1].toString());
 			v.setTag(i);
 			ll_meta.addView(v);
 		}
 		Date date = new Date(Long.parseLong(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DATE)) * 1000);
 		SimpleDateFormat df = new SimpleDateFormat("MM月dd日 HH:mm:ss",
 				Locale.SIMPLIFIED_CHINESE);
 		String strTime = df.format(date);
 		View time = inflater.inflate(R.layout.item_meta, null);
 		TextView timetxt = (TextView) time.findViewById(R.id.tvmetatxt);
 		TextView timevalue = (TextView) time.findViewById(R.id.tvmeta);
 		timetxt.setText("更新时间： ");
 		timevalue.setText(strTime);
 		ll_meta.addView(time);
 	}
 	
 	@Override
 	public void onClick(DialogInterface dialog, int which){
 		if(0 == which){
 			Uri uri = Uri.parse("tel:" + txt_phone.getText().toString());
 			Intent intent = new Intent(Intent.ACTION_DIAL, uri);
 			this.getContext().startActivity(intent);
 		}
 		else if(1 == which){
 			Uri uri = Uri.parse("smsto:" + txt_phone.getText().toString());
 			Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
 			this.getContext().startActivity(intent);
 		}
 	}
 
 	public Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case msgRefresh:
 				if(json == null){
 					Toast.makeText(GoodDetailView.this.getContext(), "刷新失败，请稍后重试！", 0).show();
 					break;
 				}
 				try {
 					JSONObject jb = new JSONObject(json);
 					JSONObject js = jb.getJSONObject("error");
 					String message = js.getString("message");
 					int code = js.getInt("code");
 					if (code == 0) {
 						new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_UPDATE)).start();
 						Toast.makeText(GoodDetailView.this.getContext(), message, 0).show();
 					}else if(2 == code){
 						if(pd != null){
 							pd.dismiss();
 						}
 						new AlertDialog.Builder(GoodDetailView.this.getContext()).setTitle("提醒")
 						.setMessage(message)
 						.setPositiveButton("确定", new DialogInterface.OnClickListener() {							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								// TODO Auto-generated method stub
 								pd = ProgressDialog.show(GoodDetailView.this.getContext(), "提示", "请稍候...");
 								pd.setCancelable(true);
 
 								new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_REFRESH, 1)).start();
 								dialog.dismiss();
 							}
 						})
 						.setNegativeButton(
 					     "取消", new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								dialog.cancel();							
 							}
 						})
 					     .show();
 
 					}else {
 						if(pd != null){
 							pd.dismiss();
 						}
 						Toast.makeText(GoodDetailView.this.getContext(), message, 0).show();
 					}
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				break;			
 			case msgUpdate:
 				if(pd!=null){
 					pd.dismiss();
 				}
 				GoodsList goods = JsonUtil.getGoodsListFromJson(json);
 				List<GoodsDetail> goodsDetails = goods.getData();
 				if(goodsDetails != null && goodsDetails.size() > 0){
 					for(int i = 0; i < goodsDetails.size(); ++ i){
 						if(goodsDetails.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 								.equals(GoodDetailView.this.detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
 							GoodDetailView.this.detail = goodsDetails.get(i);
 							break;
 						}
 					}
 					List<GoodsDetail>listMyPost = QuanleimuApplication.getApplication().getListMyPost();
 					if(listMyPost != null){
 						for(int i = 0; i < listMyPost.size(); ++ i){
 							if(listMyPost.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 									.equals(GoodDetailView.this.detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
 								listMyPost.set(i, GoodDetailView.this.detail);
 								break;
 							}
 						}
 					}
 					QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 				}
 
 				setMetaObject();
 				break;
 			case msgDelete:
 				if(pd!=null){
 					pd.dismiss();
 				}
 				try {
 					JSONObject jb = new JSONObject(json);
 					JSONObject js = jb.getJSONObject("error");
 					String message = js.getString("message");
 					int code = js.getInt("code");
 					if (code == 0) {
 						// 删除成功
 						List<GoodsDetail> listMyPost = QuanleimuApplication.getApplication().getListMyPost();
 						for(int i = 0; i < listMyPost.size(); ++ i){
 							if(listMyPost.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
 									.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
 								listMyPost.remove(i);
 								break;
 							}
 						}
 //						listMyPost.remove(pos);
 						QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 						if(m_viewInfoListener != null){
 							m_viewInfoListener.onBack();
 						}
 //						finish();
 						Toast.makeText(GoodDetailView.this.getContext(), message, 0).show();
 					} else {
 						// 删除失败
 						Toast.makeText(GoodDetailView.this.getContext(), "删除失败,请稍后重试！", 0).show();
 					}
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				break;
 			default:
 				break;
 			}
 			super.handleMessage(msg);
 		}
 	};
 
 	class RequestThread implements Runnable{
 		private REQUEST_TYPE type;
 		private int pay = 0;
 		public RequestThread(REQUEST_TYPE type){
 			this.type = type;
 		}
 		public RequestThread(REQUEST_TYPE type, int pay) {
 			this.type = type;
 			this.pay = pay;
 		}
 		@Override
 		public void run(){
 			synchronized(GoodDetailView.this){
 				ArrayList<String> requests = null;
 				String apiName = null;
 				int msgToSend = -1;
 				if(REQUEST_TYPE.REQUEST_TYPE_DELETE == type){
 					requests = doDelete();
 					apiName = "ad_delete";
 					msgToSend = msgDelete;
 				}
 				else if(REQUEST_TYPE.REQUEST_TYPE_REFRESH == type){
 					requests = doRefresh(this.pay);
 					apiName = "ad_refresh";
 					msgToSend = msgRefresh;
 				}
 				else if(REQUEST_TYPE.REQUEST_TYPE_UPDATE == type){
 					requests = doUpdate();
 					apiName = "ad_list";
 					msgToSend = msgUpdate;
 				}
 				if(requests != null){
 					String url = Communication.getApiUrl(apiName, requests);
 					System.out.println("url--->" + url);
 					try {
 						json = Communication.getDataByUrl(url);
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					myHandler.sendEmptyMessage(msgToSend);
 				}
 			}
 		}
 	}
 	
 	private ArrayList<String> doRefresh(int pay){
 		json = "";
 		ArrayList<String> list = new ArrayList<String>();
 
 		UserBean user = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
 		String mobile = user.getPhone();
 		String password = user.getPassword();
 
 		list.add("mobile=" + mobile);
 		String password1 = Communication.getMD5(password);
 		password1 += Communication.apiSecret;
 		String userToken = Communication.getMD5(password1);
 		list.add("userToken=" + userToken);
 		list.add("adId=" + detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 		list.add("rt=1");
 		if(pay != 0){
 			list.add("pay=1");
 		}
 
 		return list;
 	}
 	
 	private ArrayList<String> doUpdate(){
 		json = "";
 		ArrayList<String> list = new ArrayList<String>();
 		
 		UserBean user = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
 		String mobile = user.getPhone();
 		String password = user.getPassword();
 
 		list.add("mobile=" + mobile);
 		String password1 = Communication.getMD5(password);
 		password1 += Communication.apiSecret;
 		String userToken = Communication.getMD5(password1);
 		list.add("userToken=" + userToken);
 		list.add("query=id:" + detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 		list.add("rt=1");
 		return list;		
 	}
 	
 	private ArrayList<String> doDelete(){
 		// TODO Auto-generated method stub
 		UserBean user = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
 		String mobile = user.getPhone();
 		String password = user.getPassword();
 
 		json = "";
 //		String apiName = "ad_delete";
 		ArrayList<String> list = new ArrayList<String>();
 		list.add("mobile=" + mobile);
 		String password1 = Communication.getMD5(password);
 		password1 += Communication.apiSecret;
 		String userToken = Communication.getMD5(password1);
 		list.add("userToken=" + userToken);
 		list.add("adId=" + detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 		list.add("rt=1");
 		
 		return list;		
 	}
 
 	class MainAdapter extends BaseAdapter {
 		Context context;
 		List<String> listUrl;
 
 		public MainAdapter(Context context, List<String> listUrl) {
 			this.context = context;
 			this.listUrl = listUrl;
 		}
 
 		@Override
 		public int getCount() {
 			return listUrl.size();
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = LayoutInflater.from(context);
 			View v = null;
 //			if (convertView != null) {
 //				v = (ImageView) convertView;
 //			} else {
 				v = inflater.inflate(R.layout.item_detailview, null);
 //			}
 			ImageView iv = (ImageView) v.findViewById(R.id.ivGoods);
 			
 			BitmapFactory.Options o =  new BitmapFactory.Options();
             o.inPurgeable = true;
 			Bitmap tmb = BitmapFactory.decodeResource(context.getResources(),R.drawable.moren1, o);
 			Bitmap mb= Helper.toRoundCorner(tmb, 20);
 			tmb.recycle();
 			
 			
 			Bitmap tmb1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.moren, o);
 			Bitmap mb1= Helper.toRoundCorner(tmb1, 20);
 			tmb1.recycle();
 			
 			iv.setImageBitmap(mb);
 			
 			WindowManager wm = 
 					(WindowManager)QuanleimuApplication.getApplication().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
 			int type = wm.getDefaultDisplay().getWidth();
 
 			if (type == 240) {
 				iv.setLayoutParams(new Gallery.LayoutParams(86, 86));
 			} else if (type == 320) {
 				iv.setLayoutParams(new Gallery.LayoutParams(145, 145));
 			} else if (type == 480) {
 				iv.setLayoutParams(new Gallery.LayoutParams(210, 210));
 			} else if (type == 540) {
 				iv.setLayoutParams(new Gallery.LayoutParams(235, 235));
 			} else if (type == 640) {
 				iv.setLayoutParams(new Gallery.LayoutParams(240, 240));
 			}else{
 				iv.setLayoutParams(new Gallery.LayoutParams(245,245));
 			}
 
 			
 			if (listUrl.size() != 0 && listUrl.get(position) != null) {
 				iv.setTag(listUrl.get(position));
 				SimpleImageLoader.showImg(iv, listUrl.get(position), GoodDetailView.this.getContext());
 			} else {
 				iv.setImageBitmap(mb1);
 			}
 			return iv;
 		}
 	}
 	
 	@Override
 	public boolean onLeftActionPressed(){
 		return false;
 	}
 	
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_leftActionHint = "后退";
 		title.m_rightActionHint = "收藏";
 		title.m_title = "详细信息";
 		title.m_visible = true;
 		return title;
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = false;
 		return tab;
 	}
 	
 }
