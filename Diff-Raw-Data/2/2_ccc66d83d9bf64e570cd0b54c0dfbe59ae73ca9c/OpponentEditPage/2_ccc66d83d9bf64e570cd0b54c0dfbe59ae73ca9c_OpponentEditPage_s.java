 package de.flower.rmt.ui.manager.page.opponents;
 
 import de.flower.rmt.model.Opponent;
 import de.flower.rmt.ui.manager.ManagerBasePage;
 import de.flower.rmt.ui.manager.NavigationPanel;
 import de.flower.rmt.ui.model.OpponentModel;
 import org.apache.wicket.model.IModel;
 
 /**
  * @author flowerrrr
  */
 public class OpponentEditPage extends ManagerBasePage {
 
     public OpponentEditPage() {
         this(new OpponentModel());
     }
 
     public OpponentEditPage(IModel<Opponent> model) {
         setHeading("manager.opponent.edit.heading", null);
         addMainPanel(new OpponentEditPanel(model) {
             @Override
             protected void onClose() {
                 setResponsePage(OpponentsPage.class);
             }
         });
        // addSecondaryPanel(new Label("foobar", "Put some useful stuff here."));
     }
 
     @Override
     public String getActiveTopBarItem() {
         return NavigationPanel.OPPONENTS;
     }
 
 }
