 package com.yinheli.sxtcm.activity;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Vibrator;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.yinheli.sxtcm.Constants;
 import com.yinheli.sxtcm.R;
 import com.yinheli.sxtcm.utils.ActivityUtil;
 import com.yinheli.sxtcm.utils.FileUtils;
 import com.yinheli.sxtcm.utils.HttpClientUtil;
 import com.yinheli.sxtcm.utils.HttpClientUtil.Result;
 
 /**
  * Launcher Activity
  * 
  * @author yinheli <yinheli@gmail.com>
  *
  */
 public class LauncherActivity extends Activity {
 	
 	HttpClientUtil u = new HttpClientUtil();
 	
 	AlertDialog updateConfirmDialog;
 	
 	ProgressDialog updateProgressDialog;
 	
 	File tartgetDir;
 	
 	File targetFile;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_launcher);
 		
 		new Handler().postDelayed(new Runnable() {
 			
 			@Override
 			public void run() {
 				if (ActivityUtil.isNewworkAvailable(LauncherActivity.this)) {
 					// 检查更新
 					if (needupdate()) {
 						Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
 						v.vibrate(500);
 						AlertDialog.Builder builder = new AlertDialog.Builder(LauncherActivity.this);
 						builder.setCancelable(false);
 						builder.setTitle("更新");
 						builder.setMessage("发现新版本，请更新！");
 						builder.setPositiveButton("更新", new OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								if (updateConfirmDialog != null && updateConfirmDialog.isShowing()) {
 									updateConfirmDialog.dismiss();
 								}
 								
 								updateProgressDialog = new ProgressDialog(LauncherActivity.this);
 								updateProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 								updateProgressDialog.setTitle("更新");
 								updateProgressDialog.setMessage("升级文件下载中，请稍后");
 								updateProgressDialog.setCancelable(false);
 								updateProgressDialog.setIcon(android.R.drawable.ic_menu_set_as);
 								updateProgressDialog.show();
 								
 								tartgetDir = FileUtils.getSdCard();
 								if (tartgetDir == null) {
 									Toast.makeText(LauncherActivity.this, "SD卡不可用，尝试使用手机机身文件存储", Toast.LENGTH_SHORT).show();
 									tartgetDir = FileUtils.getAppFileDir(LauncherActivity.this);
 								}
 								
 								if (tartgetDir != null) {
 									tartgetDir.mkdirs();
 								}
 								
 								new AsyncTask<Void, Integer, Boolean>() {
 									
 									int total;
 									
 									int current;
 									
 									protected void onProgressUpdate(Integer... values) {
 										updateProgressDialog.setProgress(values[0]);
 									};
 									
 									@Override
 									protected Boolean doInBackground(
 											Void... params) {
 										try {
 											targetFile = new File(tartgetDir, "last.app");
 											
 											URL url = new URL(Constants.LAST_APP_URL);
 											URLConnection con = url.openConnection();
 											total = con.getContentLength();
 											BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
 											BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile));
 											byte[] buf = new byte[1024];
 											int len = -1;
 											while ((len = bis.read(buf)) != -1) {
 												current += len;
 												publishProgress(new Integer[]{(int) (1.0 * current / total * 100)});
 												bos.write(buf, 0, len);
 											}
 											bos.flush();
 											bis.close();
 											bos.close();
 										} catch (Exception e) {
 											e.printStackTrace();
 											return false;
 										}
 										return true;
 									}
 									
 									@Override
 									protected void onPostExecute(Boolean result) {
 										if (result != null && result) {
 											updateProgressDialog.setMessage("下载完毕，准备安装应用");
 											Intent intent = new Intent(Intent.ACTION_VIEW);
 											intent.setDataAndType(Uri.fromFile(targetFile), "application/vnd.android.package-archive");  
 											startActivity(intent);
 											finish();
 										} else {
 											Toast.makeText(LauncherActivity.this, "下载文件失败", Toast.LENGTH_SHORT).show();
 										}
 									}
 								}.execute();
 							}
 						});
 						builder.setNegativeButton("退出", new OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								System.exit(0);
 							}
 						});
 						updateConfirmDialog = builder.create();
 						updateConfirmDialog.show();
 					} else {
 						gomain();
 					}
 				} else {
 					AlertDialog.Builder builder = new AlertDialog.Builder(LauncherActivity.this);
 					builder.setCancelable(false);
 					builder.setTitle("网络不可用");
 					builder.setMessage("现在网络不可用，继续访问只能查看缓存中的数据，你可以选择设置开启网络，或者退出应用");
 					builder.setPositiveButton("设置", new OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							Intent i = new Intent("/");
 							ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
 							i.setComponent(comp);
 							i.setAction("android.intent.action.VIEW");
 							startActivity(i);
 							finish();
 						}
 					}).setNeutralButton("继续", new OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							gomain();
 						}
 					}).setNegativeButton("退出", new OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							finish();
 						}
 					});
 					
 					builder.create().show();
 				}
 			}
		}, 2000);
 	}
 	
 	private boolean needupdate() {
 		try {
 			Log.d(Constants.TAG, "检查更新");
 			Result r = u.get(Constants.CHECK_UPDATE_URL);
 			String v = new String(r.content);
 			if (Constants.VER_ID < Integer.parseInt(v)) {
 				Log.d(Constants.TAG, "发现新版本");
 				return true;
 			}
 		} catch (Exception e) {
 			// ignore
 		} 
 		return false;
 	}
 	
 	private void gomain() {
 		startActivity(new Intent(LauncherActivity.this, MainActivity.class));
 		finish();
 	}
 	
 	@Override
 	public void onBackPressed() {
 		// ignore
 	}
 }
