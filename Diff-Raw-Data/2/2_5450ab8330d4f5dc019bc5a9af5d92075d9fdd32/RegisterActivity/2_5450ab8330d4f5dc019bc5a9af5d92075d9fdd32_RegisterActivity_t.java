 package com.app.getconnected.activities;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 
 import com.app.getconnected.R;
 import com.app.getconnected.rest.RESTRequest;
 import com.app.getconnected.rest.RESTRequest.Method;
 import com.exception.getconnected.FieldValidationException;
 import com.util.getconnected.FieldValidator;
 import com.util.getconnected.JSONParser;
 
 public class RegisterActivity extends BaseActivity {
 	
 	EditText fieldUsername;
 	EditText fieldPassword;
 	EditText fieldFirstName;
 	EditText fieldLastName;
 	EditText fieldTelephoneNumber;
 	EditText fieldEmail;
 	RadioGroup fieldGender;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_register);
 		initLayout(R.string.title_activity_registeractivity, true, true, true, false);
 		
 		fieldUsername=(EditText) findViewById(R.id.username);
 		fieldPassword=(EditText) findViewById(R.id.password);
 		fieldFirstName=(EditText) findViewById(R.id.first_name);
 		fieldLastName=(EditText) findViewById(R.id.last_name);
 		fieldTelephoneNumber=(EditText) findViewById(R.id.telephone_number);
		fieldEmail=(EditText) findViewById(R.id.emailText);
 		fieldGender=(RadioGroup) findViewById(R.id.gender);
 	}
 	
 	public void register(View view)
 	{
 		Boolean validInput=true;
 		EditText[] fieldsToValidate={
 			fieldUsername, 
 			fieldPassword, 
 			fieldFirstName, 
 			fieldLastName, 
 			fieldTelephoneNumber, 
 			fieldEmail
 		};
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
 			if(attemptApiRegister())
 			{
 				Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
 				startActivityForResult(intent, 1);
 			}
 			else
 			{
 				fieldUsername.setError(getString(R.string.registration_failed));
 			}
 		}
 	}
 	private boolean attemptApiRegister()
 	{
 		String username=fieldUsername.getText().toString();
 		String password=fieldPassword.getText().toString();
 		String firstName=fieldFirstName.getText().toString();
 		String lastName=fieldLastName.getText().toString();
 		String telephoneNumber=fieldTelephoneNumber.getText().toString();
 		String email=fieldEmail.getText().toString();
 		String gender=((RadioButton)findViewById(fieldGender.getCheckedRadioButtonId())).getTag().toString();
 		
 		Map<String, String> hashMap = new HashMap<String,String>();
 		hashMap.put("username", username);
 		hashMap.put("randomPass", password);
 		hashMap.put("firstname", firstName);
 		hashMap.put("lastname", lastName);
 		hashMap.put("telephonenumber", telephoneNumber);
 		hashMap.put("email", email);
 		hashMap.put("gender", gender);
 		
 		//TODO put api base somewhere central/logical.
 		String apiBase="http://127.0.0.1/OpenRideServer-RS/resources/";
 		String url=apiBase+"register";
 		RESTRequest request = new RESTRequest(url);
 		request.setMethod(Method.POST);
 		String result="";
 		//{"RegisterRequest":{"username":"pdevos","randomPass":"randomPass","firstname":"Peter","lastname":"de Vos","gender":"M","email":"example@itract-project.eu","mobilePhoneNumber":"0612345678"}}
 		try {
 			JSONObject jObj=JSONParser.getInstance().parseMapAsObject(hashMap);
 			String jsonString="{\"RegisterRequest\":"+jObj.toString()+"}";
 			Log.d("DEBUG", jsonString);
 			request.putString("json", jsonString);
 			//TODO implement once API is reachable
 			//result = request.execute().get();
 			result="201 Created";
 		/*} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		} catch (ExecutionException e1) {
 			e1.printStackTrace();*/
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return result.equals("201 Created");
 	}
 }
