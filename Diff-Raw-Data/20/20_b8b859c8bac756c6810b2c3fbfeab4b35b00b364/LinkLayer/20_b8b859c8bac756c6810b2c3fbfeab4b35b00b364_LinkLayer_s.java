 package ec.nem.bluenet.net;
 
 
 import java.io.*;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.text.ParseException;
 import java.util.*;
 
 import android.bluetooth.*;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.util.Log;
 import ec.nem.bluenet.CommunicationThread;
 import ec.nem.bluenet.Node;
 import ec.nem.bluenet.NodeFactory;
 import ec.nem.bluenet.NodeListener;
 
 /**
  * Responsible for moving frames between nodes on the network.<br><br>
  * 
  * In our case, this just means managing Bluetooth connectivity.  In the
  * traditional internet, it could mean WiFi, ethernet, etc.
  * 
  * @author Darren White, Matt Mullins
  */
 public class LinkLayer extends Layer {
 	private static final String TAG = "LinkLayer";
 	public static final String NAME = "BlueMesh";
 	public static final java.util.UUID UUID =
 		java.util.UUID.fromString("7b3612de-9166-4262-9f48-1bddf968c423");
 	
 	CommunicationThread mCommThread;
 	AcceptThread mAcceptThread;
 	
 	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 	
 	private Map<String, Handler> mConnectionHandlers;
 	
 	public LinkLayer(CommunicationThread commThread) {
 		super();
 		mCommThread = commThread;
 	}
 
 	@Override
 	public void handleMessageFromAbove(android.os.Message msg) {
 		Segment segment = (Segment) msg.obj;
 		Node node = NodeFactory.factory.fromMacAddress(segment.nextHopMACAddress);
 		
 		try {
 			ByteArrayOutputStream packet = new ByteArrayOutputStream();
 			packet.write(segment.IPHeader.headerFields);
 			packet.write(segment.IPHeader.sourceAddress);
 			packet.write(segment.IPHeader.destinationAddress);
 			packet.write(segment.transportSegment.getRawBytes());
 			
 			LinkFrame frame = new LinkFrame();
 			frame.data = packet.toByteArray();
 			frame.protocol = LinkFrame.PROTOCOL_IP6;
 			
 			byte[] bytes = frame.encapsulate();
 			
 //			mCommThread.showProgress(true);
 			Handler h = connectToNode(node);
 			if(h != null){
 				android.os.Message m = h.obtainMessage();
 				m.obj = bytes;
 				Log.d(TAG, "Sending a message:" + m + "\nTo:" + node  );
 				h.sendMessage(m);
 //				mCommThread.showProgress(false);
 			}
 		} catch (Exception e) {
 			// \TODO: Really? Ignore all exceptions? We should fix this. 
 			e.printStackTrace();
 //			mCommThread.showProgress(false);
 		}
 	}
 	
 	private Handler connectToNode(Node n) {
 		Handler h = mConnectionHandlers.get(n.getAddress());
 		if (h != null) {
 			return h;
 		}
 		
 		BluetoothDevice device;
 		BluetoothSocket socket;
 		try {
 			device = mBluetoothAdapter.getRemoteDevice(n.getAddress());
 			socket = device.createRfcommSocketToServiceRecord(UUID);
 			
 			try {
 				socket.connect();
 			}
 			catch(IOException e) {
 				Log.e(TAG, "connectToNode, hacking: " + e.getMessage());
 				// Try a hack for some broken devices (like ones by HTC) instead:
 				Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
 				socket = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
 				socket.connect(); // If this fails, then we can't connect.
 			}
 
 			ConnectionThread ct = new ConnectionThread(socket);
 			ct.start();
 			return ct.getHandler();
 		}
 		catch(IOException e) {
 			Log.e(TAG, "connectToNode: " + e.getMessage());
 			return null;
 		}
 		catch(NoSuchMethodException e){
 			Log.e(TAG, "connectToNode: " + e.getMessage());
 			return null;
 		}
 		catch(InvocationTargetException e){
 			Log.e(TAG, "connectToNode: " + e.getMessage());
 			return null;
 		}
 		catch(IllegalAccessException e){
 			Log.e(TAG, "connectToNode: " + e.getMessage());
 			return null;
 		}
 	}
 
 	@Override
 	public void handleMessageFromBelow(android.os.Message msg) {}
 
 	@Override
 	public void stopLayer() {
 		if(mAcceptThread != null)
 			mAcceptThread.stopThread();
 		
 		super.stopLayer();
 	}
 	
 	public void run() {
 		mConnectionHandlers = new HashMap<String, Handler>();
 		
 		try {
 			mAcceptThread = new AcceptThread();
 			mAcceptThread.start();
 		}
 		catch(IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public List<Node> getPairedNodes() {
 		Set<BluetoothDevice> paired = mBluetoothAdapter.getBondedDevices();
 		ArrayList<Node> out = new ArrayList<Node>();
 
 		for (BluetoothDevice device : paired) {
 			try {
 				String address = device.getAddress();
 				if(address != null){
 					Node n = NodeFactory.factory.fromMacAddress(address);
 					n.setName(device.getName());
 					n.setDeviceName(device.getName());
 	
 					out.add(n);
 				}
 			}
 			catch(ParseException ex) {
 				// TODO: handle errors, but if Android gives us a bad Bluetooth
 				// address, I'm not sure there's a whole lot I can do about it.
 			}
 		}
 		
 		return out;
 	}
 	
 	public Node getLocalNode() {
 		try {
 			Node n = NodeFactory.factory.fromMacAddress(mBluetoothAdapter.getAddress());
 			n.setName(mBluetoothAdapter.getName());
 			n.setDeviceName(mBluetoothAdapter.getName());
 			
 			return n;
 		}
 		catch(ParseException e) {
 			return null;
 		}
 	}
 
 	private class AcceptThread extends Thread {
 		private BluetoothServerSocket mServerSocket;
 		private boolean running = true;
 
 		public AcceptThread() throws IOException {
 			mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, UUID);
 		}
 
 		public void run() {
 			try {
 				while(running) {
 					BluetoothSocket socket = mServerSocket.accept();
 					if(socket != null) {
 						ConnectionThread thread = new ConnectionThread(socket);
 						thread.start();
 					}
 				}
 			}
 			catch(IOException e) {
 				if(running) {
 					Log.e(TAG, e.getMessage());
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		public void stopThread() {
 			if(running == true) {
 				running = false;
 				// Will cause accept() to throw IOException and quit blocking:
 				try { mServerSocket.close(); } 
 				catch(IOException e) { e.printStackTrace(); }
 			}
 		}
 	}
 
 	private class ConnectionThread extends Thread {
 		private BluetoothSocket mSocket;
 		private Handler mHandler;
 		private HandlerThread mHandlerThread;
 		private String mRemoteAddress;
 
 		private ConnectionThread(BluetoothSocket socket) {
 			mSocket = socket;
 			
 			HandlerThread mHandlerThread = new HandlerThread("connection");
 			mHandlerThread.start();
 			
 			mHandler = new Handler(mHandlerThread.getLooper()) {
 				public void handleMessage(android.os.Message msg) {
 					try {
 						mSocket.getOutputStream().write((byte[])msg.obj);
 					}
 					catch(IOException e) {
 						Log.e(TAG,"Exception:" + e);
 						closeConnection();
 					}
 				}
 			};
 			
 			BluetoothDevice remote = mSocket.getRemoteDevice();
 			mRemoteAddress = remote.getAddress();
 			mConnectionHandlers.put(mRemoteAddress, mHandler);
 			for(NodeListener l:mCommThread.getNodeListeners()){
 				l.onNodeEnter(mRemoteAddress);
 			}
 		}
 		
 		public Handler getHandler() {
 			return mHandler;
 		}
 		
 		public void closeConnection() {
 			try {
 				try { mSocket.getInputStream().close(); }
 				catch(IOException e) { e.printStackTrace(); }
 				
 				try { mSocket.getOutputStream().close(); }
 				catch(IOException e) { e.printStackTrace(); }
 				
 				try { mSocket.close(); }
 				catch(IOException e) { e.printStackTrace(); }
 				
 				if(mHandlerThread != null)
 					mHandlerThread.quit();
 			}
 			catch(NullPointerException e) {
 				e.printStackTrace();
 			}
 			finally{
 				mConnectionHandlers.remove(mRemoteAddress);
 				for(NodeListener l:mCommThread.getNodeListeners()){
 					l.onNodeExit(mRemoteAddress);
 				}
 			}
 		}
 		
 		public void run() {
 			// TODO: this is probably really slow ... I should do something
 			// so that I don't have to read byte-by-byte
 			try {
 				ByteArrayOutputStream os = new ByteArrayOutputStream();
 				InputStream is = mSocket.getInputStream();
 				
 				while(true) {
 					int i = is.read();
 					os.write(i);
					Log.d(TAG, "Connection thread got something..." + i);
 					
 					if (i == 0x7e) {
 						LinkFrame frame = LinkFrame.fromEncapsulated(os.toByteArray());
 						// Make sure it at least has an IP header on it
 						if (frame.bytesRead >= 40 && frame.protocol == LinkFrame.PROTOCOL_IP6) {
 							// TODO: determine the type at runtime
 							Segment s = new Segment(Segment.TYPE_UDP);
 							
 							// pull out IP header
 							os = new ByteArrayOutputStream();
 							os.write(frame.data, 0, 8);
 							s.IPHeader.headerFields = os.toByteArray();
 							
 							// pull out source IP
 							os = new ByteArrayOutputStream();
 							os.write(frame.data, 8, 16);
 							s.IPHeader.sourceAddress = os.toByteArray();
 							
 							// pull out destination IP
 							os = new ByteArrayOutputStream();
 							os.write(frame.data, 24, 16);
 							s.IPHeader.destinationAddress = os.toByteArray();
 							
 							// rest of the data
 							os = new ByteArrayOutputStream();
 							os.write(frame.data, 40, frame.data.length - 40);
 							s.transportSegment.setRawBytes(os.toByteArray());
 							
 							// finally, I think we're ready to send s up the chain
 							android.os.Message m = new android.os.Message();
 							m.obj = s;
 							Log.d(TAG, "Got a message:" + s );
 							hSendAbove.sendMessage(m);
 						}
 						
 						os = new ByteArrayOutputStream();
 					} else if (i == -1) {
 						Log.e(TAG, "Got something that wasn't a message:" + i);
 						break;
 					}
 				}
 			}
 			catch(Exception e) {
 				closeConnection();
 			}
 		}
 	}
 
 	/**
 	 * Helper class to represent a single frame on link
 	 */
 	public static class LinkFrame {
 		public static final short PROTOCOL_IP6 = 0x0057;
 		
 		public byte[] data;
 		public short protocol;
 		public int bytesRead;
 		
 		/**
 		 * Apply PPP encapsulation to the given packet: wraps the packet in
 		 * 0x7e tokens, and uses 0x7d as an escape character.  To escape, XOR
 		 * the byte with 0x20 ... see section 3.1 and 4.2 of RFC 1662.
 		 * 
 		 * Assume the PPP link has been configured with address- and control-
 		 * field compression (i.e. omission) and does *not* use protocol field
 		 * compression (section 6, RFC 1661).
 		 * 
 		 * TODO: this is probably not very 
 		 * 
 		 * @param packet the packet to encapsulate 
 		 * @return the encapsulated and escaped version of it
 		 */
 		public byte[] encapsulate() {
 			// first layer of frame format
 			ByteArrayOutputStream toEscape = new ByteArrayOutputStream();
 			try {
 				toEscape.write((protocol >> 8) & 0xFF);
 				toEscape.write(protocol & 0xFF);
 				toEscape.write(data);
 			} catch (Exception e) {
 			}
 			
 			// escaped frame for sending directly across the wire
 			ByteArrayOutputStream escaped = new ByteArrayOutputStream();
 			
 			// framing
 			escaped.write(0x7e);
 			
 			// bulk data!
 			for (byte i : toEscape.toByteArray()) {
 				if (i != 0x7e && i != 0x7d) {
 					escaped.write(i);
 				} else {
 					escaped.write(0x7d);
 					escaped.write(i ^ 0x20);
 				}
 			}
 			
 			// framing
 			escaped.write(0x7e);
 			
 			return escaped.toByteArray();
 		}
 		
 		/**
 		 * Create a LinkFrame from an encapsulated, serialized frame,
 		 * generally what would be received from a BluetoothSocket.
 		 * 
 		 * @param bytes
 		 * @return parsed copy of bytes, which contains the packet, the
 		 * 		protocol, and the number of bytes that were actually read
 		 * 		from the byte[] passed in.
 		 */
 		public static LinkFrame fromEncapsulated(byte[] bytes) {
 			LinkFrame ret = new LinkFrame();
 			
 			// should I default to "reading" a byte just to make progress
 			// if we get misaligned?
 			ret.bytesRead = 0;
 			ret.protocol = 0;
 			ret.data = null;
 			
 			if (bytes.length < 1) {
 				return ret;
 			}
 			
 			int startReading = 0;
 			
 			if (bytes[0] == 0x7e) {
 				startReading = 1;
 			}
 			
 			// undo the escaping done in "escaped" above
 			ByteArrayOutputStream unescaped = new ByteArrayOutputStream();
 			int bytesRead = 0;
 
 			for (int i = startReading; i < bytes.length; ++i) {
 				if (bytes[i] == 0x7e) {
 					bytesRead = i + 1;
 					break;
 				} else if (bytes[i] == 0x7d) {
 					unescaped.write(bytes[i + 1] ^ 0x20);
 					i++; // skip *two* input characters this loop
 				} else {
 					unescaped.write(bytes[i]);
 				}
 			}
 			
 			if (unescaped.size() < 2) {
 				return ret;
 			} else {
 				byte[] unescapedBytes = unescaped.toByteArray(); 
 				short proto = (short) ((unescapedBytes[0] << 8) | unescapedBytes[1]);
 				
 				ByteArrayOutputStream data = new ByteArrayOutputStream();
 				try {
 					data.write(unescapedBytes, 2, unescapedBytes.length - 2);
 				} catch (Exception e) {
 				}
 				
 				ret.protocol = proto;
 				ret.bytesRead = bytesRead;
 				ret.data = data.toByteArray();
 			}
 			
 			return ret;
 		}
 	}
 }
