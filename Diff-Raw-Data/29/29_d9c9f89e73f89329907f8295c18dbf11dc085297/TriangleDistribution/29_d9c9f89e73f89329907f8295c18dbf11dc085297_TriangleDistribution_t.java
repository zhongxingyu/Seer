 package org.anvard.mcnpv;
 
import java.util.concurrent.ThreadLocalRandom;
 
 import org.springframework.util.Assert;
 
 public class TriangleDistribution implements Distribution {
 
 	private double fc;
 	private double min;
 	private double likely;
 	private double max;
 	
 	public TriangleDistribution(double min, double likely, double max) {
 		Assert.isTrue(max >= likely);
 		Assert.isTrue(likely >= min);
 		this.min = min;
 		this.likely = likely;
 		this.max = max;
		this.fc = (likely - min) / (max - min);
 	}
 	
 	/* (non-Javadoc)
	 * @see org.anvard.mcnpv.Distribution#sample()
 	 */
 	@Override
 	public double sample() {
	  double u = ThreadLocalRandom.current().nextDouble();
 		if (u < fc) {
			return min + Math.sqrt(u * (max - min) * (likely - min));
 		} else {
			return max - Math.sqrt((1 - u) * (max - min) * (max - likely));
 		}
 	}
 
 	@Override
 	public double getMin() {
 		return min;
 	}
 
 	public double getLikely() {
 		return likely;
 	}
 
 	@Override
 	public double getMax() {
 		return max;
 	}
 	
 }
