 package edu.umd.cs.linqs.action;
 
 import edu.umd.cs.psl.database.ReadOnlyDatabase;
 import edu.umd.cs.psl.model.argument.ArgumentType;
 import edu.umd.cs.psl.model.argument.GroundTerm;
 import edu.umd.cs.psl.model.argument.IntegerAttribute;
 import edu.umd.cs.psl.model.function.ExternalFunction;
 
 class ClosenessFunction implements ExternalFunction {
 
 	public static final int DEFAULT_HINGE = 0;
 	public static final double DEFAULT_SIGMA = 1.0;
 	public static final double DEFAULT_THRESH = 0.1;
 	public static final boolean DEFAULT_SQUARED = true;
 	
 	private static final ArgumentType[] argTypes = new ArgumentType[]{
 		ArgumentType.Integer,ArgumentType.Integer,	// x-coord
 		ArgumentType.Integer,ArgumentType.Integer,	// y-coord
 		ArgumentType.Integer,ArgumentType.Integer,	// width
 		ArgumentType.Integer,ArgumentType.Integer	// height
 		};
 	
 	private int hinge;
 	private double sigma;
 	private double thresh;
 	private boolean squared;
 		
 	public ClosenessFunction() {
 		hinge = DEFAULT_HINGE;
 		sigma = DEFAULT_SIGMA;
 		thresh = DEFAULT_THRESH;
 		squared = DEFAULT_SQUARED;
 	}
 	
 	public ClosenessFunction(int hinge, double sigma, double thresh, boolean squared) {
 		this.hinge = hinge;
 		this.sigma = sigma;
 		this.thresh = thresh;
 		this.squared = squared;
 	}
 	
 	public void setHinge(int hinge) {
 		this.hinge = hinge;
 	}
 	
 	public void setSigma(double sigma) {
 		this.sigma = sigma;
 	}
 	
 	public void setThresh(double thresh) {
 		this.thresh = thresh;
 	}
 	
 	public void setSquared(boolean squared) {
 		this.squared = squared;
 	}
 	
 	@Override
 	public double getValue(ReadOnlyDatabase db, GroundTerm... args) {
 		/* Get args */
 		int x1 = ((IntegerAttribute) args[0]).getValue().intValue();
 		int x2 = ((IntegerAttribute) args[1]).getValue().intValue();
 		int y1 = ((IntegerAttribute) args[2]).getValue().intValue();
 		int y2 = ((IntegerAttribute) args[3]).getValue().intValue();
 		int w1 = ((IntegerAttribute) args[4]).getValue().intValue();
 		int w2 = ((IntegerAttribute) args[5]).getValue().intValue();
 		int h1 = ((IntegerAttribute) args[6]).getValue().intValue();
 		int h2 = ((IntegerAttribute) args[7]).getValue().intValue();
 
 		//TODO: modify distance function to something more sophisticated
 		int dx = Math.abs(x1-x2);
 		int dy = Math.abs(y1-y2);
 		//double dz = Math.abs(w1/((double)h1) - w2/((double)h2));
 		int d = squared ? (dx*dx + dy*dy) : (dx + dy);
		double v = Math.exp( -Math.max(hinge, d) / sigma );
 		
 		return v < thresh ? 0.0 : v;
 	}
 
 	@Override
 	public int getArity() {
 		return argTypes.length;
 	}
 
 	@Override
 	public ArgumentType[] getArgumentTypes() {
 		return argTypes;
 	}
 
 }
 
