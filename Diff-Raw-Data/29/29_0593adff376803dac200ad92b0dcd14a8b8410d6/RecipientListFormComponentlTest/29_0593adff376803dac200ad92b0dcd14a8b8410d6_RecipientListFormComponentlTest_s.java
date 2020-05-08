 package de.flower.rmt.ui.manager.page.event.notification;
 
 import de.flower.rmt.model.event.Event;
 import de.flower.rmt.test.AbstractRMTWicketMockitoTests;
 import de.flower.rmt.test.TestData;
import de.flower.rmt.ui.common.form.field.AbstractFormFieldPanel;
 import org.apache.wicket.Component;
 import org.apache.wicket.markup.Markup;
 import org.apache.wicket.markup.html.form.FormComponent;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.testng.annotations.Test;
 
 import java.util.Collections;
 
 /**
  * @author flowerrrr
  */
 public class RecipientListFormComponentlTest extends AbstractRMTWicketMockitoTests {
 
     @Test
     public void testRender() {
         Event event = new TestData().newEvent();
         final IModel model =  Model.of(Collections.emptyList());
         Component c = new NotificationPanel.RecipientListFormComponent(model, Model.of(event));
         wicketTester.startComponentInPage(c);
         wicketTester.dumpPage();
     }
 
     @Test
     public void testRenderFormFieldPanel() {
         Event event = new TestData().newEvent();
         final IModel model =  Model.of(Collections.emptyList());
         FormComponent c = new NotificationPanel.RecipientListFormComponent(model, Model.of(event));
        wicketTester.startComponentInPage(new AbstractFormFieldPanel("panel", c) {
                    @Override
                    public Markup getAssociatedMarkup() {
                        return Markup.of("<wicket:extend><div wicket:id='input'/></wicket:extend>");
                    }
                }, Markup.of("<div wicket:id='panel' labelKey='button.add'/>"));
         wicketTester.dumpPage();
     }
 }
