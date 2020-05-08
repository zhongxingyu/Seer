 package gr.auth.ee.lcs.geneticalgorithm;
 
 import gr.auth.ee.lcs.classifiers.ClassifierSet;
 /**
  * A Natural Selection operator performing a weighted roulette wheel selection
  * @author Miltos Allamanis
  *
  */
 public class WeightedRouletteSelector implements INaturalSelector {
 
 	/**
 	 * Roulette Wheel selection strategy. 
 	 * @param howManyToSelect the number of draws
 	 * @param fromPopulation the ClassifierSet from which the selection will take place
 	 * @param toPopulation the ClassifierSet to which the selected Classifers will be added
 	 */
 	public void select(int howManyToSelect, ClassifierSet fromPopulation, ClassifierSet toPopulation) {
 	  //Find total sum
 	  double fitnessSum=0;
 	  for (int i=0;i<fromPopulation.getNumberOfMacroclassifiers();i++){
 		  fitnessSum+=fromPopulation.getClassifierNumerosity(i)*fromPopulation.getClassifier(i).fitness;
 	  }
 	  
 	  //Repeat roulette for howManyToSelect times
 	  for (int i=0;i<howManyToSelect;i++){
 		  //Roulette
 		  double rand=Math.random()*fitnessSum;
		  double tempSum=0;
 		  int selectedIndex=-1;
 		  do{
 			  selectedIndex++;
 			  tempSum+=fromPopulation.getClassifierNumerosity(selectedIndex)*fromPopulation.getClassifier(selectedIndex).fitness;			  
 		  }while(tempSum<rand);	  
 		  //Add selectedIndex
 		  toPopulation.addClassifier(fromPopulation.getClassifier(selectedIndex), 1);
 	  }//next roulette
 	  
   }
 
 }
