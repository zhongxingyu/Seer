 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package useCase;
 
 import data.Product;
 import exceptions.StorageException;
 
 /**
  *
  * @author christianlinde
  */
 public class CaseProduct {
 
   public void addProduct(String name, float price, String description, String manufactorer) throws StorageException {
     data.Storage.getInstance().addProduct(new Product(name, price, description, manufactorer));
   }
 
   public String getName( int id ) {
    return data.Storage.getInstance().getProductById(id).getName();
     //return "";
   }
 }
