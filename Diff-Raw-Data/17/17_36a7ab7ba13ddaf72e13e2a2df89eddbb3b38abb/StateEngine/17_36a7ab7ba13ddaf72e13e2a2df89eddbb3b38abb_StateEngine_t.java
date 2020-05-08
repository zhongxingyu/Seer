 package com.shoutbreak.service;
 
 import java.util.ArrayList;
 
 import com.shoutbreak.Vars;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 // this is the code run in ServiceThread
 // all messages to ServiceThread are forwarded to goToState(Message msg)
 public class StateEngine {
 
     private Handler _uiThreadHandler;
 	private User _user;
 	
 	public StateEngine(Handler uiThreadHandler, User user) {
 		_uiThreadHandler = uiThreadHandler;
 		_user = user;
 	}
 
 	public void goToState(Message msg) {
 		MessageObject obj = (MessageObject)msg.obj;
 		switch(msg.what) {
 			
 			case Vars.MESSAGE_IDLE_EXIT: {
 				// re-route based on JSON code
 				String code = "";
 				try {
 					code = obj.json.getString(Vars.JSON_CODE);
 				} catch (JSONException ex) {
 					ErrorManager.manage(ex);
 				}
 				if (code.equals(Vars.JSON_CODE_EXPIRED_AUTH)) {
 					msg.what = Vars.MESSAGE_STATE_EXPIRED_AUTH;
 					goToState(msg);
 				} else if (code.equals(Vars.JSON_CODE_SHOUTS)) {
 					msg.what = Vars.MESSAGE_STATE_RECEIVE_SHOUTS;
 					goToState(msg);				
 				} else if (code.equals(Vars.JSON_CODE_INVALID_UID)) {
 					msg.what = Vars.MESSAGE_STATE_INVALID_UID;
 					goToState(msg);
 				} else if (code.equals(Vars.JSON_CODE_LEVEL_CHANGE)) {
 					msg.what = Vars.MESSAGE_STATE_LEVEL_CHANGE;
 					goToState(msg);
 				}
 				break;
 			}
 			
 			case Vars.MESSAGE_STATE_UI_RECONNECT: {
 				msg.what = Vars.MESSAGE_STATE_INIT;
 				goToState(msg);
 				break;				
 //				if (_user.hasAccount()) {
 //					// do we need to pull a density?
 //					CellDensity tempCellDensity = _user.getCellDensity(_locationTracker);
 //					if (!tempCellDensity.isSet) {	
 //						msg.what = Vars.MESSAGE_STATE_IDLE;
 //						goToState(msg);
 //					} else {
 //						try {
 //							MessageObject obj2 = new MessageObject();
 //							JSONObject fakeJSONObject = new JSONObject();
 //							fakeJSONObject.put(Vars.JSON_DENSITY, tempCellDensity.density);
 //							_uiThreadHandler.sendMessage(Message.obtain(_uiThreadHandler, Vars.CALLBACK_RECEIVE_SHOUTS, obj2));
 //							msg.what = Vars.MESSAGE_STATE_IDLE;
 //							goToState(msg);
 //						} catch (JSONException ex) {
 //							ErrorManager.manage(ex);
 //						}
 //					}
 //				} else {
 //					msg.what = Vars.MESSAGE_STATE_INIT;
 //					goToState(msg);
 //				}
 			}
 			
 			case Vars.MESSAGE_STATE_SHOUT: {
 				// TODO: power from UI
 				String shoutPower = "10";
 				String shoutText = obj.args[0];
 				
 				// this catches the response of the HttpConnection when it finishes
 				Handler tempServiceThreadHandler = new Handler() {
 					public void handleMessage(Message message) {
 						switch (message.what) {
 							case Vars.MESSAGE_HTTP_DID_SUCCEED: {
 								MessageObject messageObject = new MessageObject();
 								messageObject.serviceEventCode = Vars.SEC_SHOUT_SENT;
 								_uiThreadHandler.sendMessage(Message.obtain(_uiThreadHandler, Vars.CALLBACK_SERVICE_EVENT_COMPLETE, messageObject));
 								break;
 							}
 						}
 					}
 				};
 				
 				PostData postData = new PostData();
 				postData.add(Vars.JSON_ACTION, Vars.JSON_ACTION_SHOUT);
 				postData.add(Vars.JSON_UID, _user.getUID());
 				postData.add(Vars.JSON_AUTH, _user.getAuth());
 				postData.add(Vars.JSON_LAT, Double.toString(_user.getLatitude()));
 				postData.add(Vars.JSON_LONG, Double.toString(_user.getLongitude()));
 				postData.add(Vars.JSON_SHOUT_TEXT, shoutText);
 				postData.add(Vars.JSON_SHOUT_POWER, shoutPower);
 				new HttpConnection(tempServiceThreadHandler).post(postData);	
 				break;
 			}
 			
 			case Vars.MESSAGE_STATE_VOTE: {
 				final String shoutID = obj.args[0];
 				final String voteString = obj.args[1];
 				// this catches the response of the HttpConnection when it finishes
 				Handler tempServiceThreadHandler = new Handler() {
 					public void handleMessage(Message message) {
 						switch (message.what) {
 							case Vars.MESSAGE_HTTP_DID_SUCCEED: {
 								int vote = Integer.parseInt(voteString);
 								_user.getInbox().reflectVote(shoutID, vote);
 								MessageObject messageObject = new MessageObject();
 								messageObject.serviceEventCode = Vars.SEC_VOTE_COMPLETED;
 								_uiThreadHandler.sendMessage(Message.obtain(_uiThreadHandler, Vars.CALLBACK_SERVICE_EVENT_COMPLETE, messageObject));	
 								break;
 							}
 						}
 					}
 				};
 				
 				PostData postData = new PostData();
 				postData.add(Vars.JSON_ACTION, Vars.JSON_ACTION_VOTE);
 				postData.add(Vars.JSON_UID, _user.getUID());
 				postData.add(Vars.JSON_AUTH, _user.getAuth());
 				postData.add(Vars.JSON_SHOUT_ID, shoutID);
 				postData.add(Vars.JSON_VOTE, voteString);
 				new HttpConnection(tempServiceThreadHandler).post(postData);	
 				break;
 			}
 			
 			case Vars.MESSAGE_STATE_RECEIVE_SHOUTS: {
 				try {
 					_user.setShoutsJustReceived(0);
 					if (obj.json.has(Vars.JSON_DENSITY)) {
 						float density = (float) obj.json.optDouble(Vars.JSON_DENSITY);
 						_user.saveDensity(density);
 					}
 					if (obj.json.has(Vars.JSON_SHOUTS)) {
 						JSONArray shouts = obj.json.getJSONArray(Vars.JSON_SHOUTS);
 						for (int i = 0; i < shouts.length(); i++) {
 							JSONObject jsonShout = shouts.getJSONObject(i);
 							_user.getInbox().addShout(jsonShout);
 						}
 						_user.setShoutsJustReceived(shouts.length());
 					}
 					if (obj.json.has(Vars.JSON_SCORES)) {
 						JSONArray scores = obj.json.getJSONArray(Vars.JSON_SCORES);
 						for (int i = 0; i < scores.length(); i++) {
 							JSONObject jsonScore = scores.getJSONObject(i);
 							_user.getInbox().updateScore(jsonScore);
 						}				
 					}
 					MessageObject messageObject = new MessageObject();
 					messageObject.serviceEventCode = Vars.SEC_RECEIVE_SHOUTS;
 					_uiThreadHandler.sendMessage(Message.obtain(_uiThreadHandler, Vars.CALLBACK_SERVICE_EVENT_COMPLETE, messageObject));				
 				} catch (JSONException ex) {
 					ErrorManager.manage(ex);
 				}				
 				break;
 			}
 			
 			case Vars.MESSAGE_STATE_INIT: {
 				if (_user.hasAccount()) {
 					msg.what = Vars.MESSAGE_STATE_IDLE;
 					goToState(msg);		
 				} else {
 					msg.what = Vars.MESSAGE_STATE_NEW_USER;
 					goToState(msg);
 				}				
 				break;
 			}
 				
 			case Vars.MESSAGE_STATE_IDLE: {			
 
 				// this catches the response of the HttpConnection when it finishes
 				Handler tempServiceThreadHandler = new Handler() {
 					public void handleMessage(Message message) {
 						switch (message.what) {
 							case Vars.MESSAGE_HTTP_DID_SUCCEED: {
 								MessageObject messageObject = (MessageObject) message.obj;
 								try {
 									String code = messageObject.json.getString(Vars.JSON_CODE);
 									if (code.equals(Vars.JSON_CODE_PING_OK)) {
 										// if normal ping, repeat this run method
 										_uiThreadHandler.sendMessage(Message.obtain(_uiThreadHandler, Vars.MESSAGE_REPOST_IDLE_DELAYED));
 									} else if (code.equals(Vars.JSON_CODE_SHOUTS) ||
 									code.equals(Vars.JSON_CODE_EXPIRED_AUTH) ||
 									code.equals(Vars.JSON_CODE_INVALID_UID) ||
 									code.equals(Vars.JSON_CODE_LEVEL_CHANGE)) {
 										// for anything else we're returning to StateEngine
 										// note we're just forwarding the message so it still has any JSON and whatnot
 										message.what = Vars.MESSAGE_IDLE_EXIT;
 										goToState(message);
 									} else {
 										// some invalid response from server, do anything?
 									}
 								} catch (JSONException ex) {
 									ErrorManager.manage(ex);
 								}
 								break;
 							}
 						}
 					}
 				};
 				
 				ArrayList<String> scoresToRequest = _user.getInbox().getOpenShoutIDs();
 								
 				PostData postData = new PostData();
 				postData.add(Vars.JSON_ACTION, Vars.JSON_ACTION_USER_PING);
 				postData.add(Vars.JSON_UID, _user.getUID());
 				postData.add(Vars.JSON_AUTH, _user.getAuth());
 				postData.add(Vars.JSON_LAT, Double.toString(_user.getLatitude()));
 				postData.add(Vars.JSON_LONG, Double.toString(_user.getLongitude()));
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
 					postData.add(Vars.JSON_SCORES, scoreReq.toString());
 				}
 				
 				// do we need to pull a density?
 				CellDensity tempCellDensity = _user.getCellDensity();
 				if (!tempCellDensity.isSet) {	
 					postData.add(Vars.JSON_DENSITY, "1");
 					//Toast.makeText(_context, "Requesting Density: " + tempCellDensity.cellX + " , " + tempCellDensity.cellY, Toast.LENGTH_SHORT).show();
 				}
 				
 				new HttpConnection(tempServiceThreadHandler).post(postData);
 				
 				break;
 			}
 			
 			case Vars.MESSAGE_STATE_EXPIRED_AUTH: {
 				try {
 					String nonce = obj.json.getString(Vars.JSON_NONCE);
 					_user.updateAuth(nonce);
 					msg.what = Vars.MESSAGE_STATE_IDLE;
 					goToState(msg);
 				} catch (JSONException ex) {
 					ErrorManager.manage(ex);
 				}				
 				break;
 			}
 				
 			case Vars.MESSAGE_STATE_NEW_USER: {
 				
 				// this catches the response of the HttpConnection when it finishes
 				Handler tempServiceThreadHandler = new Handler() {
 					public void handleMessage(Message message) {
 						switch (message.what) {
 							case Vars.MESSAGE_HTTP_DID_SUCCEED: {
 								message.what = Vars.MESSAGE_STATE_NEW_USER_2;
 								goToState(message);							
 								break;
 							}
 						}
 					}
 				};
 				
 				PostData postData = new PostData();
 				postData.add(Vars.JSON_ACTION, Vars.JSON_ACTION_CREATE_ACCOUNT);
 				new HttpConnection(tempServiceThreadHandler).post(postData);
 				break;
 			}
 				
 			case Vars.MESSAGE_STATE_NEW_USER_2: {
 				
 				// this catches the response of the HttpConnection when it finishes
 				Handler tempServiceThreadHandler = new Handler() {
 					public void handleMessage(Message message) {
 						switch (message.what) {
 							case Vars.MESSAGE_HTTP_DID_SUCCEED: {
 								try {
 									MessageObject messageObject = (MessageObject)message.obj;
 									String password = messageObject.json.getString(Vars.JSON_PW);
 									String uid = messageObject.args[0];
 									// lock user when writing to it
 									_user.setUID(uid);
 									_user.setPassword(password);
 									message.what = Vars.MESSAGE_STATE_IDLE;
 									goToState(message);
 								} catch (JSONException ex) {
 									ErrorManager.manage(ex);
 								}
 								break;
 							}
 						}
 					}
 				};
 				
 				try {
 					
 					String tempUID = obj.json.getString(Vars.JSON_UID);
 					PostData postData = new PostData();
 					postData.add(Vars.JSON_ACTION, Vars.JSON_ACTION_CREATE_ACCOUNT);
 					postData.add(Vars.JSON_UID, tempUID);
 					postData.add(Vars.JSON_ANDROID_ID, _user.getAndroidId());
 					postData.add(Vars.JSON_DEVICE_ID, _user.getDeviceId());
 					postData.add(Vars.JSON_PHONE_NUM, _user.getPhoneNumber());
 					postData.add(Vars.JSON_CARRIER_NAME, _user.getNetworkOperator());
 					MessageObject messageObject = new MessageObject();
 					messageObject.args = new String[] { tempUID };
 					new HttpConnection(tempServiceThreadHandler).post(postData, messageObject);
 					
 				} catch (JSONException ex) {
 					ErrorManager.manage(ex);
 				}
 				break;
 			}
 			
 		}
 		return;
 	}
 		
 }
