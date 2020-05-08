 package guibin.zhang.java;
 
 /**
  *
  * Using double check locking to create thread safe singleton.
  * 
  * @author Gubin Zhang <guibin.beijing@gmail.com>
  */
 public class ThreadSafeSingleton {
     
     private static volatile ThreadSafeSingleton instance;//vilatile variable
     
     public static ThreadSafeSingleton getInstance() {
         if (instance == null) {
             synchronized(ThreadSafeSingleton.class) {
                 if (instance == null) {
                     instance = new ThreadSafeSingleton();
                 }
             }
         }
         return instance;
     }
 }
