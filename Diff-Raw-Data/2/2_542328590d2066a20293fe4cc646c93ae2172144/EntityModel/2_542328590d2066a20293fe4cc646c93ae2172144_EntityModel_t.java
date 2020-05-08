 package com.svanberg.household.web.components;
 
 import com.svanberg.household.domain.DomainObject;
 import com.svanberg.household.service.DomainObjectService;
 
 import org.apache.wicket.injection.Injector;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import java.io.Serializable;
 
 /**
  * @author Andreas Svanberg (andreass) <andreas.svanberg@mensa.se>
  */
 public class EntityModel<T extends DomainObject> implements IModel<T> {
     private static final long serialVersionUID = -6617614098524238401L;
 
     private transient @SpringBean DomainObjectService domainService;
 
     private final Class<T> clazz;
     private Serializable identifier;
     private T entity;
     private transient boolean attached = false;
 
     public EntityModel(Class<T> clazz) {
         this.clazz = clazz;
     }
 
     @SuppressWarnings("unchecked")
     public EntityModel(T entity) {
         this.clazz = (Class<T>) entity.getClass();
 
         setObject(entity);
     }
 
     @Override
     public T getObject() {
         if (!attached) {
             load();
         }
 
         return entity;
     }
 
     @Override
     public void setObject(final T object) {
        identifier = object != null ? object.getIdentifier() : null;
         entity = object;
     }
 
     @Override
     public void detach() {
         domainService = null;
         attached = false;
         if (identifier != null) {
             entity = null;
         }
     }
 
     private void load() {
         if (!attached) {
             attached = true;
 
             if (identifier != null) {
                 Injector.get().inject(this);
                 entity = domainService.find(clazz, identifier);
             }
         }
     }
 }
