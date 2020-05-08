 package com.narmnevis.range.random;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.apache.commons.math3.distribution.NormalDistribution;
 
 import com.narmnevis.range.RangeContext;
 
 /**
  * @author nobeh
  * @since 1.3
  */
 public class GaussianDistributionRandomizer extends ListRandomizer {
 
 	public GaussianDistributionRandomizer() {
 	}
 
 	@Override
 	protected void init(RangeContext context) {
 		super.init(context);
		setRandoms(generateGaussianRandoms(context));
 	}
 
 	/**
 	 * @param context
 	 * @return
 	 * 
 	 * @since 1.1
 	 */
 	protected List<Double> generateGaussianRandoms(RangeContext context) {
 		// mean = 0 and std.dev = 0.33 ==> 99.7% of data is in (0, 1)
 		NormalDistribution nd = new NormalDistribution(0, 1.0 / 3);
 		List<Double> randoms = new ArrayList<>();
 		double[] samples = nd.sample(context.getSize());
 		for (double s : samples) {
 			double r = 1 * (1 + s) / 2;
 			// the very minority of 0.3%
 			if (Math.abs(r) > 1) {
 				r = Math.abs(1 - r);
 			} else if (r < 0) {
 				r = Math.abs(r);
 			}
 			assert r < 1 && r > 0 : "Sanity check for a Gaussian values between (0, 1) with (0, 0.33): " + r;
 			randoms.add(r);
 		}
 		return new CopyOnWriteArrayList<Double>(randoms);
 	}
 
 }
