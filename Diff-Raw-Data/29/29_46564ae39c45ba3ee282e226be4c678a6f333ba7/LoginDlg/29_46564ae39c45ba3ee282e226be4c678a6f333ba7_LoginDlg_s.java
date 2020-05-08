 package com.htb.cnk.dialog;
 
 import java.lang.reflect.Method;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.htb.cnk.R;
 import com.htb.cnk.data.Setting;
 import com.htb.cnk.data.UserData;
 
 public class LoginDlg {
 	private final String TAG = "LoginDlg";
 	public final static int ACTION_SUBMIT = 10;
 	private Context mActivity;
 	private Class<?> mDestActivity;
 	ProgressDialog pdialog;
 	private int mAction;
 	private boolean shown;
 	
 	public LoginDlg(Context context, Class<?> dest) {
 		mActivity = context;
 		mDestActivity = dest;
 	}
 	
 	public LoginDlg(Context context, int action) {
 		mActivity = context;
 		mAction = action;
 	}
 	
 	public void show(final int permissionRequres) {
 		if (!shown) {
 			shown = true;
 			LayoutInflater factory = LayoutInflater.from(mActivity);
 			final View DialogView = factory.inflate(R.layout.setting_dialog, null);
 			SharedPreferences sharedPre = mActivity.getSharedPreferences("userInfo",
 					Context.MODE_PRIVATE);
 			String userName = sharedPre.getString("name", "");
 			EditText userNameET = (EditText) DialogView.findViewById(R.id.edit_username);
 			
 			if (!Setting.enabledPWDCheck()) {
 				UserData.debugMode();
 				shown = false;
 				login();
 				return ;
 			}
 			userNameET.setText(userName);
 			
 			AlertDialog dlg = new AlertDialog.Builder(mActivity)
 					.setTitle("登录框")
 					.setView(DialogView)
 					.setPositiveButton("确定",
 							new DialogInterface.OnClickListener() {
 	
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									shown = false;
 									if (getUserNameAndPwd(DialogView) < 0) {
 										Toast.makeText(mActivity, "用户名密码不能为空", Toast.LENGTH_SHORT).show();
 									} else {
 										pdialog = new ProgressDialog(mActivity);
 										pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 										pdialog.setMessage("正在登陆...");
 										pdialog.setIndeterminate(false);
 										pdialog.setCancelable(false);
 										pdialog.show();
 										new Thread() {
 										public void run() {
 											try {
 												int ret = UserData.compare(permissionRequres);
 												userHandle.sendEmptyMessage(ret);
 											} catch (Exception e) {
 												userHandle.sendEmptyMessage(UserData.PWD_NETWORK_ERR);
 												e.printStackTrace();
 											}
 										}}.start();
 									}
 								}
 							}).setNegativeButton("取消",
 							new DialogInterface.OnClickListener() {
 	
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									shown = false;
 								}
 							})
 					.setCancelable(false).create();
 			dlg.show();
 		}
 	}
 	
 	private void login() {
 		if (mDestActivity != null) {
 			Intent intent = new Intent();
 			intent.setClass(mActivity, mDestActivity);
 			mActivity.startActivity(intent);
 		} else {
 			switch(mAction) {
 			case ACTION_SUBMIT:
 				try {
 					Method method = mActivity.getClass().getMethod("submitOrder", new Class[0]);
 					method.invoke(mActivity, new Object[0]);
 				} catch (NoSuchMethodException e) {
 					e.printStackTrace();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	private int getUserNameAndPwd(final View DialogView) {
 		EditText mUserName = (EditText) DialogView
 				.findViewById(R.id.edit_username);
 		final String userName = mUserName.getText()
 				.toString();
 		EditText mUserPwd = (EditText) DialogView
 				.findViewById(R.id.edit_password);
 		final String userPwd = mUserPwd.getText()
 				.toString();
 		UserData.clean();
 		if ("".equals(userName) || "".equals(userPwd)) {
 			return -1;
 		} else {
 			SharedPreferences sharedPre = mActivity.getSharedPreferences(
 					"userInfo",
 					Context.MODE_PRIVATE);
 			Editor editor = sharedPre.edit();
 			editor.putString("name", userName);
 			editor.commit();
 	
 			UserData.setUserName(userName);
 			UserData.setUserPwd(userPwd);
 			return 0;
 		}
 	}
 
 	private Handler userHandle = new Handler() {
 
 		public void handleMessage(Message msg) {
 			pdialog.cancel();
 			if (msg.what < 0) {
 				switch(msg.what) {
 				case UserData.PWD_INCORRECT:
 					Toast.makeText(mActivity,
 							R.string.userWarning,
 							Toast.LENGTH_LONG).show();
 					break;
 				case UserData.NO_PERMISSION:
 					Toast.makeText(mActivity,
 							"权限不够",
 							Toast.LENGTH_LONG).show();
 					break;
 				case UserData.PWD_NETWORK_ERR:
 					Toast.makeText(mActivity,
 							"网络错误",
 							Toast.LENGTH_LONG).show();
 					break;
 				default:
 					Log.e(TAG, "unknown err msg");
 				}
 				
 			} else {
 				login();
 			}
 		}
 	};
 }
