 package com.dylan.orzeye;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.view.Window;
 
 public class SplashActivity extends Activity {
 	public static final String APP_PATH = Environment
 			.getExternalStorageDirectory().toString() + "/OrzEye";
 	public static final String OCR_DATA_PATH = APP_PATH + "/tessdata/";;
 	public static final String DIC_DATA_PATH = APP_PATH + "/dictionary/";
 
 	private final String DIC_DATA_FILENAME = "dictionary.db";
 	private final String OCR_DATA_FILENAME = "eng.traineddata";
 
 	private final int SPLASH_DISPLAY_LENGHT = 3000;
 	private boolean isOCRDataExisted = false;
 	private boolean isDictionaryDataExisted = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_splash);
 
 		if (isDataFileExisted()) {
 			new Handler().postDelayed(new Runnable() {
 
 				@Override
 				public void run() {
 					Intent mainIntent = new Intent(SplashActivity.this,
 							CameraActivity.class);
 					SplashActivity.this.startActivity(mainIntent);
 					SplashActivity.this.finish();
 				}
 			}, SPLASH_DISPLAY_LENGHT);
 		} else {
 			final ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle("安装数据...");
			dialog.setMessage("请稍候...");
 			dialog.show();
 			final Handler handler = new Handler() {
 				public void handleMessage(android.os.Message msg) {
 					dialog.cancel();
 				}
 			};
 
 			Thread thread = new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					installDataFile();
 					handler.sendEmptyMessage(0);
 					Intent mainIntent = new Intent(SplashActivity.this,
 							CameraActivity.class);
 					SplashActivity.this.startActivity(mainIntent);
 					SplashActivity.this.finish();
 				}
 			});
 			thread.start();
 		}
 	}
 
 	private void installDataFile() {
 		if (!(new File(APP_PATH)).exists()) {
 			(new File(APP_PATH)).mkdir();
 		}
 
 		if (!isOCRDataExisted) {
 			copyDataFileToAppFolder(OCR_DATA_PATH, OCR_DATA_FILENAME, R.raw.eng);
 		}
 
 		if (!isDictionaryDataExisted) {
 			copyDataFileToAppFolder(DIC_DATA_PATH, DIC_DATA_FILENAME,
 					R.raw.dictionary);
 		}
 	}
 
 	private boolean isDataFileExisted() {
 		if ((new File(OCR_DATA_PATH + OCR_DATA_FILENAME)).exists()) {
 			isOCRDataExisted = true;
 		}
 
 		if ((new File(DIC_DATA_PATH + DIC_DATA_FILENAME)).exists()) {
 			isDictionaryDataExisted = true;
 		}
 
 		return isOCRDataExisted && isDictionaryDataExisted;
 	}
 
 	private void copyDataFileToAppFolder(String filePath, String fileName,
 			int source) {
 		if (!(new File(filePath)).exists()) {
 			(new File(filePath)).mkdir();
 		}
 
 		try {
 			InputStream is = getResources().openRawResource(source);
 			FileOutputStream fos;
 			fos = new FileOutputStream(filePath + fileName);
 			byte[] buffer = new byte[8192];
 			int count = 0;
 			while ((count = is.read(buffer)) > 0) {
 				fos.write(buffer, 0, count);
 			}
 			fos.close();
 			is.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void onBackPressed() {
 		// do nothing
 	}
 
 }
