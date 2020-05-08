 /*
  * Created on 12-Aug-2004
  * 
  * (c) 2003-2004 ThoughtWorks Ltd
  *
  * See license.txt for license details
  */
 package com.thoughtworks.jbehave.core.responsibility;
 
 import java.lang.reflect.Method;
 
 import com.thoughtworks.jbehave.core.Listener;
 import com.thoughtworks.jbehave.core.exception.JBehaveFrameworkError;
 
 /**
  * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
  */
 public class NotifyingResponsibilityVerifier implements ResponsibilityVerifier {
 
     /**
      * Verify an individual responsibility.
      * 
      * The {@link Listener} is alerted before and after the verification,
      * with calls to {@link Listener#responsibilityVerificationStarting(Method)
      * responsibilityVerificationStarting(this)} and
      * {@link Listener#responsibilityVerificationEnding(Result, Object)
      * responsibilityVerificationEnding(result)} respectively.
      */
     public Result verifyResponsibility(Listener listener, Method method, Object instance) {
         try {
             listener.responsibilityVerificationStarting(method);
             Result result = doVerifyResponsibility(method, instance);
            result = listener.responsibilityVerificationEnding(result, instance);
             return result;
         } catch (Exception e) {
             System.out.println("Problem verifying " + method);
             throw new JBehaveFrameworkError(e);
         }
     }
 
     protected Result doVerifyResponsibility(Method method, Object instance) {
         return new Result(method.getDeclaringClass().getName(), method.getName());
     }
 
 }
