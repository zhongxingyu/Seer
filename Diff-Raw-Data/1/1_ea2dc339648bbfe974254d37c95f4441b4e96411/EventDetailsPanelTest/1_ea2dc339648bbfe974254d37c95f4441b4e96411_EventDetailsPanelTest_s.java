 package de.flower.rmt.ui.player.page.event;
 
 import de.flower.rmt.model.event.Event;
 import de.flower.rmt.service.IEventManager;
 import de.flower.rmt.test.AbstractRMTWicketMockitoTests;
 import de.flower.rmt.test.TestData;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.mockito.Matchers;
 import org.testng.annotations.Test;
 
 import javax.persistence.metamodel.Attribute;
 
 import static org.mockito.Matchers.anyLong;
 import static org.mockito.Mockito.when;
 
 /**
  * @author flowerrrr
  */
 public class EventDetailsPanelTest extends AbstractRMTWicketMockitoTests {
 
     @SpringBean
     private IEventManager eventManager;
 
     @Test
     public void testRender() {
         Event event = new TestData().newEvent();
         when(eventManager.loadById(anyLong(), Matchers.<Attribute>anyVararg())).thenReturn(event);
         wicketTester.startComponentInPage(new EventDetailsPanel(Model.of(event)));
         wicketTester.dumpComponentWithPage();
         wicketTester.assertContains(event.getSummary());
     }
 
 }
