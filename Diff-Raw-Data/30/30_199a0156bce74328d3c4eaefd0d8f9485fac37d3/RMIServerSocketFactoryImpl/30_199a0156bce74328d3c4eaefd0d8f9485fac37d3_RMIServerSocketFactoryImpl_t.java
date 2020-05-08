 /*
  * Cribbed from http://vafer.org/blog/20061010091658/
  */
 
 package org.jruby.ext.jmxwrapper;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.UnknownHostException;
 import java.rmi.server.RMIServerSocketFactory;
 import javax.net.ServerSocketFactory;
 
 public class RMIServerSocketFactoryImpl implements RMIServerSocketFactory {
     private final InetAddress localAddress;
 
     public RMIServerSocketFactoryImpl(final String address) throws UnknownHostException {
         localAddress = InetAddress.getByName(address);
     }
 
     public ServerSocket createServerSocket(final int port) throws IOException  {
         return ServerSocketFactory.getDefault().createServerSocket(port, 0, localAddress);
     }
 
     public boolean equals(Object obj) {
         return obj != null && obj.getClass().equals(getClass());
     }
 
     public int hashCode() {
         return RMIServerSocketFactoryImpl.class.hashCode();
     }
 }
