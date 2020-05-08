 /**
  * The contents of this file are subject to the AED Public Use License Agreement, Version 1.0 (the "License");
  * use in any manner is strictly prohibited except in compliance with the terms of the License.
  * The License is available at http://gatherdata.org/license.
  *
  * Copyright (c) AED.  All Rights Reserved
  */
 package org.gatherdata.data.dao.jpa.internal;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.CoreMatchers.notNullValue;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Random;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 import javax.persistence.spi.PersistenceProvider;
 
 import org.gatherdata.data.core.model.FlatForm;
 import org.gatherdata.data.core.spi.BaseFlatFormDaoTest;
 import org.gatherdata.data.core.spi.FlatFormDao;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 public class JpaFlatFormDaoTest extends BaseFlatFormDaoTest {
 
     int mockPlainTextCount = 0;
     
     @Inject
     private PersistenceProvider persistenceProvider;
 
     private EntityTransaction tx;
 
     @Override
     protected FlatFormDao createStorageDaoImpl() {
         JpaFlatFormDao dao = new JpaFlatFormDao();
         dao.persistenceUnitName = "flatform-test";
         
         // guice up the instance
         Injector injector = Guice.createInjector(new JpaTestingModule());
         injector.injectMembers(dao);
         injector.injectMembers(this);
         
         return dao;
     }
 
 
     @Override
     protected void beginTransaction() {
         EntityManager em = ((JpaFlatFormDao) dao).getEntityManager();
         tx = em.getTransaction();
         tx.begin();
     }
 
     @Override
     protected void endTransaction() {
         tx.commit();
     }
     
     @Override
     protected void rollbackTransaction() {
     	tx.rollback();
     }
     
     /**
      * Must have at least one test here to convince Eclipse that
      * there are tests to run.
      */
     @Test
     public void shouldRunParentTestsInEclipse() {
         assertTrue(true);
     }
     
     /**
      * Sanity-check for the test. 
      */
     @Test
     public void shouldHavePersistenceProviderInjected() {
         assertThat(persistenceProvider, notNullValue());
     }
    
 }
