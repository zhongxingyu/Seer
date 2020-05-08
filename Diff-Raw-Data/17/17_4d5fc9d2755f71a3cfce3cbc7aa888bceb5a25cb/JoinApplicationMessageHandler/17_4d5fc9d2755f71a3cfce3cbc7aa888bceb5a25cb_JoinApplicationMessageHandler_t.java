 package edu.teco.dnd.module.messages.joinStartApp;
 
 import java.util.UUID;
 
 import edu.teco.dnd.module.ModuleApplicationManager;
 import edu.teco.dnd.network.ConnectionManager;
 import edu.teco.dnd.network.MessageHandler;
 import edu.teco.dnd.network.messages.Response;
 
 public class JoinApplicationMessageHandler implements MessageHandler<JoinApplicationMessage> {
 	final ModuleApplicationManager appManager;
 
 	public JoinApplicationMessageHandler(ModuleApplicationManager appManager) {
 		this.appManager = appManager;
 
 	}
 
 	@Override
 	public Response handleMessage(ConnectionManager connMan, UUID remoteUUID, JoinApplicationMessage message) {
 		try {
			appManager.joinApplication(message.appId, remoteUUID, message.name);
 		} catch (Exception e) {
 			return  new JoinApplicationNak(message.name, message.appId);
 		}
 		return new JoinApplicationAck(message.name, message.appId);
 	}
 
 }
