 package com.example.sicbogameexample;
 
 import java.io.IOException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import sicbo_networks.ConnectionHandler;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 public class ChangePassword extends BaseActivity implements OnClickListener{
 	
 	EditText edtCurrentPassword,edtNewPassword,edtConfirmPassword;
 	Button btnOk;
 	ImageButton imgBack;
 	ConnectionAsync connectionAsync;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_change_password);
 		edtCurrentPassword=(EditText)findViewById(R.id.edt_current_password);
 		edtNewPassword=(EditText)findViewById(R.id.edt_new_password);
 		edtConfirmPassword=(EditText)findViewById(R.id.edt_confirm_password);
 		btnOk=(Button)findViewById(R.id.btn_ok);
 		imgBack=(ImageButton)findViewById(R.id.btn_back);
 		imgBack.setOnClickListener(this);
 		btnOk.setOnClickListener(this);
 		
 	
 	}
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch(v.getId())
 		{
 		case R.id.btn_ok:
			if(edtConfirmPassword.getText().equals(edtNewPassword)!=true)
 				Toast.makeText(this, "Please type again", Toast.LENGTH_LONG).show();
 			else
 			{
 			connectionAsync = new ConnectionAsync();
 			String[] paramsName = { "old_pass", "new_pass" };
 			String[] paramsValue = { edtCurrentPassword.getText().toString().trim(),
 					edtNewPassword.getText().toString().trim()};
 			Object[] params = { GameEntity.getInstance().connectionHandler, this,
 					GameEntity.CHANGE_PASSWORD_TASK, paramsName, paramsValue };
 			connectionAsync.execute(params);
 			}
 			break;
 		case R.id.btn_back:
 			this.finish();
 		break;
 		
 		}
 		
 	}
 	class ConnectionAsync extends AsyncTask<Object, String, Integer> {
 		ConnectionHandler connectionHandler;
 		Activity activity;
         @Override
         protected void onPreExecute() {
         	// TODO Auto-generated method stub
         	super.onPreExecute();
         	createProgressDialog();
         }
 		@Override
 		protected Integer doInBackground(Object... params) {
 			// TODO Auto-generated method stub
 			connectionHandler = (ConnectionHandler) params[0];
 			activity = (Activity) params[1];
 			try {
 				connectionHandler.requestToServer((String) params[2],
 						(String[]) params[3], (Object[]) params[4]);
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Integer value) {
 			try {
 				// dataList = connectionHandler.parseData(responseName);
 				JSONObject result = connectionHandler.getResult();
                  
 				// Create user and move to game scene
 				if(result!=null)
 				{
 				if (result.getBoolean("is_success")) {					
					Intent intent = new Intent(activity,
							SicBoGameActivity.class);
 					createDialog();
					activity.startActivity(intent);
					activity.finish();
 				} else {
 					Toast.makeText(activity,result.getString("message"),
 							Toast.LENGTH_LONG).show();
 				}
 				}
 				else
 					Toast.makeText(ChangePassword.this,"Error network!Please try again", Toast.LENGTH_LONG).show();
 
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
