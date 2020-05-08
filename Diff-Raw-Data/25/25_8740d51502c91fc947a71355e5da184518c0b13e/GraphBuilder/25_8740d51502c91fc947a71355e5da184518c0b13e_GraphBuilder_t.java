 /**
  * 
  */
 package uk.ac.imperial.dws04.Presage2Experiments.Analysis;
 
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.Panel;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.jfree.chart.annotations.XYTextAnnotation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYDotRenderer;
 import org.jfree.data.general.Dataset;
 import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
 import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
 import org.jfree.data.xy.XYDataItem;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.ui.TextAnchor;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 import uk.ac.imperial.dws04.Presage2Experiments.RoadAgent.OwnChoiceMethod;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Role;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.HasRole;
 import uk.ac.imperial.dws04.utils.convert.StringSerializer;
 import uk.ac.imperial.dws04.utils.record.Pair;
 import uk.ac.imperial.presage2.core.db.DatabaseModule;
 import uk.ac.imperial.presage2.core.db.DatabaseService;
 import uk.ac.imperial.presage2.core.db.StorageService;
 import uk.ac.imperial.presage2.core.db.persistent.PersistentAgent;
 import uk.ac.imperial.presage2.core.db.persistent.PersistentEnvironment;
 import uk.ac.imperial.presage2.core.db.persistent.PersistentSimulation;
 import uk.ac.imperial.presage2.util.location.CellMove;
 
 /**
  * @author dws04
  *
  */
 public class GraphBuilder {
 	
 	private final Logger logger = Logger.getLogger(this.getClass());
 
 	final DatabaseService db;
 	final StorageService sto;
 
 	PersistentSimulation sim;
 	int t = 0;
 	int t0 = -1;
 
 	boolean headlessMode = false;
 
 	final static String imagePath = "/Users/dave/Documents/workspace/ExperimentalData/";
 	
 	
 	final static String speedTitle = "Agent Speed";
 	final static String dissTitle = "Agent Dissatisfaction";
 	final static String utilTitle = "Agent Move Utility";
 	final static String congestionTitle = "Congestion";
 	final static String ricCountTitle = "RIC Count";
 	final static String ricSizeTitle = "RIC Size";
 	final static String speedBAWTitle = "Speed BAW";
 	final static String combinedSpeedBAWTitle = "Combined Speed BAW";
 	final static List<String> chartTitles = Arrays.asList(new String[]{
 		speedTitle, dissTitle, utilTitle, congestionTitle, ricCountTitle, ricSizeTitle, speedBAWTitle, combinedSpeedBAWTitle
 	});
 	// this isn't needed for the sim charts, but is for the combined
 	final static String occupiedRICTitle = "Occupied RIC Count";
 	
 	
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		DatabaseModule module = DatabaseModule.load();
 		if (module != null) {
 			Injector injector = Guice.createInjector(module);
 			GraphBuilder gui = injector.getInstance(GraphBuilder.class);
 			if (args.length > 0 && Boolean.parseBoolean(args[0]) == true)
 				gui.headlessMode = true;
 			try {
 				List<Long> simIds = gui.init();
 				int endTime = gui.getEndTime(simIds);
 				HashMap<Long,HashMap<String,Chart>> charts = new HashMap<Long,HashMap<String,Chart>>();
 				for (Long simId : simIds) {
 					charts.put(simId,gui.buildForSim(simId));
 				}
 				gui.process(charts, endTime);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			gui.finish();
 			gui.logger.info("Finished.");
 			if (gui.headlessMode) System.exit(0);
 		}
 	}
 
 	@Inject
 	public GraphBuilder(DatabaseService db, StorageService sto) {
 		super();
 		this.db = db;
 		this.sto = sto;
 	}
 	
 	private List<Long> init() throws Exception {
 		db.start();
 		List<Long> result = sto.getSimulations();
 		return result;
 	}
 	
 	private void finish() {
 		db.stop();
 	}
 	
 	private int getEndTime(List<Long> simIds) {
 		int endTime = 0;
 		for (Long simId : simIds) {
 			int currTime = sto.getSimulationById(simId).getCurrentTime();
 			if (currTime > endTime) {
 				endTime = currTime;
 			}
 		}
 		return endTime;
 	}
 	
 	private HashMap<String,Chart> buildForSim(Long simId) throws IOException, ClassNotFoundException {
 		logger.info("Building charts for sim " + simId + "...");
 		try {
 			db.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		HashMap<String,Chart> charts = new HashMap<String,Chart>();
 		
 		sim = sto.getSimulationById(simId);
 		OwnChoiceMethod choiceMethod = OwnChoiceMethod.fromString(sim.getParameters().get("ownChoiceMethod"));
 		if (headlessMode) {
 			File exportDir = new File(imagePath + sim.getName());
 			if (!exportDir.exists())
 				exportDir.mkdir();
 			else if (!exportDir.isDirectory())
 				System.exit(60);
 		}
 		
 		// get the data you want...
 		
 		Set<PersistentAgent> pAgents = sim.getAgents();
 		int endTime = sim.getCurrentTime();
 		
 		
 		
 		/*
 		 * Agent Persistent:
 		 * simId/aid/name/state
 		 * state:
 		 *  - type (String)
 		 *  - goals (Serialised RoadAgentGoals)
 		 *  - insertedAt (Serialised int)
 		 *  - leftAt (Serialised int)
 		 */
 		/*
 		 * Agent Transient:
 		 * simId/time/aid/state
 		 * state:
 		 *  - x (double)
 		 *  - y (double)
 		 *  - z (double)
 		 *  - location (Serialised RoadLocation)
 		 *  - speed (Serialised int)
 		 *  - dissatisfaction (Serialised double)
 		 *  - move (Serialised Pair<CellMove,Integer>)
 		 */
 		/*
 		 * Environment Transient:
 		 * simId/state/time
 		 * state:
 		 *  - RIC_Count (Serialised int)
 		 *  - [ric.toString()]_roles ( Serializable / Collection<HasRole> / HashSet )
 		 *  - [ric.toString()]_chosen (Serialised Chosen)
 		 */
 		/*
 		 * Environment Persistent:
 		 * (nothing)
 		 */
 		/*
 		 * Simulation:
 		 * id/name/state/currentTime/finishTime/createdAt/classname/finishedAt/parent/parameters/startedAt
 		 */
 		
 		// make list of charts you want
 		
 		// make frame & panel and add charts to them
 		
 		// draw "non-timeSeries" charts
 		/*
 		 * Agent join\leave - this will be time along the x, but you only iterate across agents and add in the time values, not across time
 		 *  - This can be used for throughput ?
 		 */
 		
 		// draw timeSeries charts iterating over time in sim
 		/*
 		 * Agent count - ref to congestion
 		 * Agent location - can do density/congestion ?
 		 *  - by lane and loc ?
 		 * Agent speeds - avg/max/min
 		 * Agent dissatisfaction - do line for each agent and a total/average ?
 		 * Agent moveUtil - line for each and total/average ?
 		 * IPCon cluster count
 		 * IPCon max/avg cluster size percycle
 		 *  - also do an overall value ? maybe a box for each sim ?
 		 */
 		
 		/*
 		 * Declare datasets
 		 */
 		XYDataset speedDataset = new XYSeriesCollection();
 		XYSeriesCollection speedCollection = (XYSeriesCollection)speedDataset;
 
 		XYDataset dissDataset = new XYSeriesCollection();
 		XYSeriesCollection dissCollection = (XYSeriesCollection)dissDataset;
 		XYDataset moveUtilDataset = new XYSeriesCollection();
 		XYSeriesCollection moveUtilCollection = (XYSeriesCollection)moveUtilDataset;
 		
 		XYDataset congestionDataset = new XYSeriesCollection();
 		XYSeriesCollection congestionCollection = (XYSeriesCollection)congestionDataset;
 		String congestionCountKey = simId+"_agentCount";
 		congestionCollection.addSeries(new XYSeries(congestionCountKey, true, false));
 		XYSeries congestionCount = congestionCollection.getSeries(congestionCountKey);
 		String congestionChangeInKey = simId+"_changeIn";
 		congestionCollection.addSeries(new XYSeries(congestionChangeInKey, true, false));
 		XYSeries congestionChangeIn = congestionCollection.getSeries(congestionChangeInKey);
 		String congestionChangeOutKey = simId+"_changeOut";
 		congestionCollection.addSeries(new XYSeries(congestionChangeOutKey, true, false));
 		XYSeries congestionChangeOut = congestionCollection.getSeries(congestionChangeOutKey);
 		for (int t = 0; t<=endTime; t++) {
 			congestionChangeIn.add(t, 0);
 			congestionChangeOut.add(t, 0);
 		}
 		
 		XYDataset ricCountDataset = new XYSeriesCollection();
 		XYSeriesCollection ricCountCollection = (XYSeriesCollection)ricCountDataset;
 		String ricCountKey = simId+"_ricCount";
 		ricCountCollection.addSeries(new XYSeries(ricCountKey, true, false));
 		String occupiedRICCountKey = simId+"_occupiedCount";
 		ricCountCollection.addSeries(new XYSeries(occupiedRICCountKey, true, false));
 		
 		XYDataset ricMemberDataset = new XYSeriesCollection();
 		XYSeriesCollection ricMemberCollection = (XYSeriesCollection)ricMemberDataset;
 		/*
 		 * Map of RIC(toString) to maps from timestamp to acceptorcount
 		 */
 		HashMap<String, HashMap<Integer, Integer>> acceptorTimeMap = new HashMap<String, HashMap<Integer, Integer>>();
 
 		
 		// declare charts
 		DefaultTimeSeriesChart speedChart = new DefaultTimeSeriesChart(simId, choiceMethod, speedDataset, speedTitle, "timestep", "speed");
 		speedChart.hideLegend(true);
 		charts.put(speedTitle, speedChart);
 		DefaultTimeSeriesChart dissChart = new DefaultTimeSeriesChart(simId, choiceMethod, dissDataset, dissTitle, "timestep", "dissatisfaction");
 		dissChart.hideLegend(true);
 		charts.put(dissTitle, dissChart); 
 		DefaultTimeSeriesChart moveUtilChart = new DefaultTimeSeriesChart(simId, choiceMethod, moveUtilDataset, utilTitle, "timestep", "utility");
 		moveUtilChart.hideLegend(true);
 		charts.put(utilTitle, moveUtilChart);
 		DefaultTimeSeriesChart congestionChart = new DefaultTimeSeriesChart(simId, choiceMethod, congestionDataset, congestionTitle, "timestep", "AgentCount");
 		charts.put(congestionTitle, congestionChart);
 		DefaultTimeSeriesChart ricCountChart = new DefaultTimeSeriesChart(simId, choiceMethod, ricCountDataset, ricCountTitle, "timestep", "Count");
 		charts.put(ricCountTitle, ricCountChart);
 		DefaultTimeSeriesChart ricAcceptorCountChart = new DefaultTimeSeriesChart(simId, choiceMethod, ricMemberDataset, ricSizeTitle, "timestep", "Number of Acceptors");
 		ricAcceptorCountChart.hideLegend(true);
 		charts.put(ricSizeTitle, ricAcceptorCountChart);
 		
 		
 		
 		
 		// get environment values
 		PersistentEnvironment pEnv = sim.getEnvironment();
 		
 		// get persistent env data
 		
 		
 		// transient env data
 		for (int t = 0; t<=endTime; t++) {
 			Integer ricCount = (Integer) StringSerializer.fromString(pEnv.getProperty("RIC_Count", t));
 			ricCountCollection.getSeries(ricCountKey).add(t, ricCount);
 			Integer occupiedRIC = (Integer) StringSerializer.fromString(pEnv.getProperty("OccupiedRIC_Count", t));
 			ricCountCollection.getSeries(occupiedRICCountKey).add(t, occupiedRIC);
 			
 			Map<String,String> properties = pEnv.getProperties(t);
 			for (Entry<String,String>property : properties.entrySet()) {
 				if (property.getKey().endsWith("_roles")) {
 					addAcceptorCountToMap(acceptorTimeMap, t, property.getValue());
 				}
 			}
 			
 			
 			
 		}
 		
 		
 		populateRICAcceptorCountDatasetFromMap(ricMemberCollection, acceptorTimeMap);
 		
 		
 		// get agent data
 		for (PersistentAgent pAgent : pAgents) {
 			String key = pAgent.getID().toString();
 			logger.debug("Processing agent " + key);
 			
 			// add series' in all the relevant collections
 			speedCollection.addSeries(new XYSeries(key, true, false));
 			dissCollection.addSeries(new XYSeries(key, true, false));
 			moveUtilCollection.addSeries(new XYSeries(key, true, false));	
 			
 			
 			
 			// get agent persistent values
 			Integer insertedAt = (Integer) StringSerializer.fromString(pAgent.getProperty("insertedAt"));
 			Integer leftAt = (Integer) StringSerializer.fromString(pAgent.getProperty("leftAt"));
 			Double inValue = null;
 			try {
 				inValue = (Double) congestionChangeIn.getY(insertedAt);
 			} catch (IndexOutOfBoundsException e) {
 				// ditch it
 			}
 			if (inValue!=null) {
 				congestionChangeIn.update((Number)(insertedAt), (Number)(inValue+1));
 			}
 			if (leftAt!=null) {
 				Double outValue = (Double) congestionChangeOut.getY(leftAt-1);
 				congestionChangeOut.update((Number)(leftAt), (Number)(outValue+1));
 			}
 			
 			// get agent transient values
 			for (int t=0; t<=endTime; t++) {
 				logger.trace("Time " + t);
 				// get transient values
 				logger.debug("Processing speed for agent " + key);
 				Integer speed = (Integer) StringSerializer.fromString(pAgent.getState(t).getProperty("speed"));
 				speedCollection.getSeries(key).add(t, speed);
 				
 				logger.debug("Processing dissatisfaction for agent " + key);
 				Double diss = (Double) StringSerializer.fromString(pAgent.getState(t).getProperty("dissatisfaction"));
 				dissCollection.getSeries(key).add(t, diss);
 				
 				logger.debug("Processing move utility for agent " + key);
 				Pair<CellMove,Integer> pair = (Pair<CellMove,Integer>) StringSerializer.fromString(pAgent.getState(t).getProperty("move"));
 				Integer util = null;
 				if (pair!=null) {
 					util = pair.getB();
 					if (util>100) {
 						util = 100;
 					}
 				}
 				moveUtilCollection.getSeries(key).add(t, util);
 				
 			}
 		}
 		
 		ChartUtils.makeAvgLineOnChart(speedChart, endTime, "Speed", false);
 		ChartUtils.makeAvgLineOnChart(ricAcceptorCountChart, endTime, "Size", true);
 		ChartUtils.makeAvgLineOnChart(dissChart, endTime, "Dissatisfaction", false);
 		ChartUtils.makeAvgLineOnChart(moveUtilChart, endTime, "Move Utility", false);
 		tweakMoveUtilChart(moveUtilChart);
 		
 		Double agentCount = 0.0;
 		for (int t=0; t<=endTime; t++) {
 			agentCount = agentCount + (Double)congestionChangeIn.getY(t) - (Double)congestionChangeOut.getY(t);
 			congestionCount.add(t, agentCount);
 			annotateCongestionChart(congestionChart, t, simId);
 		}
 		
 
 		// declare BAW chart because it's data doesn't update...
 		DefaultBoxAndWhiskerChart speedBAW = new DefaultBoxAndWhiskerChart(simId, choiceMethod, speedCollection, speedBAWTitle, "Agent", "Speed", true);
 		speedBAW.hideLegend(true);
 		charts.put(speedBAWTitle, speedBAW);
 		BoxAndWhiskerCategoryDataset combinedSpeedBAWData = DefaultBoxAndWhiskerChart.combineDataToBAW(speedCollection, String.valueOf(simId), true);
 		DefaultBoxAndWhiskerChart combinedSpeedBAW = new DefaultBoxAndWhiskerChart(simId, choiceMethod, combinedSpeedBAWData, combinedSpeedBAWTitle, null, "Speed");
 		charts.put(combinedSpeedBAWTitle, combinedSpeedBAW);
 		
 		
 		Frame frame = new Frame("Results for Simulation " + simId);
 		Panel panel = new Panel(new GridLayout(0,2));
 		frame.add(panel);
 		for (Chart chart : charts.values()) {
 			ChartUtils.tweak(chart.getChart(), false, false);
 			ChartUtils.saveChart(chart.getChart(), imagePath, simId.toString(), chart.getChart().getTitle().getText());
 			panel.add(chart.getPanel());
 		}
 		frame.pack();
 		if (!headlessMode) {
 			frame.setVisible(true);
 			ChartUtils.savePanel(panel, imagePath, simId.toString(), "combination"); // won't draw if not visible...
 		}
 		
 		
 		
 		//db.stop();
 		logger.info("Done building charts for sim " + simId + ".");
 		return charts;
 	}
 
 	/**
 	 * @param moveUtilChart
 	 */
 	private void tweakMoveUtilChart(DefaultTimeSeriesChart moveUtilChart) {
 		XYPlot utilPlot = moveUtilChart.getXYPlot();
 		XYDotRenderer render = new XYDotRenderer();
 		int dotSize = 2;
 		render.setDotHeight(dotSize);
 		render.setDotWidth(dotSize);
 		utilPlot.setRenderer(0, render);
 		ChartUtils.makeLogRange(moveUtilChart);
 	}
 
 	/**
 	 * @param congestionChart
 	 * @param t
 	 * @param simId
 	 */
 	private static void annotateCongestionChart( DefaultTimeSeriesChart congestionChart, int t, Long simId) {
 		String congestionCountKey = simId+"_agentCount";
 		String congestionChangeInKey = simId+"_changeIn";
 		String congestionChangeOutKey = simId+"_changeOut";
 		XYSeriesCollection collection = (XYSeriesCollection)congestionChart.getXYPlot().getDataset();
 		XYSeries congestionCount = collection.getSeries(congestionCountKey);
 		XYSeries congestionChangeIn = collection.getSeries(congestionChangeInKey);
 		XYSeries congestionChangeOut = collection.getSeries(congestionChangeOutKey);
 		double angle = 2*(Math.PI/16);
 		if ((Double)congestionChangeIn.getY(t)!=0.0 && t!=0) {
 			String inAnnotate = "+"+((Double)congestionChangeIn.getY(t)).intValue();
 			XYTextAnnotation annotation = new XYTextAnnotation(inAnnotate, t-1, (Double)congestionCount.getY(t-1));
 			annotation.setFont(new Font("SansSerif", Font.PLAIN, 9));
 			TextAnchor anchor = TextAnchor.BOTTOM_RIGHT;
 			annotation.setTextAnchor(anchor);
 			annotation.setRotationAnchor(anchor);
 		    annotation.setRotationAngle(angle);
 			((XYPlot)congestionChart.getChart().getPlot()).addAnnotation(annotation);
 		}
 		if ((Double)congestionChangeOut.getY(t)!=0.0 && t!=0) {
 			String outAnnotate = "-"+((Double)congestionChangeOut.getY(t)).intValue();
 			XYTextAnnotation annotation = new XYTextAnnotation(outAnnotate, t-1, (Double)congestionCount.getY(t-1));
 			annotation.setFont(new Font("SansSerif", Font.PLAIN, 9));
 			TextAnchor anchor = TextAnchor.TOP_RIGHT;
 			annotation.setTextAnchor(anchor);
 			annotation.setRotationAnchor(anchor);
 		    annotation.setRotationAngle(-angle);
 			((XYPlot)congestionChart.getChart().getPlot()).addAnnotation(annotation);
 		}
 	}
 
 	private static void populateRICAcceptorCountDatasetFromMap(XYSeriesCollection ricMemberCollection, HashMap<String, HashMap<Integer, Integer>> acceptorTimeMap) {
 		for (Entry<String, HashMap<Integer,Integer>> topEntry : acceptorTimeMap.entrySet()) {
 			String ricName = topEntry.getKey();
 			HashMap<Integer,Integer> ricData = topEntry.getValue();
 			XYSeries series = new XYSeries(ricName, true, false);
 			
 			for (Entry<Integer,Integer> ricEntry : ricData.entrySet()) {
 				series.add(ricEntry.getKey(), ricEntry.getValue());
 			}
 			ricMemberCollection.addSeries(series);
 		}
 	}
 
 	/**
 	 * 
 	 * @param map hashmap of RICname to map of time:count
 	 * @param value String of serialised data (HashSet of HasRole)
 	 * @throws ClassNotFoundException 
 	 * @throws IOException 
 	 */
 	private static void addAcceptorCountToMap(HashMap<String, HashMap<Integer,Integer>> map, Integer time, String value) throws IOException, ClassNotFoundException {
 		@SuppressWarnings("unchecked")
 		HashSet<HasRole> roles = (HashSet<HasRole>) StringSerializer.fromString(value);
 		Integer count = 0;
 		String ricName = null;
 		for (HasRole role : roles) {
 			if (ricName==null) {
 				ricName = role.getRIC().toString();
 			}
 			if (role.getRole().equals(Role.ACCEPTOR)) {
 				count++;
 			}
 		}
 		if (count!=0) {
 			if (!map.containsKey(ricName)) {
 				map.put(ricName, new HashMap<Integer,Integer>());
 			}
 			map.get(ricName).put(time, count);
 		}
 	}
 	
 	private DefaultTimeSeriesChart makeAdjustedMoveUtilChart(final XYSeriesCollection data, Long simId, OwnChoiceMethod choiceMethod) {
 		XYSeriesCollection adjusted = new XYSeriesCollection();
 		for (Object serObj : data.getSeries()) {
 			XYSeries series = (XYSeries)serObj;
 			String key = (String) series.getKey();
 			adjusted.addSeries(new XYSeries(key));
 			for (Object itemObj : series.getItems()) {
 				XYDataItem item = (XYDataItem)itemObj;
 				Double y = item.getYValue();
 				if (y>100) {
 					y = 100.0;
 				}
 				adjusted.getSeries(key).add(item.getXValue(), y);
 			}
 		}
 		DefaultTimeSeriesChart moveUtilChart = new DefaultTimeSeriesChart(simId, choiceMethod, adjusted, utilTitle, "timestep", "utility");
 		moveUtilChart.hideLegend(true);
 		return moveUtilChart;
 	}
 	
 	private DefaultBoxAndWhiskerCategoryDataset makeBAWDataFromHashMap(HashMap<OwnChoiceMethod, ArrayList<Integer>> map) {
 		DefaultBoxAndWhiskerCategoryDataset result = new DefaultBoxAndWhiskerCategoryDataset();
 		for (Entry<OwnChoiceMethod,ArrayList<Integer>> entry : map.entrySet()) {
 			result.add(entry.getValue(), "rowKey", entry.getKey().toString());
 		}
 		return result;
 	}
 
 
 	/**
 	 * 
 	 * @param map map from simId:{map from ChartTitle:Chart}}
 	 * @param endTime
 	 * @throws IllegalAccessException 
 	 * @throws InstantiationException 
 	 */
 	private void process(HashMap<Long, HashMap<String,Chart>> map, int endTime) throws InstantiationException, IllegalAccessException {
 		logger.info("Beginning combination processing...");
 		/*
 		 * Want a chart for every chart type (from chartTitles)
 		 * On each chart, want a line/BAWitem for each moveChoiceMethod
 		 * Each choiceMethodLine should be averaged from all charts of that type of that choiceMethod 
 		 * 
 		 * Also want baw for length of sim for each choiceMethod
 		 * 
 		 *  ->
 		 *  
 		 *  Map from choiceMethod:{map from chartType:set of charts} (so all charts of each chartType for each choiceMethod are collected)
 		 *  for each choiceMethod {
 		 *  	for each chartType {
 		 *  		make avg dataset
 		 *  	}
 		 *  }
 		 *  for each chartType {
 		 *  	make chart and add line for each choiceMethod
 		 *  }
 		 *  
 		 *  
 		 */
 		HashMap<OwnChoiceMethod, ArrayList<Integer>> simLengthMap = new HashMap<OwnChoiceMethod, ArrayList<Integer>>();
 		/**
 		 * Map from choiceMethod:{map from chartType:set of charts} (so all charts of each chartType for each choiceMethod are collected)
 		 */
		HashMap<OwnChoiceMethod,HashMap<String,LinkedHashSet<Chart>>> outerChartMap = new HashMap<OwnChoiceMethod,HashMap<String,LinkedHashSet<Chart>>>();
 		HashMap<OwnChoiceMethod,HashMap<String,Dataset>> dataMap = new HashMap<OwnChoiceMethod,HashMap<String,Dataset>>();
 		for (Entry<Long,HashMap<String,Chart>> entry : map.entrySet()) {
 			Long simId = entry.getKey();
 			HashMap<String,Chart> charts = entry.getValue();
 			logger.info("Processing simId " + simId);
 			sim = sto.getSimulationById(simId);
 			
 			// get simLength
 			int length = sim.getCurrentTime();
 			OwnChoiceMethod method = OwnChoiceMethod.fromString(sim.getParameters().get("ownChoiceMethod")); // yay duplicate code 
 			if (!simLengthMap.containsKey(method)) {
 				simLengthMap.put(method, new ArrayList<Integer>());
 			}
 			simLengthMap.get(method).add(length);
 			DefaultBoxAndWhiskerCategoryDataset lengthBAWDataset = makeBAWDataFromHashMap(simLengthMap);
 			DefaultBoxAndWhiskerChart lengthBaw = new DefaultBoxAndWhiskerChart(simId, null, lengthBAWDataset, "Simulation length by choice method", "Choice Method", "Simulation Length (cycles)");
 			lengthBaw.hideLegend(true);
 			ChartUtils.saveChart(lengthBaw.getChart(), imagePath, "Overall", "LengthBaw");
 			
 			// get charts
 			for (Entry<String,Chart> chartEntry : charts.entrySet()) {
 				String chartTitle = chartEntry.getKey();
 				Chart chart = chartEntry.getValue();
 				OwnChoiceMethod choiceMethod = chart.getChoiceMethod();
 				if (!outerChartMap.containsKey(choiceMethod)) {
					outerChartMap.put(choiceMethod, new HashMap<String,LinkedHashSet<Chart>>());
 				}
 				if (!(outerChartMap.get(choiceMethod)).containsKey(chartTitle)) {
					outerChartMap.get(choiceMethod).put(chartTitle, new LinkedHashSet<Chart>());
 				}
 				outerChartMap.get(choiceMethod).get(chartTitle).add(chart);
 			}
 		}
 		logger.trace("Collected and sorted charts : " + outerChartMap);
 		logger.info("Collected info from all sims. Building datasets... ");
 			
 		// build datasets
 		for (OwnChoiceMethod choiceMethod : outerChartMap.keySet()) {
 			logger.info("Building sim datasets for method " + choiceMethod + "...");
 			// duplicate ricCount chart and rename dup to occupiedCount (HACK HACK HACK)
 			if (outerChartMap.get(choiceMethod).containsKey(ricCountTitle)) {
				LinkedHashSet<Chart> occupiedRICSet = (LinkedHashSet<Chart>) outerChartMap.get(choiceMethod).get(ricCountTitle).clone();
 				outerChartMap.get(choiceMethod).put(occupiedRICTitle, occupiedRICSet);
 			}
 			
 			dataMap.put(choiceMethod, new HashMap<String,Dataset>());
			for (Entry<String, LinkedHashSet<Chart>> innerMapEntry : outerChartMap.get(choiceMethod).entrySet()) {
 				String chartType = innerMapEntry.getKey();
 				// this is a set of charts (all of same type) for a given choice method - each one is for a specific sim
				LinkedHashSet<Chart> chartSet = innerMapEntry.getValue();
 				Class<? extends Dataset> datasetClass = datasetClassFromChartType(chartType);
 				Dataset dataset = dataMap.get(choiceMethod).get(chartType);
 				if (dataset==null) {
 					dataMap.get(choiceMethod).put(chartType, datasetClass.newInstance());
 				}
 				// not sure if this is needed, but just to be safe...
 				dataset = dataMap.get(choiceMethod).get(chartType);
 				if (datasetClass.isAssignableFrom(XYSeriesCollection.class)) {
 					XYSeriesCollection dataSeriesCollection = (XYSeriesCollection)dataset;
 					for (Chart chart : chartSet) {
 						XYPlot xyPlot = chart.getChart().getXYPlot();
 						XYSeries simSeries = null;
 						if (xyPlot.getDatasetCount()!=1) {
 							// avg dataset, so simple
 							try {
 								simSeries = (XYSeries) ((XYSeriesCollection)xyPlot.getDataset(1)).getSeries(0).clone();
 							} catch (CloneNotSupportedException e) {
 								e.printStackTrace();
 							}
 						}
 						else {
 							// doesn't have avg dataset already so need to make one
 							simSeries = simSeriesFromChartType(chartType, xyPlot, endTime);
 						}
 						simSeries.setKey(chart.getSimId());
 						dataSeriesCollection.addSeries(simSeries);
 					}
 				}
 				else if (datasetClass.isAssignableFrom(BoxAndWhiskerCategoryDataset.class)) {
 					// do this
 				}
 				// dataset now has serieses for each sim
 			}
 			logger.debug("Done building sim datasets for method " + choiceMethod + ".");
 		}
 		logger.info("Done building sim datasets. Drawing graphs...");
 		
 		for (Entry<OwnChoiceMethod, HashMap<String, Dataset>> entry : dataMap.entrySet()) {
 			OwnChoiceMethod method = entry.getKey();
 			HashMap<String,Dataset> dataByChartType = entry.getValue();
 			Frame frame = new Frame(method.toString());
 			Panel panel = new Panel(new GridLayout(0,2));
 			frame.add(panel);
 			for (Entry<String,Dataset> chartDataEntry : dataByChartType.entrySet()) {
 				String chartType = chartDataEntry.getKey();
 				Dataset chartDataset = chartDataEntry.getValue();
 				Chart chart = makeCombinedChartFromData__fromProcess(method, chartType, chartDataset);
 				if (chart!=null) {
 					ChartUtils.saveChart(chart.getChart(), imagePath, method.toString(), chartType);
 				}
 			}
 		}
 		
 		
 		logger.info("Done combination processing.");
 	}
 
 	private Chart makeCombinedChartFromData__fromProcess(OwnChoiceMethod choiceMethod, String chartType, Dataset data) {
 		if (	chartType.equalsIgnoreCase(speedTitle) ||
 				chartType.equalsIgnoreCase(dissTitle) ||
 				chartType.equalsIgnoreCase(utilTitle) ||
 				chartType.equalsIgnoreCase(congestionTitle) ||
 				chartType.equalsIgnoreCase(ricCountTitle) ||
 				chartType.equalsIgnoreCase(ricSizeTitle)  ||
 				chartType.equalsIgnoreCase(occupiedRICTitle)
 				) {
 			return new ChoiceMethodTimeSeriesChart(choiceMethod, (XYDataset)data, chartType);
 			//return new DefaultTimeSeriesChart(Long.getLong("-1"), choiceMethod, (XYDataset)data, key, "x", "y");
 		}
 		else {
 			// TODO do this
 			return null;
 		}
 	}
 
 	/**
 	 * 
 	 * @param chartType
 	 * @param xyPlot
 	 * @return
 	 */
 	private XYSeries simSeriesFromChartType(String chartType, XYPlot xyPlot, int endTime) {
 		XYSeries result = null;
 		if (	chartType.equalsIgnoreCase(congestionTitle) ||
 				chartType.equalsIgnoreCase(ricCountTitle) ||
 				chartType.equalsIgnoreCase(occupiedRICTitle)
 				) {
 			if (xyPlot.getDatasetCount()!=1) {
 				logger.warn("Unexpectedly found a " + chartType + " chart with more than one dataset ! Result may be odd.");
 			}
 			
 			/*
 			 * FIXME TODO THIS IS WRONG - it doesn't need an "avg" it needs to get the right series
 			 * In congestion's case, we need to make one series - the congestion one - while ignoring the in/out
 			 * in ricCount we want both but separately !
 			 */
 			if (chartType.equalsIgnoreCase(congestionTitle)) {
 				result = getSeriesEndingWith(chartType, xyPlot, "agentCount");
 			}
 			else if (chartType.equalsIgnoreCase(ricCountTitle)) {
 				result = getSeriesEndingWith(chartType, xyPlot, "ricCount");
 			}
 			else if (chartType.equalsIgnoreCase(occupiedRICTitle)) {
 				result = getSeriesEndingWith(chartType, xyPlot, "occupiedCount");
 			}
 			else {
 				logger.warn("Should never get here ! simSeriesFromChartType(" + chartType + ", " + xyPlot + ", " + endTime  + ")");
 			}
 		}
 		else {
 			logger.warn("Didn't recognise chart type " + chartType);
 		}
 		return result;
 	}
 
 	/**
 	 * @param chartType
 	 * @param xyPlot
 	 * @param keyEndStub
 	 */
 	private XYSeries getSeriesEndingWith(String chartType, XYPlot xyPlot,
 			String keyEndStub) {
 		List<Object> seriesObjList = ((XYSeriesCollection)xyPlot.getDataset()).getSeries();
 		for (Object obj : seriesObjList) {
 			if ( ( (String)((XYSeries)obj).getKey() ).endsWith(keyEndStub) ) {
 				try {
 					return (XYSeries) ((XYSeries)obj).clone();
 				} catch (CloneNotSupportedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		logger.warn("Couldn't find a " + keyEndStub + " series in a " + chartType + " chart.");
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param chartType {speedTitle, dissTitle, utilTitle, congestionTitle, ricCountTitle, ricSizeTitle, speedBAWTitle, combinedSpeedBAWTitle}
 	 * @return a new empty dataset corresponding to the charttype given
 	 */
 	private Class<? extends Dataset> datasetClassFromChartType(String chartType) {
 		if (	chartType.equalsIgnoreCase(speedTitle) ||
 				chartType.equalsIgnoreCase(dissTitle) ||
 				chartType.equalsIgnoreCase(utilTitle) ||
 				chartType.equalsIgnoreCase(congestionTitle) ||
 				chartType.equalsIgnoreCase(ricCountTitle) ||
 				chartType.equalsIgnoreCase(ricSizeTitle)  ||
 				chartType.equalsIgnoreCase(occupiedRICTitle)
 				) {
 			return new XYSeriesCollection().getClass();
 		}
 		else if (	chartType.equalsIgnoreCase(speedBAWTitle) ||
 				chartType.equalsIgnoreCase(combinedSpeedBAWTitle)
 				) {
 			return new DefaultBoxAndWhiskerCategoryDataset().getClass();
 		}
 		else {
 			logger.error("Did not recognise chartType: \"" + chartType + "\" so could not return the correct class.");
 			return null;
 		}
 	}
 	
 }
