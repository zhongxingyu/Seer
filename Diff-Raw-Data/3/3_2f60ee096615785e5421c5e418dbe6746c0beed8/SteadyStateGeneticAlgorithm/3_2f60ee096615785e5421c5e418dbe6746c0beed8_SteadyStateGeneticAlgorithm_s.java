 package gr.auth.ee.lcs.geneticalgorithm;
 
 import gr.auth.ee.lcs.classifiers.Classifier;
 import gr.auth.ee.lcs.classifiers.ClassifierSet;
 import gr.auth.ee.lcs.classifiers.DummySizeControlStrategy;
 
 
 
 /** 
  *  A steady-stage GA that selects two individuals from a set (with probability proportional to their total fitness) and performs a crossover and mutation corrects the classifier (if needed) and adds it to the set
  *  @author Miltos Allamanis
  * 
  */
 public class SteadyStateGeneticAlgorithm implements IGeneticAlgorithmStrategy {
 
   /**
    * The selector used for the next generation selection
    */
   protected INaturalSelector gaSelector;
 
   /**
    * The crossover operator that will be used by the GA
    */
   protected IBinaryGeneticOperator crossoverOp;
   
   /**
    * The mutation operator used by the GA
    */
   protected IUnaryGeneticOperator mutationOp;
   
   /**
    * The GA activation age. The population must have an average age, greater that this
    * in order for the GA to run
    */
   protected int gaActivationAge;
   
   /**
    * The current timestamp. Used by the GA to count generations
    */
   private int timestamp=0;
   
   /**
    * Default constructor
    * @param gaSelector the INautralSelector that selects parents for next generation
    * @param crossoverOp the crossover operator that will be used
    * @param mutationOp the mutation operator that will be used
    * @param gaActivationAge the age of the population that activates the G.A.
    */
   public SteadyStateGeneticAlgorithm(INaturalSelector gaSelector, IBinaryGeneticOperator crossoverOp, IUnaryGeneticOperator mutationOp, int gaActivationAge){
 	  this.gaSelector=gaSelector;
 	  this.crossoverOp=crossoverOp;
 	  this.mutationOp=mutationOp;
 	  this.gaActivationAge=gaActivationAge;
   }
   
   @Override
   /**
    * Evolves a set
    * If the set is empty an exception will be thrown
    * 
    */
   public void evolveSet(ClassifierSet evolveSet, ClassifierSet population) {
 	 
 	  timestamp++;
 	  
 	  int meanAge=0;
 	  for (int i=0;i<evolveSet.getNumberOfMacroclassifiers();i++){
 		  meanAge+=evolveSet.getClassifierNumerosity(i)*evolveSet.getClassifier(i).timestamp;
 	  }
 	  meanAge/=evolveSet.getTotalNumerosity();
 	  if (timestamp-meanAge<this.gaActivationAge)
 		  return;
 	  
 	for (int i=0;i<evolveSet.getNumberOfMacroclassifiers();i++){
 		evolveSet.getClassifier(i).timestamp=timestamp;
 	}
 	  
 	ClassifierSet parents=new ClassifierSet(new DummySizeControlStrategy());
 	//Select parents
 	gaSelector.select(1, evolveSet, parents);
 	Classifier parentA=parents.getClassifier(0);
 	parents.deleteClassifier(0);
 	gaSelector.select(1, evolveSet, parents);
 	Classifier parentB=parents.getClassifier(0);
 	parents.deleteClassifier(0);
 	
 	//Reproduce
 	for (int i=0;i<2;i++){
 		//produce a child
 		Classifier child=crossoverOp.operate(parentA, parentB);
 		child=mutationOp.operate(child);
 		population.addClassifier(child,1);
 	}
   }
 
 }
