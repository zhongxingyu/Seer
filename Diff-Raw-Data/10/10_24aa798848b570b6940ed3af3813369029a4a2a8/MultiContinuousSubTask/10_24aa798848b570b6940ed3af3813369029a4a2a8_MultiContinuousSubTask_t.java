 package crowdtrust;
 
 import java.util.Collection;
 
 import be.ac.ulg.montefiore.run.distributions.MultiGaussianDistribution;
 
 public class MultiContinuousSubTask extends ContinuousSubTask {
 	
 	int dimensions;
 	int [][] ranges;
 	double [][] variance;
 
 	public MultiContinuousSubTask(int id, double confidence_threshold, 
 			int number_of_labels, int max_labels){
 		super(id, confidence_threshold, number_of_labels, max_labels);
 	}
 
 	@Override
 	protected void maximiseAccuracy(Accuracy a, Response r, Response z){
 		SingleAccuracy sa = (SingleAccuracy) a;
 		ContinuousResponse cr = (ContinuousResponse) r;
 		ContinuousResponse cz = (ContinuousResponse) z;
 		
 		int total = a.getN() + 2;
 		double w = (double) total/(total + 1);
 		double alpha = sa.getAccuracy()*total;
 		
 		MultiGaussianDistribution mgd =
 				new MultiGaussianDistribution(
 						cz.getValues(precision), variance);
 		
 		double responseSpace = 1;
 		for (int i = 0; i < ranges.length; i++){
 			responseSpace *= (ranges[i][1] - ranges[i][0])*precision;
 		}
 		
 		//mle
 		double pLabel = mgd.probability(cr.getValues(precision));
 		double mle = pLabel/(pLabel + (1/responseSpace));
 		
 		sa.setAccuracy(w*(alpha/total) + (1-w)*mle);
 		a.increaseN();
 	}
 
 
 	@Override
 	protected void updateLikelihoods(Response r, Accuracy a,
 			Collection<Estimate> state) {
 		ContinuousResponse cr = (ContinuousResponse) r;
 		SingleAccuracy sa = (SingleAccuracy) a;
 		
 		boolean matched = false;
 		double responseSpace = 1;
 		for (int i = 0; i < ranges.length; i++){
 			responseSpace *= (ranges[i][1] - ranges[i][0])*precision;
 		}
 		
 		MultiGaussianDistribution mgd =
 				new MultiGaussianDistribution(
 						cr.getValues(precision), variance);
 		
 		//calculate confidence in case the response has not been seen before
 		double freshConfidence = (getZPrior()/(1-getZPrior()));
 		
 		for (Estimate record : state){
 			if(record.getR().equals(r)){
 				matched = true;
 				record.incFrequency();
 			}
 			ContinuousResponse cr2 = (ContinuousResponse) record.getR();
 			double p = sa.getAccuracy()*mgd.probability(cr2.getValues(precision)) +
 					(1 - sa.getAccuracy())/responseSpace;
			
			double newRatio = Math.log(p/(1/responseSpace));
 			record.setConfidence(record.getConfidence() + newRatio);
 			freshConfidence += newRatio;
 		}
 			
 		if (!matched){
 			Estimate e = new Estimate(r, getZPrior(), 0);
 			e.setConfidence(freshConfidence);
 			state.add(e);
 			initEstimate(e);
 		}
 	}
 
 	@Override
 	protected double getZPrior() {
 		double responseSpace = 1;
 		for (int i = 0; i < ranges.length; i++){
 			responseSpace *= (ranges[i][1] - ranges[i][0])*precision;
 		}
 		double p = 1/responseSpace;
 		return p/(1-p);
 	}
 	
 	protected static double[][] identity(double d, int dim) {
 		double[][] covariance = new double [dim][dim];
 		for (int i = 0; i < dim; i++){
 			covariance[i][i] = d;
 		}
 		return covariance;
 	}
 
 	@Override
 	protected void setRange(int[][] ranges) {
 		this.ranges = ranges;
 	}
 
 	@Override
 	protected void setDimensions(int length) {
 		dimensions = length;
 	}
 
 	@Override
 	protected void setVariance(double variance) {
 		this.variance = identity(variance, dimensions);
 	}
 }
