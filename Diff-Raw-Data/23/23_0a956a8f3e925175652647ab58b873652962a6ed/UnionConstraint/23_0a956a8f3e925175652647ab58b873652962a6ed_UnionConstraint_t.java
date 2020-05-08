 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.ilrt.wf.facets.constraints;
 
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author pldms
  */
 public class UnionConstraint extends AbstractConstraint {
     private final List <RDFNode> values;
 
     public UnionConstraint(Property property, List <RDFNode> values) {
         this(property, values, false);
     }
 
     public UnionConstraint(Property property, List <RDFNode> values, boolean inverted) {
         super(property, inverted);
        if (values == null) values = new ArrayList();
         this.values = values;
     }
 
     public List <RDFNode> getValues() { return values; }
 
     @Override
     public boolean matches(RDFNode node) {
         return values.contains(node);
     }
 
     @Override
     public String toString() 
     { 
         StringBuilder sb = new StringBuilder();
        for (RDFNode node : values) sb.append(node).append(",");
         return "UnionConstraint: " + super.toString() + " values: [" + sb.toString() + "]";
     }
     
     @Override
     public int hashCode() {
         int i = 0;
         for (RDFNode node : values) i = i ^ node.hashCode();
         return 0xf8e87da ^ i ^ ~property.hashCode();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final UnionConstraint other = (UnionConstraint) obj;
         if (this.values != other.values && (this.values == null || !this.values.equals(other.values))) {
             return false;
         }
         return (this.inverted == other.inverted) && this.property.equals(other.property);
     }
 }
