 package gov.nasa.arc.mct.fastplot.scatter;
 
 import gov.nasa.arc.mct.fastplot.bridge.AbstractPlotDataManager;
 import gov.nasa.arc.mct.fastplot.bridge.AbstractPlotDataSeries;
 import gov.nasa.arc.mct.fastplot.bridge.LegendEntry;
 import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 public class ScatterPlotDataManager implements AbstractPlotDataManager {
 	private ScatterPlot scatterPlot;
 	private Map<String, SortedMap<Long, Double>> dataPoints = new HashMap<String, SortedMap<Long, Double>>();
 	private Map<String, Map<String, ScatterPlotDataSeries>> dataSeriesMap = new HashMap<String, Map<String, ScatterPlotDataSeries>>();
 	private Map<String, List<ScatterPlotDataSeries>> dataSeriesList = new HashMap<String, List<ScatterPlotDataSeries>>();  
 	private String activeIndependent = null;
 	
 	public ScatterPlotDataManager(ScatterPlot scatterPlot) {
 		super();
 		this.scatterPlot = scatterPlot;
 	}
 	
 	public void beginGroup(String dataSetName) {
 		activeIndependent = dataSetName;
 		if (!dataSeriesMap.containsKey(dataSetName)) {
 			dataSeriesMap.put(dataSetName, new HashMap<String, ScatterPlotDataSeries>());
 		}
 	}
 	
 	public void endGroup() {
 		activeIndependent = null;
 	}
 	
 	private SortedMap<Long, Double> getDataMap(String key) {
 		if (!dataPoints.containsKey(key)) {
 			dataPoints.put(key, new TreeMap<Long,Double>());
 		}
 		return dataPoints.get(key);
 	}
 
 	@Override
 	public void addDataSet(String dataSetName, Color plottingColor) {
 		if (!dataSetName.contains(PlotConstants.NON_TIME_FEED_SEPARATOR)) {
 			beginGroup(dataSetName);
 		} else {
			System.out.println(dataSetName);
 			String dataSetNames[] = dataSetName.split(PlotConstants.NON_TIME_FEED_SEPARATOR);
 			dataSeriesMap.put(dataSetName, new HashMap<String, ScatterPlotDataSeries>());
 			LegendEntry legendEntry =
 				new LegendEntry(PlotConstants.LEGEND_BACKGROUND_COLOR, plottingColor,
 						PlotConstants.DEFAULT_TIME_AXIS_FONT, scatterPlot.getPlotLabelingAlgorithm());
 			ScatterPlotDataSeries dataSeries = 
 				new ScatterPlotDataSeries(scatterPlot, getDataMap(dataSetNames[0]), getDataMap(dataSetNames[1]), legendEntry);
 			dataSeriesMap.get(dataSetNames[0]).put(dataSetNames[1], dataSeries);
 			addToDataSeriesList(dataSetNames[0], dataSeries);
 			addToDataSeriesList(dataSetNames[1], dataSeries);
 		}
 	}
 	
 	private void addToDataSeriesList(String feed, ScatterPlotDataSeries series) {
 		if (!dataSeriesList.containsKey(feed)) {
 			dataSeriesList.put(feed, new ArrayList<ScatterPlotDataSeries>());
 		}
 		dataSeriesList.get(feed).add(series);
 	}
 
 	@Override
 	public void addData(String feed, SortedMap<Long, Double> points) {
		List<Long> timestamps = new ArrayList<Long>();
 		SortedMap<Long, Double> target = getDataMap(feed);
 		for (Entry<Long, Double> point : points.entrySet()) {
 			target.put(point.getKey(), point.getValue());
 		}
 		if (dataSeriesList.containsKey(feed)) {
 			for (ScatterPlotDataSeries series : dataSeriesList.get(feed)) {
 				series.updatePlotLine();
 			}
 		}
 		// TODO: We also need to discard old data at some point!
 		clearBefore(scatterPlot.getCurrentTimeAxisMin().getTimeInMillis());
 	}
 
 	@Override
 	public void informUpdateCacheDataStreamStarted() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void informResizeEvent() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void resizeAndReloadPlotBuffer() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public AbstractPlotDataSeries getNamedDataSeries(String name) {
 		if (name.contains(PlotConstants.NON_TIME_FEED_SEPARATOR)) {
 			String[] keys = name.split(PlotConstants.NON_TIME_FEED_SEPARATOR);
 			return dataSeriesMap.get(keys[0]).get(keys[1]);
 		}
 		if (activeIndependent == null || !dataSeriesMap.containsKey(activeIndependent)) {
 			return null;
 		}
 		return dataSeriesMap.get(activeIndependent).get(name);
 	}
 	
 	public void clearBefore (long timestamp) {
 		for (Map<String, ScatterPlotDataSeries> subMap : dataSeriesMap.values()) {
 			for (ScatterPlotDataSeries series : subMap.values()) {
 				series.clearBefore(timestamp);
 			}
 		}
 		for (SortedMap<Long, Double> points : dataPoints.values()) {
 			for (Long key : points.keySet()) {
 				if (key < timestamp) points.remove(key);
 				else break;
 			}
 		}
 	}
 
 }
