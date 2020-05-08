 /***
  * Excerpted from "Programming Concurrency on the JVM",
  * published by The Pragmatic Bookshelf.
  * Copyrights apply to this code. It may not be used to create training material, 
  * courses, books, articles, and the like. Contact us if you are in doubt.
  * We make no guarantees that this code is fit for any purpose. 
  * Visit http://www.pragmaticprogrammer.com/titles/vspcon for more book information.
 ***/
package com.agiledeveloper.pcj;

import java.util.List;
 import java.util.ArrayList;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Future;
 import java.util.concurrent.ExecutionException;
 
 public class UseEnergySource {
   private static final EnergySource energySource = EnergySource.create();
     
   public static void main(final String[] args) 
     throws InterruptedException, ExecutionException {
     System.out.println("Energy level at start: " + 
       energySource.getUnitsAvailable());
         
     List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();    
 
     for(int i = 0; i < 10; i++) {
       tasks.add(new Callable<Object>() {
         public Object call() {
           for(int j = 0; j < 7; j++) energySource.useEnergy(1);
           return null;
         }
       });
     }
     
     final ExecutorService service = Executors.newFixedThreadPool(10);
     service.invokeAll(tasks);
     
     System.out.println("Energy level at end: " + 
       energySource.getUnitsAvailable());
     System.out.println("Usage: " + energySource.getUsageCount());
     
     energySource.stopEnergySource();
     service.shutdown();
   }
 }
