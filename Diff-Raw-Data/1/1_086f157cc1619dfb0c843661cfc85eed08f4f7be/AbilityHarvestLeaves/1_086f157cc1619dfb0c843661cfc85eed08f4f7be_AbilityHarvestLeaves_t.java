 /*
  * MineQuest - Bukkit Plugin for adding RPG characteristics to minecraft
  * Copyright (C) 2011  Jason Monk
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package org.monk.MineQuest.Ability;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.inventory.ItemStack;
 import org.monk.MineQuest.Quester.Quester;
 import org.monk.MineQuest.Quester.SkillClass.SkillClass;
 import org.monk.MineQuest.Quester.SkillClass.Resource.Lumberjack;
 
 public class AbilityHarvestLeaves extends Ability {
 
 	@Override
 	public void castAbility(Quester quester, Location location,
 			LivingEntity entity) {
 		int i, j, k;
 		Location loc = new Location(location.getWorld(), (int)location.getX() - 15,
 				(int)location.getY() - 15, (int)location.getZ() - 15);
 
 		for (i = 0; i < 30; i++) {
 			loc.setY(location.getY() - 15);
 			for (j = 0; j < 30; j++) {
 				loc.setZ(location.getZ() - 15);
 				for (k = 0; k < 30; k++) {
 					Block block = loc.getWorld().getBlockAt(loc);
 					
 					
 					if (block.getType() == Material.LEAVES) {
 						block.setType(Material.AIR);
 						if (myclass.getGenerator().nextDouble() < .1) {
 							loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.SAPLING, 1));
 						}
 					}
 					
 					loc.setZ(loc.getZ() + 1);
 				}
 				loc.setY(loc.getY() + 1);
 			}
 			loc.setX(loc.getX() + 1);
 		}
 	}
 
 	@Override
 	public SkillClass getClassType() {
 		return new Lumberjack();
 	}
 
 	@Override
 	public List<ItemStack> getManaCost() {
 		List<ItemStack> cost = new ArrayList<ItemStack>();
 		
 		cost.add(new ItemStack(Material.WOOD_SPADE, 1));
 		
 		return cost;
 	}
 
 	@Override
 	public String getName() {
 		return "Harvest Leaves";
 	}
 
 	@Override
 	public int getReqLevel() {
 		return 10;
 	}
 
 }
