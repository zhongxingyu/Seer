 /*
  * Created on 29-Dec-2003
  *
  * (c) 2003-2004 ThoughtWorks
  *
  * See license.txt for licence details
  */
 package jbehave.core.listener;
 
 import jbehave.core.behaviour.BehaviourMethod;
 import jbehave.core.exception.PendingException;
 import jbehave.core.exception.VerificationException;
 import jbehave.core.result.BehaviourMethodResult;
 import jbehave.core.result.Result;
 import mock.jbehave.core.listener.PendingResultInNonJBehavePackage;
 
 
 /**
  * @author <a href="mailto:dan@jbehave.org">Dan North</a>
  */
 public class PlainTextMethodListenerBehaviour extends PlainTextListenerBehaviourSupport {
     private BehaviourMethod behaviourMethod;
 
     public void setUp() throws Exception {
         super.setUp();
         behaviourMethod = new BehaviourMethod(new FooBehaviour(), FooBehaviour.class.getMethod("shouldDoSomething", null));
     }
 
     protected PlainTextListener newPlainTextListener() {
         return new PlainTextListener(writer, timer);
     }
 
     protected Result newSuccessResult() {
         return new BehaviourMethodResult(behaviourMethod);
     }
 
     protected Result newFailureResult() {
         return new BehaviourMethodResult(behaviourMethod, new VerificationException("oops"));
     }
 
     protected Result newPendingResult() {
         return new BehaviourMethodResult(behaviourMethod, new PendingException());
     }
 
     public void shouldPrintStackTraceForFailure() throws Exception {
         // given...
         Result failed = newFailureResult();
 
         // expect...
         String expectedShortName = "Foo";
         String expectedFullName = FooBehaviour.class.getName();
 
         // when...
         listener.gotResult(failed);
         listener.printReport();
 
         // then...
         verifyOutputContains("Failures:");
         verifyOutputContains(expectedShortName);
         verifyOutputContains(expectedFullName);
         verifyOutputContains("VerificationException");
     }
 
     public void shouldPrintStackElementForPending() throws Exception {
         // given...
         Result pending = newPendingResultFromNonJBehavePackage();
 
         // expect
         String expectedShortName = "Foo";
         String expectedFullName = FooBehaviour.class.getName();
 
         // when...
         listener.gotResult(pending);
         listener.printReport();
 
         // then...
         verifyOutputContains("Pending:");
        verifyOutputContains("at mock.jbehave.core.listener.PendingResultInNonJBehavePackage.<init>(PendingResultInNonJBehavePackage.java");
     }
 
     private Result newPendingResultFromNonJBehavePackage() {
         return new PendingResultInNonJBehavePackage(behaviourMethod);
     }
 }
