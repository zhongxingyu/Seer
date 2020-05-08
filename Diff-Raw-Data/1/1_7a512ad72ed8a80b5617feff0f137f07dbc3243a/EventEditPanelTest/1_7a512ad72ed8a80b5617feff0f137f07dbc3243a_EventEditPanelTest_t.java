 package de.flower.rmt.ui.manager.page.event.edit;
 
 import de.flower.rmt.model.event.Event;
 import de.flower.rmt.model.event.EventType;
 import de.flower.rmt.test.AbstractWicketIntegrationTests;
 import de.flower.rmt.ui.model.EventModel;
 import org.apache.wicket.util.tester.FormTester;
 import org.testng.annotations.Test;
 
 /**
  * @author flowerrrr
  */
 public class EventEditPanelTest extends AbstractWicketIntegrationTests {
 
     @Test
     public void testRender() {
         Event event = eventManager.newInstance(EventType.Match);
         wicketTester.startComponentInPage(new EventEditPanel(new EventModel(event)));
         wicketTester.dumpComponentWithPage();
     }
 
     @Test
     public void testFormSubmit() {
         Event event = eventManager.newInstance(EventType.Training);
         wicketTester.startComponentInPage(new EventEditPanel(new EventModel(event)));
         wicketTester.dumpComponentWithPage();
         wicketTester.debugComponentTrees();
         String formId = "eventEditPanel:form";
         FormTester formTester = wicketTester.newFormTester(formId);
         formTester.select("team:input", 1);
         formTester.setValue("date:input", "1.12.2011");
         formTester.select("time:input", 4); // select some arbitrary value
        formTester.select("kickoff:input", 5);
         formTester.select("venue:input", 1);
         formTester.setValue("summary:input", "New training next sunday");
         formTester.setValue("comment:input", "Hope you bastards are all coming");
         // save form
         wicketTester.clickLink("submitButton");
         wicketTester.dumpPage();
         wicketTester.assertContainsNot("Fehler bei");
     }
 }
