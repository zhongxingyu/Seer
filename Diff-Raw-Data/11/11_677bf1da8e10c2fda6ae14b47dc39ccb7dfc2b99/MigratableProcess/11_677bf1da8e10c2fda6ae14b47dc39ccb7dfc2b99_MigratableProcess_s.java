 package Interface;
 
 import java.io.Serializable;
 
 /**
  * 
 *
  */
 public interface MigratableProcess extends Runnable, Serializable {
 	
 	/**
	 * suspend()
	 * 
	 * Called just before the MigratableProcess is serialized to allow
	 * the process to enter a known, safe state.
 	 */
 	void suspend();
 	
 	/**
 	 * toString()
	 * 
 	 * Should return the class name concatenated with with all of the arguments
 	 * that were passed to it when it was constructed.
 	 * 
 	 * @return The class name of the process and all of its original arguments.
 	 */
 	String toString();
 }
