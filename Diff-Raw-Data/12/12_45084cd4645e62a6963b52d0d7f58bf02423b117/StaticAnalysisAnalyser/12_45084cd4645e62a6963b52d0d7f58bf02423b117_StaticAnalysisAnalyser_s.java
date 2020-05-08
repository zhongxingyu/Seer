 package org.pescuma.buildhealth.analyser.staticanalysis;
 
 import static java.util.Arrays.*;
 import static org.pescuma.buildhealth.analyser.BuildStatusHelper.*;
 import static org.pescuma.buildhealth.analyser.NumbersFormater.*;
 import static org.pescuma.buildhealth.analyser.utils.BuildHealthAnalyserUtils.*;
 import static org.pescuma.buildhealth.core.BuildHealth.ReportFlags.*;
 import static org.pescuma.buildhealth.core.prefs.BuildHealthPreference.*;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.kohsuke.MetaInfServices;
 import org.pescuma.buildhealth.analyser.BuildHealthAnalyser;
 import org.pescuma.buildhealth.analyser.utils.BuildHealthAnalyserUtils.TreeStats;
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
  * Static analysis,language,framework,filename,line or line:column or beginLine:beginColumn:endLine:endColumn,category,message,severity (Low or Medium or High),details,URL with details
  * </pre>
  * 
  * The number is the number of violations in that place. Empty line means all file.
  * 
  * To be able to use the details (anything after filename) at least the filename is required.
  * 
  * Example:
  * 
  * <pre>
  * 1 | Static analysis,Java,Task,/a/b.java,12,Type1,Go to bed
  * 1 | Static analysis,Java,PMD,/a/b.java,12:1:12:5,A/Type1,Go to bed,http://a.com/info.html
  * 10 | Static analysis,Java,Task,/a/c.java,,Type2
  * </pre>
  */
 @MetaInfServices
 public class StaticAnalysisAnalyser implements BuildHealthAnalyser {
 	
 	public static final int COLUMN_LANGUAGE = 1;
 	public static final int COLUMN_FRAMEWORK = 2;
 	public static final int COLUMN_FILE = 3;
 	public static final int COLUMN_LINE = 4;
 	public static final int COLUMN_CATEGORY = 5;
 	public static final int COLUMN_MESSAGE = 6;
 	public static final int COLUMN_SEVERITY = 7;
 	public static final int COLUMN_DETAILS = 8;
 	public static final int COLUMN_URL = 9;
 	
 	@Override
 	public String getName() {
 		return "Static analysis";
 	}
 	
 	@Override
 	public int getPriority() {
 		return 300;
 	}
 	
 	@Override
 	public List<BuildHealthPreference> getPreferences() {
 		List<BuildHealthPreference> result = new ArrayList<BuildHealthPreference>();
 		
 		result.add(new BuildHealthPreference("Maximun munber of violations for a Good build", "<no limit>",
 				"staticanalysis", "good"));
 		result.add(new BuildHealthPreference("Maximun munber of violations for a So So build", "<no limit>",
 				"staticanalysis", "warn"));
 		
 		result.add(new BuildHealthPreference("Maximun munber of violations for a Good build", "<no limit>",
 				"staticanalysis", ANY_VALUE_KEY_PREFIX + "<language>", "good"));
 		result.add(new BuildHealthPreference("Maximun munber of violations for a So So build", "<no limit>",
 				"staticanalysis", ANY_VALUE_KEY_PREFIX + "<language>", "warn"));
 		
 		result.add(new BuildHealthPreference("Maximun munber of violations for a Good build", "<no limit>",
 				"staticanalysis", ANY_VALUE_KEY_PREFIX + "<language>", ANY_VALUE_KEY_PREFIX + "<framework>", "good"));
 		result.add(new BuildHealthPreference("Maximun munber of violations for a So So build", "<no limit>",
 				"staticanalysis", ANY_VALUE_KEY_PREFIX + "<language>", ANY_VALUE_KEY_PREFIX + "<framework>", "warn"));
 		
 		return Collections.unmodifiableList(result);
 	}
 	
 	@Override
 	public List<Report> computeReport(BuildData data, Preferences prefs, int opts) {
 		data = data.filter("Static analysis");
 		if (data.isEmpty())
 			return Collections.emptyList();
 		
 		prefs = prefs.child("staticanalysis");
 		
 		boolean highlighProblems = (opts & HighlightProblems) != 0;
 		boolean summaryOnly = (opts & SummaryOnly) != 0;
 		
 		SimpleTree<Stats> tree = buildTree(data);
 		
 		sumChildStatsAndComputeBuildStatuses(tree, prefs);
 		
 		if (summaryOnly)
 			removeNonSummaryNodes(tree, highlighProblems);
 		
		return asList(toReport(tree.getRoot(), getName(), prefs, highlighProblems, 2));
 	}
 	
 	private SimpleTree<Stats> buildTree(BuildData data) {
 		SimpleTree<Stats> tree = new SimpleTree<Stats>(new Function<String[], Stats>() {
 			@Override
 			public Stats apply(String[] name) {
 				return new Stats(name);
 			}
 		});
 		
 		for (Line line : data.getLines()) {
 			SimpleTree<Stats>.Node node = tree.getRoot();
 			
 			node = node.getChild(line.getColumn(COLUMN_LANGUAGE));
 			node = node.getChild(line.getColumn(COLUMN_FRAMEWORK));
 			
 			String category = line.getColumn(COLUMN_CATEGORY);
 			if (!category.isEmpty())
 				node = node.getChild(category);
 			
 			Stats stats = node.getData();
 			stats.add(line);
 		}
 		
 		return tree;
 	}
 	
 	private void sumChildStatsAndComputeBuildStatuses(SimpleTree<Stats> tree, final Preferences prefs) {
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
 				
 				stats.computeStatus(prefs);
 				
 				if (!parents.isEmpty())
 					parents.peekFirst().addChild(stats);
 			}
 		});
 	}
 	
 	private Report toReport(SimpleTree<Stats>.Node node, String name, Preferences prefs, boolean highlighProblems,
 			int showAllFrameworks) {
 		Stats stats = node.getData();
 		
 		List<Report> children = new ArrayList<Report>();
 		
 		children.addAll(toViolations(stats));
 		
 		for (SimpleTree<Stats>.Node child : sort(node.getChildren(), highlighProblems))
 			children.add(toReport(child, child.getName(), prefs, highlighProblems, showAllFrameworks - 1));
 		
 		String description = "";
 		if (showAllFrameworks > 0)
 			description = toDescription(stats);
 		
 		return new Report(node.isRoot() ? stats.getStatusWithChildren() : stats.getOwnStatus(), name,
 				format1000(stats.getTotal()), description, children);
 	}
 	
 	private String toDescription(Stats stats) {
 		List<Framework> fs = new ArrayList<Framework>(stats.frameworks.values());
 		Collections.sort(fs, new Comparator<Framework>() {
 			@Override
 			public int compare(Framework o1, Framework o2) {
 				int cmp = o1.language.compareToIgnoreCase(o2.language);
 				if (cmp != 0)
 					return cmp;
 				
 				return o1.framework.compareToIgnoreCase(o2.framework);
 			}
 		});
 		
 		StringBuilder result = new StringBuilder();
 		
 		int languages = countLanguages(fs);
 		String oldLanguage = "";
 		for (Framework f : fs) {
 			if (languages > 1 && !oldLanguage.equals(f.language)) {
 				oldLanguage = f.language;
 				
 				if (result.length() > 0)
 					result.append("; ");
 				
 				result.append(f.language).append(": ");
 				
 			} else {
 				if (result.length() > 0)
 					result.append(", ");
 			}
 			
 			result.append(f.framework).append(": ").append(format1000(f.total));
 		}
 		
 		return result.toString();
 	}
 	
 	private int countLanguages(List<Framework> fs) {
 		String oldLanguage = null;
 		int languages = 0;
 		for (Framework f : fs) {
 			if (oldLanguage == null || !oldLanguage.equals(f.language)) {
 				languages++;
 				oldLanguage = f.language;
 			}
 		}
 		return languages;
 	}
 	
 	private List<StaticAnalysisViolation> toViolations(Stats stats) {
 		List<StaticAnalysisViolation> violations = new ArrayList<StaticAnalysisViolation>();
 		
 		for (Line line : stats.violations)
 			violations.add(toViolation(line));
 		
 		Collections.sort(violations, new Comparator<StaticAnalysisViolation>() {
 			@Override
 			public int compare(StaticAnalysisViolation o1, StaticAnalysisViolation o2) {
 				int cmp = o1.getFilename().compareToIgnoreCase(o2.getFilename());
 				if (cmp != 0)
 					return cmp;
 				
 				cmp = toInt(o1.getLine()) - toInt(o2.getLine());
 				if (cmp != 0)
 					return cmp;
 				
 				return o1.getMessage().compareToIgnoreCase(o2.getMessage());
 			}
 			
 			private int toInt(String line) {
 				try {
 					return Integer.parseInt(line);
 				} catch (NumberFormatException e) {
 					return 0;
 				}
 			}
 		});
 		
 		return violations;
 	}
 	
 	private StaticAnalysisViolation toViolation(Line line) {
 		String language = line.getColumn(COLUMN_LANGUAGE);
 		String framework = line.getColumn(COLUMN_FRAMEWORK);
 		String filename = line.getColumn(COLUMN_FILE);
 		String fileLine = line.getColumn(COLUMN_LINE);
 		String category = line.getColumn(COLUMN_CATEGORY);
 		String message = line.getColumn(COLUMN_MESSAGE);
 		String severity = line.getColumn(COLUMN_SEVERITY);
 		String details = line.getColumn(COLUMN_DETAILS);
 		String url = line.getColumn(COLUMN_URL);
 		
 		return new StaticAnalysisViolation(BuildStatus.Good, language, framework, filename, fileLine, category,
 				message, severity, details, url);
 	}
 	
 	private static class Stats extends TreeStats {
 		
 		final List<Line> violations = new ArrayList<Line>();
 		final Map<String, Framework> frameworks = new HashMap<String, Framework>();
 		
 		Stats(String[] name) {
 			super(name);
 		}
 		
 		void add(Line line) {
 			getFramework(line.getColumn(COLUMN_LANGUAGE), line.getColumn(COLUMN_FRAMEWORK)).total += line.getValue();
 			
 			if (!line.getColumn(COLUMN_FILE).isEmpty())
 				violations.add(line);
 		}
 		
 		void addChild(Stats stats) {
 			for (Framework framework : stats.frameworks.values())
 				getFramework(framework.language, framework.framework).total += framework.total;
 			
 			mergeChildStatus(stats);
 		}
 		
 		void computeStatus(Preferences prefs) {
 			BuildStatus status = computeStatusFromThresholdIfExists(prefs.child(getNames()), getTotal(), false);
 			
 			if (status != null)
 				setOwnStatus(status);
 		}
 		
 		double getTotal() {
 			double total = 0;
 			for (Framework framework : frameworks.values())
 				total += framework.total;
 			return total;
 		}
 		
 		Framework getFramework(String language, String framework) {
 			String key = language + "\n" + framework;
 			
 			Framework result = frameworks.get(key);
 			if (result == null) {
 				result = new Framework(language, framework);
 				frameworks.put(key, result);
 			}
 			
 			return result;
 		}
 	}
 	
 	private static class Framework {
 		final String language;
 		final String framework;
 		double total = 0;
 		
 		Framework(String language, String framwork) {
 			this.language = language;
 			this.framework = framwork;
 		}
 	}
 	
 }
