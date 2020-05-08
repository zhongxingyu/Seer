 package Networking;
 
 import java.io.Serializable;
 
 public class Request implements Serializable{
 	private String command;
 	private String target;
 	Object data;
 	
 	public Request(String newCommand, String newTarget, Object newData) {
 		command = newCommand;
 		target = newTarget;
 		data = newData;
 	}
 	
 	public String getCommand() {
 		if(command == null) {
 			return "";
 		}
 		return command;
 	}
 	public String getTarget() {
 		if(target == null) {
 			return "";
 		}
 		return target;
 	}
 	public Object getData() {
 		if(data == null) {
 			return "";
 		}
 		return data;
 	}
	
	public String toString() {
		return "Request with target: " + target + " / command: " + command;
	}
 }
