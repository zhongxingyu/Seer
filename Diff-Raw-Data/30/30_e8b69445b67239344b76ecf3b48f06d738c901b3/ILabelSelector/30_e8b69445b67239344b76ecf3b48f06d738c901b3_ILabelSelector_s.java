 /**
  * 
  */
 package gr.auth.ee.lcs.utilities;
 
 /**
  * A label subset selector, used for problem transformations.
  * 
  * @author Miltos Allamanis
  * 
  */
 public interface ILabelSelector {
 	/**
 	 * Change internal status to the next combination.
 	 */
	void next();
 
 	/**
 	 * Reset combination counting.
 	 */
 	void reset();
 
 	/**
 	 * Returns if there is another combination.
 	 * 
 	 * @return true if there is another combination
 	 */
 	boolean hasNext();
 
 	/**
 	 * Returns the 0/1 status of a label at a given index, for the current
 	 * state.
 	 * 
 	 * @param labelIndex
 	 *            the label index
 	 * @return true if the label should be active, otherwise false
 	 */
 	boolean getStatus(int labelIndex);
 
 }
