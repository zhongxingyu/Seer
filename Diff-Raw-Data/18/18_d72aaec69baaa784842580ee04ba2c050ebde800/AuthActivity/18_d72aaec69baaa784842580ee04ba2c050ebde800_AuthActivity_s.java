 package org.dianmobile.droplet.activity;
 
 import org.dianmobile.droplet.R;
 import org.dianmobile.droplet.utils.Utils;
 import static org.dianmobile.droplet.constants.Constants.*;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 /**
  * ֤ҳ
  * ֤ھͲ
  * û֤ھͳ
  * 
  * @author FreeTymeKiyan
 * @version 0.0.2
  */
 public class AuthActivity extends Activity {
 
 	/*ҳؼ*/
 	/**applogo*/
 	private ImageView ivLogo;
 	/**˺ؼ*/
 	private EditText etUser;
 	/**ؼ*/
 	private EditText etCode;
 	/**¼ť*/
 	private Button btnLogIn;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		/*֤Ϣ*/
 		boolean authInfo = checkAuthInfo();
 		if (authInfo) {
 			logIn(); // ֤ϢЧҳ
 		}
 		/*֤ϢЧʼ֤*/
 		setContentView(R.layout.activity_auth);
 		initViews();
 	}
 
 	/**
 	 * ʼҳؼ
 	 */
 	private void initViews() {
 		/*˺*/
 		etUser = (EditText) findViewById(R.id.et_user);
 		/**/
 		etCode = (EditText) findViewById(R.id.et_code);
 		/*¼ť*/
 		btnLogIn = (Button) findViewById(R.id.btn_logIn);
 		btnLogIn.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if (Utils.checkNetworkState(AuthActivity.this)) {
 					/*ӳɹ*/
 					/*ץȡû*/
 					String strUser = etUser.getText().toString();  
 					String strCode = etCode.getText().toString();
 					/*жǷϷ*/
 					boolean inputValidity = checkInputValidity
 							(strUser, strCode);
 					if (inputValidity) {
 						// TODO ֤
 						// TODO ֤ɹ¼֤ݿ
 						// TODO ֤ʧܣ
 					}
 				} else {
 					/**/
 					Toast.makeText(AuthActivity.this, 
 							R.string.toast_network_not_connected, 
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 	}
 
 	/**
 	 * ûǷϷ
 	 * 
 	 * @param strUser
 	 * @param strCode
 	 * @return	false	Ϸ
 	 * 			true	Ϸ
 	 */
 	private boolean checkInputValidity(String strUser, 
 			String strCode) {
 		boolean temp = false;
		if (strUser == null) { // ûΪ
 			Toast.makeText(AuthActivity.this, R.string
 					.toast_username_null, Toast.LENGTH_SHORT)
 					.show();
		} else if (strCode == null) { // Ϊ
 			Toast.makeText(AuthActivity.this, R.string
 					.toast_usercode_null, Toast.LENGTH_SHORT)
 					.show();
 		} else {
 			temp = true;
 		}
 		return temp;
 	}
 
 	/**
 	 * û˺API֤
 	 */
 	private void auth() {
 		
 	}
 
 	/**
 	 * ֤Ϣã¼ҳ
 	 */
 	private void logIn() {
 		Intent i = new Intent();
 		i.setClass(AuthActivity.this, MainActivity.class);
 		/*ҳԼ*/
 		startActivity(i);
 		finish(); 
 	}
 
 	/**
 	 * ֤Ϣݿ
 	 * 
 	 * @return 	true	¼
 	 * 			false	Ҫ֤
 	 */
 	private boolean checkAuthInfo() {
 		boolean temp = false;
 		SharedPreferences sp = getSharedPreferences(
 				AUTH_PREF_NAME, MODE_PRIVATE);
 		if (sp.contains(AUTH_RENREN_USER_NAME) && 
 				sp.contains(AUTH_RENREN_USER_CODE)) {
 			/*ɹ֤ûϢ*/
 			temp = true;
 		}
 		return temp;
 	}
 }
