 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package week4.multicast.messages;
 
 import java.net.InetSocketAddress;
 
 /**
  *
  * @author larss
  */
 public class AcknowledgeMessage extends AbstractLamportMessage {
	private int msgClock;
 	
	public AcknowledgeMessage(InetSocketAddress addr, int clock){
 		super(addr);
		this.msgClock = clock;
 	}
 	
 	public String toString(){
		return String.format("[Acknowledge of message to time %s from %s]", msgClock, getSender());
 	}
 	
 	public int getClock(){
		return msgClock;
 	}
 }
