 package com.baixing.sharing;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 
 import com.baixing.activity.BaseActivity;
 import com.baixing.activity.MainActivity;
 import com.baixing.broadcast.CommonIntentAction;
 import com.baixing.data.GlobalDataManager;
 import com.baixing.entity.Ad;
 import com.baixing.entity.ChatMessage;
 import com.baixing.entity.ImageList;
 import com.baixing.imageCache.ImageCacheManager;
 import com.baixing.util.Util;
 import com.baixing.util.ViewUtil;
 import com.weibo.sdk.android.Oauth2AccessToken;
 import com.weibo.sdk.android.Weibo;
 import com.weibo.sdk.android.WeiboAuthListener;
 import com.weibo.sdk.android.WeiboDialogError;
 import com.weibo.sdk.android.WeiboException;
 import com.weibo.sdk.android.sso.SsoHandler;
 
 public class WeiboSSOSharingManager extends BaseSharingManager {
 	public static class WeiboAccessTokenWrapper implements Serializable {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 973987291134876738L;
 		private String token;
 		private String expires_in;
 
 		// public WeiboAccessTokenWrapper(String token, String expires){
 		// this.token = token;
 		// this.expires_in = expires;
 		// }
 		public String getToken() {
 			return token;
 		}
 
 		public void setToken(String token) {
 			this.token = token;
 		}
 
 		public String getExpires_in() {
 			return expires_in;
 		}
 
 		public void setExpires_in(String expires_in) {
 			this.expires_in = expires_in;
 		}
 
 	}
 
 	private Ad mAd;
 	private BaseActivity mActivity;
 	static final String kWBBaixingAppKey = "3747392969";
 	private static final String kWBBaixingAppSecret = "ff394d0df1cfc41c7d89ce934b5aa8fc";
 	public static final String STRING_WEIBO_ACCESS_TOKEN = "weiboaccesstoken";
 	private Weibo mWeibo;
 	private WeiboAccessTokenWrapper mToken;
 	private SsoHandler mSsoHandler;
 
 	public void doAuthorizeCallBack(int requestCode, int resultCode, Intent data) {
 		if (mSsoHandler != null) {
 			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
 		}
 	}
 
 	private WeiboAccessTokenWrapper loadToken() {
 		return (WeiboAccessTokenWrapper) Util.loadDataFromLocate(mActivity,
 				STRING_WEIBO_ACCESS_TOKEN, WeiboAccessTokenWrapper.class);
 	}
 
 	static public void saveToken(Context context, WeiboAccessTokenWrapper token) {
 		Util.saveDataToLocate(context, STRING_WEIBO_ACCESS_TOKEN, token);
 	}
 
 	public WeiboSSOSharingManager(BaseActivity activity) {
 		mActivity = activity;
 		mToken = loadToken();
 	}
 
 	class AuthDialogListener implements WeiboAuthListener {
 
 		@Override
 		public void onComplete(Bundle values) {
 			String token = values.getString("access_token");
 			String expires_in = values.getString("expires_in");
 			Oauth2AccessToken accessToken = new Oauth2AccessToken(token,
 					expires_in);
 			if (accessToken.isSessionValid()) {
 				WeiboAccessTokenWrapper wtw = new WeiboAccessTokenWrapper();
 				wtw.setToken(token);
 				wtw.setExpires_in(expires_in);
 				saveToken(mActivity, wtw);
 				doShare2Weibo(accessToken);
 			}
 		}
 
 		@Override
 		public void onError(WeiboDialogError e) {
 			ViewUtil.showToast(mActivity, e.getMessage());
 		}
 
 		@Override
 		public void onCancel() {
 		}
 
 		@Override
 		public void onWeiboException(WeiboException e) {
 			ViewUtil.showToast(mActivity, e.getMessage());
 		}
 	}
 
 	@Override
 	public void auth() {
 		try {
 			Class sso = Class.forName("com.weibo.sdk.android.sso.SsoHandler");
 			authSSO();
 		} catch (ClassNotFoundException e) {
 			authTraditional();
 		}
 	}
 
 	@Override
 	public void share(Ad ad) {
 		mAd = ad;
 		if (mToken != null && mToken.getExpires_in() != null
 				&& mToken.getExpires_in().length() > 0
 				&& mToken.getToken() != null && mToken.getToken().length() > 0) {
 			Oauth2AccessToken accessToken = new Oauth2AccessToken(
 					mToken.getToken(), mToken.getExpires_in());
 			if (accessToken.isSessionValid()) {
 				doShare2Weibo(accessToken);
 				return;
 			}
 		}
 		
 		auth();
 	}
 
 	private void authTraditional() {
 		mWeibo = Weibo.getInstance(kWBBaixingAppKey, "http://www.baixing.com");
 		mWeibo.authorize(mActivity, new AuthDialogListener());
 	}
 
 	private void unregisterListener() {
 		if (msgListener != null) {
 			mActivity.unregisterReceiver(msgListener);
 		}
 	}
 
 	private BroadcastReceiver msgListener;
 	private boolean isActive = true;
 
 	private void authSSO() {
 		Intent intent = new Intent();
 		intent.setClass(mActivity, WeiboManagerActivity.class);
 		intent.putExtra("ad", mAd);
 		mActivity.startActivity(intent);
 		isActive = false;
 		unregisterListener();
 		msgListener = new BroadcastReceiver() {
 
 			public void onReceive(Context outerContext, Intent outerIntent) {
 				if (outerIntent.getAction().equals(CommonIntentAction.ACTION_BROADCAST_WEIBO_AUTH_DONE)) {
 					mToken = loadToken();
 				}else if(outerIntent.getAction().equals(CommonIntentAction.ACTION_BROADCAST_SHARE_BACK_TO_FRONT)){
 					isActive = true;
 				}
 				if(mToken != null && isActive){
 					if(mAd != null){
 						share(mAd);
 					}
 					unregisterListener();
 				}
 			}
 
 		};
 		mActivity.registerReceiver(msgListener, new IntentFilter(
 				CommonIntentAction.ACTION_BROADCAST_WEIBO_AUTH_DONE));
 		mActivity.registerReceiver(msgListener, new IntentFilter(
 				CommonIntentAction.ACTION_BROADCAST_SHARE_BACK_TO_FRONT));		
 	}
 
 	@Override
 	public void release() {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void doShare2Weibo(Oauth2AccessToken accessToken) {
 		String imgUrl = super.getThumbnailUrl(mAd);
 		String imgPath = (imgUrl == null || imgUrl.length() == 0) ? "" : ImageCacheManager.getInstance().getFileInDiskCache(imgUrl);
 
 		Bundle bundle = new Bundle();
 		bundle.putString(WeiboSharingFragment.EXTRA_WEIBO_CONTENT,
				"我用@百姓网发布了\"" + mAd.getValueByKey("title") + "\"" + "麻烦朋友们帮忙转发一下～ " + mAd.getValueByKey("link"));
 		bundle.putString(WeiboSharingFragment.EXTRA_PIC_URI,
 				(imgPath == null || imgPath.length() == 0) ? "" : imgPath);
 		bundle.putString(WeiboSharingFragment.EXTRA_ACCESS_TOKEN,
 				accessToken.getToken());
 		bundle.putString(WeiboSharingFragment.EXTRA_EXPIRES_IN,
 				String.valueOf(accessToken.getExpiresTime()));
 		mActivity.pushFragment(new WeiboSharingFragment(), bundle, false);
 	}
 }
