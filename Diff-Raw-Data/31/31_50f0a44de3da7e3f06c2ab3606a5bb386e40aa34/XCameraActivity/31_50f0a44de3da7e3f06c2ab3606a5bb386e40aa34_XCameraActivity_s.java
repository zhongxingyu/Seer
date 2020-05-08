 
 /*
  Copyright 2012-2013, Polyvi Inc. (http://polyvi.github.io/openxface)
  This program is distributed under the terms of the GNU General Public License.
 
  This file is part of xFace.
 
  xFace is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  xFace is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with xFace.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.polyvi.xface.extension.zbar;
 
 import net.sourceforge.zbar.Config;
 import net.sourceforge.zbar.Image;
 import net.sourceforge.zbar.ImageScanner;
 import net.sourceforge.zbar.Symbol;
 import net.sourceforge.zbar.SymbolSet;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.PreviewCallback;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Vibrator;
 import android.widget.LinearLayout;
 
 import com.polyvi.xface.util.XLog;
 
 /*
  * 通过activity启动摄像机，返回preview 图片，scanner（so库）对preview 图片进行分析，
  * 如果发现条形码就返回分析结果，扫描结果通过intent将返回。
  */
 public class XCameraActivity extends Activity {
     private static final String CLASS_NAME = XCameraActivity.class.getSimpleName();
     private static final long VIBRATE_DURATION = 200L;
 
     private Camera mCamera;
     private XCameraPreview mPreview;
     private Handler mAutoFocusHandler;
     private ImageScanner mScanner;
     private boolean mPreviewing = true;
 
     static {
         System.loadLibrary("iconv");
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         try {
             LinearLayout layout = new LinearLayout(this);
             setContentView(layout);
 
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
             mCamera = getCameraInstance();
             if(null == mCamera) {
                 Intent intent = new Intent();
                 setResult(RESULT_CANCELED, intent);
                 finish();
                 return;
             }
             mAutoFocusHandler = new Handler();
             /* Instance barcode scanner */
             mScanner = new ImageScanner();
             mScanner.setConfig(0, Config.X_DENSITY, 3);
             mScanner.setConfig(0, Config.Y_DENSITY, 3);
             mPreview = new XCameraPreview(this, mCamera, previewCb, autoFocusCB);
             layout.addView(mPreview);
 
             mCamera.setPreviewCallback(previewCb);
             mCamera.startPreview();
             mPreviewing = true;
             mCamera.autoFocus(autoFocusCB);
         } catch (Exception e) {
             XLog.e(CLASS_NAME, "Error in onCreate:" + e.getMessage());
         }
     }
 
     public void onPause() {
         super.onPause();
         releaseCamera();
     }
 
     /** A safe way to get an instance of the Camera object. */
     private Camera getCameraInstance() {
         Camera c = null;
         //TODO:api level 9可选择摄像头，比如前置摄像头
         try {
             c = Camera.open();
         } catch (Exception e) {
             XLog.e(CLASS_NAME, "open camera fail");
         }
         return c;
     }
 
     private void releaseCamera() {
         if (mCamera != null) {
             mPreviewing = false;
             mCamera.setPreviewCallback(null);
             mCamera.release();
             mCamera = null;
         }
     }
 
     private Runnable doAutoFocus = new Runnable() {
         public void run() {
             if (mPreviewing) {
                 try {
                     mCamera.autoFocus(autoFocusCB);
                 } catch (Exception e) {
                    XLog.e(CLASS_NAME, "Error when running autoFocusCB"+ e.getMessage());
                 }
             }
         }
     };
 
     private PreviewCallback previewCb = new PreviewCallback() {
         public void onPreviewFrame(byte[] data, Camera camera) {
             Camera.Parameters parameters = camera.getParameters();
             Size size = parameters.getPreviewSize();
 
             Image barcode = new Image(size.width, size.height, "Y800");
             barcode.setData(data);
 
             int result = mScanner.scanImage(barcode);
 
             if (result != 0) {
                 mPreviewing = false;
                 mCamera.setPreviewCallback(null);
                 mCamera.stopPreview();
 
                 SymbolSet syms = mScanner.getResults();
 
                 playVibrate();// 振动代表成功获取二维码
 
                for (Symbol sym : syms) {
                    // 将扫描后的信息返回
                    Intent intent = new Intent();
                    intent.putExtra("Code", sym.getData());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    };
 
     // Mimic continuous auto-focusing
     private AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
         public void onAutoFocus(boolean success, Camera camera) {
             mAutoFocusHandler.postDelayed(doAutoFocus, 1000);
         }
     };
 
     /**
      * 震动
      */
     private void playVibrate() {
         // 打开震动
         Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
         vibrator.vibrate(VIBRATE_DURATION);
     }
 }
