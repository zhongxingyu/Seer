 package es.deusto.ingenieria.ssdd.chat.server;
 
 import java.net.DatagramPacket;
 import java.util.ArrayList;
 
 import es.deusto.ingenieria.ssdd.chat.data.Message;
 import es.deusto.ingenieria.ssdd.chat.data.User;
 import es.deusto.ingenieria.ssdd.chat.exceptions.IncorrectMessageException;
 
 public class MessageProccesor implements Runnable {
 
 	private DatagramPacket messageToProcces;
 	private UserList userList;
 	private Message message;
 	
 	public MessageProccesor(DatagramPacket messageToProcces, UserList userList) {
 		this.messageToProcces=messageToProcces;
 		this.userList=userList;
 		this.message=new Message();
 		
 	}
 
 	@Override
 	public void run() {
 		//controlar errores aqui (mensaje desconocido y tal)
 		try {
 			generateMessage();
 			treatMessage();
 			
 		} catch (IncorrectMessageException e) {
 			//SI EL MENSAJE RECIBIDO NO ES CORRECTO ENVIAR AL EMISOR DE ESTE MENSAJE EL 305
 			//TRATAR ESTO !!
 			e.printStackTrace();
 		}
 		//reaccion al mensaje aqui
 
 	}
 	/**
 	 * generates a message from the data of the datagram
 	 * @throws IncorrectMessageException If the message cannot be correctly created.
 	 */
 	private void generateMessage() throws IncorrectMessageException{
 		
 		//obtain all the params from the datagram data
 		String ip=messageToProcces.getAddress().getHostAddress();
 		User userFrom=userList.getUserByIp(ip);
 		User userTo=userList.getUserByIp(ip);
 		String content=new String(messageToProcces.getData());
 		int id=Integer.parseInt(content.substring(0, 3));
 		if(content.length()>3){
 			//also eliminate the first &
 			content=content.substring(3);	
 		}else{
 			content=null;
 		}
 		
 		
 		//obtain the destination user if the message should have it
 		if(Message.hasDestination(id)){
 			try{
 				String nickTo=content.split("&")[0];
 				userTo=userList.getUserByNick(nickTo);
 				if(userTo!=null){
 					content=content.substring(nickTo.length());
 				}
 			}catch(RuntimeException e){
 				throw new IncorrectMessageException("Incorrect message. No destination user found");
 			}
 		}
 		
 		//pass all the params to the message object
 		message.setFrom(userFrom);
 		message.setTo(userTo);
 		message.setText(content);
 		message.setMessageType(id);
 	}
 	/**
 	 * Dispatcher for the different message types
 	 * @throws IncorrectMessageException
 	 */
 	private void treatMessage() throws IncorrectMessageException{
 		
 		switch (message.getMessageType()){
 		case Message.CLIENT_MESSAGE_LOGIN:
 			this.loginMessageTreatment();
 			break;
		case Message.CLIENT_MESSAGE_ESTABLISH_CONNECTION:
			this.establishConnectionTreatment();
			break;
 		default: throw new IncorrectMessageException("The message type code does not exist");
 		}
 			
 	}
 	
 	
 	//Methods for the treatment of the different message types
 	
 	private void loginMessageTreatment() throws IncorrectMessageException{
 		String content=message.getText();
 		if(content!=null && !content.contains("&")){
 			if(this.userList.getUserByNick(content)!=null){
 				//The nick already exist (send 301)
 				String response=new Integer(Message.ERROR_MESSAGE_EXISTING_NICK).toString();
 				sendDatagram(message.getFrom(),response);
 				//Comprobamos el nick pero, Y si ya hay un usuario en esa IP???
 			}else{
 				//Everithing correct, send ACK
 				String response=new Integer(Message.SERVER_MESSAGE_CONNECTED).toString()+this.userList.toString();
 				sendDatagram(message.getFrom(),response);
 			}
 		}else{
 			throw new IncorrectMessageException("The Login message is not correct: '"+content.toString()+"'");
 		}
 	}
	private void establishConnectionTreatment(){
		User to=message.getTo();
		if(to!=null){
			String response=new Integer(Message.SERVER_MESSAGE_INVITATTION).toString()+this.message.getFrom().getNick();
			this.sendDatagram(this.message.getTo(), response);
		}else{
			//User disconnected
			String response=new Integer(Message.ERROR_MESSAGE_USER_IS_DISCONNECTED).toString()+this.userList.toString();
			this.sendDatagram(this.message.getFrom(), response);
		}
	}
 	private void sendDatagram(User destinationUser, String message){
 		//Que este controle lo de la longitud!!!!!
 		
 	}
 }
