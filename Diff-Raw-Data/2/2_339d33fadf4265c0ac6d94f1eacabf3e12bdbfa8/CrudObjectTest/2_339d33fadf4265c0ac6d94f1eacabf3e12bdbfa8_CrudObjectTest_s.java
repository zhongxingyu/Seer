 package com.elgoooog.podb;
 
 import com.elgoooog.podb.test.AnotherPlanet;
 import com.elgoooog.podb.test.Planet;
 import org.junit.Test;
 
 /**
  * @author Nicholas Hauschild
  *         Date: 5/4/11
  *         Time: 10:45 PM
  */
 public class CrudObjectTest {
     @Test
     public void testCreate_defaultNames() throws Exception {
         Planet planet = new Planet("Earth", 100, 6000);
         planet.create();
     }
 
     @Test
     public void testCreate_specifiedNames() throws Exception {
         AnotherPlanet planet = new AnotherPlanet("Mars", 50, 1);
         planet.create();
     }
 
     @Test
     public void testUpdate() throws Exception {
         Planet planet = new Planet("Earth", 100, 5000);
         planet.update();
     }
 
     @Test
     public void testDelete() throws Exception {
         Planet planet = new Planet("Earth", 100, 1000);
         planet.delete();
     }
 }
