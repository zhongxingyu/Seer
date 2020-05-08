 package app.xzone.storyline;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 import app.xzone.storyline.adapter.FacebookAdapter;
 
 import com.easy.facebook.android.apicall.GraphApi;
 import com.easy.facebook.android.data.Images;
 import com.easy.facebook.android.data.Photo;
 import com.easy.facebook.android.data.User;
 import com.easy.facebook.android.error.EasyFacebookError;
 import com.easy.facebook.android.facebook.FBLoginManager;
 import com.easy.facebook.android.facebook.Facebook;
 import com.easy.facebook.android.facebook.LoginListener;
 
 public class FacebookLoginActivity extends Activity implements OnClickListener, LoginListener {
 
 	private FBLoginManager fbManager;
 	private Button fbButton;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.signin_facebook);
 
 		fbButton = (Button) findViewById(R.id.fb_signin);
 		fbButton.setOnClickListener(this);
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.fb_signin:
 
			final ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setCancelable(false);
			progressDialog.setMessage("Authenticate ...");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

 			// progressDialog.show();
			FacebookAdapter.SigninWithFacebook(this, progressDialog, fbManager);
 
 			break;
 		}
 
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		fbManager.loginSuccess(data);
 
 	}
 
 	
 	@Override
 	public void loginSuccess(Facebook facebook) {
 		GraphApi graphApi = new GraphApi(facebook);
 		User user = new User();
 		
 		try{
 			user = graphApi.getMyAccountInfo();
 			System.out.println("---- email user["+user.getId()+"]: "+user.getEmail());
 			
 			
 			// login success
 			Toast.makeText(this, "Login Success", 200).show();
 			
 			// save into preference
 			System.out.println("------ 1.getAvatarLink(user.getId()):"+getAvatarLink(user.getId()));
 			SharedPreferences.Editor editor = getSharedPreferences("facebook", Activity.MODE_WORLD_READABLE).edit();
 			editor.putString("facebook.me.avatar", getAvatarLink(user.getId()));
 			editor.commit();
 			
 			// go to homeactivity
 			Intent intent = new Intent(this, HomeActivity.class);
 			intent.setAction(Intent.ACTION_VIEW);
 			startActivity(intent);
 			
 		}catch(EasyFacebookError e){
 			e.printStackTrace();
 		}
 		
 
 
 	}
 
 	@Override
 	public void logoutSuccess() {
 		fbManager.displayToast("Logout Success");
 		
 	}
 
 	@Override
 	public void loginFail() {
 		fbManager.displayToast("Login Failed!!!");
 	}
 	
 	
 	private String getAvatarLink(String id){
 		return "http://graph.facebook.com/"+id+"/picture?type=small";
 	}
 
 }
