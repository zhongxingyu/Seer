 package com.kcalculator.ejb;
 
 import java.sql.DriverManager;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.kcalculator.domain.Language;
 
 /**
  * @author arweed
  * 
  */
 @Ignore
 public class LanguageQueryTest {
 
     private static EmbeddedGlassfish embeddedServer;
     private static EntityManager em;
 
     @BeforeClass
     public static void setupOnce() throws Throwable {
         DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
         Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
         embeddedServer = EmbeddedGlassfish.createServer();
         embeddedServer.initializeServer();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("kcalculatortest");
         em = emf.createEntityManager();
     }
 
     @AfterClass
     public static void closeOnce() throws Exception {
         embeddedServer.stopServer();
     }
 
     @Ignore
     @Test
     public void testInsert() throws Throwable {
         // given
         Language language = new Language();
         language.setIsoCode("DE");
 
         EntityTransaction utx = em.getTransaction();
         utx.begin();
         em.persist(language);
         utx.commit();
     }
 
 }
