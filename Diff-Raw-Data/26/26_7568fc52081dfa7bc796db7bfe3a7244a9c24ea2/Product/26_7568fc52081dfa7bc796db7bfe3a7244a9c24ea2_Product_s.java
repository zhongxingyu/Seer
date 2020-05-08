package com.flashshop;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Evgen
  * Date: 18.10.12
  * Time: 0:16
  * To change this template use File | Settings | File Templates.
  */
 public class Product {
 
     public String categoryName;
     public String manufacturer;
     public String productName;
     public String productSku;
     public boolean productAvailability;
     public String productPrice;
     public String shortDescription;
     public String description;
     public String warranty;
 
     @Override
     public String toString() {
         return "Product{" +
                 "categoryName='" + categoryName + '\'' +
                 ", manufacturer='" + manufacturer + '\'' +
                 ", productName='" + productName + '\'' +
                 ", productSku='" + productSku + '\'' +
                 ", productPrice='" + productPrice + '\'' +
                 ", shortDescription='" + shortDescription + '\'' +
                 ", productAvailability='" + productAvailability + '\'' +
                 '}';
     }
 
 
 }
