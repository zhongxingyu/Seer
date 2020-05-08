 package Messages;
 
 
 public class ChatMsgMessage extends Message {
         public String messages=null;
 
 	public static final long minSize=1024;
 	public ChatMsgMessage(int _op,long _length,long _reserved,String _options,byte[] body){
 		super(_op,_length,_reserved,_options);
 		processBody(body);
 		if(op!=11){
 			correct=false;
 		}
 	}
 	public ChatMsgMessage(int _op,long _length,long _reserved,String _options,String _messages){
 		super(_op,_length,_reserved,_options);
 		messages=_messages;
 		if(op!=11){
 			correct=false;
 		}
 	}
 	private void processBody(byte[] body){
 		
 		if(body.length!=1024){
 			correct=false;
 			return;
 		}
 		byte [] messageArray = new byte[1024];
         for (int i = 0; i < body.length && i<1024; i++){
         	messageArray[i] = body[i];
         }
         
 	}
 	public byte[] convert(){
 		byte[] upper=super.convert();
 		byte[] storage=new byte[(int) (upper.length+minSize)];
 		for(int i=0;i<upper.length;i++){
 			storage[i]=upper[i];
 		}
 		
         int total=upper.length;
 
 		byte[] tmp=null;
 		        
         tmp=messages.getBytes();
         for(int i=0;i<tmp.length && i<1024;i++){
 			storage[total+i]=tmp[i];
 		}
                 
 		total += 1024;
                 
 		return storage;
 	}
 }
