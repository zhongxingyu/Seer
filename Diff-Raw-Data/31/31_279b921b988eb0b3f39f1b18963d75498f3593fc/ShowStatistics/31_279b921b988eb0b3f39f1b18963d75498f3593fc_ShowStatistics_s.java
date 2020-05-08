 package org.computer.knauss.reqtDiscussion.ui.ctrl;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JFrame;
 
import org.computer.knauss.reqtDiscussion.io.Util;
 import org.computer.knauss.reqtDiscussion.model.Discussion;
 import org.computer.knauss.reqtDiscussion.model.metric.AbstractDiscussionMetric;
 import org.computer.knauss.reqtDiscussion.model.metric.AbstractNetworkMetric;
 import org.computer.knauss.reqtDiscussion.ui.metrics.MetricFrame;
 import org.computer.knauss.reqtDiscussion.ui.uiModel.DiscussionTableModel;
 
 public class ShowStatistics extends AbstractDiscussionIterationCommand {
 
 	private static final long serialVersionUID = 1L;
 	Map<Integer, Discussion> discussionMap = new HashMap<Integer, Discussion>();
 	List<AbstractDiscussionMetric> metrics;
 	Set<Integer> visited = new HashSet<Integer>();
 	HighlightRelatedDiscussions hrd = null;
 
 	public ShowStatistics() {
 		super("Show statistics");
 	}
 
 	@Override
 	protected void preProcessingHook() {
 		this.metrics = new LinkedList<AbstractDiscussionMetric>();
 
 		// set the partitions to the currently used partition
 		for (AbstractNetworkMetric m : AbstractNetworkMetric.STANDARD_METRICS) {
 			m.setPartition(getVisualizationConfiguration()
 					.getDiscussionPartition());
 			m.setSocialNetwork(getVisualizationConfiguration()
 					.getSocialNetwork());
 		}
 
 		Collections
 				.addAll(this.metrics, AbstractDiscussionMetric.OTHER_METRICS);
 		Collections.addAll(this.metrics,
 				AbstractDiscussionMetric.STANDARD_METRICS);
 		Collections
 				.addAll(this.metrics, AbstractNetworkMetric.STANDARD_METRICS);
 
 		try {
 			this.hrd = new HighlightRelatedDiscussions();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// init
 		for (Discussion d : ((DiscussionTableModel) getDiscussionTableModel())
 				.getDiscussions()) {
 			this.discussionMap.put(d.getID(), d);
 		}
 
 		System.out.println("=== Statistics ===");
 		StringBuffer sb = new StringBuffer();
 		for (AbstractDiscussionMetric m : this.metrics) {
 			sb.append("\t");
 			sb.append(m.getName());
 		}
 		System.out.println(sb.toString());
 	}
 
 	@Override
 	protected void processDiscussionHook(Discussion d) {
 		// compute
 		if (!this.visited.contains(d.getID())) {
 			int[] related = new int[] { d.getID() };
 			if (this.hrd != null)
 				related = this.hrd.getRelatedDiscussionIDs(d.getID());
 			List<Discussion> tmp = new LinkedList<Discussion>();
 			for (int id : related) {
 				if (!this.visited.contains(id)) {
 					this.visited.add(id);
 					tmp.add(this.discussionMap.get(id));
 				}
 			}
 			String key = Util.idsToKey(related);
 			System.out.print(key);
 
 			Discussion[] tmpDiscussions = tmp.toArray(new Discussion[0]);
 			for (AbstractDiscussionMetric m : this.metrics) {
 				m.initDiscussions(tmpDiscussions);
 				Double result = m.considerDiscussions(tmpDiscussions);
 				m.getResults().put(key, result);
 				System.out.print("\t" + result);
 			}
 			System.out.println();
 		}
 	}
 
 	@Override
 	protected void postProcessingHook() {
 		System.out.println("=== Overall Statistics ===");
 
 		for (AbstractDiscussionMetric m : this.metrics)
 			System.out.println(m);
 
 		MetricFrame mf = new MetricFrame();
 		mf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		mf.update();
 		mf.setVisible(true);
 	}
 
 }
