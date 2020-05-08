 // Copyright (C) 2000 Phil Dawes
 // Copyright (C) 2000, 2001, 2002 Philip Aston
 // Copyright (C) 2001 Paddy Spencer
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder;
 
 import java.io.PrintStream;
 import java.io.FileOutputStream;
 import java.lang.reflect.Constructor;
 
 import net.grinder.plugin.http.HttpPluginSnifferFilter;
 import net.grinder.plugin.http.HttpPluginSnifferResponseFilter;
 import net.grinder.tools.tcpsniffer.ConnectionDetails;
 import net.grinder.tools.tcpsniffer.EchoFilter;
 import net.grinder.tools.tcpsniffer.HTTPProxySnifferEngine;
 import net.grinder.tools.tcpsniffer.JSSEConstants;
 import net.grinder.tools.tcpsniffer.NullFilter;
 import net.grinder.tools.tcpsniffer.SnifferEngine;
 import net.grinder.tools.tcpsniffer.SnifferEngineImplementation;
 import net.grinder.tools.tcpsniffer.SnifferFilter;
 import net.grinder.tools.tcpsniffer.SnifferPlainSocketFactory;
 import net.grinder.tools.tcpsniffer.SnifferSocketFactory;
 
 
 /**
  *
  * @author Phil Dawes
  * @author Philip Aston
  * @version $Revision$
  */
 public class TCPSniffer
 {
     public static final String INITIAL_TEST_PROPERTY =
 	"TCPSniffer.initialTest";
 
     private static final String SSL_SOCKET_FACTORY_CLASS =
 	"net.grinder.tools.tcpsniffer.SnifferSSLSocketFactory";
 
     public static void main(String[] args)
     {
 	final TCPSniffer tcpSniffer = new TCPSniffer(args);
 	tcpSniffer.run();
     }
 
     private Error barfUsage()
     {
 	System.err.println(
 	    "\n" +
 	    "Usage: " +
 	    "\n java " + TCPSniffer.class + " <options>" +
 	    "\n" +
 	    "\n Where options can include:" +
 	    "\n" +
 	    "\n   [-requestFilter <filter>]    Add request filter" +
 	    "\n   [-responseFilter <filter>]   Add response filter" +
 	    "\n   [-httpPluginFilter           See below" +
 	    "\n     [-initialTest <n>]         Number tests from n" +
 	    "\n   ]" +
 	    "\n   [-localHost <host name/ip>]  Default is localhost" +
 	    "\n   [-localPort <port>]          Default is 8001" +
 	    "\n   [-remoteHost <host name>]    Default is localhost" +
 	    "\n   [-remotePort <port>]         Default is 7001" +
 	    "\n   [-proxy]                     Be an HTTP proxy" +
 	    "\n   [-ssl                        Use SSL" +
 	    "\n     [-keyStore <file>]         Key store details for" +
 	    "\n     [-keyStorePassword <pass>] certificates. Equivalent to" +
 	    "\n     [-keyStoreType <type>]     javax.net.ssl.XXX properties" +
 	    "\n   ]" +
 	    "\n   [-colour]                    Be pretty on ANSI terminals" +
 	    "\n   [-timeout]                   Sniffer engine timeout" +
 	    "\n" +
 	    "\n <filter> can be the name of a class that implements" +
 	    "\n " + SnifferFilter.class.getName() + " or" +
 	    "\n one of NONE, ECHO. Default is ECHO." +
 	    "\n" +
 	    "\n When -proxy is specified, -remoteHost and -remotePort" +
 	    "\n are ignored. Specify -ssl for HTTPS support." +
 	    "\n" +
 	    "\n -httpPluginFilter sets the request and response filters" +
 	    "\n to produce a grinder.properties file suitable for use" +
 	    "\n with the HTTP plugin." +
 	    "\n" +
 	    "\n -timeout is how long (in seconds) the sniffer will wait" +
 	    "\n for a request before timing out and freeing the local" +
 	    "\n port." +
 	    "\n"
 	    );
 
 	System.exit(1);
 
 	return null;
     }
 
     private Error barfUsage(String s)
     {
 	System.err.println("\n" + "Error: " + s);
 	throw barfUsage();
     }
 
     private SnifferEngine m_snifferEngine = null;
 
     private TCPSniffer(String[] args)
     {
 	// Default values.
 	SnifferFilter requestFilter = new EchoFilter();
 	SnifferFilter responseFilter = new EchoFilter();
 	int localPort = 8001;
 	String remoteHost = "localhost";
 	String localHost = "localhost";
 	int remotePort = 7001;
 	boolean useSSL = false;
 	boolean proxy = false;
 	int initialTest = 0;
 
 	int timeout = 0; 
 
 	boolean useColour = false;
 
 	try {
 	    // Parse 1.
 	    for (int i=0; i < args.length; i++)
 	    {
 		if (args[i].equals("-initialTest")) {
 		    initialTest = Integer.parseInt(args[++i]);
 		}
 	    }
 
 	    System.setProperty(INITIAL_TEST_PROPERTY,
 			       Integer.toString(initialTest));
 
 	    // Parse 2
 	    for (int i=0; i<args.length; i++)
 	    {
 		if (args[i].equals("-requestFilter")) {
 		    requestFilter = instantiateFilter(args[++i]);
 		}
 		else if (args[i].equals("-responseFilter")) {
 		    responseFilter = instantiateFilter(args[++i]);
 		}
 		else if (args[i].equals("-httpPluginFilter")) {
 		    requestFilter = new HttpPluginSnifferFilter();
 		    responseFilter = new HttpPluginSnifferResponseFilter();
 		}
 		else if (args[i].equals("-localHost")) {
 		    localHost = args[++i];
 		}
 		else if (args[i].equals("-localPort")) {
 		    localPort = Integer.parseInt(args[++i]);
 		}
 		else if (args[i].equals("-remoteHost")) {
 		    remoteHost = args[++i];
 		}
 		else if (args[i].equals("-remotePort")) {
 		    remotePort = Integer.parseInt(args[++i]);
 		}
 		else if (args[i].equals("-ssl")) {
 		    useSSL = true;
 		}
 		else if (args[i].equals("-keyStore")) {
 		    System.setProperty(JSSEConstants.KEYSTORE_PROPERTY,
 				       args[++i]);
 		}
 		else if (args[i].equals("-keyStorePassword")) {
 		    System.setProperty(
 			JSSEConstants.KEYSTORE_PASSWORD_PROPERTY, args[++i]);
 		}
 		else if (args[i].equals("-keyStoreType")) {
 		    System.setProperty(JSSEConstants.KEYSTORE_TYPE_PROPERTY,
 				       args[++i]);
 		}
 		else if (args[i].equals("-proxy")) {
 		    proxy = true;
 		}
 		else if (args[i].equals("-timeout")) {
 		    timeout = Integer.parseInt(args[++i]) * 1000;
 		}
 		else if (args[i].equals("-output")) {
 		    // -output is used by the TCPSniffer web app only
 		    // and is not publicised, users are expected to
 		    // use shell redirection.
 		    final String outputFile = args[++i];
 		    System.setOut(new PrintStream(
 			new FileOutputStream(outputFile + ".out"), true));
 		    System.setErr(new PrintStream(
 			new FileOutputStream(outputFile + ".err"), true));
 		}
 		else if (args[i].equals("-colour")) {
 		    useColour = true;
 		}
 		else if (args[i].equals("-initialTest")) {
 		    /* Already handled */
 		    ++i;
 		}
 		else {
 		    throw barfUsage();
 		}
 	    }
 	}
 	catch (Exception e) {
 	    throw barfUsage();
 	}
 
 	if (timeout < 0) {
 	    throw barfUsage("Timeout must be non-negative");
 	}
 
 	final StringBuffer startMessage = new StringBuffer();
 
 	startMessage.append(
 	    "Initialising " + (useSSL ? "SSL" : "standard") +
 	    " sniffer engine with the parameters:" +
 	    "\n   Request filter:  " + requestFilter.getClass().getName() +
 	    "\n   Response filter: " + responseFilter.getClass().getName() +
 	    "\n   Local host:       " + localHost + 
 	    "\n   Local port:       " + localPort);
 
 	if (proxy) {
 	    startMessage.append(
 		"\n   Listening as " + (useSSL ? "an HTTP/HTTPS" : "an HTTP") +
 		" proxy");
 	} else {
 	    startMessage.append(
 		"\n   Remote host:      " + remoteHost +
 		"\n   Remote port:      " + remotePort);
 	}
 
 	if (useSSL) {
 	    startMessage.append(
 		"\n   (SSL setup could take a few seconds)");
 	}
 
 	System.err.println(startMessage);
 
 	try {
 	    final SnifferSocketFactory sslSocketFactory;
 
 	    if (useSSL) {
 		// SnifferSSLSocketFactory depends on JSSE, load
 		// dynamically.
 		final Class socketFactoryClass =
 		    Class.forName(SSL_SOCKET_FACTORY_CLASS);
 
 		sslSocketFactory =
 		    (SnifferSocketFactory)socketFactoryClass.newInstance();
 	    }
 	    else {
 		sslSocketFactory = null;
 	    }
 
 	    if (proxy) {
 		m_snifferEngine = 
 		    new HTTPProxySnifferEngine(new SnifferPlainSocketFactory(),
 					       sslSocketFactory,
 					       requestFilter,
 					       responseFilter,
 					       localHost,
 					       localPort,
 					       useColour,
 					       timeout);
 	    }
 	    else {
 		m_snifferEngine =
 		    new SnifferEngineImplementation(
 			useSSL ?
			sslSocketFactory : new SnifferPlainSocketFactory(),
 			requestFilter,
 			responseFilter,
 			new ConnectionDetails(localHost, localPort,
 					      remoteHost, remotePort,
 					      useSSL),
 			useColour,
 			timeout);
 	    }
 		
 	    System.err.println("Engine initialised, listening on port " +
 			       localPort);
 	}
 	catch (Exception e){
 	    System.err.println("Could not initialise engine:");
 	    e.printStackTrace();
 	    System.exit(2);
 	}
     }
 
     private SnifferFilter instantiateFilter(String filterClassName)
 	throws Exception
     {
 	if (filterClassName.equals("NONE")) {
 	    return new NullFilter();
 	}
 	else if (filterClassName.equals("ECHO")) {
 	    return new EchoFilter();
 	}
 
 	final Class filterClass;
 	
 	try {
 	    filterClass = Class.forName(filterClassName);
 	}
 	catch (ClassNotFoundException e){
 	    throw barfUsage("Class '" + filterClassName + "' not found");
 	}
 
 	if (!SnifferFilter.class.isAssignableFrom(filterClass)) {
 	    throw barfUsage("The specified filter class ('" +
 			    filterClass.getName() +
 			    "') does not implement the interface: '" +
 			    SnifferFilter.class.getName() + "'");
 	}
 
 	// Instantiate a filter.
 	try {
 	    return (SnifferFilter)filterClass.newInstance();
 	}
 	catch (IllegalAccessException e) {
 	    throw barfUsage("The default constructor of class '" +
 			    filterClass.getName() + "' is not public");
 	}
 	catch (InstantiationException e) {
 	    throw barfUsage("The class '" + filterClass.getName() +
 			    "' does not have a default constructor");
 	}
     }
 	
     public void run() 
     {
 	m_snifferEngine.run();
 	System.err.println("Engine exited");
 	System.exit(0);
     }
 }
