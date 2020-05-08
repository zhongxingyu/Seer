 package Server.Network;
 
 import Server.Application.ServerController;
 import NetworkCommunication.ChatMessageToServer;
 import NetworkCommunication.MessageType;
 import NetworkCommunication.NetMessage;
 import NetworkCommunication.SendCompanyResultMessage;
 import NetworkCommunication.SendSupplyMessage;
 
 public class TriggerBusinessLogicThread extends Thread {
 	public TriggerBusinessLogicThread(ClientHandler Sender, NetMessage Message) {
 		super();	
 		message = Message;
 		sender = Sender;
 	}
 	
 	private NetMessage message;
 	private ClientHandler sender;
 	
 	public void run() {
 		switch (message.get_MessageType()) {
 			case MessageType.CHATMASSAGE_TOSERVER: {
 				ServerController.redirectChatMessage(sender, new ChatMessageToServer(message.get_Content()));
 				break;
 			}
 			case MessageType.SEND_SUPPLY : {
 				ServerController.receiveSupply(sender, new SendSupplyMessage(message.get_Content()));
				break;
 			}
 			case MessageType.SEND_COMPANYRESULT:{
 				ServerController.collectResults(sender, new SendCompanyResultMessage(message.get_Content()));
				break;
 			}
 		}
 	}
 	
 }
