 package rel.rogue.ircool;
 /**
  *
  * @author Spencer
  */
 public class EventHandler extends org.pircbotx.hooks.ListenerAdapter {
     
     org.pircbotx.PircBotX user = IRCool.getUser();
     
     
     /**
      * Used for passing messages to client.
      * 
      * @param event 
      */
     @Override
     public void onMessage(org.pircbotx.hooks.events.MessageEvent event) {
        Utils.printMsg(event.getChannel().toString(), event.getUser().getNick(), event.getMessage());
     }
     
     
     /**
      * Auto-join, which needs to be made configurable. Also prints kick
      * statements.
      * 
      * @param event 
      */
     @Override
     public void onKick(org.pircbotx.hooks.events.KickEvent event) {
         if (event.getRecipient().getNick().equals(user.getNick())) {
             Utils.print(event.getChannel().toString(), "You have been kicked from " + event.getChannel().getName() + ". (" + event.getReason() + ")");
             user.joinChannel(event.getChannel().getName());
         }
         else {
             Utils.print(event.getChannel().toString(), event.getSource().getNick() + " has kicked " + event.getRecipient().getNick() + " from " + event.getChannel().getName() + ". (" + event.getReason() + ")");
         }
     }
     
     /**
      * 
      * Used for printing action statements.
      * 
      * @param event 
      */
     @Override
     public void onAction(org.pircbotx.hooks.events.ActionEvent event) {
         Utils.printAction(event.getChannel().toString(), event.getUser().getNick(), event.getMessage());
     }
     
     /**
      * 
      * Used for printing nick changes.
      * 
      * @param event 
      */
     
     @Override
     public void onNickChange(org.pircbotx.hooks.events.NickChangeEvent event) {
         if (user.getNick().equals(event.getNewNick())) {
             Utils.printCurrent("You are now known as " + event.getNewNick());
         }
         else {
             Utils.printCurrent(event.getOldNick() + " is now known as " + event.getNewNick());
         }
     }
 }
