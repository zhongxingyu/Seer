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
import org.hubiquitus.hapi.client.HDelegate;
 import org.hubiquitus.hapi.client.HClient;
 import org.hubiquitus.hapi.hStructures.HCommand;
 import org.hubiquitus.hapi.hStructures.HOptions;
import org.hubiquitus.hapi.structures.HJsonObj;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
public class HClientPhoneGapPlugin extends Plugin implements HDelegate {
 
 	private HClient hclient = null;
 	private String jsHClientCallback = null;
 	
 	@Override
 	public PluginResult execute(String action, JSONArray data, String callbackid) {
 		//First of all, create hclient instance
 		if(hclient == null)  {
 			hclient = new HClient();
 		}
 		
 		//do work depending on action
 		if (action.equals("connect")) {
 			String publisher = null;
 			String password = null;
 			String callback = null;
 			HOptions options = null;
 			try {
 				//get vars
 				JSONObject jsonObj = data.getJSONObject(0); 
 				publisher = jsonObj.getString("publisher");
 				password = jsonObj.getString("password");
 				callback = jsonObj.getString("callback");
 				JSONObject jsonOptions = (JSONObject) jsonObj.get("options");
 				options = new HOptions(jsonOptions);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			//set callback
 			jsHClientCallback = callback;
 			hclient.connect(publisher, password, this, options);
 			//hclient.connect(publisher, password, this, new HOptions());
 		} else if(action.equals("disconnect")) {
 			hclient.disconnect();
 		} else if(action.equals("hcommand")) {
 			JSONObject jsonObj = null;
 			JSONObject jsonCmd = null;
 			HCommand cmd = null;
 			try {
 				jsonObj = data.getJSONObject(0);
 				jsonCmd = (JSONObject)jsonObj.get("hcommand");
 				cmd = new HCommand(jsonCmd);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} 
 			
 			hclient.command(cmd);
 		}
 		
 		return null;
 	}
 
 	@Override
	public void hDelegate(final String type, final HJsonObj data) {
 		//do callback on main thread
 		this.webView.post(new Runnable() {
 			
 			public void run() {
 				//send callback through javascript
 			 	JSONObject jsonCallback = data.toJSON();
 			 	String jsCallbackFct = "var tmpcallback = " + jsHClientCallback + "; tmpcallback({type: \"" + type + "\",data: " + jsonCallback.toString() + "});";
 			 	sendJavascript(jsCallbackFct);
 			}
 		});	
 		
 	}
 
 }
