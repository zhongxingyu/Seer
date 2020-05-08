 package edacc.model;
 
 public class Parameter extends BaseModel {
     private int id;
     private String name;
     private String prefix;
     private String value;
     private int order;
 
     public Parameter() {
         name = prefix = value = "";
     }
 
     public int getId() {
         return id;
     }
 
     protected void setId(int id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
         if (this.isSaved()) {
             this.setModified();
         }
     }
 
     public int getOrder() {
         return order;
     }
 
     public void setOrder(int order) {
         this.order = order;
         if (this.isSaved()) {
             this.setModified();
         }
     }
 
     public String getPrefix() {
         return prefix;
     }
 
     public void setPrefix(String prefix) {
         this.prefix = prefix;
         if (this.isSaved()) {
             this.setModified();
         }
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
         if (this.isSaved()) {
             this.setModified();
         }
     }
 }
