 package com.nityankhanna.androidutils;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 
import com.nityankhanna.androidutils.security.EncryptionManager;

 /**
  * Created by Nityan Khanna on 22/10/13.
  */
public class MainActivity extends Activity implements View.OnClickListener, OnEncryptionListener {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		Button button = (Button) findViewById(R.id.button);
 
 		button.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View view) {
 
 		switch (view.getId()) {
 
 			case R.id.button:
				EncryptionManager encryptionManager = new EncryptionManager(this);
				encryptionManager.encryptData("Nityan".getBytes());
 				break;
 
 			default:
 				break;
 		}
 	}

	/**
	 * Called when encrypting data is completed.
	 *
	 * @param encryptedData The encrypted data.
	 */
	@Override
	public void onEncryptionCompleted(byte[] encryptedData) {
		Log.d(Constants.DEBUG, new String(encryptedData));
	}

	/**
	 * Called when decrypting data is completed.
	 *
	 * @param decryptedData The decrypted data.
	 */
	@Override
	public void onDecryptionCompleted(byte[] decryptedData) {
		Log.d(Constants.DEBUG, new String(decryptedData));
	}
 }
