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
 
 
 package org.hubiquitus.hapi.hStructures;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import org.hubiquitus.hapi.util.DateISO8601;
 import org.hubiquitus.hapi.util.HJsonDictionnary;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * @version 0.3
  * hAPI Command. For more info, see Hubiquitus reference
  */
 
 public class HCommand implements HJsonObj {
 	
 	private JSONObject hcommand = new JSONObject();
 	
 	public HCommand() {
 		setSent(new GregorianCalendar());
 		setTransient(true);
 	}
 	
 	public HCommand(String entity, String cmd, HJsonObj params) {
 		this();
 		setEntity(entity);
 		setCmd(cmd);
 		setParams(params);
 	}
 	
 	public HCommand(JSONObject jsonObj) {
		fromJSON(jsonObj);
 	}
 	
 	/* HJsonObj interface */
 	
 	public JSONObject toJSON() {
 		return this.hcommand;
 	}
 	
 	public void fromJSON(JSONObject jsonObj) {
 		if(jsonObj != null){
 			this.hcommand = jsonObj;
 		} else {
 			this.hcommand = new JSONObject();
 		}
 	}
 	
 	public String getHType() {
 		return "hcommand";
 	}
 	
 	@Override
 	public String toString() {
 		return hcommand.toString();
 	}
 	
 	/**
 	 * Check are made on : reqid, requester, sender, entity, sent, cmd and _transient. 
 	 * @param HCommand 
 	 * @return Boolean
 	 */
 	public boolean equals(HCommand obj) {
 		if(obj.getReqid() != this.getReqid())
 			return false;
 		if(obj.getRequester() != this.getRequester())
 			return false;
 		if(obj.getSender() != this.getSender())
 			return false;
 		if(obj.getEntity() != this.getEntity())
 			return false;
 		if(obj.getSent() != this.getSent())
 			return false;
 		if(obj.getCmd() != this.getCmd())
 			return false;
 		if(obj.getTransient() != this.getTransient())
 			return false;
 		return true;
 	}
 	
 	@Override
 	public int hashCode() {
 		return hcommand.hashCode();
 	}
 	
 	/* Getters & Setters */
 	
 	/**
 	 * Mandatory. Filled by the hApi.
 	 * @return reqid. NULL if undefined
 	 */
 	public String getReqid() {
 		String reqid;
 		try {
 			reqid = hcommand.getString("reqid");
 		} catch (Exception e) {
 			reqid = null;
 		}
 		return reqid;
 	}
 
 	public void setReqid(String reqid) {
 		try {
 			if(reqid == null) {
 				hcommand.remove("reqid");
 			} else {
 				hcommand.put("reqid", reqid);
 			}
 		} catch (JSONException e) {
 		}
 	}
 
 	/**
 	 * Filled by the hApi if empty
 	 * @return requester jid. NULL if undefined 
 	 */
 	public String getRequester() {
 		String requester;
 		try {
 			requester = hcommand.getString("requester");
 		} catch (Exception e) {
 			requester = null;
 		}
 		return requester;
 	}
 
 	public void setRequester(String requester) {
 		try {
 			if(requester == null) {
 				hcommand.remove("requester");
 			} else {
 				hcommand.put("requester", requester);
 			}
 		} catch (JSONException e) {
 		}
 	}
 
 	/**
 	 * Mandatory. Filled by the hApi.
 	 * @return sender jid. NULL if undefined 
 	 */
 	public String getSender() {
 		String sender;
 		try {
 			sender = hcommand.getString("sender");
 		} catch (Exception e) {
 			sender = null;
 		}
 		return sender;
 	}
 
 	public void setSender(String sender) {
 		try {
 			if(sender == null) {
 				hcommand.remove("sender");
 			} else {
 				hcommand.put("sender", sender);
 			}
 		} catch (JSONException e) {
 		}
 	}
 
 	/**
 	 * Mandatory.
 	 * @return entity jid. NULL if undefined 
 	 */
 	public String getEntity() {
 		String entity;
 		try {
 			entity = hcommand.getString("entity");
 		} catch (Exception e) {
 			entity = null;
 		}
 		return entity;
 	}
 	
 	public void setEntity(String entity) {
 		try {
 			if(entity == null) {
 				hcommand.remove("entity");
 			} else {
 				hcommand.put("entity", entity);
 			}
 		} catch (JSONException e) {
 		}
 	}
 
 	/**
 	 * Mandatory. Filled by the hApi if empty
 	 * @return date of submission. NULL if undefined
 	 */
 	public Calendar getSent() {
 		Calendar sent;
 		try {
 			sent = (DateISO8601.toCalendar(hcommand.getString("sent")));
 		} catch (JSONException e) {
 			sent = null;
 		}
 		return sent;
 	}
 
 	public void setSent(Calendar sent) {
 		try {
 			if(sent == null) {
 				hcommand.remove("sent");
 			} else {
 				hcommand.put("sent", DateISO8601.fromCalendar(sent));
 			}
 		} catch (JSONException e) {
 		}
 	}
 	
 	/**
 	 * Mandatory.
 	 * @return command. NULL if undefined
 	 */
 	public String getCmd() {
 		String cmd;
 		try {
 			cmd = hcommand.getString("cmd");
 		} catch (JSONException e) {
 			cmd = null;
 		}
 		return cmd;
 	}
 
 	public void setCmd(String cmd) {
 		try {
 			if(cmd == null) {
 				hcommand.remove("cmd");
 			} else {
 				hcommand.put("cmd", cmd);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @return params throws to the hserver. NULL if undefined
 	 */
 	public HJsonObj getParams() {
 		HJsonObj params;
 		try {
 			params = new HJsonDictionnary(hcommand.getJSONObject("params"));
 		} catch (JSONException e) {
 			params = null;
 		}
 		return params;
 	}
 
 	public void setParams(HJsonObj params) {
 		try {
 			if(params == null) {
 				hcommand.remove("params");
 			} else {
 				hcommand.put("params", params.toJSON());
 			}
 		} catch (JSONException e) {
 		}
 	}
 
 	/**
 	 * @return persist message or not. NULL if undefined
 	 */
 	public Boolean getTransient() {
 		Boolean _transient;
 		try {
 			_transient = hcommand.getBoolean("transient");
 		} catch (JSONException e) {
 			_transient = null;
 		}
 		return _transient;
 	}
 
 	public void setTransient(Boolean _transient) {
 		try {
 			if(_transient == null) {
 				hcommand.remove("transient");
 			} else {
 				hcommand.put("transient", _transient);
 			}
 		} catch (JSONException e) {
 		}
 	}	
 	
 	
 }
