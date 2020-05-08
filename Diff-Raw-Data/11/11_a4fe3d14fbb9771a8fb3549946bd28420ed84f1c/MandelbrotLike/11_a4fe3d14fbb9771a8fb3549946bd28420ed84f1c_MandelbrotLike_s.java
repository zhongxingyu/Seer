 package ratson.genimageexplorer.generators;
 
 public abstract class MandelbrotLike implements FunctionFactory {
 
 	protected int maxIters = 100;
	protected double r2Max;
 	protected boolean isSmooth = true;
 	protected double ln_ln_r2max;
 	protected static final double ln2 = Math.log(2);
 
 
 	/**fly-away radius. Should be at least 2.0 for correct calculation
 	 * Values, bigger than 2.0 used only for smoother flyaway speed approximation. 
 	 * The bigger this value, the slower calculation (though complexity grows extremely slowly)
 	 * */
 	public void setRMax(double r) {
 		r2Max=r*r;
 		ln_ln_r2max=Math.log(Math.log(r2Max));		
 	}
 
 	public double getRMax() {
 		return Math.sqrt(r2Max);
 	}
 
 	public void setMaxIters(int maxIters) {
 		this.maxIters = maxIters;
 	}
 
 	public int getMaxIters() {
 		return maxIters;
 	}
 
 	public void setSmooth(boolean isSmooth) {
 		this.isSmooth = isSmooth;
 	}
 
 	public boolean isSmooth() {
 		return isSmooth;
 	}
 
 }
