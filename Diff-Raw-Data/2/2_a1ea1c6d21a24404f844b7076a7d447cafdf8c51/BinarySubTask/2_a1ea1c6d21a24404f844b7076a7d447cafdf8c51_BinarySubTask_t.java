 package crowdtrust;
 
 import java.util.Collection;
 
 import db.SubTaskDb;
 
 public class BinarySubTask extends SubTask {
 
 	public BinarySubTask(int id, double confidence_threshold, 
 			int number_of_labels, int max_labels){
		super(id, 0.8, number_of_labels, max_labels);
 	} 
 
 	@Override
 	protected void maximiseAccuracy(Accuracy a, Response r, Response z){
 		BinaryAccuracy ba = (BinaryAccuracy) a;
 		BinaryR br = (BinaryR) r;
 		BinaryR bz = (BinaryR) z;
 		
 		int total;
 		double w;
 		if (br.isTrue()){
 			//maximise truePositive
 			total = ba.getPositiveN() + 2;
 			w = (double) total/(total + 1);
 			double alpha = ba.getTruePositive()*total;
 			if(bz.isTrue())
 				ba.setTruePositive(w*(alpha/total) + (1-w));
 			else {
 				ba.setTruePositive(w*(alpha/total));
 			}
 			ba.incrementPositiveN();
 			
 		} else {
 			//maximize trueNegative
 			total = ba.getNegativeN() + 2;
 			w = (double) total/(total + 1);
 			
 			double alpha = ba.getTrueNegative()*total;
 			if(bz.isTrue())
 				ba.setTrueNegative(w*(alpha/total) + (1-w));
 			else {
 				ba.setTrueNegative(w*(alpha/total));
 			}
 			ba.incrementNegativeN();
 		}
 	}
 	
 	@Override
 	protected void updateLikelihoods(Response r,  Accuracy a, 
 			Collection<Estimate> state){
 		BinaryR br = (BinaryR) r;
 		BinaryAccuracy ba = (BinaryAccuracy) a;
 		
 		double accuracy;
 		if (br.isTrue())
 			accuracy = ba.getTruePositive();
 		else
 			accuracy = ba.getTrueNegative();
 		
 		if (state.isEmpty()){
 			BinaryR tR = new BinaryR(true);
 			Estimate t = new Estimate(tR, Math.log(getZPrior()/(1 - getZPrior())),0);
 			BinaryR fR = new BinaryR(false);
 			Estimate f = new Estimate(fR, Math.log(getZPrior()/(1 - getZPrior())),0);
 			state.add(t);
 			initEstimate(t);
 			state.add(f);
 			initEstimate(f);
 		}
 			
 		for (Estimate record : state){
 			Response recordResponse = record.getR();
 			if (!recordResponse.equals(br)){
 				record.setConfidence(record.getConfidence()
 						+ Math.log(accuracy/(1-accuracy)));
 				record.incFrequency();
 			} else {
 				record.setConfidence(record.getConfidence()
 						+ Math.log(((1-accuracy)/accuracy)));
 			}
 		}
 		
 	}
 
 	@Override
 	protected Accuracy getAccuracy(int annotatorId) {
 		return db.CrowdDb.getBinaryAccuracy(annotatorId);
 	}
 
 	@Override
 	protected void updateAccuracies(Collection<AccuracyRecord> accuracies) {
 		db.CrowdDb.updateBinaryAccuracies(accuracies);
 	}
 
 	@Override
 	protected double getZPrior() {
 		return 0.5;
 	}
 
 	@Override
 	protected double expertLimit() {
 		return 2;
 	}
 
 	@Override
 	protected void updateExperts(Collection<Bee> experts) {
 		db.CrowdDb.updateBinaryExperts(experts);
 		
 	}
 
 	@Override
 	protected void updateBots(Collection<Bee> bots) {
 		db.CrowdDb.updateBinaryBots(bots);
 	}
 
 	@Override
 	protected Collection <Estimate> getEstimates(int id) {
 		return SubTaskDb.getBinaryEstimates(id);
 	}
 
 	@Override
 	protected Collection<AccuracyRecord> getAnnotators() {
 		return db.CrowdDb.getBinaryAnnotators(id);
 	}
 }
