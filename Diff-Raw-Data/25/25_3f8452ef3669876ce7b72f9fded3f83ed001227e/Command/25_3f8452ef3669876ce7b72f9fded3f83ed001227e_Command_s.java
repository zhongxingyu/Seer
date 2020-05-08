 import java.io.Serializable;
 
 public class Command implements Serializable{
 	private String ip, type, file, port;
 	
 	public Command(String type, String file, String ip, String port){
 		this.ip = ip;
 		this.type = type;
 		this.file = file;
 		this.port = port;
 	}
 	
 	public String getType(){
 		return type;
 	}
 	
 	public String getIP(){
 		return ip;
 	}
 	
 	public String getFile(){
 		return file;
 	}
 	
 	public String getPort(){
 		return port;
 	}
 	
 	public String toString(){
 		return type + " " + file + " " + ip + " " + port;
 	}
 }
