 package de.flower.rmt.ui.manager.page.event.notification;
 
 import de.flower.rmt.model.Invitation;
 import de.flower.rmt.model.event.Event;
 import de.flower.rmt.test.AbstractRMTWicketMockitoTests;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.util.tester.FormTester;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import javax.mail.internet.InternetAddress;
 import java.util.Arrays;
import java.util.HashSet;
 import java.util.List;
import java.util.Set;
 
 import static org.testng.Assert.*;
 
 /**
  * @author flowerrrr
  */
 public class SelectRecipientPanelTest extends AbstractRMTWicketMockitoTests {
 
     @BeforeMethod
     public void setUp() {
         wicketTester.setSerializationCheck(false);
     }
 
     @Test
     public void testSubmitSelectedRecipients() {
        final Set<InternetAddress> actualRecipients = new HashSet<InternetAddress>();
         Event event = testData.newEvent();
         InternetAddress ia1 = event.getInvitations().get(2).getInternetAddress();
         InternetAddress ia2 = event.getInvitations().get(4).getInternetAddress();
 
         Panel panel = new SelectRecipientPanel(Model.of(event), (IModel<List<Invitation>>) (Object) Model.ofList(event.getInvitations())) {
             @Override
             protected void onSubmit(final AjaxRequestTarget target, final List<InternetAddress> recipients) {
                 actualRecipients.addAll(recipients);
             }
 
         };
 
         wicketTester.startComponentInPage(panel);
         wicketTester.dumpComponentWithPage();
 
         FormTester formTester = wicketTester.newFormTester("form");
         formTester.select("group", 2);
         formTester.select("group", 4);
 
         wicketTester.clickLink("submitButton");
        // order of selected choices is not guaranteed to be same as in html-markup
        assertEquals(actualRecipients, new HashSet<InternetAddress>(Arrays.asList(ia1, ia2)));
     }
 }
