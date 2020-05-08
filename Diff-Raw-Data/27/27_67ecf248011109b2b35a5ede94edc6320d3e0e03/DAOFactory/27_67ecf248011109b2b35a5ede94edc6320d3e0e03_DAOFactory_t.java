 package com.george.jpa.dao;
 
 import java.lang.reflect.ParameterizedType;
 
 import javax.enterprise.inject.Produces;
 import javax.enterprise.inject.spi.InjectionPoint;
 import javax.persistence.EntityManager;
 
 import com.george.cdi.CustomerDatabase;
 
 public class DAOFactory {
     @Produces
     @SuppressWarnings({ "rawtypes", "unchecked" })
    DAO create(InjectionPoint injectionPoint, @CustomerDatabase EntityManager manager) {
         ParameterizedType type = (ParameterizedType) injectionPoint.getType();
         Class<?> classe = (Class<?>) type.getActualTypeArguments()[0];
         return new DAO(classe, manager);
     }
 
 }
