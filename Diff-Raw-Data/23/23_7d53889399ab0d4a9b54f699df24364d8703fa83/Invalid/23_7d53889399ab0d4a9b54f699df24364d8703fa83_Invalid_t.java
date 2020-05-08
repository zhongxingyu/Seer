 package chameleon.core.validation;
 
 import java.util.List;
 
 /**
  * An abstract class representing verification results that mark an element as invalid.
  * 
 * @author Marko van Dooren, Nelis Boucke
  */
 public abstract class Invalid extends VerificationResult {
 
 	public abstract List<BasicProblem> problems();
 
 	/**
 	 * @return The message of this problem.
 	 */
 	public String toString() {
 		return message();
 	}
 
 	/**
 	 * @return The message of this problem.
 	 */
 	public abstract String message();
 }
