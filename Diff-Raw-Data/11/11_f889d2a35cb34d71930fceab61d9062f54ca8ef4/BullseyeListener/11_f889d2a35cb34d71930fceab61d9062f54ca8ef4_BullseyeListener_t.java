 package com.github.janka102.bullseye;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.util.BlockIterator;
 
 public class BullseyeListener implements Listener {
 	public final Bullseye plugin;
 	public String lines = "";
 	
 	public BullseyeListener(Bullseye r_plugin) {
 		plugin = r_plugin;
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 
 	public boolean isValidBlock(Block block) { //Checks if the block hit, or the block a sign placed on, can even handle a redstone torch.
 		if(block == null) {
 			return false;
 		}
 			Material blockType = block.getType();
 			if (blockType == Material.AIR //blocks that don't work with redstone torches
 					|| blockType == Material.STEP
 					|| blockType == Material.TNT
 					|| blockType.toString().contains("STAIRS")
 					|| blockType.toString().contains("GLASS")
 					|| blockType == Material.IRON_FENCE
 					|| blockType == Material.CACTUS
 					|| blockType == Material.WEB
 					|| blockType.toString().contains("PISTON")
 					|| blockType == Material.GLOWSTONE
 					//|| blockType.toString().contains("SIGN")
 					|| blockType == Material.ENDER_PORTAL_FRAME)
 			{
 				return false;
 			}
 		
 		return true;
 	}
 	
 	public boolean isNearWater(Block sign) { 
 		for(int z = -1; z <= 1; z++) {
 			for(int x = -1; x <= 1; x++) {
 				for(int y = 0; y <= 1; y++) {
 					if(x*x+y*y+z*z == 1) {
 						Block relBlock = sign.getRelative(x, y, z);
 						if(relBlock.getType() == Material.WATER || relBlock.getType() == Material.STATIONARY_WATER){
 							return true;
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	public void signWarning(Block block, String firstLine) { //Changes text color to red of first line on a Bullseye sign placed on an invalid  block.
 		Sign sign = (Sign) block.getState();
 		sign.setLine(0, ChatColor.DARK_RED + firstLine);
 		sign.update();
 	}
 	
 	public boolean isBullseyeSign(String line) { //checks to make sure the sign is a Bullseye sign
 		if(line.equalsIgnoreCase((ChatColor.DARK_BLUE + "[bullseye]").toString()) || line.equalsIgnoreCase(("[bullseye]").toString())
 				|| line.equalsIgnoreCase((ChatColor.DARK_BLUE + "[bull]").toString()) || line.equalsIgnoreCase(("[bull]").toString())
 				|| line.equalsIgnoreCase((ChatColor.DARK_BLUE + "[be]").toString()) || line.equalsIgnoreCase(("[be]").toString())) 
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	public Block[] getHitBlockSign(Block hit){
 		Block[] signs = new Block[5];
 		int numSigns = 0;
 		for(int z = -1; z <= 1; z++) {
     		for(int x = -1; x <= 1; x++) {
     			for(int y = 0; y <= 1; y++) {
     				if(x*x+y*y+z*z == 1) {
     					Block hitSign = hit.getRelative(x, y, z);  // get the block to the North, South, East, West, and Top of the block hit
     					if(hitSign.getState() instanceof Sign) {
         					Sign hitSignSign = (Sign)hitSign.getState();
             				if (isBullseyeSign(hitSignSign.getLine(0))) { //checks to see if the sign next to the block hit is a Bullseye sign
             					org.bukkit.material.Sign s = (org.bukkit.material.Sign) hitSign.getState().getData();
             				    Block attachedBlock = hitSign.getRelative(s.getAttachedFace());
             					if(attachedBlock.equals(hit)) { //checks to make sure the sign is attached to the block hit
             						signs[numSigns] = hitSign;
             						numSigns++;
           						}
            					}
     					}
    					}
    				}
   			}
    		}
 		return signs;
 	}
 
 	@EventHandler
     public void onProjectileHit(ProjectileHitEvent event) { //called when any projectile comes into contact with something
 		Entity projectile = event.getEntity();
         if(!(projectile instanceof Arrow)) { //narrow the search for just arrows
         	 return;
         }
 
         Arrow arrow = (Arrow)projectile;
         Entity entity = arrow.getShooter();
         if(!(entity instanceof Player)) { //check to see if the player was the one who shot the arrow
         	return;
         }
         Player p = (Player)entity;
         World world = arrow.getWorld();
         
         BlockIterator bi = new BlockIterator(world, arrow.getLocation().toVector(), arrow.getVelocity().normalize(), 0, 4);
         Block hit = null;
         Material type = null;
         
         while(bi.hasNext()) {
             hit = bi.next();
             type = hit.getType();
             if(type != Material.AIR
             		&& type != Material.WATER
             		&& type != Material.STATIONARY_WATER
             		&& type != Material.LAVA
          			&& type != Material.STATIONARY_LAVA
             		&& type != Material.LONG_GRASS
             		&& type != Material.REDSTONE_WIRE
             		&& type != Material.SAPLING
             		&& type != Material.WEB
             		&& type != Material.YELLOW_FLOWER
             		&& type != Material.RED_ROSE
             		&& type != Material.DIODE_BLOCK_OFF
             		&& type != Material.DIODE_BLOCK_ON
             		&& !type.toString().contains("TRIPWIRE") //for 1.3.1
             		&& type != Material.REDSTONE_TORCH_ON
             		&& type != Material.REDSTONE_TORCH_OFF
             		&& type != Material.TORCH
             		&& type != Material.VINE
             		&& type != Material.RAILS
             		&& type != Material.DETECTOR_RAIL
             		&& type != Material.POWERED_RAIL)
             {
             	type = hit.getType();
             	break;
             }
         }
         
         //p.sendMessage(type.toString());
         
         /*
         if(type == Material.WALL_SIGN || type == Material.SIGN_POST) {
         	if (isBullseyeSign(((Sign)hit.getState()).getLine(0))) {
         		if(isNearWater(hit)) {
         			p.sendMessage(ChatColor.RED + "Can't activate Bullseye sign with water next to it!");
         			return;
         		}
         		Sign hitSignSign = (Sign)hit.getState();
         		if(hitSignSign.getLine(1).trim().length() >= 1
 						|| hitSignSign.getLine(2).trim().length() >= 1
 						|| hitSignSign.getLine(3).trim().length() >= 1) { //checks if there is a message the player wants shown on hit
 					p.sendMessage("Bullseye! You hit " + hitSignSign.getLine(1) + hitSignSign.getLine(2) + hitSignSign.getLine(3) +"!");
 				}
         		if (type == Material.SIGN_POST) {
 					//going to change the sign to a redstone torch as Sign Post
 					signToRestone(hitSignSign, hit, type, hit.getData(), true);	
 				}
 				else if (type == Material.WALL_SIGN) {
 					//going to change the sign to a redstone torch as Wall Sign
 					signToRestone(hitSignSign, hit, type, hit.getData(), false);
 				}
         		
         	}
         }
         */
 
         if(isValidBlock(hit)) { //check if the block hit by the arrow would be a block that a Bullseye sign might go
         	//hitBlock = hit;
         	//p.sendMessage(type.toString());
         	
         	if (type.toString().contains("SIGN") && isBullseyeSign(((Sign)hit.getState()).getLine(0))){
         		org.bukkit.material.Sign s = (org.bukkit.material.Sign) hit.getState().getData();
 			    Block attachedBlock = hit.getRelative(s.getAttachedFace());
 			    hit = attachedBlock;
         	}
         	
         	Block[] hitBlockSign = getHitBlockSign(hit);
        	boolean h2oSigns = true;
        	for (Block bullseyeSign : hitBlockSign) { 
         		if (bullseyeSign == null) {
             		continue;
             	}
             	Sign hitSignSign = (Sign) bullseyeSign.getState();
             	
             	if(isNearWater(bullseyeSign)) {
            		if (h2oSigns){
            			p.sendMessage(ChatColor.RED + "Couldn't activate some Bullseye signs with water near them!");
            		}
            		h2oSigns = false;
        			continue;
         		}
     			if(hitSignSign.getLine(1).trim().length() >= 1
     					|| hitSignSign.getLine(2).trim().length() >= 1
     					|| hitSignSign.getLine(3).trim().length() >= 1) { //checks if there is a message the player wants shown on hit
     				p.sendMessage("Bullseye! You hit " + hitSignSign.getLine(1) + hitSignSign.getLine(2) + hitSignSign.getLine(3) +"!");
     			}
     			if (bullseyeSign.getType() == Material.SIGN_POST) {
     				//going to change the sign to a redstone torch as Sign Post
     				signToRestone(hitSignSign, bullseyeSign, bullseyeSign.getType(), bullseyeSign.getData(), true);	
     			}
     			else if (bullseyeSign.getType() == Material.WALL_SIGN) {
     				//going to change the sign to a redstone torch as Wall Sign
     				signToRestone(hitSignSign, bullseyeSign, bullseyeSign.getType(), bullseyeSign.getData(), false);
     			}
 
         	}
 		}
 	}
 
 	public void signToRestone(final Sign bullseyeSign, final Block hitBlock, Material hitSignType, final Byte hitSignData, final boolean isPost) { //changes the Bullseye sign to a redstone torch for a specified time
 		final String[] lines = bullseyeSign.getLines();
 		
 		if (isPost) { //if the Bullseye sign is a SIGN_POST
 			hitBlock.setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(), (byte) 0x5,true); //make the Bullseye sign into a redstone torch
 		}
 		else { //if the Bullseye sign is a WALL_SIGN
 			byte data = hitBlock.getData(); // Correspond to the direction of the wall sign
 			if (data == 0x2) { //South
 				hitBlock.setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x4, true); //make the Bullseye sign into a redstone torch
 			}
 			else if (data == 0x3) { //North
 				hitBlock.setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x3, true); //make the Bullseye sign into a redstone torch
 			}
 			else if (data == 0x4) { //East
 				hitBlock.setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x2, true); //make the Bullseye sign into a redstone torch
 			}
 			else if (data == 0x5) { //West
 				hitBlock.setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(),(byte) 0x1, true); //make the Bullseye sign into a redstone torch
 			}
 			else { // Not South, North, East or West 
 				Bullseye.getBullLogger().info("Strange Data!");
 				return;
 			}
 		}
 
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { //Run this after a delay
 			public void run() {
 				try {
 					hitBlock.setType(Material.AIR); //clear the block
 
 					if (!isPost) { //if it is a wall sign, put it back as one
 						hitBlock.setType(Material.WALL_SIGN);
 						hitBlock.setTypeIdAndData(Material.WALL_SIGN.getId(), hitSignData, true);
 					}
 					else { //if a sign post, put it back as one
 						hitBlock.setType(Material.SIGN_POST);
 						hitBlock.setTypeIdAndData(Material.SIGN_POST.getId(), hitSignData, true);
 					}
 
 					Sign signtemp = (Sign) hitBlock.getState();
 					
 					for(int i = 0; i < lines.length; i++) { //restore the original text of the Bullseye sign back
 						signtemp.setLine(i, lines[i]);
 					}
 					signtemp.update(true);
 										
 					//plugin.getServer().broadcastMessage(ChatColor.AQUA + "Changed sign back.");
 				}
 				catch (RuntimeException e)
 				{
 					Bullseye.getBullLogger().severe("Error while updating redstone signToRestone :" + e.getClass() + ":" + e.getMessage());
 					return;
 				}
 			}
 		}, 25 );
 	}
 }
