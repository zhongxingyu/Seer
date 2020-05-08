 /**
  * 
  */
 package com.chinarewards.qqgbvpn.main.protocol.filter;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.mina.core.filterchain.IoFilterAdapter;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.core.write.WriteRequest;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.main.protocol.cmd.CmdConstant;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ErrorBodyMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ICommand;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.InitRequestMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.LoginRequestMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.LoginResponseMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.Message;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.login.LoginResult;
 import com.chinarewards.utils.StringUtil;
 
 /**
  * Login filter.
  * <p>
  * 
  * XXX serious refactoring is required. The implementation of this filter
  * depends on too much about the knowledge of the commands.
  * 
  * @author cream
  * @since 1.0.0 2011-08-29
  */
 public class LoginFilter extends IoFilterAdapter {
 
 	public final static String IS_LOGIN = "is_login";
 	public final static String POS_ID = "pos_id";
 
 	Logger log = LoggerFactory.getLogger(getClass());
 
 	@Override
 	public void messageReceived(NextFilter nextFilter, IoSession session,
 			Object message) throws Exception {
 		
 		log.trace("messageReceived() started");
 		
 		Boolean isLogin = (Boolean) session.getAttribute(IS_LOGIN);
 		
 		// Check whether the command ID is LOGIN
 		Message messageTmp = (Message)message;
 		ICommand msg = messageTmp.getBodyMessage();
 		long cmdId = msg.getCmdId();
 		
 		boolean checkPosIdIsNull = false;	// XXX don't know why need to do this, for old Cream code.
 
 		// if the command requires login, but no sign of login is done, 
 		// return an error package.
 		if (isLoginRequiredForCommand(cmdId)) {
 			
 			if (isLogin == null || !isLogin) {
 				ErrorBodyMessage bodyMessage = new ErrorBodyMessage();
 				bodyMessage.setErrorCode(CmdConstant.ERROR_NO_LOGIN_CODE);
 				messageTmp.setBodyMessage(bodyMessage);
 				session.write(messageTmp);
 				log.debug(
 						"POS client not logged in, cannot perform command ID={}",
 						cmdId);
 				return;
 			}
 			
 		}
 		
 		// special treatment for some command (cyril: I don't know why it is needed);
 		if (cmdId == CmdConstant.INIT_CMD_ID) {
 			// get POS ID
 			InitRequestMessage im = (InitRequestMessage) msg;
 			session.setAttribute(POS_ID, im.getPosId());
 			
 			checkPosIdIsNull = true;
 
 		} else if (cmdId == CmdConstant.LOGIN_CMD_ID
 				|| cmdId == CmdConstant.BIND_CMD_ID) {
 			// get POS ID
 			LoginRequestMessage lm = (LoginRequestMessage) msg;
 			session.setAttribute(POS_ID, lm.getPosId());
 			
 			checkPosIdIsNull = true;
 		}
 
 		if (checkPosIdIsNull) {
 			// Check POS ID for other connection(NOT init or login).
 			String posId = (String) session.getAttribute(POS_ID);
 
 			if (StringUtil.isEmptyString(posId)) {
 				throw new IllegalArgumentException("Pos Id not existed!");
 			}
 		}
 
 		log.trace("messageReceived() done, pass on next filter");
 		
 		// pass the chain when
 		// case 1: cmdId is INIT or LOGIN.
 		// case 2: had login
 		nextFilter.messageReceived(session, messageTmp);
 	}
 	
 	/**
 	 * 
 	 * XXX make it external configurable or via annotation, just don't hard code
 	 * 
 	 * @param cmdId
 	 * @return
 	 */
 	protected boolean isLoginRequiredForCommand(long cmdId) {
 		List<Long> ids = getCmdIdsExcludedFromLogin();
 		if (ids.contains(cmdId)) {
 			return false;
 		}
 		return true;
 	}
 	
 	protected List<Long> getCmdIdsExcludedFromLogin() {
 		Long[] ids = new Long[] {
 				CmdConstant.INIT_CMD_ID,
 				CmdConstant.LOGIN_CMD_ID,
 				CmdConstant.BIND_CMD_ID,
 				CmdConstant.FIRMWARE_UPGRADE_CMD_ID,
 				CmdConstant.GET_FIRMWARE_FRAGMENT_CMD_ID,
 				CmdConstant.FIRMWARE_UP_DONE_CMD_ID };
 		return Arrays.asList(ids);
 	}
 	
 
 	@Override
 	public void messageSent(NextFilter nextFilter, IoSession session,
 			WriteRequest writeRequest) throws Exception {
 		
 		log.trace("messageSent() started");
 		
 		// XXX completely wrong implementation, should be set inside
 		// the command handler.
 		
 		ICommand msg = ((Message) writeRequest.getMessage())
 				.getBodyMessage();
 		long cmdId = msg.getCmdId();
 		if (cmdId == CmdConstant.LOGIN_CMD_ID_RESPONSE
 				|| cmdId == CmdConstant.BIND_CMD_ID_RESPONSE) {
 			session.setAttribute(IS_LOGIN, false);
 
 			LoginResponseMessage lm = (LoginResponseMessage) msg;
 			int result = lm.getResult();
 			if (LoginResult.SUCCESS.getPosCode() == result) {
 				session.setAttribute(IS_LOGIN, true);
 			}
 		}
 		
		// XXX missing nextFilter.messageSent(session, writeRequest); !?!

 		
 		log.trace("messageSent() done");
 	}
 
 }
