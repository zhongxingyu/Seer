 /*
 * Copyright (c) 2013 Sonatype, Inc.
  *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
  */

 package org.sonatype.sisu.bl.support;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import com.sun.jdi.Bootstrap;
 import com.sun.jdi.VirtualMachine;
 import com.sun.jdi.VirtualMachineManager;
 import com.sun.jdi.connect.AttachingConnector;
 import com.sun.jdi.connect.Connector;
 import com.sun.jdi.connect.IllegalConnectorArgumentsException;
 
 /**
  * Helper for Java Debugger Interface (JDI) operations
  */
 public abstract class DebuggerUtils
 {
 
     /**
      * Connects to a remote vm as a debugger, using the specified hostname, port, within the specified millisecond timeout.
      *
      * @param hostName the host to connect to
      * @param port     the port to connect to
      * @param timeout  the socket timeout for connecting, see {@link java.net.Socket#setSoTimeout(int)}, or null to not
      *                 specify an explicit timeout ( not recommended )
      * @return the connected {@link VirtualMachine}
      * @throws IOException see {@link AttachingConnector#attach(java.util.Map)}
      * @throws IllegalConnectorArgumentsException see {@link AttachingConnector#attach(java.util.Map)}
      *
      * @throws IllegalArgumentException if a suitable remote connector cannot be found in this JVM
      * @throws NullPointerException     if hostname or port is null
      */
     public static VirtualMachine connectRemote( String hostName, Integer port, Integer timeout )
         throws IOException, IllegalConnectorArgumentsException
     {
         VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
         AttachingConnector a = findConnector( "com.sun.jdi.SocketAttach", vmm.attachingConnectors() );
         Map<String, Connector.Argument> args = a.defaultArguments();
         args.get( "hostname" ).setValue( checkNotNull( hostName, "hostname required" ) );
         args.get( "port" ).setValue( checkNotNull( port, "port required" ).toString() );
         if ( timeout != null )
         {
             args.get( "timeout" ).setValue( timeout.toString() );
         }
         return a.attach( args );
     }
 
     private static <T extends Connector> T findConnector( String name, List<T> connectors )
     {
         for ( T c : connectors )
         {
             if ( c.name().equals( name ) )
             {
                 return c;
             }
         }
         throw new IllegalArgumentException( "Unable to find a connector named " + name );
     }
 
 }
