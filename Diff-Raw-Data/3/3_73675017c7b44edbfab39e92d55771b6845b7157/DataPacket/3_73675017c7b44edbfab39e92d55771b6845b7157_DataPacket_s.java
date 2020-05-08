 package com.labs.rpc;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import com.labs.rpc.util.RemoteException;
 
 /**
  * Base for all transport packets
  * @author Benjamin Dezile
  */
 public class DataPacket {
 
 	protected static final String NULL = "null";					// Null value
 	protected static final int HEADER_SIZE = 21;					// Size of the header
 	
 	protected static final byte FORMAT_NULL = 0x40;					// Null
 	protected static final byte FORMAT_BOOL = 0x41;					// Boolean
 	protected static final byte FORMAT_BYTE = 0x42;					// Byte (0-255)
 	protected static final byte FORMAT_CHAR = 0x43;					// Character
 	protected static final byte FORMAT_SHORT = 0x44;				// Short integer
 	protected static final byte FORMAT_INT = 0x45;					// Integer
 	protected static final byte FORMAT_FLOAT = 0x46;				// Float
 	protected static final byte FORMAT_DOUBLE = 0x47;				// Double
 	protected static final byte FORMAT_LONG = 0x48;					// Long
 	protected static final byte FORMAT_STRING = 0x49;				// String
 	protected static final byte FORMAT_ARRAY = 0x50;				// Array of objects
 	protected static final byte FORMAT_LIST = 0x51;					// List of objects
 	protected static final byte FORMAT_JSON = 0x52;					// JSON object
 	protected static final byte FORMAT_JSON_ARRAY = 0x53;			// JSON array
 	protected static final byte FORMAT_REMOTE_EX = 0x54;			// Remote exception
 	
 	private static Long seqCounter = 0L;							// Sequence counter
 	protected byte type;											// Packet type
 	protected long seq;												// Sequence number
 	protected long time;											// Creation timestamp
 	protected byte[] payload;										// Encapsulated data
 		
 	/**
 	 * Create a new data packet
 	 * @param t byte - Packet type
 	 */
 	public DataPacket(byte t) {
 		this(t, getNextSeq());
 	}
 	
 	/**
 	 * Create an empty data packet
 	 */
 	protected DataPacket() {
 		this((byte)0,0);
 	}
 	
 	/**
 	 * Create a new data packet
 	 * @param seqNum long - Sequence number
 	 * @param t byte - Packet type
 	 */
 	protected DataPacket(byte t, long seqNum) {
 		type = t;
 		seq = seqNum;
 		time = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Get the next sequence number
 	 * @return long
 	 */
 	private static final long getNextSeq() {
 		synchronized(seqCounter) {
 			return ++seqCounter;
 		}
 	}
 	
 	/**
 	 * Set the payload bytes
 	 * @param data byte[] - Payload
 	 */
 	public void setPayload(byte[] data) {
 		payload = data;
 	}
 	
 	/**
 	 * Get the payload bytes
 	 * @return byte[]
 	 */
 	public byte[] getPayload() {
 		return payload;
 	}
 	
 	/**
 	 * Get the associated sequence number
 	 * @return long
 	 */
 	public long getSeq() {
 		return seq;
 	}
 	
 	/**
 	 * Get the associated timestamp
 	 * @return long
 	 */
 	public long getTime() {
 		return time;
 	}
 	
 	/**
 	 * Get the packet type
 	 * @return byte
 	 */
 	public byte getType() {
 		return type;
 	}
 	
 	/**
 	 * Pack an object so that it can be transported
 	 * @param arg {@link Object} - Argument
 	 * @return byte[]
 	 */
 	@SuppressWarnings("unchecked")
 	protected static byte[] packObject(Object arg) {
 		ByteBuffer buf;
 		byte type;
 		String data;
 		if (arg == null) {
 			type = FORMAT_NULL;
 			data = NULL;
 		} else if (arg instanceof Byte) {
 			type = FORMAT_BYTE;
 			data = arg.toString();
 		} else if (arg instanceof Character) {
 			type = FORMAT_CHAR;
 			data = arg.toString();
 		} else if (arg instanceof Short) {
 			type = FORMAT_SHORT;
 			data = arg.toString();
 		} else if (arg instanceof Boolean) {
 			type = FORMAT_BOOL;
 			data = arg.toString();
 		} else if (arg instanceof Integer) {
 			type = FORMAT_INT;
 			data = arg.toString();
 		} else if (arg instanceof Float) {
 			type = FORMAT_FLOAT;
 			data = arg.toString();
 		} else if (arg instanceof Double) {
 			type = FORMAT_DOUBLE;
 			data = arg.toString();
 		} else if (arg instanceof Long) {
 			type = FORMAT_LONG;
 			data = arg.toString();
 		} else if (arg instanceof String) {
 			type = FORMAT_STRING;
 			data = arg.toString();
 		} else if (arg.getClass().isArray()) {
 			JSONArray a = new JSONArray();
 			for (Object o:(Object[])arg) {
 				a.put(o);
 			}
 			type = FORMAT_ARRAY;
 			data = a.toString();
 		} else if (arg instanceof ArrayList<?>) {
 			JSONArray a = new JSONArray();
 			for (Object o:(ArrayList)arg) {
 				a.put(o);
 			}
 			type = FORMAT_LIST;
 			data = a.toString();		
 		} else if (arg instanceof JSONObject) {
 			type = FORMAT_JSON;
 			data = arg.toString();
 		} else if (arg instanceof JSONArray) {
 			type = FORMAT_JSON_ARRAY;
 			data = arg.toString();
 		} else if (arg instanceof Exception) {
 			type = FORMAT_REMOTE_EX;
 			RemoteException re = (RemoteException)arg; 
 			data = re.getMessage();
 		} else {
 			throw new IllegalArgumentException("Unsupported data type for " + arg);
 		}
 		byte[] bytes = data.getBytes();
 		buf = ByteBuffer.allocate(1 + bytes.length);
 		buf.put(type);
 		buf.put(bytes);
 		return buf.array();
 	}
 
 	/**
 	 * Unpack an object
 	 * @param argData {@link String} - Data
 	 * @return {@link Object}
 	 */
 	protected static Object unpackObject(String argData) throws Exception {
 		ByteBuffer buf = ByteBuffer.wrap(argData.substring(0,1).getBytes());
 		byte type = buf.get(0);
 		String data = argData.substring(1);
 		if (type == FORMAT_NULL) {
 			return null;
 		} else if (type == FORMAT_BOOL) {
 			return "true".equals(data);
 		} else if (type == FORMAT_BYTE) {
 			return Byte.parseByte(data);
 		} else if (type == FORMAT_CHAR) {
 			return data.charAt(0);
 		} else if (type == FORMAT_SHORT) {
 			return Short.parseShort(data);
 		} else if (type == FORMAT_INT) {
 			return Integer.parseInt(data);
 		} else if (type == FORMAT_FLOAT) {
 			return Float.parseFloat(data);
 		} else if (type == FORMAT_DOUBLE) {
 			return Double.parseDouble(data);
 		} else if (type == FORMAT_LONG) {
 			return Long.parseLong(data);
 		} else if (type == FORMAT_STRING) { 
 			return data;
 		} else if (type == FORMAT_ARRAY) {
 			JSONArray a = new JSONArray(new JSONTokener((String)data));
 			Object[] array = new Object[a.length()];
 			for (int i=0;i<a.length();i++) {
 				array[i] = a.get(i);
 			}
 			return array;			
 		} else if (type == FORMAT_LIST) {
 			JSONArray a = new JSONArray(new JSONTokener((String)data));
 			List<Object> l = new ArrayList<Object>(a.length());
 			for (int i=0;i<a.length();i++) {
 				l.add(a.get(i));
 			}
 			return l;
 		} else if (type == FORMAT_JSON) {
 			return new JSONObject(new JSONTokener((String)data));
 		} else if (type == FORMAT_JSON_ARRAY) {
 			return new JSONArray(new JSONTokener((String)data));
 		} else if (type == FORMAT_REMOTE_EX) {
 			return new RemoteException(data);
 		} else {
 			throw new IllegalArgumentException("Invalid data type: " + type);
 		}
 	}
 
 	/**
 	 * Make the header bytes
 	 * @param pl int - Payload size 
 	 * @return byte[]
 	 */
 	protected byte[] makeHeaderBytes(int pl) {
 		ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
 		header.put(type);
 		header.putInt(pl);
		header.putLong(seq);
 		header.putLong(time);
 		return header.array();
 	}
 	
 	/**
 	 * Make the packet bytes
 	 * @param header byte[] - Header bytes
 	 * @param payload byte[] - Payload bytes
 	 * @return byte[]
 	 */
 	protected byte[] makePacketBytes(byte[] header, byte[] payload) {
 		ByteBuffer data = ByteBuffer.allocate(HEADER_SIZE + payload.length);
 		data.put(header);
 		data.put(payload);
 		return data.array();
 	}
 		
 	/**
 	 * Get the packet bytes to send over
 	 * @return byte[]
 	 */
 	public byte[] getBytes() {
 		byte[] header = makeHeaderBytes(payload.length);
 		return makePacketBytes(header, payload);
 	}
 	
 	/**
 	 * Build a new packet object from raw data
 	 * @param bytes byte[] - Bytes
 	 * @return {@link DataPacket}
 	 */
 	public static DataPacket fromBytes(byte[] bytes) {
 		ByteBuffer buf = ByteBuffer.wrap(bytes);
 		DataPacket dp = new DataPacket();
 		dp.type = buf.get(0);
 		dp.seq = buf.getLong(5);
 		dp.time = buf.getLong(13);
 		int l = buf.getInt(1);
 		dp.payload = Arrays.copyOfRange(bytes, HEADER_SIZE, HEADER_SIZE + l);
 		return dp;
 	}
 	
 	/**
 	 * Read a new packet object from a byte stream
 	 * @param in {@link InputStream} - Input stream
 	 * @return {@link DataPacket}
 	 * @throws IOException
 	 */
 	public static DataPacket fromStream(InputStream in) throws IOException {
 		byte[] headerBytes = new byte[HEADER_SIZE];
 		int b,n = 0;
 		while (n < HEADER_SIZE) {
 			if ((b=in.read(headerBytes, n, HEADER_SIZE - n)) < 0) {
 				throw new IOException("Connection closed");
 			}
 			n += b;
 		}
 		DataPacket dp = new DataPacket();
 		ByteBuffer header = ByteBuffer.wrap(headerBytes);
 		dp.type = header.get(0);
 		dp.time = header.getLong(5);
 		dp.seq = header.getLong(13);
 		int l = header.getInt(1);
 		dp.payload = new byte[l];
 		n = 0;
 		while (n < l) {
 			if ((b=in.read(dp.payload, n, l - n)) < 0) {
 				throw new IOException("Connection closed");
 			}
 			n += b;
 		}
 		return dp;
 	}
 
 	/**
 	 * Assert that the given object is the same as this packet
 	 */
 	public boolean equals(Object o) {
 		if (this == o) {
 			return true;
 		}
 		DataPacket dp = (DataPacket)o;
 		if (type != dp.type) {
 			return false;
 		}
 		if (time != dp.time) {
 			return false;
 		}
 		if (seq != dp.seq) {
 			return false;
 		}
 		if (!Arrays.equals(payload, dp.payload)) {
 			return false;
 		}
 		return true;
 	}
 	
 }
