 package com.rogoapp.auth;
 
 import com.rogoapp.R;
 import com.rogoapp.RegisterActivity;
 import com.rogoapp.auth.EmailValidator;
 
 import android.accounts.Account;
 import android.accounts.AccountAuthenticatorActivity;
 import android.accounts.AccountManager;
 import android.content.Context;
 //import android.content.ContentResolver;
 import android.content.Intent;
 //import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class RogoAuthenticatorActivity extends AccountAuthenticatorActivity {
 	public static final String PARAM_AUTHTOKEN_TYPE = "auth.token";  
     public static final String PARAM_CREATE = "create";  
   
     public static final int REQ_CODE_CREATE = 1;  
     public static final int REQ_CODE_UPDATE = 2;  
   
     public static final String EXTRA_REQUEST_CODE = "req.code";  
   
     public static final int RESP_CODE_SUCCESS = 0;  
     public static final int RESP_CODE_ERROR = 1;  
     public static final int RESP_CODE_CANCEL = 2; 
     
     public static boolean createToken;
   
     @Override  
     protected void onCreate(Bundle icicle) {  
         // recreates from saved state -- icicle is the same as savedInstanceState  
         super.onCreate(icicle);  
         //TODO allow view to be created for sign in and authorization forms
         //creates a view from the login.xml file
         this.setContentView(R.layout.login);
         Button button = (Button) this.findViewById(R.id.link_to_register);
         button.setBackgroundColor(Color.TRANSPARENT);
     }  
     
     private boolean getToken(){
     	return createToken;
     }
     private void setToken(boolean Token){
     	createToken = Token;
     }
   
     public void onCancelClick(View v) {  
   
         this.finish();  
   
     }  
     public void openRegisterScreen(View v){
         final Context context = this;
         Intent intent = new Intent(context, RegisterActivity.class);
        this.finish();
         startActivity(intent);
     }
     public void onRememberMe(View V){
     	Button btnUserName = (Button) this.findViewById(R.id.btnLogin);
     	
     	if(getToken()){
     		this.setToken(false);
     	}
     	else this.setToken(true);
     	
     	if(getToken()){
     		btnUserName.setText("Remember Login");
     	}
     	else btnUserName.setText("Login"); 
     }
   
 	public void onSaveClick(View v) {  
     	EditText tvUsername;  
         EditText tvPassword;
         
         TextView txtUsername;
         TextView txtPassword;
         
         String username;  
         String password;
         
         EmailValidator validate = new EmailValidator();
          
         boolean hasErrors = false;
         boolean badUsername = false;
         boolean badPass = false;
   
         tvUsername = (EditText) this.findViewById(R.id.auth_txt_username);  
         tvPassword = (EditText) this.findViewById(R.id.auth_txt_pswd);
         
         txtUsername = (TextView) this.findViewById(R.id.txt_username);
         txtPassword = (TextView) this.findViewById(R.id.txt_pswd);
 
   
         username = tvUsername.getText().toString();  
         password = tvPassword.getText().toString();  
         
         if(validate.validate(username) && !badUsername){
         	txtUsername.setBackgroundColor(Color.WHITE);
         	txtUsername.setText("Username");
         	tvUsername.setBackgroundResource(R.drawable.blue_border);
         	//tvUsername.setBackgroundColor(Color.TRANSPARENT);
         }
         if(!badPass){
         	txtPassword.setBackgroundColor(Color.WHITE);
         	txtPassword.setText("Password");
         	tvPassword.setBackgroundResource(R.drawable.blue_border);
         	//tvPassword.setBackgroundColor(Color.TRANSPARENT);
         }
  
         if (!validate.validate(username)) {  
             hasErrors = true;
             badUsername = true;
             
             txtUsername.setText("Invalid Email Address");
             txtUsername.setBackgroundColor(Color.RED);
             tvUsername.setBackgroundColor(Color.MAGENTA);
         }  
         else if (password.length() < 6) {  
             hasErrors = true;
             badPass = true;
             
             txtPassword.setText("Incorrect Password");
             txtPassword.setBackgroundColor(Color.RED);
             tvPassword.setBackgroundColor(Color.MAGENTA);  
         }  
         if (hasErrors) {  
             return;  
         }  
           
         // Now that we have done some simple "client side" validation it  
         // is time to check with the server  
   
         //TODO ... perform some network activity here  
   
         // finished  
   
         String accountType = this.getIntent().getStringExtra(PARAM_AUTHTOKEN_TYPE);  
         if (accountType == null) {  
             accountType = AccountAuthenticator.ACCOUNT_TYPE;  
         }  
   
         AccountManager accMgr = AccountManager.get(this);  
   
         if (hasErrors) {  
   
             // handle errors  
   
         } else {  
   
             // This is the magic that adds the account to the Android Account Manager  
             final Account account = new Account(username, accountType);  
             accMgr.addAccountExplicitly(account, password, null);  
   
             // Now we tell our caller, could be the Android Account Manager or even our own application  
             // that the process was successful  
   
             final Intent intent = new Intent();  
             intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);  
             intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
             if(getToken()){
             	
             	intent.putExtra(AccountManager.KEY_AUTHTOKEN, accountType);
             	
             }
             
             this.setAccountAuthenticatorResult(intent.getExtras());  
             this.setResult(RESULT_OK, intent);  
             this.finish();  
   
         }  
     }
 }
