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
 
 package com.quartercode.minecartrevolution.core.control.sign;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import org.bukkit.Location;
import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Minecart;
 import com.quartercode.minecartrevolution.core.MinecartRevolution;
 
 public class ControlSignExecutor {
 
     private final MinecartRevolution minecartRevolution;
     private List<ControlSign>        controlSigns;
 
     public ControlSignExecutor(MinecartRevolution minecartRevolution) {
 
         this.minecartRevolution = minecartRevolution;
         controlSigns = new ArrayList<ControlSign>();
     }
 
     public List<ControlSign> getControlSigns() {
 
         return Collections.unmodifiableList(controlSigns);
     }
 
     public ControlSign getControlBlock(Class<? extends ControlSign> c) {
 
         for (ControlSign controlSign : controlSigns) {
             if (controlSign.getClass().equals(c)) {
                 return controlSign;
             }
         }
 
         return null;
     }
 
     public void setControlSigns(List<ControlSign> controlSigns) {
 
         this.controlSigns = controlSigns;
     }
 
     public void addControlSign(ControlSign controlSign) {
 
         controlSign.setMinecartRevolution(minecartRevolution);
         controlSigns.add(controlSign);
         controlSign.enable();
     }
 
     public List<Sign> getSigns(Minecart minecart) {
 
         List<Sign> signs = new ArrayList<Sign>();
         Location location = minecart.getLocation();
 
         for (int[] offsets : ControlSign.CONTROL_SIGN_OFFSETS) {
             Location signLocation = location.clone();
             signLocation.add(offsets[0], offsets[1], offsets[2]);
 
            if (signLocation.getBlock().getType() == Material.SIGN || signLocation.getBlock().getType() == Material.SIGN_POST) {
                 signs.add((Sign) signLocation.getBlock().getState());
             }
         }
 
         return signs;
     }
 
     public void execute(Minecart minecart) {
 
         for (Sign sign : getSigns(minecart)) {
             executeControlSign(sign, minecart);
         }
     }
 
     public void executeControlSign(Sign sign, Minecart minecart) {
 
         if (sign.getBlock().isBlockIndirectlyPowered()) {
             return;
         }
 
         for (ControlSign controlSign : controlSigns) {
             ControlSignInfo controlSignInfo = controlSign.getInfo();
 
             for (String label : controlSignInfo.getLabels()) {
                 if (ControlSignInfo.getFormattedLabel(label).equalsIgnoreCase(sign.getLine(0))) {
                     controlSign.execute(minecart, sign.getLine(0), sign);
                 }
             }
         }
     }
 
 }
