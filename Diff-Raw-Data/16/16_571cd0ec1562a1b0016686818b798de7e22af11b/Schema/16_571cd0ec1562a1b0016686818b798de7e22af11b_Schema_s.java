 package org.neurus.data;
 
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.TreeSet;
 
 import org.neurus.Copyable;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 public class Schema implements Copyable<Schema>{
 
   private Attribute[] attributes;
 
   private Schema(Attribute[] attributes) {
     this.attributes = attributes;
   }
 
   public int getAttributeCount() {
     return attributes.length;
   }
 
   public Attribute getAttribute(int index) {
     return attributes[index];
   }
 
   public int indexOfAttribute(String attributeName) {
    for(int x = 0; x < attributeName.length(); x++) {
       if(attributeName.equals(attributes[x].getName())) {
         return x;
       }
     }
     throw new NoSuchElementException("Attribute not found for name: " + attributeName);
   }
 
   public Instance newInstance() {
     return new Instance(this, new double[attributes.length]);
   }
 
   public Instance newInstance(Instance instance) {
     return new Instance(this, instance);
   }
 
   public static class Builder {
 
     private List<Attribute> attributes = Lists.newArrayList();
     private TreeSet<String> attributeNames = Sets.newTreeSet();
 
     public Builder addNominalAttribute(String name, String[] labels) {
       NominalAttribute nominalAttribute = new NominalAttribute(name, labels);
       addAttribute(nominalAttribute);
       return this;
     }
 
     public Builder addNumericAttribute(String name) {
       NumericAttribute numericAttribute = new NumericAttribute(name);
       addAttribute(numericAttribute);
       return this;
     }
 
     public Builder addTextAttribute(String name) {
       TextAttribute textAttribute = new TextAttribute(name);
       addAttribute(textAttribute);
       return this;
     }
 
     private void addAttribute(Attribute attribute) {
       if (!attributeNames.add(attribute.getName())) {
         throw new IllegalArgumentException("Duplicated attribute: "
             + attribute.getName());
       }
       attributes.add(attribute);
     }
 
     public Schema build() {
       Preconditions.checkState(!attributes.isEmpty(), "You need to add at least one attribute");
       return new Schema(attributes.toArray(new Attribute[attributes.size()]));
     }
   }
 
   @Override
   public Schema copy() {
     Attribute[] myAttributes = new Attribute[attributes.length];
     for(int x = 0; x < attributes.length; x++) {
       myAttributes[x] = attributes[x].copy();
     }
     return new Schema(myAttributes);
   }
 }
