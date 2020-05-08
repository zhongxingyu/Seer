 package interiores.business.models.backtracking;
 
 import interiores.business.models.constraints.Constraint;
 import interiores.business.models.constraints.furniture.BinaryConstraintEnd;
 import interiores.business.models.constraints.room.GlobalConstraint;
 import interiores.business.models.constraints.room.RoomBacktrackingTimeTrimmer;
 import interiores.business.models.constraints.room.RoomInexhaustiveTrimmer;
 import interiores.business.models.constraints.room.RoomPreliminarTrimmer;
 import interiores.business.models.constraints.room.global.BudgetConstraint;
 import interiores.business.models.constraints.room.global.EnoughSpaceConstraint;
 import interiores.business.models.constraints.room.global.SameColorConstraint;
 import interiores.business.models.constraints.room.global.SameMaterialConstraint;
 import interiores.business.models.constraints.room.global.SpaceRespectingConstraint;
 import interiores.business.models.constraints.room.global.UnfitModelsPseudoConstraint;
 import interiores.core.Debug;
 import interiores.core.business.BusinessException;
 import interiores.shared.backtracking.NoSolutionException;
 import interiores.shared.backtracking.Value;
 import interiores.shared.backtracking.VariableSet;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class Solver
 	extends VariableSet
 {
     /**
      * Represents the variable configuration that this solver must solve.
      */
     protected VariableConfig variableConfig;
     
     /**
      * Represents the number of variables.
      */
     private int variableCount;
 
     /**
      * This list contains the variables which do not have an assigned value yet.
      */
     private List<FurnitureVariable> unassignedVariables;
     
     /**
      * This list contains the variables which have an assigned value.
      */
     private List<FurnitureVariable> assignedVariables;
     
     /**
      * This is the variable that the algorithm is trying to assign in the
      * current iteration.
      * It is not in either of the previous lists.
      */
     protected FurnitureVariable actual;
     
     private List<FurnitureConstant> constants;
     
     /**
      * Indicates whether all variables have an assigned value.
      */
     private int jumpToDepth;
     protected boolean allAssigned;
     private boolean stepReset;
     protected boolean shouldStop;
     
     /**
      * Indicates restrictions amongst all variables.
      */
     private List<GlobalConstraint> globalConstraints;
     
     
    /**
     * Holds information about the relationship between constraints of
     * different variables.
     * The first string identifies the ID of the variable that, when assigned,
     * the domain of the variable identified by the second string, will be
     * trimmed proportionally to the dependence stored in this matrix.
     * 
     * Detailed explanation in the header of Constraint class.
     */
    private Map<Entry<String, String>, Integer> matrixOfDependence;
     
     /**
      * constants to calibrate the selection of the next actual variable.
      * The importance of each factor is proportional to the value of the
      * constant.
      */
    private static final int DOMAIN_SIZE_FACTOR = 60;
    private static final int SMALLEST_MODEL_FACTOR = 10;
    private static final int BINARY_CONSTRAINTS_FACTOR = 5;
     
     /**
      * Constructor.
      * This constructor does all the initilization work:
      * 
      * roomArea is initialized with the room rectangle.
      * 
      * variableCount is initialized with the number of variables.
      * 
      * allAssigned is initialized false.
      * 
      * assignedVariables is initialized empty.
      * 
      * actual is initialized null.
      * 
      * unassignedVariables is initialized with all variables.
      * Each variable is initialized with:
      * - all the models available for its furniture type.
      * - all the orientations
      * - all the positions of the room (roomArea)
      * - the list of unary constraints it is associated with.
      * 
      * constants is initialized with all FurnitureConstants.
      * 
      * globalConstraints is initialized with all global constraints.
      * The TrimUnfitModelsPseudoContraint is placed last in the list.
      * 
      * The matrix of dependence is built
      */
     public Solver(VariableConfig variableConfig, Collection<GlobalConstraint> gconst)
             throws BusinessException
     {
         this.variableConfig = variableConfig;
 
         allAssigned = false;
         stepReset = false;
         shouldStop = false;
         actual = null;
         
         globalConstraints = new ArrayList<GlobalConstraint>(gconst);
         addGlobalConstraints();
     }
     
     public boolean hasConfiguration(VariableConfig otherConfig) {
         return variableConfig.equals(otherConfig);
     }
     
     @Override
     public void solve() throws NoSolutionException {
         if(allAssigned)
             next();
         
         else {
             initVariables();
             super.solve();
         }
     }
     
     protected void initVariables() {
         variableConfig.reset();
         
         assignedVariables = new ArrayList();
         unassignedVariables = variableConfig.getVariables();
         constants = variableConfig.getConstants();
         variableCount = unassignedVariables.size();
         jumpToDepth = -1;
         
         buildMatrixOfDependence();
     }
     
     private void next() throws NoSolutionException {
         actual.undoAssignValue();
         unassignedVariables.add(actual);
         
         actual = null;
         allAssigned = false;
         depth = 0;
         jumpToDepth = assignedVariables.size();
         
         backtracking();
     }
     
     /**
      * Invoked in the constructor to initialize global constraints.
      */
     private void addGlobalConstraints() {
         globalConstraints.add(new SpaceRespectingConstraint(variableConfig.getTotalArea()));
         globalConstraints.add(new EnoughSpaceConstraint(variableConfig.getTotalArea()));
         
         boolean sameColorConstraintActive = false;
         boolean sameMaterialConstraintActive = false;
         boolean budgetConstraintActive = false;
         for (GlobalConstraint constraint : globalConstraints) {
             if (constraint instanceof SameColorConstraint)
                 sameColorConstraintActive = true;
             else if (constraint instanceof SameMaterialConstraint)
                 sameMaterialConstraintActive = true;
             else if (constraint instanceof BudgetConstraint)
                 budgetConstraintActive = true;
         }
         
         globalConstraints.add(new UnfitModelsPseudoConstraint(sameColorConstraintActive,
                 sameMaterialConstraintActive, budgetConstraintActive));
     }
     
 
     /**
      * Selects a variable from unassignedVariables and sets it
      * as actual variable.
      * 
      * Before setting a new actual variable, the previous one is stored in the
      * list of assignedVariables.
      * 
      * The variable is chosen according to 3 factors:
      * 1) The domain size: the smallest the better
      * 2) The binary constraints with variables that have not been set: the more
      * the better
      * 3) The size of the smallest model: the more the better
      * 
      * Each factor has a weight defined by a global constant, so that it can
      * be adjusted.
      * 
      * The iterators of the actual variable are reset.
      */ 
     @Override
     protected void setActualVariable() {
         if (unassignedVariables.isEmpty())
             allAssigned = true;
         
         else if(depth < jumpToDepth) {
             Debug.println(depth + " " + jumpToDepth);
             actual = assignedVariables.get(depth);
             
             if(depth + 1 == jumpToDepth)
                 assignedVariables.remove(depth);
         }
         
         else {
             //move previousactual to assigned, unless it is the first iteration
             if (actual != null)
                 assignedVariables.add(actual);
             
             int maxDomainSize = -1;
             int maxBinaryConstraints = -1;
             int maxSmallestModelArea = -1;
             int[] domainSize = new int[unassignedVariables.size()];
             int[] binaryConstraintsLoad = new int[unassignedVariables.size()];
             int[] smallestModelSize = new int[unassignedVariables.size()];
 
             int i = 0;
             //get value of each factor for each variable and compute maximums
             for (FurnitureVariable variable : unassignedVariables) {
                domainSize[i] = variable.domainSize(depth);
                 if (domainSize[i] > maxDomainSize) maxDomainSize = domainSize[i];
 
                 // the weight of all binary constraints between this variable and
                 // other variables which have not been assigned yet.
                 binaryConstraintsLoad[i] = 0;
                 
                 for (FurnitureVariable otherVariable : unassignedVariables) {
                     binaryConstraintsLoad[i] += getDependence(variable, otherVariable);
                 }
                 
                 if (binaryConstraintsLoad[i] > maxBinaryConstraints)
                     maxBinaryConstraints = binaryConstraintsLoad[i];
 
                 smallestModelSize[i] = variable.smallestModelSize();
                 if (smallestModelSize[i] > maxSmallestModelArea)
                     maxSmallestModelArea = smallestModelSize[i];
                 
                 ++i;
             }
 
             //calculate the weight of each factor for each variable and the final
             //weight of each variable, and compute the index of the variable with
             //the lowest overall weight
             int minimumWeight = -1;
             int minimumWeightVariableIndex = -1;
 
             for (i = 0; i < unassignedVariables.size(); ++i) {
 
                 int domainSizeWeight;
                 if (maxDomainSize > 0) domainSizeWeight =
                         (domainSize[i] * DOMAIN_SIZE_FACTOR) / maxDomainSize;
                 else domainSizeWeight = 0;
                 
                 int binaryConstraintsWeight;
                 if (maxBinaryConstraints > 0) binaryConstraintsWeight = maxBinaryConstraints -
                         (binaryConstraintsLoad[i] * BINARY_CONSTRAINTS_FACTOR) / maxBinaryConstraints;
                 else binaryConstraintsWeight = 0;
                 
                int smallestModelSizeWeight = maxSmallestModelArea -
                         (smallestModelSize[i] * SMALLEST_MODEL_FACTOR) / maxSmallestModelArea;
 
                 int variableWeight = domainSizeWeight + binaryConstraintsWeight +
                         smallestModelSizeWeight;
 
                 if (minimumWeight == -1 || variableWeight < minimumWeight) {
                     minimumWeight = variableWeight;
                     minimumWeightVariableIndex = i;
                 }
 
             }
             
             //set actual variable
             actual = unassignedVariables.get(minimumWeightVariableIndex);
             
             //remove it from the set of unassigned variables
             unassignedVariables.remove(minimumWeightVariableIndex);
             
             //reset iterators
             if(depth > jumpToDepth)
                 actual.resetIterators(depth);
             
             jumpToDepth = -1;
         }
     }
  
     @Override
     protected void trimDomains() {
         if(depth < jumpToDepth)
             return;
         
         for (FurnitureVariable variable : unassignedVariables) {
             variable.trimDomain(actual, depth);   
         }
         
         //global constraints that implement the bactracking time trimmer interface
         //are triggered
         for (GlobalConstraint constraint : globalConstraints) {
             if (constraint instanceof RoomBacktrackingTimeTrimmer)
                 ((RoomBacktrackingTimeTrimmer) constraint).trim(
                     assignedVariables, unassignedVariables, constants, actual);
         }
     }
 
     @Override
     protected void undoTrimDomains(Value value) {
         for (FurnitureVariable variable : unassignedVariables) {
             variable.undoTrimDomain(actual, value, depth);
         }
         
         for (GlobalConstraint constraint : globalConstraints) {
             ((RoomInexhaustiveTrimmer) constraint).notifyStepBack(
                     assignedVariables, unassignedVariables, constants, actual, (FurnitureValue) value);
         }
     }
 
     @Override
     protected boolean allAssigned() {
         if(allAssigned)
             return true;
         
         if (unassignedVariables.isEmpty() && actual.isAssigned()) {
             allAssigned = true;
         }
         
         return allAssigned;
     }
 
     @Override
     protected boolean actualHasMoreValues() {
         if(depth < jumpToDepth)
             return true;
         
         return actual.hasMoreValues();
     }
     
     @Override
     protected Value getNextActualDomainValue() {
         if(depth < jumpToDepth)
             return actual.getAssignedValue();
         
         return actual.getNextDomainValue();
     }
     
     @Override
     protected boolean canAssignToActual(Value value)
             throws NoSolutionException
     {
         if(depth < jumpToDepth)
             return true;
         
         if(shouldStop)
             throw new NoSolutionException("Solver stopped manually");
         
         //temporal assignment
         actual.assignValue(value);
         
         //1) check that furniture constraints are satisfied
         if (! actual.constraintsSatisfied()) {
             actual.undoAssignValue();
             return false;
         }
         
         //2) check that room constraints are satisfied
         for (GlobalConstraint constraint : globalConstraints) {
             if (! ((RoomInexhaustiveTrimmer) constraint).isSatisfied(
                 assignedVariables, unassignedVariables, constants, actual))
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
         if(depth < jumpToDepth)
             return;
         
         actual.assignValue(value);
     }
 
     
     @Override
     protected void undoAssignToActual() {
         actual.undoAssignValue();
     }
     
     //note: trivial implementation. To be optimized.
     @Override
     protected void preliminarTrimDomains()
             throws NoSolutionException
     {
         //1) each variable triggers its own preliminar trimmers
         for (FurnitureVariable variable : unassignedVariables)
             variable.triggerPreliminarTrimmers();
         
         //2) global constraints that implement the preliminar trimmer interface
         //are triggered
         Iterator<GlobalConstraint> it = globalConstraints.iterator();
         while(it.hasNext()) {
             GlobalConstraint constraint = it.next();
             if (constraint instanceof RoomPreliminarTrimmer) {
                 ((RoomPreliminarTrimmer) constraint).preliminarTrim(unassignedVariables, constants);
             }
             //ditch it if it doesn't implement any other interface
             if (! (constraint instanceof RoomInexhaustiveTrimmer))
                 it.remove();
         }
     }
     
     // Algorithm to build the matrix of dependence:
     // Initialize mod to all 0.
     // For each binary constraint bc:
     //      //assume v is the variable associated with bc
     //      //and w is the otherVariable of bc (bc.getOtherVariable())
     //      if w is not a constant:
     //          mod[w][v] = bc.getWeight()
     //      //this means that if w is assigned a value, the domain of v will be
     //      //restricted proportionally to the weight of bc.
     private void buildMatrixOfDependence() {
 
         matrixOfDependence = new HashMap<Entry<String, String>, Integer>();
                 
         for (FurnitureVariable v : unassignedVariables) {
             for (Constraint constraint : v.getBacktrackingConstraints()) {
                 if (constraint instanceof BinaryConstraintEnd) {
                     BinaryConstraintEnd bc = (BinaryConstraintEnd) constraint;
                     
                     InterioresVariable otherVariable = bc.getOtherVariable();
                     if (otherVariable instanceof FurnitureVariable) {
                         FurnitureVariable w = (FurnitureVariable) otherVariable;
 
                         Entry e = new SimpleEntry(w.getName(), v.getName());
                         matrixOfDependence.put(e, bc.getWeight());
                     }
                 }
             }
         }
     }
     
     private int getDependence(FurnitureVariable variable1, FurnitureVariable variable2) {
         Entry e = new SimpleEntry(variable1.getName(), variable2.getName());
         
         if (! matrixOfDependence.containsKey(e))
             return 0;
         
         return matrixOfDependence.get(e);
     }
     
     @Override
     protected void backtracking() throws NoSolutionException {
         super.backtracking();
         
         if(!allAssigned())
             undoSetActualVariable();
     }
     
     protected void undoSetActualVariable() {
         unassignedVariables.add(actual);
         if (! assignedVariables.isEmpty()) {
             actual = assignedVariables.get(assignedVariables.size()-1);
             assignedVariables.remove(assignedVariables.size()-1);
         }
     }
 }
