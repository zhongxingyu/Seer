 import java.io.IOException;
 
 /**
  * Exception that is thrown, if a tape operation couldn't be run successfully.
  * It prints the tape name and the head position in the message string.
  * Use the {@link #getTape()} method to get the tape that caused the exception.
  * @author Nils Breyer
  */
 public class TapeException extends IOException {
 	private static final long serialVersionUID = 1L;
 	private Tape tape = null;
 
 	/**
 	 * Creates a new TapeException based on a tape object
 	 * @param tape The tape that caused the exception
 	 */
 	public TapeException(Tape tape) {
 		super();
 		this.tape = tape;
 	}
 	
 	/**
 	 * Creates a new TapeException based on a tape object and a message
 	 * @param tape The tape that caused the exception
 	 * @param msg A message describing the exception
 	 */
 	public TapeException(Tape tape, String msg) {
		super(tape.getName() + ", position " + tape.getPosition() + ": " + msg);
 		this.tape = tape;
 	}
 	
 	/**
 	 * Creates a new TapeException based on a tape object, a message and an initial cause
 	 * @param tape The tape that caused the exception
 	 * @param msg A message describing the exception
 	 * @param initCause The initial cause that caused the TapeException
 	 */
 	public TapeException(Tape tape, String msg, Throwable initCause) {
 		super(tape.getName() + ", position " + tape.getPosition() + ": " + msg);
 		this.tape = tape;
 		this.initCause(initCause);
 	}
 
 	/**
 	 * Get the tape that caused the exception
 	 * @return The tape that caused the exception
 	 */
 	public Tape getTape() {
 		return this.tape;
 	}
 	
 }
