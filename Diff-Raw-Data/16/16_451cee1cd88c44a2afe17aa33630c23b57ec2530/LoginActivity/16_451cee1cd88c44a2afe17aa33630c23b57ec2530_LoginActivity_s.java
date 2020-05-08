 package gecko.mybaby.view;
 
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

 import gecko.mybaby.R;
 import gecko.mybaby.model.Baby;
 import gecko.mybaby.webservice.AddRemoteUser;
 import gecko.mybaby.webservice.LoginAutenticator;
 import gecko.mybaby.webservice.LoginAutenticator.LoginCallback;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class LoginActivity extends Activity implements LoginCallback {
 	
 	private EditText username;
 	private EditText password;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.activity_login);
 		
 		this.getReferences();
 	}
 	
 	private void getReferences() {
 
 		this.username = (EditText) this.findViewById(R.id.username);
 		this.password = (EditText) this.findViewById(R.id.password);
 	}
 
 	public void loginClicked(View view) {
 		
 		String usernameStr = this.username.getText().toString();
 		String passwordStr = this.password.getText().toString();
 
 		if (usernameStr.equals("")) {
 			
 			Toast.makeText( this,
 						    this.getResources().getString(R.string.username_not_set),
 						    Toast.LENGTH_LONG ).show();
 			return;
 		}
 		if (passwordStr.equals("")) {
 			
 			Toast.makeText( this,
 						    this.getResources().getString(R.string.password_not_set),
 						    Toast.LENGTH_LONG ).show();
 			return;
 		}
 		
 		MessageDigest md = null;
 		
 		try {
 			
 			md = MessageDigest.getInstance("SHA-512");
 		} catch (NoSuchAlgorithmException e) {
 			
 			e.printStackTrace();
 			return;
 		}
 		
 		md.update(passwordStr.getBytes());
 		byte[] hashMd5 = md.digest();
 		
 		LoginAutenticator.autenticateLogin(usernameStr, this.stringHexa(hashMd5).toUpperCase(), this);
 	}
 
 	public void addUserClicked(View view) {
 		Intent intent = new Intent(this, AddRemoteUser.class);
 		this.startActivity(intent);
		
 	}
 
 	private String stringHexa(byte[] bytes) {
 		
 		StringBuilder s = new StringBuilder();
 		for (int i = 0; i < bytes.length; i++) {
 			
 		    int parteAlta = ((bytes[i] >> 4) & 0xf) << 4;
 		    int parteBaixa = bytes[i] & 0xf;
 		    
 		    if (parteAlta == 0) {
 		    	
 		    	s.append('0');
 		    }
 		    
 		    s.append(Integer.toHexString(parteAlta | parteBaixa));
 		}
 		return s.toString();
 	}
 
 	@Override
 	public void loginResponse(int status, ArrayList<Baby> babysList, String usernameStr, String passwordStr) {
 
 		final int finalstatus = status;
 		final ArrayList<Baby> finalBabysList = babysList;
 		final String finalUsernameStr = usernameStr;
 		final String finalPasswordStr = passwordStr;
 		
 		this.runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				
 				if (finalstatus == LoginCallback.STATUS_ERROR) {
 					
 					Toast.makeText( LoginActivity.this,
 									LoginActivity.this.getResources().getString(R.string.wrong_login_or_password),
 								    Toast.LENGTH_LONG ).show();
 					return;
 				} else if (finalstatus == LoginCallback.STATUS_SUCCESS) {
 					
 					//Initiate MyBabyActivity.
 					Intent intent = new Intent(LoginActivity.this, MyBabyActivity.class);
 					intent.putExtra("babys", finalBabysList);
 					intent.putExtra("username", finalUsernameStr);
 					intent.putExtra("password", finalPasswordStr);
 //					intent.putExtra("password", this.stringHexa(hashMd5).toUpperCase());
 					LoginActivity.this.startActivity(intent);
 					
 					LoginActivity.this.finish();
 				}
 			}
 		
 		});
 	}
 
 }
