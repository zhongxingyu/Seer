 package com.page5of4.scaffold.domain;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.stereotype.Service;
 

 @Service
 public class ActiveRecordRepository implements Repository {
 
    @Override
    public Object findById(Class<?> entityClass, Object id) {
       return Finders.findAndInvokeFindById(entityClass, id.toString());
    }
 
    @Override
    public List<?> findAll(Class<?> entityClass, int page) {
       return new ArrayList<Object>(Finders.findAndInvokeFindAll(entityClass));
    }
 
    @Override
    public List<?> findAll(Class<?> entityClass) {
      return new ArrayList<Object>(Finders.findAndInvokeFindAll(entityClass));
    }
 
    @Override
    public long countAll(Class<?> entityClass) {
       return 0;
    }
 
    @Override
    public Object getIdOf(Object entity) {
       try {
          Method idGetter = Finders.findIdGetter(entity.getClass());
          return idGetter.invoke(entity);
       }
       catch(Exception error) {
          throw new RuntimeException("Error getting Id: " + entity, error);
       }
    }
 
 }
