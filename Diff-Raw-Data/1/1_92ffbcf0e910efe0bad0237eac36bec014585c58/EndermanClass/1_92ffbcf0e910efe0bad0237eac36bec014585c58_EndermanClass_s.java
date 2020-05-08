 package org.mcsg.double0negative.supercraftbros.classes;
 
 import java.util.Set;
 
import net.minecraft.server.v1_5_R3.Packet61WorldEvent;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.mcsg.double0negative.supercraftbros.GameManager;
 import org.mcsg.double0negative.supercraftbros.util.Colorizer;
 
 public class EndermanClass extends PlayerClassBase{
 
 	public EndermanClass(Player p) {
 		super(p);
 		// TODO Auto-generated constructor stub
 	}
 
 
 
 
 	@Override 
 	public void PlayerSpawn(){
 		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 80000, 2));
 
 		PlayerInventory i = player.getInventory();
 		i.clear();
 
 		i.setHelmet(Colorizer.setColor(new ItemStack(Material.LEATHER_HELMET), 37, 6, 39));
 		i.setChestplate(Colorizer.setColor(new ItemStack(Material.LEATHER_CHESTPLATE), 37, 6, 39));
 
 		ItemStack legs = Colorizer.setColor(new ItemStack(Material.LEATHER_LEGGINGS), 37, 6, 39);
 		legs.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
 		i.setLeggings(legs);
 
 		ItemStack boot = Colorizer.setColor(new ItemStack(Material.LEATHER_BOOTS),  37, 6, 39);
 		boot.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 10);
 		i.setBoots(boot);
 
 
 		ItemStack i1 = new ItemStack(381);
 		i1.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
 		i1.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
 		i.addItem(i1);
 
 		i.addItem(new ItemStack(Material.ENDER_PEARL, 20));
 
 		player.updateInventory();
 
 	}
 
 
 	public EndermanClass newInstance(Player p){
 		return new EndermanClass(p);
 	}
 
 	public String getName(){
 		return "Enderman";
 	}
 
 	public boolean sne = false;
 	public void PlayerMove(){
 		super.PlayerMove();
 		if(!fsmash){
 			if(smash){
 				if(player.isSneaking()){
 					sne = true;
 
 					Set<Player>pls = GameManager.getInstance().getGamePlayer(player).getActivePlayers();
 					
 					Location l = player.getLocation();
 					player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 1000, 1));
 					SendPacketToAll(new Packet61WorldEvent(2003,l.getBlockX(), l.getBlockY()+1, l.getBlockZ(), 0, false));
 					
 				}
 			}
 			
 			
 			
 		}
 	}
 
 
 
 
 
 }
 
 
