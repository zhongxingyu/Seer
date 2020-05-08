 package com.example.forum_app.test;
 
 import static org.junit.Assert.*;
 import junit.framework.Assert;
 
 import com.example.forum_app.*;
 
 import org.junit.Test;
 
 import android.content.res.Resources;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.UiThreadTest;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class ShowPosts extends ActivityInstrumentationTestCase2<ShowPostsActivity>{
 	
 	private ShowPostsActivity spactivity;
 	private Button btRegister;
 	private EditText etNickname;
 	private EditText etPassword;
 	private EditText etPasswordConfirm;
 	private Spinner spCountry;
 	private EditText etEmail;
 	private Spinner spGender;
 	private TextView tvRegisterError; 
 	
 	Resources res;
 	
 	@SuppressWarnings("deprecation")
 	public ShowPosts() {
 		super(ShowPostsActivity.class);
 	}
 	
 	
 	@Override
 	protected void setUp() throws Exception {
 		//super.setUp();
 		//setActivityInitialTouchMode(false);
 	    this.spactivity = getActivity();
 		//setActivityInitialTouchMode(false);
 		/*
 		 this.btRegister = (Button) this.spactivity.findViewById(com.example.forum_app.R.id.btRegister);
 		 */
 		this.res = spactivity.getResources();
 	} // end of setUp() method definition
 	
 	@UiThreadTest
 	public void testPostData() throws Throwable {
 		this.spactivity.runOnUiThread(new Runnable() {
 			public void run() {
 				// insert post
				// 
				etNickname.setText("");
				etPassword.setText("Horst123");
				etPasswordConfirm.setText("Horst123");
				spCountry.setSelection(0);
				spGender.setSelection(0);
				etEmail.setText("Host@Horsti.at");
				
				btRegister.performClick();
				Resources res = spactivity.getResources();
				//System.out.println("debug___" + tvRegisterError.getText().toString());
				//System.out.println("debug___" + res.getString(com.example.forum_app.R.string.err_missing_nickname));
				String error_message =  tvRegisterError.getText().toString();
				Assert.assertEquals(error_message, res.getString(com.example.forum_app.R.string.err_missing_nickname));
				
				etNickname.setText("Horst");
				btRegister.performClick();
				error_message =  tvRegisterError.getText().toString();
				Assert.assertEquals(error_message, "");
 		    }
 		});
 	}
 
 }
