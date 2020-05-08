 package co.shoutbreak.polling;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import co.shoutbreak.core.C;
 import co.shoutbreak.core.Mediator.ThreadSafeMediator;
 import co.shoutbreak.core.utils.ErrorManager;
 import co.shoutbreak.core.utils.SBLog;
 import co.shoutbreak.polling.http.HttpConnection;
 import co.shoutbreak.polling.http.PostData;
 
 import android.os.Handler;
 import android.os.Message;
 
 public class Polling {
 	
 	private static final String TAG = "Polling";
 	
 	private ThreadSafeMediator _safeM;
     private Handler _uiThreadHandler;
     
     public Polling(ThreadSafeMediator threadSafeMediator, Handler uiThreadHandler) {
     	SBLog.constructor(TAG);
     	_safeM = threadSafeMediator;
     	_uiThreadHandler = uiThreadHandler;
     }
 	
 	public void go(Message message) {
 		SBLog.logic("Polling - go");
 		switch (message.what) {
 			case C.STATE_IDLE: {
 				SBLog.logic("Polling - idle");
 				idle(message);
 				break;
 			}
 			case C.STATE_RECEIVE_SHOUTS: {
 				SBLog.logic("Polling - receiveShouts");
				_safeM.resetPollingDelay();				
 				receiveShouts(message);
 				break;
 			}
 			case C.STATE_VOTE: {
 				SBLog.logic("Polling - vote");
				_safeM.resetPollingDelay();				
 				vote(message);
 				break;
 			}
 			case C.STATE_SHOUT: {
 				SBLog.logic("Polling - shout");
				_safeM.resetPollingDelay();				
 				shout(message);
 				break;
 			}
 			case C.STATE_EXPIRED_AUTH: {
 				SBLog.logic("Polling - expiredAuth");
 				expiredAuth(message);
 				break;
 			}
 			case C.STATE_CREATE_ACCOUNT_2: {
 				SBLog.logic("Polling - createAccountStep2");
 				createAccountStep2(message);
 				break;
 			}
 		}
 	}
     
 	public void idle(Message message) {
 		SBLog.logic("Polling - idle");
 		if (!_safeM.userHasAccount()) {
 			createAccount(message);
 			return;
 		}
 		ping(message);
 		
 	}
 	
 	public void ping(Message message) {
 		SBLog.method(TAG, "ping()");
 		Handler httpHandler = new Handler() {
 			public void handleMessage(Message message) {
 				switch (message.what) {
 					case C.HTTP_DID_SUCCEED: {
 						if (_safeM.isResponseClean(message)) {						
 							CrossThreadPacket xPacket = (CrossThreadPacket)message.obj;
 							try {
 								String code = xPacket.json.getString(C.JSON_CODE);
 													
 								if (code.equals(C.JSON_CODE_PING_OK)) {
 									// if normal ping, introduce delay
 									if (xPacket.purpose == C.PURPOSE_LOOP_FROM_UI) {
 										xPacket.purpose = C.PURPOSE_LOOP_FROM_UI_DELAYED;
 									}
 									_uiThreadHandler.sendMessage(Message.obtain(_uiThreadHandler, C.STATE_IDLE, xPacket));
 								} else if (code.equals(C.JSON_CODE_SHOUTS)) {
 									_uiThreadHandler.sendMessage(Message.obtain(_uiThreadHandler, C.STATE_RECEIVE_SHOUTS, xPacket));
 								} else if (code.equals(C.JSON_CODE_EXPIRED_AUTH)) {
 									_uiThreadHandler.sendMessage(Message.obtain(_uiThreadHandler, C.STATE_EXPIRED_AUTH, xPacket));
 								} else if (code.equals(C.JSON_CODE_INVALID_UID)) {
 									
 								} else {
 									// some invalid response from server, do anything?
 								}
 							} catch (JSONException ex) {
 								// TODO: Manage exception
 								SBLog.error(TAG, ex.getMessage());
 							}
 						}
 						break;
 					}
 					case C.HTTP_DID_ERROR: {
 						_safeM.handlePingFailed(message);
 						break;
 					}
 				}
 			}
 		};
 		CrossThreadPacket xPacket = (CrossThreadPacket)message.obj;
 		ArrayList<String> scoresToRequest = _safeM.getOpenShoutIds();
 		PostData postData = new PostData();
 		postData.add(C.JSON_ACTION, C.JSON_ACTION_USER_PING);
 		postData.add(C.JSON_UID, _safeM.getUserId());
 		postData.add(C.JSON_AUTH, _safeM.getAuth());
 		postData.add(C.JSON_LAT, Double.toString(_safeM.getLatitude()));
 		postData.add(C.JSON_LONG, Double.toString(_safeM.getLongitude()));
 		
 		// do we need to pull a density?
 		if (! _safeM.getCellDensity().isSet) {	
 			postData.add(C.JSON_DENSITY, "1");
 			//Toast.makeText(_context, "Requesting Density: " + tempCellDensity.cellX + " , " + tempCellDensity.cellY, Toast.LENGTH_SHORT).show();
 		}
 		
 		// acknowledge any level up packets
 		if (_safeM.getUserLevelUpOccurred()) {
 			postData.add(C.JSON_LEVEL, Integer.toString(_safeM.getUserLevel()));
 		}
 		
 		if (scoresToRequest.size() > 0) {
 			StringBuilder scoreReq = new StringBuilder("[");
 			int i = 0;
 			for (String reqID : scoresToRequest) {
 				scoreReq.append("\"" + reqID + "\"");
 				if (++i != scoresToRequest.size()) {
 					scoreReq.append(", ");
 				}
 			}
 			scoreReq.append("]");
 			postData.add(C.JSON_SCORES, scoreReq.toString());
 		}
 			
 		new HttpConnection(httpHandler).post(postData, xPacket);	
 	}
 	
 	public void receiveShouts(Message message) {
 		CrossThreadPacket xPacket = (CrossThreadPacket)message.obj;
 		try {
 			if (xPacket.json.has(C.JSON_DENSITY)) {
 				double density = (double) xPacket.json.optDouble(C.JSON_DENSITY);
 				_safeM.handleDensityChange(density);
 			}
 			if (xPacket.json.has(C.JSON_SHOUTS)) {
 				JSONArray shouts = xPacket.json.getJSONArray(C.JSON_SHOUTS);
 				_safeM.handleShoutsReceived(shouts);
 			}
 			if (xPacket.json.has(C.JSON_SCORES)) {
 				JSONArray scores = xPacket.json.getJSONArray(C.JSON_SCORES);
 				_safeM.handleScoresReceived(scores);
 			}
 			if (xPacket.json.has(C.JSON_LEVEL_CHANGE)) {
 				JSONObject levelInfo = xPacket.json.getJSONObject(C.JSON_LEVEL_CHANGE);
 				_safeM.handleLevelUp(levelInfo);
 			}
 			if (xPacket.purpose == C.PURPOSE_LOOP_FROM_UI) {
 				xPacket.purpose = C.PURPOSE_LOOP_FROM_UI_DELAYED;
 			}			
 			_uiThreadHandler.sendMessage(Message.obtain(_uiThreadHandler, C.STATE_IDLE, xPacket));	
 			
 		} catch (JSONException ex) {
 			ErrorManager.manage(ex);
 		}				
 	}
 	
 	public void shout(Message message) {
 		Handler httpHandler = new Handler() {
 			public void handleMessage(Message message) {
 				switch (message.what) {
 					case C.HTTP_DID_SUCCEED: {
 						if (_safeM.isResponseClean(message)) {
 							CrossThreadPacket xPacket = (CrossThreadPacket)message.obj;
 							String code = xPacket.json.optString(C.JSON_CODE);
 							if (code.equals(C.JSON_CODE_EXPIRED_AUTH)) {
 								_safeM.handleShoutFailed(message);
 							} else {
 								_safeM.handleShoutSent();
 								// Unless shout occurs in idle thread, we don't need to sendMessage.
 								//CrossThreadPacket xPacket = (CrossThreadPacket)message.obj;
 								//_uiThreadHandler.sendMessage(Message.obtain(_uiThreadHandler, C.STATE_IDLE, xPacket)); // STATE doesn't matter - going to die
 							}
 						}
 						break;
 					}
 					case C.HTTP_DID_ERROR: {
 						_safeM.handleShoutFailed(message);
 						break;
 					}
 				}
 			}
 		};
 		CrossThreadPacket xPacket = (CrossThreadPacket)message.obj;
 		String shoutText = xPacket.sArgs[0];
 		int shoutPower = xPacket.iArgs[0];
 		PostData postData = new PostData();
 		postData.add(C.JSON_ACTION, C.JSON_ACTION_SHOUT);
 		postData.add(C.JSON_UID, _safeM.getUserId());
 		postData.add(C.JSON_AUTH, _safeM.getAuth());
 		postData.add(C.JSON_LAT, Double.toString(_safeM.getLatitude()));
 		postData.add(C.JSON_LONG, Double.toString(_safeM.getLongitude()));
 		postData.add(C.JSON_SHOUT_TEXT, shoutText);
 		postData.add(C.JSON_SHOUT_POWER, Integer.toString(shoutPower));
 		new HttpConnection(httpHandler).post(postData, xPacket);	
 	}
 	
 	public void vote(Message message) {
 		CrossThreadPacket xPacket = (CrossThreadPacket)message.obj;
 		final String shoutId = xPacket.sArgs[0];
 		final int vote = xPacket.iArgs[0];
 		Handler httpHandler = new Handler() {
 			public void handleMessage(Message message) {
 				switch (message.what) {
 					case C.HTTP_DID_SUCCEED: {
 						if (_safeM.isResponseClean(message)) {
 							CrossThreadPacket xPacket = (CrossThreadPacket)message.obj;
 							try {
 								String code = xPacket.json.getString(C.JSON_CODE);
 								if (code.equals(C.JSON_CODE_VOTE_OK)) {
 									_safeM.handleVoteFinish(shoutId, vote);
 								} else if (code.equals(C.JSON_CODE_VOTE_FAIL)) {
 									// server knows it failed
 									_safeM.handleVoteFailed(message, shoutId, vote);
 								} else {
 									// something horrible happened
 									_safeM.handleVoteFailed(message, shoutId, vote);
 								}
 							} catch (JSONException ex) {
 								SBLog.error(TAG, ex.getMessage());
 								_safeM.handleVoteFailed(message, shoutId, vote);
 							}
 						}
 						break;
 					}
 					case C.HTTP_DID_ERROR: {
 						_safeM.handleVoteFailed(message, shoutId, vote);
 						break;
 					}
 				}				
 			}
 		};
 		PostData postData = new PostData();
 		postData.add(C.JSON_ACTION, C.JSON_ACTION_VOTE);
 		postData.add(C.JSON_UID, _safeM.getUserId());
 		postData.add(C.JSON_AUTH, _safeM.getAuth());
 		postData.add(C.JSON_SHOUT_ID, shoutId);
 		postData.add(C.JSON_VOTE, Integer.toString(vote));
 		new HttpConnection(httpHandler).post(postData, xPacket);	
 	}
 	
 	public void createAccount(Message message) {
 		_safeM.handleCreateAccountStarted();
 		Handler httpHandler = new Handler() {
 			public void handleMessage(Message message) {
 				switch (message.what) {
 					case C.HTTP_DID_SUCCEED: {
 						if (_safeM.isResponseClean(message)) {
 							_uiThreadHandler.sendMessage(Message.obtain(_uiThreadHandler, C.STATE_CREATE_ACCOUNT_2, message.obj));
 						}
 						break;
 					}
 					case C.HTTP_DID_ERROR: {
 						_safeM.handleCreateAccountFailed(message);
 						break;
 					}
 				}
 			}
 		};
 		CrossThreadPacket xPacket = (CrossThreadPacket)message.obj;
 		PostData postData = new PostData();
 		postData.add(C.JSON_ACTION, C.JSON_ACTION_CREATE_ACCOUNT);
 		new HttpConnection(httpHandler).post(postData, xPacket);	
 	}
 	
 	public void createAccountStep2(Message message) {
 		Handler httpHandler = new Handler() {
 			public void handleMessage(Message message) {
 				switch (message.what) {
 					case C.HTTP_DID_SUCCEED: {
 						if (_safeM.isResponseClean(message)) {
 							try {
 								CrossThreadPacket xPacket = (CrossThreadPacket)message.obj;
 								String password = xPacket.json.getString(C.JSON_PW);
 								String uid = xPacket.sArgs[0];
 								_safeM.handleAccountCreated(uid, password);
 								_uiThreadHandler.sendMessage(Message.obtain(_uiThreadHandler, C.STATE_IDLE, xPacket));
 							} catch (JSONException ex) {
 								ErrorManager.manage(ex);
 							}
 						}
 						break;
 					}
 					case C.HTTP_DID_ERROR: {
 						_safeM.handleCreateAccountFailed(message);
 						break;
 					}
 				}
 			}
 		};
 		try {
 			CrossThreadPacket xPacket = (CrossThreadPacket)message.obj;
 			String tempUID = xPacket.json.getString(C.JSON_UID);
 			PostData postData = new PostData();
 			postData.add(C.JSON_ACTION, C.JSON_ACTION_CREATE_ACCOUNT);
 			postData.add(C.JSON_UID, tempUID);
 			postData.add(C.JSON_ANDROID_ID, _safeM.getAndroidId());
 			postData.add(C.JSON_DEVICE_ID, _safeM.getDeviceId());
 			postData.add(C.JSON_PHONE_NUM, _safeM.getPhoneNumber());
 			postData.add(C.JSON_CARRIER_NAME, _safeM.getNetworkOperator());
 			xPacket.sArgs = new String[] { tempUID };
 			new HttpConnection(httpHandler).post(postData, xPacket);
 		} catch (JSONException ex) {
 			ErrorManager.manage(ex);
 		}
 	}
 	
 	public void expiredAuth(Message message) {
 		try {
 			CrossThreadPacket xPacket = (CrossThreadPacket)message.obj;
 			String nonce = xPacket.json.getString(C.JSON_NONCE);
 			_safeM.updateAuth(nonce);
 			ping(message);
 		} catch (JSONException ex) {
 			ErrorManager.manage(ex);
 		}
 	}
 }
