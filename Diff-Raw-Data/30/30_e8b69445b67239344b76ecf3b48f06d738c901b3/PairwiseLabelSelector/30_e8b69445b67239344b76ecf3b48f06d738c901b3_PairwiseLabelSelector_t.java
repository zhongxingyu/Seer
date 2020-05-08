 /**
  * 
  */
 package gr.auth.ee.lcs.utilities;
 
 /**
  * A Pairwise label selector for PW problem transform.
  * 
  * @author Miltiadis Allamanis
  * 
  */
 public class PairwiseLabelSelector implements ILabelSelector {
 
 	/**
 	 * The number of labels used at the problem.
 	 */
 	private final int numberOfLabels;
 
 	/**
 	 * Internal state parameters.
 	 */
 	private int i = 0, j = 1;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param labels
 	 *            the problem's number of labels
 	 */
 	public PairwiseLabelSelector(int labels) {
 		numberOfLabels = labels;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gr.auth.ee.lcs.utilities.ILabelSelector#next()
 	 */
 	@Override
	public boolean next() {
 		if (!hasNext())
			return false;
 		j++;
 		if (j >= numberOfLabels) {
 			i++;
 			j = i + 1;
 		}
		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gr.auth.ee.lcs.utilities.ILabelSelector#reset()
 	 */
 	@Override
 	public void reset() {
 		i = 0;
 		j = 1;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gr.auth.ee.lcs.utilities.ILabelSelector#hasNext()
 	 */
 	@Override
 	public boolean hasNext() {
 		if (i == numberOfLabels - 2 && j == numberOfLabels - 1)
 			return false;
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gr.auth.ee.lcs.utilities.ILabelSelector#getStatus(int)
 	 */
 	@Override
 	public boolean getStatus(int labelIndex) {
 		return (labelIndex == i || labelIndex == j);
 
 	}
 
 }
