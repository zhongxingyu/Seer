 package point;
 
 import net.sourceforge.interval.ia_math.RealInterval;
 import core.Box;
 import functions.FunctionNEW;
 
 public abstract class PointAlgorithm /*implements Runnable*/ {
 	protected double alpha = 0.9;
 	protected final double epsilon = 1e-6;
 	protected FunctionNEW function;
 	protected final int maxSteps = 100;
 	protected final double stepFromSearchAreaSizeFactor = 0.25;
 	
	protected final boolean logging = false;
	
 	protected double optVal;
 	protected double optArg[];
 	
 	public PointAlgorithm() {
 //		optVal = Double.MAX_VALUE;
 	}
 	public void setFunction(FunctionNEW f) {
 		function = f;
 	}
 	public double localMinimum(Box area) {
 		minimize(area);
 		if (!area.contains(optArg)) {
			if (logging) System.out.println("Point algorithm found local optima that is outside the search area. Return middle point..");
 			optArg = middleAreaPoint(area);
 			optVal = function.calculatePoint(optArg);
 		}
 		return optVal;
 	}
 	protected abstract void minimize(Box area); 
 	
 	@Override
 	public String toString() {
 		String fullName = this.getClass().getName();
 		return fullName.substring(fullName.lastIndexOf('.')+1); // removes packages
 	}
 	
 	/*
 	 * returns n-dimensional point which is in the middle of the area 
 	 */
 	protected double[] middleAreaPoint(Box area) {
 		int dim = area.getDimension();
 		double point[] = new double[dim];
 		
 		for (int i = 0; i < dim; i++) {
 			RealInterval ii = area.getInterval(i);
 			point[i] = ii.lo() + ii.wid()/2;
 		}
 		return point;
 	}
 }
