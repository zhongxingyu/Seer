 package de.flower.rmt.test;
 
 import com.thoughtworks.xstream.converters.ConversionException;
 import de.flower.common.ui.Css;
 import de.flower.common.ui.serialize.Filter;
 import de.flower.common.ui.serialize.LoggingSerializer;
 import de.flower.rmt.model.RSVPStatus;
 import junit.framework.AssertionFailedError;
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.Page;
 import org.apache.wicket.feedback.FeedbackMessage;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
 import org.apache.wicket.request.IRequestHandler;
 import org.apache.wicket.util.lang.Classes;
 import org.apache.wicket.util.tester.FormTester;
 import org.apache.wicket.util.tester.WicketTester;
 import org.apache.wicket.util.tester.WicketTesterHelper;
 import org.apache.wicket.util.visit.IVisit;
 import org.apache.wicket.util.visit.IVisitor;
 import org.apache.wicket.util.visit.Visits;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.testng.Assert.*;
 
 /**
  * @author flowerrrr
  */
 
 public class RMTWicketTester extends WicketTester {
 
     private final static Logger log = LoggerFactory.getLogger(RMTWicketTester.class);
 
     private Filter filter = new Filter("\"de\\.flower\\.rmt\\.model\\.[^-]*?\"");
 
     private LoggingSerializer loggingSerializer = new LoggingSerializer(filter);
 
     private boolean serializationCheck = true;
 
     public RMTWicketTester(final WebApplication application) {
         super(application);
         filter.addExclusion(RSVPStatus.class.getName());
     }
 
     /**
      * Gets the debug component trees. Same as {@link WicketTester#debugComponentTrees()}, only does not log output,
      * rather returns it as a string.
      *
      * @return the debug component trees
      */
     public String getDebugComponentTrees() {
         return getDebugComponentTrees("");
     }
 
     public String getDebugComponentTrees(final String filter) {
         final StringBuilder s = new StringBuilder();
         for (final WicketTesterHelper.ComponentData obj : RMTWicketTesterHelper.getComponentData(getLastRenderedPage(), false)) {
             if (obj.path.matches(".*" + filter + ".*")) {
                 s.append("path\t").append(obj.path).append(" \t").append(obj.type).append(" \t[").append(obj.value)
                         .append("]\n");
             }
         }
         return s.toString();
     }
 
     /**
      * Uses DEBUG level instead of INFO.
      */
     @Override
     public void debugComponentTrees() {
         log.debug(getDebugComponentTrees());
     }
 
     /**
      * Uses DEBUG level instead of INFO.
      */
     @Override
     public void dumpPage() {
         log.debug(getLastResponseAsString());
     }
 
     /**
      * Dump component with page. While wicketTester.dumpPage does strip everything around the component this method will
      * dump the whole page.
      */
     public void dumpComponentWithPage() {
         log.debug(getComponentWithPage());
     }
 
     /**
      * Logs the last ajax-response as string
      */
     public void dumpAjaxResponse() {
         log.debug(getLastResponseAsString());
     }
 
     public String getComponentWithPage() {
         return getLastResponse().getDocument();
     }
 
     public void assertAjaxValidationError(final String path, final String value) {
         assertAjaxValidation(null, path, value, true);
     }
 
     public void assertNoAjaxValidationError(final String path, final String value) {
         assertAjaxValidation(null, path, value, false);
     }
 
     public void assertAjaxValidationError(final Component field, final String value) {
         assertAjaxValidation(field, null, value, true);
     }
 
     public void assertNoAjaxValidationError(final Component field, final String value) {
         assertAjaxValidation(field, null, value, false);
     }
 
     /**
      * Verifies that validation works on a form component. Triggers onchange ajax event and inspects the
      * class attribute of the validated form field.
      * <p/>
      * Assumes that the form has the wicket-id 'form'. Either field or path must be set.
      *
      * @param field       the wicket component
      * @param path        the path
      * @param value       the value
      * @param expectError the assertion
      */
     private void assertAjaxValidation(final Component field, final String path, final String value, final boolean expectError) {
         final FormTester formTester = newFormTester("form");
         Component c = field;
         if (field != null) {
             formTester.setValue(field, value);
         } else {
             formTester.setValue(path, value);
             c = formTester.getForm().get(path);
         }
         executeAjaxEvent(c, "onchange");
         dumpPage();
         final String cssClass = expectError ? Css.ERROR : Css.VALID;
         assertContains("class=\"[^\"]*" + cssClass);
     }
 
     /**
      * Asserts error-level feedback messages. Same as {@link #assertErrorMessages(String...)}, but does test for
      * #contains instead of #equals. Helps to decouple assertion from exact error messages.
      *
      * @param errorMessagesParts
      */
     public void assertErrorMessagesContains(final String... errorMessagesParts) {
         final List<String> actualMessages = getMessagesAsString(FeedbackMessage.ERROR);
         for (final String errorMesssagePart : errorMessagesParts) {
             if (!de.flower.rmt.test.StringUtils.containedInAny(errorMesssagePart, actualMessages)) {
                 log.info(actualMessages.toString());
                 fail("Error message part [" + errorMesssagePart + "] not found in any of the error messages["+ actualMessages.toString() + "].");
             }
         }
     }
 
     public List<String> getMessagesAsString(final int level) {
         final List<Serializable> messages = getMessages(level);
         final List<String> result = new ArrayList<String>();
         for (final Serializable message : messages) {
             result.add(message.toString());
         }
         return result;
     }
 
     public void assertComponentNotOnAjaxResponse(final String componentPath) {
         try {
             assertComponentOnAjaxResponse(componentPath);
             fail("Component [" + componentPath + "] was in last ajax response.");
         } catch (final AssertionFailedError e) {
         }
     }
 
     /**
      * Method will serialize the rendered page and output the size of the serialized object. Helps to detect
      * implementation errors where big objects are unintentionally serialized. If log-level is TRACE a xml-seriailzed
      * version of the page is dumped. Helps to find the objects that bloat the page size.
      */
     @Override
     public boolean processRequest(final MockHttpServletRequest request, final IRequestHandler forcedRequestHandler) {
         final boolean b = super.processRequest(request, forcedRequestHandler);
         if (isSerializationCheck()) {
             inspectSerializedPage();
         }
         return b;
     }
 
     /**
      * @throws IllegalStateException when the logging seriliazer detects illegal objects in the page-object-graph.
      */
     private void inspectSerializedPage() {
         final Page page = getLastRenderedPage();
         if (page != null) {
             try {
                 loggingSerializer.notify(page, null);
             } catch (final ConversionException e) {
                 // happens if anonymous inner classes in tested panels reference a variable of the test class. in that
                 // case xstream tries
                 // to serialize itself (the instance of this class) away => boom
                 // strictly spoken it is not a test failure, but should be avoided.
                 // See https://rb-tmp-dev.de.bosch.com/wiki/display/BHOME/2011/10/13/Debugging+serialization+errors+in+Tests
                 log.error(
                         "Cannot check serialized version of your Page/Panel. Are you having references to your test class in your tested panels?",
                         e);
                 throw e;
             }
         }
     }
 
     /**
      * In case component cannot be found log component tree so that user immediately can lookup correct path of
      * component.
      */
     @Override
     public Component getComponentFromLastRenderedPage(final String path, final boolean wantVisibleInHierarchy) {
         try {
             return super.getComponentFromLastRenderedPage(path, wantVisibleInHierarchy);
         } catch (final AssertionFailedError e) {
             return findComponent(path, wantVisibleInHierarchy);
         }
     }
 
     /**
      * This method checks if a CSS class is set on a components markup.
      *
      * @param path             to the component
      * @param expectedCssClass expected CSS class
      */
     public void assertComponentCssClass(final String path, final String expectedCssClass) {
         assertNotNull(expectedCssClass);
         final String cssClassAttribute = (String) getComponentFromLastRenderedPage(path).getMarkupAttributes().get(
                 "class");
         assertNotNull(cssClassAttribute);
         final String[] cssClasses = cssClassAttribute.split(" ");
         String wantedCssClass = cssClassAttribute;
         for (final String foundClass : cssClasses) {
             if (expectedCssClass.equals(foundClass)) {
                 wantedCssClass = foundClass;
                 break;
             }
         }
         assertEquals(expectedCssClass, wantedCssClass);
     }
 
     public boolean isSerializationCheck() {
         return serializationCheck;
     }
 
     public void setSerializationCheck(final boolean serializationCheck) {
         this.serializationCheck = serializationCheck;
     }
 
     /**
      * Iterates through all components and looks for one with given name
      */
     protected Component findComponent(String name, boolean wantVisibleInHierarchy) {
         ArrayList<RMTWicketTesterHelper.ComponentData> list = new ArrayList<RMTWicketTesterHelper.ComponentData>();
         for (final RMTWicketTesterHelper.ComponentData obj : RMTWicketTesterHelper.getComponentData(getLastRenderedPage(), false)) {
             if (obj.component.getId().equals(name)) {
                 list.add(obj);
             } else if (obj.path.endsWith(name)) {
                 list.add(obj);
             }
         }
         if (list.isEmpty()) {
             debugComponentTrees();
             fail("name: '" + name + "' does not exist for page: " + Classes.simpleName(getLastRenderedPage().getClass()));
         }
         if (list.size() > 1) {
             debugComponentTrees();
             fail("name: '" + name + "' is ambiguous for page: " + Classes.simpleName(getLastRenderedPage().getClass()));
         }
         RMTWicketTesterHelper.ComponentData c = list.get(0);
         if (!wantVisibleInHierarchy || c.component.isVisibleInHierarchy()) {
             return c.component;
         } else {
             return null;
         }
     }
 
     protected List<Component> getAllComponents(MarkupContainer parent) {
         final List<Component> list = new ArrayList<Component>();
         Visits.visitChildren(parent, new IVisitor<Component, Void>() {
             public void component(final Component component, final IVisit<Void> visit) {
                 list.add(component);
             }
         });
         return list;
     }
 }
