 /**
 Copyright (C) 2012  Delcyon, Inc.
 
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
  */
 package com.delcyon.capo.protocol.client;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.ConnectException;
 import java.net.Socket;
 import java.security.PrivateKey;
 import java.security.Signature;
 import java.util.logging.Level;
 
 import javax.net.ssl.SSLSocket;
 import javax.xml.bind.DatatypeConverter;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.Configuration.PREFERENCE;
 import com.delcyon.capo.client.CapoClient;
 import com.delcyon.capo.datastream.StreamEventFilterInputStream;
 import com.delcyon.capo.datastream.StreamEventListener;
 import com.delcyon.capo.datastream.StreamUtil;
 
 /**
  * @author jeremiah
  *
  */
 public class CapoConnection implements StreamEventListener
 {
     
 	public enum ConnectionTypes
 	{
 	    CAPO_REQUEST
 	}
 	
 	public enum ConnectionResponses
 	{
 	    OK,
 	    BUSY
 	}
 	
 	private Socket socket;	
 	private BufferedInputStream inputStream;
 	private OutputStream outputStream;
     private String serverAddress;
     private int port;
     private int securePort;
     private boolean dumpOnClose = false;
     private StackTraceElement[] callerStackTraceElements;
     @SuppressWarnings("unused")
     private StackTraceElement[] closerStackTraceElements;
     @SuppressWarnings("unused")
     private StackTraceElement[] inputStreamcallerStackTraceElements;
     @SuppressWarnings("unused")
     private StackTraceElement[] inputStreamcloserStackTraceElements;
     private String sslSID;
 
 	public CapoConnection() throws Exception
 	{
 	    serverAddress = CapoApplication.getConfiguration().getValue(PREFERENCE.SERVER_LIST).split(",")[0];
         port = CapoApplication.getConfiguration().getIntValue(PREFERENCE.PORT);
         securePort = CapoApplication.getConfiguration().getIntValue(PREFERENCE.SECURE_PORT);
 		open();					
 	}
 	
 //	public CapoConnection(String serverAddress,int port) throws Exception
 //	{
 //	    this.serverAddress = serverAddress;
 //	    this.port = port;
 //	    open();
 //	}
 	
 	/**
 	 * A little control logic so we can reuse sockets if we have them, as well as figuring out which server to connect to
 	 * @param socket
 	 * @return
 	 * @throws Exception
 	 */
 	
 	public Socket open() throws Exception
 	{
 		callerStackTraceElements = new Exception().getStackTrace();
 		
 		while (this.socket == null || socket.isClosed())
 		{
 			
 			
 			try
 			{
 			    
 			    if (CapoApplication.getSslSocketFactory() != null)
 			    {
 			        CapoClient.logger.log(Level.FINE, "Opening Secure Socket to "+serverAddress+":"+securePort);
 			        this.socket = CapoApplication.getSslSocketFactory().createSocket(serverAddress, securePort);			        
 			        this.socket.setSendBufferSize(CapoApplication.getConfiguration().getIntValue(PREFERENCE.BUFFER_SIZE)+728);
 			    }
 			    else
 			    {
 			        CapoClient.logger.log(Level.FINE, "Opening Socket to "+serverAddress+":"+port);
 			        this.socket = new Socket(serverAddress, port);    
 			    }
 			} 
 			catch (ConnectException connectException)
 			{
 			    CapoApplication.logger.log(Level.WARNING, "Error opening socket, sleeping "+CapoApplication.getConfiguration().getLongValue(CapoClient.Preferences.CONNECTION_RETRY_INTERVAL)+"ms");
 			    Thread.sleep(CapoApplication.getConfiguration().getLongValue(CapoClient.Preferences.CONNECTION_RETRY_INTERVAL));
 			}
 		}
 		socket.setKeepAlive(true);
 		socket.setTcpNoDelay(true);
 		socket.setSoLinger(false, 0);
 		
 		this.outputStream = socket.getOutputStream();
 
 		//This is just for debugging when needed. 
 		InputStream tempInputStream = socket.getInputStream();		
 		if(CapoApplication.logger.isLoggable(Level.FINE))
 		{
 		    inputStreamcallerStackTraceElements = new Exception().getStackTrace();
 		    tempInputStream = new StreamEventFilterInputStream(tempInputStream);
 		    ((StreamEventFilterInputStream) tempInputStream).addStreamEventListener(this);		    
 		}
 
 		this.inputStream = new BufferedInputStream(tempInputStream);
 		
 		if (CapoApplication.getKeyStore() != null && CapoApplication.getSslSocketFactory() != null)
 		{
 		    sslSID = DatatypeConverter.printHexBinary(((SSLSocket)socket).getSession().getId());		    
 			CapoApplication.logger.fine("SSL SID:"+sslSID);
 			String clientID = CapoApplication.getConfiguration().getValue(CapoClient.Preferences.CLIENT_ID);
 			char[] password = CapoApplication.getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray();
 			String authMessage = "AUTH:CID="+clientID;
 
 			PrivateKey privateKey = (PrivateKey) CapoApplication.getKeyStore().getKey(clientID+".private", password);
 			Signature signature = Signature.getInstance("SHA256withRSA");        
 			signature.initSign(privateKey);
 			signature.update(clientID.getBytes());
 			signature.update(((SSLSocket)socket).getSession().getId());
 			authMessage += ":SIG="+DatatypeConverter.printHexBinary(signature.sign())+":";			
 			this.outputStream.write(authMessage.getBytes());
 			this.outputStream.flush();
 			this.inputStream.read();
 		}
 		//check for a busy signal
 		this.outputStream.write(ConnectionTypes.CAPO_REQUEST.toString().getBytes());
 		this.outputStream.flush();
 		byte[] buffer = new byte[256];		
 		StreamUtil.fullyReadIntoBufferUntilPattern(inputStream, buffer, (byte)0);
 		String message = new String(buffer).trim();
 		
 		//check to see if this is a busy message
 		if (message.matches(ConnectionResponses.BUSY+" \\d+"))
 		{
 			long delaytime = Long.parseLong(message.replaceAll(ConnectionResponses.BUSY+" (\\d+)", "$1"));
 			close();
 			CapoApplication.logger.log(Level.WARNING, "Server Busy. Retrying connection in "+delaytime+"ms.");
 			Thread.sleep(delaytime);
 			open();				
 		}
 		else if (message.matches(ConnectionResponses.OK.toString()) == false)
 		{				
 			throw new Exception("Unknown message from server: '"+message+"'");				
 		}
		CapoApplication.logger.log(Level.INFO, "Opened Socket: "+socket);
 		return socket;
 	}
 	
 	
 	//CS & SS
 	@Override
 	protected void finalize() throws Throwable
 	{
 		super.finalize();
 		close();
 	}
 	
 	//CS & SS
 	public void close()
 	{
 	   
 		if (socket != null)
 		{
 			try
 			{
 			    closerStackTraceElements = new Exception("Closing stack trace").getStackTrace();
 			    if(dumpOnClose && socket.isClosed() == false && CapoApplication.logger.isLoggable(Level.FINE))
 		        {
 		            System.err.println("Closing stack trace");
 		            Thread.dumpStack();         
 		            Exception exception = new Exception("Opening stack trace");
 		            exception.setStackTrace(callerStackTraceElements);
 		            exception.printStackTrace();
 		        }
			    CapoApplication.logger.log(Level.INFO, "Closing Socket: "+socket);
 				socket.close();
//				if (socket instanceof SSLSocket)
//				{
//					((SSLSocket) socket).getSession().invalidate();
//				}
 				socket = null;
 			}
 			catch (Exception e)
 			{
 				//we don't really care if this fails.
 			}
 		}
 		if (outputStream != null)
 		{
 			try
 			{
 				outputStream.close();
 			}
 			catch (Exception e)
 			{
 				//do nothing on failure
 			}
 		}
 		if (inputStream != null)
 		{
 			try
 			{
 				inputStream.close();
 			}
 			catch (Exception e)
 			{
 				//do nothing on failure
 			}
 		}
 	}
 	
 	
 	
 	/**
 	 * This returns the output stream of the connection
 	 * @return
 	 */
 	public OutputStream getOutputStream()
 	{
 		return this.outputStream;
 	}
 
 
 	public BufferedInputStream getInputStream()
 	{
 		return this.inputStream;
 	}
 
     public void dumpOnClose(boolean dumpOnClose)
     {
         System.out.println("===================SETTING DUMP ON CLOSE====================");
         this.dumpOnClose = true;
         
     }
 	
     @Override
     public void processStreamEvent(StreamEvent streamEvent) throws IOException
     {
         if(streamEvent == StreamEvent.CLOSED && dumpOnClose)
         {
             inputStreamcloserStackTraceElements = new Exception().getStackTrace();
             System.err.println("Closing stack trace");
             Thread.dumpStack();         
             Exception exception = new Exception("Opening stack trace");
             exception.setStackTrace(callerStackTraceElements);
             exception.printStackTrace();
         }
         
     }
     
 }
