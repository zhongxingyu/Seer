 package com.chinarewards.qqgbvpn.main.protocol.handler;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.main.logic.login.LoginManager;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceHandler;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceRequest;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceResponse;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.CmdConstant;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.InitRequestMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.InitResponseMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.init.InitResult;
 import com.chinarewards.qqgbvpn.main.protocol.socket.ProtocolLengths;
 import com.google.inject.Inject;
 
 /**
  * 
  * 
  * @author Cyril
  * @since 0.1.0
  */
 public class PosRequestFirmwareUpdateHandler implements ServiceHandler {
 	
 	private Logger log = LoggerFactory.getLogger(getClass());
 	
 	@Inject
 	protected LoginManager loginManager;
 	
 	
 	/* (non-Javadoc)
 	 * @see com.chinarewards.qqgbvpn.main.protocol.ServiceHandler#execute(com.chinarewards.qqgbvpn.main.protocol.ServiceRequest, com.chinarewards.qqgbvpn.main.protocol.ServiceResponse)
 	 */
 	@Override
 	public void execute(ServiceRequest request, ServiceResponse response) {
 		
 		InitRequestMessage bodyMessage = (InitRequestMessage)request.getParameter();
 		
		log.debug("PosRequestFirmwareUpdateHandler======execute==bodyMessage=: {}", bodyMessage);
 		
		throw new UnsupportedOperationException();
		
//		InitResponseMessage  initResponseMessage  = null;
//		try {
//			initResponseMessage = loginManager.init(req, newChallenge);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			initResponseMessage = new InitResponseMessage();
//			initResponseMessage
//					.setChallenge(new byte[ProtocolLengths.CHALLENGE]);
//			initResponseMessage.setResult(InitResult.OTHERS.getPosCode());
//		}
//		initResponseMessage.setCmdId(CmdConstant.INIT_CMD_ID_RESPONSE);
//		response.writeResponse(initResponseMessage);
 	}
 
 }
