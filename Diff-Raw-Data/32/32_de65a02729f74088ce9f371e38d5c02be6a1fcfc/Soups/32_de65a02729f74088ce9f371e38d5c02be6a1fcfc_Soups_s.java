 package controllers;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import org.codehaus.groovy.tools.shell.ParseCode;
 
 public class Soups implements Serializable{
     
     private int SoupID;
     private String SoupName;
     private double SoupPrice;
     private String SoupSize;
     private String SoupImage;
     private int SoupQuantity;
     private ArrayList IngredientList;
     
 
     public Soups(int soupID,String soupname,double price,String image) {
         this.SoupID = soupID;
         this.SoupName = soupname;
         this.SoupPrice = price;
         this.SoupSize = null;
         this.SoupImage = image;
         this.SoupQuantity = 1;
         IngredientList = new ArrayList();
     }
 
     public int getSoupID() {
         return SoupID;
     }
     
     public String getSoupName() {
         return SoupName;
     }
     
     public String getSoupSize() {
         return SoupSize;
     }
     
     public int getSoupQuantitiy() {
         return SoupQuantity;
     }
     
     public void setSoupQuantitiy(int quantity) {
         SoupQuantity = quantity;
     }
     
     public void setSoupSize(String s) {
         SoupSize = s;
     }
     
     public String getSoupImage() {
         return SoupImage;
     }
     
     public void setSoupImage(String s) {
     	SoupImage = s;
     }
     
     public double getBaseSoupPrice() {
         return SoupPrice;
     }
     
     public void addIngredient(Ingredient I) {
         IngredientList.add(I);
     }
     
     public ArrayList getIngredientList(){
         return IngredientList;
     }
     
     public boolean RemoveIngredient(String id){
         for(int count=0;count < IngredientList.size();count++) {
             Object obj = IngredientList.get(count);
             Ingredient objIng = (Ingredient)obj;
             if(objIng.getIngID() == Integer.parseInt(id)) {
                 IngredientList.remove(objIng);
                 return true;
             }
         }
         return false;
     }
 
     
     public double getTotalPrice() {
         double totalPrice = 0;
         for (int i=0; i<this.IngredientList.size(); i++) {
             Object obj = this.IngredientList.get(i);
             Ingredient p = (Ingredient)obj;
             totalPrice = totalPrice + p.getPrice();
         }
        return (totalPrice + this.SoupPrice)*this.SoupQuantity;
     }
 
 }
