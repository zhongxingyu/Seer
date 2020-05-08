 /*
 SubspaceMobile - A subspace/continuum client for mobile phones
 Copyright (C) 2010 Kingsley Masters
 Email: kshade2001 at users.sourceforge.net
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 
 REVISIONS:
  */
 
 package com.subspace.network;
 
 import java.io.IOException;
 import java.net.*;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.channels.AsynchronousCloseException;
 import java.nio.channels.ClosedByInterruptException;
 import java.nio.channels.DatagramChannel;
 
 import android.util.Log;
 
 public abstract class Network implements Runnable {
    private static final boolean LOG_RAW_PACKETS = false;
    protected static final String TAG = "Subspace";
    private static final int MAX_UDP_PACKETSIZE = 512;
    
    protected INetworkCallback callback;
    
    private Thread networkThread;
    private DatagramChannel channel;
    private boolean isRunning;   
    
    private int packetOutCount = 0;
    private int packetInCount = 0;
    private long BytesOut = 0;
    private long BytesIn = 0;
    
    public Network(INetworkCallback callback) {
        setCallback(callback);
    }
 
    public final void setCallback(INetworkCallback callback) {
        this.callback = callback;
    }
 
    protected final void Connect(String host, int port) throws IOException {	   
 	   InetSocketAddress remoteHostaddress = new InetSocketAddress(host,port);
 	   channel = DatagramChannel.open();
 	   //channel.configureBlocking(false);
 	   channel.connect(remoteHostaddress);   
 	   
        //woo we are connected
        networkThread = new Thread(this);
        isRunning = true;
        networkThread.start();
    }
 
    public final void run() {
 	   ByteBuffer buffer = ByteBuffer.allocateDirect(MAX_UDP_PACKETSIZE);	   
 	   buffer.order(ByteOrder.LITTLE_ENDIAN); //subspace uses little endian
 	   
        while (isRunning) {
            try {        	   
         	   buffer.clear();        	   
         	   int lengthOfData = channel.read(buffer);
         	   //ignore random empty packets
         	   if(lengthOfData>0)
         	   {
         		   //increment packet count        	   
 	        	   BytesIn+=lengthOfData;
 	               packetInCount++;
 	               //flip buffer
 	               buffer.flip();	                      
 	               //verbose
 	               if(LOG_RAW_PACKETS)
             	   {
 	            	   Log.v(TAG, "R:" + Util.ToHex(buffer));	               
 	               }
 	               //send call back
 	               callback.Recv(buffer, true);
         	   }
            } catch (AsynchronousCloseException ioe) {
        	   Log.v(TAG,"Timeout exceeded, interrupted");
           } catch (PortUnreachableException pue)
           {
        	   Log.v(TAG,"Unable to connect, no response");
            } catch (Exception ioe) {
                Log.e(TAG,Log.getStackTraceString(ioe));
            }
        }
    }
 
    protected final void Send(ByteBuffer buffer) throws IOException {
 	   //rewind
 	   buffer.rewind();
 	   //verbose
 	   if(LOG_RAW_PACKETS)
 	   {
 		   Log.v(TAG, "S:" + Util.ToHex(buffer));
 	   }
 	   //write
 	   int writenBytes = channel.write(buffer);  
 	   
 	   //stats
 	   BytesOut+=writenBytes;
        packetOutCount++;
    }
 
    protected final void Close() throws IOException {
        isRunning = false;
        channel.disconnect();
        channel.close();
        if (this.networkThread.isAlive()) {
            this.networkThread.interrupt();
        }
    }
    
    //accessers
    public final int getSentDatagramCount()
    {
 	   return packetOutCount;
    }
    public final int getRecvDatagramCount()
    {
 	   return packetInCount;
    }
 }
