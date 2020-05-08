 // Copyright (C) 2000 Phil Dawes
 // Copyright (C) 2000, 2001, 2002, 2003 Philip Aston
 // Copyright (C) 2001 Paddy Spencer
 // Copyright (C) 2003 Bertrand Ave
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
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.lang.reflect.Constructor;
 import java.util.Properties;
 
 import net.grinder.plugin.http.HTTPPluginTCPProxyFilter;
 import net.grinder.plugin.http.HTTPPluginTCPProxyResponseFilter;
 import net.grinder.tools.tcpproxy.ConnectionDetails;
 import net.grinder.tools.tcpproxy.EchoFilter;
 import net.grinder.tools.tcpproxy.HTTPProxyTCPProxyEngine;
 import net.grinder.tools.tcpproxy.JSSEConstants;
 import net.grinder.tools.tcpproxy.NullFilter;
 import net.grinder.tools.tcpproxy.SingleServerTCPProxyEngine;
 import net.grinder.tools.tcpproxy.TCPProxyConsole;
 import net.grinder.tools.tcpproxy.TCPProxyEngine;
 import net.grinder.tools.tcpproxy.TCPProxyFilter;
 import net.grinder.tools.tcpproxy.TCPProxyPlainSocketFactory;
 import net.grinder.tools.tcpproxy.TCPProxySocketFactory;
 
 
 /**
  * This is the entry point of The TCPProxy process.
  *
  * @author Phil Dawes
  * @author Philip Aston
  * @author Bertrand Ave
  * @version $Revision$
  */
 public final class TCPProxy {
 
   private static final String SSL_SOCKET_FACTORY_CLASS =
     "net.grinder.tools.tcpproxy.TCPProxySSLSocketFactory";
 
   /**
    * Entry point.
    *
    * @param args Command line arguments.
    */
   public static void main(String[] args) {
     final TCPProxy tcpProxy = new TCPProxy(args);
     tcpProxy.run();
   }
 
   private Error barfUsage() {
     System.err.println(
       "\n" +
       "Usage: " +
       "\n java " + TCPProxy.class + " <options>" +
       "\n" +
       "\n Where options can include:" +
       "\n" +
       "\n   [-requestFilter <filter>]    Add request filter" +
       "\n   [-responseFilter <filter>]   Add response filter" +
       "\n   [-httpPlugin]                See below" +
       "\n   [-properties <file>]         Properties passed to the filters" +
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
       "\n   [-timeout]                   Proxy engine timeout" +
       "\n   [-console]                   Display the console" +
       "\n" +
       "\n <filter> can be the name of a class that implements" +
       "\n " + TCPProxyFilter.class.getName() + " or" +
       "\n one of NONE, ECHO. Default is ECHO." +
       "\n" +
       "\n When -proxy is specified, -remoteHost and -remotePort" +
       "\n are ignored. Specify -ssl for HTTPS support." +
       "\n" +
       "\n -httpPlugin sets the request and response filters" +
       "\n to produce a test script suitable for use with the" +
       "\n HTTP plugin." +
       "\n" +
       "\n -timeout is how long (in seconds) the proxy will wait" +
       "\n for a request before timing out and freeing the local" +
       "\n port." +
       "\n"
       );
 
     System.exit(1);
 
     return null;
   }
 
   private Error barfUsage(String s) {
     System.err.println("\n" + "Error: " + s);
     throw barfUsage();
   }
 
   private TCPProxyEngine m_proxyEngine = null;
 
   private TCPProxy(String[] args) {
     final PrintWriter outputWriter = new PrintWriter(System.out);
 
     // Default values.
     TCPProxyFilter requestFilter = new EchoFilter(outputWriter);
     TCPProxyFilter responseFilter = new EchoFilter(outputWriter);
     int localPort = 8001;
     String remoteHost = "localhost";
     String localHost = "localhost";
     int remotePort = 7001;
     boolean useSSL = false;
     boolean proxy = false;
     boolean console = false;
 
     int timeout = 0;
 
     boolean useColour = false;
 
     try {
       // Parse 1.
       for (int i = 0; i < args.length; i++) {
         if (args[i].equalsIgnoreCase("-properties")) {
           final Properties properties = new Properties();
           properties.load(new FileInputStream(new File(args[++i])));
           System.getProperties().putAll(properties);
         }
         else if (args[i].equalsIgnoreCase("-initialtest")) {
           final String argument = i + 1 < args.length ? args[++i] : "123";
 
           barfUsage("-initialTest is no longer supported.\n\n" +
                     "Use -DHTTPPlugin.initialTest=" + argument +
                     " or the -properties option instead.");
         }
       }
 
       // Parse 2.
       for (int i = 0; i < args.length; i++) {
         if (args[i].equalsIgnoreCase("-requestfilter")) {
           requestFilter = instantiateFilter(args[++i], outputWriter);
         }
         else if (args[i].equalsIgnoreCase("-responsefilter")) {
           responseFilter =
             instantiateFilter(args[++i], outputWriter);
         }
         else if (args[i].equalsIgnoreCase("-httpplugin")) {
           requestFilter = new HTTPPluginTCPProxyFilter(outputWriter);
           responseFilter =
             new HTTPPluginTCPProxyResponseFilter(outputWriter);
         }
         else if (args[i].equalsIgnoreCase("-localhost")) {
           localHost = args[++i];
         }
         else if (args[i].equalsIgnoreCase("-localport")) {
           localPort = Integer.parseInt(args[++i]);
         }
         else if (args[i].equalsIgnoreCase("-remotehost")) {
           remoteHost = args[++i];
         }
         else if (args[i].equalsIgnoreCase("-remoteport")) {
           remotePort = Integer.parseInt(args[++i]);
         }
         else if (args[i].equalsIgnoreCase("-ssl")) {
           useSSL = true;
         }
         else if (args[i].equalsIgnoreCase("-keystore")) {
           System.setProperty(JSSEConstants.KEYSTORE_PROPERTY,
                              args[++i]);
         }
         else if (args[i].equalsIgnoreCase("-keystorepassword")) {
           System.setProperty(
             JSSEConstants.KEYSTORE_PASSWORD_PROPERTY, args[++i]);
         }
         else if (args[i].equalsIgnoreCase("-keystoretype")) {
           System.setProperty(JSSEConstants.KEYSTORE_TYPE_PROPERTY,
                              args[++i]);
         }
         else if (args[i].equalsIgnoreCase("-proxy")) {
           proxy = true;
         }
         else if (args[i].equalsIgnoreCase("-timeout")) {
           timeout = Integer.parseInt(args[++i]) * 1000;
         }
         else if (args[i].equalsIgnoreCase("-output")) {
           // -output is used by the TCPProxy web app only
           // and is not publicised, users are expected to
           // use shell redirection.
           final String outputFile = args[++i];
           System.setOut(new PrintStream(
                           new FileOutputStream(outputFile + ".out"), true));
           System.setErr(new PrintStream(
                           new FileOutputStream(outputFile + ".err"), true));
         }
        else if (args[i].equalsIgnoreCase("-displayconsole")) {
           console = true;
         }
         else if (args[i].equalsIgnoreCase("-colour") ||
                  args[i].equalsIgnoreCase("-color")) {
           useColour = true;
         }
         else if (args[i].equals("-properties")) {
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
       " proxy engine with the parameters:" +
       "\n   Request filter:  " + requestFilter.getClass().getName() +
       "\n   Response filter: " + responseFilter.getClass().getName() +
       "\n   Local host:       " + localHost +
       "\n   Local port:       " + localPort);
 
     if (proxy) {
       startMessage.append(
         "\n   Listening as " + (useSSL ? "an HTTP/HTTPS" : "an HTTP") +
         " proxy");
     }
     else {
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
       final TCPProxySocketFactory sslSocketFactory;
 
       if (useSSL) {
         // TCPProxySSLSocketFactory depends on JSSE, load
         // dynamically.
         final Class socketFactoryClass =
           Class.forName(SSL_SOCKET_FACTORY_CLASS);
 
         sslSocketFactory =
           (TCPProxySocketFactory)socketFactoryClass.newInstance();
       }
       else {
         sslSocketFactory = null;
       }
 
       if (proxy) {
         m_proxyEngine =
           new HTTPProxyTCPProxyEngine(
             new TCPProxyPlainSocketFactory(),
             sslSocketFactory,
             requestFilter,
             responseFilter,
             outputWriter,
             localHost,
             localPort,
             useColour,
             timeout);
       }
       else {
         m_proxyEngine =
           new SingleServerTCPProxyEngine(
             useSSL ?
             sslSocketFactory : new TCPProxyPlainSocketFactory(),
             requestFilter,
             responseFilter,
             outputWriter,
             new ConnectionDetails(localHost, localPort,
                                   remoteHost, remotePort,
                                   useSSL),
             useColour,
             timeout);
       }
 
       if (console) {
         new TCPProxyConsole(m_proxyEngine);
       }
 
       System.err.println("Engine initialised, listening on port " +
                          localPort);
     }
     catch (Exception e) {
       System.err.println("Could not initialise engine:");
       e.printStackTrace();
       System.exit(2);
     }
   }
 
   private TCPProxyFilter instantiateFilter(
     String filterClassName, PrintWriter outputWriter) throws Exception {
 
     if (filterClassName.equals("NONE")) {
       return new NullFilter(outputWriter);
     }
     else if (filterClassName.equals("ECHO")) {
       return new EchoFilter(outputWriter);
     }
 
     final Class filterClass;
 
     try {
       filterClass = Class.forName(filterClassName);
     }
     catch (ClassNotFoundException e) {
       throw barfUsage("Class '" + filterClassName + "' not found");
     }
 
     if (!TCPProxyFilter.class.isAssignableFrom(filterClass)) {
       throw barfUsage("The specified filter class ('" +
                       filterClass.getName() +
                       "') does not implement the interface: '" +
                       TCPProxyFilter.class.getName() + "'");
     }
 
     // Instantiate a filter.
     try {
       final Constructor constructor =
         filterClass.getConstructor(new Class[] {PrintWriter.class});
 
       return (TCPProxyFilter)constructor.newInstance(
         new Object[] {outputWriter});
     }
     catch (NoSuchMethodException e) {
       throw barfUsage(
         "The class '" + filterClass.getName() +
         "' does not have a constructor that takes a PrintWriter");
     }
     catch (IllegalAccessException e) {
       throw barfUsage("The constructor of class '" +
                       filterClass.getName() + "' is not public");
     }
     catch (InstantiationException e) {
       throw barfUsage("The class '" + filterClass.getName() +
                       "' is abstract");
     }
   }
 
   private void run() {
     Runtime.getRuntime().addShutdownHook(
       new Thread() {
         public void run() { m_proxyEngine.stop(); }
       });
 
     m_proxyEngine.run();
     System.err.println("Engine exited");
     System.exit(0);
   }
 }
