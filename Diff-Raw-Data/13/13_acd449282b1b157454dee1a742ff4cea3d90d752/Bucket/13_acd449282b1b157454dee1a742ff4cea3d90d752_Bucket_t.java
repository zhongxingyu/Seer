 package ch.ethz.mlmq.log_analyzer;
 
 import java.util.ArrayList;
 import java.util.Collections;
import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.math.stat.descriptive.moment.Mean;
 import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
 import org.apache.commons.math.stat.descriptive.moment.Variance;
 import org.apache.commons.math.stat.descriptive.rank.Percentile;
 
 public class Bucket {
 
 	private List<Double> values = new ArrayList<>();
 	private double[] primitiveValuesCache;
 
 	/**
 	 * Timestamp of the first measurement assigned to this bucket
 	 */
 	private long startTimestamp;
 	private Percentile percentileCache;
 
 	public Bucket() {
 		this.startTimestamp = Long.MAX_VALUE;
 	}
 
	public Bucket(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

 	public int count() {
 		return values.size();
 	}
 
 	public void addValue(int duration) {
 		if (primitiveValuesCache != null || percentileCache != null) {
 			primitiveValuesCache = null;
 			percentileCache = null;
 		}
 
 		values.add((double) duration); // Could do this better...
 	}
 
 	public void addTimestamp(long timestamp) {
 		startTimestamp = Math.min(startTimestamp, timestamp);
 	}
 
 	public double mean() {
 		if (count() == 0)
 			return 0;
 		Mean m = new Mean();
 		return m.evaluate(getPrimitiveValues());
 	}
 
 	public double variance() {
 		if (count() == 0)
 			return 0;
 		Variance v = new Variance();
 		return v.evaluate(getPrimitiveValues());
 	}
 
 	public double stddev() {
 		if (count() == 0)
 			return 0;
 		StandardDeviation d = new StandardDeviation();
 		return d.evaluate(getPrimitiveValues());
 	}
 
 	public double percentile(double percentile) {
 		if (count() == 0)
 			return 0;
 		if (percentileCache == null) {
 			percentileCache = new Percentile();
 			percentileCache.setData(getPrimitiveValues());
 		}
 		return percentileCache.evaluate(percentile);
 	}
 
 	private double[] getPrimitiveValues() {
 		if (primitiveValuesCache != null)
 			return primitiveValuesCache;
 
 		primitiveValuesCache = new double[count()];
 		for (int i = 0; i < count(); i++) {
 			primitiveValuesCache[i] = values.get(i);
 		}
 		return primitiveValuesCache;
 	}
 
 	public double min() {
 		if (count() == 0)
 			return 0;
 		return Collections.min(values);
 	}
 
 	public double max() {
 		if (count() == 0)
 			return 0;
 		return Collections.max(values);
 	}
 
 	public long getTime() {
 		return startTimestamp;
 	}
 
 	public double median() {
 		return percentile(50.0);
 	}
 }
