 package com.quanleimu.view;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.quanleimu.activity.BaseActivity;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.PostGoodsBean;
 import com.quanleimu.entity.UserProfile;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ParameterHolder;
 import com.quanleimu.util.UploadImageCommand;
 import com.quanleimu.util.UploadImageCommand.ProgressListener;
 import com.quanleimu.util.ViewUtil;
 import com.quanleimu.view.MultiLevelSelectionView.MultiLevelItem;
 import com.quanleimu.widget.GenderPopupDialog;
 
 public class ProfileEditView extends BaseView {
 
 	final int BACK_EVENT_ID = 100;
 	
 	public static final int NONE = 0;
 	public static final int PHOTOHRAPH = 1;
 	public static final int PHOTOZOOM = 2; 
 	public static final int PHOTORESOULT = 3;
 	
 	final int MSG_UPDATE_SUCCED = 1;
 	final int MSG_UPDATE_FAIL = 2;
 	final int MSG_UPDATE_ERROR = 3;
 	final int MSG_GOT_CITY_LIST = 4;
 	final int MSG_NEW_IMAGE = 5;
 	final int MSG_UPLOAD_IMG_FAIL = 6;
 	final int MSG_UPLOAD_IMG_DONE = 7;
 			
 	
 	private UserProfile up;
 	private LinkedHashMap<String, PostGoodsBean> beans;
 	private String newCityId;
 	private Uri profileUri;
 	private String newServerImage;
 	private Bundle bundle = null;
 	
 	public ProfileEditView(Context context, Bundle bundle, UserProfile up){
 		super(context);
 		this.up = up;
 		this.bundle = bundle;
 		init();
 	}
 	
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_title = "编辑个人信息";
 		title.m_rightActionHint = "完成";
 		title.m_leftActionHint = "返回";
 		
 		return title;
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tabDef = new TabDef();
 		tabDef.m_visible = true;
 		
 		return tabDef;
 	}	
 	
 	protected void init(){
 		LayoutInflater inflator = LayoutInflater.from(this.getContext());
 		View v = inflator.inflate(R.layout.edit_profile, null);
 		addView(v);
 		
 		newCityId = up.location;
 		updateText(up.nickName, R.id.username);
 		updateText(up.gender, R.id.gender);
 		findViewById(R.id.city).setOnClickListener(
 				new OnClickListener() {
 
 					public void onClick(View v) {
 						MultiLevelSelectionView ml = 
 								new MultiLevelSelectionView((BaseActivity)getContext(), 
 										"china", 
 										"选择常居地", 
 										BACK_EVENT_ID,
 										3); 
 						
 						m_viewInfoListener.onNewView(ml);
 					}
 
 				});
 
 		
 		findViewById(R.id.gender).setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				GenderPopupDialog dlg = new GenderPopupDialog(ProfileEditView.this.getContext(), 
 						((TextView)findViewById(R.id.gender)).getText().equals("男"));
 				dlg.show();
 				dlg.setOnDismissListener(new DialogInterface.OnDismissListener(){
 
 					@Override
 					public void onDismiss(DialogInterface dialog) {
 						// TODO Auto-generated method stub
 						if(dialog != null){
 							((TextView)findViewById(R.id.gender)).setText(((GenderPopupDialog)dialog).isBoy() ? "男" : "女");
 						}
 					}
 					
 				});
 //				findViewById(R.id.gender_selector).setVisibility(View.VISIBLE);
 			}
 		});
 		
 //		Spinner selector = (Spinner) findViewById(R.id.gender_selector);
 //		selector.setVisibility(View.GONE);
 //		selector.setBackgroundResource(R.drawable.bk_box);
 //		List<String> genders = new ArrayList<String>();
 //		genders.add("男");
 //		genders.add("女");
 //		selector.setAdapter(new GenderSpinnerAdapter(this.getContext(), genders));
 ////		if (up.gender!=null && up.gender.length()>0)
 ////		{
 ////			selector.setSelection("男".equals(up.gender)?0 : 1);
 ////		}
 //		selector.setOnItemSelectedListener(new OnItemSelectedListener(){
 //
 //			@Override
 //			public void onItemSelected(AdapterView<?> arg0, View arg1,
 //					int arg2, long arg3) {
 //				updateText(arg2 == 0? "男" : "女", R.id.gender);
 //			}
 //
 //			@Override
 //			public void onNothingSelected(AdapterView<?> arg0) {
 //				
 //			}});
 		
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
 		updateText(format.format(new Date(Long.parseLong(up.createTime) * 1000)), R.id.create_time);
 		
 		if (newCityId != null && newCityId.length() > 0)
 		{
 			loadCityMapping(newCityId);
 		}
 		
 		findViewById(R.id.rl_image).setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				ViewUtil.pickupPhoto(getContext(), 0);
 			}
 			
 		});
 		
 		if(up.resize180Image != null && !up.resize180Image.equals("") && !up.resize180Image.equals("null")){
 			updateImage(up.resize180Image);
 		}else{
 			if(up.gender != null && up.gender.equals("女")){
 				((ImageView)this.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_girl);
 			}else{
 				((ImageView)this.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_boy);
 			}
 		}
 		
 	}
 	
 	private void updateText(String text, int resId)
 	{
 		TextView tx = (TextView) findViewById(resId);
 		tx.setText(text);
 	}
 	
 	private String getTextData(int resId)
 	{
 		TextView tx = (TextView) findViewById(resId);
 		return tx.getText().toString();
 	}
 
 	@Override
 	public void onPreviousViewBack(int message, Object obj) {
 		if (message == BACK_EVENT_ID)
 		{
 			if (obj instanceof MultiLevelItem)
 			{
 				MultiLevelItem item = (MultiLevelItem) obj;
 				updateText(item.txt, R.id.city);//
 				newCityId = item.id;
 			}
 		}
 	}
 	
 	private Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			if (pd != null && pd.isShowing()) {
 				pd.dismiss();
 			}
 			
 			switch(msg.what)
 			{
 			case MSG_UPDATE_ERROR:
 				ViewUtil.postShortToastMessage(ProfileEditView.this, "更新失败，请检查网络后重试", 10);
 				break;
 			case MSG_UPDATE_SUCCED:
 				ViewUtil.postShortToastMessage(ProfileEditView.this, "更新成功", 10);
 				saveAndexit();
 				break;
 			case MSG_UPDATE_FAIL:
 				ViewUtil.postShortToastMessage(ProfileEditView.this, (String)msg.obj, 10);
 				break;
 			case MSG_GOT_CITY_LIST:
 				beans = (LinkedHashMap) msg.obj;
 				updateText(findCityName(up.location), R.id.city);
 				break;
 			case MSG_NEW_IMAGE:
 				profileUri = (Uri) msg.obj;
 //				Bitmap bp = BitmapFactory.decodeFile(profileUri.toString());
 //				((ImageView) findViewById(R.id.personalImage)).setImageBitmap(bp);
 				updateImageView(profileUri.toString());
 				break;
 			case MSG_UPLOAD_IMG_DONE:
 				newServerImage = (String) msg.obj;
 				continueUpdateProfile();
 				break;
 			case MSG_UPLOAD_IMG_FAIL:
 				ViewUtil.postShortToastMessage(ProfileEditView.this, "上传图片失败", 10);
 				break;
 			}
 		}
 	};
 	
 	private String findCityName(String metaId)
 	{
 		PostGoodsBean bean = beans.get((String)beans.keySet().toArray()[0]);
 		return bean.getDisplayName();
 		
 	}
 	
 	public boolean onRightActionPressed()
 	{
 		if (validate())
 		{
 			if (profileUri != null)
 			{
 				pd = ProgressDialog.show(getContext(), "提示", "图片上传中，请稍等。。。");
 				new UploadImageCommand(getContext(), profileUri.toString()).startUpload(new ProgressListener() {
 					public void onStart(String imagePath) {
 						//Do nothing.
 					}
 
 					public void onCancel(String imagePath) {
 						myHandler.sendEmptyMessage(MSG_UPLOAD_IMG_FAIL);
 					}
 
 					public void onFinish(Bitmap bmp, String imagePath) {
 						Message msg = myHandler.obtainMessage(MSG_UPLOAD_IMG_DONE, imagePath);
 						myHandler.sendMessage(msg);
 					}
 					
 				});
 			}
 			else
 			{
 				continueUpdateProfile();
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private void continueUpdateProfile()
 	{
 		pd = ProgressDialog.show(getContext(),"提示", "更新中，请稍等...");
 		
 		updateProfile();
 	}
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		if (resultCode == NONE) {
 			return;
 		}
 		
 		Uri uri = null;
 		if (requestCode == PHOTOHRAPH) {
 			File picture = new File(Environment.getExternalStorageDirectory(), "temp" + 0 + ".jpg");
 			uri = Uri.fromFile(picture);
 		}
 		else if (data != null && requestCode == PHOTOZOOM)
 		{
 			uri = data.getData();
 		}
 		
 		Log.e("IMG", "img url : " + uri);
 		if (uri != null)
 		{
 			Message msg = myHandler.obtainMessage(MSG_NEW_IMAGE, uri);
 			myHandler.sendMessage(msg);
 		}
 	}
 	
 	
 	private boolean validate()
 	{
 		String nick = getTextData(R.id.username);
 		if (!nick.equals(up.nickName))
 		{
 			if (nick.trim().length() == 0)
 			{
 				ViewUtil.postShortToastMessage(ProfileEditView.this,R.string.nickname_empty, 10);
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	private void updateProfile() {
 		ParameterHolder params = new ParameterHolder();
 		params.addParameter("nickname", getTextData(R.id.username));
 		params.addParameter("gender", getTextData(R.id.gender));
 		params.addParameter("所在地", newCityId);
 		params.addParameter("userId", up.userId);
 		if (profileUri != null)
 		{
 			params.addParameter("image_i", newServerImage);
 		}
 		
 		
 		Communication.executeAsyncGetTask("user_profile_update", params, new Communication.CommandListener() {
 			
 			@Override
 			public void onServerResponse(String serverMessage) {
 				try {
 					JSONObject obj = new JSONObject(serverMessage).getJSONObject("error");
 					if (!"0".equals(obj.getString("code")))
 					{
 						Message msg = myHandler.obtainMessage(MSG_UPDATE_FAIL, obj.get("message"));
 						myHandler.sendMessage(msg);
 					}
 					else
 					{
 						myHandler.sendEmptyMessage(MSG_UPDATE_SUCCED);
 					}
 				} catch (JSONException e) {
 					myHandler.sendEmptyMessage(MSG_UPDATE_ERROR);
 				}
 				
 			}
 			
 			@Override
 			public void onException(Exception ex) {
 				myHandler.sendEmptyMessage(MSG_UPDATE_ERROR);
 			}
 		});
 	}
 	
 	private void saveAndexit()
 	{
 		up.nickName = getTextData(R.id.username);
 		up.gender = getTextData(R.id.gender);
 		up.location = newCityId;
 		//TODO: how about the image url?
 		
 		if(null != m_viewInfoListener){
 			if(bundle != null){
 				bundle.putInt("forceUpdate", 1);
 			}
 			m_viewInfoListener.onBack();
 		}
 	
 	}
 	
 	private void loadCityMapping(String cityId)
 	{
 		ParameterHolder params = new ParameterHolder();
 		params.addParameter("objIds", cityId);
 		
 		Communication.executeAsyncGetTask("metaobject", params, new Communication.CommandListener() {
 			
 			public void onServerResponse(String serverMessage) {
 				Message msg = myHandler.obtainMessage(MSG_GOT_CITY_LIST, JsonUtil.getPostGoodsBean(serverMessage));
 				myHandler.sendMessage(msg);
 			}
 			
 			public void onException(Exception ex) {
 				//Ignor
 			}
 		});
 	}
 	
 	
 	private void updateImage(String imageUrl)
 	{
 		if(imageUrl != null){
 			SimpleImageLoader.showImg((ImageView)this.findViewById(R.id.personalImage), imageUrl, null, this.getContext());
 		}
 	}
 	
 	public void updateImageView(String imgPath)
 	{
 		Uri uri = Uri.parse(imgPath);
 		String path = getRealPathFromURI(uri); // from Gallery
 		if (path == null) {
 			path = uri.getPath(); // from File Manager
 		}
 		
 		if (path != null) {
 			try {
 			    
 			    BitmapFactory.Options bfo = new BitmapFactory.Options();
 		        bfo.inJustDecodeBounds = true;
 		        BitmapFactory.decodeFile(path, bfo);
 		        
 			    BitmapFactory.Options o =  new BitmapFactory.Options();
                 o.inPurgeable = true;
                 int maxDim = 600;
                 
                 o.inSampleSize = getClosestResampleSize(bfo.outWidth, bfo.outHeight, maxDim);
                 
                 Bitmap bp = BitmapFactory.decodeFile(path, o);
                 ((ImageView) findViewById(R.id.personalImage)).setImageBitmap(bp);
 			} catch (Exception e) {
 				e.printStackTrace();
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
 	
 	public String getRealPathFromURI(Uri contentUri) {
 		String[] proj = { MediaStore.Images.Media.DATA };
 		Cursor cursor = ((Activity)getContext()).managedQuery(contentUri, proj, null, null, null);
 
 		if (cursor == null)
 			return null;
 
 		int column_index = cursor
 				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 
 		cursor.moveToFirst();
 
 		String ret = cursor.getString(column_index);
 //		cursor.close();
 		return ret;
 	}
 	
 }
