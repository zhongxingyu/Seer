 package genlab.core.model.meta;
 
 import genlab.core.usermachineinteraction.GLLogger;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 /**
  * Lists existing categories, and exposes default (and recommanded) ones.
  * 
  * TODO add an extension point to add new ones ? 
  * 
  * @author Samuel Thiriot
  *
  */
 public class ExistingAlgoCategories {
 
 	private Map<String, AlgoCategory> id2algo = new HashMap<String, AlgoCategory>();
 	
 	private Collection<String> parentCategories = new LinkedList<String>();
 	
 	public static final AlgoCategory PARSER = new AlgoCategory(
 			null, 
 			"parser", 
 			"read something from a filesystem", 
 			"parsers"
 			);
 	
 	public static final AlgoCategory PARSER_GRAPH = new AlgoCategory(
 			PARSER, 
 			"graphs", 
 			"parse graphs from files", 
 			"graphs"
 			);
 	
 	public static final AlgoCategory WRITER = new AlgoCategory(
 			null, 
 			"writers", 
 			"write something to the filesystem (one or more files)", 
 			"writers"
 			);
 	
 	public static final AlgoCategory WRITER_GRAPH = new AlgoCategory(
 			WRITER, 
 			"graphs", 
 			"write graphs to files", 
 			"graphs"
 			);
 	
 	public static final AlgoCategory GENERATORS = new AlgoCategory(
 			null, 
 			"generators", 
 			"generate things", 
 			"generators" 
 			);
 	
 	public static final AlgoCategory GENERATORS_GRAPHS = new AlgoCategory(
 			GENERATORS, 
 			"graphs", 
 			"generate graphs", 
 			"graphs" 
 			);
 
 	public static final AlgoCategory STATIC = new AlgoCategory(
 			null, 
 			"static", 
 			"classic data", 
 			"data" 
 			);
 	
 
 	public static final AlgoCategory STATIC_GRAPHS = new AlgoCategory(
 			STATIC, 
 			"graphs", 
 			"classic graphs", 
 			"graphs" 
 			);
 	
 	
 	public static final AlgoCategory STATIC_GRAPHS_LCF = new AlgoCategory(
 			STATIC_GRAPHS, 
 			"LCF", 
 			"LCF-based graphs", 
 			"famous LCF graphs" 
 			);
 	
 	
 	public static final AlgoCategory ANALYSIS = new AlgoCategory(
 			null, 
 			"analysis", 
 			"analyse data", 
 			"analysis"
 			);
 	
 	public static final AlgoCategory ANALYSIS_GRAPH = new AlgoCategory(
 			ANALYSIS, 
 			"graphs", 
 			"analyse graphs", 
 			"graphs"
 			);
 	
 	public static final AlgoCategory ANALYSIS_GRAPH_AVERAGEPATHLENGTH = new AlgoCategory(
 			ANALYSIS_GRAPH, 
 			"average path length", 
 			"analyse the average path length in graphs", 
 			"average_path_length"
 			);
 	
 	public static final AlgoCategory CONSTANTS = new AlgoCategory(
 			null, 
 			"constants", 
 			"constant values", 
 			"constants"
 			);
 	
 	public static final AlgoCategory EXPLORATION = new AlgoCategory(
 			null, 
 			"exploration", 
 			"exploration algos", 
 			"exploration"
 			);
 	
 	public static final AlgoCategory EXPLORATION_GENETIC_ALGOS = new AlgoCategory(
 			EXPLORATION, 
 			"genetic algorithms", 
 			"genetic algorithms", 
 			"genetic_algorithms"
 			);
 	
 	
 	public static final AlgoCategory CASTING = new AlgoCategory(
 			null, 
 			"conversions", 
 			"conversion algorithms", 
 			"conversion"
 			);
 
 	public static final AlgoCategory DISPLAY = new AlgoCategory(
 			null, 
 			"displays", 
 			"display information", 
 			"dislap"
 			);
 	
 	public static final AlgoCategory DISPLAY_EXPLORATION_GENETIC_ALGOS = new AlgoCategory(
 			DISPLAY, 
 			"genetic algorithms", 
 			"genetic algorithms", 
 			"genetic_algorithms"
 			);
 	
 
 	public static final AlgoCategory LOOPS = new AlgoCategory(
 			null, 
 			"loops", 
 			"loops", 
 			"loops"
 			);
 	
 	public static final AlgoCategory NOISE = new AlgoCategory(
 			null, 
 			"noise", 
 			"noise", 
 			"add noise"
 			);
 
 	public static final AlgoCategory NOISE_GRAPH = new AlgoCategory(
 			NOISE, 
 			"graphs", 
 			"graphs", 
 			"add noise to graphs"
 			);
 
 	public static final AlgoCategory COMPARISON = new AlgoCategory(
 			null, 
 			"compare", 
 			"comparison", 
 			"compare inputs"
 			);
 
 	public static final AlgoCategory COMPARISON_GRAPHS = new AlgoCategory(
 			COMPARISON, 
 			"graphs", 
 			"graph isomorphisms", 
 			"graph isomorphisms"
 			);
 
 	private ExistingAlgoCategories() {
 		declareCategory(PARSER);
 		declareCategory(PARSER_GRAPH);
 		declareCategory(GENERATORS);
 		declareCategory(GENERATORS_GRAPHS);
 		declareCategory(STATIC_GRAPHS_LCF);
 		
 		declareCategory(WRITER);
 		declareCategory(WRITER_GRAPH);
 		declareCategory(ANALYSIS);
 		declareCategory(ANALYSIS_GRAPH);
 		declareCategory(CONSTANTS);
 		declareCategory(STATIC);
 		declareCategory(STATIC_GRAPHS);
 		declareCategory(EXPLORATION);
 		declareCategory(EXPLORATION_GENETIC_ALGOS);
 		declareCategory(CASTING);
 		declareCategory(DISPLAY);
		declareCategory(DISPLAY_EXPLORATION_GENETIC_ALGOS);
 		declareCategory(LOOPS);
 		declareCategory(NOISE);
 		declareCategory(NOISE_GRAPH);
 		
 		declareCategory(COMPARISON);
 		declareCategory(COMPARISON_GRAPHS);
 	}
 
 	public AlgoCategory getCategoryForId(String id) {
 		return id2algo.get(id);
 	}
 	
 	public void declareCategory(AlgoCategory ac) {
 		GLLogger.debugTech("added algo category: "+ac, ExistingAlgoCategories.class);
 		id2algo.put(ac.getTotalId(), ac);
 		if (ac.getParentCategory() == null && !parentCategories.contains(ac.getTotalId())) {
 			GLLogger.debugTech("added parent algo category: "+ac, ExistingAlgoCategories.class);
 			parentCategories.add(ac.getTotalId());
 		}
 	}
 	
 	public Collection<String> getParentCategories() {
 		return Collections.unmodifiableCollection(parentCategories);
 	}
 	
 	private final static ExistingAlgoCategories singleton = new ExistingAlgoCategories();
 	
 	public final static ExistingAlgoCategories getExistingAlgoCategories() {
 		return singleton;
 	}
 	
 	public Map<String, AlgoCategory> getAllCategories() {
 		return Collections.unmodifiableMap(id2algo);
 	}
 	
 
 }
