 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.exception;
 
 /**
  * Validation exception.
  * 
  * @author ne_Sachirou
  */
 public class ValidateException extends Exception {
	private static final long serialVersionUID = 1L;
 
 	public ValidateException() {
 		super();
 	}
 
 	public ValidateException(String message) {
 		super(message);
 	}

	public ValidateException(Throwable e) {
		super(e);
	}
 }
