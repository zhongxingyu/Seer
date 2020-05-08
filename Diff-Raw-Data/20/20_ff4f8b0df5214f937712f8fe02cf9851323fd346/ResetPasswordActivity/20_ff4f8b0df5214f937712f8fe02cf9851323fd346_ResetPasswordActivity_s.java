 package com.example.wecharades.views;
 
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.example.wecharades.R;
 import com.example.wecharades.presenter.ResetPresenter;
 
 
 public class ResetPasswordActivity extends GenericActivity {
 
 	EditText emailInput;
 	TextView errorMsg;
 	ProgressBar resetProgress;
 	View myView;
 	ResetPresenter presenter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState, new ResetPresenter(this));
 		setContentView(R.layout.resetpassword);
 		//initializing the presenter
 		presenter = (ResetPresenter) super.getPresenter();
 
 		//Getting the view associated with this Activity
 		myView = getWindow().getDecorView().findViewById(android.R.id.content);
 		emailInput				= (EditText) findViewById(R.id.resetPassword);
 		resetProgress			= (ProgressBar) findViewById(R.id.progress);
//		presenter.setProgressSpinnerInvisible(resetProgress);
 	}
 	
 	/**
 	 * 
 	 */
 	public void onStart(){
 		super.onStart();
 		presenter.setListeners(emailInput);
 		
 	}
 
 	/**
 	 * The user should enter it's e-mail to receive a new password
 	 * @param arg0
 	 */
 	public void onClickResetPassword(View view) {
 		presenter.resetPassword(emailInput.getText().toString(), myView, resetProgress);
 	}
 
 
 	@Override
 	public TextView getTextArea() {
 		return errorMsg;
 	}
 
 	public void showSuccessMessage(String message) {
 		errorMsg.setTextColor(Color.parseColor("#458B00"));
 		errorMsg.setText(message);
 	}
 
 	@Override
 	protected ProgressBar getProgressSpinner() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
