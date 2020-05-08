 package com.od.jtimeseries.server.servermetrics.jmx;
 
 import com.od.jtimeseries.timeseries.function.aggregate.AbstractDelegatingAggregateFunction;
 import com.od.jtimeseries.timeseries.function.aggregate.AggregateFunction;
 import com.od.jtimeseries.timeseries.function.aggregate.AggregateFunctions;
 import com.od.jtimeseries.util.numeric.DoubleNumeric;
 import com.od.jtimeseries.util.numeric.Numeric;
 
 /**
  * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 04-Feb-2010
 * Time: 10:10:52
 *
 *  Several jmx attributes represent total time (e.g. total process CPU time)
  *
 *  For each time period, we want to measure the increase in total time as a
  * percentage of time elapsed over the period we are monitoring.
 *
 *  e.g. If the process has 500ms added to total process CPU time in a 1000ms period, then it has used 50% of a processor
 *       (if there are more than one processors, this value may exceed 100%, which is expected)
 */
 class JmxPercentageOfTimeFunction extends AbstractDelegatingAggregateFunction {
 
    private long lastRecordedTimeValue;
     private long millisInPeriod;
 
     public JmxPercentageOfTimeFunction(long millisInPeriod) {
         this(0, millisInPeriod);
     }
 
    JmxPercentageOfTimeFunction(long lastRecordedTimeValue, long millisInPeriod) {
         super(AggregateFunctions.SUM());
        this.lastRecordedTimeValue = lastRecordedTimeValue;
         this.millisInPeriod = millisInPeriod;
     }
 
     public Numeric calculateAggregateValue() {
         double result = Double.NaN;
         Numeric newCollectionTime = super.calculateAggregateValue();
         if ( ! Double.isNaN(newCollectionTime.doubleValue())) {
            if ( lastRecordedTimeValue != 0) {
                long difference = newCollectionTime.longValue() - lastRecordedTimeValue;
                 result = (100 * difference) / (double)millisInPeriod;
                 //System.out.println("Calculated percentage " + result);
             }
            this.lastRecordedTimeValue = newCollectionTime.longValue();
         }
         return DoubleNumeric.valueOf(result);
     }
 
     public AggregateFunction nextInstance() {
        return new JmxPercentageOfTimeFunction(lastRecordedTimeValue, millisInPeriod);
     }
 }
