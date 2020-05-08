 /*
  * Licensed under the MIT license
  * http://htmlpreview.github.com/?https://github.com/tapfortap/Documentation/blob/master/License.html
  * Copyright (c) 2013 Tap for Tap
  */
 
 package com.tapfortap.ti;
 
 import org.appcelerator.kroll.KrollProxy;
 import org.appcelerator.kroll.annotations.Kroll;
 
 import com.tapfortap.AppWall;
 import com.tapfortap.AppWall.AppWallListener;
 
 @Kroll.proxy(creatableInModule = TapForTapTitaniumModule.class)
 public class AppWallProxy extends KrollProxy implements AppWallListener {
 
 	@Kroll.method
 	public void prepare() {
 		AppWall.prepare(getActivity());
 	}
 
 	@Kroll.method
 	public void show() {
 		AppWall.show(getActivity());
 		AppWall.setListener(this);
 	}
 
	public void onReceiveAd() {
		fireEvent("receive", null);
	}

	@Override
	public void onShow() {
		fireEvent("show", null);
	}

 	@Override
 	public void onDismiss() {
 		fireEvent("dismiss", null);
 	}
 
 	@Override
 	public void onFail(String reason) {
 		fireEvent("fail", reason);
 	}
 
 }
