 package jk_5.nailed.map.teleport;
 
 import cpw.mods.fml.common.eventhandler.SubscribeEvent;
 import jk_5.nailed.network.NailedNetworkHandler;
 import jk_5.nailed.network.NailedPacket;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.item.EntityItem;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class TeleportEventListenerEffect {
 
     @SubscribeEvent
     public void onTeleportStart(TeleportEvent.TeleportEventStart event){
         NailedNetworkHandler.sendPacketToAllPlayersInDimension(new NailedPacket.Particle(event.entity.posX, event.entity.posY, event.entity.posZ, "teleport"), event.entity.dimension);
         playSound(event.entity, event.options);
     }
 
     @SubscribeEvent
     public void onTeleportEnd(TeleportEvent.TeleportEventEnd event){
         NailedNetworkHandler.sendPacketToAllPlayersInDimension(new NailedPacket.Particle(event.entity.posX, event.entity.posY, event.entity.posZ, "teleport"), event.entity.dimension);
         playSound(event.entity, event.options);
     }
 
     private static void playSound(Entity entity, TeleportOptions options){
        if(entity instanceof EntityItem){
             entity.worldObj.playSoundAtEntity(entity, "nailed:teleport.pop", 0.8F, entity.worldObj.rand.nextFloat() * 0.2F + 0.9F);
        }else if(options.getSound() != null && !options.getSound().isEmpty()){
            entity.worldObj.playSoundAtEntity(entity, options.getSound(), 0.8F, entity.worldObj.rand.nextFloat() * 0.2F + 0.9F);
         }else{
             entity.worldObj.playSoundAtEntity(entity, "nailed:teleport", 0.8F, entity.worldObj.rand.nextFloat() * 0.2F + 0.9F);
         }
     }
 }
