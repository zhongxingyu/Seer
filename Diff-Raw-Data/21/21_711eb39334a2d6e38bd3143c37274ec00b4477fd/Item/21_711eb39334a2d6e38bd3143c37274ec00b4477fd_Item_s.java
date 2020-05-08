 
 package squeak.model;
 
 public final class Item {
     private int id;
    private String name;
     private String classification;
     private int quantity;
     private String type;
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getClassification() {
         return classification;
     }
 
     public void setClassification(String classification) {
         this.classification = classification;
     }
 
     public int getQuantity() {
         return quantity;
     }
 
     public void setQuantity(int quantity) {
         this.quantity = quantity;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 }
