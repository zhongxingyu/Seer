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
 
    public boolean isIsupdated() {
         return isupdated;
     }
 
    public void setIsupdated() {
        this.isupdated = true;
     }
 
    public boolean isIsadded() {
        return isadded;
     }
 
     public void setIsadded() {
         this.isadded = true;
     }
 
    public boolean isIsdeleted() {
        return isdeleted;
    }
 
     public void setIsdeleted() {
         this.isdeleted = true;
     }
 }
