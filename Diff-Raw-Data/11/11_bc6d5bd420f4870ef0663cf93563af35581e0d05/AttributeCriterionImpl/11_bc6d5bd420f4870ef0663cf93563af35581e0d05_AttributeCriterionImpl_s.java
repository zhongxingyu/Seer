 package nl.amis.table.view.model;
 
 
 import commonj.sdo.Property;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import oracle.adf.view.rich.model.AttributeCriterion;
 import oracle.adf.view.rich.model.AttributeDescriptor;
 
 
 public class AttributeCriterionImpl extends AttributeCriterion {
   private final List values;
   private final AttributeDescriptor attributeDescriptor;
   private final Map<String,AttributeDescriptor.Operator> operators;
   private AttributeDescriptor.Operator operator = null;
   private RequiredType requiredType = RequiredType.OPTIONAL;
   private boolean matchCase = false;
   
   private final class ObservableArrayList extends ArrayList {
     private final ObservableBoolean changed;
     ObservableArrayList(final ObservableBoolean changed) {
       super(1);
       this.changed = changed;
       add("");
     }
     
     @Override
     public Object get(final int index) {
       System.out.println(attributeDescriptor.getName() + "-GET: " + index + ", value: " + super.get(index));
       return super.get(index);
     }
     
     @Override
    public Object set(final int index, final Object value) {
      final boolean modified = !value.equals(get(index));
      System.out.println(attributeDescriptor.getName() + "-SET: " + index + ", is: " + value + ", was:" + get(index) + ", changed: " + changed + " (" + modified + ")");
      final Object result = super.set(index, value);
      changed.setValue(changed.booleanValue() || modified);
      return result;
     }
   }
 
   public AttributeCriterionImpl(final Property property, final ObservableBoolean changed) {
     super();
     values = new ObservableArrayList(changed);
     attributeDescriptor = new AttributeDescriptorImpl(property);
     operators = new HashMap<String,AttributeDescriptor.Operator>(attributeDescriptor.getSupportedOperators().size());
     
     final Iterator<AttributeDescriptor.Operator> i = attributeDescriptor.getSupportedOperators().iterator();
     
     while(i.hasNext()) {
       final AttributeDescriptor.Operator o = i.next();
       operators.put(o.getLabel(), o);
     }
   }
 
   public AttributeDescriptor getAttribute() {
     return attributeDescriptor;
   }
 
   public Map<String, AttributeDescriptor.Operator> getOperators() {
     return operators;
   }
 
   public List<? extends Object> getValues() {
     System.out.println("getValues: " + values);
     return values;
   }
 
   public boolean isRemovable() {
     return false;
   }
 
   public void setOperator(final AttributeDescriptor.Operator operator) {
     System.out.println("setOperator: " + operator);
     this.operator = operator;
   }
   
   public AttributeDescriptor.Operator getOperator() {
     System.out.println("getOperator: " + operator);
     return operator;
   }
 
   public boolean hasDependentCriterion(final int index) {
     //System.out.println("hasDependentCriterion: " + index);
     return false;
   }
 
   public void setMatchCase(final boolean matchCase) {
     //System.out.println("setMatchCase: " + matchCase);
     this.matchCase = matchCase;
   }
 
   public boolean getMatchCase() {
     //System.out.println("getMatchCase: " + matchCase);
     return matchCase;
   }
 
   public void setRequiredType(final RequiredType requiredType) {
     //System.out.println("setRequiredType: " + requiredType);
     this.requiredType = requiredType;
   }
 
   public RequiredType getRequiredType() {
     //System.out.println("getRequiredType: " + requiredType);
     return requiredType;
   }
 }
