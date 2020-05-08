 package interiores.business.models.backtracking;
 
 import interiores.business.models.FurnitureModel;
 import interiores.business.models.Orientation;
 import interiores.business.models.OrientedRectangle;
 import interiores.business.models.Room;
 import interiores.business.models.constraints.BinaryConstraintSet;
 import interiores.business.models.constraints.UnaryConstraint;
 import interiores.core.Debug;
 import interiores.shared.backtracking.Value;
 import interiores.shared.backtracking.VariableSet;
 import interiores.utils.BinaryConstraintAssociation;
 import java.awt.Point;
 import java.util.Iterator;
 import java.util.List;
 
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
     private FurnitureVariable[] variables;
     
     /**
      * This is the variable that the algorithm is trying to assign in the
      * current iteration.
      */
     private FurnitureVariable actual;
     
     /**
      * Indicates whether all variables have an assigned value.
      */
     private boolean allAssigned;
 
     /**
      * Contains the room rectangle
      */
     private OrientedRectangle roomArea;
     
     /**
      * Indicates restrictions amongst two variables.
      */
     private BinaryConstraintSet binaryConstraints;
        
     /**
      * Default Constructor.
      */
     public FurnitureVariableSet(Room room, List<String> variableNames,
         List<List<FurnitureModel>> variablesModels, 
         List<List<UnaryConstraint>> variablesUnaryConstraints,
         List<BinaryConstraintAssociation> bca) {
         
         roomArea = new OrientedRectangle(new Point(0, 0), room.getDimension(), Orientation.S);
         
         variableCount = variablesModels.size();
 
         variables = new FurnitureVariable[variableCount];
         for(int i = 0; i < variableCount; ++i)
             variables[i] = new FurnitureVariable(variableNames.get(i), variablesModels.get(i), room,
                 variablesUnaryConstraints.get(i), variableCount);
 
         
        this.binaryConstraints = new BinaryConstraintSet();
        for (int i = 0; i < bca.size(); i++) {
            Debug.println("Adding Binary constraint " + bca.get(i).toString());
            Debug.println("Furniture1 is " + getVariable(bca.get(i).furniture1).getID());
            Debug.println("Furniture2 is " + getVariable(bca.get(i).furniture2).getID());
            Debug.println("Constraint is " + bca.get(i).constraint.toString());
            binaryConstraints.addConstraint(getVariable(bca.get(i).furniture1), getVariable(bca.get(i).furniture2),
                    bca.get(i).constraint);
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
         
         OrientedRectangle actualArea = ((FurnitureValue) value).getArea();
         
         if (roomArea.contains(actualArea)) {
             assignToActual(value);
             for (int i = 0; i < depth; ++i) {
                 OrientedRectangle otherArea = ((FurnitureValue) variables[i].getAssignedValue()).getArea();
                 if (!binaryConstraints.isSatisfied(actual, variables[i]) ||
                     actualArea.intersects(otherArea)) {
                     undoAssignToActual();
                     return false;
                 }
 
             }
             undoAssignToActual();
             return true;
         }
         return false;
 
     }
 
     
     @Override
     protected void assignToActual(Value value) {        
         actual.assignValue(value);
     }
 
     
     @Override
     protected void undoAssignToActual() {
         actual.undoAssignValue();
     }
     
     
     //note: trivial implementation. To be optimized.
     @Override
     protected void preliminarTrimDomains() {
         for (int i = 0; i < variableCount; ++i) {
             Iterator it = variables[i].unaryConstraints.iterator();
             while (it.hasNext()) {
                 UnaryConstraint constraint = (UnaryConstraint) it.next();
                 constraint.eliminateInvalidValues(variables[i]);
             }
         }
     }
     
     private FurnitureVariable getVariable(String name) {
         for (int i = 0; i < variableCount; i++)
             if (variables[i].getID().equals(name)) return variables[i];
         return null;
     }
     
     
     @Override
     public String toString() {
         StringBuilder result = new StringBuilder();
         String NEW_LINE = System.getProperty("line.separator");
 
         result.append(this.getClass().getName() + ":" + NEW_LINE);
         result.append("Iteration: " + depth + NEW_LINE);
         result.append("Number of variables: " + variableCount + NEW_LINE);
         
         result.append("Variables without assigned value: " + NEW_LINE);
         for (int i = depth; i < variableCount; ++i) {
            //result.append(variables[i].getID() + NEW_LINE);
             result.append(variables[i].getAssignedValue().toString() + NEW_LINE);
         }
 //        result.append("Actual variable:" + NEW_LINE);
 //        if (actual == null) result.append("none" + NEW_LINE);
 //        else result.append(actual.toString());
 //        result.append("Are all variables assigned? " + NEW_LINE);
         if (allAssigned) result.append("Yes" + NEW_LINE);
         else result.append("No" + NEW_LINE);
         result.append("Binary restrictions:" + NEW_LINE);
         result.append(binaryConstraints.toString());
 
         return result.toString();
     }
     
 }
 
 
 //    public enum Cell {
 //		ACTIVE,PASSIVE,EMPTY,SUPPORT,WALKPATH;
 //    }
 //    
 //    /**
 //    * This map contains, for each iteration of the algorithm,
 //    * the current state of all discrete positions of the room.
 //    * This attribute might be necessary to check that every place in the room is
 //    * accessible.
 //    */
 //    private Cell[][][] map;    
