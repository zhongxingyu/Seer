 package compilateur;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 public class CompilationError {
 	private List<String> messages;
 	private Set<Error> errors;
 	
 	public enum Error {
 		IDENT_UNKNOWN, TYPE_ITERATION_INCORRECT, TYPE_CONDITION_INCORRECT, NAME_ALREADY_TAKEN,
 		PARAMETER_TYPE_INCORRECT, NUMBER_PARAMETERS_INCORRECT, RETURNED_TYPE_INCORRECT, NO_ERROR,
 		TYPE_AFFECTATION_DONT_MATCH, CODE_ERROR, OPERANDS_DONT_MATCH, INVALID_OPERATION
 	}
 	
 	/**
 	 * Constructor
 	 */
 	public CompilationError() {
 		this.errors = new HashSet<Error>();
 		this.messages = new LinkedList<String>();
 	}
 	
 	/**
 	 * @return True if errors have occurred.
 	 */
 	public boolean errorsOccurred() {
 		return this.errors.size()>0;
 	}
 	
 	/**
 	 * @return A string containing all the error messages.
 	 */
 	public String getErrorMessages() {
 		String messages = "";
 		for(String message: this.messages) {
 			messages += message+"\n\n";
 		}
 		return messages;
 	}
 	
 	/**
 	 * Check that there is only and that it has the "correct" type.
 	 * @param type The type it should have.
 	 * @return True if there is only one error of type type.
 	 */
 	public boolean checkTypeError(Error type) {
		if(this.errors.size()!=1) {
 			return false;
 		}
 		return this.errors.contains(type);
 	}
 	
 	/**
 	 * @return The set of error types.
 	 */
 	public Set<Error> getErrorTypes() {
 		return this.errors;
 	}
 	
 	/**
 	 * Add an error message to the list.
 	 * @param number The error's type.
 	 * @param message The error message.
 	 */
 	public void addError(Error type, String message) {
 		if(type!=Error.NO_ERROR) {
 			this.errors.add(type);
 		}
 		this.messages.add(message);
 	}
 }
