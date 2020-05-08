 
 package com.cart.model;
 
 import java.util.ArrayList;
 
 
 public class Catalog {
     
     private static Catalog instance = null;
     
     //list of catalog products
     private ArrayList<Product> products;
     
     private Catalog(){
       
        products = new ProductsLoad().getProductslist();
         
     }
     
     //returns instance of catalog
     public static Catalog getInstance(){
         
         if(instance == null){
             instance = new Catalog();
         }
         
         return instance;
     }
     
     //adds products to the catalog
     public void addProduct(Product product){
         
         products.add(product);
     }
     
     //returns products form the catalog
     public ArrayList getCatalog(){
         return this.products;
     }
     
     
 }
