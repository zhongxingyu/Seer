 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashSet;
 import net.minecraft.server.EntityHuman;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillTelekinesis extends ActiveSkill {
 
     public SkillTelekinesis(Heroes plugin) {
         super(plugin, "Telekinesis");
         setDescription("Activate levers, buttons and other interactable objects from afar!");
         setUsage("/skill telekinesis");
         setArgumentRange(0, 0);
         setIdentifiers(new String[]{"skill telekinesis"});
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         HashSet<Byte> transparent = new HashSet<Byte>();
         transparent.add((byte) Material.AIR.getId());
         transparent.add((byte) Material.WATER.getId());
        transparent.add((byte) Material.REDSTONE_TORCH_ON.getId());
        transparent.add((byte) Material.REDSTONE_TORCH_OFF.getId());
        transparent.add((byte) Material.REDSTONE_WIRE.getId());
        transparent.add((byte) Material.TORCH.getId());
        transparent.add((byte) Material.SNOW.getId());
        Block block = player.getTargetBlock(transparent, 15);
         if (block.getType() == Material.LEVER || block.getType() == Material.STONE_BUTTON) {
             EntityHuman eH = ((CraftPlayer) player).getHandle();
             //Can't adjust levers/Buttons through CB 
             net.minecraft.server.Block.byId[block.getTypeId()].interact(((CraftWorld)block.getWorld()).getHandle(), block.getX(), block.getY(), block.getZ(), eH);
            //In Case Bukkit eaver fixes blockState changes on levers:
             //Lever lever = (Lever) block.getState().getData();
             //lever.setPowered(!lever.isPowered());
             //block.getState().update();
             broadcastExecuteText(hero);
             return true;
         } 

         Messaging.send(player, "You must target a lever or button");
         return false;
     }
 
 }
