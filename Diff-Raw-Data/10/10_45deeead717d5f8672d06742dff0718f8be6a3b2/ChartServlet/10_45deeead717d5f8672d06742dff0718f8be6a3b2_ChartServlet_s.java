 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.gui.impl.servlets;
 
 import java.awt.Color;
 import java.net.URI;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.imageio.IIOException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
 import org.jfree.data.time.Minute;
 import org.jfree.data.time.RegularTimePeriod;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.component.ComponentContext;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 import org.osgi.service.monitor.MonitorAdmin;
 import org.osgi.service.monitor.Monitorable;
 import org.osgi.service.monitor.MonitoringJob;
 import org.osgi.service.monitor.StatusVariable;
 import org.paxle.gui.ALayoutServlet;
 
 /**
  * @scr.component immediate="true" metatype="false"
  * @scr.service interface="javax.servlet.Servlet"
  * @scr.property name="org.paxle.servlet.path" value="/chart"
  * @scr.property name="org.paxle.servlet.doUserAuth" value="false" type="Boolean"
  */
 public class ChartServlet extends ALayoutServlet implements EventHandler, ServiceListener {
 	private static final long serialVersionUID = 1L;	
 	
 	/**
 	 * Memory used by the java runtime
 	 */
 	private static final String TSERIES_MEMORY_USAGE = "java.lang.runtime/memory.used";		
 	
 	/**
 	 * Current size of the lucene index
 	 * @see org.paxle.se.index.IIndexSearcher#getDocCount()
 	 */
 	public static final String TSERIES_INDEX_SIZE = "org.paxle.lucene-db/docs.known";
 	
 	/**
 	 * Total number of {@link URI}s known to the command-db
 	 */
 	public static final String TSERIES_CMD_SIZE_TOTAL = "org.paxle.data.cmddb/size.total";
 	
 	/**
 	 * Number of {@link URI}s enqueued in the command-db
 	 */
 	public static final String TSERIES_CMD_SIZE_ENQUEUD = "org.paxle.data.cmddb/size.enqueued";	
 	
 	/**
 	 * Current PPM of the crawler
 	 * @see org.paxle.core.IMWComponent#getPPM()
 	 */
 	public static final String TSERIES_PPM_CRAWLER = "org.paxle.crawler/ppm";
 	
 	/**
 	 * Current PPM of the parser
 	 * @see org.paxle.core.IMWComponent#getPPM()
 	 */
 	public static final String TSERIES_PPM_PARSER = "org.paxle.parser/ppm";
 	
 	/**
 	 * Current PPM of the indexer
 	 * @see org.paxle.core.IMWComponent#getPPM()
 	 */
 	public static final String TSERIES_PPM_INDEXER = "org.paxle.indexer/ppm";	
 	
 	/**
 	 * The {@link Constants#SERVICE_PID} of the {@link Monitorable} providing
 	 * informations about the cpu usage of the jvm
 	 * 
 	 * @see #TSERIES_CPU_TOTAL
 	 * @see #TSERIES_CPU_USER
 	 * @see #TSERIES_CPU_SYSTEM
 	 */
 	public static final String CPU_MONITORABLE_ID = "os.usage.cpu";
 	
 	/**
 	 * Total CPU Usage
 	 */
 	public static final String TSERIES_CPU_TOTAL = CPU_MONITORABLE_ID + "/cpu.usage.total";
 	
 	/**
 	 * User CPU Usage
 	 */
 	public static final String TSERIES_CPU_USER = CPU_MONITORABLE_ID + "/cpu.usage.user";
 	
 	/**
 	 * System CPU Usage
 	 */
 	public static final String TSERIES_CPU_SYSTEM = CPU_MONITORABLE_ID + "/cpu.usage.system";
 
 	/**
 	 * An arraylist containing all full-path names of all {@link StatusVariable variables}
 	 * to monitor
 	 */
 	private static final String[] TSERIES = new String[] {
 		TSERIES_MEMORY_USAGE,
 		TSERIES_INDEX_SIZE,
 		TSERIES_PPM_CRAWLER,
 		TSERIES_PPM_PARSER,
 		TSERIES_PPM_INDEXER,
 		TSERIES_CPU_TOTAL,
 		TSERIES_CPU_USER,
 		TSERIES_CPU_SYSTEM,
 		TSERIES_CMD_SIZE_TOTAL,
 		TSERIES_CMD_SIZE_ENQUEUD
 	};
 	
 	private HashMap<String,HashSet<String>> variableTree;	
 	
 	/**
 	 * A map containing the fullpath of a {@link StatusVariable} as key and the it's 
 	 * {@link StatusVariable#getType() type} as {@link Integer} 
 	 * 
 	 * This list is required by {@link #handleEvent(Event)} to parse the received value
 	 * 
 	 * @see #addVariables4Monitor(ServiceReference, HashSet, boolean)
 	 * @see #handleEvent(Event)
 	 */
 	private HashMap<String,Integer> typeList;
 
 	/**
 	 * An OSGi bundle-context used to access other services registered to the system
 	 */
 	private BundleContext context;
 	
 	/**
 	 * Currently active monitoring job
 	 */
 	private MonitoringJob currentMonitorJob;
 	
 	/**
 	 * OSGI Monitor Admin Service
 	 * @scr.reference 
 	 */
 	private MonitorAdmin monitorService;
 	
 	/**
 	 * For logging
 	 */
 	private Log logger = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * A map containing all {@link TimeSeries} that are filled by {@link #handleEvent(Event)}
 	 */
 	private HashMap<String,TimeSeries> seriesMap = new HashMap<String,TimeSeries>();
 
 	/**
 	 * A map containing all available {@link JFreeChart charts} as their naomes 
 	 */
 	private HashMap<String, JFreeChart> chartMap = new HashMap<String, JFreeChart>();
 	
 	protected void activate(ComponentContext context) {
 		this.context = context.getBundleContext();
 		
 		this.typeList = new HashMap<String, Integer>();
 		this.variableTree = new HashMap<String, HashSet<String>>();
 		this.buildVariableTree();
 		
 		// create charts
 		this.chartMap.put("mem", this.createMemoryChart());
 		this.chartMap.put("ppm", this.createPPMChart());
 		this.chartMap.put("index", this.createIndexChart());
 		this.chartMap.put("system" ,this.createCPUChart());		
 
 		// registering servlet as event-handler: required to receive monitoring-events
 		Dictionary<String,Object> properties = new Hashtable<String,Object>();
 		properties.put(EventConstants.EVENT_TOPIC, new String[]{"org/osgi/service/monitor"});
 		properties.put(EventConstants.EVENT_FILTER, String.format("(mon.listener.id=%s)",ChartServlet.class.getName()));
 		this.context.registerService(EventHandler.class.getName(), this, properties);						
 		
 		try {
 			// detecting already registered monitorables and
 			// determine which of their variables we need to monitor 
 			final HashSet<String> variableNames = new HashSet<String>();
 			final ServiceReference[] services = this.context.getServiceReferences(Monitorable.class.getName(), null);
 			if (services != null) {
 				for (ServiceReference reference : services) {
 					this.addVariables4Monitor(reference, variableNames, true, true);
 				}
 				this.startScheduledJob(variableNames);
 			}
 			
 			// registering this class as service-listener
 			this.context.addServiceListener(this, String.format("(%s=%s)",Constants.OBJECTCLASS, Monitorable.class.getName()));			
 		} catch (InvalidSyntaxException e) {
 			// this should not occur
 			this.logger.error(e);
 		}
 	}
 		
 	protected void deactivate(ComponentContext context) {
 		this.typeList.clear();
 		this.variableTree.clear();
 		this.chartMap.clear();
 		// TODO:
 	}
 	
 	private void buildVariableTree() {
 		for (String fullPath : TSERIES) {
 			int idx = fullPath.indexOf('/');
 			String monitorableId = fullPath.substring(0, idx);
 			String variableId = fullPath.substring(idx+1);
 			
 			HashSet<String> variableIds;
 			if (variableTree.containsKey(monitorableId)) {
 				variableIds = variableTree.get(monitorableId);
 			} else {
 				variableIds = new HashSet<String>();
 				variableTree.put(monitorableId, variableIds);
 			}
 				
 			variableIds.add(variableId);
 		}
 	}	
 	
 	private JFreeChart createPPMChart() {
 		/*
 		 * INIT TIME-SERIES 
 		 */
         final TimeSeriesCollection dataset = new TimeSeriesCollection();
         
         TimeSeries crawlerPPM = new TimeSeries("Crawler PPM", Minute.class);
         crawlerPPM.setMaximumItemAge(24*60);
         dataset.addSeries(crawlerPPM);
         this.seriesMap.put(TSERIES_PPM_CRAWLER, crawlerPPM);
 
         TimeSeries parserPPM = new TimeSeries("Parser PPM", Minute.class);
         parserPPM.setMaximumItemAge(24*60);
         dataset.addSeries(parserPPM);
         this.seriesMap.put(TSERIES_PPM_PARSER, parserPPM);
         
         TimeSeries indexerPPM = new TimeSeries("Indexer PPM", Minute.class);
         indexerPPM.setMaximumItemAge(24*60);
         dataset.addSeries(indexerPPM);
         this.seriesMap.put(TSERIES_PPM_INDEXER, indexerPPM);
         
 		/*
 		 * INIT CHART
 		 */        
         JFreeChart chart = ChartFactory.createTimeSeriesChart(
                 null,
                 "Time", 
                 "PPM",
                 dataset,
                 true,
                 false,
                 false
             );
         
         // change axis data format
 		((DateAxis) chart.getXYPlot().getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
 		chart.setBackgroundPaint(Color.WHITE);
 		return chart;        
 	}
 	
 	private JFreeChart createCPUChart() {
 		final TimeSeriesCollection dataset = new TimeSeriesCollection();
 		
         TimeSeries series = null;
 
     	series = new TimeSeries("Total CPU Usage", Minute.class);
         series.setMaximumItemAge(24*60);
         dataset.addSeries(series);	        
         this.seriesMap.put(TSERIES_CPU_TOTAL, series);
         
     	series = new TimeSeries("User CPU Usage", Minute.class);
         series.setMaximumItemAge(24*60);
         dataset.addSeries(series);	        
         this.seriesMap.put(TSERIES_CPU_USER, series);
         
     	series = new TimeSeries("System CPU Usage", Minute.class);
         series.setMaximumItemAge(24*60);
         dataset.addSeries(series);	        
         this.seriesMap.put(TSERIES_CPU_SYSTEM, series);           
 		
 		// init chart
         JFreeChart chart = ChartFactory.createTimeSeriesChart(
                 null,
                 "Time", 
                 "Usage [%]",
                 dataset,
                 true,
                 false,
                 false
             );
         
         // change axis data format
 		((DateAxis) chart.getXYPlot().getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
 		chart.getXYPlot().getRangeAxis().setRange(0, 100);
 		chart.getXYPlot().setNoDataMessage("No data available");
 		chart.setBackgroundPaint(Color.WHITE);
 		return chart;   
 	}
 	
 	private JFreeChart createIndexChart() {
 		// init Time-Series
 		TimeSeries indexSizeSeries = new TimeSeries("Index Size", Minute.class);
 		indexSizeSeries.setMaximumItemAge(24*60);
 		this.seriesMap.put(TSERIES_INDEX_SIZE, indexSizeSeries);
 		
 		// init chart
 		JFreeChart chart = ChartFactory.createTimeSeriesChart(
                 null,
                 "Time", 
                 "#Docs",
                 new TimeSeriesCollection(indexSizeSeries),
                 true,
                 false,
                 false
             );
 		
 		XYPlot plot = chart.getXYPlot();
 		
 		final TimeSeriesCollection linksDataset = new TimeSeriesCollection();
 		
 		TimeSeries totalLinksSeries = new TimeSeries("Total URI", Minute.class);
 		totalLinksSeries.setMaximumItemAge(24*60);
 		linksDataset.addSeries(totalLinksSeries);
 		this.seriesMap.put(TSERIES_CMD_SIZE_TOTAL, totalLinksSeries);	
 		
 		TimeSeries enqueuedLinksSeries = new TimeSeries("Enqueued URI", Minute.class);
 		enqueuedLinksSeries.setMaximumItemAge(24*60);
 		linksDataset.addSeries(enqueuedLinksSeries);
 		this.seriesMap.put(TSERIES_CMD_SIZE_ENQUEUD, enqueuedLinksSeries);				
 		
         NumberAxis axis2 = new NumberAxis("#Links");
         axis2.setAutoRangeIncludesZero(false); 
         axis2.setNumberFormatOverride(new DecimalFormat("#,##0"));
 		plot.setRangeAxis(1, axis2);
 		plot.setDataset(1, linksDataset);
 		plot.setRenderer(1, new StandardXYItemRenderer());
 		plot.mapDatasetToRangeAxis(1, 1);
 		
 		NumberAxis axis1 = (NumberAxis) plot.getRangeAxis(0);
 		axis1.setNumberFormatOverride(new DecimalFormat("#,##0"));		
 		
 		// change axis date format
 		((DateAxis) plot.getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
 		chart.setBackgroundPaint(Color.WHITE);
 		return chart;
 	}
 
 	private JFreeChart createMemoryChart() {
     	// init time series
     	TimeSeries usedmemSeries = new TimeSeries("Used MEM", Minute.class);
     	usedmemSeries.setMaximumItemAge(24*60);
     	this.seriesMap.put(TSERIES_MEMORY_USAGE, usedmemSeries);
 		
 		// init collections and chart
         final TimeSeriesCollection dataset = new TimeSeriesCollection();
         dataset.addSeries(usedmemSeries);
         
 		/*
 		 * INIT CHART
 		 */        
         JFreeChart chart = ChartFactory.createTimeSeriesChart(
                 null,
                 "Time", 
                 "Memory [MB]",
                 dataset,
                 true,
                 false,
                 false
             );
         
         // change axis data format
 		((DateAxis) chart.getXYPlot().getDomainAxis()).setDateFormatOverride(new SimpleDateFormat("HH:mm"));
 		
 		long maxMemory = Runtime.getRuntime().maxMemory();
 		if (maxMemory != Long.MAX_VALUE) {
 			((NumberAxis) chart.getXYPlot().getRangeAxis()).setRange(0, (double)maxMemory/(double)(1024*1024));
         }
 		
 		chart.setBackgroundPaint(Color.WHITE);
 		return chart;            
 	}
 	
 	@Override
     protected void doRequest(HttpServletRequest request, HttpServletResponse response) {
 		String chartType = null;
 		try {
 			String display = request.getParameter("display");
 			chartType = request.getParameter("t");
 			
 			if ((display == null || display.equals("plain")) && (chartType != null)) {
 				// getting requested graph size 
 				String wStrg = request.getParameter("w");
 				int width = Integer.valueOf(wStrg == null ? "385": wStrg);
 	
 				String hStrg = request.getParameter("h");
 				int height = Integer.valueOf(hStrg == null ? "200": hStrg);
 	
 				// getting the reuested chart
 				JFreeChart chart = this.chartMap.get(chartType);
 				if (chart == null) {
 					response.setStatus(404);
 					return;
 				}
 	
 				// set response type
 				response.setContentType("image/png");		
 	
 				// render image
 				ServletOutputStream out = response.getOutputStream();
 				ChartUtilities.writeChartAsPNG(out, chart, width, height);
 				out.flush();
 			} else {
 				// using velocity template
 				super.doRequest(request, response);
 			}
 		} catch (Exception e) {
 			if (e instanceof IIOException && 
 				e.getCause() != null && 
 				e.getCause().getCause() != null &&
 				e.getCause().getCause().getMessage() != null && 
 				e.getCause().getCause().getMessage().equalsIgnoreCase("Broken pipe")
 			) {
 				this.logger.debug(String.format(
 						"Broken pipe while writing chart '%s'.",
 						chartType
 				));
 			} else {
 				this.logger.error(String.format(
 						"Unexpected '%s' while writing chart '%s'.",
 						e.getClass().getName(),
 						chartType
 				),e);
 			}
 		}
 	}
 	
 	@Override
 	protected void fillContext(Context context, HttpServletRequest request) {
 		context.put("chartServlet", this);
 		context.put("chartMap", this.chartMap);
 		context.put("chartTypeMap", this.chartMap.keySet());
 	}
 	
 	/**
 	 * Choosing the template to use 
 	 */
 	@Override
 	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
 		return this.getTemplate("/resources/templates/ChartServlet.vm");
 	}	
 
 	/**
 	 * @see EventHandler#handleEvent(Event)
 	 */
 	public void handleEvent(Event event) {
 		String pid = (String) event.getProperty("mon.monitorable.pid");
 		String name = (String) event.getProperty("mon.statusvariable.name");
 		Object value = event.getProperty("mon.statusvariable.value");
 		
 		final String fullPath = pid + "/" + name;
 		this.updateValue(fullPath, value);
 	}
 	
 	/**
 	 * Function to update charts with the newly received value 
 	 * @param fullPath the full-name of the changed {@link StatusVariable}
 	 * @param value the value of a {@link StatusVariable} received via event
 	 */
 	private void updateValue(String fullPath, Object value) {
 		final TimeSeries series = this.seriesMap.get(fullPath); 
 		if (series != null) {
 			Number num = null;
 			
 			/* 
 			 * According to the MA spec the values 
 			 * must be delivered as String 
 			 */
 			if (value instanceof String) {
 				Integer type = this.typeList.get(fullPath);
 				if (type != null) {
 					switch (type.intValue()) {
 						case StatusVariable.TYPE_FLOAT:
 							num = Float.valueOf((String)value);
 							break;
 						
 						case StatusVariable.TYPE_INTEGER:
 							num = Integer.valueOf((String)value);
 							break;							
 							
 						// these types are not supported by the chart-servlet for now
 						/*
 						case StatusVariable.TYPE_BOOLEAN:							
 						case StatusVariable.TYPE_STRING:
 						*/
 							
 						default:
 							break;
 					}
 				}
 				
 			} 
 			/* 
 			 * XXX: this is a bug of the apache felix implementation.
 			 * As defined in the OSGi spec, the value should be delivered
 			 * as String
 			 */
 			else if (value instanceof Number) {
 				num = (Number)value;
 			} else {
 				this.logger.warn(String.format(
 						"Unexpected type of monitoring variable '%s': %s",
 						value.getClass().getSimpleName(),
 						fullPath
 				));
 			}
 			
 			if (num != null) {
 				if (fullPath.equalsIgnoreCase(TSERIES_MEMORY_USAGE)) {
 					num = Integer.valueOf(num.intValue() / ( 1024 * 1024));
 				} else if (fullPath.startsWith(CPU_MONITORABLE_ID)) {
 					num = new Double(num.doubleValue() * 100f);
 				}				
 				series.addOrUpdate(new Minute(new Date()), num);
 			}
 		}
 	}
 
 	/**
 	 * @see ServiceListener#serviceChanged(ServiceEvent)
 	 */
 	public void serviceChanged(ServiceEvent event) {
 		// getting the service reference
 		ServiceReference reference = event.getServiceReference();
 		this.serviceChanged(reference, event.getType());
 	}		
 	
 	private void serviceChanged(ServiceReference reference, int eventType) {
 		if (reference == null) return;
 		if (eventType == ServiceEvent.MODIFIED) return;
 		
 		// ignoring unknown services
 		String pid = (String) reference.getProperty(Constants.SERVICE_PID);
 		if (!variableTree.containsKey(pid)) return;
 		
 		// getting currently monitored variables
 		final HashSet<String> currentVariableNames = new HashSet<String>();
 		if (this.currentMonitorJob != null) {
 			String[] temp = this.currentMonitorJob.getStatusVariableNames();
 			if (temp != null) {
 				currentVariableNames.addAll(Arrays.asList(temp));
 			}
 			
 			try {
 				// stopping old monitoring-job
 				this.currentMonitorJob.stop();
 			} catch (NullPointerException e) {
 				// XXX this is a bug in the MA implementation and should be ignored for now
 				this.logger.debug(e);
 			}
 			this.currentMonitorJob = null;
 		}		
 		
 		// getting variables of changed service
 		final HashSet<String> diffVariableNames = new HashSet<String>();
 		this.addVariables4Monitor(
 				reference, 
 				diffVariableNames, 
 				eventType == ServiceEvent.REGISTERED,
 				eventType == ServiceEvent.REGISTERED
 		);
 		
 		if (eventType == ServiceEvent.REGISTERED) {			
 			// adding new variable
 			currentVariableNames.addAll(diffVariableNames);
 		} else if (eventType == ServiceEvent.UNREGISTERING) {
 			currentVariableNames.removeAll(diffVariableNames);
 		}
 		
 		// restarting monitoring job
 		this.startScheduledJob(currentVariableNames);
 	}
 	
 	/**
 	 * Add the full-path of all {@link StatusVariable variables} of the given {@link Monitorable} into the set
 	 * @param reference a reference to a {@link Monitorable}
 	 * @param variableNames the set where the variable-names should be appended
 	 * @param determineType if <code>true</code> the type of the {@link StatusVariable} is determined via call to
 	 * 					    {@link StatusVariable#getType()} and stored into {@link #typeList}
 	 * @param updateCharts if determineType was enabled and this is <code>true</code> the current value of the {@link StatusVariable}
 	 * 	                   is used to update the chart via call to {@link #updateValue(String, Object)}
 	 */
 	private void addVariables4Monitor(ServiceReference reference, HashSet<String> variableNames, boolean determineType, boolean updateCharts) {
 		String pid = (String) reference.getProperty(Constants.SERVICE_PID);
 		if (!variableTree.containsKey(pid)) return;
 		
 		for (String name : variableTree.get(pid)) {
 			final String fullPath = pid + "/" + name;
 			
 			// append full-path
 			variableNames.add(fullPath);
 			
 			// getting the type of the status-variable
 			if (determineType) {
 				try {
 					Monitorable mon = (Monitorable) this.context.getService(reference);
 					StatusVariable var = mon.getStatusVariable(name);	
 					Integer type = Integer.valueOf(var.getType());
 					
 					this.typeList.put(fullPath, type);
 					
 					// updating charts if requested
 					if (updateCharts) {
 						String value = null;
 						switch (type.intValue()) {
 							case StatusVariable.TYPE_FLOAT:
 								value = Float.toString(var.getFloat());
 								break;
 							case StatusVariable.TYPE_INTEGER:
 								value = Integer.toString(var.getInteger());
 								break;								
 							case StatusVariable.TYPE_BOOLEAN:
 								value = Boolean.toString(var.getBoolean());
 								break;
 							case StatusVariable.TYPE_STRING:
 								value = var.getString();
 								break;
 							default:
 								break;
 						}
 						
 						this.updateValue(fullPath, value);
 					}
 				} catch (Exception e) {
 					this.logger.error(String.format(
 							"Unexpected '%s' while trying to determine type of statusvariable '%s'.",
 							e.getClass().getName(),
 							fullPath
 					), e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Starting a new monitoring job with the given variables to monitor
 	 * @param variableNames full-path of the {@link StatusVariable variables} to monitor
 	 */
 	private void startScheduledJob(Set<String> variableNames) {
 		if (variableNames.size() == 0) return;
 		this.currentMonitorJob = this.monitorService.startScheduledJob(
 				ChartServlet.class.getName(), // listener.id
 				variableNames.toArray(new String[variableNames.size()]),
 				60, // seconds
 				0   // Forever
 		);		
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Iterator<RegularTimePeriod> getMergedPeriods(List<TimeSeries> seriesList) {
 		TreeSet<RegularTimePeriod> periods = new TreeSet<RegularTimePeriod>();
 		
 		for (TimeSeries series : seriesList) {
 			if (series.getItemCount() == 0) continue;
 			periods.addAll(series.getTimePeriods());
 		}
 		
 		return periods.iterator();
 	}
 }
