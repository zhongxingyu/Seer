 package directi.androidteam.training.chatclient.Roster;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 
 /**
  * Created with IntelliJ IDEA.
  * User: rajat
  * Date: 10/9/12
  * Time: 9:53 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Roster {
     private ArrayList<RosterItem> roster;
     private Comparator comparator;
 
     public Roster(Comparator comparator) {
         this.comparator = comparator;
         this.roster = new ArrayList<RosterItem>();
     }
 
     public ArrayList<RosterItem> getRoster() {
         return this.roster;
     }
 
     public RosterItem searchRosterItem(String accountUID, String bareJID) {
         for (RosterItem rosterItem : this.roster) {
             if (rosterItem.getAccount().equals(accountUID) && rosterItem.getBareJID().equals(bareJID)) {
                 return rosterItem;
             }
         }
         return null;
     }
 
     private void deleteRosterItem(String accountUID, String bareJID) {
         int i = 0;
         for (i = 0; i < this.roster.size(); i++) {
             if (this.roster.get(i).getAccount().equals(accountUID) && this.roster.get(i).getBareJID().equals(bareJID)) {
                 break;
             }
         }
         this.roster.remove(i);
     }
 
     public void insertRosterItem(RosterItem rosterItem) {
         if (this.searchRosterItem(rosterItem.getAccount(), rosterItem.getBareJID()) != null) {
             this.deleteRosterItem(rosterItem.getAccount(), rosterItem.getBareJID());
         }
         int i = 0;
         while (i < this.roster.size() && this.comparator.compare(this.roster.get(i), rosterItem) < 0) {
             i++;
         }
         while (i < this.roster.size()) {
             rosterItem = this.roster.set(i, rosterItem);
             i++;
         }
         this.roster.add(rosterItem);
     }
 
     public void deleteRosterItemsWithAccount(String accountUID) {
         int i = 0;
         ArrayList<RosterItem> newRoster = new ArrayList<RosterItem>();
         for (i = 0; i < this.roster.size(); i++) {
             if (!(this.roster.get(i).getAccount().equals(accountUID))) {
                 newRoster.add(this.roster.get(i));
             }
         }
         this.roster = newRoster;
     }
 }
