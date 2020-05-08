 package crowdtrust;
 
 import java.util.Collection;
 
 import org.apache.commons.math3.distribution.NormalDistribution;
 
 public class SingleContinuousSubTask extends ContinuousSubTask {
 
 	int [] range;
 	double variance;
 	
 	SingleContinuousSubTask(int id, double confidence_threshold, 
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
 		
 		NormalDistribution nd = 
 				new NormalDistribution(
 						cz.getValues(precision)[0], Math.sqrt(variance));
 		
 		double responseSpace = (range[1] - range[0])*precision;
 		
 		//mle
 		double pLabel = nd.density(cr.getValues(precision)[0]);
 		double mle = pLabel/(pLabel + 1/responseSpace);
 		sa.setAccuracy(w*(alpha/total) + (1-w)*mle);
 		a.increaseN();
 	}
 	
 	@Override
 	protected void updateLikelihoods(Response r, Accuracy a,
 			Collection<Estimate> state) {
 		ContinuousResponse cr = (ContinuousResponse) r;
 		SingleAccuracy sa = (SingleAccuracy) a;
 		
 		boolean matched = false;
 		
 		NormalDistribution nd = 
 				new NormalDistribution(
 						cr.getValues(precision)[0], Math.sqrt(variance));
 			
 		double pResponseSpace = 1/((range[1] - range[0])*precision);
 		
 		//base confidence to be computed 
 		//if the response has not appeared before
 		double freshConfidence = (getZPrior()/(1-getZPrior()));
 			
 		for (Estimate record : state){
 			if(record.getR().equals(r)){
 				matched = true;
 				record.incFrequency();
 			}
 			ContinuousResponse cr2 = (ContinuousResponse) record.getR();
 			double p = sa.getAccuracy()*nd.density(cr2.getValues(precision)[0]) + 
 				(1-sa.getAccuracy())*pResponseSpace;
			double newRatio = Math.log(p/pResponseSpace);
 			record.setConfidence(record.getConfidence() + newRatio);
 			freshConfidence += newRatio;
 		}
 			
 		if (!matched){
 			Estimate e = new Estimate(r, freshConfidence, 0);
 			state.add(e);
 			initEstimate(e);
 		}
 	}
 
 	@Override
 	protected double getZPrior() {
 		double p = 1/(range[1] - range[0])*precision;
 		return p/(1-p);
 	}
 
 	@Override
 	protected Collection<Estimate> getEstimates(int id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected void setRange(int[][] ranges) {
 		this.range = ranges[0];
 	}
 
 	@Override
 	protected void setDimensions(int length) {
 	}
 
 	@Override
 	protected void setVariance(double variance) {
 		this.variance = variance;
 	}
 
 
 }
