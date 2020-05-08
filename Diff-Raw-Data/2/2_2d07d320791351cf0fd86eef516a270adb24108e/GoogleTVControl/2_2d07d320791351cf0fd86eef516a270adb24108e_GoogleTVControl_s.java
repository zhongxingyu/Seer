 /*
  Copyright (c) 2011, Sony Ericsson Mobile Communications AB
 
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.
 
  * Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.
 
  * Neither the name of the Sony Ericsson Mobile Communications AB nor the names
  of its contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.doubtech.livewatch.googletvremote;
 
 import com.doubtech.livewatch.googletvremote.widgets.KeyCodeButton;
 import com.doubtech.livewatch.googletvremote.widgets.SoftDpad;
 import com.doubtech.livewatch.googletvremote.widgets.SoftDpad.DpadListener;
 import com.example.google.tv.anymotelibrary.client.AnymoteClientService;
 import com.example.google.tv.anymotelibrary.client.AnymoteClientService.ClientListener;
 import com.example.google.tv.anymotelibrary.client.AnymoteSender;
 import com.google.anymote.Key.Code;
 import com.sonyericsson.extras.liveware.extension.util.view.HorizontalPager;
 import com.sonyericsson.extras.liveware.sdk.R;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 
 /**
  * The sample control for SmartWatch handles the control on the accessory.
  * This class exists in one instance for every supported host application that
  * we have registered to
  */
 class GoogleTVControl extends HorizontalPager implements ClientListener {
 	private static final String TAG = "GoogleTVControl";
 	private static final boolean DEBUG = true;
 	  
 	private SoftDpad mDpad;
 	
 	private AnymoteClientService mAnymoteClientService;
     private AnymoteSender anymoteSender;
     private boolean mBound;
 
     private KeyCodeButton mChannelDown;
     private KeyCodeButton mChannelUp;
 
     /**
      * Create sample control.
      *
      * @param hostAppPackageName Package name of host application.
      * @param context The context.
      * @param handler The handler to use
      */
     GoogleTVControl(final Context context, int device, final String hostAppPackageName) {
         super(context, device, hostAppPackageName);
         
         // Inflate all remote page views.
         final LayoutInflater inflater = LayoutInflater.from(context);
         final ViewGroup viewContainer = (ViewGroup)inflater.inflate(R.layout.remote_page_layout, null);
         for(int i = 0; i < viewContainer.getChildCount(); i++) {
 	        final ViewGroup controlPage = (ViewGroup)viewContainer.getChildAt(i);
 	        setListeners(controlPage);
 	        addView(controlPage);
         }
     }
     
     OnTouchListener mKeyCodePressedListener = new OnTouchListener() {
 
         @Override
         public boolean onTouch(View v, MotionEvent event) {
         	if(DEBUG) {
         		Log.d(TAG, "Pressed: " + ((KeyCodeButton)v).getKeyCode());
         	}
             if(null != anymoteSender) {
                 KeyCodeButton button = (KeyCodeButton)v;
                 anymoteSender.sendKeyPress(button.getKeyCode());
             }
             return true;
         }
 
     };
 
     private void setListeners(ViewGroup controls) {
         for(int i = 0; i < controls.getChildCount(); i++) {
             View child = controls.getChildAt(i);
             if(child instanceof ViewGroup) {
                 setListeners((ViewGroup)child);
             } else if(child instanceof KeyCodeButton) {
                 child.setOnTouchListener(mKeyCodePressedListener);
             }
         }
     }
 
     /** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection mConnection = new ServiceConnection() {
         /*
          * ServiceConnection listener methods.
          */
         public void onServiceConnected(ComponentName name, IBinder service) {
             mAnymoteClientService = ((AnymoteClientService.AnymoteClientServiceBinder) service)
                     .getService();
             mAnymoteClientService.attachClientListener(GoogleTVControl.this);
         }
 
         public void onServiceDisconnected(ComponentName name) {
             mAnymoteClientService.detachClientListener(GoogleTVControl.this);
             mAnymoteClientService = null;
         }
     };
 
     @Override
     public void onConnected(AnymoteSender anymoteSender) {
         this.anymoteSender = anymoteSender;
     }
 
     @Override
     public void onDisconnected() {
         this.anymoteSender = null;
     }
 
     @Override
     public void onConnectionError() {
         this.anymoteSender = null;
     }
     
     @Override
     public void onStop() {
         super.onStop();
         unbind();
     }
     
     public void onPause() {
         super.onPause();
        unbind();
     }
 
     private void unbind() {
         if(mBound) {
             mContext.unbindService(mConnection);
             mBound = false;
         }
     }
     
     @Override
     public void onResume() {
         super.onResume();
         // Bind to the AnymoteClientService
         Intent intent = new Intent(mContext, AnymoteClientService.class);
         mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
         mBound = true;
     }
     
 }
