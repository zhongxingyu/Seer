 package org.timadorus.webapp.client.eventhandling.events;
 
 import org.timadorus.webapp.beans.Campaign;
 import org.timadorus.webapp.beans.User;
 import org.timadorus.webapp.client.eventhandling.handler.ShowCreateFractionHandler;
 
 import com.google.gwt.event.shared.GwtEvent;
 
 public class ShowCreateFractionEvent extends GwtEvent<ShowCreateFractionHandler> {
 
  public static final GwtEvent.Type<ShowCreateFractionHandler> SHOWDIALOG = new GwtEvent.Type<ShowCreateFractionHandler>();
 
   private User user;
 
   private Campaign campaign;
 
   private String text;
 
   public ShowCreateFractionEvent(User user, Campaign campaign) {
     super();
     this.user = user;
     this.campaign = campaign;
     this.text = "";
   }
 
   public ShowCreateFractionEvent(User user, Campaign campaign, String text) {
     super();
     this.user = user;
     this.campaign = campaign;
     this.text = "";
   }
 
   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<ShowCreateFractionHandler> getAssociatedType() {
     return SHOWDIALOG;
   }
 
   @Override
   protected void dispatch(ShowCreateFractionHandler handler) {
     handler.show(user, campaign, text);
   }
 
 }
