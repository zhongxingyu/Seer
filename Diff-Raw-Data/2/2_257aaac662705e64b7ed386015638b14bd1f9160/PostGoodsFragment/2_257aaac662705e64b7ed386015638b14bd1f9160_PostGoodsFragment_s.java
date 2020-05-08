 //xumengyi@baixing.com
 package com.baixing.view.fragment;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Message;
 import android.text.InputFilter;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.baixing.activity.BaseActivity;
 import com.baixing.activity.BaseFragment;
 import com.baixing.broadcast.CommonIntentAction;
 import com.baixing.data.GlobalDataManager;
 import com.baixing.entity.BXLocation;
 import com.baixing.entity.PostGoodsBean;
 import com.baixing.entity.UserBean;
 import com.baixing.imageCache.ImageCacheManager;
 import com.baixing.jsonutil.JsonUtil;
 import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
 import com.baixing.tracking.TrackConfig.TrackMobile.Key;
 import com.baixing.tracking.TrackConfig.TrackMobile.PV;
 import com.baixing.tracking.Tracker;
 import com.baixing.util.ErrorHandler;
 import com.baixing.util.Util;
 import com.baixing.util.post.ImageUploader;
 import com.baixing.util.post.PostCommonValues;
 import com.baixing.util.post.PostLocationService;
 import com.baixing.util.post.PostNetworkService;
 import com.baixing.util.post.PostNetworkService.PostResultData;
 import com.baixing.util.post.PostUtil;
 import com.baixing.widget.CustomDialogBuilder;
 import com.baixing.widget.ImageSelectionDialog;
 import com.quanleimu.activity.R;
 
 public class PostGoodsFragment extends BaseFragment implements OnClickListener{
 	private static final int MSG_GEOCODING_TIMEOUT = 0x00010011;
 	static final public String KEY_INIT_CATEGORY = "cateNames";
 	static final String KEY_LAST_POST_CONTACT_USER = "lastPostContactIsRegisteredUser";
 	static final String KEY_IS_EDITPOST = "isEditPost"; 
 	static final String KEY_CATE_ENGLISHNAME = "cateEnglishName";
 	static final private String KEY_IMG_BUNDLE = "key_image_bundle";
 	static final private String FILE_LAST_CATEGORY = "lastCategory";
 	static final int MSG_POST_SUCCEED = 0xF0000010; 
 	protected String categoryEnglishName = "";
 	private String categoryName = "";
 	protected LinearLayout layout_txt;
 	private LinkedHashMap<String, PostGoodsBean> postList = new LinkedHashMap<String, PostGoodsBean>();
 	private static final int NONE = 0;
 	private static final int PHOTORESOULT = 3;
 	private static final int MSG_CATEGORY_SEL_BACK = 11;
 	private static final int MSG_DIALOG_BACK_WITH_DATA = 12;
 	private static final int MSG_UPDATE_IMAGE_LIST = 13;
 	protected PostParamsHolder params = new PostParamsHolder();
 	protected boolean editMode = false;
 //	protected ArrayList<String> listUrl = new ArrayList<String>();
 	protected Bundle imgSelBundle = null;
 	private ImageSelectionDialog imgSelDlg = null;	
 	private View locationView = null;
 	private BXLocation detailLocation = null;
     protected List<String> bmpUrls = new ArrayList<String>();
     private EditText etDescription = null;
     private EditText etContact = null;
     private PostLocationService postLBS;
     private PostNetworkService postNS;
     
     protected ArrayList<String> photoList = new ArrayList<String>();
     private Bitmap firstImage = null;
     protected boolean isNewPost = true;
     private boolean finishRightNow = false;
     
     @Override
 	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
 		if (resultCode == NONE) {
 			return;
 		} else if (resultCode == Activity.RESULT_FIRST_USER) {
 			finishRightNow = true;
 			return;
 		}
 		
 		if (resultCode == Activity.RESULT_OK) {
 			photoList.clear();
 			if (data.getExtras().containsKey(CommonIntentAction.EXTRA_IMAGE_LIST)){
 				ArrayList<String> result = data.getStringArrayListExtra(CommonIntentAction.EXTRA_IMAGE_LIST);
 				photoList.addAll(result);
 			}
 			
 			if (photoList != null && photoList.size() > 0) {
 				firstImage = ImageUploader.getInstance().getThumbnail(photoList.get(0));
 			}
 			else {
 				firstImage = null;
 			}
 		}
 		
 		handler.sendEmptyMessage(MSG_UPDATE_IMAGE_LIST);
 
 //		FragmentManager fm = getActivity().getSupportFragmentManager();
 //
 //		Fragment fg = fm.getFragment(this.imgSelBundle, "imageFragment");
 //		if(fg != null && (fg instanceof ImageSelectionDialog)){
 //			this.imgSelDlg = (ImageSelectionDialog)fg;
 //		}
 //		if(this.imgSelDlg != null &&
 //				(requestCode == CommonIntentAction.PhotoReqCode.PHOTOHRAPH
 //				|| requestCode == CommonIntentAction.PhotoReqCode.PHOTOZOOM
 //				|| requestCode == PHOTORESOULT)){
 //			imgSelDlg.setMsgOutHandler(handler);
 //			if(imgSelBundle == null){
 //				imgSelBundle = new Bundle();
 //			}
 //			imgSelDlg.setMsgOutBundle(this.imgSelBundle);
 //			imgSelDlg.onActivityResult(requestCode, resultCode, data);
 //		}
     }
     
     private void initWithCategoryNames(String categoryNames) {
     	if(categoryNames == null || categoryNames.length() == 0){
 			categoryNames = (String)Util.loadDataFromLocate(this.getActivity(), FILE_LAST_CATEGORY, String.class);
 		}
 		if(categoryNames != null && !categoryNames.equals("")){
 			String[] names = categoryNames.split(",");
 			if(names.length == 2){
 				this.categoryEnglishName = names[0];
 				this.categoryName = names[1];
 				
 			}else if(names.length == 1){
 				this.categoryEnglishName = names[0];
 			}
 			Util.saveDataToLocate(this.getActivity(), FILE_LAST_CATEGORY, categoryNames);
 		}
     }
     
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		if (savedInstanceState != null) {
 			isNewPost = false;
 		} else {
 			isNewPost = !editMode;
 		}
 		
 		String categoryNames = this.getArguments().getString(KEY_INIT_CATEGORY);
 		initWithCategoryNames(categoryNames);
 				
 		if (savedInstanceState != null){
 			postList.putAll( (HashMap<String, PostGoodsBean>)savedInstanceState.getSerializable("postList"));
 			params = (PostParamsHolder) savedInstanceState.getSerializable("params");
 //			listUrl.addAll((List<String>) savedInstanceState.getSerializable("listUrl"));
 			photoList.addAll((List<String>) savedInstanceState.getSerializable("listUrl"));
 			imgHeight = savedInstanceState.getInt("imgHeight");
 			imgSelBundle = savedInstanceState.getBundle(KEY_IMG_BUNDLE);
 		}
 		
 		if(imgSelBundle == null){
 			imgSelBundle =  new Bundle();
 		}
 
 //		if(imgSelDlg == null){ //FIXME: remove 
 //			imgSelDlg = new ImageSelectionDialog(imgSelBundle);
 //			imgSelDlg.setMsgOutHandler(handler);
 //		}
 		
 		String appPhone = GlobalDataManager.getInstance().getPhoneNumber();
 		if(!editMode && (appPhone == null || appPhone.length() == 0)){
 			UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
 			if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
 				String mobile = user.getPhone();
 				GlobalDataManager.getInstance().setPhoneNumber(mobile);
 			}
 		}
 		
 		this.postLBS = new PostLocationService(this.handler);
 		postNS =  new PostNetworkService(handler);
 	}
 		
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		PostUtil.extractInputData(layout_txt, params);
 		outState.putSerializable("params", params);
 		outState.putSerializable("postList", postList);
 		outState.putSerializable("listUrl", photoList);
 		outState.putInt("imgHeight", imgHeight);
 		outState.putBundle(KEY_IMG_BUNDLE, imgSelBundle);
 	}
 	
 	private void doClearUpImages() {
 		//Clear the upload image list.
 		this.photoList.clear();
 		this.firstImage = null;
 		ImageUploader.getInstance().clearAll();
 	}
 
 	@Override
 	public boolean handleBack() {
 //		if(imgSelDlg != null)
 //			if(imgSelDlg.handleBack()){
 //				return true;
 //		}		
 //		return super.handleBack();
 		AlertDialog.Builder builder = new Builder(getActivity());
 		builder.setMessage("退出发布？");
 		builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				//Do nothing.
 			}
 		});
 		builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				doClearUpImages();
 				finishFragment();
 			}
 		});
 		builder.create().show();
 		
 		return true;
 	}	
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		postLBS.start();
 		if(!editMode) {
 			this.pv = PV.POST;
 			Tracker.getInstance()
 			.pv(this.pv)
 			.append(Key.SECONDCATENAME, categoryEnglishName)
 			.end();
 		}	
 		
 		if (finishRightNow) {
 			finishRightNow = false;
 			doClearUpImages();
 			finishFragment();
 		}
 	}
 	
 	@Override
 	public void onPause() {
 		postLBS.stop();
 		PostUtil.extractInputData(layout_txt, params);
 		setPhoneAndAddress();
 		super.onPause();
 	}
 
 	@Override
 	public void onStackTop(boolean isBack) {
 		if(isBack){
 			final ScrollView scroll = (ScrollView) this.getView().findViewById(R.id.goodscontent);
 			scroll.post(new Runnable() {            
 			    @Override
 			    public void run() {
 			           scroll.fullScroll(View.FOCUS_DOWN);              
 			    }
 			});
 		}
 		
 		if (isNewPost) {
 			isNewPost = false;
			this.startImgSelDlg(Activity.RESULT_FIRST_USER, "下一步");
 		}
 		
 	}	
 
 	@Override
 	public void onViewCreated (View view, Bundle savedInstanceState){
 		super.onViewCreated(view, savedInstanceState);
 		showPost();
 	}
 
 	@Override
 	public View onInitializeView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
 		ViewGroup v = (ViewGroup) inflater.inflate(R.layout.postgoodsview, null);		
 		layout_txt = (LinearLayout) v.findViewById(R.id.layout_txt);		
 		Button button = (Button) v.findViewById(R.id.iv_post_finish);
 		button.setOnClickListener(this);
 		if (!editMode)
 			button.setText("立即免费发布");
 		else
 			button.setText("立即更新信息");
 		return v;
 	}
 	
 	protected void startImgSelDlg(final int cancelResultCode, String finishActionLabel){
 //		if(container != null){
 //			imgSelBundle.putSerializable(ImageSelectionDialog.KEY_IMG_CONTAINER, container);
 //		}
 //		imgSelDlg.setMsgOutBundle(imgSelBundle);
 //		imgSelDlg.show(getFragmentManager(), null);
 		
 		Intent backIntent = new Intent();
 		backIntent.setClass(getActivity(), getActivity().getClass());
 		
 		Intent goIntent = new Intent();
 		goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_INTENT, backIntent);
 		goIntent.setAction(CommonIntentAction.ACTION_IMAGE_CAPTURE);
 		goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_REQUST_CODE, CommonIntentAction.PhotoReqCode.PHOTOHRAPH);
 		goIntent.putStringArrayListExtra(CommonIntentAction.EXTRA_IMAGE_LIST, this.photoList);
 		goIntent.putExtra(CommonIntentAction.EXTRA_FINISH_ACTION_LABEL, finishActionLabel);
 		goIntent.putExtra(CommonIntentAction.EXTRA_COMMON_FINISH_CODE, cancelResultCode);
 //		BXLocation loc = GlobalDataManager.getInstance().getLocationManager().getCurrentPosition(true); 
 //		if (loc != null) {
 //			goIntent.putExtra("location", loc);
 //		}
 		getActivity().startActivity(goIntent);
 	}
 
 	private void deployDefaultLayout(){
 		addCategoryItem();
 		HashMap<String, PostGoodsBean> pl = new HashMap<String, PostGoodsBean>();
 		for(int i = 1; i < PostCommonValues.fixedItemNames.length; ++ i){
 			PostGoodsBean bean = new PostGoodsBean();
 			bean.setControlType("input");
 			bean.setDisplayName(PostCommonValues.fixedItemDisplayNames[i]);
 			bean.setName(PostCommonValues.fixedItemNames[i]);
 			bean.setUnit("");
 			if(PostCommonValues.fixedItemNames[i].equals("价格")){
 				bean.setNumeric(1);//.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
 				bean.setUnit("元");
 			}
 			pl.put(PostCommonValues.fixedItemNames[i], bean);
 		}
 		buildPostLayout(pl);
 	}
 	
 	public void updateNewCategoryLayout(String cateNames){
 		if(cateNames == null) return;
 		String[] names = cateNames.split(",");
 		if(names != null){
 			if(categoryEnglishName.equals(names[0])) return;
 		}
 		initWithCategoryNames(cateNames);
 		resetData(true);
 		Util.saveDataToLocate(getActivity(), FILE_LAST_CATEGORY, cateNames);
 		this.showPost();
 	}
 	
 	protected String getCityEnglishName(){
 		return GlobalDataManager.getInstance().getCityEnglishName();
 	}
 	
 	private void showPost(){
 		if(this.categoryEnglishName == null || categoryEnglishName.length() == 0){
 			deployDefaultLayout();
 			return;
 		}
 
 		String cityEnglishName = getCityEnglishName();		
 		Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(this.getActivity(), categoryEnglishName + cityEnglishName);
 		String json = pair.second;
 		if (json != null && json.length() > 0) {			
 			if (pair.first + (24 * 3600) >= System.currentTimeMillis()/1000) {
 				if(postList == null || postList.size() == 0){
 					postList = JsonUtil.getPostGoodsBean(json);
 				}
 				addCategoryItem();
 				buildPostLayout(postList);
 				loadCachedData();
 				return;
 			}
 		}
 		showSimpleProgress();
 		postNS.retreiveMetaAsync(cityEnglishName, categoryEnglishName);
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if(v.getId() == R.id.iv_post_finish){
 			Tracker.getInstance()
 			.event(!editMode ? BxEvent.POST_POSTBTNCONTENTCLICKED:BxEvent.EDITPOST_POSTBTNCONTENTCLICKED)
 			.append(Key.SECONDCATENAME, categoryEnglishName).end();			
 			this.postAction();
 		}else if(v.getId() == R.id.location){
 			Tracker.getInstance().event((!editMode)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, PostCommonValues.STRING_DETAIL_POSITION).end();			
 			if(this.detailLocation != null && locationView != null){
 				setDetailLocationControl(detailLocation);
 			}else if(detailLocation == null){
 				Toast.makeText(this.getActivity(), "无法获得当前位置", 0).show();
 			}
 		}else if(v.getId() == R.id.myImg){
 			Tracker.getInstance().event((!editMode)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, "image").end();
 			
 //			if(!editMode){
 				startImgSelDlg(Activity.RESULT_CANCELED, "完成");
 //			}
 		}else if(v.getId() == R.id.img_description){
 			final View et = v.findViewById(R.id.description_input);
 			if(et != null){
 				et.postDelayed(new Runnable(){
 					@Override
 					public void run(){
 						if (et != null){
 							Tracker.getInstance().event((!editMode)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, PostCommonValues.STRING_DESCRIPTION).end();
 							et.requestFocus();
 							InputMethodManager inputMgr = (InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 							inputMgr.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
 						}
 					}			
 				}, 100);
 			}
 		}
 	}
 	
 	private void setPhoneAndAddress(){
 		String phone = params.getData("contact");
 		if(phone != null && phone.length() > 0 && !editMode){
 			GlobalDataManager.getInstance().setPhoneNumber(phone);
 		}
 		String address = params.getData(PostCommonValues.STRING_DETAIL_POSITION);
 		if(address != null && address.length() > 0){
 			GlobalDataManager.getInstance().setAddress(address);
 		}		
 	}
 	
 	private void postAction() {
 		PostUtil.extractInputData(layout_txt, params);
 		setPhoneAndAddress();
 		if(!this.checkInputComplete()){
 			return;
 		}
 		String detailLocationValue = params.getUiData(PostCommonValues.STRING_DETAIL_POSITION);
 		if(this.detailLocation != null && (detailLocationValue == null || detailLocationValue.length() == 0)){
 			showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
 			postAd(detailLocation);
 		}else{
 			this.sendMessageDelay(MSG_GEOCODING_TIMEOUT, null, 5000);
 			this.showSimpleProgress();
 			postLBS.retreiveLocation(GlobalDataManager.getInstance().cityName, getFilledLocation());			
 		}
 	}
 
 	private boolean checkInputComplete() {
 		if(this.categoryEnglishName == null || this.categoryEnglishName.equals("")){
 			Toast.makeText(this.getActivity(), "请选择分类" ,0).show();
 			popupCategorySelectionDialog();
 			return false;
 		}
 		
 		for (int i = 0; i < postList.size(); i++) {
 			String key = (String) postList.keySet().toArray()[i];
 			PostGoodsBean postGoodsBean = postList.get(key);
 			if (postGoodsBean.getName().equals(PostCommonValues.STRING_DESCRIPTION) || 
 					(postGoodsBean.getRequired().endsWith("required") && !PostUtil.inArray(postGoodsBean.getName(), PostCommonValues.hiddenItemNames) && !postGoodsBean.getName().equals("title") && !postGoodsBean.getName().equals(PostCommonValues.STRING_AREA))) {
 				if(!params.containsKey(postGoodsBean.getName()) 
 						|| params.getData(postGoodsBean.getName()).equals("")
 						|| (postGoodsBean.getUnit() != null && params.getData(postGoodsBean.getName()).equals(postGoodsBean.getUnit()))){
 					if(postGoodsBean.getName().equals("images"))continue;
 					postResultFail("please entering " + postGoodsBean.getDisplayName() + "!");
 					Toast.makeText(this.getActivity(), "请填写" + postGoodsBean.getDisplayName() + "!", 0).show();
 					return false;
 				}
 			}
 		}
 		
 		if (ImageUploader.getInstance().hasPendingJob()) {
 			Toast.makeText(this.getActivity(), "图片上传中", Toast.LENGTH_SHORT).show();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	private String getFilledLocation(){
 		String toRet = "";
 		for(int m = 0; m < layout_txt.getChildCount(); ++ m){
 			View v = layout_txt.getChildAt(m);
 			PostGoodsBean bean = (PostGoodsBean)v.getTag(PostCommonValues.HASH_POST_BEAN);
 			if(bean == null) continue;
 			if(bean.getName().equals(PostCommonValues.STRING_DETAIL_POSITION)){
 				TextView tv = (TextView)v.getTag(PostCommonValues.HASH_CONTROL);
 				if(tv != null && !tv.getText().toString().equals("")){
 					toRet = tv.getText().toString();
 				}
 				break;
 			}
 		}
 		return toRet;
 	}
 	
 	protected void mergeParams(HashMap<String, String> list){}
 	
 	protected void postAd(BXLocation location){
 		HashMap<String, String> list = new HashMap<String, String>();
 		list.put("categoryEnglishName", categoryEnglishName);
 		list.put("cityEnglishName", GlobalDataManager.getInstance().getCityEnglishName());
 		
 		HashMap<String, String> mapParams = new HashMap<String, String>();
 		Iterator<String> ite = params.keyIterator();
 		while(ite.hasNext()){
 			String key = ite.next();
 			String value = params.getData(key);
 			mapParams.put(key, value);
 		}
 		mergeParams(list);
 //		this.postNS.postAdAsync(mapParams, list, postList, bmpUrls, location, editMode);
 		bmpUrls.clear();
 		bmpUrls.addAll(ImageUploader.getInstance().getServerUrlList());
 		this.postNS.postAdAsync(mapParams, list, postList, bmpUrls, location, editMode);
 	}
 
 	private int getLineCount() {
 		return etDescription != null ? etDescription.getLineCount() : 1;
 	}
 	
 	private int getDescLength() {
 		return etDescription != null ? etDescription.getText().length() : 0;
 	}
 	
 	private int getContactLength() {
 		return etContact != null ? etContact.getText().length() : 0;
 	}
 	
 	private int getImgCount() {
 		int imgCount = 0;
 		for (int i = 0; i < bmpUrls.size(); i++) {				
 			if(bmpUrls.get(i) != null && bmpUrls.get(i).contains("http:")){
 				imgCount++;
 			}
 		}
 		return imgCount;
 	}
 	
 	private void postResultSuccess() {
 		BxEvent event = editMode ? BxEvent.EDITPOST_POSTRESULT : BxEvent.POST_POSTRESULT;
 		Tracker.getInstance().event(event)
 		.append(Key.SECONDCATENAME, categoryEnglishName)
 		.append(Key.POSTSTATUS, 1)
 		.append(Key.POSTPICSCOUNT, getImgCount())
 		.append(Key.POSTDESCRIPTIONLINECOUNT, getLineCount())
 		.append(Key.POSTDESCRIPTIONTEXTCOUNT, getDescLength())
 		.append(Key.POSTCONTACTTEXTCOUNT, getContactLength()).end();
 	}
 	
 	private void postResultFail(String errorMsg) {
 		BxEvent event = editMode ? BxEvent.EDITPOST_POSTRESULT : BxEvent.POST_POSTRESULT;
 		Tracker.getInstance().event(event)
 			.append(Key.SECONDCATENAME, categoryEnglishName)
 			.append(Key.POSTSTATUS, 0)
 			.append(Key.POSTFAILREASON, errorMsg)
 			.append(Key.POSTPICSCOUNT, getImgCount())
 			.append(Key.POSTDESCRIPTIONLINECOUNT, getLineCount())
 			.append(Key.POSTDESCRIPTIONTEXTCOUNT, getDescLength())
 			.append(Key.POSTCONTACTTEXTCOUNT, getContactLength()).end();
 	}
 	
 	private void loadCachedData(){
 		if(params.size() == 0) return;
 
 		Iterator<String> it = params.keyIterator();
 		while (it.hasNext()){
 			String name = it.next();
 			for (int i=0; i<layout_txt.getChildCount(); i++)
 			{
 				View v = layout_txt.getChildAt(i);
 				PostGoodsBean bean = (PostGoodsBean)v.getTag(PostCommonValues.HASH_POST_BEAN);
 				if(bean == null || 
 						!bean.getName().equals(name)//check display name 
 						) continue;
 				View control = (View)v.getTag(PostCommonValues.HASH_CONTROL);
 				String displayValue = params.getUiData(name);
 				
 				if(control instanceof CheckBox){
 					if(displayValue.contains(((CheckBox)control).getText())){
 						((CheckBox)control).setChecked(true);
 					}
 					else{
 						((CheckBox)control).setChecked(false);
 					}
 				}else if(control instanceof TextView){
 					((TextView)control).setText(displayValue);
 				}
 			}
 		}	
 	}
 	
 	private void clearCategoryParameters(){//keep fixed(common) parameters there
 		Iterator<String> ite = params.keyIterator();
 		while(ite.hasNext()){
 			String key = ite.next();
 			if(!PostUtil.inArray(key, PostCommonValues.fixedItemNames)){
 				params.remove(key);
 				ite = params.keyIterator();
 			}
 		}
 	}
 	
 	private void resetData(boolean clearImgs){
 		if(this.layout_txt != null){
 			View v = layout_txt.findViewById(R.id.img_description);
 			layout_txt.removeAllViews();
 			layout_txt.addView(v);
 		}
 //		postList.clear();
 		
 		if(null != Util.loadDataFromLocate(getActivity(), FILE_LAST_CATEGORY, String.class)){
 			clearCategoryParameters();
 			if(clearImgs){
 //				listUrl.clear();
 				this.doClearUpImages();
 				this.bmpUrls.clear();
 				if(this.imgSelDlg != null){
 					imgSelDlg.clearResource();
 				}
 				this.imgSelBundle.clear();// = null;
 				
 				layout_txt.findViewById(R.id.imgCout).setVisibility(View.INVISIBLE);
 				
 				params.remove(PostCommonValues.STRING_DESCRIPTION);
 				params.remove("价格");
 			}
 		}
 	}
 
 	private void handleBackWithData(int message, Object obj) {
 		if(message == MSG_CATEGORY_SEL_BACK && obj != null){
 			String[] names = ((String)obj).split(",");
 			if(names.length == 2){
 				if(names[0].equals(this.categoryEnglishName)){
 					return;
 				}
 				this.categoryEnglishName = names[0];
 				this.categoryName = names[1];
 				
 			}else if(names.length == 1){
 				if(names[0].equals(this.categoryEnglishName)){
 					return;
 				}
 				this.categoryEnglishName = names[0];
 			}
 			
 			resetData(false);
 			Util.saveDataToLocate(getActivity(), FILE_LAST_CATEGORY, obj);
 			this.showPost();
 		}
 		PostUtil.fetchResultFromViewBack(message, obj, layout_txt, params);
 	}
 	
 	@Override
 	public void onFragmentBackWithData(int message, Object obj){	
 		handleBackWithData(message, obj);
 	}
 	
 	protected String getAdContact(){
 		return "";
 	}
 
 	private void appendBeanToLayout(PostGoodsBean postBean){
 		UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
 		if (postBean.getName().equals("contact") &&
 			(postBean.getValues() == null || postBean.getValues().isEmpty()) &&
 			(user != null && user.getPhone() != null && user.getPhone().length() > 0)){
 			List<String> valueList = new ArrayList<String>(1);
 			valueList.add(user.getPhone());
 			postBean.setValues(valueList);
 			postBean.setLabels(valueList);
 		}	
 		
 		ViewGroup layout = createItemByPostBean(postBean);//FIXME:
 
 		if(layout != null && !postBean.getName().equals(PostCommonValues.STRING_DETAIL_POSITION)){
 			ViewGroup.LayoutParams lp = layout.getLayoutParams();
 			lp.height = getResources().getDimensionPixelOffset(R.dimen.post_item_height);
 			layout.setLayoutParams(lp);
 		}
 
 		if(postBean.getName().equals(PostCommonValues.STRING_DETAIL_POSITION)){
 			layout.findViewById(R.id.location).setOnClickListener(this);
 			((TextView)layout.findViewById(R.id.postinput)).setHint("请输入");
 			locationView = layout;
 			
 			String address = GlobalDataManager.getInstance().getAddress();
 			if(address != null && address.length() > 0){
 				((TextView)layout.findViewById(R.id.postinput)).setText(address);
 			}
 		}else if(postBean.getName().equals("contact") && layout != null){
 			etContact = ((EditText)layout.getTag(PostCommonValues.HASH_CONTROL));
 			etContact.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
 			String phone = GlobalDataManager.getInstance().getPhoneNumber();
 			if(editMode){
 				etContact.setText(getAdContact());
 			}else{
 				if(phone != null && phone.length() > 0){
 					etContact.setText(phone);
 				}
 			}
 		}else if (postBean.getName().equals(PostCommonValues.STRING_DESCRIPTION) && layout != null){
 			etDescription = (EditText) layout.getTag(PostCommonValues.HASH_CONTROL);
 		}
 		
 		if(layout != null){
 			layout_txt.addView(layout);
 		}
 	}
 
 	private void popupCategorySelectionDialog(){
 		Bundle bundle = createArguments(null, null);
 		bundle.putSerializable("items", (Serializable) Arrays.asList(PostCommonValues.mainCategories));
 		bundle.putInt("maxLevel", 1);
 		bundle.putInt(ARG_COMMON_REQ_CODE, MSG_CATEGORY_SEL_BACK);
 		if(categoryEnglishName != null && !categoryEnglishName.equals("") && categoryName != null) {
 			bundle.putString("selectedValue", categoryName);
 		}
 		PostUtil.extractInputData(layout_txt, params);
 		CustomDialogBuilder cdb = new CustomDialogBuilder(getActivity(), PostGoodsFragment.this.getHandler(), bundle);
 		cdb.start();
 	}
 	
 	private void addCategoryItem(){
 		Activity activity = getActivity();
 		if(editMode)return;
 		if(layout_txt != null){
 			if(layout_txt.findViewById(R.id.arrow_down) != null) return;
 		}
 		LayoutInflater inflater = LayoutInflater.from(activity);
 		View categoryItem = inflater.inflate(R.layout.item_post_select, null);
 		
 		categoryItem.setTag(PostCommonValues.HASH_CONTROL, categoryItem.findViewById(R.id.posthint));//tag
 		((TextView)categoryItem.findViewById(R.id.postshow)).setText("分类");
 		categoryItem.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				Tracker.getInstance().event(BxEvent.POST_INPUTING).append(Key.ACTION, "类目").end();
 				popupCategorySelectionDialog();
 			}				
 		});//categoryItem.setOnClickListener
 		
 		if(categoryEnglishName != null && !categoryEnglishName.equals("") && categoryName != null){
 			 ((TextView)categoryItem.findViewById(R.id.posthint)).setText(categoryName);
 		}
 		PostUtil.adjustMarginBottomAndHeight(categoryItem);
 		layout_txt.addView(categoryItem);
 	}
 	
 	private void buildFixedPostLayout(HashMap<String, PostGoodsBean> pl){
 		if(pl == null || pl.size() == 0) return;
 		
 		HashMap<String, PostGoodsBean> pm = new HashMap<String, PostGoodsBean>();
 		Object[] postListKeySetArray = pl.keySet().toArray();
 		for(int i = 0; i < pl.size(); ++ i){
 			for(int j = 0; j < PostCommonValues.fixedItemNames.length; ++ j){
 				PostGoodsBean bean = pl.get(postListKeySetArray[i]);
 				if(bean.getName().equals(PostCommonValues.fixedItemNames[j])){					
 					pm.put(PostCommonValues.fixedItemNames[j], bean);
 					break;
 				}
 			}
 		}
 		
 		if(pm.containsKey(PostCommonValues.STRING_DESCRIPTION)){
 			PostGoodsBean bean = pm.get(PostCommonValues.STRING_DESCRIPTION);
 			if(bean != null){
 				View v = layout_txt.findViewById(R.id.img_description);
 				EditText text = (EditText)v.findViewById(R.id.description_input);
 				text.setText("");
 				text.setOnTouchListener(new OnTouchListener() {
 					@Override
 					public boolean onTouch(View v, MotionEvent event) {
 						if (event.getAction() == MotionEvent.ACTION_DOWN) {
 							Tracker.getInstance().event((editMode)?BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, PostCommonValues.STRING_DESCRIPTION).end();
 						}
 						return false;
 					}
 				});
 
 				text.setHint("请输入" + bean.getDisplayName());
 				v.setTag(PostCommonValues.HASH_POST_BEAN, bean);
 				v.setTag(PostCommonValues.HASH_CONTROL, text);
 				v.setOnClickListener(this);
 				
 				v.findViewById(R.id.myImg).setOnClickListener(this);
 				((ImageView)v.findViewById(R.id.myImg)).setImageBitmap(ImageCacheManager.getInstance().loadBitmapFromResource(R.drawable.btn_add_picture));
 				if(imgSelBundle != null){
 		    		Object[] container = (Object[])imgSelBundle.getSerializable(ImageSelectionDialog.KEY_IMG_CONTAINER);
 					if(container != null && container.length > 0
 							&& ((ImageSelectionDialog.ImageContainer)container[0]).status == ImageSelectionDialog.ImageStatus.ImageStatus_Normal){
 //						Bitmap bp = ImageSelectionDialog.getThumbnailWithPath(((ImageSelectionDialog.ImageContainer)container[0]).thumbnailPath);
 						Bitmap bp = this.firstImage;
 						if(bp != null){
 							((ImageView)v.findViewById(R.id.myImg)).setImageBitmap(bp);
 							((TextView)v.findViewById(R.id.imgCout)).setVisibility(View.VISIBLE);
 							int count = photoList == null ? 0 : photoList.size();
 //							for(int i = 0; i < container.length; ++ i){
 //								if(((ImageSelectionDialog.ImageContainer)container[i]).status == ImageSelectionDialog.ImageStatus.ImageStatus_Unset){
 //									break;
 //								}
 //								++ count;
 //							}
 							((TextView)v.findViewById(R.id.imgCout)).setText(String.valueOf(count));
 						}
 					}
 				}				
 			}			
 		}
 		
 		for(int i = 0; i < PostCommonValues.fixedItemNames.length; ++ i){
 			if(pm.containsKey(PostCommonValues.fixedItemNames[i]) && !PostCommonValues.fixedItemNames[i].equals(PostCommonValues.STRING_DESCRIPTION)){
 				this.appendBeanToLayout(pm.get(PostCommonValues.fixedItemNames[i]));
 			}else if(!pm.containsKey(PostCommonValues.fixedItemNames[i])){
 				params.remove(PostCommonValues.fixedItemNames[i]);
 			}
 		}
 	}
 	
 	private void addHiddenItemsToParams(){
 		if (postList == null || postList.isEmpty())
 			return ;
 		Set<String> keySet = postList.keySet();
 		for (String key : keySet){
 			PostGoodsBean bean = postList.get(key);
 			for (int i = 0; i< PostCommonValues.hiddenItemNames.length; i++){
 				if (bean.getName().equals(PostCommonValues.hiddenItemNames[i])){
 					String defaultValue = bean.getDefaultValue();
 					if (defaultValue != null && defaultValue.length() > 0) {
 						this.params.put(bean.getName(), defaultValue, defaultValue);
 					} else {
 						this.params.put(bean.getName(), bean.getLabels().get(0), bean.getValues().get(0));
 					}
 					break;
 				}
 			}
 		}
 	}
 	
 	protected void buildPostLayout(HashMap<String, PostGoodsBean> pl){
 		this.getView().findViewById(R.id.goodscontent).setVisibility(View.VISIBLE);
 		this.getView().findViewById(R.id.networkErrorView).setVisibility(View.GONE);
 		this.reCreateTitle();
 		this.refreshHeader();
 		if(pl == null || pl.size() == 0){
 			return;
 		}
 		buildFixedPostLayout(pl);
 		addHiddenItemsToParams();
 		
 		Object[] postListKeySetArray = pl.keySet().toArray();
 		for (int i = 0; i < pl.size(); i++) {
 			String key = (String) postListKeySetArray[i];
 			PostGoodsBean postBean = pl.get(key);
 			
 			if(PostUtil.inArray(postBean.getName(), PostCommonValues.fixedItemNames) || postBean.getName().equals("title") || PostUtil.inArray(postBean.getName(), PostCommonValues.hiddenItemNames))
 				continue;
 			
 			if(postBean.getName().equals(PostCommonValues.STRING_AREA)){
 				continue;
 			}
 			this.appendBeanToLayout(postBean);
 		}
 	}
 
 	private void updateImageInfo(View rootView) {
 		if(rootView != null){
 			ImageView iv = (ImageView)rootView.findViewById(R.id.myImg);
 			if (iv != null && firstImage != null) {
 				iv.setImageBitmap(this.firstImage);
 			}
 			else if (iv != null) {
 				iv.setImageBitmap(ImageCacheManager.getInstance().loadBitmapFromResource(R.drawable.btn_add_picture));
 			}
 			
 			TextView tv = (TextView) rootView.findViewById(R.id.imgCout);
 			if(tv != null){
 				int containerCount = photoList == null ? 0 : photoList.size();
 				if(containerCount > 0){
 					tv.setText(String.valueOf(containerCount));
 					tv.setVisibility(View.VISIBLE);
 				}else{
 					tv.setVisibility(View.INVISIBLE);
 				}
 			}
 		}
 		
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void handleMessage(Message msg, final Activity activity, View rootView) {
 		hideProgress();
 		
 		switch (msg.what) {
 		case MSG_DIALOG_BACK_WITH_DATA:{
 			Bundle bundle = (Bundle)msg.obj;
 			handleBackWithData(bundle.getInt(ARG_COMMON_REQ_CODE), bundle.getSerializable("lastChoise"));
 			break;
 		}		
 		case MSG_UPDATE_IMAGE_LIST:
 		{
 			updateImageInfo(rootView);
 			break;
 		}
 		case ImageSelectionDialog.MSG_IMG_SEL_DISMISSED:{
 			if(imgSelBundle != null){
 				ImageSelectionDialog.ImageContainer[] container = 
 						(ImageSelectionDialog.ImageContainer[])imgSelBundle.getSerializable(ImageSelectionDialog.KEY_IMG_CONTAINER);
 				if(getView() != null && container != null){
 					ImageView iv = (ImageView)this.getView().findViewById(R.id.myImg);
 					if(iv != null){						
 						if(container != null 
 								&& container.length > 0
 								&& container[0].status == ImageSelectionDialog.ImageStatus.ImageStatus_Normal
 								&& container[0].bitmapPath != null){
 							Bitmap thumbnail = ImageSelectionDialog.getThumbnailWithPath(container[0].thumbnailPath);
 							if(iv != null && thumbnail != null){
 								iv.setImageBitmap(thumbnail);
 							}else{
 								iv.setImageBitmap(ImageCacheManager.getInstance().loadBitmapFromResource(R.drawable.btn_add_picture));
 							}
 						}else{
 							iv.setImageBitmap(ImageCacheManager.getInstance().loadBitmapFromResource(R.drawable.btn_add_picture));
 						}
 					}
 					
 					TextView tv = (TextView)getView().findViewById(R.id.imgCout);
 					if(iv != null){
 						int containerCount = 0;
 						for(int i = 0; i < container.length; ++ i){
 							if(container[i].status == ImageSelectionDialog.ImageStatus.ImageStatus_Unset){
 								break;
 							}else if(container[i].status == ImageSelectionDialog.ImageStatus.ImageStatus_Normal){
 								++ containerCount;
 							}
 						}
 						if(containerCount > 0){
 							tv.setText(String.valueOf(containerCount));
 							tv.setVisibility(View.VISIBLE);
 						}else{
 							tv.setVisibility(View.INVISIBLE);
 						}
 					}
 					
 					bmpUrls.clear();
 					if(container != null){
 						for(int i = 0; i < container.length; ++ i){
 							if(container[i].status == ImageSelectionDialog.ImageStatus.ImageStatus_Normal){
 								bmpUrls.add(container[i].bitmapUrl);
 							}
 						}
 					}				
 				}
 			}
 		}
 			break;
 		case PostCommonValues.MSG_GET_META_SUCCEED:
 			postList = (LinkedHashMap<String, PostGoodsBean>)msg.obj;
 			addCategoryItem();
 			buildPostLayout(postList);
 			loadCachedData();
 			break;
 
 		case PostCommonValues.MSG_GET_META_FAIL:
 			hideProgress();
 			this.getView().findViewById(R.id.goodscontent).setVisibility(View.GONE);
 			this.getView().findViewById(R.id.networkErrorView).setVisibility(View.VISIBLE);
 			this.reCreateTitle();
 			this.refreshHeader();
 
 			break;
 		case PostCommonValues.MSG_POST_SUCCEED:
 			hideProgress();
 			
 			doClearUpImages();
 			
 			String id = ((PostResultData)msg.obj).id;
 			boolean isRegisteredUser = ((PostResultData)msg.obj).isRegisteredUser;
 			String message = ((PostResultData)msg.obj).message;
 			int code = ((PostResultData)msg.obj).error;
 			if (!id.equals("") && code == 0) {
 				postResultSuccess();
 				Toast.makeText(activity, message, 0).show();
 				final Bundle args = createArguments(null, null);
 				args.putInt("forceUpdate", 1);
 				resetData(!editMode);
 				if(!editMode){
 					showPost();
 					String lp = getArguments().getString("lastPost");
 					if(lp != null && !lp.equals("")){
 						lp += "," + id;
 					}else{
 						lp = id;
 					}
 					args.putString("lastPost", lp);
 					
 					args.putString("cateEnglishName", categoryEnglishName);
 					args.putBoolean(KEY_IS_EDITPOST, editMode);
 					
 					args.putBoolean(KEY_LAST_POST_CONTACT_USER,  isRegisteredUser);
 					if(activity != null){							
 						args.putInt(MyAdFragment.TYPE_KEY, MyAdFragment.TYPE_MYPOST);
 						
 						Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_POST_FINISH);
 						intent.putExtras(args);
 						activity.sendBroadcast(intent);
 					}						
 				}else{
 					PostGoodsFragment.this.finishFragment(PostGoodsFragment.MSG_POST_SUCCEED, null);
 				}
 			}else{
 				postResultFail(message);
 				if(code == 505){
 					AlertDialog.Builder bd = new AlertDialog.Builder(this.getActivity());
 	                bd.setTitle("")
 	                        .setMessage(message)
 	                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
 	                        	@Override
 	                            public void onClick(DialogInterface dialog, int which) {
 	                                dialog.dismiss();
 	        						if(activity != null){
 	        							resetData(true);
 	        							showPost();
 	        							Bundle args = createArguments(null, null);
 	        							args.putInt(MyAdFragment.TYPE_KEY, MyAdFragment.TYPE_MYPOST);
 	        							Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_POST_FINISH);
 	        							intent.putExtras(args);
 	        							activity.sendBroadcast(intent);							
 	        						}
 	                            }
 	                        });
 	                AlertDialog alert = bd.create();
 	                alert.show();	
 				}
 			}
 			break;
 		case PostCommonValues.MSG_POST_FAIL:
 			hideProgress();
 			if(msg.obj != null){
 				Toast.makeText(activity, (String)msg.obj, 0).show();
 			}
 			break;
 		case ErrorHandler.ERROR_SERVICE_UNAVAILABLE:
 			hideProgress();
 			ErrorHandler.getInstance().handleMessage(msg);
 //			this.getView().findViewById(R.id.goodscontent).setVisibility(View.GONE);
 //			this.getView().findViewById(R.id.networkErrorView).setVisibility(View.VISIBLE);		
 //			this.reCreateTitle();
 //			this.refreshHeader();
 			break;
 		case MSG_GEOCODING_TIMEOUT:
 		case PostCommonValues.MSG_GEOCODING_FETCHED:			
 			showSimpleProgress();
 			postAd(msg.obj == null ? null : (BXLocation)msg.obj);
 			break;
 		case PostCommonValues.MSG_GPS_LOC_FETCHED:
 			detailLocation = (BXLocation)msg.obj;
 			break;
 		}
 	}
 
 	private int imgHeight = 0;
 	
 	////to fix stupid system error. all text area will be the same content after app is brought to front when activity not remain is checked
 	private void setInputContent(){		
 		if(layout_txt == null) return;
 		for(int i = 0; i < layout_txt.getChildCount(); ++ i){
 			View v = layout_txt.getChildAt(i);
 			PostGoodsBean bean = (PostGoodsBean)v.getTag(PostCommonValues.HASH_POST_BEAN);
 			if(bean == null) continue;
 			View control = (View)v.getTag(PostCommonValues.HASH_CONTROL);
 			if(control != null && control instanceof TextView){
 				if(params != null && params.containsKey(bean.getName())){
 					String value = params.getUiData(bean.getName());
 					if(value == null){
 						value = params.getUiData(bean.getName());
 					}
 					if(bean.getName().equals("contact")){
 						if(editMode){
 							((TextView)control).setText(getAdContact());
 						}else{
 							String phone = GlobalDataManager.getInstance().getPhoneNumber();
 							if(phone != null && phone.length() > 0){
 								((TextView)control).setText(phone);
 								continue;
 							}
 						}
 					}
 					((TextView)control).setText(value);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onStart(){
 		super.onStart();
 		setInputContent();
 	}
 	
 	@Override
 	public void initTitle(TitleDef title){
 		title.m_visible = true;
 		title.m_leftActionHint = "返回";
 		title.m_title = "免费发布";//(categoryName == null || categoryName.equals("")) ? "发布" : categoryName;
 	}
 	
 	private ViewGroup createItemByPostBean(PostGoodsBean postBean){
 		Activity activity = getActivity();
 		ViewGroup layout = PostUtil.createItemByPostBean(postBean, activity);
 
 		if (layout == null)
 			return null;
 
 		if(postBean.getControlType().equals("select") || postBean.getControlType().equals("checkbox")){
 			final String actionName = ((PostGoodsBean)layout.getTag(PostCommonValues.HASH_POST_BEAN)).getDisplayName();
 			layout.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					Tracker.getInstance().event((!editMode) ? BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, actionName).end();
 
 					PostGoodsBean postBean = (PostGoodsBean) v.getTag(PostCommonValues.HASH_POST_BEAN);
 
 					if (postBean.getControlType().equals("select") || postBean.getControlType().equals("tableSelect")) {
 							if(postBean.getLevelCount() > 0){
 									ArrayList<MultiLevelSelectionFragment.MultiLevelItem> items = 
 											new ArrayList<MultiLevelSelectionFragment.MultiLevelItem>();
 									for(int i = 0; i < postBean.getLabels().size(); ++ i){
 										MultiLevelSelectionFragment.MultiLevelItem t = new MultiLevelSelectionFragment.MultiLevelItem();
 										t.txt = postBean.getLabels().get(i);
 										t.id = postBean.getValues().get(i);
 										items.add(t);
 									}
 									Bundle bundle = createArguments(null, null);
 									bundle.putInt(ARG_COMMON_REQ_CODE, postBean.getName().hashCode());
 									bundle.putSerializable("items", items);
 									bundle.putInt("maxLevel", postBean.getLevelCount() - 1);
 									String selectedValue = null;
 									selectedValue = params.getData(postBean.getName());
 									
 									if (selectedValue != null)
 										bundle.putString("selectedValue", selectedValue);
 
 									PostUtil.extractInputData(layout_txt, params);
 									CustomDialogBuilder cdb = new CustomDialogBuilder(getActivity(), getHandler(), bundle);
 									cdb.start();
 							}else{
 								Bundle bundle = createArguments(postBean.getDisplayName(), null);
 								bundle.putInt(ARG_COMMON_REQ_CODE, postBean.getName().hashCode());
 								bundle.putBoolean("singleSelection", false);
 								bundle.putSerializable("properties",(ArrayList<String>) postBean.getLabels());
 								TextView txview = (TextView)v.getTag(PostCommonValues.HASH_CONTROL);
 								if (txview !=  null)
 								{
 									bundle.putString("selected", txview.getText().toString());
 								}
 								((BaseActivity)getActivity()).pushFragment(new OtherPropertiesFragment(), bundle, false);
 							}//postBean.getLevelCount() <= 0
 					}else if(postBean.getControlType().equals("checkbox")){
 						if(postBean.getLabels().size() > 1){
 							Bundle bundle = createArguments(postBean.getDisplayName(), null);
 							bundle.putInt(ARG_COMMON_REQ_CODE, postBean.getName().hashCode());
 							bundle.putBoolean("singleSelection", false);
 							bundle.putSerializable("properties",(ArrayList<String>) postBean.getLabels());
 							TextView txview = (TextView)v.getTag(PostCommonValues.HASH_CONTROL);
 							if (txview !=  null){
 								bundle.putString("selected", txview.getText().toString());
 							}
 							((BaseActivity)getActivity()).pushFragment(new OtherPropertiesFragment(), bundle, false);
 						}
 						else{
 							View checkV = v.findViewById(R.id.checkitem);
 							if(checkV != null && checkV instanceof CheckBox){
 								((CheckBox)checkV).setChecked(!((CheckBox)checkV).isChecked());
 							}
 						}
 					}
 				}
 			});//layout.setOnClickListener:select or checkbox
 		} else {//not select or checkbox
 			final String actionName = ((PostGoodsBean)layout.getTag(PostCommonValues.HASH_POST_BEAN)).getDisplayName();
 			((View)layout.getTag(PostCommonValues.HASH_CONTROL)).setOnTouchListener(new OnTouchListener() {
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					if (event.getAction() == MotionEvent.ACTION_DOWN) {
 						Tracker.getInstance().event((!editMode) ? BxEvent.POST_INPUTING:BxEvent.EDITPOST_INPUTING).append(Key.ACTION, actionName).end();
 					}
 					return false;
 				}
 			});
 			
 			layout.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					View ctrl = (View) v.getTag(PostCommonValues.HASH_CONTROL);
 					ctrl.requestFocus();
 					InputMethodManager inputMgr = (InputMethodManager) ctrl.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 					inputMgr.showSoftInput(ctrl, InputMethodManager.SHOW_IMPLICIT);
 				}
 			});			
 		}		
 		PostUtil.adjustMarginBottomAndHeight(layout);
 		
 		return layout;
 	}
 	
 	private void setDetailLocationControl(BXLocation location){
 		if(location == null) return;
 		if(locationView != null && locationView.findViewById(R.id.postinput) != null){
 			String address = (location.detailAddress == null || location.detailAddress.equals("")) ? 
             		((location.subCityName == null || location.subCityName.equals("")) ?
 							"" 
 							: location.subCityName)
 					: location.detailAddress;
             if(address == null || address.length() == 0) return;
             if(location.adminArea != null && location.adminArea.length() > 0){
             	address = address.replaceFirst(location.adminArea, "");
             }
             if(location.cityName != null && location.cityName.length() > 0){
             	address = address.replaceFirst(location.cityName, "");
             }
 			((TextView)locationView.findViewById(R.id.postinput)).setText(address);
 		}		
 	}
 	
 	public boolean hasGlobalTab() {
 		return false;
 	}
 }
