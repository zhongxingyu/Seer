 package ch.zhaw.i11b.pwork.sem2.server.messagehandlers;
 
 import ch.zhaw.i11b.pwork.sem2.beans.Message;
 
 /**
  * @author oups
  *
  */
 public class MMSMessageHandler extends AbstractMessageHandler {
 
 	/**
 	 * @param target
 	 * @param message
 	 */
 	public MMSMessageHandler(String target, Message message) {
 		super(target, message);
 		// TODO Auto-generated constructor stub
 	}
 
 	/* (non-Javadoc)
 	 * @see ch.zhaw.i11b.pwork.sem2.server.messagehandlers.AbstractMessageHandler#send()
 	 */
 	public boolean send() {
 		// TODO Auto-generated method stub
		return true;
 	}
 
 }
