 package ch.zhaw.i11b.pwork.sem2.server.messagehandlers;
 
 import ch.zhaw.i11b.pwork.sem2.beans.Message;
 
 /**
  * 
  * @author oups
  *
  */
 public class SMSMessageHandler extends AbstractMessageHandler {
 
 	/**
 	 * 
 	 * @param target
 	 * @param message
 	 */
 	public SMSMessageHandler(String target, Message message) {
 		super(target, message);
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @return boolean
 	 */
 	public boolean send() {
 		// TODO Auto-generated method stub
		return true;
 	}
 
 }
