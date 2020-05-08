 package tsdaggregator;
 
 import org.apache.log4j.Logger;
 import org.joda.time.Period;
 
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: barp
  * Date: 9/14/12
  * Time: 11:38 PM
  * To change this template use File | Settings | File Templates.
  */
 public class LineProcessor {
     private Class parserClass;
     private Set<Statistic> statisticsClasses;
     private String hostName;
     private String serviceName;
     private Set<Period> periods;
     private AggregationListener listener;
    private HashMap<String, TSData> aggregations;
     static final Logger _Logger = Logger.getLogger(LineProcessor.class);
 
    public LineProcessor(Class parserClass, Set<Statistic> statisticsClasses, String hostName, String serviceName, Set<Period> periods, AggregationListener listener, HashMap<String, TSData> aggregations) {
         this.parserClass = parserClass;
         this.statisticsClasses = statisticsClasses;
         this.hostName = hostName;
         this.serviceName = serviceName;
         this.periods = periods;
         this.listener = listener;
         this.aggregations = aggregations;
     }
 
     public boolean invoke(String line) {
         LogLine data = null;
         try {
             data = (LogLine) parserClass.newInstance();
         } catch (InstantiationException ex) {
             _Logger.error("Could not instantiate LogLine parser", ex);
             return true;
         } catch (IllegalAccessException ex) {
             _Logger.error("Could not instantiate LogLine parser", ex);
             return true;
         }
 
         data.parseLogLine(line);
         for (Map.Entry<String, CounterVariable> entry : data.getVariables().entrySet()) {
             TSData tsdata = aggregations.get(entry.getKey());
             if (tsdata == null) {
                 tsdata = new TSData(entry.getKey(), periods, listener, hostName, serviceName, statisticsClasses);
                 aggregations.put(entry.getKey(), tsdata);
             }
             tsdata.addMetric(entry.getValue().getValues(), data.getTime());
         }
         return false;
     }
 }
