 package org.anvard.mcnpv;
 
import java.util.Random;
 
 import org.springframework.util.Assert;
 
 public class TriangleDistribution implements Distribution {
 
 	private double fc;
 	private double min;
 	private double likely;
 	private double max;
	private Random r;
 	
 	public TriangleDistribution(double min, double likely, double max) {
 		Assert.isTrue(max >= likely);
 		Assert.isTrue(likely >= min);
 		this.min = min;
 		this.likely = likely;
 		this.max = max;
		this.fc = (max - min) / (likely - min);
		this.r = new Random();
 	}
 	
 	/* (non-Javadoc)
	 * @see org.anvard.mcnpv.Distribution#nextDouble()
 	 */
 	@Override
 	public double sample() {
		double u = r.nextDouble();
 		if (u < fc) {
			return min + Math.sqrt(u * (likely - min) * (max - min));
 		} else {
			return likely - Math.sqrt((1 - u) * (likely - min) * (likely - max));
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
