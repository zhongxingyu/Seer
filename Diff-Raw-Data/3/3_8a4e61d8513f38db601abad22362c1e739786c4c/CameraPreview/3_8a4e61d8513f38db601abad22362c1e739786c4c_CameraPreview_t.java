 package org.g_okuyama.capture;
 
 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.Size;
 
 import android.os.Environment;
 import android.os.Handler;
 import android.provider.MediaStore.Images;
 import android.util.Log;
 import android.view.SurfaceHolder;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 
 class CameraPreview implements SurfaceHolder.Callback {
     public static final String TAG = "ContShooting";
     Camera mCamera = null;
     Context mContext = null;
 
     AutoFocusCallback mFocus = null;
     //private boolean mFocusFlag = false;
 
     private Size mSize = null;
     private List<Size> mSupportList = null;
     //T|[gXgɑ΂[̉l̃CfbNX
     private int mOffset = 0;
     private File mFile = null;
     PreviewCallback mPreviewCallback = null;
     
     //for setting
 	private boolean mSetColor = false;
 	private boolean mSetScene = false;
 	private boolean mSetWhite = false;
 	private boolean mSetSize = false;
 	private String mSetValue = null;
 	private int mSetInt = 0;
 	
 	//ݒ
 	private String mEffect = null;
 	private String mScene = null;
 	private String mWhite = null;
 	private int mPicIdx = 0;
 	private String mSizeStr = null;
 	//Aʐ
 	private int mMax = 0;
 	//݂̎Be
 	private int mNum = 0;
 	
 	//AʊԊu
 	private int mInterval = 0;
 	
 	//ʃTCY
 	int mWidth = 0;
 	int mHeight = 0;
 		
     CameraPreview(Context context){
         mContext = context;
 	}
 	
 	public void setField(String effect, String scene, String white, String size, int width, int height){
         mEffect = effect;
         mScene = scene;
         mWhite = white;
         //mPicIdx = size;
         mSizeStr = size;
         mWidth = width;
         mHeight = height;
 	}
     
     public void surfaceCreated(SurfaceHolder holder) {
     	//Log.d(TAG, "enter CameraPreview#surfaceCreated");
 
     	if(mCamera == null){
     	    try{
                 mCamera = Camera.open();
     	        
     	    }catch(RuntimeException e){
     	        new AlertDialog.Builder(mContext)
     	        .setTitle(R.string.sc_error_title)
     	        .setMessage(mContext.getString(R.string.sc_error_cam))
     	        .setPositiveButton(R.string.sc_error_cam_ok, new DialogInterface.OnClickListener() {
     	            public void onClick(DialogInterface dialog, int which) {
     	                System.exit(0);
     	            }
     	        })
     	        .show();
     	            
     	        try {
     	            this.finalize();
     	        } catch (Throwable t) {
     	            System.exit(0);                 
     	        }
     	        return;
     	    }
     	}
     	
     	if(mSupportList == null){
     	    createSupportList();
     	}
 
     	try {
             mCamera.setPreviewDisplay(holder);               
         } catch (IOException e) {
             Log.e(TAG, "IOException in surfaceCreated");
             mCamera.release();
             mCamera = null;
         }
     }
     
     private void createSupportList(){
         Camera.Parameters params = mCamera.getParameters();
         mSupportList = Reflect.getSupportedPreviewSizes(params);
            
         if (mSupportList != null && mSupportList.size() > 0) {
             /*
             for(int i=0;i<mSupportList.size();i++){
                 Log.d(TAG, "SupportedSize = " + mSupportList.get(i).width + "*" + mSupportList.get(i).height);
             }
             */
 
             //~Ƀ\[g
             Collections.sort(mSupportList, new PreviewComparator());
 
             /*
             for(int i=0;i<mSupportList.size();i++){
                 Log.d(TAG, "SupportedSize = " + mSupportList.get(i).width + "*" + mSupportList.get(i).height);
             }
             */
             
             for(int i = 0; i < mSupportList.size(); i++){
                 if(mSupportList.get(i).width > mWidth){
                     continue;
                 }
                 
                 if(mSupportList.get(i).height > mHeight){
                     continue;
                 }
                 
                 mSize = mSupportList.get(i);
                 mOffset = i;
                 break;
             }
             
             //Log.d(TAG, "size = " + mSize.width + "*" + mSize.height);
 
             if(mSize == null){
                 mSize = mSupportList.get(0);
                 mOffset = 0;
             }
             //params.setPreviewSize(mSize.width, mSize.height);
             //mCamera.setParameters(params);     
         }
     }
     
     public void surfaceDestroyed(SurfaceHolder holder) {
         //Log.d(TAG, "enter CameraPreview#surfaceDestroyed");
     	release();
     }
 
     public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
         //Log.d(TAG, "enter CameraPreview#surfaceChanged");
         
         //Cameraopen()łȂƂp
         if(mCamera == null){
             return;
         }
 
         //~߂ȂsetParameters()ƃG[ƂȂꍇ邽ߎ~߂
         mCamera.stopPreview();
 
         /*
         //if(mFocusFlag){
         	mFocus = new AutoFocusCallback(){
         		public void onAutoFocus(boolean success, Camera camera) {
         			//mCamera.setOneShotPreviewCallback(mPreviewCallback);
         			mCamera.setPreviewCallback(mPreviewCallback);
         		}
         	};
         //}
         */
         
         //ݒʂŐݒ肵Ƃ
         if(mSetValue != null){
         	if(mSetColor == true){
                 mEffect = mSetValue;
         	}
         	else if(mSetScene == true){
                 mScene = mSetValue;
         	}
         	else if(mSetWhite == true){
                 mWhite = mSetValue;
         	}
         	else if(mSetSize == true){
                 mPicIdx = mSetInt;
                 mSizeStr = getSizeList().get(mPicIdx);
         	}
         	
             mSetValue = null;
             mSetColor = false;
             mSetScene = false;
             mSetWhite = false;
             mSetSize = false;
             mSetInt = 0;
         }
         //ݒʂŐݒ肵ȂƂ
         else{
             List<String> list = getSizeList();
             for(int i = 0; i<list.size(); i++){
                 if(list.get(i).equals(mSizeStr)){
                     mPicIdx = i;
                 }
                 //mSizeStr"0"̂ƂmPicIdxɒlݒ肳ꂸɔ(=0ɂȂ)
             }
         }
         
         setAllParameters();
 
         //mPreviewCallback = new PreviewCallback(this);
         mCamera.startPreview();
         //focus
         mFocus = new AutoFocusCallback(){
             public void onAutoFocus(boolean success, Camera camera) {
                 mPreviewCallback = new PreviewCallback(CameraPreview.this);
             }
         };
         try{
             mCamera.autoFocus(mFocus);
         }catch(Exception e){
             mPreviewCallback = new PreviewCallback(CameraPreview.this);            
         }
     }
     
     private void setAllParameters(){
         Camera.Parameters param = mCamera.getParameters();
         
         /*
         param.setColorEffect(mEffect);            
         param.setSceneMode(mScene);
         param.setWhiteBalance(mWhite);
         mSize = mSupportList.get(mOffset + mPicIdx);        
         param.setPreviewSize(mSize.width, mSize.height);
         mCamera.setParameters(param);
         */
 
         //xɕ̃p[^ݒ肷Ɨ[邽߁A1ݒ肷
         try{
             param.setColorEffect(mEffect);            
             mCamera.setParameters(param);                
         }catch(Exception e){
             param = mCamera.getParameters();
         }
 
         try{
             param.setSceneMode(mScene);
             mCamera.setParameters(param);                
         }catch(Exception e){
             param = mCamera.getParameters();
         }
 
         try{
             param.setWhiteBalance(mWhite);
             mCamera.setParameters(param);                
         }catch(Exception e){
             param = mCamera.getParameters();
         }
 
         try{
             mSize = mSupportList.get(mOffset + mPicIdx);        
             param.setPreviewSize(mSize.width, mSize.height);
             mCamera.setParameters(param);
         }catch(Exception e){
             //nothing to do
         }
     }
     
     public void resumeShooting(){
     	if(mPreviewCallback != null){
     		if(mCamera != null){
     			mCamera.startPreview();
     			mCamera.setPreviewCallback(mPreviewCallback);
     		}
     	}
 
     	//{^\u~vɕύX
     	((ContShooting)mContext).displayStop();
     }
     
     public void stopShooting(){
     	//Log.d(TAG, "enter CameraPreview#stopPreview");
 
     	mCamera.stopPreview();
         mCamera.setPreviewCallback(null);
 		//{^\uJnvɕύX
 		((ContShooting)mContext).displayStart();
         mNum = 0;
 		//vr[Jn(摜ۑ͂Ȃ(setPreviewCallbackĂ΂Ȃ))
         mCamera.startPreview();
     }
 
     void doAutoFocus(){
     	if(mCamera != null && mFocus != null){
            mCamera.setPreviewCallback(null);
     		try{
     			mCamera.autoFocus(mFocus);
     		}catch(Exception e){
     			mPreviewCallback = new PreviewCallback(CameraPreview.this);            
     		}
     	}
     }
     
     public void setZoom(boolean flag){
     	/*
     	if(mCamera == null){
     		return;
     	}
     	
         Camera.Parameters params = mCamera.getParameters();
 
         //if(params.isSmoothZoomSupported() == false){
         //Log.d(TAG, "Zoom is not supported");
         //	return;
         //}
         
         List ZoomRatislist = params.getZoomRatios ();
         for (int i=0;i < ZoomRatislist.size();i++) {
         	Log.d("camera", "list " + i + " = " + ZoomRatislist.get(i));
         }
 
     	
         int cur = params.getZoom();
         int max = params.getMaxZoom();
 
         Log.d(TAG, "currentZoom: " + cur);
         Log.d(TAG, "maxZoom: " + max);
         
         if(flag){
         	if(cur < max){
         		//mCamera.startSmoothZoom(++cur);
         		params.setZoom(++cur);
         		mCamera.setParameters(params);
         	}
         }
         else{
         	if(cur > 0){
         		//mCamera.startSmoothZoom(--cur);
         		params.setZoom(--cur);
         		mCamera.setParameters(params);
         	}
         }
         */
     }
     
     List<String> getEffectList(){
         Camera.Parameters param = mCamera.getParameters();
         return param.getSupportedColorEffects();
     }
     
     List<String> getWhiteBalanceList(){
         Camera.Parameters param = mCamera.getParameters();
         return param.getSupportedWhiteBalance();
     }
     
     List<String> getSceneModeList(){
         Camera.Parameters param = mCamera.getParameters();
         return param.getSupportedSceneModes();
     }
     
     List<String> getSizeList(){
     	List<String> list = new ArrayList<String>();
     	for(int i = mOffset; i<mSupportList.size(); i++){
     		String size = mSupportList.get(i).width + "x" + mSupportList.get(i).height;
     		list.add(size);
     	}
     	return list;
     }
     
     void setColorValue(String value){
     	mSetColor = true;
     	mSetValue = value;
     }
     
     void setSceneValue(String value){
     	mSetScene = true;
     	mSetValue = value;
     }
     
     void setWhiteValue(String value){
     	mSetWhite = true;
     	mSetValue = value;
     }
     
     void setSizeValue(int value){
     	mSetSize = true;
     	mSetInt = value;
     	//}[N̂
     	mSetValue = "hoge";
     }
     
     void setShootNum(int num){
         mMax = num;
     }
     
     void setInterval(int interval){
         mInterval = interval;
     }
     
     void countShoot(){
     	//((ContShooting)mContext).count();
 
         if(mInterval == 0 && ((ContShooting)mContext).mMode == 1){
             //R[obNĊJ
             mCamera.setPreviewCallback(mPreviewCallback);                
         }
 
         mNum++;
         if(mMax!=0){
             if(mNum >= mMax){
                 stopShooting();
                 ((ContShooting)mContext).setMode(0);
             }
         }        
     }
     
     void release(){
         if(mCamera != null){
             mCamera.setPreviewCallback(null);
             mCamera.stopPreview();
             mCamera.release();
             mCamera = null;
         }
         
         ((ContShooting)mContext).setMode(0);
         ((ContShooting)mContext).displayStart();
         
         mNum=0;
         //mSupportList = null;
     }
 
     class PreviewComparator implements java.util.Comparator {
     	public int compare(Object s, Object t) {
     		//~
     		return ((Size) t).width - ((Size) s).width;
     	}
     }
 
     
     public class PreviewCallback implements Camera.PreviewCallback {
         private CameraPreview mPreview = null;
 
         PreviewCallback(CameraPreview preview){
             mPreview = preview;
         }
 
         public void onPreviewFrame(byte[] data, Camera camera) {
         	//Log.d(TAG, "enter CameraPreview#onPreviewFrame");
             //Log.d(TAG, "data.length = " + data.length);
         	
             //UR[obN~߂
         	camera.setPreviewCallback(null);
 
         	//BeԊuݒp̃^C}
             if(mInterval != 0){
                 Thread t2 = new Thread(){
                     public void run(){
                         try {
                             Thread.sleep(mInterval * 1000);
                         } catch (InterruptedException e) {
                         }
 
                         if(mCamera != null){
                             //BêƂ̓R[obNĊJB~͍ĊJȂ
                             if(((ContShooting)mContext).mMode == 1){
                                 mCamera.setPreviewCallback(mPreviewCallback);                                   
                             }
                         }
                     }
                 };
                 t2.start();
             }
 
             //convert to "real" preview size. not size setting before.
             Size size = convertPreviewSize(data);
 
             final int width = size.width;
             final int height = size.height;            
 
             Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
             
             ((ContShooting)mContext).count();
             ImageAsyncTask task = new ImageAsyncTask(mContext, CameraPreview.this, data, size);
             task.execute(bmp);
        }
         
         private Size convertPreviewSize(byte[] data){
             double displaysize = data.length / 1.5;
             Size size;
             int x, y;
             
             for(int i=0; i<mSupportList.size(); i++){
                 size = mSupportList.get(i);
                 x = size.width;
                 y = size.height;
                 if((x*y) == displaysize){
                     return size;
                 }
             }
             return null;
         }
     }
 }
