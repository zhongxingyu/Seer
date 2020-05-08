 package ch.ethz.inf.vs.android.siwehrli.ws;
 
 //imports used for WS
 import org.ksoap2.SoapEnvelope;
 import org.ksoap2.serialization.SoapObject;
 import org.ksoap2.serialization.SoapSerializationEnvelope;
 import org.ksoap2.transport.HttpTransportSE;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	//WS strings
 	private static final String NAMESPACE = "http://webservices.vslecture.vs.inf.ethz.ch/";
 	private static final String METHOD_NAME = "getSpot";
 	private static final String URL = "http://vslab.inf.ethz.ch:80/SunSPOTWebServices/SunSPOTWebservice";
 	private static final String SOAP_ACTION = "http://vslab.inf.ethz.ch:80/SunSPOTWebServices/SunSPOTWebservice/getSpot";
 	
 	//Views to show results in
 	private TextView resultTextBox;
 	private TextView rawXMLView;
 	private TextView rawObjectView;
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		resultTextBox = (TextView) findViewById(R.id.resultTextBox);
 		rawXMLView = (TextView) findViewById(R.id.rawXMLTextView);
 		rawObjectView = (TextView) findViewById(R.id.rawObjectView);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	//Called when button pressed
 	public void onGetTempButtonPressed(View v) {
 		//create and execute asynchronous network task
 		String[] args = { NAMESPACE, METHOD_NAME, URL, SOAP_ACTION };
 		SoapTask myTask = new SoapTask();
 		myTask.execute(args);
 	}
 	
 	class SoapTask extends AsyncTask<String[], Void, SoapObject> {
 		
 		//stores the raw xml response
 		String rawXmlResponse;
 
 		protected SoapObject doInBackground(String[]... args) {
 			//build request
 			SoapObject request = new SoapObject(args[0][0], args[0][1]);
 			request.addProperty("id", "Spot3");
 			
 			//pack request into a soap envelope
 			SoapSerializationEnvelope mySoapEnvelope = new SoapSerializationEnvelope(
 					SoapEnvelope.VER10);
 			mySoapEnvelope.setOutputSoapObject(request);
 			
 			//send envelope over http
 			HttpTransportSE httpTransport = new HttpTransportSE(args[0][2]);
 			
 			//enable debug for raw xml response
 			httpTransport.debug = true;
 			
 			try {
 				//send envelope
 				httpTransport.call(args[0][3], mySoapEnvelope);
 				
 				//retrieve response
 				rawXmlResponse = httpTransport.responseDump;
 				SoapObject result = (SoapObject) mySoapEnvelope.getResponse();
 				return result;
 			} catch (Exception e) {
 				//will be thrown if no network, etc
 				return null;
 			}
 		}
 
 		protected void onPostExecute(SoapObject result) {
 			//gets executed by ui thread after networking is finished
 			try {
 				//retrieve temperature and set textView contents
 				String temp = result
 						.getPrimitivePropertyAsString("temperature");
 				resultTextBox.setText(temp);
 				rawXMLView.setText(rawXmlResponse);
 				rawObjectView.setText(result.toString());
 			} catch (NullPointerException e) {
 				//result was null, something went wrong, apologize
 				resultTextBox.setText("no data");
 				rawXMLView.setText("Something went wrong. Sorry!");
				rawObjectView.setText("");
 			}
 		}
 	}
 }
