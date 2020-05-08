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
 import nl.coralic.beta.sms.utils.objects.Key;
 import nl.coralic.beta.sms.utils.objects.Response;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
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
 
     // TODO: integration Test case
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.wizard);
 	spnProvider = (Spinner) findViewById(R.id.spnProvider);
 	adapter = ArrayAdapter.createFromResource(this, R.array.providers, android.R.layout.simple_spinner_item);
 	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	spnProvider.setAdapter(adapter);
 
 	txtUsername = (EditText) findViewById(R.id.txtUsername);
 	txtPassword = (EditText) findViewById(R.id.txtPassword);
 
 	done = (Button) findViewById(R.id.btnWizardNext);
 	done.setOnClickListener(new View.OnClickListener()
 	{
 	    public void onClick(View v)
 	    {
 		checkAccount();
 	    }
 	});
     }
 
     private void checkAccount()
     {
 	if (txtUsername.getText().toString().equals(""))
 	{
 	    Toast.makeText(Wizard.this, getString(R.string.TOAST_USERNAME_EMPTY), Toast.LENGTH_SHORT).show();
 	    return;
 	}
 	if (txtPassword.getText().toString().equals(""))
 	{
 	    Toast.makeText(Wizard.this, getString(R.string.TOAST_PASSWORD_EMPTY), Toast.LENGTH_SHORT).show();
 	    return;
 	}
 
 	task = new AsyncTask<Void, Void, Response>()
 	{
 	    @Override
 	    protected void onPreExecute()
 	    {
 		dialog = ProgressDialog.show(Wizard.this, "Verifying", Wizard.this.getString(R.string.ALERT_VERIFYING), true, true, new DialogInterface.OnCancelListener()
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
 		return BetamaxHandler
 			.sendSMS(adapter.getItem((int) spnProvider.getSelectedItemId()).toString(), txtUsername.getText().toString(), txtPassword.getText().toString(), "00", "00", "fake");
 	    }
 
 	    @Override
 	    protected void onPostExecute(Response response)
 	    {
 		dialog.dismiss();
 		// If we get an error as response then it means the username/password is wrong, otherwise it should be oke
		if ("error".equals(response.getErrorMessage()))
 		{
 		    Toast.makeText(Wizard.this, Wizard.this.getString(R.string.ALERT_VERIFY_FAILED_USERPASS), Toast.LENGTH_LONG).show();
 		}
 		else
 		{
 		    // If one of these then an http error occurred, in other cases it is oke
 		    if (R.string.ERR_CONN_ERR == response.getErrorCode() || R.string.ERR_NO_ARGUMENTS == response.getErrorCode() || R.string.ERR_PROV_NO_RESP == response.getErrorCode())
 		    {
 			Toast.makeText(Wizard.this, Wizard.this.getString(R.string.ALERT_VERIFY_FAILED) + " " + response.getErrorMessage(), Toast.LENGTH_LONG).show();
 		    }
 		    else
 		    {
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Wizard.this);
 			SharedPreferences.Editor editor = prefs.edit();
 			editor.putBoolean(Key.VERIFIED.toString(), true);
 			editor.putString(Key.USERNAME.toString(), txtUsername.getText().toString());
 			editor.putString(Key.PASSWORD.toString(), txtPassword.getText().toString());
 			editor.putFloat(Key.PROVIDER.toString(), spnProvider.getSelectedItemId());
 			editor.commit();
 			// We can just close this activity so it is not on the activity stack anymore
 			finish();
 		    }
 		}
 	    }
 	};
 	task.execute();
     }
 }
