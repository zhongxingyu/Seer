 package com.chalcodes.weaponm.network;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chalcodes.weaponm.LogName;
 import com.chalcodes.weaponm.event.EventSupport;
 import com.chalcodes.weaponm.event.EventType;
 
 /**
  * Network sessions update the manager by posting runnables to the UI thread.
  *
  * @author <a href="mailto:kjkrum@gmail.com">Kevin Krumwiede</a>
  */
 public class NetworkManager {
 	static final int BUFFER_SIZE = 8192;
 	private final Logger log = LoggerFactory.getLogger(LogName.forObject(this));
 	private final EventSupport eventSupport;
 	private final ByteBuffer writeBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
 	private State state = State.DISCONNECTED;
 	private Thread networkThread;
 	private SocketChannel channel;
 	private long bytesWritten;
 	private boolean locked;
 
 	public NetworkManager(EventSupport eventSupport) {
 		this.eventSupport = eventSupport;
 	}
 	
 	public void connect(String host, int port) {
 		if(state == State.DISCONNECTED) {
 			setState(State.CONNECTING);
 			networkThread = new Thread(new NetworkSession(this, host, port, eventSupport));
 			networkThread.start();
 		}
 	}
 	
 	public void disconnect() {
 		if(state != State.DISCONNECTED) {
 			networkThread.interrupt();
 		}
 	}
 	
 	public boolean isConnected() {
 		return state == State.CONNECTED;
 	}
 	
 	public boolean isConnecting() {
 		return state == State.CONNECTING;
 	}
 	
 	void setState(State state) {
 		this.state = state;
 		switch(state) {
 		case CONNECTING:
 			eventSupport.dispatchEvent(EventType.NET_CONNECTING);
 			break;
 		case CONNECTED:
 			try {
 				// this convinces TWGS we're a proper Telnet client
 				write("\u00FF\u00FC\u00F6");
 			}
 			catch(IOException e) {
 				log.error("error sending Telnet handshake", e);
 				disconnect();
 				break;
 			}
 			eventSupport.dispatchEvent(EventType.NET_CONNECTED);
 			break;
 		case DISCONNECTED:
 			networkThread = null;
 			channel = null;
 			eventSupport.dispatchEvent(EventType.NET_DISCONNECTED);
 			break;
 		}
 	}
 
 	void setChannel(SocketChannel channel) {
 		this.channel = channel;
 	}
 	
 	static enum State {
 		DISCONNECTED,
 		CONNECTING,
 		CONNECTED
 	}
 	
 	/**
 	 * 
 	 * @param string the text to send
 	 * @throws NetworkLockedException if the network is locked
 	 * @throws IOException if an I/O error occurs
 	 */
 	public void write(String string) throws IOException {
 		if(locked) {
 			throw new NetworkLockedException();
 		}
 		else if(state != State.CONNECTED){
 			throw new IOException("Network is not connected.");
 		}
 		else {
 			int copied = 0;
 			while(copied < string.length()) {
 				while(writeBuffer.hasRemaining() && copied < string.length()) {
 					writeBuffer.put((byte) string.charAt(copied));
 					++copied;
 				}
 				writeBuffer.flip();
 				try {
 					while(writeBuffer.hasRemaining()) {
 						bytesWritten += channel.write(writeBuffer);
 					}
 				}
 				catch(IOException e) {
 					disconnect();
 					throw e;
 				}
 				finally {
 					writeBuffer.clear();
 				}				
 			}
 		}
 	}
 	
 	/**
 	 * Returns the total number of bytes written since the creation of this
 	 * network manager.  Used by event dispatch tasks to determine if anything
 	 * wrote to the network during the current batch of events.
 	 * 
 	 * @return
 	 */
 	long getBytesWritten() {
 		return bytesWritten;
 	}
 	
 	void setLocked(boolean locked) {
 		this.locked = locked;
 	}
 	
 	/**
 	 * Calling {@link #write(String)} while the network is locked will result
 	 * in a {@link NetworkLockedException}.
 	 * 
 	 * @return true if the network is locked; otherwise false
 	 */
 	public boolean isLocked() {
 		return locked;
 	}
 
 }
