 package com.quanleimu.view;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.PostGoodsBean;
 import com.quanleimu.entity.PostMu;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.Helper;
 import com.quanleimu.util.Util;
 import com.quanleimu.util.BXDecorateImageView;
 import com.quanleimu.view.BaseView.ETAB_TYPE;
 import com.quanleimu.view.BaseView.TabDef;
 import com.quanleimu.view.BaseView.TitleDef;
 
 import android.os.Environment;
 import java.util.Set;
 import java.io.Serializable;
 import com.quanleimu.activity.BaseActivity;
 import com.quanleimu.activity.MyApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.activity.Login;
 
 public class PostGoodsView extends BaseView implements OnClickListener {
 	public ImageView img1, img2, img3;
 	public String categoryEnglishName = "";
 	public String json = "";
 	public LinearLayout layout_txt;
 	public LinkedHashMap<String, PostGoodsBean> postList;		//发布模板每一项的集合
 	public static final int NONE = 0;
 	public static final int PHOTOHRAPH = 1;
 	public static final int PHOTOZOOM = 2; 
 	public static final int PHOTORESOULT = 3;
 	public static final int POST_LIST = 4;
 	public static final String IMAGEUNSPECIFIED = "image/*";
 
 	private LinkedHashMap<String, TextView> tvlist;
 	// private int selId;
 	private String displayname;
 	private LinkedHashMap<String, String> postMap;				//发布需要提交的参数集合	
 	private LinkedHashMap<String, String> initialValueMap;
 	private LinkedHashMap<Integer, Object> btMap;				//根据postList集添加对应的控件View
 	private LinkedHashMap<String, Object> editMap;				
 	private EditText descriptionEt, titleEt;
 	private AlertDialog ad; 
 	private Button photoalbum, photomake, photocancle;
 	private ArrayList<String>bitmap_url;
 	private ImageView[] imgs;
 	private String mobile, password;
 	private UserBean user;
 	private GoodsDetail goodsDetail;
 	public List<String> listUrl;
 	private int currentImgView = -1;
 	private int uploadCount = 0;
 	
 	private BaseActivity baseActivity;
 	private Bundle bundle;
 	
 	public PostGoodsView(BaseActivity context, Bundle bundle, String categoryEnglishName){
 		super(context, bundle);
 		this.baseActivity = context;
 		this.categoryEnglishName = categoryEnglishName;
 		this.bundle = bundle;
 		init();
 	}
 
 	public PostGoodsView(BaseActivity context, Bundle bundle, String categoryEnglishName, GoodsDetail detail){
 		super(context, bundle);
 		this.baseActivity = context;
 		this.categoryEnglishName = categoryEnglishName;
 		this.goodsDetail = detail;
 		this.bundle = bundle;
 		init();
 	}
 
 	@Override
 	public void onDestroy() {
 		// TODO Auto-generated method stub
 		for(int i = 0; i < 3; ++ i){
 			File file = new File(Environment.getExternalStorageDirectory(), "temp" + i + ".jpg");
 			if(file.exists()){
 				file.delete();
 			}
 			file = null;
 		}
 		super.onDestroy();
 	}
 	
 	private String getEditMapKeyDisplayName(String keyName){
 		if(null == editMap) return null;
 		Set<String>keySet = editMap.keySet();
 		if(null == keySet) return null;
 		Object[] keys = (Object[])keySet.toArray();
 		for(int i = 0; i < keys.length; ++ i){
 			String[] subKeys = ((String)keys[i]).split(" ");
 			if(subKeys.length != 2) continue;
 			if(subKeys[1].equals(keyName)) return subKeys[0];
 		}
 		return null;	
 	}
 	
 	private Object getEditMapValue(String key){
 		if(null == editMap) return null;
 		Set<String>keySet = editMap.keySet();
 		if(null == keySet) return null;
 		Object[] keys = (Object[])keySet.toArray();
 		for(int i = 0; i < keys.length; ++ i){
 			String[] subKeys = ((String)keys[i]).split(" ");
 			for(int j = 0; j < subKeys.length; ++ j){
 				if(subKeys[j].equals(key)){
 					return editMap.get(keys[i]);
 				}
 			}
 		}
 		return null;
 	}
 
 	private void ConfirmAbortAlert(){
 			AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
 			builder.setTitle("提示:")
 					.setMessage("您所填写的数据将会丢失,放弃发布？")
 					.setNegativeButton("否", null)
 					.setPositiveButton("是",
 							new DialogInterface.OnClickListener() {
 	
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									if(m_viewInfoListener != null){
 										m_viewInfoListener.onBack();
 									}
 //									finish();
 								}
 							});
 			builder.create().show();
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event){
 		if(keyCode == KeyEvent.KEYCODE_BACK && filled()){
 			ConfirmAbortAlert();
 			return true;
 		}
 		
 		return super.onKeyDown(keyCode, event);			
 	}
 	
 	private void init() {
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		View v = inflater.inflate(R.layout.postgoodsview, null);
 		this.addView(v);
 
 		layout_txt = (LinearLayout) v.findViewById(R.id.layout_txt);
 		
 		baseActivity.getWindow().setSoftInputMode(
 				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		
 		initial();

		pd = new ProgressDialog(this.getContext());
		pd.setTitle("提示");
		pd.setMessage("请稍候...");
		pd.setCancelable(true);

		usercheck();

 	}
 
 	/**
 	 * 编辑发布
 	 */
 	private void editpostUI() {
 		// TODO Auto-generated method stub
 		//有goodsDetail，显示goodsDetail内容，编辑发布。
 		if (goodsDetail != null) {
 			System.out.println("editMap--->" + editMap.toString());
 			System.out.println("goodsDetail--->"
 					+ goodsDetail.getMetaData().toString());
 			// metaData里的参数
 			ArrayList<String> metas = goodsDetail.getMetaData();
 			for (int i = 0; i < metas.size(); i++) {
 				String[] curMeta = metas.get(i).split(" ");
 				String key = curMeta[0];
 				Object obj = this.getEditMapValue(key);
 				System.out.println("obj--->" + obj);
 				PostGoodsBean bean = postList.get(key);
 				if(bean != null && !bean.getUnit().equals("")){
 					if(curMeta[1] != null){
 						int pos = curMeta[1].indexOf(bean.getUnit());
 						if(pos != -1){
 							curMeta[1] = curMeta[1].substring(0, pos);
 						}
 					}
 				}
 				if (obj != null) {
 					if (obj instanceof TextView) {
 						((TextView) obj).setText(curMeta[1]);
 					} else if (obj instanceof EditText) {
 						((EditText) obj).setText(curMeta[1]);
 					} else if (obj instanceof List<?>) {
 						String value = curMeta[1];
 						List<CheckBox> list = ((List<CheckBox>) obj);
 						for (int j = 0; j < list.size(); j++) {
 							CheckBox c = list.get(j);
 							String v = (String) c.getTag();
 							if (value.contains(v)) {
 								c.setChecked(true);
 							} else {
 								c.setChecked(false);
 							}
 						}
 					}
 				}
 				postMap.put(curMeta[0], curMeta[1]);
 				
 				//PostGoodsBean bean = postList.get(key);
 				if(null == bean) continue;
 				HashMap<String, String>map = bean.getLvmap();
 				String value = null;
 				if(map.containsKey(curMeta[1])){
 					value = map.get(curMeta[1]);
 				}
 				if (value != null && value.length() > 0) {
 					postMap.put(key, value);
 				} else if(postMap.get(key) == null || postMap.get(key).toString().length() == 0){
 					postMap.put(key, "");
 				}
 			}
 			Set<String>keySet = goodsDetail.getKeys();
 			Object[] keys = null;
 			if(keySet != null){
 				keys = keySet.toArray();
 			}
 			Object objArea = this.getEditMapValue("地区");
 			if(objArea != null){
 				String strArea = goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
 				String[] areas = strArea.split(",");
 				if(areas.length >= 2){
 					if(objArea instanceof TextView){
 						((TextView)objArea).setText(areas[1]);
 					}
 					else if(objArea instanceof EditText){
 						((EditText)objArea).setText(areas[1]);
 					}
 					PostGoodsBean areaBean = postList.get("地区");
 					if(areaBean != null && areaBean.getValues() != null && areaBean.getLabels() != null){
 						List<String> areaLabels = areaBean.getLabels();
 						for(int i = 0; i < areaLabels.size(); ++ i){
 							if(areaLabels.get(i).equals(areas[1])){
 								postMap.put("地区", areaBean.getValues().get(i));
 								break;
 							}
 						}
 					}
 				}
 			}
 			for(int i = 0; i < keys.length; ++ i){
 				Object obj = this.getEditMapValue((String)keys[i]);
 				PostGoodsBean bean = postList.get(this.getEditMapKeyDisplayName((String)keys[i]));
 				String labelValue = goodsDetail.getValueByKey((String)keys[i]);
 				if(null != bean && null != bean.getValues() && null != bean.getLabels()){
 					for(int j = 0; j < bean.getValues().size(); ++ j){
 						if(labelValue.equals(bean.getValues().get(j))){
 							labelValue = bean.getLabels().get(j).toString();
 							break;
 						}
 					}
 				}
 				if (obj != null) {
 					if (obj instanceof TextView) {
 						String text = ((TextView)obj).getText().toString();
 						if(((TextView)obj).getText() != null 
 								&& !((TextView)obj).getText().toString().equals("")
 								&& !((TextView)obj).getText().toString().equals("请选择")
 								&& !((TextView)obj).getText().toString().equals("请输入")
 								&& !((TextView)obj).getText().toString().equals("请填写"))
 							continue;
 						((TextView) obj).setText(labelValue);
 					} else if (obj instanceof EditText) {
 						if(((EditText)obj).getText() != null 
 								&& !((EditText)obj).getText().toString().equals("")
 								&& !((EditText)obj).getText().toString().equals("")
 								&& !((EditText)obj).getText().toString().equals("请选择")
 								&& !((EditText)obj).getText().toString().equals("请输入")
 								&& !((EditText)obj).getText().toString().equals("请填写"))
 							continue;
 						((EditText) obj).setText(labelValue);
 					} else if (obj instanceof List<?>) {
 						String value = goodsDetail.getValueByKey((String)keys[i]);
 						List<CheckBox> list = ((List<CheckBox>) obj);
 						for (int j = 0; j < list.size(); j++) {
 							CheckBox c = list.get(j);
 							String v = (String) c.getTag();
 							if (value.contains(v)) {
 								c.setChecked(true);
 							} else {
 								c.setChecked(false);
 							}
 						}
 					}
 					postMap.put(getEditMapKeyDisplayName((String)keys[i]), goodsDetail.getValueByKey((String)keys[i]));
 				}					
 			}
 			
 			if (goodsDetail.getImageList() != null) {
 				String b = (goodsDetail.getImageList().getResize180())
 						.substring(1, (goodsDetail.getImageList()
 								.getResize180()).length() - 1);
 				b = Communication.replace(b);
 				if (b.contains(",")) {
 					String[] c = b.split(",");
 					for (int k = 0; k < c.length; k++) {
 						listUrl.add(c[k]);
 					}
 				}else{
 					listUrl.add(b);
 				}
 				
 				String big = (goodsDetail.getImageList().getBig())
 						.substring(1, (goodsDetail.getImageList()
 								.getBig()).length() - 1);
 				big = Communication.replace(big);
 				String[] cbig = big.split(",");
 
 				//System.out.println("listUrl.size--->" + listUrl.size());
 				for (int j = 0; j < listUrl.size(); j++) {
 					//System.out.println(listUrl.get(j) + "   " + imgs[j]);
 					String bigUrl = (cbig == null || cbig.length <= j) ? null : cbig[j];
 					new Thread(new Imagethread(listUrl.get(j), bigUrl)).start();
 //					imgs[j].setTag(listUrl.get(j));
 //					LoadImage.addTask(listUrl.get(j), imgs[j]);
 //					LoadImage.doTask();
 				}
 			}
 		}
 		
 		//copy to initial value map
 		for(int j = 0; j < postMap.size(); ++j)
 		{
 			String key = (String) postMap.keySet().toArray()[j];
 			initialValueMap.put(key, postMap.get(key).toString());
 		}
 		
 		for(int i = 0; i < listUrl.size(); ++i){
 			if(listUrl.get(i) != null && listUrl.get(i).length() > 0){
 				initialValueMap.put(listUrl.get(i), "existingTag");
 			}
 		}
 	}
 
 	/**
 	 * 用户登陆进发布页、否则进登陆页
 	 */
 	private void usercheck() {
 		user = (UserBean) Util.loadDataFromLocate(this.getContext(), "user");
 		if (user == null) {
 			if(this.m_viewInfoListener != null){
 				m_viewInfoListener.onNewView(new Login(baseActivity, bundle));
 			}
 		} else {
 			mobile = user.getPhone();
 			password = user.getPassword();
 			
 			//获取发布模板
 			String cityEnglishName = MyApplication.getApplication().cityEnglishName;
 			if(goodsDetail != null && goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CITYENGLISHNAME).length() > 0){
 				cityEnglishName = goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CITYENGLISHNAME);
 			}
 			PostMu postMu = (PostMu) Util.loadDataFromLocate(this.getContext(),
 					categoryEnglishName + cityEnglishName);
 			if (postMu != null && !postMu.getJson().equals("")) {
 				json = postMu.getJson();
 				Long time = postMu.getTime();
 				if (time + (24 * 3600 * 100) < System.currentTimeMillis()) {
 					//更新界面
 					myHandler.sendEmptyMessage(1);
 					//下载新数据	false-不更新界面
 					new Thread(new GetCategoryMetaThread(false,cityEnglishName)).start();
 				} else {
 					//更新界面
 					myHandler.sendEmptyMessage(1);
 				}
 			} else {
 				//下载新数据	true--更新界面
 				pd.show();
 				new Thread(new GetCategoryMetaThread(true,cityEnglishName)).start();
 			}
 
 		}
 	}
 
 	/**
 	 * 初始化
 	 */
 	private void initial() {
 		tvlist = new LinkedHashMap<String, TextView>();
 		postList = new LinkedHashMap<String, PostGoodsBean>();
 		postMap = new LinkedHashMap<String, String>();
 		initialValueMap = new LinkedHashMap<String, String>();
 		
 		listUrl = new ArrayList<String>();
 		// textViewMap = new LinkedHashMap<Integer, TextView>();
 		// checkBoxMap = new LinkedHashMap<Integer, List<CheckBox>>();
 		bitmap_url = new ArrayList<String>();
 		bitmap_url.add(null);
 		bitmap_url.add(null);
 		bitmap_url.add(null);
 		btMap = new LinkedHashMap<Integer, Object>();
 		editMap = new LinkedHashMap<String, Object>();
 		
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
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 
 		if (v == img1 || v == img2 || v == img3) {
 			// 3张图片点击事件
 			for (int i = 0; i < imgs.length; i++) {
 				if (imgs[i].equals(v)) {
 					currentImgView = i;
 					ImageStatus status = getCurrentImageStatus(i);
 					if(ImageStatus.ImageStatus_Unset == status){
 						showDialog();
 					}
 					else if(ImageStatus.ImageStatus_Failed == status){
 						String[] items = {"重试", "换一张"};
 						new AlertDialog.Builder(this.getContext())
 						.setTitle("选择操作")
 						.setItems(items, new DialogInterface.OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								// TODO Auto-generated method stub
 								if(0 == which){
 									new Thread(new UpLoadThread(bitmap_url.get(currentImgView), currentImgView)).start();
 								}
 								else{
 									bitmap_url.set(currentImgView, null);
 									imgs[currentImgView].setImageResource(R.drawable.d);
 									showDialog();
 									//((BXDecorateImageView)imgs[currentImgView]).setDecorateResource(-1, BXDecorateImageView.ImagePos.ImagePos_LeftTop);
 								}
 								
 							}
 						})
 						.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								// TODO Auto-generated method stub
 								dialog.dismiss();
 							}
 						}).show();
 					}
 					else{
 						//String[] items = {"删除"};
 						new AlertDialog.Builder(this.getContext())
 						.setMessage("删除当前图片?")
 						.setPositiveButton("删除", new DialogInterface.OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								// TODO Auto-generated method stub
 								bitmap_url.set(currentImgView, null);
 								imgs[currentImgView].setImageResource(R.drawable.d);
 //								((BXDecorateImageView)imgs[currentImgView]).setDecorateResource(-1, BXDecorateImageView.ImagePos.ImagePos_LeftTop);
 							}
 						})
 						.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								// TODO Auto-generated method stub
 								dialog.dismiss();
 							}
 						}).show();
 					}
 				}
 			}
 		} else if (v == photoalbum) {
 			// 相册
 			if (ad.isShowing()) {
 				ad.dismiss();
 			}
 			Intent intent3 = new Intent(Intent.ACTION_GET_CONTENT);
 			intent3.addCategory(Intent.CATEGORY_OPENABLE);
 			intent3.setType(IMAGEUNSPECIFIED);
 			baseActivity.startActivityForResult(Intent.createChooser(intent3, "选择图片"),
 					PHOTOZOOM);
 
 		} else if (v == photomake) {
 			// 拍照
 			if (ad.isShowing()) {
 				ad.dismiss();
 			}
 			Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 			intent2.putExtra(MediaStore.EXTRA_OUTPUT,
 					Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp" + this.currentImgView + ".jpg")));
 			baseActivity.startActivityForResult(intent2, PHOTOHRAPH);
 
 		} else if (v == photocancle) {
 			ad.dismiss();
 		}
 	}
 	
 	@Override
 	public boolean onLeftActionPressed(){
 		if(filled()){
 			ConfirmAbortAlert();
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	@Override
 	public boolean onRightActionPressed(){
 		if(uploadCount > 0){
 			Toast.makeText(this.getContext(),"图片正在上传" + "!", 0).show();
 		}
 		// 提交
 		else if (check2()) {
 			//循环发布需提交的数据集合
 			extractInputData();
 			System.out.println("postMap---？"+postMap);
 			pd = ProgressDialog.show(this.getContext(), "提示", "请稍候...");
 			pd.setCancelable(true);
 			new Thread(new UpdateThread()).start();
 		}
 		return true;
 	}
 
 
 	private void extractInputData() {
 		for (int i = 0; i < postList.size(); i++) {
 			String key = (String) postList.keySet().toArray()[i];
 			PostGoodsBean postGoodsBean = postList.get(key);
 			if (postGoodsBean.getControlType().equals("input")
 					|| postGoodsBean.getControlType()
 							.equals("textarea")) {
 				//文本框
 				EditText et = (EditText) btMap.get(i);
 				postMap.put(postGoodsBean.getDisplayName(), et
 						.getText().toString() + postGoodsBean.getUnit());
 			} else if (postGoodsBean.getControlType()
 					.equals("checkbox")) {
 				//多选选择
 				String value = "";
 				@SuppressWarnings("unchecked")
 				List<CheckBox> l = (List<CheckBox>) btMap.get(i);
 				for (int j = 0; j < l.size(); j++) {
 					CheckBox c = l.get(j);
 					if (c.isChecked()) {
 						value = value
 								+ postGoodsBean.getValues().get(j);
 					}
 				}
 				postMap.put(postGoodsBean.getDisplayName(), value);
 			}
 		}
 	}
 	
 	/**
 	 * check whether any content has been filled
 	 * 
 	 * @return
 	 */
 	private boolean filled() {
 		boolean bRet = false;
 		
 		for(int i = 0; i < bitmap_url.size(); ++i){
 			if(bitmap_url.get(i) != null && bitmap_url.get(i).length() > 0 && initialValueMap.get(bitmap_url.get(i)) == null)
 				return true;
 		}
 		
 		extractInputData();
 		
 		for (int i = 0; i < postList.size(); i++) {
 			String key = (String) postList.keySet().toArray()[i];
 			PostGoodsBean postGoodsBean = postList.get(key);
 			
 			String displayName = postGoodsBean.getDisplayName();
 			
 			String value = postMap.get(displayName);
 			String initialValue = initialValueMap.get(displayName)+postGoodsBean.getUnit();
 			
 			if(initialValue == null || !initialValue.equals(value)){
 				bRet = true;
 				break;
 			}
 		}			
 			
 		return bRet;
 	}
 
 	/**
 	 * 检查必填内容是否填写
 	 * 
 	 * @return
 	 */
 	private boolean check2() {
 		for (int i = 0; i < postList.size(); i++) {
 			String key = (String) postList.keySet().toArray()[i];
 			PostGoodsBean postGoodsBean = postList.get(key);
 			if (postGoodsBean.getRequired().endsWith("required")) {
 				if (postGoodsBean.getControlType().equals("select")) {
 					TextView obj = (TextView) btMap.get(i);
 					if (obj.getText().toString().trim().length() == 0
 							|| obj.getText().toString().trim().equals("请选择")) {
 						
 						Toast.makeText(this.getContext(),
 								"请填写" + postGoodsBean.getDisplayName() + "!", 0)
 								.show();
 						return false;
 					}
 				} else if (postGoodsBean.getControlType().equals("input")) {
 					EditText obj = (EditText) btMap.get(i);
 					if (obj.getText().toString().trim().length() == 0) {
 						Toast.makeText(this.getContext(),
 								"请填写" + postGoodsBean.getDisplayName() + "!", 0)
 								.show();
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * 显示拍照相册对话框
 	 */
 	private void showDialog() {
 		View view = LinearLayout.inflate(this.getContext(), R.layout.upload_head, null);
 		Builder builder = new AlertDialog.Builder(this.getContext());
 		builder.setView(view);
 		ad = builder.create();
 
 		WindowManager.LayoutParams lp = ad.getWindow().getAttributes();
 		lp.y = 300;
 		ad.onWindowAttributesChanged(lp);
 		ad.show();
 
 		photoalbum = (Button) view.findViewById(R.id.photo_album);
 		photoalbum.setOnClickListener(this);
 		photomake = (Button) view.findViewById(R.id.photo_make);
 		photomake.setOnClickListener(this);
 		photocancle = (Button) view.findViewById(R.id.photo_cancle);
 		photocancle.setOnClickListener(this);
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
 
 			String city = MyApplication.getApplication().cityName;
 			if(goodsDetail != null){
 				String goodsCity = goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
 				if(null != goodsCity){
 					String[]cities = goodsCity.split(",");
 					if(cities != null && cities.length > 0){
 						city = cities[0];
 					}
 				}
 			}
 			if(editMap != null && PostGoodsView.this.getEditMapValue("地区") != null){
 				Object objArea = PostGoodsView.this.getEditMapValue("地区");
 				String tvText = null;
 				if(objArea instanceof TextView){
 					tvText = ((TextView)objArea).getText().toString();
 				}
 				else if(objArea instanceof EditText){
 					tvText = ((EditText)objArea).getText().toString();
 				}
 				if(tvText != null && !tvText.equals("")){
 					city = city + "," + tvText;
 				}
 			}
 			if(editMap != null && PostGoodsView.this.getEditMapValue("具体地区") != null){
 				Object objArea = PostGoodsView.this.getEditMapValue("具体地区");
 				String tvText = null;
 				if(objArea instanceof TextView){
 					tvText = ((TextView)objArea).getText().toString();
 				}
 				else if(objArea instanceof EditText){
 					tvText = ((EditText)objArea).getText().toString();
 				}
 				if(tvText != null && !tvText.equals("")){
 					city = city + "," + tvText;
 				}
 			}
 			if(!city.equals("")){
 				String googleUrl = String.format("http://maps.google.com/maps/geo?q=%s&output=csv", city);
 				try{
 					String googleJsn = Communication.getDataByUrl(googleUrl);
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
 			list.add("cityEnglishName=" + MyApplication.getApplication().cityEnglishName);
 			list.add("rt=1");
 			//根据goodsDetail判断是发布还是修改发布
 			if (goodsDetail != null) {
 				list.add("adId=" + goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
 			}
 			//发布发布集合
 			for (int i = 0; i < postMap.size(); i++) {
 				String key = (String) postMap.keySet().toArray()[i];
 
 				String values = postMap.get(key);
 				if (values != null && values.length() > 0 && postList.get(key) != null) {
 					list.add(URLEncoder.encode(postList.get(key).getName())
 							+ "=" + URLEncoder.encode(values));
 				}
 			}
 			//发布图片
 			for (int i = 0; i < bitmap_url.size(); i++) {
 					//System.out.println("bitmap_url.keySet().toArray()[i]--?:"+bitmap_url.keySet().toArray()[i]);
 				if(bitmap_url.get(i) != null && bitmap_url.get(i).contains("http:")){
 					list.add("image=" + bitmap_url.get(i));
 				}
 			}
 			// list.add("title=" + "111");
 			// list.add("description=" +
 			// URLEncoder.encode(descriptionEt.getText().toString()));
 			
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url);
 				System.out.println("json---->" + json);
 				if (json != null) {
 					JSONObject jsonObject = new JSONObject(json);
 					JSONObject json = jsonObject.getJSONObject("error");
 					code = json.getInt("code");
 					message = json.getString("message");
 					if (code == 0) {
 						// 发布成功
 						myHandler.sendEmptyMessageDelayed(3, 3000);
 					} else {
 
 						myHandler.sendEmptyMessage(2);
 					}
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
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
 			this.cityEnglishName = (this.cityEnglishName == null ? MyApplication.getApplication().cityEnglishName : this.cityEnglishName);
 			list.add("categoryEnglishName=" + categoryEnglishName);
 			list.add("cityEnglishName=" + this.cityEnglishName);
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url);
 				if (json != null) {
 					// 获取数据成功
 					PostMu postMu = new PostMu();
 					postMu.setJson(json);
 					postMu.setTime(System.currentTimeMillis());
 					//保存模板
 					Helper.saveDataToLocate(PostGoodsView.this.getContext(), categoryEnglishName
 							+ this.cityEnglishName, postMu);
 					if (isUpdate) {
 						myHandler.sendEmptyMessage(1);
 					}
 				} else {
 					// {"error":{"code":0,"message":"\u66f4\u65b0\u4fe1\u606f\u6210\u529f"},"id":"191285466"}
 					myHandler.sendEmptyMessage(2);
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				myHandler.sendEmptyMessage(10);
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	private Uri uri = null;
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		if (resultCode == NONE) {
 			return;
 		}
 		// 拍照
 		if (requestCode == PHOTOHRAPH) {
 			// 设置文件保存路径这里放在跟目录下
 			File picture = new File(Environment.getExternalStorageDirectory(), "temp" + this.currentImgView + ".jpg");
 			uri = Uri.fromFile(picture);
 			getBitmap(uri, PHOTOHRAPH); // 直接返回图片
 			//startPhotoZoom(uri); //截取图片尺寸
 		}
 
 		if (data == null) {
 			return;
 		}
 
 		// 读取相册缩放图片
 		if (requestCode == PHOTOZOOM) {
 			uri = data.getData();
 			//startPhotoZoom(uri);
 			getBitmap(uri, PHOTOZOOM);
 		}
 		// 处理结果
 		if (requestCode == PHOTORESOULT) {
 			File picture = new File("/sdcard/cropped.jpg");
 			
 			uri = Uri.fromFile(picture);
 			getBitmap(uri, PHOTOHRAPH);
 			File file = new File(Environment.getExternalStorageDirectory(), "temp" + this.currentImgView + "jpg");
 			try {
 				if(file.isFile() && file.exists()){
 					file.delete();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 /*
 			Bundle extras = data.getExtras();
 			if (extras != null) {
 				Bitmap tphoto = extras.getParcelable("data");
 				ByteArrayOutputStream stream = new ByteArrayOutputStream();
 				tphoto.compress(Bitmap.CompressFormat.JPEG, 100, stream); // (0 -
 				// 100)压缩文件
 				// saveSDCard(photo);
 				Bitmap photo = Util.newBitmap(tphoto, 480, 480);
 				imgs[this.currentImgView].setImageBitmap(photo);
 				imgs[this.currentImgView].setFocusable(true);				
 				
 				tphoto.recycle();
 //				imgs[bitmap_url.size()].setImageBitmap(photo);
 
 				new Thread(new UpLoadThread(photo)).start();
 
 			}*/
 		}
 
 	}
 	
 	@Override
 	public void onPreviousViewBack(int message, Object obj){		
 		if (message == POST_LIST) {
 			int id = (Integer)obj;
 			TextView tv = tvlist.get(displayname);
 			String txt = postList.get(displayname).getLabels().get(id);
 			String txtValue = postList.get(displayname).getValues().get(id);
 			System.out.println(id+" "+txt+" "+txtValue);
 			postMap.put(displayname, txtValue);
 			tv.setText(txt);
 		}
 	}
 
 	
 	private void getBitmap(Uri uri, int id) {
 		String path = uri == null ? "" : uri.toString();
 //		Bitmap photo = null;
 
 //		path = getRealPathFromURI(uri); // from Gallery
 
 //		if (path == null) {
 //			path = uri.getPath(); // from File Manager
 //		}
 		if (uri != null) {
 //			try {
 //			    BitmapFactory.Options o =  new BitmapFactory.Options();
 //                o.inPurgeable = true;
 //                o.inSampleSize = 2;
 //				Bitmap tphoto = BitmapFactory.decodeFile(path, o);
 				//photo = Util.newBitmap(tphoto, 480, 480);
 				//tphoto.recycle();
 				//imgs[this.currentImgView].setImageBitmap(photo);
 				// imgs[bitmap_url.size()].setPadding(5, 5, 5, 5);
 				// imgs[bitmap_url.size()].setBackgroundResource(R.drawable.btn_camera);
 				imgs[this.currentImgView].setFocusable(true);
 
 				new Thread(new UpLoadThread(path, currentImgView)).start();
 //			} catch (Exception e) {
 				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 		}
 
 	}
 
 	
 	public String getRealPathFromURI(Uri contentUri) {
 		String[] proj = { MediaStore.Images.Media.DATA };
 		Cursor cursor = baseActivity.managedQuery(contentUri, proj, null, null, null);
 
 		if (cursor == null)
 			return null;
 
 		int column_index = cursor
 				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 
 		cursor.moveToFirst();
 
 		return cursor.getString(column_index);
 	}
 
 	public void startPhotoZoom(Uri uri) {
 		Intent intent = new Intent("com.android.camera.action.CROP");
 		intent.setDataAndType(uri, IMAGEUNSPECIFIED);
 		intent.putExtra("crop", "true");
 		// aspectX aspectY 是宽高的比例
 		intent.putExtra("aspectX", 1);
 		intent.putExtra("aspectY", 1);
 		// outputX outputY 是裁剪图片宽高
 		int width = baseActivity.getWindowManager().getDefaultDisplay().getWidth();
 		//intent.putExtra("outputX", width);
 		//intent.putExtra("outputY", width);
 		intent.putExtra("return-data", false);
 		intent.putExtra("output",Uri.fromFile(new File("/sdcard/cropped.jpg")));
 		baseActivity.startActivityForResult(intent, PHOTORESOULT);
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
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	// 管理线程的Handler
 	Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			if (pd.isShowing()) {
 				pd.dismiss();
 			}
 			switch (msg.what) {
 			case 1:
 				//根据模板显示
 				layout_txt.setOrientation(LinearLayout.VERTICAL);
 				postList = JsonUtil.getPostGoodsBean(json); 
 				for (int i = 0; i < postList.size(); i++) {
 					final int position = i;
 					String key = (String) postList.keySet().toArray()[i];
 					PostGoodsBean postBean = postList.get(key);
 					System.out.println("postList--->" + postList);
 					LinearLayout layout = new LinearLayout(PostGoodsView.this.getContext());
 					TextView tvshow = new TextView(PostGoodsView.this.getContext());
 					tvshow.setTextColor(Color.BLACK);
 					TextView tvcontent = new TextView(PostGoodsView.this.getContext());
 					tvcontent.setTextColor(Color.BLACK);
 					ImageView ivforward = new ImageView(PostGoodsView.this.getContext());
 					EditText etcontent = new EditText(PostGoodsView.this.getContext());
 //					etcontent.setTextColor(Color.BLACK);
 
 					img1 = new BXDecorateImageView(PostGoodsView.this.getContext());
 					//((BXDecorateImageView)img1).setDecorateDResource(R.drawable.gou, BXDecorateImageView.ImagePos.ImagePos_RightTop);
 					img2 = new BXDecorateImageView(PostGoodsView.this.getContext());
 					img3 = new BXDecorateImageView(PostGoodsView.this.getContext());
 					imgs = new ImageView[] { img1, img2, img3 };
 					TextView tvlastunit = new TextView(PostGoodsView.this.getContext());
 					tvlastunit.setTextColor(Color.BLACK);
 
 					TextView ttxx = new TextView(PostGoodsView.this.getContext());
 //					ttxx.setTextColor(Color.BLACK);
 
 					if (i == 0) {
 						layout.setBackgroundResource(R.drawable.btn_top_bg);
 					} else if (i == postList.size() - 1) {
 						layout.setBackgroundResource(R.drawable.btn_down_bg);
 					} else {
 						layout.setBackgroundResource(R.drawable.btn_m_bg);
 					}
 
 					layout.setGravity(Gravity.CENTER_VERTICAL);
 					layout.setPadding(10, 10, 10, 10);
 
 					if (postBean.getRequired().equals("required")) {
 						// xx.setImageResource(R.drawable.icon);
 						// layout.addView(xx);
 						ttxx.setGravity(Gravity.TOP);
 						ttxx.setTextColor(Color.RED);
 						ttxx.setText("*");
 						layout.addView(ttxx);
 					}
 
 					if (postBean.getControlType().equals("input")) {
 						// 输入框
 						tvshow.setText(postBean.getDisplayName());
 						tvshow.setTextSize(18);
 						layout.addView(tvshow);
 						layout.setOrientation(LinearLayout.HORIZONTAL);
 						
 						etcontent.setTextSize(16);
 						etcontent.setTextColor(0xff595959);
 						etcontent.setLayoutParams(new LayoutParams(
 								LayoutParams.FILL_PARENT,
 								LayoutParams.WRAP_CONTENT, 1));
 						
 
 						etcontent.setBackgroundDrawable(null);						
 						etcontent.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);	
 						
 						if(postBean.getName().equals("contact")){
 							etcontent.setText(user.getPhone());
 							postMap.put(postBean.getDisplayName(), user.getPhone());
 						}
 						else
 						{
 							etcontent.setText("");
 						}						
 						etcontent.setHint("请输入");
 						etcontent.setHintTextColor(0xff595959);
 
 
 						// textViewMap.put(position, etcontent);
 						btMap.put(position, etcontent);
 						// 联系方式已添加
 						editMap.put(postBean.getDisplayName() + " " + postBean.getName(), etcontent);
 
 						layout.addView(etcontent);
 						
 						if (!postBean.getUnit().equals("")) {
 							tvlastunit.setTextSize(18);
 							tvlastunit.setLayoutParams(new LayoutParams(
 									LayoutParams.WRAP_CONTENT,
 									LayoutParams.WRAP_CONTENT));
 							tvlastunit.setText(postBean.getUnit());
 						}
 						
 						layout.addView(tvlastunit);
 					} else if (postBean.getControlType().equals("select")) {
 						tvshow.setText(postBean.getDisplayName());
 						tvshow.setTextSize(18);
 						tvcontent.setLayoutParams(new LayoutParams(
 								LayoutParams.FILL_PARENT,
 								LayoutParams.WRAP_CONTENT, 1));
 						tvcontent.setTextSize(16);
 						tvcontent.setTextColor(0xff595959);
 						LayoutParams lp = new LayoutParams(
 								LayoutParams.FILL_PARENT,
 								LayoutParams.WRAP_CONTENT, 1);
 						lp.setMargins(0, 0, 20, 0);
 						tvcontent.setLayoutParams(lp);
 						tvcontent.setGravity(Gravity.RIGHT);
 						tvcontent.setText("请选择");
 						btMap.put(position, tvcontent);
 						editMap.put(postBean.getDisplayName() + " " + postBean.getName(), tvcontent);
 						ivforward.setLayoutParams(new LayoutParams(
 								LayoutParams.WRAP_CONTENT,
 								LayoutParams.WRAP_CONTENT));
 						ivforward.setImageResource(R.drawable.arrow);
 
 						layout.addView(tvshow);
 						layout.addView(tvcontent);
 						layout.addView(ivforward);
 
 					} else if (postBean.getControlType().equals("tableSelect")) {
 						tvshow.setText(postBean.getDisplayName());
 						tvshow.setTextSize(18);
 						tvcontent.setLayoutParams(new LayoutParams(
 								LayoutParams.FILL_PARENT,
 								LayoutParams.WRAP_CONTENT, 1));
 						tvcontent.setTextSize(16);
 						tvcontent.setTextColor(0xff595959);
 						LayoutParams lp = new LayoutParams(
 								LayoutParams.FILL_PARENT,
 								LayoutParams.WRAP_CONTENT, 1);
 						lp.setMargins(0, 0, 20, 0);
 						tvcontent.setLayoutParams(lp);
 						tvcontent.setGravity(Gravity.RIGHT);
 						tvcontent.setText("请选择");
 						btMap.put(position, tvcontent);
 						editMap.put(postBean.getDisplayName() + " " + postBean.getName(), tvcontent);
 						ivforward.setLayoutParams(new LayoutParams(
 								LayoutParams.WRAP_CONTENT,
 								LayoutParams.WRAP_CONTENT));
 						ivforward.setImageResource(R.drawable.arrow);
 
 						layout.addView(tvshow);
 						layout.addView(tvcontent);
 						layout.addView(ivforward);
 
 					} else if (postBean.getControlType().equals("checkbox")) {
 						layout.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
 						tvshow.setText(postBean.getDisplayName());
 						//try not to display left side label if it 's same with label of checkbox
 						List<String> rightLabels = postBean.getLabels();
 						if(rightLabels.size() > 0){
 							if(((String)(rightLabels.get(0))).equals(postBean.getDisplayName())){
 								tvshow.setText("");
 							}
 						}
 						
 						tvshow.setTextSize(18);
 						tvshow.setPadding(3, 0, 3, 0);
 						tvshow.setWidth(100);
 						layout.addView(tvshow);
 						LinearLayout checkbox_layout = new LinearLayout(
 								PostGoodsView.this.getContext());
 						
 						checkbox_layout.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
 						checkbox_layout.setLayoutParams(new LayoutParams(
 								LayoutParams.WRAP_CONTENT,
 								LayoutParams.WRAP_CONTENT));
 						checkbox_layout.setOrientation(LinearLayout.VERTICAL);
 						checkbox_layout.setPadding(0, 0, 15, 0);
 						List<String> boxes = postBean.getLabels();
 						List<CheckBox> boxeslist = new ArrayList<CheckBox>();
 						for (int j = 0; j < boxes.size(); j++) {
 							String ss = boxes.get(j);
 							CheckBox checkbox = new CheckBox(PostGoodsView.this.getContext());
 							checkbox.setLayoutParams(new LayoutParams(
 									LayoutParams.WRAP_CONTENT,
 									LayoutParams.WRAP_CONTENT));
 							checkbox.setTag(postBean.getLabels().get(j));
 							checkbox.setTextSize(16);
 							checkbox.setText(ss);
 							boxeslist.add(checkbox);
 							checkbox_layout.addView(checkbox);
 						}
 						layout.addView(checkbox_layout);
 						// checkBoxMap.put(position, boxeslist);
 						btMap.put(position, boxeslist);
 						editMap.put(postBean.getDisplayName() + " " + postBean.getName(), boxeslist);
 					} else if (postBean.getControlType().equals("textarea")) {
 						descriptionEt = new EditText(PostGoodsView.this.getContext());
 						descriptionEt.setTextSize(16);
 						descriptionEt.setTextColor(0xff595959);
 						tvshow.setText(postBean.getDisplayName());
 						tvshow.setTextSize(18);
 						descriptionEt.setHint("请输入");
 						descriptionEt.setHintTextColor(0xff595959);
 						descriptionEt.setGravity(Gravity.TOP);
 						descriptionEt.setLines(5);
 						descriptionEt.setText(MyApplication.getApplication().getPersonMark());
 						// textViewMap.put(position, descriptionEt);
 						btMap.put(position, descriptionEt);
 						editMap.put(postBean.getDisplayName() + " " + postBean.getName(), descriptionEt);
 						layout.setOrientation(LinearLayout.VERTICAL);
 						layout.addView(tvshow);
 						layout.addView(descriptionEt);
 					} else if (postBean.getControlType().equals("image")) {
 					    
 					    int height = baseActivity.getWindowManager().getDefaultDisplay().getHeight();
 			            int fixHotHeight = height * 15 / 100;
 			            if(fixHotHeight < 50)
 			            {
 			                fixHotHeight = 50;
 			            }
 			            //fixHotHeight = layout.getHeight() - 5 * 2;
                         img1.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                         img1.setAdjustViewBounds(true);                       
                         img1.setMaxHeight(fixHotHeight);
                         img1.setMaxWidth(fixHotHeight);
                         LinearLayout l1 = new LinearLayout(PostGoodsView.this.getContext());
 			            l1.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
 			            l1.addView(img1);
                         
                         img2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                         img2.setAdjustViewBounds(true);
                         img2.setMaxHeight(fixHotHeight);
                         img2.setMaxWidth(fixHotHeight);
                         LinearLayout l2 = new LinearLayout(PostGoodsView.this.getContext());
 			            l2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
 			            l2.addView(img2);
                         
                         img3.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
                         img3.setAdjustViewBounds(true);
                         img3.setMaxHeight(fixHotHeight);
                         img3.setMaxWidth(fixHotHeight);
                         LinearLayout l3 = new LinearLayout(PostGoodsView.this.getContext());
 			            l3.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			            l3.addView(img3);
                         
 						img1.setImageResource(R.drawable.d);
 						img2.setImageResource(R.drawable.d);
 						img3.setImageResource(R.drawable.d);
 						img1.setOnClickListener(PostGoodsView.this);
 						img2.setOnClickListener(PostGoodsView.this);
 						img3.setOnClickListener(PostGoodsView.this);
 						layout.addView(l1);
 						layout.addView(l2);
 						layout.addView(l3);
 						
 						btMap.put(position, imgs);
 						editMap.put(postBean.getDisplayName() + " " + postBean.getName(), imgs);
 					}
 
 					if(postMap.get(postBean.getDisplayName()) == null)
 						postMap.put(postBean.getDisplayName(), "");
 					
 					tvlist.put(postBean.getDisplayName(), tvcontent);
 
 					layout.setTag(postBean);
 					layout.setOnClickListener(new OnClickListener() {
 						public void onClick(View v) {
 							PostGoodsBean postBean = (PostGoodsBean) v.getTag();
 							if (postBean.getControlType().equals("select") || postBean.getControlType().equals("tableSelect")) {
 								displayname = postBean.getDisplayName();
 								if(m_viewInfoListener != null){
 									m_viewInfoListener.onNewView(new PostGoodsSelectionView(baseActivity, bundle, postBean, POST_LIST));
 								}
 							}
 						}
 					});
 					layout_txt.addView(layout);
 				}
 				editpostUI();
 				break;
 
 			case 2:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						PostGoodsView.this.getContext());
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
 					Toast.makeText(PostGoodsView.this.getContext(), message, 0).show();
 					if (!id.equals("")) {
 						// 发布成功
 						// Toast.makeText(PostGoods.this, "未显示，请手动刷新",
 						// 3).show();
 						if(m_viewInfoListener != null){
 							m_viewInfoListener.onSwitchToTab(BaseView.ETAB_TYPE.ETAB_TYPE_MINE);
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
 				if (pd != null) {
 					pd.dismiss();
 				}
 				Toast.makeText(PostGoodsView.this.getContext(), "网络连接失败，请检查设置！", 3).show();
 				break;
 			}
 			super.handleMessage(msg);
 		}
 	};
 
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
 
 			baseActivity.runOnUiThread(new Runnable(){
 				public void run(){
 					//((BXDecorateImageView)imgs[PostGoods.this.currentImgView]).setDecorateResource(R.drawable.alert_orange, BXDecorateImageView.ImagePos.ImagePos_Center);
 					imgs[currentIndex].setImageResource(R.drawable.u);
 					imgs[currentIndex].setClickable(false);
 					imgs[currentIndex].invalidate();
 				}
 			});	
 			synchronized(PostGoodsView.this){
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
 			thumbnailBmp = PostGoodsView.createThumbnail(currentBmp, imgs[currentIndex].getHeight());
 			currentBmp.recycle();
 			currentBmp = null;
 	
 			if (result != null) {
 				bitmap_url.set(currentIndex, result);
 
                 baseActivity.runOnUiThread(new Runnable(){
 					public void run(){
 						imgs[currentIndex].setImageBitmap(thumbnailBmp);
 						imgs[currentIndex].setClickable(true);
 						imgs[currentIndex].invalidate();
 						Toast.makeText(PostGoodsView.this.getContext(), "上传图片成功", 5).show();
 					}
 				});	                
 			} else {
 //				PostGoods.BXImageAndUrl imgAn dUrl = new PostGoods.BXImageAndUrl();
 				baseActivity.runOnUiThread(new Runnable(){
 					public void run(){
 						imgs[currentIndex].setImageResource(R.drawable.f);
 						imgs[currentIndex].setClickable(true);
 						bitmap_url.set(currentIndex, bmpPath);
 						//((BXDecorateImageView)imgs[PostGoods.this.currentImgView]).setDecorateResource(R.drawable.alert_red, BXDecorateImageView.ImagePos.ImagePos_RightTop);
 						imgs[currentIndex].invalidate();
 						Toast.makeText(PostGoodsView.this.getContext(), "上传图片失败", 5).show();
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
 	// 开启线程下载图片
 	class Imagethread implements Runnable {
 		private String url;
 		private String urlBig;
 		private Bitmap bitmap = null;
 		private int currentIndex = -1;
 		public Imagethread(String url, String urlBig){
 			this.url = url;
 			this.urlBig = urlBig;
 		}
 		@Override
 		public void run() {
 			try {
 				Bitmap tbitmap = Util.getImage(url);
 				
 				//Bitmap bitmap = Util.newBitmap(tbitmap, 480, 480);
 				for(int i = 0; i < PostGoodsView.this.bitmap_url.size(); ++ i){
 					if(null != PostGoodsView.this.bitmap_url.get(i) 
 							&& null != PostGoodsView.this.bitmap_url.get(i)
 							&& PostGoodsView.this.bitmap_url.get(i).contains("http:")){
 						continue;						
 					}
 					PostGoodsView.this.bitmap_url.set(i, urlBig);
 					currentIndex = i;
 					this.bitmap = tbitmap;
 	                baseActivity.runOnUiThread(new Runnable(){
 						public void run(){
 							if(currentIndex >= 0 && bitmap != null){
 								PostGoodsView.this.imgs[currentIndex].setImageBitmap(bitmap);
 							}
 						}
 					});
 					//PostGoods.this.imgs[i].setImageBitmap(tbitmap);
 					break;					
 				}
 				//tbitmap.recycle();
 				//new Thread(new UpLoadThread(bitmap)).start();
 				
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_title = "发布";
 		title.m_leftActionHint = "选择类目";
 		title.m_rightActionHint = "立即发布";
 		return title;
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = false;
 //		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
 		return tab;
 	}
 
 }
