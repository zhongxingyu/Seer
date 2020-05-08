 package directi.androidteam.training.chatclient.Roster;
 
 import directi.androidteam.training.TagStore.Presence;
 import directi.androidteam.training.TagStore.Tag;
 
 public class RosterManager {
     private Roster roster = new Roster(new LexicalCumPresenceComparator());
     private static RosterManager rosterManager = new RosterManager();
 
     public static RosterManager getInstance() {
         return rosterManager;
     }
 
     public void setRosterList(Tag rosterResult) {
         for (Tag tag : rosterResult.getChildTag("query").getChildTags()) {
             if (tag.getAttribute("subscription").equals("both")) {
                 RosterItem rosterItem = new RosterItem();
                 rosterItem.setBareJID(tag.getAttribute("jid"));
                 rosterItem.setPresence("unavailable");
                 rosterItem.setStatus("");
                 rosterItem.setVCard(new VCard());
                 this.roster.insertRosterItem(rosterItem);
             }
         }
         RequestRoster.callerActivity.runOnUiThread(new Runnable() {
             public void run() {
                 ((DisplayRosterActivity) RequestRoster.callerActivity).updateRosterList(roster.getRoster());
             }
         });
     }
 
     public void updatePresence(Presence presence) {
         RosterItem rosterItem = this.roster.searchRosterItem(presence.getFrom().split("/")[0]);
         if (rosterItem == null) {return;}
         rosterItem.setStatus(presence.getStatus());
         if (presence.getShow() == null) {
             rosterItem.setPresence("chat");
         } else {
             rosterItem.setPresence(presence.getShow());
         }
         this.roster.insertRosterItem(rosterItem);
         SendPresence.callerActivity.runOnUiThread(new Runnable() {
             public void run() {
                 ((DisplayRosterActivity) SendPresence.callerActivity).updateRosterList(roster.getRoster());
             }
         });
     }
 
     public void updatePhoto(VCard vCard, String from) {
         RosterItem rosterItem = this.roster.searchRosterItem(from);
         if (rosterItem == null) {return;}
         if (vCard.getAvatar() != null) {
             rosterItem.setVCard(vCard);
         }
         SendPresence.callerActivity.runOnUiThread(new Runnable() {
             public void run() {
                 ((DisplayRosterActivity) SendPresence.callerActivity).updateRosterList(roster.getRoster());
             }
         });
     }
 
     public RosterItem getRosterItem(String bareJID) {
         return roster.searchRosterItem(bareJID);
     }

    public void clearRoster() {
        this.roster = new Roster(new LexicalCumPresenceComparator());
    }
 }
