 /**
  * 
  */
 package org.zmonitor.web;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.zmonitor.MonitorMeta;
 import org.zmonitor.impl.MonitorMetaBase;
 import org.zmonitor.marker.Marker;
 
 /**
  * @author Ian YT Tsai(Zanyking)
  *
  */
 public class WebMonitorMeta extends MonitorMetaBase{
 	protected static final long serialVersionUID = 7285893125167926262L;
 	protected String mimeType;
 	protected String protocol;
 	protected String remoteAddr;
 	protected String queryStr;
 	protected String remoteUser;
 	protected String requestURI;
 	protected String servletPath;
 	protected String scheme;
 	protected String reqSessionId;
 	protected String serverName;
 	protected int serverPort;
 	protected String uuid;
 	
 	protected WebMonitorMeta(){}
	public WebMonitorMeta(Marker marker, HttpServletRequest req) {
		super(marker, ZMonitorFilter.TRACKER_NAME, null);
 		mimeType = req.getContentType();
 		protocol = req.getProtocol();
 		remoteAddr = req.getRemoteAddr();
 		queryStr = req.getQueryString();
 		remoteUser = req.getRemoteUser();
 		requestURI = req.getRequestURI();
 		scheme = req.getScheme();
 		reqSessionId = req.getRequestedSessionId();
 		serverName = req.getServerName();
 		serverPort = req.getServerPort();
 		servletPath = req.getServletPath();
 	}
 	
 	
 	
 	public String getUuid() {
 		return uuid;
 	}
 	public void setUuid(String uuid) {
 		this.uuid = uuid;
 	}
 	
 	public String getMimeType() {
 		return mimeType;
 	}
 
 	public String getProtocol() {
 		return protocol;
 	}
 
 	public String getRemoteAddr() {
 		return remoteAddr;
 	}
 
 	public String getQueryStr() {
 		return queryStr;
 	}
 	public Map<String, Object> getQuery() {
 		return WebUtils.toMap(queryStr);
 	}
 
 	public String getRemoteUser() {
 		return remoteUser;
 	}
 
 	public String getRequestURI() {
 		return requestURI;
 	}
 
 	public String getScheme() {
 		return scheme;
 	}
 
 	public String getReqSessionId() {
 		return reqSessionId;
 	}
 
 	public String getServerName() {
 		return serverName;
 	}
 
 	public int getServerPort() {
 		return serverPort;
 	}
 
 	public String getServletPath() {
 		return servletPath;
 	}
 	
 	public boolean isSimilar(MonitorMeta mMeta) {
 		if (this == mMeta)
 			return true;
 		if (!super.isSimilar(mMeta))
 			return false;
 		
 		WebMonitorMeta other = (WebMonitorMeta) mMeta;
 		if (queryStr == null) {
 			if (other.queryStr != null)
 				return false;
 		} else if (!queryStr.equals(other.queryStr))
 			return false;
 		
 		if (requestURI == null) {
 			if (other.requestURI != null)
 				return false;
 		} else if (!requestURI.equals(other.requestURI))
 			return false;
 		if (scheme == null) {
 			if (other.scheme != null)
 				return false;
 		} else if (!scheme.equals(other.scheme))
 			return false;
 		
 		return true;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		
 		result = prime * result
 				+ ((queryStr == null) ? 0 : queryStr.hashCode());
 		result = prime * result
 				+ ((requestURI == null) ? 0 : requestURI.hashCode());
 		result = prime * result
 				+ ((scheme == null) ? 0 : scheme.hashCode());
 		return result;
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		WebMonitorMeta other = (WebMonitorMeta) obj;
 		if (mimeType == null) {
 			if (other.mimeType != null)
 				return false;
 		} else if (!mimeType.equals(other.mimeType))
 			return false;
 		if (protocol == null) {
 			if (other.protocol != null)
 				return false;
 		} else if (!protocol.equals(other.protocol))
 			return false;
 		if (queryStr == null) {
 			if (other.queryStr != null)
 				return false;
 		} else if (!queryStr.equals(other.queryStr))
 			return false;
 		if (remoteAddr == null) {
 			if (other.remoteAddr != null)
 				return false;
 		} else if (!remoteAddr.equals(other.remoteAddr))
 			return false;
 		if (remoteUser == null) {
 			if (other.remoteUser != null)
 				return false;
 		} else if (!remoteUser.equals(other.remoteUser))
 			return false;
 		if (reqSessionId == null) {
 			if (other.reqSessionId != null)
 				return false;
 		} else if (!reqSessionId.equals(other.reqSessionId))
 			return false;
 		if (requestURI == null) {
 			if (other.requestURI != null)
 				return false;
 		} else if (!requestURI.equals(other.requestURI))
 			return false;
 		if (scheme == null) {
 			if (other.scheme != null)
 				return false;
 		} else if (!scheme.equals(other.scheme))
 			return false;
 		if (serverName == null) {
 			if (other.serverName != null)
 				return false;
 		} else if (!serverName.equals(other.serverName))
 			return false;
 		if (serverPort != other.serverPort)
 			return false;
 		return true;
 	}
 
 	
 	protected WebMonitorMeta clone(){
 		WebMonitorMeta clone = new WebMonitorMeta();
 		clone.className = this.className;
 		clone.fileName = this.fileName;
 		clone.lineNumber = this.lineNumber;
 		clone.marker = this.marker;
 		clone.methodName = this.methodName;
 		clone.trackerName = this.trackerName;
 		clone.mimeType = this.mimeType;
 		clone.protocol = this.protocol;
 		clone.remoteAddr = this.remoteAddr;
 		clone.queryStr = this.queryStr;
 		clone.remoteUser = this.remoteUser;
 		clone.requestURI = this.requestURI;
 		clone.scheme = this.scheme;
 		clone.reqSessionId = this.reqSessionId;
 		clone.serverName = this.serverName;
 		clone.serverPort = this.serverPort;
 		clone.servletPath = this.servletPath;
 		return clone;
 	}
 	
 }
