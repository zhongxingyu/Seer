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
 	private final BluetoothAdapter btAdapter;
 	private final BluetoothInterface btInterface;
 	BluetoothServerSocket btServerSocket = null;
 
 	private Connection parent = null;
 	private Connection child;
 
 	private final Map<String, BluetoothSocket> sockets = new HashMap<String, BluetoothSocket>();
 	private final Map<String, BluetoothSocket> redundantSockets = new HashMap<String, BluetoothSocket>();
 
 	private final Map<String, Connection> connections = new HashMap<String, Connection>(); 
 	private final ArrayList<String> addresses = new ArrayList<String>();
 
 	private Thread acceptThread = null;	
 
 	private final UUID uuid = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
 
 	private final byte arrayListMsgID = 0x0;
 	private final byte addressMsgID = 0x4;
 	private final byte stringMsgID = 0x8;
 
 	public BluetoothService(BluetoothInterface ie)
 	{
 		btInterface = ie;
 		btAdapter = BluetoothAdapter.getDefaultAdapter();		
 	}
 
 	public synchronized void start()
 	{
 		Log.d("Bluetooth", "Service started");
 
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
 
 	public void linkBuilding()
 	{	
 		btInterface.displayMessage("Starting discovery");
 		btAdapter.startDiscovery();
 	}
 
 	private void connectingSucceeded(BluetoothDevice device)
 	{		
 		String address = device.getAddress();
 		addresses.add(address);
 
 		btInterface.updateDevices(addresses);		
 		btInterface.displayMessage("Connecting succeeded, sending addresses");		
 		btInterface.addPairedDevice(address);		
 
 		// Notify parent of new node in the network
 		if (parent != null)			
 			parent.sendString(address, addressMsgID);
 
 		// Send the child the current known addresses in the network
 		child = connections.get(device.getAddress());
 		child.sendObject(addresses, arrayListMsgID);		
 	}
 
 	private void acceptingSucceeded(BluetoothDevice device)
 	{
 		String address = device.getAddress();
 		addresses.add(address);
 
 		btInterface.updateDevices(addresses);
 		btInterface.displayMessage("Accepting succeeded, adding address");	
 
 		parent = connections.get(address);	
 	}
 
 	public void broadcastMessage(String message)
 	{
 		Log.d("Bluetooth", "Active connections: " + connections.size());
 		for (Connection con : connections.values())
 		{			
 			con.sendString(message, stringMsgID);
 		}
 	}
 	
 	public boolean connect(BluetoothDevice device)
 	{        
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
 				final String address = device.getAddress();
 
 				Log.i("Bluetooth", "Connected DJECKO");	        			
 
 				if( !sockets.containsKey(address) )
 				{					
 					Connection con = new Connection(btSocket);
 					connections.put(address, con);				
 
 					sockets.put(address, btSocket);				
 					connectingSucceeded(device);
 					return true;
 				}
 			}     			
 		}
 		Log.e("Bluetooth", "Failed to connect.");
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
 						Connection con = new Connection(btSocket);						
 						connections.put(address, con);						
 						sockets.put(address, btSocket);
 
 						acceptingSucceeded(remoteDevice);
 						btInterface.addPairedDevice(address);						
 					}
 					else if( !redundantSockets.containsKey(address)) 
 						redundantSockets.put(address, btSocket);									
 				}
 			}						
 		}
 	}
 
 	private class Connection 
 	{
 		private final BluetoothSocket socket;
 
 		private final InputStream inStream;
 		private final OutputStream outStream;
 
 		private final Thread connectedThread;
 
 		public Connection(BluetoothSocket s)
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
 
 		public void sendString(String s, byte msgID) 
 		{
 			byte[] bytes = null;
 
 			try { bytes = s.getBytes("UTF-8"); } 
 			catch (UnsupportedEncodingException e) { e.printStackTrace(); }
 
 			ByteArrayOutputStream bos = new ByteArrayOutputStream( );
 			try 
 			{
 				bos.write(bytes);
 				bos.write(msgID);
 			}
 			catch (IOException e) { e.printStackTrace(); }			
 
 			bytes = bos.toByteArray();
 
 			try { outStream.write(bytes); }
 			catch (IOException e) { e.printStackTrace(); }
 		}
 
 		public void sendBytes(byte[] buffer)
 		{
 			try { outStream.write(buffer); } 
 			catch (IOException e) { e.printStackTrace(); }
 		}
 
 		public void sendObject(Object obj, byte msgID)
 		{
 			ByteArrayOutputStream bos = new ByteArrayOutputStream();
 			ObjectOutput out = null;
 
 			try 
 			{
 				out = new ObjectOutputStream(bos);
 				out.writeObject(obj);
 				out.writeByte(msgID);
 
 				byte[] bytes = bos.toByteArray();
 				sendBytes(bytes);
 			} 
 			catch (IOException e) { e.printStackTrace(); }
 			finally 
 			{
 				try { out.close(); bos.close(); }
 				catch (IOException e) { e.printStackTrace(); }		  
 			}
 		}
 
 		private class ConnectionThread implements Runnable
 		{
 			private final Map<Byte, Parser> parserMap = new HashMap<Byte, Parser>();
 			
 			public ConnectionThread()
 			{
 				parserMap.put(arrayListMsgID, new Parser()
 				{
 					public void parse(byte[] buffer, int size)
 					{
 						btInterface.displayMessage("Received arrayList");
 		
						ByteArrayInputStream bis = new ByteArrayInputStream(buffer, 0, size);			            	
 						ObjectInput in = null;
 						
 						try 
 						{
 							in = new ObjectInputStream(bis);
 							Object o = in.readObject();			            	  
 							addresses.addAll((ArrayList<String>) o);
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
 		
 						btInterface.updateDevices(addresses);
 						linkBuilding();
 					}
 				});
 				
 				parserMap.put(addressMsgID, new Parser()
 				{
 					public void parse(byte[] buffer, int size)						
 					{
 						btInterface.displayMessage("Received address");
 						String address = null;
 
						try { address = new String(buffer, 0, size, "UTF-8"); }
 						catch (UnsupportedEncodingException e) { e.printStackTrace(); }
 
 						addresses.add(address);            	
 						btInterface.updateDevices(addresses);
 
 						// Send new address to parent if applicable
 						if (parent != null)
 							parent.sendString(address, addressMsgID);            	
 					}
 				});
 				
 				parserMap.put(stringMsgID, new Parser()
 				{
 					public void parse(byte[] buffer, int size)
 					{
 						btInterface.displayMessage("Received string");
 						String text = null;
 
						try { text = new String(buffer, 0, size, "UTF-8"); }
 						catch (UnsupportedEncodingException e) { e.printStackTrace(); }
 
 						btInterface.displayMessage(text);
 					}
 				});
 			}
 			
 						
 			public void run() 
 			{
 				byte[] buffer = new byte[1024];
				int size = 0;
 
 				while( true )
 				{
 					try { size = inStream.read(buffer); } catch (IOException e) { break; }
 
 					if (size > 0)
 					{		           
 						byte id = buffer[size - 1];
						if (parserMap.get(id) != null)
							parserMap.get(id).parse(buffer, size - 1);
 					}
 				}
 			}
 		}	    	    
 	}	
 }
 
 interface Parser
 {
 	public void parse(byte[] buffer, int size);
 }
