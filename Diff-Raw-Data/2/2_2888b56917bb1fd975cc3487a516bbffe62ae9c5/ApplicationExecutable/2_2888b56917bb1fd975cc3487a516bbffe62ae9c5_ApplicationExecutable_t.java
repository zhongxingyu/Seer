 package com.github.pellaton.estol.executable;
 
 /**
  * The {@link ApplicationExecutable} contains the main application logic. After loading the Spring application context,
 * a bean of this type is searched and it's {@code run())} method is invoked. 
  * 
  * @author Michael Pellaton
  */
 public interface ApplicationExecutable {
   
   /**
    * Method implementing the main application logic. Upon return of this method, the application is shut down.
    */
   void run();
 
 }
