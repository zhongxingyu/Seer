 package au.edu.griffith.ict;
 
 /**
 * 
  * @author Brittany Creary
 *
  */
 public class MenuItem{
     /** The name of the menu item */
     private String name;
     /** The price per item */
     private float price;
     /** The items ID number */
     private int itemNo;
     
     /**
      * Creates a new menu item. You must add this item to the menu after creating a new instance.
      * @param itemNo The item number (Unique ID)
      * @param name The name of the item
      * @param price The price per unit for the item
      */
     public MenuItem(int itemNo, String name, float price){
         this.itemNo = itemNo;
         this.name = name;
         this.price = price;
     }
     
     /**
      * @return The price per item
      */
     public float getPrice(){
         return price;
     }
     
     /**
      * Sets the price per item
      * @param ppu The price per item
      */
     public void setPricePerUnit(float ppu){
         this.price = ppu;
     }
     
     /**
      * @return The name of the item
      */
     public String getName(){
         return name;
     }
     
     /**
      * @return The item's unique ID in the menu
      */
     public int getItemNo(){
         return itemNo;
     }
     
     @Override
     public String toString(){
        return itemNo + ": " + name + "...$" + price;
     }
 }
