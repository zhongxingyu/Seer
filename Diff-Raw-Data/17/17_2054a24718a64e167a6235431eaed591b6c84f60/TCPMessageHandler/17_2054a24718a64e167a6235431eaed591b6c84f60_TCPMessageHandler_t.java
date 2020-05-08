 package edu.uw.cs.cse461.net.tcpmessagehandler;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.net.base.NetBase;
 import edu.uw.cs.cse461.util.Log;
 
 
 /**
  * Sends/receives a message over an established TCP connection.
  * To be a message means the unit of write/read is demarcated in some way.
  * In this implementation, that's done by prefixing the data with a 4-byte
  * length field.
  * <p>
  * Design note: TCPMessageHandler cannot usefully subclass Socket, but rather must
  * wrap an existing Socket, because servers must use ServerSocket.accept(), which
  * returns a Socket that must then be turned into a TCPMessageHandler.
  *  
  * @author zahorjan
  *
  */
 public class TCPMessageHandler implements TCPMessageHandlerInterface {
 	private static final String TAG="TCPMessageHandler";
 	
 	//--------------------------------------------------------------------------------------
 	// instance varibles
 	//--------------------------------------------------------------------------------------
 	private Socket mySocket;
 	private int myMaxReadLen;
 	private static final int MAX_READ_LEN_DEFAULT = 1000;
 	InputStream myInStream;
 	OutputStream myOutStream;
 	//--------------------------------------------------------------------------------------
 	// helper routines
 	//--------------------------------------------------------------------------------------
 
 	/**
 	 * We need an "on the wire" format for a binary integer.
 	 * This method encodes into that format, which is little endian
 	 * (low order bits of int are in element [0] of byte array, etc.).
 	 * @param i
 	 * @return A byte[4] encoding the integer argument.
 	 */
 	protected static byte[] intToByte(int i) {
 		ByteBuffer b = ByteBuffer.allocate(4);
 		b.order(ByteOrder.LITTLE_ENDIAN);
 		b.putInt(i);
 		byte buf[] = b.array();
 		return buf;
 	}
 	
 	/**
 	 * We need an "on the wire" format for a binary integer.
 	 * This method decodes from that format, which is little endian
 	 * (low order bits of int are in element [0] of byte array, etc.).
 	 * @param buf
 	 * @return 
 	 */
 	protected static int byteToInt(byte buf[]) {
 		// You need to implement this.  It's the inverse of intToByte().
		ByteBuffer b = ByteBuffer.allocate(4);
		b.put(buf);
		b.order(ByteOrder.LITTLE_ENDIAN);
		int i = b.getInt(0);
 		return i;
 	}
 
 	/**
 	 * Constructor, associating this TCPMessageHandler with a connected socket.
 	 * @param sock
 	 * @throws IOException
 	 */
 	public TCPMessageHandler(Socket sock) throws IOException {
 		mySocket = sock;
 		myMaxReadLen = MAX_READ_LEN_DEFAULT;
 		myInStream = mySocket.getInputStream();
 		myOutStream = mySocket.getOutputStream();
 	}
 	
 	/**
 	 * Closes the underlying socket and renders this TCPMessageHandler useless.
 	 */
 	public void close() {
 		try {
 			mySocket.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Set the read timeout on the underlying socket.
 	 * @param timeout Time out, in msec.
 	 * @return The previous time out.
 	 */
 	@Override
 	public int setTimeout(int timeout) throws SocketException {
 		int prevTimeout = mySocket.getSoTimeout();
 		mySocket.setSoTimeout(timeout);
 		return prevTimeout;
 	}
 	
 	/**
 	 * Enable/disable TCPNoDelay on the underlying TCP socket.
 	 * @param value The value to set
 	 * @return The old value
 	 */
 	@Override
 	public boolean setNoDelay(boolean value) throws SocketException {
 		boolean prevNoDelay = mySocket.getTcpNoDelay();
 		mySocket.setTcpNoDelay(value);
 		return prevNoDelay;
 	}
 	
 	/**
 	 * Sets the maximum allowed size for which decoding of a message will be attempted.
 	 * @return The previous setting of the maximum allowed message length.
 	 */
 	@Override
 	public int setMaxReadLength(int maxLen) {
 		int prevMaxReadLen = myMaxReadLen;
 		myMaxReadLen = maxLen;
 		return prevMaxReadLen;
 	}
 
 	/**
 	 * Returns the current setting for the maximum read length
 	 */
 	@Override
 	public int getMaxReadLength() {
 		return myMaxReadLen;
 	}
 	
 	//--------------------------------------------------------------------------------------
 	// send routines
 	//--------------------------------------------------------------------------------------
 	
 	@Override
 	public void sendMessage(byte[] buf) throws IOException {
 		byte[] length = intToByte(buf.length);
		ByteBuffer message = ByteBuffer.allocate(length.length + buf.length);
		message.put(length, 0, length.length);
		message.put(buf, 0, buf.length);
		myOutStream.write(message.array());
 	}
 	
 	/**
 	 * Uses str.getBytes() for conversion.
 	 */
 	@Override
 	public void sendMessage(String str) throws IOException {
 		sendMessage(str.getBytes());
 	}
 
 	/**
 	 * We convert the int to the one the wire format and send as bytes.
 	 */
 	@Override
 	public void sendMessage(int value) throws IOException{
 		sendMessage(intToByte(value));
 	}
 	
 	/**
 	 * Sends JSON string representation of the JSONArray.
 	 */
 	@Override
 	public void sendMessage(JSONArray jsArray) throws IOException {
 		//TODO: not sure if this is what they are asking for
 		sendMessage(jsArray.toString());
 	}
 	
 	/**
 	 * Sends JSON string representation of the JSONObject.
 	 */
 	@Override
 	public void sendMessage(JSONObject jsObject) throws IOException {
 		//TODO: not sure if this is what they are asking for
 		sendMessage(jsObject.toString());
 	}
 	
 	//--------------------------------------------------------------------------------------
 	// read routines
 	//   All of these invert any encoding done by the corresponding send method.
 	//--------------------------------------------------------------------------------------
 	
 	@Override
 	public byte[] readMessageAsBytes() throws IOException {
 		// read in the length of this message
 		int length = readMessageAsInt();
 		
 		// check if the length is short enough
 		if (length > myMaxReadLen) {
 			throw new IOException("Message length too large");
 		}
 		
 		// length is good, read in the message
 		byte[] messageBuf = new byte[length];
 		int bytesToRead = length;
 		while (bytesToRead > 0) {
 			bytesToRead -= myInStream.read(messageBuf, length - bytesToRead, bytesToRead);
 		}
 		return messageBuf;
 	}
 	
 	@Override
 	public String readMessageAsString() throws IOException {
 		return new String(readMessageAsBytes());
 	}
 
 	@Override
 	public int readMessageAsInt() throws IOException {
 		byte[] lengthBuf = new byte[4];
		myInStream.read(lengthBuf, 0, 4);
 		return byteToInt(lengthBuf);
 	}
 	
 	@Override
 	public JSONArray readMessageAsJSONArray() throws IOException, JSONException {
 		return new JSONArray(readMessageAsString());
 	}
 	
 	@Override
 	public JSONObject readMessageAsJSONObject() throws IOException, JSONException {
 		return new JSONObject(readMessageAsString());
 	}
 }
