 package ar.com.datatsunami.bigdata.cobol;
 
 import ar.com.datatsunami.bigdata.cobol.field.Field;
 
 /**
  * Represents some problem when parsing the line.
  * 
  * @author Horacio G. de Oro
  * 
  */
 public class ParserException extends Exception {
 
 	private static final long serialVersionUID = 5088113420235418928L;
 
	public Field<?, ?> field = null;
 	public String value = null;
 
 	public ParserException() {
 		super();
 	}
 
 	public ParserException(String message) {
 		super(message);
 	}
 
 	public ParserException(String message, Throwable cause) {
 		super(message, cause);
 	}
 
 	public ParserException(Throwable cause) {
 		super(cause);
 	}
 
	public ParserException(String message, Throwable cause, Field<?, ?> field, String value) {
 		super(message, cause);
 		this.field = field;
 		this.value = value;
 	}
 
	@Override
	public String toString() {
		return super.toString() + " - Field: '" + this.field + "' - Value: '" + this.value + "'";
	}

 }
