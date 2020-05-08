 package org.jggug.kobo.gircbot.core;
 
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.List;
import java.util.Map;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jibble.pircbot.User;
 
 public class PrimaryMonitor {
 
     private static final Log LOG = LogFactory.getLog(PrimaryMonitor.class);
 
     protected String defaultChannel;
     protected List<String> orderedPrimaryNicks;
     protected IrcControl ircControl;
    protected Map<String, Boolean> primaryStatus = new HashMap<String, Boolean>();
 
     public PrimaryMonitor(String defaultChannel, List<String> orderedPrimaryNicks, IrcControl ircControl) {
         this.defaultChannel = defaultChannel;
         this.orderedPrimaryNicks = orderedPrimaryNicks;
         this.ircControl = ircControl;
     }
 
     public boolean isPrimary(String channel) {
         String myNick = ircControl.getNick();
 
         List<String> joinedNicks = getJoinedNicks(channel);
         if (LOG.isDebugEnabled()) LOG.debug(String.format("joinedNicks: %s", joinedNicks));
         if (!joinedNicks.contains(myNick)) {
             if (LOG.isDebugEnabled()) LOG.debug(String.format("This bot seems not to join yet.: %s in %s %s", myNick, channel, joinedNicks));
             return false;
         }
 
         if (LOG.isDebugEnabled()) LOG.debug(String.format("orderedPrimaryNicks: %s", orderedPrimaryNicks));
         if (!orderedPrimaryNicks.contains(myNick)) {
             throw new IllegalStateException(String.format("Why isn't there this bot in orderedPrimaryNicks? : %s %s", myNick, orderedPrimaryNicks));
         }
 
         List<String> joinedPrimaryNicks = new ArrayList<String>(orderedPrimaryNicks);
         joinedPrimaryNicks.retainAll(joinedNicks);
         if (LOG.isDebugEnabled()) LOG.debug(String.format("joined orderedPrimaryNicks: %s -> %s", orderedPrimaryNicks, joinedPrimaryNicks));
 
         // when the first element is the own nick, this bot is primary.
         int index = joinedPrimaryNicks.indexOf(myNick);
         boolean isPrimary = (index == 0);
        boolean wasPrimary = (primaryStatus.containsKey(channel) ? primaryStatus.get(channel) : false);
         if (LOG.isDebugEnabled()) LOG.debug(String.format("checking: %s in %s %s (primary:%s)", myNick, channel, joinedPrimaryNicks, isPrimary));
         if (isPrimary && !wasPrimary) {
             String message = MessageUtils.getMessage("core.GircBot.isPrimary.active");
             ircControl.sendMessage(channel, message);
             LOG.info(String.format("%s: %s in %s %s", message, myNick, channel, joinedPrimaryNicks));
            primaryStatus.put(channel, true);
         }
         else if (!isPrimary && wasPrimary) {
             String message = MessageUtils.getMessage("core.GircBot.isPrimary.standby");
             ircControl.sendMessage(channel, message);
             LOG.info(String.format("%s: %s in %s %s", message, myNick, channel, joinedPrimaryNicks));
            primaryStatus.put(channel, false);
         }
         else {
             assert (isPrimary == wasPrimary) : "Status is not changed";
         }
         return isPrimary;
     }
 
     protected List<String> getJoinedNicks(String channel) {
         List<String> nicks = new ArrayList<String>();
         for (User user : ircControl.getUsers(channel)) {
             nicks.add(user.getNick());
         }
         return nicks;
     }
 
     public boolean isPrimaryGlobally() {
         return isPrimary(defaultChannel);
     }
 }
