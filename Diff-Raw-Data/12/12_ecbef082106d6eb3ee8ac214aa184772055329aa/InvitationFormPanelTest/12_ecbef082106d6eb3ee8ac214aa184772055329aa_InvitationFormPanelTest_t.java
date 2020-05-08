 package de.flower.rmt.ui.page.event.player;
 
 import de.flower.rmt.model.db.entity.Invitation;
 import de.flower.rmt.model.db.entity.User;
 import de.flower.rmt.model.db.entity.event.Event;
 import de.flower.rmt.test.AbstractRMTWicketMockitoTests;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.testng.annotations.Test;
 
 /**
  * @author flowerrrr
  */
 public class InvitationFormPanelTest extends AbstractRMTWicketMockitoTests {
 
     @Test
     public void testRender() {
         Event event = testData.newEvent();
         Invitation invitation = new Invitation(event, (User) null);
         wicketTester.startComponentInPage(new InvitationFormTestPanel(Model.of(invitation), Model.of(false)));
         wicketTester.dumpComponentWithPage();
        wicketTester.assertInvisible("invitationClosedMessage");
     }
 
     @Test
     public void testRenderEventClosed() {
         Event event = testData.newEvent();
         Invitation invitation = new Invitation(event, (User) null);
         wicketTester.startComponentInPage(new InvitationFormTestPanel(Model.of(invitation), Model.of(true)));
         wicketTester.dumpComponentWithPage();
        wicketTester.assertVisible("invitationClosedMessage");
     }
 
     private static class InvitationFormTestPanel extends InvitationFormPanel {
 
         public InvitationFormTestPanel(final IModel<Invitation> model, IModel<Boolean> eventClosedModel) {
             super("panel", model, eventClosedModel);
         }
 
         @Override
         protected void onSubmit(final Invitation invitation, final AjaxRequestTarget target) {
         }
     }
 
 }
