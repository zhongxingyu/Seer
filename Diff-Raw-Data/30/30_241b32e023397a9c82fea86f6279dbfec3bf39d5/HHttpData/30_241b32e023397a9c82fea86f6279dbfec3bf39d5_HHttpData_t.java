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
 
 
 package org.hubiquitus.hubotsdk.adapters.HHttpAdapter;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.codec.binary.Base64;
 import org.hubiquitus.hapi.hStructures.HJsonObj;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class HHttpData implements HJsonObj {
 	private Map<String, HHttpAttachement> attachments = null;
 
 	private byte[] rawBody = null;
 	private String method = null;
 	private String queryArgs = null;
 	private String queryPath = null;
 	private String serverName = null;
 	private Integer serverPort = null;
 	
 	public HHttpData() {};
 	
 	public HHttpData(JSONObject jsonObj){
 		fromJSON(jsonObj);
 	}
 	
 	/* HJsonObj interface */
 	public JSONObject toJSON() {
 		JSONObject jsonObj = new JSONObject();
 		
		JSONObject jsonAttachments = new JSONObject();
 		for (String key : attachments.keySet()) {
 			try {
				jsonAttachments.put(key, attachments.get(key).toJSON());
 			} catch (JSONException e) {
 			}
 		}
 		
 		try {
			jsonObj.put("attachments", jsonAttachments);
 		} catch (JSONException e) {
 		}
 		
 		if (this.rawBody != null) {
 			try {
 				jsonObj.put("rawBody", Base64.encodeBase64String(this.rawBody));
 			} catch (JSONException e) {
 			}
 		}
 		
 		try {
 			jsonObj.put("method", method);
 		} catch (JSONException e) {
 		}
 		
 		try {
 			jsonObj.put("queryArgs", queryArgs);
 		} catch (JSONException e) {
 		}
 		
 		try {
 			jsonObj.put("queryPath", queryPath);
 		} catch (JSONException e) {
 		}
 		
 		try {
 			jsonObj.put("serverName", serverName);
 		} catch (JSONException e) {
 		}
 		
 		try {
 			jsonObj.put("serverPort", serverPort);
 		} catch (JSONException e) {
 		}
 		
 		return jsonObj;
 	}
 	
 	public void fromJSON(JSONObject jsonObj) {
 		if (jsonObj != null) {
 			try {
 				this.method = jsonObj.getString("method");
 			} catch (JSONException e) {
 			}
 
 			try {
 				this.queryArgs = jsonObj.getString("queryArgs");
 			} catch (JSONException e) {
 			}
 
 			try {
 				this.queryPath = jsonObj.getString("queryPath");
 			} catch (JSONException e) {
 			}
 			
 			try {
 				this.serverName = jsonObj.getString("serverName");
 			} catch (JSONException e) {
 			}
 			
 			try {
 				this.serverPort = jsonObj.getInt("serverPort");
 			} catch (JSONException e) {
 			}
 
 			String encodedRawBody;
 			try {
 				encodedRawBody = jsonObj.getString("rawBody");
 				if (encodedRawBody != null) {
 					this.rawBody = Base64.decodeBase64(encodedRawBody);
 				}
 			} catch (JSONException e) {
 			}
 
 			JSONObject jsonAttachements = null;
 			try {
				jsonAttachements = jsonObj.getJSONObject("attachments");
 			} catch (JSONException e) {
 			}
 			
 			if (jsonAttachements != null) {
 				String[] keys = JSONObject.getNames(jsonAttachements);
				if (keys != null) {
					this.attachments = new HashMap<String, HHttpAttachement>();
					for (int i = 0; i < keys.length; i++) {
						HHttpAttachement hattachement;
						try {
							hattachement = new HHttpAttachement(jsonAttachements.getJSONObject(keys[i]));
							this.attachments.put(keys[i], hattachement);
						} catch (JSONException e) {
						}	
					}
 				}
 			}
 		}
 	}
 	
 	public String getHType() {
 		return "hhttpdata";
 	}
 	
 	/* getters and setters */
 	public Map<String, HHttpAttachement> getAttachments() {
 		return attachments;
 	}
 
 	public void setAttachments(Map<String, HHttpAttachement> attachments) {
 		this.attachments = attachments;
 	}
 
 	public byte[] getRawBody() {
 		return rawBody;
 	}
 
 	public void setRawBody(byte[] rawBody) {
 		this.rawBody = rawBody;
 	}
 
 	public String getMethod() {
 		return method;
 	}
 
 	public void setMethod(String method) {
 		this.method = method;
 	}
 
 	public String getQueryArgs() {
 		return queryArgs;
 	}
 
 	public void setQueryArgs(String queryArgs) {
 		this.queryArgs = queryArgs;
 	}
 
 	public String getQueryPath() {
 		return queryPath;
 	}
 
 	public void setQueryPath(String queryPath) {
 		this.queryPath = queryPath;
 	}
 
 	public String getServerName() {
 		return serverName;
 	}
 
 	public void setServerName(String serverName) {
 		this.serverName = serverName;
 	}
 
 	public Integer getServerPort() {
 		return serverPort;
 	}
 
 	public void setServerPort(Integer serverPort) {
 		this.serverPort = serverPort;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((attachments == null) ? 0 : attachments.hashCode());
 		result = prime * result + ((method == null) ? 0 : method.hashCode());
 		result = prime * result
 				+ ((queryArgs == null) ? 0 : queryArgs.hashCode());
 		result = prime * result
 				+ ((queryPath == null) ? 0 : queryPath.hashCode());
 		result = prime * result + Arrays.hashCode(rawBody);
 		result = prime * result
 				+ ((serverName == null) ? 0 : serverName.hashCode());
 		result = prime * result
 				+ ((serverPort == null) ? 0 : serverPort.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		HHttpData other = (HHttpData) obj;
 		if (attachments == null) {
 			if (other.attachments != null)
 				return false;
 		} else if (!attachments.equals(other.attachments))
 			return false;
 		if (method == null) {
 			if (other.method != null)
 				return false;
 		} else if (!method.equals(other.method))
 			return false;
 		if (queryArgs == null) {
 			if (other.queryArgs != null)
 				return false;
 		} else if (!queryArgs.equals(other.queryArgs))
 			return false;
 		if (queryPath == null) {
 			if (other.queryPath != null)
 				return false;
 		} else if (!queryPath.equals(other.queryPath))
 			return false;
 		if (!Arrays.equals(rawBody, other.rawBody))
 			return false;
 		if (serverName == null) {
 			if (other.serverName != null)
 				return false;
 		} else if (!serverName.equals(other.serverName))
 			return false;
 		if (serverPort == null) {
 			if (other.serverPort != null)
 				return false;
 		} else if (!serverPort.equals(other.serverPort))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "HHttpData [attachments=" + attachments + ", rawBody="
 				+ Arrays.toString(rawBody) + ", method=" + method
 				+ ", queryArgs=" + queryArgs + ", queryPath=" + queryPath
 				+ ", serverName=" + serverName + ", serverPort=" + serverPort
 				+ "]";
 	}
 
 
 }
