 package org.computer.knauss.reqtDiscussion.model.metric;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.computer.knauss.reqtDiscussion.io.Util;
 import org.computer.knauss.reqtDiscussion.model.Discussion;
 import org.computer.knauss.reqtDiscussion.ui.ctrl.HighlightRelatedDiscussions;
 
 /**
  * Metrics about Discussions.
  * 
  * @author eknauss
  * 
  */
 public abstract class AbstractDiscussionMetric {
 
 	public final static AbstractDiscussionMetric TIME_LENGTH = new TimeLengthMetric();
 	public final static AbstractDiscussionMetric NUMBER_COMMENTS = new CommentNumberMetric();
 	public final static AbstractDiscussionMetric NUMBER_CONTRIBUTORS = new ContributorNumberMetric();
 	public final static AbstractDiscussionMetric PATTERN_METRIC = new PatternMetric();
 	/**
 	 * A set of metrics that is often computed together.
 	 */
 	public static final AbstractDiscussionMetric[] STANDARD_METRICS = {
			TIME_LENGTH, NUMBER_COMMENTS, NUMBER_CONTRIBUTORS };
 
	public static final AbstractDiscussionMetric[] OTHER_METRICS = {PATTERN_METRIC};
 	/**
 	 * Returns the name of the metric for GUI and Reports.
 	 * 
 	 * @return
 	 */
 	public abstract String getName();
 
 	private Map<String, Double> values = new HashMap<String, Double>();
 	private DecimalFormat df;
 
 	public final Double getAverage() {
 		Double sum = 0d;
 		int instances = this.values.size();
 		for (Double n : this.values.values()) {
 			sum += n;
 		}
 		return sum / instances;
 	}
 
 	public final Double getMin() {
 		Double min = Double.MAX_VALUE;
 		for (Double n : this.values.values()) {
 			if (n < min)
 				min = n;
 		}
 		return min;
 	}
 
 	public final Double getMax() {
 		Double max = Double.MIN_VALUE;
 		for (Double n : this.values.values()) {
 			if (n > max)
 				max = n;
 		}
 		return max;
 	}
 
 	public final void computeMetric(Discussion[] discussions) {
 		Set<Integer> visited = new HashSet<Integer>();
 		Map<Integer, Discussion> discussionMap = new HashMap<Integer, Discussion>();
 		HighlightRelatedDiscussions hrd = null;
 		try {
 			hrd = new HighlightRelatedDiscussions();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// init
 		for (Discussion d : discussions) {
 			discussionMap.put(d.getID(), d);
 		}
 
 		// compute
 		for (Discussion d : discussions) {
 			if (!visited.contains(d.getID())) {
 				int[] related = new int[] { d.getID() };
 				if (hrd != null)
 					related = hrd.getRelatedDiscussionIDs(d.getID());
 				List<Discussion> tmp = new LinkedList<Discussion>();
 				for (int id : related) {
 					if (!visited.contains(id)) {
 						visited.add(id);
 						tmp.add(discussionMap.get(id));
 					}
 				}
 				Discussion[] tmpDiscussions = tmp.toArray(new Discussion[0]);
 				initDiscussions(tmpDiscussions);
 				this.values.put(Util.idsToKey(related),
 						considerDiscussions(tmpDiscussions));
 			}
 		}
 	}
 
 	public void initDiscussions(Discussion[] discussions) {
 		// override if needed.
 	}
 
 	/**
 	 * Returns a map with the results. The String-key contains the id of the
 	 * discussion. Note that it may contain multiple ids, if it is a complex
 	 * Discussion.
 	 * 
 	 * @return
 	 */
 	public Map<String, Double> getResults() {
 		return this.values;
 	}
 
 	public DecimalFormat getDecimalFormat() {
 		if (this.df == null) {
 			this.df = new DecimalFormat("#.#");
 		}
 		return this.df;
 	}
 
 	public abstract Double considerDiscussions(Discussion[] wi);
 
 	public final void reset() {
 		this.values.clear();
 	}
 
 	public String toString() {
 		DecimalFormat df = getDecimalFormat();
 		return getName() + " (avg,min,max): " + df.format(getAverage()) + ", "
 				+ df.format(getMin()) + ", " + df.format(getMax());
 	}
 }
