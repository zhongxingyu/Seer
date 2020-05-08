 package interiores.business.models.backtracking;
 
 import interiores.business.models.FurnitureModel;
 import interiores.business.models.OrientedRectangle;
 import interiores.business.models.constraints.UnaryConstraint;
 import interiores.core.Debug;
 import interiores.shared.backtracking.Value;
 import interiores.shared.backtracking.Variable;
 import interiores.utils.Dimension;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 public class FurnitureVariable
 	implements Variable
 {
     private String identifier;
     
     private int resolution;
 
     /**
      * The domain of the variable.
      */
     private Domain domain;
     
     /**
      * This list contains the constraints regarding the variable.
      */
     public Collection<UnaryConstraint> unaryConstraints;
 
     /**
     * Represents the value taken by the variable, in case it is assigned.
     * Only valid when isAssigned is true.
     */
     public Value assignedValue;
     private boolean isAssigned;
     
     /**
     * Represents the iteration of the algorithm.
     */
     public int iteration;
     
     /**
      * Value of the cheapest model
      */
     private float minPrice;
     
     
     /**
      * Default Constructor. The resulting variable has as domain the models in
      * "models", every position in room and all orientations.
      * The set of restrictions is "unaryConstraints". Its resolution defaults to 5.
      * @pre the iteration of the variableSet is 0
      */
     public FurnitureVariable(String id, List<FurnitureModel> models, Dimension roomSize,
             Collection<UnaryConstraint> unaryConstraints, int variableCount) {
         
         this(id, models, roomSize, unaryConstraints, variableCount, 5);
     }
     
     
     public FurnitureVariable(String id, List<FurnitureModel> models,
             Dimension roomSize, Collection<UnaryConstraint> unaryConstraints,
             int variableCount, int resolution) { 
         
         identifier = id;
         this.resolution = resolution;
         
         isAssigned = false;
         iteration = 0;
     
         domain = new Domain(models, roomSize, variableCount, resolution);
         
         this.unaryConstraints = unaryConstraints;
         
         //minPrice not calculated yet
         minPrice = -1;
     }
 
     
     
     /**
      * Resets the iterators so that they will iterate through all of the
      * variables' domain, for the iteration "iteration" of the algorithm.
      */
     public void resetIterators(int iteration) {
         domain.resetIterators(iteration);
     }
     
     //Pre: we have not iterated through all domain values yet.
     @Override
     public Value getNextDomainValue() {
         return domain.getNextDomainValue(iteration);
     }  
     
     //Pre: the 3 iterators point to valid values
     @Override
     public boolean hasMoreValues() {
         return domain.hasMoreValues(iteration);
     }
 
     
     @Override
     public void assignValue(Value value) {
         isAssigned = true;
         assignedValue = value;
     }
 
     
     @Override
     public void undoAssignValue() {
         isAssigned = false;
         assignedValue = null;        
     }
   
     /**
      * Moves positions, models and orientations which are still valid to the
      * next level.
      * All positions from the HashSet at the position "iteration" which are
      * still valid must be moved to the HashSet in the position "iteration"+1.
      * To do this operation, we move all positions preliminarily, and then move
      * back those that are not valid. We estimate this reduces the amount of
      * HashSet operations.
      * All models from the List at the position "iteration" which are still 
      * valid must be moved to the List in the position "iteration"+1.
      */
     //pre: variable has an assigned value.
     //pre: if trimDomain or undoTrimDomain has already been called once,
     //     "iteration" value must be related to the value of "iteration" of the
     //     previous call (+1 if it was a trimDomain or equal if it was a
     //     undoTrimDomain).
     //     otherwise, it must be 0.
     //
     @Override
     public void trimDomain(Variable variable, int iteration) {
         // 0) update internal iteration
         this.iteration = iteration + 1;
         
         // Prepare next iteration
         // 1) preliminar move of all positions
         domain.saveAllPositions(iteration);
                
         // 2) send the affected positions back
         FurnitureValue value = (FurnitureValue) variable.getAssignedValue();
         OrientedRectangle invalidRectangle = value.getArea();
         
         domain.stripInvalidRectangle(invalidRectangle, iteration);        
         
         // 3) move all models
         domain.saveAllModels(iteration);
         
         // 4) move all orientations
         domain.saveAllOrientations(iteration);
     }
 
     
     /**
      * Merges back values from step "iteration"+1 to "iteration" level.
      * To do this operation, we swap the containers first if the destination
      * level's container has less elements.
      */
     //pre: trimDomain has already been called once.
     //     "iteration" value must be related to the value of "iteration" of the
     //     previous call to trimDomain or undoTrimDomain (equal if it was
     //     trimDomain or -1 if it was undoTrimDomain).
     @Override
     public void undoTrimDomain(Variable variable, Value value, int iteration) {
         domain.undoTrimDomain(iteration);       
     }
 
     
     @Override
     public boolean isAssigned() {
         return isAssigned;
     }
 
     
     @Override
     public Value getAssignedValue() {
         return assignedValue;
     }
 
     public String getID() {
         return identifier;
     }	
 
     void applyUnaryConstraints() {
        for (UnaryConstraint constraint : unaryConstraints)
             constraint.eliminateInvalidValues(domain);
     }	
     
     /**
      * Returns the price of the cheapest model.
      * @return 
      */
     // pre: iteration == 0
     public float getMinPrice() {
 
         if (minPrice >= 0) {
             //it is already calculated
             return minPrice;
         }
         else {
             Iterator<FurnitureModel> it = domain.getModels(0).iterator();
             if (it.hasNext()) {
                 FurnitureModel model = it.next();
                 minPrice = model.getPrice();
                 while (it.hasNext()) {
                     model = (FurnitureModel) it.next();
                     if (model.getPrice() < minPrice) minPrice = model.getPrice();
                 }
             }
             else {
                 //there are no models
                 minPrice = 0;
             }
             return minPrice;
         }
     }
     
     
     /**
      * Eliminates models from the domain such that exists another model
      * smaller and cheaper.
      */
     //pre: iteration == 0
     void trimUnfitModels() {
         Iterator<FurnitureModel> evaluatedModelIterator = domain.getModels(0).iterator();
         while (evaluatedModelIterator.hasNext()) {
             FurnitureModel evaluatedModel = evaluatedModelIterator.next();
             boolean hasBeenRemoved = false;
             Iterator<FurnitureModel> it = evaluatedModelIterator;
             while (! hasBeenRemoved && it.hasNext()) {
                 FurnitureModel model = it.next();
                 boolean evaluatedModelIsLessFit = (
                         evaluatedModel.getPrice() >= model.getPrice() &&
                         evaluatedModel.getSize().depth >= model.getSize().depth &&
                         evaluatedModel.getSize().width >= model.getSize().width);
                 if (evaluatedModelIsLessFit) {
                     evaluatedModelIterator.remove();
                     hasBeenRemoved = true;
                 }
             }
         }
     }
     
     
     void trimTooExpensiveModels(float maxPrice) {
         Iterator<FurnitureModel> it = domain.getModels(0).iterator();        
         while (it.hasNext()) {
             FurnitureModel model = it.next();
             if (model.getPrice() > maxPrice) it.remove();
         }
     }
     
     
     @Override
     public String toString() {
         StringBuilder result = new StringBuilder();
         String NEW_LINE = System.getProperty("line.separator");
 
         result.append(this.getClass().getName() + ":" + NEW_LINE);
         result.append("Assigned value: ");
         if (isAssigned) result.append(assignedValue.toString() + NEW_LINE);
         else result.append("none" + NEW_LINE);
         
         //result.append(" Models available" + NEW_LINE);
         //for (FurnitureModel model : domainModels)
         //        result.append(model.getName() + " ");
         
         
 //        result.append(" Positions available by iteration" + NEW_LINE);
 //        result.append("iteration    positions" + NEW_LINE);
 //        for (int i = 0; i < domainPositions.length && domainPositions[i] != null; ++i) {
 //            result.append(i + "             [ ");
 //            for (Point point : domainPositions[i]) {
 //                result.append("(" + point.x + "," + point.y + ") ");
 //            }
 //             result.append("]" + NEW_LINE);    
 //        }        
         
         result.append(" Constraints:" + NEW_LINE);
         for (UnaryConstraint constraint : unaryConstraints) {
             result.append(constraint.toString());
         }
 
         return result.toString();
     }
 
     void trimObstructedPositions() {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
 
 
     
 }
