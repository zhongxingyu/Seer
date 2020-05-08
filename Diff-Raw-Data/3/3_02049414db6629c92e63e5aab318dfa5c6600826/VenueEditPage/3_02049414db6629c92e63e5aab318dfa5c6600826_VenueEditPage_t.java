 package de.flower.rmt.ui.page.venues.manager;
 
 import de.flower.rmt.model.Venue;
 import de.flower.rmt.ui.model.VenueModel;
 import de.flower.rmt.ui.page.base.manager.ManagerBasePage;
 import de.flower.rmt.ui.page.base.manager.NavigationPanel;
import de.flower.rmt.ui.page.venues.player.WeatherPanel;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.model.IModel;
 
 /**
  * @author flowerrrr
  */
 public class VenueEditPage extends ManagerBasePage {
 
     public VenueEditPage() {
         this(new VenueModel());
     }
 
     public VenueEditPage(IModel<Venue> model) {
         super(model);
         setHeading("manager.venue.edit.heading", null);
         addMainPanel(new VenueEditPanel(model) {
             @Override
             protected void onClose(AjaxRequestTarget target) {
                 setResponsePage(VenuesPage.class);
             }
         });
        addSecondaryPanel(new WeatherPanel(model));
     }
 
     @Override
     public String getActiveTopBarItem() {
         return NavigationPanel.VENUES;
     }
 
 }
