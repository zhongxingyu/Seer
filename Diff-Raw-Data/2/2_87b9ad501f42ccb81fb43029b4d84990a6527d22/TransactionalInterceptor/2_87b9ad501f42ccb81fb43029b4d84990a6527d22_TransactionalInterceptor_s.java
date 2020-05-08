 package net.ftlines.blog.cdidemo.cdi;
 
 import javax.inject.Inject;
 import javax.interceptor.AroundInvoke;
 import javax.interceptor.Interceptor;
 import javax.interceptor.InvocationContext;
 import javax.persistence.EntityManager;
 
 /**
  * NB: this file is derived from the open-source project hosted at https://github.com/42Lines/blog-cdidemo
  */
 @Transactional
 @Interceptor
 public class TransactionalInterceptor {
   @Inject
   EntityManager em;
 
   @AroundInvoke
   public Object wrapInTransaction(InvocationContext invocation) throws Exception {
    boolean owner = em.getTransaction().isActive();
 
     if (owner) {
       em.getTransaction().begin();
     }
 
     try {
       return invocation.proceed();
     } catch (RuntimeException e) {
       em.getTransaction().setRollbackOnly();
       throw e;
     } finally {
       if (owner) {
         if (em.getTransaction().getRollbackOnly()) {
           em.getTransaction().rollback();
         } else {
           em.getTransaction().commit();
         }
       }
     }
   }
 }
