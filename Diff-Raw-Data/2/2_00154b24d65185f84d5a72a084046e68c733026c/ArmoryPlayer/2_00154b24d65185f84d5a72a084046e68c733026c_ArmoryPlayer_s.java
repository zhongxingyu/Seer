package newTeam.player.building.factory;
 
 import battlecode.common.*;
 import newTeam.common.*;
 import newTeam.common.util.Logger;
 import newTeam.player.BasePlayer;
 import newTeam.player.building.recycler.RecyclerCommPlayer;
 import newTeam.player.building.BuildingPlayer;
 import newTeam.state.BaseState;
 import newTeam.state.idle.Idling;
 import newTeam.state.factory.*;
 
 public class ArmoryPlayer extends BuildingPlayer {
 
     boolean hasRecycler;
 
     public ArmoryPlayer(BaseState state) {
         super(state);
 
         hasRecycler = false;
     }
 
     @Override
     public BaseState determineNewStateBasedOnNewSpecificPlayer(BaseState oldState) {
         return new BuildingDishOnSelf( oldState );
     }
 
     @Override
     public void initialize() {
     }
 
     @Override
     public void doSpecificPlayerStatelessActions() {
         super.doSpecificPlayerStatelessActions();
 
         Message[] messages = myCH.myBCH.receiveMessages();
 
         for( Message message : messages )
         {
 
             if( !hasRecycler && MessageCoder.getMessageType(message).equals(MessageCoder.RECYCLER_PING) )//&& MessageCoder.isValid(message) )
             {
                 myK.myRecyclerNode = RecyclerNode.getFromPing(message);
                 hasRecycler = true;
                 System.out.println("synced with recycler ID: " + myK.myRecyclerNode.myRobotID);
             }
         }
 
         if( Clock.getRoundNum() % QuantumConstants.PING_CYCLE_LENGTH == 1 && myCH.myBCH.canBroadcast() )
         {
                 myCH.myBCH.addToQueue( generateFactoryPing() );
         }
 
         myCH.myBCH.broadcastFromQueue();
 
     }
 
     @Override
     public BasePlayer determineSpecificPlayerGivenNewComponent(ComponentType compType,
                                                                BaseState state) {
 
         return this;
 
     }
 
     public Message generateFactoryPing()
     {
         return MessageCoder.encodeMessage(MessageCoder.FACTORY_PING, myRC.getRobot().getID(), myRC.getLocation(), Clock.getRoundNum(), false, null, null, null);
     }
 
 }
