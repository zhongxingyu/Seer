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
 package org.hubiquitus.hapi.phonegap;
 
 import org.apache.cordova.api.Plugin;
 import org.apache.cordova.api.PluginResult;
 import org.hubiquitus.hapi.client.HClient;
 import org.hubiquitus.hapi.client.HCommandDelegate;
 import org.hubiquitus.hapi.client.HMessageDelegate;
 import org.hubiquitus.hapi.client.HStatusDelegate;
 import org.hubiquitus.hapi.hStructures.HCommand;
 import org.hubiquitus.hapi.hStructures.HMessage;
 import org.hubiquitus.hapi.hStructures.HOptions;
 import org.hubiquitus.hapi.hStructures.HResult;
 import org.hubiquitus.hapi.hStructures.HStatus;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 /**
  * @cond internal
  */
 
 public class HClientPhoneGapPlugin extends Plugin implements HStatusDelegate, HMessageDelegate {
 
 	private HClient hclient = null;
 	
 	/**
 	 * Receive actions from phonegap and dispatch them to the corresponding function
 	 */
 	@Override
 	public PluginResult execute(String action, JSONArray data, String callbackid) {
 		//First of all, create hclient instance
 		if(hclient == null)  {
 			hclient = new HClient();
 			hclient.onStatus(this);
 			hclient.onMessage(this);
 		}
 		
 		//do work depending on action
 		if (action.equalsIgnoreCase("connect")) {
 			this.connect(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("disconnect")) {
 			this.disconnect(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("command")) {
 			this.command(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("subscribe")) {
 			this.subscribe(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("unsubscribe")) {
 			this.unsubscribe(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("publish")) {
 			this.publish(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("getlastmessages")) {
 			this.getLastMessages(action, data, callbackid);
 		} else if(action.equalsIgnoreCase("getsubscriptions")) {
 			this.getSubscriptions(action, data, callbackid);
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Bridge to HClient.getSubcriptions
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void getSubscriptions(String action, JSONArray data, String callbackid) {
 		JSONObject jsonObj = null;
 		String jsonCallback = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			
 			final String cmdCallback = jsonCallback;
 			
 			//set the callback
 			HCommandDelegate commandDelegate = new CommandsDelegate(cmdCallback);
 			hclient.getSubscriptions(commandDelegate);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Bridge to HClient.getLastMessages
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void getLastMessages(String action, JSONArray data, String callbackid) {
 		JSONObject jsonObj = null;
 		String chid = null;
 		int nbLastMsg = -1;
 		String jsonCallback = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			
 			try {
 				chid = jsonObj.getString("chid");
 			} catch (Exception e) {
 			}
 			
 			try {
 				nbLastMsg = jsonObj.getInt("nbLastMsg");
 			} catch (Exception e) {
 			}
 			
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			
 			final String cmdCallback = jsonCallback;
 			
 			//set the callback
 			HCommandDelegate commandDelegate = new CommandsDelegate(cmdCallback);
 			
 			if (nbLastMsg < 0) {
 				hclient.getLastMessages(chid, commandDelegate);
 			} else {
 				hclient.getLastMessages(chid, nbLastMsg, commandDelegate);
 			}
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Bridge to HClient unsubscribe
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void unsubscribe(String action, JSONArray data, String callbackid) {
 		JSONObject jsonObj = null;
 		String chid = null;
 		String jsonCallback = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			try {
 				chid = jsonObj.getString("chid");
 			} catch (Exception e) {
 			}
 			
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			
 			final String cmdCallback = jsonCallback;
 			
 			//set the callback
 			HCommandDelegate commandDelegate = new CommandsDelegate(cmdCallback);
 			hclient.unsubscribe(chid, commandDelegate);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 
 	/**
 	 * Bridge to HClient.publish
 	 * Convert json message to hmessage
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void publish(String action, JSONArray data, String callbackid) {
 		JSONObject jsonObj = null;
 		JSONObject jsonMsg = null;
 		String jsonCallback = null;
 		HMessage msg = null;
 		Log.i("DEBUG", "publish");
 		try {
 			jsonObj = data.getJSONObject(0);
			
 			try {
 				jsonMsg = jsonObj.getJSONObject("hmessage");
 			} catch (Exception e) {
 			}
 			
 			msg = new HMessage(jsonMsg);
			
 			
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			
 			final String cmdCallback = jsonCallback;
 			//set the callback
 			HCommandDelegate commandDelegate = new CommandsDelegate(cmdCallback);
 			
 			hclient.publish(msg, commandDelegate);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Bridge to HClient.subscribe
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void subscribe(String action, JSONArray data, String callbackid) {
 		JSONObject jsonObj = null;
 		String chid = null;
 		String jsonCallback = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			
 			try {
 				chid = jsonObj.getString("chid");
 			} catch (Exception e) {
 			}
 			
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			
 			final String cmdCallback = jsonCallback;
 			
 			//set the callback
 			HCommandDelegate commandDelegate = new CommandsDelegate(cmdCallback);
 			
 			hclient.subscribe(chid, commandDelegate);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Bridge to HClient.command
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void command(String action, JSONArray data, String callbackid) {
 		JSONObject jsonObj = null;
 		JSONObject jsonCmd = null;
 		HCommand cmd = null;
 		String jsonCallback = null;
 		try {
 			jsonObj = data.getJSONObject(0);
 			
 			try {
 				jsonCmd = jsonObj.getJSONObject("hcommand");
 			}  catch (Exception e) {
 			}
 			
 			try {
 				jsonCallback = jsonObj.getString("callback");
 			} catch (Exception e) {
 			}
 			
 			final String cmdCallback = jsonCallback;
 			
 			cmd = new HCommand(jsonCmd);
 			
 			//set the callback
 			HCommandDelegate commandDelegate = new CommandsDelegate(cmdCallback);
 			
 			hclient.command(cmd, commandDelegate);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/**
 	 * Bridge to HClient.disconnect
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void disconnect(String action, JSONArray data, String callbackid) {
 		hclient.disconnect();
 	}
 	
 	/**
 	 * bridge to HClient.connect
 	 * @param action
 	 * @param data
 	 * @param callbackid
 	 */
 	public void connect(String action, JSONArray data, String callbackid) {
 		String publisher = null;
 		String password = null;
 		HOptions options = null;
 		try {
 			//get vars
 			JSONObject jsonObj = data.getJSONObject(0); 
 			publisher = jsonObj.getString("publisher");
 			password = jsonObj.getString("password");
 			JSONObject jsonOptions = (JSONObject) jsonObj.get("options");
 			options = new HOptions(jsonOptions);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		//set callback
 		hclient.connect(publisher, password, options);
 		//hclient.connect(publisher, password, this, new HOptions());
 	}
 
 	/**
 	 * Helper fonction, that will call a jsCallback with an argument (model used in hapi);
 	 * @param callback
 	 * @param arg
 	 */
 	private void notifyJsCallback(final String jsCallback, final String arg) {
 		if (jsCallback != null && jsCallback.length() > 0) {
 			
 			//do callback on main thread
 			this.webView.post(new Runnable() {
 
 				public void run() {
 					//send callback through javascript
 					String jsCallbackFct = jsCallback + "(" + arg + ");";
 					sendJavascript(jsCallbackFct);
 				}
 			});	
 		}
 	}
 	
 	@Override
 	public void onStatus(HStatus status) {
 		notifyJsCallback("hClient.onStatus", status.toJSON().toString());
 	}
 
 	@Override
 	public void onMessage(HMessage message) {
 		notifyJsCallback("hClient.onMessage", message.toJSON().toString());		
 	}
 	
 	/**
 	 * Command delegate for all js commands. call the right js callback
 	 *
 	 */
 	private class CommandsDelegate implements HCommandDelegate {
 
 		private String cmdCallback = null;
 		
 		/**
 		 * Init with js callback function
 		 * @param cmdCallback
 		 */
 		public CommandsDelegate(String cmdCallback) {
 			this.cmdCallback = cmdCallback;
 		}
 		
 		@Override
 		public void onResult(HResult result) {
 			notifyJsCallback("var tmpcallback = " + this.cmdCallback + "; tmpcallback", result.toJSON().toString());	
 			
 		}
 		
 	}
 }
 
 /**
  * @endcond
  */
