 package com.example.wecharades.presenter;
 
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.os.AsyncTask;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.view.Window;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.example.wecharades.R;
 import com.example.wecharades.model.DatabaseException;
 import com.example.wecharades.views.LoginActivity;
 import com.example.wecharades.views.ResetPasswordActivity;
 
 
 public class ResetPresenter extends Presenter {
 
 	private ResetPasswordActivity activity;
 	private String email;
 	private View parentView;
 	private DatabaseException dbException;
 
 	public ResetPresenter(ResetPasswordActivity activity) {
 		super(activity);
 		this.activity = activity;
 		parentView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
 	}
 
 	public void resetPassword(String email, View myView, ProgressBar resetProgress) {
 		this.email = email;
 		ResetPasswordTask reset = new ResetPasswordTask();
 		reset.execute();
 
 	}
 
 	private class ResetPasswordTask extends AsyncTask<Void, Long, Boolean>{
 
 		private int exceptionState = 0;
 		private final static int NO_EXCEPTION = 0;
 		private final static int CAUGHT_EXCEPTION = 1;
 
 		public ResetPasswordTask(){
 
 		}
 		@Override
 		protected void onPreExecute(){
 			//Show the progress spinner
 			activity.showProgressSpinner(getAllChildren(parentView));
 		}
 
 		@Override
 		protected Boolean doInBackground(Void... arg0) {
 			try {
 				dc.resetPassword(email);
 			} catch (DatabaseException e) {
 				dbException = e;
 				exceptionState = CAUGHT_EXCEPTION;
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result){
 			if(exceptionState == CAUGHT_EXCEPTION){
 				final Dialog dialog = new Dialog(activity);
 				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 				dialog.setContentView(R.layout.dialog_error);
 				dialog.setCanceledOnTouchOutside(false);
 				dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));               
 
 				TextView errorText = (TextView) dialog.findViewById(R.id.errorText);
 				errorText.setText(dbException.prettyPrint());
 
 				Button ok = (Button) dialog.findViewById(R.id.ok);
 				ok.setOnClickListener(new OnClickListener() {          
 					public void onClick(View v) {
 						dialog.dismiss();
 					}
 				});
 
 				dialog.show();
 			}
 			else{
 
 				final Dialog dialog = new Dialog(activity);
 				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 				dialog.setContentView(R.layout.dialog_success);
 				dialog.setCanceledOnTouchOutside(false);
 				dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));               
 
 				TextView successText = (TextView) dialog.findViewById(R.id.successText);
 				successText.setText("Email has been sent");
 
 				Button ok = (Button) dialog.findViewById(R.id.ok);
 				ok.setOnClickListener(new OnClickListener() {          
 					public void onClick(View v) {
 						dialog.dismiss();
						Intent i = new Intent(activity.getApplicationContext(), LoginActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						activity.startActivity(i);
 					}
 				});
 
 				dialog.show();
 			}
 			exceptionState = NO_EXCEPTION;
 			activity.hideProgressSpinner(getAllChildren(parentView));
 		}
 	}
 
 	public void setListeners(EditText emailInput) {
 		emailInput.setOnEditorActionListener(new OnEditorActionListener() {
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_DONE) {
 					activity.onClickResetPassword(v);
 					return true;
 				} else {
 					return false;
 				}
 			}
 		});
 		
 	}
 }
