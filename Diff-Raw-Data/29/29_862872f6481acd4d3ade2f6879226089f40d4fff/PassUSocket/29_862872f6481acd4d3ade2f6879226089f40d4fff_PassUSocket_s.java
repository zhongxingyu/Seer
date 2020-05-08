 package org.secmem232.passu.android.network;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 
 import org.secmem232.passu.android.AR;
 import org.secmem232.passu.android.D;
 import org.secmem232.passu.android.natives.KeyCodeMap;
 import org.secmem232.passu.android.natives.NativeKeyCode;
 import org.secmem232.passu.android.natives.WindowsKeyCode;
 
 import android.graphics.Point;
 import android.util.Log;
 
 public class PassUSocket implements PacketListener {
 	private final String LOG = "PassUSocket";
 
 	private Socket socket;
 	private OutputStream sendStream;
 	private InputStream recvStream;
 
 	private PacketReceiver packetReceiver;	
 
 	private int client_id;
 	// Event listeners
 	private VirtualEventListener mVirtEventListener;
 	private ServerConnectionListener mServerConnectionListener;
 	private AddOptionListener mAddOptionListener;
 
 	private PacketSender packetSender;
 	
 	private Point oldPoint = new Point(0, 0);
 
 	public PassUSocket(ServerConnectionListener listener){
 		if(D.D) Log.w(LOG, "PassUSocket");
 		mServerConnectionListener = listener;
 	}
 
 	/**
 	 * Check whether connected to server or not.
 	 * @return true if connected to server, false otherwise
 	 */
 	public boolean isConnected(){
 		if(D.D) Log.w(LOG, "isConnected");
 		return socket != null ? socket.isConnected() : false;		
 	}
 
 	public void setVirtualEventListener(VirtualEventListener listener){
 		if(D.D) Log.w(LOG, "setVirtualEventListener");
 		mVirtEventListener = listener;
 	}
 
 	public void setAddOptionListener(AddOptionListener listner) {
 		if(D.D) Log.w(LOG, "setAddOptionListener");
 		mAddOptionListener = listner;
 	}
 
 
 	/**
 	 * Connect to specified host.
 	 * @param ipAddr ip address
 	 * @throws IOException
 	 */
 	public synchronized boolean connect(String ip, int port){
 		if(D.D) Log.w(LOG, "connect");
 		try{
 			socket = new Socket();
 			socket.connect(new InetSocketAddress(ip, port), 2000); // Set timeout to 2 seconds
 
 			// Open outputStream
 			sendStream = socket.getOutputStream();
 			packetSender = new PacketSender(sendStream);
 			
 			// Open inputStream
 			recvStream = socket.getInputStream();		
 						
 			// Create and start packet receiver
 			packetReceiver = new PacketReceiver(recvStream);
 			packetReceiver.setPacketListener(this);
 			packetReceiver.start();	
 
 			mServerConnectionListener.onServerConnected(ip, port);
 			
 			Packet packet = new Packet(
 					PacketHeader.Message_Type.CLIENT, 0, 2, 1, 0,
 					127, 0, 0, 1, AR.width, AR.height);
 			sendEcho(packet.toString().getBytes());
 			return true;
 		} catch(IOException e) {
 			e.printStackTrace();
 			mServerConnectionListener.onServerConnectionFailed();
 			
 			return false;
 		}
 	}
 
 	/**
 	 * Disconnect from host.
 	 * @throws IOException
 	 */
 	public void disconnect(){
 		if(D.D) Log.w(LOG, "disconnect");
 		Packet packet = new Packet(PacketHeader.Message_Type.CLIENT, client_id, 0, 0,  1,
 				127, 0, 0, 1, AR.width, AR.height);
 		sendEcho(packet.toString().getBytes());
 		
 		synchronized(this){
 			if(socket != null){
 				try{				
 					recvStream.close();
 					sendStream.close();
 					packetReceiver = null;				
 					socket.close();		
 					socket = null;
 				}catch(IOException e){
 					e.printStackTrace();
 				} finally{
 					mServerConnectionListener.onServerDisconnected();
 				}
 			}
 		}
 	}
 
 	private void cleanup(){
 		if(D.D) Log.w(LOG, "cleanup");
 		Packet packet = new Packet(PacketHeader.Message_Type.CLIENT, client_id, 0, 0, 1,
 				127, 0, 0, 1, AR.width, AR.height);
 		sendEcho(packet.toString().getBytes());
 		
 		synchronized(this){
 			if(socket != null){
 				try{
 					recvStream.close();
 					sendStream.close();
 					packetReceiver = null;
 					socket.close();
 					socket = null;
 
 				}catch(IOException e){
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	//Send screen state(On or Off)
 	public void sendScreenOnOffState(boolean state){
 		if(D.D) Log.w(LOG, "sendScreenOnOffState");
 
 	}
 
 	public void setClipboardText(Packet packet){
 		if(D.D) Log.w(LOG, "setClipboardText");
 		//String str = new String(packet.getPayload(), 0, packet.getHeader().getPayloadLength()).trim();		
 		//mAddOptionListener.setClipBoard(str);
 	}
 
 	//Send notification to Host
 	public void sendEcho(byte[] b){
 		if(D.D) Log.w(LOG, "sendEcho");
 		try{
 			packetSender.send(b);
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onPacketReceived(Packet packet) {
 		if(D.D) Log.w(LOG, "onPacketReceived");
 		Log.w(LOG, packet.toString());
 		if( packet.getHeader().getMessageType() == PacketHeader.Message_Type.KEYBOARD ) {
 			if( packet.getUpdownFlag() == Packet.Updown_Flag.KEYUP ) {
				Integer keyCode = KeyCodeMap.M.get(packet.getKeyCode());
				if(keyCode != null)
					mVirtEventListener.onKeyUp(keyCode);
 			} else if( packet.getUpdownFlag() == Packet.Updown_Flag.KEYDOWN ) {
				Integer keyCode = KeyCodeMap.M.get(packet.getKeyCode());
				if(keyCode != null)
					mVirtEventListener.onKeyDown(keyCode);
 			}
 		} else if( packet.getHeader().getMessageType() == PacketHeader.Message_Type.MOUSE ) {
 			mVirtEventListener.onSetCoordinates(packet.getXCoordinate(), packet.getYCoordinate());
 			AR.getInstance().m_Service.Update(packet.getXCoordinate(), packet.getYCoordinate(), true);
 			
 			if(packet.getXCoordinate() <= 3 ||
 					packet.getYCoordinate() <= 3 ||
 					packet.getXCoordinate() >= AR.width - 3 ||
 					packet.getYCoordinate() >= AR.height - 3) {
 				AR.getInstance().m_Service.HideCursor();
 			} else {
 				AR.getInstance().m_Service.ShowCursor();
 			}
 			
 			if( packet.getLeftRight() == Packet.LeftRight.LEFT ) {
 				if( packet.getUpdownFlag() == Packet.Updown_Flag.UP ) {
 					mVirtEventListener.onTouchUp();
 				} else if( packet.getUpdownFlag() == Packet.Updown_Flag.DOWN ) {
 					mVirtEventListener.onTouchDown();
 				} else {
 					
 				}
 			} else if( packet.getLeftRight() == Packet.LeftRight.RIGHT ) {
 				if( packet.getUpdownFlag() == Packet.Updown_Flag.DOWN ) {
 					mVirtEventListener.onKeyDown(NativeKeyCode.KEY_BACK);
 					mVirtEventListener.onKeyUp(NativeKeyCode.KEY_BACK);
 				} 
 			}
 		} else if( packet.getHeader().getMessageType() == PacketHeader.Message_Type.CLIENT ) {
 		}
 	}
 
 	@Override
 	public void onInterrupt() {
 		if(D.D) Log.w(LOG, "onInterrupt");
 		mServerConnectionListener.onServerConnectionInterrupted();	
 		synchronized(this){
 			if(socket != null){
 				try{					
 					recvStream.close();
 					sendStream.close();
 					packetReceiver = null;
 					socket.close();
 					socket = null;
 				}catch(IOException e){
 					e.printStackTrace();
 				}
 			} 
 		}
 	}
 }
 
