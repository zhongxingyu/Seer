 package org.daum.library.replica.listener;
 
 import org.daum.library.replica.cache.StoreCommand;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jed
  * Date: 20/06/12
  * Time: 12:21
  * To change this template use File | Settings | File Templates.
  */
 public class PropertyChangeEvent {
 
     private  boolean isupdated=false;
     private boolean isadded=false;
     private boolean isdeleted=false;
 
     private Object id;
     public Object getId() {
         return id;
     }
     public void setId(Object id) {
         this.id = id;
     }
 
    public boolean isAdded() {
        return isadded;
    }
    public boolean isUpdated() {
         return isupdated;
     }
 
    public boolean isDeleted() {
        return isdeleted;
     }
 

    public void setIsupdated()
    {
        this.isupdated = true;
     }
 
     public void setIsadded() {
         this.isadded = true;
     }
 
 
     public void setIsdeleted() {
         this.isdeleted = true;
     }
 }
