 package interiores.business.models.backtracking;
 
 import interiores.business.models.Orientation;
 import interiores.business.models.backtracking.Area.Area;
 import interiores.business.models.constraints.Constraint;
 import interiores.business.models.constraints.furniture.BacktrackingTimeTrimmer;
 import interiores.business.models.constraints.furniture.InexhaustiveTrimmer;
 import interiores.business.models.constraints.furniture.PreliminarTrimmer;
 import interiores.business.models.room.FurnitureModel;
 import interiores.shared.backtracking.Value;
 import interiores.shared.backtracking.Variable;
 import interiores.utils.Dimension;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 public class FurnitureVariable
 	extends InterioresVariable
 {
     @XmlElementWrapper
     protected Map<Class, Constraint> furnitureConstraints;
     
     @XmlTransient
     protected Domain domain;
     
    /**
     * Represents the iteration of the algorithm.
     */
     @XmlTransient
     public int iteration;  
     
     @XmlTransient
     private float minPrice;
     private int maxWidth;
     private int maxDepth;
     private int minSize;
     
     /**
      * Default constructor.
      * JAXB needs it!
      */
     public FurnitureVariable()
     { }
     
     /**
      * Default Constructor. The resulting variable has as domain the models in
      * "models", every position in room and all orientations.
      * The set of restrictions is "unaryConstraints". Its resolution defaults to 5.
      * @pre the iteration of the variableSet is 0
      */
     public FurnitureVariable(String typeName)
     {
         super(typeName);
         
         furnitureConstraints = new HashMap();
     }
     
     public FurnitureVariable(String id, String typeName, FurnitureValue value) {
         this(typeName);
         
         assignValue(value);
     }
     
     @Override
     public boolean isConstant() {
         return false;
     }
     
     public void createDomain(HashSet<FurnitureModel> models, Dimension roomSize, int variableCount) {
         domain = new Domain(models, roomSize, variableCount);
         iteration = 0;
         initializeMaxMinFields();
         undoAssignValue();
     }
     
     public Collection<Constraint> getConstraints() {
         return furnitureConstraints.values();
     }
     
     /**
      * Resets the iterators so that they will iterate through all of the
      * variables' domains, for the iteration "iteration" of the algorithm.
      */
     public void resetIterators(int iteration) {
         // update internal iteration
         this.iteration = iteration;
         
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
   
     /**
      * Moves positions, models and orientations which are still valid to the
      * next level.
      * To do this operation, we move all values preliminarily, and then move
      * back those that are not valid.
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
         this.iteration = iteration;
         // 1) preliminar move of all positions
         forwardIteration();
                
         // 2) Run trimmers
         for (Constraint constraint : furnitureConstraints.values())
             if (constraint instanceof BacktrackingTimeTrimmer)
                 ((BacktrackingTimeTrimmer) constraint).trim(this);
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
         domain.reverseIteration(iteration);  
     }
     
     public Domain getDomain() {
         return domain;
     }
     
     /**
      * Returns the price of the cheapest model.
      * @return 
      */
     public float getMinPrice() {
         return minPrice;
     }
     
     private void initializeMaxMinFields() {
         minPrice = -1;
         maxWidth = 0;
         maxDepth = 0;
         minSize = -1;
         for (FurnitureModel model : domain.getModels(0)) {
             if (minPrice == -1 || model.getPrice() < minPrice)
                 minPrice = model.getPrice();
             if (model.getSize().width > maxWidth)
                 maxWidth = model.getSize().width;
             if (model.getSize().depth > maxDepth)
                 maxDepth = model.getSize().depth;
             if (minSize == -1 || model.areaSize() < minSize)
                 minSize = model.areaSize();
             
         }
         if (minPrice == -1) minPrice = 0;
         if (minSize == -1) minSize = 0;
     }
 
     public int domainSize() {
         return domain.domainSize(iteration);
     }
 
     public int smallestModelSize() {
         return minSize;
     }
 
     public void triggerPreliminarTrimmers() {
         for (Constraint constraint : furnitureConstraints.values()) {
             if (constraint instanceof PreliminarTrimmer) {
                 PreliminarTrimmer preliminarTrimmer = (PreliminarTrimmer) constraint;
                 preliminarTrimmer.preliminarTrim(this);
             }
             //ditch it if it doesn't implement any other interface
             if (! (constraint instanceof InexhaustiveTrimmer));
                 //removing constraints unhabilitated
                 //furnitureConstraints.remove(constraint.getClass());
         }
     }
 
     public boolean constraintsSatisfied() {
         for (Constraint constraint : furnitureConstraints.values())
             if (! ((InexhaustiveTrimmer) constraint).isSatisfied(this))
                 return false;
         return true;
     }
 
     public int getMaxWidth() {
         return maxWidth;
     }
 
     public int getMaxDepth() {
         return maxDepth;
     }
     
     //FUNCTIONS TO MODIFY THE DOMAIN
     //forwardIteration(), and consfraint - variable interface
     
     /**
      * Moves all values of the current iteration to the next iteration
      */
     public void forwardIteration() {
         domain.forwardIteration(iteration);
     }
     
    
     //CONSTRAINT - VARIABLE INTERFACE
     
     public void eliminateExceptP(Area validPositions) {
         domain.eliminateExceptP(validPositions);
     }
 
     
        /**
      * Any value of the domain of the next iteration included in the
      * parameter is trimmed (moved to the previous iteration)
      * @param invalidArea 
      */
     public void trimP(Area invalidArea) {
         domain.trimP(invalidArea, iteration);
     }
     
     
     /**
      * Any value of the domain of the next iteration not included in the
      * parameter is trimmed (moved to the previous iteration)
      * @param validArea 
      */
     public void trimExceptP(Area validArea) {
         domain.trimExceptP(validArea, iteration);
     }
 
     public void eliminateExceptM(HashSet<FurnitureModel> validModels) {
         domain.eliminateExceptM(validModels);
     }
 
     public void eliminateExceptO(HashSet<Orientation> validOrientations) {
         domain.eliminateExceptO(validOrientations);
     }
 
     public void trimExceptM(HashSet<FurnitureModel> validModels) {
         domain.trimExceptM(validModels, iteration);
     }
 
     public void trimExceptO(HashSet<Orientation> validOrientations) {
         domain.trimExceptO(validOrientations, iteration);
     }
     
 }
