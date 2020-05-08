 
 
 package org.vpac.grisu.view.ws.login;
 
 import org.apache.log4j.Logger;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.handler.AbstractHandler;
 import org.codehaus.xfire.soap.SoapVersion;
 import org.globus.myproxy.CredentialInfo;
 import org.globus.myproxy.MyProxy;
 import org.globus.myproxy.MyProxyException;
 import org.ietf.jgss.GSSCredential;
 import org.ietf.jgss.GSSException;
 import org.jdom.Element;
 import org.jdom.Namespace;
 import org.vpac.grisu.control.utils.ServerPropertiesManager;
 import org.vpac.grisu.credential.model.ProxyCredential;
 
 /**
  * This one gets the username & password out of the http header and uses it to
  * get a credential from the MyProxy server.
  * 
  * @author Markus Binsteiner
  * 
  */
 public class MyProxyAuthHandler extends AbstractHandler {
 
 	static final Logger myLogger = Logger.getLogger(MyProxyAuthHandler.class
 			.getName());
 
 	public final static String AUTH_NS = "http://grisu.vpac.org/grisu-ws";
 	public final static String AUTH_NAME = "AuthenticationToken";
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.codehaus.xfire.handler.Handler#invoke(org.codehaus.xfire.MessageContext)
 	 */
 	public void invoke(MessageContext context) throws Exception {
 
 		SoapVersion version = context.getInMessage().getSoapVersion();
 		
 		Element header = context.getInMessage().getHeader();
 		if (header == null)
 			return;
 
 		Element authEl = header.getChild(AUTH_NAME, Namespace
 				.getNamespace(AUTH_NS));
 
 		if (authEl == null) {
 			throw new XFireFault("No authentication information in header.",
 					XFireFault.SENDER);
 		}
 
 		String username = authEl.getChildText("Username", Namespace
 				.getNamespace(AUTH_NS));
 		String password = authEl.getChildText("Password", Namespace
 				.getNamespace(AUTH_NS));
 		String myProxyServer = authEl.getChildText("MyProxyServer", Namespace
 				.getNamespace(AUTH_NS));
 		String myProxyPort = authEl.getChildText("MyProxyPort", Namespace
 				.getNamespace(AUTH_NS));
 
 		if (username == null || "".equals(username) || password == null
 				| "".equals(password)) {
 			throw new XFireFault(
 					"No valid authentication information in header.",
 					XFireFault.SENDER);
 		}
 
 		setAuth(username, password, myProxyServer, myProxyPort, context);
 
 	}
 
 
 
 	private void setAuth(String username, String password,
 			String myProxyServer, String myProxyPort,
 			MessageContext handlerContext) throws XFireFault {
 
 		// MessageContext context = MessageContextHelper.getContext();
 		// if ( context == null ) {
 		// context = handlerContext;
 		// MessageContextHelper.setContext(context);
 		// }
 
 		ProxyCredential credential = (ProxyCredential) handlerContext
 				.getSession().get("credential");
 		// ProxyCredential credential =
 		// (ProxyCredential)context.getProperty("credential");
 		int port = Integer.parseInt(myProxyPort);
 
 		if (credential == null || !credential.isValid()) {
 
 			credential = createProxyCredential(username, password,
 					myProxyServer, port, ServerPropertiesManager
 							.getMyProxyLifetime());
 
 			if (credential == null || !credential.isValid())
 				throw new XFireFault(
 						"No valid authentication information in header: Could not create valid credential.",
 						XFireFault.SENDER);
 
 			// put the credential into the session
 			handlerContext.getSession().put("credential", credential);
 			
 		} else {
 
 			long oldLifetime = -2;
 			try {
 				oldLifetime = credential.getGssCredential()
 						.getRemainingLifetime();
 			} catch (GSSException e) {
 				oldLifetime = -2;
 			}
 			if (oldLifetime < ServerPropertiesManager
 					.getMinProxyLifetimeBeforeGettingNewProxy()) {
 				myLogger
 						.debug("Credential reached minimum lifetime. Creating new one. Old lifetime: "
 								+ oldLifetime);
 
 
 				credential = createProxyCredential(username, password,
 						myProxyServer, port, ServerPropertiesManager
 								.getMyProxyLifetime());
 				if (credential == null || !credential.isValid())
 					throw new XFireFault(
 							"No valid authentication information in header: Could not create valid credential.",
 							XFireFault.SENDER);
 
 				// put the credential into the session
 				handlerContext.getSession().put("credential", credential);
 
 				
 			}
 			
 			long endTime = proxyEndtime(credential.getGssCredential(), username, password, myProxyServer, port);
 			handlerContext.getSession().put("credentialEndTime", endTime);
 			// put the credential endtime into the session
 			
 		}
 	}
 	
 	private ProxyCredential createProxyCredential(String username,
 			String password, String myProxyServer, int port, int lifetime) {
 		MyProxy myproxy = new MyProxy(myProxyServer, port);
 		GSSCredential proxy = null;
 		try {
 			proxy = myproxy.get(username, password, lifetime);
 
 			int remaining = proxy.getRemainingLifetime();
 
 			if (remaining <= 0)
 				throw new RuntimeException("Proxy not valid anymore.");
 
 			return new ProxyCredential(proxy);
 		} catch (Exception e) {
			e.printStackTrace();
 			myLogger.error("Could not create myproxy credential: "
 					+ e.getLocalizedMessage());
 			return null;
 		}
 
 	}
 
 	private long proxyEndtime(GSSCredential credential, String username, String password, String myProxyServer, int port) {
 
 		MyProxy myproxy = new MyProxy(myProxyServer, port);
 		CredentialInfo info = null;
 		try {
 			info = myproxy.info(credential, username, password);
 		} catch (MyProxyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return info.getEndTime();
 	}
 	
 }
