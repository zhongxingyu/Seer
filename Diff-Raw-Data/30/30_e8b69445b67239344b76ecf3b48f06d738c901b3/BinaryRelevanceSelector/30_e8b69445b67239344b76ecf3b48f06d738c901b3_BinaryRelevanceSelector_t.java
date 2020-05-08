 /**
  * 
  */
 package gr.auth.ee.lcs.utilities;
 
 /**
  * A Binary Relevance Label Selector. We select one after another.
  * 
  * @author Miltiadis Allamanis
  * 
  */
 public class BinaryRelevanceSelector implements ILabelSelector {
 
 	/**
 	 * The number of labels used at the selector.
 	 */
 	private final int numberOfLabels;
 
 	/**
 	 * The current label that the iterator presents.
 	 */
 	private int currentLabel = 0;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param labels
 	 *            the number of labels at the problem
 	 */
 	public BinaryRelevanceSelector(int labels) {
 		numberOfLabels = labels;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gr.auth.ee.lcs.utilities.ILabelSelector#next()
 	 */
 	@Override
	public boolean next() {
		if (currentLabel < numberOfLabels - 1){
 			currentLabel++;
			return true;
		}
		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gr.auth.ee.lcs.utilities.ILabelSelector#reset()
 	 */
 	@Override
 	public void reset() {
 		currentLabel = 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gr.auth.ee.lcs.utilities.ILabelSelector#hasNext()
 	 */
 	@Override
 	public boolean hasNext() {
 		if (currentLabel < numberOfLabels - 1)
 			return true;
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gr.auth.ee.lcs.utilities.ILabelSelector#getStatus(int)
 	 */
 	@Override
 	public boolean getStatus(int labelIndex) {
 		return labelIndex == currentLabel;
 	}
 
 }
