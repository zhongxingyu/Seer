 package com.baixing.view.fragment;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.location.Location;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Message;
 import android.os.Parcelable;
 import android.provider.MediaStore;
 import android.text.InputType;
 import android.util.Log;
 import android.util.Pair;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.baixing.adapter.CheckableAdapter;
 import com.baixing.adapter.CheckableAdapter.CheckableItem;
 import com.baixing.broadcast.CommonIntentAction;
 import com.baixing.entity.BXLocation;
 import com.baixing.entity.GoodsDetail;
 import com.baixing.entity.PostGoodsBean;
 import com.baixing.entity.UserBean;
 import com.baixing.imageCache.SimpleImageLoader;
 import com.baixing.jsonutil.JsonUtil;
 import com.baixing.util.Communication;
 import com.baixing.util.LocationService;
 import com.baixing.util.LocationService.BXRgcListener;
 import com.baixing.util.TrackConfig.TrackMobile.BxEvent;
 import com.baixing.util.TrackConfig.TrackMobile.Key;
 import com.baixing.util.TrackConfig.TrackMobile.PV;
 import com.baixing.widget.ImageSelectionDialog;
 import com.baixing.util.Tracker;
 import com.baixing.util.Util;
 import com.baixing.util.ViewUtil;
 import com.baixing.view.fragment.MultiLevelSelectionFragment.MultiLevelItem;
 import com.baixing.widget.CustomDialogBuilder;
 import com.quanleimu.activity.BaseActivity;
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 
 public class PostGoodsFragment extends BaseFragment implements BXRgcListener, OnClickListener, QuanleimuApplication.onLocationFetchedListener, OnKeyListener{
 	
 	public static final int MSG_START_UPLOAD = 5;
 	public static final int MSG_FAIL_UPLOAD = 6;
 	public static final int MSG_SUCCED_UPLOAD = 7;
 	private static final int MSG_GETLOCATION_TIMEOUT = 8;
 	
 	
 	private static final int VALUE_LOGIN_SUCCEEDED = 9;
 	
 	private static final int MSG_GEOCODING_FETCHED = 0x00010010;
 	private static final int MSG_GEOCODING_TIMEOUT = 0x00010011;
 	
 	static final public int HASH_POST_BEAN = "postBean".hashCode();
 	static final public int HASH_CONTROL = "control".hashCode();
 	static final private int MSG_MORE_DETAIL_BACK = 0xF0000001;
 	
 	static final public String KEY_INIT_CATEGORY = "cateNames";
 	static final public String KEY_LAST_POST_CONTACT_USER = "lastPostContactIsRegisteredUser";
 	static final public String KEY_IS_EDITPOST = "isEditPost"; 
 	static final public String KEY_CATE_ENGLISHNAME = "cateEnglishName";
 	static final private String STRING_DETAIL_POSITION = "具体地点";
 	static final private String STRING_AREA = "地区";
 	static final private String FILE_LAST_CATEGORY = "lastCategory";
 	static final private String STRING_DESCRIPTION = "description";
 	
 	public static final int MSG_POST_SUCCEED = 0xF0000010; 
 	public String categoryEnglishName = "";
 	public String categoryName = "";
 	public String json = "";
 	public LinearLayout layout_txt;
 	public LinkedHashMap<String, PostGoodsBean> postList;		//发布模板每一项的集合
 	public static final int NONE = 0;
 //	public static final int PHOTOHRAPH = 1;
 //	public static final int PHOTOZOOM = 2; 
 	public static final int PHOTORESOULT = 3;
 	public static final int POST_LIST = 4;
 	public static final int POST_OTHERPROPERTIES = 5;
 	public static final int POST_CHECKSELECT = 6;
 	public static final int MSG_MULTISEL_BACK = 10;
 	public static final int MSG_CATEGORY_SEL_BACK = 11;
 	public static final int MSG_DIALOG_BACK_WITH_DATA = 12;
 	public static final String IMAGEUNSPECIFIED = "image/*";
 
 	private PostParamsHolder params;
 	private PostParamsHolder originParams;
 	
 //	private AlertDialog ad; 
 //	private Button photoalbum, photomake, photocancle;
 	private ArrayList<String> origin_bitmap_url;
 	private String mobile, password;
 	private UserBean user;
 	private GoodsDetail goodsDetail;
 	public ArrayList<String> listUrl;
 	private Bundle imgSelBundle = null;
 	private ImageSelectionDialog imgSelDlg = null;
 	
 	private View locationView = null;
 //	private View districtView = null;
 	
 	private BXLocation detailLocation = null;
     private BXLocation cacheLocation = null;
     
     private List<String> bmpUrls = new ArrayList<String>();
 //	private ArrayList<String> otherProperties = new ArrayList<String>();
 	
 //	private View categoryItem = null;
 	
     
     private EditText etDescription = null;
     private EditText etContact = null;
     
     @Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == NONE) {
 			return;
 		}
 
 		if(this.imgSelDlg != null &&
 				(requestCode == CommonIntentAction.PhotoReqCode.PHOTOHRAPH
 				|| requestCode == CommonIntentAction.PhotoReqCode.PHOTOZOOM
 				|| requestCode == PHOTORESOULT)){
 			imgSelDlg.onActivityResult(requestCode, resultCode, data);
 		}
     }
     
     private static final String []texts = {"物品交易", "车辆买卖", "房屋租售", "全职招聘", 
 		   "兼职招聘", "求职简历", "交友活动", "宠物", 
 		   "生活服务", "教育培训"};
     
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
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 //		this.postLayoutCreated = false;
 		
 		String categoryNames = this.getArguments().getString(KEY_INIT_CATEGORY);
 		initWithCategoryNames(categoryNames);
 		
 		
 		this.goodsDetail = (GoodsDetail) getArguments().getSerializable("goodsDetail");
 		postList = new LinkedHashMap<String, PostGoodsBean>();
 		
 		params = new PostParamsHolder();
 		originParams = new PostParamsHolder();
 		
 		listUrl = new ArrayList<String>();
 //		origin_bitmap_url = (ArrayList<String>) bitmap_url.clone();
 		
 //		currentImgView = -1;
 //		uploadCount = 0;
 //		
 //		cachedBps = new Bitmap[] {null, null, null};
 //		
 		if (savedInstanceState != null)
 		{
 			postList.putAll( (HashMap)savedInstanceState.getSerializable("postList"));
 			params = (PostParamsHolder) savedInstanceState.getSerializable("params");
 			listUrl.addAll((List) savedInstanceState.getSerializable("listUrl"));
 //			bitmap_url.clear();
 //			bitmap_url.addAll((List) savedInstanceState.getSerializable("bitmapUrl"));
 //			Util.filterArrayList(bitmap_url, 3);
 //			currentImgView = savedInstanceState.getInt("imgIndex", -1);
 //			uploadCount = savedInstanceState.getInt("uploadCount", 0);
 			imgHeight = savedInstanceState.getInt("imgHeight");
 			Parcelable[] ps = savedInstanceState.getParcelableArray("imgs");
 //			cachedBps = new Bitmap[ps.length];
 //			int i = 0;
 //			for (Parcelable p : ps)
 //			{
 //				cachedBps[i++] = (Bitmap) p;
 //			}
 		}
 		
 		user = (UserBean) Util.loadDataFromLocate(this.getActivity(), "user", UserBean.class);
 		if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
 			mobile = user.getPhone();
 			password = user.getPassword();
 		}
 	}
 		
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		
 		synchronized(this){
 			outState.putSerializable("params", params);
 			outState.putSerializable("postList", postList);
 			outState.putSerializable("listUrl", listUrl);
 //			outState.putSerializable("bitmapUrl", bitmap_url);
 //			outState.putInt("imgIndex", currentImgView);
 //			outState.putInt("uploadCount", uploadCount);
 			outState.putInt("imgHeight", imgHeight);
 //			outState.putParcelableArray("imgs", cachedBps);
 		}
 	}
 	
 	
 
 
 
 	@Override
 	public void onDestroy() {
 //		for(int i = 0; i < 3; ++ i){
 //			File file = new File(Environment.getExternalStorageDirectory(), "temp" + i + ".jpg");
 //			if(file.exists()){
 //				file.delete();
 //			}
 //			file = null;
 //		}
 		super.onDestroy();
 	}
 	
 	private void ConfirmAbortAlert(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
 		
 		builder.setTitle(R.string.dialog_title_info)
 				.setMessage(R.string.dialog_message_discard_input)
 				.setNegativeButton(R.string.no, null)
 				.setPositiveButton(R.string.yes,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 //									m_viewInfoListener.onExit(PostGoodsView.this);//FIXME:
 								finishFragment();
 							}
 						});
 		
 		builder.create().show();
 	}
 	
 	
 	
 
 	@Override
 	public boolean handleBack() {
 		if(imgSelDlg != null)
 			if(imgSelDlg.handleBack()){
 				return true;
 		}
 		if(filled()){
 			ConfirmAbortAlert();
 			return true;
 		}
 		
 		return super.handleBack();
 	}
 	
 	
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 	}
 	
 	
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		if (goodsDetail!=null) {//edit
 			this.pv = PV.EDITPOST;
 			Tracker.getInstance()
 			.pv(this.pv)
 			.append(Key.SECONDCATENAME, categoryEnglishName)
 			.append(Key.ADID, goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
 			.end();
 		}
 		else {//new post
 			this.pv = PV.POST;
 			Tracker.getInstance()
 			.pv(this.pv)
 			.append(Key.SECONDCATENAME, categoryEnglishName)
 			.end();
 		}		
 	}
 	
 	public void onPause() {
 		QuanleimuApplication.getApplication().removeLocationListener(this);		
 		extractInputData(layout_txt, params);
 //		this.postLayoutCreated = false;
 		super.onPause();
 	}
 	
 //	private boolean inLocating = false;
 	@Override
 	public void onStackTop(boolean isBack) {
 //		if(!userValidated){
 //			usercheck();
 //		}
 //		else
 		{
 //			this.showPost();
 		}
 		if(isBack){
 //			this.detailLocation = null;
 			final ScrollView scroll = (ScrollView) this.getView().findViewById(R.id.goodscontent);
 			scroll.post(new Runnable() {            
 			    @Override
 			    public void run() {
 			           scroll.fullScroll(View.FOCUS_DOWN);              
 			    }
 			});
 		}
 		if(!isBack && this.goodsDetail == null){
 //			inLocating = true;
 			QuanleimuApplication.getApplication().addLocationListener(this);
 //			handler.sendEmptyMessageDelayed(MSG_GETLOCATION_TIMEOUT, 100);
 		}
 		
 	}
 	
 	
 
 	@Override
 	public void onDestroyView() {
 		super.onDestroyView();
 	}
 
 	@Override
 	public void onViewCreated (View view, Bundle savedInstanceState){
 		super.onViewCreated(view, savedInstanceState);
 		showPost();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		View v = inflater.inflate(R.layout.postgoodsview, null);
 		
 		layout_txt = (LinearLayout) v.findViewById(R.id.layout_txt);
 //		v.findViewById(R.id.image_layout).setVisibility(View.GONE);
 		Button button = (Button) v.findViewById(R.id.iv_post_finish);
 		button.setOnClickListener(this);
 		if (goodsDetail == null)
 			button.setText("立即免费发布");
 		else
 			button.setText("立即更新信息");
 		
 
 		getActivity().getWindow().setSoftInputMode(
 				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		
 		return v;
 	}
 	
 	private static String getDisplayValue(PostGoodsBean bean, GoodsDetail detail, String detailKey){
 		if(bean == null || detail == null || detailKey == null || detailKey.equals(""))return "";
 		String value = detail.getValueByKey(detailKey);
 		String displayValue = "";
 		if(bean.getControlType().equals("input") || bean.getControlType().equals("textarea")){
 			displayValue = detail.getValueByKey(detailKey);
 			if(displayValue != null && !bean.getUnit().equals("")){
 				int pos = displayValue.indexOf(bean.getUnit());
 				if(pos != -1){
 					displayValue = displayValue.substring(0, pos);
 				}
 			}
 			return displayValue;
 		}
 		else if(bean.getControlType().equals("select") || bean.getControlType().equals("checkbox")){
 			List<String> beanVs = bean.getValues();
 			if(beanVs != null){
 				for(int t = 0; t < beanVs.size(); ++ t){
 					if(bean.getControlType().equals("checkbox") && bean.getLabels() != null && bean.getLabels().size() > 1){
 						if(value.contains(beanVs.get(t))){
 							displayValue += (displayValue.equals("") ? "" : ",") + bean.getLabels().get(t);
 							continue;
 						}
 					}
 					if(beanVs.get(t).equals(value)){
 						displayValue = bean.getLabels().get(t);
 						break;
 					}
 				}
 			}
 			if(displayValue.equals("")){
 				String _sValue = detail.getValueByKey(detailKey + "_s"); 
 				if(_sValue != null && !_sValue.equals("")){
 					return _sValue;
 				}
 			}
 		}
 		return displayValue;
 	}
 	
 	private void startImgSelDlg(ArrayList<String> bmpUrls, ArrayList<Bitmap> cachedBps, ArrayList<String> thumbUrls){
 		if(imgSelBundle == null){
 			imgSelBundle =  new Bundle();
 			imgSelBundle.putSerializable(ImageSelectionDialog.KEY_BITMAP_URL, bmpUrls);
 			imgSelBundle.putSerializable(ImageSelectionDialog.KEY_CACHED_BPS, cachedBps);
 			imgSelBundle.putSerializable(ImageSelectionDialog.KEY_THUMBNAIL_URL, thumbUrls);
 		}
 		
 		if(imgSelDlg == null){
 			imgSelDlg = new ImageSelectionDialog(imgSelBundle);
 			imgSelDlg.setMsgOutHandler(handler);
 		}
 		imgSelDlg.show(getFragmentManager(), null);
 		
 	}
 	
 	private void editpostUI() {
 		if(goodsDetail == null) return;
 		for(int i = 0; i < layout_txt.getChildCount(); ++ i){
 			View v = layout_txt.getChildAt(i);
 			PostGoodsBean bean = (PostGoodsBean)v.getTag(HASH_POST_BEAN);
 			if(bean == null) continue;
 			String detailValue = goodsDetail.getValueByKey(bean.getName());
 			if(detailValue == null || detailValue.equals(""))continue;
 			String displayValue = getDisplayValue(bean, goodsDetail, bean.getName());
 			View control = (View)v.getTag(HASH_CONTROL);
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
 			this.params.put(bean.getDisplayName(), displayValue, detailValue);
 			
 		
 			if(bean.getDisplayName().equals(STRING_AREA)){
 				String strArea = goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
 				String[] areas = strArea.split(",");
 				if(areas.length >= 2){
 					if(control instanceof TextView){
 						((TextView)control).setText(areas[areas.length - 1]);
 					}
 					if(bean.getValues() != null && bean.getLabels() != null){
 						List<String> areaLabels = bean.getLabels();
 						for(int t = 0; t < areaLabels.size(); ++ t){
 							if(areaLabels.get(t).equals(areas[1])){
 //								postMap.put("地区", bean.getValues().get(t));
 								params.getData().put(STRING_AREA, bean.getValues().get(t));
 //								params.put("地区", areas[areas.length - 1], bean.getValues().get(t));
 								break;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		if (goodsDetail.getImageList() != null) {
 			String b = (goodsDetail.getImageList().getResize180());
 //					.substring(1, (goodsDetail.getImageList()
 //							.getResize180()).length() - 1);
 			if(b == null || b.equals("")) return;
 			b = Communication.replace(b);
 			if (b.contains(",")) {
 				String[] c = b.split(",");
 				for (int k = 0; k < c.length; k++) {
 					listUrl.add(c[k]);
 				}
 			}else{
 				listUrl.add(b);
 			}
 			
 			if(listUrl.size() > 0){
 				SimpleImageLoader.showImg(layout_txt.findViewById(R.id.myImg), listUrl.get(0), "", getActivity());
 				((TextView)layout_txt.findViewById(R.id.imgCout)).setText(String.valueOf(listUrl.size()));
 				layout_txt.findViewById(R.id.imgCout).setVisibility(View.VISIBLE);
 			}else{
 				layout_txt.findViewById(R.id.imgCout).setVisibility(View.INVISIBLE);
 			}
 			
 //			String big = (goodsDetail.getImageList().getBig());
 //			big = Communication.replace(big);
 //			String[] cbig = big.split(",");
 //			ArrayList<String> smalls = new ArrayList<String>();
 //			ArrayList<String> bigs = new ArrayList<String>();
 //			for (int j = 0; j < listUrl.size(); j++) {
 //				String bigUrl = (cbig == null || cbig.length <= j) ? null : cbig[j];
 //				smalls.add(listUrl.get(j));
 //				bigs.add(bigUrl);
 //			}
 		}
 	}
 	
 	private void doPost(boolean registered, BXLocation location){
 		showSimpleProgress();
 		new Thread(new UpdateThread(registered, location)).start();		
 	}
 	
 //	private void postNoRegister(){
 //		doPost(false);
 //	}
 
 	private boolean usercheck() {
 		return (user != null && user.getPhone() != null && !user.getPhone().equals(""));
 //			doPost(true);
 //		}
 //		else {
 //			doPost(false);
 //			final String[] names = {"免注册发布","已注册用户发布"};
 //			new AlertDialog.Builder(this.getActivity()).setTitle("请选择")//.setMessage("无法确定当前位置")
 //			.setItems(names, new DialogInterface.OnClickListener(){
 //				
 //				@Override
 //				public void onClick(DialogInterface dialog, int which){
 //					switch(which){
 //						case 0:
 //							postNoRegister();
 //							break;
 //						case 1:
 //							Bundle bundle = createArguments(null, "取消");
 //							bundle.putInt(LoginFragment.KEY_RETURN_CODE, VALUE_LOGIN_SUCCEEDED);
 //							pushFragment(new LoginFragment(), bundle);
 //							break;
 //					}				
 //				}
 //			})
 //			.setNegativeButton("取消", new DialogInterface.OnClickListener(){
 //				@Override
 //				public void onClick(DialogInterface dialog, int which){
 //					dialog.dismiss();
 //				}
 //			}).show();
 //		}
 	}
 		
 	private void deployDefaultLayout(){
 		initImageLayout();
 		addCategoryItem();
 		if(getView() != null){
 			View imgV = getView().findViewById(R.id.myImg);
 			if(imgV != null){
 				imgV.setOnClickListener(this);
 			}
 			
 			View textArea = getView().findViewById(R.id.img_description);
 			if(textArea != null){
 				textArea.setOnClickListener(this);
 			}
 		}
 		LayoutInflater inflater = LayoutInflater.from(getActivity());
 		for(int i = 1; i < fixedItemNames.length; ++ i){	
 			if(fixedItemNames[i].equals(STRING_DESCRIPTION))continue;
 			View v = fixedItemDisplayNames[i].equals(STRING_DETAIL_POSITION) ? 
 					inflater.inflate(R.layout.item_post_location, null) : 
 						inflater.inflate(R.layout.item_post_edit, null);	
 			((TextView)v.findViewById(R.id.postshow)).setText(fixedItemDisplayNames[i]);
 
 			EditText text = (EditText)v.findViewById(R.id.postinput);
 			
 			PostGoodsBean bean = new PostGoodsBean();
 			bean.setControlType("input");
 			bean.setDisplayName(fixedItemDisplayNames[i]);
 			bean.setName(fixedItemNames[i]);
 
 			v.setTag(HASH_CONTROL, text);
 			v.setTag(HASH_POST_BEAN, bean);
 			
 			if(fixedItemNames[i].equals("价格")){
 				text.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
 			}else if(fixedItemNames[i].equals("contact")) {
 				text.setInputType(InputType.TYPE_CLASS_PHONE);
 			}else if(fixedItemNames[i].equals(STRING_DETAIL_POSITION)){
 				v.findViewById(R.id.location).setOnClickListener(this);
 				locationView = v;
 			}
 			LinearLayout.LayoutParams layoutParams = (LayoutParams) v.getLayoutParams();
 			if (layoutParams == null)
 				layoutParams = new LinearLayout.LayoutParams(
 				     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
 			layoutParams.bottomMargin = v.getContext().getResources().getDimensionPixelOffset(R.dimen.post_padding);
 			v.setLayoutParams(layoutParams);
 			layout_txt.addView(v);
 		}
 		loadCachedData();
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
 	
 	private void showPost(){
 		if(this.categoryEnglishName == null || categoryEnglishName.length() == 0){
 			deployDefaultLayout();
 			return;
 		}
 
 		String cityEnglishName = QuanleimuApplication.getApplication().cityEnglishName;
 		if(goodsDetail != null && goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CITYENGLISHNAME).length() > 0){
 			cityEnglishName = goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CITYENGLISHNAME);
 		}
 		
 		Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(this.getActivity(), categoryEnglishName + cityEnglishName);
 		json = pair.second;
 		if (json != null && json.length() > 0) {
 			addCategoryItem();
 			if (pair.first + (24 * 3600) < System.currentTimeMillis()/1000) {
 				showSimpleProgress();
 				new Thread(new GetCategoryMetaThread(cityEnglishName)).start();
 			} else {
 				buildPostLayout();
 				loadCachedData();
 			}
 		} else {
 			showSimpleProgress();
 			new Thread(new GetCategoryMetaThread(cityEnglishName)).start();
 		}
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if(v.getId() == R.id.iv_post_finish){
 			postFinish();
 		}else if(v.getId() == R.id.location){
 			if(this.detailLocation != null && locationView != null){
 				setDetailLocationControl(detailLocation);
 			}else if(detailLocation == null){
 				Toast.makeText(this.getActivity(), "无法获得当前位置", 0).show();
 			}
 		}else if(v.getId() == R.id.myImg){
 			if(goodsDetail != null){
 				if(bmpUrls.size() == 0){
 					String big = (goodsDetail.getImageList().getBig());
 					big = Communication.replace(big);
 					String[] cbig = big.split(",");
 					ArrayList<String> smalls = new ArrayList<String>();
 					ArrayList<String> bigs = new ArrayList<String>();
 					for (int j = 0; j < listUrl.size(); j++) {
 						String bigUrl = (cbig == null || cbig.length <= j) ? null : cbig[j];
 						smalls.add(listUrl.get(j));
 						bigs.add(bigUrl);
 					}
 					startImgSelDlg(bigs, null, smalls);
 				}else{
 					startImgSelDlg(null, null, null);
 				}							
 			}else{
 				startImgSelDlg(null, null, null);
 			}
 		}else if(v.getId() == R.id.img_description){
 			final View et = v.findViewById(R.id.description_input);
 			if(et != null){
 				et.postDelayed(new Runnable(){
 					@Override
 					public void run(){
 						if (et != null)
 						{
 							et.requestFocus();
 							InputMethodManager inputMgr = 
 									(InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 							inputMgr.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
 						}
 					}			
 				}, 100);
 			}
 		}
 //		else if (v == photoalbum) {
 //			// 相册
 //			if (ad.isShowing()) {
 //				ad.dismiss();
 //			}
 //			Intent thirdparty = new Intent(this.getActivity(), ThirdpartyTransitActivity.class);
 //			Bundle ext = new Bundle();
 //			ext.putString(ThirdpartyTransitActivity.ThirdpartyKey, ThirdpartyTransitActivity.ThirdpartyType_Albam);
 //			thirdparty.putExtras(ext);
 //			thirdparty.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 //			activity.startActivity(thirdparty);
 //			
 //		} else if (v == photomake) {
 //			if (ad.isShowing()) {
 //				ad.dismiss();
 //			}
 //			Intent thirdparty = new Intent(this.getActivity(), ThirdpartyTransitActivity.class);
 //			Bundle ext = new Bundle();
 //			ext.putString(ThirdpartyTransitActivity.ThirdpartyKey, ThirdpartyTransitActivity.ThirdpartyType_Photo);
 //			ext.putInt(ThirdpartyTransitActivity.Name_PhotoNumber, this.currentImgView);
 //			thirdparty.putExtras(ext);
 //			thirdparty.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 //			activity.startActivity(thirdparty);
 //
 //		} else if (v == photocancle) {
 //			ad.dismiss();
 //		}
 	}
 	
 	private void postFinish() {
 		Log.d("postgoods",goodsDetail==null?"POST_POSTBTNCONTENTCLICKED":"EDITPOST_POSTBTNCONTENTCLICKED");
 		//tracker
 		Tracker.getInstance()
 		.event(goodsDetail==null?BxEvent.POST_POSTBTNCONTENTCLICKED:BxEvent.EDITPOST_POSTBTNCONTENTCLICKED)
 		.append(Key.SECONDCATENAME, categoryEnglishName)
 		.end();
 		
 		this.postAction();
 	}
 	
 	private boolean gettingLocationFromBaidu = false;
 	@Override
 	public void handleRightAction(){
 		Log.d("postgoods",goodsDetail==null?"POST_POSTBTNHEADERCLICKED":"EDITPOST_POSTBTNHEADERCLICKED");
 		if (this.getView().findViewById(R.id.goodscontent).isShown())
 		{
 			//tracker
 			Tracker.getInstance()
 			.event(goodsDetail==null?BxEvent.POST_POSTBTNHEADERCLICKED:BxEvent.EDITPOST_POSTBTNHEADERCLICKED)
 			.append(Key.SECONDCATENAME, categoryEnglishName)
 			.end();
 			
 			this.postAction();
 		}
 	}
 	
 	private void postAction() {
 		//定位成功的情况下，发布时保存当前经纬度和地理位置
         if (/*inLocating == false &&*/ locationView != null && cacheLocation != null) {
             String inputAddress = ((TextView)locationView.findViewById(R.id.postinput)).getText().toString();
             BXLocation lastLocation = new BXLocation(cacheLocation);
             lastLocation.detailAddress = inputAddress;
             Util.saveDataToLocate(getActivity(), "lastLocation", lastLocation);
         }
 
 //		if(uploadCount > 0){
 //			postResultFail("images are uploading!");
 //			Toast.makeText(this.getActivity(),"图片正在上传" + "!", 0).show();
 //		}
 //		else
 		{
 			extractInputData(layout_txt, params);
 			if(!check2()){
 				return;
 			}
 			if(this.detailLocation != null){
 				doPost(usercheck(), detailLocation);
 			}else{
 				this.sendMessageDelay(MSG_GEOCODING_TIMEOUT, null, 5000);
 				retreiveLocation();
 			}
 		}
 	}
 	
 	static public void extractInputData(ViewGroup vg, PostParamsHolder params){
 		if(vg == null) return;
 		for(int i = 0; i < vg.getChildCount(); ++ i){
 			PostGoodsBean postGoodsBean = (PostGoodsBean)vg.getChildAt(i).getTag(HASH_POST_BEAN);
 			if(postGoodsBean == null) continue;
 			
 			if (postGoodsBean.getControlType().equals("input") 
 					|| postGoodsBean.getControlType().equals("textarea")) {
 				EditText et = (EditText)vg.getChildAt(i).getTag(HASH_CONTROL);
 				if(et != null){
 //					String displayValue = et.getText().toString();
 //					displayValue = displayValue.endsWith(postGoodsBean.getUnit()) ? 
 					params.put(postGoodsBean.getDisplayName(),  et.getText().toString(), et.getText().toString());
 				}
 			}
 			else if(postGoodsBean.getControlType().equals("checkbox")){
 				if(postGoodsBean.getValues().size() == 1){
 					CheckBox box = (CheckBox)vg.getChildAt(i).getTag(HASH_CONTROL);
 					if(box != null){
 						if(box.isChecked()){
 							params.put(postGoodsBean.getDisplayName(), postGoodsBean.getValues().get(0), postGoodsBean.getValues().get(0));
 						}
 						else{
 							params.remove(postGoodsBean.getDisplayName());
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private static String trimUnit(String value, String unit) {
 		if (unit == null || unit.length() == 0 || value == null || value.length() == 0)
 			return value;
 		int pos = value.indexOf(unit);
 		if(pos != -1){
 			value = value.substring(0, pos);
 		}
 		return value;
 	}
 
 	private boolean filled() {//判断是否填入数据
 		// check if images uploaded.
 //		for (String url : bitmap_url)
 //		{
 //			if (url != null && url.startsWith("http://")) 
 //				return true;
 //		}
 //		for (int i = 0; i < bitmap_url.size(); i++) {
 //			String url = bitmap_url.get(i);
 //			String originUrl = origin_bitmap_url.get(i);
 //			if (url != originUrl) {
 //				return true;
 //			}
 //		}
 		
 		extractInputData(layout_txt, params);
 		if(!this.getView().findViewById(R.id.goodscontent).isShown() || 
 				this.params == null || this.params.getData() == null || this.params.getData().size() == 0){
 			return false;
 		}
 		
 		// check if params modified.
 		Set<String> keySet = postList.keySet();
 		for (String key : keySet)
 		{
 			PostGoodsBean bean = postList.get(key);
 			String value = trimUnit(params.getData(bean.getDisplayName()), bean.getUnit());
 			String originValue = trimUnit(originParams.getData(bean.getDisplayName()), bean.getUnit());
 			
 			if (value != null && !value.equals(originValue))
 				return true;
 		}
 		
 		return false;
 	}
 
 	
 	private boolean check2() {
 		LinkedHashMap<String, String> postMap = params.getData();
 		for (int i = 0; i < postList.size(); i++) {
 			String key = (String) postList.keySet().toArray()[i];
 			PostGoodsBean postGoodsBean = postList.get(key);
 			if (postGoodsBean.getName().equals(STRING_DESCRIPTION) || 
 					(postGoodsBean.getRequired().endsWith("required") && ! this.isHiddenItem(postGoodsBean) && !postGoodsBean.getName().equals(STRING_AREA))) {
 				if(!postMap.containsKey(postGoodsBean.getDisplayName()) 
 						|| postMap.get(postGoodsBean.getDisplayName()).equals("")
 						|| (postGoodsBean.getUnit() != null && postMap.get(postGoodsBean.getDisplayName()).equals(postGoodsBean.getUnit()))){
 					if(postGoodsBean.getName().equals("images"))continue;
 					postResultFail("please entering " + postGoodsBean.getDisplayName() + "!");
 					Toast.makeText(this.getActivity(), "请填写" + postGoodsBean.getDisplayName() + "!", 0).show();
 					return false;
 				}
 			}
 		}
 		if(categoryEnglishName.equals("nvzhaonan")){
 			for (int i = 0; i < bmpUrls.size(); i++) {
 				if(bmpUrls.get(i) != null && bmpUrls.get(i).contains("http:")){
 					return true;
 				}
 			}
 			postResultFail("upload an image,let others know you!");
 			Toast.makeText(this.getActivity(), "传张照片吧，让大家更好地认识你^-^" ,0).show();
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * 显示拍照相册对话框
 	 */
 	private void showDialog() {
 //		View view = LinearLayout.inflate(this.getActivity(), R.layout.upload_head, null);
 //		Builder builder = new AlertDialog.Builder(this.getActivity());
 //		builder.setView(view);
 //		ad = builder.create();
 //
 //		WindowManager.LayoutParams lp = ad.getWindow().getAttributes();
 //		lp.y = 300;
 //		ad.onWindowAttributesChanged(lp);
 //		ad.show();
 //
 //		photoalbum = (Button) view.findViewById(R.id.photo_album);
 //		photoalbum.setOnClickListener(this);
 //		photomake = (Button) view.findViewById(R.id.photo_make);
 //		photomake.setOnClickListener(this);
 //		photocancle = (Button) view.findViewById(R.id.photo_cancle);
 //		photocancle.setOnClickListener(this);
 	}
 	
 	private String getFilledLocation(){
 		String toRet = "";
 		for(int m = 0; m < layout_txt.getChildCount(); ++ m){
 			View v = layout_txt.getChildAt(m);
 			PostGoodsBean bean = (PostGoodsBean)v.getTag(HASH_POST_BEAN);
 			if(bean == null) continue;
 			if(bean.getName().equals(STRING_DETAIL_POSITION)){
 				TextView tv = (TextView)v.getTag(HASH_CONTROL);
 				if(tv != null && !tv.getText().toString().equals("")){
 					toRet = tv.getText().toString();
 				}
 				break;
 			}
 		}
 		return toRet;
 	}
 
 	private boolean retreiveLocation(){
 		Log.d("location", "location   retreive location");
 		String city = QuanleimuApplication.getApplication().cityName;
 //		if(goodsDetail != null){
 //			String goodsCity = goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
 //			if(null != goodsCity){
 //				String[]cities = goodsCity.split(",");
 //				if(cities != null && cities.length > 0){
 //					city = cities[0];
 //				}
 //			}
 //		}
 //			for(int m = 0; m < layout_txt.getChildCount(); ++ m){
 //				View v = layout_txt.getChildAt(m);
 //				PostGoodsBean bean = (PostGoodsBean)v.getTag(HASH_POST_BEAN);
 //				if(bean == null) continue;
 //				if(bean.getDisplayName().equals(STRING_AREA)){
 //					TextView tv = (TextView)v.getTag(HASH_CONTROL);
 //					if(tv != null && !tv.getText().toString().equals("")){
 //						city += "," + tv.getText();
 //					}
 //				}
 //			}
 		String addr = getFilledLocation();
 
 		this.showSimpleProgress();
 		this.gettingLocationFromBaidu = true;
 		return LocationService.getInstance().geocode(addr, city, this);
 //		if(!city.equals("")){
 //			String googleUrl = String.format("http://maps.google.com/maps/geo?q=%s&output=csv", city);
 //			try{
 //				String googleJsn = Communication.getDataByUrlGet(googleUrl);
 //				String[] info = googleJsn.split(",");
 //				if(info != null && info.length == 4){
 //					//goodsDetail.setValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT, info[2]);
 //					//goodsDetail.setValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON, info[3]);
 //					list.add("lat=" + info[2]);
 //					list.add("lng=" + info[3]);
 //				}
 //			}catch(UnsupportedEncodingException e){
 //				e.printStackTrace();
 //			}catch(Exception e){
 //				e.printStackTrace();
 //			}
 //		}
 	}
 	
 	private Pair<Double, Double> retreiveCoorFromGoogle(){
 		String city = getFilledLocation();
 		if(city == null || city.equals("")){
 			return new Pair<Double, Double>((double)0, (double)0);
 		}
 		String googleUrl = String.format("http://maps.google.com/maps/geo?q=%s&output=csv", city);
 		try{
 			String googleJsn = Communication.getDataByUrlGet(googleUrl);
 			String[] info = googleJsn.split(",");
 			if(info != null && info.length == 4){
 				return new Pair<Double, Double>(Double.parseDouble(info[2]), Double.parseDouble(info[3]));
 			}
 		}catch(UnsupportedEncodingException e){
 			e.printStackTrace();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		return new Pair<Double, Double>((double)0, (double)0);
 	}
 
 	class UpdateThread implements Runnable {
 		private boolean registered = false;
 		private BXLocation location = null;
 		public UpdateThread(boolean registered, BXLocation location){
 			this.registered = registered;
 			this.location = location;
 		}
 		public void run() {
 			Log.d("location", "location, in UpdateThread::run");
 			String apiName = "ad_add";
 			ArrayList<String> list = new ArrayList<String>();
 
 			if(registered){
 				list.add("mobile=" + mobile);
 				String password1 = Communication.getMD5(password);
 				password1 += Communication.apiSecret;
 				String userToken = Communication.getMD5(password1);
 				list.add("userToken=" + userToken);	
 			}
 			
 			list.add("categoryEnglishName=" + categoryEnglishName);
 			list.add("cityEnglishName=" + QuanleimuApplication.getApplication().cityEnglishName);
 			list.add("rt=1");
 			//根据goodsDetail判断是发布还是修改发布
 			if (goodsDetail != null) {
 				list.add("adId=" + goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 				apiName = "ad_update";
 			}
 						
 //			if(this.location != null){
 				Log.d("location", "location, setDistrictByLocation");
 				setDistrictByLocation(location);
 				
 				Pair<Double, Double> coorGoogle = retreiveCoorFromGoogle();
 				list.add("lat=" + coorGoogle.first);
 				list.add("lng=" + coorGoogle.second);
 //				String baiduUrl = String.format("http://api.map.baidu.com/ag/coord/convert?from=4&to=2&x=%s&y=%s", 
 //						String.valueOf(location.fGeoCodedLon == 0 ? location.fLon : location.fGeoCodedLon),
 //						String.valueOf(location.fGeoCodedLat == 0 ? location.fLat : location.fGeoCodedLat));
 //				try{
 //					Log.d("location", "location, call baiduurl get data");
 //					String baiduJsn = Communication.getDataByUrlGet(baiduUrl);
 //					Log.d("location", "location, baiduurl returns");
 //					JSONObject js = new JSONObject(baiduJsn);
 //					Object errorCode = js.get("error");
 //					if(errorCode instanceof Integer && (Integer)errorCode == 0){
 //						String x = (String)js.get("x");
 //						String y = (String)js.get("y");
 //						byte[] bytes = Base64.decode(x, Base64.DEFAULT);
 //						x = new String(bytes, "UTF-8");
 //						
 //						bytes = Base64.decode(y, Base64.DEFAULT);
 //						y = new String(bytes, "UTF-8");
 //						
 //						Double dx = Double.valueOf(x);
 //						Double dy = Double.valueOf(y);
 //						list.add("lat=" + dx);
 //						list.add("lng=" + dy);
 //						Log.d("location", "location, baiduurl parse succeed");
 //					}
 //				}catch(Exception e){
 //					e.printStackTrace();
 //					Log.d("location", "location, baiduurl parse error");
 //					list.add("lat=" + (location.fGeoCodedLat == 0 ? location.fLat : location.fGeoCodedLat));
 //					list.add("lng=" + (location.fGeoCodedLon == 0 ? location.fLon : location.fGeoCodedLon));
 //				}							
 //			}
 			
 			LinkedHashMap<String, String> postMap = params.getData();
 			//发布发布集合
 			for (int i = 0; i < postMap.size(); i++) {
 				String key = (String) postMap.keySet().toArray()[i];
 				String values = postMap.get(key);
 				
 				if (values != null && values.length() > 0 && postList.get(key) != null) {
 					try{
 						list.add(URLEncoder.encode(postList.get(key).getName(), "UTF-8")
 								+ "=" + URLEncoder.encode(values, "UTF-8").replaceAll("%7E", "~"));//ugly, replace, what's that? 
 						if(postList.get(key).getName().equals(STRING_DESCRIPTION)){//generate title from description
 							list.add("title"
 									+ "=" + URLEncoder.encode(values.substring(0, Math.min(25, values.length())), "UTF-8").replaceAll("%7E", "~"));
 						}
 					}catch(UnsupportedEncodingException e){
 						e.printStackTrace();
 					}
 				}
 			}
 			//发布图片
 			String images = "";
 			int imgCount = 0;
 			for (int i = 0; i < bmpUrls.size(); i++) {				
 				if(bmpUrls.get(i) != null && bmpUrls.get(i).contains("http:")){
 					images += "," + bmpUrls.get(i);
 					imgCount++;
 				}
 			}
 			if(images != null && images.length() > 0 && images.charAt(0) == ','){
 				images = images.substring(1);
 			}
 			if(images != null && images.length() > 0){
 				list.add("images=" + images);
 			}
 			
 			// list.add("title=" + "111");
 			// list.add("description=" +
 			// URLEncoder.encode(descriptionEt.getText().toString()));
 			int    errorCode = 0;
 			String errorMsg = "内部错误，发布失败";
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url, true);
 				if (json != null) {
 					JSONObject jsonObject = new JSONObject(json);
 					JSONObject json = jsonObject.getJSONObject("error");
 					code = json.getInt("code");
 					message = replaceTitleToDescription(json.getString("message"));
 //					myHandler.sendEmptyMessage(3);
 //					Log.d("person","case 3");
 					sendMessage(3, null);
 					
 					errorCode = code;
 					errorMsg = message;
 				}else {
 					errorCode = -1;
 					errorMsg = "解析错误";
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 				errorMsg = "解析错误";
 			} catch (Communication.BXHttpException e) {
 				errorCode = e.errorCode;
 				if(e.errorCode == 414){
 					errorMsg = "内容超出规定长度，请修改后重试";
 				}
 				else{
 					errorMsg = replaceTitleToDescription(e.msg);
 				}
 				
 			} catch(Exception e){
 				e.printStackTrace();
 				errorCode = -2;
 //				errorMsg = e.getMessage();
 				errorMsg = "发布失败";
 			}
 			
 			if (errorMsg.equals("发布成功"))
 				postResultSuccess();
 			else
 				postResultFail(errorMsg);
 						
 			hideProgress();
 			final String fmsg = errorMsg;
 			if(getActivity() != null){
 				((BaseActivity)getActivity()).runOnUiThread(new Runnable(){
 					@Override
 					public void run(){
 						if(getActivity() != null && fmsg != null){
 							Toast.makeText(getActivity(), fmsg, 0).show();
 						}
 					}
 				});
 			}
 		}
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
 		BxEvent event = goodsDetail != null ? BxEvent.EDITPOST_POSTRESULT : BxEvent.POST_POSTRESULT;
 
 		Tracker.getInstance().event(event)
 		.append(Key.SECONDCATENAME, categoryEnglishName)
 		.append(Key.POSTSTATUS, 1)
 		.append(Key.POSTPICSCOUNT, getImgCount())
 		.append(Key.POSTDESCRIPTIONLINECOUNT, getLineCount())
 		.append(Key.POSTDESCRIPTIONTEXTCOUNT, getDescLength())
 		.append(Key.POSTCONTACTTEXTCOUNT, getContactLength())
 		.append(Key.POSTDETAILPOSITIONAUTO, autoLocated)
         .append(Key.POSTENTRY, QuanleimuApplication.postEntryFlag)
 		.end();
 	}
 	
 	private void postResultFail(String errorMsg) {
 		BxEvent event = goodsDetail != null ? BxEvent.EDITPOST_POSTRESULT : BxEvent.POST_POSTRESULT;
 		Tracker.getInstance().event(event)
 				.append(Key.SECONDCATENAME, categoryEnglishName)
 				.append(Key.POSTSTATUS, 0)
 				.append(Key.POSTFAILREASON, errorMsg)
 				.append(Key.POSTPICSCOUNT, getImgCount())
 				.append(Key.POSTDESCRIPTIONLINECOUNT, getLineCount())
 				.append(Key.POSTDESCRIPTIONTEXTCOUNT, getDescLength())
 				.append(Key.POSTCONTACTTEXTCOUNT, getContactLength())
 				.append(Key.POSTDETAILPOSITIONAUTO, autoLocated)
                 .append(Key.POSTENTRY, QuanleimuApplication.postEntryFlag)
 				.end();
 	}
 	
 	/**
 	 * 获取模板线程
 	 */
 	public int code = -1;
 	public String message = "";
 
 	class GetCategoryMetaThread implements Runnable {
 
 //		private boolean isUpdate;
 		private String cityEnglishName = null;
 
 		public GetCategoryMetaThread(String cityEnglishName) {
 			this.cityEnglishName = cityEnglishName;
 //			this.isUpdate = isUpdate;
 		}
 //		public GetCategoryMetaThread() {
 //			this.isUpdate = isUpdate;
 //		}
 		
 
 		@Override
 		public void run() {
 
 			String apiName = "category_meta_post";
 			ArrayList<String> list = new ArrayList<String>();
 			this.cityEnglishName = (this.cityEnglishName == null ? QuanleimuApplication.getApplication().cityEnglishName : this.cityEnglishName);
 			list.add("categoryEnglishName=" + categoryEnglishName);
 			list.add("cityEnglishName=" + this.cityEnglishName);
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url, false);
 				if (json != null) {
 					postList = JsonUtil.getPostGoodsBean(json);
 					if(postList == null || postList.size() == 0){
 						sendMessage(10, null);
 						return;
 					}
 					// 获取数据成功
 					Activity activity = getActivity();
 					if (activity != null)
 					{
 						//保存模板
 						Util.saveJsonAndTimestampToLocate(activity, categoryEnglishName
 								+ this.cityEnglishName, json, System.currentTimeMillis()/1000);
 //						if (isUpdate) {
 							sendMessage(1, null);
 //						}
 					}
 				} else {
 					// {"error":{"code":0,"message":"\u66f4\u65b0\u4fe1\u606f\u6210\u529f"},"id":"191285466"}
 					sendMessage(2, null);
 				}
 				return;
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				sendMessage(10, null);
 				e.printStackTrace();
 			} catch (Communication.BXHttpException e){
 				
 			}
 			sendMessage(10, null);
 		}
 	}
 		
 //	private void appendSelectedProperties(String[] lists){
 //		if(lists == null || lists.length == 0) return;
 //		for(int i = 0; i < lists.length; ++ i){
 //			PostGoodsBean bean = this.postList.get(otherProperties.get(Integer.parseInt(lists[i]) - i));
 //			if(bean == null) continue;
 //			this.appendBeanToLayout(bean);
 //			otherProperties.remove(Integer.parseInt(lists[i]) - i);
 //		}
 //		if(otherProperties.size() == 0){
 //			if(layout_txt.getChildCount() > 0){
 //				layout_txt.removeViewAt(layout_txt.getChildCount() - 1);
 //			}
 //		}
 //		else{
 //			if(layout_txt.getChildCount() > 0){
 //				View v = layout_txt.getChildAt(layout_txt.getChildCount() - 1);
 //				if(v != null){
 //					View v2 = v.findViewById(R.id.postshow);
 //					if(v2 != null && v2 instanceof TextView){
 //						((TextView)v2).setText(otherProperties.toString());
 //					}
 //				}
 //			}
 //		}
 //	}
 	
 	private void loadCachedData()
 	{
 //		if (imgs != null)
 //		{
 //			for (int i=0; imgs.length>i; i++)
 //			{
 //				if (i >= 0 && i < cachedBps.length && cachedBps[i] != null)
 //				{
 //					imgs[i].setImageBitmap(cachedBps[i]);
 //					imgs[i].invalidate();
 //				}
 //			}
 //		}
 		
 		LinkedHashMap<String, String> uiMap = params.getUiData();
 		if (uiMap == null)
 		{
 			return;
 		}
 		Iterator<String> it = uiMap.keySet().iterator();
 		while (it.hasNext()){
 			String displayName = it.next();
 			for (int i=0; i<layout_txt.getChildCount(); i++)
 			{
 				View v = layout_txt.getChildAt(i);
 				PostGoodsBean bean = (PostGoodsBean)v.getTag(HASH_POST_BEAN);
 				if(bean == null || !bean.getDisplayName().equals(displayName)) continue;
 				View control = (View)v.getTag(HASH_CONTROL);
 				String displayValue = uiMap.get(displayName);
 				
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
 	
 	static public boolean fetchResultFromViewBack(int message, Object obj, ViewGroup vg, PostParamsHolder params){//??
 		if(vg == null) return false;
 		
 		boolean match = false;
 		for(int i = 0; i < vg.getChildCount(); ++ i){
 			View v = vg.getChildAt(i);
 			PostGoodsBean bean = (PostGoodsBean)v.getTag(HASH_POST_BEAN);
 			if(bean == null) continue;
 			if(bean.getName().hashCode() == message){
 				if(obj instanceof Integer){
 					TextView tv = (TextView)v.getTag(HASH_CONTROL);
 					String txt = bean.getLabels().get((Integer)obj);
 					String txtValue = bean.getValues().get((Integer)obj);
 //					postMap.put(bean.getDisplayName(), txtValue);
 					if(tv != null){
 						tv.setText(txt);
 					}
 					match = true;
 					params.put(bean.getDisplayName(), txt, txtValue);
 				}
 				else if(obj instanceof String){
 					TextView tv = (TextView)v.getTag(HASH_CONTROL);
 					String check = (String)obj;
 					String[] checks = check.split(",");
 					String value = "";
 					String txt = "";
 					for(int t = 0; t < checks.length; ++ t){
 						if(checks[t].equals(""))continue;
 //						if(bean.getLabels().size() <= Integer.parseInt(checks[t])){
 //							Log.d("outofbounds", "hahaha:   out of bound:   " + bean.getLabels().size() + "   " + checks[t]);
 //						}
 		 				txt += "," + bean.getLabels().get(Integer.parseInt(checks[t]));
 						value += "," + bean.getValues().get(Integer.parseInt(checks[t]));
 					}
 					if(txt.length() > 0){
 						txt = txt.substring(1);
 					}
 					if(value.length() > 0){
 						value = value.substring(1);
 					}
 					if(tv != null){
 //						tv.setWidth(vg.getWidth() * 2 / 3);
 						tv.setText(txt);
 					}
 					match = true;
 					params.put(bean.getDisplayName(), txt, value);
 				}
 				else if(obj instanceof MultiLevelSelectionFragment.MultiLevelItem){
 					TextView tv = (TextView)v.getTag(HASH_CONTROL);
 					if(tv != null){
 //						tv.setWidth(vg.getWidth() * 2 / 3);
 						tv.setText(((MultiLevelSelectionFragment.MultiLevelItem)obj).txt);
 					}
 					match = true;
 					params.put(bean.getDisplayName(), ((MultiLevelSelectionFragment.MultiLevelItem)obj).txt, ((MultiLevelSelectionFragment.MultiLevelItem)obj).id);
 				}
 			}
 		}
 		
 		return match;
 	}
 	private void resetData(boolean clearImgs){
 		if(this.layout_txt != null){
 			View v = layout_txt.findViewById(R.id.img_description);
 			layout_txt.removeAllViews();
 			layout_txt.addView(v);
 		}
 		postList.clear();
 		
 		if(clearImgs){
 			if(null != Util.loadDataFromLocate(getActivity(), FILE_LAST_CATEGORY, String.class)){
 				params.clear();
 				listUrl.clear();
 				this.bmpUrls.clear();
 				if(this.imgSelDlg != null){
 					imgSelDlg.clearResource();
 					imgSelDlg = null;
 				}
 				this.imgSelBundle = null;
 				
 				layout_txt.findViewById(R.id.imgCout).setVisibility(View.INVISIBLE);
 			}
 		}
 	}
 
 	private void handleBackWithData(int message, Object obj) {
 
 		if(message == PostGoodsFragment.VALUE_LOGIN_SUCCEEDED){
 			this.handleRightAction();
 			return;
 		}else if(message == MSG_CATEGORY_SEL_BACK && obj != null){
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
 		boolean match = fetchResultFromViewBack(message, obj, layout_txt, params);
 		if(match){
 //			postMap.put(result.first, result.second);
 			return;
 		}
 		switch(message){
 		case MSG_MORE_DETAIL_BACK:
 			params.merge((PostParamsHolder) obj);
 			break;
 //		case POST_OTHERPROPERTIES:
 //			String list = (String)obj;
 //			if(!list.equals("")){
 //				String[] lists = list.split(",");
 //				appendSelectedProperties(lists);
 //			}
 //			break;
 	
 //		case MSG_CATEGORY_SEL_BACK:{
 //			layout_txt.removeAllViews();
 //			otherProperties.clear();
 //			postList.clear();
 //			params.clear();
 //			bitmap_url.clear();
 //			bitmap_url.add(null);
 //			bitmap_url.add(null);
 //			bitmap_url.add(null);
 //			
 //			int i = 0;
 //			for (Bitmap bp : cachedBps)
 //			{
 //				if (bp != null) bp.recycle();
 //				cachedBps[i++] = null;
 //			}
 //
 //			listUrl.clear();
 //			imgs = null;
 //			currentImgView = -1;
 //			uploadCount = 0;
 //						
 ////			this.addCategoryItem();
 ////			TextView tv = (TextView)layout_txt.findViewById(R.layout.item_post_select).getTag(HASH_CONTROL);
 //			getArguments().putString("cateNames", (String) obj); //Update cate names.
 //			String[] backMsg = ((String)obj).split(",");
 //			if(backMsg == null || backMsg.length != 2) break;
 ////			if(tv != null){
 ////				tv.setText(backMsg[1]);
 ////			}
 //			this.categoryEnglishName = backMsg[0];
 //			this.categoryName = backMsg[1];
 //
 ////			this.usercheck();
 //			showPost();
 //			break;
 //		}
 		default:
 			break;
 		}
 	}
 	
 	@Override
 	public void onFragmentBackWithData(int message, Object obj){	
 		handleBackWithData(message, obj);
 	}
 	
 	
 //	private void getBitmap(Uri uri, int id) {
 //		String path = uri == null ? "" : uri.toString();
 //		Log.w("QLM", "upload image : " + path);
 //		if (uri != null) {
 //				if (imgs != null)
 //				{
 //					imgs[this.currentImgView].setFocusable(true);
 //				}
 //				new Thread(new UpLoadThread(path, currentImgView)).start();
 //		}
 //
 //	}
 	
 	public String getRealPathFromURI(Uri contentUri) {
 		String[] proj = { MediaStore.Images.Media.DATA };
 		Cursor cursor = getActivity().managedQuery(contentUri, proj, null, null, null);
 
 		if (cursor == null)
 			return null;
 
 		int column_index = cursor
 				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 
 		cursor.moveToFirst();
 
 		String ret = cursor.getString(column_index);
 //		cursor.close();
 		return ret;
 	}
 
 
 	public void startPhotoZoom(Uri uri) {
 		Intent intent = new Intent("com.android.camera.action.CROP");
 		intent.setDataAndType(uri, IMAGEUNSPECIFIED);
 		intent.putExtra("crop", "true");
 		// aspectX aspectY 是宽高的比例
 		intent.putExtra("aspectX", 1);
 		intent.putExtra("aspectY", 1);
 		intent.putExtra("return-data", false);
 		intent.putExtra("output",Uri.fromFile(new File("/sdcard/cropped.jpg")));
 		getActivity().startActivityForResult(intent, PHOTORESOULT);
 	}
 
 //	public void saveSDCard(Bitmap photo) {
 //		try {
 //			String filepath = "/sdcard/baixing";
 //			File files = new File(filepath);
 //			files.mkdir();
 //			File file = new File(filepath, "temp" + this.currentImgView + ".jpg");
 //			FileOutputStream outStream = new FileOutputStream(file);
 //			String path = file.getAbsolutePath();
 //			Log.i(path, path);
 //			photo.compress(CompressFormat.JPEG, 100, outStream);
 //			outStream.close();
 //		} catch (FileNotFoundException e) {
 //			e.printStackTrace();
 //		} catch (IOException e) {
 //			e.printStackTrace();
 //		}
 //	}
 	
 	private void initImageLayout(){
 //		this.layout_txt.findViewById(R.id.image_layout).setVisibility(View.VISIBLE);
 //		layout_txt.findViewById(R.id.myImg).setOnClickListener(PostGoodsFragment.this);
 //		layout_txt.findViewById(R.id.iv_1).setOnClickListener(PostGoodsFragment.this);
 //		layout_txt.findViewById(R.id.iv_2).setOnClickListener(PostGoodsFragment.this);
 //		layout_txt.findViewById(R.id.iv_3).setOnClickListener(PostGoodsFragment.this);
 //		imgs = new ImageView[]{(ImageView)layout_txt.findViewById(R.id.iv_1),
 //				(ImageView)layout_txt.findViewById(R.id.iv_2),
 //				(ImageView)layout_txt.findViewById(R.id.iv_3)
 //		};
 	}
 	
 	private void appendBeanToLayout(PostGoodsBean postBean)
 	{
 		if (postBean.getName().equals("contact") &&
 			(postBean.getValues() == null || postBean.getValues().isEmpty()) &&
 			(user != null && user.getPhone() != null && user.getPhone().length() > 0))
 		{
 			List<String> valueList = new ArrayList<String>(1);
 			valueList.add(user.getPhone());
 			postBean.setValues(valueList);
 			postBean.setLabels(valueList);
 		}	
 		
 	
 //		Activity activity = getActivity();
 		ViewGroup layout = createItemByPostBean(postBean, this);//FIXME:
 		if(postBean.getName().equals(STRING_DETAIL_POSITION)){
 			layout.findViewById(R.id.location).setOnClickListener(this);
 //			if(inLocating){
 //				((TextView)layout.findViewById(R.id.postinput)).setHint("定位中...");
 //			}else{
 				((TextView)layout.findViewById(R.id.postinput)).setHint("请输入");
 //			}
 			((TextView)layout.findViewById(R.id.postinput)).setOnKeyListener(this);
 //			((TextView)layout.findViewById(R.id.postinput)).addTextChangedListener(this);
 			locationView = layout;
 //			if(this.detailLocation != null && !inLocating){
 //				setDetailLocationControl(detailLocation);
 //			}
 		}else if(postBean.getName().equals(STRING_AREA)){
 //			districtView = layout;
 //			if(this.detailLocation != null && !inLocating){
 //				setDetailLocationControl(detailLocation);
 //			}			
 		}
 		if(postBean.getName().equals("contact") && layout != null){
 			etContact = ((EditText)layout.getTag(HASH_CONTROL));
 			etContact.setText(mobile);
 		}
 		if (postBean.getName().equals(STRING_DESCRIPTION) && layout != null){
 			etDescription = (EditText) layout.getTag(HASH_CONTROL);
 		}
 		
 		if (postBean.getControlType().equals("image")) {
 			initImageLayout();
 		}
 //		LayoutInflater flater = LayoutInflater.from(activity);
 //		View border = flater.inflate(R.layout.seperator, null);
 //		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, 1, 1);
 //		border.setLayoutParams(lp);
 		
 		if(layout != null){
 			layout_txt.addView(layout);
 		}
 		//layout_txt.addView(border);
 	
 	}
 	private AlertDialog ad=null;
 //	private String[] mListString = {"姓名：王魁锋","性别：男","年龄：23",  
 //            "居住地：上海市普陀区","邮箱：wangkuifeng0118@126.com"};
 	List<Map<String, Object>> list = null;
 	class SecondCateAdapter extends BaseAdapter{
 		
 		@Override
 		public int getCount() {
 			return list.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return list.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			TextView tv = null;
 			if (position==0) {
 				v = LayoutInflater.from(getActivity()).inflate(R.layout.item_seccategory_simple, null);
 				tv = (TextView)v.findViewById(R.id.tv);
 			} else {
 				v = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_1, null);
 				tv = (TextView)v.findViewById(android.R.id.text1);
 			}
 			if (tv!=null)
 				tv.setText((String)list.get(position).get("tvCategoryName"));
 
 			return v;
 		}
 		
 	}
 	
 	private void addCategoryItem(){
 		Activity activity = getActivity();
 		if(this.goodsDetail != null)return;
 		if(layout_txt != null){
 			if(layout_txt.findViewById(R.id.arrow_down) != null) return;
 		}
 		LayoutInflater inflater = LayoutInflater.from(activity);
 		View categoryItem = inflater.inflate(R.layout.item_post_select, null);
 		categoryItem.setTag(HASH_CONTROL, categoryItem.findViewById(R.id.posthint));//tag
 		((TextView)categoryItem.findViewById(R.id.postshow)).setText("分类");
 		((ImageView)categoryItem.findViewById(R.id.post_next)).setImageResource(R.drawable.arrowdown);
 		categoryItem.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 //				Bundle bundle = createArguments(null, null);
 //				bundle.putInt(ARG_COMMON_REQ_CODE, MSG_CATEGORY_SEL_BACK);
 //				pushFragment(new GridCateFragment(), bundle);
 				
 				Bundle bundle = createArguments(null, null);
 				bundle.putSerializable("items", (Serializable) Arrays.asList(texts));
 				bundle.putInt("maxLevel", 1);
 				bundle.putInt(ARG_COMMON_REQ_CODE, MSG_CATEGORY_SEL_BACK);
 				if(categoryEnglishName != null && !categoryEnglishName.equals("") && categoryName != null) {
 					bundle.putString("selectedValue", categoryName);
 				}
 				
 				extractInputData(layout_txt, params);
 				CustomDialogBuilder cdb = new CustomDialogBuilder(getActivity(), PostGoodsFragment.this.getHandler(), bundle);
 				cdb.start();
 			}				
 		});//categoryItem.setOnClickListener
 		
 		
 		
 		if(categoryEnglishName != null && !categoryEnglishName.equals("") && categoryName != null){
 			 ((TextView)categoryItem.findViewById(R.id.posthint)).setText(categoryName);
 		}
 		
 		LinearLayout.LayoutParams layoutParams = (LayoutParams) categoryItem.getLayoutParams();
 		if (layoutParams == null)
 			layoutParams = new LinearLayout.LayoutParams(
 			     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
 		layoutParams.bottomMargin = categoryItem.getContext().getResources().getDimensionPixelOffset(R.dimen.post_padding);
 		categoryItem.setLayoutParams(layoutParams);
 		
 		layout_txt.addView(categoryItem);
 	}
 	
 	private String[] fixedItemNames = {"images", STRING_DESCRIPTION, "价格", "contact", STRING_DETAIL_POSITION};
 	private String[] fixedItemDisplayNames = {"", "描述", "价格", "联系电话", STRING_DETAIL_POSITION};
 	private String[] hiddenItemNames = {"wanted", "faburen"};
 	private boolean autoLocated;
 
 	
 	private void buildFixedPostLayout(){//添加fixedItemNames和postList交集的beanLayout
 		if(this.postList == null || this.postList.size() == 0) return;
 		
 		HashMap<String, PostGoodsBean> pm = new HashMap<String, PostGoodsBean>();
 		Object[] postListKeySetArray = postList.keySet().toArray();
 		for(int i = 0; i < postList.size(); ++ i){
 			for(int j = 0; j < fixedItemNames.length; ++ j){
 				PostGoodsBean bean = postList.get(postListKeySetArray[i]);
 				if(bean.getName().equals(fixedItemNames[j])){					
 					pm.put(fixedItemNames[j], bean);
 					break;
 				}
 			}
 		}
 		
 		if(pm.containsKey(STRING_DESCRIPTION)){
 			PostGoodsBean bean = pm.get(STRING_DESCRIPTION);
 			if(bean != null){
 				View v = layout_txt.findViewById(R.id.img_description);
 				EditText text = (EditText)v.findViewById(R.id.description_input);
 				text.setText("");
 				v.setTag(HASH_POST_BEAN, bean);
 				v.setTag(HASH_CONTROL, text);
 				v.setOnClickListener(this);
 //				TextView tv = (TextView)layout_txt.findViewById(R.id.description);
 //				tv.setText(bean.getDisplayName());
 				
 				v.findViewById(R.id.myImg).setOnClickListener(this);
 				((ImageView)v.findViewById(R.id.myImg)).setImageResource(R.drawable.btn_add_picture);
 				if(imgSelBundle != null){
 					ArrayList<Bitmap> bps = (ArrayList<Bitmap>)imgSelBundle.getSerializable(ImageSelectionDialog.KEY_CACHED_BPS);
 					if(bps != null && bps.size() > 0){
 						((ImageView)v.findViewById(R.id.myImg)).setImageBitmap(bps.get(0));
 					}
 				}				
 			}			
 		}
 		
 		for(int i = 0; i < fixedItemNames.length; ++ i){
 			if(pm.containsKey(fixedItemNames[i]) && !fixedItemNames[i].equals(STRING_DESCRIPTION)){
 				this.appendBeanToLayout(pm.get(fixedItemNames[i]));
 			}
 		}
 	}
 	
 	private boolean isFixedItem(PostGoodsBean bean){
 		for(int i = 0; i < fixedItemNames.length; ++ i){
 			if(bean.getName().equals(fixedItemNames[i])) return true;
 		}
 		return false;
 	}
 	
 	private void addHiddenItemsToParams()
 	{
 		if (postList == null || postList.isEmpty())
 			return ;
 		Set<String> keySet = postList.keySet();
 		for (String key : keySet)
 		{
 			PostGoodsBean bean = postList.get(key);
 			for (int i = 0; i<  hiddenItemNames.length; i++)
 			{
 				if (bean.getName().equals(hiddenItemNames[i]))
 				{
 					String defaultValue = bean.getDefaultValue();
 					if (defaultValue != null && defaultValue.length() > 0) {
 						//String key, String uiValue, String data
 						this.params.put(bean.getDisplayName(), 
 								defaultValue,
 								defaultValue);
 					} else {
 						this.params.put(bean.getDisplayName(), 
 								bean.getLabels().get(0), 
 								bean.getValues().get(0));
 					}
 					break;
 				}
 			}
 		}
 	}
 	
 	private boolean isHiddenItem(PostGoodsBean bean)
 	{
 		for (int i = 0; i < hiddenItemNames.length; ++i)
 		{
 			if (bean.getName().equals(hiddenItemNames[i]))
 			{
 				return true;
 			}else if(bean.getName().equals("title")){//特殊处理
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private void buildPostLayout(){
 		this.getView().findViewById(R.id.goodscontent).setVisibility(View.VISIBLE);
 		this.getView().findViewById(R.id.networkErrorView).setVisibility(View.GONE);
 		this.reCreateTitle();
 		this.refreshHeader();
 		Log.d(TAG, "start to build layout");
 //		otherProperties.clear();
 		
 //		final Activity activity = getActivity();
 		
 		//根据模板显示
 		if(null == json || json.equals("")) return;
 		if(postList == null || postList.size() == 0){
 			postList = JsonUtil.getPostGoodsBean(json);
 		}
 		buildFixedPostLayout();//添加固定item的layout
 		addHiddenItemsToParams();//params中加入隐藏元素的default值
 		
 		Object[] postListKeySetArray = postList.keySet().toArray();
 		for (int i = 0; i < postList.size(); i++) {
 			String key = (String) postListKeySetArray[i];
 			PostGoodsBean postBean = postList.get(key);
 			
 			if(isFixedItem(postBean) || isHiddenItem(postBean))//排除固定和隐藏元素
 				continue;
 			
 			if(postBean.getName().equals(STRING_AREA)){
 //				this.appendBeanToLayout(postBean);
 				continue;
 			}
 			
 //			if(goodsDetail != null && (postBean.getName().equals("images") && (goodsDetail.getImageList() != null 
 //					&& goodsDetail.getImageList().getResize180() != null 
 //					&& !goodsDetail.getImageList().getResize180().equals("")))){
 //			if(postBean.getName().equals("images")){
 //				this.appendBeanToLayout(postBean);
 //				continue;
 //			}
 
 //			if(!postBean.getRequired().endsWith("required") 
 //					&& (goodsDetail == null 
 //						|| goodsDetail.getValueByKey(postBean.getName()) == null 
 //						|| goodsDetail.getValueByKey(postBean.getName()).equals(""))){
 //				otherProperties.add(postBean.getDisplayName());
 //				continue;
 //			}
 			
 			this.appendBeanToLayout(postBean);//加入元素
 		}//for : postList
 //		if(otherProperties.size() > 0){
 //			LayoutInflater inflater = LayoutInflater.from(activity);
 //			View v = inflater.inflate(R.layout.item_post_select, null);
 //			((TextView)v.findViewById(R.id.postshow)).setText(otherProperties.toString());
 ////			((TextView)v.findViewById(R.id.postshow)).setWidth(layout_txt.getWidth() * 2 / 3);
 //			((TextView)v.findViewById(R.id.posthint)).setText("非必选");
 //			v.setOnClickListener(new OnClickListener(){
 //				@Override
 //				public void onClick(View v) {
 //					Bundle bundle = createArguments(null, null);
 //					bundle.putInt(ARG_COMMON_REQ_CODE, MSG_MORE_DETAIL_BACK);
 //					bundle.putSerializable("beans", postList);
 //					bundle.putSerializable("details", otherProperties);
 //					bundle.putSerializable("existing", params);
 //					
 //					pushFragment(new FillMoreDetailFragment(), bundle);
 //				}	
 //			});
 //			layout_txt.addView(v);
 //		}
 
 		
 		editpostUI();//编辑goodsDetail时调用
 		originParams.merge(params);//orginPrams合并params
 		extractInputData(layout_txt, originParams);//将界面元素的值存入originParams	
 	}//buildPostLayout
 	
 
 
 	@Override
 	protected void handleMessage(Message msg, Activity activity, View rootView) {
 
 		if(msg.what != MSG_GETLOCATION_TIMEOUT){
 			hideProgress();
 		}
 		
 		switch (msg.what) {
 //		case MSG_GETLOCATION_TIMEOUT:{
 //			if(inLocating){
 ////				setDetailLocationControl(null);
 ////				if(this.locationView != null){
 ////					((TextView)locationView.findViewById(R.id.postinput)).setHint("请输入");
 ////				}
 //			}
 //			inLocating = false;			
 //			break;
 //		}
 		case MSG_DIALOG_BACK_WITH_DATA:{
 			Bundle bundle = (Bundle)msg.obj;
 			handleBackWithData(bundle.getInt(ARG_COMMON_REQ_CODE), bundle.getSerializable("lastChoise"));
 			break;
 		}
 		
 		case ImageSelectionDialog.MSG_IMG_SEL_DISMISSED:{
 			if(imgSelBundle != null){
 				ArrayList<Bitmap> bps = (ArrayList<Bitmap>)imgSelBundle.getSerializable(ImageSelectionDialog.KEY_CACHED_BPS);
 				if(getView() != null){
 					ImageView iv = (ImageView)this.getView().findViewById(R.id.myImg);
 					if(iv != null){						
 						if(bps != null && bps.size() > 0){
 							if(iv != null){
 								iv.setImageBitmap(bps.get(0));
 							}
 						}else{
 							iv.setImageResource(R.id.myImg);
 						}
 					}
 					
 					TextView tv = (TextView)getView().findViewById(R.id.imgCout);
 					if(iv != null){
 						if(bps != null && bps.size() > 0){
 							tv.setText(String.valueOf(bps.size()));
 							tv.setVisibility(View.VISIBLE);
 						}else{
 							tv.setVisibility(View.INVISIBLE);
 						}
 					}
 				}
 				ArrayList<String> urls = (ArrayList<String>)imgSelBundle.getSerializable(ImageSelectionDialog.KEY_BITMAP_URL);
 				if(urls != null){
 					bmpUrls.clear();
 					bmpUrls.addAll(urls);
 				}
 			}
 			break;
 		}		
 		case -2:{
 			loadCachedData();
 			break;
 		}
 		case 1:
 			addCategoryItem();
 			buildPostLayout();
 			loadCachedData();
 			break;
 
 		case 2:
 			hideProgress();
 			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 			builder.setTitle("提示:")
 					.setMessage(message)
 					.setPositiveButton("确定",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dialog.dismiss();
 								}
 							});
 			builder.create().show();
 			this.getView().findViewById(R.id.goodscontent).setVisibility(View.GONE);
 			this.getView().findViewById(R.id.networkErrorView).setVisibility(View.VISIBLE);
 			this.reCreateTitle();
 			this.refreshHeader();
 
 			break;
 		case 3:
 			try {
 				hideProgress();
 				JSONObject jsonObject = new JSONObject(json);
 				String id;
 				boolean isRegisteredUser = false;
 				try {
 					id = jsonObject.getString("id");
 					isRegisteredUser = jsonObject.getBoolean("contactIsRegisteredUser");
 				} catch (Exception e) {
 					id = "";
 					e.printStackTrace();
 				}
 				JSONObject json = jsonObject.getJSONObject("error");
 				String message = replaceTitleToDescription(json.getString("message"));
 				Toast.makeText(activity, message, 0).show();
 				if (!id.equals("") && code == 0) {
 					final Bundle args = createArguments(null, null);
 					args.putInt("forceUpdate", 1);
 					// 发布成功
 					// Toast.makeText(PostGoods.this, "未显示，请手动刷新",
 					// 3).show();
 					resetData(true);
 					
 //					cxt.sendBroadcast(intent);
 
 
 					if(goodsDetail == null){
 						showPost();
 						String lp = getArguments().getString("lastPost");
 						if(lp != null && !lp.equals("")){
 							lp += "," + id;
 						}else{
 							lp = id;
 						}
 						args.putString("lastPost", lp);
 						
 						args.putString("cateEnglishName", categoryEnglishName);
 						args.putBoolean(KEY_IS_EDITPOST, goodsDetail!=null);
 						
 						args.putBoolean(KEY_LAST_POST_CONTACT_USER,  isRegisteredUser);
 //						PostGoodsFragment.this.finishFragment(PostGoodsFragment.MSG_POST_SUCCEED, null);
 						if(activity != null){							
 							args.putInt(PersonalPostFragment.TYPE_KEY, PersonalPostFragment.TYPE_MYPOST);
 							
 							Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_POST_FINISH);
 							intent.putExtra(CommonIntentAction.EXTRA_MSG_FINISHED_POST, args);
 							activity.sendBroadcast(intent);
 //							((BaseActivity)activity).pushFragment(new PersonalPostFragment(), args, false);
 						}						
 					}else{
 						PostGoodsFragment.this.finishFragment(PostGoodsFragment.MSG_POST_SUCCEED, null);
 					}
 				}else{
 					if(code == 505){
 //						PostGoodsFragment.this.finishFragment(PostGoodsFragment.MSG_POST_SUCCEED, null);
 						if(activity != null){
 							resetData(true);
 							showPost();
 							Bundle args = createArguments(null, null);
 							args.putInt(PersonalPostFragment.TYPE_KEY, PersonalPostFragment.TYPE_MYPOST);
 //							((BaseActivity)activity).pushFragment(new PersonalPostFragment(), args, false);
 							Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_POST_FINISH);
 							intent.putExtra(CommonIntentAction.EXTRA_MSG_FINISHED_POST, args);
 							activity.sendBroadcast(intent);							
 						}						
 					}
 				}
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			break;
 		case 4:
 
 			break;
 		case 10:
 			hideProgress();
 			Toast.makeText(activity, "网络连接失败，请检查设置！", 3).show();
 			this.getView().findViewById(R.id.goodscontent).setVisibility(View.GONE);
 			this.getView().findViewById(R.id.networkErrorView).setVisibility(View.VISIBLE);		
 			this.reCreateTitle();
 			this.refreshHeader();
 			break;
 		case MSG_GEOCODING_TIMEOUT:
 		case MSG_GEOCODING_FETCHED:			
 			if(gettingLocationFromBaidu){
 				showSimpleProgress();
 				(new Thread(new UpdateThread(usercheck(), msg.obj == null ? null : (BXLocation)msg.obj))).start();
 				gettingLocationFromBaidu = false;
 			}
 			break;
 		}
 	}
 
 	private String replaceTitleToDescription(String msg) {
 		// replace title to description in message
 		PostGoodsBean titleBean = null, descriptionBean = null;
 		for (String key : postList.keySet()) {
 			PostGoodsBean bean = postList.get(key);
 			if (bean.getName().equals("title")) 
 				titleBean = bean;
 			if (bean.getName().equals(STRING_DESCRIPTION))
 				descriptionBean = bean;
 		}
 		if (titleBean != null && descriptionBean != null)
 			msg = msg.replaceAll(titleBean.getDisplayName(), descriptionBean.getDisplayName());
 		return msg;
 	}
 	
 	
 	private int imgHeight = 0;
 	
 	////to fix stupid system error. all text area will be the same content after app is brought to front when activity not remain is checked
 	private void setInputContent(){		
 		if(layout_txt == null) return;
 		for(int i = 0; i < layout_txt.getChildCount(); ++ i){
 			View v = layout_txt.getChildAt(i);
 			PostGoodsBean bean = (PostGoodsBean)v.getTag(HASH_POST_BEAN);
 			if(bean == null) continue;
 			View control = (View)v.getTag(HASH_CONTROL);
 			if(control != null && control instanceof TextView){
 				if(params != null && params.containsKey(bean.getDisplayName())){
 					((TextView)control).setText(params.getUiData(bean.getDisplayName()));
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void onStart(){
 		super.onStart();
 		setInputContent();
 //		imgHeight = imgs[0].getMeasuredHeight()
 	}
 	
 	class SetBitmapThread implements Runnable{
 		private int index = -1;
 		private Bitmap bmp;
 		public SetBitmapThread(int index, Bitmap bmp){
 			this.index = index;
 			this.bmp = bmp;
 		}
 		
 		@Override
 		public void run(){
 //			PostGoodsFragment.this.imgs[index].setImageBitmap(bmp);
 //			PostGoodsFragment.this.imgs[index].setClickable(true);
 		}
 	}
 	
 	class Imagethread implements Runnable {
 		private List<String> smalls;
 		private List<String> bigs;
 		public Imagethread(List<String> smalls, List<String> bigs){
 			this.smalls = smalls;
 			this.bigs = bigs;
 		}
 		@Override
 		public void run() {
 			
 			Activity activity = getActivity();
 			if (activity == null)
 			{
 				return;
 			}
 			
 //			for(int i = 0; i < smalls.size(); ++ i){
 //				PostGoodsFragment.this.imgs[i].setClickable(false);
 //			}
 //			for(int t = 0; t < smalls.size(); ++ t){
 //				try {
 //					Bitmap tbitmap = Util.getImage(smalls.get(t));
 //					PostGoodsFragment.this.bitmap_url.set(t, bigs.get(t));
 //					PostGoodsFragment.this.origin_bitmap_url.set(t, bigs.get(t));
 //					activity.runOnUiThread(new SetBitmapThread(t, tbitmap));
 //					
 //				} catch (Exception e) {
 //					e.printStackTrace();
 //					PostGoodsFragment.this.imgs[t].setClickable(true);
 //				}
 //			}
 		}
 	}
 
 	
 	@Override
 	public void initTitle(TitleDef title){
 		title.m_visible = true;
 		title.m_title = "免费发布";//(categoryName == null || categoryName.equals("")) ? "发布" : categoryName;
		title.m_leftActionHint = "返回";
 //		if(this.getView().findViewById(R.id.goodscontent).isShown()){		
 //			title.m_rightActionHint = "完成";
 //		}
 	}
 	
 	@Override
 	public void initTab(TabDef tab){
 		tab.m_visible = false;
 //		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
 	}
 	
 	static public void popupSelection(BaseFragment fragment, View v, PostGoodsBean bean){
 		if(bean.getLabels() == null || bean.getLabels().size() <= 0) return;
 		AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
 		LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
 		View popupView = inflater.inflate(R.layout.popup_container, null);
 		List<String> items = bean.getLabels();
 		List<CheckableItem> checkableItems = new ArrayList<CheckableItem>();
 		for(int i = 0; i < items.size(); ++ i){
 			CheckableItem item = new CheckableItem();
 			item.checked = false;
 			item.txt = items.get(i);
 			checkableItems.add(item);
 		}
 		CheckableAdapter adapter = new CheckableAdapter(fragment.getActivity(), checkableItems, 20, false);
 		((ListView)popupView.findViewById(R.id.popup_list)).setAdapter(adapter);
 		builder.setView(popupView);
 		builder.show();
 	}
 	
 	public static ViewGroup createItemByPostBean(PostGoodsBean postBean, final BaseFragment fragment){//??
 		ViewGroup layout = null;
 		
 		Activity activity = fragment.getActivity();
 		if (postBean.getControlType().equals("input")) {
 			LayoutInflater inflater = LayoutInflater.from(activity);
 			View v = postBean.getName().equals(STRING_DETAIL_POSITION) ? 
 					inflater.inflate(R.layout.item_post_location, null) : 
 						inflater.inflate(R.layout.item_post_edit, null);	
 			((TextView)v.findViewById(R.id.postshow)).setText(postBean.getDisplayName());
 
 			EditText text = (EditText)v.findViewById(R.id.postinput);
 			v.setTag(HASH_POST_BEAN, postBean);
 			v.setTag(HASH_CONTROL, text);
 			if(postBean.getNumeric() != 0){
 				text.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
 			}
 			
 			if (postBean.getName().equals("contact")) {
 				text.setInputType(InputType.TYPE_CLASS_PHONE);
 			}
 			
 			if (!postBean.getUnit().equals("")) {
 				((TextView)v.findViewById(R.id.postunit)).setText(postBean.getUnit());
 			}
 			layout = (ViewGroup)v;
 		} else if (postBean.getControlType().equals("select")) {//select的设置
 			LayoutInflater inflater = LayoutInflater.from(activity);
 			View v = inflater.inflate(R.layout.item_post_select, null);	
 			((TextView)v.findViewById(R.id.postshow)).setText(postBean.getDisplayName());
 			v.setTag(HASH_POST_BEAN, postBean);
 			v.setTag(HASH_CONTROL, v.findViewById(R.id.posthint));
 			layout = (ViewGroup)v;
 		}
 		else if (postBean.getControlType().equals("checkbox")) {
 			LayoutInflater inflater = LayoutInflater.from(activity);
 
 			if(postBean.getLabels().size() > 1){
 				View v = inflater.inflate(R.layout.item_post_select, null);
 				((TextView)v.findViewById(R.id.postshow)).setText(postBean.getDisplayName());
 				v.setTag(HASH_POST_BEAN, postBean);
 				v.setTag(HASH_CONTROL, v.findViewById(R.id.posthint));
 				layout = (ViewGroup)v;
 			}
 			else{
 				View v = inflater.inflate(R.layout.item_text_checkbox, null);
 				v.findViewById(R.id.divider).setVisibility(View.GONE);
 				((TextView)v.findViewById(R.id.checktext)).setText(postBean.getDisplayName());
 				v.findViewById(R.id.checkitem).setTag(postBean.getDisplayName());
 				v.setTag(HASH_POST_BEAN, postBean);
 				v.setTag(HASH_CONTROL, v.findViewById(R.id.checkitem));	
 				layout = (ViewGroup)v;				
 			}
 		} else if (postBean.getControlType().equals("textarea")) {
 			LayoutInflater inflater = LayoutInflater.from(activity);
 			View v = inflater.inflate(R.layout.item_post_description, null);
 			((TextView)v.findViewById(R.id.postdescriptionshow)).setText(postBean.getDisplayName());
 
 			EditText descriptionEt = (EditText)v.findViewById(R.id.postdescriptioninput);
 
 			if(postBean.getName().equals(STRING_DESCRIPTION))//description is builtin keyword
 			{
 				String personalMark = QuanleimuApplication.getApplication().getPersonMark();
 				if(personalMark != null && personalMark.length() > 0){
 					personalMark = "\n\n" + personalMark;
 					descriptionEt.setText(personalMark);
 				}
 			}
 			
 			v.setTag(HASH_POST_BEAN, postBean);
 			v.setTag(HASH_CONTROL, descriptionEt);
 			layout = (ViewGroup)v;
 		}//获取到item的layout
 		
 		if (layout == null)
 			return null;
 		
 		if(postBean.getControlType().equals("select") || postBean.getControlType().equals("checkbox")){
 			layout.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					PostGoodsBean postBean = (PostGoodsBean) v.getTag(HASH_POST_BEAN);
 
 					if (postBean.getControlType().equals("select") || postBean.getControlType().equals("tableSelect")) {
 							if(postBean.getLevelCount() > 0){
 //								if(postBean.getLevelCount() == 1){
 //									popupSelection(fragment, v, postBean);
 //								}else{
 
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
 									if (fragment instanceof PostGoodsFragment)
 									{
 										PostGoodsFragment postGoodsFragment = (PostGoodsFragment) fragment;
 										selectedValue = postGoodsFragment.params.getData(postBean.getDisplayName());
 									}else if (fragment instanceof FillMoreDetailFragment)
 									{
 										FillMoreDetailFragment fillMoreDetailFragment = (FillMoreDetailFragment) fragment;
 										selectedValue = fillMoreDetailFragment.params.getData(postBean.getDisplayName());
 									}
 									
 									if (selectedValue != null)
 										bundle.putString("selectedValue", selectedValue);
 
 									//以下代码为使用dialog的方式切换
 									extractInputData(((PostGoodsFragment)fragment).layout_txt, ((PostGoodsFragment)fragment).params);
 									CustomDialogBuilder cdb = new CustomDialogBuilder(fragment.getActivity(), fragment.getHandler(), bundle);
 									cdb.start();
 									
 									//以下代码为使用MultiLevelSelectionFragment切换
 //									((BaseActivity)fragment.getActivity()).pushFragment(new MultiLevelSelectionFragment(), bundle, false);
 									
 //								}
 							}//postBean.getLevelCount() > 0
 							else{
 								Bundle bundle = createArguments(postBean.getDisplayName(), null);
 								bundle.putInt(ARG_COMMON_REQ_CODE, postBean.getName().hashCode());
 								bundle.putBoolean("singleSelection", false);
 								bundle.putSerializable("properties",(ArrayList<String>) postBean.getLabels());
 								TextView txview = (TextView)v.getTag(HASH_CONTROL);
 								if (txview !=  null)
 								{
 									bundle.putString("selected", txview.getText().toString());
 								}
 								((BaseActivity)fragment.getActivity()).pushFragment(new OtherPropertiesFragment(), bundle, false);
 							}//postBean.getLevelCount() <= 0
 					}
 					else if(postBean.getControlType().equals("checkbox")){
 						if(postBean.getLabels().size() > 1){
 							Bundle bundle = createArguments(postBean.getDisplayName(), null);
 							bundle.putInt(ARG_COMMON_REQ_CODE, postBean.getName().hashCode());
 							bundle.putBoolean("singleSelection", false);
 							bundle.putSerializable("properties",(ArrayList<String>) postBean.getLabels());
 							TextView txview = (TextView)v.getTag(HASH_CONTROL);
 							if (txview !=  null)
 							{
 								bundle.putString("selected", txview.getText().toString());
 							}
 							((BaseActivity)fragment.getActivity()).pushFragment(new OtherPropertiesFragment(), bundle, false);
 						}
 						else{
 							View checkV = v.findViewById(R.id.checkitem);
 							if(checkV != null && checkV instanceof CheckBox){
 								((CheckBox)checkV).setChecked(!((CheckBox)checkV).isChecked());
 							}
 						}
 					}
 				}
 			});//layout.setOnClickListener
 		} else {//not select or checkbox
 			layout.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					View ctrl = (View) v.getTag(HASH_CONTROL);
 					ctrl.requestFocus();
 					InputMethodManager inputMgr = 
 							(InputMethodManager) ctrl.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 					inputMgr.showSoftInput(ctrl, InputMethodManager.SHOW_IMPLICIT);
 				}
 			});
 			
 		}
 
 		LinearLayout.LayoutParams layoutParams = (LayoutParams) layout.getLayoutParams();
 		if (layoutParams == null)
 			layoutParams = new LinearLayout.LayoutParams(
 			     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
 		layoutParams.bottomMargin = layout.getContext().getResources().getDimensionPixelOffset(R.dimen.post_padding);
 		layout.setLayoutParams(layoutParams);
 		
 		return layout;
 	}
 	
 	private void setDistrictByLocation(BXLocation location){
 		if(location == null || location.subCityName == null) return;
 		if(this.postList != null && postList.size() > 0){
 			Object[] postListKeySetArray = postList.keySet().toArray();
 			for(int i = 0; i < postList.size(); ++ i){
 				PostGoodsBean bean = postList.get(postListKeySetArray[i]);
 				if(bean.getName().equals(STRING_AREA)){
 					if(bean.getLabels() != null){
 						for(int t = 0; t < bean.getLabels().size(); ++ t){
 							if(location.subCityName.contains(bean.getLabels().get(t))){
 //								((TextView)districtView.findViewById(R.id.posthint)).setText(bean.getLabels().get(t));
 								params.put(bean.getDisplayName(), bean.getLabels().get(t), bean.getValues().get(t));
 								originParams.put(bean.getDisplayName(), bean.getLabels().get(t), bean.getValues().get(t));
 								return;
 							}
 						}
 					}						
 				}
 			}
 		}		
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
 		
 //		
 //        cacheLocation = location;
 //        String autoAddress = "";
 //
 //        BXLocation lastLocation = (BXLocation)Util.loadDataFromLocate(getActivity(), "lastLocation", BXLocation.class);
 //        if (lastLocation != null) {
 //            autoAddress = (lastLocation.detailAddress == null || lastLocation.detailAddress.equals("")) ? 
 //            		((lastLocation.subCityName == null || lastLocation.subCityName.equals("")) ?
 //							"" 
 //							: lastLocation.subCityName)
 //					: lastLocation.detailAddress;
 //
 //            if (location != null && location.detailAddress != null && !location.detailAddress.equals("")) {
 //                Location newLocation = new Location("newLocation");
 //                newLocation.setLatitude(location.fLat);
 //                newLocation.setLongitude(location.fLon);
 //                Location oldLocation = new Location("oldLocation");
 //                oldLocation.setLatitude(lastLocation.fLat);
 //                oldLocation.setLongitude(lastLocation.fLon);
 //
 //                float distance = newLocation.distanceTo(oldLocation);
 //                if (distance > 1000) {
 //                    autoAddress = location.detailAddress;
 //                }
 //            }
 //        } else {
 //        	autoAddress = (location == null) ? "" :
 //        		((location.detailAddress == null || location.detailAddress.equals("")) ? 
 //        				((location.subCityName == null || location.subCityName.equals("")) ?
 //    							"" 
 //    							: location.subCityName)
 //    					: location.detailAddress);
 //        }
 //
 //		if(locationView != null && locationView.findViewById(R.id.postinput) != null){
 //			CharSequence chars = ((TextView)locationView.findViewById(R.id.postinput)).getText();
 //			if(chars == null || chars.toString().equals("")){
 //				((TextView)locationView.findViewById(R.id.postinput)).setText(autoAddress);
 //				autoLocated = true;
 //				for (String key : postList.keySet())
 //				{
 //					PostGoodsBean bean = postList.get(key);
 //					if (bean.getName().equals(STRING_DETAIL_POSITION)) {
 //						originParams.put(bean.getDisplayName(), autoAddress, autoAddress);
 //					}
 //				}
 //				
 //				Toast.makeText(getActivity(), "已获得当前位置", Toast.LENGTH_SHORT).show();
 //			}
 //		}
 //		if(districtView != null && location != null && location.subCityName != null && !location.subCityName.equals("")){
 //			CharSequence chars = ((TextView)districtView.findViewById(R.id.posthint)).getText();
 //			if(chars != null && !chars.toString().equals("")) return;
 //			if(this.postList != null && postList.size() > 0){
 //				Object[] postListKeySetArray = postList.keySet().toArray();
 //				for(int i = 0; i < postList.size(); ++ i){
 //					PostGoodsBean bean = postList.get(postListKeySetArray[i]);
 //					if(bean.getName().equals(STRING_AREA)){
 //						if(bean.getLabels() != null){
 //							for(int t = 0; t < bean.getLabels().size(); ++ t){
 //								if(location.subCityName.contains(bean.getLabels().get(t))){
 //									((TextView)districtView.findViewById(R.id.posthint)).setText(bean.getLabels().get(t));
 //									params.put(bean.getDisplayName(), bean.getLabels().get(t), bean.getValues().get(t));
 //									return;
 //								}
 //							}
 //						}						
 //					}
 //				}
 //			}
 //		}
 	}
 
 
 	@Override
 	public void onLocationFetched(BXLocation location) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void onGeocodedLocationFetched(BXLocation location) {
 		// TODO Auto-generated method stub
 		if(location == null) return;
 		if(handler != null){
 			handler.removeMessages(MSG_GETLOCATION_TIMEOUT);
 		}
 		
 //		if(this.inLocating){
 			detailLocation = location;
 //			if(locationView != null){
 //				this.getActivity().runOnUiThread(new Runnable(){
 //					@Override
 //					public void run(){
 //						setDetailLocationControl(detailLocation);
 //					}
 //				});
 //			}
 //		}
 //		this.inLocating = false;
 	}
 
 
 
 	@Override
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 //		this.inLocating = false;
 		return false;
 	}
 
 
 
 	@Override
 	public int getEnterAnimation() {
 		return R.anim.zoom_enter;
 	}
 
 
 
 	@Override
 	public int getExitAnimation() {
 		return R.anim.zoom_exit;
 	}
 
 	private boolean inreverse = false;
 
 	@Override
 	public void onRgcUpdated(BXLocation location) {
 		Log.d("location", "location   onRgcUpdate");
 		if(!this.gettingLocationFromBaidu) return;
 		// TODO Auto-generated method stub
 		if(!inreverse && location != null && (location.subCityName == null || location.subCityName.equals(""))){
 			Log.d("location", "location   call reverseGeocode");
 			LocationService.getInstance().reverseGeocode(location.fLat, location.fLon, this);
 			inreverse = true;
 		}else{
 			Log.d("location", "location   MSG_GEOCODING_FETCHED");
 			sendMessage(MSG_GEOCODING_FETCHED, location);
 		}
 	}
 
 
 
 //	@Override
 //	public void afterTextChanged(Editable s) {
 //
 //		
 //	}
 
 
 //
 //	@Override
 //	public void beforeTextChanged(CharSequence s, int start, int count,
 //			int after) {
 //		// TODO Auto-generated method stub
 //		
 //	}
 
 
 
 //	@Override
 //	public void onTextChanged(CharSequence s, int start, int before, int count) {
 //		// TODO Auto-generated method stub
 //		if(this.detailLocation == null) return;
 //		if(s != null && !s.toString().equals(detailLocation.detailAddress) && !s.toString().equals(detailLocation.subCityName)){
 //			detailLocation = null;
 //		}
 //	}
 
 
 
 	
 }
