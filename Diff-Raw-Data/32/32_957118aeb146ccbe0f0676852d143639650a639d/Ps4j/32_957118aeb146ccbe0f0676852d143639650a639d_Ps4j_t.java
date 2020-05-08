 package com.github.adeshmukh.ps4j;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.collect.Collections2.transform;
 import static com.google.common.collect.Lists.newArrayList;
 import static java.lang.String.format;
 import static java.util.concurrent.Executors.newFixedThreadPool;
 
 import java.lang.management.ManagementFactory;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sun.jvmstat.monitor.MonitorException;
 import sun.jvmstat.monitor.MonitoredHost;
 import sun.jvmstat.monitor.VmIdentifier;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.base.Predicate;
 import com.google.common.collect.FluentIterable;
 import com.google.common.collect.ImmutableSortedSet;
 import com.google.common.collect.Iterables;
 
 /**
  * Entry point into the ps4j api.
  *
  * @author adeshmukh
  */
 @SuppressWarnings("restriction")
 public class Ps4j {
 
     // Ignoring parameterization of Measure since using the non-parameterized aspect of Measure
     @SuppressWarnings("rawtypes")
     private final Predicate<Measure> METRICS_FILTER = new Predicate<Measure>() {
         @Override
         public boolean apply(Measure input) {
             return config.hasMetric(input.getMetric().getName());
         }
     };
 
     private final Function<Record, Record> RECORD_FIELD_STRAINER = new Function<Record, Record>() {
         @Override
         public Record apply(Record input) {
             return Record.create().addAll(Iterables.filter(input.getMeasures(), METRICS_FILTER));
         }
     };
 
     private static final Logger log = LoggerFactory.getLogger(Ps4j.class);
 
     private static final String VMID_TEMPLATE = "//%s?mode=r";
 
     private static final Predicate<Record> NOOP_RECORDS_FILTER = new Predicate<Record>() {
 
         @Override
         public boolean apply(Record input) {
             return input != Record.NOOP;
         }
     };
 
     private static final Function<Future<Record>, Record> FUTURE_TO_RECORD_TRANSFORMER = new Function<Future<Record>, Record>() {
 
         @Override
         public Record apply(Future<Record> future) {
             try {
                 return future.get();
             } catch (Exception e) {
                 return Record.NOOP;
             }
         }
     };
 
     private static final Comparator<Metric<?>> METRIC_NAME_COMPARATOR = new Comparator<Metric<?>>() {
 
         @Override
         public int compare(Metric<?> m0, Metric<?> m1) {
             return m0.getName().compareTo(m1.getName());
         }
     };
 
     private static final Function<Metric<?>, String> GET_NAME = new Function<Metric<?>, String>() {
         @Override
         public String apply(Metric<?> metric) {
             return metric.getName();
         }
     };
 
     private MonitoredHost monitoredHost;
     private Ps4jConfig config;
     private Iterable<Record> records;
 
     public static enum Ps4jAction {
         OPTIONS, MEASURE
     }
 
     public Ps4j(Ps4jConfig config) throws Ps4jException {
         checkArgument(config != null, "config cannot be null");
 
         try {
             this.config = config;
             monitoredHost = MonitoredHost.getMonitoredHost(config.getHostname());
         } catch (Exception e) {
             throw new Ps4jException(e);
         }
     }
 
     public Collection<Metric<?>> options() throws Ps4jException {
         ImmutableSortedSet.Builder<Metric<?>> builder = ImmutableSortedSet.<Metric<?>> orderedBy(METRIC_NAME_COMPARATOR);
         for (Meter meter : config.getMeters()) {
             builder.addAll(meter.supportedMetrics());
         }
         Set<Metric<?>> retval = builder.build();
         List<String> metricNames = config.getMetricNames();
         if (!metricNames.isEmpty()) {
             List<String> badMetrics = newArrayList(metricNames);
             badMetrics.removeAll(transform(retval, GET_NAME));
             if (!badMetrics.isEmpty()) {
                 throw new Ps4jException("Invalid metrics specified: " + Joiner.on(",").join(badMetrics));
             }
         }
         return retval;
     }
 
     public Iterable<Record> measure() throws Ps4jException {
         options(); // validate config.getMetricNames()
 
         ExecutorService threadPool = null;
         try {
             // 1. Prepare input for execution
             List<VmIdentifier> vmIds = monitoredVmIds(monitoredHost);
             log.debug("Available vmIds: [{}]", vmIds);
 
             // 2. Execute in threadpool
             int numThreads = (int) (vmIds.size() * config.getConcurrencyFactor());
             log.debug("Num threads: [{}]", numThreads);
             threadPool = newFixedThreadPool(numThreads);
             List<Future<Record>> results = new ArrayList<Future<Record>>(vmIds.size());
             for (VmIdentifier vmId : vmIds) {
                 Future<Record> recordHolder = threadPool.submit(newMeasureMonitorsTask(vmId));
                 results.add(recordHolder);
             }
 
             // 3. Prepare and display output
             records = FluentIterable.from(results)
                     .transform(FUTURE_TO_RECORD_TRANSFORMER)
                     .filter(NOOP_RECORDS_FILTER)
                     .transform(RECORD_FIELD_STRAINER);
             log.debug("Available records: [{}]", Iterables.size(records));
         } catch (Exception e) {
             throw new Ps4jException(e);
         } finally {
             if (threadPool != null)
                 threadPool.shutdown();
         }
         return records;
     }
 
     private Callable<Record> newMeasureMonitorsTask(VmIdentifier vmId) {
         Collection<Meter> meters = new LinkedList<Meter>();
         Iterables.addAll(meters, config.getMeters());
         return new Ps4jTask(monitoredHost, vmId, meters);
     }
 
     private List<VmIdentifier> monitoredVmIds(MonitoredHost monitoredHost) throws RuntimeException {
         List<VmIdentifier> vmIds = null;
         try {
 
             Integer currentVmId = currentVmId();
 
             @SuppressWarnings("unchecked")
             Set<Number> vmIdNums = monitoredHost.activeVms();
             vmIds = new ArrayList<VmIdentifier>(vmIdNums.size());
             for (Number vmId : vmIdNums) {
                 if (currentVmId.intValue() == vmId.intValue()) {
                     continue;
                 }
                 VmIdentifier vmIdentifier = new VmIdentifier(format(VMID_TEMPLATE, vmId));
                 vmIds.add(vmIdentifier);
             }
         } catch (URISyntaxException ue) {
             throw new RuntimeException(ue);
         } catch (MonitorException me) {
             throw new RuntimeException(me);
         }
         return vmIds;
     }
 
     private static Integer currentVmId() {
         try {
             return Integer.parseInt(System.getProperty("sun.java.launcher.pid"));
         } catch (Exception e1) {
             try {
                 log.debug("currentVmId by System property failed, attempting using JMX");
                 String mxname = ManagementFactory.getRuntimeMXBean().getName();
                 return Integer.parseInt(mxname.split("@")[0]);
             } catch (Exception e2) {
                 log.error("currentVmId by JMX failed. Returning default. Current Process may not be filtered in output");
             }
         }
         return -1;
     }
 
 }
