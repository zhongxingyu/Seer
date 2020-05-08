 /*
  * Copyright 2013 Intel Corporation All Rights Reserved.
  *
  * The source code contained or described herein and all documents related to
  * the source code ("Material") are owned by Intel Corporation or its suppliers
  * or licensors. Title to the Material remains with Intel Corporation or its
  * suppliers and licensors. The Material contains trade secrets and proprietary
  * and confidential information of Intel or its suppliers and licensors. The
  * Material is protected by worldwide copyright and trade secret laws and
  * treaty provisions.
  * No part of the Material may be used, copied, reproduced, modified, published
  * , uploaded, posted, transmitted, distributed, or disclosed in any way
  * without Intel's prior express written permission.
  *
  * No license under any patent, copyright, trade secret or other intellectual
  * property right is granted to or conferred upon you by disclosure or delivery
  * of the Materials, either expressly, by implication, inducement, estoppel or
  * otherwise. Any license under such intellectual property rights must be
  * express and approved by Intel in writing.
  *
  * @brief One sentence description
  * @date 2013年9月4日
  *
  */
 
 package com.habzy.pillow;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import com.habzy.pillow.util.CameraUtil;
 import com.habzy.pillow.util.Util;
 import com.tencent.mm.sdk.openapi.GetMessageFromWX;
 import com.tencent.mm.sdk.openapi.IWXAPI;
 import com.tencent.mm.sdk.openapi.SendMessageToWX;
 import com.tencent.mm.sdk.openapi.WXAPIFactory;
 import com.tencent.mm.sdk.openapi.WXAppExtendObject;
 import com.tencent.mm.sdk.openapi.WXMediaMessage;
 
 import java.io.File;
 
 /**
  * Detailed description
  */
 public class GetFromWXActivity extends Activity {
 
     private IWXAPI api;
     private Bundle bundle;
 
     private static final String SDCARD_ROOT = Environment.getExternalStorageDirectory()
             .getAbsolutePath();
 
     private static final String TAG = "GetFromWXActivity";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         api = WXAPIFactory.createWXAPI(this, MainActivity.APP_ID);
         bundle = getIntent().getExtras();
         Log.d(TAG, "onCreate bundle:" + bundle + "  api:" + api);
 
         setContentView(R.layout.get_from_wx);
 
         initView();
     }
 
     /**
      * 
      */
     private void initView() {
         findViewById(R.id.create_msg).setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 final String dir = SDCARD_ROOT + "/tencent/";
                 File file = new File(dir);
                 if (!file.exists()) {
                     file.mkdirs();
                 }
 
                CameraUtil.takePhoto(GetFromWXActivity.this, dir, "get_appdata", 0x100);
             }
         });
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onActivityResult(int, int,
      * android.content.Intent)
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         Log.d(TAG, "onActivityResult requestCode:" + requestCode + " resultCode" + resultCode);
 
         switch (requestCode) {
             case 0x100: {
                 if (resultCode == RESULT_OK) {
                     final WXAppExtendObject appdata = new WXAppExtendObject();
                    final String path = SDCARD_ROOT + "/tencent/get_appdata";
                     appdata.filePath = path;
                     appdata.extInfo = "this is ext info";
 
                     final WXMediaMessage msg = new WXMediaMessage();
                     msg.setThumbImage(Util.extractThumbNail(path, 150, 150, true));
                     msg.title = "this is title";
                     msg.description = "this is description";
                     msg.mediaObject = appdata;
 
                     GetMessageFromWX.Resp resp = new GetMessageFromWX.Resp();
                     resp.transaction = buildTransaction();
                     resp.message = msg;
 
                     boolean sendRespResult = api.sendResp(resp);
                     Log.d(TAG, "send response to weixin success? " + sendRespResult);
 
                     finish();
                 }
                 break;
             }
             default:
                 break;
         }
     }
 
     private String buildTransaction() {
         final GetMessageFromWX.Req req = new GetMessageFromWX.Req(bundle);
         return req.transaction;
     }
 
 }
