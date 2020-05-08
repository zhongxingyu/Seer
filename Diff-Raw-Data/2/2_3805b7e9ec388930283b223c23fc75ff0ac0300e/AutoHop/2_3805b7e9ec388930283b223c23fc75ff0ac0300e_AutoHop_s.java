 package me.desht.autohop;
 
 /*
     This file is part of autohop
 
     AutoHop is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     AutoHop is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with AutoHop.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 public class AutoHop extends JavaPlugin implements Listener {
 
 	private static Set<Integer> passable = new HashSet<Integer>();
 
 	static {
 		passable.add(Material.AIR.getId());
 		passable.add(Material.WATER.getId());
 		passable.add(Material.STATIONARY_WATER.getId());
 		passable.add(Material.SAPLING.getId());
 		passable.add(Material.POWERED_RAIL.getId());
 		passable.add(Material.DETECTOR_RAIL.getId());
 		passable.add(Material.WEB.getId());
 		passable.add(Material.LONG_GRASS.getId());
 		passable.add(Material.DEAD_BUSH.getId());
 		passable.add(Material.YELLOW_FLOWER.getId());
 		passable.add(Material.RED_ROSE.getId());
 		passable.add(Material.BROWN_MUSHROOM.getId());
 		passable.add(Material.RED_MUSHROOM.getId());
 		passable.add(Material.TORCH.getId());
 		passable.add(Material.FIRE.getId());
 		passable.add(Material.REDSTONE_WIRE.getId());
 		passable.add(Material.CROPS.getId());
 		passable.add(Material.SIGN_POST.getId());
 		passable.add(Material.LADDER.getId());
 		passable.add(Material.RAILS.getId());
 		passable.add(Material.WALL_SIGN.getId());
 		passable.add(Material.LEVER.getId());
 		passable.add(Material.STONE_PLATE.getId());
 		passable.add(Material.WOOD_PLATE.getId());
 		passable.add(Material.REDSTONE_TORCH_OFF.getId());
 		passable.add(Material.REDSTONE_TORCH_ON.getId());
 		passable.add(Material.STONE_BUTTON.getId());
 		passable.add(Material.SNOW.getId());
 		passable.add(Material.SUGAR_CANE.getId());
 		passable.add(Material.PORTAL.getId());
 		passable.add(Material.DIODE_BLOCK_OFF.getId());
 		passable.add(Material.DIODE_BLOCK_ON.getId());
 		passable.add(Material.PUMPKIN_STEM.getId());
 		passable.add(Material.MELON_STEM.getId());
 		passable.add(Material.VINE.getId());
 		passable.add(Material.WATER_LILY.getId());
 		passable.add(Material.NETHER_WARTS.getId());
 		passable.add(Material.ENDER_PORTAL.getId());
 	}
 
 	@Override
 	public void onDisable() {
 	}
 
 	@Override
 	public void onEnable() { 
 		PluginManager pm = this.getServer().getPluginManager();
 
 		pm.registerEvents(this, this);
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerMove(PlayerMoveEvent event) {
 		Location f = event.getFrom();
 		Location t = event.getTo();
 
 		// delta X and Z - which way the player is going
 		double dx = t.getX() - f.getX();
 		double dz = t.getZ() - f.getZ();
 		// extrapolation of next X and Z the player will get to
 		double nextX = t.getX() + dx;
 		double nextZ = t.getZ() + dz;
 		// X and Z position within a block - a player pushing against a wall will 
 		// have X or Z either < ~0.3 or > ~0.7 due to player entity bounding box size
 		double tx = nextX - Math.floor(nextX);
 		double tz = nextZ - Math.floor(nextZ);
 
 //		System.out.println("yaw = " + t.getYaw() + " dx = " + dx + " dz = " + dz + " nextX = " + nextX + " tx = " + tx + " nextZ = " + nextZ + " tz = " + tz);
 
 		float yaw = t.getYaw() % 360;
 		if (yaw < 0) yaw += 360;
 
 		Block toBlock = t.getBlock();
 		BlockFace face = null;
 		if (yaw >= 45 && yaw < 135 && dx <= 0.0 && tx < 0.3001) {
 			face = BlockFace.NORTH;
 		} else if (yaw >= 135 && yaw < 225 && dz <= 0.0 && tz < 0.3001) {
 			face = BlockFace.EAST;
		} else if (yaw >= 225 && yaw < 315 && dx >= 0.0 && tz > 0.6999) {
 			face = BlockFace.SOUTH;
 		} else if ((yaw >= 315 || yaw < 45) && dz >= 0.0 && tz > 0.6999) {
 			face = BlockFace.WEST;
 		} else {
 			return;
 		}
 
 		Block b = toBlock.getRelative(face);
 //		System.out.println("check block " + face + " type = " + b.getType());
 
 		if (!passable.contains(b.getTypeId()) &&
 				passable.contains(b.getRelative(BlockFace.UP).getTypeId()) &&
 				passable.contains(b.getRelative(BlockFace.UP, 2).getTypeId())) {
 			Vector v = event.getPlayer().getVelocity();
 //			System.out.println("current velocity = " + v);
 			
 			if (v.getY() <= 0.0 && v.getY() >= -0.08) {
 //				System.out.println("jump!");
 				v.setX(v.getX() + dx);
 				v.setY(0.5);
 				v.setZ(v.getZ() + dz);
 				event.getPlayer().setVelocity(v);
 			}
 		}
 	}
 }
