 package semantic;
 
 @SuppressWarnings("serial")
public class TypeError extends MiniTError {

	public TypeError(String message, int line) {
		super(message, line);
		// Auto-generated constructor stub
	}
 
	public TypeError(String message) {
		super(message, -1);
	}
}
