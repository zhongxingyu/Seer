 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.societies.android.platform.useragent.feedback.guis;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.societies.android.api.comms.IMethodCallback;
 import org.societies.android.api.events.IAndroidSocietiesEvents;
 import org.societies.android.api.events.IPlatformEventsCallback;
 import org.societies.android.api.events.PlatformEventsHelperNotConnectedException;
 import org.societies.android.remote.helper.EventsHelper;
 import org.societies.android.platform.useragent.feedback.R;
 import org.societies.android.platform.useragent.feedback.constants.UserFeedbackActivityIntentExtra;
 import org.societies.api.schema.useragent.feedback.ExpFeedbackResultBean;
 import org.societies.api.schema.useragent.feedback.UserFeedbackBean;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class AcknackPopup extends Activity{
 
 	private static final String CLIENT_NAME      = "org.societies.android.platform.useragent.feedback.guis.AcknackPopup";
 	private static final String LOG_TAG = AcknackPopup.class.getName();
 	EventsHelper eventsHelper = null;
 	private boolean isEventsConnected = false;
 	private String resultPayload = "";
 	private UserFeedbackBean eventInfo;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.acknack_activity);
 
 		//RETRIEVE USERFEEDBACK BEAN FROM INTENT
 		Intent intent = getIntent();
 		Bundle bundle = intent.getExtras();
 		eventInfo = bundle.getParcelable(UserFeedbackActivityIntentExtra.EXTRA_PRIVACY_POLICY);
 		
 		TextView txtView = (TextView) findViewById(R.id.textView1);
 		txtView.setText(eventInfo.getProposalText());
 		LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout1);
 
 		for (String option: eventInfo.getOptions()){
 			Button button = new Button(this);
 			button.setText(option);
 			button.setTag(option);
 			layout.addView(button);
 
 			button.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					AcknackPopup.this.resultPayload = (String) v.getTag();
 					Log.d(LOG_TAG, "Connected to eventsManager - resultFlag true");
 
 					if (isEventsConnected){	    
 						publishEvent();	               
 					}else{	                
 						eventsHelper = new EventsHelper(AcknackPopup.this);	  
 						eventsHelper.setUpService(new IMethodCallback() {
 							@Override							
 							public void returnAction(String result) {		
 								Log.d(LOG_TAG, "eventMgr callback: ReturnAction(String) called");	
 							}
 							@Override							
 							public void returnAction(boolean resultFlag) {
 								Log.d(LOG_TAG, "eventMgr callback: ReturnAction(boolean) called. Connected");
 								if (resultFlag){		
 									isEventsConnected = true;
 									Log.d(LOG_TAG, "Connected to eventsManager - resultFlag true");		
 									publishEvent();								
 								}							
 							}
 							@Override
 							public void returnException(String result) {
 							}						
 						});	           
 					}
 				}
 			});
 		}
 		Log.d(LOG_TAG, "onCreate in AcknackPopup");
 	}
 
 	private void publishEvent() {
 		try {    		
 			ExpFeedbackResultBean bean = new ExpFeedbackResultBean();    		
 			List<String> feedback = new ArrayList<String>();    		
 			feedback.add(this.resultPayload);    		
 			bean.setFeedback(feedback);    		
 			bean.setRequestId(eventInfo.getRequestId());
			eventsHelper.publishEvent(IAndroidSocietiesEvents.UF_RESPONSE_INTENT, bean, new IPlatformEventsCallback() {
				@Override				
				public void returnAction(int result) { }
				@Override				
				public void returnAction(boolean resultFlag) { }
				@Override
				public void returnException(int exception) { }			
			});
 			//FINISH
 			eventsHelper.tearDownService(new IMethodCallback() {
 				@Override
 				public void returnException(String result) { }
 				@Override
 				public void returnAction(String result) { }
 				@Override
 				public void returnAction(boolean resultFlag) { }
 			});
 			finish();
 		} catch (PlatformEventsHelperNotConnectedException e) {
 			e.printStackTrace();
 		}	
 	}
 
 }
