 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.marcel.web.asynch;
 
 import java.io.Serializable;
 import java.util.concurrent.ExecutorService;
 import javax.enterprise.inject.Instance;
 import javax.inject.Inject;
 import javax.interceptor.AroundInvoke;
 import javax.interceptor.Interceptor;
 import javax.interceptor.InvocationContext;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Marcel
  */
 @FutureSupport
@Interceptor
 public class FutureInterceptor implements Serializable
 {
     private static final long serialVersionUID = 7938266823530974338L;
 
    
     @AroundInvoke
     public Object executeAsynchronous(final InvocationContext invocationContext) throws Exception
     {
         System.out.println("executeAsynchronous start");
         
        
         try
         {
              Runnable worker = new Runnable() {
 
                  @Override
                  public void run() {
                      try { 
                         System.out.println("worker proceed context");
                          invocationContext.proceed();
                      } catch (Exception ex) {
                          ex.printStackTrace();
                          Logger.getLogger(FutureInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                      }
                  }
              };
             new Thread(worker).start();
              
        } catch (Exception ex) {
            ex.printStackTrace();
         }
         
        return null; 
     }
 }
