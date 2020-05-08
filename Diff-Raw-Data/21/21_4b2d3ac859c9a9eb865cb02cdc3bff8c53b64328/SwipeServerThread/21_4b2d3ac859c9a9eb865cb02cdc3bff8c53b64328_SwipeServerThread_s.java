 package com.appspot.manup.autograph;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.InterruptedIOException;
 import java.io.PrintWriter;
 import java.net.InetAddress;
import java.net.NetworkInterface;
 import java.net.ServerSocket;
 import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
 
 import android.os.Process;
 import android.util.Log;
 
 /**
  *
  * Opens socket server.
  *
  * Can be killed by an interrupt
  *
  */
 public class SwipeServerThread extends Thread
 {
     private static final String TAG = SwipeServerThread.class.getSimpleName();
 
     private static final int PORT = 12345;
     private static final int BACKLOG = 10;
     private static final int SOCKET_TIME_OUT = 5000;
 
 
 
     private final SignatureDatabase mSignatureDatabase;
 
     public SwipeServerThread(SignatureDatabase signatureDatabase)
     {
         super(TAG);
         mSignatureDatabase = signatureDatabase;
     } // SwipeServerThread
 
     @Override
     public void run()
     {
         Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
 
         try
         {
             handleRequests();
         } // try
         catch (final InterruptedException e)
         {
             Log.v(TAG, "Interrupted, now stopping.", e);
         } // catch
         catch (final IOException e)
         {
             Log.d(TAG, "An error occured while handling requests.", e);
         } // catch
     } // run
 
     private void handleRequests() throws IOException, InterruptedException
     {
         InetAddress hostInetAddress = NetworkUtils.getLocalIpAddress();
         if (hostInetAddress == null)
         {
             Log.e(TAG, "Could not get host inet address, aborting running server");
             return;
         } // if
         Log.d(TAG, "host inet: " + hostInetAddress);
 
         ServerSocket serverSocket = null;
         try
         {
             serverSocket = new ServerSocket(PORT, BACKLOG, hostInetAddress);
             serverSocket.setSoTimeout(SOCKET_TIME_OUT);
 
             Log.d(TAG, "Server socket: " + serverSocket.getInetAddress() + ":"
                     + serverSocket.getLocalPort());
 
             while (true)
             {
                 try
                 {
                     handleRequest(serverSocket);
                 } // try
                 catch (final IOException e)
                 {
                     Log.d(TAG, "Failed to handle request.", e);
                 } // catch
             } // while
         } // try
         finally
         {
             if (serverSocket != null)
             {
                 serverSocket.close();
             } // if
         } // finally
     }
 
     private void handleRequest(final ServerSocket serverSocket) throws InterruptedException,
             IOException
     {
         Socket socket = null;
         try
         {
             socket = waitForIncomingConnection(serverSocket);
             final String magStripeNumber = readMagStripeNumber(socket);
 
             if (magStripeNumber == null)
             {
                 Log.w(TAG, "Mag stripe number could not be read.");
                 return;
             } // if
 
             Log.d(TAG, "Mag stripe number: " + magStripeNumber);
 
             final boolean inserted = insertMagStripeNumber(magStripeNumber);
 
             if (!inserted)
             {
                 Log.e(TAG, "Failed to insert mag stripe number into database.");
             } // if
 
             writeResponse(socket, inserted);
         } // try
         finally
         {
             if (socket != null)
             {
                 socket.close();
             } // if
         } // finally
     } // handleRequest
 
     private Socket waitForIncomingConnection(final ServerSocket serverSocket)
             throws InterruptedException, IOException
     {
         Log.d(TAG, "Waiting for incoming connection...");
         while (!Thread.currentThread().isInterrupted())
         {
             try
             {
                 return serverSocket.accept();
             } // try
             catch (final InterruptedIOException e)
             {
                 // Timed out, loop.
             } // catch
         } // while
         throw new InterruptedException("Interrupted while waiting for incoming connection.");
     } // read
 
     private String readMagStripeNumber(final Socket socket) throws IOException
     {
        BufferedReader input = null;
         try
         {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return input.readLine();
         } // try
         finally
         {
            if (input != null)
            {
                input.close();
            } // if
         } // finally
     } // readMagStripe
 
     private boolean insertMagStripeNumber(final String magStripeNumber)
     {
         return mSignatureDatabase.addSignature(magStripeNumber) != -1;
     } // insertMagStripeNumber
 
     private void writeResponse(final Socket socket, final boolean inserted) throws IOException
     {
         PrintWriter output = null;
         try
         {
             output = new PrintWriter(socket.getOutputStream());
             output.print("Ciao buddy");
         } // try
         finally
         {
             if (output != null)
             {
                output.close();
             } // if
         } // finally
     } // writeResponse
 
 } // SwipeServerThread
