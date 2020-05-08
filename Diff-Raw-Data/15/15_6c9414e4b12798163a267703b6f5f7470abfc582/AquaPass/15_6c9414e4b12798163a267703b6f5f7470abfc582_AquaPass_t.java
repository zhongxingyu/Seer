 /*
  * This file is part of Aqualock.
  *
  * Copyright (c) 2012, AlmuraDev <http://www.almuramc.com/>
  * Aqualock is licensed under the Almura Development License.
  *
  * Aqualock is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Aqualock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License. If not,
  * see <http://www.gnu.org/licenses/> for the GNU General Public License.
  */
 package com.almuramc.aqualock.bukkit.display;
 
 import java.util.HashMap;
 
 import com.almuramc.aqualock.bukkit.AqualockPlugin;
 import com.almuramc.aqualock.bukkit.display.button.CloseButton;
 import com.almuramc.aqualock.bukkit.display.button.UnlockButton;
 import com.almuramc.aqualock.bukkit.display.field.PasswordField;
 import com.almuramc.aqualock.bukkit.display.label.CostToUseValueLabel;
 import com.almuramc.aqualock.bukkit.display.label.OwnerLabel;
 import com.almuramc.aqualock.bukkit.display.label.PasswordLabel;
 import com.almuramc.aqualock.bukkit.display.label.RealOwnersLabel;
 import com.almuramc.aqualock.bukkit.display.label.UseCostLabel;
 import com.almuramc.aqualock.bukkit.lock.BukkitLock;
 import com.almuramc.bolt.lock.Lock;
 
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 
 import org.bukkit.Location;
 
 public class AquaPass extends CachedGeoPopup {
 	private final AqualockPlugin plugin;
 	//Widgets
 	private final GenericButton unlockButton, closeButton;
 	private final GenericLabel costToUseLabel, costToUseOutputLabel, passwordLabel, ownerLabel, realOwnersOutputLabel;
 	private final GenericTextField passwordField;
 	private final GenericTexture borderTexture, aquaPhoto;
 	//Geo
 	private Location location;
 	private static HashMap<Location, Boolean> openedLocations = new HashMap<>();
 
 	public AquaPass(AqualockPlugin plugin) {
 		this.plugin = plugin;
 		borderTexture = new GenericTexture("http://www.almuramc.com/images/playerplus.png");
 		borderTexture
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setPriority(RenderPriority.High)
 				.setWidth(200)
 				.setHeight(100)
 				.shiftXPos(-105)
 				.shiftYPos(-60);
 		aquaPhoto = new GenericTexture("http://www.almuramc.com/images/aqualock.png");
 		aquaPhoto
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setPriority(RenderPriority.Normal)
 				.setWidth(40)
 				.setHeight(40)
				.shiftXPos(-90)
 				.shiftYPos(-55);
 		closeButton = new CloseButton(plugin);
 		closeButton
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(18)
 				.setWidth(40)
 				.shiftXPos(42)
 				.shiftYPos(17);
 		unlockButton = new UnlockButton(plugin);
 		unlockButton
 				.setAuto(true)
 				.setEnabled(false)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(18)
 				.setWidth(40)
 				.shiftXPos(-10)
 				.shiftYPos(17);
 		costToUseLabel = new UseCostLabel("Cost to use:");
 		costToUseLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(10)
 				.setWidth(40)
				.shiftXPos(0)
 				.shiftYPos(-25);
 		costToUseOutputLabel = new CostToUseValueLabel("");
 		costToUseOutputLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(10)
 				.setWidth(40)
				.shiftXPos(35)
 				.shiftYPos(-25);
 		passwordField = new PasswordField();
 		passwordField
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(14)
 				.setWidth(107)
 				.shiftXPos(-25)
 				.shiftYPos(-3);
 		passwordLabel = new PasswordLabel("Password:");
 		passwordLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(8)
 				.setWidth(40)
 				.shiftXPos(-80)
 				.shiftYPos(0);
 		realOwnersOutputLabel = new RealOwnersLabel("");
 		realOwnersOutputLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(10)
 				.setWidth(40)
 				.shiftXPos(0)
 				.shiftYPos(-40);
 		ownerLabel = new OwnerLabel("Owner:");
 		ownerLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(10)
 				.setWidth(40)
 				.shiftXPos(-40)
 				.shiftYPos(-40);
 		attachWidgets(plugin, borderTexture, aquaPhoto, closeButton, unlockButton, realOwnersOutputLabel, costToUseLabel, costToUseOutputLabel, passwordField, passwordLabel, ownerLabel);
 		passwordField.setFocus(true);
 		passwordField.setTabIndex(0);
 		passwordField.setMaximumCharacters(15);
 		passwordField.setMaximumLines(1);
 		passwordField.setPasswordField(true);
 		this.setTransparent(true);
 	}
 
 	/**
 	 * Populates the panel with information from the backend
 	 */
 	public void populate(Lock lock) {
 		openedLocations.put(getLocation(), true);
 		realOwnersOutputLabel.setText(lock.getOwner());
 		costToUseOutputLabel.setText(Double.toString(((BukkitLock) lock).getUseCost()));
 		unlockButton.setEnabled(true);
 		this.setDirty(true);
 	}
 
 	@Override
 	public boolean isOpen() {
 		return openedLocations.get(location) == null ? false : openedLocations.get(location);
 	}
 
 	@Override
 	public void setOpen(boolean open) {
 		openedLocations.put(location, open);
 	}
 
 	public static boolean isOpen(Location location) {
 		return openedLocations.get(location) == null ? false : openedLocations.get(location);
 	}
 
 	public Location getLocation() {
 		return location;
 	}
 
 	public void setLocation(Location location) {
 		this.location = location;
 	}
 
 	public String getPassword() {
 		return passwordField.getText();
 	}
 }
