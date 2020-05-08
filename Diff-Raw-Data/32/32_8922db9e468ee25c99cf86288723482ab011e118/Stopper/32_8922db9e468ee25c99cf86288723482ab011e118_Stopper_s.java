 /*
  * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
  */
 
 package com.sudothought.jmx;
 
 import javax.management.JMException;
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorServer;
 import javax.management.remote.MBeanServerForwarder;
 import java.io.IOException;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 
 /**
  * The Stopper class creates an MBeanServerForwarder that can intercept
  * a call to a stop() operation on a "fake" MBean, and stop the connector
  * when that operation is called.
  * Any other called received from the remote side is forwarded to the wrapped
  * MBeanServer.
  * If this forwarder is configured on a JMXConnectorServer, then it will be \
  * possible to stop that JMXConnectorServer remotely using Stopper.stopServer()
  * (see below).
  * The Stopper.stopServer(JMXConnector) method will invoke the stop() operation
  * on the "fake" MBean recognized by the MBeanServerForwarder.
  * <p/>
  * By default the name of the "fake" MBean is:
  * {@code JMImplementation:type=JMXConnectorStopper,name=<instancename>}
  * where {@code <instancename>} defaults to {@code "Stopper"}.
  * <p/>
  * It is possible to specify an alternate value for {@code <instancename>}
  * with the "example.rmi.agent.stopper" system property.
  * The stopper name must be a "shared secret" between the server and the
  * clients that are authorized to stop it.
  * If you started your server using the property
  * example.rmi.agent.stopper=0xf43456ef then you must start your client
  * with the same system property before invoking Stopper.stopServer();
  *
  * @author dfuchs
  */
 public class Stopper {
 
     public static String STOPPER_PROPERTY = "jmx.agent.stopper";
     public static String STOPPER_NAME     = "Stopper";
     public static String STOP             = "stop";
 
     /**
      * Creates an MBeanServerForwarder that will forward all incoming calls
      * to a wrapped MBeanServer, except when the operation {@code stop()} is
      * invoked on a fake "Stopper" MBean (see above).
      * In that case, the returned forwarder will stop the given connector and
      * unexport the given registry (if not null).
      * Use it as follows:
      * <code>
      * final Registry registry     = LocateRegistry.createRegistry(...);
      * final JMXConnectorServer cs =
      * JMXConnectorServerFactory.newJMXConnectorServer(...);
      * MBeanServerForwarder mbsf = Stopper.createForwarderFor(cs,registry);
      * cs.setMBeanServerForwarder(mbsf);
      * cs.start();
      * </code>
      * <p/>
      * To stop the server remotely, simply connects to it with a
      * JMXConnector and invoke Stopper.stopServer(connector);
      * You cannot stop the server using jconsole because the "fake" MBean does
      * not appear in queryNames() etc... Only a client that "knows the secret"
      * will be able to stop the connector server.
      *
      * @param connector the connector to stop.
      * @param registry  the registry to unexport.
      * @return
      */
     public static MBeanServerForwarder createForwarderFor(final JMXConnectorServer connector, final Registry registry) {
 
         final InvokeOperationInterceptor interceptor = new InvokeOperationInterceptor(getDefaultStopperName(), STOP) {
 
             @Override
             public Object intercept(MBeanServer server, ObjectName mbean,
                                     String invoke, Object[] args, String[] signature) throws Exception {
 
                 // stop the connector.
                 //
                 connector.stop();
 
                 // need to unexport the registry if we want to recreate
                 // it later...
                 //
                 if (registry != null)
                     UnicastRemoteObject.unexportObject(registry, false);
                 System.out.println("Agent stopped");
                 return null;
             }
 
             @Override
             public ClassLoader getClassLoaderFor(MBeanServer server, ObjectName mbean) {
                 return null;
             }
         };
         return ForwardingInterceptor.newForwardingInterceptor(interceptor);
     }
 
     /**
      * Get the "fake" stopper MBean name.
      *
      * @return The default stopper name, built using the value provided by
      *         the {@code example.rmi.agent.stopper} system property. if any.
      */
     public static ObjectName getDefaultStopperName() {
         final String stopperName = System.getProperty(STOPPER_PROPERTY, STOPPER_NAME);
         try {
             return ObjectName.getInstance("JMImplementation:type=JMXConnectorStopper,name=" + stopperName);
         }
         catch (MalformedObjectNameException x) {
             throw new IllegalArgumentException(stopperName, x);
         }
     }
 
     /**
      * Stops the server to which the provided JMXConnector {@code c} is
      * connected. Works only if the server was configured to use an
      * MBeanServerForwarder created by this class - and if the stopper
      * names used at the remote side matches the stopper name used at
      * the client side (the server and client share the same secret).
      */
     public static void stopServer(JMXConnector c)
             throws IOException {
         try {
             c.getMBeanServerConnection().invoke(getDefaultStopperName(), STOP, null, null);
         }
         catch (JMException x) {
             throw new IOException("can't stop server: " + x, x);
         }
     }
 }
