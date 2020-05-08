 /**
  * 
  */
 package whyq.activity;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 
 import oauth.signpost.OAuth;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.message.BasicNameValuePair;
 
 import whyq.WhyqApplication;
 import whyq.WhyqMain;
 import whyq.interfaces.JoinPerm_Delegate;
 import whyq.model.User;
 import whyq.utils.API;
 import whyq.utils.Constants;
 import whyq.utils.RSA;
 import whyq.utils.Util;
 import whyq.utils.XMLParser;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.whyq.R;
 
 
 /**
  * @author Linh Nguyen This activity supports to create new Perming account.
  */
 public class JoinWhyqActivity extends Activity implements TextWatcher,
 		JoinPerm_Delegate {
 	private static final int GET_IMAGE = 0;
 	// Button createAccount;
 	EditText firstName;
 	EditText lastName;
 	EditText email;
 	EditText password;
 	EditText confirmPassword;
 	private SharedPreferences prefs;
 	private Button createAccount;
 	private ProgressDialog dialog;
 	private JoinWhyqActivity context;
 	private View progressBar;
 	private ImageView imgAvatar;
 	private String avatarPath;
 	private String firstNameValue;
 	private String lastNameValue;
 	private String emailValue;
 	private String passValue;
 	private String confirmPassValue;
 	private String notify;
 	private EditText userName;
 	private String userNameValue;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.whyq_join);
 
 		context = JoinWhyqActivity.this;
 		TextView textView = (TextView) findViewById(R.id.permpingTitle);
 //		Typeface tf = Typeface.createFromAsset(getAssets(),
 //				"ufonts.com_franklin-gothic-demi-cond-2.ttf");
 		Util.applyTypeface(textView, Util.sTypefaceRegular);
 		textView.setText("New Account");
 //		if (textView != null) {
 //			textView.setTypeface(WhyqApplication.sTypefaceRegular);
 //		}
 
 		imgAvatar = (ImageView)findViewById(R.id.imgAvatar);
 		prefs = PreferenceManager
 				.getDefaultSharedPreferences(getApplicationContext());
 		dialog = new ProgressDialog(context);
 		firstName = (EditText) findViewById(R.id.first_name);
 		lastName = (EditText) findViewById(R.id.last_name);
 		// name.addTextChangedListener(this);
 		// nickName = (EditText) findViewById(R.id.nickName);
 		email = (EditText) findViewById(R.id.email);
 		// email.addTextChangedListener(this);
 		password = (EditText) findViewById(R.id.acccountPassword);
 		// password.addTextChangedListener(this);
 		confirmPassword = (EditText) findViewById(R.id.confirm_password);
 		// confirmPassword.addTextChangedListener(this);
 		userName = (EditText)findViewById(R.id.user_name);
 		progressBar = (ProgressBar) findViewById(R.id.prgBar);
 		createAccount = (Button) findViewById(R.id.loginPerm);
 		createAccount.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				if(checkInputData()){
 					exeSignup();
 				}else{
 //					Util.showDialog(context, notify);
 				}
 			}
 		});
 
 	}
 	protected void exeSignup() {
 		// TODO Auto-generated method stub
 
 		showDialog();
 		// Send request to server to create new account along with its
 		// params
 		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
 				8);
 		// nameValuePairs.add(new BasicNameValuePair("type",
 		// prefs.getString(Constants.LOGIN_TYPE, "")));
 		if (prefs.getString(Constants.LOGIN_TYPE, "").equals(
 				Constants.FACEBOOK_LOGIN)) {// Facebook
 			nameValuePairs.add(new BasicNameValuePair("oauth_token",
 					prefs.getString(Constants.ACCESS_TOKEN, "")));
 			nameValuePairs.add(new BasicNameValuePair("first_name",
 					firstNameValue));// prefs.getString(Constants.ACCESS_TOKEN,
 														// ""))
 			nameValuePairs.add(new BasicNameValuePair("last_name",
 					lastNameValue));
 			nameValuePairs.add(new BasicNameValuePair("email", emailValue));
 			nameValuePairs.add(new BasicNameValuePair("password",
 					passValue));
 			nameValuePairs.add(new BasicNameValuePair("cpassword",
 					confirmPassValue));
 		} else if (prefs.getString(Constants.LOGIN_TYPE, "").equals(
 				Constants.TWITTER_LOGIN)) { // Twitter
 			nameValuePairs.add(new BasicNameValuePair("oauth_token",
 					prefs.getString(OAuth.OAUTH_TOKEN, "")));
 			nameValuePairs.add(new BasicNameValuePair(
 					"oauth_token_secret", prefs.getString(
 							OAuth.OAUTH_TOKEN_SECRET, "")));
 			nameValuePairs.add(new BasicNameValuePair("first_name",
 					firstNameValue));// prefs.getString(Constants.ACCESS_TOKEN,
 														// ""))
 			nameValuePairs.add(new BasicNameValuePair("last_name",
 					lastNameValue));
 			nameValuePairs.add(new BasicNameValuePair("email",emailValue));
 			RSA rsa = new RSA();
 			String pass = null;
 			try {
 				pass = rsa.RSAEncrypt(password.getText().toString());
 			} catch (InvalidKeyException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchAlgorithmException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchPaddingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalBlockSizeException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (BadPaddingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			nameValuePairs
 					.add(new BasicNameValuePair("password", pass));
 			nameValuePairs.add(new BasicNameValuePair("cpassword",
 					confirmPassword.getText().toString()));
 		} else { // Whyq
 			RSA rsa = new RSA();
 			String pass = "", confirmPass = "";
 			try {
 				pass = rsa.RSAEncrypt(passValue);
 				// confirmPass =
 				// rsa.RSAEncrypt(confirmPassword.getText().toString());
 			} catch (InvalidKeyException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchAlgorithmException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchPaddingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalBlockSizeException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (BadPaddingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			nameValuePairs.add(new BasicNameValuePair("email",emailValue));
 			nameValuePairs
 					.add(new BasicNameValuePair("password", pass));
 			// nameValuePairs.add(new BasicNameValuePair("cpassword",
 			// confirmPass));
 			nameValuePairs.add(new BasicNameValuePair("first_name",
 					firstNameValue));// prefs.getString(Constants.ACCESS_TOKEN,
 														// ""))
 			nameValuePairs.add(new BasicNameValuePair("last_name",
 					lastNameValue));
 			
 			
 			try {
 				ByteArrayOutputStream bos = new ByteArrayOutputStream();
 				// Bitmap bm = BitmapFactory.decodeFile(filePath);
 				Bitmap bm = getBitmap2(avatarPath);
 //				bm.compress(CompressFormat.JPEG, 75, bos);
 				bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
 				byte[] data = bos.toByteArray();
 
 				String fileName = new File(avatarPath).getName();
 				ByteArrayBody bab = new ByteArrayBody(data, fileName);
 				nameValuePairs.add(new BasicNameValuePair("avatar",
 						bab.toString()));
 			} catch (Exception e) {
 				// TODO: handle exception
 				e.printStackTrace();
 			}
 
 
 		}
 
 		XMLParser parser = new XMLParser(XMLParser.JOIN_WHYQ,
 				JoinWhyqActivity.this, API.createAccountURL,
 				nameValuePairs);
 		User user = parser.getUser();
 		hideDialog();
 	
 	}
 	protected boolean checkInputData() {
 		// TODO Auto-generated method stub
 		firstNameValue = firstName.getText().toString();
 		lastNameValue = lastName.getText().toString();
 		emailValue =email.getText().toString();
 		passValue = password.getText().toString();
 		confirmPassValue = confirmPassword.getText().toString();
 		userNameValue = userName.getText().toString();
 		notify = "";
 		boolean status = true;
 		if(firstNameValue.equals("")){
 			notify+="\nInput First Name";
 			status = false;
 			firstName.setError("Please input your first name");
 			
 		}
 		if(lastNameValue.equals("")){
 			notify+="Input Last Name";
 			status = false;
 			lastName.setError("Please input your last name");
 		}
 		if(emailValue.equals("")){
 			notify+="Input Email";
 			status = false;
 			email.setError("Please input your email");
		}else{
			if(!Util.isValidEmail(emailValue)){
				notify+="Your email format is not correct. Please try again";
				status = false;
				email.setError("Your email format is not correct. Please try again");
				
			}
 		}
 		if(userNameValue.equals("")){
 			notify+="Input Username";
 			status = false;
 			userName.setError("Please input your username");
 		}
 		if(passValue.equals("")){
 			notify+="Input password";
 			status = false;
 			password.setError("Please input your password");
 		}
 		if(confirmPassValue.equals("")){
 			status = false;
 			notify+="Please input your confirm password";
 			confirmPassword.setError("Please input your confirm password");
 		}
 		if(!passValue.equals(confirmPassValue)){
 			status = false;
			Toast.makeText(context, "Your confirmed password is not match", Toast.LENGTH_SHORT).show();
 		}
 		return status;
 	}
 	private Bitmap getBitmap2(String path) {
 		try {
 
 			final int IMAGE_MAX_SIZE = 1024;// 1200000; // 1.2MP
 			BitmapFactory.Options o = new BitmapFactory.Options();
 			o.inJustDecodeBounds = true;
 			InputStream in = new FileInputStream(path);
 			BitmapFactory.decodeStream(in, null, o);
 			in.close();
 			in = new FileInputStream(path);
 
 			// Decode image size
 
 			double scale = 1;
 			int currentWidth = o.outWidth;
 			if (currentWidth > IMAGE_MAX_SIZE) {
 				scale = currentWidth / IMAGE_MAX_SIZE;
 
 			}
 
 			Bitmap b = null;
 			if (scale > 1) {
 
 				double y = (double) o.outHeight / scale;
 				double x = 1024;
 				b = BitmapFactory.decodeFile(path);
 				Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
 						(int) y, true);
 				b = scaledBitmap;
 				System.gc();
 
 			} else {
 				b = BitmapFactory.decodeFile(path);
 			}
 
 			/*
 			 * Log.d("", "bitmap size - width: " + b.getWidth() +
 			 * ", height: " + b.getHeight() + "");
 			 */
 			return b;
 		} catch (Exception e) {
 			// Log.e("", e.getMessage(), e);
 			return null;
 		}
 	}
 
 	public void afterTextChanged(Editable s) {
 		// TODO Auto-generated method stub
 		if (firstName.getText().toString().equals("")) {
 			firstName.setError("Name is required!");
 		}
 		if (lastName.getText().toString().equals("")) {
 			firstName.setError("Name is required!");
 		}
 		if (email.getText().toString().equals("")) {
 			email.setError("Email is required!");
 		}
 		if (password.getText().toString().equals("")) {
 			password.setError("Password is required!");
 		}
 		if (confirmPassword.getText().toString().equals("")
 				|| !confirmPassword.getText().toString()
 						.equals(password.getText().toString())) {
 			confirmPassword.setError("Confirm password is invalid!");
 		}
 	}
 
 	public void beforeTextChanged(CharSequence s, int start, int count,
 			int after) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onTextChanged(CharSequence s, int start, int before, int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onSuccess(User user) {
 		// TODO Auto-generated method stub
 		hideDialog();
 		if (user != null) {
 			// Store the user object to PermpingApplication
 			WhyqApplication state = (WhyqApplication) getApplicationContext();
 			state.setUser(user);
 			if(getApplicationContext() != null && email != null && password != null) {
 				if(email.getText().toString().length() > 0 && password.getText().toString().length() > 0) {
 					XMLParser.storePermpingAccount(getApplicationContext(), user);
 
 				}
 			}
 			ListActivity.isLogin = true;
 			ListActivity.loginType = 0;
 			Intent intent = new Intent(JoinWhyqActivity.this, WhyqMain.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			 finish();
 		} else {
 			Toast toast = Toast.makeText(getApplicationContext(),
 					"Joined perm failed! Please try again.", Toast.LENGTH_LONG);
 			toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 300);
 			toast.show();
 
 		}
 
 	}
 
 	@Override
 	public void onError() {
 		// TODO Auto-generated method stub
 		hideDialog();
 		Toast toast = Toast.makeText(getApplicationContext(),
 				"Joined perm failed! Please try again.", Toast.LENGTH_LONG);
 		toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 300);
 		toast.show();
 		this.finish();
 	}
 
 	private void showDialog() {
 		// dialog.show();
 		progressBar.setVisibility(View.VISIBLE);
 	}
 
 	private void hideDialog() {
 		// dialog.dismiss();
 		progressBar.setVisibility(View.INVISIBLE);
 	}
 
 	public void takeImageOnclicked(View v) {
 		Intent intent = new Intent(JoinWhyqActivity.this, ImageActivity.class);
 		startActivityForResult(intent,GET_IMAGE);
 	}
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d("onActivityResult Join","");
 	    if (resultCode == RESULT_OK) {
 	        if (requestCode == GET_IMAGE ) {
 	        	avatarPath = data.getStringExtra("path");
 	        	Log.d("onActivityResult Join",""+avatarPath);
 	        	if(avatarPath!=null){
 	        		 File imgFile = new  File(avatarPath);
 	        		if(imgFile.exists()){
 	        			imgAvatar.setImageURI(Uri.fromFile(imgFile));
 	        		}
 	        	}
 	        }
 	    }
 	    
 	    ImageActivity.imagePath = "";
 	}
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 			finish();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	public void onBack(View v) {
 		finish();
 	}
 	
 
 }
