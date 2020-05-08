 /*
  * CustomAgent.java
  *
  * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *   - Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *   - Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  *
  *   - Neither the name of Sun Microsystems nor the names of its
  *     contributors may be used to endorse or promote products derived
  *     from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * Created on Jul 25, 2007, 11:42:49 AM
  *
  */
 
 package com.sudothought.jmx;
 
 import javax.management.MBeanServer;
 import javax.management.remote.JMXConnectorServer;
 import javax.management.remote.JMXConnectorServerFactory;
 import javax.management.remote.JMXServiceURL;
 import javax.management.remote.rmi.RMIConnectorServer;
 import javax.rmi.ssl.SslRMIClientSocketFactory;
 import javax.rmi.ssl.SslRMIServerSocketFactory;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.net.InetAddress;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.HashMap;
 
 /**
  * This CustomAgent will start an RMI Connector Server using only
  * port "example.rmi.agent.port".
  *
  * @author dfuchs
  */
 public class JmxAgent {
 
    public static final String RMI_HOSTNAME  = "java.rmi.server.hostname";
     public static final String PORT_PROPERTY = "jmx.agent.port";
     public static final String DEFAULT_PORT  = "3412";
 
 
     private JmxAgent() {
     }
 
     public static int getServerPort() {
         return Integer.parseInt(System.getProperty(PORT_PROPERTY, DEFAULT_PORT));
     }
 
     public static void premain(String agentArgs) throws IOException {
 
         final JMXConnectorServer cs = startAgent(agentArgs, false);
 
         // Start the CleanThread daemon...
         final Thread clean = new CleanThread(cs);
         clean.start();
     }
 
     public static JMXConnectorServer startAgent(String agentArgs, final boolean createForwarder) throws IOException {
 
         // Ensure cryptographically strong random number generator used
         // to choose the object number - see java.rmi.server.ObjID
         System.setProperty("java.rmi.server.randomIDs", "true");
 
         // Start an RMI registry on port specified by example.rmi.agent.port
         final int port = getServerPort();
         System.out.println("Create RMI registry on port " + port);
 
         // We create a couple of SslRMIClientSocketFactory and
         // SslRMIServerSocketFactory. We will use the same factories to export
         // the RMI Registry and the JMX RMI Connector Server objects. This
         // will allow us to use the same port for all the exported objects.
         // If we didn't use the same factories everywhere, we would have to
         // use at least two ports, because two different RMI Socket Factories
         // cannot share the same port.
         //
         final SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
         final SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
 
         // Create the RMI Registry using the SSL socket factories above.
         // In order to use a single port, we must use these factories
         // everywhere, or nowhere. Since we want to use them in the JMX
         // RMI Connector server, we must also use them in the RMI Registry.
         // Otherwise, we wouldn't be able to use a single port.
         //
         final Registry registry = LocateRegistry.createRegistry(port, csf, ssf);
 
         // Retrieve the PlatformMBeanServer.
         //
         final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
 
         // Environment map.
         // Specify the SSL Socket Factories:
         final HashMap<String, Object> env = new HashMap<String, Object>();
         env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
         env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
 
         // For binding the JMX RMI Connector Server with the registry
         // created above:
         //
         env.put("com.sun.jndi.rmi.factory.socket", csf);
 
         // Create an RMI connector server.
         //
         // As specified in the JMXServiceURL the RMIServer stub will be
         // registered in the RMI registry running in the local host on
         // port 3000 with the name "jmxrmi". This is the same name the
         // out-of-the-box management agent uses to register the RMIServer
         // stub too.
         //
         // The port specified in "service:jmx:rmi://"+hostname+":"+port
         // is the second port, where RMI connection objects will be exported.
         // Here we use the same port as that we choose for the RMI registry.
         // The port for the RMI registry is specified in the second part
         // of the URL, in "rmi://"+hostname+":"+port
         //
         //System.out.println("Create an RMI connector server");

        // See if java.rmi.server.hostname was used at startup
        final String hostname;
        if (System.getProperty(RMI_HOSTNAME) != null)
            hostname = System.getProperty(RMI_HOSTNAME);
        else
            hostname = InetAddress.getLocalHost().getHostName();

         final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + hostname +
                                                     ":" + port + "/jndi/rmi://" + hostname + ":" + port + "/jmxrmi");
 
         //System.out.println("Creating jmx proxy with URL: " + url);
 
         // Now create the server from the JMXServiceURL
         final JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
         if (createForwarder) {
             cs.setMBeanServerForwarder(Stopper.createForwarderFor(cs, registry));
             System.out.println("Stopper ready for: " + Stopper.getDefaultStopperName());
         }
 
         // Start the RMI connector server.
         System.out.println("RMI connector starting on port: " + port);
         cs.start();
         System.out.println("Proxy started at: " + cs.getAddress());
         return cs;
     }
 
     /**
      * The CleanThread daemon thread will wait until all non-daemon threads
      * are terminated, excluding those non-daemon threads that are kept alive
      * by the presence of a started JMX RMI Connector Server. When no other
      * non-daemon threads remain, it stops the JMX RMI Connector Server,
      * allowing the application to terminate gracefully.
      */
     public static class CleanThread extends Thread {
         private final JMXConnectorServer cs;
 
         public CleanThread(JMXConnectorServer cs) {
             super("JMX Agent Cleaner");
             this.cs = cs;
             setDaemon(true);
         }
 
         public void run() {
             boolean loop = true;
             try {
                 while (loop) {
                     final Thread[] all = new Thread[Thread.activeCount() + 100];
                     final int count = Thread.enumerate(all);
                     loop = false;
                     for (int i = 0; i < count; i++) {
                         final Thread t = all[i];
                         // daemon: skip it.
                         if (t.isDaemon()) continue;
                         // RMI Reaper: skip it.
                         if (t.getName().startsWith("RMI Reaper")) continue;
                         if (t.getName().startsWith("DestroyJavaVM")) continue;
                         // Non daemon, non RMI Reaper: join it, break the for
                         // loop, continue in the while loop (loop=true)
                         loop = true;
                         try {
                             // Found a non-daemon thread. Wait for it.
                             System.out.println("Waiting on thread " + t.getName() + " [id=" + t.getId() + "]");
                             t.join();
                         }
                         catch (Exception ex) {
                             ex.printStackTrace();
                         }
                         break;
                     }
                 }
                 // We went through a whole for-loop without finding any thread
                 // to join. We can close cs.
             }
             catch (Exception ex) {
                 ex.printStackTrace();
             }
             finally {
                 try {
                     // if we reach here it means the only non-daemon threads
                     // that remain are reaper threads - or that we got an
                     // unexpected exception/error.
                     //
                     cs.stop();
                 }
                 catch (Exception ex) {
                     ex.printStackTrace();
                 }
             }
         }
     }
 }
