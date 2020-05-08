 /**
  * 
  */
 package org.helios.hiex.agent.tracer.ctile;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.wily.introscope.agent.IAgent;
 import com.wily.introscope.agent.stat.DataAccumulatorFactory;
 import com.wily.introscope.agent.stat.IDataAccumulator;
 import com.wily.introscope.agent.stat.IIntegerAverageDataAccumulator;
 import com.wily.introscope.agent.trace.ASingleMetricTracerFactory;
 import com.wily.introscope.agent.trace.INameFormatter;
 import com.wily.introscope.agent.trace.InvocationData;
 import com.wily.introscope.agent.trace.ProbeIdentification;
 import com.wily.util.StringUtils;
 import com.wily.util.feedback.IModuleFeedbackChannel;
 import com.wily.util.feedback.Module;
 import com.wily.util.heartbeat.ITimestampedRunnable;
 import com.wily.util.properties.AttributeListing;
 
 
 /**
  * <p>Title: MethodTimerCtile</p>
  * <p>Description: Introscope Agent Tracer to trace the nth percentile value during
  * an interval.
  * <ul>
  * <li>The threshold elapsed time value of the nth percentile.</li>
  * <li>The number of transactions with elapsed times at or below the nth
  * percentile.</li>
  * <li>The number of transactions with elapsed times above the nth percentile.</li>
  * <li>The percentage of transactions with elapsed times at or below the nth
  * percentile.</li>
  * <li>The percentage of transactions with elapsed times above the nth
  * percentile.</li>
  * </ul></p> 
  * <p>Company: Helios Development Group LLC</p>
  * @author Whitehead (nwhitehead AT heliosdev DOT org)
  * <p><code>org.helios.hiex.agent.tracer.ctile.MethodTimerCtile</code></p>
  */
 public class MethodTimerCtile extends ASingleMetricTracerFactory implements
 		ITimestampedRunnable {
 	/** The agent logging channel */
 	private IModuleFeedbackChannel log;
 	/** The defined formatter */
 	private INameFormatter formatter = null;
 	/** The agent's tracer factory */
 	private DataAccumulatorFactory accumulatorFactory = null;
 	/** The percentile to calculate */
 	private int percentile = 0;
 	/** Metric name --> interval and data accumulators map */
 	private Map<String, CtileMetricAccumulator> accumulators = new ConcurrentHashMap<String, CtileMetricAccumulator>(
 			100);
 	/** Module */
 	private static final Module module = new Module("MethodTimerCtile");
 	/** Alternator */
 	private AtomicBoolean alternator = new AtomicBoolean(false);
 	/** Trace performance flag */
 	private boolean tracePerformance = false;
 	/** debug output flag */
 	private boolean debug = false;
 	/** The map of constants to configured sub metric names */
 	private Map<String, String> metricNameMap = new HashMap<String, String>();
 	/** The pbd defined resource pattern */
 	private String formattedResource = null;
 	/** The name of the percentile based resource segment */
 	private String percentileResourceName = null;
 	/** The name of the summary percentile based resource segment */
 	private String summaryPercentileResourceName = null;
 	
 	/** The key of the percentile parameter */
 	public static final String PERCENTILE_PARAM = "percentile";
 	/** The key of the performance parameter */
 	public static final String PERCENTILE_PERF = "performance";
 	/** The key of the performance period window */
 	public static final String PERCENTILE_PERIOD = "period";
 
 	/** The counter key of the percentile elapsed time */
 	public static final String PERCENTILE_ELAPSED = "percentileelapsed";
 	/** The counter key of the count at or below percentile */
 	public static final String COUNT_LTOE_PERCENTILE = "countltoe";
 	/** The counter key of the count above percentile */
 	public static final String COUNT_GT_PERCENTILE = "countgt";
 
 	/** The counter key of the mean */
 	public static final String MEAN_ELAPSED = "mean";
 	/** The counter key of the count */
 	public static final String COUNT_ELAPSED = "count";
 	/** The counter key of the standard deviation */
 	public static final String STDDEV_ELAPSED = "stddev";
 	/** The counter key of the debug output */
 	public static final String DEBUG = "debug";
 
 	/*
 	 * ===================== Formatting Resources =====================
 	 * Formatted
 	 * Name:WebServices|ByCaller|{servicename}|{operationname}|{header/SessionHeader/ns1/http;--example.org/username}:90
 	 * Average Response Time (ms) Formatted
 	 * Resource:WebServices|ByCaller|{servicename}|{operationname}|{header/SessionHeader/ns1/http;--example.org/username}
 	 * Name
 	 * Parameter:WebServices|ByCaller|{servicename}|{operationname}|{header/SessionHeader/ns1/http;--example.org/username}:90
 	 * Average Response Time (ms)
 	 * Resource:WebServices|ByCaller|{servicename}|{operationname}|{header/SessionHeader/ns1/http;--example.org/username}
 	 * =====================
 	 */
 
 	/**
 	 * Creates a new MethodTimeCtile tracer instance.
 	 * 
 	 * @param agent
 	 * @param parameters
 	 * @param probe
 	 * @param sampleTracedObject
 	 */
 	public MethodTimerCtile(IAgent agent, AttributeListing parameters,
 			ProbeIdentification probe, Object sampleTracedObject) {
 		super(agent, parameters, probe, sampleTracedObject);
 		formatter = this.getCustomNameFormatter();
 		formattedResource = this.getFormattedName();		
 		accumulatorFactory = getDataAccumulatorFactory();		
 		log = agent.IAgent_getModuleFeedback();
 		if (formatter == null) {
			log.info("\n\n\tCustom Name Formatter is null");
 		} else {
 			log.info("\n\n\tCustom Name Formatter:"
 					+ formatter.getClass().getName());
 		}
 		if (log.isDebugEnabled()) {
 			StringBuilder b = new StringBuilder(
 					"\n\n\n\n\t=====================\n\tFormatting Resources\n\t=====================");
 			b.append("\n\t").append("Formatted Name:").append(
 					this.getFormattedName());
 			b.append("\n\t").append("Formatted Resource:").append(
 					this.getFormattedResource());
 			b.append("\n\t=====================\n");
 			log.info(b.toString());
 			System.out.println(b.toString());
 		}
 
 		String ctile = getTracerParameterValue(PERCENTILE_PARAM, "90");
 		String periodParam = getTracerParameterValue(PERCENTILE_PERIOD, "15000");
 		try {
 			tracePerformance = Boolean.parseBoolean(getTracerParameterValue(
 					PERCENTILE_PERF, "false"));
 		} catch (Exception e) {
 			tracePerformance = false;
 		}
 		try {
 			debug = Boolean
 					.parseBoolean(getTracerParameterValue(DEBUG, "false"));
 		} catch (Exception e) {
 			debug = false;
 		}
 
 		percentile = Integer.parseInt(ctile);
 		percentileResourceName = "Percentile " + percentile;
 		summaryPercentileResourceName = getCtileCalculationResourceName(formattedResource, percentileResourceName);
 		long period = Long.parseLong(periodParam);
 
 		initPatterns();
 		agent.IAgent_getCommonHeartbeat().addBehavior(this, module.getName(), true, period, true);
 		if (debug) {
 			getDataAccumulatorFactory().safeGetLongFluctuatingCounterDataAccumulator(summaryPercentileResourceName + "|Debug:Percentile Period (ms)").ILongCounterDataAccumulator_setValue(period);
 		}
 	}
 
 	/**
 	 * Initializes the parameterized metric names. Except for
 	 * percentile_elapsed, if the parameter value is null, the metric will not
 	 * be traced.
 	 */
 	private void initPatterns() {
 
 		String tmp = getTracerParameterValue(PERCENTILE_ELAPSED, "Elapsed Time Ctile");
 		if (tmp != null) metricNameMap.put(PERCENTILE_ELAPSED, percentileResourceName + ":" + tmp);
 
 		tmp = getTracerParameterValue(COUNT_LTOE_PERCENTILE, null);
 		if (tmp != null) metricNameMap.put(COUNT_LTOE_PERCENTILE, percentileResourceName + ":" + tmp);
 		
 		tmp = getTracerParameterValue(COUNT_GT_PERCENTILE, null);
 		if (tmp != null) metricNameMap.put(COUNT_GT_PERCENTILE, percentileResourceName + ":" + tmp);
 
 		tmp = getTracerParameterValue(MEAN_ELAPSED, null);
 		if (tmp != null) metricNameMap.put(MEAN_ELAPSED, percentileResourceName + ":" + tmp);
 
 		tmp = getTracerParameterValue(COUNT_ELAPSED, null);
 		if (tmp != null) metricNameMap.put(COUNT_ELAPSED, percentileResourceName + ":" + tmp);
 
 		tmp = getTracerParameterValue(STDDEV_ELAPSED, null);
 		if (tmp != null) metricNameMap.put(STDDEV_ELAPSED, percentileResourceName + ":" + tmp);
 
 
 		if (tracePerformance) {
 			metricNameMap.put(PERCENTILE_PERF, percentileResourceName
 					+ "|Debug:Percentile Processing Time (ms)");
 		}
 		if (debug) {
 			metricNameMap.put(DEBUG, percentileResourceName
 					+ "|Debug:Debug Output");
 		}
 
 		if (log.isDebugEnabled()) {
 			StringBuilder b = new StringBuilder(
 					"\nMethodTimerCtile Parameterized Metric Names for " + this.percentileResourceName + ":");
 			for (Entry<String, String> entry : metricNameMap.entrySet()) {
 				b.append("\n\t").append(entry.getKey()).append(":").append(
 						entry.getValue());
 			}
 			b.append("\n");
 			log.debug(b.toString());			
 		}
 	}
 
 	/**
 	 * Callback from the agent scheduler on every percentile window period.
 	 * 
 	 * @param t
 	 * @see com.wily.util.heartbeat.ITimestampedRunnable#ITimestampedRunnable_execute(long)
 	 */
 	public void ITimestampedRunnable_execute(long t) {
 		long start = System.currentTimeMillis();
 		int count = 0;
 		alternator.set(!alternator.get());
 		for (Entry<String, CtileMetricAccumulator> acc : accumulators
 				.entrySet()) {
 			try {
 				if (log.isDebugEnabled()) {
 					log.debug("Interval Calc Percentile Issuing for ["
 							+ acc.getKey() + "]");
 				}
 				acc.getValue().calcAndPublishInterval();
 				count++;
 			} catch (Exception e) {
 				if (log.isErrorEnabled(module)) {
 					log.error("Interval Calc Percentile Failure for ["
 							+ acc.getKey() + "]", e);
 				}
 			}
 		}
 		long elapsed = System.currentTimeMillis() - start;
 		if (debug) {
 			getDataAccumulatorFactory().safeGetLongAverageDataAccumulator(
 					this.summaryPercentileResourceName
 							+ "|Debug:Total Percentile Calc Elapsed Time (ms)")
 					.ILongAggregatingDataAccumulator_recordDataPoint(elapsed);
 			getDataAccumulatorFactory()
 					.safeGetIntegerFluctuatingCounterDataAccumulator(
 							this.summaryPercentileResourceName
 									+ "|Debug:Total Percentile Calc Items Count")
 					.IIntegerCounterDataAccumulator_setValue(count);
 		}
 	}
 
 	/**
 	 * Acquires the current interval accumulator.
 	 * 
 	 * @param counterName
 	 * @return
 	 */
 	protected CtileMetricAccumulator getCtileMetricAccumulator(
 			String counterName) {
 		CtileMetricAccumulator cma = accumulators.get(counterName);
 		if (cma == null) {
 			if(log.isDebugEnabled()) {
 				log.debug("Creating CtileMetricAccumulator for [" + counterName + "]" );
 			}
 			cma = new CtileMetricAccumulator(percentile, alternator,
 					tracePerformance, debug, accumulatorFactory, counterName,
 					metricNameMap, log);
 			accumulators.put(counterName, cma);
 		}
 		return cma;
 	}
 
 	/**
 	 * @param formattedMetricName
 	 * @return
 	 * @see com.wily.introscope.agent.trace.ASingleMetricTracerFactory#createDataAccumulator(java.lang.String)
 	 */
 	protected final IDataAccumulator createDataAccumulator(
 			String formattedMetricName) {
 		return getDataAccumulatorFactory()
 				.safeGetIntegerAverageDataAccumulator(formattedMetricName);
 	}
 
 	protected String getCtileCalculationResourceName(String fmtResource, String pctResource) {
 		String ctileResource = fmtResource;
 		Pattern p = Pattern.compile("(\\{.*?})");
 		Matcher m = p.matcher(fmtResource);
 		if(m.find()) {
 			ctileResource = m.replaceAll("");			
 		}
 		ctileResource = ctileResource.replaceAll("\\|{2,}", "|");
 		if(ctileResource.endsWith("|")) ctileResource = ctileResource.substring(0, ctileResource.length()-1);
 		return ctileResource + "|" + pctResource;
 
 	}
 
 	/**
 	 * @param tracerIndex
 	 * @param data
 	 * @see com.wily.introscope.agent.trace.ITracer#ITracer_startTrace(int,
 	 *      com.wily.introscope.agent.trace.InvocationData)
 	 */
 	public void ITracer_startTrace(int tracerIndex, InvocationData data) {
 		data.storeWallClockStartTime();
 		addToBlameStackIfEnabled(data);
 	}
 
 	/**
 	 * Callback from the agent on instrumented method completion.
 	 * 
 	 * @param tracerIndex
 	 * @param data
 	 * @see com.wily.introscope.agent.trace.ITracer#ITracer_finishTrace(int,
 	 *      com.wily.introscope.agent.trace.InvocationData)
 	 */
 	public void ITracer_finishTrace(int tracerIndex, InvocationData data) {
		String metricName = formatter.INameFormatter_format(formattedResource, data);
 		com.wily.introscope.stat.blame.BlameStackSnapshot snapshot = removeFromBlameStackIfEnabledAndReturnSnapshot(data);
 		IIntegerAverageDataAccumulator average = (IIntegerAverageDataAccumulator) getDataAccumulator(metricName + ":Average Elapsed Time (ms)");
 		//this.getAgent().IAgent_getDataAccumulatorFactory().getIntegerAverageDataAccumulator("");
 		
 		int time = data.getWallClockElapsedTimeAsInt();
 		if (!average.IDataAccumulator_isShutOff()) {
 			average.IIntegerAggregatingDataAccumulator_recordDataPoint(time, 	snapshot);
 		}		
 		getCtileMetricAccumulator(metricName).addElapsedTime(time);
 	}
 
 	/**
 	 * @param name
 	 * @param defaultValue
 	 * @return
 	 */
 	protected String getTracerParameterValue(String name, String defaultValue) {
 		String v = getParameter(name);
 		if (StringUtils.isEmpty(v))
 			return defaultValue;
 		else
 			return v.trim();
 	}
 
 }
 
 /*
  * public static int calcPercentile(boolean weighted, int percentile,
  * int...values) { if(weighted) return calcWeightedPercentile(percentile,
  * values); else return calcPercentile(percentile, values); } private static int
  * calcPercentile(int percentile, int...values) { if(values==null ||
  * values.length < 1 || (percentile <1 || percentile >99)) throw new
  * IllegalArgumentException("Invalid values or percentile"); TIntArrayList
  * valuesArray = new TIntArrayList(values); int max = valuesArray.max(); return
  * calcPercentile(max, percentile); } private static int
  * calcWeightedPercentile(int percentile, int...values) { if(values==null ||
  * values.length < 1 || (percentile <1 || percentile >99)) throw new
  * IllegalArgumentException("Invalid values or percentile"); TIntArrayList
  * valuesArray = new TIntArrayList(values); int max = valuesArray.max(); int min =
  * valuesArray.min(); int range = max-min; return calcPercentile(range,
  * percentile); }
  */
 
 /*
  * private void initCounters() { String formattedName = getFormattedName();
  * DataAccumulatorFactory af = getDataAccumulatorFactory();
  * 
  * if(patterns.get(PERCENTILE_ELAPSED)!=null) { counters.put(PERCENTILE_ELAPSED,
  * af.safeGetIntegerFluctuatingCounterDataAccumulator(
  * formattedName.split(":")[0] + "|" + percentile + "th Percentile:" +
  * patterns.get(PERCENTILE_ELAPSED)));
  * if(log.isDebugEnabled())log.debug("Created Counter [" +
  * counters.get(PERCENTILE_ELAPSED)); }
  * if(patterns.get(COUNT_LTOE_PERCENTILE)!=null) {
  * counters.put(COUNT_LTOE_PERCENTILE,
  * af.safeGetIntegerFluctuatingCounterDataAccumulator(
  * formattedName.split(":")[0] + "|" + percentile + "th Percentile:" +
  * patterns.get(COUNT_LTOE_PERCENTILE)));
  * if(log.isDebugEnabled())log.debug("Created Counter [" +
  * counters.get(COUNT_LTOE_PERCENTILE)); }
  * if(patterns.get(COUNT_GT_PERCENTILE)!=null) {
  * counters.put(COUNT_GT_PERCENTILE,
  * af.safeGetIntegerFluctuatingCounterDataAccumulator(
  * formattedName.split(":")[0] + "|" + percentile + "th Percentile:" +
  * patterns.get(COUNT_GT_PERCENTILE)));
  * if(log.isDebugEnabled())log.debug("Created Counter [" +
  * counters.get(COUNT_GT_PERCENTILE)); } if(patterns.get(MEAN_ELAPSED)!=null) {
  * counters.put(MEAN_ELAPSED,
  * af.safeGetIntegerFluctuatingCounterDataAccumulator(
  * formattedName.split(":")[0] + "|" + percentile + "th Percentile:" +
  * patterns.get(MEAN_ELAPSED))); if(log.isDebugEnabled())log.debug("Created
  * Counter [" + counters.get(MEAN_ELAPSED)); }
  * if(patterns.get(COUNT_ELAPSED)!=null) { counters.put(COUNT_ELAPSED,
  * af.safeGetIntegerFluctuatingCounterDataAccumulator(
  * formattedName.split(":")[0] + "|" + percentile + "th Percentile:" +
  * patterns.get(COUNT_ELAPSED))); if(log.isDebugEnabled())log.debug("Created
  * Counter [" + counters.get(COUNT_ELAPSED)); }
  * if(patterns.get(STDDEV_ELAPSED)!=null) { counters.put(STDDEV_ELAPSED,
  * af.safeGetIntegerFluctuatingCounterDataAccumulator(
  * formattedName.split(":")[0] + "|" + percentile + "th Percentile:" +
  * patterns.get(STDDEV_ELAPSED))); if(log.isDebugEnabled())log.debug("Created
  * Counter [" + counters.get(STDDEV_ELAPSED)); } if(tracePerformance) {
  * counters.put(PERCENTILE_PERF, af.safeGetLongAverageDataAccumulator(
  * formattedName.split(":")[0] + "|" + percentile + "th Percentile:" +
  * patterns.get(PERCENTILE_PERF))); if(log.isDebugEnabled())log.debug("Created
  * Counter [" + counters.get(PERCENTILE_PERF)); } if(debug) {
  * counters.put(DEBUG, af.safeGetStringEveryEventDataAccumulator(
  * formattedName.split(":")[0] + "|" + percentile + "th Percentile|Debug:" +
  * patterns.get(DEBUG))); if(log.isDebugEnabled())log.debug("Created Counter [" +
  * counters.get(DEBUG)); counters.put(DEBUG_MEMORY,
  * af.safeGetLongFluctuatingCounterDataAccumulator( formattedName.split(":")[0] +
  * "|" + percentile + "th Percentile|Debug:" + patterns.get(DEBUG_MEMORY)));
  * if(log.isDebugEnabled())log.debug("Created Counter [" +
  * counters.get(DEBUG_MEMORY)); counters.put(DEBUG_DEEP_MEMORY,
  * af.safeGetLongFluctuatingCounterDataAccumulator( formattedName.split(":")[0] +
  * "|" + percentile + "th Percentile|Debug:" +
  * patterns.get(DEBUG_DEEP_MEMORY))); if(log.isDebugEnabled())log.debug("Created
  * Counter [" + counters.get(DEBUG_DEEP_MEMORY)); counters.put(DEBUG_ACC_MEMORY,
  * af.safeGetLongFluctuatingCounterDataAccumulator( formattedName.split(":")[0] +
  * "|" + percentile + "th Percentile|Debug:" + patterns.get(DEBUG_ACC_MEMORY)));
  * if(log.isDebugEnabled())log.debug("Created Counter [" +
  * counters.get(DEBUG_ACC_MEMORY)); }
  * 
  * 
  * if(log.isDebugEnabled()) { StringBuilder b = new
  * StringBuilder("\nMethodTimerCtile Metric CounterTypes:"); for(Entry<String,
  * IDataAccumulator> entry: counters.entrySet()) {
  * entry.getValue().IDataAccumulator_getMetric();
  * b.append("\n\t").append(entry.getKey()).append(":").append(entry.getValue().getClass().getName());
  * b.append("\n\t\t").append(entry.getValue().IDataAccumulator_getMetric().getSuffixingAttributeURL()); }
  * b.append("\n"); log.debug(b.toString()); } }
  */
