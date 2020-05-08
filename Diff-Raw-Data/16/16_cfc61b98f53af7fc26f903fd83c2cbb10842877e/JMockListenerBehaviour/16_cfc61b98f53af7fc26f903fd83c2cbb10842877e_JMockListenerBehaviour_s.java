 /*
  * Created on 27-Jun-2004
  * 
  * (c) 2003-2004 ThoughtWorks Ltd
  *
  * See license.txt for license details
  */
 package jbehave.extensions.jmock.listener;
 
 import java.lang.reflect.Method;
 import java.util.List;
 
 import jbehave.extensions.jmock.JMockable;
 import jbehave.framework.Listener;
 import jbehave.framework.ResponsibilityVerification;
 import jbehave.framework.ResponsibilityVerifier;
 import jbehave.framework.Verify;
 import junit.framework.AssertionFailedError;
 
import org.jmock.Mock;

 /**
  * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
  * @author <a href="mailto:damian.guy@thoughtworks.com">Damian Guy</a>
  */
 public class JMockListenerBehaviour {
 	private Listener listener;
 
 	public void setUp() {
 		listener = new JMockListener();
 	}
 
    public static class BehaviourClassWithPrivateMock {
         public boolean verifyWasCalled = false;
         
         private Mock someMock = new Mock(List.class) {
             public void verify() {
                 verifyWasCalled = true;
             }
         };
         
         public void shouldDoSomething() {
             someMock.stubs();
         }
     }
 
     /** pull out the first responsibility method in a spec */
     private Method firstResponsibilityMethod(Class behaviourClass) throws Exception {
         Method[] methods = behaviourClass.getMethods();
         for (int i = 0; i < methods.length; i++) {
             Method method = methods[i];
             if (method.getName().startsWith("should")) {
                 return method;
             }
         }
       throw new Error("No responsibility method found in " + behaviourClass.getName());
     }
 
     private ResponsibilityVerifier getSingleResponsibilityVerifier(Class behaviourClass) throws Exception {
         return new ResponsibilityVerifier(firstResponsibilityMethod(behaviourClass));
     }
     
 	public void shouldVerifyPublicMockFieldsWhenBehaviourMethodSucceeds() throws Exception {
         // setup
         ResponsibilityVerifier verifier = getSingleResponsibilityVerifier(BehaviourClassWithPrivateMock.class);
         ResponsibilityVerification verification = verifier.verifyResponsibility(Listener.NULL);
 		BehaviourClassWithPrivateMock behaviourClassInstance = new BehaviourClassWithPrivateMock();
 
         // execute
         listener.responsibilityVerificationEnding(verification, behaviourClassInstance);
         
         // verify
         Verify.that(behaviourClassInstance.verifyWasCalled);
 	}
 
	public static class BehaviourClassWithFailingMock {
         public boolean verifyWasCalled = false;
 
         private Mock someMock = new Mock(List.class) {
             public void verify() {
                 throw new AssertionFailedError("blah was not invoked");
             }
         };
 
         public void shouldDoSomething() {
             someMock.stubs();
         }
     }
 
 	public void shouldCreateNewVerificationWhenVerifyFails() throws Exception {
 		// setup
         ResponsibilityVerifier verifier = getSingleResponsibilityVerifier(BehaviourClassWithPrivateMock.class);
         ResponsibilityVerification verification = verifier.verifyResponsibility(Listener.NULL);
         Object behaviourClassInstance = new BehaviourClassWithFailingMock();
 		// execute
         ResponsibilityVerification verifyMockResult = listener.responsibilityVerificationEnding(verification, behaviourClassInstance);
 
 		// verify
 		Verify.notNull(verifyMockResult);
 		Verify.not(verifyMockResult == verification);
 	}
 
 	interface AnIntf {
 		String someMethod();
 	}
 
 	public static class BehaviourClassThatUsesJMock implements JMockable {
 
 		public void shouldUseAMock() throws Exception {
 	        Mock m = new Mock(AnIntf.class);
 			m.expects(Invoked.once()).method("someMethod").withNoArguments()
                 .will(Return.value("hello"));
 		}
 	}
 
 	public void shouldAutomaticallyVerifyMocks() throws Exception {
 		// setup
 		ResponsibilityVerifier verifier = getSingleResponsibilityVerifier(BehaviourClassThatUsesJMock.class);
 		BehaviourClassThatUsesJMock behaviourClassInstance = new BehaviourClassThatUsesJMock();
 
 		// execute
 	    listener.responsibilityVerificationStarting(verifier, behaviourClassInstance);
 		behaviourClassInstance.shouldUseAMock();
 		ResponsibilityVerification verification = listener.responsibilityVerificationEnding(new ResponsibilityVerification("shouldUseAMock", "AJMockUsingSpec"), behaviourClassInstance);
 		// verify
 		Verify.that("should fail JMock verification", verification.failed());
 	}
 }
