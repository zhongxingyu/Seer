 package interiores.business.models.constraints.furniture;
 
 import interiores.business.models.backtracking.FurnitureVariable;
 import interiores.business.models.backtracking.InterioresVariable;
 import interiores.business.models.constraints.Constraint;
 import interiores.business.models.constraints.furniture.binary.MaxDistanceConstraint;
 import interiores.business.models.constraints.furniture.binary.MinDistanceConstraint;
 import interiores.business.models.room.elements.WantedFurniture;
 import interiores.core.Debug;
 import interiores.core.business.BusinessException;
 import java.util.Collection;
 import java.util.Map;
 import java.util.TreeMap;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlSeeAlso;
 
 /**
  *
  * @author alvaro
  */
 @XmlRootElement
 @XmlSeeAlso({MaxDistanceConstraint.class, MinDistanceConstraint.class})
 public abstract class BinaryConstraintEnd
     extends Constraint
     implements InexhaustiveTrimmer, BacktrackingTimeTrimmer, Cloneable
 {
     
     private static Map<String, Class<? extends Constraint>> availableConstraints = new TreeMap();
     
     public static void addConstraintClass(String name, Class<? extends BinaryConstraintEnd> constraintClass)
     {
         addConstraintClass(availableConstraints, name, constraintClass);
     }
     
     public static Class<? extends BinaryConstraintEnd> getConstraintClass(String name)
             throws BusinessException
     {
         return (Class<? extends BinaryConstraintEnd>) getConstraintClass(availableConstraints, name, "binary");
     }
     
     public static Collection<String> getConstraintClasses() {
         return availableConstraints.keySet();
     }
     
     
     
     //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
     
     protected InterioresVariable otherVariable;
     
     public BinaryConstraintEnd()
     { }
     
     public BinaryConstraintEnd(InterioresVariable otherVariable) {
         this.otherVariable = otherVariable;
     }
     
     public void setOtherVariable(InterioresVariable otherVariable) {
         if(this.otherVariable != null)
             throw new UnsupportedOperationException("You can only set the other variable once.");
         
         this.otherVariable = otherVariable;
     }
     
     /**
      * Given 2 variables, of which the first one has an assigned value, trims
      * the domain of the second variable according to the constraint.
      * @param assignedVariable
      * @param toTrimVariable
      * @param room
      */
     @Override
     public final void trim(FurnitureVariable variable) {
         if (otherVariable.isAssigned())
             trim2(variable);
     }
  
     public abstract void trim2(FurnitureVariable variable);
 
     @Override
     public final boolean isSatisfied(FurnitureVariable variable) {
         if (! otherVariable.isAssigned())
             return true;
         else return isSatisfied2(variable);
     }
 
     public abstract boolean isSatisfied2(FurnitureVariable variable);
     
     /**
      * Returns a estimation of the impact (wheight) of the constraint. The more
      * restrictive, the higher the weight.
      * @return 
      */
     public abstract int getWeight();
     
     public final InterioresVariable getOtherVariable() {
         return otherVariable;
     }
     
     public void bound(WantedFurniture start) throws CloneNotSupportedException {
         if(otherVariable.isConstant())
             return;
         
         WantedFurniture end = (WantedFurniture) otherVariable;
         
        if (end == otherVariable)
            throw new BusinessException("Can't have a restriction with itself");
        
         if(end.isBounding())
             return;
         
         Debug.println("Bounding constraint...");
         
         BinaryConstraintEnd otherEnd = (BinaryConstraintEnd) clone();
         otherEnd.otherVariable = start;
         
         end.addBinaryConstraint(otherEnd);
     }
     
     public void unbound() {
         if(otherVariable.isConstant())
             return;
         
         Debug.println("Unbounding constraint...");
         
         WantedFurniture end = (WantedFurniture) otherVariable;
         end.removeBinaryConstraint(getClass());
     }
 }
