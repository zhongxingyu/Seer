 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.cart.model;
 
 import java.util.ArrayList;
 
 public class ProductsLoad {
     
     private ArrayList<Product> productslist;
     
     public ProductsLoad(){
         
         productslist = new ArrayList<Product>();
         loadProducts();
         
     }
     
     public void loadProducts() {
         Product p1 = new Product();
         Product p2 = new Product();
         
         p1.setProductID(1);
         p1.setProductName("HeadFirst Java");
         p1.setProductDescription("Beginner Book");
         p1.setProductPrice(20.00);
        productslist.add(p1);
         
         p2.setProductID(2);
         p2.setProductName("SCJP");
         p2.setProductDescription("Professionals Book");
         p2.setProductPrice(30.00);
        productslist.add(p2);
     }
 
     public ArrayList<Product> getProductslist() {
         return productslist;
     }
 
     
             
     
 }
