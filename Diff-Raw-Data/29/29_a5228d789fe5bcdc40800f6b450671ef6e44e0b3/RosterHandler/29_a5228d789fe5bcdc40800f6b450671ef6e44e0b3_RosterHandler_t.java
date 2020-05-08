 package directi.androidteam.training.chatclient.PacketHandlers;
 
 import android.util.Log;
 import directi.androidteam.training.StanzaStore.PresenceS;
 import directi.androidteam.training.StanzaStore.RosterPush;
 import directi.androidteam.training.StanzaStore.RosterResult;
 import directi.androidteam.training.TagStore.Tag;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ssumit
  * Date: 9/7/12
  * Time: 2:03 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RosterHandler implements Handler{
     private static RosterHandler rosterHandler = null;
 
     private RosterHandler() {
     }
     public static RosterHandler getInstance() {
         if(rosterHandler==null){
             rosterHandler = new RosterHandler();
             return rosterHandler;
         }
         return rosterHandler;
     }
 
     @Override
     public void processPacket(Tag tag) {
         String tagName = tag.getTagname();
         Log.d("packet - name ",tag.getTagname());
         if (tagName.equals("iq")){
             String type = tag.getAttribute("type");
             if (type.equals("get")){
                 Log.d("Packet Error","It is RosterManager Get Packet. This packet is always sent from Client side!");
             }
             else if(type.equals("set")){
                 Log.d("Packet ACK","RosterManager Push Packet.");
                 RosterPush rosterPush = new RosterPush(tag);
                 String jid = rosterPush.getJID();
             }
             else if (type.equals("error")){
                 Log.d("Packet Error","Error Reported From Server Side possibly due to RosterManager Result");
             }
             else if(type.equals("result")){
                 RosterResult rosterResult = new RosterResult(tag);
         //       RosterManager rosterManager = RosterManager.getInstance();
       //         rosterManager.setRosterList(rosterResult.getListOfRosters());
             }
             else {
                 Log.d("Packet Error","Unidentified IQ Packet, type = "+type);
             }
         }
         else if(tagName.equals("presence")){
             PresenceS presence = new PresenceS();
             String type = presence.getType();
             if(type.equals("error")){
                 Log.d("Packet Error","Error From Server Side In Presence Packet");
             }
             else {
                 Log.d("Packet ACK","Presence Packet");
             }
         }
         else {
             Log.d("Packet Error","Unidentified Packet, tagname = "+tagName);
         }
     }
 }
