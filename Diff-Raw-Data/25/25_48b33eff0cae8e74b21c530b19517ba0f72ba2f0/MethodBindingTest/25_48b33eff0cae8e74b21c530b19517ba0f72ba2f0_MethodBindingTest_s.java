 package org.seasar.struts.pojo;
 
 import org.apache.struts.action.ActionMapping;
 import org.seasar.extension.unit.S2TestCase;
 import org.seasar.framework.container.ComponentNotFoundRuntimeException;
 import org.seasar.struts.mock.MockActionMapping;
 
 /**
  * 
  * @author Katsuhiko Nagashima
  * 
  */
 public class MethodBindingTest extends S2TestCase {
 
     private ActionMapping mapping = new MockActionMapping();
    
     protected void setUp() throws Exception {
         super.setUp();
         include("MethodBindingTest.dicon");
     }
 
     public void testExecute() {
         MethodBinding methodBinding = new MethodBinding("#{bindingAction.exe}");
 
         String forward = (String) methodBinding.invoke(this.mapping);
         assertEquals("success", forward);
     }
 
     public void testMethodBindingDownload() {
         MethodBinding methodBinding = new MethodBinding("#{bindingAction.download}");
 
         String forward = (String) methodBinding.invoke(this.mapping);
         assertNull(forward);
     }
 
     public void testNoRegisteredComponent() {
         MethodBinding methodBinding = new MethodBinding("#{noregisteredAction.download}");
 
         try {
             methodBinding.invoke(this.mapping);
             fail();
         } catch (ComponentNotFoundRuntimeException e) {
             // success
         }
     }
 
     public void testIndexedExecute() {
         MethodBinding methodBinding = new MethodBinding("#{bindingAction.exe}", 10);
 
         String forward = (String) methodBinding.invoke(this.mapping);
         assertEquals("success10", forward);
     }
 
 }
