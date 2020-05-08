 //liuchong@baixing.com
 package com.baixing.view.fragment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Message;
 import android.support.v4.view.ViewPager;
 import android.text.ClipboardManager;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.baixing.activity.BaseFragment;
 import com.baixing.adapter.VadImageAdapter;
 import com.baixing.android.api.ApiError;
 import com.baixing.android.api.ApiParams;
 import com.baixing.android.api.cmd.BaseCommand;
 import com.baixing.android.api.cmd.BaseCommand.Callback;
 import com.baixing.android.api.cmd.HttpGetCommand;
 import com.baixing.data.GlobalDataManager;
 import com.baixing.entity.Ad;
 import com.baixing.entity.Ad.EDATAKEYS;
 import com.baixing.entity.AdList;
 import com.baixing.entity.UserBean;
 import com.baixing.imageCache.ImageCacheManager;
 import com.baixing.imageCache.ImageLoaderManager;
 import com.baixing.jsonutil.JsonUtil;
 import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
 import com.baixing.util.Communication;
 import com.baixing.util.ErrorHandler;
 import com.baixing.util.TextUtil;
 import com.baixing.util.Util;
 import com.baixing.util.VadListLoader;
 import com.baixing.util.ViewUtil;
 import com.baixing.view.AdViewHistory;
 import com.baixing.view.vad.VadLogger;
 import com.baixing.view.vad.VadPageController;
 import com.baixing.view.vad.VadPageController.ActionCallback;
 import com.baixing.widget.ContextMenuItem;
 import com.quanleimu.activity.R;
 
 public class VadFragment extends BaseFragment implements View.OnTouchListener,View.OnClickListener, OnItemSelectedListener, VadListLoader.HasMoreListener, VadListLoader.Callback, ActionCallback, Callback {
 
 	public interface IListHolder{
 		public void startFecthingMore();
 		public boolean onResult(int msg, VadListLoader loader);//return true if getMore succeeded, else otherwise
 	};
 	
 //	private static int NETWORK_REQ_DELETE = 1;
 //	private static int NETWORK_REQ_REFRESH = 2;
 //	private static int NETWORK_REQ_UPDATE = 3;
 	
 	private static final int MSG_REFRESH = 5;
 	private static final int MSG_UPDATE = 6;
 	private static final int MSG_DELETE = 7;
 	private static final int MSG_LOAD_AD_EVENT = 8;
 	public static final int MSG_ADINVERIFY_DELETED = 0x00010000;
 	public static final int MSG_MYPOST_DELETED = 0x00010001;
 
 	public Ad detail = new Ad();
 	private String json = "";
 	
 //	private WeakReference<Bitmap> mb_loading = null;
 	
 	private boolean keepSilent = false;
 	
 	private VadListLoader mListLoader;
 	
 	private IListHolder mHolder = null;
 	
 	private VadPageController pageController;
 	
 	List<View> pages = new ArrayList<View>();
 	
 	enum REQUEST_TYPE{
 
 		REQUEST_TYPE_REFRESH(MSG_REFRESH, "ad_refresh"),
 		REQUEST_TYPE_UPDATE(MSG_UPDATE, "ad_list"),
 		REQUEST_TYPE_DELETE(MSG_DELETE, "ad_delete");
 		public int reqCode;
 		public String apiName;
 		REQUEST_TYPE(int requestCode, String apiName) {
 			this.reqCode = requestCode;
 			this.apiName = apiName;
 		}
 	}
 	
 	@Override
 	public void onDestroy(){
 		this.keepSilent = true;
 		
 //		Thread t = new Thread(new Runnable(){
 //			public void run(){
 //				try{
 //					Thread.sleep(2000);
 //					if(mb_loading != null && mb_loading.get() != null){
 //						mb_loading.get().recycle();
 //						mb_loading = null;
 //					}
 //				}catch(Exception e){
 //					e.printStackTrace();
 //				}
 //			}
 //		});
 //		t.start();
 	
 		super.onDestroy();
 	}
 	
 	@Override
 	public boolean handleBack(){
 		this.keepSilent = false;
 
 		return false;
 	}
 	
 	@Override
 	public void onPause() {
 		this.keepSilent = true;
 		super.onPause();
 		pages.clear();
 	}
 	
 	@Override
 	public void onResume(){
 		
 		VadLogger.trackPageView(detail, getAppContext());
 		
 		this.keepSilent = false;
 		super.onResume();
 	}
 	
 	private boolean isMyAd(){
 		if(detail == null) return false;
 		return GlobalDataManager.getInstance().isMyAd(detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID));		
 	}
 	
 	private boolean isInMyStore(){
 		if(detail == null) return false;
 		return GlobalDataManager.getInstance().isFav(detail);
 	}
 //	
 	public boolean onTouch (View v, MotionEvent event){
 	    switch (event.getAction()) {
 	    case MotionEvent.ACTION_DOWN:
 	    case MotionEvent.ACTION_MOVE: 
 	    	if(getView() != null && getView().findViewById(R.id.svDetail) != null){
 	    		((ViewPager)getView().findViewById(R.id.svDetail)).requestDisallowInterceptTouchEvent(true);
 	    	}
 	        break;
 	    case MotionEvent.ACTION_OUTSIDE:
 	    case MotionEvent.ACTION_UP:
 	    	if(getView() != null && getView().findViewById(R.id.svDetail) != null){
 	    		((ViewPager)getView().findViewById(R.id.svDetail)).requestDisallowInterceptTouchEvent(false);
 	    	}
 	        break;		
 	    }
 		return this.keepSilent;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.mListLoader = (VadListLoader) getArguments().getSerializable("loader");
 		int index = getArguments().getInt("index", 0);
 		if(mListLoader == null 
 				|| mListLoader.getGoodsList() == null 
 				|| mListLoader.getGoodsList().getData() == null
 				|| mListLoader.getGoodsList().getData().size() <= index){
 			return;
 		}
 		detail = mListLoader.getGoodsList().getData().get(index);
 		if (savedInstanceState != null) //
 		{
 //			this.mListLoader.setHandler(handler);
 			this.mListLoader.setHasMoreListener(this);
 		}
 		
 	}
 	
 	@Override
 	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		if(detail == null || mListLoader == null) return null;
 		final int originalSelect = getArguments().getInt("index", 0);
 		this.keepSilent = false;//magic flag to refuse unexpected touch event
 		
 		final View v = inflater.inflate(R.layout.gooddetailview, null);
 		
 		pageController = new VadPageController(v, detail, this, originalSelect);
 		
 		BitmapFactory.Options o =  new BitmapFactory.Options();
         o.inPurgeable = true;
 //        mb_loading = new WeakReference<Bitmap>(BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.icon_vad_loading, o));
         
         mListLoader.setSelection(originalSelect);
         mListLoader.setCallback(this);       
         
         return v;
 	}
 	
 	private void notifyPageDataChange(boolean hasMore)
 	{
 		if(keepSilent) return;
 		pageController.resetLoadingPage(hasMore);
 	}
 	
 	private void updateContactBar(View rootView, boolean forceHide)
 	{
 		AdViewHistory.getInstance().markRead(detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
 		
 		if (!detail.isValidMessage() && !forceHide)
 		{
 			String tips = detail.getValueByKey("tips"); 
 			if(tips == null || tips.equals("")){
 				tips  = "该信息不符合《百姓网公约》";
 			}
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			AlertDialog dialog = builder.setTitle(R.string.dialog_title_info)
 			.setMessage(tips)
 			.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					postDelete(true, new OnCancelListener() {
 						
 						@Override
 						public void onCancel(DialogInterface dialog) {
 							finishFragment();
 						}
 					});
 				}
 			})
 			.setPositiveButton(R.string.appeal, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					VadLogger.trackMofifyEvent(detail, BxEvent.MYVIEWAD_APPEAL);
 					Bundle bundle = createArguments("申诉", null);
 					bundle.putInt("type", 1);
 					bundle.putString("adId", detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
 					pushAndFinish(new FeedbackFragment(), bundle);
 				}
 			}).create();
 			dialog.setCanceledOnTouchOutside(false);
 			dialog.setOnCancelListener(new OnCancelListener() {
 				public void onCancel(DialogInterface dialog) {
 					finishFragment();
 				}
 			});
 			dialog.show();
 		}
 		
 		LinearLayout rl_phone = (LinearLayout)rootView.findViewById(R.id.phonelayout);
 		if (forceHide)
 		{
 			rl_phone.setVisibility(View.GONE);
 			return;
 		}
 		else if (isMyAd() || !detail.isValidMessage())
 		{
 			rootView.findViewById(R.id.phone_parent).setVisibility(View.GONE);
 			rootView.findViewById(R.id.vad_tool_bar).setVisibility(View.VISIBLE);
 			
 			
 			rootView.findViewById(R.id.vad_btn_edit).setOnClickListener(this);
 			rootView.findViewById(R.id.vad_btn_refresh).setOnClickListener(this);
 			rootView.findViewById(R.id.vad_btn_delete).setOnClickListener(this);
 			rootView.findViewById(R.id.vad_btn_forward).setOnClickListener(this);
 			
 			if (!detail.isValidMessage())
 			{
 				rootView.findViewById(R.id.vad_btn_edit).setVisibility(View.GONE);
 				rootView.findViewById(R.id.vad_btn_refresh).setVisibility(View.GONE);
 			}
 			return;
 		}
 		
 		
 		rootView.findViewById(R.id.phone_parent).setVisibility(View.VISIBLE);
 		rootView.findViewById(R.id.vad_tool_bar).setVisibility(View.GONE);
 
 		final String contactS = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CONTACT);
 		final String mobileArea = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_MOBILE_AREA);
 		ViewGroup btnBuzz = (ViewGroup) rootView.findViewById(R.id.vad_buzz_btn);
 		ImageView btnImg = (ImageView) btnBuzz.findViewById(R.id.vad_buzz_btn_img);
 //		TextView btnTxt = (TextView) btnBuzz.findViewById(R.id.vad_buzz_btn_txt);
 //		btnTxt.setTextColor(getResources().getColor(R.color.vad_sms));
 		
 		final boolean buzzEnable = TextUtil.isNumberSequence(contactS) && mobileArea != null && !"".equals(mobileArea) ? true : false;
 		btnBuzz.setEnabled(buzzEnable);
 		if (!buzzEnable)
 		{
 //			btnTxt.setTextColor(getResources().getColor(R.color.common_button_disable));
 			btnImg.setImageBitmap(ImageCacheManager.getInstance().loadBitmapFromResource(R.drawable.icon_sms_disable));
 		}
 		
 		rootView.findViewById(R.id.vad_buzz_btn).setOnClickListener(this);
 		rl_phone.setVisibility(View.VISIBLE);
 
 		//Enable or disable call button
 		final boolean callEnable = TextUtil.isNumberSequence(contactS);
 		rootView.findViewById(R.id.vad_call_btn).setEnabled(callEnable);
 		rootView.findViewById(R.id.vad_call_btn).setOnClickListener(callEnable ? this : null);
 		View callImg = rootView.findViewById(R.id.icon_call);
 		callImg.setBackgroundResource(callEnable ? R.drawable.icon_call : R.drawable.icon_call_disable);
 		TextView txtCall = (TextView) rootView.findViewById(R.id.txt_call);
 		String text = "立即拨打" + contactS;
 		if (mobileArea != null && mobileArea.length() > 0 && !GlobalDataManager.getInstance().getCityName().equals(mobileArea))
 		{
 //			text = contactS + "(" + mobileArea + ")";
 		}
 		else if (mobileArea == null || "".equals(mobileArea.trim()))
 		{
 //			text = contactS + "(非手机号)";
 			ContextMenuItem opts = (ContextMenuItem) rootView.findViewById(R.id.vad_call_nonmobile);
 			opts.updateOptionList("", getResources().getStringArray(R.array.item_call_nonmobile), 
 					new int[] {R.id.vad_call_nonmobile + 1, R.id.vad_call_nonmobile + 2});
 		}
 		
 		txtCall.setText(callEnable ? text : "无联系方式");
 		txtCall.setTextColor(getResources().getColor(callEnable ? R.color.vad_call_btn_text : R.color.common_button_disable));
 		
 	}
 	
 	
 	private boolean handleRightBtnIfInVerify(){
 		if(!detail.getValueByKey("status").equals("0")){
 			showSimpleProgress();
 			executeModify(REQUEST_TYPE.REQUEST_TYPE_DELETE, 0);
 
 			return true;	
 		}
 		return false;
 	}
 	
 	private void handleStoreBtnClicked(){
 		if(handleRightBtnIfInVerify()) return;
 		//tracker
 		VadLogger.trackLikeUnlike(detail);
 		
 		if(!isInMyStore()){			
 			List<Ad> myStore = GlobalDataManager.getInstance().addFav(detail); 
 			
 			if (myStore != null)
 			{
 				Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "listMyStore", myStore);
 			}
 						
 			updateTitleBar(getTitleDef());
 			Toast.makeText(GlobalDataManager.getInstance().getApplicationContext(), "收藏成功", 3).show();
 		}
 		else  {
 			List<Ad> favList = GlobalDataManager.getInstance().removeFav(detail);
 			Util.saveDataToLocate(this.getAppContext(), "listMyStore", favList);
 			updateTitleBar(getTitleDef());
 			Toast.makeText(this.getActivity(), "取消收藏", 3).show();
 		}
 	}
 	
 	class ManagerAlertDialog extends AlertDialog{
 		public ManagerAlertDialog(Context context, int theme){
 			super(context, theme);
 		}
 	}
 	
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.vad_title_fav_parent:
 			handleStoreBtnClicked();
 			break;
 		case R.id.vad_call_btn:
 		{
 			VadLogger.trackContactEvent(BxEvent.VIEWAD_MOBILECALLCLICK, detail);
 			
 			final String mobileArea = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_MOBILE_AREA);
 			if (mobileArea == null || "".equals(mobileArea.trim()))
 			{
 				getView().findViewById(R.id.vad_call_nonmobile).performLongClick();
 			}
 			else
 			{
 				startContact(false);
 			}
 			
 			break;
 		}
 		case R.id.vad_buzz_btn:
 			startContact(true);
 			break;
 		case R.id.vad_btn_refresh:{
 			showSimpleProgress();
 			executeModify(REQUEST_TYPE.REQUEST_TYPE_REFRESH, 0);
 
 			VadLogger.trackMofifyEvent(detail, BxEvent.MYVIEWAD_REFRESH);
 			break;
 		}
 		case R.id.vad_btn_edit:{
 			
 			Bundle args = createArguments(null, null);
 			args.putSerializable("goodsDetail", detail);
 			args.putString("cateNames", detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME));
 			pushFragment(new EditAdFragment(), args);
             VadLogger.trackMofifyEvent(detail, BxEvent.MYVIEWAD_EDIT);
 			break;
 		}
 		case R.id.vad_btn_delete:{
 			postDelete(true, null);
 			break;
 		}
 		case R.id.vad_btn_forward:{
 			//my viewad share
 			(new SharingFragment(detail, "myViewad")).show(getFragmentManager(), null);
 			break;
 		}
 		}
 	}
 	
 	private void postDelete(boolean cancelable, OnCancelListener listener)
 	{
 		Builder builder = new AlertDialog.Builder(getActivity()).setTitle("提醒")
 		.setMessage("是否确定删除")
 		.setPositiveButton("确定", new DialogInterface.OnClickListener() {							
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				showSimpleProgress();
 				executeModify(REQUEST_TYPE.REQUEST_TYPE_DELETE, 0);
 				VadLogger.trackMofifyEvent(detail, BxEvent.MYVIEWAD_DELETE);
 			}
 		});
 		
 		if (cancelable)
 		{
 			builder = builder.setNegativeButton(
 					"取消", new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.cancel();							
 						}
 					});
 		}
 		
 		AlertDialog dialog = builder.create();
 		dialog.show();
 		if (listener != null)
 		{
 			dialog.setOnCancelListener(listener);
 		}
 		
 		dialog.show();
 	}
 
 
 	private boolean galleryReturned = true;
 	
 	@Override
 	protected void onFragmentBackWithData(int requestCode, Object result){
 		if(PostGoodsFragment.MSG_POST_SUCCEED == requestCode){
 			this.finishFragment(requestCode, result);
 		}else if(BigGalleryFragment.MSG_GALLERY_BACK == requestCode){
 //			Log.d("haha", "hahaha,   from gallery back");
 			galleryReturned = true;
 		}
 	}
 
 	
 	@Override
 	protected void handleMessage(Message msg, Activity activity, View rootView) {
 
 		switch (msg.what) {
 		case MSG_LOAD_AD_EVENT: {
 			Pair<Integer, Object> data = (Pair<Integer, Object>)msg.obj;
 			processEvent(data.first.intValue(), data.second);
 			break;
 		}
 		case MSG_REFRESH:
 			if(json == null){
 				Toast.makeText(activity, "刷新失败，请稍后重试！", 0).show();
 				break;
 			}
 			try {
 				JSONObject jb = new JSONObject(json);
 				JSONObject js = jb.getJSONObject("error");
 				String message = js.getString("message");
 				int code = js.getInt("code");
 				if (code == 0) {
 					executeModify(REQUEST_TYPE.REQUEST_TYPE_UPDATE, 0);
 					Toast.makeText(getActivity(), message, 0).show();
 				}else if(2 == code){
 					hideProgress();
 					new AlertDialog.Builder(getActivity()).setTitle("提醒")
 					.setMessage(message)
 					.setPositiveButton("确定", new DialogInterface.OnClickListener() {							
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							showSimpleProgress();
 							executeModify(REQUEST_TYPE.REQUEST_TYPE_REFRESH, 1);
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
 					hideProgress();
 					Toast.makeText(getActivity(), message, 0).show();
 				}
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			break;			
 		case MSG_UPDATE:
 			hideProgress();
 			AdList goods = JsonUtil.getGoodsListFromJson(json);
 			List<Ad> goodsDetails = goods.getData();
 			if(goodsDetails != null && goodsDetails.size() > 0){
 				for(int i = 0; i < goodsDetails.size(); ++ i){
 					if(goodsDetails.get(i).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)
 							.equals(detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))){
 						detail = goodsDetails.get(i);
 						break;
 					}
 				}
 				List<Ad>listMyPost = GlobalDataManager.getInstance().getListMyPost();
 				if(listMyPost != null){
 					for(int i = 0; i < listMyPost.size(); ++ i){
 						if(listMyPost.get(i).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)
 								.equals(detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))){
 							listMyPost.set(i, detail);
 							break;
 						}
 					}
 				}
 				//QuanleimuApplication.getApplication().setListMyPost(listMyPost);
 			}
 
 //			setMetaObject(); FIXME: should update current UI.
 			break;
 		case MSG_DELETE:
 			hideProgress();
 			try {
 				JSONObject jb = new JSONObject(json);
 				JSONObject js = jb.getJSONObject("error");
 				String message = js.getString("message");
 				int code = js.getInt("code");
 				if (code == 0) {
 					if(detail.getValueByKey("status").equals("0")){
 						List<Ad> listMyPost = GlobalDataManager.getInstance().getListMyPost();
 						if(null != listMyPost){
 							for(int i = 0; i < listMyPost.size(); ++ i){
 								if(listMyPost.get(i).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)
 										.equals(detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))){
 									listMyPost.remove(i);
 									break;
 								}
 							}
 						}
 						finishFragment(MSG_MYPOST_DELETED, null);
 					}
 					else{
 						finishFragment(MSG_ADINVERIFY_DELETED, detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
 					}
 //					finish();
 					Toast.makeText(activity, message, 0).show();
 				} else {
 					// 删除失败
 					Toast.makeText(activity, "删除失败,请稍后重试！", 0).show();
 					finishFragment();
 				}
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			break;
 		default:
 			break;
 		}
 	
 	}
 	
 	private void executeModify(REQUEST_TYPE request, int pay) {
 		json = "";
 		
 		ApiParams params = new ApiParams();
 		UserBean user = (UserBean) Util.loadDataFromLocate(this.getActivity(), "user", UserBean.class);
 		if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
 			params.appendUserInfo(user);
 		}
 		params.addParam("rt", 1);
 		
 		switch(request) {
 		case REQUEST_TYPE_DELETE:
 			params.addParam("adId", detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID));
 			break;
 		case REQUEST_TYPE_REFRESH:
 			params.addParam("adId", detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID));
 			if(pay != 0){
 				params.addParam("pay", 1);
 			}
 			break;
 		case REQUEST_TYPE_UPDATE:
 			params.addParam("query", "id:" + detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID));
 		}
 			
 		BaseCommand cmd = HttpGetCommand.createCommand(request.reqCode, request.apiName, params);
 		cmd.execute(this);
 	}
 
 	@Override
 	public void initTitle(TitleDef title){
 		title.m_leftActionHint = "返回";
 		title.m_rightActionHint = "";//detail.getValueByKey("status").equals("0") ? "收藏" : null;
 		if(this.mListLoader != null && mListLoader.getGoodsList() != null && mListLoader.getGoodsList().getData() != null){
 			title.m_title = ( this.mListLoader.getSelection() + 1 ) + "/" + 
 					this.mListLoader.getGoodsList().getData().size();			
 		}
 		title.m_visible = true;
 		
 		LayoutInflater inflater = LayoutInflater.from(this.getActivity());
 		title.m_titleControls = inflater.inflate(R.layout.vad_title, null); 
 		
 		updateTitleBar(title);
 	}
 	
 	private void updateTitleBar(TitleDef title)
 	{
 		
 		if(isMyAd() || !detail.isValidMessage()){
 			title.m_titleControls.findViewById(R.id.vad_title_fav_parent).setVisibility(View.GONE);
 		}
 		else{
 			title.m_titleControls.findViewById(R.id.vad_title_fav_parent).setVisibility(View.VISIBLE);
 		}
 		
 		title.m_titleControls.findViewById(R.id.vad_title_fav_parent).setOnClickListener(this);
 		TextView favBtn = (TextView) title.m_titleControls.findViewById(R.id.btn_fav_unfav);
 		if (favBtn != null)
 		{
 			favBtn.setText(isInMyStore() ? "取消收藏" : "收藏");
 		}
 		
 		TextView createTimeView = (TextView) title.m_titleControls.findViewById(R.id.vad_create_time);
 		if(detail != null){
 			String dateV = detail.getValueByKey(EDATAKEYS.EDATAKEYS_DATE);
 			if (dateV != null)
 			{
 				try {
 					long timeL = Long.parseLong(dateV) * 1000;
 					createTimeView.setText(TextUtil.timeTillNow(timeL, getAppContext()) + "发布");
 				}
 				catch(Throwable t)
 				{
 					createTimeView.setText("");
 				}
 			}
 			
 			TextView viewTimes = (TextView) getTitleDef().m_titleControls.findViewById(R.id.vad_viewed_time);
 			viewTimes.setText(detail.getValueByKey("count") + "次查看");
 		}
 	}
 	
     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
     {
     	if (parent.getAdapter() instanceof VadImageAdapter)
     	{
     		VadImageAdapter mainAdapter = (VadImageAdapter) parent.getAdapter();
     		
     		List<String> listUrl = mainAdapter.getImages();
     		ArrayList<String> urls = new ArrayList<String>();
     		urls.add(listUrl.get(position));
     		for(int index = 0; (index + position < listUrl.size() || position - index >= 0); ++index){
     			if(index + position < listUrl.size())
     				urls.add(listUrl.get(index+position));
     			
     			if(position - index >= 0)
     				urls.add(listUrl.get(position-index));				
     		}
     		ImageLoaderManager.getInstance().AdjustPriority(urls);
     	}
     }
 
     @Override
     public void onNothingSelected(AdapterView<?> arg0)
     {
         // TODO Auto-generated method stub
     }	
 
 
 	@Override
 	public void onHasMoreStatusChanged() {
 	}
 	
 	public void setListHolder(IListHolder holder)
 	{
 		this.mHolder = holder;
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem menuItem) {
 		
 		switch (menuItem.getItemId())
 		{
 			case R.id.vad_call_nonmobile + 1: {
 				startContact(false);
 				return true;
 			}
 			case R.id.vad_call_nonmobile + 2: {
 				ClipboardManager clipboard = (ClipboardManager)
 				        getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
 				clipboard.setText(detail.getValueByKey(EDATAKEYS.EDATAKEYS_CONTACT));
 				ViewUtil.postShortToastMessage(getView(), R.string.tip_clipd_contact, 0);
 				return true;
 			}
 		}
 		
 		return super.onContextItemSelected(menuItem);
 	}
 	
 	private void startContact(boolean sms)
 	{
 		if (sms){//右下角发短信
 			VadLogger.trackContactEvent(BxEvent.VIEWAD_SMS, detail);
 		}
 			
 		String contact = detail.getValueByKey(EDATAKEYS.EDATAKEYS_CONTACT);
 		if (contact != null)
 		{
 			Intent intent = new Intent(
 					sms ? Intent.ACTION_SENDTO : Intent.ACTION_DIAL,
 					Uri.parse((sms ? "smsto:" : "tel:") + contact));
 			if (sms) {
				intent.putExtra("sms_body", "你好，我在百姓网看到你发的\"" + detail.getValueByKey(EDATAKEYS.EDATAKEYS_TITLE) + "\",");
 			}
 			
 			List<ResolveInfo> ls = getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
 			if (ls != null && ls.size() > 0)
 			{
 				startActivity(intent);
 			}
 			else
 			{
 				ViewUtil.postShortToastMessage(getView(), sms ? R.string.warning_no_sms_app_install : R.string.warning_no_phone_app_install, 0);
 			}
 		}
 	}
 	
 	public void showMap() {
 		VadLogger.trackShowMapEvent(detail);
 		if (keepSilent) { // FIXME:
 			Toast.makeText(getActivity(), "当前无法显示地图", 1).show();
 			return;
 		}
 		else
 		{
 			ViewUtil.startMapForAds(getActivity(), detail);
 		}
 					
 	}
 	
 	public boolean hasGlobalTab()
 	{
 		return false;
 	}
 
 	@Override
 	public void onRequestComplete(int respCode, Object data) {
 		sendMessage(MSG_LOAD_AD_EVENT, Pair.create(respCode, data));
 	}
 	
 	private void processEvent(int respCode, Object data) {
 
 
 		if(null != mHolder){
 			if(mHolder.onResult(respCode, mListLoader)){
 //				onGotMore();
 				pageController.loadMoreSucced();
 			}else{
 				onNoMore();
 			}
 			
 			if(respCode == ErrorHandler.ERROR_NETWORK_UNAVAILABLE){
 				pageController.loadMoreFail();
 			}
 		}else{
 			switch (respCode) {
 			case VadListLoader.MSG_FINISH_GET_FIRST:				 
 				AdList goodsList = JsonUtil.getGoodsListFromJson(mListLoader.getLastJson());
 				mListLoader.setGoodsList(goodsList);
 				if (goodsList == null || goodsList.getData().size() == 0) {
 					ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_COMMON_FAILURE, "没有符合的结果，请稍后并重试！");
 				} else {
 					//QuanleimuApplication.getApplication().setListGoods(goodsList.getData());
 				}
 				mListLoader.setHasMore(true);
 				notifyPageDataChange(true);
 				break;
 			case VadListLoader.MSG_NO_MORE:					
 				onNoMore();
 				
 				mListLoader.setHasMore(false);
 				notifyPageDataChange(false);
 				
 				break;
 			case VadListLoader.MSG_FINISH_GET_MORE:	
 				AdList goodsList1 = JsonUtil.getGoodsListFromJson(mListLoader.getLastJson());
 				if (goodsList1 == null || goodsList1.getData().size() == 0) {
 					ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_COMMON_WARNING, "后面没有啦！");
 					
 					onNoMore();
 					
 					mListLoader.setHasMore(false);
 					notifyPageDataChange(false);
 				} else {
 					List<Ad> listCommonGoods =  goodsList1.getData();
 					for(int i=0;i<listCommonGoods.size();i++)
 					{
 						mListLoader.getGoodsList().getData().add(listCommonGoods.get(i));
 					}
 					//QuanleimuApplication.getApplication().setListGoods(mListLoader.getGoodsList().getData());	
 					
 					mListLoader.setHasMore(true);
 					notifyPageDataChange(true);
 					pageController.loadMoreSucced();
 				}
 				break;
 			case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
 				ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
 				pageController.loadMoreFail();
 				
 				break;
 			}
 		}
 	
 	}
 	
 	private void onNoMore() {
 		View root = getView();
 		if (root != null)
 		{
 			ViewUtil.postShortToastMessage(root, "后面没有啦！", 0);
 		}
 	}
 
 	@Override
 	public int totalPages() {
 			if(mListLoader == null || mListLoader.getGoodsList() == null || mListLoader.getGoodsList().getData() == null){
 			return 0;
 		}
 		return mListLoader.getGoodsList().getData().size();//+ (mListLoader.hasMore() ? 1 : 0);
 		
 	}
 
 	@Override
 	public Ad getAd(int pos) {
 		return mListLoader.getGoodsList().getData().get(pos);
 	}
 
 	@Override
 	public void onLoadMore() {
 		if (null != mHolder) {
 			mHolder.startFecthingMore();
 		} else {
 			mListLoader
 					.startFetching(
 							false,
 							((VadListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE == mListLoader
 									.getDataStatus()) ? Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE
 									: Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL));
 		}
 	}
 
 	@Override
 	public void onPageSwitchTo(int pos) {
 		keepSilent = false;//magic flag to refuse unexpected touch event
 		//tracker
 		VadLogger.trackPageView(detail, VadFragment.this.getAppContext());
 		
 		if (pos != totalPages())
 		{
 			detail = mListLoader.getGoodsList().getData().get(pos);
 			mListLoader.setSelection(pos);
 			updateTitleBar(getTitleDef());
 			updateContactBar(getView(), false);
 		}
 		else
 		{
 			updateTitleBar(getTitleDef());
 			updateContactBar(getView(), true);
 		}
 	}
 
 	@Override
 	public void onRequestBigPic(int pos, Ad detail) {
 		if(galleryReturned){
 			Bundle bundle = createArguments(null, null);
 			bundle.putInt("postIndex", pos);
 			bundle.putSerializable("goodsDetail", detail);
 			galleryReturned = false;
 			pushFragment(new BigGalleryFragment(), bundle);		
 		}
 	}
 
 	@Override
 	public void onRequestMap() {
 		showMap();
 	}
 	
 	public void onRequestUserAd(int userId, String userNick) {
 		Bundle args = createArguments(null, null);
 		args.putInt("userId", userId);
 		args.putString("userNick", userNick);
 		args.putString("secondCategoryName", detail.getValueByKey(EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME));
 		args.putString("adId", detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
 
 		pushFragment(new UserAdFragment(), args);
 	}
 
 	@Override
 	public void onPageInitDone(ViewPager pager, final int pageIndex) {
 		final ViewPager vp = pager != null ? pager : (ViewPager) getActivity().findViewById(R.id.svDetail);
 		if (vp != null && pageIndex == vp.getCurrentItem())
 		{
 			updateTitleBar(getTitleDef());
 			updateContactBar(vp.getRootView(), false);
 		}		
 	}
 
 	@Override
 	public boolean hasMore() {
 		return mListLoader == null ? false : mListLoader.hasMore();
 	}
 
 	@Override
 	public void onNetworkDone(int requstCode, String responseData) {
 		json = responseData;
 		sendMessage(requstCode, null);
 	}
 
 	@Override
 	public void onNetworkFail(int requstCode, ApiError error) {
 		ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
 		hideProgress();
 		
 	}
 	
 }
