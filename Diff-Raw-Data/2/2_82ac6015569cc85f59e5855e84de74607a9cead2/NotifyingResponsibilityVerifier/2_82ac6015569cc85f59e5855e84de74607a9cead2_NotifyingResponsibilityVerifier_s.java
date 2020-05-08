 /*
  * Created on 12-Aug-2004
  * 
  * (c) 2003-2004 ThoughtWorks Ltd
  *
  * See license.txt for license details
  */
 package jbehave.framework.responsibility;
 
 import java.lang.reflect.Method;
 
 import jbehave.framework.Listener;
 import jbehave.framework.exception.BehaviourFrameworkError;
 
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
     * {@link Listener#responsibilityVerificationEnding(ResponsibilityVerification,Object)
      * responsibilityVerificationEnding(result)} respectively.
      */
     public Result verifyResponsibility(Listener listener, Method method) {
         try {
             listener.responsibilityVerificationStarting(method);
             Object instance = method.getDeclaringClass().newInstance();
             Result result = doVerifyResponsibility(method, instance);
             listener.responsibilityVerificationEnding(result, instance);
             return result;
         } catch (Exception e) {
             throw new BehaviourFrameworkError(e);
         }
     }
 
     protected Result doVerifyResponsibility(Method method, Object instance) {
         return new Result(method.getDeclaringClass().getName(), method.getName());
     }
 
 }
