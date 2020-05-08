 /*
  * Licensed under the MIT license
  * http://htmlpreview.github.com/?https://github.com/tapfortap/Documentation/blob/master/License.html
  * Copyright (c) 2013 Tap for Tap
  */
 
 package com.tapfortap.ti;
 
 import org.appcelerator.kroll.KrollProxy;
 import org.appcelerator.kroll.annotations.Kroll;
 
 import com.tapfortap.Interstitial;
 import com.tapfortap.Interstitial.InterstitialListener;
 
 @Kroll.proxy(creatableInModule = TapForTapTitaniumModule.class)
 public class InterstitialProxy extends KrollProxy implements InterstitialListener {
 
 	@Kroll.method
 	public void prepare() {
 		Interstitial.prepare(getActivity());
 	}
 
 	@Kroll.method
 	public void show() {
 		Interstitial.show(getActivity());
 		Interstitial.setListener(this);
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
