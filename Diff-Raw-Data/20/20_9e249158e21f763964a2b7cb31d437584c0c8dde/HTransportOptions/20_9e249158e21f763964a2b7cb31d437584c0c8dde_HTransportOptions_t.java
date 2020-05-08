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
 
 package org.hubiquitus.hapi.transport;
 
 import org.hubiquitus.hapi.structures.JabberID;
 
 /** 
  * @internal
  * @version 0.3
  * options used for transport layers
  */
 
 public class HTransportOptions {
 	private JabberID jid = null;
 	private String password = null;
 	private String serverHost = null;
 	private int serverPort = 0;
 	private String endpointHost = null;
 	private int endpointPort = 0;
 	private String endpointPath = null;
 	private String hserver = "hnode";
 	
 	public HTransportOptions() {
 		super();
 	}
 	
 	/* getters and setters */
 
 	/**
 	 * Convenient function
 	 * give username part of the jid
 	 * @return username(without domain or resource)
 	 */
 	public String getUsername() {
 		if (jid == null) {
 			throw new NullPointerException("Error : " + this.getClass().getName() + " need a jid");
 		}
 		
 		return jid.getUsername();
 	}
 	
 	/**
 	 * Convenient function
 	 * give resource part of the jid
 	 * @return resource
 	 */
 	public String getResource() {
 		if (jid == null) {
 			throw new NullPointerException("Error : " + this.getClass().getName() + " need a jid");
 		}
 		
 		return jid.getResource();
 	}
 	
 	/**
	 * @return hserver service name (by default it should be "hnode.domain")
 	 */
 	public String getHserverService() {
 		String nodeService = null;
 		
 		if(this.jid != null) {
 			nodeService = this.hserver + "." + this.jid.getDomain();
 		}
 		
 		return nodeService;
 	}
	
	/**
	 * @return pubsub service name (by default it should be "pubsub")
	 */
	public String getPubsubService() {
		return "pubsub" + "." + this.jid.getDomain();
	}
	
 	/**
 	 * @return user jid (ie : my_user@domain.com/resource)
 	 */
 	public JabberID getJid() {
 		return jid;
 	}
 
 
 	public void setJid(JabberID jid) {
 		this.jid = jid;
 	}
 
 
 	public String getPassword() {
 		return password;
 	}
 
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 
 	/**
 	 * @return server host (ie : localhost)
 	 */
 	public String getServerHost() {
 		return serverHost;
 	}
 
 
 	public void setServerHost(String serverHost) {
 		if (serverHost == null || serverHost.equals("")) {
 			this.serverHost = null;
 		} else {
 			this.serverHost = serverHost;
 		}
 	}
 
 	/** 
 	 * @return server port (ie : 5222 for xmpp)
 	 */
 	public int getServerPort() {
 		return serverPort;
 	}
 
 
 	public void setServerPort(int serverPort) {
 		this.serverPort = serverPort;
 	}
 
 
 	/**
 	 * @return endpoint host (ie : localhost)
 	 */
 	public String getEndpointHost() {
 		return endpointHost;
 	}
 
 
 	public void setEndpointHost(String endpointHost) {
 		this.endpointHost = endpointHost;
 	}
 	
 	/** 
 	 * @return endpoint port (ie : 8080)
 	 */
 	public int getEndpointPort() {
 		return endpointPort;
 	}
 
 
 	public void setEndpointPort(int endpointPort) {
 		this.endpointPort = endpointPort;
 	}
 
 	/**
 	 * @return endpoint path (ie my_path) without begin /
 	 */
 	public String getEndpointPath() {
 		return endpointPath;
 	}
 
 
 	public void setEndpointPath(String endpointPath) {
 		this.endpointPath = endpointPath;
 	}
 
 	public String getHserver() {
 		return hserver;
 	}
 
 	public void setHserver(String hserver) {
 		this.hserver = hserver;
 	}
 	/* overrides */
 	
 	@Override
 	public String toString() {
 		return "HTransportOptions [jid=" + jid + ", password=" + password
 				+ ", serverHost=" + serverHost + ", serverPort=" + serverPort
 				+ ", endpointHost=" + endpointHost + ", endpointPort="
 				+ endpointPort + ", endpointPath=" + endpointPath + ", hNode="
 				+ hserver + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((endpointHost == null) ? 0 : endpointHost.hashCode());
 		result = prime * result
 				+ ((endpointPath == null) ? 0 : endpointPath.hashCode());
 		result = prime * result + endpointPort;
 		result = prime * result + ((hserver == null) ? 0 : hserver.hashCode());
 		result = prime * result + ((jid == null) ? 0 : jid.hashCode());
 		result = prime * result
 				+ ((password == null) ? 0 : password.hashCode());
 		result = prime * result
 				+ ((serverHost == null) ? 0 : serverHost.hashCode());
 		result = prime * result + serverPort;
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
 		HTransportOptions other = (HTransportOptions) obj;
 		if (endpointHost == null) {
 			if (other.endpointHost != null)
 				return false;
 		} else if (!endpointHost.equals(other.endpointHost))
 			return false;
 		if (endpointPath == null) {
 			if (other.endpointPath != null)
 				return false;
 		} else if (!endpointPath.equals(other.endpointPath))
 			return false;
 		if (endpointPort != other.endpointPort)
 			return false;
 		if (hserver == null) {
 			if (other.hserver != null)
 				return false;
 		} else if (!hserver.equals(other.hserver))
 			return false;
 		if (jid == null) {
 			if (other.jid != null)
 				return false;
 		} else if (!jid.equals(other.jid))
 			return false;
 		if (password == null) {
 			if (other.password != null)
 				return false;
 		} else if (!password.equals(other.password))
 			return false;
 		if (serverHost == null) {
 			if (other.serverHost != null)
 				return false;
 		} else if (!serverHost.equals(other.serverHost))
 			return false;
 		if (serverPort != other.serverPort)
 			return false;
 		return true;
 	}
 }
