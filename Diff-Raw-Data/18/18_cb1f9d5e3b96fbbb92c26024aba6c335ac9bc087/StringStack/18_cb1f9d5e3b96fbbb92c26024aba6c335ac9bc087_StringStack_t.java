 
 public class StringStack {
 	private String myString;
 	
 	public StringStack(){
 		myString = "";
 	}
 	
 	public String pop(){
 		int length = myString.length(); 
 		String last = myString.substring(length-1);
		if(length>1){ myString = myString.substring(0, length-1); }
 		else{ myString = ""; }
 		return last; 
 	}
 	
 	public void push(String letter){
 		myString += letter;
 	}
 	
 	public boolean isEmpty(){
 		boolean empty = myString.length()==0;
 		return empty; 
 	}
 	
 	public int length(){
 		return myString.length(); 
 	}
 	
 }
