 package de.franziskuskiefer.android.vault.avtivities;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.KeySpec;
 import java.util.Arrays;
 
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.PBEKeySpec;
 
 import net.sourceforge.zbar.Symbol;
 
 import org.bouncycastle.util.encoders.Base64;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.text.InputType;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.dm.zbar.android.scanner.ZBarConstants;
 import com.dm.zbar.android.scanner.ZBarScannerActivity;
 
 import de.franziskuskiefer.android.vault.R;
 import de.franziskuskiefer.android.vault.controller.AppPreferences;
 import de.franziskuskiefer.android.vault.controller.BasicActivity;
 import de.franziskuskiefer.android.vault.controller.DatabaseController;
 
 public class Setup extends BasicActivity {
 
 	private static final int ZBAR_QR_SCANNER_REQUEST = 1;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_setup);
 
 		final Button button = (Button) findViewById(R.id.BtnSetupOk);
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				launchQRScanner();
 			}
 		});
 		
 		final Button cancel = (Button) findViewById(R.id.BtnSetupCancel);
 		cancel.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_MAIN);
 			    intent.addCategory(Intent.CATEGORY_HOME);
 			    startActivity(intent);
 			}
 		});
 
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.setup_menu, menu);
 		
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		
 		switch (item.getItemId()) {
 			case R.id.menuReset:
 				DatabaseController.resetDB(getApplicationContext());
 				break;
 	
 			default:
 				break;
 		}
 		
 		return true;
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d(INPUT_SERVICE, "Finished QR-Code scanning...");
 		switch (requestCode) {
 			case ZBAR_QR_SCANNER_REQUEST:
 				if (resultCode == RESULT_OK) {
 					onUnlock(data.getStringExtra(ZBarConstants.SCAN_RESULT));
 				} else if(resultCode == RESULT_CANCELED && data != null) {
 					String error = data.getStringExtra(ZBarConstants.ERROR_INFO);
 					if(!TextUtils.isEmpty(error)) {
 						Log.d(INPUT_SERVICE, "Scanning Error:\n"+error);
 						Toast.makeText(this, "Sorry, something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
 					}
 				}
 				break;
 		}
 	}
 	
 	private void onUnlock(final String key){
 		
 		// check the password first
 		final EditText inputPwd = new EditText(this);
 		inputPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
 		new AlertDialog.Builder(this)
 			.setTitle("Password")
			.setMessage("Password please ...")
 			.setView(inputPwd)
 			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					
 					// create combined key
					String pwd = "";
					if (inputPwd.getText() != null){
						pwd = inputPwd.getText().toString();
					} else {
						// user entered an empty password -> use it ...
					}
 					byte[] salt = Base64.decode(new AppPreferences(getApplicationContext()).getSalt());
 					byte[] p = makePwdKey(pwd, salt);
 					byte[] k = hexStringToByteArray(key);
 					byte[] combinedKey = xore(p, k);
 
 					// open the vault
 					Intent myIntent = new Intent(getBaseContext(), Vault.class);
 					myIntent.putExtra("key", Arrays.toString(combinedKey));
 					startActivity(myIntent);
 
 					// unlocked
 					new AppPreferences(getApplicationContext()).saveAppStatus(true);
 				}
 			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					canceled();
 				}
 			}).show();
 	}
 	
 	private void wrongPassword(){
 		Toast.makeText(this, "Sorry, wrong password", Toast.LENGTH_SHORT).show();
 	}
 	
 	private void canceled(){
 		Toast.makeText(this, "Canceled login", Toast.LENGTH_SHORT).show();
 	}
 	
 	private byte[] makePwdKey(String pwd, byte[] salt){
 		try {
 			SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
 			KeySpec ks = new PBEKeySpec(pwd.toCharArray(),salt,1024,128);
 			SecretKey s = f.generateSecret(ks);
 			
 			// need an additional sha256 as we do not have PBKDF2WithHmacSHA2 without additional libs
 			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
 			
 			return sha256.digest(s.getEncoded());
 		} catch (NoSuchAlgorithmException e) {
 			Log.e("MainActivity", e.getLocalizedMessage(), e);
 		} catch (InvalidKeySpecException e) {
 			Log.e("MainActivity", e.getLocalizedMessage(), e);
 		}
 		
 		return null;
 	}
 	
 	private byte[] xore(byte[] b1, byte[] b2){
 		int l = b1.length > b2.length ? b1.length : b2.length;
 		byte[] result = new byte[l];
 		for (int i = 0; i < l; ++i){
 			if (i < b1.length && i < b2.length)
 				result[i] = (byte) (b1[i] ^ b2[i]);
 			else if (i < b1.length && i >= b2.length)
 				result[i] = b1[i];
 			else if (i >= b1.length && i < b2.length)
 				result[i] = b2[i];
 		}
 		
 		return result;
 	}
 	
 	private byte[] xor(byte[] b1, byte[] b2){
 		if (b1.length == b2.length) {
 			byte[] result = new byte[b1.length];
 			int i = 0;
 			for (byte b : b1)
 				result[i] = (byte) (b ^ b2[i++]);
 			
 			return result;
 		}
 		
 		return null;
 	}
 	
 	private static byte[] hexStringToByteArray(String s) {
 	    int len = s.length();
 	    byte[] data = new byte[len / 2];
 	    for (int i = 0; i < len; i += 2) {
 	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
 	                             + Character.digit(s.charAt(i+1), 16));
 	    }
 	    return data;
 	}
 	
 	public void launchQRScanner() {
 		if (isCameraAvailable()) {
 			Intent intent = new Intent(this, ZBarScannerActivity.class);
 			intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
 			startActivityForResult(intent, ZBAR_QR_SCANNER_REQUEST);
 			Log.d(INPUT_SERVICE, "Started QR-Code scanning...");
 		} else {
 			Toast.makeText(this, "Rear Facing Camera Unavailable", Toast.LENGTH_SHORT).show();
 			Log.e(INPUT_METHOD_SERVICE, "Rear Facing Camera Unavailable");
 		}
 	}
 
 	public boolean isCameraAvailable() {
 		PackageManager pm = getPackageManager();
 		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
 	}
 }
