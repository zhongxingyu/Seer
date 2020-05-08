 package com.example.projectbat;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.util.Log;
 
 public class BluetoothService 
 {	
 	private BluetoothServerSocket btServerSocket = null;
 	private Thread acceptThread = null;	
 	
 	private final Map<String, BluetoothSocket> sockets = new HashMap<String, BluetoothSocket>();
 	private final Map<String, BluetoothSocket> redundantSockets = new HashMap<String, BluetoothSocket>();
 	private final Map<String, Connection> connections = new HashMap<String, Connection>(); 	
 
 	private final UUID uuid = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
 	
 	private final int STRING = 1;
 	private final int OBJECT = 2;
 	
 	public final BluetoothAdapter btAdapter;
 	public final BluetoothInterface btInterface;
 	
 	public final LinkBuildingHandlers linkBuildingHandlers;
 	public final AudioHandlers audioHandlers;
 	
 	public final ArrayList<String> addresses = new ArrayList<String>();
 	
 	public Connection parent = null;
 	public Connection child = null;
 	
 	public final byte addressesMsgID = 0x2;
 	public final byte addressMsgID = 0x4;
 	public final byte broadcastMsgID = 0x6;
 	public final byte sendtoMsgID = 0x8;
 	
 	public final String BUILDING_DONE = "BuildingDone";
 	public final String START_LISTENING = "StartListening";	
 
 	public BluetoothService(final BluetoothInterface ie)
 	{
 		btInterface = ie;
 		btAdapter = BluetoothAdapter.getDefaultAdapter();	
 		
 		linkBuildingHandlers = new LinkBuildingHandlers(this);
 		audioHandlers = new AudioHandlers(this);
 	}
 
 	public synchronized void start()
 	{
 		btInterface.displayMessage("Service started");
 		
 		acceptThread = new Thread(new AcceptThread());
 		acceptThread.setDaemon(true);
 		acceptThread.start();        	
 	}
 
 	public synchronized void stop()
 	{
 		try{ btServerSocket.close(); }
 		catch(IOException e){ Log.e("Bluetooth", e.getMessage()); }
 
 		for(BluetoothSocket btSocket : sockets.values())
 		{
 			try{ btSocket.close(); }
 			catch(IOException e){ Log.e("Bluetooth", e.getMessage()); }
 		}
 		for(BluetoothSocket btSocket : redundantSockets.values())
 		{
 			try{ btSocket.close(); }
 			catch(IOException e){ Log.e("Bluetooth", e.getMessage()); }
 		}		
 	}
 	
 	public void broadcastMessage(final String message)
 	{
 		btInterface.displayMessage("Broadcasting message: " + message);
 				
 		for (Connection con : connections.values())					
 			con.sendString(message, broadcastMsgID);		
 	}
 	
 	public void sendToId(final String address, final String msg)
 	{
 		btInterface.displayMessage("Sending: " + msg + " to: " + address);
 		
 		final int myIndex = addresses.indexOf(btAdapter.getAddress());
 		final int targetIndex = addresses.indexOf(address);
 		
 		ArrayList<String> data = new ArrayList<String>();
 		data.add(address);
 		data.add(msg);
 		
 		if (myIndex < targetIndex)
 			parent.sendObject(data, sendtoMsgID);
 		else
 			child.sendObject(data, sendtoMsgID);
 	}
 	
 	public boolean connect(final BluetoothDevice device)
 	{
 		final String address = device.getAddress();
 		
 		if(sockets.containsKey(address)) return connections.get(address) != null;
 		
 		btAdapter.cancelDiscovery();
 
 		BluetoothSocket btSocket = null;
 		try { btSocket = device.createInsecureRfcommSocketToServiceRecord(uuid); }
 		catch (IOException e) { Log.e("Bluetooth", e.getMessage()); }
 
 		if(btSocket != null)
 		{
 			try { btSocket.connect(); }
 			catch (IOException e)
 			{
 				try { btSocket.close(); }
 				catch(IOException e1) { Log.e("Bluetooth", e1.getMessage()); }
 
 				Log.e("Bluetooth", e.getMessage());
 			}
 
 			if(btSocket.isConnected())
 			{
 				btInterface.displayMessage("Connecting succeeded");	        			
 									
 				child = new Connection(btSocket);
 				connections.put(address, child);				
 
 				sockets.put(address, btSocket);				
 				linkBuildingHandlers.connectingSucceeded(device);
 				return true;				
 			}     			
 		}
 		btInterface.displayMessage("Connecting failed.");
 		return false;
 	}
 	
 	private class AcceptThread implements Runnable
 	{	
 		public void run()
 		{		
 			try
 			{
 				btServerSocket = btAdapter.listenUsingInsecureRfcommWithServiceRecord("THUNDER", uuid);
 			}
 			catch (IOException e)
 			{
 				Log.e("Bluetooth", e.getMessage());
 				btInterface.exit();
 			}
 
 			while(true)
 			{
 				BluetoothSocket btSocket = null;
 
 				try { btSocket = btServerSocket.accept(); }
 				catch(IOException e) { Log.e("Bluetooth", e.getMessage()); return; }
 
 				if(btSocket != null)
 				{
 					final BluetoothDevice remoteDevice = btSocket.getRemoteDevice();
 					final String address = remoteDevice.getAddress();					
 
 					if( !sockets.containsKey(address) )
 					{
 						btInterface.displayMessage("Accepting succeeded");
 						
 						parent = new Connection(btSocket);						
 						connections.put(address, parent);						
 						sockets.put(address, btSocket);
 						
 						linkBuildingHandlers.acceptingSucceeded(remoteDevice);												
 					}
 					else if( !redundantSockets.containsKey(address)) 
 						redundantSockets.put(address, btSocket);									
 				}
 			}						
 		}
 	}
 
 	public class Connection 
 	{
 		private final BluetoothSocket socket;
 
 		private final InputStream inStream;
 		private final OutputStream outStream;
 
 		private final Thread connectedThread;
 
 		public Connection(final BluetoothSocket s)
 		{			
 			socket = s;	
 
 			InputStream tmpIn = null;
 			OutputStream tmpOut = null;
 
 			try 
 			{
 				tmpIn = socket.getInputStream();
 				tmpOut = socket.getOutputStream();
 			}
 			catch (IOException e) 
 			{
 				Log.e("Bluetooth", "Failed to create i/o streams.", e);
 				btInterface.exit();
 			}			
 
 			inStream = tmpIn;
 			outStream = tmpOut;
 
 			connectedThread = new Thread(new ConnectionThread());
 			connectedThread.start();
 		}
 
 		public void sendString(final String s, final byte msgID) 
 		{
 			btInterface.displayMessage("Sending string: " + s);
 			
 			byte[] bytes = null;
 
 			try { bytes = s.getBytes("UTF-8"); } 
 			catch (UnsupportedEncodingException e) { e.printStackTrace(); }
 
 			ByteArrayOutputStream bos = new ByteArrayOutputStream();
 			try 
 			{
 				bos.write(bytes);
 				bos.write(msgID);
 				bos.flush();
 				
 				bytes = bos.toByteArray();
 				sendBytes(bytes);
 			}
 			catch (IOException e) { e.printStackTrace(); }			
 			finally 
 			{
 				try { bos.close(); }
 				catch (IOException e) { e.printStackTrace(); }		  
 			}			
 		}
 
 		public void sendObject(final Object obj, final byte msgID)
 		{
			btInterface.displayMessage("Sending object");
			
 			ByteArrayOutputStream bos = new ByteArrayOutputStream();
 			ObjectOutput out = null;
 			byte[] bytes = null;
 			
 			try
 			{
 				out = new ObjectOutputStream(bos);
 				out.writeObject(obj);
 				out.writeByte(msgID);
 				out.flush();
 
 				bytes = bos.toByteArray();
 				sendBytes(bytes);
 			} 
 			catch (IOException e) { e.printStackTrace(); }
 			finally 
 			{
 				try { out.close(); bos.close(); }
 				catch (IOException e) { e.printStackTrace(); }		  
 			}
 		}
 		
 		private void sendBytes(final byte[] buffer)
 		{
 			try { outStream.write(buffer); } 
 			catch (IOException e) { e.printStackTrace(); }
 		}
 
 		private class ConnectionThread implements Runnable
 		{
 			private final Map<Byte, Parser> parserMap = new HashMap<Byte, Parser>();
 			
 			public ConnectionThread()
 			{			
 				parserMap.put(addressesMsgID, new Parser()
 				{					
 					public void parse(final byte[] buffer, final int size)
 					{
 						btInterface.displayMessage("Received addresses.");
 						
 						ArrayList<String> arrayList = objectToArrayList(buffer, size);						
 						
 						if (arrayList != null)
 							linkBuildingHandlers.processReceivedAddresses(arrayList);
 					}
 				});
 				
 				parserMap.put(addressMsgID, new Parser()
 				{
 					public void parse(final byte[] buffer, final int size)						
 					{
 						btInterface.displayMessage("Received address");
 			
 						String address = null;
 
 						try { address = new String(buffer, 0, size, "UTF-8"); }
 						catch (UnsupportedEncodingException e) { e.printStackTrace(); }
 
 						if (address != null)
 							linkBuildingHandlers.propagateAddress(address);      	
 					}
 				});
 				
 				parserMap.put(broadcastMsgID, new Parser()
 				{
 					public void parse(final byte[] buffer, final int size)
 					{
 						btInterface.displayMessage("Received broadcast");
 						
 						final ArrayList<String> data = new ArrayList<String>();						
 						String msg = null;
 
 						try { msg = new String(buffer, 0, size, "UTF-8"); }
 						catch (UnsupportedEncodingException e) { e.printStackTrace(); }
 						
 						if (msg == null)
 							return;
 						
 						btInterface.displayMessage(msg);
 						audioHandlers.handlerMap.get(msg).handler();
 						
 						data.add(msg);								
 						relayMessage(data, STRING);						
 					}
 				});
 				
 				parserMap.put(sendtoMsgID, new Parser()
 				{					
 					public void parse(final byte[] buffer, final int size) 
 					{
 						btInterface.displayMessage("Send to msg");
 						
 						String targetAddress, command;
 						ArrayList<String> arrayList = objectToArrayList(buffer, size);												
 						
 						if (arrayList == null)
 							return;
 						
 						targetAddress = arrayList.get(0);
 						command = arrayList.get(1);
 						
						if (targetAddress == btAdapter.getAddress())
 							audioHandlers.handlerMap.get(command).handler();							
 						else						
 							relayMessage(arrayList, OBJECT);
 					}
 				});
 			}			
 						
 			public void run() 
 			{
 				final byte[] buffer = new byte[1024];
 				int size = 0;
 
 				while( true )
 				{
 					try { size = inStream.read(buffer); } catch (IOException e) { break; }
 
 					if (size > 0)
 					{		           
 						byte id = buffer[size - 1];
 						if (parserMap.get(id) != null)
 							parserMap.get(id).parse(buffer, size - 1);
 						else
 							btInterface.displayMessage("id error: " + Byte.toString(id));
 					}
 				}
 			}
 		}
 		
 		private ArrayList<String> objectToArrayList(final byte[] buffer, final int size)
 		{
			btInterface.displayMessage("Received addresses.");
 			
 			ByteArrayInputStream bis = new ByteArrayInputStream(buffer, 0, size);			            	
 			ObjectInput in = null;						
 			ArrayList<String> arrayList = null;
 			
 			try 
 			{
 				in = new ObjectInputStream(bis);
 				Object o = in.readObject();			            	  
 				arrayList = (ArrayList<String>) o;
 			}
 			catch (IOException e) { e.printStackTrace(); }
 			catch (ClassNotFoundException e) { e.printStackTrace(); }
 			finally 
 			{
 				try 
 				{
 					bis.close();
 					in.close();
 				} catch (IOException e) { e.printStackTrace(); }			            	  
 			}
 			return arrayList;
 		}
 		
 		private void relayMessage(final ArrayList<String> msg, final int msgType)
 		{
 			for(Map.Entry<String, BluetoothSocket> e : sockets.entrySet())
 			{
 				final String address = e.getKey();
 				final BluetoothSocket sock = e.getValue();
 				
 				if (sock != socket)
 				{
 					Connection con = connections.get(address);
 					
 					if (msgType == STRING)
 						con.sendString(msg.get(0), broadcastMsgID);
 					else if (msgType == OBJECT)
 						con.sendObject(msg, sendtoMsgID);
 				}								
 			}
 		}
 	}	
 }
 
 interface Parser
 {
 	public void parse(final byte[] buffer, final int size);
 }
