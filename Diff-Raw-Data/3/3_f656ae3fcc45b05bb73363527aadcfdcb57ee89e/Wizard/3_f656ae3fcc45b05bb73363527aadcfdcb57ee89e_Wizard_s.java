 /*
  * Copyright 2011 Armin Čoralić
  * 
  * 	http://blog.coralic.nl
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /**
  * 
  */
 package nl.coralic.beta.sms;
 
 import nl.coralic.beta.sms.betamax.BetamaxHandler;
 import nl.coralic.beta.sms.utils.objects.BetamaxArguments;
 import nl.coralic.beta.sms.utils.objects.Const;
 import nl.coralic.beta.sms.utils.objects.Response;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 /**
  * @author "Armin Čoralić"
  * 
  */
 public class Wizard extends Activity
 {
     Button done;
     EditText txtUsername;
     EditText txtPassword;
     Spinner spnProvider;
     ProgressDialog dialog;
     ArrayAdapter<CharSequence> adapter;
     private AsyncTask<Void, Void, Response> task;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
 	super.onCreate(savedInstanceState);
 	setView();
 	assignUiComponentsToVariables();
 	setListeners();
     }
 
     private void setView()
     {
 	// allow custom title
 	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 	setContentView(R.layout.wizard);
 	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
     }
 
     private void assignUiComponentsToVariables()
     {
 	spnProvider = (Spinner) findViewById(R.id.spnProvider);
 	adapter = ArrayAdapter.createFromResource(this, R.array.providers, android.R.layout.simple_spinner_item);
 	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	spnProvider.setAdapter(adapter);
 	txtUsername = (EditText) findViewById(R.id.txtUsername);
 	txtPassword = (EditText) findViewById(R.id.txtPassword);
 	done = (Button) findViewById(R.id.btnWizardNext);
     }
 
     private void setListeners()
     {
 	done.setOnClickListener(new View.OnClickListener()
 	{
 	    public void onClick(View v)
 	    {
 		if (areValuesCorrect())
 		{
 		    validateAccount();
 		}
 	    }
 	});
     }
 
     private boolean areValuesCorrect()
     {
 	if (txtUsername.getText().toString().equals(""))
 	{
 	    Toast.makeText(Wizard.this, getString(R.string.TOAST_USERNAME_EMPTY), Toast.LENGTH_SHORT).show();
 	    return false;
 	}
 	if (txtPassword.getText().toString().equals(""))
 	{
 	    Toast.makeText(Wizard.this, getString(R.string.TOAST_PASSWORD_EMPTY), Toast.LENGTH_SHORT).show();
 	    return false;
 	}
 	return true;
     }
 
     private void validateAccount()
     {
 	task = new AsyncTask<Void, Void, Response>()
 	{
 	    @Override
 	    protected void onPreExecute()
 	    {
 		dialog = ProgressDialog.show(Wizard.this, Wizard.this.getString(R.string.ALERT_VERIFYING_TITLE), Wizard.this.getString(R.string.ALERT_VERIFYING), true, true,
 			new DialogInterface.OnCancelListener()
 			{
 			    public void onCancel(DialogInterface dialog)
 			    {
 				// If the users presses back button cancel the task
 				task.cancel(true);
 			    }
 			});
 	    }
 
 	    @Override
 	    protected Response doInBackground(Void... v)
 	    {
 		BetamaxArguments arguments = new BetamaxArguments(adapter.getItem((int) spnProvider.getSelectedItemId()).toString(),txtUsername.getText().toString(), txtPassword.getText().toString(), "00", "00", "fake");
 		return BetamaxHandler.sendSMS(arguments);
 	    }
 
 	    @Override
 	    protected void onPostExecute(Response response)
 	    {
 		dialog.dismiss();
 		Log.d(Const.TAG_WZD, "Check isUsernamePasswordValid: " + response.getErrorMessage());
 		if (isUsernamePasswordValid(response.getErrorMessage()))
 		{
 		    Log.d(Const.TAG_WZD, "Check isResponseOke");
 		    if (isResponseOke(response.getErrorCode()))
 		    {
 			setPreferences();
 			// We can just close this activity so it is not on the activity stack anymore
 			finish();
 		    }
 		    else
 		    {
 			Toast.makeText(Wizard.this, Wizard.this.getString(R.string.ALERT_VERIFY_FAILED) + " " + response.getErrorMessage(), Toast.LENGTH_LONG).show();
 		    }
 		}
 		else
 		{
 		    Toast.makeText(Wizard.this, Wizard.this.getString(R.string.ALERT_VERIFY_FAILED_USERPASS), Toast.LENGTH_LONG).show();
 		}
 	    }
 	};
 	task.execute();
     }
 
     private boolean isUsernamePasswordValid(String errorMessage)
     {
	// If we get an error as response then it means the username/password is wrong, otherwise it should be oke
	if ("error".equalsIgnoreCase(errorMessage))
 	{
 	    return false;
 	}
 	return true;
     }
 
     private boolean isResponseOke(int errorCode)
     {
 	// If one of these then an http error occurred, in other cases it is oke
 	if (R.string.ERR_CONN_ERR == errorCode || R.string.ERR_NO_ARGUMENTS == errorCode || R.string.ERR_PROV_NO_RESP == errorCode)
 	{
 	    return false;
 	}
 	return true;
     }
 
     private void setPreferences()
     {
 	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Wizard.this);
 	SharedPreferences.Editor editor = prefs.edit();
 	editor.putBoolean(Const.KEY_VERIFIED, true);
 	editor.putString(Const.KEY_USERNAME, txtUsername.getText().toString());
 	editor.putString(Const.KEY_PASSWORD, txtPassword.getText().toString());
 	editor.putFloat(Const.KEY_PROVIDERID, spnProvider.getSelectedItemId());
 	editor.putString(Const.KEY_PROVIDER, spnProvider.getSelectedItem().toString());
 	editor.commit();
     }
 }
