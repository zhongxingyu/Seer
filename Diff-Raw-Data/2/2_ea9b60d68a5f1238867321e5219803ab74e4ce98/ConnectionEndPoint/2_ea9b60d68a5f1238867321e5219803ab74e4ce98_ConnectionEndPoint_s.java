 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.transfer.forwardingNodes;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.Signature;
 import de.tuilmenau.ics.fog.facade.events.ClosedEvent;
 import de.tuilmenau.ics.fog.facade.events.ConnectedEvent;
 import de.tuilmenau.ics.fog.facade.events.DataAvailableEvent;
 import de.tuilmenau.ics.fog.facade.events.ErrorEvent;
 import de.tuilmenau.ics.fog.facade.events.ServiceDegradationEvent;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.packets.PleaseCloseConnection;
 import de.tuilmenau.ics.fog.packets.PleaseUpdateRoute;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.EventSourceBase;
 import de.tuilmenau.ics.fog.util.Logger;
 
 
 public class ConnectionEndPoint extends EventSourceBase implements Connection
 {
 	public ConnectionEndPoint(Name bindingName, Logger logger, LinkedList<Signature> authentications)
 	{
 		this.logger = logger;
 		this.bindingName = bindingName;
 		this.authentications = authentications;
 	}
 	
 	@Override
 	public void connect()
 	{
 		if(isConnected()) {
 			notifyObservers(new ConnectedEvent(this));
 		} else {
 			// TODO
 		}
 	}
 	
 	public void setForwardingNode(ClientFN forwardingNode)
 	{
 		this.forwardingNode = forwardingNode;
 	}
 	
 	public ClientFN getForwardingNode()
 	{
 		return forwardingNode;
 	}
 	
 	@Override
 	public boolean isConnected()
 	{
 		if(forwardingNode != null) {
 			return forwardingNode.isConnected();
 		} else {
 			return false;
 		}
 	}
 	
 	@Override
 	public LinkedList<Signature> getAuthentications()
 	{
 		return authentications;
 	}
 	
 	@Override
 	public Name getBindingName()
 	{
 		return bindingName;
 	}
 	
 	@Override
 	public Description getRequirements()
 	{
 		if(forwardingNode != null) {
 			return forwardingNode.getDescription();
 		} else {
 			return null;
 		}
 	}
 	
 	public void setPacketTraceRouting(boolean pState)
 	{
 		mPacketTraceRouting = pState;
 	}
 	
 	@Override
 	public void write(Serializable data) throws NetworkException
 	{
 		if(data != null) {
 			if(forwardingNode != null) {
 				// just a method to test update route by manual command
 				if(Config.Connection.ENABLE_UPDATE_ROUTE_BY_COMMAND) {
 					if(data.equals(Config.Connection.UPDATE_ROUTE_COMMAND)) {
 						data = new PleaseUpdateRoute(true);
 					}
 				}
 				
 				Packet packet = new Packet(data);
 				if(mPacketTraceRouting){
 					packet.activateTraceRouting();
 				}
 				if(Config.Connection.LOG_PACKET_STATIONS){
 					Logging.log(this, "Sending: " + packet);
 				}
 				forwardingNode.send(packet);
 			} else {
 				throw new NetworkException(this, "Connection end point is not connected. Write operation failed.");
 			}
 		}
 	}
 	
 	@Override
 	public Object read() throws NetworkException
 	{
 		//TODO blocking mode?
 		
 		if(mInputStream != null) {
 			throw new NetworkException(this, "Receiving is done via input stream. Do not call Connection.read."); 
 		}
 		
 		if(mReceiveBuffer != null) {
 			synchronized (this) {
 				if(!mReceiveBuffer.isEmpty()) {
 					return mReceiveBuffer.removeFirst();
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public int available()
 	{
 		if(mInputStream != null) {
 			return mInputStream.available();
 		}
 		
 		if(mReceiveBuffer != null) {
 			return mReceiveBuffer.size();
 		}
 		
 		// no data or connection not open
 		return 0;
 	}
 	
 	@Override
 	public synchronized OutputStream getOutputStream() throws IOException
 	{
 		if(mOutputStream == null) {
 			mOutputStream = new OutputStream() {
 				@Override
 				public void write(int value) throws IOException
 				{
 					try {
 						ConnectionEndPoint.this.write(new byte[] { (byte)value });
 					}
 					catch(NetworkException exc) {
 						throw new IOException(exc);
 					}
 				}
 				
 				public synchronized void write(byte b[], int off, int len) throws IOException
 				{
 					if(b != null) {
 						try {
 							// Copy array since some apps will reuse b in order
 							// to send the next data chunk! That copy can only be
 							// avoided, if the calls behind "write" really do a
 							// deep copy of the packet. However, the payload will
 							// only be copied if the packet is send through a real
 							// lower layer. In pure simulation scenarios that never
 							// happens.
 							byte[] copyB = new byte[len];
 							System.arraycopy(b, off, copyB, 0, len);
 							
 							ConnectionEndPoint.this.write(copyB);
 						}
 						catch(NetworkException exc) {
 							throw new IOException(exc);
 						}
 					}
 				}
 				
 				@Override
 				public void flush() throws IOException
 				{
 					// nothing to do
 				}
 
 			};
 		}
 
 		return mOutputStream;
 	}
 	
 	public synchronized InputStream getInputStream() throws IOException
 	{
 		if(mInputStream == null) {
 			mInputStream = new CEPInputStream();
 			
 			// if there are already some data, copy it to stream
 			// and delete buffer
 			if(mReceiveBuffer != null) {
 				for(Object obj : mReceiveBuffer) {
 					mInputStream.addToBuffer(obj);
 				}
 				
 				mReceiveBuffer = null;
 			}
 		}
 
 		return mInputStream;
 	}
 	
 	/**
 	 * Called by higher layer to close socket.
 	 */
 	@Override
 	public void close()
 	{
 		logger.log(this, "Closing " + this);
 		if(isConnected()) {
 			// inform peer about closing operation
 			try {
 				logger.log(this, "  ..sending PleaseCloseConnection");		
 				write(new PleaseCloseConnection());
 			}
 			catch(NetworkException exc) {
 				logger.err(this, "Can not send close connection message. Closing without it.", exc);
 			}
 			
 			forwardingNode.closed();
 		}else {
 			logger.log(this, "CEP cannot be closed because it is not connected");
 		}
 			
 		
 		cleanup();
 	}
 	
 	/**
 	 * Called by forwarding node, if it was closed
 	 */
	public void closed()
 	{
 		cleanup();
 		
 		// inform higher layer about closing
 		notifyObservers(new ClosedEvent(this));
 	}
 	
 	public void setError(Exception exc)
 	{
 		notifyObservers(new ErrorEvent(exc, this));
 	}
 	
 	public void informAboutNetworkEvent()
 	{
 		notifyObservers(new ServiceDegradationEvent(this));
 	}
 	
 	private synchronized void cleanup()
 	{
 		try {
 			if(mOutputStream != null) mOutputStream.close();
 			if(mInputStream != null) mInputStream.close();
 			
 			mOutputStream = null;
 			mInputStream = null;
 		} catch (IOException tExc) {
 			// ignore exception
 			logger.warn(this, "Ignoring exception during closing operation.", tExc);
 		}
 		
 		mReceiveBuffer = null;
 	}
 	
 	/**
 	 * Called if FoG receives data for a connection end point.
 	 * The method delivers the data to the higher layer or buffers it.
 	 * 
 	 * @param data Received data for higher layer
 	 */
 	public synchronized void receive(Object data)
 	{
 		try {
 			if(mInputStream != null) {
 				mInputStream.addToBuffer(data);
 			} else {
 				if(mReceiveBuffer == null) {
 					mReceiveBuffer = new LinkedList<Object>();
 				}
 				
 				mReceiveBuffer.addLast(data);
 			}
 			
 			notifyObservers(new DataAvailableEvent(this));
 		}
 		catch(IOException exc) {
 			logger.err(this, "Can not receive data '" +data +"'. Closing connection.", exc);
 			close();
 		}
 	}
 	
 	@Override
 	public String toString()
 	{
 		if(forwardingNode != null) {
 			return super.toString() +"@" +forwardingNode.getEntity();
 		} else {
 			return super.toString();
 		}
 	}
 
 	private class CEPInputStream extends ByteArrayInputStream
 	{
 		public CEPInputStream()
 		{
 			super(new byte[0]);
 		}
 
 		@Override
 		public synchronized int read()
 		{
 			int res = super.read();
 			
 			// current buffer empty?
 			if(res < 0) {
 				// blocks until buffers changed
 				res = flipBuffers();
 				
 				// if no error occurred, read again
 				if(res >= 0) res = read();
 			}
 			
 			return res;
 		}
 		
 		@Override
 		public synchronized int read(byte recBuffer[], int offset, int length)
 		{
 			int res = super.read(recBuffer, offset, length);
 			
 			// current buffer empty?
 			if(res < 0) {
 				// blocks until buffers changed
 				res = flipBuffers();
 				
 				// if no error occurred, read again
 				if(res >= 0) res = read(recBuffer, offset, length);
 			}
 			
 			return res;
 		}
 		
 		@Override
 		public void close() throws IOException
 		{
 			super.close();
 			
 			synchronized (buffer) {
 				buffer.close();
 				buffer.notifyAll();
 			}
 		}
 		
 		/**
 		 * Replaces the current buffer of the input stream (which is empty)
 		 * with the current buffer of the output stream, which contains
 		 * the remaining data received via the connection.
 		 * 
 		 * @return number of new bytes; -1 on error
 		 */
 		private synchronized int flipBuffers()
 		{
 			if(isConnected()) {
 				synchronized (buffer) {
 					// wait until 
 					while(buffer.size() <= 0) {
 						try {
 							buffer.wait();
 						}
 						catch (InterruptedException exc) {
 							// ignore it
 						}
 						
 						if(!isConnected()) return -1;
 					}
 					
 					// reset read buffer with buffer from output stream
 					this.count = buffer.size();
 					this.buf = buffer.replaceBuffer();
 					this.pos = 0;
 					this.mark = 0;
 				}
 				return this.count;
 			} else {
 				return -1;
 			}
 		}
 		
 		public void addToBuffer(Object data) throws IOException
 		{
 			if(data != null) {
 				if(data instanceof byte[]) {
 					buffer.write((byte[]) data);
 				} else {
 					buffer.write(data.toString().getBytes());
 				}
 				
 				synchronized (buffer) {
 					buffer.notify();
 				}
 			}
 		}
 		
 		private class CEPByteArrayOutputStream extends ByteArrayOutputStream
 		{
 			/**
 			 * Extracts the current puffer from the output stream and replaces it
 			 * with a new empty one.
 			 * 
 			 * @return current buffer
 			 */
 			public synchronized byte[] replaceBuffer()
 			{
 				byte[] oldBuf = buf;
 				buf = new byte[Math.max(32, count)];
 				count = 0;
 				
 				return oldBuf;
 			}
 		}
 		
 		private CEPByteArrayOutputStream buffer = new CEPByteArrayOutputStream();
 	}
 	
 	
 	private Name bindingName;
 	
 	private Logger logger;
 	private ClientFN forwardingNode;
 	private boolean mPacketTraceRouting = false;
 	private LinkedList<Signature> authentications;
 	
 	private OutputStream mOutputStream;
 	private CEPInputStream mInputStream;
 	private LinkedList<Object> mReceiveBuffer;
 }
