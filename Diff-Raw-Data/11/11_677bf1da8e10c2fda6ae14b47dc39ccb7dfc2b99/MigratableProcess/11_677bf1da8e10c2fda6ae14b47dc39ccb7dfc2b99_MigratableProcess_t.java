 package Interface;
 
 import java.io.Serializable;
 
 /**
  * 
 * @author Yin Xu, Zheng Kou
  */
 public interface MigratableProcess extends Runnable, Serializable {
 	
 	/**
	 * suspend() is called before the MigratableProcess is ready for 
	 * serialization.
 	 */
 	void suspend();
 	
 	/**
 	 * toString()
 	 * Should return the class name concatenated with with all of the arguments
 	 * that were passed to it when it was constructed.
 	 * 
 	 * @return The class name of the process and all of its original arguments.
 	 */
 	String toString();
 }
