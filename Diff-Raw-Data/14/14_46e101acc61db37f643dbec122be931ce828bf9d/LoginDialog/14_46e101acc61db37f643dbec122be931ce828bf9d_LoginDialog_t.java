 package com.intalker.borrow.ui.login;
 
 import com.intalker.borrow.HomeActivity;
 import com.intalker.borrow.R;
 import com.intalker.borrow.cloud.CloudAPIAsyncTask.ICloudAPITaskListener;
 import com.intalker.borrow.cloud.CloudAPI;
 import com.intalker.borrow.config.AppConfig;
 import com.intalker.borrow.data.UserInfo;
 import com.intalker.borrow.ui.control.HaloButton;
 import com.intalker.borrow.util.DensityAdaptor;
 import com.intalker.borrow.util.LayoutUtil;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Color;
 import android.text.InputType;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.ImageView.ScaleType;
 import android.widget.Toast;
 
 public class LoginDialog extends Dialog {
 	private RelativeLayout mContent = null;
 	private RelativeLayout mMainLayout = null;
 	private EditText mEmailInput = null;
 	private EditText mPasswordInput = null;
 	private HaloButton mLoginBtn = null;
 	private HaloButton mCancelBtn = null;
 
 	public LoginDialog(Context context) {
 		super(context, R.style.Theme_TransparentDialog);
 
 		mContent = new RelativeLayout(context);
 		this.setContentView(mContent);
 
 		mMainLayout = new RelativeLayout(context);
 		mMainLayout.setBackgroundResource(R.drawable.login_bg);
 		RelativeLayout.LayoutParams mainLayoutLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		mainLayoutLP.width = DensityAdaptor.getDensityIndependentValue(280);
 		mainLayoutLP.height = DensityAdaptor.getDensityIndependentValue(250);
 
 		mContent.addView(mMainLayout, mainLayoutLP);
 
 		int margin = LayoutUtil.getLoginDialogMargin();
 		int y = DensityAdaptor.getDensityIndependentValue(40);
 		mEmailInput = createElement(R.string.email, margin, y);
 		// mEmailInput.setInputType(InputType.TYPE_CLASS_TEXT);//??
 		mEmailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
 		// Test
 		mEmailInput.setText("tom.dong@openlib.com");
 
 		y = DensityAdaptor.getDensityIndependentValue(100);
 		mPasswordInput = createElement(R.string.password, margin, y);
 		mPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT
 				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
 		// Test
 		mPasswordInput.setText("dong");
 
 		mMainLayout.addView(createSeparator(DensityAdaptor
 				.getDensityIndependentValue(160)));
 
 		int largeMargin = LayoutUtil.getLargeMargin();
 		mLoginBtn = new HaloButton(context, R.drawable.ok);
 		RelativeLayout.LayoutParams loginBtnLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		loginBtnLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		loginBtnLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		loginBtnLP.leftMargin = largeMargin;
 		loginBtnLP.bottomMargin = margin;
 		mMainLayout.addView(mLoginBtn, loginBtnLP);
 		mLoginBtn.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				login();
 			}
 
 		});
 
 		mCancelBtn = new HaloButton(context, R.drawable.back);
 		RelativeLayout.LayoutParams cancelBtnLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		cancelBtnLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 		cancelBtnLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		cancelBtnLP.rightMargin = largeMargin;
 		cancelBtnLP.bottomMargin = margin;
 		mMainLayout.addView(mCancelBtn, cancelBtnLP);
 		mCancelBtn.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				cancelLogin();
 			}
 
 		});
 	}
 
 	private View createSeparator(int y) {
 		ImageView v = new ImageView(this.getContext());
 		v.setImageResource(R.drawable.hori_separator);
 		v.setScaleType(ScaleType.FIT_XY);
 		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.FILL_PARENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 
 		lp.topMargin = y;
 		lp.width = LayoutUtil.getDetailDialogWidth();
 
 		v.setLayoutParams(lp);
 		return v;
 	}
 
 	private EditText createElement(int labelTextResId, int leftMargin,
 			int topMargin) {
 		Context context = this.getContext();
 		TextView label = new TextView(context);
 		label.setTextColor(Color.WHITE);
 		label.setText(labelTextResId);
 		RelativeLayout.LayoutParams labelLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		labelLP.leftMargin = leftMargin;
 		labelLP.topMargin = topMargin;
 		mMainLayout.addView(label, labelLP);
 
 		EditText input = new EditText(context);
 		input.setTextSize(12.0f);
 		RelativeLayout.LayoutParams inputLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		inputLP.width = LayoutUtil.getLoginDialogInputWidth();
 		inputLP.leftMargin = leftMargin
 				+ DensityAdaptor.getDensityIndependentValue(70);
 		inputLP.topMargin = topMargin
 				- DensityAdaptor.getDensityIndependentValue(10);
 		mMainLayout.addView(input, inputLP);
 
 		return input;
 	}
 
 	private void doAfterLogin(int returnCode) {
 		if (CloudAPI.isSuccessful(this.getContext(), returnCode)) {
 			HomeActivity.getApp().getBookGallery().updatePanels(UserInfo.getCurLoggedinUser());
 			HomeActivity.getApp().getSocialPanel().getFriendsView().refreshList();
 			this.dismiss();
 			HomeActivity.getApp().toggleSignUpPanel(false);
 		}
 	}
 
 	private void login() {
 		CloudAPI.login(this.getContext(), mEmailInput.getText().toString(),
 				mPasswordInput.getText().toString(),
 				new ICloudAPITaskListener() {
 
 					@Override
 					public void onFinish(int returnCode) {
 						doAfterLogin(returnCode);
 					}
 
 				});
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 		super.show();
 		if (!AppConfig.isDebugMode) {
 			UserInfo userInfo = UserInfo.getCurLoggedinUser();
 			if (null != userInfo) {
 				this.mEmailInput.setText(userInfo.getEmail());
 				this.mPasswordInput.setText("");
 			}
		} else {
			this.mEmailInput.setText("tom.dong@openlib.com");
			this.mPasswordInput.setText("dong");
 		}
 	}
 
 	private void cancelLogin() {
 		this.dismiss();
 	}
 }
