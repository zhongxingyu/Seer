 package playerstuff;
 
 /**
  *
  * @author James
  */
 public class Item {
 
     private String name;
     private int quantity = 0;
 
     public Item(String name, int quantity){
         this.name = name;
         this.quantity = quantity;
     }
 
     public String getName(){
         return name;
     }
 
     public int getQuantity(){
         return quantity;
     }
 
     public void addQuantity(int quantity){
        quantity += Math.abs(quantity);
     }
 }
