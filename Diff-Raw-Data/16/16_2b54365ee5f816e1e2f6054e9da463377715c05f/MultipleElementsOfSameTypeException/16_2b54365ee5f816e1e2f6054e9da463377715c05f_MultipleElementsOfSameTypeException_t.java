 package am.bizis.stspr.exception;
 
 public class MultipleElementsOfSameTypeException extends
 		IllegalArgumentException {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -9084508920387580905L;
 
	private static final String MSG="Element stejneho typu jiz je vlozen do EPO";
 	public MultipleElementsOfSameTypeException() {
		super(MSG);
 	}
 
 	public MultipleElementsOfSameTypeException(String s) {
		super(s+": "+s);
 	}
 
 	public MultipleElementsOfSameTypeException(Throwable cause) {
		super(MSG,cause);
 	}
 
 	public MultipleElementsOfSameTypeException(String message, Throwable cause) {
		super(MSG+": "+message, cause);
 	}
 
 }
