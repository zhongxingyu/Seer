 package org.pescuma.buildhealth.analyser.diskusage;
 
 import static java.util.Arrays.*;
 import static org.pescuma.buildhealth.analyser.NumbersFormater.*;
 import static org.pescuma.buildhealth.core.BuildHealth.ReportFlags.*;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.List;
 
 import org.kohsuke.MetaInfServices;
 import org.pescuma.buildhealth.analyser.BuildHealthAnalyser;
 import org.pescuma.buildhealth.analyser.utils.SimpleTree;
 import org.pescuma.buildhealth.core.BuildData;
 import org.pescuma.buildhealth.core.BuildData.Line;
 import org.pescuma.buildhealth.core.BuildStatus;
 import org.pescuma.buildhealth.core.Report;
 import org.pescuma.buildhealth.core.prefs.BuildHealthPreference;
 import org.pescuma.buildhealth.prefs.Preferences;
 
 import com.google.common.base.Function;
 
 /**
  * Expect the lines to be:
  * 
  * <pre>
  * Disk usage,tag (optional),folder or file name (optional)
  * </pre>
  * 
  * All values are in bytes
  * 
  * Example:
  * 
  * <pre>
  * 10 | Disk usage
  * 1024 | Disk usage,Executable
  * 1024 | Disk usage,Executable,/tmp/X
  * </pre>
  */
 @MetaInfServices
 public class DiskUsageAnalyser implements BuildHealthAnalyser {
 	
 	public static final int COLUMN_TAG = 1;
 	public static final int COLUMN_PATH = 2;
 	
 	@Override
 	public String getName() {
 		return "Disk usage";
 	}
 	
 	@Override
 	public int getPriority() {
 		return 600;
 	}
 	
 	@Override
 	public List<BuildHealthPreference> getPreferences() {
 		List<BuildHealthPreference> result = new ArrayList<BuildHealthPreference>();
 		
 		result.add(new BuildHealthPreference("If file tags should be used in the full report tree", "true",
 				"diskUsage", "reportWithTags"));
 		
 		return Collections.unmodifiableList(result);
 	}
 	
 	@Override
 	public List<Report> computeReport(BuildData data, Preferences prefs, int opts) {
 		data = data.filter("Disk usage");
 		if (data.isEmpty())
 			return Collections.emptyList();
 		
 		prefs = prefs.child("diskUsage");
 		
 		boolean summaryOnly = (opts & SummaryOnly) != 0;
 		
 		if (summaryOnly) {
 			double total = data.sum();
 			
 			return asList(new Report(BuildStatus.Good, getName(), formatBytes(total)));
 		}
 		
 		boolean useTags = prefs.get("reportWithTags", true);
 		if (useTags && !hasTags(data))
 			useTags = false;
 		
 		SimpleTree<Stats> tree = buildTree(data, useTags);
 		
 		propagateToParents(tree);
 		
 		return asList(toReport(tree.getRoot(), getName()));
 	}
 	
 	private boolean hasTags(BuildData data) {
 		Collection<String> tags = data.getDistinct(COLUMN_TAG);
 		return tags.size() > 1 || !tags.iterator().next().isEmpty();
 	}
 	
 	private SimpleTree<Stats> buildTree(BuildData data, boolean useTags) {
 		SimpleTree<Stats> tree = new SimpleTree<Stats>(new Function<String[], Stats>() {
 			@Override
 			public Stats apply(String[] name) {
 				return new Stats();
 			}
 		});
 		
 		for (Line line : data.getLines()) {
 			SimpleTree<Stats>.Node node = tree.getRoot();
 			
 			if (useTags) {
 				String tag = line.getColumn(COLUMN_TAG);
 				if (tag.isEmpty())
 					tag = "No tag";
 				node = node.getChild(tag);
 			}
 			
			String[] path = line.getColumn(COLUMN_PATH).split("(?<=[\\\\/])");
 			for (String p : path)
 				if (!p.isEmpty())
 					node = node.getChild(p);
 			
 			node.getData().total += line.getValue();
 		}
 		return tree;
 	}
 	
 	private void propagateToParents(SimpleTree<Stats> tree) {
 		tree.visit(new SimpleTree.Visitor<Stats>() {
 			Deque<Stats> parents = new ArrayDeque<Stats>();
 			
 			@Override
 			public void preVisitNode(SimpleTree<Stats>.Node node) {
 				parents.push(node.getData());
 			}
 			
 			@Override
 			public void posVisitNode(SimpleTree<Stats>.Node node) {
 				parents.pop();
 				
 				Stats stats = node.getData();
 				
 				if (!parents.isEmpty())
 					parents.peekFirst().add(stats);
 			}
 		});
 	}
 	
 	private Report toReport(SimpleTree<Stats>.Node node, String name) {
 		List<Report> children = new ArrayList<Report>();
 		
 		for (SimpleTree<Stats>.Node child : node.getChildren())
 			children.add(toReport(child, child.getName()));
 		
 		Stats stats = node.getData();
 		
 		return new Report(BuildStatus.Good, name, formatBytes(stats.total), children);
 	}
 	
 	private static class Stats {
 		
 		double total;
 		
 		void add(Stats stats) {
 			total += stats.total;
 		}
 	}
 }
