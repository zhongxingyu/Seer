 package interiores.business.models.backtracking;
 
 import interiores.business.exceptions.ElementNotFoundBusinessException;
 import interiores.business.exceptions.WantedElementNotFoundException;
 import interiores.business.models.FurnitureType;
 import interiores.business.models.Orientation;
 import interiores.business.models.OrientedRectangle;
 import interiores.business.models.WantedFurniture;
 import interiores.business.models.WishList;
 import interiores.business.models.catalogs.NamedCatalog;
 import interiores.business.models.constraints.GlobalConstraint;
 import interiores.core.Debug;
 import interiores.shared.backtracking.Value;
 import interiores.shared.backtracking.VariableSet;
 import interiores.utils.BinaryConstraintAssociation;
 import interiores.utils.Dimension;
 import java.awt.Point;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.PriorityQueue;
 import java.util.TreeMap;
 
 public class FurnitureVariableSet
 	extends VariableSet
 {	
 
     /**
      * Represents the number of variables.
      */
     private int variableCount;
     
     /**
      * Contains all the variables of the variable set.
      * Moreover, it gives information about what variables have an assigned
      * value and, in such cases, in which iteration of the algorithm they
      * were assigned:
      * At any given iteration i (depth = i) of the algorithm, the first i
      * elements of the vector have assigned values, and they were assigned in
      * the iteration correspondent to their position in the vector.
      * Variables beyond the first i positions have no assigned value yet.
      * 
      * The vector size never changes and is equal to the amount of variables
      * (which is also the number of iterations of the algorithm), but their
      * elements might be reallocated.
      */
     protected FurnitureVariable[] variables;
     
     /**
      * This is the variable that the algorithm is trying to assign in the
      * current iteration.
      */
     protected FurnitureVariable actual;
     
     /**
      * Indicates whether all variables have an assigned value.
      */
     protected boolean allAssigned;
 
     /**
      * Contains the room rectangle
      */
     private OrientedRectangle roomArea;
     
     /**
      * Indicates restrictions amongst two variables.
      */
     private VariableConstraintSet binaryConstraints;
    
     /**
      * Indicates restrictions amongst all variables.
      */
     Map<String, GlobalConstraint> globalConstraints;
             
     /**
      * Default Constructor.
      */
     public FurnitureVariableSet(WishList wishList, NamedCatalog<FurnitureType> furnitureCatalog)
             throws ElementNotFoundBusinessException, WantedElementNotFoundException
     {
         Dimension roomDimension = wishList.getRoom().getDimension();
         roomArea = new OrientedRectangle(new Point(0, 0), roomDimension, Orientation.S);
         
         variableCount = wishList.getSize();
         variables = new FurnitureVariable[variableCount];
         
         int i = 0;
 //        for(WantedFixed wantedFixed : wishList.getWantedFixed()) {
 //            String variableName = wantedFixed.getName();
 //            variables[i] = new FurnitureVariable(variableName, wantedFixed,
 //                    roomDimension, wantedFixed.getUnaryConstraints(), variableCount);
 //            ++i;
 //        }
         
        PriorityQueue<Entry<Integer, FurnitureVariable>> queue = new PriorityQueue(variableCount+1,
                 new Comparator<Entry<Integer, FurnitureVariable>>() {
                     @Override
                     public int compare(Entry<Integer, FurnitureVariable> e1,
                             Entry<Integer, FurnitureVariable> e2)
                     {
                         if(e1.getKey() > e2.getKey()) return -1;
                         if(e1.getKey() == e2.getKey()) return 0;
                         
                         return 1;
                     }
                 }
         );
         
         for(WantedFurniture wantedFurniture : wishList.getWantedFurniture()) {
             String variableName = wantedFurniture.getName();
             FurnitureType furnitureType = furnitureCatalog.get(wantedFurniture.getTypeName());
             
             int priority = wishList.getPriority(variableName);
             Debug.println("Adding variable " + variableName + " with " + priority + " binary "
                     + "constraints.");
             
             queue.add(new SimpleEntry(
                     priority,
                     new FurnitureVariable(variableName, furnitureType.getFurnitureModels(), roomDimension,
                         wantedFurniture.getUnaryConstraints(), variableCount)
                     ));
             i++;
         }
         
         i = 0;
         while(!queue.isEmpty()) {
             variables[i] = queue.poll().getValue();
             i++;
         }
         
         binaryConstraints = new VariableConstraintSet();
 
         for(BinaryConstraintAssociation bca : wishList.getBinaryConstraints()) {
             Debug.println("Adding Binary constraint " + bca.toString());
             Debug.println("Furniture1 is " + getVariable(bca.furniture1).getID());
             Debug.println("Furniture2 is " + getVariable(bca.furniture2).getID());
             Debug.println("Constraint is " + bca.constraint.toString());
 
             binaryConstraints.addConstraint(getVariable(bca.furniture1),
                     getVariable(bca.furniture2), bca.constraint);
         }
         
         allAssigned = false;
         actual = null;
     }
    
 
     /**
      * Selects a variable from variables[depth..variableCount-1] and sets it
      * as actual variable.
      * The iterators of the actual variable are reset.
      */
     //note: trivial implementation. To be optimized.
     @Override
     protected void setActualVariable() {
         if (variables.length != 0) {
             actual = variables[depth];
             actual.resetIterators(depth);
         }
         else {
             allAssigned = true;
         }
     }
  
     
     @Override
     protected void trimDomains() {
         for (int i = depth + 1; i < variableCount; ++i) {
             variables[i].trimDomain(actual, depth);
         }
     }
 
     
     @Override
     protected void undoTrimDomains(Value value) {
         for (int i = depth + 1; i < variableCount; ++i) {
             variables[i].undoTrimDomain(actual, value, depth);
         }
     }
 
     
     @Override
     protected boolean allAssigned() {
         if (depth == (variableCount - 1) && actual.isAssigned()) {
             allAssigned = true;
         }
         return allAssigned;
     }
 
     
     @Override
     protected boolean actualHasMoreValues() {
         return actual.hasMoreValues();
     }
     
     
     @Override
     protected Value getNextActualDomainValue() {
         return actual.getNextDomainValue();
     }
     
     
     //note: preliminar implementation. Final implementation should take more
     //things into consideration (e.g., not blocking paths)
     @Override
     protected boolean canAssignToActual(Value value) {
         
         FurnitureValue actual_fv = (FurnitureValue) value;
         // A little explanation: fv.getArea() gets the ACTIVE area of actual_fv
         // while fv.getWholeArea() gets the PASSIVE + ACTIVE area of actual_fv
         
         if (! roomArea.contains(actual_fv.getWholeArea())) return false;
 
         actual.assignValue(value);
         for (int i = 0; i < depth; ++i) {
             FurnitureValue other_fv = (FurnitureValue) variables[i].getAssignedValue();
             
             if (!binaryConstraints.isSatisfied(actual, variables[i])
                 || actual_fv.getArea().intersects(other_fv.getWholeArea())
                 || actual_fv.getWholeArea().intersects(other_fv.getArea()) )
             {
                 actual.undoAssignValue();
                 return false;
             }
 
         }
         actual.undoAssignValue();
         return true;
     }
 
     
     @Override
     protected void assignToActual(Value value) {        
         actual.assignValue(value);
     }
 
     
     @Override
     protected void undoAssignToActual() {
         if (depth >= 0) {
             actual = variables[depth];
         }
         actual.undoAssignValue();
     }
     
     
     //note: trivial implementation. To be optimized.
     @Override
     protected void preliminarTrimDomains() {
         
         //1) remove values which do not fit some unary constraint
         for (int i = 0; i < variableCount; ++i)
             variables[i].applyUnaryConstraints();
         
         //2) remove pieces of furniture such that there is another piece
         // smaller and cheaper
         for (int i = 0; i < variableCount; ++i)
             variables[i].trimUnfitModels();
         
         // @TODO Refactorize
         //3) remove furniture too expensive
         /*float minBudget = 0;
         for (int i = 0; i < variableCount; ++i)
             minBudget += variables[i].getMinPrice();
        
         for (int i = 0; i < variableCount; ++i) {
             //if a model from this variable is more expensive than maxPrice,
             //there is no possible assignmentment to variables such that
             //variables[i] has assigned this model and the maxBudget is not exceeded 
             float maxPrice = maxBudget - ( minBudget - variables[i].getMinPrice());
             variables[i].trimTooExpensiveModels(maxPrice);
         }
         
         //4) remove positions such that no model fit there due to walls and
         //topology elements
         for (int i = 0; i < variableCount; ++i)
             variables[i].trimObstructedPositions();*/
         
     }
     
     
     private FurnitureVariable getVariable(String name) {
         for (int i = 0; i < variableCount; i++)
             if (variables[i].getID().equals(name)) return variables[i];
         return null;
     }
    
     
     public Map<String, FurnitureValue> getValues() {
         Map<String, FurnitureValue> values = new HashMap();
         
         for(int i = 0; i < variables.length; ++i)
             values.put(variables[i].getID(), (FurnitureValue) variables[i].getAssignedValue());
         
         return values;
     }
  
     
     @Override
     public String toString() {
         StringBuilder result = new StringBuilder();
         String NEW_LINE = System.getProperty("line.separator");
 
         result.append(this.getClass().getName() + ":" + NEW_LINE);
         result.append("Iteration: " + depth + NEW_LINE);
         result.append("Number of variables: " + variableCount + NEW_LINE);
         
         result.append("Variables: " + NEW_LINE);
         for (int i = depth; i < variableCount; ++i) {
             result.append(variables[i].getID() + ": ");
             result.append(variables[i].getAssignedValue().toString() + NEW_LINE);
         }
 //        result.append("Actual variable:" + NEW_LINE);
 //        if (actual == null) result.append("none" + NEW_LINE);
 //        else result.append(actual.toString());
 //        result.append("Are all variables assigned? " + NEW_LINE);
         if (allAssigned) result.append("All assigned: Yes" + NEW_LINE);
         else result.append("No" + NEW_LINE);
         result.append("Binary restrictions:" + NEW_LINE);
         result.append(binaryConstraints.toString());
 
         return result.toString();
     }
 }
