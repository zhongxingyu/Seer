 package aor.Spells.Spells;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import aor.Spells.*;
 
 
 public class MidasTouch extends Spell{
 	public ArrayList<Player> players=new ArrayList<Player>(0);
 	public HashMap<Player,ArrayList<Location>> locations=new HashMap<Player,ArrayList<Location>>();
 	public HashMap<Player,ArrayList<Material>> materials=new HashMap<Player,ArrayList<Material>>();
 	public HashMap<Player,ArrayList<Byte>> bytes=new HashMap<Player,ArrayList<Byte>>();
 	public boolean isMidas=false;
 	public MidasTouch(SpellsMain instance){
 		plugin=instance;
 		spellDescription="This spell turns every block you touch into a gold block for one minute.";
 		shortName="Midas";
 		spellName="Midas' Touch";
 		setRequiredItems(new ItemStack(Material.GOLD_BLOCK,10));
 		onBlockDamage=true;
 		onPlayerMove=true;
 		onBlockBreak=true;
 	}
 	public void castSpell(Player player){
 		if(checkInventoryRequirements(player.getInventory()))
 		{
 			if(players.contains(player)){
 				player.sendMessage("Are you really that greedy?");
 			}
 			else{
 				removeRequiredItemsFromInventory(player.getInventory());
 				player.sendMessage("You now have Midas' Touch!");
 				players.add(player);
 				locations.put(player,new ArrayList<Location>());
 				materials.put(player,new ArrayList<Material>());
 				bytes.put(player,new ArrayList<Byte>());
 				isMidas=true;
 				delayedRun(1200,0);
 				onPlayerMove(new PlayerMoveEvent(player, null, null));
 			}
 		} else player.sendMessage("Could not cast! Spell requires 10 gold blocks.");
 
 		
 	}
 	public void run(Object... arg){
 		while(locations.get(players.get(0)).size()>0){
 			locations.get(players.get(0)).get(0).getWorld().getBlockAt(locations.get(players.get(0)).get(0)).setType(materials.get(players.get(0)).get(0));
 			locations.get(players.get(0)).get(0).getWorld().getBlockAt(locations.get(players.get(0)).get(0)).setData(bytes.get(players.get(0)).get(0));
 			locations.get(players.get(0)).remove(0);
 			materials.get(players.get(0)).remove(0);
 			bytes.get(players.get(0)).remove(0);
 		}
 		locations.remove(players.get(0));
 		materials.remove(players.get(0));
 		bytes.remove(players.get(0));
 		players.get(0).sendMessage("You finally decide that you don't want everything you touch to become gold.");
 		players.remove(0);
 		if(players.size()==0){
 			isMidas=false;
 		}
 	}
 	public void onBlockDamage(BlockDamageEvent event){
 		if(isMidas){
 			for(int i=0;i<players.size();i++){
 				if(locations.get(players.get(i)).contains(event.getBlock().getLocation())){
 					event.setCancelled(true);
 					event.getPlayer().sendMessage("NOPE!!!");
 				}
 			}
 		}
 	}
 	public void onPlayerMove(PlayerMoveEvent event){
 		if(isMidas){
 			if(players.contains(event.getPlayer())){
 				for(int i=0;i<3;i++){
 					for(int i2=0;i2<3;i2++){
 						for(int i3=0;i3<4;i3++){
 							changeToGold(event.getPlayer().getLocation().getBlock().getRelative(i-1,i3-1,i2-1),event.getPlayer());
 						}
 					}
 				}
 			}
 		}
 	}
 	public void onBlockBreak(BlockBreakEvent event){
 		if(isMidas){
 			for(int i=0;i<players.size();i++){
 				if(locations.get(players.get(i)).contains(event.getBlock().getLocation())){
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 	public void changeToGold(Block block,Player player){
 		if(block.getType()==Material.BED_BLOCK||block.getType()==Material.BEDROCK||block.getType()==Material.BOAT||block.getType()==Material.BOOKSHELF||block.getType()==Material.BRICK||block.getType()==Material.BRICK||block.getType()==Material.CAKE_BLOCK||block.getType()==Material.COAL_ORE||block.getType()==Material.COBBLESTONE||block.getType()==Material.COBBLESTONE_STAIRS||block.getType()==Material.DEAD_BUSH||block.getType()==Material.DIAMOND_BLOCK||block.getType()==Material.DIAMOND_ORE||block.getType()==Material.DIRT||block.getType()==Material.FENCE||block.getType()==Material.GLASS||block.getType()==Material.GLOWING_REDSTONE_ORE||block.getType()==Material.GLOWSTONE||block.getType()==Material.GOLD_ORE||block.getType()==Material.GRASS||block.getType()==Material.GRAVEL||block.getType()==Material.ICE||block.getType()==Material.IRON_BLOCK||block.getType()==Material.IRON_DOOR_BLOCK||block.getType()==Material.IRON_ORE||block.getType()==Material.JACK_O_LANTERN||block.getType()==Material.LAPIS_BLOCK||block.getType()==Material.LAPIS_ORE||block.getType()==Material.LAVA||block.getType()==Material.LEAVES||block.getType()==Material.MOSSY_COBBLESTONE||block.getType()==Material.NETHERRACK||block.getType()==Material.OBSIDIAN||block.getType()==Material.PUMPKIN||block.getType()==Material.REDSTONE_ORE||block.getType()==Material.SAND||block.getType()==Material.SNOW_BLOCK||block.getType()==Material.SOIL||block.getType()==Material.SOUL_SAND||block.getType()==Material.STATIONARY_LAVA||block.getType()==Material.STATIONARY_WATER||block.getType()==Material.STEP||block.getType()==Material.STONE||block.getType()==Material.SUGAR_CANE_BLOCK||block.getType()==Material.TNT||block.getType()==Material.TRAP_DOOR||block.getType()==Material.WOOD||block.getType()==Material.LOG||block.getType()==Material.WOOD_STAIRS||block.getType()==Material.WOODEN_DOOR||block.getType()==Material.WOOL||block.getType()==Material.WORKBENCH){
//			System.out.println(block.getLocation());
 			locations.get(player).add(block.getLocation());
 			materials.get(player).add(block.getType());
 			bytes.get(player).add(block.getData());
 			block.setType(Material.GOLD_BLOCK);
//			System.out.println("Block Changed!");
 		}
 	}
 }
