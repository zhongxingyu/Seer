 package Source;
 
 public class Variable {
 	public static enum VAR_TYPE {INT, STRING, STRINGLIST};
 	public VAR_TYPE type;
 	public Object value;
 	public Variable(VAR_TYPE type, Object value) {
 		this.type = type;
 		this.value = value;
 	}
 }
