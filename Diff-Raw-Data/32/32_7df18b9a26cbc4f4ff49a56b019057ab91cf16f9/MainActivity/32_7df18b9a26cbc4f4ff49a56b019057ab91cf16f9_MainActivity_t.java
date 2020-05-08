 package fr.upsilon.inventirap;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	private final int QRCODE_RESULT = 0;
 	private final int REQUEST_RESULT = 1;
 	
 	public static final int REQUEST_BAD_ADDRESS = 1;
 	public static final int REQUEST_NO_MATERIAL = 2;
 	public static final int REQUEST_BADLY_FORMATTED = 3;
 	
 	private Context context;
 	private Button scanButton;
 	private Button ParamsButton;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         context = this;
         
         /* first time */
         String name = getResources().getString(R.string.app_name);
         SharedPreferences prefs = context.getSharedPreferences(name, MODE_PRIVATE);
         String server_ip = prefs.getString(getResources().getString(R.string.SERVER_IP), null);
         if (server_ip == null) {
         	Editor editor = prefs.edit();
         	//editor.putString(getResources().getString(R.string.SERVER_IP), "http://inventirap.irap.omp.eu/");
         	editor.putString(getResources().getString(R.string.SERVER_IP), "http://192.168.1.14:8080/Inventirap/cakephp");
         	editor.commit();
     	}
         
         scanButton = (Button) findViewById(R.id.scanButton);
         scanButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				//check ZXing presence
 				try {
 					PackageManager pm = getPackageManager();
 					pm.getPackageInfo("com.google.zxing.client.android", 0);
 				} catch (NameNotFoundException e) {
 					Toast t = Toast.makeText(context, R.string.zxing_not_found, Toast.LENGTH_LONG);
 					t.show();
 					return;
 				}
 
 				// start ZXing QR Code decoder
 		        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
 		        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
 		        startActivityForResult(intent, QRCODE_RESULT);
 			}
 		});
         
         ParamsButton = (Button) findViewById(R.id.paramsButton);
         ParamsButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				Log.d("", "click on params");
 				Intent paramIntent = new Intent(context, ParametersActivity.class);
 				startActivity(paramIntent);
 			}
 		});
     }
     
     /* result from QR Code detection */
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
     	
         if (requestCode == QRCODE_RESULT) {
             if (resultCode == RESULT_OK) {
         		//String contents = "IRAP-12-0001";
                 String contents = intent.getStringExtra("SCAN_RESULT");
 //                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                 Log.d(this.getClass().getName(), "content : " + contents);
                 
                 if (!contents.startsWith("IRAP-")) {
                 	Toast t = Toast.makeText(context, R.string.invalid_qr_code, Toast.LENGTH_LONG);
                 	t.show();
                 	return;
                 }
                 
                 Intent requestIntent = new Intent(context, RequestActivity.class);
                 requestIntent.putExtra(getString(R.string.DECODED_VALUE), contents);
                 startActivityForResult(requestIntent, REQUEST_RESULT);
                 
             } else if (resultCode == RESULT_CANCELED) {
            	/*Intent requestIntent = new Intent(context, RequestActivity.class);
                requestIntent.putExtra(getString(R.string.DECODED_VALUE), "IRAP-12-0001");
                startActivityForResult(requestIntent, REQUEST_RESULT);*/
                 Toast toast = Toast.makeText(context, R.string.qr_code_not_found, Toast.LENGTH_LONG);
                 toast.show();
                 
             }
         } else if (requestCode == REQUEST_RESULT) {
         	Toast t;
         	switch(resultCode) {
         	case REQUEST_BAD_ADDRESS:
 				t = Toast.makeText(context, R.string.webservice_error, Toast.LENGTH_LONG);
             	t.show();
             	break;
         	case REQUEST_NO_MATERIAL:
         		t = Toast.makeText(context, R.string.no_such_element, Toast.LENGTH_LONG);
             	t.show();
             	break;
         	case REQUEST_BADLY_FORMATTED:
         		t = Toast.makeText(context, R.string.json_error, Toast.LENGTH_LONG);
             	t.show();
         		break;
         	default:
         		
         	}
         	
         }
     }
 }
