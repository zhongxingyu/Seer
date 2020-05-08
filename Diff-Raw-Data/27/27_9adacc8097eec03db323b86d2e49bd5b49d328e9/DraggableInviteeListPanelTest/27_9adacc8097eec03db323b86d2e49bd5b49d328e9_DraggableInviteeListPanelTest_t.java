 package de.flower.rmt.ui.page.event.manager.lineup;
 
 import de.flower.rmt.model.db.entity.Invitation;
 import de.flower.rmt.model.db.entity.event.Event;
 import de.flower.rmt.test.AbstractRMTWicketIntegrationTests;
 import de.flower.rmt.ui.model.EventModel;
 import org.testng.annotations.Test;
 
 /**
  * @author flowerrrr
  */
 public class DraggableInviteeListPanelTest extends AbstractRMTWicketIntegrationTests {
 
     @Test
     public void testRender() {
         Event event = testData.createEventWithResponses();
         testData.createLineup(event);
        wicketTester.startComponentInPage(new TestDraggableInviteeListPanel(event));
         wicketTester.dumpComponentWithPage();
     }

    private static class TestDraggableInviteeListPanel extends DraggableInviteeListPanel {

        public TestDraggableInviteeListPanel(final Event event) {
            super(new EventModel(event));
        }

        @Override
        protected boolean isDraggablePlayerVisible(final Invitation invitation) {
            return true;
        }
    }
 }
