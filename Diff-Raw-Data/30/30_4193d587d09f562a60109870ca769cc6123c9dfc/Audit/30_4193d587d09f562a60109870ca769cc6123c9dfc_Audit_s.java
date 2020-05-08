 /*
  * Copyright (c) 2013.
  * All rights reserved.
  */
 package com.project.commons.audit.aop;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Date;
 
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Pointcut;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 /**
  * @author mhua
  */
 @Aspect
 @Component
 public class Audit {
 
     /**
      * @
      */
     private final Logger LOG = LoggerFactory.getLogger(Audit.class);
 
     /**
      * @
      */
     @Pointcut("@annotation(com.transretail.crm.annotation.Auditable)")
     private void audit() {
     }
 
     /**
      * @param pjp_P
     * @return
      * @throws Throwable
      */
     @Around("audit()")
     public Object around(ProceedingJoinPoint pjp_P) throws Throwable {
         Object[] args = pjp_P.getArgs();
         for (int i = 0; i < args.length; i ++) {
             Object arg = args[i];
 //            try {
 //                Class<?> clazz = arg.getClass();
 //
 //                Method methodGet = clazz.getDeclaredMethod("getAuditTrace");
 //
 //                AuditTrace obj = (AuditTrace) methodGet.invoke(arg, null);
 //                if (obj == null) {
 //                    obj = new AuditTrace();
 //                    obj.setCreatedBy("SYSTEM");
 //                    obj.setCreatedDateTime(new Date());
 //                } else {
 //                    obj.setUpdatedBy("SYSTEM");
 //                    obj.setUpdatedDateTime(new Date());
 //                }
 //
 //                Method methodSet = clazz.getDeclaredMethod("setAuditTrace", AuditTrace.class);
 //                methodSet.invoke(arg, obj);
 //            } catch (IllegalAccessException iaex) {
 //                LOG.error(iaex.toString());
 //            } catch (IllegalArgumentException iaex) {
 //                LOG.error(iaex.toString());
 //            } catch (InvocationTargetException itex) {
 //                LOG.error(itex.toString());
 //            }
         }
 
         Object value = pjp_P.proceed();
         return value;
     }
 
 }
