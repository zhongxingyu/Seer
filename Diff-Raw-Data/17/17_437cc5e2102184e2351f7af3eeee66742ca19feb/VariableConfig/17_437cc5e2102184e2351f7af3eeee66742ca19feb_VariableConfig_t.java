 package interiores.business.models.backtracking;
 
 import interiores.business.models.Orientation;
 import interiores.business.models.OrientedRectangle;
 import interiores.business.models.constraints.furniture.BinaryConstraintEnd;
 import interiores.business.models.constraints.furniture.binary.MaxDistanceConstraint;
 import interiores.business.models.constraints.furniture.binary.MinDistanceConstraint;
 import interiores.business.models.constraints.furniture.binary.StraightFacingConstraint;
 import interiores.business.models.constraints.furniture.unary.WallConstraint;
 import interiores.business.models.room.FurnitureType;
 import interiores.business.models.room.elements.WantedFurniture;
 import interiores.utils.Dimension;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author hector
  */
 public class VariableConfig
 {
     private Dimension areaSize;
     private List<FurnitureConstant> constants;
     private List<FurnitureVariable> assignedVariables;
     private List<FurnitureVariable> unassignedVariables;
     private Map<FurnitureVariable, FurnitureType> furnitureTypes;
     
     private static final int AWAY_DISTANCE = 200;
     
     public VariableConfig(Dimension areaSize) {
         this.areaSize = areaSize;
         
         constants = new ArrayList();
         assignedVariables = new ArrayList();
         unassignedVariables = new ArrayList();
         furnitureTypes = new HashMap();
     }
     
     public void addVariable(FurnitureVariable variable, FurnitureType ftype) {
         if(variable.isAssigned())
             assignedVariables.add(variable);
         
         else {
             variable.undoAssignValue();
             unassignedVariables.add(variable);
         }
         
         furnitureTypes.put(variable, ftype);
     }
     
     public void addConstant(FurnitureConstant constant) {
         constants.add(constant);
     }
     
     public boolean isConsistent() {
         for(FurnitureVariable assignedVariable : assignedVariables)
             if(!assignedVariable.hasDomain())
                 return false;
         
         return unassignedVariables.isEmpty();
     }
     
     public void sort() {
         Collections.sort(assignedVariables);
     }
     
     public void resetDomains() {
         unassignedVariables.addAll(assignedVariables);
         assignedVariables.clear();
         
         for(FurnitureVariable unassignedVariable : unassignedVariables) {
             FurnitureType ftype = furnitureTypes.get(unassignedVariable);
             unassignedVariable.createDomain(ftype.getFurnitureModels(), areaSize, unassignedVariables.size());
             addDefaultConstraints(ftype,unassignedVariable);
         }
     }
     
     public void unassignLast() {
         int lastIndex = assignedVariables.size() - 1;
         
         if(lastIndex < 0)
             return;
         
         FurnitureVariable variable = assignedVariables.get(lastIndex);
         variable.undoAssignValue();
         
         unassignedVariables.add(variable);
         assignedVariables.remove(lastIndex);
     }
     
     public List<FurnitureVariable> getAssignedVariables() {
         return assignedVariables;
     }
     
     public List<FurnitureVariable> getUnassignedVariables() {
         return unassignedVariables;
     }
     
     public List<FurnitureConstant> getConstants() {
         return constants;
     }
     
     public OrientedRectangle getTotalArea() {
         return new OrientedRectangle(new Point(0, 0), areaSize, Orientation.S);
     }
 
     private void addDefaultConstraints(FurnitureType ftype, FurnitureVariable unassignedVariable) {
         
         // First, we treat wall-clinging
         if (ftype.shouldBeClungToWalls())
         {
             WallConstraint wc = new WallConstraint(areaSize.width, areaSize.depth, Orientation.values());
             unassignedVariable.addBacktrackingConstraint(wc);
         }
             
         // Afterwards, we assign all the binary constraints that could take place
         HashMap<String,String> pcs = ftype.getPlacementConstraints();
         for (String otype : pcs.keySet()) {
             // For each placement constraint defined, we get other end affected by (if any)
             FurnitureVariable end = getPossibleEnd(otype);
             if (end != null)
             {
                 // We translate the placement constraint into a binary one
                 BinaryConstraintEnd bce = translateIntoBinaryConstraint(pcs.get(otype));
                 // And try setting it to both variables affected
                 try {
                     bce.setOtherVariable(end);
                     bce.bound((WantedFurniture) unassignedVariable);
                     unassignedVariable.addBacktrackingConstraint(bce);
                 }
                 catch(Exception e) {}
             }
         }
         
     }
 
     private FurnitureVariable getPossibleEnd(String otype) {
        int nb_assignations = -1;
        FurnitureVariable end = null;
         
        // From all the suitable ends (if there's any), we select the one with less
        // binary constraints defined over it, so as to balance constraint load
         for (FurnitureVariable fv : unassignedVariables) {
            if (fv.getName().contains(otype) && nb_assignations < ((WantedFurniture) fv).getBinaryConstraints().size()) {
                nb_assignations = fv.getConstraints().size();
                end = fv;
            }
         }
        return end;
     }
 
     private BinaryConstraintEnd translateIntoBinaryConstraint(String placement) {
         BinaryConstraintEnd bce = null;
         if (placement.equals("next-to")) bce = new MaxDistanceConstraint(0);
         else if (placement.equals("away-from")) bce = new MinDistanceConstraint(AWAY_DISTANCE);
         else if (placement.equals("in-front-of")) bce = new StraightFacingConstraint(1000);
         
         return bce;
     }
 }
