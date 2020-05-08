 /*******************************************************************************
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @contributor(s): Freerider Team 2 (Group 3, IT2901 Spring 2013, NTNU)
  * @version: 2.0
  * 
  * Copyright 2013 Freerider Team 2
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package no.ntnu.idi.socialhitchhiking.inbox;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Calendar;
 import java.util.concurrent.ExecutionException;
 
 import no.ntnu.idi.freerider.model.Journey;
 import no.ntnu.idi.freerider.model.Notification;
 import no.ntnu.idi.freerider.model.NotificationType;
 import no.ntnu.idi.freerider.model.Route;
 import no.ntnu.idi.freerider.model.User;
 import no.ntnu.idi.freerider.protocol.JourneyResponse;
 import no.ntnu.idi.freerider.protocol.NotificationRequest;
 import no.ntnu.idi.freerider.protocol.Request;
 import no.ntnu.idi.freerider.protocol.RequestType;
 import no.ntnu.idi.freerider.protocol.Response;
 import no.ntnu.idi.freerider.protocol.ResponseStatus;
 import no.ntnu.idi.freerider.protocol.SingleJourneyRequest;
 import no.ntnu.idi.freerider.protocol.UserRequest;
 import no.ntnu.idi.socialhitchhiking.R;
 import no.ntnu.idi.socialhitchhiking.SocialHitchhikingApplication;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 import no.ntnu.idi.socialhitchhiking.map.MapActivityAbstract;
 import no.ntnu.idi.socialhitchhiking.map.MapRoute;
 
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class NotificationHandler{
 	private static Inbox in;
 	private static Notification not;
 	private static SocialHitchhikingApplication app;
 	
 	
 	/**
 	 * Static method to be called if the calling Activity is a {@link MapActivityAbstract}.
 	 * 
 	 * 
 	 * @param type - {@link NotificationType} 
 	 * @param com
 	 * @return
 	 */
 	public static boolean handleMap(NotificationType type,String com){
 		return createRequest(true, type, com);
 	}
 	/**
 	 * Static method to handle a {@link Notification} when it's clicked. 
 	 * Does nothing if the Notification is already read. Uses a
 	 * switch based on the {@link NotificationType), to handle unread 
 	 * Notifications.
 	 * 
 	 * @param nf - Notification to be handled
 	 * @param ap - Pointer to the Application, which will be used to get the current user etc.
 	 * @param i - The calling Inbox-activity which dialogs will be created upon
 	 */
 	public static void handleNotification(Notification nf,SocialHitchhikingApplication ap,Inbox i){
 		app = ap;
 		not = nf;
 		in = i;
 		if(not.isRead() && not.getType() == NotificationType.MESSAGE){
 			createChatDialog(not);
 		}
 		else if(not.isRead()){
 			createConfirmDialog("Inactive", "Notification is inactive");
 		}
 		else{
 			switch (not.getType()) {
 			case DRIVER_CANCEL:
 				createAorRDialog();
 				break;
 			case HITCHHIKER_ACCEPTS_DRIVER_CANCEL:
 				createMessageDialog(true,"Hitchhiker acknowledged", not.getSenderName()+" accepts cancel");
 				break;
 			case HITCHHIKER_CANCEL:
 				createMessageDialog(false,"Hitchhiker cancelled request", not.getSenderName()+" cancelled the request");
 				break;
 			case HITCHHIKER_REQUEST:
 				createNotificationDialog();
 				break;
 			case REQUEST_ACCEPT:
 				createAorRDialog();
 				break;
 			case REQUEST_REJECT:
 				createMessageDialog(false,"Request rejected by driver", "Your request was rejected by "+not.getSenderName());
 				break;
 			case MESSAGE:
 				createChatDialog(not);
 				break;
 			case RATING:
 				createMessageDialogRating("Driver rating request", "Would you recommend "+ not.getSenderName()+"?");
 				break;
 			default:
 				createMessageDialog(false,"Unknown", "Status unknown");
 				break;
 			}
 		}
 		
 	}
 	
 	public static void createAorRDialog(){
 		final Dialog aorRDialog = new Dialog(in);
 		
 		
 		aorRDialog.setContentView(R.layout.message_layout);
 		
 		ImageView okBtn = (ImageView)aorRDialog.findViewById(R.id.replyBtn);
 		ImageView showBtn = (ImageView)aorRDialog.findViewById(R.id.showJourneyBtn);
 		ImageView markAsReadBtn = (ImageView)aorRDialog.findViewById(R.id.markAsReadBtn);
 		ImageView facebookBtn = (ImageView)aorRDialog.findViewById(R.id.shareOnFaceBtn);
 		
 		TextView contentTxt = (TextView)aorRDialog.findViewById(R.id.contentViewField);
 		TextView targetTxt = (TextView)aorRDialog.findViewById(R.id.targetTxt);
 		TextView content = (TextView)aorRDialog.findViewById(R.id.contentTxt);
 		
 		if(not.getType() == NotificationType.DRIVER_CANCEL){
 			aorRDialog.setTitle("Driver cancelled ride");
 			contentTxt.setText("The ride has been cancelled by " + not.getSenderName());
 			facebookBtn.setVisibility(View.GONE);
 		}else{
 			aorRDialog.setTitle("Request accepted by driver");
 			contentTxt.setText("Your request was accepted by " +not.getSenderName());
 		}
 		((TextView)aorRDialog.findViewById(android.R.id.title)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
 		targetTxt.setVisibility(View.GONE);
 		content.setVisibility(View.GONE);
 		okBtn.setVisibility(View.GONE);
 		
 		facebookBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showFacebookDialog();
 				aorRDialog.dismiss();
 			}
 		});
 		
 		markAsReadBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				createMarkedAsReadRequest();
 				aorRDialog.dismiss();
 			}
 		});
 		
 		showBtn.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				showBtn();
 			}
 		});
 		
 		aorRDialog.show();
 	}
 	
 	private static void createCommentForRequest(final NotificationType nt){
 		final EditText input = new EditText(in);
 		new AlertDialog.Builder(in).
 		setTitle("Comment").
 		setMessage("Write a comment").
 		setView(input).
 		setPositiveButton("OK", new OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				createRequest(false, nt, input.getText().toString());
 			}
 		}).show();
 	}
 	
 	/**
 	 * Creates a dialog, which asks the user whether they want to accept or reject
 	 * the Hitchhiker. Creates an accept notification or a reject notification
 	 * depending on the answer.
 	 */
 	
 	private static void createNotificationDialog(){
 		final Dialog notifDialog = new Dialog(in);
 		notifDialog.setTitle("Hitchhiker request");
 		
 		notifDialog.setContentView(R.layout.notif_layout);
 		
 		ImageView okBtn = (ImageView)notifDialog.findViewById(R.id.okBtn);
 		ImageView showBtn = (ImageView)notifDialog.findViewById(R.id.showBtn);
 		TextView contentTxt = (TextView)notifDialog.findViewById(R.id.questionField);
 		
 		contentTxt.setText("Do you want to pick up " +not.getSenderName());
 		
 		okBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				createCommentForRequest(NotificationType.REQUEST_ACCEPT);
 				notifDialog.dismiss();
 			}
 		});
 		
 		showBtn.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				showBtn();
 			}
 		});
 		
 		notifDialog.show();
 		
 	}
 	
 	/**
 	 * Creates a simple dialog to show a message when a Notification is clicked.
 	 * 
 	 * @param title - The title of the dialog that will be created.
 	 * @param msg - The message of the dialog that will be created.
 	 */
 	private static void createMessageDialog(final boolean hitchhiker_cancel,String title,String msg){
 		new AlertDialog.Builder(in).
 		setTitle(title).
 		setMessage(msg).
 		setPositiveButton("OK", new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface arg0, int arg1) {
 				if(hitchhiker_cancel){
 					createCommentForRequest(NotificationType.HITCHHIKER_ACCEPTS_DRIVER_CANCEL);
 				}
 				createMarkedAsReadRequest();
 			}
 		}).
 		setNegativeButton("Cancel", null).
 		
 		show();
 	}
 	
 	private static void createMessageDialogRating(String title,String msg){
 		new AlertDialog.Builder(in).
 		setTitle(title).
 		setMessage(msg).
 		setPositiveButton("Yes", new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface arg0, int arg1) {
 				User u = new User(not.getSenderName(), not.getSenderID());
 				sendRating(u);
 				createMarkedAsReadRequest();
 			}
 		}).
 		setNegativeButton("No", new OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 //				not.setRead(true);
 				createMarkedAsReadRequest();
 			}
 		}).
 		show();
 	}
 	public static void sendRating(User user){
     	UserRequest req = new UserRequest(RequestType.THUMBS_UP, user);
     	
     	try {
 			Response res = RequestTask.sendRequest(req, app);
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void sendMessage(User mid, EditText input){
 		Notification nots = new Notification(app.getUser().getID(), mid.getID(), app.getUser().getFullName(), input.getText().toString(), not.getJourneySerial(), NotificationType.MESSAGE, Calendar.getInstance());
     	NotificationRequest req = new NotificationRequest(app.getUser(), nots);
     	
     	try {
 			Response res = RequestTask.sendRequest(req, app);
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void showFacebookDialog(){
 		Journey journey = new Journey(999);
 		try{
 			SingleJourneyRequest r = new SingleJourneyRequest(RequestType.GET_JOURNEY, app.getUser(), not.getJourneySerial());
 			JourneyResponse response = (JourneyResponse)RequestTask.sendRequest(r, app);
 			if(response.getStatus() == ResponseStatus.OK){
 				if(response.getJourneys().size() > 0){
 					journey = response.getJourneys().get(0);
 				}
 			}
 			else if(response.getStatus() == ResponseStatus.FAILED){
 			}
 		}catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 		Route route = new Route(journey.getRoute().getOwner(), journey.getRoute().getName(), journey.getRoute().getRouteData(), journey.getRoute().getSerial());
 		route.setMapPoints(journey.getRoute().getMapPoints());
 		Intent intent = new Intent(in, no.ntnu.idi.socialhitchhiking.utility.ShareOnFacebook.class);
 		intent.putExtra("isDriver", false);
 		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		app.setJourneyPickupPoint(not.getStartPoint());
 		app.setJourneyDropoffPoint(not.getStopPoint());
 		app.setSelectedNotification(not);
 		app.setSelectedRoute(route);
 		app.setSelectedJourney(journey);
 		app.startActivity(intent);
 	}
 	
 	public static void showBtn(){
 		Journey journey = new Journey(999);
 		
 		try{
 			SingleJourneyRequest r = new SingleJourneyRequest(RequestType.GET_JOURNEY, app.getUser(), not.getJourneySerial());
 			JourneyResponse response = (JourneyResponse)RequestTask.sendRequest(r, app);
 			if(response.getStatus() == ResponseStatus.OK){
 				if(response.getJourneys().size() > 0){
 					journey = response.getJourneys().get(0);
 					
 					Intent intent = new Intent(in, no.ntnu.idi.socialhitchhiking.map.MapActivityJourney.class);
 					MapRoute mr = new MapRoute(journey.getRoute().getOwner(), journey.getRoute().getName(), journey.getRoute().getSerial(), journey.getRoute().getMapPoints());
 					intent.putExtra("Journey", true);
 					intent.putExtra("journeyAccepted", true);
 					intent.putExtra("journeyRejected", false);
 					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 					app.setSelectedMapRoute(mr);
 					app.setSelectedJourney(journey);
 					app.setJourneyPickupPoint(not.getStartPoint());
 					app.setJourneyDropoffPoint(not.getStopPoint());
 					app.setSelectedNotification(not);
 					
 					app.startActivity(intent);
 				}
 			}
 			else if(response.getStatus() == ResponseStatus.FAILED){
 				Toast.makeText(in, "Ride is finished", Toast.LENGTH_SHORT).show();
 			}
 		}catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	
 	
 	public static void createChatDialog(final Notification not){
 		
 		final Dialog messageDialog = new Dialog(in);
 		messageDialog.setContentView(R.layout.message_layout);
 		messageDialog.setTitle("Message");
 		
 		TextView nameTxt = (TextView)messageDialog.findViewById(R.id.nameTxt);
 		TextView contentTxt = (TextView)messageDialog.findViewById(R.id.contentViewField);
 		
 		ImageView replyBtn = (ImageView)messageDialog.findViewById(R.id.replyBtn);
 		ImageView showRideBtn = (ImageView)messageDialog.findViewById(R.id.showJourneyBtn);
 		ImageView markAsReadBtn = (ImageView)messageDialog.findViewById(R.id.markAsReadBtn);
 		ImageView facebookBtn = (ImageView)messageDialog.findViewById(R.id.shareOnFaceBtn);
 		
 		facebookBtn.setVisibility(View.GONE);
 		nameTxt.setText(not.getSenderName());
 		contentTxt.setText(not.getComment());
 		
 		if(not.isRead()){
 			markAsReadBtn.setVisibility(View.GONE);
 		}
 		
 		replyBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				final Dialog replyDialog = new Dialog(in);
 				replyDialog.setContentView(R.layout.replay_layout);
				replyDialog.setTitle("Replay");
 				
 				ImageView sendBtn = (ImageView)replyDialog.findViewById(R.id.sendBtn);
 				
 				TextView sendTxt = (TextView)replyDialog.findViewById(R.id.sendTxt);
 				TextView messageFromTxt = (TextView)replyDialog.findViewById(R.id.messageFromTxt);
 				TextView messageContent = (TextView)replyDialog.findViewById(R.id.messageContent);
 				
 				final EditText inputField = (EditText)replyDialog.findViewById(R.id.input);
 				
 				sendTxt.setText(not.getSenderName());
 				messageFromTxt.setText("Message from: " + not.getSenderName());
 				messageContent.setText(not.getComment());
 				
 				sendBtn.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						User midUser = new User();
 						midUser.setID(not.getSenderID());
 						//sendMessage(midUser, inputField);
 						
 						if(inputField.getText().toString().equals("")){
 							inputField.setHint("Please fill in your message");
 							Toast toast = Toast.makeText(in, "Please fill in your message", Toast.LENGTH_SHORT);
 							toast.setGravity(Gravity.BOTTOM, toast.getXOffset() / 2, toast.getYOffset() / 2);
 							toast.show();
 						}else{
 							sendMessage(midUser, inputField);
 							Toast toast = Toast.makeText(in, "Message sent", Toast.LENGTH_SHORT);
 							toast.setGravity(Gravity.BOTTOM, toast.getXOffset() / 2, toast.getYOffset() / 2);
 							toast.show();
 							replyDialog.dismiss();
 						}
 					}
 				});
 				replyDialog.show();
 			}
 		});
 		
 		showRideBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showBtn();
 				
 			}
 		});
 		
 		markAsReadBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				createMarkedAsReadRequest();
 				messageDialog.dismiss();
 			}
 		});
 		
 		messageDialog.show();
 		
 		
 	}
 	/**
 	 * Creates a simple dialog to show a message when a Notification is clicked.
 	 * 
 	 * @param title - The title of the dialog that will be created.
 	 * @param msg - The message of the dialog that will be created.
 	 */
 	private static void createConfirmDialog(String title,String msg){
 		new AlertDialog.Builder(in).
 		setTitle(title).
 		setMessage(msg).
 		setNeutralButton("OK", null).
 		show();
 	}
 	/**
 	 * Creates a {@link NotificationRequest} to mark a Notification as read,
 	 * and sends it to the server. Creates a dialog that show if the request
 	 * was successfully sent.
 	 * 
 	 */
 	private static boolean createMarkedAsReadRequest() {
 		NotificationRequest req = new NotificationRequest(RequestType.MARK_NOTIFICATION_READ,app.getUser(), not);
 		if(sendNotificationRequest(req)){
 			in.setNotificationRead(not);
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * Creates a {@link NotificationRequest} and sends it to the server.
 	 * Creates a dialog that show if the request was successfully sent.
 	 * 
 	 * @param accept
 	 */
 	private static boolean createRequest(boolean mapmode,NotificationType type,String com) {
 		Notification notif = new Notification(app.getUser().getID(), not.getSenderID(), "",com, not.getJourneySerial(), type);
 		NotificationRequest req = new NotificationRequest(app.getUser(), notif);
 		if(sendNotificationRequest(req)){
 			if(!mapmode)createConfirmDialog("Confirmed", "Successfully sent reply");
 			return createMarkedAsReadRequest();
 		}
 		else {
 			if(!mapmode)createConfirmDialog("ERROR", "Reply was not sent");
 			return false;
 		}
 	}
 	/**
 	 * Sends a notification request.
 	 * 
 	 * @param req - The request to be sent. 
 	 * @return false if something went wrong, true if everything went well.
 	 */
 	public static boolean sendNotificationRequest(Request req){
 		Response res;
 		try {
 			res = RequestTask.sendRequest(req,app);
 			boolean succeded = res.getStatus() == ResponseStatus.OK;
 			return succeded;
 		} catch (ClientProtocolException e) {
 		} catch (IOException e) {
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 }
