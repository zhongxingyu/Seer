 package interiores.business.models.constraints.furniture.unary;
 
 import interiores.business.models.backtracking.FurnitureVariable;
 import interiores.business.models.constraints.furniture.UnaryConstraint;
 import interiores.business.models.room.FurnitureModel;
 import interiores.core.business.BusinessException;
 import interiores.utils.CoolColor;
 import java.util.HashSet;
 import java.util.Iterator;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * ColorConstraint represents a constraint imposed over the color of a piece of furniture
  * @author larribas
  */
 @XmlRootElement
 public class ColorConstraint
     extends UnaryConstraint {
     
     /** 
      * 'color' represents the exact color a piece of furniture should be, in
      * order to satisfy the constraint.
      */
     @XmlAttribute
     private CoolColor color;
     
     public ColorConstraint() {
         
     }
         
     /**
      * Creates a color constraint such that only those pieces of furniture matching "color" will satisfy it
      * @param color The color that will define the constraint
      */
     public ColorConstraint(CoolColor color) {
         this.color = color;
     }
     
     public ColorConstraint(String color)
             throws BusinessException
     {
         this(CoolColor.getEnum(color));
     }
     
     /**
      * Eliminates models which do not satisfy the constraint.
      * @param variable The variable whose values have to be checked.
      */
     @Override
     public void preliminarTrim(FurnitureVariable variable) {
         HashSet<FurnitureModel> validModels = variable.getDomain().getModels(0);
         Iterator<FurnitureModel> it = validModels.iterator();
         while (it.hasNext()) {
            if (! it.next().getColor().equals(color.getColor()))
                 it.remove();
         }
         
         variable.eliminateExceptM(validModels);
     }
     
     
     @Override
     public String toString() {
         return "Color: " + color.name();
         //return this.getClass().getName() + " Color: " + color;
     }
 }
