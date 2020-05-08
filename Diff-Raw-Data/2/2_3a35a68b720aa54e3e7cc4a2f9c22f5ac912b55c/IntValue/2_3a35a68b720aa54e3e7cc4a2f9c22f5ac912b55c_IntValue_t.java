 package syntax;
 
 public class IntValue extends Value{
 	boolean isUndef;
 	int value;
 	public IntValue(Object i) {
 		value =  Integer.parseInt((String)i);
		isUndef = false;
 	}
 	public String toString(){
 		if(isUndef)
 			return "undef";
 		else
 			return String.valueOf(value);
 	}
 	
 	public Object eval() {
 		if (isUndef) return null;
 		else return value;
 	}
 }
