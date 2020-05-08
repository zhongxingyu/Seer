 package com.tehbeard.utils.testSuite.syringe;
 
import me.tehbeard.utils.syringe.Inject;
 
 public class Patient {
 
     @Inject
     String name = "john";
 
     @Override
     public String toString() {
         return this.name;
     }
 }
