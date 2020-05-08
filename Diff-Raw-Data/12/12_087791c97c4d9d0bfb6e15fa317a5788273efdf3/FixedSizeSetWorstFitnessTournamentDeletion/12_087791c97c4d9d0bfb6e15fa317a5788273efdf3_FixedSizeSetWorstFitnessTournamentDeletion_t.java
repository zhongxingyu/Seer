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
	 * @param populationSize the size that the population will have
	 * @param tournamentSize the size of the tournaments done
 	 */
	public FixedSizeSetWorstFitnessTournamentDeletion(int populationSize, int tournamentSize){
 		this.populationSize=populationSize;	
		mySelector=new BestFitnessTournamentSelector(tournamentSize,false);
 	}
 	
 	/**
 	 * @see gr.auth.ee.lcs.classifiers.ISizeControlStrategy#controlSize(gr.auth.ee.lcs.classifiers.ClassifierSet)
 	 */
 	@Override
 	public void controlSize(ClassifierSet aSet) {
 		ClassifierSet toBeDeleted=new ClassifierSet(new DummySizeControlStrategy());
 		while (aSet.totalNumerosity>populationSize){
 			mySelector.select(1, aSet, toBeDeleted) ;
			aSet.deleteClassifier(toBeDeleted.getClassifier(0));
 			toBeDeleted.deleteClassifier(0);
 		}
 
 	}
 
 }
