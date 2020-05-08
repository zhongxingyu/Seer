 package fedora.client;
 
 import java.net.URL;
 import java.net.MalformedURLException;
 import javax.xml.rpc.ServiceException;
 
 import fedora.server.access.FedoraAPIA;
 import fedora.server.access.FedoraAPIAServiceLocator;
 
 /**
  *
  * <p><b>Title:</b> APIAStubFactory.java</p>
  * <p><b>Description:</b> </p>
  *
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public abstract class APIAStubFactory {
 
 	/**
 	 * Method to rewrite the default API-A base URL (specified in the service
 	 * locator class FedoraAPIAServiceLocator).  In this case we allow the protocol,
 	 * host, and port parts of the service URL to be replaced.  A SOAP stub
 	 * will be returned with the desired service endpoint URL.
 	 * @param protocol
 	 * @param host
 	 * @param port
 	 * @param path
 	 * @param username
 	 * @param password
 	 * @return
 	 * @throws MalformedURLException
 	 * @throws ServiceException
 	 */
 	public static FedoraAPIA getStub(String protocol, String host, int port, String username, String password)
         throws MalformedURLException, ServiceException {
         	
 	    FedoraAPIAServiceLocator locator=new FedoraAPIAServiceLocator(username, password);
 	
 		//SDP - HTTPS support added
 		URL ourl = null;
 		URL nurl = null;  
 	    if (protocol.equalsIgnoreCase("http")) { 
 			ourl=new URL(locator.getFedoraAPIAPortSOAPHTTPAddress());
 			nurl=rewriteServiceURL(ourl, protocol, host, port, null);
 			if (Administrator.INSTANCE==null) {
 				// if running without Administrator, don't wrap it with the statusbar stuff
 				return locator.getFedoraAPIAPortSOAPHTTP(nurl);
 			} else {
 				return new APIAStubWrapper(locator.getFedoraAPIAPortSOAPHTTP(nurl));
 			}
 	    } else if (protocol.equalsIgnoreCase("https")){
 			ourl=new URL(locator.getFedoraAPIAPortSOAPHTTPSAddress());
 			nurl=rewriteServiceURL(ourl, protocol, host, port, null);
 			if (Administrator.INSTANCE==null) {
 				// if running without Administrator, don't wrap it with the statusbar stuff
 				return locator.getFedoraAPIAPortSOAPHTTPS(nurl);
 			} else {
 				return new APIAStubWrapper(locator.getFedoraAPIAPortSOAPHTTPS(nurl));
 			}
 	    } else {
 			throw new javax.xml.rpc.ServiceException("The protocol" + " " + protocol 
 				+ " is not supported by this service.");
 	    }
 	}
 
 	/**
 	 * Method to rewrite the default API-A base URL (specified in the service
 	 * locator class FedoraAPIAServiceLocator).  In this case we allow the protocol,
 	 * host, port, and PATH parts of the service URL to be replaced.  A SOAP stub
 	 * will be returned with the desired service endpoint URL.
 	 * @param protocol
 	 * @param host
 	 * @param port
 	 * @param path
 	 * @param username
 	 * @param password
 	 * @return
 	 * @throws MalformedURLException
 	 * @throws ServiceException
 	 */	
 	public static FedoraAPIA getStubAltPath(String protocol, String host, int port, 
 		String path, String username, String password)
 		throws MalformedURLException, ServiceException {
         	
 		FedoraAPIAServiceLocator locator=new FedoraAPIAServiceLocator(username, password);
 	
 		//SDP - HTTPS support added
 		URL ourl = null;
 		URL nurl = null;  
 		if (protocol.equalsIgnoreCase("http")) { 
 			ourl=new URL(locator.getFedoraAPIAPortSOAPHTTPAddress());
 			nurl=rewriteServiceURL(ourl, protocol, host, port, path);
 			if (Administrator.INSTANCE==null) {
 				// if running without Administrator, don't wrap it with the statusbar stuff
 				return locator.getFedoraAPIAPortSOAPHTTP(nurl);
 			} else {
 				return new APIAStubWrapper(locator.getFedoraAPIAPortSOAPHTTP(nurl));
 			}
 		} else if (protocol.equalsIgnoreCase("https")){
 			ourl=new URL(locator.getFedoraAPIAPortSOAPHTTPSAddress());
 			nurl=rewriteServiceURL(ourl, protocol, host, port, path);
 			if (Administrator.INSTANCE==null) {
 				// if running without Administrator, don't wrap it with the statusbar stuff
 				return locator.getFedoraAPIAPortSOAPHTTPS(nurl);
 			} else {
 				return new APIAStubWrapper(locator.getFedoraAPIAPortSOAPHTTPS(nurl));
 			}
 		} else {
 			throw new javax.xml.rpc.ServiceException("The protocol" + " " + protocol 
 				+ " is not supported by this service.");
 		}
 	}
 
 	private static URL rewriteServiceURL(URL ourl, String protocol, String host, int port, String path) 
 		throws MalformedURLException, ServiceException {
 	
 		StringBuffer nurl=new StringBuffer();    
 		if (protocol.equalsIgnoreCase("http")) { 
 			nurl.append("http://"); 
 		} else if (protocol.equalsIgnoreCase("https")){
 			nurl.append("https://"); 
 		} else {
 			throw new javax.xml.rpc.ServiceException("The protocol" + " " + protocol 
 				+ " is not supported by this service.");
 		}
 	
 		nurl.append(host);
 		nurl.append(':');
 		nurl.append(port);
 	
 		// Use the path, query, and fragment from the original URL
 		// Otherwise, if an alternate path is provided, use it to complete the service URL
		path=path.trim();
		if (!path.startsWith("/")){
			path="/" + path;
		}
 		if (path==null || path.equals(""))
 		{
 			nurl.append(ourl.getPath());
 			if ((ourl.getQuery()!=null) && (!ourl.getQuery().equals("")) ) {
 				nurl.append('?');
 				nurl.append(ourl.getQuery());
 			}
 			if ((ourl.getRef()!=null) && (!ourl.getRef().equals("")) ) {
 				nurl.append('#');
 				nurl.append(ourl.getRef());
 			}
 		} else {
 			nurl.append(path);
 		}
 		if (fedora.server.Debug.DEBUG) {
 			System.out.println("OLD APIA Service URL = " + ourl);
 			System.out.println("NEW APIA Service URL = " + nurl);
 		}	
 	
 		return new URL(nurl.toString());
 	}
 /*
 	public static void showUsage(String msg) {
 		System.err.println("Syntax:");
 		System.err.println("  APIAStubFactory PROTOCOL HOST PORT PATH USER PASSWORD");
 		System.err.println("ERROR  : " + msg);
 		System.exit(1);
 	}
 	public static void main(String[] args) throws Exception {
 		try {
 			if (args.length!=6) {
 				APIAStubFactory.showUsage("You must provide six arguments.");
 			} else {
 				String protocol=args[0];
 				String host=args[1];
 				int port=Integer.parseInt(args[2]);
 				String path=(args[3]);
 				String username=(args[4]);
 				String password=(args[5]);
 				FedoraAPIA stub = APIAStubFactory.getStubAltPath(protocol, host, port, path, username, password);
 			}
 		} catch (Exception e) {
 			APIAStubFactory.showUsage(e.getClass().getName() + " - "
 				+ (e.getMessage()==null ? "(no detail provided)" : e.getMessage()));
 		}
 	}
 */
 }
