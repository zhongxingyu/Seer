 package edu.kpi.asu.rduboveckij.cam.math.neighbordhoods;
 
 import java.util.Collection;
import java.util.Map;
 import java.util.TreeMap;
 
 import edu.kpi.asu.rduboveckij.cam.domain.Precedent;
 import edu.kpi.asu.rduboveckij.cam.math.metric.Metric;
 
 public class NeighborhoodsImpl extends Neighbordhoods {
 	public NeighborhoodsImpl() {
 	}
 
 	public NeighborhoodsImpl(Metric metric) {
 		super(metric);
 	}
 
 	/**
 	 * calc K neighborhoods method
 	 * 
	 * @param v Array Precedent
 	 * 
	 * @param a point for find result
 	 * 
 	 * @return min(metric(b, a))
 	 */
 	public double apply(Collection<Precedent> v, double[] a) {
		Map<Double, Precedent> D = new TreeMap<Double, Precedent>();
 		for (final Precedent vi : v)
 			D.put(metric.apply(vi.getParams(), a), vi);
		return ((Precedent) D.values().toArray()[0]).getResult();
 	}
 
 }
