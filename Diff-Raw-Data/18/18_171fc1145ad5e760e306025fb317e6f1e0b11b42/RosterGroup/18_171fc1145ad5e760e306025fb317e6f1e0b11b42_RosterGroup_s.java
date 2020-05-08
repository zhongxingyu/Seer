 package directi.androidteam.training.chatclient.Roster;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ssumit
  * Date: 9/12/12
  * Time: 3:59 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RosterGroup {
     private String groupName;
     private ArrayList<RosterEntry> rosterEntries;
 
     public RosterGroup(String groupName, ArrayList<RosterEntry> rosterEntries) {
         this.groupName = groupName;
         this.rosterEntries = rosterEntries;
     }
 
     public RosterGroup(String groupName) {
         this.groupName = groupName;
         rosterEntries = new ArrayList<RosterEntry>();
     }
 
     public String getGroupName() {
         return groupName;
     }
 
     public void setGroupName(String groupName) {
         this.groupName = groupName;
     }
 
     public ArrayList<RosterEntry> getRosterEntries() {
         return rosterEntries;
     }
     public void addRosterEntry(RosterEntry rosterEntry){
         if (rosterEntries==null)
             rosterEntries = new ArrayList<RosterEntry>();
         rosterEntries.add(rosterEntry);
         rosterEntry.addRosterGroup(this);
     }
 }
