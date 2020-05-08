 package controllers;
 
 import helpers.BaumgartnerSampleTable;
 import helpers.Timer;
 
 import java.util.ArrayList;
 
 import models.Renderer;
 import play.mvc.Controller;
 import algorithms.cna.CNAlgorithm;
 import algorithms.cna.NecException;
 import datastructures.graph.Graph;
 import datastructures.mt.MinimalTheorySet;
 import datastructures.random.RandomGraphInput;
 import datastructures.random.RandomMTSetGenerator;
 
 public class CNAController extends Controller {
 
     private static Renderer renderer;
     private static RandomMTSetGenerator generator;
     private static Timer timer;
     private static boolean showBundleNumRenderer;
 
     public static void setup() {
 	render();
     }
 
     public static void prepare(String layers) {
 	int i = Integer.parseInt(layers);
 	render(i);
     }
 
     public static void generateGraph(ArrayList<Integer> bundles1,
 	    ArrayList<Integer> bundles2, ArrayList<Integer> bundles3,
 	    ArrayList<Integer> alterFactors, String epi, String showBundleNum) {
 	try {
 	    showBundleNumRenderer = (showBundleNum != null);
 	    boolean makeEpi = (epi != null);
 	    RandomMTSetGenerator generator;
 	    MinimalTheorySet theories;
 	    ArrayList<ArrayList<Integer>> list;
 	    RandomGraphInput input;
 
 	    list = new ArrayList<ArrayList<Integer>>();
 	    list.add(bundles1);
 	    list.add(bundles2);
 	    list.add(bundles3);
 
 	    input = new RandomGraphInput(list, alterFactors);
 	    generator = new RandomMTSetGenerator(input.getLevels(), makeEpi);
 	    theories = generator.getMTSet();
 
 	    Graph graph = new Graph(theories);
 	    Renderer renderer = new Renderer();
 	    renderer.setShowEdgeLabels(showBundleNumRenderer);
 	    renderer.config(graph);
 
 	    String graphPath = renderer.getImageSource();
 	    String generatedGraph = theories.toString();
 	    render(graphPath, generatedGraph);
 	} catch (OutOfMemoryError e) {
	    flash.error("Phuu! This calculation was to complex! Server is out of Memory! Please wait one minute and try again.");
 	    params.flash();
 	    setup();
 	    e.printStackTrace();
 	}
     }
 
     public static void calcCNAGraph(String generatedGraphPath,
 	    String generatedGraph, String lines) {
 	timer = new Timer();
 	renderer = new Renderer();
 	int randomLines = Integer.parseInt(lines);
 	renderer.setShowEdgeLabels(showBundleNumRenderer);
 	// try {
 	// TODO Make GraphToTable Parser change this
 	// CNAlgorithm cnaAlgorithm = new CNAlgorithm(generator.getTable(),
 	// randomLines);
 
 	// MinimalTheorieSet theories = cnaAlgorithm.getMinimalTheorieSet();
 	ArrayList<String> graphPaths = new ArrayList<String>();
 	ArrayList<String> stringGraphs = new ArrayList<String>();
 
 	// TODO Make rendering correct.
 	renderer = new Renderer();
 	renderer.setShowEdgeLabels(true);
 	// renderer.config(treeToGraph);
 	graphPaths.add(renderer.getImageSource());
 	// stringGraphs.add(stringToTree.getTree().toString());
 
 	// Long time = timer.timeElapsed();
 
 	// String originalTable = cnaAlgorithm.getOriginalTable().toString();
 	// String elapsedTime = time.toString() + " ms";
 	// String effects = cnaAlgorithm.getEffects().toString();
 	// String sufTable = cnaAlgorithm.getSufTable().toString();
 	// String msufTable = cnaAlgorithm.getMsufTable().toString();
 	// String necList = cnaAlgorithm.getNecList().toString();
 	// String mnecTable = cnaAlgorithm.getMnecTable().toString();
 	// String deleted = cnaAlgorithm.getDeleted().toString();
 	// String fmt = cnaAlgorithm.getMinimalTheorieSet().toString();
 
 	// render(elapsedTime, originalTable, graphPaths, stringGraphs,
 	// generatedGraphPath, generatedGraph, effects, sufTable,
 	// msufTable, necList, mnecTable, fmt, deleted);
 	// } catch (NecException e) {
 	// flash.error("NEC error.");
 	// params.flash();
 	// setup();
 	// }
     }
 
     public static void baumgartnerSample() {
 	timer = new Timer();
 	CNAlgorithm cnaAlgorithm;
 	try {
 	    cnaAlgorithm = new CNAlgorithm(
 		    new BaumgartnerSampleTable().getSampleTable());
 	    MinimalTheorySet theories = cnaAlgorithm.getMinimalTheorySet();
 	    ArrayList<String> stringGraphs = new ArrayList<String>();
 	    String graphPath;
 
 	    renderer = new Renderer();
 	    Graph graph = new Graph(theories);
 	    Renderer renderer = new Renderer();
 	    renderer.setShowEdgeLabels(showBundleNumRenderer);
 	    renderer.config(graph);
 	    graphPath = renderer.getImageSource();
 
 	    // TODO
 	    // for (MinimalTheorie theorie : theories) {
 	    // CNAList list = new CNAList();
 	    // list.add(theorie.toString());
 	    // StringToTree stringToTree = new StringToTree(list);
 	    // TreeToGraph treeToGraph = new TreeToGraph(
 	    // stringToTree.getTree(), stringToTree.getNumOfEffects(),
 	    // stringToTree.getTotalFactors());
 	    // renderer = new Renderer();
 	    // renderer.setEdgeLabels(true);
 	    // renderer.setChangingVertexColors(true);
 	    // renderer.config(treeToGraph);
 	    // graphPaths.add(renderer.getImageSource());
 	    // stringGraphs.add(stringToTree.getTree().toString());
 	    // }
 	    Long time = timer.timeElapsed();
 	    // String calculatedGraph =
 	    String elapsedTime = time.toString() + " ms";
 	    String effects = cnaAlgorithm.getEffects().toString();
 	    String sufTable = cnaAlgorithm.getSufTable().toString();
 	    String msufTable = cnaAlgorithm.getMsufTable().toString();
 	    String necList = cnaAlgorithm.getNecList().toString();
 	    String mnecTable = cnaAlgorithm.getMnecTable().toString();
 	    render(elapsedTime, graphPath, stringGraphs, effects, sufTable,
 		    msufTable, necList, mnecTable);
 	} catch (NecException e) {
 	    flash.error("NEC error.");
 	    params.flash();
 	    setup();
 	    e.printStackTrace();
 	}
 
     }
 }
