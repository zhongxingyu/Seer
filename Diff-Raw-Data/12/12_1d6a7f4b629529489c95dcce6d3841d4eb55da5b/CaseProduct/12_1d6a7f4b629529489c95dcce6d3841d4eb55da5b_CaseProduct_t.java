 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package useCase;
 
 import data.Product;
import data.Storage;
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
    return "TEST name";
    String mStor = data.Storage.getInstance().getProductById(0).getName();
    
    if ( data.Storage.getInstance().getProductById(id).getName() != null ) {
      return data.Storage.getInstance().getProductById(id).getName();
    } else {
      return "";
    }
    //return data.Storage.getInstance().getProductById(id).getName();
     //return "";
   }
 }
