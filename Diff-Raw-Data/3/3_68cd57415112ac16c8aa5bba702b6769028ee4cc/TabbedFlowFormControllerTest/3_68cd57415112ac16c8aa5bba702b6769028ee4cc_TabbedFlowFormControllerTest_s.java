 package gov.nih.nci.cabig.ctms.web.tabs;
 
 import gov.nih.nci.cabig.ctms.CommonsSystemException;
 import static org.easymock.classextension.EasyMock.*;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Map;
 
 /**
  * @author Rhett Sutphin
  */
 // Maven appears to skip tests named "Abstract*"
 public class TabbedFlowFormControllerTest extends WebTestCase {
     private TestController controller;
     private Flow<Object> flow;
     private Object command;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         controller = new TestController();
         flow = new Flow<Object>("test");
         flow.addTab(new Tab<Object>("One", "one", "one"));
         flow.addTab(new Tab<Object>("Two", "two", "two"));
         flow.addTab(new Tab<Object>("Three", "three", "three"));
         controller.setFlow(flow);
 
         command = new Object();
     }
 
     public void testViewIsForSelectedTab() throws Exception {
         assertEquals("two", controller.getViewName(request, command, 1));
     }
 
     public void testCount() throws Exception {
         assertEquals(3, controller.getPageCount(request, command));
     }
 
     @SuppressWarnings({ "unchecked" })
     public void testRefdataIncludesTab() throws Exception {
         Map<String, Object> actual
             = (Map<String, Object>) controller.referenceData(request, command, errors, 2);
         Tab<Object> tab = (Tab<Object>) actual.get("tab");
         assertNotNull("No tab in refdata", tab);
         assertSame("Wrong tab in refdata", flow.getTab(2), tab);
     }
 
     @SuppressWarnings({ "unchecked" })
     public void testRefdataIncludesFlow() throws Exception {
         Map<String, Object> actual
             = (Map<String, Object>) controller.referenceData(request, command, errors, 2);
         Flow<Tab<Object>> actualFlow = (Flow<Tab<Object>>) actual.get("flow");
         assertNotNull("No flow in refdata", actualFlow);
         assertSame("Wrong flow in refdata", flow, actualFlow);
     }
 
     @SuppressWarnings({ "unchecked" })
     public void testRefdataIncludesThatFromTab() throws Exception {
         flow.addTab(new Tab<Object>() {
             @Override
             public Map<String, Object> referenceData() {
                 Map<String, Object> refdata = super.referenceData();
                 refdata.put("answer", 42);
                 return refdata;
             }
         });
 
         Map<String, Object> actual
             = (Map<String, Object>) controller.referenceData(request, command, errors, 3);
         assertEquals("Tab refdata not present", 42, actual.get("answer"));
     }
 
     @SuppressWarnings({ "unchecked" })
     public void testTabRefdataReceivesCommand() throws Exception {
         flow.addTab(new Tab<Object>() {
             @Override
             public Map<String, Object> referenceData(Object command) {
                 Map<String, Object> refdata = super.referenceData(command);
                 refdata.put("recvdCommand", command);
                 return refdata;
             }
         });
 
         Map<String, Object> actual
             = (Map<String, Object>) controller.referenceData(request, command, errors, 3);
         assertSame("Command not passed to refdata call", command, actual.get("recvdCommand"));
     }
 
     @SuppressWarnings({ "unchecked" })
     public void testValidatePageValidatesTab() throws Exception {
         Tab<Object> mockTab = createMock(Tab.class);
 
         mockTab.setNumber(3);
         mockTab.validate(command, errors);
         expect(mockTab.isAllowDirtyBack()).andReturn(false);
         expect(mockTab.isAllowDirtyForward()).andReturn(true);
         replay(mockTab);
 
         flow.addTab(mockTab);
         controller.validatePage(command, errors, 3, false);
         verify(mockTab);
         assertFalse(controller.isAllowDirtyBack());
         assertTrue(controller.isAllowDirtyForward());
     }
 
     @SuppressWarnings({ "unchecked" })
     public void testPostProcessPageDelegatesToTab() throws Exception {
         Tab<Object> mockTab = createMock(Tab.class);
 
         mockTab.setNumber(3);
         mockTab.postProcess(request, command, errors);
         replay(mockTab);
 
         flow.addTab(mockTab);
         controller.postProcessPage(request, command, errors, 3);
         verify(mockTab);
     }
 
     public void testNoExceptionIfNoTabConfigurer() throws Exception {
         controller.getFlow(command);
     }
 
     public void testTabConfigurerUsedIfProvided() throws Exception {
         TabConfigurer configurer = createMock(TabConfigurer.class);
         controller.setTabConfigurer(configurer);
 
         configurer.injectDependencies(flow);
         replay(configurer);
 
         controller.getFlow(command);
         verify(configurer);
     }
 
     public void testExceptionOnGetFlowWithNonStaticFlowFactory() throws Exception {
         controller.setFlowFactory(new StubFlowFactory());
         try {
             controller.getFlow();
             fail("Exception not thrown");
         } catch (CommonsSystemException cse) {
             assertEquals(
                 "getFlow() only works with StaticFlowFactory.  You are using StubFlowFactory.",
                 cse.getMessage());
         }
     }
     
     public void testGetFlowWorksWithDefaults() throws Exception {
         assertNotNull(controller.getFlow());
     }
 
     private static class TestController extends AbstractTabbedFlowFormController<Object> {
         private boolean finished;
 
         public boolean isFinished() {
             return finished;
         }
 
         @Override
         protected ModelAndView processFinish(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
             finished = true;
             return null;
         }
     }
 
     private static class StubFlowFactory implements FlowFactory<Object> {
         public Flow<Object> createFlow(Object command) {
             throw new UnsupportedOperationException("createFlow not implemented");
         }
     }
 }
