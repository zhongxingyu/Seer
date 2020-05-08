 package am.bizis.stspr.exception;
 
 public class ConditionException extends IllegalArgumentException {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1992906617538729940L;
	private static String MSG="Nesplnena podminka";
 
 	public ConditionException() {
		super(MSG);
 	}
 
 	public ConditionException(String s) {
		super(MSG+": "+s);
 	}
 
 	public ConditionException(Throwable cause) {
		super(MSG,cause);
 	}
 
 	public ConditionException(String message, Throwable cause) {
		super(MSG+": "+message, cause);
 	}
 
 }
