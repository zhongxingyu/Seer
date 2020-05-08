 package com.htb.cnk;
 
 import java.lang.reflect.Method;
 
import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.UserData;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class LoginDlg {
 	public final static int ACTION_SUBMIT = 10;
 	private Context mActivity;
 	private Class<?> mDestActivity;
 	private int mAction;
 	
 	public LoginDlg(Context context, Class<?> dest) {
 		mActivity = context;
 		mDestActivity = dest;
 	}
 	
 	public LoginDlg(Context context, int action) {
 		mActivity = context;
 		mAction = action;
 	}
 	
 	public void show() {
 		LayoutInflater factory = LayoutInflater.from(mActivity);
 		// 得到自定义对话框
 		final View DialogView = factory.inflate(R.layout.setting_dialog,
 				null);
 		SharedPreferences sharedPre = mActivity.getSharedPreferences("userInfo",
 				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
 		String userName = sharedPre.getString("name", "");
 		EditText userNameET = (EditText) DialogView
 				.findViewById(R.id.edit_username);
 		userNameET.setText(userName);
 		// 创建对话框
 		AlertDialog dlg = new AlertDialog.Builder(mActivity)
 				.setTitle("登录框")
 				.setView(DialogView)
 				// 设置自定义对话框样式
 				.setPositiveButton("确定",
 						new DialogInterface.OnClickListener() {// 设置监听事件
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								EditText mUserName = (EditText) DialogView
 										.findViewById(R.id.edit_username);
 								final String userName = mUserName.getText()
 										.toString();
 								EditText mUserPwd = (EditText) DialogView
 										.findViewById(R.id.edit_password);
 								final String userPwd = mUserPwd.getText()
 										.toString();
 								UserData.clean();
 								if ("".equals(userName)
 										|| "".equals(userPwd)) {
 									dialog.cancel();
 								} else {
 									SharedPreferences sharedPre = mActivity.getSharedPreferences(
 											"userInfo",
 											Context.MODE_WORLD_WRITEABLE
 													| Context.MODE_WORLD_READABLE);
 									Editor editor = sharedPre.edit();
 									editor.putString("name", userName);
 									editor.commit();
 
 									UserData.setUserName(userName);
 									UserData.setUserPwd(userPwd);
 								}
 								new Thread() {
 								public void run() {
 									try {
 										int ret = UserData.Compare();
 										userHandle.sendEmptyMessage(ret);
 									} catch (Exception e) {
 										e.printStackTrace();
 									}
 								}}.start();
 							}
 						}).setNegativeButton("取消",// 设置取消按钮
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 							}
 						}).create();// 创建对话框
 		dlg.show();// 显示对话框
 	}
 	
 	private Handler userHandle = new Handler() {
 
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				Toast.makeText(mActivity,
 						R.string.userWarning,
 						Toast.LENGTH_SHORT).show();
 			} else {
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
 		}
 	};
 }
