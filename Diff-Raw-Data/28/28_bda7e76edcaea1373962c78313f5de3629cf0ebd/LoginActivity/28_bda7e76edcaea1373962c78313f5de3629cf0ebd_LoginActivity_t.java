 package com.ag.zhaisujie.activity;
 
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.ag.zhaisujie.App;
 import com.ag.zhaisujie.HttpUtil;
 import com.ag.zhaisujie.R;
 import com.ag.zhaisujie.ToastUtil;
 import com.ag.zhaisujie.ValidUtil;
 import com.ag.zhaisujie.model.User;
 /**
  * 
  *    LoginActivity.java
  *     <p>
  *     ¼ҳ
  *     Copyright: Copyright(c) 2013 
  *     @author Gavin_Feng
  *     </p>
  * 
  */
 public class LoginActivity extends Activity {
 	private TimeCount time;
 	private Button backBtn;
 	private EditText phoneTxt;
 	private EditText codeTxt;
 	private Button getCodeBtn;
 	private Button loginBtn;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.login);
 		backBtn=(Button)findViewById(R.id.title_btn_back);
 		backBtn.setVisibility(View.VISIBLE);
 		backBtn.setOnClickListener(listener);
 		phoneTxt=(EditText)findViewById(R.id.login_txt_phone);
 		codeTxt=(EditText)findViewById(R.id.login_txt_pwd);
 		getCodeBtn=(Button)findViewById(R.id.login_btn_get_code);
 		loginBtn=(Button)findViewById(R.id.login_btn_login);
 		getCodeBtn.setOnClickListener(listener);
 		loginBtn.setOnClickListener(listener);
 	}
 	
 	//֤
 	private void getCode(){
 		String phone=phoneTxt.getText().toString().trim();
 		if("".equals(phoneTxt)){
 			ToastUtil.show(this, "ֻ룡");
 			phoneTxt.requestFocus();
 			return;
 		}else if(!ValidUtil.isMobileNO(phone)){
 			ToastUtil.show(this, "ֻǷ");
 			phoneTxt.requestFocus();
 			return;
 		}
 		
 		String rtn=HttpUtil.getCode(phone);
 		if(App.FAIL.equals(rtn)){
 			ToastUtil.show(this, "ȡ֤ʧܣ");
 		}else{
 			time = new TimeCount(60000, 1000);//CountDownTimer
 			time.start();
 			ToastUtil.show(this,"עնϢ");
 		}
 		
 	}
 	private void login(){
 		String phone=phoneTxt.getText().toString().trim();
 		String pwd=codeTxt.getText().toString().trim();
		if("".equals(phone)){
 			ToastUtil.show(this, "ֻ룡");
 			phoneTxt.requestFocus();
 			return;
 		}else if(!ValidUtil.isMobileNO(phone)){
 			ToastUtil.show(this, "ֻǷ");
 			phoneTxt.requestFocus();
 			return;
 		}else if("".equals(pwd)){
 			ToastUtil.show(this, "֤룡");
 			phoneTxt.requestFocus();
 			return;
 		}
 		String rtn=HttpUtil.login(phone, pwd);
 		if(App.FAIL.equals(rtn)){
 			ToastUtil.show(this, "½ʧܣ");
 		}else{
 			try{
 				User user=new User();
 				JSONTokener jsonParser = new JSONTokener(rtn);
 				JSONObject job= (JSONObject)jsonParser.nextValue();
 				user.setUid(job.getInt("uid"));
 				user.setUserName(job.getString("username"));
 				if(App.getInstance()!=null){
 					App.getInstance().setUser(user);
 					MainActivity.initButton();
 					this.finish();
 				}
 			}catch(Exception ex){
 				ex.printStackTrace();
 				ToastUtil.show(this, "½쳣");
 			}
 		}
 	}
 	OnClickListener listener = new OnClickListener() {
 		public void onClick(View v) {
 			Button btn = (Button) v;
 			switch (btn.getId()) {
 			case R.id.title_btn_back:
 				//Intent intent = new Intent(LoginActivity.this, MainActivity.class);
 				//LoginActivity.this.startActivity(intent);
 				LoginActivity.this.finish();
 				break;
 			case R.id.login_btn_get_code:	
 				getCode();
 				break;
 			case R.id.login_btn_login:	
 				login();
 				break;
 			}
 		}
 	};
 	/* һʱڲ */
 	class TimeCount extends CountDownTimer {
 		public TimeCount(long millisInFuture, long countDownInterval) {
 			super(millisInFuture, countDownInterval);// Ϊʱ,ͼʱʱ
 		}
 
 		@Override
 		public void onFinish() {// ʱʱ
 			getCodeBtn.setText("ȡ֤");
 			getCodeBtn.setClickable(true);
 		}
 
 		@Override
 		public void onTick(long millisUntilFinished) {// ʱʾ
 			getCodeBtn.setClickable(false);
 			getCodeBtn.setText("ȡ"+millisUntilFinished / 1000 );
 		}
 	}
 }
