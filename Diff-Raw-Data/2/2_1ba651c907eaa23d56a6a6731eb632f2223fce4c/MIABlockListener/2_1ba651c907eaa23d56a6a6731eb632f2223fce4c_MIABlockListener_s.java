 package com.bukkit.Top_Cat.MIA;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.BlockRightClickEvent;
 import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
 import org.bukkit.event.block.BlockInteractEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockDamageLevel;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 
 /**
  * MIA block listener
  * @author Thomas Cheyney
  */
 public class MIABlockListener extends BlockListener {
     private final MIA plugin;
 
     public MIABlockListener(final MIA plugin) {
         this.plugin = plugin;
     }
     
     @Override
     public void onBlockPlace(BlockPlaceEvent event) {
     	int town = plugin.mf.intown(event.getBlock());
 		if (town > 0 && town != plugin.mf.playertownId(event.getPlayer()) && !event.getPlayer().isOp()) {
 			event.setCancelled(true);
 		} else {
 			plugin.mf.updatestats(event.getPlayer(), 1, event.getBlock().getTypeId());
 			plugin.mf.updatestats(event.getPlayer(), 2, 8, 1);
 		}
 	}
     
     ArrayList<Block> b = new ArrayList<Block>();
     
     @Override
     public void onBlockDamage(BlockDamageEvent event) {
     	int town = plugin.mf.intown(event.getBlock());
 		if (town > 0 && town != plugin.mf.playertownId(event.getPlayer()) && !event.getPlayer().isOp()) {
 			event.setCancelled(true);
 		}
     	if (event.getDamageLevel() == BlockDamageLevel.BROKEN && !event.isCancelled()) {	
     		if (b.contains(event.getBlock())) {
 	    		event.setCancelled(true);
 	    		event.getBlock().setType(Material.AIR);
     		}
     		plugin.mf.updatestats(event.getPlayer(), 0, event.getBlock().getTypeId());
     		plugin.mf.updatestats(event.getPlayer(), 2, 9, 1);
     	}
     }
     
     @Override
     public void onBlockInteract(BlockInteractEvent event) {
    	if ((event.getBlock().getType() == Material.CHEST || event.getBlock().getType() == Material.FURNACE || event.getBlock().getType() == Material.WORKBENCH) && !plugin.mf.inowntown((Player) event.getEntity()) && plugin.mf.intown((Player) event.getEntity()) > 0) {
     		plugin.mf.sendmsg((Player) event.getEntity(), "4This object is locked!");
     		event.setCancelled(true);
     	}
     }
     HashMap<Block, HashMap<Player, Integer>> signdes = new HashMap<Block, HashMap<Player, Integer>>();
     HashMap<Block, Integer[]> opengate = new HashMap<Block, Integer[]>();
     
     public void checklift(World w, Player p, int x, int y, int z) {
     	if (w.getBlockAt(x, y, z).getType() == Material.WALL_SIGN) {
 			BlockFace s = new org.bukkit.material.Sign(Material.SIGN, w.getBlockAt(x, y, z).getData()).getFacing();
 			double xd = x;
 			double zd = z;
 			if (s == BlockFace.SOUTH) {
 				System.out.println("South");
 			} else if (s == BlockFace.NORTH || s == BlockFace.NORTH_WEST) {
 				xd += .5; zd += .5;
 			} else if (s == BlockFace.WEST) {
 				System.out.println("West");
 			} else {
 				System.out.println("Lolwut?");
 				System.out.println(s.toString());
 			}
 			while (w.getBlockAt(x, y, z).getType() == Material.AIR || w.getBlockAt(x, y, z).getType() == Material.WALL_SIGN) {
 				y--;
 			}
 			double yd = y; 
 			if (w.getBlockAt(x, y, z).getType() == Material.STEP) {
 				yd += .5;
 			} else {
 				yd++;
 			}
 			p.teleportTo(new Location(w, xd, yd, zd, p.getLocation().getYaw(), p.getLocation().getPitch()));
 		}
     }
     
     public boolean dogatecol(HashMap<Block, Boolean> fb, int x, int y, int z, World w) {
     	if (!fb.containsKey(w.getBlockAt(x, y, z))){
     		int tmpy = y;
     		if (w.getBlockAt(x, y, z).getType() != Material.FENCE) { return false; }
 	    	for (int y1 = y + 1; y1 <= y + 12; y1++) {
 	            if (w.getBlockAt(x, y1, z).getType() == Material.FENCE) {
 	                y = y1;
 	            } else {
 	                break;
 	            }
 	        }
     	
 	    	if (w.getBlockAt(x, y + 1, z).getType() == Material.AIR) {
 	            return false;
 	        }
 	    	
 	    	if (!fb.containsKey(w.getBlockAt(x, y, z))){
 	    		fb.put(w.getBlockAt(x, tmpy, z), false);
 		    	fb.put(w.getBlockAt(x, y, z), true);
 		    	Boolean close = w.getBlockAt(x, y - 1, z).getType() != Material.FENCE;
 		    	
 		    	int minY = Math.max(0, y - 12);
 		    	for (int y1 = y - 1; y1 >= minY; y1--) {
 		    		Material cur = w.getBlockAt(x, y1, z).getType();
 		
 		            // Allowing water allows the use of gates as flood gates
 		            if (cur != Material.WATER
 		                    && cur != Material.STATIONARY_WATER
 		                    && cur != Material.LAVA
 		                    && cur != Material.STATIONARY_LAVA
 		                    && cur != Material.FENCE
 		                    && cur != Material.AIR) {
 		                break;
 		            }
 		            
 		            w.getBlockAt(x, y1, z).setType(close ? Material.FENCE : Material.AIR);
 		            
 		            dogatecol(fb, x + 1, y1, z, w);
 		            dogatecol(fb, x - 1, y1, z, w);
 		            dogatecol(fb, x, y1, z + 1, w);
 		            dogatecol(fb, x, y1, z - 1, w);
 		    	}
 	            dogatecol(fb, x + 1, y, z, w);
 	            dogatecol(fb, x - 1, y, z, w);
 	            dogatecol(fb, x, y, z + 1, w);
 	            dogatecol(fb, x, y, z - 1, w);
 		    	return true;
 	    	}
     	}
     	return false;
     }
     
     @Override
     public void onBlockRightClick(BlockRightClickEvent event) {
 		World w = event.getPlayer().getWorld();
     	if (event.getBlock().getType() == Material.WALL_SIGN || event.getBlock().getType() == Material.SIGN_POST) {
     		String signtxt = ((Sign) event.getBlock().getState()).getLine(1);
     		int x = event.getBlock().getX();
     		int z = event.getBlock().getZ();
     		int y = event.getBlock().getY();
     		//System.out.println(x + "," + event.getBlock().getY() + "," + z);
     		if (signtxt.equalsIgnoreCase("[Gate]")) {
     			HashMap<Block, Boolean> foundblocks = new HashMap<Block, Boolean>();
     			
     			//Find gate
     			for (int x1 = x - 3; x1 <= x + 3; x1++) {
                     for (int y1 = y - 3; y1 <= y + 6; y1++) {
                         for (int z1 = z - 3; z1 <= z + 3; z1++) {
                             dogatecol(foundblocks, x1, y1, z1, w);
                         }
                     }
                 }
     			
     		} else if (signtxt.equalsIgnoreCase("[Lift Down]")) {
     			for (int i = event.getBlock().getY() - 1; i > 0; i--) {
     				checklift(w, event.getPlayer(), x, i, z);
     			}
     		} else if (signtxt.equalsIgnoreCase("[Lift Up]")) {
     			for (int i = event.getBlock().getY() + 1; i < 128; i++) {
     				checklift(w, event.getPlayer(), x, i, z);
     			}
     		} else if (signtxt.equalsIgnoreCase("[Bridge]")) {
     			BlockFace s = new org.bukkit.material.Sign(Material.SIGN, event.getBlock().getData()).getFacing();
     			int xd = 0;
     			int zd = 0;
     			if (s == BlockFace.SOUTH) {
     				xd = -1;
     				//System.out.println("South");
     			} else if (s == BlockFace.NORTH) {
     				xd = 1;
     				System.out.println("North");
     			} else if (s == BlockFace.WEST) {
     				zd = 1;
     				System.out.println("West");
     			} else if (s == BlockFace.EAST) {
     				zd = -1;
     				System.out.println("East");
     			} else {
     				System.out.println("Lolwut?");
     				System.out.println(s.toString());
     			}
     			for (int i = 1; i < 15; i++) {
     				int nx = x + (i * xd);
     				int nz = z + (i * zd);
     				
     				if (w.getBlockAt(nx, event.getBlock().getY(), nz).getType() == Material.SIGN_POST) {
     					String end_signtxt = ((Sign) w.getBlockAt(nx, event.getBlock().getY(), nz).getState()).getLine(1);
     					if (end_signtxt.equalsIgnoreCase("[Bridge End]") || end_signtxt.equalsIgnoreCase("[Bridge]")) {
         					Block sign1 = event.getBlock();
         					Block sign2 = w.getBlockAt(nx, event.getBlock().getY(), nz);
         					// Check blocks below signs are of the same type
         					Material b1 = sign1.getRelative(0, -1, 0).getType();
         					Material b2 = sign2.getRelative(0, -1, 0).getType();
         					Material b3 = sign1.getRelative(zd, -1, xd).getType();
         					Material b4 = sign1.getRelative(-zd, -1, -xd).getType();
         					Material b5 = sign2.getRelative(zd, -1, xd).getType();
         					Material b6 = sign2.getRelative(-zd, -1, -xd).getType();
         					//plugin.mf.sendmsg(event.getPlayer(), b1.toString() + b2.toString() + b3.toString() + b4.toString() + b5.toString() + b6.toString());
         					if (b1 == b2 && b2 == b3 && b3 == b4 && b4 == b5 && b5 == b6) {
         						boolean air = true;
         						boolean cle = false;
         						for (int j = 1; j < i; j++) {
         							for (int k = -1; k < 2; k++) {
         								Material b7 = sign1.getRelative((j * xd) + (k * zd), -1, (j * zd) + (k * xd)).getType();
         								if (b1 == b7) {
         									cle = true;
         								}
         								if (b7 != Material.AIR && b7 != b1 && b7 != Material.STATIONARY_WATER && b7 != Material.STATIONARY_LAVA) {
         									air = false;
         								}
         							}
         						}
         						if (air) {
         							if (cle) {
         								b1 = Material.AIR;
         							}
             						for (int j = 1; j < i; j++) {
             							for (int k = -1; k < 2; k++) {
             								sign1.getRelative((j * xd) + (k * zd), -1, (j * zd) + (k * xd)).setType(b1);
             							}
             						}
         						} else {
         							plugin.mf.sendmsg(event.getPlayer(), "Bridge area not empty!");
         						}
         					} else {
         						plugin.mf.sendmsg(event.getPlayer(), "Block types don't match!");
         					}
     					}
     					break;
     				}
     			}
     		} else if (signtxt.equalsIgnoreCase("[Door]")) {
     			BlockFace s = new org.bukkit.material.Sign(Material.SIGN, event.getBlock().getData()).getFacing();
     			int xd = 0;
     			int zd = 0;
     			if (s == BlockFace.SOUTH || s == BlockFace.NORTH) {
     				xd = 1;
     				System.out.println("North/South");
     			} else if (s == BlockFace.WEST || s == BlockFace.EAST) {
     				zd = 1;
     				System.out.println("East/West");
     			} else {
     				System.out.println("Lolwut?");
     				System.out.println(s.toString());
     			}
     			for (int i = -15; i < 15; i++) {
     				if (i == 0)
     					continue;
     				
     				int nx = x;
     				int nz = z;
     				int ny = y + i;
     				
     				if (w.getBlockAt(nx, ny, nz).getType() == Material.SIGN_POST) {
     					String end_signtxt = ((Sign) w.getBlockAt(nx, ny, nz).getState()).getLine(1);
     					if (end_signtxt.equalsIgnoreCase("[Door End]") || end_signtxt.equalsIgnoreCase("[Door]")) {
         					Block sign1 = event.getBlock();
         					Block sign2 = w.getBlockAt(nx, ny, nz);
     						if (i > 0) {
     							ny = 1;
     						} else {
     							ny = -1;
     						}
         					// Check blocks below signs are of the same type
         					Material b1 = sign1.getRelative(0, ny, 0).getType();
         					Material b2 = sign2.getRelative(0, -ny, 0).getType();
         					Material b3 = sign1.getRelative(zd, ny, xd).getType();
         					Material b4 = sign1.getRelative(-zd, ny, -xd).getType();
         					Material b5 = sign2.getRelative(zd, -ny, xd).getType();
         					Material b6 = sign2.getRelative(-zd, -ny, -xd).getType();
         					//plugin.mf.sendmsg(event.getPlayer(), b1.toString() + b2.toString() + b3.toString() + b4.toString() + b5.toString() + b6.toString());
         					if (b1 == b2 && b2 == b3 && b3 == b4 && b4 == b5 && b5 == b6) {
         						boolean air = true;
         						boolean cle = false;
         						for (int p = 2; p < Math.abs(i) - 1; p++) {
         							int j = p;
         							if (i < 0)
         								j *= -1;
         							for (int k = -1; k < 2; k++) {
         								Material b7 = sign1.getRelative((k * zd), j, (k * xd)).getType();
         								if (b1 == b7) {
         									cle = true;
         								}
         								if (b7 != Material.AIR && b7 != b1 && b7 != Material.STATIONARY_WATER && b7 != Material.STATIONARY_LAVA) {
         									air = false;
         								}
         							}
         						}
         						if (air) {
         							if (cle) {
         								b1 = Material.AIR;
         							}
         							for (int p = 2; p < Math.abs(i) - 1; p++) {
             							int j = p;
             							if (i < 0)
             								j *= -1;
             							for (int k = -1; k < 2; k++) {
             								sign1.getRelative((k * zd), j, (k * xd)).setType(b1);
             							}
             						}
         						} else {
         							plugin.mf.sendmsg(event.getPlayer(), "Bridge area not empty!");
         						}
         					} else {
         						plugin.mf.sendmsg(event.getPlayer(), "Block types don't match!");
         					}
     					}
     					break;
     				}
     			}
     		}
 	    	if (!opengate.containsKey(event.getBlock())) {
 	    		int p = 0;
 	    		ArrayList<String> s = plugin.mf.getDest(event.getBlock());
 	    		if (signdes.containsKey(event.getBlock())) {
 	    			HashMap<Player, Integer> ug = signdes.get(event.getBlock());
 	    			if (ug.containsKey(event.getPlayer())) {
 	    				p = ug.get(event.getPlayer()) + 1;
 	    			}
 		    		if (s.size() <= p) {
 		    			p = 0;
 		    		}
 		    		ug.put(event.getPlayer(), p);
 	    		} else {
 	    			HashMap<Player, Integer> ug = new HashMap<Player, Integer>();
 	    			ug.put(event.getPlayer(), p);
 	    			signdes.put(event.getBlock(), ug);
 	    		}
 	    		if (s.size() > 0) {
 		    		// Display next destination on sign  s.get(p);
 		    		int t = p - 1;
 		    		int m = p + 1; 
 		    		if (t < 0) { t = 0; m++; }
 		    		if (m >= s.size()) { m = s.size() - 1; if (s.size() > 2) { t--; } }
 		    		Sign si = ((Sign) event.getBlock().getState());
 		    		si.setLine(0, "- " + plugin.mf.gateName(event.getBlock()) + " -");
 		    		//int j = 0;
 		    		
 		    		int l = 0;
 		    		for (l = t; l <= m; l++) {
 		    			String li = s.get(l);
 		    			if (l == p) li = " > " + li + " < ";
 		    			// j = l - t;
 			    		si.setLine((l - t) + 1, li);
 		    		}
 		    		for (int l2 = (l - t) + 1; l2 < 4; l2++) {
 		    			si.setLine(l2, "");
 		    		}
 		    		si.update();
 	    		}
 	     	}
     	}
     	
     	if (event.getBlock().getType() == Material.STONE_BUTTON) {
     		HashMap<String, String> gd = plugin.mf.gateData(event.getBlock());
     		int p = -1;
     		int xo = 0;
     		int zo = 0;
     		if (gd.containsKey("rot")) {
     			Integer rot = Integer.parseInt(gd.get("rot"));
     		    		
 	    		switch (rot) {
 	    		case 0:
 	            	zo = 1;
 	            	break;
 	            case 1:
 	            	xo = -1;
 	            	break;
 	            case 2:
 	            	zo = -1;
 	            	break;
 	            case 3:
 	            	xo = 1;
 	            	break;
 	    		}
     		}
     		Block si = w.getBlockAt(event.getBlock().getX() + (xo * 3), event.getBlock().getY(), event.getBlock().getZ() + (zo * 3));
     		if (signdes.containsKey(si)) {
     			HashMap<Player, Integer> ug = signdes.get(si);
     			if (ug.containsKey(event.getPlayer())) {
     				p = ug.get(event.getPlayer());
     			}
     		}
     		ArrayList<String> s = plugin.mf.getDest(si);
     		if (p > -1 && !opengate.containsKey(si)) {
     			HashMap<Player, Integer> ug = signdes.get(si);
     			ug.remove(event.getPlayer());
     			/*Sign sis = ((Sign) si.getState()); DOESN'T UPDATE???
 	    		sis.setLine(0, "--" + plugin.mf.gateName(si) + "--");
 	    		sis.setLine(1, "");
 	    		sis.setLine(2, "*" + s.get(p) + "*");
 	    		sis.setLine(3, "*" + event.getPlayer().getName() + "*");
 	    		sis.update();*/
     			
 	    		Integer[] l = plugin.mf.gateSign(s.get(p));
 	    		//plugin.get
 	    		plugin.mf.openportal(plugin.getServer().getWorlds().get(l[3]).getBlockAt(l[0], l[1], l[2]));
 	    		plugin.mf.openportal(si);
 
 	    		java.util.Date time = new java.util.Date();
 				Integer tnow = (int) Math.round(((double) time.getTime() / 1000));
 	    		
     			Integer[] gstat = new Integer[4];
     			gstat[0] = plugin.mf.gateId(s.get(p));
     			gstat[1] = event.getPlayer().getEntityId();
     			gstat[2] = Integer.parseInt( plugin.mf.gateData(si).get("Id") );
     			gstat[3] = tnow;
     			Integer[] gstat2 = new Integer[4];
     			gstat2[0] = Integer.parseInt( plugin.mf.gateData(si).get("Id") );
     			gstat2[1] = 0;
     			gstat2[2] = plugin.mf.gateId(s.get(p));
     			gstat2[3] = tnow;
     			opengate.put(w.getBlockAt(l[0], l[1], l[2]), gstat2);
     			opengate.put(si, gstat);
     		} else {
     			if (opengate.containsKey(si)) {
     				Integer[] gstat3 = opengate.get(si);
     				opengate.remove(si);
     				Integer[] l = plugin.mf.gateSign(gstat3[2]);
     				opengate.remove(w.getBlockAt(l[0], l[1], l[2]));
         			plugin.mf.closeportal(plugin.getServer().getWorlds().get(l[3]).getBlockAt(l[0], l[1], l[2]), event.getPlayer().getWorld());
         			plugin.mf.closeportal(si, event.getPlayer().getWorld());
     			}
     			w.getBlockAt(event.getBlock().getX() + xo - zo, event.getBlock().getY() - 1, event.getBlock().getZ() + zo + xo).setType(Material.AIR);
     		}
     	}
     	
     	if (event.getItemInHand().getType() == Material.STICK) {
     		int town = plugin.mf.intown(event.getBlock());
     		plugin.mf.sendmsg(event.getPlayer(), "Block belongs to: " + plugin.mf.towninfo(town).get("name"));
     	}
     }
     
     @Override
     public void onBlockRedstoneChange(BlockRedstoneEvent event) {
     	
     }
     
     @Override
     public void onBlockFlow(BlockFromToEvent event) {
     	int town1 = plugin.mf.intown(event.getBlock());
     	int town2 = plugin.mf.intown(event.getToBlock());
     	if (town1 == 0 && town1 != town2 && event.getToBlock().getType() == Material.AIR) {
     		event.getToBlock().setType(Material.LEAVES);
     		b.add(event.getToBlock());
     	}
     }
     
     @Override
     public void onBlockIgnite(BlockIgniteEvent event) {
     	if (!(event.getCause() == IgniteCause.FLINT_AND_STEEL && event.getPlayer() != null && plugin.mf.notothertown(event.getPlayer()))) {
     		event.setCancelled(true);
     	}
     }
 
 }
