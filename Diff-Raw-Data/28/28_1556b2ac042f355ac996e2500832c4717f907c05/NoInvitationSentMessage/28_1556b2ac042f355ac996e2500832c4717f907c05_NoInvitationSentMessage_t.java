 package de.flower.rmt.ui.manager.page.invitations;
 
 import de.flower.common.ui.feedback.AlertMessage;
 import de.flower.common.ui.feedback.AlertMessagePanel;
 import de.flower.rmt.model.event.Event;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.ResourceModel;
 
 /**
  * @author flowerrrr
  */
 public class NoInvitationSentMessage extends AlertMessage {
 
     private IModel<Event> model;
 
     public NoInvitationSentMessage(final IModel<Event> model) {
         super(new ResourceModel("alert.message.event.noinvitationsent"),
                 new ResourceModel("alert.message.button.send.invitation"));
         this.model = model;
     }
 
     @Override
     public boolean onClick(final AlertMessagePanel alertMessagePanel) {
         alertMessagePanel.setResponsePage(new NotificationPage(model));
         return false; // don't hide until user has actually sent an invitation
     }
 
     @Override
     public boolean isVisible(final AlertMessagePanel alertMessagePanel) {
         return !model.getObject().isInvitationSent();
     }

    @Override
    public String getId() {
        return super.getId() + model.getObject().getId();
    }
 }
