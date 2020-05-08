 package com.wishlistery.view;
 
 import java.io.Serializable;
 
 import javax.faces.context.ExternalContext;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 
 import com.wishlistery.domain.BaseEntity;
 
 public abstract class BaseBean<T extends BaseEntity> implements Serializable {
     private static final long serialVersionUID = -3344572481329361875L;
 
     private long id = 0;
     
     protected T entity = newEntityInstance();
     
     @Inject
     protected EntityManager em;
 
     @Inject
     ExternalContext externalContext;
     
     private Class<T> entityType;
 
     @SuppressWarnings("unchecked")
     public BaseBean() {
         entityType = (Class<T>) newEntityInstance().getClass();
     }
     
     public String load() {
         try {
             if (!entity.isPersisted()) { //already loaded
                 entity = em.find(entityType, id);
             }
         } catch (NoResultException e) {
             entity = newEntityInstance();
             return encodeUrl(entityType.getSimpleName().toLowerCase() + "?faces-redirect=true");
         }
         return null;
     }
 
     public String save() {
         if (id == 0) {
             em.persist(entity);
             return encodeUrl(entityType.getSimpleName().toLowerCase() + "?faces-redirect=true&id=" + entity.getId());
         } else {
             entity = em.merge(entity);
             return null;
         }
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
         if (id > 0) {
             load();
         }
     }
 
     public boolean isPersisted() {
         return id > 0;
     }
 
     public T getEntity() {
         return entity;
     }
 
     public void setEntity(T entity) {
         this.entity = entity;
     }
  
     private String encodeUrl(String view) {
         return externalContext.encodeResourceURL(view);
     }
     
     protected abstract T newEntityInstance();
 }
