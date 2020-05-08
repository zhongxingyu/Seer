 package com.ecnmelog.model;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * Unit test for simple App.
  */
 public class StockageTest 
     extends TestCase
 {
     /**
      * Create the test case
      *
      * @param testName name of the test case
      */
     public StockageTest( String testName )
     {
         super( testName );
     }
 
     /**
      * @return the suite of tests being tested
      */
     public static Test suite()
     {
         return new TestSuite( StockageTest.class );
     }
 
     /**
      * Test de vérification d'initialisation des emplacements
      */
     public void testInitEmplacement()
     {
         Stockage stock = new Stockage(100);
         Attente att = new Attente();
         assertEquals(100, stock.countEmplacementsDispo());
         assertEquals(5, stock.countEmplacementsDispo(1));
         assertEquals(65, stock.countEmplacementsDispo(2));
         assertEquals(30, stock.countEmplacementsDispo(0));
     }
     
     /**
      * Test de vérification du chargement d'un container dans la zone d'attente
      */
     public void testAddContainer()
     {
         Stockage stock = new Stockage(100);
         Attente att = new Attente();
         try{
             att.addContainer(new Container(1, 2));
             att.addContainer(new Container(2, 1));
         }catch(ContainerException e){
             System.out.println(e.getMessage());
         }
         att.removeContainerByType(2);
         assertEquals(att.countContainers(), 1);
         assertEquals(att.countContainers(0), 0);
         assertEquals(att.countContainers(1), 1);
         assertEquals(att.countContainers(2), 0);
     }
     
     public void testStoreContainer()
     {
       
       Stockage stock = new Stockage(100);
       Attente att = new Attente();
       try{
         att.addContainer(new Container(1, 0));
         att.addContainer(new Container(2, 1));
         att.addContainer(new Container(3, 1));
         att.addContainer(new Container(4, 1));
         att.addContainer(new Container(5, 1));
       }catch(ContainerException e){
         System.out.println(e.getMessage());
       }
       try{
         stock.storeContainer(1, 1);
         stock.storeContainer(2, 31);
       }
       catch(ContainerException e)
       {
         System.out.println(e.getMessage());
       }
       catch(EmplacementException e)
       {
         System.out.println(e.getMessage());
       }
       assertEquals(att.countContainers(), 3);
       assertEquals(stock.countContainers(), 2);
       assertEquals(stock.countContainers(1), 1);
       assertEquals(stock.countContainers(2), 0);
     }
 }
