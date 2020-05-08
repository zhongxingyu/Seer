 package com.psddev.dari.db;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 
 //import org.slf4j.Logger;
 //import org.slf4j.LoggerFactory;
 
 class MetricAction extends Modification<Object> {
 
     //private static final Logger LOGGER = LoggerFactory.getLogger(MetricAction.class);
 
     private transient final Map<String, Metric> recordMetrics = new HashMap<String, Metric>();
     private transient final Map<String, ObjectField> metricFields = new HashMap<String, ObjectField>();
     private transient final Map<String, Map<String, Double>> metricValueCache = new HashMap<String, Map<String, Double>>();
     private transient final Map<String, Map<String, Map<String, Double>>> metricValuesCache = new HashMap<String, Map<String, Map<String, Double>>>();
 
     private ObjectField findMetricField(String internalName) {
         ObjectType recordType = ObjectType.getInstance(getOriginalObject().getClass());
         if (internalName == null) {
             for (ObjectField objectField : recordType.getFields()) {
                 if (objectField.as(Metric.FieldData.class).isMetricValue()) {
                     return objectField;
                 }
             }
         } else {
            ObjectField objectField = recordType.getField(internalName);
             if (objectField != null && objectField.as(Metric.FieldData.class).isMetricValue()) {
                 return objectField;
             }
         }
         throw new RuntimeException("At least one numeric field must be marked as @MetricValue");
     }
 
     private Map<String, Double> getMetricCache(String internalName) {
         if (! metricValueCache.containsKey(internalName)) {
             metricValueCache.put(internalName, new HashMap<String, Double>());
         }
         return metricValueCache.get(internalName);
     }
 
     private Map<String, Map<String, Double>> getMetricValuesCache(String internalName) {
         if (! metricValuesCache.containsKey(internalName)) {
             metricValuesCache.put(internalName, new HashMap<String, Map<String, Double>>());
         }
         return metricValuesCache.get(internalName);
     }
 
     private ObjectField getMetricField(String internalName) {
         if (!metricFields.containsKey(internalName)) {
             metricFields.put(internalName, findMetricField(internalName));
         }
         return metricFields.get(internalName);
     }
 
     // Explicit dimension
     public void incrementDimensionMetric(String metricFieldInternalName, String dimensionValue, double amount) {
         incrementDimensionMetric(metricFieldInternalName, dimensionValue, amount, null);
     }
 
     // Explicit dimension
     public void incrementDimensionMetric(String metricFieldInternalName, String dimensionValue, double amount, Long eventDateMillis) {
         try {
             metricValueCache.remove(metricFieldInternalName);
             getMetricObject(metricFieldInternalName).setEventDate(eventDateMillis);
             getMetricObject(metricFieldInternalName).incrementMetric(dimensionValue, amount);
         } catch (SQLException e) {
             throw new DatabaseException(getMetricObject(metricFieldInternalName).getDatabase(), "Error in Metric.incrementMetric() : " + e.getLocalizedMessage());
         }
     }
 
     // Explicit dimension
     public void setDimensionMetric(String metricFieldInternalName, String dimensionValue, double c) {
         try {
             metricValueCache.remove(metricFieldInternalName);
             getMetricObject(metricFieldInternalName).setMetric(dimensionValue, c);
         } catch (SQLException e) {
             throw new DatabaseException(getMetricObject(metricFieldInternalName).getDatabase(), "Error in Metric.setMetric() : " + e.getLocalizedMessage());
         }
     }
 
     // All dimensions
     public void deleteMetric(String metricFieldInternalName) {
         try {
             getMetricObject(metricFieldInternalName).deleteMetric();
         } catch (SQLException e) {
             throw new DatabaseException(getMetricObject(null).getDatabase(), "Error in Metric.deleteMetric() : " + e.getLocalizedMessage());
         }
     }
 
     // Sum of all dimensions
     public double getMetricSum(String metricFieldInternalName) {
         final String cacheKey = "sum";
         if (! getMetricCache(metricFieldInternalName).containsKey(cacheKey)) {
             try {
                 Metric cr = getMetricObject(metricFieldInternalName);
                 cr.setQueryDateRange(null, null);
                 Double metricValue = cr.getMetricSum();
                 if (metricValue == null) {
                     getMetricCache(metricFieldInternalName).put(cacheKey, 0.0d);
                 } else {
                     getMetricCache(metricFieldInternalName).put(cacheKey, metricValue);
                 }
             } catch (SQLException e) {
                 throw new DatabaseException(getMetricObject(metricFieldInternalName).getDatabase(), "Error in Metric.getMetric() : " + e.getLocalizedMessage());
             }
         }
         return getMetricCache(metricFieldInternalName).get(cacheKey);
     }
 
     // Explicit dimension
     public double getDimensionMetric(String metricFieldInternalName, String dimensionValue) {
         final String cacheKey = dimensionValue;
         if (! getMetricCache(metricFieldInternalName).containsKey(cacheKey)) {
             try {
                 Metric cr = getMetricObject(metricFieldInternalName);
                 cr.setQueryDateRange(null, null);
                 Double metricValue = cr.getMetric(dimensionValue);
                 if (metricValue == null) {
                     getMetricCache(metricFieldInternalName).put(cacheKey, 0.0d);
                 } else {
                     getMetricCache(metricFieldInternalName).put(cacheKey, metricValue);
                 }
             } catch (SQLException e) {
                 throw new DatabaseException(getMetricObject(metricFieldInternalName).getDatabase(), "Error in Metric.getMetric() : " + e.getLocalizedMessage());
             }
         }
         return getMetricCache(metricFieldInternalName).get(cacheKey);
     }
 
     // All dimensions
     public Map<String, Double> getMetricValues(String metricFieldInternalName) {
         final String cacheKey = null;
         if (! getMetricValuesCache(metricFieldInternalName).containsKey(cacheKey)) {
             try {
                 Metric cr = getMetricObject(metricFieldInternalName);
                 cr.setQueryDateRange(null, null);
                 Map<String, Double> metricValues = cr.getMetricValues();
                 if (metricValues == null) {
                     getMetricValuesCache(metricFieldInternalName).put(cacheKey, new HashMap<String, Double>());
                 } else {
                     getMetricValuesCache(metricFieldInternalName).put(cacheKey, metricValues);
                 }
             } catch (SQLException e) {
                 throw new DatabaseException(getMetricObject(metricFieldInternalName).getDatabase(), "Error in Metric.getMetric() : " + e.getLocalizedMessage());
             }
         }
         return getMetricValuesCache(metricFieldInternalName).get(cacheKey);
     }
 
     // Sum of all dimensions
     public double getMetricSumOverDateRange(String metricFieldInternalName, Long startTimestamp, Long endTimestamp) {
         final String cacheKey = "sum:"+String.valueOf(startTimestamp) + ":" + String.valueOf(endTimestamp);
         if (! getMetricCache(metricFieldInternalName).containsKey(cacheKey)) {
             try {
                 Metric cr = getMetricObject(metricFieldInternalName);
                 if (cr.getEventDateProcessor().equals(MetricInterval.None.class)) {
                     throw new RuntimeException("Date range does not apply - no MetricInterval");
                 }
                 cr.setQueryDateRange(startTimestamp, endTimestamp);
                 Double metricValue = cr.getMetricSum();
                 if (metricValue == null) {
                     getMetricCache(metricFieldInternalName).put(cacheKey, 0.0d);
                 } else {
                     getMetricCache(metricFieldInternalName).put(cacheKey, metricValue);
                 }
             } catch (SQLException e) {
                 throw new DatabaseException(getMetricObject(metricFieldInternalName).getDatabase(), "Error in Metric.getMetric() : " + e.getLocalizedMessage());
             }
         }
         return getMetricCache(metricFieldInternalName).get(cacheKey);
     }
 
     // Explicit dimension
     public double getMetricOverDateRange(String metricFieldInternalName, String dimensionValue, Long startTimestamp, Long endTimestamp) {
         final String cacheKey = String.valueOf(dimensionValue) + ":" + String.valueOf(startTimestamp) + ":" + String.valueOf(endTimestamp);
         if (! getMetricCache(metricFieldInternalName).containsKey(cacheKey)) {
             try {
                 Metric cr = getMetricObject(metricFieldInternalName);
                 if (cr.getEventDateProcessor().equals(MetricInterval.None.class)) {
                     throw new RuntimeException("Date range does not apply - no MetricInterval");
                 }
                 cr.setQueryDateRange(startTimestamp, endTimestamp);
                 Double metricValue = cr.getMetric(dimensionValue);
                 if (metricValue == null) {
                     getMetricCache(metricFieldInternalName).put(cacheKey, 0.0d);
                 } else {
                     getMetricCache(metricFieldInternalName).put(cacheKey, metricValue);
                 }
             } catch (SQLException e) {
                 throw new DatabaseException(getMetricObject(metricFieldInternalName).getDatabase(), "Error in Metric.getMetric() : " + e.getLocalizedMessage());
             }
         }
         return getMetricCache(metricFieldInternalName).get(cacheKey);
     }
 
     // All dimensions
     public Map<String, Double> getMetricValuesOverDateRange(String metricFieldInternalName, Long startTimestamp, Long endTimestamp) {
         final String cacheKey = String.valueOf(startTimestamp) + ":" + String.valueOf(endTimestamp);
         if (! getMetricValuesCache(metricFieldInternalName).containsKey(cacheKey)) {
             try {
                 Metric cr = getMetricObject(metricFieldInternalName);
                 if (cr.getEventDateProcessor().equals(MetricInterval.None.class)) {
                     throw new RuntimeException("Date range does not apply - no MetricInterval");
                 }
                 cr.setQueryDateRange(startTimestamp, endTimestamp);
                 Map<String, Double> metricValues = cr.getMetricValues();
                 if (metricValues == null) {
                     getMetricValuesCache(metricFieldInternalName).put(cacheKey, new HashMap<String, Double>());
                 } else {
                     getMetricValuesCache(metricFieldInternalName).put(cacheKey, metricValues);
                 }
             } catch (SQLException e) {
                 throw new DatabaseException(getMetricObject(metricFieldInternalName).getDatabase(), "Error in Metric.getMetric() : " + e.getLocalizedMessage());
             }
         }
         return getMetricValuesCache(metricFieldInternalName).get(cacheKey);
     }
 
     private Metric getMetricObject(String metricFieldInternalName) {
         // if metricFieldInternalName is null, it will return the *first* @MetricValue in the type
 
         if (! recordMetrics.containsKey(metricFieldInternalName)) {
             ObjectField metricField = getMetricField(metricFieldInternalName);
             Metric recordMetric = new Metric(this, metricField.getUniqueName());
             recordMetric.setEventDateProcessor(metricField.as(Metric.FieldData.class).getEventDateProcessor());
             recordMetrics.put(metricFieldInternalName, recordMetric);
         }
 
         return recordMetrics.get(metricFieldInternalName);
     }
 }
