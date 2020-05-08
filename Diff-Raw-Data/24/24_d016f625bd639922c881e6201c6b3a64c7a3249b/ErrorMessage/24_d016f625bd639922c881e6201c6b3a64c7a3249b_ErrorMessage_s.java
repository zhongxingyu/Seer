 package Messages;
 
 
 public class ErrorMessage extends Message {
 
 	public ErrorMessage(int _op,long _length,long _reserved,String _options,byte[] body){
 		super(_op,_length,_reserved,_options);
 		if(op!=13){
 			correct=false;
 		}
 	}
 
 }
