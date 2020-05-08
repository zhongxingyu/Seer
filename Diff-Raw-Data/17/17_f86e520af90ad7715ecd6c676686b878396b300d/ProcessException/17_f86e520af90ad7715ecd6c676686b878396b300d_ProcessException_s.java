 package Process;
 
 @SuppressWarnings("serial")
 public class ProcessException extends RuntimeException{
	public String myMessage;
 	
 	public ProcessException(String input){
		myMessage = input;
	}
	
	public String displayError(){
		return myMessage;
 	}
 }
