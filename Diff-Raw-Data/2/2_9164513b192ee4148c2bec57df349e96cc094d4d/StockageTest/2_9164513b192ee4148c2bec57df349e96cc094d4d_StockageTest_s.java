 package com.ecnmelog.model;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * Tests unitaires pour la classe Stockage
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
      * Test de vérification de la méthode de stockage des containers
      */
     public void testStoreContainer()
     {
       
       Stockage stock = new Stockage(100);
       Attente att = new Attente();
       att.empty();
       stock.empty();
       try{
         att.addContainer(new Container(1, 0));
         att.addContainer(new Container(2, 1));
         att.addContainer(new Container(3, 1));
         att.addContainer(new Container(4, 1));
         att.addContainer(new Container(5, 1));
       }catch(ContainerException e){
         System.out.println("--store--"+e.getMessage());
       }
       try{
         stock.storeContainer(1, 1);
         stock.storeContainer(2, 31);
       }
       catch(ContainerException e)
       {
         System.out.println("--store--"+e.getMessage());
       }
       catch(EmplacementException e)
       {
         System.out.println("--store--"+e.getMessage());
       }
       assertEquals(3, att.countContainers());
       assertEquals(2, stock.countContainers());
       assertEquals(1, stock.countContainers(1));
       assertEquals(0, stock.countContainers(2));
     }
     
     /**
      * Test de vérification de la méthode de vidage des containers
      */
     public void testEmpty()
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
         System.out.println("--empty--"+e.getMessage());
       }
       try{
         stock.storeContainer(1, 1);
         stock.storeContainer(2, 31);
       }
       catch(ContainerException e)
       {
         System.out.println("--empty--"+e.getMessage());
       }
       catch(EmplacementException e)
       {
         System.out.println("--empty--"+e.getMessage());
       }
       
       stock.empty();
       assertEquals(0, stock.countContainers());
     }
     
     public void testGetEmplacementLibre() {
         
       Stockage stock = new Stockage(100);
       Attente att = new Attente();
       att.empty();
       stock.empty();
       
       try{
         for(int i=0; i<=1; i++) {
             att.addContainer(new Container(i, 0));
         }
         for(int i=2; i<=7; i++){
             att.addContainer(new Container(i, 1));
         }
         for(int i=8; i<=9; i++){
             att.addContainer(new Container(i, 2));
         }
       }catch(ContainerException e){
         System.out.println(e.getMessage());
       }
 
         //On teste si les emplacements sont bien vides
         try {
             assertEquals(1, stock.getEmplacementLibre(0));
             assertEquals(31, stock.getEmplacementLibre(1));
             assertEquals(36, stock.getEmplacementLibre(2));
         } catch (EmplacementException e) {
             fail();
         } catch (ContainerException e) {
             fail();
         }
         
         try {
             for(int i=0; i<=1; i++) {
                 stock.storeContainer(i, stock.getEmplacementLibre(0));
             }
             for(int i=2; i<=7; i++) {
                 stock.storeContainer(i, stock.getEmplacementLibre(1));
             }
             for(int i=8; i<=9; i++) {
                 stock.storeContainer(i, stock.getEmplacementLibre(2));
             }
         } catch (ContainerException e) {
             fail();
         } catch (EmplacementException e) {
             fail();
         }
         
         try {
             // Les 2 premiers emplacements ont été pris par des
             // containers normaux, le 3e par le 6e container frigo
             // qui est donc mis dans un emplacement normal
             assertEquals(4, stock.getEmplacementLibre(0));
             // Les containers frigo sont désormais stockés dans les
             // emplacements normaux
             assertEquals(4, stock.getEmplacementLibre(1));
             // Les containers surtarifés continuent à se stocker
             // normalement
             assertEquals(38, stock.getEmplacementLibre(2));
         } catch (EmplacementException e) {
             fail();
         } catch (ContainerException e) {
             fail();
         }
     }
     
     public void testTraiterAttente() {
         Stockage stock = new Stockage(100);
         Attente att = new Attente();
         att.empty();
         stock.empty();
         
         // On ajoute 2 containers normaux
         // On ajoute 6 containers frigo
         // On ajoute 2 containers surtarifés
         try {
             for(int i=0; i<=1; i++)
                 att.addContainer(new Container(i, 0));
             for(int i=2; i<=7; i++)
                 att.addContainer(new Container(i, 1));
             for(int i=8; i<=9; i++)
                 att.addContainer(new Container(i, 2));
         } catch(ContainerException e) {
             System.out.println(e.getMessage());
         }
         
         // On stocke tout
         stock.traiterAttente();
         
         // On vérifie que tous les containers ont été stockés
         assertEquals(2, stock.countContainers(0));
         assertEquals(6, stock.countContainers(1));
         assertEquals(2, stock.countContainers(2));
         
         // On vérfie qu'ils l'ont été aux bons emplacements
         // 2 containers normaux + 1 frigo dans les emplacements normaux
         // 30 - 3 = 27
         assertEquals(27, stock.countEmplacementsDispo(0));
         // 5 - 5 = 0
         assertEquals(0, stock.countEmplacementsDispo(1));
         // 65 - 2 = 63
         assertEquals(63, stock.countEmplacementsDispo(2));
     }
     
     public void testTraiterAttenteOverFlow() {
         // Cas où tous les containers ne peuvent être chargés
         Stockage stock = new Stockage(100);
         Attente att = new Attente();
         att.empty();
         stock.empty();
         
         // On ajoute 35 containers normaux
         // On ajoute 6 containers frigo
         // On ajoute 67 containers surtarifés
         try {
             for(int i=0; i<=34; i++)
                 att.addContainer(new Container(i, 0));
             for(int i=0; i<=5; i++)
                 att.addContainer(new Container(i + 35, 1));
             for(int i=0; i<=66; i++)
                 att.addContainer(new Container(i + 41, 2));
         } catch(ContainerException e) {
             System.out.println(e.getMessage());
         }
         
         stock.traiterAttente();
         
         // On vérifie que tous les containers qui pouvaient l'être ont été stockés
         assertEquals(27, stock.countContainers(0));
         assertEquals(6, stock.countContainers(1));
         assertEquals(67, stock.countContainers(2));
         
        // On vérfie qu'il reste bien 5 containers normaux en attente
         assertEquals(8, att.countContainers(0));
         assertEquals(0, att.countContainers(1));
         assertEquals(0, att.countContainers(2));
     }
 }
