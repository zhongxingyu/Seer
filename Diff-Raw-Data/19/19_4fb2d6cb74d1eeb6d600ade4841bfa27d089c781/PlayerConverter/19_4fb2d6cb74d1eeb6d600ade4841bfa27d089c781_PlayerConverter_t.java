 package org.ucam.srcf.assassins.storage.hibernate;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.BiMap;
 import com.google.common.collect.EnumBiMap;
 
final class PlayerConverter {
   private static BiMap<WaterWeaponStatus, org.ucam.srcf.assassins.proto.Player.WaterWeaponStatus> waterWeaponStatus;
 
   static {
     waterWeaponStatus = EnumBiMap.create(WaterWeaponStatus.class, org.ucam.srcf.assassins.proto.Player.WaterWeaponStatus.class);
     waterWeaponStatus.put(WaterWeaponStatus.NO_WATER, org.ucam.srcf.assassins.proto.Player.WaterWeaponStatus.NO_WATER);
     waterWeaponStatus.put(WaterWeaponStatus.WATER_WITH_CARE, org.ucam.srcf.assassins.proto.Player.WaterWeaponStatus.WATER_WITH_CARE);
     waterWeaponStatus.put(WaterWeaponStatus.FULL_WATER, org.ucam.srcf.assassins.proto.Player.WaterWeaponStatus.FULL_WATER);
   }
 
   private PlayerConverter() {
   }
 
   static Player convertProtoToStorage(org.ucam.srcf.assassins.proto.Player player) {
     Preconditions.checkNotNull(player);
     Player domainPlayer = new Player();
     if (player.hasId()) {
       domainPlayer.setId(player.getId());
     }
     domainPlayer.setName(player.getName());
     domainPlayer.setWaterWeaponStatus(waterWeaponStatus.inverse().get(player.getWaterWeaponStatus()));
     return domainPlayer;
   }
 
   static org.ucam.srcf.assassins.proto.Player convertStorageToProto(Player player) {
     Preconditions.checkNotNull(player);
     org.ucam.srcf.assassins.proto.Player.Builder playerBuilder = org.ucam.srcf.assassins.proto.Player.newBuilder();
 
     playerBuilder.setId(player.getId());
     playerBuilder.setName(player.getName());
     playerBuilder.setWaterWeaponStatus(waterWeaponStatus.get(player.getWaterWeaponStatus()));
 
     return playerBuilder.build();
   }
 }
