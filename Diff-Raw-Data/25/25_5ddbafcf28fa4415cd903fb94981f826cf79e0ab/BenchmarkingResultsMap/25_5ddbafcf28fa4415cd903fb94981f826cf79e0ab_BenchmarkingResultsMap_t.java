 package dna.profiler.datatypes.benchmarkresults;
 
 import java.text.DecimalFormat;
 
 import dna.profiler.datatypes.ComparableEntryMap;
 
 public class BenchmarkingResultsMap extends ComparableEntryMap implements
 		Comparable<BenchmarkingResultsMap> {
 	double aggregatedValue;
 
	public void put(double value) {
		this.aggregatedValue = value;
 	}
 
 	public void add(ComparableEntryMap resSecondIn) {
 		BenchmarkingResultsMap resSecond = (BenchmarkingResultsMap) resSecondIn;
 		this.aggregatedValue += resSecond.getValue();
 	}
 
 	public double getValue() {
 		return this.aggregatedValue;
 	}

 	public String toString() {
		DecimalFormat f = new DecimalFormat("###,##0.00");
 		return f.format(this.getValue());
 	}
 
 	/**
 	 * This should compare different complexity maps based on their counted
 	 * accesses.
 	 * 
 	 * Returning -1 iff this < o Returning 0 iff this == o Returning 1 iff this
 	 * > o
 	 */
 	@Override
 	public int compareTo(BenchmarkingResultsMap o) {
 		return Double.compare(this.aggregatedValue, o.getValue());
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		long temp;
 		temp = Double.doubleToLongBits(aggregatedValue);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		BenchmarkingResultsMap other = (BenchmarkingResultsMap) obj;
 		if (Double.doubleToLongBits(aggregatedValue) != Double
 				.doubleToLongBits(other.aggregatedValue)) {
 			return false;
 		}
 		return true;
 	}
 
 }
