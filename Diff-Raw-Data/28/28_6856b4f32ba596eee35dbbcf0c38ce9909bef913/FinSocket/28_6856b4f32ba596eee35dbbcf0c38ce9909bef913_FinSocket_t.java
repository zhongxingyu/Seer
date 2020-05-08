 package br.ufu.facom.network.dlontology;
 import java.util.HashMap;
 import java.util.Map;
 
 import br.ufu.facom.network.dlontology.msg.CustomParser;
 import br.ufu.facom.network.dlontology.msg.Message;
 import br.ufu.facom.network.dlontology.msg.OWLParser;
 
 public class FinSocket {
	public static String interfaceDefault;
 	//Carrega a biblioteca libFinSocket.so
     static {  
     	System.loadLibrary("FinSocket");  
     }
 
 	//Functional
 	private int sock = -1;
 	private boolean promisc = false;
 
 	//Non-Function
 	private boolean registered;
 	private String title;
 	private HashMap<String,Integer> workspaces;
 
 	private int ifIndex = -1;
 	private String ifName = null;
 	
 	//Campos disprezíveis
 	private int dtsVlanDefault=2;
 	//Parser
 	private static OWLParser parser = new CustomParser();
 
 	//Métodos nativos
 	private native int finOpen();
 
 	private native boolean finClose(int sock);
 
 	private native boolean finWrite(int ifIndex, int sock, byte[] data, int offset, int len);
 
 	private native int finRead(int sock, byte[] data, int offset, int len);
 
 	private native boolean setPromiscousMode(String ifName, int sock);
 	
 	private native Map<Integer,String> getNetIfs();
 
     //Interface
 	public boolean close(){
 		return finClose(sock);
 	}
 
 	public boolean open(){
 		this.sock = finOpen();
 		this.workspaces = new HashMap<String,Integer>();
 		
 		Map<Integer,String> ifs = getNetIfs();
 		for(Integer index : ifs.keySet()){
 			String name = ifs.get(index); 
 			if(!name.startsWith("lo")){
				if(interfaceDefault == null || interfaceDefault.equalsIgnoreCase(name)){
					ifIndex = index;
					ifName = name;
					debug("Interface utilizada: "+ifIndex+" - "+ifName);
					break;
				}
 			}
 		}
 		
 		return isOpenned();
 	}
 	
 	private boolean isOpenned() {
 		return this.sock >= 0 && ifIndex >=0;  
 	}
 
 	public boolean write(String destin, byte[] msg){
 		if(workspaces.containsKey(destin) || destin.equals("DTS")){
 			for(Message message : parser.fragmentMessage(this.title,destin.equals("DTS")?dtsVlanDefault:workspaces.get(destin),destin,msg)){
 				if(!write(message))
 					return false;
 			}
 			return true;
 		}else{
 			throw new RuntimeException("Destino invalido!");
 		}
 	}
 	
 	public boolean write(Message message){
 		if(isOpenned()){
 			String destin = message.getDestination();
 			
 			if(workspaces.containsKey(destin) || destin.equals("DTS")){
 				debug("Sending FinFrame:"+message.getSequence());
 				byte[] bytes = parser.parse(message);
 				return finWrite(ifIndex, sock, bytes, 0, bytes.length);
 			}else{
 				throw new RuntimeException("Workspace invalido!");
 			}
 		}else{
 			throw new RuntimeException("FinSocket não aberto!");
 		}
 	}
 
 
 	private int writeBuffer(byte[] buffer, int bufferIndex, byte[] target, int targetIndex, int targetSize) {
 		for(int i=targetIndex; i<targetSize; i++ ){
 			buffer[bufferIndex++] = target[i];
 		}
 		return bufferIndex;
 	}
 	
 	public Message read(){
 		if(isOpenned()){
 			if(!promisc)
 				if(!setPromiscousMode(ifName,sock)){
 					System.err.println("Não foi possível colocar a interface em modo promíscuo.");
 					return null;
 				}else
 					promisc = true;
 	
 			Message msgObj = null;
 			
 			while(true){
 				byte bytes[] = new byte[Message.MAX_FRAME_SIZE];
 				
 				int offset = finRead(sock,bytes,0,Message.MAX_FRAME_SIZE);
 	
 				if(parser.validStartMessage(bytes)){
 					if(parser.validEndMessage(bytes,offset)){ // Message Complete
 						Message currentMsgObj = parser.parseMessage(bytes);
 						
 						if(currentMsgObj != null && workspaces.containsKey(currentMsgObj.getDestination())){
 							if(msgObj != null){
 								debug("Merging message... FinFrame:"+currentMsgObj.getSequence());
 								msgObj.merge(currentMsgObj);
 							}else
 								msgObj = currentMsgObj;
 							
 							if(!msgObj.isFragmented()){
 								debug("Receiving Message... FinFrame:"+msgObj.getSequence());
 								return msgObj;
 							}
 						}else{
 							if(currentMsgObj == null)
 								debug("Parse Fail!");
 							else
 								debug("Workspace fail : " + currentMsgObj.getDestination());
 							currentMsgObj=null;
 						}
 					}else{
 						debug("Incorrect Message!");
 					}
 				}else{ // Não é uma mensagem FinLan
 					debug("Non-Finlan message!");
 				}
 			}
 		}else{
 			throw new RuntimeException("FinSocket não aberto!");
 		}
 	}
 	
 	//DTS Interaction
 	public boolean register(String title){
 		String msg = "<Subscriber rdf:about=\"#Register_"+title+"\"><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/><Entity rdf:resource=\"#"+title+"\"/></Subscriber>";
 		
 		if(write(new Message("","DTS",dtsVlanDefault,msg.getBytes()))){
 			this.title = title;
 			this.registered = true;
 		
 			return true;
 		}else{
 			System.err.println("Não foi possivel enviar mensagem de registro.");
 			return false;
 		}
 	}
 	
 	public boolean unregister(){
 		if(registered){
 			String msg = "<Unsubscriber rdf:about=\"#Unregister_"+title+"\"><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/><Entity rdf:resource=\"#"+title+"\"/></Unsubscriber>";
 		
 			write("DTS",msg.getBytes());
 		
 			this.title = null;
 			this.registered = false;
 		
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean join(String workspace){
 		String msg = "<Join rdf:about=\"#Join_"+title+"_"+workspace+"\"><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/><Entity rdf:resource=\"#"+title+"\"/><Workspace rdf:resource=\"#"+workspace+"\"/></Join>";
 		
 		write("DTS",msg.getBytes());
 
 		//TODO Read result and interpret
 		
 		this.workspaces.put(workspace,dtsVlanDefault);
 		
 		return true;
 	}
 	
 	public boolean disjoin(String workspace){
 		if(this.workspaces.containsKey(workspace)){
 			String msg = "<Disjoin rdf:about=\"#Disjoin_"+title+"_"+workspace+"\"><rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/><Entity rdf:resource=\"#"+title+"\"/><Workspace rdf:resource=\"#"+workspace+"\"/>\n</Disjoin>";
 		
 			write("DTS",msg.getBytes());
 		
 			this.workspaces.remove(workspace);
 		}
 		return true;
 	}
 
 	//Function to print the debug
 	private void debug(String info) {
 		System.out.println(info);
 	}
 	
 } 
