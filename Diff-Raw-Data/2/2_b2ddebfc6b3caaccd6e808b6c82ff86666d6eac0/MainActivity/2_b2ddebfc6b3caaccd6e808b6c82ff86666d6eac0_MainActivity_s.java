 package arm.testpulsa;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.telephony.SmsManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.Spinner;
 import android.widget.Toast;
 import arm.testpulsa.about.AboutApp;
 import arm.testpulsa.dialogs.ConfirmationDialog;
 import arm.testpulsa.dialogs.PinDialog;
 import arm.testpulsa.model.NominalValue;
 import arm.testpulsa.model.OperatorOption;
 import arm.testpulsa.model.ServerOption;
 
 public class MainActivity extends Activity implements TextWatcher {
 
 	private static final String TAG = MainActivity.class.getSimpleName();
 
 	EditText txtPhone, txtNominal, txtUserPin;
 	Spinner spnOperator;
 	Spinner spnNominal;
 	Spinner spnServer;
 	RadioGroup groupRadio;
 	RadioButton rdoPredefined;
 	Button btnSendForm;
 
 	private static String userPin;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		userPin = prefs.getString("pin", "");
 
 		showPinDialogIfPinNotSet(userPin);
 
 		// Find view
 		txtPhone = (EditText) findViewById(R.id.textPhoneNumber);
 		spnOperator = (Spinner) findViewById(R.id.spinnerOperator);
 		spnServer = (Spinner) findViewById(R.id.spinnerServer);
 		spnNominal = (Spinner) findViewById(R.id.spinnerNominal);
 		groupRadio = (RadioGroup) findViewById(R.id.radiogroupNominal);
 		rdoPredefined = (RadioButton) findViewById(R.id.radioSpinner);
 		btnSendForm = (Button) findViewById(R.id.btn_sendForm);
 
 		// set listener
 		txtPhone.addTextChangedListener(this);
 		groupRadio.setOnCheckedChangeListener(new GroupRadioCheckedChange());
 		btnSendForm.setEnabled(false);
 		btnSendForm.setOnClickListener(new SendButtonOnClick());
 		setAdapter();
 		Log.i(TAG, "onCreate");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.opt_about:
 			startActivity(new Intent(MainActivity.this, AboutApp.class));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void afterTextChanged(Editable s) {
 		try {
 			TextHelper.verifyPhoneNumber(s);
 			btnSendForm.setEnabled(true);
 			txtPhone.setTextColor(Color.BLACK);
 		} catch (ArmPulsaAddressMalformedException e) {
 			btnSendForm.setEnabled(false);
 			txtPhone.setTextColor(Color.RED);
 		}
 	}
 
 	@Override
 	public void beforeTextChanged(CharSequence s, int start, int count,
 			int after) {
 		// Nothing to do here!
 	}
 
 	@Override
 	public void onTextChanged(CharSequence s, int start, int before, int count) {
 		// Noting to do here!
 	}
 
 	private void showPinDialogIfPinNotSet(String pin) {
 		if (pin.length() < 3) {
 			new PinDialog(this).show();
 		}
 	}
 
 	private void setAdapter() {
 		// spinner nominal adapter
 		ArrayAdapter<OperatorOption> spnOperatorOptionAdapter = new ArrayAdapter<OperatorOption>(
 				this, android.R.layout.simple_spinner_item,
 				new OperatorOption[] { new OperatorOption(1, "Telkomsel", "S"),
 						new OperatorOption(2, "Telkomsel Transfer", "P"),
 						new OperatorOption(3, "Flexi", "F"),
 						new OperatorOption(4, "Flexi Transfer", "FT"),
 						new OperatorOption(5, "XL", "X"),
 						new OperatorOption(6, "XL Transfer", "XT"),
 						new OperatorOption(7, "Indosat", "I"),
 						new OperatorOption(8, "Indosat Transfer", "IT"),
 						new OperatorOption(9, "Indosat SMS", "IS"),
 						new OperatorOption(10, "Indosat Internet", "IG"),
 						new OperatorOption(11, "3", "T"),
 						new OperatorOption(12, "Axis", "AX"),
 						new OperatorOption(13, "Axis Transfer", "AXT"),
 						new OperatorOption(14, "Esia", "E"),
 						new OperatorOption(15, "Esia Transfer", "ET"),
 						new OperatorOption(16, "Smartfren", "V"),
 						new OperatorOption(17, "Ceria", "C") });
 		spnOperatorOptionAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spnOperator.setAdapter(spnOperatorOptionAdapter);
 
 		// spinner server adapter
 		ArrayAdapter<ServerOption> spnServerOptionAdapter = new ArrayAdapter<ServerOption>(
 				this, android.R.layout.simple_spinner_item, new ServerOption[] {
 						new ServerOption(1, "Alpha", "+6287792021743"),
 						new ServerOption(2, "Beta", "+6282389230342") });
 		spnServerOptionAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spnServer.setAdapter(spnServerOptionAdapter);
 
 		// spinner nominal adapter
 		ArrayAdapter<NominalValue> spnNominalAdapter = new ArrayAdapter<NominalValue>(
 				this, android.R.layout.simple_spinner_item, new NominalValue[] {
 						new NominalValue(1, "Rp. 5.000", 5),
 						new NominalValue(2, "Rp. 10.000", 10),
 						new NominalValue(3, "Rp. 15.000", 15),
 						new NominalValue(4, "Rp. 20.000", 20),
 						new NominalValue(5, "Rp. 25.000", 25),
 						new NominalValue(6, "Rp. 50.000", 50),
 						new NominalValue(7, "Rp. 100.000", 100) });
 		spnNominalAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spnNominal.setAdapter(spnNominalAdapter);
 	}
 
 	public void sendSMS(String phoneNo, String message) {
 		String SENT = "SMS_SENT";
 		String DELIVERED = "SMS_DELIVERED";
 
 		PendingIntent sentPI = PendingIntent.getBroadcast(
 				getApplicationContext(), 0, new Intent(SENT), 0);
 
 		PendingIntent deliveredPI = PendingIntent.getBroadcast(
 				getApplicationContext(), 0, new Intent(DELIVERED), 0);
 
 		// ---when the SMS has been sent---
 		registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context arg0, Intent arg1) {
 				switch (getResultCode()) {
 				case Activity.RESULT_OK:
 					Toast.makeText(getBaseContext(), "SMS dikirim",
 							Toast.LENGTH_SHORT).show();
 					break;
 				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
 					Toast.makeText(getBaseContext(), "Generic failure",
 							Toast.LENGTH_SHORT).show();
 					break;
 				case SmsManager.RESULT_ERROR_NO_SERVICE:
 					Toast.makeText(getBaseContext(), "No service",
 							Toast.LENGTH_SHORT).show();
 					break;
 				case SmsManager.RESULT_ERROR_NULL_PDU:
 					Toast.makeText(getBaseContext(), "Null PDU",
 							Toast.LENGTH_SHORT).show();
 					break;
 				case SmsManager.RESULT_ERROR_RADIO_OFF:
 					Toast.makeText(getBaseContext(), "Radio off",
 							Toast.LENGTH_SHORT).show();
 					break;
 				}
 			}
 		}, new IntentFilter(SENT));
 
 		// ---when the SMS has been delivered---
 		registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context arg0, Intent arg1) {
 				switch (getResultCode()) {
 				case Activity.RESULT_OK:
 					Toast.makeText(getBaseContext(), "SMS terkirim",
 							Toast.LENGTH_SHORT).show();
 					break;
 				case Activity.RESULT_CANCELED:
 					Toast.makeText(getBaseContext(), "SMS tidak terkirim",
 							Toast.LENGTH_SHORT).show();
 					break;
 				}
 			}
 		}, new IntentFilter(DELIVERED));
 
 		SmsManager sms = SmsManager.getDefault();
 		sms.sendTextMessage(phoneNo, null, message, sentPI, deliveredPI);
 	}
 
	// FIXME some problems maybe shown up because of a change in the sendSMS
	
 	private class SendButtonOnClick implements OnClickListener {
 		 
         public void onClick(View v) {
             String telpNumber, operator = null, value = null;
             NominalValue nv = (NominalValue) spnNominal.getSelectedItem();
             telpNumber = txtPhone.getText().toString();
             /* userPin = txtUserPin.getText().toString(); not used */
             OperatorOption op = (OperatorOption) spnOperator.getSelectedItem();
             ServerOption so = (ServerOption) spnServer.getSelectedItem();
             if (rdoPredefined.isChecked()) {
                 value = String.valueOf(nv.value);
                 operator = String.valueOf(op.kode);
             }
             // Send SMS
             final String smsMessage = String.format("%s%s.%s.%s", operator,
                     value, telpNumber, userPin);
             final String serverPhone = String.valueOf(so.server);
             new ConfirmationDialog(MainActivity.this, telpNumber,
                     String.valueOf(op.name), String.valueOf(nv.name),
                     new ConfirmDialogListener() {
  
                         @Override
                         public void onConfirmed() {
                             //Toast.makeText(MainActivity.this,serverPhone + smsMessage, 5).show();
                              sendSMS(serverPhone, smsMessage);
  
                             // reset view to default value
                             txtPhone.setText("");
                              
                             spnOperator.setSelection(0);
                             spnNominal.setSelection(0);
  
                             // request focus to txtPhone
                             txtPhone.requestFocus();
                         }
  
                         @Override
                         public void onCancel() {
                             // nothing to do here
                         }
                     }).show();
  
             Log.d(TAG, "onSendButton Clicked, Send SMS will be:\n" + smsMessage);
         }
     }
  
     private class GroupRadioCheckedChange implements OnCheckedChangeListener {
  
         public void onCheckedChanged(RadioGroup group, int checkedId) {
             switch (checkedId) {
             case R.id.radioSpinner:
                 spnNominal.setVisibility(View.VISIBLE);
                 break;
             default:
                 break;
             }
         }
     }
 
 	/**
 	 * Callback interface for Confirmation Dialog
 	 * 
 	 * @author adrianbabame copas from Facebook SDK
 	 * @author damnedivan copas from adrianbabame
 	 */
 	public static interface ConfirmDialogListener {
 		/**
 		 * Called when ok pressed
 		 * 
 		 * Executed by thread that executed dialog
 		 */
 		public void onConfirmed();
 
 		/**
 		 * Called when a dialog is canceled by the user.
 		 * 
 		 * Executed by the thread that initiated the dialog.
 		 * 
 		 */
 		public void onCancel();
 	}
 }
