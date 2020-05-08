 package directi.androidteam.training.chatclient.Roster;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ssumit
  * Date: 9/12/12
  * Time: 3:57 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RosterEntry {
     public String jid;
     public ArrayList<RosterGroup> rosterGroups;
     public String status;
     public String presence;
 
     public RosterEntry(String jid) {
         this.jid = jid;
         rosterGroups = null;
         status="";
         presence="";
         RosterGroupManager rosterGroupManager = RosterGroupManager.getInstance();
         RosterGroup rosterGroup = rosterGroupManager.getRosterGroupByName("default");
         this.addRosterGroup(rosterGroup);
     }
 
     public RosterEntry(String jid, ArrayList<RosterGroup> rosterGroups, String status, String presence) {
         this.jid = jid;
         this.rosterGroups = rosterGroups;
         this.status = status;
         this.presence = presence;
     }
 
     public String getJid() {
         return jid;
     }
 
     public ArrayList<RosterGroup> getRosterGroups() {
         return rosterGroups;
     }
 
     public String getStatus() {
         return status;
     }
 
     public String getPresence() {
         return presence;
     }
 
     public void setRosterGroups(ArrayList<RosterGroup> rosterGroups) {
         this.rosterGroups = rosterGroups;
     }
 
     public void setStatus(String status) {
         this.status = status;
     }
 
     public void setPresence(String presence) {
         this.presence = presence;
     }
 
     public void addRosterGroup(RosterGroup rosterGroup) {
         if(rosterGroups==null)
             rosterGroups = new ArrayList<RosterGroup>();
        if(rosterGroups.contains(rosterGroup))
            return;
         rosterGroups.add(rosterGroup);
        rosterGroup.addRosterEntry(this);
     }
 }
