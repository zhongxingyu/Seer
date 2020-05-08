 package service.state.impl.mail;
 
 import java.net.Socket;
 
import model.StatusCodes;
 import model.validator.IpValidator;
 
 import service.AbstractSockectService;
 import service.state.State;
 
 public class ValidationState extends State {
 
 	public ValidationState(AbstractSockectService owner) {
 		super(owner);
 	}
 
 	@Override
 	public void exec(String[] params) {
 		IpValidator ipValidator = new IpValidator();
 		Socket serviceSocket = owner.getSocket();
 		String clientIp = serviceSocket.getInetAddress().getHostAddress();
 		logger.info("Checking access for new connection: " + serviceSocket.getInetAddress().getHostAddress());
 		boolean isClientIpBanned = ipValidator.isBanned(clientIp);
		if (isClientIpBanned == true) {
 			logger.info("Client ip: " +  clientIp + " is banned, closing connection...");
			owner.echoLine(StatusCodes.ERR_BANNED_IP, clientIp);
 			owner.setEndOfTransmission(true);
 			return;
 		}
 		owner.getStateMachine().setState(new ParseMailState(owner));
 	}
 
 }
