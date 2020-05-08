 package com.novel.reader;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.ads.Ad;
 import com.google.ads.AdListener;
 import com.google.ads.AdRequest;
 import com.google.ads.AdRequest.ErrorCode;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.kosbrother.tool.DetectScrollView;
 import com.kosbrother.tool.DetectScrollView.DetectScrollViewListener;
 import com.kosbrother.tool.Report;
 import com.novel.reader.api.NovelAPI;
 import com.novel.reader.entity.Article;
 import com.novel.reader.entity.Bookmark;
 import com.novel.reader.util.Setting;
 
 public class ArticleActivity extends SherlockFragmentActivity implements DetectScrollViewListener {
 
     private static final int    ID_SETTING  = 0;
     private static final int    ID_RESPONSE = 1;
     private static final int    ID_ABOUT_US = 2;
     private static final int    ID_GRADE    = 3;
     private static final int    ID_Bookmark = 4;
     private static final int    ID_Report   = 5;
 
     private int                 textSize;
     private int                 textLanguage;                                    // 0 for 繁體, 1 for 簡體
     private int                 readingDirection;                                // 0 for 直向, 1 for 橫向
     private int                 clickToNextPage;                                 // 0 for yes, 1 for no
     private int                 stopSleeping;                                    // 0 for yes, 1 for no
     private int                 textColor;
     private int                 textBackground;
 
     private TextView            articleTextView;
     private DetectScrollView    articleScrollView;
     private Button              articleButtonUp;
     private Button              articleButtonDown;
     private TextView            articlePercent;
     private Article             myAricle;                                        // uset to get article text
     private Article             theGottenArticle;
     private Boolean             downloadBoolean;
     private AlertDialog.Builder addBookMarkDialog;
     private Bundle              mBundle;
     private String              novelName;
     private String              articleTitle;
     private int                 articleId = -1;
     private String              novelPic;
     private int                 novelId;
     private int                 yRate = -1;
     private int                 ariticlePosition = -1;
     private ArrayList<Integer>  articleIDs;
     // private ProgressDialog progressDialog= null;
     private AlertDialog.Builder aboutUsDialog;
     private final String        adWhirlKey  = "215f895eb71748e7ba4cb3a5f20b061e";
     private ActionBar           ab;
     private LinearLayout        layoutProgress;
     private int                 currentY    = 0;
     private int                 appTheme;
     private Boolean             booleanSend;
     private String              reportContent;
     private int                 articleNum = -1;
     private ArrayList<Integer>  articleNums;
 	private WebView             articleWebView;
 	private LinearLayout articleLayout;
 	private int articleAdType;
 
 	
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Setting.setApplicationActionBarTheme(this);
         setContentView(R.layout.layout_article);
 
         
         restorePreValues();
         setViews();
 
         ab = getSupportActionBar();
         mBundle = this.getIntent().getExtras();
         novelName = mBundle.getString("NovelName");
         novelPic = mBundle.getString("NovelPic");
         novelId = mBundle.getInt("NovelId");
         
         if(myAricle == null){
 	         articleTitle = mBundle.getString("ArticleTitle");
 	         articleId = mBundle.getInt("ArticleId");
 	         downloadBoolean = mBundle.getBoolean("ArticleDownloadBoolean", false);
 	         yRate = mBundle.getInt("ReadingRate", 0);
 	         articleIDs = mBundle.getIntegerArrayList("ArticleIDs");
 	         ariticlePosition = mBundle.getInt("ArticlePosition");
 	         articleNum = mBundle.getInt("ArticleNum");
 	         articleNums = mBundle.getIntegerArrayList("ArticleNums");
 	         if(savedInstanceState != null){
	        	 articleTitle = savedInstanceState.getString("ArticleTitle");
		         articleId = savedInstanceState.getInt("ArticleId");
		         downloadBoolean = savedInstanceState.getBoolean("ArticleDownloadBoolean", false);
		         yRate = savedInstanceState.getInt("ReadingRate", 0);
		         articleIDs = savedInstanceState.getIntegerArrayList("ArticleIDs");
		         ariticlePosition = savedInstanceState.getInt("ArticlePosition");
		         articleNum = savedInstanceState.getInt("ArticleNum");
		         articleNums = savedInstanceState.getIntegerArrayList("ArticleNums");
 	         }
 
 	  	    if (articleIDs != null) {
 	  	        if (downloadBoolean) {
 	  	            myAricle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", true, articleNums.get(ariticlePosition));
 	  	        } else {
 	  	            myAricle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", false, articleNums.get(ariticlePosition));
 	  	        }
 	  	    } else {
 	  	        if (downloadBoolean) {
 	  	            myAricle = new Article(articleId, novelId, "", articleTitle, "", true, articleNum);
 	  	        } else {
 	  	            myAricle = new Article(articleId, novelId, "", articleTitle, "", false, articleNum);
 	  	        }
 	  	    }
 	  	
 	  	    new DownloadArticleTask().execute();
 	  	    new UploadUserReadNovelTask().execute();
         }
         
         ab.setDisplayShowCustomEnabled(true);
         ab.setDisplayShowTitleEnabled(false);
 
         setActionBarTitle(articleTitle);
 
         // ab.setTitle(novelName);
         ab.setDisplayHomeAsUpEnabled(true);
 
         setAboutUsDialog();
     }
     
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
       NovelAPI.createRecentBookmark(new Bookmark(0, myAricle.getNovelId(), myAricle.getId(), yRate, novelName, myAricle.getTitle(), novelPic, true),
                 ArticleActivity.this);
       savedInstanceState.putString("ArticleTitle", myAricle.getTitle());
       savedInstanceState.putInt("ArticleId", myAricle.getId());
       savedInstanceState.putBoolean("ArticleDownloadBoolean", myAricle.isDownload());
       savedInstanceState.putInt("ReadingRate", yRate);
       savedInstanceState.putIntegerArrayList("ArticleIDs", articleIDs);
       savedInstanceState.putInt("ArticlePosition", ariticlePosition);
       savedInstanceState.putInt("ArticleNum", myAricle.getNum());
       savedInstanceState.putIntegerArrayList("ArticleNums", articleNums);
       super.onSaveInstanceState(savedInstanceState);
     }
 
     private void setActionBarTitle(String articleTitle) {
         LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View v = inflator.inflate(R.layout.item_title, null);
         TextView titleText = ((TextView) v.findViewById(R.id.title));
         titleText.setText(novelName + ":" + articleTitle);
         titleText.setSelected(true);
         int theme = Setting.getSetting(Setting.keyAppTheme, this);
         if (theme != Setting.initialAppTheme) {
             titleText.setTextColor(getResources().getColor(R.color.white));
             RelativeLayout bottom_buttons = (RelativeLayout) findViewById(R.id.bottom_buttons);
             bottom_buttons.setBackgroundColor(getResources().getColor(R.color.light_black));
             articlePercent.setTextColor(getResources().getColor(R.color.white));
         }
         ab.setCustomView(v);
     }
 
     private void restorePreValues() {
         textSize = Setting.getSetting(Setting.keyTextSize, ArticleActivity.this);
         textColor = Setting.getSetting(Setting.keyTextColor, ArticleActivity.this);
         textBackground = Setting.getSetting(Setting.keyTextBackground, ArticleActivity.this);
         textLanguage = Setting.getSetting(Setting.keyTextLanguage, ArticleActivity.this);
         readingDirection = Setting.getSetting(Setting.keyReadingDirection, ArticleActivity.this);
         clickToNextPage = Setting.getSetting(Setting.keyClickToNextPage, ArticleActivity.this);
         stopSleeping = Setting.getSetting(Setting.keyStopSleeping, ArticleActivity.this);
 
         if (readingDirection == 0) {
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         } else if (readingDirection == 1) {
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         }
 
         if (stopSleeping == 0) {
             ArticleActivity.this.findViewById(android.R.id.content).setKeepScreenOn(true);
         }
     }
 
     private void setViews() {
 
         layoutProgress = (LinearLayout) findViewById(R.id.layout_progress);
         articleTextView = (TextView) findViewById(R.id.article_text);
         articleWebView = (WebView)findViewById(R.id.article_webview);
         articleScrollView = (DetectScrollView) findViewById(R.id.article_scrollview);
         articleButtonUp = (Button) findViewById(R.id.article_button_up);
         articleButtonDown = (Button) findViewById(R.id.article_button_down);
         articlePercent = (TextView) findViewById(R.id.article_percent);
         articleLayout = (LinearLayout)findViewById(R.id.article_layout);
 
         articleScrollView.setScrollViewListener(ArticleActivity.this);
 
         articleTextView.setTextSize(textSize);
         articleTextView.setTextColor(textColor);
         if(textBackground!=Setting.initialTextBackground){
         	articleLayout.setBackgroundColor(textBackground);
         	layoutProgress.setBackgroundColor(textBackground);
         }
         	
         
         
         
         articleButtonUp.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
 
                 if (articleIDs != null) {
                     if (ariticlePosition != 0) {
                         ariticlePosition = ariticlePosition - 1;
                         if (downloadBoolean) {
                             myAricle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", true, articleNums.get(ariticlePosition));
                         } else {
                             myAricle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", false, articleNums.get(ariticlePosition));
                         }
                         new UpdateArticleTask().execute();
                     } else {
                         Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_up), Toast.LENGTH_SHORT).show();
                     }
                 } else {
                     new GetPreviousArticleTask().execute();
                 }
             }
         });
 
         articleButtonDown.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 if (articleIDs != null) {
                     if (ariticlePosition < articleIDs.size() - 1) {
                         ariticlePosition = ariticlePosition + 1;
                         if (downloadBoolean) {
                             myAricle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", true, articleNums.get(ariticlePosition));
                         } else {
                             myAricle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", false, articleNums.get(ariticlePosition));
                         }
                         new UpdateArticleTask().execute();
                     } else {
                         Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_down), Toast.LENGTH_SHORT).show();
                     }
                 } else {
                     new GetNextArticleTask().execute();
                 }
 
             }
         });
 
         if (clickToNextPage == 0) {
             articleTextView.setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View arg0) {
                     // new GetNextArticleTask().execute();
                     int kk = articleScrollView.getHeight();
                     int tt = articleTextView.getHeight();
                     articleScrollView.scrollTo(0, currentY + kk - 8);
                 }
             });
         }
 
         addBookMarkDialog = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.add_my_bookmark_title))
                 .setMessage(getResources().getString(R.string.add_my_bookmark_content)).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         NovelAPI.insertBookmark(
                                 new Bookmark(0, myAricle.getNovelId(), myAricle.getId(), yRate, novelName, myAricle.getTitle(), novelPic, false),
                                 ArticleActivity.this);
                     }
                 }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.cancel();
                     }
                 });
 
     }
 
     @Override
     public void onScrollChanged(DetectScrollView scrollView, int x, int y, int oldx, int oldy) {
         int kk = articleScrollView.getHeight();
         int tt = articleTextView.getHeight();
 
         currentY = y;
         yRate = (int) (((double) (y) / (double) (tt)) * 100);
         int xx = (int) (((double) (y + kk) / (double) (tt)) * 100);
         if (xx > 100)
             xx = 100;
         String yPositon = Integer.toString(xx);
         articlePercent.setText(yPositon + "%");
     }
 
     public static int dip2px(Context context, float dpValue) {
         final float scale = context.getResources().getDisplayMetrics().density;
         return (int) (dpValue * scale + 0.5f);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         // getMenuInflater().inflate(R.menu.activity_main, menu);
 
         menu.add(0, ID_SETTING, 0, getResources().getString(R.string.menu_settings)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(0, ID_RESPONSE, 1, getResources().getString(R.string.menu_respond)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(0, ID_ABOUT_US, 2, getResources().getString(R.string.menu_aboutus)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(0, ID_GRADE, 3, getResources().getString(R.string.menu_recommend)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(0, ID_Bookmark, 5, getResources().getString(R.string.menu_add_bookmark)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         menu.add(0, ID_Report, 4, getResources().getString(R.string.menu_report)).setIcon(R.drawable.icon_report)
                 .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
         int itemId = item.getItemId();
         switch (itemId) {
         case android.R.id.home:
             finish();
             // Toast.makeText(this, "home pressed", Toast.LENGTH_LONG).show();
             break;
         case ID_SETTING: // setting
             Intent intent = new Intent(ArticleActivity.this, SettingActivity.class);
             startActivity(intent);
             break;
         case ID_RESPONSE: // response
             final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
             emailIntent.setType("plain/text");
             emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { getResources().getString(R.string.respond_mail_address) });
             emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.respond_mail_title));
             emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
             startActivity(Intent.createChooser(emailIntent, "Send mail..."));
             break;
         case ID_ABOUT_US:
             aboutUsDialog.show();
             break;
         case ID_GRADE:
             Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.recommend_url)));
             startActivity(browserIntent);
             break;
         case ID_Bookmark:
             addBookMarkDialog.show();
             break;
         case ID_Report:
         	Report.createReportDialog(this,novelName+"("+novelId+")",myAricle.getTitle()+"(Num:"+myAricle.getNum()+")");  	
             break;
         }
         return true;
     }
 
     
     private class UploadUserReadNovelTask extends AsyncTask{
 
 		@Override
 		protected Object doInBackground(Object... arg0) {
 			NovelAPI.sendNovel(myAricle.getId(), Settings.Secure.getString(ArticleActivity.this.getContentResolver(),Settings.Secure.ANDROID_ID));
 			return null;
 		}
     	
     }
 
     private class DownloadArticleTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             if (myAricle != null) {
                 theGottenArticle = NovelAPI.getArticle(myAricle, ArticleActivity.this);
                 if (theGottenArticle != null) {
                     myAricle = theGottenArticle;
                 }
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
 
             layoutProgress.setVisibility(View.GONE);
             setArticleText();
             myAricle.setNovelId(novelId);
 
             new GetLastPositionTask().execute();
             if(articleAdType == Setting.InterstitialAd)
             	AdViewUtil.requestInterstitialAd(ArticleActivity.this);
 
         }
     }
     
     private void setArticleText(){
     	if(myAricle.getText().indexOf("*&&$$*") > 0){
         	// this is img of text
         	articleScrollView.setVisibility(View.GONE);
         	articleWebView.setVisibility(View.VISIBLE);
         	String[] urls = myAricle.getText().split("\\*&&\\$\\$\\*");
         	String html = "<html><body>";
         	String imgString = "";
         	for(int i=0; i < urls.length ; i++){
         		imgString += "<img src=\""+urls[i]+"\"><br><br>";
         	}
         	html += imgString + "<br><br></body></html>";
         	String mime = "text/html";
         	String encoding = "utf-8";
         	articleWebView.getSettings().setSupportZoom(true);
             articleWebView.getSettings().setBuiltInZoomControls(true);
             articleWebView.getSettings().setLoadWithOverviewMode(true);
             articleWebView.getSettings().setUseWideViewPort(true);
             articleWebView.loadDataWithBaseURL(null, html, mime, encoding, null);
         }else{
         	articleScrollView.setVisibility(View.VISIBLE);
         	articleWebView.setVisibility(View.GONE);
             String text = "";
             if(textLanguage == 1){
             	try {
             	    text = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(myAricle.getText());
 	            }catch (IOException e) {
 	            	e.printStackTrace();
 	            }
             }else{
             	text = myAricle.getText();
             }
             articleTextView.setText(text+"\n");
         }
     }
 
     private class UpdateArticleTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             layoutProgress.setVisibility(View.VISIBLE);
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             if (myAricle != null) {
                 theGottenArticle = NovelAPI.getArticle(myAricle, ArticleActivity.this);
                 if (theGottenArticle != null) {
                     myAricle = theGottenArticle;
                 }
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
 
             super.onPostExecute(result);
             layoutProgress.setVisibility(View.GONE);
             if (theGottenArticle != null) {
             	setArticleText();
 
                 myAricle.setNovelId(novelId);
                 articleScrollView.scrollTo(0, 0);
                 setActionBarTitle(myAricle.getTitle());
                 articlePercent.setText("0%");
                 if(articleAdType == Setting.InterstitialAd)
                 	AdViewUtil.requestInterstitialAd(ArticleActivity.this);
 
             } else {
                 Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_data), Toast.LENGTH_SHORT).show();
             }
 
         }
     }
 
     private class GetPreviousArticleTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             layoutProgress.setVisibility(View.VISIBLE);
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             if (myAricle != null) {
                 theGottenArticle = NovelAPI.getPreviousArticle(myAricle, ArticleActivity.this);
                 if (theGottenArticle != null) {
                     myAricle = theGottenArticle;
                 }
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
 
             super.onPostExecute(result);
             layoutProgress.setVisibility(View.GONE);
             if (theGottenArticle != null) {
                 setArticleText();
 
                 myAricle.setNovelId(novelId);
                 articleScrollView.scrollTo(0, 0);
                 setActionBarTitle(myAricle.getTitle());
                 articlePercent.setText("0%");
                 if(articleAdType == Setting.InterstitialAd)
                 	AdViewUtil.requestInterstitialAd(ArticleActivity.this);
 
             } else {
                 Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_up), Toast.LENGTH_SHORT).show();
             }
 
         }
     }
 
     private class GetNextArticleTask extends AsyncTask {
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             layoutProgress.setVisibility(View.VISIBLE);
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             if (myAricle != null) {
                 theGottenArticle = NovelAPI.getNextArticle(myAricle, ArticleActivity.this);
                 if (theGottenArticle != null) {
                     myAricle = theGottenArticle;
                 }
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
             layoutProgress.setVisibility(View.GONE);
             if (theGottenArticle != null) {
             	setArticleText();
 
                 myAricle.setNovelId(novelId);
                 articleScrollView.scrollTo(0, 0);
                 setActionBarTitle(myAricle.getTitle());
                 articlePercent.setText("0%");
                 if(articleAdType == Setting.InterstitialAd)
                 	AdViewUtil.requestInterstitialAd(ArticleActivity.this);
             } else {
                 Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_down), Toast.LENGTH_SHORT).show();
             }
 
         }
     }
 
     private class GetLastPositionTask extends AsyncTask {
 
         @Override
         protected Object doInBackground(Object... params) {
 
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
 
             super.onPostExecute(result);
 
             int kk = articleScrollView.getHeight();
             int tt = articleTextView.getHeight();
 
             if (yRate == 0) {
                 int xx = (int) (((double) (kk) / (double) (tt)) * 100);
                 if (xx > 100)
                     xx = 100;
                 String yPositon = Integer.toString(xx);
                 articlePercent.setText(yPositon + "%");
                 articleScrollView.scrollTo(0, 0);
             } else {
                 String yPositon = Integer.toString(yRate);
                 articlePercent.setText(yPositon + "%");
                 currentY = yRate * tt / 100;
                 articleScrollView.scrollTo(0, currentY);
             }
 
         }
     }
 
     private void setAboutUsDialog() {
 
         aboutUsDialog = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.about_us_string)).setIcon(R.drawable.play_store_icon)
                 .setMessage(getResources().getString(R.string.about_us))
                 .setPositiveButton(getResources().getString(R.string.yes_string), new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
 
                     }
                 });
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         int originTextLan = textLanguage;
 
         textSize = Setting.getSetting(Setting.keyTextSize, ArticleActivity.this);
         textColor = Setting.getSetting(Setting.keyTextColor, ArticleActivity.this);
         textBackground = Setting.getSetting(Setting.keyTextBackground, ArticleActivity.this);
         textLanguage = Setting.getSetting(Setting.keyTextLanguage, ArticleActivity.this);
         readingDirection = Setting.getSetting(Setting.keyReadingDirection, ArticleActivity.this);
         clickToNextPage = Setting.getSetting(Setting.keyClickToNextPage, ArticleActivity.this);
         stopSleeping = Setting.getSetting(Setting.keyStopSleeping, ArticleActivity.this);
         // appTheme = Setting.getSetting(Setting.keyAppTheme, ArticleActivity.this);
 
         articleTextView.setTextSize(textSize);
         articleTextView.setTextColor(textColor);
         if(textBackground!=Setting.initialTextBackground){
         	articleLayout.setBackgroundColor(textBackground);
         	layoutProgress.setBackgroundColor(textBackground);
         }
 
         if (originTextLan != textLanguage) {
             if (textLanguage == 1) {
                 String text = "";
                 try {
                     text = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(myAricle.getTitle() + "\n\n" + myAricle.getText());
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 articleTextView.setText(text);
             } else {
                 articleTextView.setText(articleTitle + "\n\n" + myAricle.getText());
             }
         }
 
         if (readingDirection == 0) {
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         } else if (readingDirection == 1) {
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         }
 
         if (clickToNextPage == 0) {
             articleTextView.setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View arg0) {
                     // new GetNextArticleTask().execute();
                     int kk = articleScrollView.getHeight();
                     int tt = articleTextView.getHeight();
                     articleScrollView.scrollTo(0, currentY + kk - 8);
                 }
             });
         } else {
             articleTextView.setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View arg0) {
                     // do nothing
                 }
             });
         }
 
         if (stopSleeping == 0) {
             ArticleActivity.this.findViewById(android.R.id.content).setKeepScreenOn(true);
         }
         
         articleAdType = Setting.getSetting(Setting.keyArticleAdType, ArticleActivity.this);
         if(articleAdType == Setting.BannerAd){
         	((LinearLayout) findViewById(R.id.adonView)).setVisibility(View.VISIBLE);
         	AdViewUtil.setBannerAdView((LinearLayout) findViewById(R.id.adonView), this);
         }else
         	((LinearLayout) findViewById(R.id.adonView)).setVisibility(View.GONE);
 
     }
 
     @Override
     protected void onPause() {
         NovelAPI.createRecentBookmark(new Bookmark(0, myAricle.getNovelId(), myAricle.getId(), yRate, novelName, myAricle.getTitle(), novelPic, true),
                 ArticleActivity.this);
         super.onPause();
     }
 
     public void rotationHoriztion(int beganDegree, int endDegree, AdView view) {
         final float centerX = 320 / 2.0f;
         final float centerY = 48 / 2.0f;
         final float zDepth = -0.50f * view.getHeight();
 
         Rotate3dAnimation rotation = new Rotate3dAnimation(beganDegree, endDegree, centerX, centerY, zDepth, true);
         rotation.setDuration(1000);
         rotation.setInterpolator(new AccelerateInterpolator());
         rotation.setAnimationListener(new Animation.AnimationListener() {
             public void onAnimationStart(Animation animation) {
             }
 
             public void onAnimationEnd(Animation animation) {
             }
 
             public void onAnimationRepeat(Animation animation) {
             }
         });
         view.startAnimation(rotation);
     }
     
     @Override
     public void onStart() {
       super.onStart();
       EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onStop() {
       super.onStop();
       EasyTracker.getInstance().activityStop(this);
     }
 
 }
