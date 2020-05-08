 /**
  * 
  */
 package com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec;
 
 import java.nio.charset.Charset;
 
 import org.apache.mina.core.buffer.IoBuffer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.common.Tools;
 import com.chinarewards.qqgbvpn.main.exception.PackageException;
 import com.chinarewards.qqgbvpn.main.protocol.CmdCodecFactory;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.CmdConstant;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ErrorBodyMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.HeadMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ICommand;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.Message;
 import com.chinarewards.qqgbvpn.main.protocol.socket.ProtocolLengths;
 import com.chinarewards.qqgbvpn.main.session.CodecException;
 import com.chinarewards.qqgbvpn.main.session.ISessionKey;
 import com.chinarewards.qqgbvpn.main.session.SessionKeyCodec;
 
 /**
  * 
  * 
  * @author cyril
  * @since 0.1.0
  */
 public class ProtocolMessageDecoder {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(ProtocolMessageDecoder.class);
 
 	SessionKeyCodec sessionKeyCodec = new SessionKeyCodec();
 	
 	/**
 	 * Stores the result of message decoding.
 	 * 
 	 * @author cyril
 	 * @since 0.1.0
 	 */
 	public static class Result {
 
 		private final boolean moreDataRequired;
 		
 		private final long oweLength;
 
 		private final Object message;
 		
 		private final HeadMessage header;
 
 		/**
 		 * 
 		 * @param moreDataRequired
 		 * @param oweLength
 		 * @param headMessage
 		 * @param message
 		 */
 		public Result(boolean moreDataRequired, long oweLength,
 				HeadMessage headMessage, Object message) {
 			this.moreDataRequired = moreDataRequired;
 			this.oweLength = oweLength;
 			this.message = message;
 			this.header = headMessage;
 		}
 
 		/**
 		 * Returns parsed header, if parsed, no matter the header is correct
 		 * or not.
 		 * 
 		 * @return
 		 */
 		public HeadMessage getHeader() {
 			return header;
 		}
 		
 		/**
 		 * Returns whether more data is required in order to parse the raw
 		 * stream.
 		 * 
 		 * @return <code>true</code> if more input is required,
 		 *         <code>false</code> otherwise.
 		 */
 		public boolean isMoreDataRequired() {
 			return moreDataRequired;
 		}
 
 		/**
 		 * Returns the expected length. The value is valid only if 
 		 * {@link #isMoreDataRequired()} returns <code>true</code>.
 		 * 
 		 * @return the expectedLength
 		 */
 		public long getOweLength() {
 			return oweLength;
 		}
 
 		/**
 		 * The parsed message. This method will return a value if and only if
 		 * {@link #isMoreDataRequired()} returns <code>true</code>. Otherwise
 		 * this method will return <code>false</code>.
 		 * 
 		 * @return the message
 		 */
 		public Object getMessage() {
 			return message;
 		}
 
 	}
 
 	private CmdCodecFactory cmdCodecFactory;
 
 	/**
 	 * Creates an instance of <code>ProtocolMessageDecoder</code>.
 	 * 
 	 * @param cmdCodecFactory
 	 */
 	public ProtocolMessageDecoder(CmdCodecFactory cmdCodecFactory) {
 		this.cmdCodecFactory = cmdCodecFactory;
 	}
 
 	/**
 	 * 
 	 * @param in
 	 * @param charset
 	 */
 	public Result decode(IoBuffer in, Charset charset) {
 
 		log.trace("IoBuffer remaining bytes: {}, current position: {}",
 				in.remaining(), in.position());
 
 		// check length, it must greater than header length
 		if (in.remaining() >= ProtocolLengths.HEAD) {
 			
 			boolean sessionKeyDecoded = false;
 			long cmdBodySize = 0;
 			
 			int start = in.position();
 			
 			// parse message header
 			log.trace("Parsing message header ({} bytes)..",
 					ProtocolLengths.HEAD);
 			PackageHeadCodec headCodec = new PackageHeadCodec();
 			HeadMessage header = headCodec.decode(in);	// parse
 			log.trace("- message header: {}", header);
 			log.trace("- IoBuffer remaining (for body): {}", in.remaining());
 
 			// the pure command size
 			// if msg size is 100, header is 16, then cmdBodySize = 84.
 			cmdBodySize = header.getMessageSize() - ProtocolLengths.HEAD; 
 			
 			/***** field extension *****/
 			
 			// act according to the flag.. must be in proper order!!!
 
 			// flag: session key
 			if ((header.getFlags() & HeadMessage.FLAG_SESSION_ID) != 0) {
 				
 				log.debug("flag FLAG_SESSION_ID is set in header");
 				log.debug("in.remaining() = {}", in.remaining());
 				
 				// make sure we have enough data to decode
 				if (in.remaining() >= 4) {
 					
 					// 1 byte of header
 					short sessionKeyVersion = in.getUnsigned();
 					
 					// 1 byte: reserved.
 					in.getUnsigned();
 					
 					// 2 byte of length
 					int sessionKeyLength = in.getUnsignedShort();
 					
 					log.debug("sessionKeyVersion={}", sessionKeyVersion);
 					log.debug("sessionKeyLength={}", sessionKeyLength);
 					
 					// update the command body size
 					// if cmdBodySize = 84, session key length = 10, then
 					// cmdBodySize = 84 - 1 - 2 - 10 = 71
 					cmdBodySize -= (sessionKeyLength + 4);
 					
 					if (sessionKeyVersion == 0 && sessionKeyLength == 0) {
 						// special - it means the client recognize the 
 						// session ID bit.
 						
 						// the 4 bytes are consumed.
 						log.debug("version 0 session key detected, client can process session ID!");
 						
 					} else if (in.remaining() < sessionKeyLength) {
 						// not enough data. reset to original position.
 						long owe = header.getMessageSize()
 								- ProtocolLengths.HEAD - 4 - in.remaining();
 						in.position(start);	// restore the original position.
 						return new Result(true, owe, header, null);
 					} else {
 						// enough data to decode session key!
 						// decode the session key.
 						// FIXME distinguish between different exception.
 						try {
 							log.debug(
 									"decoding session key (version: {}, key length: {})",
 									sessionKeyVersion, sessionKeyLength);
 							in.position(start + 3);
 							// decode the session key.
 							ISessionKey sessionKey = sessionKeyCodec.decode(in);
 							header.setSessionKey(sessionKey);
 							log.debug("session key decoded: {}", sessionKey);
 							
 							// session key successfully decoded.
 							sessionKeyDecoded = true;
 							
 						} catch (CodecException e) {
 							log.warn(
 									"error when decoding session key (version: {}, key length: {})",
 									new Object[] { sessionKeyVersion,
 											sessionKeyLength }, e);
 						}
 					}
 				} else {
 					// we don't have enough information to decode header.
 					long owe = header.getMessageSize()
 							- ProtocolLengths.HEAD - in.remaining();
 					in.position(start);	// restore the original position.
 					return new Result(true, owe, header, null);
 				}
 			}
 			/***** field extension *****/
 			
 			log.debug("before parsing body: cmdBodySize={}, in.remaining={}", cmdBodySize, in.remaining());
 			
 			// make sure we have enough data to feed.
 			if (in.remaining() < cmdBodySize) {
 				// more data is required.
 				long owe = cmdBodySize - in.remaining();
 				log.trace("More bytes are required to parse the complete message (still need {} bytes)",
 						owe);
 				in.position(start);	// restore the original position.
 				return new Result(true, owe, header, null);
 			}
 
 			//
 			// validate the checksum.
 			//
 			
 			// byteTmp: raw bytes
 			int calculatedChecksum = 0;
 			{
 				// reset to original position;
 				in.position(start);
 				byte[] byteTmp = new byte[(int)header.getMessageSize()];	// XXX possible truncation
 				// important! just consume the required number of bytes
 				in.get(byteTmp);
 //				CodecUtil.debugRaw(log, byteTmp);	// some debug output
 				Tools.putUnsignedShort(byteTmp, 0, 10);
 
 				// calculate the checksum
 				calculatedChecksum = Tools.checkSum(byteTmp, byteTmp.length);
 			}
 
 			// validate the checksum. if not correct, return an error response.
 			if (calculatedChecksum != header.getChecksum()) {
 				ErrorBodyMessage bodyMessage = new ErrorBodyMessage();
 				bodyMessage.setErrorCode(CmdConstant.ERROR_CHECKSUM_CODE);
 				Message message = new Message(header, bodyMessage);
 				log.trace(
 						"Received message has invalid checksum (calc: 0x{}, actual: 0x{})",
 						Integer.toHexString(calculatedChecksum),
 						Integer.toHexString(header.getChecksum()));
 				return new Result(false, 0, header, message);
 			}
 
 			// really decode the command message.
			in.position(start + ProtocolLengths.HEAD);
 			ICommand bodyMessage = null;
 			try {
 				bodyMessage = this.decodeMessageBody(in, charset, header);
 				// a message is completely decoded.
 				Message message = new Message(header, bodyMessage);
 				return new Result(false, 0, header, message);
 			} catch (Throwable e) {
 				log.trace("Unexpected error when decoding message", e);
 				ErrorBodyMessage errorBodyMessage = new ErrorBodyMessage();
 				errorBodyMessage.setErrorCode(CmdConstant.ERROR_MESSAGE_CODE);
 				Message message = new Message(header, errorBodyMessage);
 				return new Result(false, 0, header, message);
 			}
 
 		} else {
 			
 			long owe = ProtocolLengths.HEAD - in.remaining();
 			log.trace("More bytes are required to parse the header message (still need {} bytes)",
 					owe);
 			return new Result(true, owe, null, null);
 			
 		}
 
 	}
 
 	protected ICommand decodeMessageBody(IoBuffer in, Charset charset,
 			HeadMessage header) throws PackageException {
 
 		// get cmdId and process it
 		int position = in.position();
 		long cmdId = in.getUnsignedInt();
 		in.position(position);
 		log.debug("cmdId: {}", cmdId);
 
 		// get the message codec for this command ID.
 		ICommandCodec bodyMessageCoder = cmdCodecFactory.getCodec(cmdId);
 		log.trace("Command codec for command ID {}: {}", cmdId,
 				bodyMessageCoder);
 		if (bodyMessageCoder != null) {
 			// codec is found, decode it
 			return bodyMessageCoder.decode(in, charset);
 		} else {
 			// no codec is found:
 			// 1. consume all bytes in the buffer.
 			long toConsume = header.getMessageSize() - ProtocolLengths.HEAD;
 			if (toConsume > 0 && in.remaining() > 0) {
 				long actualConsume = toConsume > in.remaining() ?
 						in.remaining() : toConsume;
 				in.skip((int)actualConsume);	// XXX potential problem
 			}
 			// 2. report invalid command ID.
 			ErrorBodyMessage bodyMessage = new ErrorBodyMessage();
 			bodyMessage.setErrorCode(CmdConstant.ERROR_INVALID_CMD_ID);
 			return bodyMessage;
 		}
 	}
 
 }
