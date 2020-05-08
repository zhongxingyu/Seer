 /*******************************************************************************
  * Copyright (c) 2012 rmateus.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  ******************************************************************************/
 package cm.aptoide.pt;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import org.holoeverywhere.app.Activity;
 import org.holoeverywhere.app.AlertDialog;
 import org.holoeverywhere.app.AlertDialog.Builder;
 import org.holoeverywhere.app.ProgressDialog;
 import org.holoeverywhere.widget.AdapterView;
 import org.holoeverywhere.widget.Button;
 import org.holoeverywhere.widget.CheckBox;
 import org.holoeverywhere.widget.LinearLayout;
 import org.holoeverywhere.widget.ProgressBar;
 import org.holoeverywhere.widget.Spinner;
 import org.holoeverywhere.widget.TextView;
 import org.holoeverywhere.widget.Toast;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.Dialog;
 import android.content.ActivityNotFoundException;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.RemoteException;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.support.v4.widget.CursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
 import android.text.SpannableString;
 import android.text.style.UnderlineSpan;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageView;
 import android.widget.RatingBar;
import cm.aptoide.com.actionbarsherlock.view.Menu;
import cm.aptoide.com.actionbarsherlock.view.MenuItem;
 import cm.aptoide.com.nostra13.universalimageloader.core.DisplayImageOptions;
 import cm.aptoide.com.nostra13.universalimageloader.core.ImageLoader;
 import cm.aptoide.com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
 import cm.aptoide.com.viewpagerindicator.CirclePageIndicator;
 import cm.aptoide.pt.adapters.ViewPagerAdapterScreenshots;
 import cm.aptoide.pt.contentloaders.SimpleCursorLoader;
 import cm.aptoide.pt.contentloaders.ViewApkLoader;
 import cm.aptoide.pt.services.AIDLServiceDownloadManager;
 import cm.aptoide.pt.services.ServiceDownloadManager;
 import cm.aptoide.pt.util.NetworkUtils;
 import cm.aptoide.pt.util.RepoUtils;
 import cm.aptoide.pt.util.quickaction.ActionItem;
 import cm.aptoide.pt.util.quickaction.EnumQuickActions;
 import cm.aptoide.pt.util.quickaction.QuickAction;
 import cm.aptoide.pt.views.EnumApkMalware;
 import cm.aptoide.pt.views.EnumDownloadFailReason;
 import cm.aptoide.pt.views.EnumDownloadStatus;
 import cm.aptoide.pt.views.ViewApk;
 import cm.aptoide.pt.views.ViewCache;
 import cm.aptoide.pt.views.ViewDownload;
 import cm.aptoide.pt.views.ViewDownloadManagement;
 import cm.aptoide.pt.webservices.MalwareStatus;
 import cm.aptoide.pt.webservices.TasteModel;
 import cm.aptoide.pt.webservices.WebserviceGetApkInfo;
 import cm.aptoide.pt.webservices.comments.AddComment;
 import cm.aptoide.pt.webservices.comments.Comment;
 import cm.aptoide.pt.webservices.comments.ViewComments;
 import cm.aptoide.pt.webservices.login.Login;
 import cm.aptoide.pt.webservices.taste.EnumUserTaste;
 import cm.aptoide.pt.webservices.taste.Likes;
 
import com.mopub.mobileads.MoPubView;
 
 
 
public class ApkInfo extends Activity implements LoaderCallbacks<Cursor> {
 
 
 	private MoPubView mAdView;
 
     private ViewApk viewApk = null;
     private Database db;
     private Spinner spinner;
     SimpleCursorAdapter adapter;
     long id;
     Category category;
     Activity context;
     boolean spinnerInstanciated = false;
     CheckBox scheduledDownloadChBox;
     private ViewDownloadManagement download;
 
 
     private boolean isRunning = false;
 
     private AIDLServiceDownloadManager serviceDownloadManager = null;
 
     private boolean serviceManagerIsBound = false;
 
     private ServiceConnection serviceManagerConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service) {
             // This is called when the connection with the service has been
             // established, giving us the object we can use to
             // interact with the service.  We are communicating with the
             // service using AIDL, so here we set the remote service interface.
             serviceDownloadManager = AIDLServiceDownloadManager.Stub.asInterface(service);
             serviceManagerIsBound = true;
 
             Log.v("Aptoide-ApkInfo", "Connected to ServiceDownloadManager");
 
             continueLoading();
         }
 
         public void onServiceDisconnected(ComponentName className) {
             // This is called when the connection with the service has been
             // unexpectedly disconnected -- that is, its process crashed.
             serviceManagerIsBound = false;
             serviceDownloadManager = null;
 
             Log.v("Aptoide-ApkInfo", "Disconnected from ServiceDownloadManager");
         }
     };
 
     private AIDLDownloadObserver.Stub serviceDownloadManagerCallback = new AIDLDownloadObserver.Stub() {
         @Override
         public void updateDownloadStatus(ViewDownload update) throws RemoteException {
             download.updateProgress(update);
             handler.sendEmptyMessage(update.getStatus().ordinal());
         }
     };
 
     private ViewGroup viewLikes;
     private ViewGroup viewLikesButton;
     private View loading;
     private String installString;
     private boolean unstrustedPayment = false;
 
 
     @Override
     protected void onCreate(Bundle arg0) {
         AptoideThemePicker.setAptoideTheme(this);
         super.onCreate(arg0);
         setContentView(R.layout.app_info);
 
 //		getSupportActionBar().setIcon(R.drawable.brand_padding);
 //		getSupportActionBar().setTitle("");
 //		getSupportActionBar().setHomeButtonEnabled(true);
 //		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
         if(!isRunning){
             isRunning = true;
 
             if(!serviceManagerIsBound){
                 bindService(new Intent(this, ServiceDownloadManager.class), serviceManagerConnection, Context.BIND_AUTO_CREATE);
             }else{
                 continueLoading();
             }
 
         }
 
     }
 
     /**
      *
      */
     protected void continueLoading() {
         category = Category.values()[getIntent().getIntExtra("category", -1)];
         context = this;
         pd = new ProgressDialog(context);
         db = Database.getInstance();
         id = getIntent().getExtras().getLong("_id");
         loadElements(id);
     }
 
     /**
      *
      */
     private void loadApkVersions() {
         if(category.equals(Category.INFOXML)){
             spinner = (Spinner) findViewById(R.id.spinnerMultiVersion);
             adapter = new SimpleCursorAdapter(this,
             		cm.aptoide.pt.R.layout.simple_spinner_item, null,
                     new String[] { "vername" ,"repo_id"}, new int[] { android.R.id.text1 },
                     CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
             adapter.setViewBinder(new ViewBinder() {
 
                 @Override
                 public boolean setViewValue(View textView, Cursor cursor, int position) {
                     ((android.widget.TextView) textView).setText(getString(R.string.version)+" " + cursor.getString(position) +" - "+RepoUtils.split(db.getServer(cursor.getLong(3),false).url));
                     return true;
                 }
             });
             adapter.setDropDownViewResource(cm.aptoide.pt.R.layout.simple_spinner_dropdown_item);
             spinner.setAdapter(adapter);
             spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
                 @Override
                 public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                     if (spinnerInstanciated) {
                         if(!download.isNull()){
                             try {
                                 serviceDownloadManager.callUnregisterDownloadObserver(viewApk.hashCode());
                             } catch (RemoteException e) {
                                 e.printStackTrace();
                             }
                         }
                         loadElements(arg3);
                     } else {
                         spinnerInstanciated = true;
                     }
                 }
 
                 @Override
                 public void onNothingSelected(AdapterView<?> arg0) {
 
                 }
             });
 
             getSupportLoaderManager().initLoader(0, null, ApkInfo.this);
 
         }
     }
 
 
 
     String webservicespath= null;
     Likes likes;
     String repo_string;
     ProgressDialog pd;
 
     private OnClickListener openListener = new OnClickListener() {
 
         @Override
         public void onClick(View v) {
             try{
                 Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(viewApk.getApkid());
                 startActivity(LaunchIntent);
             } catch (Exception e) {
                 e.printStackTrace();
                 Toast.makeText(context, R.string.no_launcher_activity, Toast.LENGTH_LONG).show();
             }
 
         }
     };
     @SuppressLint("NewApi")
     private void loadElements(long id) {
         viewComments = (ViewGroup) findViewById(R.id.commentContainer);
         viewComments.removeAllViews();
         viewLikes = (ViewGroup) findViewById(R.id.likesLayout);
         loading = LayoutInflater.from(context).inflate(R.layout.loadingfootercomments, null);
         viewComments.addView(loading, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
         installString = getString(R.string.install);
         viewLikesButton = (ViewGroup) findViewById(R.id.ratings);
         ((TextView) viewLikes.findViewById(R.id.likes)).setText(context.getString(R.string.loading_likes));
         ((TextView) viewLikes.findViewById(R.id.dislikes)).setText("");
 
 
 
 
         findViewById(R.id.downloading_icon).setVisibility(View.GONE);
         findViewById(R.id.downloading_name).setVisibility(View.GONE);
         findViewById(R.id.download_progress).setVisibility(View.GONE);
         ProgressBar progress = (ProgressBar) findViewById(R.id.downloading_progress);
         progress.setIndeterminate(true);
         Bundle b = new Bundle();
         b.putLong("_id", id);
 
 //		findViewById(R.id.inst_version).setVisibility(View.VISIBLE);
         getSupportLoaderManager().restartLoader(20, b, new LoaderCallbacks<ViewApk>() {
 
 			@Override
             public Loader<ViewApk> onCreateLoader(int arg0, final Bundle arg1) {
                 pd.show();
                 pd.setMessage(getString(R.string.please_wait));
                 pd.setCancelable(false);
                 return new ViewApkLoader(ApkInfo.this) {
 
                     @Override
                     public ViewApk loadInBackground() {
                         return db.getApk(arg1.getLong("_id"), category);
                     }
                 };
 
             }
             @Override
             public void onLoadFinished(Loader<ViewApk> arg0, ViewApk arg1) {
 //                AdView adView = (AdView)findViewById(R.id.adView);
 //                adView.loadAd(new AdRequest());
 
                 mAdView = (MoPubView) findViewById(R.id.adview);
                 mAdView.setAdUnitId("18947d9a99e511e295fa123138070049"); // Enter your Ad Unit ID from www.mopub.com
                 mAdView.loadAd();
                 pd.dismiss();
                 viewApk = arg1;
                 new Thread(new Runnable() {
                     @Override
                     public void run() {
                         loadDescription();
                     }
                 }).start();
 
                 if(viewApk.getLikes()!=-1){
                     setLikes(viewApk.getLikes()+"", viewApk.getDislikes()+"");
                 }
 
                 if(viewApk.getComments().size()>0){
                     setComments(viewApk.getComments());
                     loading.setVisibility(View.GONE);
                 }
 
                 loadScreenshots();
 
                 //viewApk.getWebservicePath
 
                 int installedVercode = db.getInstalledAppVercode(viewApk.getApkid());
 
                 if(installedVercode<=viewApk.getVercode()&&installedVercode!=0){
                     findViewById(R.id.inst_version).setVisibility(View.VISIBLE);
                     ((TextView) findViewById(R.id.inst_version)).setText(getString(R.string.installed_version)+": " + db.getInstalledAppVername(viewApk.getApkid()));
                     if(installedVercode<viewApk.getVercode()&&!getIntent().hasExtra("installed")){
                         installString = getString(R.string.update);
                     }else if(getIntent().hasExtra("installed")){
                         installString = getString(R.string.open);
                     }
                     ((Button) findViewById(R.id.btinstall)).setText(installString);
 
                 }else if(installedVercode>viewApk.getVercode()){
                     if(getIntent().hasExtra("installed")){
                         installString = getString(R.string.open);
                     }else{
                         installString = getString(R.string.install);
 
                     }
                     ((Button) findViewById(R.id.btinstall)).setText(installString);
 
                     findViewById(R.id.inst_version).setVisibility(View.GONE);
                 }
 
                 if(installedVercode==viewApk.getVercode()){
                     if(getIntent().hasExtra("installed")){
                         installString = getString(R.string.open);
                     }else{
                         installString = getString(R.string.install);
                     }
                     ((Button) findViewById(R.id.btinstall)).setText(installString);
                     findViewById(R.id.inst_version).setVisibility(View.GONE);
                 }
 
 
                 repo_string = viewApk.getRepoName();
                 checkDownloadStatus();
                 webservicespath = viewApk.getWebservicesPath();
 
                 try {
                     ((RatingBar) findViewById(R.id.ratingbar)).setRating(Float.parseFloat(viewApk.getRating()));
                     ((RatingBar) findViewById(R.id.ratingbar)).setIsIndicator(true);
                 } catch (Exception e) {
                     ((RatingBar) findViewById(R.id.ratingbar)).setRating(0);
                     ((RatingBar) findViewById(R.id.ratingbar)).setIsIndicator(true);
                 }
                 ((TextView) findViewById(R.id.app_store)).setText(getString(R.string.store)+": " +repo_string);
                 ((TextView) findViewById(R.id.versionInfo)).setText(getString(R.string.clear_dwn_title) + " " + viewApk.getDownloads() + " "+ getString(R.string.size)+" "+ viewApk.getSize() + "KB");
                 ((TextView) findViewById(R.id.version_label)).setText(getString(R.string.version) + " "+ viewApk.getVername());
                 ((TextView) findViewById(R.id.app_name)).setText(viewApk.getName());
 //				ImageLoader imageLoader = ImageLoader.getInstance(context);
 //				imageLoader.DisplayImage(viewApk.getIcon(),(ImageView) findViewById(R.id.app_icon), context, (viewApk.getApkid()+"|"+viewApk.getVercode()));
                 DisplayImageOptions options = new DisplayImageOptions.Builder()
                         .displayer(new FadeInBitmapDisplayer(1000))
                         .showStubImage(android.R.drawable.sym_def_app_icon)
                         .resetViewBeforeLoading()
                         .cacheInMemory()
                         .cacheOnDisc()
                         .build();
                 ImageLoader.getInstance().displayImage(viewApk.getIcon(), (ImageView) findViewById(R.id.app_icon), options, null, (viewApk.getApkid()+"|"+viewApk.getVercode()));
 
 
                 new GetApkInfo().execute();
 
 
 				/*Comments comments = new Comments(context,webservicespath);
 				comments.getComments(repo_string, viewApk.getApkid(),viewApk.getVername(),(LinearLayout) findViewById(R.id.commentContainer), false);*/
                 likes = new Likes(context, webservicespath);
 				/*likes.getLikes(repo_string, viewApk.getApkid(), viewApk.getVername(),(ViewGroup) findViewById(R.id.likesLayout),(ViewGroup) findViewById(R.id.ratings));*/
 
                 ItemBasedApks items = new ItemBasedApks(context,viewApk);
                 items.getItems((LinearLayout) findViewById(R.id.itembasedapks_container),(LinearLayout)findViewById(R.id.itembasedapks_maincontainer),(TextView)findViewById(R.id.itembasedapks_label));
 
                 if(!spinnerInstanciated){
                     loadApkVersions();
                 }
                 setClickListeners();
 
                 //Malware badges
                 loadMalwareBadges();
                 if (Build.VERSION.SDK_INT >= 11)
                 {
                     invalidateOptionsMenu();
                 }
 
 //                if(!getIntent().hasExtra("installed")){
 //                    new checkPaymentTask().execute();
 //                }
 
 
 
 
             }
 
             private void loadMalwareBadges() {
                 new Thread(new Runnable() {
 
                     @Override
                     public void run() {
                         try{
                             loadMalware(viewApk.getMalwareStatus());
                         }catch (Exception e){
                             e.printStackTrace();
                         }
                     }
                 }).start();
 
             }
 
             @Override
             public void onLoaderReset(Loader<ViewApk> arg0) {
 
             }
         });
 
 
         //
 
         //
 
 
         //		Button serch_mrkt = (Button)findViewById(R.id.btmarket);
         //		serch_mrkt.setOnClickListener(new OnClickListener() {
         //
         //			public void onClick(View v) {
 
         //			}
         //
         //		});
 
         //
 
 
 
         //
         //
 
 
     }
 
 
     public void loadMalware(final MalwareStatus malwareStatus){
     	runOnUiThread(new Runnable() {
 
     		@Override
     		public void run() {
     			try{
     				EnumApkMalware apkStatus = EnumApkMalware.valueOf(malwareStatus.getStatus().toUpperCase(Locale.ENGLISH));
     				Log.d("ApkInfoMalware-malwareStatus", malwareStatus.getStatus());
     				Log.d("ApkInfoMalware-malwareReason", malwareStatus.getReason());
 
     				switch(apkStatus){
     				case SCANNED:
     					((TextView) findViewById(R.id.app_badge_text)).setText(getString(R.string.trusted));
     					((ImageView) findViewById(R.id.app_badge)).setImageResource(R.drawable.badge_scanned);
     					((LinearLayout) findViewById(R.id.badge_layout)).setOnClickListener(new OnClickListener() {
     						@Override
     						public void onClick(View v) {
     							View trustedView = LayoutInflater.from(ApkInfo.this).inflate(R.layout.dialog_anti_malware, null);
     							Builder dialogBuilder = new AlertDialog.Builder(ApkInfo.this).setView(trustedView);
     							final AlertDialog trustedDialog = dialogBuilder.create();
     							trustedDialog.setIcon(R.drawable.badge_scanned);
     							trustedDialog.setTitle(getString(R.string.app_trusted, viewApk.getName()));
     							trustedDialog.setCancelable(true);
 
     							TextView tvSignatureValidation = (TextView) trustedView.findViewById(R.id.tv_signature_validation);
     							tvSignatureValidation.setText(getString(R.string.signature_verified));
     							ImageView check_signature = (ImageView) trustedView.findViewById(R.id.check_signature);
     							check_signature.setImageResource(R.drawable.yes);
 
     							trustedDialog.setButton(Dialog.BUTTON_NEUTRAL, "Ok", new Dialog.OnClickListener() {
     								@Override
     								public void onClick(DialogInterface arg0, int arg1) {
     									trustedDialog.dismiss();
     								}
     							});
     							trustedDialog.show();
     						}
     					});
     					break;
     					//    			case UNKNOWN:
     					//    				((TextView) findViewById(R.id.app_badge_text)).setText(getString(R.string.unknown));
     					//    				((ImageView) findViewById(R.id.app_badge)).setImageResource(R.drawable.badge_unknown);
     					//    				break;
     				case WARN:
     					((TextView) findViewById(R.id.app_badge_text)).setText(getString(R.string.warning));
     					((ImageView) findViewById(R.id.app_badge)).setImageResource(R.drawable.badge_warn);
     					((LinearLayout) findViewById(R.id.badge_layout)).setOnClickListener(new OnClickListener() {
     						@Override
     						public void onClick(View v) {
     							View warnView = LayoutInflater.from(ApkInfo.this).inflate(R.layout.dialog_anti_malware, null);
     							Builder dialogBuilder = new AlertDialog.Builder(ApkInfo.this).setView(warnView);
     							final AlertDialog warnDialog = dialogBuilder.create();
     							warnDialog.setIcon(R.drawable.badge_warn);
     							warnDialog.setTitle(getString(R.string.app_warning, viewApk.getName()));
     							warnDialog.setCancelable(true);
 
     							TextView tvSignatureValidation = (TextView) warnView.findViewById(R.id.tv_signature_validation);
     							tvSignatureValidation.setText(getString(R.string.signature_not_verified));
     							ImageView check_signature = (ImageView) warnView.findViewById(R.id.check_signature);
     							check_signature.setImageResource(R.drawable.failed);
 
     							warnDialog.setButton(Dialog.BUTTON_NEUTRAL, "Ok", new Dialog.OnClickListener() {
     								@Override
     								public void onClick(DialogInterface arg0, int arg1) {
     									warnDialog.dismiss();
     								}
     							});
     							warnDialog.show();
     						}
     					});
     					break;
     					//    			case CRITICAL:
     					//    				((TextView) findViewById(R.id.app_badge_text)).setText(getString(R.string.critical));
     					//    				((ImageView) findViewById(R.id.app_badge)).setImageResource(R.drawable.badge_critical);
     					//    				break;
     				default:
     					break;
     				}
     			}catch (Exception e) {
     				e.printStackTrace();
     			}
     		}
 
     	});
 
     }
 
     /* (non-Javadoc)
      * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
      */
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         menu.clear();
         if(viewApk!=null){
             if(db.getInstalledAppVercode(viewApk.getApkid())!=0){
                 menu.add(0,0,0,R.string.uninstall).setIcon(android.R.drawable.ic_delete);
             }
             menu.add(0,1,0,R.string.search_market).setIcon(android.R.drawable.ic_menu_add);
         }
 
         return super.onPrepareOptionsMenu(menu);
     }
 
     /* (non-Javadoc)
      * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case 0:
                 Uri uri = Uri.fromParts("package", viewApk.getApkid(), null);
                 Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                 startActivity(intent);
                 finish();
                 break;
             case 1:
                 Intent i = new Intent();
                 i.setAction(android.content.Intent.ACTION_VIEW);
                 i.setData(Uri.parse("market://details?id="+viewApk.getApkid()));
                 try{
                     startActivity(i);
                 }catch (ActivityNotFoundException e){
                     Toast toast= Toast.makeText(context, context.getString(R.string.error_no_market), Toast.LENGTH_SHORT);
                     toast.show();
                 }
                 break;
             default:
                 break;
         }
         if (item.getItemId() == android.R.id.home) {
             finish();
             return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     /**
      *
      */
     private void loadScreenshots() {
         new Thread(new Runnable() {
 
             @Override
             public void run() {
                 try{
 
                     final ArrayList<String> originalList = viewApk.getScreenshots();
 //					switch (category) {
 //					case TOP:
 //					case LATEST:
 //					case ITEMBASED:
 //					case EDITORSCHOICE:
 //					case TOPFEATURED:
 //					case USERBASED:
 //						db.getScreenshots(originalList,viewApk,category);
 //						thumbnailList = new String[originalList.size()];
 //
 //						for ( int i = 0; i!= originalList.size();i++){
 //							thumbnailList[i]=screenshotToThumb(originalList.get(i));
 //						}
 //
 //						break;
 //
 //					case INFOXML:
 //                        thumbnailList = new String[array.length()];
 //						for ( int i = 0; i!= array.length();i++){
 //							thumbnailList[i]=screenshotToThumb(array.getString(i));
 //						}
 //
 //						for(int i=0;i < array.length();i++){
 //							originalList.add(array.getString(i));
 //						}
 //						break;
 //
 //					default:
 //						break;
 //					}
                     final CirclePageIndicator pi = (CirclePageIndicator) findViewById(R.id.indicator);
                     final CustomViewPager screenshots = (CustomViewPager) findViewById(R.id.screenShotsPager);
 
 
 
                     runOnUiThread(new Runnable() {
 
                         public void run() {
                             if(originalList!=null&&originalList.size()>0){
                                 String hashCode = (viewApk.getApkid()+"|"+viewApk.getVercode());
                                 screenshots.setAdapter(new ViewPagerAdapterScreenshots(context,originalList,hashCode,false));
                                 TypedValue a = new TypedValue();
                                 getTheme().resolveAttribute(R.attr.custom_color, a, true);
                                 pi.setFillColor(a.data);
                                 pi.setViewPager(screenshots);
                                 pi.setRadius(4.5f);
                                 findViewById(R.id.screenshots_container).setVisibility(View.VISIBLE);
                                 findViewById(R.id.screenshots_label).setVisibility(View.VISIBLE);
                                 if(originalList.size()==1){
                                     findViewById(R.id.right).setVisibility(View.GONE);
                                 }
                                 pi.setOnPageChangeListener(new OnPageChangeListener() {
 
                                     public void onPageSelected(int position) {
 
                                         findViewById(R.id.left).setVisibility(View.VISIBLE);
                                         findViewById(R.id.right).setVisibility(View.VISIBLE);
 
                                         if(position==0){
                                             findViewById(R.id.left).setVisibility(View.GONE);
                                         }
                                         if (position==originalList.size()-1){
                                             findViewById(R.id.right).setVisibility(View.GONE);
                                         }
                                     }
 
                                     public void onPageScrolled(int arg0, float arg1, int arg2) {
 
                                     }
 
                                     public void onPageScrollStateChanged(int arg0) {
 
                                     }
                                 });
                             }
 
                         }
                     });
 
 
                 } catch (Exception e){
                     e.printStackTrace();
                 }
 
             }
 
 
         }).start();
     }
 
     private void loadDescription() {
         Cursor c = getContentResolver().query(ExtrasContentProvider.CONTENT_URI, new String[]{ExtrasDbOpenHelper.COLUMN_COMMENTS_COMMENT}, ExtrasDbOpenHelper.COLUMN_COMMENTS_APKID+"=?", new String[]{viewApk.getApkid()}, null);
 
         description_text = "";
         if(c.moveToFirst()){
             description_text = c.getString(0);
         }else{
             description_text=getString(R.string.no_descript);
         }
 
         c.close();
 
 
         runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
 
                 final TextView description = (TextView) findViewById(R.id.descript);
                 description.setText(description_text);
                 if(description.getLineCount()>10){
                     description.setMaxLines(10);
                     findViewById(R.id.show_all_description).setVisibility(View.VISIBLE);
                     findViewById(R.id.show_all_description).setOnClickListener(new OnClickListener() {
 
 
                         @Override
                         public void onClick(View v) {
 
                             if(collapsed){
                                 collapsed=false;
                                 scrollPosition = findViewById(R.id.app_info_scroller).getScrollY();
                                 description.setMaxLines(Integer.MAX_VALUE);
                                 ((TextView)findViewById(R.id.show_all_description)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_more_arrow_up, 0);
                                 ((TextView) findViewById(R.id.show_all_description)).setText(getString(R.string.show_less));
                             }else{
                                 collapsed=true;
                                 ((TextView)findViewById(R.id.show_all_description)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_more_arrow_down, 0);
                                 description.setMaxLines(10);
                                 findViewById(R.id.app_info_scroller).scrollTo(0, scrollPosition);
                                 ((TextView) findViewById(R.id.show_all_description)).setText(getString(R.string.show_more));
                             }
                         }
                     });
                     findViewById(R.id.description_container).setOnClickListener(new OnClickListener() {
 
 
                         @Override
                         public void onClick(View v) {
 
                             if(collapsed){
                                 collapsed=false;
                                 scrollPosition = findViewById(R.id.app_info_scroller).getScrollY();
                                 description.setMaxLines(Integer.MAX_VALUE);
                                 ((TextView)findViewById(R.id.show_all_description)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_more_arrow_up, 0);
                                 ((TextView) findViewById(R.id.show_all_description)).setText(getString(R.string.show_less));
                             }else{
                                 collapsed=true;
                                 ((TextView)findViewById(R.id.show_all_description)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_more_arrow_down, 0);
                                 description.setMaxLines(10);
                                 findViewById(R.id.app_info_scroller).scrollTo(0, scrollPosition);
                                 ((TextView) findViewById(R.id.show_all_description)).setText(getString(R.string.show_more));
                             }
                         }
                     });
                 }
             }
 
 
         });
     }
 
 
     /**
      *
      */
     private void setClickListeners() {
         if(getIntent().hasExtra("installed")){
             findViewById(R.id.btinstall).setOnClickListener(openListener );
         }else if(getIntent().hasExtra("updates")){
             findViewById(R.id.btinstall).setOnClickListener(installListener);
         }else{
             findViewById(R.id.btinstall).setOnClickListener(installListener);
         }
 
         findViewById(R.id.add_comment).setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 Intent i = new Intent(ApkInfo.this,AddComment.class);
                 i.putExtra("apkid", viewApk.getApkid());
                 i.putExtra("version", viewApk.getVername());
                 i.putExtra("repo", repo_string);
                 i.putExtra("webservicespath",  "http://webservices.aptoide.com/");
                 startActivityForResult(i, AddComment.ADD_COMMENT_REQUESTCODE);
             }
         });
 
         findViewById(R.id.likesImage).setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 postLike(EnumUserTaste.LIKE,repo_string);
 
             }
         });
 
         findViewById(R.id.dislikesImage).setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 postLike(EnumUserTaste.DISLIKE,repo_string);
 
             }
         });
 
 
 
         findViewById(R.id.more_comments).setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 Intent i = new Intent(ApkInfo.this,ViewComments.class);
                 i.putExtra("repo", repo_string);
                 i.putExtra("apkid", viewApk.getApkid());
                 i.putExtra("vername", viewApk.getVername());
                 i.putExtra("webservicespath", "http://webservices.aptoide.com/");
                 startActivity(i);
 
             }
         });
 
         scheduledDownloadChBox = (CheckBox) findViewById(R.id.schedule_download_box);
         scheduledDownloadChBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 if(isChecked){
                     ((Button) findViewById(R.id.btinstall)).setText(R.string.schDwnBtn);
                 }else{
                     ((Button) findViewById(R.id.btinstall)).setText(installString);
                 }
             }
         });
 
     }
 
     OnClickListener installListener = new OnClickListener() {
 
         @Override
         public void onClick(View v) {
             findViewById(R.id.btinstall).setOnClickListener(null);
             new Thread(new Runnable() {
                 public void run() {
                     if(scheduledDownloadChBox.isChecked()){
                         db.insertScheduledDownload(viewApk.getApkid(), viewApk.getVercode(), viewApk.getVername(), viewApk.getPath(),viewApk.getName(),viewApk.getMd5(),viewApk.getIcon());
                         runOnUiThread(new Runnable() {
 
                             @Override
                             public void run() {
                                 Toast toast= Toast.makeText(context, context.getString(R.string.addSchDown), Toast.LENGTH_SHORT);
                                 toast.show();
                             }
                         });
                     }else{
                         ViewCache cache = new ViewCache(viewApk.hashCode(), viewApk.getMd5(),viewApk.getApkid(),viewApk.getVername());
                         if(cache.isCached() && cache.hasMd5Sum() && cache.checkMd5()){
                             try {
                                 serviceDownloadManager.callInstallApp(cache);
                             } catch (RemoteException e) {
                                 e.printStackTrace();
                             }
                         }else{
                             if(category.equals(Category.ITEMBASED)||category.equals(Category.TOP)||category.equals(Category.TOPFEATURED)||category.equals(Category.EDITORSCHOICE)){
                                 download = new ViewDownloadManagement(
                                         viewApk.getPath(),
                                         viewApk,
                                         cache);
                             }else{
                                 download = new ViewDownloadManagement(
                                         viewApk.getPath(),
                                         viewApk,
                                         cache,
                                         db.getServer(viewApk.getRepo_id(), false).getLogin());
                             }
                             
                              
 
 
                             runOnUiThread(new Runnable() {
 
                                 @Override
                                 public void run() {
                                     ImageView manage = (ImageView) findViewById(R.id.icon_manage);
 ////                                    manage.setVisibility(View.GONE);
                                     manage.setOnClickListener(new OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             setupQuickActions(view);
                                         }
                                     });
                                     findViewById(R.id.download_progress).setVisibility(View.VISIBLE);
 //                                    findViewById(R.id.icon_manage).setVisibility(View.VISIBLE);
                                     findViewById(R.id.downloading_name).setVisibility(View.INVISIBLE);
                                 }
                             });
 
                             try {
                                 serviceDownloadManager.callStartDownloadAndObserve(download,serviceDownloadManagerCallback);
                             } catch (RemoteException e) {
                                 e.printStackTrace();
                             }
 
 
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     findViewById(R.id.btinstall).setOnClickListener(installListener);
                                 }
                             });
 
                         }
                     }
                 }
             }).start();
 
         }
     };
 
     /**
      *
      */
     private void checkDownloadStatus() {
         try {
             download = serviceDownloadManager.callGetAppDownloading(viewApk.hashCode());
         } catch (RemoteException e1) {
             e1.printStackTrace();
         }
 
         Log.d("Aptoide-ApkInfo", "getAppDownloading: "+download);
 
         if(!download.isNull()){
             ImageView manage = (ImageView) findViewById(R.id.icon_manage);
 //            manage.setVisibility(View.GONE);
             manage.setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     setupQuickActions(view);
                 }
             });
             try {
                 serviceDownloadManager.callRegisterDownloadObserver(viewApk.hashCode(), serviceDownloadManagerCallback);
             } catch (RemoteException e) {
                 e.printStackTrace();
             }
             findViewById(R.id.download_progress).setVisibility(View.VISIBLE);
 //            findViewById(R.id.icon_manage).setVisibility(View.VISIBLE);
             findViewById(R.id.downloading_name).setVisibility(View.INVISIBLE);
             ((ProgressBar) findViewById(R.id.downloading_progress)).setProgress(download.getProgress());
             ((TextView) findViewById(R.id.speed)).setText(download.getSpeedInKBpsString(this));
             ((TextView) findViewById(R.id.speed)).setTextColor(Color.WHITE);
             ((TextView) findViewById(R.id.progress)).setText(download.getProgressString());
             ((TextView) findViewById(R.id.progress)).setTextColor(Color.WHITE);
         }
     }
 
 
     String description_text;
     private boolean collapsed = true;
     int scrollPosition = 0;
 
     @Override
     protected void onActivityResult(int arg0, int arg1, Intent arg2) {
         super.onActivityResult(arg0, arg1, arg2);
         loadElements(id);
     }
 
     protected void postLike(EnumUserTaste like, String repo_string) {
         if(Login.isLoggedIn(this)){
 
             try{
                 likes.postLike(repo_string, viewApk.getApkid(), viewApk.getVername(), like, (ViewGroup) findViewById(R.id.ratings));
             }catch(Exception e){
                 e.printStackTrace();
             }
 
         }else{
             Intent i = new Intent(this,Login.class);
             startActivityForResult(i, Login.REQUESTCODE);
         }
     }
 
 
     private Handler handler = new Handler(){
 
         public void handleMessage(Message msg) {
             Log.d("Aptoide-ApkInfo", "download status update: "+EnumDownloadStatus.reverseOrdinal(msg.what).name());
             ProgressBar progress;
             switch (EnumDownloadStatus.reverseOrdinal(msg.what)) {
                 case PAUSED:
                     progress = (ProgressBar) findViewById(R.id.downloading_progress);
                     progress.setIndeterminate(false);
                     progress.setProgress(download.getProgress());
                     ((TextView) findViewById(R.id.speed)).setText(download.getSpeedInKBpsString(ApkInfo.this));
                     ((TextView) findViewById(R.id.speed)).setTextColor(Color.WHITE);
                     ((TextView) findViewById(R.id.progress)).setText(download.getProgressString());
                     ((TextView) findViewById(R.id.progress)).setTextColor(Color.WHITE);
                     break;
 
                 case RESUMING:
                     progress = (ProgressBar) findViewById(R.id.downloading_progress);
                     progress.setIndeterminate(false);
                     progress.setProgress(download.getProgress());
                     ((TextView) findViewById(R.id.speed)).setText(download.getSpeedInKBpsString(ApkInfo.this));
                     ((TextView) findViewById(R.id.speed)).setTextColor(Color.WHITE);
                     ((TextView) findViewById(R.id.progress)).setText(download.getProgressString());
                     ((TextView) findViewById(R.id.progress)).setTextColor(Color.WHITE);
                     break;
 
                 case DOWNLOADING:
                     progress = (ProgressBar) findViewById(R.id.downloading_progress);
                     progress.setIndeterminate(false);
                     progress.setProgress(download.getProgress());
                     ((TextView) findViewById(R.id.speed)).setText(download.getSpeedInKBpsString(ApkInfo.this));
                     ((TextView) findViewById(R.id.speed)).setTextColor(Color.WHITE);
                     ((TextView) findViewById(R.id.progress)).setText(download.getProgressString());
                     ((TextView) findViewById(R.id.progress)).setTextColor(Color.WHITE);
                     break;
 
                 case FAILED:
                     Log.d("ApkInfo-DownloadListener", "Download Failed due to: "+download.getDownload().getFailReason().toString(getApplicationContext()));
                     if(download.getDownload().getFailReason().equals(EnumDownloadFailReason.IP_BLACKLISTED)){
                         new DialogIpBlacklisted(ApkInfo.this).show();
                     }else{
                         Toast toast= Toast.makeText(context, context.getString(R.string.download_failed_due_to)+": "+download.getDownload().getFailReason().toString(getApplicationContext()), Toast.LENGTH_SHORT);
                         toast.show();
                     }
                     findViewById(R.id.download_progress).setVisibility(View.GONE);
 //                    findViewById(R.id.icon_manage).setVisibility(View.GONE);
                     findViewById(R.id.downloading_name).setVisibility(View.GONE);
                     findViewById(R.id.btinstall).setOnClickListener(installListener);
                     break;
 
                 case RESTARTING:
                     break;
                 case STOPPED:
                 case COMPLETED:
                     if(actionBar!=null){
                         actionBar.dismiss();
                     }
                     findViewById(R.id.btinstall).setOnClickListener(installListener);
                     findViewById(R.id.download_progress).setVisibility(View.GONE);
 //                    findViewById(R.id.icon_manage).setVisibility(View.GONE);
                     findViewById(R.id.downloading_name).setVisibility(View.GONE);
                     break;
 
                 default:
                     break;
             }
 
         }
     };
 
 
 
     @Override
     public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
         return new SimpleCursorLoader(ApkInfo.this) {
 
             @Override
             public Cursor loadInBackground() {
                 return db.getAllApkVersions(viewApk.getApkid(),viewApk.getId(),viewApk.getVername(), getIntent()
                         .getExtras().getBoolean("top", false),viewApk.getRepo_id());
             }
         };
 
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
         adapter.swapCursor(arg1);
         if(arg1.getCount()>1){
             spinner.setVisibility(View.VISIBLE);
         }
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> arg0) {
         adapter.swapCursor(null);
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
 
         if(download!=null&&!download.isNull()){
             try {
                 serviceDownloadManager.callUnregisterDownloadObserver(viewApk.hashCode());
             } catch (RemoteException e) {
                 e.printStackTrace();
             }
         }
         unbindService(serviceManagerConnection);
         handler = null;
         mAdView.destroy();
     }
 
 
 
 
     private QuickAction actionBar;
     private void setupQuickActions(View view){
         actionBar  = new QuickAction(context);
         ActionItem playItem = new ActionItem(EnumQuickActions.PLAY.ordinal(), "Resume", context.getResources().getDrawable(R.drawable.ic_media_play));
         ActionItem pauseItem = new ActionItem(EnumQuickActions.PAUSE.ordinal(), "Pause", context.getResources().getDrawable(R.drawable.ic_media_pause));
         ActionItem stopItem = new ActionItem(EnumQuickActions.STOP.ordinal(), "Stop", context.getResources().getDrawable(R.drawable.ic_media_stop));
 
         switch (download.getDownloadStatus()) {
             case SETTING_UP:
             case RESTARTING:
             case RESUMING:
                 break;
 
             case DOWNLOADING:
                 actionBar.addActionItem(pauseItem);
                 break;
 
             default:
                 actionBar.addActionItem(playItem);
                 break;
         }
         actionBar.addActionItem(stopItem);
         actionBar.show(view);
 
         actionBar.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
             @Override
             public void onItemClick(QuickAction quickAction, int pos, final int actionId) {
                 new Thread(new Runnable() {
                     public void run() {
                         try {
                             switch (EnumQuickActions.reverseOrdinal(actionId)) {
                                 case PLAY:
                                     serviceDownloadManager
                                             .callResumeDownload(download.hashCode());
                                     break;
 
                                 case PAUSE:
                                     serviceDownloadManager
                                             .callPauseDownload(download.hashCode());
                                     break;
 
                                 case STOP:
                                     serviceDownloadManager
                                             .callStopDownload(download.hashCode());
                                     break;
 
                                 default:
                                     break;
                             }
                         } catch (RemoteException e) {
                             e.printStackTrace();
                         }
                     }
                 }	).start();
 
             }
         });
     }
 
     private ViewGroup viewComments;
 
     private class GetApkInfo extends AsyncTask<Void,Void,Void> {
 
         private WebserviceGetApkInfo webservice;
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             if(!getIntent().hasExtra("installed") && viewApk.isPaid()){
                 findViewById(R.id.btinstall).setEnabled(false);
                 ((Button)findViewById(R.id.btinstall)).setTextColor(Color.GRAY);
             }
         }
 
         /**
          * Override this method to perform a computation on a background thread. The
          * specified parameters are the parameters passed to {@link #execute}
          * by the caller of this task.
          * <p/>
          * This method can call {@link #publishProgress} to publish updates
          * on the UI thread.
          *
          * @param params The parameters of the task.
          * @return A result, defined by the subclass of this task.
          * @see #onPreExecute()
          * @see #onPostExecute
          * @see #publishProgress
          */
         @Override
         protected Void doInBackground(Void... params) {
 
             try {
                 webservice = new WebserviceGetApkInfo(webservicespath,viewApk,category,Login.getToken(context),viewApk.getMd5());
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (JSONException e) {
                 e.printStackTrace();
             }
 
 
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         /**
          * <p>Runs on the UI thread after {@link #doInBackground}. The
          * specified result is the value returned by {@link #doInBackground}.</p>
          * <p/>
          * <p>This method won't be invoked if the task was cancelled.</p>
          *
          * @param aVoid The result of the operation computed by {@link #doInBackground}.
          * @see #onPreExecute
          * @see #doInBackground
          * @see #onCancelled(Object)
          */
         @Override
         protected void onPostExecute(Void aVoid) {
             super.onPostExecute(aVoid);
             unstrustedPayment = false;
             try {
                 if(!getIntent().hasExtra("installed"))
                     checkPayment(webservice.getPayment());
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             try{
 
             	if(webservice.hasLatestVersion()){
             		TextView getLatest = (TextView) findViewById(R.id.getLatest);
             		String getLatestText = getString(R.string.get_latest_version);
 
             		if(!webservice.getMalwareInfo().getStatus().toUpperCase(Locale.ENGLISH).equals("SCANNED")){
             			getLatestText = getString(R.string.get_latest_version_and_trusted);
             		}
             		SpannableString spanString = new SpannableString(getLatestText);
             		spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
             		getLatest.setText(spanString);
             		getLatest.setVisibility(View.VISIBLE);
                     getLatest.setOnClickListener(new OnClickListener() {
                         @Override
                         public void onClick(View v) {
 
                             String url = null;
                             try {
                                 url = webservice.getLatestVersionURL();
                                 Intent i = new Intent(Intent.ACTION_VIEW);
                                 i.setData(Uri.parse(url));
                                 startActivity(i);
                             } catch (JSONException e) {
                                 e.printStackTrace();
                             }
 
 
                         }
                     });
 
 
 
                 }
 
             }catch (Exception e){
 
             }
 
             try {
                loadMalware(webservice.getMalwareInfo());
             } catch (Exception e) {
                 e.printStackTrace();
             }
             try{
                 ArrayList<Comment> result = webservice.getComments();
                 loading.setVisibility(View.GONE);
                 setComments(result);
                 if(result.isEmpty()){
                     TextView tv = new TextView(context);
                     tv.setText(context.getString(R.string.no_comments));
                     tv.setPadding(8, 2, 2, 2);
                     viewComments.addView(tv);
                 }
                 if(webservice.isSeeAll()){
                     findViewById(R.id.more_comments).setVisibility(View.VISIBLE);
                 }
 
             }catch (Exception e){
                 e.printStackTrace();
             }
 
             try {
                 if(webservice.isScreenshotChanged()){
                     viewApk.setScreenShots(db.getScreenshots(viewApk,category));
                     loadScreenshots();
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             try{
                 TasteModel model = webservice.getLikes();
 
                 setLikes(model.likes,model.dislikes);
 
                 if(model.uservote!=null){
 
                     if(model.uservote.equals("like")){
                         ((Button) viewLikesButton.findViewById(R.id.likesImage)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_btn_over , 0, 0, 0);
                         ((Button) viewLikesButton.findViewById(R.id.dislikesImage)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.dislike_btn, 0);
                     }else if(model.uservote.equals("dislike")){
                         ((Button) viewLikesButton.findViewById(R.id.likesImage)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_btn, 0, 0, 0);
                         ((Button) viewLikesButton.findViewById(R.id.dislikesImage)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.dislike_btn_over, 0);
                     }
                 }
 
             }catch (Exception e){
                 if(viewApk.getLikes()==-1){
                     ((TextView) viewLikes.findViewById(R.id.likes)).setText(context.getString(R.string.tastenotavailable));
                     ((TextView) viewLikes.findViewById(R.id.dislikes)).setText("");
                 }
                 if(viewApk.getComments().isEmpty()){
                     TextView tv = new TextView(context);
                     tv.setText(context.getString(R.string.no_internet_connection));
                     viewComments.addView(tv);
                     loading.setVisibility(View.GONE);
                 }
                 if(viewApk.isPaid()){
                     findViewById(R.id.btinstall).setOnClickListener( buyListener );
                     ((Button) findViewById(R.id.btinstall)).setText(getString(R.string.buy) + " $" + viewApk.getPrice());
 
                     unstrustedPayment = true;
                 } else {
                     ((Button) findViewById(R.id.btinstall)).setText(installString);
                     findViewById(R.id.btinstall).setOnClickListener( installListener );
                 }
 
 //                Toast.makeText(context, "Failed to check Payment", Toast.LENGTH_LONG).show();
 
 
             }
             findViewById(R.id.btinstall).setEnabled(true);
             ((Button)findViewById(R.id.btinstall)).setTextColor(Color.WHITE);
 
 
 
 
 
         }
 
 
     }
 
 
 
     private void setComments(ArrayList<Comment> result) {
         viewComments.removeAllViews();
         for(Comment comment : result){
             View v = LayoutInflater.from(context).inflate(R.layout.row_comment_item, null);
             ((TextView) v.findViewById(R.id.author)).setText(comment.username);
             ((TextView) v.findViewById(R.id.content)).setText(comment.text);
             ((TextView) v.findViewById(R.id.date)).setText(comment.timeStamp.toString());
             viewComments.addView(v);
         }
     }
 
     public void setLikes(String likes, String dislikes) {
         ((TextView) viewLikes.findViewById(R.id.likes)).setText(likes);
         ((TextView) viewLikes.findViewById(R.id.dislikes)).setText(dislikes);
     }
 
     public class checkPaymentTask extends AsyncTask<Void, Void, JSONObject>{
 
         ProgressDialog pd = new ProgressDialog(context);
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
 
         }
 
         @Override
         protected JSONObject doInBackground(Void... params) {
             JSONObject json = null;
             try{
                 NetworkUtils utils = new NetworkUtils();
 //                String request = "http://webservices.aptoide.com/webservices/checkPaidApk/" +Login.getToken(context) +  "/" + Login.getUserLogin(context) + "/"+ viewApk.getRepoName() +"/"+viewApk.getApkid() + "/" + viewApk.getVername()+"/json";
 
                 String request = "http://webservices.aptoide.com/webservices/checkPaidApk/" +Login.getToken(context) +  "/"+ viewApk.getRepoName() +"/"+viewApk.getApkid() + "/" + viewApk.getVername()+"/json";
 
                 System.out.println(request);
                 json = utils.getJsonObject(request, ApkInfo.this);
             }catch (Exception e){
                 e.printStackTrace();
             }
             return json;
         }
 
         @Override
         protected void onPostExecute(JSONObject json) {
             super.onPostExecute(json);
             if(pd.isShowing())pd.dismiss();
             checkPayment(json);
 
 
         }
 
     }
 
     private void checkPayment(JSONObject json) {
         try{
 
             System.out.println("JSON" + json);
             String status = json.getString("status");
             if(status.equals("OK")){
                 try{
                     double price = json.getDouble("amount");
                     if(price > 0 && json.has("apkpath")){
 
                         String path = json.getString("apkpath");
                         viewApk.setPath(path);
                         viewApk.setIsPaid(true);
                         findViewById(R.id.btinstall).setOnClickListener(installListener);
                         ((Button) findViewById(R.id.btinstall)).setText(R.string.install);
 
                     }
                 }catch (Exception e){
                     e.printStackTrace();
                 }
             }else{
                 try{
 
                     if(json.has("payStatus")){
                         if(json.getString("payStatus").equals("pending")){
                             ((Button) findViewById(R.id.btinstall)).setText("Pending");
                             ((Button) findViewById(R.id.btinstall)).setEnabled(false);
                         }
                     }else{
                         double price = json.getDouble("amount");
                         if(price > 0){
                             findViewById(R.id.btinstall).setOnClickListener(buyListener);
                             findViewById(R.id.btinstall).setEnabled(true);
                             ((Button) findViewById(R.id.btinstall)).setText("Buy" + " $" + price);
                         }
                     }
 
                 }catch (Exception e){
                     e.printStackTrace();
                 }
 
             }
         }catch (Exception e){
             e.printStackTrace();
             if(!getIntent().hasExtra("installed")){
                 findViewById(R.id.btinstall).setEnabled(true);
                 findViewById(R.id.btinstall).setOnClickListener( installListener );
                 ((Button) findViewById(R.id.btinstall)).setText(R.string.install);
             }
 //            Toast.makeText(context, "Failed to check Payment", Toast.LENGTH_LONG).show();
         }
 
 
 
     }
 
     private OnClickListener buyListener = new OnClickListener() {
 
         @Override
         public void onClick(View v) {
 
             if(Login.isLoggedIn(context)){
 
             	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
             	alertDialogBuilder.setTitle(R.string.payment_method);
             	alertDialogBuilder
             		.setIcon(android.R.drawable.ic_menu_info_details)
             		.setMessage(getString(R.string.paypal_message))
             		.setCancelable(false)
             		.setPositiveButton(getString(R.string.credit_card),new DialogInterface.OnClickListener() {
             			public void onClick(DialogInterface dialog,int id) {
             				 Intent i = new Intent(ApkInfo.this, CreditCard.class);
                              i.putExtra("apkid", viewApk.getApkid());
                              i.putExtra("versionName", viewApk.getVername());
                              i.putExtra("repo", viewApk.getRepoName());
                              startActivityForResult(i, 1);
 
             			}
             		 })
             		.setNegativeButton(getString(R.string.paypal),new DialogInterface.OnClickListener() {
             			public void onClick(DialogInterface dialog,int id) {
             				  Intent i = new Intent(ApkInfo.this, Buy.class);
                               i.putExtra("apkid", viewApk.getApkid());
                               i.putExtra("versionName", viewApk.getVername());
                               i.putExtra("repo", viewApk.getRepoName());
                               startActivityForResult(i, 1);
             			}
             		});
             	AlertDialog alertDialog = alertDialogBuilder.create();
             	alertDialog.show();
             	
                 if(unstrustedPayment){
                 	AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(context);
                 	alertDialogBuilder1.setTitle(R.string.payment_warning_title);
                 	alertDialogBuilder1
                 		.setIcon(android.R.drawable.ic_menu_info_details)
                 		.setMessage(R.string.payment_warning_text)
                 		.setCancelable(false)
                 		.setNeutralButton(getString(android.R.string.ok),new DialogInterface.OnClickListener() {
                 			public void onClick(DialogInterface dialog,int id) {
                 				dialog.cancel();
 
                 			}
                 		 });
                 	AlertDialog alertDialog1 = alertDialogBuilder1.create();
                 	alertDialog1.show();
                 	
                 }
             }else{
                 startActivityForResult(new Intent(ApkInfo.this,Login.class), 1);
             }
 
 
 
 
 
         }
     };
 
 }
