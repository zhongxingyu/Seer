 package com.ainast.morepowerfulmobsreloaded;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.MidiUnavailableException;
 import javax.sound.midi.Sequence;
 
 import me.egordm.simpleattributes.API.SimpleAttributesAPI;
 import me.egordm.simpleattributes.Attributes.AttributeType;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryAction;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.herocraftonline.heroes.api.events.CharacterDamageEvent;
 import com.herocraftonline.heroes.api.events.ExperienceChangeEvent;
 import com.herocraftonline.heroes.api.events.HeroEnterCombatEvent;
 import com.herocraftonline.heroes.api.events.HeroKillCharacterEvent;
 import com.herocraftonline.heroes.api.events.HeroRegainHealthEvent;
 import com.herocraftonline.heroes.api.events.HeroRegainManaEvent;
 import com.herocraftonline.heroes.api.events.SkillDamageEvent;
 import com.herocraftonline.heroes.api.events.SkillUseEvent;
 import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.skill.SkillType;
 
 public class PlayerEvents implements Listener{
 	HashMap<Player, List<ItemStack>> itemsToReadd = new HashMap<Player, List<ItemStack>>();
 	
 	@EventHandler
 	public void onPlayerJoinServerEvent(PlayerJoinEvent event){
 		Player player = event.getPlayer();
 		Hero hero = MPMTools.getHeroes().getCharacterManager().getHero(player);
 	
 		hero.clearMaxMana();
 		hero.clearHealthBonuses();
 		hero.resetMaxHP();
 		
 		if (MPMTools.playerAttributes.containsKey(player)) MPMTools.playerAttributes.get(player).clear();
 	}
 	
 	@EventHandler
 	public void onPlayerMoveEvent(PlayerMoveEvent event){
 		Player player = event.getPlayer();
 		if (MPMTools.playerAttributes.containsKey(player)){
 			HashMap<String, Long> attributes = MPMTools.playerAttributes.get(player);
 			if (attributes.containsKey(MPMAttributeType.FLOWER_CHILD)){
 				int chance = MPMTools.generator.nextInt(100)+1;
 				if (chance<=20){
 					  Location location = event.getPlayer().getLocation().clone();
 					  location.add(0, -1, 0);
 					  if (location.getBlock().equals(Material.GRASS)){
 						  location.add(0,1,0);
 						  location.getBlock().setType(Material.RED_ROSE);
 					  }
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onLaunchArrowEvent(ProjectileLaunchEvent event) {
 		  System.out.println("ProjectileLaunchEvent");
 	        if(event.getEntity() instanceof Arrow) {
 	            if(event.getEntity().getShooter() instanceof Player) {
 	            	HashMap<String, Long> attributes = MPMTools.playerAttributes.get(event.getEntity().getShooter());
 	            	if (attributes.containsKey(MPMAttributeType.ARROW_VELOCITY_MULTIPLIER)){
 	            		System.out.println("Arrow Velocity Multiplier");
 	            		Long value = attributes.get(MPMAttributeType.ARROW_VELOCITY_MULTIPLIER);
	            		event.getEntity().setVelocity(event.getEntity().getVelocity().clone().multiply(value/100));
 	            	}
 	            }
 	        }
 	    }
 	
 	@EventHandler
 	public void onWeaponDamageEvent(WeaponDamageEvent event){
 		//System.out.println("WeaponDamageEvent");
 		if (!(event.getEntity() instanceof Player)) return;
 		Player player = (Player) event.getAttackerEntity();
 		HashMap<String, Long> attributes = MPMTools.playerAttributes.get(player);
 		
 		if (attributes.containsKey(MPMAttributeType.POISONOUS)){
 			long value = attributes.get(MPMAttributeType.POISONOUS);
 			int chance = MPMTools.generator.nextInt(100)+1;
 			
 			if (chance<=value && event.getEntity() instanceof Player){
 				Player p = (Player) event.getEntity();
 				Hero h = MPMTools.getHeroes().getCharacterManager().getHero(p);
 				p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 3, true));
 			}
 		}
 		
 		if (attributes.containsKey(MPMAttributeType.BLINDING)){
 			long value = attributes.get(MPMAttributeType.BLINDING);
 			int chance = MPMTools.generator.nextInt(100)+1;
 			
 			if (chance<=value && event.getEntity() instanceof Player){
 				Player p = (Player) event.getEntity();
 				Hero h = MPMTools.getHeroes().getCharacterManager().getHero(p);
 				p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 3, true));
 			}
 		}
 		
 		
 		if (attributes.containsKey(MPMAttributeType.CONFUSING)){
 			long value = attributes.get(MPMAttributeType.CONFUSING);
 			int chance = MPMTools.generator.nextInt(100)+1;
 			
 			if (chance<=value && event.getEntity() instanceof Player){
 				Player p = (Player) event.getEntity();
 				Hero h = MPMTools.getHeroes().getCharacterManager().getHero(p);
 				//System.out.println("Confusing");
 				p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 3, true));
 			}
 		}
 		
 		if (attributes.containsKey(MPMAttributeType.WITHERING)){
 			long value = attributes.get(MPMAttributeType.WITHERING);
 			int chance = MPMTools.generator.nextInt(100)+1;
 			
 			if (chance<=value && event.getEntity() instanceof Player){
 				Player p = (Player) event.getEntity();
 				Hero h = MPMTools.getHeroes().getCharacterManager().getHero(p);
 				//System.out.println("Withering");
 				p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 3, true));
 			}
 		}
 		
 		if (attributes.containsKey(MPMAttributeType.WEAKINING)){
 			long value = attributes.get(MPMAttributeType.WEAKINING);
 			int chance = MPMTools.generator.nextInt(100)+1;
 			
 			if (chance<=value && event.getEntity() instanceof Player){
 				Player p = (Player) event.getEntity();
 				Hero h = MPMTools.getHeroes().getCharacterManager().getHero(p);
 				//System.out.println("Weakening");
 				p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 3, true));
 			}
 		}
 		
 		if (attributes.containsKey(MPMAttributeType.SLOWING)){
 			long value = attributes.get(MPMAttributeType.SLOWING);
 			int chance = MPMTools.generator.nextInt(100)+1;
 			
 			if (chance<=value && event.getEntity() instanceof Player){
 				Player p = (Player) event.getEntity();
 				Hero h = MPMTools.getHeroes().getCharacterManager().getHero(p);
 				//System.out.println("Slowing");
 				p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 3, true));
 			}
 		}
 		
 	}
 	 
 	@EventHandler
 	public void onSkillDamageEvent(SkillDamageEvent event){
 		
 		if (!(event.getEntity() instanceof Player)) return;
 		
 		Set<SkillType> st = event.getSkill().getTypes();
 		
 		//System.out.println(st.toString());
 		
 		Player player = (Player) event.getEntity();
 		
 		HashMap<String, Long> attributes = MPMTools.playerAttributes.get(player);
 	
 		double oldDamage = event.getDamage();
 		double modifiedDamage = 0;
 		if (attributes.containsKey(MPMAttributeType.RESISTANCE_TO_DARK)){
 			if (st.contains(SkillType.DARK)){
 				long value = attributes.get(MPMAttributeType.RESISTANCE_TO_DARK);
 				modifiedDamage += oldDamage * (value/100.0);
 			}
 		}
 		if (attributes.containsKey(MPMAttributeType.RESISTANCE_TO_EARTH)){
 			if (st.contains(SkillType.EARTH)){
 				long value = attributes.get(MPMAttributeType.RESISTANCE_TO_EARTH);
 				modifiedDamage += oldDamage * (value/100.0);
 			}
 		}
 		if (attributes.containsKey(MPMAttributeType.RESISTANCE_TO_FORCE)){
 			if (st.contains(SkillType.FORCE)){
 				long value = attributes.get(MPMAttributeType.RESISTANCE_TO_FORCE);
 				modifiedDamage += oldDamage * (value/100.0);
 			}
 		}
 		if (attributes.containsKey(MPMAttributeType.RESISTANCE_TO_ICE)){
 			if (st.contains(SkillType.ICE)){
 				long value = attributes.get(MPMAttributeType.RESISTANCE_TO_ICE);
 				modifiedDamage += oldDamage * (value/100.0);
 			}
 		}
 		if (attributes.containsKey(MPMAttributeType.RESISTANCE_TO_LIGHT)){
 			if (st.contains(SkillType.LIGHT)){
 				long value = attributes.get(MPMAttributeType.RESISTANCE_TO_LIGHT);
 				modifiedDamage += oldDamage * (value/100.0);
 			}
 		}
 		if (attributes.containsKey(MPMAttributeType.RESISTANCE_TO_LIGHTNING)){
 			if (st.contains(SkillType.LIGHTNING)){
 				long value = attributes.get(MPMAttributeType.RESISTANCE_TO_LIGHTNING);
 				modifiedDamage += oldDamage * (value/100);
 			}
 		}
 		if (attributes.containsKey(MPMAttributeType.RESISTANCE_TO_PHYSICAL)){
 			if (st.contains(SkillType.PHYSICAL)){
 				long value = attributes.get(MPMAttributeType.RESISTANCE_TO_PHYSICAL);
 				modifiedDamage += oldDamage * (value/100.0);
 			}
 		}
 		if (attributes.containsKey(MPMAttributeType.RESISTANCE_TO_MAGIC)){
 			if (st.contains(SkillType.MANA)){
 				long value = attributes.get(MPMAttributeType.RESISTANCE_TO_MAGIC);
 				modifiedDamage += oldDamage * (value/100.0);
 			}
 		}
 		
 		if (attributes.containsKey(MPMAttributeType.POISONOUS)){
 			long value = attributes.get(MPMAttributeType.POISONOUS);
 			int chance = MPMTools.generator.nextInt(100)+1;
 			
 			if (chance<=value && event.getEntity() instanceof Player){
 				Player p = (Player) event.getEntity();
 				Hero h = MPMTools.getHeroes().getCharacterManager().getHero(p);
 				p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 3, true));
 			}
 		}
 		
 		if (attributes.containsKey(MPMAttributeType.BLINDING)){
 			long value = attributes.get(MPMAttributeType.BLINDING);
 			int chance = MPMTools.generator.nextInt(100)+1;
 			
 			if (chance<=value && event.getEntity() instanceof Player){
 				Player p = (Player) event.getEntity();
 				Hero h = MPMTools.getHeroes().getCharacterManager().getHero(p);
 				p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 3, true));
 			}
 		}
 		
 		//System.out.println("Damage: " + oldDamage);
 		//System.out.println("modifiedDamage: " + modifiedDamage);
 		//System.out.println("Total: " + (oldDamage-modifiedDamage));
 		event.setDamage(oldDamage - modifiedDamage);
 	}
 	
 	@EventHandler
 	public void onHeroRegainManaEvent(HeroRegainManaEvent event){
 		Hero hero = event.getHero();
 		HashMap<String, Long> attributes = MPMTools.playerAttributes.get(hero.getPlayer());
 		if (attributes.containsKey(MPMAttributeType.MANA_REGENERATION)){
 			long oldAmount = event.getAmount();
 			long extraRegen = attributes.get(MPMAttributeType.MANA_REGENERATION);
 			long regenModifier = extraRegen + oldAmount;
 			if (regenModifier<1) regenModifier = 0;
 			event.setAmount((int) (regenModifier));
 		}	
 	}
 	
 	@EventHandler
 	public void onHeroRegainHealthEvent(HeroRegainHealthEvent event){
 		Hero hero = event.getHero();
 		HashMap<String, Long> attributes = MPMTools.playerAttributes.get(hero.getPlayer());
 		if (attributes.containsKey(MPMAttributeType.HEAL_BONUS)){
 			long extraHealth = attributes.get(MPMAttributeType.HEAL_BONUS);
 			event.setAmount((double) (event.getAmount() + extraHealth));
 		}	
 	}
 	
 	@EventHandler
 	public void onPlayerRegainHealthEvent(EntityRegainHealthEvent event){
 		if (!(event.getEntity() instanceof Player)) return;
 		Player player = (Player) event.getEntity();
 		//System.out.println(player.getName() + " has regenerated " + event.getAmount() + " health");
 		if (MPMTools.playerAttributes.containsKey(player)){
 			if (event.getAmount()>player.getMaxHealth()){
 				event.setAmount(player.getMaxHealth());
 			}
 			
 			if (MPMTools.playerAttributes.get(player).containsKey(MPMAttributeType.HEALTH_REGENERATION)){
 				double oldAmount = event.getAmount();
 				double modifier = MPMTools.playerAttributes.get(player).get(MPMAttributeType.HEALTH_REGENERATION);
 				
 				double setAmount = oldAmount + modifier;
 				if (setAmount<1) setAmount = 0;
 				event.setAmount(oldAmount + modifier);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onExperienceChangeEvent(ExperienceChangeEvent event){
 		double oldExperience = event.getExpChange();
 		double experience = oldExperience;
 		Player player = event.getHero().getPlayer();
 		if (MPMTools.playerAttributes.containsKey(player)){
 			HashMap<String, Long> attributes = MPMTools.playerAttributes.get(player);
 			if (attributes.containsKey(MPMAttributeType.NEGATE_EXPERIENCE)){
 				event.setCancelled(true);
 				return;
 			}
 			
 			if (attributes.containsKey(MPMAttributeType.INCREASE_EXPERIENCE)){
 				experience = (oldExperience + (oldExperience * attributes.get(MPMAttributeType.INCREASE_EXPERIENCE)/100.0));
 			}
 			
 			if (attributes.containsKey(MPMAttributeType.DECREASE_EXPERIENCE)){
 				experience =  (oldExperience - (oldExperience * attributes.get(MPMAttributeType.INCREASE_EXPERIENCE)/100.0));
 			}		
 		}
 		event.setExpGain(experience);
 	}
 	
 	
 	
 	@EventHandler
 	public void onPlayerDeathEvent(PlayerDeathEvent event){
 		Player player = event.getEntity();
 		if (MPMTools.playerAttributes.containsKey(player)){
 			List<ItemStack> drops = new ArrayList<ItemStack>(event.getDrops());
 			List<ItemStack> dropsToRemove =  new ArrayList<ItemStack>();
 			List<ItemStack> dropsToAdd = new ArrayList<ItemStack>();
 			
 			for (ItemStack item : drops){
 				if (item.hasItemMeta()){
 					if (item.getItemMeta().hasLore()){
 						if (item.getItemMeta().getLore().contains(MPMAttributeType.DEATH_DEFYING)){
 							System.out.println(item.getItemMeta().getDisplayName());
 							//System.out.println(item.getItemMeta().getLore().toString());
 							dropsToRemove.add(item);
 							item.setDurability((short) (item.getDurability() + ItemTools.durabilityModifier(item.getType())));
 							dropsToAdd.add(item);
 						}else if(item.getItemMeta().getLore().contains(MPMAttributeType.DEVILS_TAKE)){
 							dropsToRemove.add(item);
 						}
 					}
 					MPMTools.playerAttributes.get(player).clear();
 				}
 			}
 		
 			this.itemsToReadd.put(player, dropsToAdd);			
 			for (ItemStack item : dropsToRemove){
 				event.getDrops().remove(item);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerRespawnEvent(PlayerRespawnEvent event){
 		if (itemsToReadd.containsKey(event.getPlayer())){
 			List<ItemStack> readd = itemsToReadd.get(event.getPlayer());
 			for (ItemStack item : readd){
 				event.getPlayer().getInventory().addItem(item);
 			}
 			
 			itemsToReadd.remove(event.getPlayer());
 		}	
 	}
 	
 	@EventHandler
 	public void onHeroEnterCombatEvent(HeroEnterCombatEvent event){
 		Hero hero = event.getHero();
 		Player player = hero.getPlayer();
 		if (MPMTools.playerAttributes.containsKey(player)){
 			if (MPMTools.playerAttributes.get(player).containsKey(MPMAttributeType.DRAGON_GROWL)){
 				player.getWorld().playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);	
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEvent(PlayerInteractEvent event){
 		Player player = event.getPlayer();
 		if (player.getItemInHand()==null) return;
 		
 		if (event.getAction()==Action.RIGHT_CLICK_AIR && MPMTools.playerAttributes.containsKey(player)){
 			if (MPMTools.playerAttributes.get(player).containsKey(MPMAttributeType.MITCHIRINEKO_MARCH)){
 				InputStream is = MPMTools.plugin.getResource("mo.mid");
 				
 				Set<Player> playerList = new HashSet<Player>();
 				for (Entity e : player.getNearbyEntities(35, 35, 35)){
 					if (e instanceof Player) playerList.add((Player) e);
 				}
 				playerList.add(player);
 				try {
 					MidiUtil.playMidi(is, (float) 1, playerList);
 				} catch (InvalidMidiDataException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				} catch (MidiUnavailableException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}else if(MPMTools.playerAttributes.get(player).containsKey(MPMAttributeType.GIVE_RANDOM_ITEM)){
 				player.getInventory().addItem(TieredItems.getRandomItem(100));
 				player.updateInventory();
 			}else if (MPMTools.playerAttributes.get(player).containsKey(MPMAttributeType.TEST_SKILL)){
 				Hero h = MPMTools.getHeroes().getCharacterManager().getHero(player);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onCraftItemEvent(CraftItemEvent event){
 		ItemStack item = event.getCurrentItem();
 		
 		if (item.getType()==Material.DIAMOND_SWORD){
 			item = SimpleAttributesAPI.addItemAttribute(item, "Diamond Sword" ,  AttributeType.GENERIC_ATTACK_DAMAGE, 45);
 			event.setCurrentItem(item);
 		}
 	}
 }
