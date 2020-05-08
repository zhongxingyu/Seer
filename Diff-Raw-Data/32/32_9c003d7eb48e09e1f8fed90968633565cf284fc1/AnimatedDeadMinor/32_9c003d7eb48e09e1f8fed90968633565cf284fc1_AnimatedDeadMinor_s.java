 package TotalWizard.Handlers.Magic.Spells.Summons;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.v1_6_R2.entity.CraftZombie;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Zombie;
 import org.bukkit.material.MaterialData;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.entity.LivingEntity;
 
 import TotalWizard.Handlers.Magic.Spells.Spell;
 
 public class AnimatedDeadMinor extends Spell
 {
 
 	@Override
 	public String getName()
 	{
 		return "Summon Zombieling";
 	}
 
 	@Override
 	public boolean Cast(Player Player)
 	{
 		Location Pointed = Player.getTargetBlock(null, 30).getLocation();
 		CraftZombie Zombie = (CraftZombie)((LivingEntity)Pointed.getWorld().spawnEntity(Pointed, EntityType.ZOMBIE));
 		Zombie.setBaby(true);
 		Zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,200,1));
 		for(LivingEntity E : this.getEntitiesNearLocation(Pointed, 5))
 		{
 			if (!E.hasMetadata("NPC"))
 			{
 				if (E instanceof Player && !((Player)E).getName().equalsIgnoreCase(Player.getName()))
 				{
 					Zombie.setTarget(E);
 					return true;
 				}
 				else
 				{
					Zombie.setTarget(E);
					return true;
 				}
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public int getLevelRequirement()
 	{
 		return 55;
 	}
 
 	@Override
 	public int getManaRequirement()
 	{
 		return 160;
 	}
 
 	@Override
 	public int getCastExp()
 	{
 		return 108;
 	}
 
 	@Override
 	public String[] getDescription()
 	{
 		return new String[] {ChatColor.YELLOW + "Summon a baby zombie and send it",ChatColor.YELLOW + "to attack the nearest foe",ChatColor.RED + "Requires level 55 magic, costs 160 mana" };
 	}
 
 	@Override
 	public MaterialData getMaterialData()
 	{
 		return new MaterialData(Material.SKULL_ITEM,(byte)2);
 	}
 
 }
