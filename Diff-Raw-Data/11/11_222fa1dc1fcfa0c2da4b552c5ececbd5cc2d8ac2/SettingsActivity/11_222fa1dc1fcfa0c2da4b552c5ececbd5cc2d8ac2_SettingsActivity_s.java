 package com.ardaxi.authenticator;
 
 import java.text.MessageFormat;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class SettingsActivity extends PreferenceActivity {
 	private SharedPreferences sharedPreferences;
 	private enum Setting
 	{
 		ENCRYPTION, LEGAL, NOVALUE;
 
 		public static Setting toSetting(String str)
 		{
 			try {
 				return valueOf(str.toUpperCase());
 			}
 			catch (Exception e)
 			{
 				return NOVALUE;
 			}
 		}
 		
 		public String toString()
 		{
 			return this.name().toLowerCase();
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {     
 		super.onCreate(savedInstanceState);
 		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 		addPreferencesFromResource(R.xml.preferences);
 		refresh();
 	}
 	
 	private void refresh()
 	{
 		getPreferenceScreen().findPreference(Setting.ENCRYPTION.toString()).setSummary(sharedPreferences.getBoolean(Setting.ENCRYPTION.toString(), false) ? R.string.changepin : R.string.enablepin);
 	}
 
 	@Override
 	public boolean onPreferenceTreeClick (PreferenceScreen preferenceScreen, Preference preference)
 	{
 		switch(Setting.toSetting(preference.getKey()))
 		{
 		case ENCRYPTION:
 			AccountsDbAdapter.initialize(this);
 			final View frame = getLayoutInflater().inflate(R.layout.pin,
 					(ViewGroup) findViewById(R.id.pin_root));
 			final EditText nameEdit = (EditText) frame.findViewById(R.id.pin_edittext);
 			final Builder dialogBuilder = new AlertDialog.Builder(this)
 			.setView(frame)
 			.setTitle("Enter new PIN")
 			.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					new Crypto().execute("encrypt".toCharArray(),nameEdit.getText().toString().toCharArray());
 				}
 			});
 			if(!sharedPreferences.getBoolean(Setting.ENCRYPTION.toString(), false)) // Only decrypt when encrypted.
 			{
 				dialogBuilder.show();
 				return true;
 			}
 			new AlertDialog.Builder(this)
 			.setView(frame)
 			.setTitle("Enter old PIN")
 			.setPositiveButton("Change",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							new Crypto().execute("decrypt".toCharArray(),nameEdit.getText().toString().toCharArray());
 							dialogBuilder.show();
 						}
 					})
 					.setNegativeButton("Disable", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							new Crypto().execute("decrypt".toCharArray(),nameEdit.getText().toString().toCharArray());
 						}
 					})
 					.setNeutralButton("Cancel", null)
 					.show();
 			return true;
 		case LEGAL:
 			startActivity(new Intent(this, LegalActivity.class));
 			return true;
 		}
 		return super.onPreferenceTreeClick(preferenceScreen, preference);
 	}
 	
 	private class Crypto extends AsyncTask<char[], String, Boolean>
 	{
 		private DialogInterface.OnCancelListener _cancelListener = new DialogInterface.OnCancelListener() {
 
 			public void onCancel(DialogInterface dialog) {
 				Crypto.this.cancel(true);
 			}
 		};
 		private ProgressDialog dialog;
 		private String error = null;
 		
 		private ProgressDialog getDialog()
 		{
 			if(dialog == null)
 			{
 				dialog = ProgressDialog.show(SettingsActivity.this, "", "Loading...", true, false);
 				dialog.setOnCancelListener(_cancelListener);
 			}
 			return dialog;
 		}
 
 		@Override
 		protected void onCancelled()
 		{
 			getDialog().dismiss();
 			if(error != null)
 				showAlertDialog(error);
 			else
 				Toast.makeText(SettingsActivity.this, "Y U NO WAIT?", Toast.LENGTH_SHORT).show();
 		}
 
 		protected void onPreExecute()
 		{
 			getDialog();
 		}
 		
 		@Override
 		protected Boolean doInBackground(char[]... arg0) {
			boolean encrypt = arg0[0].toString().equals("encrypt");
 			publishProgress(encrypt ? "Encrypting." : "Decrypting.");
 			CryptoHelper crypto = new CryptoHelper(SettingsActivity.this, arg0[1]);
 			Cursor cursor = AccountsDbAdapter.getSecrets();
 			cursor.moveToFirst();
 			while(!cursor.isAfterLast())
 			{
 				String clientSecret = cursor.getString(cursor.getColumnIndex(AccountsDbAdapter.CLIENT_SECRET_COLUMN));
 				String serverSecret = cursor.getString(cursor.getColumnIndex(AccountsDbAdapter.SERVER_SECRET_COLUMN));
 				int id = cursor.getInt(cursor.getColumnIndex(AccountsDbAdapter.ID_COLUMN));
 				if(encrypt)
 					AccountsDbAdapter.updateSecrets(id, crypto.encrypt(clientSecret), crypto.encrypt(serverSecret));
 				else
 					AccountsDbAdapter.updateSecrets(id, crypto.decrypt(clientSecret), crypto.decrypt(serverSecret));
 			}
 			crypto.close();
 			return encrypt;
 		}
 		
 		protected void onProgressUpdate(String... progress)
 		{
 			getDialog().setMessage(MessageFormat.format("{0} {1}", progress[0], SettingsActivity.this.getResources().getString(R.string.please_wait)));
 		}
 
 		protected void onPostExecute(Boolean result)
 		{
 			getDialog().dismiss();
 			sharedPreferences.edit().putBoolean(Setting.ENCRYPTION.toString(), result).commit();
 			refresh();
 		}
 	}
 	
 	private void showAlertDialog(String message)
 	{
 		new AlertDialog
 		.Builder(this)
 		.setTitle(R.string.error)
 		.setMessage(message)
 		.setIcon(android.R.drawable.ic_dialog_alert)
 		.setCancelable(false)
 		.setNegativeButton(R.string.okay, null)
 		.show();
 	}
 }
