 package com.spiteful.cipher.ui;
 
 import net.minidev.json.JSONObject;
 import net.minidev.json.JSONValue;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 import com.spiteful.cipher.Constants;
 import com.spiteful.cipher.android.R;
 import com.spiteful.cipher.model.Message;
 import com.spiteful.cipher.service.VerifyService;
 import com.spiteful.cipher.service.WebActionCallback;
 import com.spiteful.cipher.util.Decoder;
 
 public class ScanActivity extends Activity implements WebActionCallback {
 	private static final String tag = ScanActivity.class.toString();
 	private Message message = new Message();
 	private boolean success;
 	private ProgressDialog progressBar;
 	private TextView levelText;
 	private TextView encodedText;
 	private TextView parityText;
 	private TextView decodedText;
 	private TextView successText;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_scanning);
 		
 		getUiElements();
 		setupUi();
 		
 		IntentIntegrator integrator = new IntentIntegrator(this);
 		integrator.initiateScan();
 	}
 
 	private void getUiElements() {
 		this.levelText = (TextView) this.findViewById(R.id.tv_level);
 		this.encodedText = (TextView) this.findViewById(R.id.tv_encoded);
 		this.parityText = (TextView) this.findViewById(R.id.tv_parity);
 		this.decodedText = (TextView) this.findViewById(R.id.tv_decoded);
 		this.successText = (TextView) this.findViewById(R.id.tv_success);
 	}
 
 	private void setupUi() {
 		this.levelText.setText(message.getLevel() + "");
 		this.encodedText.setText(message.getEncoded());
 		this.decodedText.setText(message.getDecoded());
 		this.parityText.setText(message.getParity());
 		this.successText.setText(success ? "Yes!" : "NO...");
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null) {
 			Log.d(tag, scanResult.getContents());
 
 			Object object = JSONValue.parse(scanResult.getContents());
 			if (object instanceof JSONObject) {
 				handleMessage((JSONObject) object);
 			} else {
 				// TODO Tell the user
 			}
 		}
 	}
 
 	private void handleMessage(JSONObject json) {
 		message = new Message();
 		int level = (Integer)json.get(Constants.LEVEL_KEY);
 		int id = (Integer) json.get(Constants.ID_KEY);
 		String data = (String) json.get(Constants.ENCODED_KEY);
 		String decoded = "";
 		message.setId(id);
 		message.setLevel(level);
 		message.setEncoded(data);
 
 		getUiElements();
 		setupUi();
 
 		switch (message.getLevel()) {
 		case 1:
 			decoded = Decoder.decodeLevel1(message.getEncoded());
 			break;
 		case 2:
 			decoded = Decoder.decodeLevel2(message.getEncoded());
 			break;
 		case 3:
 			String parity = (String) json.get(Constants.PARITY_KEY);
 			message.setParity(parity);
 			decoded = Decoder.decodeLevel3(message.getEncoded(), message.getParity());
 			break;
 
 		default:
 			break;
 		}
 		message.setDecoded(decoded);
 		contactServer(message);
 	}
 
 	private void contactServer(Message message) {
 		startSpinner();
 
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		int id = Integer.parseInt(prefs.getString(Constants.TEAMID_PREF, "0"));
 		int pin = Integer.parseInt(prefs.getString(Constants.TEAMPIN_PREF, "0"));
		
 		VerifyService service = new VerifyService(this, message, id, pin);
 		service.performAction();
 	}
 
 	@Override
 	public void onCompleted(JSONObject json) {
 		stopSpinner();
 		
 		success = (Boolean) json.get(Constants.RESULT_KEY);
 		if(success) {
 			Toast.makeText(this, "Congrats bro!", Toast.LENGTH_LONG).show();
 		} else {
 			Toast.makeText(this, "Dude, you suck", Toast.LENGTH_LONG).show();
 		}
 		setupUi();
 	}
 	
 	
 	private void startSpinner() {
 		progressBar = new ProgressDialog(this);
 		progressBar.setCancelable(false);
 		progressBar.setMessage("Loading ...");
 		progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		progressBar.show();
 	}
 
 	private void stopSpinner() {
 		progressBar.cancel();
 	}
 }
