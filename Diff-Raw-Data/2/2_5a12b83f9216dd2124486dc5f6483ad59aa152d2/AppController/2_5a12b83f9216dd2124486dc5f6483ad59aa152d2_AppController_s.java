 package org.aksw.linkedqa.client.mvc.controllers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.aksw.linkedqa.client.ChartWidget;
 import org.aksw.linkedqa.client.Constants;
 import org.aksw.linkedqa.client.GreetingServiceAsync;
 import org.aksw.linkedqa.client.mvc.events.AppEvents;
 import org.aksw.linkedqa.client.mvc.views.ChartView;
 import org.aksw.linkedqa.client.mvc.views.LinksetGrid;
 import org.aksw.linkedqa.client.mvc.views.TaskGridView;
 import org.aksw.linkedqa.shared.MyChart;
 import org.aksw.linkedqa.shared.TimeLinePackage;
 
 import com.extjs.gxt.charts.client.Chart;
 import com.extjs.gxt.charts.client.event.ChartEvent;
 import com.extjs.gxt.charts.client.event.ChartListener;
 import com.extjs.gxt.charts.client.model.BarDataProvider;
 import com.extjs.gxt.charts.client.model.ChartModel;
 import com.extjs.gxt.charts.client.model.Legend;
 import com.extjs.gxt.charts.client.model.Legend.Position;
 import com.extjs.gxt.charts.client.model.ScaleProvider;
 import com.extjs.gxt.charts.client.model.axis.XAxis;
 import com.extjs.gxt.charts.client.model.axis.XAxis.XLabels;
 import com.extjs.gxt.charts.client.model.axis.YAxis;
 import com.extjs.gxt.charts.client.model.charts.BarChart;
 import com.extjs.gxt.charts.client.model.charts.BarChart.BarStyle;
 import com.extjs.gxt.ui.client.Registry;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.data.BaseModel;
 import com.extjs.gxt.ui.client.data.Model;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SliderEvent;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.store.GroupingStore;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Label;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.Slider;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.extjs.gxt.ui.client.widget.Viewport;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridView;
 import com.extjs.gxt.ui.client.widget.grid.GroupingView;
 import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NodeList;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RootPanel;
 
 
 interface Transformer<I, O> {
 	O transform(I input);
 }
 
 // TODO We need some class that tells us how to use (load & display) the measures
 
 
 class Range<T>
 {
 	private T min;
 	private T max;
 	
 	public Range(T min, T max) {
 		this.min = min;
 		this.max = max;
 	}
 	
 	public T getMin() {
 		return min;
 	}
 	public T getMax() {
 		return max;
 	}
 }
 
 class PropertyDefinition
 	implements Comparable<PropertyDefinition>
 {
 	private String id;
 	private String displayName;
 	
 	private NumberFormat format;
 	
 	// If null: do auto adjust.
 	private Number min; 
 	private Number max;
 
 	
 	final NumberFormat doubleFormat = NumberFormat.getFormat("0.00");
 	//final NumberFormat integerFormat = NumberFormat.getPercentFormat();
 	
 	
 	public PropertyDefinition(String id, String displayName, Number min, Number max, NumberFormat format) 
 	{
 		this.id = id;
 		this.displayName = displayName;
 		this.min = min;
 		this.max = max;
 		this.format = format;
 	}
 	
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getDisplayName() {
 		return displayName;
 	}
 
 	public void setDisplayName(String displayName) {
 		this.displayName = displayName;
 	}
 
 	public Number getMin() {
 		return min;
 	}
 
 	public void setMin(Number min) {
 		this.min = min;
 	}
 
 	public Number getMax() {
 		return max;
 	}
 	
 	public void setMax(Number max) {
 		this.max = max;
 	}
 	
 	public NumberFormat getFormat() {
 		return format;
 	}
 	
 	//private static Map<String, PropertyDefinition> defaultMap = null;
 	
 	private static List<PropertyDefinition> defaultMap = null;
 	/*
 	// Sort by display name (rather than id)
 	public static TreeMap<String, PropertyDefinition> getSortedDefaultMap()
 	{
 		
 	}*/
 	
 	//public static Map<String, PropertyDefinition> getDefaultMap()
 	
 	public static List<PropertyDefinition> getDefaultMap()
 	{
 		if(defaultMap == null) {
 			//defaultMap = new TreeMap<String, PropertyDefinition>();
 			defaultMap = new ArrayList<PropertyDefinition>();
 		
 			putDefault(new PropertyDefinition("precision", "Pessimistic Positive Precision", 0.0, 1.0, NumberFormat.getPercentFormat()));
 			putDefault(new PropertyDefinition("recall", "Pessimistic Positive Recall", 0.0, 1.0, NumberFormat.getPercentFormat()));
 			
 			putDefault(new PropertyDefinition("estimatedPrecisionLowerBound", "Estimated Precision Lower Bound", 0.0, 1.0, NumberFormat.getPercentFormat()));
 			putDefault(new PropertyDefinition("estimatedPrecisionUpperBound", "Estimated Precision Upper Bound", 0.0, 1.0, NumberFormat.getPercentFormat()));
 			
 			putDefault(new PropertyDefinition("linksetSize", "Triples", 0.0, null, NumberFormat.getFormat("0")));
 			putDefault(new PropertyDefinition("linksetDuplicateSize", "Linkset Duplicates", 0.0, null, NumberFormat.getFormat("0")));
 			putDefault(new PropertyDefinition("linksetErrorCount", "Linkset Errors", 0, null, NumberFormat.getFormat("0")));
 		}
 		
 		return defaultMap;
 	}
 	
 	private static void putDefault(PropertyDefinition def) {
 		//defaultMap.put(def.getId(), def);
 		defaultMap.add(def);
 	}
 
 	public int compareTo(PropertyDefinition other) {
 		return this.displayName.compareTo(other.displayName);
 	}
 }
 
 
 
 public class AppController
 	extends Controller
 {
 	private TaskGridView taskView;
 	private GreetingServiceAsync service;
 	private ChartView chartView;
 
 	
 	private Chart overviewChartView;
 	private LinksetGrid linksetGrid;
 	
 	
 	private Chart timeLineChart;
 	
 	private Label noChartLabel;
 	
 	private LayoutContainer timeLineChartContainer;
 	
 	private LayoutContainer summaryPanel;
 	
 	
 	private LayoutContainer outliersPanel;
 	
 	//private Grid<Model> summaryGrid;
 	
 	//private Button linksetGrid;
 	//private Chart ove
 
 	// Here we need to be aware of our views
 	
 	// TODO Replace with a proper map
 	String[] measureNames = new String[] {"precision", "recall", "duplicates", "linkSetErrorCount"};
 	private int activeMeasureIndex = 0;
 	
 	private String activePackageName = null;
 	
 	public AppController() {
 		registerEventTypes(AppEvents.Init);
 		registerEventTypes(AppEvents.Error);
 		registerEventTypes(AppEvents.UiReady);
 		registerEventTypes(AppEvents.TaskSelectionChanged);
 		registerEventTypes(AppEvents.LinksetSelectionChanged);
 	}
 
 	@Override
 	public void initialize() {
 		super.initialize();
 		
 		//PropertyManager.get();
 		//pm.add("precision", new Range<Double>(0, 1));
 		
 		chartView = new ChartView(this);
 		taskView = new TaskGridView(this);
 
 		chartView = new ChartView(this);
 		linksetGrid = new LinksetGrid();
 		
 		overviewChartView = ChartWidget.createChart();
 	}
 
 	
 	int getValueClass(double value, double scale, int n) {
 		return Math.min(n - 1, (int)(value * scale * n));		
 	}
 	
 	
 	/**
 	 * Returns an object that creates keys for grouping.
 	 * 
 	 * @param min
 	 * @param max
 	 * @param n
 	 * @return
 	 */
 	public static Transformer<Number, Integer> createGrouper(final int n, final Number scale) {
 		//final double _min = min.doubleValue();
 		//final double _max = max.doubleValue();
 		
 		return new Transformer<Number, Integer>() {
 			public Integer transform(Number input) {				
 				//double val = Math.max(_input, _min);
 				//val = Math.min(val, _max);
 				
 				//double scale = (_max - _min) / (double)(n - 1);
 				
 				return (int)(input.doubleValue() * scale.doubleValue() * n);		
 			}
 		};
 	}
 	
 	
 	Range<Number> extractRange(ListStore<? extends Model> store, String attribute)
 	{
 		Double min = null;
 		Double max = null;
 
 		for(ModelData raw : store.getModels()) {
 			Double value = tryParseDouble(raw.get(attribute));
 			if(value == null) {
 				continue;
 			}
 			
 			if(min == null) {
 				min = value;
 				max = value;
 			}
 			
 			if(value.compareTo(min) < 0) {
 				min = value;
 			}
 			
 			if(value.compareTo(max) > 0) {
 				max = value;
 			}
 		}
 		
 		return min == null ? null : new Range<Number>(min, max);
 	}
 	
 	
 	ListStore<Model> deriveHistogram(ListStore<? extends Model> store, String attribute, PropertyDefinition property, int n) {
 		int counts[] = new int[n];
 		Arrays.fill(counts, 0);
 
 		double min;
 		double max;
 
 		Range<Number> range = null;
 		if(property.getMax() == null || property.getMin() == null) {
 			range = extractRange(store, attribute);
 			
 			if(range == null) {
 				return new ListStore<Model>();
 			}
 		}
 		
 		min = property.getMin() != null ? property.getMin().doubleValue() : range.getMin().doubleValue();
 		max = property.getMax() != null ? property.getMax().doubleValue() : range.getMax().doubleValue();
 		
 		double delta = max - min; 
 		double scale = delta / (double)n;
 		
 		for(ModelData raw : store.getModels()) {
 			Double value = tryParseDouble(raw.get(attribute));
 			if(value == null) {
 				continue;
 			}
 						
 			int x = getValueClass(value, scale, n);
 		
 			//GWT.log("xxx = " + prec + "    " + x);
 			counts[x]++;
 		}
 
 		ListStore<Model> result = new ListStore<Model>();
 		for(int i = 0; i < counts.length; ++i) {
 			double v = i * scale;
 			
 			String label = property.getFormat().format(v);
 			
 			Model data = new BaseModel();
 			data.set("label", label);
 			data.set("count", counts[i]);
 			
 			result.add(data);
 		}
 
 		return result;
 	}
 
 	public void addTimeLineChart(ChartModel model, String measureName, ListStore<Model> listStore) {
 
 		BarChart bar = new BarChart(BarStyle.GLASS);
 	    bar.setColour("#00aa00");
 	    BarDataProvider barProvider = new BarDataProvider(measureName, "snapshotDate");//new BarDataProvider("alphasales", "month");  
 	    barProvider.bind(listStore);  
 	    bar.setDataProvider(barProvider); 
 	    //bar.addChartListener(listener);  
 	    model.addChartConfig(bar);		
 	    
 	    
 	    /*
 	    ChartModel chartModel = new ChartModel("My Title", "font-size: 23px;font-weight:bold; font-family: Verdana; text-align: center;");
 
 	    //XAxis with label at 45°
 	    XAxis xaxis = new XAxis();
 	    List<String> axisList = new ArrayList<String>();
 	    axisList.add("Axis A");
 	    axisList.add("Axis B");
 	    axisList.add("Axis C");
 
 	    XLabels XLabelSerie = xaxis.new XLabels(axisList);
 	     XLabelSerie.setRotationAngle(-45);
 	     xaxis.setLabels(XLabelSerie);
 
 	    chartModel.setXAxis(xaxis);
 	    */
 
 	}
 	
 	
 	public ChartModel createTimeLineChartModel(String title)
 	{
 		ChartModel model = new ChartModel(title,  
 		        "font-size: 14px; font-family: Verdana; text-align: center;");  
 	    model.setBackgroundColour("#fefefe");  
 	    model.setLegend(new Legend(Position.TOP, true));  
 	    //model.setScaleProvider(ScaleProvider.ROUNDED_NEAREST_SCALE_PROVIDER);  
 
 
 	    XAxis xAxis = new XAxis();
 	    XLabels xLabels = xAxis.new XLabels();
 	    xLabels.setRotationAngle(-45);
 	    xAxis.setLabels(xLabels);
 
 	    model.setXAxis(xAxis);
 
 	    YAxis yAxis = new YAxis();
 	    yAxis.setMax(1.0);
 	    yAxis.setSteps(0.1);
 	    model.setYAxis(yAxis);
 	    
 	    //model.getXAxis().getLabels().setRotationAngle(-45);
 	    return model;
 	}
 	
 	
 	public void resetOverviewChart(int measureIndex) {
 		//String measureName = this.measureNames[measureIndex];
 		PropertyDefinition property = PropertyDefinition.getDefaultMap().get(measureIndex);
 		
 		String measureName = property.getId();
 		
 		
 		ListStore<Model> store = Registry.get(Constants.EVALUATION_STORE);
 
 		
 		int n = 10;
 		ListStore<Model> histogram = deriveHistogram(store, measureName, property, n);
 		//ListStore<Model> h = deriveHistogram(store, "recall", n);
 		
 		/*
 		histogram.addFilter(new StoreFilter<Model>() {
 
 			public boolean select(Store<Model> store, Model parent, Model item,
 					String property) {
 				
 				Number x = item.get(property);
 				if(x == null) {
 					return true;
 				}
 				//GWT.log("Prop = " + property + " Value: " + x);
 				return x.doubleValue() >= 1.0;
 			}});
 		
 
 		histogram.applyFilters("precision");
 		*/
 		
 		
 		//histogram.f
 		
 		String snapshotTitle = "latest snapshot";
 		//snapshot at 19-3-2011
 		
 		String title = property.getDisplayName() + " distribution for " + snapshotTitle;
 		
 		ChartModel model = new ChartModel(title,  
 		        "font-size: 14px; font-family: Verdana; text-align: center;");  
 		    model.setBackgroundColour("#fefefe");  
 		    model.setLegend(new Legend(Position.TOP, true));  
 		    model.setScaleProvider(ScaleProvider.ROUNDED_NEAREST_SCALE_PROVIDER);  
 		  
 		    
 		    ChartListener listener = new ChartListener() {  
 		        public void chartClick(ChartEvent ce) {  
 		          
 		        	//ce.get
 		        	
 		        	int row = ce.getChartConfig().getValues().indexOf(ce.getDataType());  
 		        	int col = ce.getChartModel().getChartConfigs().indexOf(ce.getChartConfig()) + 1;
 		          
 		        	//MessageBox.info("Magic not implemented yet.", "" + row + ", "  + col + " --- " + ce.getChartConfig().getDataProvider().toString(), null);
 
 		        	GridView gridView = linksetGrid.getGrid().getView();
 		        	if(gridView instanceof GroupingView) {
 		        		GroupingView groupingView = (GroupingView)gridView;
 		        		NodeList<Element> groups = groupingView.getGroups();
 		        		
 		        		groups.getItem(row).scrollIntoView();
 		        	}
 
 		        	
 		        	//.getGroups().getItem(0).sc
 		        	
 		        	//ce.getChartModel().g
 		        	
 		        	
 		          //CellSelectionModel<TeamSales> csm = (CellSelectionModel<TeamSales>) teamSalesGrid.getSelectionModel();  
 		         /*
 		          if (selRadio.getValue()) {  
 		            csm.selectCell(row, col);  
 		          } else {  
 		            teamSalesGrid.startEditing(row, col);  
 		          }*/  
 		        }  
 		      };  
 		    
 		    {
 		    BarChart bar = new BarChart(BarStyle.GLASS);  
 		    bar.setColour("#00aa00");  
 		    BarDataProvider barProvider = new BarDataProvider("count", "label");//new BarDataProvider("alphasales", "month");  
 		    barProvider.bind(histogram);  
 		    bar.setDataProvider(barProvider);  
 		    bar.addChartListener(listener);  
 		    model.addChartConfig(bar);
 		    }
 
 		    /*
 		    {
 		    BarChart bar = new BarChart(BarStyle.GLASS);  
 		    bar.setColour("#aa0000");  
 		    BarDataProvider barProvider = new BarDataProvider("count", "label");//new BarDataProvider("alphasales", "month");  
 		    barProvider.bind(h);  
 		    bar.setDataProvider(barProvider);  
 		    //bar.addChartListener(listener);  
 		    model.addChartConfig(bar);  
 		    }
 		    */
 		    
 		    
 		    //overviewChartView.getWidget().addChartModel(model); 
 		    overviewChartView.setChartModel(model);
 		    /*
 		    bar = new BarChart(BarStyle.GLASS);  
 		    bar.setColour("#0000cc");  
 		    barProvider = new BarDataProvider("betasales");  
 		    barProvider.bind(store);  
 		    bar.setDataProvider(barProvider);  
 		    //bar.addChartListener(listener);  
 		    model.addChartConfig(bar);
 		    */  
 		  
 	}
 	
 	public static Double tryParseDouble(Object o) {
 		if(o == null) {
 			return null;
 		} else if(o instanceof Double) {
 			return (Double)o;
 		}
 		
 		
 		try {
 			return Double.parseDouble(o.toString());
 		} catch(Exception e) {
 			return null;
 		}
 	}
 	
 	
 	public void resetView(int measureIndex) {
 		activeMeasureIndex = measureIndex;
 		resetOverviewChart(measureIndex);
 		resetTimeLineChart();
 	}
 	
 	public void onInit(AppEvent event) {		
 
 		// Make sure the other widgets initialize
 		forwardToView(taskView, event);
 		forwardToView(chartView, event);
 		//forwardToView(overviewChartView, event);
 		
 		
 		Viewport viewport = new Viewport();
 		viewport.setLayout(new FitLayout());
 
 		
 		TabPanel panel = new TabPanel();
 		panel.setLayoutData(new FitLayout());
 		panel.setResizeTabs(true);
 		//panel.setEnableTabScroll(true);
 		//panel.setAnimScroll(true);
 		//panel.setAutoHeight(true);
 
 		TabItem overviewTab = new TabItem();
 		overviewTab.setLayout(new FitLayout());
 		overviewTab.setLayoutOnChange(true);
 		overviewTab.setClosable(false);
 		overviewTab.setText("Linkset Status");
 		//overviewTab.setAutoHeight(true);
 		//overviewTab.setAutoWidth(true);
 		//overviewTab.setSize(500, 500);
 
 		//overviewTab.add(overviewChartView.getWidget());
 		
 		LayoutContainer overviewPanel = new LayoutContainer();
 		overviewPanel.setLayout(new RowLayout()); //Orientation.HORIZONTAL
 		overviewPanel.setScrollMode(Scroll.AUTO);
 		//overviewPanel.setLayout(new TableLayout());
 		//overviewPanel.setSize(500, 500);
 		//overviewPanel.setAutoWidth(true);
 		//overviewPanel.
 		overviewPanel.setLayoutOnChange(true);
 		//overviewPanel.si
 		
 		
 		ContentPanel infoPanel = new ContentPanel();
 		infoPanel.setFrame(true);
 		infoPanel.setHeaderVisible(false);
 		infoPanel.setTitle("Test");
 		Label label = new Label("This tab provides an overview of the latest statistics of the LATC datasets. Click a bar in the diagram to see the corresponding datasets."); // Select a linkset for seeing its timeline.");
 		
 		/*
 		SimpleComboBox<String> combo = new SimpleComboBox<String>();
 		combo.add("Precision");
 		combo.add("Recall");
 		combo.add("Duplicates");
 		
 		combo.setSimpleValue("Precision");
 
 		infoPanel.add(combo);
 		*/
 
 		final ListBox lb = new ListBox();
 		
 		for(PropertyDefinition item : PropertyDefinition.getDefaultMap()) {
 			lb.addItem(item.getDisplayName());
 		}
 				
 		lb.addChangeHandler(new ChangeHandler() {
 			public void onChange(ChangeEvent event) {
 				
 				
 				int index = lb.getSelectedIndex();
 				if(index < 0) {
 					return;
 				}
 				resetView(index);
 			}
 		});
 		
 		infoPanel.add(lb);
 		
 
 		/* Slider for the snapshot date */
 		int margins = 30;
 
 		Slider slider = new Slider();
 	    slider.setWidth(300);
 	    //slider.setIncrement(10);
 	    //slider.setMaxValue(200);
 	    slider.setClickToChange(true);
 
 	    //infoPanel.add(slider, new FillData(margins));
 	    overviewPanel.add(slider);
 		
 	    slider.addListener(Events.Change, new Listener<SliderEvent>() {
 			public void handleEvent(SliderEvent se) {
 				
 				MessageBox.info("info", "new value is: " + se.getNewValue(), null);
 			}
 	    });
 		
 
 		
 		//label.setStyleAttribute("font", "normal 20px courier");
 		infoPanel.add(label);
 		
 		overviewPanel.add(infoPanel);
 		
 		LayoutContainer tmpContainer = new LayoutContainer();
 		tmpContainer.setLayout(new ColumnLayout());
 
 		LayoutContainer chartContainer = new LayoutContainer();
 		chartContainer.setLayout(new FitLayout());
 		chartContainer.setSize(300, 300);
 		
 		
 		chartContainer.add(overviewChartView);
 		tmpContainer.add(chartContainer);
 		linksetGrid.setSize(900,300);
 		//linksetGrid.setAutoHeight(true);
 		tmpContainer.add(linksetGrid);
 		
 		
 		overviewPanel.add(tmpContainer);
 		
 		
 		timeLineChartContainer = new LayoutContainer(new FitLayout());
 		timeLineChartContainer.setLayoutOnChange(true);
 		//timeLineChart.setChartModel(new ChartModel("oaeuaeoueo"));
 		//timeLineChart.setChartModel(model)
 		timeLineChartContainer.setSize(1200, 300);
 		
 		timeLineChartContainer.setBorders(true);
 		//timeLineChartContainer.set
 		
 		noChartLabel = new Label("Select a link set for viewing its timeline");
 		noChartLabel.setStyleAttribute("font-family", "Verdana");
 		noChartLabel.setStyleAttribute("text-align", "center");
 		noChartLabel.setStyleAttribute("color", "#aaaaaa");
 		//noChartLabel.
 		//ttimeLineChartContainer.setV
 		
 		//font-size: 23px;font-weight:bold; f
 		
 		//noChartLabel.set
 		timeLineChartContainer.add(noChartLabel);
 		//timeLineChart = ChartWidget.createChart();
 		//timeLineChart.setChartModel(new ChartModel());
 		//timeLineChartContainer.add(timeLineChart);
 		
 		//timeLineChart.setSize(800, 300);
 		// Trying to get the chart to auto size... but failed so far
 		//timeLineChart.setHeight(300);
 		//chartContainer2.add(timeLineChart, new RowData(-1, 1));
 		//chartContainer2.setSize(800, 300);
 		//chartContainer2.setStyleAttribute("backgrou, value)
 		//overviewPanel.add(chartContainer2);
 		overviewPanel.add(timeLineChartContainer);
 		//overviewPanel.add(timeLineChart);
 		
 		overviewTab.add(overviewPanel);
 		
 		
 		TabItem metricsTab = new TabItem();
 		metricsTab.setLayout(new FitLayout());
 		metricsTab.setLayoutOnChange(true);
 		metricsTab.setClosable(false);
 		metricsTab.setText("Metrics");
 
 		LayoutContainer metricsPanel = new LayoutContainer();
 		metricsPanel.setLayout(new RowLayout()); //Orientation.HORIZONTAL
 		metricsPanel.setScrollMode(Scroll.AUTO);
 		metricsPanel.setLayoutOnChange(true);
 
 
 		summaryPanel = new LayoutContainer(new FitLayout());
 		summaryPanel.setSize(300, 300);
 		summaryPanel.setLayoutOnChange(true);
 		
 		
 		outliersPanel = new LayoutContainer(new ColumnLayout());
 		outliersPanel.setSize(800, 300);
 		outliersPanel.setLayoutOnChange(true);
 		
 		metricsPanel.add(taskView.getTaskGrid());
 		metricsPanel.add(summaryPanel);
 		
 		chartView.getWidget().setSize(1500, 500);
 		
 		metricsPanel.add(chartView.getWidget());
 		metricsPanel.add(outliersPanel);
 		
 		//item.add(new Label("Test Content"));
 
 		
 		// TODO: Outliers and statistics 
 		
 		
 		
 		metricsTab.add(metricsPanel);
 		
 		panel.add(overviewTab);
 		panel.add(metricsTab);
 	
 
 		
 		
 		//((Viewport)RootPanel.get().getWidget(0)).add(panel);
 		
 		viewport.add(panel);
 		RootPanel.get().add(viewport);
 		
 		//System.out.println("Initializing application...");
 		//super.initialize();
 
 		
 		service = (GreetingServiceAsync)Registry.get(Constants.MAIN_SERVICE);
 		
 		resetEvaluationGrid();
 		
 		/*
 		service.test(new AsyncCallback<String>() {			
 			public void onSuccess(String result) {
 				
 				GWT.log("I got here with result " + result);
 				
 				Dispatcher.forwardEvent(AppEvents.Error, result);
 				//ListStore<ModelData> taskStore = Registry.get(Constants.TASK_STORE);
 				//taskStore.add(result);
 				
 				//GWT.log("Loaded " + taskStore.getCount() + " task descriptions");
 				
 				//Dispatcher.forwardEvent(AppEvents.TasksRetrieved, result);
 			}
 			
 			public void onFailure(Throwable caught) {
 				Dispatcher.forwardEvent(AppEvents.Error, caught);
 			}
 		});*/
 		
 
 
 		service.getLatestMetricsEvaluations(new AsyncCallback<Map<String, Model>>() {
 			public void onSuccess(Map<String, Model> result) {
 				ListStore<Model> taskStore = Registry.get(Constants.TASK_STORE);
 
 				for(Entry<String, Model> entry : result.entrySet()) {
 					
 					Model model = entry.getValue();
 					
 					String str = model.get("metricsReport");
 					
 					GWT.log(str);
 					
 					JSONValue json = JSONParser.parseStrict(str);
 					JSONObject map = json.isObject();
 
 					//Map<String, Object> map = JsonConverter.decode(str);
 					
 					//GWT.log(map.keySet().toString());
 					//GWT.log("ss = " + map.get("sampleSize"));
 					model.set("sampled", map.get("sampled").isBoolean());
 					model.set("sampleSize", map.get("sampleSize").isNumber());
 					model.set("direction", map.get("direction").isNumber());
 
 					model.set("metricsReport", map);
 					
 					//String o = entry.getValue().get("metricsReport");
 					taskStore.add(model);
 				}
 				//GWT.log("Loaded " + taskStore.getCount() + " task descriptions");
 				
 				Dispatcher.forwardEvent(AppEvents.TasksRetrieved, result);
 
 				/*
 				GWT.log(result.toString());
 */				
 				
 				
 			}
 
 			public void onFailure(Throwable caught) {
 				Dispatcher.forwardEvent(AppEvents.Error, caught);
 			}
 		});
 		
 		
 		/*
 		service.getTaskDescriptions(new AsyncCallback<List<TaskDescription>>() {			
 			public void onSuccess(List<TaskDescription> result) {
 				
 				GWT.log("Result is " + result);
 				ListStore<ModelData> taskStore = Registry.get(Constants.TASK_STORE);
 				taskStore.add(result);
 				
 				//GWT.log("Loaded " + taskStore.getCount() + " task descriptions");
 				
 				Dispatcher.forwardEvent(AppEvents.TasksRetrieved, result);
 			}
 			
 			public void onFailure(Throwable caught) {
 				Dispatcher.forwardEvent(AppEvents.Error, caught);
 			}
 		});
 		*/
 
 		
 		
 		taskView.getTaskGrid().getGrid().getSelectionModel().addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<Model>>() {
 			public void handleEvent(SelectionChangedEvent<Model> event) {
 				// We need to notify some component that updates the diagrams
 				//MessageBox.alert("Info", "" + event.getSelectedItem(), null);
 				Dispatcher.forwardEvent(AppEvents.TaskSelectionChanged, event.getSelectedItem());
 			}
 		});
 		
 		
 		
 		linksetGrid.getGrid().getSelectionModel().addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<Model>>() {
 			public void handleEvent(SelectionChangedEvent<Model> event) {
 				// We need to notify some component that updates the diagrams
 				//MessageBox.alert("Info", "" + event.getSelectedItem(), null);
 				Dispatcher.forwardEvent(AppEvents.LinksetSelectionChanged, event.getSelectedItem());
 			}
 		});
 
 	}
 	
 	public void resetEvaluationGrid() {
 
 		//service.get
 		
 		service.getLatestEvaluations(new AsyncCallback<Map<String, Model>>() {			
 			public void onSuccess(Map<String, Model> result) {
 			
 				//ListStore<Model> evalStore = Registry.get(Constants.EVALUATION_STORE);
 				//GroupingStore<Model> evalStore = Registry.get(Constants.EVALUATION_STORE);
 				
 				GroupingStore<Model> evalStore = new GroupingStore<Model>();
 				evalStore.groupBy("precision_group");
 				
 				
 				
 				Transformer<Number, Integer> grouper = createGrouper(10, 1.0);
 				
 				for(Entry<String, Model> entry : result.entrySet()) {
 					entry.getValue().set("name", entry.getKey());
 					
 					Model model = entry.getValue();
					double value = model.get("precision");
 					model.set("precision_group", grouper.transform(value));
 
 					value = model.get("recall");
 					model.set("recall_group", grouper.transform(value));
 
 					
 					Double precision = tryParseDouble(entry.getValue().get("precision"));
 					if(precision == null) {
 						continue;
 					}
 					
 					GWT.log("name :" + entry.getValue().get("name") + " prec: " + entry.getValue().get("precision"));
 					
 					evalStore.add(entry.getValue());
 				}
 			
 				
 				GroupingStore<Model> store = Registry.get(Constants.EVALUATION_STORE);
 				store.removeAll();
 				store.add(evalStore.getModels());
 				
 				
 				resetOverviewChart(activeMeasureIndex);
 				
 				//MessageBox.info("yay", "eee", null);
 				//ListStore<ModelData> taskStore = Registry.get(Constants.EVALUATION_STORE);
 				//taskStore.add(result);
 				
 				//GWT.log("Loaded " + taskStore.getCount() + " task descriptions");
 				
 				//Dispatcher.forwardEvent(AppEvents.TasksRetrieved, result);
 			}
 			
 			public void onFailure(Throwable caught) {
 				Dispatcher.forwardEvent(AppEvents.Error, caught);
 			}
 		});
 	}
 	
 	
 	public void onError(AppEvent event) {
 		MessageBox.alert("Error", "" + event.getData(), null);
 	}
 
 	public static final DateTimeFormat dateFormatter = DateTimeFormat.getFormat("MMM d y");
 	
 	public void onLinksetSelectionChanged(AppEvent event) {
 		//MessageBox.info("blah", "aeu", null);
 		
 		Model model = event.getData();
 		
 		activePackageName = (String)model.get("name");
 		//final Format dateFormatter = new SimpleDateFormat("%E %M %y");
 		
 		resetTimeLineChart();
 	}
 
 	public void resetTimeLineChart()
 	{
 		resetTimeLineChart(activePackageName, this.measureNames[this.activeMeasureIndex]);
 	}
 	
 	public void resetTimeLineChart(String packageName, final String measureName) {
 		service.getTimeLineEvaluations(packageName, new AsyncCallback<TimeLinePackage>() {			
 			public void onSuccess(TimeLinePackage result) {
 				
 				ListStore<Model> store = new ListStore<Model>();
 				
 				for(Entry<Date, Model> entry : result.getMap().entrySet()) {
 					//Model tmp = new BaseModel();
 					Model tmp = entry.getValue();
 					
 					String displayDate = dateFormatter.format(entry.getKey());
 					
 					tmp.set("snapshotDate", displayDate);
 					store.add(tmp);
 				}
 									
 				resetTimeLineChart(result.getName(), measureName, store);
 			}
 			public void onFailure(Throwable caught) {
 				Dispatcher.forwardEvent(AppEvents.Error, caught);
 			}
 		});
 	}
 
 	public void resetTimeLineChart(String name, String measureName, ListStore<Model> store) {
 
 		GWT.log("TimeLine: " + name + ", " + measureName);
 
 		if(timeLineChart != null) {
 			timeLineChart.removeFromParent();
 		}
 
 		timeLineChart = ChartWidget.createChart();
 
 		ChartModel chartModel = createTimeLineChartModel("Time line for " + name);
 		noChartLabel.removeFromParent();
 		addTimeLineChart(chartModel, measureName, store);
 		timeLineChart.setChartModel(chartModel);
 
 
 		timeLineChartContainer.add(timeLineChart);
 
 		
 		//store.addFilter(filter)
 		//MessageBox.info("AOEU", "" + result.toString(), null);
 		
 		//ListStore<ModelData> taskStore = Registry.get(Constants.TASK_STORE);
 		//taskStore.add(result);
 		
 		//GWT.log("Loaded " + taskStore.getCount() + " task descriptions");
 		
 		//Dispatcher.forwardEvent(AppEvents.TasksRetrieved, result);
 	}
 
 	
 	
 	public Grid<Model> createSummaryGrid(ListStore<Model> store) {
 		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
 
 		ColumnConfig column = new ColumnConfig();
 		column.setId("metricName");
 		column.setHeader("Metric Name");
 		column.setWidth(150);
 		column.setRowHeader(true);
 		configs.add(column);
 
 		column = new ColumnConfig();
 		column.setId("status");
 		column.setHeader("Status");
 		column.setWidth(150);
 		column.setRowHeader(true);
 		configs.add(column);
 		
 		column = new ColumnConfig();
 		column.setId("distanceChange");
 		column.setHeader("Distance Change");
 		column.setWidth(150);
 		column.setRowHeader(true);
 		configs.add(column);
 		
 		ColumnModel cm = new ColumnModel(configs);
 
 		
 		Grid<Model> grid = new Grid<Model>(store, cm);
 		grid.setStyleAttribute("borderTop", "none");
 		//grid.setAutoExpandColumn("name");
 		grid.setBorders(false);
 		grid.setStripeRows(true);
 		grid.setColumnLines(true);
 		grid.setColumnReordering(true);
 		//grid.getAriaSupport().setLabelledBy(cp.getHeader().getId() + "-label");
 
 		return grid;
 	}
 	
 	
 	public Map<String, Double> extractDoubleMap(JSONObject json) {
 		Map<String, Double> result = new HashMap<String, Double>();
 		
 		for(String key : json.keySet()) {
 			JSONValue value = json.get(key);
 			if(value.isNumber() != null) {
 				result.put(key, value.isNumber().doubleValue());
 			}
 		}
 		
 		return result;
 	}
 	
 	public static <V, K> Map<V, Set<K>> invert(Map<K, V> map) {
 		Map<V, Set<K>> result = new TreeMap<V, Set<K>>();
 		
 		for(Entry<K, V> entry : map.entrySet()) {
 			
 			Set<K> set = result.get(entry.getValue());
 			if(set == null) {
 				set = new TreeSet<K>();
 				result.put(entry.getValue(), set);
 			}
 			
 			set.add(entry.getKey());
 		}
 
 		return result;
 	}
 	
 	
 	
 	public void updateOutliersSummary(JSONObject json) {
 		Map<String, ListStore<Model>> metricToOutliers = new HashMap<String, ListStore<Model>>();
 		
 		for(String metricName : json.keySet()) {
 			JSONObject value = json.get(metricName).isObject();
 			
 			ListStore<Model> store = new ListStore<Model>();
 			
 			if(value != null) {
 				Map<String, Double> map = extractDoubleMap(value);
 				
 				Map<Double, Set<String>> inverted = invert(map);
 				
 				int rank = 0;
 				for(Entry<Double, Set<String>> entry : inverted.entrySet()) {
 					++rank;
 					
 					Model model = new BaseModel();
 					
 					for(String resource : entry.getValue()) {
 						model.set("rank", rank);
 						model.set("distanceChange", entry.getKey());
 						model.set("resource", resource);
 					}
 					
 					store.add(model);
 				}
 			}
 			
 			if(!store.getModels().isEmpty()) {
 				metricToOutliers.put(metricName, store);
 			}
 			
 		}
 
 		chartView.getWidget().setOutliers(metricToOutliers);
 		
 		/*
 		//outliersPanel.removeAll();
 
 		for(Entry<String, ListStore<Model>> entry : metricToOutliers.entrySet()) {
 			
 			
 			//Grid<Model> grid = createOutliersGrid(entry.getValue());			
 			//outliersPanel.add(grid);
 		}*/
 		
 	}
 	
 	
 	public void updateMetricsSummary(JSONObject json) {
 		// Create a model from the json
 		ListStore<Model> store = new ListStore<Model>();
 		
 
 		for(String key : json.keySet()) {
 			Model model = new BaseModel();
 
 			JSONObject value = json.get(key).isObject();
 			model.set("metricName", key);
 
 			//MessageBox.info("title", value.keySet().toString(), null);
 			
 			String status = value.get("status").isString().stringValue();
 			Double distanceChange = value.get("change").isNumber().doubleValue();
 
 			model.set("status", status);
 			model.set("distanceChange", distanceChange);
 			/*
 			for(String k : value.keySet()) {
 				JSONObject v = value.get(k).isObject();
 				
 				if(v.isObject() != null) {
 					// Nothing to do
 				} else if(v.isNumber() != null) {
 					model.set("distanceChange", value.isNumber().doubleValue());				
 				} else if(v.isString() != null) {
 					model.set("status", value.isString().stringValue());
 				}
 			}
 			*/
 			
 			if(!model.getProperties().isEmpty()) {
 				store.add(model);
 			}
 		}
 
 		
 		Grid<Model> grid = createSummaryGrid(store); 
 		
 		summaryPanel.removeAll();
 		summaryPanel.add(grid);
 	}
 	
 	
 	
 	public void onTaskSelectionChanged(AppEvent event) {
 		Model model = event.getData();
 		String packageId = model.get("name");
 		
 		final JSONObject metricsReport = model.get("metricsReport");
 		
 
 		service.getCharts2(packageId, new AsyncCallback<List<MyChart>>() {			
 			public void onSuccess(List<MyChart> result) {
 				
 				
 				Set<String> keys = new TreeSet<String>();
 				for(MyChart model : result) {
 					String name = model.get("type");
 					keys.add(name);
 				}
 				
 				
 				chartView.getWidget().resetSlots(new ArrayList<String>(keys));
 				;
 				chartView.getWidget().setModels(result);
 
 				
 				if(metricsReport != null) {
 					
 					JSONObject summary = metricsReport.get("reportContent").isObject().get("metricStatus").isObject(); 			
 					updateMetricsSummary(summary);
 					
 					
 					JSONObject outliers = metricsReport.get("reportContent").isObject().get("outliers").isObject();
 					updateOutliersSummary(outliers);
 				}
 
 				
 				
 				
 				
 				//Dispatcher.forwardEvent(AppEvents.Error, );
 			}
 			
 			public void onFailure(Throwable caught) {
 				Dispatcher.forwardEvent(AppEvents.Error, caught);
 			}
 		});
 			
 		/*
 		service.getCharts(packageId, new AsyncCallback<List<ChartModel>>() {			
 			public void onSuccess(List<ChartModel> result) {
 				
 				//ListStore<ModelData> chartStore = Registry.get(Constants.CHART_STORE);
 				//chartStore.add(result);
 				
 				//chartView.getWidget().setChartModels(result);
 				
 				//GWT.log("Loaded " + taskStore.getCount() + " task descriptions");
 				
 				//Dispatcher.forwardEvent(AppEvents.TasksRetrieved, result);
 			}
 			
 			public void onFailure(Throwable caught) {
 				Dispatcher.forwardEvent(AppEvents.Error, caught);
 			}
 		});*/
 
 
 	}
 	
 	@Override
 	public void handleEvent(AppEvent event) {
 		if(event.getType().equals(AppEvents.Init)) {
 			onInit(event);
 		} else if(event.getType().equals(AppEvents.Error)) {
 			onError(event);
 		} else if(event.getType().equals(AppEvents.TaskSelectionChanged)) {
 			onTaskSelectionChanged(event);
 		} else if(event.getType().equals(AppEvents.LinksetSelectionChanged)) {
 			onLinksetSelectionChanged(event);
 		}else {
 			forwardToView(taskView, event);
 			forwardToView(chartView, event);
 		}
 	}
 
 }
