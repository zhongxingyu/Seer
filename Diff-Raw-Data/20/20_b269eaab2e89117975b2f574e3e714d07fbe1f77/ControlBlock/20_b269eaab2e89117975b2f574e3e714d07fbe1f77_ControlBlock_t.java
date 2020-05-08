 /*
  * This file is part of MinecartRevolution-Core.
  * Copyright (c) 2012 QuarterCode <http://www.quartercode.com/>
  *
  * MinecartRevolution-Core is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * MinecartRevolution-Core is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MinecartRevolution-Core. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.minecartrevolution.core.control.block;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import com.quartercode.minecartrevolution.core.control.Control;
 import com.quartercode.minecartrevolution.core.control.sign.ControlSign;
 
 public abstract class ControlBlock extends Control {
 
     public boolean hasSontrolSign(Location blockLocation) {
 
         return getControlSigns(blockLocation).size() > 0;
     }
 
     private ControlBlockInfo info;
 
     public List<Sign> getControlSigns(Location blockLocation) {
 
         List<Sign> signs = new ArrayList<Sign>();
 
         for (int[] offsets : ControlSign.CONTROL_SIGN_OFFSETS) {
             Location location = blockLocation.clone();
             location.add(offsets[0], offsets[1], offsets[2]);
 
            if (location.getBlock().getState() instanceof Sign) {
                 signs.add((Sign) location.getBlock().getState());
             }
         }
 
         return signs;
     }
 
     public ControlBlockInfo getInfo() {
 
         if (info == null) {
             info = createInfo();
         }
 
         return info;
     }
 
     protected abstract ControlBlockInfo createInfo();
 
     public abstract void execute(Minecart minecart, Block block);
 
     public boolean allowPlace(Player player, Block block) {
 
         return true;
     }
 
     public boolean allowDestroy(Player player, Block block) {
 
         return true;
     }
 
 }
