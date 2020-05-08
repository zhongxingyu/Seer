 package com.george.jpa.dao;
 
 import java.lang.reflect.ParameterizedType;
 
 import javax.enterprise.inject.Produces;
 import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
 import javax.persistence.EntityManager;
 
 import com.george.cdi.CustomerDatabase;
 
 public class DAOFactory {
    @Inject
    @CustomerDatabase
    private EntityManager manager;

     @Produces
     @SuppressWarnings({ "rawtypes", "unchecked" })
    public DAO create(InjectionPoint injectionPoint) {
         ParameterizedType type = (ParameterizedType) injectionPoint.getType();
         Class<?> classe = (Class<?>) type.getActualTypeArguments()[0];
         return new DAO(classe, manager);
     }
 
 }
