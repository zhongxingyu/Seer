 /**
  * 
  */
 package com.chinarewards.qqgbvpn.main.session;
 
 import java.util.Arrays;
 
 import org.apache.mina.core.buffer.IoBuffer;
 
 import com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec.SessionKeyDecoder;
 import com.chinarewards.qqgbvpn.main.session.v1.codec.main.V1SessionKeyCodec;
 
 /**
  * General session key codec. Encode session key to and from network byte
  * format.
  * 
  * @author Cyril
  * @since 0.1.0
  */
 public class SessionKeyCodec implements SessionKeyDecoder {
 
 	/**
 	 * Encode the session key into complete network ready format.
 	 * 
 	 * @param key
 	 * @return
 	 * @throws CodecException
 	 */
 	public byte[] encode(Object key) throws CodecException {
 
 		V1SessionKeyCodec skCodec = new V1SessionKeyCodec();
 		byte[] encoded = skCodec.encode(key);
 
		IoBuffer buf = IoBuffer.allocate(encoded.length + 4);
 
 		// first byte is the version
 		buf.putUnsigned((byte) 0x01);
 
 		// another byte resireved
 		buf.putUnsigned((byte) 0x00);
 
 		// 3rd and 4th bytes are for length
 		buf.putUnsignedShort((int) encoded.length);
 
 		// remaining are for the content
 		buf.put(encoded);
 
 		return buf.array();
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param bytes
 	 * @return
 	 * @throws CodecException
 	 */
 	public Object decode(byte[] bytes) throws CodecException {
 
 		// the first byte is the session key version
 		if (bytes[0] != 0x01) {
 			throw new CodecException("Unsupported session key version "
 					+ bytes[0]);
 		}
 
 		// the 2nd and 3rd bytes is the length.
 		// FIXME implements length check.
 
 		// any bytes left is for the key content
 		V1SessionKeyCodec skCodec = new V1SessionKeyCodec();
 		Object key = skCodec.decode(Arrays.copyOfRange(bytes, 3, bytes.length));
 		return key;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec.SessionKeyDecoder
 	 * #decode(org.apache.mina.core.buffer.IoBuffer)
 	 */
 	public ISessionKey decode(IoBuffer in) throws CodecException {
 
 		short version = in.getUnsigned();
 		int length = in.getUnsignedShort();
 		byte[] content = new byte[length];
 		
 		in.get(content);
 		
 		// make sure we accept the session
 		if (version != 0x01) {
 			throw new CodecException("Unsupported session key version "
 					+ version);
 		}
 
 		V1SessionKeyCodec skCodec = new V1SessionKeyCodec();
 		return skCodec.decode(content);
 	}
 
 }
