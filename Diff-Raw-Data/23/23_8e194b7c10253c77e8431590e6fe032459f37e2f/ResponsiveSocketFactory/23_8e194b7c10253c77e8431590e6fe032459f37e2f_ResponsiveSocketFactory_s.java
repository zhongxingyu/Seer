 /*******************************************************************************
  * Copyright (c) 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.team.internal.ccvs.core.util;
 
import java.io.*;
 import java.lang.reflect.*;
 import java.net.*;
 
 import org.eclipse.core.runtime.*;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.team.internal.ccvs.core.*;
 
import com.jcraft.jsch.SocketFactory;

 /**
  * Class copied from "org.eclipse.jsch.internal.core"
  */
public class ResponsiveSocketFactory implements SocketFactory {
   private static final String JAVA_NET_PROXY="java.net.Proxy"; //$NON-NLS-1$
   private static final int DEFAULT_TIMEOUT=60; // Seconds
  InputStream in = null;
  OutputStream out = null;
   private IProgressMonitor monitor;
   private final int timeout;
   private static Class proxyClass;
   private static boolean hasProxyClass = true;
   public ResponsiveSocketFactory(IProgressMonitor monitor, int timeout) {
     if (monitor == null)
       monitor = new NullProgressMonitor();
     this.monitor = monitor;
     this.timeout=timeout;
   }
  public InputStream getInputStream(Socket socket) throws IOException {
    if (in == null)
      in = socket.getInputStream();
    return in;
  }
  public OutputStream getOutputStream(Socket socket) throws IOException {
    if (out == null)
      out = socket.getOutputStream();
    return out;
  }
   public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
     Socket socket = null;
     socket = createSocket(host, port, timeout / 1000, monitor);
     // Null out the monitor so we don't hold onto anything
     // (i.e. the SSH2 session will keep a handle to the socket factory around
     monitor = new NullProgressMonitor();
     // Set the socket timeout
     socket.setSoTimeout(timeout);
     return socket;
   }
   
   /**
    * Helper method that will time out when making a socket connection.
    * This is required because there is no way to provide a timeout value
    * when creating a socket and in some instances, they don't seem to
    * timeout at all.
    */
   private Socket createSocket(final String host, final int port, int timeout, IProgressMonitor monitor) throws UnknownHostException, IOException {
     
     // Start a thread to open a socket
     final Socket[] socket = new Socket[] { null };
     final Exception[] exception = new Exception[] {null };
     final Thread thread = new Thread(new Runnable() {
       public void run() {
         try {
           Socket newSocket = internalCreateSocket(host, port);
           synchronized (socket) {
             if (Thread.interrupted()) {
               // we we're either canceled or timed out so just close the socket
               newSocket.close();
             } else {
               socket[0] = newSocket;
             }
           }
         } catch (UnknownHostException e) {
           exception[0] = e;
         } catch (IOException e) {
           exception[0] = e;
         }
       }
     });
     thread.start();
     
     // Wait the appropriate number of seconds
     if (timeout == 0) timeout = DEFAULT_TIMEOUT;
     for (int i = 0; i < timeout; i++) {
       try {
         // wait for the thread to complete or 1 second, which ever comes first
         thread.join(1000);
       } catch (InterruptedException e) {
         // I think this means the thread was interrupted but not necessarily timed out
         // so we don't need to do anything
       }
       synchronized (socket) {
         // if the user canceled, clean up before preempting the operation
         if (monitor.isCanceled()) {
           if (thread.isAlive()) {
             thread.interrupt();
           }
           if (socket[0] != null) {
             socket[0].close();
           }
           // this method will throw the proper exception
           Policy.checkCanceled(monitor);
         }
       }
     }
     // If the thread is still running (i.e. we timed out) signal that it is too late
     synchronized (socket) {
       if (thread.isAlive()) {
         thread.interrupt();
       }
     }
     if (exception[0] != null) {
       if (exception[0] instanceof UnknownHostException)
         throw (UnknownHostException)exception[0];
       else
         throw (IOException)exception[0];
     }
     if (socket[0] == null) {
       throw new InterruptedIOException(NLS.bind(CVSMessages.Util_timeout, new String[] { host })); 
     }
     return socket[0];
   }
   
   /* private */  Socket internalCreateSocket(final String host, final int port)
       throws UnknownHostException, IOException{
     Class proxyClass = getProxyClass();
     if (proxyClass != null) {
       // We need to disable proxy support for the socket
       try{
         
         // Obtain the value of the NO_PROXY static field of the proxy class
         Field field = proxyClass.getField("NO_PROXY"); //$NON-NLS-1$
         Object noProxyObject = field.get(null);
         Constructor constructor = Socket.class.getConstructor(new Class[] { proxyClass });
         Object o = constructor.newInstance(new Object[] { noProxyObject });
         if(o instanceof Socket){
           Socket socket=(Socket)o;
           socket.connect(new InetSocketAddress(host, port), timeout * 1000);
           return socket;
         }
       }
       catch(SecurityException e){
         CVSProviderPlugin.log(IStatus.ERROR, NLS.bind("An internal error occurred while connecting to {0}", host), e); //$NON-NLS-1$
       }
       catch(NoSuchFieldException e){
     	  CVSProviderPlugin.log(IStatus.ERROR, NLS.bind("An internal error occurred while connecting to {0}", host), e); //$NON-NLS-1$
       }
       catch(IllegalArgumentException e){
     	  CVSProviderPlugin.log(IStatus.ERROR, NLS.bind("An internal error occurred while connecting to {0}", host), e); //$NON-NLS-1$
       }
       catch(IllegalAccessException e){
     	  CVSProviderPlugin.log(IStatus.ERROR, NLS.bind("An internal error occurred while connecting to {0}", host), e); //$NON-NLS-1$
       }
       catch(NoSuchMethodException e){
     	  CVSProviderPlugin.log(IStatus.ERROR, NLS.bind("An internal error occurred while connecting to {0}", host), e); //$NON-NLS-1$
       }
       catch(InstantiationException e){
     	  CVSProviderPlugin.log(IStatus.ERROR, NLS.bind("An internal error occurred while connecting to {0}", host), e); //$NON-NLS-1$
       }
       catch(InvocationTargetException e){
     	  CVSProviderPlugin.log(IStatus.ERROR, NLS.bind("An internal error occurred while connecting to {0}", host), e); //$NON-NLS-1$
       }
       
     }
     return new Socket(host, port);
   }
   
   private synchronized Class getProxyClass() {
     if (hasProxyClass && proxyClass == null) {
       try{
         proxyClass = Class.forName(JAVA_NET_PROXY);
       }
       catch(ClassNotFoundException e){
         // We couldn't find the class so we'll assume we are using pre-1.5 JRE
         hasProxyClass = false;
       }
     }
     return proxyClass;
   }
 
 }
