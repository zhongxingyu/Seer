 /*
  * Licensed under the MIT license
  * http://htmlpreview.github.com/?https://github.com/tapfortap/Documentation/blob/master/License.html
  * Copyright (c) 2013 Tap for Tap
  */
 
 package com.tapfortap.ane;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.view.OrientationEventListener;
 import android.widget.FrameLayout;
 
 import com.adobe.fre.FREContext;
 import com.adobe.fre.FREFunction;
 
 import com.tapfortap.Banner;
 import com.tapfortap.Interstitial;
 import com.tapfortap.AppWall;
 
 import com.tapfortap.ane.functions.AppWallIsReadyFunction;
 import com.tapfortap.ane.functions.CreateAdViewFunction;
 import com.tapfortap.ane.functions.GetVersionFunction;
 import com.tapfortap.ane.functions.InterstitialIsReadyFunction;
 import com.tapfortap.ane.functions.InitializeWithApiKeyFunction;
 import com.tapfortap.ane.functions.PrepareAppWallFunction;
 import com.tapfortap.ane.functions.PrepareInterstitialFunction;
 import com.tapfortap.ane.functions.RemoveAdViewFunction;
 import com.tapfortap.ane.functions.SetAutoScale;
 import com.tapfortap.ane.functions.SetGenderFunction;
 import com.tapfortap.ane.functions.SetLocationFunction;
 import com.tapfortap.ane.functions.SetModeFunction;
 import com.tapfortap.ane.functions.SetUserAccountIdFunction;
 import com.tapfortap.ane.functions.SetYearOfBirthFunction;
 import com.tapfortap.ane.functions.ShowAppWallFunction;
 import com.tapfortap.ane.functions.ShowInterstitialFunction;
 
 public class TapForTapExtensionContext extends FREContext {
 	public Banner banner;
     public Interstitial interstitial;
     public AppWall appWall;
 	public FrameLayout layout;
    public Boolean autoScale;
 
     @Override
     public Map<String, FREFunction> getFunctions() {
         Map<String, FREFunction> functions = new HashMap<String, FREFunction>();
 
         functions.put("initializeWithApiKey", new InitializeWithApiKeyFunction());
         functions.put("setYearOfBirth", new SetYearOfBirthFunction());
         functions.put("setGender", new SetGenderFunction());
         functions.put("setLocation", new SetLocationFunction());
         functions.put("setUserAccountId", new SetUserAccountIdFunction());
         functions.put("getVersion", new GetVersionFunction());
         functions.put("createAdView", new CreateAdViewFunction());
         functions.put("removeAdView", new RemoveAdViewFunction());
         functions.put("prepareInterstitial", new PrepareInterstitialFunction());
         functions.put("showInterstitial", new ShowInterstitialFunction());
         functions.put("interstitialIsReady", new InterstitialIsReadyFunction());
         functions.put("prepareAppWall", new PrepareAppWallFunction());
         functions.put("showAppWall", new ShowAppWallFunction());
         functions.put("appWallIsReady", new AppWallIsReadyFunction());
         functions.put("setMode", new SetModeFunction());
         functions.put("setAutoScale", new SetAutoScale());
 
         return functions;
     }
 
     @Override
     public void dispose() {
         banner.setListener(null);
     	banner = null;
         interstitial = null;
         appWall = null;
     	layout = null;
     }
 }
