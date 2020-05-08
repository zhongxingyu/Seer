 package fr.cg95.cvq.generator.common;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * @author rdj@zenexity.fr
  */
 public class Step {
 
     private int index;
     private String name;
     private String ref;
     private boolean required = true;
     
     private List<Condition> conditions = new ArrayList<Condition>();
     
     public Step (String index, String name, String ref, String requiredString) {
         try { 
             this.index = Integer.valueOf(index).intValue();
         } catch (NumberFormatException nfe) {
             throw new RuntimeException("Step() - Step index {"+ index +"} is not an integer");
         }
         this.name = name;
         this.ref = ref;
         if (requiredString != null)
             this.required = new Boolean(requiredString).booleanValue();
        if (this.ref != null && (this.ref.equals("validation") || this.ref.equals("document")))
            this.required = false;
     }
     
     public Step (int index, String name, String ref) {
         this.index = index;
         this.name = name;
         this.ref = ref;
     }
     
     public int getIndex() {
         return index;
     }
     
     public void setIndex(int index) {
         this.index = index;
     }
     
     public String getName() {
         return name;
     }
     
     public void setName(String name) {
         this.name = name;
     }
     
     public String getCamelCaseName() {
         return StringUtils.uncapitalize(name);
     }
     
     public String getRef() {
         return ref;
     }
     
     public void setRef(String ref) {
         this.ref = ref;
     }
     
     public boolean isRequired() {
         return required;
     }
 
     public void setRequired(boolean required) {
         this.required = required;
     }
 
     public List<Condition> getConditions() {
         return conditions;
     }
     
     public void addCondition(Condition condition) {
         for (Condition c : conditions)
             if (c.getName().equals(condition.getName()))
                 throw new RuntimeException("addCondition() - Condition {"+ condition.getName() +"} " 
                         + "is already associated with Step {"+ name +"}");
         
         conditions.add(condition);
     }
     
 }
