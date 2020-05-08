 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package data;
 
 import exceptions.StorageException;
 
 /**
  *
  * @author delbertooo
  */
 public abstract class StorageData implements IStorageData, ICopyable {
 
     protected int id = 0;
     
     public final int getId() {
         return id;
     }
 
     public final void setId(int id) throws StorageException {
         if (this.id == 0) {
             this.id = id;
         } else {
             throw new StorageException("ID is already set.");
         }
     }
 }
