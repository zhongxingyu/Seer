 package prefwork.rating.method.rater.fuzzy;
 
 public class Metric implements Family{
 
 	private double simToDist(double x){
 		return 1-x;
		//return (1-x)/(x);
 	}
 	private double distToSim(double x){
 		return 1-x;
 		//return 1/(1+x);
 	}
 	/**
 	 * Tady se odcitaji vzdalenosti, takze scitaji podobnosti.
 	 */
 	public  double implicator(double x, double y, double lambda){		
 		return Math.max(0,(1+lambda)*distToSim((simToDist(x)-simToDist(y))));
 	
 	}
 	/**
 	 * Tady se scitaji vzdalenosti, tedy jakoby odcitaji podobnosti.
 	 */
 	public  double conjunctor(double x, double y, double lambda){	
 		return Math.min(1,(1-lambda)*distToSim((simToDist(x)+simToDist(y))));	
 	}
 	public double getMostSpecific() {
		return 0.0;
 	}
 	public String toString() {
 		return "Metric"+getMaxLambda();
 	}
 	@Override
 	public double getMaxLambda() {
		return 100;
 	}
 	@Override
 	public double getMinLambda() {
 		return 0;
 	}	
 	public double getEpsilon(double l) {
 		return BaseFunctions.Epsilon;
 	}		
 }
