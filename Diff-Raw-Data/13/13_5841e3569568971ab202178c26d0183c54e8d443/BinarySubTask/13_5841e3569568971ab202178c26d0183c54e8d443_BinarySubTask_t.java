 package crowdtrust;
 
 import java.util.Collection;
 
 import db.SubTaskDb;
 
 public class BinarySubTask extends SubTask {
 
 	public BinarySubTask(int id, double confidence_threshold, 
 			int number_of_labels, int max_labels){
 		super(id, confidence_threshold, number_of_labels, max_labels);
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
 			total = ba.getPositiveN();
 			w = total/total + 1;
 			
 			double alpha = ba.getTruePositive()*total;
 			if(bz.isTrue())
 				ba.setTruePositive(w*(alpha/total) + (1-w));
 			else {
 				ba.setTruePositive(w*(alpha/total));
 			}
 			
 			ba.incrementPositiveN();
 			
 		} else {
 			//maximize trueNegative
 			total = ba.getNegativeN();
 			w = total/total + 1;
 			
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
 		
 		boolean matched = false;
 		for (Estimate record : state){
 			if(record.getR().equals(br)){
 				record.setConfidence(record.getConfidence()
 						* (accuracy/(1-accuracy)));
 				matched = true;
 			} else {
 				record.setConfidence(record.getConfidence()
 						* ((1-accuracy)/accuracy));
 			}
 		}
 		
 		if (!matched){
 			Estimate e = new Estimate(r, getZPrior());
			double base = Math.pow((accuracy/(1-accuracy)), this.number_of_labels);
			e.setConfidence(e.getConfidence() * base);
 			state.add(e);
 			addEstimate(e);
 		}
 	}
 	
 	@Override
 	protected void addEstimate(Estimate e) {
 		db.SubTaskDb.addBinaryEstimate(e, id);
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
 		return 1;
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
 	protected void updateEstimates(Collection<Estimate> state) {
 		db.SubTaskDb.updateBinaryEstimates(state, id);
 	}
 
 	@Override
 	protected Collection<AccuracyRecord> getAnnotators() {
 		return db.CrowdDb.getBinaryAnnotators(id);
 	}
 }
