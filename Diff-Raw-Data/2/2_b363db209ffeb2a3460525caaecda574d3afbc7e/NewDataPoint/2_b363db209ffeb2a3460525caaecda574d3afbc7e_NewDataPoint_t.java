 package com.cs4900.signalseeker;
 
 import java.util.HashMap;
 import java.util.Scanner;
 
 import org.apache.http.client.ResponseHandler;
 
 import com.cs4900.signalseeker.data.DataEntry;
 import com.cs4900.signalseeker.data.DataList;
 import com.cs4900.signalseeker.network.HTTPRequestHelper;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.telephony.PhoneStateListener;
 import android.telephony.SignalStrength;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class NewDataPoint extends Activity {
 	double latitude;
 	double longitude;
 
 	protected static final String CLASSTAG = NewDataPoint.class.getSimpleName();
 
 	private DataEntry newDataEntry;
 	private DataList dataList;
 
 	private EditText locationEditText;
 	private TextView latitudeTextView;
 	private TextView longitudeTextView;
 	private TextView carrierTextView;
 	private TextView wifiTextView;
 	private Spinner cellSpinner;
 	private Button submitButton;
 
 	private ProgressDialog progressDialog;
 
 	private final Handler handler = new Handler() {
 		@Override
 		public void handleMessage(final Message msg) {
 			Log.v(Constants.LOGTAG, " " + NewDataPoint.CLASSTAG + " create worker thread done.");
 			progressDialog.dismiss();
 
 			String bundleResult = msg.getData().getString("RESPONSE");
 
 			// Pattern pattern =
 			// Pattern.compile("<id type=\"integer\">\\d+<id>");
 			Scanner s = new Scanner(bundleResult);
 			int id = 0;
 			while (s.hasNextLine()) {
 				String line = s.nextLine();
 				if (line.contains("id type")) {
 					Scanner s1 = new Scanner(line).useDelimiter("\\D+");
 
 					id = s1.nextInt();
 					break;
 				}
 
 				// Bundle b = msg.getData();
 				// CatalogEntry ce = CatalogEntry.fromBundle(b);
 
 				dataList = DataList.parse(NewDataPoint.this);
 				dataList.delete(newDataEntry);
 				newDataEntry.setId(new Integer(id));
 				dataList.create(newDataEntry);
 				Log.v(Constants.LOGTAG, " " + NewDataPoint.CLASSTAG + " " + newDataEntry + ", ~~~~~~~~~~~~~~~~~~~~~~~~~");
 
 			}
 			finish();
 		}
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.newdatapoint);
 
 		newDataEntry = new DataEntry();
 
 		initializeNewDataPoint();
 
 		// Location view
 		locationEditText = (EditText) findViewById(R.id.new_location);
 
 		// Latitude view
 		latitudeTextView = (TextView) findViewById(R.id.new_latitude);
 		latitudeTextView.setText("" + getIntent().getDoubleExtra("latitude", 0));
 
 		// Longitude view
 		longitudeTextView = (TextView) findViewById(R.id.new_longitude);
 		longitudeTextView.setText("" + getIntent().getDoubleExtra("longitude", 0));
 
 		// Wifi signal strength view
 		wifiTextView = (TextView) findViewById(R.id.new_wifi);
 		wifiTextView.setText("" + newDataEntry.getWifi());
 
 		// Carrier name view
 		carrierTextView = (TextView) findViewById(R.id.new_carrier);
 		carrierTextView.setText(newDataEntry.getCarrier());
 
 		// Carrier signal strength view
 		cellSpinner = (Spinner) findViewById(R.id.new_cell_spinner);
 		ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.cell_signal_array,
 				android.R.layout.simple_spinner_item);
 		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		cellSpinner.setAdapter(arrayAdapter);
 
 		// Submit button view
 		submitButton = (Button) findViewById(R.id.submit_button);
 		submitButton.setOnClickListener(new Button.OnClickListener() {
 
 			public void onClick(View v) {
 				try {
 					if (locationEditText.getText().toString().equals("")) {
 						Context context = getApplicationContext();
 						CharSequence text = "Please Enter a location name.";
 						int duration = Toast.LENGTH_SHORT;
 						Toast toast = Toast.makeText(context, text, duration);
 						toast.show();
 					}
 					else {
 						createDataPoint();
 						finish();
 					}
 
 				} catch (Exception e) {
 					Log.i(Constants.LOGTAG + ": " + NewDataPoint.CLASSTAG, "Failed to Submit new Data Point" + e.getMessage() + "]");
 				}
 			}
 		});
 
 	}
 
 	@SuppressWarnings("static-access")
 	public void initializeNewDataPoint() {
 
 		newDataEntry.setLatitude(getIntent().getDoubleExtra("latitude", 0));
 		newDataEntry.setLongitude(getIntent().getDoubleExtra("longitude", 0));
 
 		// Wifi signal strength
 		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 		int RSSI = wifiManager.getConnectionInfo().getRssi();
		newDataEntry.setWifi(wifiManager.calculateSignalLevel(RSSI, 5));
 
 		// Carrier name
 		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 		newDataEntry.setCarrier(telephonyManager.getNetworkOperatorName());
 	}
 
 	public void createDataPoint() {
 		Log.v(Constants.LOGTAG, " " + NewDataPoint.CLASSTAG + " updateProduct");
 
 		this.progressDialog = ProgressDialog.show(this, " Working...", " Creating Signal Point", true, false);
 
 		final ResponseHandler<String> responseHandler = HTTPRequestHelper.getResponseHandlerInstance(this.handler);
 
 		final HashMap<String, String> params = new HashMap<String, String>();
 		params.put("location",locationEditText.getText().toString());
 		params.put("latitude", "" + newDataEntry.getLatitude());
 		params.put("longitude", "" + newDataEntry.getLongitude());
 		params.put("wifi", "" + newDataEntry.getWifi());
 		params.put("carrier", newDataEntry.getCarrier());
 		params.put("cell", "" + cellSpinner.getSelectedItemPosition());
 		params.put("id", String.valueOf(newDataEntry.getId()));
 		params.put("address", newDataEntry.getLatitude() + ", " + newDataEntry.getLongitude());
 		params.put("gmaps", "true");
 		
 		// Create a new data point locally
 		newDataEntry.setLocation(locationEditText.getText().toString());
 		newDataEntry.setCell(cellSpinner.getSelectedItemPosition());
 		dataList = DataList.parse(NewDataPoint.this);
 		dataList.create(newDataEntry);
 
 		// create data point on the server in a separate thread for
 		// ProgressDialog/Handler
 		// when complete send "empty" message to handler
 		new Thread() {
 			@Override
 			public void run() {
 				// networking stuff ...
 				HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);
 
 				helper.performPost(HTTPRequestHelper.MIME_TEXT_PLAIN, "http://signalseeker.herokuapp.com/data.xml", null, null, null, params);
 			}
 		}.start();
 	}
 }
