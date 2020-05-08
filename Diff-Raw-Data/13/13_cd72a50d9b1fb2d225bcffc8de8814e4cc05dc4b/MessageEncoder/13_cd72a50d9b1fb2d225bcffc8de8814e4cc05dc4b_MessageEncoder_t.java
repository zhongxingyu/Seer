 package com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec;
 
 import java.nio.charset.Charset;
 import java.util.Arrays;
 
 import org.apache.mina.core.buffer.IoBuffer;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolEncoder;
 import org.apache.mina.filter.codec.ProtocolEncoderOutput;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.common.Tools;
 import com.chinarewards.qqgbvpn.main.protocol.CmdCodecFactory;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ErrorBodyMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.HeadMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ICommand;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.Message;
 import com.chinarewards.qqgbvpn.main.protocol.socket.ProtocolLengths;
 import com.google.inject.Injector;
 
 public class MessageEncoder implements ProtocolEncoder {
 
 	private Logger log = LoggerFactory.getLogger(getClass());
 
 	private Charset charset;
 
 	private Injector injector;
 	
 	protected CmdCodecFactory cmdCodecFactory;
 
 	/**
 	 * XXX can 'injector' be skipped?
 	 * 
 	 * @param charset
 	 * @param injector
 	 * @param cmdCodecFactory
 	 */
 	public MessageEncoder(Charset charset, Injector injector,
 			CmdCodecFactory cmdCodecFactory) {
 		this.charset = charset;
 		this.injector = injector;
 		this.cmdCodecFactory = cmdCodecFactory;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.mina.filter.codec.ProtocolEncoder#dispose(org.apache.mina.
 	 * core.session.IoSession)
 	 */
 	@Override
 	public void dispose(IoSession session) throws Exception {
 		// TODO Auto-generated method stub
 
 		System.out.println("dispose()");
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.mina.filter.codec.ProtocolEncoder#encode(org.apache.mina.core
 	 * .session.IoSession, java.lang.Object,
 	 * org.apache.mina.filter.codec.ProtocolEncoderOutput)
 	 */
 	@Override
 	public void encode(IoSession session, Object message,
 			ProtocolEncoderOutput out) throws Exception {
 		
 		log.debug("encode message start");
 		
 		log.debug("message========:"+message.getClass());
 		
 		Message msg = (Message) message;
 		HeadMessage headMessage = msg.getHeadMessage();
 		ICommand bodyMessage = msg.getBodyMessage();
 
 		long cmdId = bodyMessage.getCmdId();
 		byte[] bodyByte = null;
 		log.debug("cmdId is ({})", cmdId);
 		
 		if(bodyMessage instanceof ErrorBodyMessage){
 			bodyByte = new byte[ProtocolLengths.COMMAND + 4];
 			ErrorBodyMessage errorBodyMessage = (ErrorBodyMessage)bodyMessage;
 			Tools.putUnsignedInt(bodyByte, errorBodyMessage.getCmdId(), 0);
 			Tools.putUnsignedInt(bodyByte, errorBodyMessage.getErrorCode(), ProtocolLengths.COMMAND);
 		}else{
 //				throw new RuntimeException("cmd id is not exits,cmdId is :"+cmdId);
 //			}
 			//Dispatcher
 			//IBodyMessageCoder bodyMessageCoder = injector.getInstance(Key.get(IBodyMessageCoder.class, Names.named(cmdName)));
			
			// XXX handles the case no codec is found for the command ID.
			
 			ICommandCodec bodyMessageCoder = cmdCodecFactory.getCodec(cmdId);
 			log.trace("bodyMessageCoder = {}", bodyMessageCoder);
 			bodyByte = bodyMessageCoder.encode(bodyMessage, charset);
 		}
 
 		log.debug("bodyByte====return====:({})",Arrays.toString(bodyByte));
 		
 		byte[] headByte = new byte[ProtocolLengths.HEAD];
 
 		Tools.putUnsignedInt(headByte, headMessage.getSeq(), 0);
 		Tools.putUnsignedInt(headByte, headMessage.getAck(), 4);
 		Tools.putUnsignedShort(headByte, headMessage.getFlags(), 8);
 		Tools.putUnsignedShort(headByte, 0, 10);
 		Tools.putUnsignedInt(headByte, ProtocolLengths.HEAD + bodyByte.length,
 				12);
 
 		byte[] result = new byte[ProtocolLengths.HEAD + bodyByte.length];
 
 		Tools.putBytes(result, headByte, 0);
 		Tools.putBytes(result, bodyByte, ProtocolLengths.HEAD);
 		
 		int checkSumVal = Tools.checkSum(result, result.length);
 
 		Tools.putUnsignedShort(result, checkSumVal, 10);
 
 		log.debug("checkSumVal=====return===:({})" ,checkSumVal);
 		
 		IoBuffer buf = IoBuffer.allocate(result.length);
 
 		// debug print
 		log.debug("Encoded byte content");
 		CodecUtil.debugRaw(log, result);
 
 		// write to Mina session
 		buf.put(result);
 		buf.flip();
 		out.write(buf);
 		log.debug("encode message end");
 	}
 
 }
