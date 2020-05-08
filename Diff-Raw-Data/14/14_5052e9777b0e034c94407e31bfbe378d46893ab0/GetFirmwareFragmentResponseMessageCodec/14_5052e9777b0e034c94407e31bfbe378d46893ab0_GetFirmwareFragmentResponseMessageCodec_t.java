 package com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec;
 
 import java.nio.charset.Charset;
 
 import org.apache.mina.core.buffer.IoBuffer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.main.exception.PackageException;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.GetFirmwareFragmentResponseMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ICommand;
 import com.chinarewards.qqgbvpn.main.protocol.socket.ProtocolLengths;
 
 /**
  * Implements the codec for the message get firmware fragment response.
  * <p>
  * XXX add test case for this codec
  * 
  * @author cyril
  * @since 0.1.0
  */
 public class GetFirmwareFragmentResponseMessageCodec implements ICommandCodec {
 
 	// private Logger log = LoggerFactory.getLogger(getClass());
 
 	@Override
 	public ICommand decode(IoBuffer in, Charset charset)
 			throws PackageException {
 
 		throw new UnsupportedOperationException(
 				"Message decoding not supported");
 	}
 
 	/**
 	 * TODO implements encoding function.
 	 */
 	@Override
 	public byte[] encode(ICommand bodyMessage, Charset charset) {
 
 		GetFirmwareFragmentResponseMessage msg = (GetFirmwareFragmentResponseMessage) bodyMessage;
 
 		// prepare buffer
		int bufLength = 0;
		if (msg.getContent() != null) {
			bufLength += msg.getContent().length;
 		}
		
		IoBuffer buf = IoBuffer.allocate(ProtocolLengths.COMMAND
				+ ProtocolLengths.RESULT + bufLength);
 
 		// encode data
 		// command ID
 		buf.putUnsignedInt(msg.getCmdId());
 		// result
 		buf.putShort((short) msg.getResult());
 		// content - optional
		if (msg.getContent() != null && bufLength > 0) {
 			buf.put(msg.getContent());
 		}
 
 		// return result
 		return buf.array();
 	}
 }
