 package network.packets.clientPackets.impl;
 
 import manager.RoomsManager;
 import network.packets.clientPackets.BaseClientPacket;
 
 /**
  * Created with IntelliJ IDEA.
  *
  * @author : sergiyloza
  * @date : 18/03/2013
  * @time : 15:40
  */
 public class RequestCreateRoom extends BaseClientPacket {
 
     private String roomName;
     private int creatorId;
 
     @Override
     public void readImpl() {
         creatorId = readInt();
         roomName = readString();
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void runImpl() {
        RoomsManager.getInstance().createNewRoom(roomName, getClient());
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
