 /*
  * Based on CustomAgent.java found at the following URLs:
  * https://blogs.oracle.com/jmxetc/entry/connecting_through_firewall_using_jmx
  * https://blogs.oracle.com/jmxetc/entry/more_on_premain_and_jmx
  */
 /*
  * CustomAgent.java
  *
  * Copyright 2007, 2011 Sun Microsystems, Inc.  All Rights Reserved.
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
 
 package org.jruby.ext.jmxwrapper;
 
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.rmi.registry.LocateRegistry;
 import java.util.HashMap;
 import javax.management.MBeanServer;
 import javax.management.remote.JMXConnectorServer;
 import javax.management.remote.JMXConnectorServerFactory;
 import javax.management.remote.JMXServiceURL;
 import java.rmi.server.RMIServerSocketFactory;
 import javax.management.remote.rmi.RMIConnectorServer;
 
 public class Agent {
 
     private static Thread cleaner;
 
     private Agent() { }
 
     public static void premain(String agentArgs) throws IOException {
 
         // Ensure cryptographically strong random number generator used
         // to choose the object number - see java.rmi.server.ObjID
         //
         System.setProperty("java.rmi.server.randomIDs", "true");
 
         // Ensure JRuby JMX beans are available in all runtimes
         System.setProperty("jruby.management.enabled", "true");
 
        final int port = Integer.parseInt(System.getProperty("org.jruby.jmxwrapper.agent.port", "5900"));
        final String hostname = System.getProperty("org.jruby.jmxwrapper.agent.hostname", "localhost");

        // Make sure our RMI server knows which host we're binding to
        System.setProperty("java.rmi.server.hostname", hostname);
        System.setProperty("java.rmi.server.disableHttp", "true");

         final RMIServerSocketFactory factory = new RMIServerSocketFactoryImpl(hostname);
 
         LocateRegistry.createRegistry(port, null, factory);
         MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
         HashMap<String,Object> env = new HashMap<String,Object>();
         env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, factory);
 
         // Create an RMI connector server.
         //
         // As specified in the JMXServiceURL the RMIServer stub will be
        // registered in the RMI registry running in the local host with the
        // name "jmxrmi". This is the same name the out-of-the-box
        // management agent uses to register the RMIServer stub too.
         //
         // The port specified in "service:jmx:rmi://"+hostname+":"+port
         // is the second port, where RMI connection objects will be exported.
         // Here we use the same port as that we choose for the RMI registry.
         // The port for the RMI registry is specified in the second part
         // of the URL, in "rmi://"+hostname+":"+port
         //
        JMXConnectorServer cs = new RMIConnectorServer(makeJMXServiceURL(hostname, port), env, mbs);
 
         cs.start();
         cleaner = new CleanThread(cs);
         cleaner.start();
     }
 
     public static JMXServiceURL makeJMXServiceURL(String hostname, int port) throws IOException {
         return new JMXServiceURL("service:jmx:rmi://"+hostname+
                                  ":"+port+"/jndi/rmi://"+hostname+":"+port+"/jmxrmi");
     }
 
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
                     final Thread[] all = new Thread[Thread.activeCount()+100];
                     final int count = Thread.enumerate(all);
                     loop = false;
                     for (int i=0;i<count;i++) {
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
                             t.join();
                         } catch (Exception ex) {
                         }
                         break;
                     }
                 }
             } catch (Exception ex) {
             } finally {
                 try {
                     cs.stop();
                 } catch (Exception ex) {
                 }
             }
         }
     }
 }
