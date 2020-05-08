 package directi.androidteam.training.chatclient.Roster;
 
 import android.util.Log;
 import directi.androidteam.training.StanzaStore.PresenceS;
 import directi.androidteam.training.TagStore.Tag;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ssumit
  * Date: 9/10/12
  * Time: 12:32 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RosterManager {
     public ArrayList<Tag> rosterList  = new ArrayList<Tag>();
     public static final RosterManager ROSTER_MANAGER = new RosterManager();
     private RosterManager() {
     }
     public static RosterManager getInstance(){
         return ROSTER_MANAGER;
     }
 
     public void setRosterList(ArrayList<Tag> list){
         rosterList = list;
         for (Tag tag : list) {
             Log.d("setRoster tagname :",tag.getTagname());
             if(tag.getTagname().equals("item")){
                new RosterEntry(tag.getAttribute("jid"));
                 Log.d("setRoster : ","hey");
             }
         }
         DisplayRosterActivity.showAllRosters();
     }
     public ArrayList getRosterList(){
         return rosterList;
     }
     public void displayRoster(String groupName){
         Log.d("setRoster : ","displayRoster");
         RosterGroupManager rosterGroupManager = RosterGroupManager.getInstance();
         RosterGroup rosterGroup = rosterGroupManager.getRosterGroupByName(groupName);
         ArrayList<RosterEntry> rosterEntries =  rosterGroup.getRosterEntries();
         for (RosterEntry rosterEntry : rosterEntries) {
             Log.d("JID :",rosterEntry.getJid());
         }
     }
     public void addRosterEntry(Map<String,Object> rosterEntry){
         ;//RosterSet rosterSet = new RosterSet(JID.jid,,);
     }
     public void sendMyPresence(){
         PresenceS presence = new PresenceS();
     }
 }
