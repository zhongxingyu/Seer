 package fedora.common;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.security.Security;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.httpclient.HostConfiguration;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.protocol.Protocol;
 
 public class HttpClient {
 
 	private org.apache.commons.httpclient.HttpClient apacheCommonsClient;
 	
 	/*
 	private void doGet(String username, String password,
     		//org.apache.commons.httpclient.HttpClient client, 
 			int millisecondsWait, int redirectDepth) throws Exception {
 		getMethod = doGetMethod(username, password, millisecondsWait, redirectDepth);		
 	}
 	*/
 
 
     /*
     public HttpClient(String protocol, String host, String port, String url) {
     	this(protocol, host, port, url, null, null);
     }
     */
 	public GetMethod doNoAuthnGet(int millisecondsWait, int redirectDepth) 
 	throws Exception {
     	System.err.println("doNoAuthnGet.../ ");
 			return doAuthnGet(millisecondsWait, redirectDepth, null, null);
     }    
 	
     public GetMethod doAuthnGet(int millisecondsWait, int redirectDepth, String username, String password) 
     throws Exception {
     	System.err.println("doAuthnGet... " + this.relativePath + "for " + username + " " + password + 
     			" " );
 	  	getMethod = null;
 	  	try {
 	  		boolean authenticate = false;
 	  		apacheCommonsClient.setConnectionTimeout(millisecondsWait);
 	  		if (
 	  				((username == null) && (password == null)) 
 	  		||  	((username != null) && "".equals(username) && (password != null) && "".equals(password))
 			) {
 	  			System.err.println("doAuthnGet(), don't authenticate " + username + " " + password);
 	  		} else {
 		  		if ((username == null) || (password == null) || ("".equals(username))) {
 		  			throw new Exception("unexpected username password mix");
 		  		}
 	  			System.err.println("doAuthnGet(), do authenticate " + username + " " + password);		  		
 		  		apacheCommonsClient.getState().setCredentials(null, null, new UsernamePasswordCredentials(username, password));
 		  		apacheCommonsClient.getState().setAuthenticationPreemptive(true);
 		  		authenticate = true;
 	  		}
 	  		System.err.println("doAuthnGet(), after setup");
 	  		int resultCode = -1;
	  		String workingPath = absoluteUrl;
 	  		for (int loops = 0; (workingPath != null) && (loops < redirectDepth); loops++) {
 	  			getMethod = new GetMethod(workingPath);
 	  			System.err.println("doAuthnGet(), getMethod=" + getMethod + " relpath="+workingPath);	  			
 	  			System.err.println("doAuthnGet(), new loop, url=" + workingPath);
 	  			getMethod.setDoAuthentication(authenticate);
 	  			workingPath = null;
 	  			System.err.println("doAuthnGet(), got GetMethod object=" + getMethod);
 	  			getMethod.setFollowRedirects(true);
 	  	    	System.err.println("just setFollowRedirects(true)"); 
 	  			resultCode = apacheCommonsClient.executeMethod(getMethod);
 	  	    	System.err.println("resultCode=" + resultCode); 
 	  			if (300 <= resultCode && resultCode <= 399) {
 	  				workingPath=getMethod.getResponseHeader("Location").getValue();
 	  				System.err.println("doAuthnGet(), got redirect, new url=" + workingPath);
 	  			}
 	  		}
 	  	} catch (Throwable th) {
 	  		if (getMethod != null) {
 	  			getMethod.releaseConnection();
 	  		}
 	  		System.err.println("doAuthnGet " + th.getMessage());
 	  		if (th.getCause() != null) {
 		  		System.err.println("doAuthnGet " + th.getCause().getMessage());	  			
 	  		}
 	  		throw new Exception("failed connection");
 	    }
 	  	return getMethod;
     }
     
     //private String username = "";
     //private String password = "";
 	private String absoluteUrl = null;
 	private String relativePath = null;
     private String protocol = "";
     private String host = "";
     private String port = "";
     
     private String relativeUrl = null;
     public String getRelativeUrl() {
     	return relativeUrl;
     }
     
     private GetMethod getMethod = null;
     public GetMethod getGetMethod() {
     	return getMethod;
     }
     
     public int getStatusCode() {
     	return (getMethod == null) ? -1 : getMethod.getStatusCode();
     }
     
     private final void releaseConnection() {
 		try {   	
 			getMethod.releaseConnection();
 		} catch (Throwable t) {
 	  		System.err.println(t.getMessage());
 	  		if (t.getCause() != null) {
 		  		System.err.println(t.getCause().getMessage());	  			
 	  		}
 		}
 	}
 
 	private final void closeStream() {
 		try {
 			getGetMethod().getResponseBodyAsStream().close();
 		} catch (Throwable t) {
 	  		System.err.println(t.getMessage());
 	  		if (t.getCause() != null) {
 		  		System.err.println(t.getCause().getMessage());	  			
 	  		}
 		}
     }
 
     public final void close() {
     	closeStream();
     	releaseConnection();
     }
     
     private static final int sslPort = 8443; 
     //private static final String host = "localhost";
     private static final boolean allowSelfSignedCertificates = true;
    
     private static final String captureProtocol = "([^:]+?)://";
     private static final String captureHostWithPort = "([^:]+?):([^/]+?)";
     private static final String captureHostWithoutPort = "([^/]+?)";
     private static final String capturePath = "/(.*)";
     private static final String captureWithPort = captureProtocol + captureHostWithPort + capturePath;
     private static final String captureWithoutPort = captureProtocol + captureHostWithoutPort + capturePath;
     private static final Pattern patternWithPort = Pattern.compile(captureWithPort);
     private static final Pattern patternWithoutPort = Pattern.compile(captureWithoutPort);
     
     public HttpClient(String protocol, String host, String port, String path
     		// , String username, String password
 			) {
     	//this.username = username;
     	//this.password = password;
     	System.err.println("HttpClient " + protocol + " " + host + " " + port + " " + path);
     	if ( (protocol == null) || "".equals(protocol)
     	||   (host == null) || "".equals(host)
     	||   (port == null) || "".equals(port) ) {
     		absoluteUrl = path;
 			System.err.println("parsing " + absoluteUrl + " against " + patternWithPort);
     		//parse url as absolute url into components
     		Matcher matcherWithPort = patternWithPort.matcher(absoluteUrl);
     		if (matcherWithPort.matches()) {
     			protocol = matcherWithPort.group(1);
     			host = matcherWithPort.group(2);
     			port = matcherWithPort.group(3);
     			relativePath = matcherWithPort.group(4);    
 				System.err.println("matched with port " + protocol + " " + host + " " + port + " " + absoluteUrl + " " + relativePath);
     		} else {
     			System.err.println("parsing " + absoluteUrl + " against " + patternWithoutPort);    			
         		Matcher matcherWithoutPort = patternWithoutPort.matcher(absoluteUrl);
         		if (matcherWithoutPort.matches()) {
         			protocol = matcherWithoutPort.group(1);
         			host = matcherWithoutPort.group(2);
         			relativePath = matcherWithoutPort.group(3);
         			if ("http".equals(protocol)) {  // SUPER FIXUP HERE XACML wdn5ef
         				port = "8080";
         			} else if ("http".equals(protocol)) {
         				port = "8443";        				
         			} else {
         				System.err.println("unsupported protocol");
         			}
     				System.err.println("matched without port " + protocol + " " + host + " " + port + " " + absoluteUrl + " " + relativePath);
         		} else {
     				System.err.println("didn't match");        			
     				System.err.println("captureWithPort="+captureWithPort);        			
     				System.err.println("captureWithoutPort="+captureWithoutPort);        			        			
         		}
     		}
     	} else {
     		relativePath = path;
         	absoluteUrl = HttpClient.makeUrl(protocol, host, port, relativePath);
 			System.err.println("not matched " + protocol + " " + host + " " + port + " " + absoluteUrl + " " + relativePath);
     	}
 		System.err.println("protocol="+protocol);
 		System.err.println("host="+host);
 		System.err.println("port="+port);
 		System.err.println("relativePath="+relativePath);
 		System.err.println("absoluteUrl="+absoluteUrl);
 
         try {
         	Protocol easyhttps = null;
         	if (allowSelfSignedCertificates) {
         		//required to use EasySSLProtocolSocketFactory
         		easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), sslPort);
         	}
         	if (allowSelfSignedCertificates) {
         		/* http://jakarta.apache.org/commons/httpclient/sslguide.html seems to say that this should
         		enable self-signed certificates.  can't make it work.
         		Protocol.registerProtocol("https", easyhttps);
         		//check it out:
         		Protocol x = Protocol.getProtocol("https");
         		System.err.println("proto equals?="+easyhttps.equals(x));
         		System.err.println("proto ==?="+(easyhttps==x)); 
         		System.err.println("x="+x.toString());
         		System.err.println("x="+x.getScheme() + " " + x.getDefaultPort() + " " + x.isSecure());        		
         		*/
         	}        	
         	apacheCommonsClient = new org.apache.commons.httpclient.HttpClient(new MultiThreadedHttpConnectionManager());
         	if (allowSelfSignedCertificates) {
         		/* http://jakarta.apache.org/commons/httpclient/sslguide.html says that this works per client
         		instance to enable self-signed certificates.  and it does.
         		//check it out:
         		HostConfiguration hostConfiguration = httpClient.getHostConfiguration();
         		Protocol y = hostConfiguration.getProtocol();
         		System.err.println("proto equals?="+easyhttps.equals(y));
         		System.err.println("proto ==?="+(easyhttps==y)); 
         		System.err.println("y="+y.toString());
         		System.err.println("y="+y.getScheme() + " " + y.getDefaultPort() + " " + y.isSecure());        		
         		*/
         		apacheCommonsClient.getHostConfiguration().setHost(host, sslPort, easyhttps); //required
         	}
         } catch (Exception e) {
 	  		log(e.getMessage());
 	  		if (e.getCause() != null) {
 		  		log(e.getCause().getMessage());	  			
 	  		}
         }
     }
 
     /*
     public HttpClient(String protocol, String host, String port, String url) {
     	this(protocol, host, port, url, null, null);
     }
     */
     /*
     public HttpClient(String url, String username, String password) {
         this(null, null, null, url, username, password);
     }    
     */
     public HttpClient(String url) {
     	//this(url, null, null);
         this(null, null, null, url);    	
     }    
     
     public static String makeUrl(String protocol, String host, String port, String more) {
     	String url = protocol + "://" + host 
 		+ (((port != null) && ! "".equals(port)) ? (":" + port) : "") 
 		+ more;
     	return url;
     }
     
     public String getLineResponseUrl() {
     	String textResponse = "";
         try {
             if (getStatusCode() != 200) {
             	textResponse = "ERROR: request failed, response code was " + getStatusCode();
             } else {
                 BufferedReader in = new BufferedReader(new InputStreamReader(getGetMethod().getResponseBodyAsStream()));
                 textResponse = in.readLine();
                 if (textResponse == null) {
                 	textResponse = "ERROR: response was empty.";
                 }
             }
         } catch (Exception e) {
         	textResponse =  "ERROR: couldn't connect";
 	  		log(e.getMessage());
 	  		if (e.getCause() != null) {
 		  		log(e.getCause().getMessage());	  			
 	  		}
         } finally {
   			close();
         }
         return textResponse;
     }
  
     private boolean log = false;
     
     private final void log(String msg) {
     	if (log) {
   	  	System.err.println(msg);	  		
     	}
     }
     
     public static final void main(String[] args) {
     	HttpClient httpClient = null;
    		System.err.println("SC:call HttpClient()...");
 		if (args.length == 3) {
 			httpClient = new HttpClient(args[0]);
 		} else if (args.length == 6) {
 				httpClient = new HttpClient(args[0], args[1], args[2], args[3]);				
 		}
    		System.err.println("...SC:call HttpClient()");
     	try {
     		if (args.length == 3) {
 	       		System.err.println("SC:call HttpClient.doAuthnGet()...");
 				httpClient.doAuthnGet(20000, 25, args[1], args[2]);
 	       		System.err.println("...SC:call HttpClient.doAuthnGet()");
 	       		System.err.println("SC:call HttpClient.getLineResponseUrl()...");			
 		    	String line = httpClient.getLineResponseUrl();
 		    	System.err.println(line);
     		} else if (args.length == 6) {    		
 	       		System.err.println("SC:call HttpClient.doAuthnGet()...");
 				httpClient.doAuthnGet(20000, 25, args[4], args[5]);
 	       		System.err.println("...SC:call HttpClient.doAuthnGet()");
 	       		System.err.println("SC:call HttpClient.getLineResponseUrl()...");			
 		    	String line = httpClient.getLineResponseUrl();
 		    	System.err.println(line);
     		}
     	} catch (Exception e) {
 			System.err.println("failed on " + e.getMessage());
 		}
     }
     
 }
