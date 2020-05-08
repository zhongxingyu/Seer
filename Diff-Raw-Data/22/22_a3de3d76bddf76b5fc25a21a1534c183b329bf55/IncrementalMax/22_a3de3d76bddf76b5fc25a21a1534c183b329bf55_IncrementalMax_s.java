 /* $Id */
 package niagara.logical;
 
 import niagara.optimizer.colombia.Attribute;
 import niagara.optimizer.colombia.Op;
 import niagara.xmlql_parser.op_tree.unryOp;
 import niagara.xmlql_parser.syntax_tree.*;
 
 public class IncrementalMax extends IncrementalGroup {
     private Attribute maxAttribute;
     private Double emptyGroupValue;
 
     public IncrementalMax() {}
     
     public IncrementalMax(skolem skolemAttributes, Attribute maxAttribute) {
         super(skolemAttributes);
         this.maxAttribute = maxAttribute;
     }
 
     public void setMaxAttribute(Attribute maxAttribute) {
         this.maxAttribute = maxAttribute;
     }
 
     public void setEmptyGroupValue(Double emptyGroupValue) {
 	this.emptyGroupValue = emptyGroupValue;
     }
 
     public Double getEmptyGroupValue() {
 	return emptyGroupValue;
     }
 
     public Attribute getMaxAttribute() {
         return maxAttribute;
     }
 
     public void dump() {System.out.println(getName());}
 
     public Op copy() {
        return new IncrementalMax(skolemAttributes, maxAttribute);
     }
     
     public boolean equals(Object o) {
         if (o == null || !(o instanceof IncrementalMax))
             return false;
         if (o.getClass() != IncrementalMax.class)
             return o.equals(this);
         IncrementalMax ia = (IncrementalMax) o;
         return skolemAttributes.equals(ia.skolemAttributes)
             && maxAttribute.equals(ia.maxAttribute);
     }
     
     public int hashCode() {
         return skolemAttributes.hashCode() ^ maxAttribute.hashCode();
     }
 }
 
