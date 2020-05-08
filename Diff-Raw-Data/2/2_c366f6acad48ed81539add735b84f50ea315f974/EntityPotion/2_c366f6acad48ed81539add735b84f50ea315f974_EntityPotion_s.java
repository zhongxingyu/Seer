 package io.github.harryprotist.block;
 
 import org.bukkit.entity.Player;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.World;
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.util.Vector;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.potion.PotionEffect;
 
 import java.util.*;
 
 import io.github.harryprotist.Spell;
 
 public class EntityPotion extends EntityFunction
 {
 	public final static int ARGC = 2;
 
 	private Material pm; // the material of arg1
 	private boolean instant;
 	
 	private static Map<Material, Object[]> btp; // block (value) to potion
 	static {
 		btp = new HashMap<Material, Object[]>(); 
 		// this is kinda dumb, yeah
 		// I'll move it to a file I swear, maybe
 
 		// 		Material				Effect					Init Cost,   	  Cost/s
 		btp.put(Material.GLASS,			new Object[]{ PotionEffectType.SPEED, 			new Integer(1000), new Integer(10)});	// Speed
 		btp.put(Material.SOUL_SAND,		new Object[]{ PotionEffectType.SLOW,			new Integer(2000), new Integer(10)});	// Slowness	
 		btp.put(Material.IRON_BLOCK,		new Object[]{ PotionEffectType.FAST_DIGGING,		new Integer(10000), new Integer(10)});	// Haste
 		btp.put(Material.DIRT,			new Object[]{ PotionEffectType.SLOW_DIGGING,		new Integer(2000), new Integer(10)});	// Fatigue		
 		btp.put(Material.LOG,			new Object[]{ PotionEffectType.INCREASE_DAMAGE,		new Integer(10000), new Integer(25)});	// Strength
 		btp.put(Material.GOLD_BLOCK,		new Object[]{ PotionEffectType.HEAL,			new Integer(20000), new Integer(0)});	// Healing
 		btp.put(Material.TNT,			new Object[]{ PotionEffectType.HARM,			new Integer(40000), new Integer(0)});	// Harming	
 		btp.put(Material.LEAVES,		new Object[]{ PotionEffectType.JUMP,			new Integer(1000), new Integer(5)});	// Jump Boost
 		btp.put(Material.MOSSY_COBBLESTONE,	new Object[]{ PotionEffectType.CONFUSION,		new Integer(10000), new Integer(40)});	// Nasea	
 		btp.put(Material.GLOWSTONE,		new Object[]{ PotionEffectType.REGENERATION,		new Integer(50000), new Integer(100)});	// Regeneration
 		btp.put(Material.OBSIDIAN,		new Object[]{ PotionEffectType.DAMAGE_RESISTANCE,	new Integer(25000), new Integer(25)});	// Resistance
 		btp.put(Material.NETHERRACK,		new Object[]{ PotionEffectType.FIRE_RESISTANCE,		new Integer(10000), new Integer(10)});	// Fire Resist
 		btp.put(Material.ICE,			new Object[]{ PotionEffectType.WATER_BREATHING,		new Integer(10000), new Integer(10)});	// Water Breathe
 		btp.put(Material.THIN_GLASS,		new Object[]{ PotionEffectType.INVISIBILITY,		new Integer(9000), new Integer(5)});	// Invisibility
 		btp.put(Material.PUMPKIN,		new Object[]{ PotionEffectType.BLINDNESS,		new Integer(8000), new Integer(30)});	// Blindness
 		btp.put(Material.JACK_O_LANTERN,	new Object[]{ PotionEffectType.NIGHT_VISION,		new Integer(1000), new Integer(5)});	// Night Vision	
 		btp.put(Material.MELON,			new Object[]{ PotionEffectType.HUNGER,			new Integer(1000), new Integer(10)});	// Hunger
 		btp.put(Material.CLAY,			new Object[]{ PotionEffectType.WEAKNESS,		new Integer(3000), new Integer(10)});	// Weakness	
 		btp.put(Material.WEB,			new Object[]{ PotionEffectType.POISON,			new Integer(12000), new Integer(10)});	// Poison	
 		btp.put(Material.ENDER_STONE,		new Object[]{ PotionEffectType.WITHER,			new Integer(10000), new Integer(100)});	// Wither
 	}
 
 	public EntityPotion(ArrayList<Integer> a, Player c, Location l, ArrayList<Entity> eL) {
 		super(a, c, l, eL);	
 	
 		instant = false;
 		pm = Material.AIR;
 	}
 
 	public boolean isValid() {
 
 		if (!(argv.size() < 1)) {
 			pm = Spell.getValueMaterial(argv.get(0));
			if (pm == null) return false;
 
 			PotionEffectType pe = (PotionEffectType)(btp.get(pm)[0]);
 			instant = pe.isInstant();
 			//caster.sendMessage("valid instance of " + pe.getName());
 			
 			return ((argv.size() == 1 && instant) || (argv.size() == ARGC));
 		}
 		return false;
 	}
 	public int getManaCost() {
 
 		int seconds = instant? 0:(argv.get(1));
 		return ( ((Integer)(btp.get(pm)[1])).intValue() + ((Integer)(btp.get(pm)[2])).intValue() * seconds) * entList.size();
 	}
 	public void runFunction() {
 		
 		PotionEffectType pet = (PotionEffectType)(btp.get(pm)[0]);
 		if (pet == null) return;
 		
 		PotionEffect pe = new PotionEffect(pet, (instant? 1:(argv.get(1).intValue() * 20)), 1);
 		for (Entity e : entList) {
 			if (e instanceof LivingEntity) {
 				//caster.sendMessage("found an entity, applying " + pet.getName() + " for " + (instant? 1:(argv.get(1))) + " seconds");
 				pe.apply((LivingEntity)e);
 			}
 		}
 	}
 }
