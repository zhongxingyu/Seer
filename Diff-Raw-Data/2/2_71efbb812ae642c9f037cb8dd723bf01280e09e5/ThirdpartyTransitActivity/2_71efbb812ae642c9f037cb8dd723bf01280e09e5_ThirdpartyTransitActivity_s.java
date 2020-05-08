 package com.quanleimu.activity;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 
 import com.baixing.broadcast.CommonIntentAction;
 
 public class ThirdpartyTransitActivity extends Activity{
 	static public final String ThirdpartyKey = "thirdparty";
 //	static public final String ThirdpartyType_Albam = "albam";
 //	static public final String ThirdpartyType_Photo = "photo";
 //	static public final String Name_PhotoNumber = "currentview";
 	public static final String IMAGEUNSPECIFIED = "image/*";
 //	public static final String isFromPhotoOrAlbam = "isFromPhotoOrAlbam";
 	public static final String Key_RequestCode = "requestCode";
 	public static final String Key_RequestResult = "requestResult";
 	public static final String Key_Data = "data";
 //	private static final int PHOTOZOOM = 2;
 //	private static final int PHOTOHRAPH = 1;
 //	private static final int NONE = 0;
 //	private int photoNameNumber = -1; 
 	private boolean isCreate = false;
 	enum ETHIRDPARTYTYPE{
 		ETHIRDPARTYTYPE_ALBAM,
 		ETHIRDPARTYTYPE_PHOTO
 	}
 	private ETHIRDPARTYTYPE tptype = null;
 	@Override
 	public void onCreate(Bundle bundle){
 		Log.e("QLM", "third party create");
 		this.getWindow().setBackgroundDrawable(null);
 		this.isCreate = bundle == null;
 		super.onCreate(bundle);
 	}
 	
 	
 	
 	@Override
 	protected void onNewIntent(Intent intent) {
 		Log.e("QLM", "third party onNewIntent()");
 		super.onNewIntent(intent);
 	}
 
 
 
 	@Override
 	protected void onResume() {
 		Log.e("QLM", "third party onResume()");
 		super.onResume();
 		if (isCreate)
 		{
 			Intent intent = this.getIntent();
 			Bundle extBundle = intent.getExtras();
 			
 			Intent goIntent = null; 
 			if (CommonIntentAction.ACTION_IMAGE_CAPTURE.equals(intent.getAction()))
 			{
 				goIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 				if (intent.hasExtra(CommonIntentAction.EXTRA_IMAGE_SAEV_PATH))
 				{
					goIntent.putExtra(MediaStore.ACTION_IMAGE_CAPTURE, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), intent.getStringExtra(CommonIntentAction.EXTRA_IMAGE_SAEV_PATH))));
 				}
 			}
 			else if (CommonIntentAction.ACTION_IMAGE_SELECT.equals(intent.getAction()))
 			{
 				goIntent = new Intent(Intent.ACTION_GET_CONTENT);
 				goIntent.addCategory(Intent.CATEGORY_OPENABLE);
 				goIntent.setType(IMAGEUNSPECIFIED);
 				goIntent = Intent.createChooser(goIntent, "选择图片");
 			}
 			
 			if (goIntent != null)
 			{
 				startActivityForResult(goIntent, intent.getIntExtra(CommonIntentAction.EXTRA_COMMON_REQUST_CODE, -1));
 			}
 			
 //			if(extBundle.containsKey(ThirdpartyKey)){
 //				String key = extBundle.getString(ThirdpartyKey);
 //				if(key != null){
 //					if(key.equals(ThirdpartyType_Albam)){
 //						tptype = ETHIRDPARTYTYPE.ETHIRDPARTYTYPE_ALBAM;
 //						startThirdparty(0);
 //					}
 //					else if(key.equals(ThirdpartyType_Photo)){
 //						tptype = ETHIRDPARTYTYPE.ETHIRDPARTYTYPE_PHOTO;
 //						photoNameNumber = extBundle.getInt(Name_PhotoNumber);
 //						startThirdparty(photoNameNumber);
 //					}
 //				}
 //			}
 			isCreate = false;
 		}
 
 	}
 
 
 
 //	private void startThirdparty(int tmpName){
 //		if (tptype == ETHIRDPARTYTYPE.ETHIRDPARTYTYPE_ALBAM) {
 //			Intent intent3 = new Intent(Intent.ACTION_GET_CONTENT);
 //			intent3.addCategory(Intent.CATEGORY_OPENABLE);
 //			intent3.setType(IMAGEUNSPECIFIED);
 //			startActivityForResult(Intent.createChooser(intent3, "选择图片"), PHOTOZOOM);
 //
 //		} else if (ETHIRDPARTYTYPE.ETHIRDPARTYTYPE_PHOTO == tptype) {
 //			Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 //			intent2.putExtra(MediaStore.EXTRA_OUTPUT,
 //					Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp" + tmpName + ".jpg")));
 //			startActivityForResult(intent2, PHOTOHRAPH);
 //		} 
 //		
 //	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Intent backIntent = (Intent) getIntent().getExtras().get(CommonIntentAction.EXTRA_COMMON_INTENT);//new Intent(this, QuanleimuMainActivity.class);
 		Bundle bundle = new Bundle();
 		bundle.putBoolean(CommonIntentAction.EXTRA_COMMON_IS_THIRD_PARTY, true);
 		bundle.putInt(CommonIntentAction.EXTRA_COMMON_REQUST_CODE, requestCode);
 		bundle.putInt(CommonIntentAction.EXTRA_COMMON_RESULT_CODE, resultCode);
 		bundle.putParcelable(CommonIntentAction.EXTRA_COMMON_DATA, data);
 		backIntent.putExtras(bundle);
 //		backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		this.startActivity(backIntent);
 		this.finish();
 	}
 }
