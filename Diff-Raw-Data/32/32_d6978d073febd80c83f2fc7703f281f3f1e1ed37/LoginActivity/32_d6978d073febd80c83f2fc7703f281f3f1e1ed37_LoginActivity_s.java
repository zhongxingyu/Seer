 package com.app.getconnected.activities;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 
 import com.app.getconnected.R;
 import com.app.getconnected.rest.RESTRequest;
 import com.app.getconnected.rest.RESTRequest.Method;
 import com.exception.getconnected.FieldValidationException;
 import com.util.getconnected.FieldValidator;
 import com.util.getconnected.JSONParser;
 
 public class LoginActivity extends BaseActivity {
 	
 	EditText fieldUsername;
 	EditText fieldPassword;
 	EditText focusView;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		initLayout(R.string.title_activity_loginactivity, true, true, true, false);
		
 		fieldUsername = (EditText) findViewById(R.id.username);
 		fieldPassword = (EditText) findViewById(R.id.password);
 	}
 	
 	public void register(View view)
 	{
 		Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
 		startActivityForResult(intent, 1);
 	}
 	
 	public void attemptLogin(View view)
 	{
 		Boolean validInput=true;
 		EditText[] fieldsToValidate={fieldUsername, fieldPassword};
 		for(EditText textField : fieldsToValidate)
 		{
 			textField.setError(null);
 			try {
 				FieldValidator.validateTextField(textField);
 			} catch (FieldValidationException e) {
 				textField.setError(getString(e.getIndex()));
 				validInput=false;
 			}
 		}
 		if(validInput)
 		{
 			if(attemptApiLogin())
 			{
 				loggedIn=true;
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
 				startActivityForResult(intent, 1);
 			}
 			else
 			{
 				fieldUsername.setError(getString(R.string.login_incorrect_credentials));
 			}
 		}
 	}
 	private boolean attemptApiLogin()
 	{
 		String username=fieldUsername.getText().toString();
 		String password=fieldPassword.getText().toString();		
 		Map<String, String> hashMap = new HashMap<String,String>();
 		hashMap.put("username", username);
 		hashMap.put("password", password);
 		//TODO put api base somewhere central/logical.
 		String apiBase="http://127.0.0.1/OpenRideServer-RS/resources/";
 		//TODO find out the proper login url
 		String url=apiBase+"user/login";
 		RESTRequest request = new RESTRequest(url);
 		request.setMethod(Method.POST);
 		String result="";
 		try {
 			JSONObject jObj=JSONParser.getInstance().parseMapAsObject(hashMap);
 			String jsonString="{\"LoginRequest\":"+jObj.toString()+"}";
 			request.putString("json", jsonString);
 			//TODO implement once API is reachable
 			//result = request.execute().get();
 			if(username.equals("user"))
 				result="200 OK";
 			else
 				result="Unknown";
 		/*} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		} catch (ExecutionException e1) {
 			e1.printStackTrace();*/
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return result.equals("200 OK");
 	}
 }
