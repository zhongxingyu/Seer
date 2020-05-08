 package ch.bfh.evoting.alljoyn;
 /******************************************************************************
  * Copyright 2013, Qualcomm Innovation Center, Inc.
  *
  *    All rights reserved.
  *    This file is licensed under the 3-clause BSD license in the NOTICE.txt
  *    file for this project. A copy of the 3-clause BSD license is found at:
  *
  *        http://opensource.org/licenses/BSD-3-Clause.
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the license is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the license for the specific language governing permissions and
  *    limitations under the license.
  *    
  *    Modified by Phil��mon von Bergen
  ******************************************************************************/
 
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 import org.alljoyn.bus.BusException;
 import org.alljoyn.bus.BusObject;
 import org.alljoyn.bus.Status;
 import org.alljoyn.bus.annotation.BusSignalHandler;
 
 import org.alljoyn.cops.peergroupmanager.PeerGroupManager;
 import org.alljoyn.cops.peergroupmanager.BusObjectData;
 import org.alljoyn.cops.peergroupmanager.PeerGroupListener;
 
 import ch.bfh.evoting.alljoyn.AllJoynMessage.Type;
 import ch.bfh.evoting.alljoyn.util.JavaSerialization;
 import ch.bfh.evoting.alljoyn.util.SerializationUtil;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Base64;
 import android.util.Log;
 
 /**
  * This handler run in its own thread an queues the message it has to process
  * Original class from AllJoyn PeerManagerApp example
  * Adapted by Philémon von Bergen
  *
  */
 public class BusHandler extends Handler {
 
 	/*
 	 * Constants
 	 */
 	private static final String TAG = BusHandler.class.getSimpleName();
 	private static final String SERVICE_NAME = "ch.bfh.evoting";
 	private static final String PREFS_NAME = "network_preferences";
 
 	private static final String MESSAGE_PARTS_SEPARATOR = "||";
 
 	/*
 	 * AllJoyn
 	 */
 	private SimpleService   mSimpleService = new SimpleService();
 	private PeerGroupManager  mGroupManager;
 
 	/*
 	 * Identity informations
 	 */
 	private HashMap<String, Identity> identityMap = new HashMap<String, Identity>();
 	private SharedPreferences userDetails;
 
 	/*
 	 * Other
 	 */
 	private Context context;
 	private String lastJoinedNetwork;
 	private boolean amIAdmin = false;
 	private boolean connected = false;
 	
 	private MessageEncrypter messageEncrypter;
 	private MessageAuthenticater messageAuthenticater;
 	private AllJoynMessage signatureVerificationTask;
 	private SerializationUtil su;
 
 
 	public static final int INIT = 1;
 	public static final int CREATE_GROUP = 2;
 	public static final int DESTROY_GROUP = 3;
 	public static final int JOIN_GROUP = 4;
 	public static final int LEAVE_GROUP = 5;
 	public static final int UNLOCK_GROUP = 6;
 	public static final int LOCK_GROUP = 7;
 	public static final int SET_PORT = 8;
 	public static final int JOIN_OR_CREATE = 9;
 	public static final int DISCONNECT = 10;
 	public static final int PING = 13;
 	public static final int REPROCESS_MESSAGE = 14;
 
 	/**
 	 * Initialization of the bus handler
	 * @param looper
 	 * @param ctx Android context of the application
 	 */
 	public BusHandler(Looper looper, Context ctx) {
 		super(looper);
 		// Connect to an AllJoyn object.
 		this.doInit();
 		this.context = ctx;
 		userDetails = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
 
 		su = new SerializationUtil(new JavaSerialization());
 		
 		messageEncrypter = new MessageEncrypter();
 		messageAuthenticater = new MessageAuthenticater();
 		//We generate a new key pair each time the app is started, so we do not need to save it in order to reuse it later
 		//Creating a new key pair takes time, so we create only 512 bits keys. This is sufficient since the key pair is used during 
 		//maximum one hour and a brute force attack should be done online
 		messageAuthenticater.generateKeys();
 	}
 
 
 	@Override
 	public void handleMessage(Message msg) {
 		Status status =  null;
 		Intent intent;
 		switch(msg.what) {
 		case INIT: {
 			doInit();
 			break;
 		}
 		case CREATE_GROUP: {
 			Bundle data = msg.getData();
 			String groupName = data.getString("groupName");
 			String groupPassword = data.getString("groupPassword");
 			status = doCreateGroup(groupName, groupPassword);
 			Log.d(TAG, "status of group creation "+ status);
 			switch(status){
 			case OK:
 				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("NetworkServiceStarted"));
 				break;
 			case ALLJOYN_JOINSESSION_REPLY_ALREADY_JOINED:
 				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("NetworkServiceStarted"));
 				break;
 			case INVALID_DATA:
 				//invalid group name
 				intent = new Intent("networkConnectionFailed");
 				intent.putExtra("error", 1);
 				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 				break;
 			case ALREADY_FINDING:
 				//group name already exists
 				intent = new Intent("networkConnectionFailed");
 				intent.putExtra("error", 2);
 				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 				break;
 			default:
 				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("networkConnectionFailed"));
 				break;
 			}
 			break;
 		}
 		case DESTROY_GROUP : {
 			status = doDestroyGroup((String) msg.obj);
 			break;
 		}
 		case JOIN_GROUP: {
 			Bundle data = msg.getData();
 			String groupName = data.getString("groupName");
 			String groupPassword = data.getString("groupPassword");
 			String saltShortDigest = data.getString("saltShortDigest");
 			status = doJoinGroup(groupName, groupPassword, saltShortDigest); 
 			Log.d(TAG, "status of group join "+ status);
 			switch(status){
 
 			case OK:
 				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("NetworkServiceStarted"));
 				break;
 			case ALLJOYN_JOINSESSION_REPLY_ALREADY_JOINED:
 				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("NetworkServiceStarted"));
 				break;
 			case INVALID_DATA:
 				//invalid group name
 				intent = new Intent("networkConnectionFailed");
 				intent.putExtra("error", 1);
 				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 				break;
 			case ALLJOYN_FINDADVERTISEDNAME_REPLY_FAILED:
 				//group not found
 				intent = new Intent("networkConnectionFailed");
 				intent.putExtra("error", 3);
 				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 				break;
 			case ALLJOYN_JOINSESSION_REPLY_FAILED:
 				//group not joined
 				intent = new Intent("networkConnectionFailed");
 				intent.putExtra("error", 4);
 				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 				break;
 			default:
 				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("networkConnectionFailed"));
 				break;
 			}
 			break;
 		}
 		case LEAVE_GROUP : {
 			status = doLeaveGroup((String) msg.obj);
 			break;
 		}
 		case UNLOCK_GROUP : {
 			status = doUnlockGroup((String) msg.obj);
 			break;
 		}
 		case LOCK_GROUP : {
 			status = doLockGroup((String) msg.obj);
 			break;
 		}
 		case SET_PORT : {
 			doSetPort((Short) msg.obj);
 			break;
 		}
 		case JOIN_OR_CREATE : {
 			status = doJoinOrCreate((String) msg.obj);
 			break;
 		}
 		case PING: {
 			Bundle data = msg.getData();
 			String groupName = data.getString("groupName");
 			String pingString = data.getString("pingString");
 			boolean encrypted = data.getBoolean("encrypted", true);
 			Type type = (Type)data.getSerializable("type");
 			doPing(groupName, pingString, encrypted, type);
 			break;
 		}
 		case REPROCESS_MESSAGE: {
 			Bundle data = msg.getData();
 			AllJoynMessage message = (AllJoynMessage)data.getSerializable("message");
 			this.processMessage(message);
 			break;
 		}
 		case DISCONNECT: {
 			doDisconnect();
 			break;
 		}
 		default:
 			break;
 		}
 	}
 
 	/******************************************************************************
 	 * 
 	 * Network actions
 	 *
 	 ******************************************************************************/
 
 	/**
 	 * Initialize AllJoyn
 	 */
 	private void doInit() {
 		PeerGroupListener pgListener = new PeerGroupListener() {
 			
 			@Override
 			public void foundAdvertisedName(String groupName, short transport) {}
 
 			@Override
 			public void lostAdvertisedName(String groupName, short transport) {}
 
 			@Override
 			public void groupLost(String groupName) {
 				if(mGroupManager.listHostedGroups().contains(groupName) && mGroupManager.getNumPeers(groupName)==1){
 					//signal was send because admin stays alone in the group
 					//not necessary to manage this case for us
 					Log.d(TAG,"Group destroyed event ignored");
 					return;
 				}
 				Log.d(TAG, "Group "+groupName+" was destroyed.");
 				Intent i = new Intent("groupDestroyed");
 				i.putExtra("groupName", groupName);
 				LocalBroadcastManager.getInstance(context).sendBroadcast(i);
 			}
 
 			@Override
 			public void peerAdded(String busId, String groupName, int numParticipants){
 				Log.d(TAG, "peer added");
 
 				if(amIAdmin){
 					Log.d(TAG, "Sending salt "+ Base64.encodeToString(messageEncrypter.getSalt(), Base64.DEFAULT));
 					Message msg = obtainMessage(BusHandler.PING);
 					Bundle data = new Bundle();
 					data.putString("groupName", lastJoinedNetwork);
 					data.putString("pingString", Base64.encodeToString(messageEncrypter.getSalt(), Base64.DEFAULT));
 					data.putBoolean("encrypted", false);
 					data.putSerializable("type", Type.SALT);
 					msg.setData(data);
 					sendMessage(msg);
 				}
 
 				//update UI
 				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("participantStateUpdate"));
 
 			}
 			
 			@Override
 			public void peerRemoved(String peerId, String groupName,
 					int numPeers) {
 				//update UI
 				Log.d(TAG, "peer left");
 				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("participantStateUpdate"));
 				super.peerRemoved(peerId, groupName, numPeers);
 			}
 			
 		};
 
 		ArrayList<BusObjectData> busObjects = new ArrayList<BusObjectData>();
 		busObjects.add(new BusObjectData(mSimpleService, "/SimpleService"));
 		mGroupManager = new PeerGroupManager(SERVICE_NAME, pgListener, busObjects);
 		mGroupManager.registerSignalHandlers(this);
 	}
 
 	/**
 	 * Disconnect AllJoyn
 	 */
 	private void doDisconnect() {
 		mGroupManager.cleanup();
 		connected = false;
 		getLooper().quit();
 	}
 
 	/**
 	 * Create a group
 	 * @param groupName name of the group (must begin with a character, not a number!)
 	 * @return status of the creation
 	 */
 	private Status doCreateGroup(String groupName, String groupPassword) {
 		lastJoinedNetwork = null;
 		//If group already exists, connection will fail
 		//if multicast is not supported on the network, listFoundGroups
 		//will be empty, so this it will only be detected during the group creation
 		//that this name is already used.
 		if(mGroupManager.listFoundGroups().contains(groupName)){
 			return Status.ALREADY_FINDING;
 		}
 		messageEncrypter.reset();
 
 		//Create a salt and derive the key
 		messageEncrypter.setPassword(groupPassword);
 		messageEncrypter.generateSalt();
 
 		//create the group
 		Status status = mGroupManager.createGroup(groupName);
 		if(status == Status.OK || status == Status.ALLJOYN_JOINSESSION_REPLY_ALREADY_JOINED){
 			//save my identity
 			String myName = userDetails.getString("identification", "");
 			identityMap.put(this.getIdentification(), new Identity(myName, messageAuthenticater.getMyPublicKey()));
 			//some flags
 			lastJoinedNetwork = groupName;
 			amIAdmin = true;
 			connected = true;
 		}
 		return status;
 	}
 
 	/**
 	 * Destroy a group
 	 * @param groupName name of the group
 	 * @return status of the destruction of the group
 	 */
 	private Status doDestroyGroup(String groupName) {
 		amIAdmin = false;
 		lastJoinedNetwork = null;
 		messageEncrypter.reset();
 		return mGroupManager.destroyGroup(groupName);
 	}
 
 	/**
 	 * Join an existing group
 	 * @param groupName name of the group
 	 * @return status of join
 	 */
 	private Status doJoinGroup(String groupName, String groupPassword, String saltShortDigest) {
 		amIAdmin = false;
 		messageEncrypter.reset();
 		messageEncrypter.setPassword(groupPassword);
 		messageEncrypter.setSaltShortDigest(saltShortDigest);
 		Log.d(TAG, "Joining group...");
 		Status status = mGroupManager.joinGroup(groupName);
 
 		if(status == Status.OK){
 			String myName = userDetails.getString("identification", "");
 			identityMap.put(this.getIdentification(), new Identity(myName, messageAuthenticater.getMyPublicKey()));
 
 			lastJoinedNetwork = groupName;
 			connected = true;
 			
 			if(messageEncrypter.isReady())
 				sendMyIdentity();
 		}
 		return status;
 	}
 
 	/**
 	 * Leave a group
 	 * @param groupName name of the group
 	 * @return status
 	 */
 	private Status doLeaveGroup(String groupName) {
 		return mGroupManager.leaveGroup(groupName);
 	}
 
 	/**
 	 * Unlock a group
 	 * @param groupName name of the group
 	 * @return status
 	 */
 	private Status doUnlockGroup(String groupName) {
 		return mGroupManager.unlockGroup(groupName);
 	}
 
 	/**
 	 * Lock a group
 	 * @param groupName
 	 * @return status
 	 */
 	private Status doLockGroup(String groupName) {
 		return mGroupManager.lockGroup(groupName);
 	}
 
 	/**
 	 * Set the port on which the message exchange should be done
 	 * @param sessionPort
 	 */
 	private void doSetPort(short sessionPort){
 		mGroupManager.setSessionPort(sessionPort);
 	}
 
 	/**
 	 * Create or join a group depending on if it already exists or not
 	 * @param groupName  name of the group
 	 * @return status
 	 */
 	private Status doJoinOrCreate(String groupName){
 		return mGroupManager.joinOrCreateGroup(groupName).getStatus();
 	}
 
 	/******************************************************************************
 	 * 
 	 * Network informations
 	 *
 	 ******************************************************************************/
 
 	/**
 	 * Get my peer unique id
 	 * @return my peer unique id
 	 */
 	public String getIdentification(){
 		return mGroupManager.getMyPeerId();
 	}
 
 	/**
 	 * Get all participants of the given group
 	 * @param groupName the name of the group of which we want to get the participants
 	 * @return list of participant unique id
 	 */
 	public ArrayList<String> getParticipants(String groupName) {
 		return mGroupManager.getPeers(groupName);
 	}
 
 	/**
 	 * Get the well-known name of the given peer
 	 * @param peerId the peer unique identifier
 	 * @return the well-known name of the given peer
 	 */
 	public String getPeerWellKnownName(String peerId){
 
 		if(!identityMap.containsKey(peerId)){
 			return null;
 		} else {
 			return identityMap.get(peerId).getName();
 		}
 	}
 
 	/**
 	 * List the existing group on the network
 	 * @return a list of name of the existing groups
 	 */
 	public ArrayList<String> listGroups() {
 		return mGroupManager.listFoundGroups();
 	}
 	
 	/**
 	 * Get the three first letters of the digest (Base64 encoded) of the generated salt
 	 * @return the three first letters of the digest of the generated salt
 	 */
 	public String getSaltShortDigest(){
 		return this.messageEncrypter.getSaltShortDigest(messageEncrypter.getSalt());
 	}
 
 	/******************************************************************************
 	 * 
 	 * Message processing
 	 *
 	 ******************************************************************************/
 
 	/**
 	 * Send a message to the given group
 	 * @param groupName group to send the message to
 	 * @param message message to send
 	 * @param encrypted indicate if message must be encrypted or not 
 	 * @param type type of the content in the message
 	 */
 	private void doPing(String groupName, String message, boolean encrypted, Type type) {
 
 		//if messageEncrypter is not ready or group joining is not terminated, we enqueue the message
 		if((!messageEncrypter.isReady() && encrypted) || !connected){
 			Message msg = this.obtainMessage(BusHandler.PING);
 			Bundle data = new Bundle();
 			data.putString("groupName", groupName);
 			data.putString("pingString", message);
 			data.putBoolean("encrypted", encrypted);
 			data.putSerializable("type", type);
 			msg.setData(data);
 			this.sendMessage(msg);
 			Log.d(TAG, "Queueing message to send "+message);
 			return;
 		}
 
 		//Create the message object with the received parameter
 		AllJoynMessage messageObject = new AllJoynMessage(this.messageEncrypter, this.messageAuthenticater);
 		if(type==null) type = Type.NORMAL;
 		messageObject.setType(type);
 		messageObject.setSender(this.getIdentification());
 		boolean messageEncrypted = messageObject.setMessage(message, encrypted);
 		if(!messageEncrypted){
 			Log.e(TAG, "Message encryption failed");
 			return;
 		}
 		boolean messageSigned = messageObject.signMessage();
 		if(!messageSigned){
 			Log.e(TAG, "Signature failed");
 			return;
 		}
 		
 		//Serialize the message
 		messageObject.setMessageAuthenticater(null);
 		messageObject.setMessageEncrypter(null);
 		String toSend = su.serialize(messageObject);
 				
 		try {
 			Log.d(TAG, "sending message of size "+toSend.getBytes("UTF-8").length+" bytes. Maximum allowed by AllJoyn is 128Kb.");
 		} catch (UnsupportedEncodingException e1) {
 			e1.printStackTrace();
 		}
 		
 		//Send the message
 		SimpleInterface simpleInterface = mGroupManager.getSignalInterface(groupName, mSimpleService, SimpleInterface.class);
 		try {
 			if(simpleInterface != null) {
 				simpleInterface.Ping(toSend);
 			}
 		} catch (BusException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * Simple class implementing the AllJoyn interface
 	 */
 	class SimpleService implements SimpleInterface, BusObject {
 		public void Ping(String Str) {}        
 	}
 
 	/**
 	 * Signal Handler for the Ping signal
	 * This method receives the message from the other peers
 	 */
 	@BusSignalHandler(iface = "org.alljoyn.bus.samples.simple.SimpleInterface", signal = "Ping")
 	public void Ping(String str) {
 		
 		if(str==null) return;
 		
 		//Deserialize the message
 		AllJoynMessage message = (AllJoynMessage)su.deserialize(str, AllJoynMessage.class);
 		message.setMessageAuthenticater(this.messageAuthenticater);
 		message.setMessageEncrypter(this.messageEncrypter);
 		
 		if(connected){
 			String sender = mGroupManager.getSenderPeerId();
 			//we ask alljoin for the sender id and compare it with the sender found in the message
 			//if it is different, someone is trying to send a message on behalf of another peer
 			if(!message.getSender().equals(sender)){
 				Intent intent = new Intent("attackDetected");
 				intent.putExtra("type", 2);
 				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 				Log.e(TAG,"The name of the sender of the message indicated by AllJoyn is "+ sender + " but the sender indicated in the message is "+message.getSender()+"!!");	
 				return;
 			}
 		}
 		
 		processMessage(message);
 	}
 
 	/**
 	 * Method processing the message received
 	 * @param messageObject object containing the message transmitted over the newtork
 	 */
 	private void processMessage(AllJoynMessage messageObject){
 
 		/*
 		 * First we check if decrypter is ready. If not, only Salt message can go further
 		 */
 		//if messageEncrypter isn't ready to decrypt and message is encrypted
 		if(!messageEncrypter.isReady() && messageObject.isEncrypted() /*&& !message.startsWith(MESSAGE_PREFIX_SALT)*/){
 			//requeue the message in order to process it later
 			Message msg = this.obtainMessage(BusHandler.REPROCESS_MESSAGE);
 			Bundle data = new Bundle();
 			data.putSerializable("message", messageObject);
 			msg.setData(data);
 			this.sendMessage(msg);
 			Log.d(TAG, "Requeueing message received");
 			return;
 		}
 
 
 		/*
 		 * Second, we check if the message contains the salt and extract it
 		 */
 		//Check if the message contains the salt
 		if(messageObject.getType().equals(Type.SALT)){
 			if(messageEncrypter.getSalt()==null){
 				this.saltReceived(messageObject);
 				return;
 			}
 		}
 		
 		
 		/*
 		 * Third, we check if the message contains an identity
 		 */
 		//Check if message contain an identity
 		if(messageObject.getType().equals(Type.IDENTITY)){
 			extractIdentity(messageObject);
 			return;
 		}
 
 		/*
 		 * Fourth, we verify the signature, if we know the sender, otherwise we set a flag
 		 */
 		if(identityMap.containsKey(messageObject.getSender())){
 			boolean result = messageObject.verifyMessage(identityMap.get(messageObject.getSender()).getPublicKey());//messageAuthenticater.verifySignature(identityMap.get(sender).getPublicKey(), Base64.decode(signature,Base64.DEFAULT), Base64.decode(message,Base64.DEFAULT));
 			if(!result){
 				//signature verification failed
 				//ignoring message
 				Log.e(TAG,"Wrong signature");
 				return;
 			} else {
 				Log.d(TAG,"Signature correct");
 			}
 		} else {
 			//message not containing a salt nor an identity coming from an unknow person => ignore 
 			return;
 		}
 
 		/*
 		 * Fifth, we decrypt the message
 		 */
 		String decryptedString = messageObject.getMessage();//messageEncrypter.decrypt(Base64.decode(message.getBytes(), Base64.DEFAULT));
 		if(decryptedString==null || decryptedString.equals("")){
 			//decryption failed
 			Log.d(TAG, "Message decryption failed");
 			return;
 		}
 
 		/*
 		 * Sixth, we transmit message to the application
 		 */
 		//Send the message to the app
 		Intent i = new Intent("messageArrived");
 		i.putExtra("senderId", messageObject.getSender());
 		i.putExtra("senderName", identityMap.get(messageObject.getSender()).getName());
 		i.putExtra("message", decryptedString);
 		LocalBroadcastManager.getInstance(context).sendBroadcast(i);
 	}
 
 	/******************************************************************************
 	 * 
 	 * Helper methods
 	 *
 	 ******************************************************************************/
 
 
 	/**
 	 * Helper method extracting an identity from a received message
 	 * @param messageObject the original message received
 	 */
 	private void extractIdentity(AllJoynMessage messageObject) {
 		Log.d(TAG, "Exctracting identity "+ messageObject.getMessage());
 		
 		//Get the name and its corresponding key key
 		StringTokenizer tokenizer = new StringTokenizer(messageObject.getMessage(), MESSAGE_PARTS_SEPARATOR);
 		if(tokenizer.countTokens()!=2){
 			//malformed string
 			Log.d(TAG,"String was not composed of 2 parts");
 			return;
 		}
 		
 		String peerId = messageObject.getSender();
 		//then peerName
 		String peerName = (String)tokenizer.nextElement();
 		//and finally peerKey
 		String peerKey = (String)tokenizer.nextElement();
 
 		Identity newIdentity = new Identity(peerName, messageAuthenticater.decodePublicKey(peerKey));
 
 		//Check if identity is already known
 		if(identityMap.containsKey(peerId)){
 			//if yes, check if the same identity as received before
 			if(!identityMap.get(peerId).equals(newIdentity)){
 				//If not someone is trying to impersonate somebody else
 				Intent intent = new Intent("attackDetected");
 				intent.putExtra("type", 1);
 				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 				Log.e(TAG,"Two different data received for peer "+ peerId);	
 				return;
 			}
 		} else {
 			boolean verification = messageObject.verifyMessage(newIdentity.getPublicKey());
 			if(verification){
 				//Save the new identity
 				Log.d(TAG,"identity received "+newIdentity.getName());
 				identityMap.put(peerId, newIdentity);
 				this.sendMyIdentity();
 				//Update the UI
 				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("participantStateUpdate"));
 
 				if(peerId.equals(signatureVerificationTask.getSender())){
 					verifySignatureSalt();
 				}
 			} else {
 				Log.e(TAG,"Wrong signature for identiy message from "+peerId);
 				return;
 			}
 		}
 	}
 
 
 	/**
 	 * Helper method extracting the salt from the recived message
 	 * @param message original message containing the salt
 	 */
 	private void saltReceived(AllJoynMessage message){
 		
 		Log.d(TAG, "Exctracting salt "+ message.getMessage());
 
 		if(messageEncrypter.getSalt()==null){
 			messageEncrypter.setSalt(message.getMessage());
 		} else{
 			if(!messageEncrypter.getSalt().equals(message.getMessage())){
 				//someone is sending a false salt
 				Intent intent = new Intent("attackDetected");
 				intent.putExtra("type", 3);
 				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
 				Log.e(TAG,"Different salts have been received!");
 				return;
 			}
 		}
 		
 		
 		//Add to verification task in order to check the signature later
 		signatureVerificationTask = message;
 
 		//send my identity
 		if(connected){
 			this.sendMyIdentity();
 		}
 	}
 
 	/**
 	 * Helper method send my identity to the other peers
 	 */
 	private void sendMyIdentity(){
 		Log.d(TAG, "Sending my identity");
 
 		String myName = userDetails.getString("identification", "");
 
 		byte[] publicKeyBytes = messageAuthenticater.getMyPublicKey().getEncoded();
 		if(publicKeyBytes==null){
 			Log.e(TAG, "Key encoding not supported");
 		}
 		String myKey = Base64.encodeToString(publicKeyBytes, Base64.DEFAULT);
 		String identity = myName+MESSAGE_PARTS_SEPARATOR+myKey;
 		Log.d(TAG,"Send my identity");
 
 		Message msg = obtainMessage(BusHandler.PING);
 		Bundle data = new Bundle();
 		data.putString("groupName", lastJoinedNetwork);
 		data.putString("pingString", identity);
 		data.putSerializable("type", Type.IDENTITY);
 		msg.setData(data);
 		sendMessage(msg);
 	}
 
 	/**
 	 * Helper method called when we received the identity of the sender of the salt message
 	 * in order to check if the signature was correct
 	 */
 	private void verifySignatureSalt(){
 		AllJoynMessage message =  signatureVerificationTask;
 		
 		boolean verification = message.verifyMessage(identityMap.get(message.getSender()).getPublicKey());//messageAuthenticater.verifySignature(identityMap.get(message.getSender()).getPublicKey(), Base64.decode(message.getSignature(),Base64.DEFAULT), message.getMessage().getBytes());
 		if(verification){
 			//everything OK
 			Log.d(TAG, "Signature of earlyier received message was OK");
 			return;
 		} else {
 			//when verification failed
 			//signature of salt message was false
 			//=> reinit salt
 			messageEncrypter.reset();
 			Log.e(TAG, "Signature of salt message was incorrect");
 		}
 	}
 
 
 }
