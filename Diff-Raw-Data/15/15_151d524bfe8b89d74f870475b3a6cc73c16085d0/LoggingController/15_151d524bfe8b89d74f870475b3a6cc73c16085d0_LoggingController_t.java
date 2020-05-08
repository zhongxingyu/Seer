 package controller;
 
 import java.io.Serializable;
 
 import javax.faces.bean.ApplicationScoped;
 import javax.faces.bean.ManagedBean;
 
 import rest.ResponseResource;
 import util.ILoggingObserver;
 
 @ManagedBean
 @ApplicationScoped
 public class LoggingController implements ILoggingObserver, Serializable{
 
 	private static final long serialVersionUID = 1L;
 	
 	private String logMessage = "";
 	
 	public LoggingController(){
 		ResponseResource.registerLoggingObserver(this);
 	}
 	
 	public String getLogMessage() {
 		return logMessage;
 	}
 
 	public void setLogMessage(String logMessage) {
 		this.logMessage = logMessage;
 	}
 	
 	@Override
 	public void log(String message) {
		
		if((this.logMessage.split("\n",-1).length) == 50){
			this.logMessage = message;
		} else {
			String messageTampon = this.logMessage;
			this.logMessage = message + "\n" + messageTampon;
		}
 	}
 }
