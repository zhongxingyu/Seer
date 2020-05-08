 package org.timadorus.webapp.client.eventhandling.handler;
 
 import org.timadorus.webapp.beans.Campaign;
 import org.timadorus.webapp.beans.User;
 
 import com.google.gwt.event.shared.EventHandler;
 
 public interface ShowCreateFractionHandler extends EventHandler {
 
   /**
    * 
    * @param user
    * @param campaign
   * @param text
   *          The text is optional. It will be displayed on top of the content.
    */
  public void show(User user, Campaign campaign, String text);

 }
