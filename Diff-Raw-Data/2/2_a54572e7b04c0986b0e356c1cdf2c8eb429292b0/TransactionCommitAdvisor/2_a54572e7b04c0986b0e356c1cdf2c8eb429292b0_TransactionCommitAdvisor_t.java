 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.util;
 
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.aop.AfterReturningAdvice;
 
 import java.lang.reflect.Method;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ldangelo
  * Date: Jul 6, 2009
  * Time: 4:15:31 PM
  * To change this template use File | Settings | File Templates.
  */
 public class TransactionCommitAdvisor implements MethodInterceptor {
    private Log logger = LogFactory.getLog(TransactionCommitAdvisor.class);
 
     public Object invoke(MethodInvocation invocation) throws Throwable {
         if (invocation.getMethod().getName().equals("commit")) {
             // unwind the taskstack and execute the tasks on the stack
             logger.info("===== executing task stack!");
             TaskStack.execute();
         }
         Object rval =  invocation.proceed();
         logger.debug("======= After: " + invocation.getMethod().getName());
         return rval;
     }
     
     public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
 
         logger.debug("=========== After: " + method.getName());
 
     }
 
 
 }
