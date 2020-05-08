 package com.voladroid.model.compare;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.mat.SnapshotException;
 import org.eclipse.mat.snapshot.Histogram;
 import org.eclipse.mat.snapshot.query.SnapshotQuery;
 import org.eclipse.mat.util.IProgressListener;
 
 import com.voladroid.model.Dump;
 
 public class ObjectResultProducer implements ResultProducer {
 
 	protected Histogram getHistogram(Dump d, IProgressListener listener)
 			throws SnapshotException {
 		return (Histogram) SnapshotQuery.lookup("histogram",
 				d.getSnapshot(listener)).execute(listener);
 	}
 
 	@Override
 	public Result compare(List<Dump> dumps, IProgressListener listener) {
 		List<Histogram> histograms = new ArrayList<Histogram>();
 		for (Dump d : dumps) {
 			try {
 				histograms.add(getHistogram(d, listener));
 			} catch (SnapshotException e) {
 				e.printStackTrace();
 			}
 		}
 
 		double totalAvrage = totalAvrage(histograms);
 
 		ArrayList<Long> samples = samples(histograms);
 		double diffAvg = sampleAvrage(samples);
 
 		double stdDev = standardDeviation(samples, diffAvg);
 
 		return new Result(diffAvg, stdDev, totalAvrage,
 				samples.toArray(new Long[0]));
 	}
 
 	protected double standardDeviation(ArrayList<Long> samples, double diffAvg) {
 		double std = 0;
 		for (long l : samples) {
 			std += Math.pow(l - diffAvg, 2);
 		}
 		double stdDev = Math.sqrt(std * 1 / samples.size());
 		return stdDev;
 	}
 
 	private double totalAvrage(List<Histogram> histograms) {
		double total = 0;
 		for (Histogram g : histograms) {
 			total += g.getNumberOfObjects();
 		}
 		double totalAvrage = total / histograms.size();
 		return totalAvrage;
 	}
 
 	private ArrayList<Long> samples(List<Histogram> histograms) {
 		ArrayList<Long> samples = new ArrayList<Long>();
 		HistogramComparer comparer = new HistogramComparer();
 		for (int n = 0; n < histograms.size() - 1; n++) {
 			Histogram a = histograms.get(n);
 			Histogram b = histograms.get(n + 1);
 			Histogram d = (Histogram) comparer.compare(a, b);
 
 			// sample is the number of objects changed
 			samples.add(d.getNumberOfObjects());
 		}
 
 		return samples;
 	}
 
	protected double sampleAvrage(List<Long> samples) {
		double diffTotal = 0;
 		for (long l : samples) {
 			diffTotal += l;
 		}
 		return diffTotal / samples.size();
 	}
 
 }
