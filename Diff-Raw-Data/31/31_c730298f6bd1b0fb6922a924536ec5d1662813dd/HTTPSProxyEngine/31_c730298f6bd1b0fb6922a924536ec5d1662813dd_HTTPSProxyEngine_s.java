 //Based on HTTPProxySnifferEngine.java from The Grinder distribution.
 // The Grinder distribution is available at http://grinder.sourceforge.net/
 
 package mitm;
 
 import java.io.BufferedInputStream;
 import java.io.InterruptedIOException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.math.BigInteger;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.security.Principal;
 import javax.security.cert.X509Certificate;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.SSLSocket;
 
 import iaik.asn1.ObjectID;
 import iaik.asn1.structures.Name;
 
 
 /** 
  * HTTPS proxy implementation.
  *
  * A HTTPS proxy client first send a CONNECT message to the proxy
  * port. The proxy accepts the connection responds with a 200 OK,
  * which is the client's queue to send SSL data to the proxy. The
  * proxy just forwards it on to the server identified by the CONNECT
  * message.
  *
  * The Java API presents a particular challenge: it allows sockets
  * to be either SSL or not SSL, but doesn't let them change their
  * stripes midstream. (In fact, if the JSSE support was stream
  * oriented rather than socket oriented, a lot of problems would go
  * away). To hack around this, we accept the CONNECT then blindly
  * proxy the rest of the stream through a special
  * ProxyEngine class (ProxySSLEngine) which is instantiated to
  * handle SSL.
  *
  */
 public class HTTPSProxyEngine extends ProxyEngine
 {
 
     public static final String ACCEPT_TIMEOUT_MESSAGE = "Listen time out";
 
     private String m_tempRemoteHost;
     private int m_tempRemotePort;
 
     private final Pattern m_httpsConnectPattern;
 
     private final ProxySSLEngine m_proxySSLEngine;
     
     public HTTPSProxyEngine(MITMPlainSocketFactory plainSocketFactory,
 			    MITMSSLSocketFactory sslSocketFactory,
 			    ProxyDataFilter requestFilter,
 			    ProxyDataFilter responseFilter,
 			    String localHost,
 			    int localPort,
 			    int timeout)
         throws IOException, PatternSyntaxException
     {
 	// We set this engine up for handling plain HTTP and delegate
 	// to a proxy for HTTPS.
 	super(plainSocketFactory,
 	      requestFilter,
 	      responseFilter,
 	      new ConnectionDetails(localHost, localPort, "", -1, false),
 	      timeout);
 	
  	m_httpsConnectPattern =
 	    Pattern.compile("^CONNECT[ \\t]+([^:]+):(\\d+).*\r\n\r\n",
 			    Pattern.DOTALL);
 
 	// When handling HTTPS proxies, we use our plain socket to
 	// accept connections on. We suck the bit we understand off
 	// the front and forward the rest through our proxy engine.
 	// The proxy engine listens for connection attempts (which
 	// come from us), then sets up a thread pair which pushes data
 	// back and forth until either the server closes the
 	// connection, or we do (in response to our client closing the
 	// connection). The engine handles multiple connections by
 	// spawning multiple thread pairs.
 
 	assert sslSocketFactory != null;
 	m_proxySSLEngine = new ProxySSLEngine(sslSocketFactory, requestFilter, responseFilter);
 
     }
 
     public void run()
     {
 	// Should be more than adequate.
 	final byte[] buffer = new byte[40960];
 
         while (true) {
             try {
 		//Plaintext Socket with client (i.e. browser)
                 final Socket localSocket = getServerSocket().accept();
 
 		// Grab the first plaintext upstream buffer, which we're hoping is 
 		// a CONNECT message.
 		final BufferedInputStream in =
 		    new BufferedInputStream(localSocket.getInputStream(),
 					    buffer.length);
 
 		in.mark(buffer.length);
 
 		// Read a buffer full.
 		final int bytesRead = in.read(buffer);
 
 		final String line =
 		    bytesRead > 0 ?
 		    new String(buffer, 0, bytesRead, "US-ASCII") : "";
 
 		final Matcher httpsConnectMatcher =
 		    m_httpsConnectPattern.matcher(line);
 
 		// 'grep' for CONNECT message and extract the remote server/port
 
 		if (httpsConnectMatcher.find()) {//then we have a proxy CONNECT message!
 		    // Discard any other plaintext data the client sends us:
 		    while (in.read(buffer, 0, in.available()) > 0) {
 		    }
 
 		    final String remoteHost = httpsConnectMatcher.group(1);
 
 		    // Must be a port number by specification.
 		    final int remotePort = Integer.parseInt(httpsConnectMatcher.group(2));
 
 		    final String target = remoteHost + ":" + remotePort;
 
 		    System.err.println("******* Establishing a new HTTPS proxy connection to " + target);
 
 		    m_tempRemoteHost = remoteHost;
 		    m_tempRemotePort = remotePort;
 
 		    SSLSocket remoteSocket = null;
 		    try {
 			    //Lookup the "common name" field of the certificate from the remote server:
     			remoteSocket = (SSLSocket)
 			    m_proxySSLEngine.getSocketFactory().createClientSocket(remoteHost, remotePort);
 		    } catch (IOException ioe) {
 			    ioe.printStackTrace();
     			// Try to be nice and send a reasonable error message to client
     			sendClientResponse(localSocket.getOutputStream(),"504 Gateway Timeout",remoteHost,remotePort);
     			continue;
 		    }
 
 		    // TODO(cs255): get the remote server's Distinguished Name (DN) and serial number from its actual certificate,
 		    //   so that we can copy those values in the certificate that we forge.
 		    //   (Recall that we, as a MITM, obtain the server's actual certificate from our own session as a client
 		    //    to that server.)
 		    javax.security.cert.X509Certificate[] serverCertChain = null;
 		    iaik.x509.X509Certificate serverCertificate = null;
            X509Certificate java_cert = null;
            Principal serverDN = null;
            BigInteger serverSerialNumber = null;

            if (remoteSocket != null) {
                java_cert = remoteSocket.getSession().getPeerCertificateChain()[0];
                serverDN = java_cert.getIssuerDN();
    		    serverSerialNumber = java_cert.getSerialNumber();                
            }
 
 		    //We've already opened the socket, so might as well keep using it:
 		    m_proxySSLEngine.setRemoteSocket(remoteSocket);
 
 		    //This is a CRUCIAL step:  we dynamically generate a new cert, based
 		    // on the remote server's DN, and return a reference to the internal
 		    // server socket that will make use of it.
 		    ServerSocket localProxy = m_proxySSLEngine.createServerSocket(serverDN, serverSerialNumber);
 
 		    //Kick off a new thread to send/recv data to/from the remote server.
 		    // Remote server's response data is made available via an internal 
 		    // SSLServerSocket.  All this work is handled by the m_proxySSLEngine:
 		    new Thread(m_proxySSLEngine, "HTTPS proxy SSL engine").start();
 
 		    try {Thread.sleep(10);} catch (Exception ignore) {}
 
 		    // Create a new socket connection to our proxy engine.
 		    final Socket sslProxySocket =
 			getSocketFactory().createClientSocket(
 			    getConnectionDetails().getLocalHost(),
 			    localProxy.getLocalPort());
 
 		    // Now set up a couple of threads to punt
 		    // everything we receive over localSocket to
 		    // sslProxySocket, and vice versa.
 		    new Thread(new CopyStreamRunnable(
 				   in, sslProxySocket.getOutputStream()),
 			       "Copy to proxy engine for " + target).start();
 
 		    final OutputStream out = localSocket.getOutputStream();
 
 		    new Thread(new CopyStreamRunnable(
 				   sslProxySocket.getInputStream(), out),
 			       "Copy from proxy engine for " + target).start();
 
 		    // Send a 200 response to send to client. Client
 		    // will now start sending SSL data to localSocket.
 		    sendClientResponse(out,"200 OK",remoteHost,remotePort);
 		}
 		else { //Not a CONNECT request.. nothing we can do.
 		    System.err.println(
 			"Failed to determine proxy destination from message:");
 		    System.err.println(line);
 		    sendClientResponse(localSocket.getOutputStream(),"501 Not Implemented","localhost",
 				       getConnectionDetails().getLocalPort());
 		}
 	    }
 	    catch (InterruptedIOException e) {
 		System.err.println(ACCEPT_TIMEOUT_MESSAGE);
 		break;
 	    }
 	    catch (Exception e) {
 		e.printStackTrace(System.err);
 	    }
         }
     }
 
     private void sendClientResponse(OutputStream out, String msg, String remoteHost, int remotePort) throws IOException {
 	final StringBuffer response = new StringBuffer();
 	response.append("HTTP/1.0 ").append(msg).append("\r\n");
 	response.append("Host: " + remoteHost + ":" +
 			remotePort + "\r\n");
 	response.append("Proxy-agent: CS255-MITMProxy/1.0\r\n");
 	response.append("\r\n");
 	out.write(response.toString().getBytes());
 	out.flush();
     }
 
     /*
      * Used to funnel data between a client (e.g. a web browser) and a
      * remote SSLServer, that the client is making a request to.
      *
      */
     private class ProxySSLEngine extends ProxyEngine {
 	Socket remoteSocket = null;
 	int timeout = 0;
 	/*
 	 * NOTE: that port number 0, used below indicates a system-allocated,
 	 * dynamic port number.
 	 */
 	ProxySSLEngine(MITMSSLSocketFactory socketFactory,
 		       ProxyDataFilter requestFilter,
 		       ProxyDataFilter responseFilter)
 	    throws IOException
 	{
 	    super(socketFactory, requestFilter, responseFilter,
 		  new ConnectionDetails(HTTPSProxyEngine.this.
 					getConnectionDetails().getLocalHost(),
 					0, "", -1, true),
 		  0);
 	}
 
 	public final void setRemoteSocket(Socket s) {
 	    this.remoteSocket = s;
 	}
 
 	public final ServerSocket createServerSocket(Principal remoteServerDN, BigInteger serialNumber) throws IOException, java.security.GeneralSecurityException, Exception {
 	    MITMSSLSocketFactory ssf = new MITMSSLSocketFactory(remoteServerDN, serialNumber);
 	    // you may want to consider caching this result for better performance
 	    m_serverSocket = ssf.createServerSocket("localhost", 0, timeout);
 	    return m_serverSocket;
 	}
 
 
 	/*
 	 * localSocket.get[In|Out]putStream() is data that's (indirectly)
 	 * being read from / written to the client.
 	 *
 	 * m_tempRemoteHost is the remote SSL Server.
 	 */
 	public void run()
 	{
 		try {
 		    final Socket localSocket = this.getServerSocket().accept();
 
 		     System.err.println("New proxy connection to " +
 		       m_tempRemoteHost + ":" + m_tempRemotePort);
 
 		     this.launchThreadPair(localSocket, remoteSocket,
 					  localSocket.getInputStream(),
 					  localSocket.getOutputStream(),
 					  m_tempRemoteHost, m_tempRemotePort);
 		} catch(IOException e) {
 		    e.printStackTrace(System.err);
 		}
 	}
     }
 
 }
