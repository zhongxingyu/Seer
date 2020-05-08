 package com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec;
 
 import java.nio.charset.Charset;
 import org.apache.mina.core.buffer.IoBuffer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import com.chinarewards.qqgbvpn.common.Tools;
 import com.chinarewards.qqgbvpn.main.exception.PackageException;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ICommand;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.InitResponseMessage;
 import com.chinarewards.qqgbvpn.main.protocol.socket.ProtocolLengths;
 
 /**
  * init message coder
  * 
  * @author huangwei
  *
  */
 public class InitMessageResponseCodec implements ICommandCodec {
 	
 	private Logger log = LoggerFactory.getLogger(getClass());
 	
 	/**
 	 * description：mock pos test use it!
 	 * @param bodyMessage
 	 * @param charset
 	 * @return
 	 * @time 2011-9-22   下午07:23:35
 	 * @author Seek
 	 */
 	@Override
 	public ICommand decode(IoBuffer in, Charset charset)
 			throws PackageException {
 		log.debug("init response message decode");
 		InitResponseMessage message = new InitResponseMessage();
 		log.debug("in.remaining()={}", in.remaining());
		if (in.remaining() != ProtocolLengths.COMMAND+ProtocolLengths.RESULT+ProtocolLengths.CHALLENGE) {
 			throw new PackageException(
 					"login packge message body error, body message is :" + in);
 		}
 		long cmdId = in.getUnsignedInt();
 		int result = in.getUnsignedShort();
 		
 		
 		// decode challenge, Pay attention to middle \0
 		byte[] challenge = new byte[ProtocolLengths.CHALLENGE];
 		in.get(challenge);
 		
 		// reconstruct message.
 		message.setCmdId(cmdId);
 		message.setResult(result);
 		message.setChallenge(challenge);
 		
 		log.debug("init message request:cmdId is ({}) , result is ({}), challenge is ({}) ",
 					new Object[]{cmdId, result, challenge});
 		return message;
 	}
 
 	@Override
 	public byte[] encode(ICommand bodyMessage, Charset charset) {
 		log.debug("init response message encode");
 		InitResponseMessage responseMessage = (InitResponseMessage) bodyMessage;
 		long cmdId = responseMessage.getCmdId();
 		int result = responseMessage.getResult();
 		byte[] challeuge = responseMessage.getChallenge();
 
 		byte[] resultByte = new byte[ProtocolLengths.COMMAND+ProtocolLengths.RESULT+ProtocolLengths.CHALLENGE];
 		
 		Tools.putUnsignedInt(resultByte, cmdId, 0);
 		Tools.putUnsignedShort(resultByte, result, ProtocolLengths.COMMAND);
 		Tools.putBytes(resultByte, challeuge, ProtocolLengths.COMMAND+ProtocolLengths.RESULT);
 		
 		return resultByte;
 	}
 
 }
