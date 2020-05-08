 package vcu.blademonitor.RandomMetric;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Random;
 
 import vcu.blademonitor.simpleMonitoringServices.MetricStructure;
 
 public class RandomMetric {
	private final int randMax = 10;
 
	public MetricStructure createRandomMetric() {
 		MetricStructure myMetric = new MetricStructure("RandomNode");
 		Random generator = new Random();
 		int loopLimit = generator.nextInt(randMax);
 
 		String metricName;
 		double metricValue;
 		Date metricTime;
 
 		for (int i = 0; i < loopLimit; i++) {
			metricName = "RandomeMetric_" + i;
 			myMetric.addMetric(metricName);
 
			metricValue = generator.nextInt(randMax);
 			myMetric.setMetricValue(metricName, metricValue);
 
 			metricTime = Calendar.getInstance().getTime();
 			myMetric.setMetricTime(metricName, metricTime);
 		}
 
 		return myMetric;
 	}
 }
