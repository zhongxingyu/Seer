 /**
  * 
  */
 package gr.auth.ee.lcs.classifiers;
 
 import gr.auth.ee.lcs.geneticalgorithm.BestFitnessTournamentSelector;
 
 /**
  * A fixed size control strategy. Classifiers are deleted by worst-tournaments
  * @author Miltos Allamanis
  *
  */
 public class FixedSizeSetWorstFitnessTournamentDeletion implements ISizeControlStrategy {
 
 	private BestFitnessTournamentSelector mySelector;
 	private int populationSize;
 	
 	/**
 	 * Constructor of deletion strategy
	 * @param populationSize
 	 */
	public FixedSizeSetWorstFitnessTournamentDeletion(int populationSize){
 		this.populationSize=populationSize;	
		mySelector=new BestFitnessTournamentSelector(10,false);
 	}
 	
 	/**
 	 * @see gr.auth.ee.lcs.classifiers.ISizeControlStrategy#controlSize(gr.auth.ee.lcs.classifiers.ClassifierSet)
 	 */
 	@Override
 	public void controlSize(ClassifierSet aSet) {
 		ClassifierSet toBeDeleted=new ClassifierSet(new DummySizeControlStrategy());
 		while (aSet.totalNumerosity>populationSize){
 			mySelector.select(1, aSet, toBeDeleted) ;
			aSet.deleteClassifier(0);
 			toBeDeleted.deleteClassifier(0);
 		}
 
 	}
 
 }
