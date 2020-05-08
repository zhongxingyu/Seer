 package org.hubiquitus.android.SimpleClient;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.hubiquitus.hapi.client.HDelegate;
 import org.hubiquitus.hapi.client.HClient;
 import org.hubiquitus.hapi.hStructures.HCommand;
 import org.hubiquitus.hapi.hStructures.HOptions;
 import org.hubiquitus.hapi.hStructures.HStatus;
 import org.hubiquitus.hapi.structures.HJsonObj;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class SimpleClientActivity extends Activity  implements HDelegate{
     /** Called when the activity is first created. */
 	
 	private String login;
 	private String password;
 	private String gateways;
 	private String serverHost;
 	private String serverPort;
 	private String transport;
 	
 	private Button connectionButton;
 	private Button deconnectionButton;
 	private Button clearButton;
 	private Button hechoButton;
 	
 	private EditText loginEditText;
 	private EditText passwordEditText;
 	private EditText gatewaysEditText;
 	private EditText serverportEditText;
 	private EditText serverhostEditText;
 	private EditText hechoEditText;
 	
 	private TextView outputTextArea;
 	private RadioGroup transportRadioGroup;
 	
 	private ScrollView outputScroller;
 	private TextView connectionStatusLabel;
 	
 	private HClient client;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
         initComponent();
         initListenerBoutonConnection();
         initListenerBoutonDeconnection();
         initListenerBoutonClear();
         initListenerhechoButton();
         
         client = new HClient();
     }
     
     public void initComponent() {
     	
     	connectionButton = (Button) findViewById(R.id.ConnectionButton);
     	deconnectionButton = (Button) findViewById(R.id.DeconnectionButton);
     	clearButton = (Button) findViewById(R.id.ClearButton);
     	hechoButton = (Button) findViewById(R.id.hechoButton);
     	
     	loginEditText = (EditText) findViewById(R.id.loginText);
     	passwordEditText = (EditText) findViewById(R.id.passwordText);
     	gatewaysEditText = (EditText) findViewById(R.id.gatewaysText); 
     	serverportEditText = (EditText) findViewById(R.id.serverportText);
     	serverhostEditText = (EditText) findViewById(R.id.serverhostText);
     	hechoEditText = (EditText) findViewById(R.id.hechoText);
     	
     	transportRadioGroup = (RadioGroup) findViewById(R.id.transportGroupbutton);
     	outputTextArea = (TextView) findViewById(R.id.outputView);
     	outputScroller = (ScrollView)findViewById(R.id.scrollview);
     	connectionStatusLabel = (TextView)findViewById(R.id.connectionStatusLabel);
     	
     	loginEditText.setText("");
     	passwordEditText.setText("");
     	serverhostEditText.setText("");
     	gatewaysEditText.setText("");
     	
     }
    
     
     public void initListenerBoutonConnection() {
     	final SimpleClientActivity parentClass = this;
     	OnClickListener listener = new OnClickListener()
         {
    	       	public void onClick(View v) {
 	   	       	login = loginEditText.getText().toString();
 	   	        password = passwordEditText.getText().toString();
 	   	        gateways = gatewaysEditText.getText().toString();
 	   	        serverHost = serverhostEditText.getText().toString();
 	   	        serverPort = serverportEditText.getText().toString();
 	   	        
 	   	        String[] endpointsArray = gateways.split(";");
 	   	        ArrayList<String> endpoints = new ArrayList<String>();
 	   	        for (int i = 0; i < endpointsArray.length; i++) {
 	   	        	endpoints.add(endpointsArray[i]);
 	   	        }
 	   	        
 				RadioButton temp = (RadioButton) findViewById(transportRadioGroup.getCheckedRadioButtonId());
 				transport = temp.getText().toString();
 				
 	   	        //outputTextArea.append("login : " + login + " , password : " + password 
 	   	        //					+ " , gateways : " + gateways + " , serverHost : " + serverHost 
 	   	        //					+ " , serverPort : " + serverPort + " , transport : " + transport);
 				
 				HOptions options = new HOptions();
 				options.setServerHost(serverHost);
 				options.setServerPort(serverPort);
 				options.setTransport(transport);
 				options.setEndpoints(endpoints);
 				
 				//client.connect("admin@localhost", "", parentClass, new HOptions());
 				client.connect(login, password, parentClass, options);
    	       	}
         };
         connectionButton.setOnClickListener(listener);
     
     }
     
     public void initListenerBoutonDeconnection() {
     
     	OnClickListener listener = new OnClickListener()
         {
    	       	public void onClick(View v) {
    	       		client.disconnect();
    	       	}
         };
         deconnectionButton.setOnClickListener(listener);
     
     }
 
 
 	public void initListenerBoutonClear() {
 	
 		OnClickListener listener = new OnClickListener()
 	    {
 		       	public void onClick(View v) {
 				
 		       		TextView text = (TextView) findViewById(R.id.outputView);
 		       		text.setText("");
 				
 				}
 	
 	    };
 	    clearButton.setOnClickListener(listener);
 	
 	}
 	
 	public void initListenerhechoButton() {
 		OnClickListener listener = new OnClickListener()
 	    {
 		       	public void onClick(View v) {
 		       		JSONObject params = new JSONObject();
 		       		try {
 						params.put("text",hechoEditText.getText().toString());
 						HCommand cmd = new HCommand("hNode.localhost", "hecho", params);
 						client.command(cmd);
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 	
 	    };
 	    hechoButton.setOnClickListener(listener);
 	}
 
	public void hDelegate(final String type, final HJsonObj data) {
 		Log.i("DEBUG", "callback for type " + type + " with data " + data.toString());
 		runOnUiThread(new Runnable() {
 			
 			public void run() {
 				if (type.equals("hStatus")) {
 					HStatus status = (HStatus)data;
 					connectionStatusLabel.setText(status.getStatus().toString());
 				}
 				outputTextArea.append("Type : " + type + "  data : " + data.toString() + "\n\n");
 				Timer scrollTimer = new Timer();
 				TimerTask scrollTask = new TimerTask() {
 					
 					@Override
 					public void run() {
 						runOnUiThread(new Runnable() {
 							
 							public void run() {
 								outputScroller.smoothScrollTo(0, outputTextArea.getBottom());
 								
 							}
 						});
 						
 					}
 				};
 				
 				scrollTimer.schedule(scrollTask, 10);
 			}
 		});	
 		
 	}
 	
 	
 	
 }	
