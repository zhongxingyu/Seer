 package org.purl.wf4ever.rosrs.client;
 
 import java.net.URI;
 
 /**
  * A few utilities for the tests.
  * 
  * @author piotrekhol
  * 
  */
 public final class TestUtils {
 
     /** Some ROSRS available by HTTP. */
     public static final ROSRService ROSRS = new ROSRService(URI.create("http://sandbox.wf4ever-project.org/rodl/ROs/"),
            "b1a8ad77-790c-4bf1-a452-f1c99a662732");
 
 
     //public static final ROSRService ROSRS = new ROSRService(URI.create("http://localhost:8082/ROs/"),
     //        "0522a6c6-7000-43df-8");
 
     /**
      * Constructor.
      */
     private TestUtils() {
         //nope
     }
 }
