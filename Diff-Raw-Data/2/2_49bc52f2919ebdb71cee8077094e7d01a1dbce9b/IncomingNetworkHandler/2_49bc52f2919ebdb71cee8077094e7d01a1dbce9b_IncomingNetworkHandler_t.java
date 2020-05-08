 /********************************************************************************
  *                                                                              *
  *  This file is part of Recraft.                                               *
  *                                                                              *
  *  Recraft is free software: you can redistribute it and/or modify             *
  *  it under the terms of the GNU General Public License as published by        *
  *  the Free Software Foundation, either version 3 of the License, or           *
  *  (at your option) any later version.                                         *
  *                                                                              *
  *  Recraft is distributed in the hope that it will be useful,                  *
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  *  GNU General Public License for more details.                                *
  *                                                                              *
  *  You should have received a copy of the GNU General Public License           *
  *  along with Recraft.  If not, see <http://www.gnu.org/licenses/>.            *
  *                                                                              *
  *  Copyright 2012 Chris Foster.                                                *
  *                                                                              *
  ********************************************************************************/
 
 package recraft.network;
 
 import java.io.EOFException;
 import java.io.ObjectInputStream;
 import java.net.Socket;
 import java.util.LinkedList;
 
 import recraft.core.Packet;
 
 /** Uses a thread to listen to incomingSocket for incoming packets.  Packets can be found in the LinkedList
  * incomingQueue.  NOTE: Synchronize all accesses to incomingQueue! */
 public class IncomingNetworkHandler
 {
 	public final LinkedList<Packet> incomingQueue;
 
 	private Socket incomingSocket;
 	private Object socketLock;
 
 	private ObjectInputStream incomingStream;
 	private Thread listener;
 
 	public IncomingNetworkHandler(Socket incomingSocket, Object socketLock)
 	{
 		this.incomingQueue = new LinkedList<Packet>();
 
 		this.incomingSocket = incomingSocket;
 		this.socketLock = socketLock;
 
 		try
 		{
 			this.incomingStream = new ObjectInputStream(incomingSocket.getInputStream());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		this.listener = new Thread(new Listener(this.incomingStream, this.incomingQueue));
 		this.listener.start();
 	}
 
 	public IncomingNetworkHandler(Socket incomingSocket)
 	{
 		this(incomingSocket, new Object());
 	}
 
 	public boolean isIntact()
 	{
		if (this.listener == null)
			return false;
 		return this.listener.isAlive();
 	}
 
 	public void close()
 	{
 		synchronized (this.socketLock)
 		{
 			if (this.incomingSocket == null)
 				return;
 
 			try
 			{
 				this.listener.interrupt();
 				this.incomingSocket.shutdownInput();
 				this.listener.join();
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 
 			this.incomingStream = null;
 			this.listener = null;
 			this.incomingSocket = null;
 		}
 	}
 
 	/** Waits on incomingStream and populates incomingQueue. */
 	private static class Listener implements Runnable
 	{
 		private ObjectInputStream incomingStream;
 		private LinkedList<Packet> incomingQueue;
 
 		public Listener(ObjectInputStream incomingStream, LinkedList<Packet> incomingQueue)
 		{
 			this.incomingStream = incomingStream;
 			this.incomingQueue = incomingQueue;
 		}
 
 		@Override
 		public void run()
 		{
 			while (!Thread.interrupted())
 			{
 				Packet in = null;
 				try
 				{
 					in = (Packet)this.incomingStream.readObject();
 				}
 				catch (EOFException e)
 				{
 					break;
 				}
 				catch (Exception e)
 				{
 					e.printStackTrace();
 				}
 
 				if (in != null)
 					synchronized (this.incomingQueue)
 					{
 						this.incomingQueue.add(in);
 					}
 			}
 		}
 	}
 }
