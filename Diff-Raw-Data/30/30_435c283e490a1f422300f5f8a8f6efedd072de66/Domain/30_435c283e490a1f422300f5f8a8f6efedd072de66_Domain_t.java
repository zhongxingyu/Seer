 
 package interiores.business.models.backtracking;
 
 import interiores.business.models.Orientation;
 import interiores.business.models.backtracking.Area.Area;
 import interiores.business.models.room.FurnitureModel;
 import interiores.shared.backtracking.Value;
 import interiores.utils.Dimension;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 /**
  * Domain represents the set of values which a given variable can take.
  * Moreover, it has information staged by algorithm iterations.
  * For each iteration, we know which values were available and which were not.
  * This allows stepping back.
  * @author nil.mamano
  */
 public class Domain {
       
     /**
      * The domain has a stage for each iteration.
      * It gives information about what values have been discarded and,
      * in such cases, in which iteration of the algorithm they were discarded:
      * At any given iteration i (depth = i) of the algorithm, all values which
      * have not been discarded are in the Stage at the i position of the vector.
      * Values which have been discarded are in the Stage at the position
      * correspondent to the iteration in which they were discarded.
      * Stages beyond the position i are empty.
      */
     private Stage[] domain;
     
     
     public Domain(HashSet<FurnitureModel> models, Dimension roomSize,
             int variableCount) {
         
        domain = new Stage[variableCount+1];
         
         domain[0] = new Stage(models, roomSize);
         
         for (int i = 1; i < variableCount; ++i) {
             domain[i] = new Stage();
         }
         
         
     }
     
     public static Domain empty() {
         return new Domain(new HashSet(), new Dimension(0, 0), 0);
     }
     
     public void resetIterators(int iteration) {
         domain[iteration].initializeIterators();
     }
     
     //Pre: we have not iterated through all domain values yet.
     public Value getNextDomainValue(int iteration) {   
         return domain[iteration].getNextDomainValue();
     }
     
     public boolean hasMoreValues(int iteration) {
         return domain[iteration].hasMoreValues();
     }
 
     
 
     
 
     
     public HashSet<FurnitureModel> getModels(int iteration) {
         return domain[iteration].getModels();
     }
     
     public Area getPositions(int iteration) {
         return domain[iteration].getPositions();
     }
     
     public HashSet<Orientation> getOrientations(int iteration) {
         return domain[iteration].getOrientations();
     }
 
 
 
     /**
      * Returns the size of the domain.
      * @param iteration
      * @return 
      */
     int domainSize(int iteration) {
         return domain[iteration].size();
     }
 
     void forwardIteration(int iteration) {
         domain[iteration].swapPositions(domain[iteration+1]);
         domain[iteration].swapModels(domain[iteration+1]);
         domain[iteration].swapOrientations(domain[iteration+1]);
     }
     
     void reverseIteration(int iteration) {
         //preliminarily move them all forward
         domain[iteration+1].union(domain[iteration]);
         
         //swap domains
         Stage aux = domain[iteration];
         domain[iteration] = domain[iteration+1];
         domain[iteration+1] = aux;
     }
     
     
     void trimExceptP(Area validPositions, int iteration) {
         //modify domain[iteration+1]
         Area discardedPositions = domain[iteration+1].intersectionP(validPositions);
         //modify domain[iteration]
         domain[iteration].unionP(discardedPositions);
         
     }
     
     void trimExceptM(HashSet<FurnitureModel> validModels, int iteration) {
         //modify domain[iteration+1]
         HashSet<FurnitureModel> discardedModels = domain[iteration+1].intersectionM(validModels);
         //modify domain[iteration]
         domain[iteration].unionM(discardedModels);
     }
     
     void trimP(Area invalidPositions, int iteration) {
         //modify domain[iteration+1]
         Area discardedPositions = domain[iteration+1].difference(invalidPositions);
         //modify domain[iteration]
         domain[iteration].unionP(discardedPositions);
 
     }
     
     
     void eliminateExceptM(HashSet<FurnitureModel> validModels) {
         domain[0].eliminateExceptM(validModels);
     }
     
     void eliminateExceptO(HashSet<Orientation> validOrientations) {
         domain[0].eliminateExceptO(validOrientations);
     }
     
     void eliminateExceptP(Area validPositions) {
         domain[0].eliminateExceptP(validPositions);
     }
     
      void trimExceptO(HashSet<Orientation> validOrientations, int iteration) {
         //modify domain[iteration+1]
         HashSet<Orientation> discardedModels = domain[iteration+1].intersectionO(validOrientations);
         //modify domain[iteration]
         domain[iteration].unionO(discardedModels);
     }
         
 }
