 package testharness;
 
 import static org.junit.Assert.fail;
 
 import net.sourceforge.interval.ia_math.RealInterval;
 import algorithms.Algorithm;
 import core.Box;
 import functions.FunctionNEW;
 import functions.Function_DeJong_nD;
 import functions.Function_Price5_2D;
 import functions.Function_Rastrigin10_2D;
 import functions.Function_RosenbrockG_nD;
 import functions.Function_SixHumpCamelBack_2D;
 
 /**
  * @author nvpanov
  *
  */
 public class TestHarness {
 //	protected Random rnd = new Random();
 	
 	public final TestData f_DeJong_Zero(Algorithm alg, int dim, RealInterval area) {
 		if ( !area.contains(0) )
 			throw new IllegalArgumentException("Suppose to contain 0");
 		final double checkValue = 0;
 		final double checkArg = 0;
 		
 		Box box = new Box(dim, area);
 		FunctionNEW f = new Function_DeJong_nD(dim);
 		return solveFunction(f, box, alg, checkValue, checkArg);
 	}
 	public final TestData f_DeJong_NotSim(Algorithm alg, int dim) {
 		final double checkValue = 10*10*dim;
 		final double checkArg = 10;
 		Box area = new Box(dim, new RealInterval(10, 200));
 		FunctionNEW f = new Function_DeJong_nD(dim);
 		return solveFunction(f, area, alg, checkValue, checkArg);
 	}
 	public final TestData f_Price5_Zero(Algorithm alg, RealInterval area) {
 		if ( !area.contains(0) )
 			throw new IllegalArgumentException("Suppose to contain 0");
 		final int dim = 2;
 		final double checkValue = 0;
 		final double checkArg = 0;
 		FunctionNEW f = new Function_Price5_2D();
 		Box box = new Box(dim, area);
 		return solveFunction(f, box, alg, checkValue, checkArg);
 	}
 	public final TestData f_Rastrigin10(Algorithm alg, RealInterval area) {
 		final int dim = 2;
 		final double checkValue = -2;
 		final double checkArg = 0;
 		if ( !area.contains(checkArg) )
 			throw new IllegalArgumentException();
 
 		FunctionNEW f = new Function_Rastrigin10_2D();
 		Box box = new Box(dim, area);
 		return solveFunction(f, box, alg, checkValue, checkArg);
 	}
 	public final TestData f_SixHumpCamelBack(Algorithm alg, RealInterval area) {
 		final int dim = 2;
 		final double checkValue = -1.03163; //x = +0.08984 y = -0.71266
 											//x = -0.08984 y = +0.71266
 		if (!(area.contains(0.08984) && area.contains(-0.08984)) )
 			throw new IllegalArgumentException("Interval is square, I know, " +
 					"but it doesn't contain proper X value");
 		if (!(area.contains(0.71266) && area.contains(-0.71266)) )
 			throw new IllegalArgumentException("Interval is square, I know, " +
 					"but it doesn't contain proper Y value");
 		final double checkArg = Double.NaN;
 		FunctionNEW f = new Function_SixHumpCamelBack_2D();
 		Box box = new Box(dim, area);
 		return solveFunction(f, box, alg, checkValue, checkArg);
 	}
 	public final TestData f_RosenbrockGn(Algorithm alg, int dim, RealInterval area) {
 		FunctionNEW f = new Function_RosenbrockG_nD(dim);
 		Box box = new Box(dim, area);
 		final double checkValue = 0;
 		final double checkArg = 1;
		assert area.contains(checkArg) : "Global minima for this function is in point {1}. Test search area has to include it.";
 		return solveFunction(f, box, alg, checkValue, checkArg);
 	}
 	
 	
 	protected TestData solveFunction(FunctionNEW f, Box area, Algorithm algorithm, double checkValue, double checkArg) {
 		algorithm.setProblem(f, area);
 
 		long t0 = System.currentTimeMillis();
 		algorithm.solve();
 		long time = System.currentTimeMillis() - t0;
 		
 
 		final int dim = area.getDimension();
 		Box[] optArea = algorithm.getOptimumArea();
 		RealInterval optValue = algorithm.getOptimumValue();
 		
 //		System.out.println(f.toString());
 //		System.out.println("The computation took " + ((time+50)/100) + " sec.");
 	    
 		// check the value
     	if(!optValue.contains(checkValue)) {
    		if ((int)checkValue - checkValue !=0) { // check value looks like xx.xxx => can be approximate
     			final double delta = 1e-4;
     			if (Math.abs(optValue.hi() - checkValue) < delta ||
     					Math.abs(optValue.lo() - checkValue) < delta	) 
     			{
     				System.out.println(" > Found " + optValue + ", whiche check = " + checkValue + " (delta < " + delta + ")");
    			} 
    		}else {
	    		System.out.println(" > Wrong optimum value was found!");
	    		System.out.println(" > Found: " + optValue + ", it doesn't contain " + checkValue);
	    		System.out.println(" > Dim: " + dim + ", area: " + area.getInterval(0));
	    		fail("Wrong optimum VALUE was found by " + algorithm.toString() + " for " + f.toString() + " function!");
     		}
     	}
 		
 		// check the area
 		int[] argOK = new int[dim];
 		for (Box b : optArea) {
 //	    	System.out.println(b);
 	    	for (int i = 0; i < dim; i++) {
 	    		RealInterval ii = b.getInterval(i);
 	    		if(Double.isNaN(checkArg) // we do not know the check value 
 	    				|| (ii.hi() >= checkArg && ii.lo() <= checkArg) )
 	    			argOK[i]++;
 	    	}
 	    }
     	for (int i = 0; i < dim; i++) {
     		if(argOK[i] < 1) { // this dimension doesn't contain right result
         		System.out.println(" > Wrong area for optimum was found!");
         		System.out.println(" > On dimension #" + i + ". The known value for area is + " + checkArg);
         		fail("Wrong AREA for optimum was found by " + algorithm.toString() + " for " + f.toString() + " function!");
     			
     		}
     	}
     	TestData r = new TestData(f.toString(), algorithm.toString(), time, optValue, area.getInterval(0), dim, null);
     	return r; 
 	}
 }
