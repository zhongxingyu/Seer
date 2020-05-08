 /**
  * 
  */
 package gr.auth.ee.lcs.impementations.meta;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import weka.core.Instances;
 import gr.auth.ee.lcs.AbstractLearningClassifierSystem;
 import gr.auth.ee.lcs.evaluators.AccuracyRecallEvaluator;
 import gr.auth.ee.lcs.evaluators.ExactMatchEvalutor;
 import gr.auth.ee.lcs.evaluators.HammingLossEvaluator;
 import gr.auth.ee.lcs.impementations.RTASLCS;
 import gr.auth.ee.lcs.meta.BaggedEnsemble;
 import gr.auth.ee.lcs.utilities.SettingsLoader;
 
 /**
  * An ensemble of RTASLCSs
  * @author Miltiadis Allamanis
  *
  */
 public class EnsembleRTASLCS extends BaggedEnsemble {
 
 	public EnsembleRTASLCS(AbstractLearningClassifierSystem[] lcss) {
 		super((int) SettingsLoader.getNumericSetting("numberOfLabels", 1), lcss);
 
 		ensemble = new RTASLCS[(int) SettingsLoader.getNumericSetting(
 				"ensembleSize", 7)];
 
 		for (int i = 0; i < ensemble.length; i++)
 			try {
 				ensemble[i] = new RTASLCS();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gr.auth.ee.lcs.meta.BaggedEnsemble#createNew()
 	 */
 	@Override	
 	public AbstractLearningClassifierSystem createNew() {
 
 		AbstractLearningClassifierSystem[] newEnsemble = new AbstractLearningClassifierSystem[ensemble.length];
 		for (int i = 0; i < ensemble.length; i++) {
 			newEnsemble[i] = ensemble[i].createNew();
 		}
		return new EnsembleBRSeqUCSComb(newEnsemble);
 
 	}
 
 	@Override
 	public String[] getEvaluationNames() {
 		String[] names = { "Accuracy(pcut)", "Recall(pcut)",
 				"HammingLoss(pcut)", "ExactMatch(pcut)", "Accuracy(ival)",
 				"Recall(ival)", "HammingLoss(ival)", "ExactMatch(ival)"
 				};
 		return names;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * gr.auth.ee.lcs.meta.BaggedEnsemble#getEvaluations(weka.core.Instances)
 	 */
 	@Override
 	public double[] getEvaluations(Instances testSet) {
 		this.setElements(ensemble[0].getClassifierTransformBridge(), null);
 		double[] results = new double[8];
 		Arrays.fill(results, 0);
 
 		for (int i = 0; i < ensemble.length; i++)
 			((RTASLCS) ensemble[i]).proportionalCutCalibration();
 
 		final AccuracyRecallEvaluator accEval = new AccuracyRecallEvaluator(
 				testSet, false, this, AccuracyRecallEvaluator.TYPE_ACCURACY);
 		results[0] = accEval.evaluateLCS(this);
 
 		final AccuracyRecallEvaluator recEval = new AccuracyRecallEvaluator(
 				testSet, false, this, AccuracyRecallEvaluator.TYPE_RECALL);
 		results[1] = recEval.evaluateLCS(this);
 
 		final HammingLossEvaluator hamEval = new HammingLossEvaluator(testSet,
 				false, numberOfLabels, this);
 		results[2] = hamEval.evaluateLCS(this);
 
 		final ExactMatchEvalutor testEval = new ExactMatchEvalutor(testSet,
 				false, this);
 		results[3] = testEval.evaluateLCS(this);
 
 		for (int i = 0; i < ensemble.length; i++) {
 			final AccuracyRecallEvaluator selfAcc = new AccuracyRecallEvaluator(
 					ensemble[i].instances, false, ensemble[i],
 					AccuracyRecallEvaluator.TYPE_ACCURACY);
 			((RTASLCS) ensemble[i]).internalValidationCalibration(selfAcc);
 		}
 
 		results[4] = accEval.evaluateLCS(this);
 		results[5] = recEval.evaluateLCS(this);
 		results[6] = hamEval.evaluateLCS(this);
 		results[7] = testEval.evaluateLCS(this);
 		
 
 		return results;
 	}
 
 }
