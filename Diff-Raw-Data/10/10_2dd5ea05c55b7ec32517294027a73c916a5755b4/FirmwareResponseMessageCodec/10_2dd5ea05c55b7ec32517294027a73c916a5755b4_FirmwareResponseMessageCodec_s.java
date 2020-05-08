 package com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec;
 
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import org.apache.mina.core.buffer.IoBuffer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import com.chinarewards.qqgbvpn.main.exception.PackageException;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.CmdConstant;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.FirmwareUpgradeRequestResponseMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ICommand;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ValidateResponseMessage;
 import com.chinarewards.qqgbvpn.main.protocol.socket.ProtocolLengths;
 
 /**
  * firmware request codec
  * <p>
  * 
  * XXX should separate into two codec classes.
  * 
  * @author kmtong
  * @since 0.1.0 2011-0915
  */
 public class FirmwareResponseMessageCodec implements ICommandCodec {
 	
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
 		log.debug("validate response message encode");
 		FirmwareUpgradeRequestResponseMessage message = new FirmwareUpgradeRequestResponseMessage();
 		
 		long cmdId = in.getUnsignedInt();
 		int result = in.getUnsignedShort();
 		long size = in.getUnsignedInt();
 		String firmwareName = null;
 		
 		//获取
 		byte remains[] = new byte[in.capacity()-in.position()];
 		in.get(remains);
 		String remainsStr = new String(remains);
 		
 		int begin = 0;
 		int end = 0;
 		
 		if( (end = remainsStr.indexOf(CmdConstant.SEPARATOR, begin)) != -1){
 			firmwareName = remainsStr.substring(begin, end);
 		}
 		
 		message.setResult(result);
 		message.setSize(size);
 		message.setFirmwareName(firmwareName);
 		
 		log.debug("search message request:cmdId is ({}) , result is ({}), size is ({})" +
 				", firmwareName is ({})", new Object[]{cmdId, result, size, firmwareName});
 		return message;
 	}
 	
 	@Override
 	public byte[] encode(ICommand bodyMessage, Charset charset) {
 		log.debug("FirmwareRequest message encode");
 
 		FirmwareUpgradeRequestResponseMessage responseMessage = (FirmwareUpgradeRequestResponseMessage) bodyMessage;
 
 		IoBuffer buf = IoBuffer.allocate(ProtocolLengths.COMMAND
 				+ ProtocolLengths.RESULT + 4);
 		buf.setAutoExpand(true);
 
 		buf.putUnsignedInt(responseMessage.getCmdId());
 		buf.putShort((short) responseMessage.getResult());
 		buf.putUnsignedInt(responseMessage.getSize());
 
 		try {
			StringBuffer sb = new StringBuffer(
					responseMessage.getFirmwareName());
			buf.putString(sb, charset.newEncoder());
			buf.put((byte)0);
 		} catch (CharacterCodingException e) {
 			e.printStackTrace();
 			buf.put((byte)0);
 		}
 		return Arrays.copyOfRange(buf.array(), 0, buf.position());
 	}
 	
 }
