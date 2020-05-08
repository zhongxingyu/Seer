 package org.ucam.srcf.assassins.storage.hibernate;
 
 /**
  * Converter between protos and hibernate entities.
  * @author Mike Cripps <scotsman@cantab.net>
  */
final class StateChangeConverter {

  private StateChangeConverter() {
  }
 
   public static StateChange convertProtoToStorage(org.ucam.srcf.assassins.proto.StateChange stateChange) {
     StateChange change = constructStateChange(stateChange);
     if (stateChange.hasId()) {
       change.setId(stateChange.getId());
     }
     return change;
   }
 
   private static StateChange constructStateChange(
       org.ucam.srcf.assassins.proto.StateChange stateChange) {
     switch (stateChange.getType()) {
     case ADD_PLAYER:
       return convertAddPlayer(stateChange.getAddPlayer());
     case KILL:
       return convertKill(stateChange.getKill());
     default:
       throw new IllegalArgumentException("Unrecognised StateChange type - " + stateChange.getType());
     }
   }
 
   private static AddPlayer convertAddPlayer(org.ucam.srcf.assassins.proto.AddPlayer addPlayer) {
     AddPlayer domainAddPlayer = new AddPlayer();
     domainAddPlayer.setPlayer(PlayerConverter.convertProtoToStorage(addPlayer.getPlayer()));
     return domainAddPlayer;
   }
 
   private static org.ucam.srcf.assassins.proto.AddPlayer convertAddPlayer(AddPlayer addPlayer) {
     return org.ucam.srcf.assassins.proto.AddPlayer.newBuilder()
         .setPlayer(PlayerConverter.convertStorageToProto(addPlayer.getPlayer()))
         .build();
   }
 
   private static Kill convertKill(org.ucam.srcf.assassins.proto.Kill kill) {
     Kill domainKill = new Kill();
     domainKill.setKiller(PlayerConverter.convertProtoToStorage(kill.getKiller()));
     domainKill.setVictim(PlayerConverter.convertProtoToStorage(kill.getVictim()));
     return domainKill;
   }
 
   private static org.ucam.srcf.assassins.proto.Kill convertKill(Kill stateChange) {
     return org.ucam.srcf.assassins.proto.Kill.newBuilder()
         .setKiller(PlayerConverter.convertStorageToProto(stateChange.getKiller()))
         .setVictim(PlayerConverter.convertStorageToProto(stateChange.getVictim()))
         .build();
   }
 
   public static org.ucam.srcf.assassins.proto.StateChange convertStorageToProto(StateChange stateChange) {
     org.ucam.srcf.assassins.proto.StateChange.Builder stateChangeBuilder = org.ucam.srcf.assassins.proto.StateChange.newBuilder();
     stateChangeBuilder.setId(stateChange.getId());
     if (stateChange instanceof Kill) {
       stateChangeBuilder.setType(org.ucam.srcf.assassins.proto.StateChange.Type.KILL);
       stateChangeBuilder.setKill(convertKill((Kill) stateChange));
     } else if (stateChange instanceof AddPlayer) {
       stateChangeBuilder.setType(org.ucam.srcf.assassins.proto.StateChange.Type.ADD_PLAYER);
       stateChangeBuilder.setAddPlayer(convertAddPlayer((AddPlayer) stateChange));
     }
     return stateChangeBuilder.build();
   }
 }
