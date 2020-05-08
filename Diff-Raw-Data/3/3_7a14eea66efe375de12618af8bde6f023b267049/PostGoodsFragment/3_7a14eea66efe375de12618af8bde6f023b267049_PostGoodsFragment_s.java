 package com.quanleimu.view.fragment;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.provider.MediaStore;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewParent;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.activity.BaseActivity;
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.BaseFragment.TabDef;
 import com.quanleimu.activity.BaseFragment.TitleDef;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.activity.ThirdpartyTransitActivity;
 import com.quanleimu.broadcast.CommonIntentAction;
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.PostGoodsBean;
 import com.quanleimu.entity.PostMu;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.Helper;
 import com.quanleimu.util.Util;
 import com.quanleimu.util.ViewUtil;
 
 public class PostGoodsFragment extends BaseFragment implements OnClickListener{
 
 	public static final int MSG_START_UPLOAD = 5;
 	public static final int MSG_FAIL_UPLOAD = 6;
 	public static final int MSG_SUCCED_UPLOAD = 7;
 	
 	static final public int HASH_POST_BEAN = "postBean".hashCode();
 	static final public int HASH_CONTROL = "control".hashCode();
 	static final private int MSG_MORE_DETAIL_BACK = 0xF0000001;
 	public ImageView img1, img2, img3;
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
 	public static final String IMAGEUNSPECIFIED = "image/*";
 
 	private PostParamsHolder params;
 	
 //	private AlertDialog ad; 
 //	private Button photoalbum, photomake, photocancle;
 	private ArrayList<String>bitmap_url;
 	private ImageView[] imgs;
 	private Bitmap[] cachedBps;
 	private String mobile, password;
 	private UserBean user;
 	private GoodsDetail goodsDetail;
 	public ArrayList<String> listUrl;
 	private int currentImgView = -1;
 	private int uploadCount = 0;
 	
 //	private BaseActivity baseActivity;
 //	private Bundle bundle;
 	
 	private boolean userValidated = false;
 	private boolean loginTried = false;
 	
 	static private String lastCategoryEnglishName = null;
 	static private String lastCategoryShowName = null;
 	
 	private ArrayList<String> otherProperties = new ArrayList<String>();
 	
 //	private View categoryItem = null;
 	
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		String categoryNames = this.getArguments().getString("cateNames");
 		this.goodsDetail = (GoodsDetail) getArguments().getSerializable("goodsDetail");
 		
 		String[] names = categoryNames.split(",");
 		if(names.length == 2){
 			this.categoryEnglishName = names[0];
 			this.categoryName = names[1];
 			
 			if (goodsDetail == null) //FIXME: just keep logic when do refactor.
 			{
 				lastCategoryEnglishName = names[0];
 				lastCategoryShowName = names[1];
 				Helper.saveDataToLocate(getActivity(), 
 						"lastcategorynames", lastCategoryEnglishName + "," + lastCategoryShowName);
 			}
 		}else if(names.length == 1){
 			this.categoryEnglishName = names[0];
 		}
 		
 		postList = new LinkedHashMap<String, PostGoodsBean>();
 		
 		params = new PostParamsHolder();
 		
 		listUrl = new ArrayList<String>();
 		bitmap_url = new ArrayList<String>(3);
 		bitmap_url.add(null);
 		bitmap_url.add(null);
 		bitmap_url.add(null);
 		currentImgView = -1;
 		uploadCount = 0;
 		
 		cachedBps = new Bitmap[] {null, null, null};
 		
 		if (savedInstanceState != null)
 		{
 			postList.putAll( (HashMap)savedInstanceState.getSerializable("postList"));
 			params = (PostParamsHolder) savedInstanceState.getSerializable("params");
 			listUrl.addAll((List) savedInstanceState.getSerializable("listUrl"));
 			bitmap_url.addAll((List) savedInstanceState.getSerializable("bitmapUrl"));
 			Util.filterArrayList(bitmap_url, 3);
 			currentImgView = savedInstanceState.getInt("imgIndex", -1);
 			uploadCount = savedInstanceState.getInt("uploadCount", 0);
 			
 			Parcelable[] ps = savedInstanceState.getParcelableArray("imgs");
 			cachedBps = new Bitmap[ps.length];
 			int i = 0;
 			for (Parcelable p : ps)
 			{
 				cachedBps[i++] = (Bitmap) p;
 			}
 		}
 	}
 	
 	
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		
 		outState.putSerializable("params", params);
 		outState.putSerializable("postList", postList);
 		outState.putSerializable("listUrl", listUrl);
 		outState.putSerializable("bitmapUrl", bitmap_url);
 		outState.putInt("imgIndex", currentImgView);
 		outState.putInt("uploadCount", uploadCount);
 		
 		outState.putParcelableArray("imgs", cachedBps);
 		
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
 		
 		builder.setTitle("提示:")
 				.setMessage("您所填写的数据将会丢失,放弃发布？")
 				.setNegativeButton("否", null)
 				.setPositiveButton("是",
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
 	}
 	
 	public void onPause() {
 		
 		extractInputData(layout_txt, params);
 		
 		super.onPause();
 	}
 	
 
 	@Override
 	public void onStackTop(boolean isBack) {
 		if(!userValidated){
 			usercheck();
 		}
 		else
 		{
 			this.showPost();
 		}
 		
 	}
 	
 	
 
 	@Override
 	public void onDestroyView() {
 		super.onDestroyView();
 	}
 
 
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		View v = inflater.inflate(R.layout.postgoodsview, null);
 		
 		layout_txt = (LinearLayout) v.findViewById(R.id.layout_txt);
 		
 		getActivity().getWindow().setSoftInputMode(
 				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		
		showSimpleProgress();
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
 			
 		
 			if(bean.getDisplayName().equals("地区")){
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
 								params.getData().put("地区", bean.getValues().get(t));
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
 			
 			String big = (goodsDetail.getImageList().getBig());
 //					.substring(1, (goodsDetail.getImageList()
 //							.getBig()).length() - 1);
 			big = Communication.replace(big);
 			String[] cbig = big.split(",");
 			List<String> smalls = new ArrayList<String>();
 			List<String> bigs = new ArrayList<String>();
 			for (int j = 0; j < listUrl.size(); j++) {
 				String bigUrl = (cbig == null || cbig.length <= j) ? null : cbig[j];
 				if(j > 2)break;
 				smalls.add(listUrl.get(j));
 				bigs.add(bigUrl);
 			}
 			new Thread(new Imagethread(smalls, bigs)).start();
 		}
 	}
 
 	private void usercheck() {
 		user = (UserBean) Util.loadDataFromLocate(this.getActivity(), "user");
 		if (user == null) {
 			if(loginTried){
 				finishFragment();
 			}else{
 				Bundle bundle = createArguments(null, "取消");
 				pushFragment(new LoginFragment(), bundle);
 				loginTried = true;
 			}
 		} else {
 			showPost();
 		}
 	}
 	
 	private void showPost()
 	{
 		userValidated = true;
 		mobile = user.getPhone();
 		password = user.getPassword();
 		String last = (String)Helper.loadDataFromLocate(getActivity(), "lastcategorynames");
 		if(last != null && !last.equals(",")){
 			String[] lasts = last.split(",");
 			if(lasts != null && lasts.length == 2){
 				lastCategoryEnglishName = lasts[0];
 				lastCategoryShowName = lasts[1];
 			}
 		}
 		//获取发布模板
 		String cityEnglishName = QuanleimuApplication.getApplication().cityEnglishName;
 		if(goodsDetail != null && goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CITYENGLISHNAME).length() > 0){
 			cityEnglishName = goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CITYENGLISHNAME);
 		}
 		if(categoryEnglishName == null || categoryEnglishName.equals("")){
 			categoryEnglishName = lastCategoryEnglishName;
 		}
 		PostMu postMu =  (PostMu) Util.loadDataFromLocate(this.getActivity(), categoryEnglishName + cityEnglishName);
 		if (postMu != null && !postMu.getJson().equals("")) {
 			json = postMu.getJson();
 			Long time = postMu.getTime();
 			if (time + (24 * 3600 * 100) < System.currentTimeMillis()) {
 //				myHandler.sendEmptyMessage(1);
 //				sendMessage(1, null);
 				addCategoryItem();
 				buildPostLayout();
 				new Thread(new GetCategoryMetaThread(false,cityEnglishName)).start();
 			} else {
 //				myHandler.sendEmptyMessage(1);
 //				sendMessage(1, null);
 				addCategoryItem();
 				buildPostLayout();
 			}
 		} else {
 			new Thread(new GetCategoryMetaThread(true,cityEnglishName)).start();
 		}
 
 		loadCachedData();
 	}
 	
 	@Override
 	public void onClick(View v) {
 		final Activity activity = getActivity();
 		
 		if (v == img1 || v == img2 || v == img3) {
 			for (int i = 0; i < imgs.length; i++) {
 				if (imgs[i].equals(v)) {
 					currentImgView = i;
 					ImageStatus status = getCurrentImageStatus(i);
 					if(ImageStatus.ImageStatus_Unset == status){
 //						showDialog();
 						ViewUtil.pickupPhoto(getActivity(), this.currentImgView);
 					}
 					else if(ImageStatus.ImageStatus_Failed == status){
 						String[] items = {"重试", "换一张"};
 						new AlertDialog.Builder(activity)
 						.setTitle("选择操作")
 						.setItems(items, new DialogInterface.OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								if(0 == which){
 									new Thread(new UpLoadThread(bitmap_url.get(currentImgView), currentImgView)).start();
 								}
 								else{
 									if (cachedBps[currentImgView] != null)
 									{
 										cachedBps[currentImgView].recycle();
 										cachedBps[currentImgView] = null;
 									}
 									bitmap_url.set(currentImgView, null);
 									imgs[currentImgView].setImageResource(R.drawable.d);
 //									showDialog();
 									ViewUtil.pickupPhoto(getActivity(), currentImgView);
 									//((BXDecorateImageView)imgs[currentImgView]).setDecorateResource(-1, BXDecorateImageView.ImagePos.ImagePos_LeftTop);
 								}
 								
 							}
 						})
 						.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								dialog.dismiss();
 							}
 						}).show();
 					}
 					else{
 						//String[] items = {"删除"};
 						new AlertDialog.Builder(this.getActivity())
 						.setMessage("删除当前图片?")
 						.setPositiveButton("删除", new DialogInterface.OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								bitmap_url.set(currentImgView, null);
 								imgs[currentImgView].setImageResource(R.drawable.d);
 								cachedBps[currentImgView] = null;
 //								((BXDecorateImageView)imgs[currentImgView]).setDecorateResource(-1, BXDecorateImageView.ImagePos.ImagePos_LeftTop);
 							}
 						})
 						.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								dialog.dismiss();
 							}
 						}).show();
 					}
 				}
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
 	
 	@Override
 	public void handleRightAction(){
 		if(uploadCount > 0){
 			Toast.makeText(this.getActivity(),"图片正在上传" + "!", 0).show();
 		}
 		// 提交
 		else{
 			extractInputData(layout_txt, params);
 			if(!check2()){
 				return;
 			}
 			showSimpleProgress();
 			new Thread(new UpdateThread()).start();
 		}
 	}
 	
 	static public void extractInputData(ViewGroup vg, PostParamsHolder params){
 		if(vg == null);
 		for(int i = 0; i < vg.getChildCount(); ++ i){
 			PostGoodsBean postGoodsBean = (PostGoodsBean)vg.getChildAt(i).getTag(HASH_POST_BEAN);
 			if(postGoodsBean == null) continue;
 			
 			if (postGoodsBean.getControlType().equals("input") 
 					|| postGoodsBean.getControlType().equals("textarea")) {
 				EditText et = (EditText)vg.getChildAt(i).getTag(HASH_CONTROL);
 				if(et != null){
 					params.put(postGoodsBean.getDisplayName(), et.getText().toString(), et.getText().toString() + postGoodsBean.getUnit());
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
 
 	private boolean filled() {
 		if(null == user || layout_txt == null || layout_txt.getChildCount() == 2 || layout_txt.getChildCount() == 0) return false;
 		
 		return true;
 	}
 
 	private boolean check2() {
 		LinkedHashMap<String, String> postMap = params.getData();
 		for (int i = 0; i < postList.size(); i++) {
 			String key = (String) postList.keySet().toArray()[i];
 			PostGoodsBean postGoodsBean = postList.get(key);
 			if (postGoodsBean.getRequired().endsWith("required")) {
 				if(!postMap.containsKey(postGoodsBean.getDisplayName()) 
 						|| postMap.get(postGoodsBean.getDisplayName()).equals("")
 						|| (postGoodsBean.getUnit() != null && postMap.get(postGoodsBean.getDisplayName()).equals(postGoodsBean.getUnit()))){
 					Toast.makeText(this.getActivity(), "请填写" + postGoodsBean.getDisplayName() + "!", 0).show();
 					return false;
 				}
 			}
 		}
 		if(categoryEnglishName.equals("nvzhaonan")){
 			for (int i = 0; i < bitmap_url.size(); i++) {
 				if(bitmap_url.get(i) != null && bitmap_url.get(i).contains("http:")){
 					return true;
 				}
 			}
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
 
 	/**
 	 * 发布线程
 	 * 
 	 * @author Administrator
 	 * 
 	 */
 	class UpdateThread implements Runnable {
 		public void run() {
 			String apiName = "ad_add";
 			ArrayList<String> list = new ArrayList<String>();
 
 			String city = QuanleimuApplication.getApplication().cityName;
 			if(goodsDetail != null){
 				String goodsCity = goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
 				if(null != goodsCity){
 					String[]cities = goodsCity.split(",");
 					if(cities != null && cities.length > 0){
 						city = cities[0];
 					}
 				}
 			}
 			for(int m = 0; m < layout_txt.getChildCount(); ++ m){
 				View v = layout_txt.getChildAt(m);
 				PostGoodsBean bean = (PostGoodsBean)v.getTag(HASH_POST_BEAN);
 				if(bean == null) continue;
 				if(bean.getDisplayName().equals("地区")){
 					TextView tv = (TextView)v.getTag(HASH_CONTROL);
 					if(tv != null && !tv.getText().toString().equals("")){
 						city += "," + tv.getText();
 					}
 				}
 			}
 			for(int m = 0; m < layout_txt.getChildCount(); ++ m){
 				View v = layout_txt.getChildAt(m);
 				PostGoodsBean bean = (PostGoodsBean)v.getTag(HASH_POST_BEAN);
 				if(bean == null) continue;
 				if(bean.getDisplayName().equals("具体地点")){
 					TextView tv = (TextView)v.getTag(HASH_CONTROL);
 					if(tv != null && !tv.getText().toString().equals("")){
 						city += "," + tv.getText();
 					}
 				}
 			}
 			if(!city.equals("")){
 				String googleUrl = String.format("http://maps.google.com/maps/geo?q=%s&output=csv", city);
 				try{
 					String googleJsn = Communication.getDataByUrlGet(googleUrl);
 					String[] info = googleJsn.split(",");
 					if(info != null && info.length == 4){
 						//goodsDetail.setValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT, info[2]);
 						//goodsDetail.setValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON, info[3]);
 						list.add("lat=" + info[2]);
 						list.add("lng=" + info[3]);
 					}
 				}catch(UnsupportedEncodingException e){
 					e.printStackTrace();
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}	
 
 
 			list.add("mobile=" + mobile);
 			String password1 = Communication.getMD5(password);
 			password1 += Communication.apiSecret;
 			String userToken = Communication.getMD5(password1);
 			list.add("userToken=" + userToken);
 			list.add("categoryEnglishName=" + categoryEnglishName);
 			list.add("cityEnglishName=" + QuanleimuApplication.getApplication().cityEnglishName);
 			list.add("rt=1");
 			//根据goodsDetail判断是发布还是修改发布
 			if (goodsDetail != null) {
 				list.add("adId=" + goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 				apiName = "ad_update";
 			}
 			LinkedHashMap<String, String> postMap = params.getData();
 			//发布发布集合
 			for (int i = 0; i < postMap.size(); i++) {
 				String key = (String) postMap.keySet().toArray()[i];
 
 				String values = postMap.get(key);
 				if (values != null && values.length() > 0 && postList.get(key) != null) {
 					try{
 						list.add(URLEncoder.encode(postList.get(key).getName(), "UTF-8")
 								+ "=" + URLEncoder.encode(values, "UTF-8").replaceAll("%7E", "~"));
 					}catch(UnsupportedEncodingException e){
 						e.printStackTrace();
 					}
 				}
 			}
 			//发布图片
 			String images = "";
 			for (int i = 0; i < bitmap_url.size(); i++) {				
 				if(bitmap_url.get(i) != null && bitmap_url.get(i).contains("http:")){
 					images += "," + bitmap_url.get(i);
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
 			String errorMsg = "内部错误，发布失败";
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url, true);
 				if (json != null) {
 					JSONObject jsonObject = new JSONObject(json);
 					JSONObject json = jsonObject.getJSONObject("error");
 					code = json.getInt("code");
 					message = json.getString("message");
 //					myHandler.sendEmptyMessage(3);
 					sendMessage(3, null);
 					return;
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (Communication.BXHttpException e) {
 				if(e.errorCode == 414){
 					errorMsg = "内容超出规定长度，请修改后重试";
 				}
 				else{
 					errorMsg = e.msg;
 				}
 				
 			} catch(Exception e){
 				e.printStackTrace();
 			}
 			hideProgress();
 			final String fmsg = errorMsg;
 			((BaseActivity)getActivity()).runOnUiThread(new Runnable(){
 				@Override
 				public void run(){
 					Toast.makeText(getActivity(), fmsg, 0).show();
 				}
 			});
 			
 		}
 	}
 
 	/**
 	 * 获取模板线程
 	 */
 	public int code = -1;
 	public String message = "";
 
 	class GetCategoryMetaThread implements Runnable {
 
 		private boolean isUpdate;
 		private String cityEnglishName = null;
 
 		public GetCategoryMetaThread(boolean isUpdate, String cityEnglishName) {
 			this.cityEnglishName = cityEnglishName;
 			this.isUpdate = isUpdate;
 		}
 		public GetCategoryMetaThread(boolean isUpdate) {
 			this.isUpdate = isUpdate;
 		}
 		
 
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
 					// 获取数据成功
 					PostMu postMu = new PostMu();
 					postMu.setJson(json);
 					postMu.setTime(System.currentTimeMillis());
 					Activity activity = getActivity();
 					if (activity != null)
 					{
 						//保存模板
 						Helper.saveDataToLocate(activity, categoryEnglishName
 								+ this.cityEnglishName, postMu);
 						if (isUpdate) {
 							sendMessage(1, null);
 						}
 					}
 				} else {
 					// {"error":{"code":0,"message":"\u66f4\u65b0\u4fe1\u606f\u6210\u529f"},"id":"191285466"}
 					sendMessage(2, null);
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				sendMessage(10, null);
 				e.printStackTrace();
 			} catch (Communication.BXHttpException e){
 				
 			}
 
 		}
 	}
 	
 	private Uri uri = null;
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		Log.d("QLM", "start to handle activity result");
 		if (resultCode == NONE) {
 			return;
 		}
 		// 拍照 
 		if (requestCode == CommonIntentAction.PhotoReqCode.PHOTOHRAPH) {
 			// 设置文件保存路径这里放在跟目录下
 			File picture = new File(Environment.getExternalStorageDirectory(), "temp" + this.currentImgView + ".jpg");
 			uri = Uri.fromFile(picture);
 			getBitmap(uri, requestCode); // 直接返回图片
 			//startPhotoZoom(uri); //截取图片尺寸
 		}
 
 		if (data == null) {
 			return;
 		}
 
 		// 读取相册缩放图片
 		if (requestCode == CommonIntentAction.PhotoReqCode.PHOTOZOOM) {
 			uri = data.getData();
 			//startPhotoZoom(uri);
 			getBitmap(uri, requestCode);
 		}
 		// 处理结果
 		if (requestCode == PHOTORESOULT) {
 			File picture = new File("/sdcard/cropped.jpg");
 			
 			uri = Uri.fromFile(picture);
 			getBitmap(uri, CommonIntentAction.PhotoReqCode.PHOTOHRAPH);
 			File file = new File(Environment.getExternalStorageDirectory(), "temp" + this.currentImgView + "jpg");
 			try {
 				if(file.isFile() && file.exists()){
 					file.delete();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 	
 	private void appendSelectedProperties(String[] lists){
 		if(lists == null || lists.length == 0) return;
 		for(int i = 0; i < lists.length; ++ i){
 			PostGoodsBean bean = this.postList.get(otherProperties.get(Integer.parseInt(lists[i]) - i));
 			if(bean == null) continue;
 			this.appendBeanToLayout(bean);
 			otherProperties.remove(Integer.parseInt(lists[i]) - i);
 		}
 		if(otherProperties.size() == 0){
 			if(layout_txt.getChildCount() > 0){
 				layout_txt.removeViewAt(layout_txt.getChildCount() - 1);
 			}
 		}
 		else{
 			if(layout_txt.getChildCount() > 0){
 				View v = layout_txt.getChildAt(layout_txt.getChildCount() - 1);
 				if(v != null){
 					View v2 = v.findViewById(R.id.postshow);
 					if(v2 != null && v2 instanceof TextView){
 						((TextView)v2).setText(otherProperties.toString());
 					}
 				}
 			}
 		}
 	}
 	
 	private void loadCachedData()
 	{
 		if (imgs != null)
 		{
 			for (int i=0; imgs.length>i; i++)
 			{
 				if (i >= 0 && i < cachedBps.length && cachedBps[i] != null)
 				{
 					imgs[i].setImageBitmap(cachedBps[i]);
 					imgs[i].invalidate();
 				}
 			}
 		}
 		
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
 	
 	static public boolean fetchResultFromViewBack(int message, Object obj, ViewGroup vg, PostParamsHolder params){
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
 	
 	@Override
 	public void onFragmentBackWithData(int message, Object obj){	
 		boolean match = fetchResultFromViewBack(message, obj, layout_txt, params);
 		if(match){
 //			postMap.put(result.first, result.second);
 			return;
 		}
 		switch(message){
 		case MSG_MORE_DETAIL_BACK:
 			params.merge((PostParamsHolder) obj);
 			break;
 		case POST_OTHERPROPERTIES:
 			String list = (String)obj;
 			if(!list.equals("")){
 				String[] lists = list.split(",");
 				appendSelectedProperties(lists);
 			}
 			break;
 	
 		case MSG_CATEGORY_SEL_BACK:{
 			layout_txt.removeAllViews();
 			otherProperties.clear();
 			postList.clear();
 			params.clear();
 			bitmap_url.clear();
 			bitmap_url.add(null);
 			bitmap_url.add(null);
 			bitmap_url.add(null);
 			
 			int i = 0;
 			for (Bitmap bp : cachedBps)
 			{
 				if (bp != null) bp.recycle();
 				cachedBps[i++] = null;
 			}
 
 			listUrl.clear();
 			imgs = null;
 			currentImgView = -1;
 			uploadCount = 0;
 						
 //			this.addCategoryItem();
 //			TextView tv = (TextView)layout_txt.findViewById(R.layout.item_post_select).getTag(HASH_CONTROL);
 			getArguments().putString("cateNames", (String) obj); //Update cate names.
 			String[] backMsg = ((String)obj).split(",");
 			if(backMsg == null || backMsg.length != 2) break;
 //			if(tv != null){
 //				tv.setText(backMsg[1]);
 //			}
 			this.categoryEnglishName = backMsg[0];
 			this.categoryName = backMsg[1];
 			lastCategoryEnglishName = backMsg[0];
 			lastCategoryShowName = backMsg[1];
 			Helper.saveDataToLocate(getActivity(), 
 					"lastcategorynames", lastCategoryEnglishName + "," + lastCategoryShowName);
 
 			this.usercheck();
 			
 			break;
 		}
 		default:
 			break;
 		}
 	}
 	
 	
 	private void getBitmap(Uri uri, int id) {
 		String path = uri == null ? "" : uri.toString();
 		Log.w("QLM", "upload image : " + path);
 		if (uri != null) {
 				if (imgs != null)
 				{
 					imgs[this.currentImgView].setFocusable(true);
 				}
 
 				new Thread(new UpLoadThread(path, currentImgView)).start();
 		}
 
 	}
 	
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
 
 	public void saveSDCard(Bitmap photo) {
 		try {
 			String filepath = "/sdcard/baixing";
 			File files = new File(filepath);
 			files.mkdir();
 			File file = new File(filepath, "temp" + this.currentImgView + ".jpg");
 			FileOutputStream outStream = new FileOutputStream(file);
 			String path = file.getAbsolutePath();
 			Log.i(path, path);
 			photo.compress(CompressFormat.JPEG, 100, outStream);
 			outStream.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void appendBeanToLayout(PostGoodsBean postBean){
 		Activity activity = getActivity();
 		ViewGroup layout = createItemByPostBean(postBean, this);//FIXME:
 		if(postBean.getName().equals("contact") && layout != null){
 			((EditText)layout.getTag(HASH_CONTROL)).setText(mobile);
 		}
 		if (postBean.getControlType().equals("image")) {
 			layout = new LinearLayout(this.getActivity());
 			((LinearLayout)layout).setOrientation(LinearLayout.HORIZONTAL);
 			layout.setPadding(10, 10, 10, 10);
 			layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));
 			
 			int height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
 			int fixHotHeight = height * 15 / 100;
 			if(fixHotHeight < 50)
 			{
 			    fixHotHeight = 50;
 			}
 			img1 = new ImageView(activity);
 			img2 = new ImageView(activity);
 			img3 = new ImageView(activity);
 			imgs = new ImageView[] { img1, img2, img3 };
 			
 		//fixHotHeight = layout.getHeight() - 5 * 2;
 		    img1.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		    img1.setAdjustViewBounds(true);                       
 		    img1.setMaxHeight(fixHotHeight);
 		    img1.setMaxWidth(fixHotHeight);
 		    LinearLayout l1 = new LinearLayout(activity);
 		    l1.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
 		    l1.addView(img1);
 		    
 		    img2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		    img2.setAdjustViewBounds(true);
 		    img2.setMaxHeight(fixHotHeight);
 		    img2.setMaxWidth(fixHotHeight);
 		    LinearLayout l2 = new LinearLayout(activity);
 		    l2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
 		    l2.addView(img2);
 		    
 		    img3.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
 		    img3.setAdjustViewBounds(true);
 		    img3.setMaxHeight(fixHotHeight);
 		    img3.setMaxWidth(fixHotHeight);
 		    LinearLayout l3 = new LinearLayout(activity);
 		    l3.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		    l3.addView(img3);
 		    
 			img1.setImageResource(R.drawable.d);
 			img2.setImageResource(R.drawable.d);
 			img3.setImageResource(R.drawable.d);
 			img1.setOnClickListener(PostGoodsFragment.this);
 			img2.setOnClickListener(PostGoodsFragment.this);
 			img3.setOnClickListener(PostGoodsFragment.this);
 			layout.addView(l1);
 			layout.addView(l2);
 			layout.addView(l3);
 		}
 		
 		TextView border = new TextView(activity);
 		border.setLayoutParams(new LayoutParams(
 				LayoutParams.FILL_PARENT, 1, 1));
 		border.setBackgroundResource(R.drawable.list_divider);
 
 		if(layout_txt.getChildCount() % 2 == 1){
 			int insertIndex = 
 					layout_txt.getChildCount() >= 3 ? layout_txt.getChildCount() - 3 : layout_txt.getChildCount() - 1;
 			insertIndex = insertIndex >= 0 ? insertIndex : 0;
 			layout_txt.addView(layout, insertIndex);
 			layout_txt.addView(border, insertIndex + 1);
 		}
 		else{
 			layout_txt.addView(layout);
 			layout_txt.addView(border);
 		}
 	}
 	
 	private void addCategoryItem(){
 		Activity activity = getActivity();
 		if(this.goodsDetail != null)return;
 		LayoutInflater inflater = LayoutInflater.from(activity);
 		View categoryItem = inflater.inflate(R.layout.item_post_select, null);
 		categoryItem.setTag(HASH_CONTROL, categoryItem.findViewById(R.id.posthint));
 		((TextView)categoryItem.findViewById(R.id.postshow)).setText("分类");
 		categoryItem.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 					Bundle bundle = createArguments(null, null);
 					bundle.putInt(ARG_COMMON_REQ_CODE, MSG_CATEGORY_SEL_BACK);
 					pushFragment(new GridCateFragment(), bundle);
 			}				
 		});
 		layout_txt.addView(categoryItem);
 		
 		if(categoryEnglishName != null && !categoryEnglishName.equals("") && categoryName != null){
 			 ((TextView)categoryItem.findViewById(R.id.posthint)).setText(categoryName);
 		}
 		
 		TextView border = new TextView(activity);
 		border.setLayoutParams(new LayoutParams(
 				LayoutParams.FILL_PARENT, 1, 1));
 		border.setBackgroundResource(R.drawable.list_divider);
 		layout_txt.addView(border);
 	}
 	
 	private void buildPostLayout(){
 		Log.d(TAG, "start to build layout");
 		otherProperties.clear();
 		
 		final Activity activity = getActivity();
 		
 		//根据模板显示
 		if(null == json || json.equals("")) return;
 		postList = JsonUtil.getPostGoodsBean(json); 
 		Object[] postListKeySetArray = postList.keySet().toArray();
 		for (int i = 0; i < postList.size(); i++) {
 			String key = (String) postListKeySetArray[i];
 			PostGoodsBean postBean = postList.get(key);
 			if(postBean.getName().equals("地区")){
 				this.appendBeanToLayout(postBean);
 				continue;
 			}
 //			if(goodsDetail != null && (postBean.getName().equals("images") && (goodsDetail.getImageList() != null 
 //					&& goodsDetail.getImageList().getResize180() != null 
 //					&& !goodsDetail.getImageList().getResize180().equals("")))){
 			if(postBean.getName().equals("images")){
 				this.appendBeanToLayout(postBean);
 				continue;
 			}
 
 			if(!postBean.getRequired().endsWith("required") 
 					&& (goodsDetail == null 
 						|| goodsDetail.getValueByKey(postBean.getName()) == null 
 						|| goodsDetail.getValueByKey(postBean.getName()).equals(""))){
 				otherProperties.add(postBean.getDisplayName());
 				continue;
 			}
 			this.appendBeanToLayout(postBean);
 		}
 		if(otherProperties.size() > 0){
 			LayoutInflater inflater = LayoutInflater.from(activity);
 			View v = inflater.inflate(R.layout.item_post_select, null);
 			((TextView)v.findViewById(R.id.postshow)).setText(otherProperties.toString());
 //			((TextView)v.findViewById(R.id.postshow)).setWidth(layout_txt.getWidth() * 2 / 3);
 			((TextView)v.findViewById(R.id.posthint)).setText("非必选");
 			v.setOnClickListener(new OnClickListener(){
 				@Override
 				public void onClick(View v) {
 					Bundle bundle = createArguments(null, null);
 					bundle.putInt(ARG_COMMON_REQ_CODE, MSG_MORE_DETAIL_BACK);
 					bundle.putSerializable("beans", postList);
 					bundle.putSerializable("details", otherProperties);
 					bundle.putSerializable("existing", params);
 					
 					pushFragment(new FillMoreDetailFragment(), bundle);
 				}	
 			});
 			layout_txt.addView(v);
 		}
 		editpostUI();		
 	}
 	
 	
 
 	@Override
 	protected void handleMessage(Message msg, Activity activity, View rootView) {
 
 		hideProgress();
 		
 		switch (msg.what) {
 		case MSG_START_UPLOAD:
 		{
 			Integer index = (Integer) msg.obj;
 			if (imgs != null)
 			{
 				imgs[index.intValue()].setImageResource(R.drawable.u);
 				imgs[index].setClickable(false);
 				imgs[index.intValue()].invalidate();
 			}
 			break;
 		}
 		case MSG_FAIL_UPLOAD:
 		{
 			if (imgs != null)
 			{
 				Integer index = (Integer) msg.obj;
 				imgs[index.intValue()].setImageResource(R.drawable.f);
 				imgs[index].setClickable(true);
 				imgs[index.intValue()].invalidate();
 			}
 			break;
 		}
 		case MSG_SUCCED_UPLOAD:
 		{
 			Integer index = (Integer) msg.obj;
 			if (imgs != null)
 			{
 				imgs[index].setImageBitmap(cachedBps[index]);
 				imgs[index].setClickable(true);
 				imgs[index].invalidate();
 			}
 			break;
 		}
 		case -2:
 		{
 			loadCachedData();
 			break;
 		}
 		case 1:
 			addCategoryItem();
 			buildPostLayout();
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
 			break;
 		case 3:
 			try {
 				hideProgress();
 				JSONObject jsonObject = new JSONObject(json);
 				String id;
 				try {
 					id = jsonObject.getString("id");
 				} catch (Exception e) {
 					id = "";
 					e.printStackTrace();
 				}
 				JSONObject json = jsonObject.getJSONObject("error");
 				String message = json.getString("message");
 				Toast.makeText(activity, message, 0).show();
 				if (!id.equals("")) {
 					Bundle args = createArguments(null, null);
 					args.putInt("forceUpdate", 1);
 					// 发布成功
 					// Toast.makeText(PostGoods.this, "未显示，请手动刷新",
 					// 3).show();
 					if(goodsDetail == null){
 						String lp = getArguments().getString("lastPost");
 						if(lp != null && !lp.equals("")){
 							lp += "," + id;
 						}else{
 							lp = id;
 						}
 //						Log.d("lastpost", lp + "    ***********************************");
 						args.putString("lastPost", lp);
 					}
 					((BaseActivity) activity).pushFragment(new PersonalInfoFragment(), args, true);
 					
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
 			break;
 		}
 	}
 
 
 
 
 	/**
 	 * 上传头像
 	 * 
 	 * @author Administrator
 	 * 
 	 */
 	class UpLoadThread implements Runnable {
 		private String bmpPath;
 		private int currentIndex = -1;
 		private Bitmap thumbnailBmp = null;
 
 		public UpLoadThread(String path, int index) {
 			super();
 			this.bmpPath = path;
 			currentIndex = index;
 		}
 
 		public void run() {
 
 			final Activity activity = getActivity();
 			if (activity == null)
 			{
 				return;
 			}
 			activity.runOnUiThread(new Runnable(){
 				public void run(){
 					//((BXDecorateImageView)imgs[PostGoods.this.currentImgView]).setDecorateResource(R.drawable.alert_orange, BXDecorateImageView.ImagePos.ImagePos_Center);
 					sendMessage(MSG_START_UPLOAD, currentIndex);
 				}
 			});	
 			synchronized(PostGoodsFragment.this){
 //			try{
 			//	uploadMutex.wait();
 //			}catch(InterruptedException e){
 				//e.printStackTrace();
 //			}
 			++ uploadCount;
 			if(bmpPath == null || bmpPath.equals("")) return;
 
 			Uri uri = Uri.parse(bmpPath);
 			String path = getRealPathFromURI(uri); // from Gallery
 			if (path == null) {
 				path = uri.getPath(); // from File Manager
 			}
 			Bitmap currentBmp = null;
 			if (path != null) {
 				try {
 				    
 				    BitmapFactory.Options bfo = new BitmapFactory.Options();
 			        bfo.inJustDecodeBounds = true;
 			        BitmapFactory.decodeFile(path, bfo);
 			        
 				    BitmapFactory.Options o =  new BitmapFactory.Options();
 	                o.inPurgeable = true;
 	                
 	                int maxDim = 600;
 	                
 	                o.inSampleSize = getClosestResampleSize(bfo.outWidth, bfo.outHeight, maxDim);
 	                
 	                
 	                currentBmp = BitmapFactory.decodeFile(path, o);
 					//photo = Util.newBitmap(tphoto, 480, 480);
 					//tphoto.recycle();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}			
 			if(currentBmp == null) {
 				-- uploadCount;
 				return;
 			}
 				
 			String result = Communication.uploadPicture(currentBmp);	
 			-- uploadCount;
 			thumbnailBmp = PostGoodsFragment.createThumbnail(currentBmp, imgs[currentIndex].getHeight());
 			cachedBps[currentIndex] = thumbnailBmp;
 			currentBmp.recycle();
 			currentBmp = null;
 	
 			if (result != null) {
 				bitmap_url.set(currentIndex, result);
 
 				activity.runOnUiThread(new Runnable(){
 					public void run(){
 						sendMessage(MSG_SUCCED_UPLOAD, currentIndex);
 						Toast.makeText(activity, "上传图片成功", 0).show();
 					}
 				});	                
 			} else {
 //				PostGoods.BXImageAndUrl imgAn dUrl = new PostGoods.BXImageAndUrl();
 				activity.runOnUiThread(new Runnable(){
 					public void run(){
 						bitmap_url.set(currentIndex, bmpPath);
 						//((BXDecorateImageView)imgs[PostGoods.this.currentImgView]).setDecorateResource(R.drawable.alert_red, BXDecorateImageView.ImagePos.ImagePos_RightTop);
 						sendMessage(MSG_FAIL_UPLOAD, currentIndex);
 						Toast.makeText(activity, "上传图片失败", 0).show();
 					}
 				});						
 			}
 //			uploadMutex.notifyAll();
 			}
 		}
 	}
 	
 	private static int getClosestResampleSize(int cx, int cy, int maxDim)
     {
         int max = Math.max(cx, cy);
         
         int resample = 1;
         for (resample = 1; resample < Integer.MAX_VALUE; resample++)
         {
             if (resample * maxDim > max)
             {
                 resample--;
                 break;
             }
         }
         
         if (resample > 0)
         {
             return resample;
         }
         return 1;
     }
 
 	static private Bitmap createThumbnail(Bitmap srcBmp, int thumbHeight)
 	{
 		Float width  = new Float(srcBmp.getWidth());
 		Float height = new Float(srcBmp.getHeight());
 		Float ratio = width/height;
 		Bitmap thumbnail = Bitmap.createScaledBitmap(srcBmp, (int)(thumbHeight*ratio), thumbHeight, true);
 
 //		int padding = (THUMBNAIL_WIDTH - imageBitmap.getWidth())/2;
 //		imageView.setPadding(padding, 0, padding, 0);
 //		imageView.setImageBitmap(imageBitmap);
 		return thumbnail;
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
 			PostGoodsFragment.this.imgs[index].setImageBitmap(bmp);
 			PostGoodsFragment.this.imgs[index].setClickable(true);
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
 			
 			for(int i = 0; i < smalls.size(); ++ i){
 				PostGoodsFragment.this.imgs[i].setClickable(false);
 			}
 			for(int t = 0; t < smalls.size(); ++ t){
 				try {
 					Bitmap tbitmap = Util.getImage(smalls.get(t));
 					PostGoodsFragment.this.bitmap_url.set(t, bigs.get(t));
 					activity.runOnUiThread(new SetBitmapThread(t, tbitmap));
 					
 				} catch (Exception e) {
 					e.printStackTrace();
 					PostGoodsFragment.this.imgs[t].setClickable(true);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void initTitle(TitleDef title){
 		title.m_visible = true;
 		title.m_title = "发布";
 		title.m_leftActionHint = "返回";
 		title.m_rightActionHint = "立即发布";
 	}
 	
 	@Override
 	public void initTab(TabDef tab){
 		tab.m_visible = false;
 //		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
 	}
 	
 	enum ImageStatus{
 		ImageStatus_Normal,
 		ImageStatus_Unset,
 		ImageStatus_Failed
 	}
 	private ImageStatus getCurrentImageStatus(int index){
 		if(bitmap_url.get(index) == null)return ImageStatus.ImageStatus_Unset;
 		if(bitmap_url.get(index).contains("http:")) return ImageStatus.ImageStatus_Normal; 
 		return ImageStatus.ImageStatus_Failed;
 	}
 	
 	public static ViewGroup createItemByPostBean(PostGoodsBean postBean, final BaseFragment fragment){
 		ViewGroup layout = null;
 		
 		Activity activity = fragment.getActivity();
 		if (postBean.getControlType().equals("input")) {
 			LayoutInflater inflater = LayoutInflater.from(activity);
 			View v = inflater.inflate(R.layout.item_post_edit, null);
 			((TextView)v.findViewById(R.id.postshow)).setText(postBean.getDisplayName());
 
 			EditText text = (EditText)v.findViewById(R.id.postinput);
 			v.setTag(HASH_POST_BEAN, postBean);
 			v.setTag(HASH_CONTROL, text);
 			if(postBean.getNumeric() != 0){
 				text.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
 			}
 			if (!postBean.getUnit().equals("")) {
 				((TextView)v.findViewById(R.id.postunit)).setText(postBean.getUnit());
 			}
 			layout = (ViewGroup)v;
 		} else if (postBean.getControlType().equals("select")) {
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
 
 			if(postBean.getName().equals("description"))//description is builtin keyword
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
 		}
 		if(postBean.getControlType().equals("select") || postBean.getControlType().equals("checkbox")){
 			layout.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					PostGoodsBean postBean = (PostGoodsBean) v.getTag(HASH_POST_BEAN);
 
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
 								((BaseActivity)fragment.getActivity()).pushFragment(new MultiLevelSelectionFragment(), bundle, false);
 							}
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
 							}
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
 			});
 		}
 		return layout;
 	}
 	
 }
