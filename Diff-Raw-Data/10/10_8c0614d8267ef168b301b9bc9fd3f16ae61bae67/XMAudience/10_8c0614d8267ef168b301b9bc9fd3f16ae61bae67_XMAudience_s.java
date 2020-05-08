 package org.haibara.autoxm;
 
 import io.socket.IOAcknowledge;
 import io.socket.SocketIOException;
 
 import java.util.Map;
 
 import org.json.JSONObject;
 
 public class XMAudience extends XMRobot{
 	
 	private boolean initialized = false;
 	
 	public XMAudience(String id, String pw, Map<String, String> taskMap) {
 		super(id, pw, taskMap);		
 	}
 	
 	public void MessageHandler(JSONObject json, IOAcknowledge ack) {
 	}
 	public void MessageHandler(String data, IOAcknowledge ack) {
 		super.MessageHandler(data, ack);
 	}
 	public void ErrorHandler(SocketIOException socketIOException) {
 		super.ErrorHandler(socketIOException);
 	}
 	
 	public void DisconnectHandler() {
 		atRoom = false;
 	}
 	public void ConnectHandler() {
 	}
 	
 	public void EventHandler(String event, IOAcknowledge ack, Object... args) {
 		super.EventHandler(event, ack, args);
 		if (!initialized) {
 			atRoom = true;
			DEBUG("I am here "+this.uid+" rate:"+this.rate+" period:"+this.ratePeriod);
 			if (this.rate == 0 || this.ratePeriod < 30) {
 				this.rateSignal = false;
 			} else {
 				this.rateSignal = true;
 				this.startRateTask();
 			}
 			if (this.chatPeriod < 5 || this.chatMessage == null || this.chatMessage.length() == 0) {
 				this.chatSignal = false;
 			} else {
 				this.chatSignal = true;
 				this.startChatTask();
 			}
 			initialized = true;
 		}
 	}
 	
 	public XMDJ extendToDJ() {
 		return null;
 	}
 	
 	public void stopWork() {
 		if (this.chatThread != null) {
 			this.chatThread.stopWork();
 		}
 		if (this.rateThread != null) {
 			this.rateThread.stopWork();
 		}
 	}
 }
