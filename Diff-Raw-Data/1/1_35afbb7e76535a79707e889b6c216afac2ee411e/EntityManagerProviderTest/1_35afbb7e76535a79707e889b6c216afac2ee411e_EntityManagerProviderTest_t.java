 package com.magenta.guice.jpa;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import org.testng.annotations.Test;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.fail;
 
 /*
 * Project: Maxifier
 * Author: Aleksey Didik
 * Created: 23.05.2008 10:19:35
 * 
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 */
 @Test
 public class EntityManagerProviderTest {
 
     @Test
     public void testInjection() throws Exception {
 

         EntityManager mockEM = mock(EntityManager.class);
         final EntityManagerFactory mockEMF = mock(EntityManagerFactory.class);
         when(mockEMF.createEntityManager()).thenReturn(mockEM);
         EntityTransaction entityTransaction = mock(EntityTransaction.class);
         when(mockEM.getTransaction()).thenReturn(entityTransaction);
 
         Injector inj = Guice.createInjector(new AbstractModule() {
             @Override
             protected void configure() {
                 bind(EntityManagerFactory.class).toInstance(mockEMF);
             }
         }, new JPAModule());
         Foo foo = inj.getInstance(Foo.class);
         foo.dbWork();
         verify(mockEMF).createEntityManager();
         verify(mockEM).createQuery("in db");
         try {
             foo.notDbWork();
             fail("Method must be failed, entityManager used but not in annotated wi @DB method");
         } catch (IllegalStateException ignored) {
         }
         verify(mockEM, never()).createQuery("not in db");
     }
 
 
     static class Foo {
 
         @Inject
         private EntityManager entityManager;
 
         @DB
         public void dbWork() {
             entityManager.createQuery("in db");
         }
 
         public void notDbWork() {
             entityManager.createQuery("not in db");
         }
 
     }
 }
