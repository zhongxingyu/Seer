 package rekkyn.spacetime.handlers;
 
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.passive.EntitySheep;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.PotionEffect;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import net.minecraftforge.event.entity.player.AttackEntityEvent;
 import rekkyn.spacetime.Spacetime;
 
 public class EventHandler {
     
     @ForgeSubscribe
     public void DamageCheck(AttackEntityEvent event) {
         if (event.target instanceof EntityPlayer) {
             EntityPlayer player = event.entityPlayer;
             EntityPlayer target = (EntityPlayer) event.target;
             if (player.getCurrentEquippedItem() != null
                     && player.getCurrentEquippedItem().itemID == Spacetime.ironRod.itemID) {
                 if (target.username.equals("epic_jdog")) {
                     float pitch = (float) Math.pow(2.0D, (6 - 12) / 12.0D);
                     target.worldObj.playSoundEffect(target.posX, target.posY, target.posZ, "note.harp", 1.0F, pitch);
                     pitch = (float) Math.pow(2.0D, 10 / 12.0D);
                     target.worldObj.playSoundEffect(target.posX, target.posY, target.posZ, "note.harp", 1.0F, pitch);
                     pitch = (float) Math.pow(2.0D, (13 - 12) / 12.0D);
                     target.worldObj.playSoundEffect(target.posX, target.posY, target.posZ, "note.harp", 1.0F, pitch);
                     pitch = (float) Math.pow(2.0D, (12 - 12) / 12.0D);
                     target.worldObj.playSoundEffect(target.posX, target.posY, target.posZ, "note.harp", 1.0F, pitch);
                     pitch = (float) Math.pow(2.0D, (15 - 12) / 12.0D);
                     target.worldObj.playSoundEffect(target.posX, target.posY, target.posZ, "note.harp", 1.0F, pitch);
                     pitch = (float) Math.pow(2.0D, (20 - 12) / 12.0D);
                     target.worldObj.playSoundEffect(target.posX, target.posY, target.posZ, "note.harp", 1.0F, pitch);
                 } else if (target.username.equals("p_slice")) {
                     for (int lmnop = 0; lmnop < 16; lmnop++) {
                         EntityItem item = new EntityItem(target.worldObj, target.posX + (Math.random() - 0.5D) * 2,
                                 target.posY + target.getEyeHeight(), target.posZ + (Math.random() - 0.5D) * 2,
                                 new ItemStack(Item.potato));
                         item.delayBeforeCanPickup = 200;
                         item.lifespan = 100;
                         item.motionX = (float) (Math.random() * 0.40000000298023224D - 0.20000000149011612D);
                         item.motionY = 0.40000000298023224D;
                         item.motionZ = (float) (Math.random() * 0.40000000298023224D - 0.20000000149011612D);
                         System.out.println(item.motionX);
                         
                         if (!target.worldObj.isRemote) {
                             target.worldObj.spawnEntityInWorld(item);
                         }
                     }
                 } else if (target.username.equals("epic_lion")) {
                    EntitySheep sheep = new EntitySheep(event.target.worldObj);
                    sheep.setPosition(event.target.posX, event.target.posY, event.target.posZ);
                    sheep.motionX = (float) (Math.random() * 0.40000000298023224D - 0.20000000149011612D);
                    sheep.motionY = 1;
                    sheep.motionZ = (float) (Math.random() * 0.40000000298023224D - 0.20000000149011612D);
                    sheep.setSheared(true);
                    sheep.setEntityHealth(1);
                    sheep.addPotionEffect(new PotionEffect(20, 100));
                    if (!event.target.worldObj.isRemote) {
                        event.target.worldObj.spawnEntityInWorld(sheep);
                    }
                 }
             }
         }
     }
     
     @ForgeSubscribe
     public void DeathCheck(LivingDeathEvent event) {
         if (event.source.damageType == "player") {
             EntityPlayer player = (EntityPlayer) event.source.getEntity();
             if (player.getCurrentEquippedItem() != null) {
                 if (player.getCurrentEquippedItem().getItem() == Spacetime.spacetimeSword) {
                     SpacetimeChargeHandler.addChargeToTotal(player, (int) (event.entityLiving.experienceValue / 0.005));
                 }
             }
         }
     }
     
 }
