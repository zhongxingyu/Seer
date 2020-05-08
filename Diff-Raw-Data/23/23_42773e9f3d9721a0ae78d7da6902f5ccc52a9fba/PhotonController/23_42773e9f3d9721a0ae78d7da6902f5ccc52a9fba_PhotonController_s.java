 package com.slobodastudio.discussions.photon;
 
 import com.slobodastudio.discussions.ApplicationConstants;
 import com.slobodastudio.discussions.data.PreferenceHelper;
 import com.slobodastudio.discussions.data.model.ArgPointChanged;
 import com.slobodastudio.discussions.data.model.SelectedPoint;
 import com.slobodastudio.discussions.photon.constants.ActorPropertiesKey;
 import com.slobodastudio.discussions.photon.constants.DeviceType;
 import com.slobodastudio.discussions.photon.constants.DiscussionEventCode;
 import com.slobodastudio.discussions.photon.constants.DiscussionOperationCode;
 import com.slobodastudio.discussions.photon.constants.DiscussionParameterKey;
 import com.slobodastudio.discussions.photon.constants.LiteLobbyOpKey;
 import com.slobodastudio.discussions.photon.constants.LiteOpParameterKey;
 import com.slobodastudio.discussions.photon.constants.LiteOpPropertyType;
 import com.slobodastudio.discussions.photon.constants.PhotonConstants;
 import com.slobodastudio.discussions.utils.MyLog;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.ResultReceiver;
 import android.util.Log;
 
 import de.exitgames.client.photon.EventData;
 import de.exitgames.client.photon.IPhotonPeerListener;
 import de.exitgames.client.photon.LitePeer;
 import de.exitgames.client.photon.OperationResponse;
 import de.exitgames.client.photon.StatusCode;
 import de.exitgames.client.photon.TypedHashMap;
 import de.exitgames.client.photon.enums.DebugLevel;
 import de.exitgames.client.photon.enums.LiteEventKey;
 import de.exitgames.client.photon.enums.LiteOpCode;
 import de.exitgames.client.photon.enums.LiteOpKey;
 import de.exitgames.client.photon.enums.PeerStateValue;
 
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class PhotonController implements IPhotonPeerListener {
 
 	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
 	private static final String TAG = PhotonController.class.getSimpleName();
 	private final PhotonServiceCallbackHandler mCallbackHandler;
 	private String mGameLobbyName;
 	private DiscussionUser mLocalUser;
 	private final Hashtable<Integer, DiscussionUser> mOnlineUsers;
 	private LitePeer mPeer;
 	private final SyncResultReceiver mSyncResultReceiver;
 	private Timer mTimer;
 
 	public PhotonController() {
 
 		super();
 		// default values
 		mSyncResultReceiver = new SyncResultReceiver(new Handler());
 		mCallbackHandler = new PhotonServiceCallbackHandler();
 		// TODO: make as a set this list
 		mOnlineUsers = new Hashtable<Integer, DiscussionUser>();
 	}
 
 	private static Integer[] toIntArray(final List<Integer> integerList) {
 
 		Integer[] intArray = new Integer[integerList.size()];
 		for (int i = 0; i < integerList.size(); i++) {
 			intArray[i] = integerList.get(i);
 		}
 		return intArray;
 	}
 
 	public void connect(final Context context, final int discussionId, final String dbSrvAddr,
 			final String UsrName, final int usrDbId) {
 
 		mGameLobbyName = dbSrvAddr + "discussion#" + discussionId;
 		mLocalUser = new DiscussionUser();
 		mLocalUser.setUserName(UsrName);
 		mLocalUser.setUserId(usrDbId);
 		mPeer = new LitePeer(this, PhotonConstants.USE_TCP);
 		mPeer.setSentCountAllowance(5);
 		Log.d(TAG, "[connect] url: " + PreferenceHelper.getPhotonUrl(context));
 		if (!mPeer.connect(PreferenceHelper.getPhotonUrl(context), PhotonConstants.APPLICATION_NAME)) {
 			throw new IllegalArgumentException("Can't connect to the server. Server address: "
 					+ PreferenceHelper.getPhotonUrl(context) + " ; Application name: "
 					+ PhotonConstants.APPLICATION_NAME);
 		}
 		startPeerUpdateTimer();
 	}
 
 	@Override
 	public void debugReturn(final DebugLevel level, final String message) {
 
 		if (DebugLevel.ERROR.equals(level)) {
 			Log.e(TAG, message);
 			mCallbackHandler.onErrorOccured(message);
 		} else if (DebugLevel.WARNING.equals(level)) {
 			Log.w(TAG, message);
 		} else {
 			if (DEBUG) {
 				Log.v(TAG, level.name() + " : " + message);
 			}
 		}
 	}
 
 	public void disconnect() {
 
 		if (isConnected()) {
 			if (mTimer == null) {
 				throw new IllegalStateException("Timer was null at the disconnect point");
 			}
 			if (mPeer == null) {
 				throw new IllegalStateException("Peer was null at the disconnect point");
 			}
 			mPeer.opCustom(DiscussionOperationCode.NOTIFY_LEAVE_USER, null, true);
 			// run this method off the ui thread
 			mPeer.opLeave();
 		}
 	}
 
 	public PhotonServiceCallbackHandler getCallbackHandler() {
 
 		return mCallbackHandler;
 	}
 
 	public ResultReceiver getResultReceiver() {
 
 		return mSyncResultReceiver;
 	}
 
 	public boolean isConnected() {
 
 		return (mPeer != null) && (mPeer.getPeerState() == PeerStateValue.Connected);
 	}
 
 	@Override
 	public void onEvent(final EventData event) {
 
 		// most events will contain the actorNumber of the player who sent the event, so check if the event
 		// origin is known
 		if (DEBUG) {
 			Log.d(TAG, "[onEvent] " + DiscussionEventCode.asString(event.Code) + ", parameters: "
 					+ event.Parameters.values().toString());
 		}
 		switch (event.Code.byteValue()) {
 			case DiscussionEventCode.JOIN:
 				// Event is defined by Lite. A peer entered the room. It could be this peer!
 				// This event provides the current list of actors and a actorNumber of the player who is new.
 				// get the list of current players and check it against local list - create any that's not yet
 				// there
 				Integer[] actorsInGame = (Integer[]) event.Parameters.get(LiteEventKey.ActorList);
 				ArrayList<Integer> unknownActors = new ArrayList<Integer>();
 				for (Integer i : actorsInGame) {
 					if (i.intValue() != mLocalUser.getActorNumber()) {
 						if (!mOnlineUsers.containsKey(i)) {
 							unknownActors.add(i);
 						}
 					}
 				}
 				if (unknownActors.size() > 0) {
 					opRequestActorsInfo(unknownActors);
 				}
 				break;
 			case DiscussionEventCode.LEAVE:
 				// Event is defined by Lite. Someone left the room.
 				Integer leftActorNumber = (Integer) event.Parameters.get(LiteEventKey.ActorNr);
 				mCallbackHandler.onEventLeave(mOnlineUsers.get(leftActorNumber));
 				mOnlineUsers.remove(leftActorNumber);
 				logUsersOnline();
 				break;
 			case DiscussionEventCode.ARG_POINT_CHANGED:
 				onArgPointChangedEvent(event.Parameters);
 				break;
 			default:
 				Log.e(TAG, "[onEvent] unsupported event: "
 						+ DiscussionEventCode.asString(event.Code.byteValue()));
 		}
 	}
 
 	@Override
 	public void onOperationResponse(final OperationResponse operationResponse) {
 
 		byte opCode = operationResponse.OperationCode;
 		short returnCode = operationResponse.ReturnCode;
 		if (DEBUG) {
 			Log.d(TAG, "[onOperationResponse] " + DiscussionOperationCode.asString(opCode)
 					+ ", return code: " + returnCode + ", parameters: "
 					+ operationResponse.Parameters.values().toString());
 		}
 		if (returnCode != (short) 0) {
 			debugReturn(DebugLevel.INFO, "[onOperationResponse] " + opCode + "/" + returnCode
 					+ ", error message: " + operationResponse.DebugMessage);
 			return;
 		}
 		switch (opCode) {
 			case DiscussionOperationCode.TEST:
 				// ignore it, just for tests
 				break;
 			case DiscussionOperationCode.JOIN:
 				if (operationResponse.Parameters.containsKey(LiteOpKey.ActorNr)) {
 					mLocalUser.setActorNumber(((Integer) operationResponse.Parameters.get(LiteOpKey.ActorNr))
 							.intValue());
 				} else {
 					throw new IllegalStateException(
 							"Expected an actor number here to update local user number");
 				}
 				if (operationResponse.Parameters.containsKey(LiteOpKey.ActorProperties)) {
 					HashMap<Integer, Object> resp = (HashMap<Integer, Object>) operationResponse.Parameters
 							.get(LiteOpKey.ActorProperties);
 					updateOnlineUsers(resp);
 				} else {
 					// no users online, we are first
 					// throw new IllegalStateException(
 					// "Expected an actors list with properties here to update online users");
 				}
 				logUsersOnline();
 				mCallbackHandler.onConnect();
 				break;
 			case DiscussionOperationCode.LEAVE:
 				mPeer.disconnect();
 				mOnlineUsers.clear();
 				mLocalUser = null;
 				break;
 			case DiscussionOperationCode.GET_PROPERTIES:
 				HashMap<Integer, Object> resp = (HashMap<Integer, Object>) operationResponse.Parameters
 						.get(LiteOpKey.ActorProperties);
 				updateOnlineUsers(resp);
 				logUsersOnline();
 				break;
 			default:
 				Log.e(TAG, "[onEvent] unsupported operation: " + DiscussionOperationCode.asString(opCode));
 		}
 	}
 
 	@Override
 	public void onStatusChanged(final StatusCode statusCode) {
 
 		switch (statusCode) {
 			case Connect:
 				debugReturn(DebugLevel.INFO, "peerStatusCallback(): " + statusCode.name() + ", peer.state: "
 						+ mPeer.getPeerState());
 				opJoinFromLobby();
 				break;
 			case Disconnect:
 				debugReturn(DebugLevel.INFO, "peerStatusCallback(): " + statusCode.name() + ", peer.state: "
 						+ mPeer.getPeerState());
 				mLocalUser = null;
 				mTimer.cancel();
 				break;
 			case DisconnectByServer:
 			case DisconnectByServerLogic:
 			case DisconnectByServerUserLimit:
 			case EncryptionEstablished:
 			case EncryptionFailedToEstablish:
 			case Exception:
 			case InternalReceiveException:
 			case QueueIncomingReliableWarning:
 			case QueueIncomingUnreliableWarning:
 			case QueueOutgoingAcksWarning:
 			case QueueOutgoingReliableError:
 			case QueueOutgoingReliableWarning:
 			case QueueOutgoingUnreliableWarning:
 			case QueueSentWarning:
 			case SendError:
 			case TimeoutDisconnect:
 				debugReturn(DebugLevel.ERROR, "peerStatusCallback(): " + statusCode.name() + ", peer.state: "
 						+ mPeer.getPeerState());
 				break;
 			default:
 				throw new IllegalArgumentException("Unknown status code: " + statusCode.name());
 		}
 	}
 
 	private void logUsersOnline() {
 
 		if (DEBUG) {
 			Log.d(TAG, "Users online: " + (mOnlineUsers.size() + 1));
 			Log.d(TAG, "Local user name: " + mLocalUser.getUserName() + " user id: " + mLocalUser.getUserId()
 					+ " actor num: " + mLocalUser.getActorNumber());
 			for (DiscussionUser user : mOnlineUsers.values()) {
 				Log.d(TAG, "Online user name: " + user.getUserName() + " user id: " + user.getUserId()
 						+ " actor num: " + user.getActorNumber());
 			}
 		}
 	}
 
 	private void onArgPointChangedEvent(final TypedHashMap<Byte, Object> parameters) {
 
 		if (DEBUG) {
 			Log.d(TAG, "[onArgPointChangedEvent] point id: "
 					+ parameters.get(DiscussionParameterKey.ARG_POINT_ID) + " , topic id: "
 					+ parameters.get(DiscussionParameterKey.CHANGED_TOPIC_ID) + " , event type: "
 					+ parameters.get(DiscussionParameterKey.POINT_CHANGE_TYPE));
 		}
 		int pointId = (Integer) parameters.get(DiscussionParameterKey.ARG_POINT_ID);
 		int type = (Integer) parameters.get(DiscussionParameterKey.POINT_CHANGE_TYPE);
 		int topicId = (Integer) parameters.get(DiscussionParameterKey.CHANGED_TOPIC_ID);
 		ArgPointChanged argPointChanged = new ArgPointChanged();
 		argPointChanged.setEventType(type);
 		argPointChanged.setPointId(pointId);
 		argPointChanged.setTopicId(topicId);
 		mCallbackHandler.onArgPointChanged(argPointChanged);
 	}
 
 	private void opJoinFromLobby() {
 
 		HashMap<Byte, Object> actorProperties = new HashMap<Byte, Object>();
 		actorProperties.put(ActorPropertiesKey.NAME, mLocalUser.getUserName());
 		actorProperties.put(ActorPropertiesKey.DB_ID, mLocalUser.getUserId());
 		actorProperties.put(ActorPropertiesKey.DEVICE_TYPE, DeviceType.ANDROID);
 		opJoinFromLobby(mGameLobbyName, PhotonConstants.LOBBY, actorProperties, true);
 	}
 
 	private boolean opJoinFromLobby(final String gameName, final String lobbyName,
 			final HashMap<Byte, Object> actorProperties, final boolean broadcastActorProperties) {
 
 		if (!isConnected()) {
 			throw new IllegalStateException("Cant perfom operation \"opJoinFromLobby\" in disconnected state");
 		}
 		if (actorProperties == null) {
 			throw new IllegalArgumentException(
 					"Actor properties was null and required for operation parameters");
 		}
 		TypedHashMap<Byte, Object> joinParameters = new TypedHashMap<Byte, Object>(Byte.class, Object.class);
 		joinParameters.put(LiteLobbyOpKey.RoomName, gameName);
 		joinParameters.put(LiteLobbyOpKey.LobbyName, lobbyName);
 		joinParameters.put(LiteOpKey.ActorProperties, actorProperties);
 		joinParameters.put(LiteOpKey.Broadcast, broadcastActorProperties);
 		return mPeer.opCustom(LiteOpCode.Join, joinParameters, true);
 	}
 
 	private boolean opRequestActorsInfo(final List<Integer> unknownActorsNumbers) {
 
 		if (!isConnected()) {
 			throw new IllegalStateException("Cant perfom [opRequestActorsInfo] in disconnected state");
 		}
 		if ((unknownActorsNumbers == null) || (unknownActorsNumbers.size() <= 0)) {
 			throw new IllegalArgumentException("Tried to ger actors info without actors numbers");
 		}
 		TypedHashMap<Byte, Object> opRequestParameters = new TypedHashMap<Byte, Object>(Byte.class,
 				Object.class);
 		opRequestParameters.put(LiteOpParameterKey.ACTORS, toIntArray(unknownActorsNumbers));
 		opRequestParameters.put(LiteOpParameterKey.PROPERTIES, Byte.valueOf(LiteOpPropertyType.ACTOR));
 		return mPeer.opCustom(LiteOpCode.GetProperties, opRequestParameters, true);
 	}
 
 	private boolean opSendArgPointChanged(final ArgPointChanged argPointChanged) {
 
 		if (DEBUG) {
 			Log.d(TAG, "[opSendArgPointChanged] point id: " + argPointChanged.getPointId() + " , topic id: "
 					+ argPointChanged.getTopicId() + " , event type: " + argPointChanged.getEventType());
 		}
 		if (!isConnected()) {
 			throw new IllegalStateException(
 					"You trying to send notification while not connected to the server");
 		}
 		if (argPointChanged.getPointId() < -1) {
 			throw new IllegalArgumentException("[opSendArgPointChanged] Point id can't be below zero");
 		}
 		if (argPointChanged.getTopicId() < 0) {
 			throw new IllegalArgumentException("[opSendArgPointChanged] Topic id can't be below zero");
 		}
 		TypedHashMap<Byte, Object> structureChangedParameters = new TypedHashMap<Byte, Object>(Byte.class,
 				Object.class);
		structureChangedParameters.put(DiscussionParameterKey.POINT_CHANGE_TYPE, argPointChanged
 				.getEventType());
 		structureChangedParameters.put(DiscussionParameterKey.ARG_POINT_ID, argPointChanged.getPointId());
 		structureChangedParameters.put(DiscussionParameterKey.CHANGED_TOPIC_ID, argPointChanged.getTopicId());
 		return mPeer.opCustom(DiscussionOperationCode.NOTIFY_ARGPOINT_CHANGED, structureChangedParameters,
 				true);
 	}
 
 	private boolean opSendStatsEvent(final int statsEvent, final SelectedPoint selectedPoint) {
 
 		if (!isConnected()) {
 			throw new IllegalStateException(
 					"Cant perfom operation \"opSendStatsEvent\" in disconnected state");
 		}
 		if (DEBUG) {
 			Log.d(TAG, "[opSendStatsEvent] topic id: " + selectedPoint.getTopicId() + ", userId: "
 					+ selectedPoint.getPersonId() + ", discussionId: " + selectedPoint.getDiscussionId());
 		}
 		TypedHashMap<Byte, Object> eventStatsParameters = new TypedHashMap<Byte, Object>(Byte.class,
 				Object.class);
 		eventStatsParameters.put(DiscussionParameterKey.DISCUSSION_ID, selectedPoint.getDiscussionId());
 		eventStatsParameters.put(DiscussionParameterKey.USER_ID, selectedPoint.getPersonId());
 		eventStatsParameters.put(DiscussionParameterKey.CHANGED_TOPIC_ID, selectedPoint.getTopicId());
 		eventStatsParameters.put(DiscussionParameterKey.STATS_EVENT, statsEvent);
 		eventStatsParameters.put(DiscussionParameterKey.DEVICE_TYPE, DeviceType.ANDROID);
 		return mPeer.opCustom(DiscussionOperationCode.STATS_EVENT, eventStatsParameters, true);
 	}
 
 	private void startPeerUpdateTimer() {
 
 		// FIXME: stop this timer
 		mTimer = new Timer("main loop");
 		TimerTask timerTask = new TimerTask() {
 
 			long lastDispatchTime = 0xFFFFFFFF;
 			long lastSendTime = 0xFFFFFFFF;
 
 			@Override
 			public void run() {
 
 				if (mPeer == null) {
 					throw new IllegalStateException("Run timer on null peer");
 				}
 				// test if it's time to dispatch all incoming commands to the application. Dispatching
 				// will empty the queue of incoming messages and will fire the related callbacks.
 				if ((System.currentTimeMillis() - lastDispatchTime) > PhotonConstants.DISPATCH_INTERVAL) {
 					lastDispatchTime = System.currentTimeMillis();
 					// dispatch all incoming commands
 					try {
 						while (mPeer.dispatchIncomingCommands()) {
 							// wait until false in dispatchIncomingCommands
 						}
 					} catch (ConcurrentModificationException e) {
 						MyLog.e(TAG, "[peerService] cant dispatch incoming command", e);
 					} catch (NoSuchElementException e) {
 						MyLog.e(TAG, "[peerService] cant dispatch incoming command", e);
 					}
 				}
 				// to spare some overhead, we will send outgoing packets in certain intervals, as defined
 				// in the settings menu.
 				if ((System.currentTimeMillis() - lastSendTime) > PhotonConstants.SEND_INTERVAL) {
 					lastSendTime = System.currentTimeMillis();
 					if (mPeer != null) {
 						mPeer.sendOutgoingCommands();
 					}
 				}
 			}
 		};
 		mTimer.schedule(timerTask, 0, 5);
 	}
 
 	private void updateOnlineUsers(final HashMap<Integer, Object> actorsProperties) {
 
 		Iterator<Entry<Integer, Object>> it = actorsProperties.entrySet().iterator();
 		while (it.hasNext()) {
 			Entry<Integer, Object> pairs = it.next();
 			DiscussionUser newUser = new DiscussionUser();
 			newUser.setActorNumber(pairs.getKey());
 			HashMap<Byte, Object> actorProperties = (HashMap<Byte, Object>) pairs.getValue();
 			newUser.setUserId((Integer) actorProperties.get(Byte.valueOf((byte) 2)));
 			newUser.setUserName((String) actorProperties.get(Byte.valueOf((byte) 1)));
 			mOnlineUsers.put(newUser.getActorNumber(), newUser);
 			it.remove(); // avoids a ConcurrentModificationException
 			mCallbackHandler.onEventJoin(newUser);
 		}
 	}
 
 	public class SyncResultReceiver extends ResultReceiver {
 
 		public static final String EXTRA_ARG_POINT_CHANGED = "intent.extra.key.EXTRA_ARG_POINT_CHANGED";
 		public static final String EXTRA_SELECTED_POINT = "intent.extra.key.EXTRA_SELECTED_POINT";
 		public static final String EXTRA_STATS_EVENT = "intent.extra.key.EXTRA_STATS_EVENT";
 		public static final int STATUS_ARG_POINT_CHANGED = 0x3;
 		public static final int STATUS_EVENT_CHANGED = 0x4;
 
 		public SyncResultReceiver(final Handler handler) {
 
 			super(handler);
 		}
 
 		@Override
 		protected void onReceiveResult(final int resultCode, final Bundle resultData) {
 
 			if (DEBUG) {
 				Log.d(TAG, "[onReceiveResult] code: " + resultCode + ", data: " + resultData.toString());
 			}
 			super.onReceiveResult(resultCode, resultData);
 			switch (resultCode) {
 				case STATUS_ARG_POINT_CHANGED:
 					ArgPointChanged argPointChanged = resultData.getParcelable(EXTRA_ARG_POINT_CHANGED);
 					if (isConnected()) {
 						opSendArgPointChanged(argPointChanged);
 					}
 					break;
 				case STATUS_EVENT_CHANGED:
 					SelectedPoint selectedPoint = resultData.getParcelable(EXTRA_SELECTED_POINT);
 					int statsEvent = resultData.getInt(EXTRA_STATS_EVENT);
 					if (isConnected()) {
 						opSendStatsEvent(statsEvent, selectedPoint);
 					}
 					break;
 				default:
 					throw new IllegalArgumentException("Unknown result code: " + resultCode);
 			}
 		}
 	}
 }
