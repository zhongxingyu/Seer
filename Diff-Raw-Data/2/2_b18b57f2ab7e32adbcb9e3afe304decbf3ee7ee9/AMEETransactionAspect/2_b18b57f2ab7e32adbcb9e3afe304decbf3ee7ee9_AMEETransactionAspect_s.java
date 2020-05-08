 package com.amee.base.transaction;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.aspectj.lang.annotation.AfterReturning;
 import org.aspectj.lang.annotation.AfterThrowing;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Before;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.core.annotation.Order;
 import org.springframework.stereotype.Service;
 
 /**
  * An aspect bean which publishes {@link TransactionEvent}s before and after invocations of methods
  * annotated with {@link AMEETransaction}.
  * <p/>
  * {@link AMEETransaction}s must not be nested in method stacks.
  */
 @Aspect
 @Order(0)
 @Service
 public class AMEETransactionAspect implements ApplicationContextAware {
 
     private final Log log = LogFactory.getLog(getClass());
 
     // The current ApplicationContext.
     private ApplicationContext applicationContext;
 
     /**
      * Before methods are invoked, publish the {@link TransactionEventType} BEFORE_BEGIN event.
      */
     @Before("@annotation(com.amee.base.transaction.AMEETransaction)")
     public void before() {
         log.trace("before()");
         publishEvent(TransactionEventType.BEFORE_BEGIN);
     }
 
     /**
      * When exceptions are thrown within a method, publish the {@link TransactionEventType} ROLLBACK & END events.
      */
     @AfterThrowing("@annotation(com.amee.base.transaction.AMEETransaction)")
     public void afterThrowing() {
         log.trace("afterThrowing()");
         publishEvent(TransactionEventType.ROLLBACK);
         publishEvent(TransactionEventType.END);
     }
 
     /**
     * After method returns, publish the {@link TransactionEventType} COMMENT & END events.
      */
     @AfterReturning("@annotation(com.amee.base.transaction.AMEETransaction)")
     public void afterReturning() {
         log.trace("afterReturning()");
         publishEvent(TransactionEventType.COMMIT);
         publishEvent(TransactionEventType.END);
     }
 
     /**
      * Publishes a {@link TransactionEvent} for the supplied {@link TransactionEventType}.
      *
      * @param transactionEventType to publish
      */
     private void publishEvent(TransactionEventType transactionEventType) {
         applicationContext.publishEvent(new TransactionEvent(this, transactionEventType));
     }
 
     /**
      * Set the {@link ApplicationContext} instance.
      *
      * @param applicationContext to set
      */
     @Override
     public void setApplicationContext(ApplicationContext applicationContext) {
         this.applicationContext = applicationContext;
     }
 }
 
 
