 package com.guoli.hotel.activity.user;
 
 import java.util.HashMap;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.guoli.hotel.R;
 import com.guoli.hotel.activity.BaseActivity2;
 import com.guoli.hotel.bean.LoginUserInfo;
 import com.guoli.hotel.net.GuoliRequest;
 import com.guoli.hotel.net.request.bean.UserRegisterBean;
 import com.guoli.hotel.utils.DialogUtils;
 import com.guoli.hotel.utils.LoginUtils;
 import com.guoli.hotel.utils.ToastUtil;
 import com.guoli.hotel.widget.BottomTabbar;
 import com.msx7.core.Manager;
 import com.msx7.core.command.ErrorCode;
 import com.msx7.core.command.IResponseListener;
 import com.msx7.core.command.model.Request;
 import com.msx7.core.command.model.Response;
 
 public class LoginActivity extends BaseActivity2 {
 
 	public static final int RESULT_LOGIN_OK = 0x0001;
 	public static final int RESULT_UN_LOGIN = 0x0002;
 	public static final String PARAM_SEACRCH = "order_search";
 	public static final String PARAM_ORDER = "order";
	public static final int PAGE_ORDER = 3;
 
 	private EditText passwordView;
 	private String password;
 	private EditText idView;
 	private String id;
 	private Dialog dialog;
 	private LinearLayout unregestLayout;
 
 	@Override
 	public void onAfterCreate(Bundle savedInstanceState) {
 		setTitle(R.string.login);
 		new BottomTabbar(this, -1);
 		findViewById(R.id.textView6).setOnClickListener(onForgetPassword);
 		findViewById(R.id.textView7).setOnClickListener(onRegistUser);
 		findViewById(R.id.button2).setOnClickListener(onLoginListener);
 		Button unLoginBtn = (Button) findViewById(R.id.button1);
 		unLoginBtn.setOnClickListener(onUnLogClickListener);
 
 		unregestLayout = (LinearLayout) findViewById(R.id.unregestLayout);
 		passwordView = (EditText) findViewById(R.id.textView5);
 		idView = (EditText) findViewById(R.id.textView3);
 		hideLayout();
 		if (getIntent().hasExtra(PARAM_SEACRCH)) {
 			unLoginBtn.setText(R.string.login_unregest_search);
 		} else if (getIntent().hasExtra(PARAM_ORDER)) {
 			unLoginBtn.setText(R.string.login_unregest);
 		}
 	}
 
 	@Override
 	public int getContentId() {
 		return R.layout.login_activity;
 	}
 
 	/**
 	 * 点击 注册用户
 	 * 
 	 * @param v
 	 */
 	View.OnClickListener onRegistUser = new View.OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			startActivityForResult(new Intent(LoginActivity.this, RegisterActivity.class), RESULT_OK);
 		}
 	};
 
 	/**
 	 * 点击 忘记密码
 	 * 
 	 * @param v
 	 */
 	View.OnClickListener onForgetPassword = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			startActivityForResult(new Intent(LoginActivity.this, ForgetPasswordActivity.class), RESULT_OK);
 		}
 	};
 	/**
 	 * 登录
 	 */
 	View.OnClickListener onLoginListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			password = passwordView.getText().toString();
 			id = idView.getText().toString();
 
 			if (TextUtils.isEmpty(id)) {
 				DialogUtils.showDialog("提示", "请输入登录名", LoginActivity.this);
 				return;
 			}
 
 			if (TextUtils.isEmpty(password)) {
 				DialogUtils.showDialog("提示", "请输入密码", LoginActivity.this);
 				return;
 			}
 
 			Request request = new GuoliRequest("user_login", new UserRegisterBean(id, password, null));
 			Manager.getInstance().executePoset(request, loginResponseListener);
 
 			dialog = DialogUtils.showProgressDialog(LoginActivity.this, "登陆中...");
 		}
 	};
 
 	IResponseListener loginResponseListener = new IResponseListener() {
 
 		@Override
 		public void onSuccess(Response response) {
 			if (null != dialog && dialog.isShowing()) {
 				dialog.cancel();
 			}
 
 			if (null == response) {
 				return;
 			}
 
 			Log.d("MSG", "onSuccess:" + response.getData().toString());
 			HashMap<String, Object> map = new Gson().fromJson(response.result.toString(),
 					new TypeToken<HashMap<String, Object>>() {
 					}.getType());
 
 			if ("1".equalsIgnoreCase(map.get("success").toString())) {
 				LoginUserInfo info = new Gson().fromJson(new Gson().toJson(map.get("userinfo")), LoginUserInfo.class);
 				saveLogin(info.uid, info.username, info.mobile, info.nickName,info.ticketnum);
 				Intent data = new Intent();
 				data.putExtra("username", info.username);
 				setResult(AccountActivity.ACCOUNT_LOGIN, data);
 				finish();
 			}
 
 			ToastUtil.show(map.get("message").toString());
 		}
 
 		@Override
 		public void onError(Response response) {
 			if (null != dialog && dialog.isShowing()) {
 				dialog.cancel();
 			}
 
 			Log.i("tag", "response=" + (response == null ? null : response.result));
 			ToastUtil.show(ErrorCode.getErrorCodeString(response.errorCode));
 		}
 	};
 
 	/**
 	 * 非会员直接预订
 	 */
 	View.OnClickListener onUnLogClickListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			setResult(RESULT_UN_LOGIN);
 			finish();
 		}
 	};
 
 	private void saveLogin(String uid, String username, String mobile, String nickName,String ticketnum) {
 		LoginUtils.isLogin = 2;
 		LoginUtils.uid = uid;
 		LoginUtils.username = username;
 		LoginUtils.memberMobile = mobile;
 		LoginUtils.nickName = nickName;
 		LoginUtils.ticketnum=ticketnum;
 	}
 
 	/**
 	 * 隐藏非会员登陆
 	 */
 	private void hideLayout() {
 		int loginType = getIntent().getIntExtra("loginType", 0);
 		if (loginType == AccountActivity.HIDE_LAYOUT_LOGIN) {
 			unregestLayout.setVisibility(View.GONE);
 		}
 	}
 
 }
