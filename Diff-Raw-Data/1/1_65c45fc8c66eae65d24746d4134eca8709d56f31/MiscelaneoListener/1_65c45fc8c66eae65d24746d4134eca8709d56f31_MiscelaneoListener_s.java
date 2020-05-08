 package miscelaneo.listeners;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import miscelaneo.Miscelaneo;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Door;
 
 @SuppressWarnings("deprecation")
 public class MiscelaneoListener implements Listener{
 	
 	Miscelaneo plugin;
 	
 	public MiscelaneoListener(Miscelaneo listener) {
 		plugin=listener;
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
 	public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
 		player.setCompassTarget(player.getWorld().getBlockAt(0, 0, -12550820).getLocation());
 	}
 	  
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
 		Player player = event.getPlayer();
 		player.setCompassTarget(player.getWorld().getBlockAt(0, 0, -12550820).getLocation());
 	}
 	 
 	@EventHandler
     public void onPlayerInteract(PlayerInteractEvent evnt) {
 		
 		
         Player player = evnt.getPlayer();
         Action action = evnt.getAction();
         Block block = evnt.getClickedBlock();
   
         
         ItemStack inHand = evnt.getPlayer().getItemInHand();
         int inHandId = evnt.getPlayer().getItemInHand().getTypeId();
         Material material;
 		Random r = new Random();
 		int r2 = 0;
 		int itemcalidad = 50 +10;  // AQUI COLOCAR EL CHECK DE LA CALIDAD DEL ITEM EN LA MANO
 		
         if (player.hasMetadata("NPC")) return; // Checkear si el jugador es NPC.
 
         /* Fix for NPE on interacting with air */
         if (block == null) {
             material = Material.AIR;
         }else {
             material = block.getType();
         }
         
         player.setCompassTarget(player.getWorld().getBlockAt(0, 0, -12550820).getLocation());
         
         switch (action) {
 	        case RIGHT_CLICK_BLOCK:
 	        if(material == Material.NETHERRACK){
 	        	evnt.setCancelled(true);
 	        }
 /*			if(material == Material.SOIL){
 	        	if((inHandId==295) || (inHandId==338) || (inHandId==361) || (inHandId==362) || (inHandId==391) || (inHandId==392)){  // Wheat Seeds, Sugar Cane, Pumpkin Seeds, Melon Seeds, Carrot, Potato
 	        		// checkear el radio alrededor del block. si no tiene bloque molino o espantapajaros, event.setcanceled
 	        		boolean canFarm = false;
 	        		
 	        		Location location = player.getLocation();
 	        		Location locationAhead = location.getBlock().getRelative(getPlayerFacing(player), 20).getLocation();
 
 	        		int radiomolino = 50;
 	        		
 /*	        		for (int x = -(radiomolino); x <= 50; x++){
 	        			for (int y = -(radiomolino); y <= 50; y++){
 	        				for (int z = -(radiomolino); z <= 50; z++){
 	        					player.sendMessage("coordenada x: "+x+" coordenada y: "+y+" coordenada z: "+z);
 	        					
 	        					Location loc = location.getBlock().getRelative(x, y, z).getLocation();
 
 	        					if (!getTransparentMaterials().contains(loc.getBlock().getTypeId()==29)){
 	        					
 	        						player.sendMessage("ESTE coordenada x: "+x+" coordenada y: "+y+" coordenada z: "+z);
 	        					//player.sendBlockChange(loc, Material.SNOW_BLOCK.getId(), (byte) 0);
 
 	        					}
 	    	        		}
 		        		}
 	        		}*/
 /*	        		if(canFarm==false){
 	        			evnt.setCancelled(true);
 	        			player.sendMessage(ChatColor.RED+"Esta zona no es adecuada para plantar");
 	        		}else if(canFarm==true){
 	        			canFarm=false;
 	        		}
 		        }
         	}else*/ if(material == Material.STONE){  // Colocar los soportes de las minas en la piedra.
 	        	if(inHandId==370){ // colocado como provisional, ghast tear (370)
 	        		r2 = r.nextInt(100);
 	        		if(inHand.getAmount()>1){
 	        			player.getInventory().getItemInHand().setAmount(player.getInventory().getItemInHand().getAmount()-1);
 	        		}else if(inHand.getAmount()<=1){
 	        			player.getInventory().removeItem(player.getInventory().getItemInHand());
 	        		}
 	        		if(r2<=itemcalidad){
 	        			block.setTypeIdAndData(98, (byte) 3, true); //chiseled stone brick
 	        			player.sendMessage(ChatColor.GREEN+"Has colocado el soporte para la mina en la piedra");
 	        		}else{
 	        			evnt.setCancelled(true);
 	        			player.sendMessage(ChatColor.RED+"El soporte para la mina no tenia suficiente calidad como para aguantar el peso de la roca y se ha roto");
 	        		}
 	        	}
 	        }else if(material == Material.IRON_DOOR_BLOCK){ // Abrir las puertas de hierro con click derecho.
 				BlockState state = block.getState();
 				Door door = (Door) state.getData();
 				BlockState state2;
 				if (door.isTopHalf()) {
 					Door top = door;
 					state2 = block.getRelative(BlockFace.DOWN).getState();
 					Door bottom = (Door) state2.getData();
 					if (top.isOpen() == false) {
 						top.setOpen(true);
 						bottom.setOpen(true);
 						player.getWorld().playSound(block.getLocation(), Sound.DOOR_CLOSE, 1, 1);
 					}else {
 						top.setOpen(false);
 						bottom.setOpen(false);
 						player.getWorld().playSound(block.getLocation(), Sound.DOOR_CLOSE, 1, 1);
 					}
 					state.update();
 					state2.update();
 				}else {
 					Door bottom = door;
 					state2 = block.getRelative(BlockFace.UP).getState();
 					Door top = (Door) state2.getData();
 					if (bottom.isOpen() == false) {
 						bottom.setOpen(true);
 						top.setOpen(true);
 						player.getWorld().playSound(block.getLocation(), Sound.DOOR_CLOSE, 1, 1);
 					}else {
 						bottom.setOpen(false);
 						top.setOpen(false);
 						player.getWorld().playSound(block.getLocation(), Sound.DOOR_CLOSE, 1, 1);
 					}
 					state.update();
 					state2.update();
 				}
 			}else if(inHand.getTypeId() == 385){
 				evnt.setCancelled(true);
 			}
 			default:
 				break;
 		}
 	}
 	
 	public static List<Material> getTransparentMaterials(){
 		Material[] materials = {Material.AIR, Material.BED, Material.BED_BLOCK, Material.BREWING_STAND, Material.BROWN_MUSHROOM,
 				Material.BURNING_FURNACE, Material.CACTUS, Material.CAKE_BLOCK, Material.CAULDRON, Material.CHEST,
 				Material.DEAD_BUSH, Material.DETECTOR_RAIL, Material.DIODE, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON,
 				Material.DISPENSER, Material.DRAGON_EGG, Material.EGG, Material.ENCHANTMENT_TABLE, Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME,
 				Material.ENDER_STONE, Material.FENCE, Material.FENCE_GATE, Material.FIRE, Material.FURNACE, Material.GLASS, Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2,
 				Material.IRON_DOOR_BLOCK, Material.IRON_FENCE, Material.JUKEBOX, Material.LADDER, Material.LAVA, Material.LEVER, Material.LONG_GRASS, Material.MELON_STEM,
 				Material.MOB_SPAWNER, Material.NETHER_FENCE, Material.NETHER_STALK, Material.NETHER_WARTS, Material.PAINTING, Material.PISTON_BASE, Material.PISTON_EXTENSION,
 				Material.PISTON_MOVING_PIECE, Material.PISTON_STICKY_BASE, Material.PORTAL, Material.PUMPKIN_STEM, Material.RED_ROSE, Material.RED_MUSHROOM, Material.SAPLING, Material.SIGN, Material.SIGN_POST, Material.STATIONARY_LAVA,
 				Material.SNOW, Material.STATIONARY_WATER, Material.STONE_BUTTON, Material.SUGAR_CANE_BLOCK, Material.THIN_GLASS, Material.TNT, Material.TORCH, Material.TRAP_DOOR, Material.VINE, Material.WALL_SIGN,
 				Material.WATER, Material.WEB, Material.WHEAT, Material.WOODEN_DOOR, Material.WORKBENCH, Material.YELLOW_FLOWER};
 
 		return Arrays.asList(materials);
 	}
 
 	private BlockFace getPlayerFacing(Player player) {
 		float y = player.getLocation().getYaw();
         if( y < 0 ) y += 360;
         y %= 360;
         int i = (int)((y+8) / 22.5);
         
         if(i == 0) return BlockFace.WEST;
         else if(i == 1) return BlockFace.NORTH_WEST;
         else if(i == 2) return BlockFace.NORTH_WEST;
         else if(i == 3) return BlockFace.NORTH_WEST;
         else if(i == 4) return BlockFace.NORTH;
         else if(i == 5) return BlockFace.NORTH_EAST;
         else if(i == 6) return BlockFace.NORTH_EAST;
         else if(i == 7) return BlockFace.NORTH_EAST;
         else if(i == 8) return BlockFace.EAST;
         else if(i == 9) return BlockFace.SOUTH_EAST;
         else if(i == 10) return BlockFace.SOUTH_EAST;
         else if(i == 11) return BlockFace.SOUTH_EAST;
         else if(i == 12) return BlockFace.SOUTH;
         else if(i == 13) return BlockFace.SOUTH_WEST;
         else if(i == 14) return BlockFace.SOUTH_WEST;
         else if(i == 15) return BlockFace.SOUTH_WEST;
 
         return BlockFace.WEST;
 
 	}
 
 	@EventHandler //----- Tocar a la puerta
 	public void onBlockDamage(BlockDamageEvent event) {
 
         Player player = event.getPlayer();
         Block block = event.getBlock();
         int material = block.getTypeId();
 
 		if(player.hasMetadata("NPC")) return;
 
 		if((material==64) || (material==71)){
 			player.getWorld().playSound(block.getLocation(), Sound.ZOMBIE_WOOD, 1, 1);
 		}
 	}
 
 	/* Romper hielos y demases */
 	@EventHandler 
 	public void OnPlayerMoveEvent(PlayerMoveEvent event){
 		Player player = event.getPlayer();
 		if(player.getGameMode()==GameMode.SURVIVAL){
 			Location loc = event.getPlayer().getLocation();
 			loc.setY(loc.getY() - 1);
 			Location loc2 = event.getPlayer().getLocation();
 			loc2.setY(loc.getY() -2);
 			
 			World w = loc.getWorld();
 			Block b = w.getBlockAt(loc);
 			Block b2 = w.getBlockAt(loc2);
 			
 			if ((b.getType() == Material.ICE) && (b2.getType() == (Material.STATIONARY_WATER) || b2.getType() == (Material.WATER))){
 				int r2=0;
 				Random r = new Random();
 				
 				r2 = r.nextInt(10);
 				if (r2==1){
 					
 					/* Bucles y demases */
 					int x = b.getX();
 					int z = b.getZ();
 					x++;
 					z++;
 					Location bLoc = b.getLocation();
 					bLoc.setX(x);
 					bLoc.setZ(z);
 
 					for (int i = 0;i<3;i++){
 						for (int j = 0;j<3;j++){
 							
 							if (player.getWorld().getBlockAt(bLoc).getType() == Material.ICE){
 								player.getWorld().getBlockAt(bLoc).setType(Material.WATER);
 								
 							}
 							bLoc.setZ(bLoc.getZ() - 1);
 							
 						}
 						bLoc.setX(bLoc.getX() - 1);
 						
 					}
 					
 
 					player.sendMessage(ChatColor.DARK_RED+"Has roto el hielo por el que caminabas!");
 				}
 			}
 
 		}
 		
 		
 		
 		// for server getworld getblockat
 	}
 /*	@EventHandler //----- Sprint
 	public void onPlayerSprint(final PlayerToggleSprintEvent event){
 		
 		final Player player = event.getPlayer();
 	    final Timer tiempoespera = new Timer();    
 	    long tiempoaguante = 5000; // Colocar 5000 (5 segs) * nivel de resistencia del jugador para que aumente la distancia que puedes sprintar sin cansarte.
 	    
 	    TimerTask timerTask = new TimerTask(){
 	    	public void run(){
 	    		if(player.isSprinting()){
 		    		event.setCancelled(true);
 					if(!player.hasPotionEffect(PotionEffectType.SLOW)){
 						player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 3));
 					}
 					tiempoespera.cancel();
 	    		}
 		    }
 	    };
 	    
 	    if(player.isSprinting()){
 	    	tiempoespera.scheduleAtFixedRate(timerTask, tiempoaguante, 1); 
 	    }
 
 	}*/
 
 
 
 }
