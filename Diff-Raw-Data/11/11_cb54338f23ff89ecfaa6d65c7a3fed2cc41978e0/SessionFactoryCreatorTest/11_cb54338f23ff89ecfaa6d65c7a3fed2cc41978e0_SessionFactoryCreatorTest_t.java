 package br.ime.usp.commendans.components;
 
 import org.junit.Test;
 
 public class SessionFactoryCreatorTest {
 
     @Test
     public void shouldGetSessionFactory() {
        SessionFactoryCreator sfc = new SessionFactoryCreator();
         sfc.create();
     }
 
 }
