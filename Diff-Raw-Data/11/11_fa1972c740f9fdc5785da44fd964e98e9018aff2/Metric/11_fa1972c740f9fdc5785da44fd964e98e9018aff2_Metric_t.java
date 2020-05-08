 package com.psddev.dari.db;
 
 import java.sql.SQLException;
import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.psddev.dari.util.ObjectUtils;
 
 @Metric.Embedded
 public class Metric extends Record {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(Metric.class);
 
     private final transient State owner;
     private final transient ObjectField field;
     private transient MetricDatabase metricDatabase;
 
     /**
      * @param owner Can't be {@code null}.
      * @param field Can't be {@code null}.
      */
     public Metric(State owner, ObjectField field) {
         this.owner = owner;
         this.field = field;
     }
 
     /**
      * Returns the state that owns this metric instance.
      *
      * @return Never {@code null}.
      */
     public State getOwner() {
         return owner;
     }
 
     /**
      * Returns the field that this metric instance updates.
      *
      * @return Never {@code null}.
      */
     public ObjectField getField() {
         return field;
     }
 
     /**
      * Returns the MetricDatabase or throw an exception if it could not find the SQL database. 
      * 
      */
     private MetricDatabase getMetricDatabase() {
         if (metricDatabase == null) {
             metricDatabase = MetricDatabase.Static.getMetricDatabase(owner, field);
         }
         if (metricDatabase == null) {
             throw new RuntimeException ("Metric field " +field.getUniqueName()+" cannot determine SQL database for database " + owner.getDatabase().getName() + " (" + owner.getDatabase().getClass().getName() + "), this Metric object is unusable!");
         }
         return metricDatabase;
     }
 
     /**
      * Increases the metric value by the given {@code amount}.
      */
     public void increment(double amount) {
         incrementDimensionAt(amount, null, null);
     }
 
     /**
      * Increases the metric value by the given {@code amount} and associate it
      * with the given {@code dimension} and {@code time}.
      *
      * @param dimension May be {@code null}.
      * @param time If {@code null}, right now.
      */
     public void incrementDimensionAt(double amount, String dimension, DateTime time) {
         try {
             getMetricDatabase().incrementMetric(owner.getId(), time, dimension, amount);
         } catch (SQLException e) {
             throw new DatabaseException(getMetricDatabase().getDatabase(), "Error in MetricDatabase.incrementMetric() : " + e.getLocalizedMessage());
         }
     }
 
     /**
      * Asynchronously (within the given {@code within} seconds) increases the
      * metric value by the given {@code amount} and associate it with the given
      * {@code dimension} and {@code time}.
      *
      * @param dimension May be {@code null}.
      * @param time May be {@code null}.
      * @param within In seconds. If less than or equal to {@code 0}, uses
      * {@code 1.0} instead.
      */
     public void incrementEventually(double amount, String dimension, DateTime time, double within) {
         if (within <= 0) {
             within = 1.0;
         }
         UUID dimensionId;
         try {
             dimensionId = getMetricDatabase().getDimensionId(dimension);
         } catch (SQLException e) {
             throw new DatabaseException(getMetricDatabase().getDatabase(), "Error in MetricDatabase.getDimensionId() : " + e.getLocalizedMessage());
         }
         MetricIncrementQueue.queueIncrement(getOwner().getId(), dimensionId, time, getMetricDatabase(), amount, within);
     }
 
     /**
      * Returns when the metric value associated with the given
      * {@code dimension} was last updated.
      *
      * @param dimension May be {@code null}.
      * @return May be {@code null}.
      */
     public DateTime getLastDimensionUpdate(String dimension) {
         try {
             Static.preFetchMetrics(getOwner(), getMetricDatabase().getDimensionId(dimension), null, null);
             return getMetricDatabase().getLastUpdate(getOwner().getId(), dimension);
         } catch (SQLException e) {
             throw new DatabaseException(getMetricDatabase().getDatabase(), "Error in MetricDatabase.getLastUpdate() : " + e.getLocalizedMessage());
         }
     }
 
     /**
      * Returns when the metric value was last updated.
      *
      * @return May be {@code null}.
      */
     public DateTime getLastUpdate() {
         return getLastDimensionUpdate(null);
     }
 
     /**
      * Sets the metric value to the given {@code amount} and associates it with
      * the given {@code dimension} and {@code time}.
      *
      * @param dimension May be {@code null}.
      * @param time If {@code null}, right now.
      */
     public void setDimensionAt(double amount, String dimension, DateTime time) {
         try {
             getMetricDatabase().setMetric(getOwner().getId(), time, dimension, amount);
         } catch (SQLException e) {
             throw new DatabaseException(getMetricDatabase().getDatabase(), "Error in MetricDatabase.setMetric() : " + e.getLocalizedMessage());
         }
     }
 
     /** Deletes all metric values. */
     public void deleteAll() {
         try {
             getMetricDatabase().deleteMetric(getOwner().getId());
         } catch (SQLException e) {
             throw new DatabaseException(getMetricDatabase().getDatabase(), "Error in MetricDatabase.deleteMetric() : " + e.getLocalizedMessage());
         }
     }
 
     /**
      * Returns the metric value associated with the given {@code dimension}
      * between the given {@code start} and {@code end}.
      *
      * @param dimension May be {@code null}.
      * @param start If {@code null}, beginning of time.
      * @param end If {@code null}, end of time.
      */
     public double getByDimensionBetween(String dimension, DateTime start, DateTime end) {
         try {
             Long startTimestamp = (start == null ? null : start.getMillis());
             Long endTimestamp = (end == null ? null : end.getMillis());
             Static.preFetchMetrics(getOwner(), getMetricDatabase().getDimensionId(dimension), startTimestamp, endTimestamp);
             Double metricValue = getMetricDatabase().getMetric(getOwner().getId(), dimension, startTimestamp, endTimestamp);
             return metricValue == null ? 0.0 : metricValue;
         } catch (SQLException e) {
             throw new DatabaseException(getMetricDatabase().getDatabase(), "Error in MetricDatabase.getMetric() : " + e.getLocalizedMessage());
         }
     }
 
     /**
      * Returns the metric value associated with the given {@code dimension}.
      *
      * @param dimension May be {@code null}.
      */
     public double getByDimension(String dimension) {
         return getByDimensionBetween(dimension, null, null);
     }
 
     /**
      * Returns the sum of all metric values in each dimension between the given
      * {@code start} and {@code end}.
      *
      * @param start If {@code null}, beginning of time.
      * @param end If {@code null}, end of time.
      */
     public double getSumBetween(DateTime start, DateTime end) {
         return getByDimensionBetween(null, start, end);
     }
 
     /**
      * Returns the sum of all metric values in each dimension.
      */
     public double getSum() {
         return getSumBetween(null, null);
     }
 
     /**
      * Groups the metric values between the given {@code start} and {@code end}
      * by each dimension.
      *
      * @param start If {@code null}, beginning of time.
      * @param end If {@code null}, end of time.
      * @return Never {@code null}.
      */
     public Map<String, Double> groupByDimensionBetween(DateTime start, DateTime end) {
         try {
             Long startTimestamp = (start == null ? null : start.getMillis());
             Long endTimestamp = (end == null ? null : end.getMillis());
             Map<String, Double> metricValues = getMetricDatabase().getMetricValues(getOwner().getId(), startTimestamp, endTimestamp);
             return metricValues;
         } catch (SQLException e) {
             throw new DatabaseException(getMetricDatabase().getDatabase(), "Error in MetricDatabase.getMetricValues() : " + e.getLocalizedMessage());
         }
     }
 
     /**
      * Groups the metric values by each dimension.
      *
      * @return Never {@code null}.
      */
     public Map<String, Double> groupByDimension() {
         return groupByDimensionBetween(null, null);
     }
 
     /**
      * Groups the metric values associated with the given {@code dimension}
      * between the given {@code start} and {@code end} by the given
      * {@code interval}.
      *
      * @param dimension May be {@code null}.
      * @param start If {@code null}, beginning of time.
      * @param end If {@code null}, end of time.
      * @return Never {@code null}.
      */
     public Map<DateTime, Double> groupByDate(String dimension, MetricInterval interval, DateTime start, DateTime end) {
         try {
             Long startTimestamp = (start == null ? null : start.getMillis());
             Long endTimestamp = (end == null ? null : end.getMillis());
             Map<DateTime, Double> metricTimeline = getMetricDatabase().getMetricTimeline(getOwner().getId(), dimension, startTimestamp, endTimestamp, interval);
             return metricTimeline;
         } catch (SQLException e) {
             throw new DatabaseException(getMetricDatabase().getDatabase(), "Error in MetricDatabase.getMetricTimeline() : " + e.getLocalizedMessage());
         }
     }
 
     /**
      * Groups the sum of all metric values between the given {@code start} and
      * {@code end} by the given {@code interval}.
      *
      * @param start If {@code null}, beginning of time.
      * @param end If {@code null}, end of time.
      * @return Never {@code null}.
      */
     public Map<DateTime, Double> groupSumByDate(MetricInterval interval, DateTime start, DateTime end) {
         return groupByDate(null, interval, start, end);
     }
 
     /**
      * Repairs the cumulativeAmount value for all rows since the beginning of
      * time. This method takes a long time to complete and should only be used
      * if something has gone wrong.
      *
      */
     public void repair() {
         try {
             getMetricDatabase().reconstructCumulativeAmounts(getOwner().getId());
         } catch (SQLException e) {
             throw new DatabaseException(getMetricDatabase().getDatabase(), "Error in MetricDatabase.reconstructCumulativeAmounts() : " + e.getLocalizedMessage());
         }
     }
 
     /**
      * Returns the metric value associated with the main ({@code null})
      * dimension between the given {@code start} and {@code end}.
      *
      * @deprecated Use {@link #getSumBetween} instead.
      */
     @Deprecated
     public double getValue(DateTime start, DateTime end) {
         return getByDimensionBetween(null, start, end);
     }
 
     /**
      * Returns the metric value associated with the main ({@code null})
      * dimension between the given {@code start} and {@code end}.
      * @deprecated Use {@link #getSumBetween} instead
      */
     @Deprecated
     public double getValueBetween(DateTime start, DateTime end) {
         return getByDimensionBetween(null, start, end);
     }
 
     /**
      * Returns the metric value associated with the main ({@code null})
      * dimension.
      * @deprecated Use {@link #getSum} instead.
      */
     @Deprecated
     public double getValue() {
         return getByDimensionBetween(null, null, null);
     }
 
     public static class Static {
 
         private static final String EXTRA_METRICS_FETCHED_PREFIX = "dari.metric.preFetched.";
 
         private static void preFetchMetrics(State state, UUID dimensionId, Long startTimestamp, Long endTimestamp) {
             if (state == null || state.getType() == null) {
                 return;
             }
             String extraKey = EXTRA_METRICS_FETCHED_PREFIX + ObjectUtils.to(String.class, dimensionId) + '.' + ObjectUtils.to(String.class, startTimestamp) + '.' + ObjectUtils.to(String.class, endTimestamp);
             if (state.getExtra(extraKey) != null && ((Boolean) state.getExtra(extraKey)) == true) {
                 return;
             }
             state.getExtras().put(extraKey, true);
            List<ObjectField> fields = new ArrayList<ObjectField>(state.getType().getMetricFields());
             fields.addAll(state.getDatabase().getEnvironment().getMetricFields());
             Set<MetricDatabase> metricDatabases = new HashSet<MetricDatabase>();
             for (ObjectField field : fields) {
                 MetricDatabase mdb = MetricDatabase.Static.getMetricDatabase(state, field);
                 if (mdb != null) {
                     metricDatabases.add(mdb);
                 }
             }
             doDatabasePreFetch(state.getId(), dimensionId, startTimestamp, endTimestamp, metricDatabases);
         }
 
         private static void doDatabasePreFetch(UUID id, UUID dimensionId, Long startTimestamp, Long endTimestamp, Collection<MetricDatabase> metricDatabases) {
             if (metricDatabases.isEmpty()) return;
             try{
                 MetricDatabase.Static.preFetchMetricSums(id, dimensionId, startTimestamp, endTimestamp, metricDatabases);
             } catch (SQLException ex) {
                 LOGGER.warn("Exception when prefetching Metrics for object "+id+": " + ex.getLocalizedMessage());
             }
         }
 
     }
 
 }
