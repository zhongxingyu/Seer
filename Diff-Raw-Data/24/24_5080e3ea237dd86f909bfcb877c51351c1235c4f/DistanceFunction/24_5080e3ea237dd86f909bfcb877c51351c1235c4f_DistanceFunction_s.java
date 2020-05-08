 package edu.umd.cs.linqs.action;
 
 import edu.umd.cs.psl.database.ReadOnlyDatabase;
 import edu.umd.cs.psl.model.argument.ArgumentType;
 import edu.umd.cs.psl.model.argument.GroundTerm;
 import edu.umd.cs.psl.model.argument.IntegerAttribute;
 import edu.umd.cs.psl.model.function.ExternalFunction;
 
 class DistanceFunction implements ExternalFunction {
 
 	private static final ArgumentType[] argTypes = new ArgumentType[]{
 		ArgumentType.Integer,ArgumentType.Integer,	// x-coord
 		ArgumentType.Integer,ArgumentType.Integer,	// y-coord
 		ArgumentType.Integer,ArgumentType.Integer,	// width
 		ArgumentType.Integer,ArgumentType.Integer	// height
 		};
 	
 	public DistanceFunction() {
 		
 	}
 	
 	@Override
 	public double getValue(ReadOnlyDatabase db, GroundTerm... args) {
 		/* Get args */
		Integer x1 = ((IntegerAttribute) args[0]).getValue().intValue();
		Integer x2 = ((IntegerAttribute) args[1]).getValue().intValue();
		Integer y1 = ((IntegerAttribute) args[2]).getValue().intValue();
		Integer y2 = ((IntegerAttribute) args[3]).getValue().intValue();
		Integer w1 = ((IntegerAttribute) args[4]).getValue().intValue();
		Integer w2 = ((IntegerAttribute) args[5]).getValue().intValue();
		Integer h1 = ((IntegerAttribute) args[6]).getValue().intValue();
		Integer h2 = ((IntegerAttribute) args[7]).getValue().intValue();
 
 		//TODO: compute distance between bounding boxes
 		
 		return 0;
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
 
class NearFunction extends DistanceFunction {
	@Override
	public double getValue(ReadOnlyDatabase db, GroundTerm... args) {
		return 1.0 - super.getValue(db, args);
	}
}

