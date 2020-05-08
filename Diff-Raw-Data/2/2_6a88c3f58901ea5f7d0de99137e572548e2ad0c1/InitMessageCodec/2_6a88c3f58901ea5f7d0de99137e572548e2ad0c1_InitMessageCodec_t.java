 package com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec;
 
 import java.nio.charset.Charset;
 
 import org.apache.mina.core.buffer.IoBuffer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.common.Tools;
 import com.chinarewards.qqgbvpn.main.exception.PackageException;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ICommand;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.InitRequestMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.InitResponseMessage;
 import com.chinarewards.qqgbvpn.main.protocol.socket.ProtocolLengths;
 
 /**
  * init message coder
  * 
  * @author huangwei
  *
  */
 public class InitMessageCodec implements ICommandCodec {
 	
 	private Logger log = LoggerFactory.getLogger(getClass());
 
 	@Override
 	public ICommand decode(IoBuffer in, Charset charset)
 			throws PackageException {
 		log.debug("init message decode");
 		InitRequestMessage message = new InitRequestMessage();
 		log.debug("in.remaining()={}", in.remaining());
 		if (in.remaining() != ProtocolLengths.COMMAND + ProtocolLengths.POS_ID) {
 			throw new PackageException(
 					"login packge message body error, body message is :" + in);
 		}
 		long cmdId = in.getUnsignedInt();
 		
 		// decode POS ID, Pay attention to middle \0
 		byte[] posid = new byte[ProtocolLengths.POS_ID];
 		in.get(posid);
 		int len = 0;
 		for (len = 0; len < posid.length; len++) {
 			if (posid[len] == 0) break;
 		}
 		
 		// reconstruct message.
 		message.setCmdId(cmdId);
		if (len > 0) {
 			message.setPosid(new String(posid, 0, len, charset));
 		}
 		log.debug("init message request:cmdId is ({}) , posid is ({})",new Object[]{cmdId,posid});
 		return message;
 	}
 
 	@Override
 	public byte[] encode(ICommand bodyMessage, Charset charset) {
 		log.debug("init message encode");
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
