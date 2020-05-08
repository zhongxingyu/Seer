 /*
     This file is part of Dev Envy.
 
     Dev Envy is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     Dev Envy is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with Dev Envy. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.devenvy.net;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.log4j.Logger;
 
 /**
  * <p>
  * Continually listens for new connections and continually listens for data on those connections in separate threads.
  * </p>
  * 
  * @author Gary Buyn
  */
 public abstract class TcpServer implements Runnable, Server
 {
     /**
      * <p>
      * The message of a {@link java.net.SocketException SocketException} that signifies a {@link java.net.Socket Socket} has been closed.
      * </p>
      */
    private static final String SOCKET_CLOSED_MESSAGE = "socket closed";
 
     /**
      * <p>
      * The individual {@link Client}s this <code>TcpServer</code> is connected to.
      * </p>
      */
     private Collection<Client> fClients;
 
     /**
      * <p>
      * Logs messages associated with this class.
      * </p>
      */
     private Logger fLogger;
 
     /**
      * <p>
      * The {@link java.net.ServerSocket ServerSocket} listening for new connections.
      * </p>
      */
     private ServerSocket fServerSocket;
 
     /**
      * <p>
      * Creates an instance of <code>TcpServer</code>.
      * </p>
      * 
      * @param serverSocket The {@link java.net.ServerSocket ServerSocket} listening for new connections.
      */
     public TcpServer(final ServerSocket serverSocket)
     {
         fServerSocket = serverSocket;
 
         fClients = new ArrayList<Client>();
         fLogger = Logger.getLogger(getClass());
     }
 
     @Override
     public final void dispose() throws IOException
     {
         for (Client client : fClients)
         {
             client.dispose();
         }
 
         fServerSocket.close();
     }
 
     /**
      * <p>
      * Retrieves an instance of the {@link Client} that will listen for data.
      * </p>
      * 
      * @param socket The {@link java.net.Socket Socket} to listen for data on.
      * 
      * @return An instance of the <code>Client</code> that will listen for data.
      */
     protected abstract Client getClientInstance(Socket socket);
 
     /**
      * <p>
      * Override to perform additional actions after a new connection is established.
      * </p>
      * 
      * @param socket The {@link java.net.Socket Socket} of the new connection.
      */
     protected void onAcceptConnection(final Socket socket)
     {}
 
     @Override
     public final void run()
     {
         try
         {
             // While the server is still accepting new connections.
             while (!fServerSocket.isClosed())
             {
                 try
                 {
                     Socket socket = fServerSocket.accept();
                     startClientListener(socket);
                     onAcceptConnection(socket);
 
                     fLogger.debug("Accepted connection from " + socket.getRemoteSocketAddress());
                 }
                 catch (SocketException e)
                 {
                     // If the connection accepter was closed locally.
                    if (e.getMessage().equalsIgnoreCase(SOCKET_CLOSED_MESSAGE))
                     {
                         fLogger.debug("The connection accepter was closed.");
                         dispose();
                     }
                     else
                     {
                         fLogger.fatal("The connection accepter has been unexpectedly terminated.");
                         dispose();
                         throw e;
                     }
                 }
             }
         }
         catch (Exception e)
         {
             fLogger.fatal("Epic fail!", e);
         }
     }
 
     /**
      * <p>
      * Continually listens for data on the new connection in a separate thread (a client listener).
      * </p>
      * 
      * @param socket The {@link java.net.Socket Socket} to listen for data on.
      * 
      * @throws IOException Thrown if the <code>Client</code> fails to be instantiated.
      */
     private void startClientListener(final Socket socket) throws IOException
     {
         final Client client = getClientInstance(socket);
         Thread clientThread = new Thread()
         {
             @Override
             public void run()
             {
                 try
                 {
                     // While the connection to the client is open.
                     while (client.isConnected())
                     {
                         client.receiveData();
                     }
                 }
                 catch (Exception e)
                 {
                     fLogger.fatal("The connection to the client has been unexpectedly terminated.", e);
                 }
             }
         };
         clientThread.start();
 
         fClients.add(client);
     }
 }
