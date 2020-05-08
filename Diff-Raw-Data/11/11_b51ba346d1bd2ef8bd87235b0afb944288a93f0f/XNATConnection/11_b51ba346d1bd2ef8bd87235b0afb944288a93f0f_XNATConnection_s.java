 package edu.gatech.cs7450.xnat;
 
 import java.io.Serializable;
 
 import javax.ws.rs.core.Cookie;
 
 import org.apache.log4j.Logger;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 
 /** 
  * Connection helper for XNAT.
  */
 public class XNATConnection implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
 	private static final Logger _log = Logger.getLogger(XNATConnection.class);
 	
 	/** Host and possibly port and path prefix. */
 	private final String hostBase;
 	/** The username. */
 	private final String user;
 	
 	private transient String pass;
 	
 	private transient Client client;
 	private transient Cookie sessCookie;
 	
 	public XNATConnection(String hostBase, String user, String pass) {
 		if( hostBase == null ) throw new NullPointerException("hostBase is null");
 		if( user == null ) throw new NullPointerException("user is null");
 		
 		this.hostBase = hostBase;
 		this.user = user;
 		this.pass = pass;
 		
 		client = Client.create();
 		client.addFilter(new HTTPBasicAuthFilter(user, pass));
 	}
 	
 	/**
 	 * Get a resource builder, authenticating and adding the session cookie first.
 	 * 
 	 * @param path the resource path, which will be appended to the host base
 	 * @return
 	 */
 	public WebResource.Builder resource(String path) {
 		maybeAuthenticate();
 		return client.resource(hostBase + path).cookie(sessCookie);
 	}
 	
 	/**
 	 * (Re)authenticate and store the new session cookie.
 	 * @throws XNATException if the session cookie could not be found
 	 */
 	public void authenticate() throws XNATException {
 		try {
 			ClientResponse response = client.resource(hostBase + "/JSESSION").get(ClientResponse.class);
 			for( Cookie cookie : response.getCookies() ) {
 				if( "JSESSIONID".equals(cookie.getName()) ) {
 					sessCookie = cookie;
 					return;
 				}
 			}
 			final String MSG = "JSESSIONID cookie not found, response: " + response;
 			_log.error(MSG);
 			throw new XNATException(MSG);
 		} catch( UniformInterfaceException e ) {
 			final String MSG = "Error authenticating with the xNAT server.";
 			_log.error(MSG, e);
 			throw new XNATException(MSG, e);
 		}
 		
 	}
 	
 	/**
 	 * Authenticate only if there's no cookie currently stored.
 	 */
 	public void maybeAuthenticate() {
 		if( sessCookie == null )
 			authenticate();
 	}
 	
 	/**
 	 * Returns the host base, which is the URL for the xNAT REST API.
 	 * @return the host base
 	 */
 	public String getHostBase() {
 		return hostBase;
 	}
 	
 	/**
 	 * Returns the username used to authenticate.
 	 * @return the username
 	 */
 	public String getUser() {
 		return user;
 	}
 
 // Object overrides
 	@Override
 	public String toString() {
 		String pass = this.pass;
 		if( pass != null )
 			pass = "******";
 		return "XNATConnection [hostBase=" + hostBase + ", user=" + user 
 			+ ", pass=" + pass + ", authenticated=" + (sessCookie != null) + "]";
 	}
 
 	/** Hashing base on host and username, subject to change. */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((hostBase == null) ? 0 : hostBase.hashCode());
 		result = prime * result + ((user == null) ? 0 : user.hashCode());
 		return result;
 	}
 
 	/** Equality based on host and username, subject to change. */
 	@Override
 	public boolean equals(Object obj) {
 		if( this == obj )
 			return true;
 		if( obj == null )
 			return false;
 		if( getClass() != obj.getClass() )
 			return false;
 		XNATConnection other = (XNATConnection)obj;
 		if( hostBase == null ) {
 			if( other.hostBase != null )
 				return false;
 		} else if( !hostBase.equals(other.hostBase) )
 			return false;
 		if( user == null ) {
 			if( other.user != null )
 				return false;
 		} else if( !user.equals(other.user) )
 			return false;
 		return true;
 	}
 }
