 /*
  * Copyright 2011, Qualcomm Innovation Center, Inc.
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package com.vendsy.bartsy.venue;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import android.app.Application;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Base64;
 import android.util.Log;
 
 import com.google.android.gcm.GCMRegistrar;
 import com.vendsy.bartsy.venue.model.AppObservable;
 import com.vendsy.bartsy.venue.model.Order;
 import com.vendsy.bartsy.venue.model.Profile;
 import com.vendsy.bartsy.venue.service.ConnectivityService;
 import com.vendsy.bartsy.venue.utils.Constants;
 import com.vendsy.bartsy.venue.utils.Utilities;
 import com.vendsy.bartsy.venue.view.AppObserver;
 
 /**
  * The ChatAppliation class serves as the Model (in the sense of the common user
  * interface design pattern known as Model-View-Controller) for the chat
  * application.
  * 
  * The ChatApplication inherits from the relatively little-known Android
  * application framework class Application. From the Android developers
  * reference on class Application:
  * 
  * Base class for those who need to maintain global application state. You can
  * provide your own implementation by specifying its name in your
  * AndroidManifest.xml's <application> tag, which will cause that class to be
  * instantiated for you when the process for your application/package is
  * created.
  * 
  * The important property of class Application is that its lifetime coincides
  * with the lifetime of the application, not its activities. Since we have
  * persistent state in our connections to the outside world via our AllJoyn
  * objects, and that state cannot be serialized, saved and restored; we need a
  * persistent object to ensure that state is held if transient objects like
  * Activities are destroyed and recreated by the Android application framework
  * during its normal operation.
  * 
  * This object holds the global state for our chat application, and starts the
  * Android Service that handles the background processing relating to our
  * AllJoyn connections.
  * 
  * Additionally, this class provides the Model for an MVC framework. It provides
  * a relatively abstract idea of what it is the application is doing. For
  * example, we provide methods oriented to conceptual actions (like our user has
  * typed a message) instead of methods oriented to the implementation (like,
  * create an AllJoyn bus object and register it). This allows the user interface
  * to be relatively independent of the channel implementation.
  * 
  * Android Activities can come and go in sometimes surprising ways during the
  * operation of an application. For example, when a phone is rotated from
  * portrait to landscape orientation, the displayed Activities are deleted and
  * recreated in the new orientation. This class holds the persistent state that
  * is required to correctly display Activities when they are recreated.
  */
 public class BartsyApplication extends Application implements AppObservable {
 	private static final String TAG = "Bartsy";
 	public static String PACKAGE_NAME;
 	
 	/**
 	 * When created, the application fires an intent to create the AllJoyn
 	 * service. This acts as sort of a combined view/controller in the overall
 	 * architecture.
 	 */
 	@Override
 	public void onCreate() {
 		Log.i(TAG, "onCreate()");
 		PACKAGE_NAME = getApplicationContext().getPackageName();
 
 		// Start the background connectivity service if running on Alljoyn
 		if (Constants.USE_ALLJOYN) {
 			Intent intent = new Intent(this, ConnectivityService.class);
 			mRunningService = startService(intent);
 			if (mRunningService == null) {
 				Log.i(TAG, "onCreate(): failed to startService()");
 			}
 		}
 
 		// load venue profile if it exists. this is an application-wide
 		// variable.
 		loadVenueProfile();
 
 		// GCM registration
 		// --------------------------------------------------
 		GCMRegistrar.checkDevice(this);
 		GCMRegistrar.checkManifest(this);
 		final String regId = GCMRegistrar.getRegistrationId(this);
 		if (regId.equals("")) {
 			GCMRegistrar.register(this, Utilities.SENDER_ID);
 		} else {
 			Log.v(TAG, "Already registered");
 		}
 		System.out.println("the registration id is:::::" + regId);
 
 		// --------------------------------------------
 
 	}
 
 	/**
 	 * 
 	 * This venue ID represents the venue in which this tablet is setup. This is
 	 * used only on the tablet.
 	 * 
 	 */
 
 	String venueProfileID = null;
 	String venueProfileName = null;
 
 	void loadVenueProfile() {
 		SharedPreferences sharedPref = getSharedPreferences(getResources()
 				.getString(R.string.config_shared_preferences_name),
 				Context.MODE_PRIVATE);
 		venueProfileID = sharedPref.getString("RegisteredVenueId", null);
 		venueProfileName = sharedPref.getString("RegisteredVenueName", null);
 	}
 
 	/*****
 	 * 
 	 * The list of people present (when checked in) is also saved here, in the
 	 * global state. We have all handling code here because the application
 	 * always runs in the background. If there is an activity that displays a
 	 * view of that list listening, we will send an update message, but this
 	 * code will always correctly change the model so that we never lose orders
 	 * even if hte phone (or tablet) is in sleep mode, etc.
 	 * 
 	 */
 
 	public ArrayList<Profile> mPeople = new ArrayList<Profile>();
 	public static final String PEOPLE_UPDATED = "PEOPLE_UPDATED";
 
 	/*
 	 * Called when we have a new person check in a venue
 	 */
 
 	void addPerson(String userid, String name, String location, String info,
 			String description, String image // base64 encoded image
 	) {
 		Log.i(TAG, "New user checked in: " + name + " (" + userid + ")");
 
 		// Decode the user image and create a new incoming profile
 		byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
 		Bitmap img = BitmapFactory.decodeByteArray(decodedString, 0,
 				decodedString.length);
 		Profile profile = new Profile(userid, name, location, info,
 				description, img);
 
 		mPeople.add(profile);
 		notifyObservers(PEOPLE_UPDATED);
 	}
 	
 	public void addPerson(Profile profile) {
 
 //		// Decode the user image and create a new incoming profile
 //		byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
 //		Bitmap img = BitmapFactory.decodeByteArray(decodedString, 0,
 //				decodedString.length);
		Profile existingPeople = null;
		for (Profile people : mPeople) {
			if(profile.userID.equals(people.userID)){
				existingPeople = people;
				break;
			}
		}
		if(existingPeople==null){
			mPeople.add(profile);
			notifyObservers(PEOPLE_UPDATED);
		}
 	}
 
 	
 	/**
 	 * Called when we have a person check out of a venue
 	 */
 	void removePerson(String profileId) {
 		for (Profile profile : mPeople) {
 			if(profileId.equals(profile.userID)){
 				mPeople.remove(profile);
 				break;
 			}
 		}
 		
 		notifyObservers(PEOPLE_UPDATED);
 	}
 
 	/**********
 	 * 
 	 * The order list is saved in the global application state. This is done to
 	 * avoid losing any orders while the other activities are swapped in and out
 	 * as the user navigates in different screens.
 	 * 
 	 */
 
 	public ArrayList<Order> mOrders = new ArrayList<Order>();
 
 	public static final String ORDERS_UPDATED = "ORDERS_UPDATED";
 
 	/*
 	 * This add a new order after verifying the person placing it is currently
 	 * checked in this venue
 	 */
 
 	void addOrder(String serverOrderID, String title,
 			String description, String price, String userid) {
 
 		Log.i(TAG, "New " + title + " for: " + userid);
 
 		// Find the person who placed the order in the list of people in this
 		// bar. If not found, don't accept the order
 		Profile person = null;
 		for (Profile p : mPeople) {
 			if (p.userID.equalsIgnoreCase(userid)) {
 				// User found
 				person = p;
 				break;
 			}
 		}
 		if (person == null) {
 			Log.d(TAG, "Error processing command. user not checked in: "
 					+ userid);
 			return;
 		}
 
 		// Create a new order
 		Order order = new Order();
 		order.initialize( 
 				serverOrderID, // server-side order number
 				title, // Title
 				description, // Description
 				price, // Price
 				"", // Image resource
 				person); // Order sender ID
 
 		mOrders.add(order);
 		notifyObservers(ORDERS_UPDATED);
 	}
 	
 	public void addOrder(Order order) {
 		// Find the person who placed the order in the list of people in this
 		// bar. If not found, don't accept the order
 		order.orderSender = null;
 		for (Profile p : mPeople) {
 			if (p.userID.equalsIgnoreCase(order.profileId)) {
 				// User found
 				order.orderSender = p;
 				break;
 			}
 		}
 		if (order.orderSender == null) {
 			Log.d(TAG, "Error processing command. user not checked in: "
 					+ order.profileId);
 			return;
 		}
 		
 		// Add the order to the list of orders
 		mOrders.add(order);
 		notifyObservers(ORDERS_UPDATED);
 	}
 
 
 	/************************************************************************
 	 * 
 	 * 
 	 * 
 	 * TODO - This is the AllJoyn code
 	 * 
 	 * 
 	 * 
 	 */
 
 	ComponentName mRunningService = null;
 
 	/**
 	 * Since our application is "rooted" in this class derived from Application
 	 * and we have a long-running service, we can't just call finish in one of
 	 * the Activities. We have to orchestrate it from here. We send an event
 	 * notification out to all of our observers which tells them to exit.
 	 * 
 	 * Note that as a result of the notification, all of the observers will stop
 	 * -- as they should. One of the things that will stop is the AllJoyn
 	 * Service. Notice that it is started in the onCreate() method of the
 	 * Application. As noted in the Android documentation, the Application class
 	 * never gets torn down, nor does it provide a way to tear itself down.
 	 * Thus, if the Chat application is ever run again, we need to have a way of
 	 * detecting the case where it is "re-run" and then "re-start" the service.
 	 */
 	public void quit() {
 		notifyObservers(APPLICATION_QUIT_EVENT);
 		mRunningService = null;
 	}
 
 	/**
 	 * Application components call this method to indicate that they are alive
 	 * and may have need of the AllJoyn Service. This is required because the
 	 * Android Application class doesn't have an end to its lifecycle other than
 	 * through "kill -9". See quit().
 	 */
 	public void checkin() {
 		Log.i(TAG, "checkin()");
 		if (Constants.USE_ALLJOYN && mRunningService == null) {
 			Log.i(TAG, "checkin():  Starting the AllJoynService");
 			Intent intent = new Intent(this, ConnectivityService.class);
 			mRunningService = startService(intent);
 			if (mRunningService == null) {
 				Log.i(TAG, "checkin(): failed to startService()");
 			}
 		}
 	}
 
 	public static final String APPLICATION_QUIT_EVENT = "APPLICATION_QUIT_EVENT";
 
 	/**
 	 * This is the method that AllJoyn Service calls to tell us that an error
 	 * has happened. We are provided a module, which corresponds to the high-
 	 * level "hunk" of code where the error happened, and a descriptive string
 	 * that we do not interpret.
 	 * 
 	 * We expect the user interface code to sort out the best activity to tell
 	 * the user about the error (by calling getErrorModule) and then to call in
 	 * to get the string.
 	 */
 	public synchronized void alljoynError(Module m, String s) {
 		mModule = m;
 		mErrorString = s;
 		notifyObservers(ALLJOYN_ERROR_EVENT);
 	}
 
 	/**
 	 * Return the high-level module that caught the last AllJoyn error.
 	 */
 	public Module getErrorModule() {
 		return mModule;
 	}
 
 	/**
 	 * The high-level module that caught the last AllJoyn error.
 	 */
 	private Module mModule = Module.NONE;
 
 	/**
 	 * Enumeration of the high-level moudules in the system. There is one value
 	 * per module.
 	 */
 	public static enum Module {
 		NONE, GENERAL, USE, HOST
 	}
 
 	/**
 	 * Return the error string stored when the last AllJoyn error happened.
 	 */
 	public String getErrorString() {
 		return mErrorString;
 	}
 
 	/**
 	 * The string representing the last AllJoyn error that happened in the
 	 * AllJoyn Service.
 	 */
 	private String mErrorString = "ER_OK";
 
 	/**
 	 * The object we use in notifications to indicate that an AllJoyn error has
 	 * happened.
 	 */
 	public static final String ALLJOYN_ERROR_EVENT = "ALLJOYN_ERROR_EVENT";
 
 	/**
 	 * Called from the AllJoyn Service when it gets a FoundAdvertisedName. We
 	 * know by construction that the advertised name will correspond to a chat
 	 * channel. Note that the channel here is the complete well-known name of
 	 * the bus attachment advertising the channel. In most other places it is
 	 * simply the channel name, which is the final segment of the well-known
 	 * name.
 	 */
 	public synchronized void addFoundChannel(String channel) {
 		Log.i(TAG, "addFoundChannel(" + channel + ")");
 		removeFoundChannel(channel);
 		mChannels.add(channel);
 		Log.i(TAG, "addFoundChannel(): added " + channel);
 		notifyObservers(NEW_CHANNEL_FOUND_EVENT);
 
 	}
 
 	/**
 	 * The object we use in notifications to indicate that a channel has been
 	 * found. By default Bartsy joins new channels automatically unless it's
 	 * already connected to a channel. If it's already connected it adds the new
 	 * channel to the list of channels in the main action bar UI and notifies
 	 * the user with a notification that there are other services available
 	 * nearby
 	 */
 	public static final String NEW_CHANNEL_FOUND_EVENT = "NEW_CHANNEL_FOUND_EVENT";
 
 	/**
 	 * Called from the AllJoyn Service when it gets a LostAdvertisedName. We
 	 * know by construction that the advertised name will correspond to an chat
 	 * channel.
 	 */
 	public synchronized void removeFoundChannel(String channel) {
 		Log.i(TAG, "removeFoundChannel(" + channel + ")");
 
 		for (Iterator<String> i = mChannels.iterator(); i.hasNext();) {
 			String string = i.next();
 			if (string.equals(channel)) {
 				Log.i(TAG, "removeFoundChannel(): removed " + channel);
 				i.remove();
 			}
 		}
 	}
 
 	/**
 	 * Whenever the user is asked for a channel to join, it needs the list of
 	 * channels found via FoundAdvertisedName. This method provides that list.
 	 * Since we have no idea how or when the caller is going to access or change
 	 * the list, and we are deeply paranoid, we provide a deep copy.
 	 */
 	public synchronized List<String> getFoundChannels() {
 		Log.i(TAG, "getFoundChannels()");
 		List<String> clone = new ArrayList<String>(mChannels.size());
 		for (String string : mChannels) {
 			Log.i(TAG, "getFoundChannels(): added " + string);
 			clone.add(new String(string));
 		}
 		return clone;
 	}
 
 	/**
 	 * The channels list is the list of all well-known names that correspond to
 	 * channels we might conceivably be interested in. We expect that the "use"
 	 * GUID will allow the local user to have this list displayed in a
 	 * "join channel" dialog, whereupon she will choose one. This will
 	 * eventually result in a joinSession call out from the AllJoyn Service
 	 */
 	private List<String> mChannels = new ArrayList<String>();
 
 	/**
 	 * The application has three ideas about the state of its channels. This is
 	 * very detailed for a real application, but since this is an AllJoyn
 	 * sample, we think it is important to convey the detailed state back to our
 	 * user, whom we assume knows what it all means.
 	 * 
 	 * We have a basic bus attachment state, which reflects the fact that we
 	 * can't do anything without a bus attachment. When the service comes up it
 	 * automatically connects and starts discovering other instances of the
 	 * application, so this isn't terribly interesting.
 	 */
 	public ConnectivityService.BusAttachmentState mBusAttachmentState = ConnectivityService.BusAttachmentState.DISCONNECTED;
 
 	/**
 	 * Set the status of the "host" channel. The AllJoyn Service part of the
 	 * Application is expected to make this call to set the status to reflect
 	 * the status of the underlying AllJoyn session.
 	 */
 	public synchronized void hostSetChannelState(
 			ConnectivityService.HostChannelState state) {
 		mHostChannelState = state;
 		notifyObservers(HOST_CHANNEL_STATE_CHANGED_EVENT);
 	}
 
 	/**
 	 * Get the state of the "use" channel.
 	 */
 	public synchronized ConnectivityService.HostChannelState hostGetChannelState() {
 		return mHostChannelState;
 	}
 
 	/**
 	 * The "host" state which reflects the state of the part of the system
 	 * related to hosting an chat channel. In a "real" application this kind of
 	 * detail probably isn't appropriate, but we want to do so for this sample.
 	 */
 	private ConnectivityService.HostChannelState mHostChannelState = ConnectivityService.HostChannelState.IDLE;
 
 	/**
 	 * Set the name part of the "host" channel. Since we are going to "use" a
 	 * channel that is implemented remotely and discovered through an AllJoyn
 	 * FoundAdvertisedName, this must come from a list of advertised names.
 	 * These names are our channels, and so we expect the GUI to choose from
 	 * among the list of channels it retrieves from getFoundChannels().
 	 * 
 	 * Since we are talking about user-level interactions here, we are talking
 	 * about the final segment of a well-known name representing a channel at
 	 * this point.
 	 */
 	public synchronized void hostSetChannelName(String name) {
 		mHostChannelName = name;
 		notifyObservers(HOST_CHANNEL_STATE_CHANGED_EVENT);
 	}
 
 	/**
 	 * Get the name part of the "use" channel.
 	 */
 	public synchronized String hostGetChannelName() {
 		return mHostChannelName;
 	}
 
 	/**
 	 * The name of the "host" channel which the user has selected.
 	 */
 	private String mHostChannelName;
 
 	/**
 	 * The object we use in notifications to indicate that the state of the
 	 * "host" channel or its name has changed.
 	 */
 	public static final String HOST_CHANNEL_STATE_CHANGED_EVENT = "HOST_CHANNEL_STATE_CHANGED_EVENT";
 
 	/**
 	 * Set the status of the "use" channel. The AllJoyn Service part of the
 	 * appliciation is expected to make this call to set the status to reflect
 	 * the status of the underlying AllJoyn session.
 	 */
 	public synchronized void useSetChannelState(
 			ConnectivityService.UseChannelState state) {
 		mUseChannelState = state;
 		notifyObservers(USE_CHANNEL_STATE_CHANGED_EVENT);
 	}
 
 	/**
 	 * Get the state of the "use" channel.
 	 */
 	public synchronized ConnectivityService.UseChannelState useGetChannelState() {
 		return mUseChannelState;
 	}
 
 	/**
 	 * The "use" state which reflects the state of the part of the system
 	 * related to using a remotely hosted chat channel. In a "real" application
 	 * this kind of detail probably isn't appropriate, but we want to do so for
 	 * this sample.
 	 */
 	private ConnectivityService.UseChannelState mUseChannelState = ConnectivityService.UseChannelState.IDLE;
 
 	/**
 	 * The name of the "use" channel which the user has selected.
 	 */
 	private String mUseChannelName = null;
 
 	/**
 	 * Set the name part of the "use" channel. Since we are going to "use" a
 	 * channel that is implemented remotely and discovered through an AllJoyn
 	 * FoundAdvertisedName, this must come from a list of advertised names.
 	 * These names are our channels, and so we expect the GUI to choose from
 	 * among the list of channels it retrieves from getFoundChannels().
 	 * 
 	 * Since we are talking about user-level interactions here, we are talking
 	 * about the final segment of a well-known name representing a channel at
 	 * this point.
 	 */
 	public synchronized void useSetChannelName(String name) {
 		mUseChannelName = name;
 		notifyObservers(USE_CHANNEL_STATE_CHANGED_EVENT);
 	}
 
 	/**
 	 * Get the name part of the "use" channel.
 	 */
 	public synchronized String useGetChannelName() {
 		return mUseChannelName;
 	}
 
 	/**
 	 * The object we use in notifications to indicate that the state of the
 	 * "use" channel or its name has changed.
 	 */
 	public static final String USE_CHANNEL_STATE_CHANGED_EVENT = "USE_CHANNEL_STATE_CHANGED_EVENT";
 
 	/**
 	 * This is the method that the "use" tab user interface calls when the user
 	 * indicates that she wants to join a channel. The channel name must have
 	 * been previously set with a call to setUseChannelName(). The "use" channel
 	 * is the channel that we talk about in the "Use" tab. Since it's a remote
 	 * channel in a remote bus attachment, we need to tell the AllJoyn Service
 	 * to go join the corresponding session.
 	 */
 	public synchronized void useJoinChannel() {
 		clearHistory();
 		notifyObservers(USE_CHANNEL_STATE_CHANGED_EVENT);
 		notifyObservers(USE_JOIN_CHANNEL_EVENT);
 	}
 
 	/**
 	 * The object we use in notifications to indicate that user has requested
 	 * that we join a channel in the "use" tab.
 	 */
 	public static final String USE_JOIN_CHANNEL_EVENT = "USE_JOIN_CHANNEL_EVENT";
 
 	/**
 	 * This is the method that the "use" tab user interface calls when the user
 	 * indicates that she wants to leave a channel. Since we're talking about a
 	 * remote channel corresponding to a session with a remote bus attachment,
 	 * we needto tell the AllJoyn Service to leave the corresponding session.
 	 */
 	public synchronized void useLeaveChannel() {
 		notifyObservers(USE_CHANNEL_STATE_CHANGED_EVENT);
 		notifyObservers(USE_LEAVE_CHANNEL_EVENT);
 	}
 
 	/**
 	 * The object we use in notifications to indicate that user has requested
 	 * that we leave a channel in the "use" tab.
 	 */
 	public static final String USE_LEAVE_CHANNEL_EVENT = "USE_LEAVE_CHANNEL_EVENT";
 
 	/**
 	 * This is the method that the "host" tab user interface calls when the user
 	 * has completed providing her preferences for hosting a channel.
 	 */
 	public synchronized void hostInitChannel() {
 		notifyObservers(HOST_CHANNEL_STATE_CHANGED_EVENT);
 		notifyObservers(HOST_INIT_CHANNEL_EVENT);
 	}
 
 	/**
 	 * The object we use in notifications to indicate that user has requested
 	 * that we initialize the host channel parameters in the "use" tab.
 	 */
 	public static final String HOST_INIT_CHANNEL_EVENT = "HOST_INIT_CHANNEL_EVENT";
 
 	/**
 	 * This is the method that the "host" tab user interface calls when the user
 	 * indicates that she wants to start hosting a channel.
 	 */
 	public synchronized void hostStartChannel() {
 		notifyObservers(HOST_CHANNEL_STATE_CHANGED_EVENT);
 		notifyObservers(HOST_START_CHANNEL_EVENT);
 	}
 
 	/**
 	 * The object we use in notifications to indicate that user has requested
 	 * that we initialize the host channel parameters in the "use" tab.
 	 */
 	public static final String HOST_START_CHANNEL_EVENT = "HOST_START_CHANNEL_EVENT";
 
 	/**
 	 * This is the method that the "host" tab user interface calls when the user
 	 * indicates that she wants to stop hosting a channel.
 	 */
 	public synchronized void hostStopChannel() {
 		notifyObservers(HOST_CHANNEL_STATE_CHANGED_EVENT);
 		notifyObservers(HOST_STOP_CHANNEL_EVENT);
 	}
 
 	/**
 	 * The object we use in notifications to indicate that user has requested
 	 * that we initialize the host channel parameters in the "use" tab.
 	 */
 	public static final String HOST_STOP_CHANNEL_EVENT = "HOST_STOP_CHANNEL_EVENT";
 
 	/**
 	 * Whenever our local user types a message, we need to send it out on the
 	 * channel, which we do by calling addOutboundItem. This will eventually
 	 * result in an AllJoyn Bus Signal being sent to the other participants on
 	 * the channel. Since the sessions that implement the channel don't "echo"
 	 * back to the source, we need to echo the message into our history.
 	 */
 	public synchronized void newLocalUserMessage(String message) {
 		addInboundItem("Me", message);
 		if (useGetChannelState() == ConnectivityService.UseChannelState.JOINED) {
 			addOutboundItem(message);
 		}
 	}
 
 	/**
 	 * Whenever a user types a message into the channel, we expect the AllJoyn
 	 * Service local to that user to send the message to everyone participating
 	 * on the channel. At each participant, the messages arrive in the AllJoyn
 	 * Service as a Bus Signal. The Service handles the signals and passes the
 	 * associated messages on to us here. We expect the nickname to be the
 	 * unique ID of the sending bus attachment. This is not very user friendly,
 	 * but is convenient and guaranteed to be unique.
 	 */
 	public synchronized void newRemoteUserMessage(String nickname,
 			String message) {
 		addInboundItem(nickname, message);
 	}
 
 	final int OUTBOUND_MAX = 5;
 
 	/**
 	 * The object we use in notifications to indicate that the the user has
 	 * entered a message and it is queued to be sent to the outside world.
 	 */
 	public static final String OUTBOUND_CHANGED_EVENT = "OUTBOUND_CHANGED_EVENT";
 
 	/**
 	 * The outbound list is the list of all messages that have been originated
 	 * by our local user and are designed for the outside world.
 	 */
 	private List<String> mOutbound = new ArrayList<String>();
 
 	/**
 	 * Whenever the local user types a message for distribution to the channel
 	 * it calls newLocalMessage. We are called to queue up the message and send
 	 * a notification to all of our observers indicating that the we have
 	 * something ready to go out. We expect that the AllJoyn Service will
 	 * eventually respond by calling back in here to get items off of the queue
 	 * and send them down the session corresponding to the channel.
 	 */
 	private void addOutboundItem(String message) {
 		if (mOutbound.size() == OUTBOUND_MAX) {
 			mOutbound.remove(0);
 		}
 		mOutbound.add(message);
 		notifyObservers(OUTBOUND_CHANGED_EVENT);
 	}
 
 	/**
 	 * Whenever the local user types a message for distribution to the channel
 	 * it is queued to a list of outbound messages. The AllJoyn Service is
 	 * notified and calls in here to get the outbound messages that need to be
 	 * sent.
 	 */
 	public synchronized String getOutboundItem() {
 		if (mOutbound.isEmpty()) {
 			return null;
 		} else {
 			return mOutbound.remove(0);
 		}
 	}
 
 	/**
 	 * The object we use in notifications to indicate that the history state of
 	 * the model has changed and observers need to synchronize with it.
 	 */
 	public static final String HISTORY_CHANGED_EVENT = "HISTORY_CHANGED_EVENT";
 
 	/**
 	 * Whenever a message comes in from the AllJoyn Service over its channel
 	 * session, it calls in here. We just add the message item to the history
 	 * list, with the "nickname" provided by Service. This is currently expected
 	 * to be the unique name of the bus attachment originating the message. Once
 	 * the message is saved in the history, a change notification will be sent
 	 * to all observers indicating that the history has changed. The user
 	 * interface part of the application is then expected to wake up and
 	 * syncrhonize itself to the new history.
 	 */
 	private void addInboundItem(String nickname, String message) {
 		addHistoryItem(nickname, message);
 	}
 
 	/**
 	 * Don't keep an infinite amount of history. Although we don't want to admit
 	 * it, this is a toy application, so we just keep a little history.
 	 */
 	final int HISTORY_MAX = 20;
 
 	/**
 	 * The history list is the list of all messages that have been originated or
 	 * recieved by the "use" channel.
 	 */
 	private List<String> mHistory = new ArrayList<String>();
 
 	/**
 	 * Whenever a user in the channel types a message, it needs to result in the
 	 * history being updated with the nickname of the user originating the
 	 * message and the message itself. We keep a history list of a given maximum
 	 * size just for general principles. This history list contains the local
 	 * time at which the message was recived, the nickname of the user who
 	 * originated the message and the message itself. We send a change
 	 * notification to all observers indicating that the history has changed
 	 * when we modify it.
 	 */
 	private void addHistoryItem(String nickname, String message) {
 		if (mHistory.size() == HISTORY_MAX) {
 			mHistory.remove(0);
 		}
 
 		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
 		Date date = new Date();
 		// mHistory.add("[" + dateFormat.format(date) + "] (" + nickname + ") "
 		// + message);
 
 		// Don't add local history messages for now - TODO
 		if (nickname.equalsIgnoreCase("me"))
 			return;
 
 		mHistory.add(message);
 		notifyObservers(HISTORY_CHANGED_EVENT);
 	}
 
 	/**
 	 * Clear the history list. Whenever a user joins a new channel, we want to
 	 * get rid of any existing history to avoid confusion.
 	 */
 	private void clearHistory() {
 		mHistory.clear();
 		notifyObservers(HISTORY_CHANGED_EVENT);
 	}
 
 	/**
 	 * Whenever a new message is added to the history list, an update
 	 * notification is sent to all of the observers registered to this object
 	 * that indicates that the history list has changed. When the observer hears
 	 * that the list has changed, it calls in here to get the new contents.
 	 * Since we have no idea how or when the caller is going to access or change
 	 * the list, and we are deeply paranoid, we provide a deep copy.
 	 */
 	public synchronized List<String> getHistory() {
 		List<String> clone = new ArrayList<String>(mHistory.size());
 		for (String string : mHistory) {
 			clone.add(new String(string));
 		}
 		return clone;
 	}
 
 	public synchronized String getLastMessage() {
 		if (mHistory.size() == 0)
 			return null;
 		else
 			return mHistory.get(mHistory.size() - 1);
 	}
 
 	/**
 	 * This object is really the model of a model-view-controller architecture.
 	 * The observer/observed design pattern is used to notify view-controller
 	 * objects when the model has changed. The observed object is this object,
 	 * the model. Observers correspond to the view-controllers which in this
 	 * case are the Android Activities (corresponding to the use tab and the
 	 * hsot tab) and the Android Service that does all of the AllJoyn work. When
 	 * an observer wants to register for change notifications, it calls here.
 	 */
 	@Override
 	public synchronized void addObserver(AppObserver obs) {
 		Log.i(TAG, "addObserver(" + obs + ")");
 		if (mObservers.indexOf(obs) < 0) {
 			mObservers.add(obs);
 		}
 	}
 
 	/**
 	 * When an observer wants to unregister to stop receiving change
 	 * notifications, it calls here.
 	 */
 	@Override
 	public synchronized void deleteObserver(AppObserver obs) {
 		Log.i(TAG, "deleteObserver(" + obs + ")");
 		mObservers.remove(obs);
 	}
 
 	/**
 	 * This object is really the model of a model-view-controller architecture.
 	 * The observer/observed design pattern is used to notify view-controller
 	 * objects when the model has changed. The observed object is this object,
 	 * the model. Observers correspond to the view-controllers which in this
 	 * case are the Android Activities (corresponding to the use tab and the
 	 * Host tab) and the Android Service that does all of the AllJoyn work. When
 	 * the model (this object) wants to notify its observers that some
 	 * interesting event has happened, it calls here and provides an object that
 	 * identifies what has happened. To keep things obvious, we pass a
 	 * descriptive string which is then sent to all observers. They can decide
 	 * to act or not based on the content of the string.
 	 */
 	private void notifyObservers(Object arg) {
 		Log.i(TAG, "notifyObservers(" + arg + ")");
 		for (AppObserver obs : mObservers) {
 			Log.i(TAG, "notify observer = " + obs);
 			obs.update(this, arg);
 		}
 	}
 
 	/**
 	 * The observers list is the list of all objects that have registered with
 	 * us as observers in order to get notifications of interesting events.
 	 */
 	private List<AppObserver> mObservers = new ArrayList<AppObserver>();
 }
