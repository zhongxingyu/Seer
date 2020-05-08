 package nl.astraeus.persistence;
 
 import java.io.Serializable;
 
 /**
  * SimpleReference
  * <p/>
  * User: rnentjes
  * Date: 7/27/11
  * Time: 1:02 PM
  */
 public class PersistentReference<K, M extends Persistent<K>> implements Serializable {
     public static final long serialVersionUID = 1L;
 
     private transient M incoming;
     private K id;
     private Class<M> cls;
 
     public PersistentReference(Class<M> cls) {
         id = null;
         this.cls = cls;
     }
 
     public PersistentReference(M model) {
         if (model != null) {
             set(model);
         }
     }
 
     public PersistentReference(Class<M> cls, K id) {
         this.cls = cls;
         this.id = id;
     }
 
     public M getIncoming() {
         return incoming;
     }
 
     public void clearIncoming() {
         incoming = null;
     }
 
     public Class<M> getType() {
         return cls;
     }
 
     public K getId() {
         return id;
     }
 
     public M get() {
         M result = incoming;
 
         if (result == null && cls != null && id != null) {
            result = (M) PersistentManager.get().find(cls, id);
         }
 
         return result;
     }
 
     public void set(M model) {
         this.incoming = model;
 
         if (model != null) {
             this.cls = (Class<M>) model.getClass();
             this.id = model.getId();
         } else {
             this.cls = null;
             this.id = null;
         }
     }
 
     public boolean isNull() {
         return (cls == null || id == null);
     }
     
     public String toString() {
         return cls.getName()+":"+id;
     }

 }
