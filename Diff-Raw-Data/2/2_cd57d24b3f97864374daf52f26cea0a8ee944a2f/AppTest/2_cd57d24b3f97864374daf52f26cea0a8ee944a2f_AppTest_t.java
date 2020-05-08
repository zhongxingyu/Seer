 package com.prodyna.bow.dummy;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * Unit test for simple App.
  */
 public class AppTest 
     extends TestCase
 {
     /**
      * Create the test case
      *
      * @param testName name of the test case
      */
     public AppTest( String testName )
     {
         super( testName );
     }
 
     /**
      * @return the suite of tests being tested
      */
     public static Test suite()
     {
         return new TestSuite( AppTest.class );
     }
 
     /**
      * Rigourous Test :-)
      */
     public void testApp()
     {	
     	System.out.println("test the app");
         assertTrue( true );
     }
     
     public void testApp_process(){
     	App app = new App();
     	app.process();
     	assertTrue(true);
     }
     
     public void testApp_process2(){
     	App app = new App();
     	app.process();
    	assertTrue(true);
     }
 }
