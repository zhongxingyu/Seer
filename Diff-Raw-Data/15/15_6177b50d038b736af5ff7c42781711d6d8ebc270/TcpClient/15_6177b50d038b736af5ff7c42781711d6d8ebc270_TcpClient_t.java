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
 import java.net.Socket;
 import java.net.SocketException;
 
 import org.apache.log4j.Logger;
 
 /**
  * <p>
  * A client that can receive and send data to and from another <code>Client</code> over an IP network using the TCP protocol.
  * </p>
  * 
  * @author Gary Buyn
  */
 public abstract class TcpClient implements Client
 {
     /**
      * <p>
      * Sends 'heartbeats' periodically to ensure the connection is still 'alive'.
      * </p>
      * 
      * @author Gary Buyn
      */
     public class Heartbeat implements Runnable
     {
         @Override
         public void run()
         {
             fLogger.debug("Heartbeat started.");
 
             try
             {
                 do
                 {
                     try
                     {
                         Thread.sleep(fHeartbeatInterval);
 
                         if (isConnected())
                         {
                             sendHeartbeat();
                         }
                     }
                     catch (InterruptedException e)
                     {
                         Thread.currentThread().interrupt();
                     }
                     catch (IOException e)
                     {
                         fLogger.debug("The connection to " + fSocket.getRemoteSocketAddress() + " was closed remotely.");
                         dispose();
                     }
                 }
                 while (isConnected() && !Thread.interrupted());
             }
             catch (Exception e)
             {
                 fLogger.error("Failed to maintain heartbeat.");
             }
 
             fLogger.debug("Heartbeat stopped.");
         }
     }
 
     /**
      * <p>
      * The message of a {@link java.net.SocketException SocketException} that signifies a {@link java.net.Socket Socket} has been closed locally.
      * </p>
      */
     private static final Object BROKEN_PIPE_MESSAGE = "Broken pipe";
 
     /**
      * <p>
      * The default data that is sent periodically to ensure the connection is still 'alive'.
      * </p>
      */
     private static final byte[] DEFAULT_HEARTBEAT_DATA = new byte[] {-1};
 
     /**
      * <p>
      * The default time interval between heartbeats (in milliseconds).
      * </p>
      */
     private static final int DEFAULT_HEARTBEAT_INTERVAL = 10000;
 
     /**
      * <p>
      * The default maximum number of bytes that can be received during a single call to {@link #receiveData()}.
      * </p>
      */
     private static final int DEFAULT_MAX_DATA_RECEIVABLE = 1024;
 
     /**
      * <p>
      * The message of a {@link java.net.SocketException SocketException} that signifies a {@link java.net.Socket Socket} has been closed remotely.
      * </p>
      */
    private static final String SOCKET_CLOSED_MESSAGE = "socket closed";
 
     /**
      * <p>
      * The bytes that have been received during the last call to {@link #receiveData()}.
      * </p>
      */
     private byte[] fData;
 
     /**
      * <p>
      * The data that is sent periodically to ensure the connection is still 'alive'.
      * </p>
      */
     private byte[] fHeartbeatData;
 
     /**
      * <p>
      * The time interval between heartbeats (in milliseconds).
      * </p>
      */
     private int fHeartbeatInterval;
 
     /**
      * <p>
      * The thread on which the {@link Heartbeat} is running.
      * </p>
      */
     private Thread fHeartbeatThread;
 
     /**
      * <p>
      * Logs messages associated with this class.
      * </p>
      */
     private Logger fLogger;
 
     /**
      * <p>
      * The maximum number of bytes that can be received during a single call to {@link #receiveData()}.
      * </p>
      */
     private int fMaxDataReceivable;
 
     /**
      * <p>
      * The socket over which the TCP connection is made.
      * </p>
      */
     private Socket fSocket;
 
     /**
      * <p>
      * Creates an instance of <code>TcpClient</code>.
      * </p>
      * 
      * @param socket The socket over which the TCP connection is made.
      */
     public TcpClient(final Socket socket)
     {
         fSocket = socket;
 
         fData = null;
         fHeartbeatData = DEFAULT_HEARTBEAT_DATA;
         fHeartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
         fHeartbeatThread = null;
         fLogger = Logger.getLogger(getClass());
         fMaxDataReceivable = DEFAULT_MAX_DATA_RECEIVABLE;
     }
 
     @Override
     public void dispose() throws IOException
     {
         fSocket.close();
         maintainHeartbeat(false);
     }
 
     @Override
     public byte[] getHeartbeatData()
     {
         return (fHeartbeatData);
     }
 
     @Override
     public int getHeartbeatInterval()
     {
         return (fHeartbeatInterval);
     }
 
     @Override
     public int getMaxDataReceivable()
     {
         return (fMaxDataReceivable);
     }
 
     @Override
     public boolean isConnected()
     {
         return (fSocket.isConnected() && !fSocket.isClosed());
     }
 
     /**
      * <p>
      * Determines whether the given data represents a 'heartbeat' sent to this <code>Client</code>.
      * </p>
      * 
      * @param data The data to compare against the 'heartbeat' data.
      * @param dataLength The length of the data to compare against the 'heartbeat' data.
      * 
      * @return True if the given data represents a 'heartbeat' sent to this <code>Client</code>, false otherwise.
      */
     private boolean isHeartbeat(final byte[] data, final int dataLength)
     {
         boolean heartbeat = true;
 
         if (dataLength != fHeartbeatData.length)
         {
             heartbeat = false;
         }
         else
         {
             for (int index = 0; index < fHeartbeatData.length; index++)
             {
                 if (data[index] != fHeartbeatData[index])
                 {
                     heartbeat = false;
                     break;
                 }
             }
         }
 
         return (heartbeat);
     }
 
     @Override
     public void maintainHeartbeat(final boolean maintainHeartbeat)
     {
         if (maintainHeartbeat)
         {
             // Start the heartbeat if it is not running.
             if (fHeartbeatThread == null || !fHeartbeatThread.isAlive())
             {
                 fHeartbeatThread = new Thread(new Heartbeat());
                 fHeartbeatThread.start();
             }
         }
         else
         {
             // Stop the heartbeat if it is running.
             if (fHeartbeatThread != null && fHeartbeatThread.isAlive())
             {
                 fHeartbeatThread.interrupt();
             }
         }
     }
 
     /**
      * <p>
      * A callback that must be implemented by subclasses to process the data received.
      * </p>
      * 
      * @param data The data received.
      * @param dataLength The length of the data received.
      */
     protected abstract void onReceiveData(byte[] data, final int dataLength);
 
     @Override
     public void receiveData() throws IOException
     {
         // Ensure the correct amount of data can be received.
         if (fData == null || fData.length != fMaxDataReceivable)
         {
             fData = new byte[fMaxDataReceivable];
         }
 
         try
         {
             // If the connection to the client was closed remotely.
             int dataLength = fSocket.getInputStream().read(fData);
             if (dataLength == -1)
             {
                 fLogger.debug("The connection to " + fSocket.getRemoteSocketAddress() + " was closed remotely.");
                 dispose();
             }
             else if (isHeartbeat(fData, dataLength))
             {
                 fLogger.debug("Heartbeat received.");
             }
             else
             {
                 onReceiveData(fData, dataLength);
             }
         }
         catch (SocketException e)
         {
             // If the connection to the client was closed locally.
            if (e.getMessage().equalsIgnoreCase(SOCKET_CLOSED_MESSAGE))
             {
                 fLogger.debug("The connection to " + fSocket.getRemoteSocketAddress() + " was closed locally.");
             }
             else
             {
                 throw e;
             }
 
             dispose();
         }
     }
 
     @Override
     public void sendData(final byte[] data) throws IOException
     {
         try
         {
             fSocket.getOutputStream().write(data);
         }
         catch (SocketException e)
         {
             // If the connection to the client was closed remotely.
             if (e.getMessage().equals(BROKEN_PIPE_MESSAGE))
             {
                 fLogger.debug("The connection to " + fSocket.getRemoteSocketAddress() + " was closed remotely.");
             }
             else
             {
                 throw e;
             }
 
             dispose();
         }
     }
 
     /**
      * <p>
      * Sends a 'heartbeat'.
      * </p>
      * 
      * @throws IOException Thrown if the underlying socket fails to send the 'heartbeat' data.
      */
     private void sendHeartbeat() throws IOException
     {
         sendData(fHeartbeatData);
     }
 
     @Override
     public void setHeartbeatData(final byte[] heartbeatData)
     {
         fHeartbeatData = heartbeatData;
     }
 
     @Override
     public void setHeartbeatInterval(final int heartbeatInterval)
     {
         fHeartbeatInterval = heartbeatInterval;
     }
 
     @Override
     public void setMaxDataReceivable(final int maxDataReceivable)
     {
         fMaxDataReceivable = maxDataReceivable;
     }
 }
