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
				Assert.assertEquals("", "");
 		    }
 		});
 	}
 
 }
