 package com.chinaece.gaia.gui;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.chinaece.gaia.R;
 import com.chinaece.gaia.constant.Gaia;
 import com.chinaece.gaia.db.DataStorage;
 import com.chinaece.gaia.http.OAHttpApi;
 
 public class GaiaActivity extends Activity {
 	private URL formatUrl;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 //		DataStorage.load(this);
 //		if(DataStorage.properties.containsKey("token") && DataStorage.properties.containsKey("url")){
 //			Intent intent = new Intent(GaiaActivity.this,MainActivity.class);
 //			startActivity(intent);	
 //			this.finish();
 //			return;
 //		}
 		setContentView(R.layout.main);
 		Button loginButton = (Button) findViewById(R.id.btnLogin);
 		final EditText url = (EditText) findViewById(R.id.edtOAUrl);
 		final EditText user = (EditText) findViewById(R.id.edtUserId);
 		final EditText password = (EditText) findViewById(R.id.edtPassWord);
 		final EditText domain = (EditText) findViewById(R.id.edtDomain);
 		loginButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				try {
 					formatUrl = new URL(url.getText().toString());
 					ApiTask task = new ApiTask();
 					task.execute(formatUrl.toString(), user.getText()
 							.toString().trim(), password.getText().toString().trim(), domain
 							.getText().toString().trim());
 				} catch (MalformedURLException e) {
 				  Toast.makeText(GaiaActivity.this, "请输入正确的网址", Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 		
 		Button Ebutton = (Button) findViewById(R.id.btnExit);
 		Ebutton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Dialog dialog = new AlertDialog.Builder(GaiaActivity.this)
						.setTitle("提示")
 						.setMessage("确定要退出OA系统")
 						.setPositiveButton("确定",
 								new DialogInterface.OnClickListener() {
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										GaiaActivity.this.finish();
 									}
 								})
 						.setNegativeButton("取消",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										return;
 									}
 								}).create();
 				dialog.show();
 			}
 		});
 	}
 	
 	class ApiTask extends AsyncTask<String, Integer, Boolean> {
 		private ProgressDialog dialog;
 		
 		@Override
         protected void onPreExecute() {
 			dialog = ProgressDialog.show(GaiaActivity.this, "请稍等...", "正在登录...");
         }
 		
 		@Override
 		protected Boolean doInBackground(String... params) {
 			OAHttpApi OaApi = new OAHttpApi(params[0]);
 			boolean flag = OaApi.getToken(params[1], params[2], params[3]);
 			return flag;
 		}
 		
 		@Override
 		protected void onPostExecute(Boolean flag) {
 			dialog.dismiss();
 			if (flag) {
 				DataStorage.properties.put("token", Gaia.USER.getToken());
 				DataStorage.properties.put("url", formatUrl.toString());
 				DataStorage.save(GaiaActivity.this);
 				Intent intent = new Intent(GaiaActivity.this,MainActivity.class);
 				startActivityForResult(intent,11);
 			} else {
 				Toast.makeText(GaiaActivity.this, "请输入合法的用户名和密码",
 						Toast.LENGTH_LONG).show();
 			}
 		}
 		
 	}
 }
