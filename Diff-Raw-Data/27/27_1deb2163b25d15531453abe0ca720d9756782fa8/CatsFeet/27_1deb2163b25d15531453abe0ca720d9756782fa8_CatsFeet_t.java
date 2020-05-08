 package com.gmail.ne0nx3r0.rareitems.item.spells;
 
 import com.gmail.ne0nx3r0.rareitems.RareItems;
 import com.gmail.ne0nx3r0.rareitems.item.ItemProperty;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class CatsFeet extends ItemProperty
 {
     public CatsFeet()
     {
         super(40,"Cat's Feet","Lets you or a clicked target jump much higher for 60 seconds / lvl",4);
     }
     
     @Override
     public boolean onInteract(PlayerInteractEvent e,int level)
     {
         e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP,20*60*level,1*level));
         
         e.getPlayer().sendMessage("You can jump higher!");
            
        RareItems.ipm.addTemporaryEffect(e.getPlayer().getName(),this,level,20*60*level);
         
         return true;
     }    
     
     @Override
     public boolean onInteractEntity(PlayerInteractEntityEvent e, int level)
     {
         if(e.getRightClicked() instanceof LivingEntity)
         {
             int duration = 20*60*level;
         
             LivingEntity le = (LivingEntity) e.getRightClicked();
 
             le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,duration,level));
                     
             if(le instanceof Player)
             {
                 e.getPlayer().sendMessage("You cast Cat's Feet on "+((Player) le).getName()+"!");
                 ((Player) le).sendMessage(e.getPlayer().getName()+" cast Cat's Feet on you!");
             }
             else
             {
                 e.getPlayer().sendMessage("You cast Cat's Feet on that thing!");
             }
             
            RareItems.ipm.addTemporaryEffect(((Player) le).getName(),this,level,duration);
             
             return true;
         }
         return false;
     }
 }
