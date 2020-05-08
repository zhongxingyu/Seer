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
	AbstractLamportMessage msg;
 	
	public AcknowledgeMessage(InetSocketAddress addr, AbstractLamportMessage m){
 		super(addr);
		msg = m;
 	}
 	
 	public String toString(){
		return String.format("[Acknowledge of message to time %s from %s]", getClock(), getSender());
 	}
 	
 	public int getClock(){
		return msg.getClock() + msg.getSender().hashCode();
 	}
 }
