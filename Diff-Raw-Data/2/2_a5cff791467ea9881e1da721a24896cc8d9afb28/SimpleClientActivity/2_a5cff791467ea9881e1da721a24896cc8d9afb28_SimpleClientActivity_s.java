 /*
  * Copyright (c) Novedia Group 2012.
  *
  *     This file is part of Hubiquitus.
  *
  *     Hubiquitus is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Hubiquitus is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with Hubiquitus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.hubiquitus.android.SimpleClient;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.hubiquitus.hapi.client.HCommandDelegate;
 import org.hubiquitus.hapi.client.HClient;
 import org.hubiquitus.hapi.client.HMessageDelegate;
 import org.hubiquitus.hapi.client.HStatusDelegate;
 import org.hubiquitus.hapi.hStructures.HCommand;
 import org.hubiquitus.hapi.hStructures.HJsonObj;
 import org.hubiquitus.hapi.hStructures.HMessage;
 import org.hubiquitus.hapi.hStructures.HOptions;
 import org.hubiquitus.hapi.hStructures.HResult;
 import org.hubiquitus.hapi.hStructures.HStatus;
 import org.hubiquitus.hapi.util.HJsonDictionnary;
 
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
 
 public class SimpleClientActivity extends Activity  implements HStatusDelegate, HMessageDelegate, HCommandDelegate{
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
 	private Button subscribeButton;
 	private Button unsubscribeButton;
 	private Button publishButton;
 	private Button getLastMessageButton;
 	private Button getSubcriptionButton;
 
 	private EditText loginEditText;
 	private EditText passwordEditText;
 	private EditText gatewaysEditText;
 	private EditText serverportEditText;
 	private EditText serverhostEditText;
 	private EditText channelIDText;
 	private EditText nbLastMessageText;
 	private EditText MessageEditText;
 
 	private TextView outputTextArea;
 	private RadioGroup transportRadioGroup;
 	private RadioGroup messageRadioGroup;
 
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
 		initListenerSubscribeButton();
 		initListenerUnsubscribeButton();
 		initListenerPublishButton();
 		initListenerGetLastMessageButton();
 		initListenerGetSubscriptionButton();
 
 		client = new HClient();
 		client.onStatus(this);
 		client.onMessage(this);
 	}
 
 	public void initComponent() {
 
 		connectionButton = (Button) findViewById(R.id.ConnectionButton);
 		deconnectionButton = (Button) findViewById(R.id.DeconnectionButton);
 		clearButton = (Button) findViewById(R.id.ClearButton);
 		hechoButton = (Button) findViewById(R.id.hechoButton);
 		subscribeButton = (Button) findViewById(R.id.SubscribeButton);
 		unsubscribeButton = (Button) findViewById(R.id.UnsubscribeButton);
 		publishButton = (Button) findViewById(R.id.PublishButton);
 		getLastMessageButton = (Button) findViewById(R.id.GetLastMessageButton);
 		getSubcriptionButton = (Button) findViewById(R.id.GetSubcriptionButton);
 
 		loginEditText = (EditText) findViewById(R.id.loginText);
 		passwordEditText = (EditText) findViewById(R.id.passwordText);
 		gatewaysEditText = (EditText) findViewById(R.id.gatewaysText); 
 		serverportEditText = (EditText) findViewById(R.id.serverportText);
 		serverhostEditText = (EditText) findViewById(R.id.serverhostText);
 		channelIDText = (EditText) findViewById(R.id.ChannelIDText);
 		nbLastMessageText = (EditText) findViewById(R.id.NbLastMessageText);
 		MessageEditText = (EditText) findViewById(R.id.MessageText);
 
 		transportRadioGroup = (RadioGroup) findViewById(R.id.transportGroupbutton);
 		messageRadioGroup = (RadioGroup) findViewById(R.id.MessageGroupbutton);
 		outputTextArea = (TextView) findViewById(R.id.outputView);
 		outputScroller = (ScrollView)findViewById(R.id.scrollview);
 		connectionStatusLabel = (TextView)findViewById(R.id.connectionStatusLabel);
 
 		loginEditText.setText("");
 		passwordEditText.setText("");
 		serverhostEditText.setText("");
 		channelIDText.setText("");
 		gatewaysEditText.setText("");
 		MessageEditText.setText("");		
 
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
 				client.connect(login, password, options);
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
 		final SimpleClientActivity outerClass = this;
 		OnClickListener listener = new OnClickListener()
 		{
 			public void onClick(View v) {
 				HJsonDictionnary params = new HJsonDictionnary();
 				params.put("text",MessageEditText.getText().toString());
 				HCommand cmd = new HCommand("hnode.hub.novediagroup.com", "hecho", params);
 				client.command(cmd, outerClass);
 			}
 
 		};
 		hechoButton.setOnClickListener(listener);
 	}
 	
 	public void initListenerSubscribeButton() {
 		final SimpleClientActivity outerClass = this;
 		OnClickListener listener = new OnClickListener() {
 			public void onClick(View v) {
 				client.subscribe(channelIDText.getText().toString(), outerClass);
 			}
 		};
 		subscribeButton.setOnClickListener(listener);
 	}
 	
 	public void initListenerUnsubscribeButton() {
 		final SimpleClientActivity outerClass = this;
 		OnClickListener listener = new OnClickListener() {
 			public void onClick(View v) {
 				client.unsubscribe(channelIDText.getText().toString(), outerClass);
 			}
 		};
 		unsubscribeButton.setOnClickListener(listener);
 	}
 	
 	public void initListenerPublishButton() {
 		final SimpleClientActivity outerClass = this;
 		OnClickListener listener = new OnClickListener() {
 			public void onClick(View v) {
 				HMessage message = new HMessage();
 				message.setPublisher(loginEditText.getText().toString());
 				message.setChid(channelIDText.getText().toString());
 				message.setPublished(new GregorianCalendar());
 				message.setType("obj");
 				RadioButton temp = (RadioButton) findViewById(messageRadioGroup.getCheckedRadioButtonId());
 				if(temp.getText().toString().equalsIgnoreCase("Transient")) {
 					message.setTransient(true);
 				} else {
 					message.setTransient(false);
 				}
 
 				HJsonDictionnary payload = new HJsonDictionnary();
 				payload.put("text",MessageEditText.getText().toString());
 				message.setPayload(payload);
 				client.publish(message, outerClass);
 			}
 		};
 		publishButton.setOnClickListener(listener);
 	}
 	
 	public void initListenerGetLastMessageButton() {
 		final SimpleClientActivity outerClass = this;
 		OnClickListener listener = new OnClickListener() {
 			public void onClick(View v) {
 				String chid = channelIDText.getText().toString();
 				try{
 					int nbLastMessage = Integer.parseInt(nbLastMessageText.getText().toString());
 					if(nbLastMessage > 0) {
 						client.getLastMessages(chid, nbLastMessage, outerClass);
 					} else {
 						client.getLastMessages(chid, outerClass);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		};
 		getLastMessageButton.setOnClickListener(listener);
 	}
 	
 	public void initListenerGetSubscriptionButton() {
 		final SimpleClientActivity outerClass = this;
 		OnClickListener listener = new OnClickListener() {
 			public void onClick(View v) {
 				client.getSubscriptions(outerClass);
 			}
 		};
 		getSubcriptionButton.setOnClickListener(listener);
 	}
 	
 	public void onResult(final HResult result) {
 		runOnUiThread(new Runnable() {
 
 			public void run() {
 				if(true) {
 					outputTextArea.append("HResult : " + result + "\n\n");
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
 			}
 		});	
 
 	}
 
 	@Override
 	public void onStatus(final HStatus status) {
 		runOnUiThread(new Runnable() {
 
 			public void run() {
				connectionStatusLabel.setText(status.getStatus().toString());
 				outputTextArea.append("Status : " + status.getStatus() + " error : " + status.getErrorCode() + "  errorMsg : " + status.getErrorMsg() + "\n\n");
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
 
 	@Override
 	public void onMessage(final HMessage message) {
 		runOnUiThread(new Runnable() {
 
 			public void run() {
 				outputTextArea.append("HMessage : " + message + "\n\n");
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
