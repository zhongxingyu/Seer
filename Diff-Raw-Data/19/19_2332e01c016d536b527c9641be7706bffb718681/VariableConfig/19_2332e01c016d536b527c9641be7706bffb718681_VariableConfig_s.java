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
 import interiores.core.Debug;
 import interiores.core.business.BusinessException;
 import interiores.utils.Dimension;
 import java.awt.Point;
 import java.util.ArrayList;
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
     private List<FurnitureVariable> variables;
     private Map<FurnitureVariable, FurnitureType> furnitureTypes;
     
     private static final int AWAY_DISTANCE = 200;
     
     public VariableConfig(Dimension areaSize) {
         this.areaSize = areaSize;
         
         constants = new ArrayList();
         variables = new ArrayList();
         furnitureTypes = new HashMap();
     }
     
     public void addVariable(FurnitureVariable variable, FurnitureType ftype) {
         variables.add(variable);
         furnitureTypes.put(variable, ftype);
     }
     
     public void addConstant(FurnitureConstant constant) {
         constants.add(constant);
     }
     
     public boolean isConsistent() {
         for(FurnitureVariable variable : variables)
             if(variable.isDirty() || !variable.isAssigned())
                 return false;
         
         for(FurnitureConstant constant : constants)
             if(constant.isDirty())
                 return false;
         
         return true;
     }
     
     public void reset() {
         for(FurnitureConstant fixed : constants)
             fixed.clean();
         
         for(FurnitureVariable variable : variables) {
             FurnitureType ftype = furnitureTypes.get(variable);
             variable.undoAssignValue();
             variable.createDomain(ftype.getFurnitureModels(), areaSize, variables.size());
            
             addDefaultConstraints(ftype, variable);
         }
     }
     
     public List<FurnitureVariable> getVariables() {
         return new ArrayList(variables);
     }
     
     public List<FurnitureConstant> getConstants() {
         return new ArrayList(constants);
     }
     
     public OrientedRectangle getTotalArea() {
         return new OrientedRectangle(new Point(0, 0), areaSize, Orientation.S);
     }
     
     private void addDefaultConstraints(FurnitureType ftype, FurnitureVariable variable) {
         
         // First, we treat wall-clinging
         if (ftype.shouldBeClungToWalls())
         {
             WallConstraint wc = new WallConstraint(areaSize.width, areaSize.depth, Orientation.values());
             variable.addBacktrackingConstraint(wc);
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
                 bce.setOtherVariable(end);
                 variable.addBacktrackingConstraint(bce);
                 
                 Debug.println("getting counterpart for " + bce + " respect from " + variable);
                 BinaryConstraintEnd bceCounterPart = bce.getCounterPart(variable);
                 Debug.println("otype = " + otype + ", bce = " + bce + ", counterpart = " + bceCounterPart);
                 end.addBacktrackingConstraint(bceCounterPart);
             }
         }
     }
 
     private FurnitureVariable getPossibleEnd(String otype) {
         int nb_assignations = -1;
         FurnitureVariable end = null;
         
         // From all the suitable ends (if there's any), we select the one with less
         // binary constraints defined over it, so as to balance constraint load
         for (FurnitureVariable fv : variables) {
             if (fv.getTypeName().equals(otype) &&
                     nb_assignations < ((WantedFurniture) fv).getBinaryConstraints().size())
             {
                 nb_assignations = fv.getConstraints().size();
                 end = fv;
             }
         }
         
         return end;
     }
 
     private BinaryConstraintEnd translateIntoBinaryConstraint(String placement) {
         BinaryConstraintEnd bce;
         
         if (placement.equals("next-to"))
             bce = new MaxDistanceConstraint(0);
         
         else if (placement.equals("away-from"))
             
             bce = new MinDistanceConstraint(AWAY_DISTANCE);
         
         else if (placement.equals("in-front-of"))
             bce = new StraightFacingConstraint();
         
         else
             throw new BusinessException("Unknown constraint: " + placement);
         
         return bce;
     }
     
     public boolean equals(VariableConfig other) {
         if(! areaSize.equals(other.areaSize))
             return false;
         
         if(! constants.equals(other.constants))
             return false;
         
         if(! variables.equals(other.variables))
             return false;
         
         return true;
     }
 }
