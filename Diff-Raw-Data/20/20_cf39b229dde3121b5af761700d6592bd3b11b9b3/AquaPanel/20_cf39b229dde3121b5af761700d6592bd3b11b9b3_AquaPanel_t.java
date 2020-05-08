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
 
 import java.util.List;
 
 import com.almuramc.aqualock.bukkit.AqualockPlugin;
 import com.almuramc.aqualock.bukkit.display.button.ApplyButton;
 import com.almuramc.aqualock.bukkit.display.button.CloseButton;
 import com.almuramc.aqualock.bukkit.display.button.UnlockButton;
 import com.almuramc.aqualock.bukkit.display.checkbox.EveryoneCheckbox;
 import com.almuramc.aqualock.bukkit.display.field.CloseTimerField;
 import com.almuramc.aqualock.bukkit.display.field.CoOwnerField;
 import com.almuramc.aqualock.bukkit.display.field.DamageField;
 import com.almuramc.aqualock.bukkit.display.field.OwnerField;
 import com.almuramc.aqualock.bukkit.display.field.PasswordField;
 import com.almuramc.aqualock.bukkit.display.field.UseCostField;
 import com.almuramc.aqualock.bukkit.display.field.UserField;
 import com.almuramc.aqualock.bukkit.display.label.CloseTimerLabel;
 import com.almuramc.aqualock.bukkit.display.label.CoOwnerLabel;
 import com.almuramc.aqualock.bukkit.display.label.CreateCostLabel;
 import com.almuramc.aqualock.bukkit.display.label.CreateCostValueLabel;
 import com.almuramc.aqualock.bukkit.display.label.DamageLabel;
 import com.almuramc.aqualock.bukkit.display.label.OwnerLabel;
 import com.almuramc.aqualock.bukkit.display.label.PasswordLabel;
 import com.almuramc.aqualock.bukkit.display.label.UseCostLabel;
 import com.almuramc.aqualock.bukkit.display.label.UserLabel;
 import com.almuramc.aqualock.bukkit.lock.BukkitLock;
 import com.almuramc.aqualock.bukkit.lock.DoorBukkitLock;
 import com.almuramc.aqualock.bukkit.util.LockUtil;
 import com.almuramc.bolt.lock.Lock;
 
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericCheckBox;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.gui.Widget;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 
 import org.bukkit.Location;
 
 public class AquaPanel extends GenericPopup {
 	private final AqualockPlugin plugin;
 	//Widgets
 	private final GenericButton closeButton, applyButton, unlockButton;
 	private final GenericCheckBox everyoneCheckbox;
 	private final GenericLabel usersLabel, coownersLabel, costToUseLabel, damageOnFailLabel, costToCreateOutputLabel, costToCreateLabel, passwordLabel, ownerLabel, closeTimerLabel;
 	private final GenericTextField usersField, coownersField, costToUseField, damageOnFailField, passwordField, ownerField, closeTimerField;
 	private final GenericTexture borderTexture;
 	//Geo
 	private Location location;
 
 	public AquaPanel(AqualockPlugin plugin) {
 		this.plugin = plugin;
 		borderTexture = new GenericTexture("http://www.almuramc.com/images/playerplus.png");
 		borderTexture
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setPriority(RenderPriority.High)
 				.setWidth(400)
 				.setHeight(200)
 				.shiftXPos(-185)
 				.shiftYPos(-80);
 		closeButton = new CloseButton(plugin);
 		closeButton
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(18)
 				.setWidth(40)
 				.shiftXPos(142)
 				.shiftYPos(87);
 		applyButton = new ApplyButton(plugin);
 		applyButton
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(18)
 				.setWidth(40)
 				.shiftXPos(90)
 				.shiftYPos(87);
 		unlockButton = new UnlockButton(plugin);
 		unlockButton
 				.setAuto(true)
 				.setEnabled(false)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(18)
 				.setWidth(40)
 				.shiftXPos(40)
 				.shiftYPos(87);
 		usersField = new UserField();
 		usersField
 				.setMaximumLines(2)
 				.setMaximumCharacters(100)
 				.setTabIndex(2)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(29)
 				.setWidth(165)
 				.shiftXPos(15)
 				.shiftYPos(40);
 		usersLabel = new UserLabel("Users:");
 		usersLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(11)
 				.setWidth(40)
 				.shiftXPos(15)
 				.shiftYPos(29);
 		coownersField = new CoOwnerField();
 		coownersField
 				.setMaximumLines(2)
 				.setMaximumCharacters(100)
 				.setTabIndex(1)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(29)
 				.setWidth(165)
 				.shiftXPos(15)
 				.shiftYPos(-7);
 		coownersLabel = new CoOwnerLabel("Co-Owners:");
 		coownersLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(10)
 				.setWidth(40)
 				.shiftXPos(15)
 				.shiftYPos(-19);
 		costToCreateOutputLabel = new CreateCostValueLabel("");
 		costToCreateOutputLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(10)
 				.setWidth(40)
 				.shiftXPos(-68)
 				.shiftYPos(0);
 		costToCreateLabel = new CreateCostLabel("Cost to create:");
 		costToCreateLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(10)
 				.setWidth(40)
 				.shiftXPos(-148)
 				.shiftYPos(0);
 		damageOnFailField = new DamageField();
 		damageOnFailField
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(14)
 				.setWidth(40)
 				.shiftXPos(-70)
 				.shiftYPos(20);
 		damageOnFailLabel = new DamageLabel("Damage on fail:");
 		damageOnFailLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(10)
 				.setWidth(40)
 				.shiftXPos(-146)
 				.shiftYPos(23);
 		costToUseField = new UseCostField();
 		costToUseField
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(14)
 				.setWidth(40)
 				.shiftXPos(-70)
 				.shiftYPos(40);
 		costToUseLabel = new UseCostLabel("Cost to use:");
 		costToUseLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(10)
 				.setWidth(40)
 				.shiftXPos(-132)
 				.shiftYPos(43);
 		closeTimerField = new CloseTimerField();
 		closeTimerField
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(14)
 				.setWidth(40)
 				.shiftXPos(-70)
 				.shiftYPos(60);
 		closeTimerLabel = new CloseTimerLabel("Auto Close Timer:");
 		closeTimerLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(10)
 				.setWidth(40)
 				.shiftXPos(-157)
 				.shiftYPos(65);
 		everyoneCheckbox = new EveryoneCheckbox();
 		everyoneCheckbox
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(10)
 				.setWidth(40)
 				.shiftXPos(-165)
 				.shiftYPos(87);
 		passwordField = new PasswordField();
 		passwordField
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(14)
 				.setWidth(107)
 				.shiftXPos(70)
 				.shiftYPos(-49);
 		passwordLabel = new PasswordLabel("Password:");
 		passwordLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(8)
 				.setWidth(40)
 				.shiftXPos(15)
 				.shiftYPos(-45);
 		ownerField = new OwnerField();
 		ownerField
 				.setMaximumCharacters(18)
 				.setMaximumLines(1)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(14)
 				.setWidth(107)
 				.shiftXPos(70)
 				.shiftYPos(-69);
 		ownerLabel = new OwnerLabel("Owner:");
 		ownerLabel
 				.setAuto(true)
 				.setAnchor(WidgetAnchor.CENTER_CENTER)
 				.setHeight(10)
 				.setWidth(40)
 				.shiftXPos(15)
 				.shiftYPos(-65);
 		attachWidgets(plugin, borderTexture, closeButton, applyButton, unlockButton, usersField, usersLabel, coownersField, coownersLabel,
 				costToUseField, costToUseLabel, damageOnFailField, damageOnFailLabel, costToCreateOutputLabel,
 				costToCreateLabel, everyoneCheckbox, passwordField, passwordLabel, ownerField, ownerLabel, closeTimerField, closeTimerLabel);
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
 		if (lock == null) {
 			for (Widget widget : getAttachedWidgets()) {
 				if (widget instanceof GenericTextField && (!(widget instanceof OwnerField))) {
 					((GenericTextField) widget).setText("");
 				} else if (widget instanceof GenericCheckBox) {
 					((GenericCheckBox) widget).setChecked(false);
 				} else if (widget instanceof OwnerField) {
 					((OwnerField) widget).setText(getPlayer().getName());
 				} else if (widget instanceof CreateCostValueLabel) {
 					final double value = plugin.getConfiguration().getCosts().getLockCost(getLocation().getBlock().getType());
 					String hexColor = "ffffff"; //white
 					if (value > 0.0) {
 						hexColor = "008000"; //green
 					} else if (value < 0.0) {
 						hexColor = "ff0000"; //red
 					}
 					((CreateCostValueLabel) widget).setText(Double.toString(value).replaceAll("[^\\d.]", ""));
 					((CreateCostValueLabel) widget).setTextColor(new Color(hexColor));
 				} else if (widget instanceof UnlockButton) {
 					widget.setTooltip("You cannot unlock a lock that doesn't exist!");
 					((UnlockButton) widget).setEnabled(false);
 				}
 			}
 			applyButton.setText("Lock");
 			closeTimerField.setText("5");
 			costToCreateLabel.setText("Cost to create:");
 			this.setDirty(true);
 			return;
 		}
 		ownerField.setText(lock.getOwner());
 		//Loop through all co-owners and build a string to insert into the field
 		final StringBuilder output = new StringBuilder();
 		final List<String> coowners = lock.getCoOwners();
 		for (int i = 0; i < coowners.size(); i++) {
 			if (i > 0) {
 				output.append(", ");
 			}
 			output.append(coowners.get(i));
 		}
 		coownersField.setText(output.toString());
 		final List<String> users = lock.getUsers();
 		output.delete(0, output.length());
		if (everyoneCheckbox.isChecked()) {
			output.append("");
		} else {
			for (int i = 0; i < users.size(); i++) {
				if (i > 0) {
					output.append(", ");
				}
				output.append(users.get(i));
 			}
 		}
 		usersField.setText(output.toString());
 		//Change label names for modifying locks
 		costToCreateLabel.setText("Cost to change:");
 		final double value = plugin.getConfiguration().getCosts().getUpdateCost(getLocation().getBlock().getType());
 		String hexColor = "ffffff"; //white
 		if (value > 0.0) {
 			hexColor = "008000"; //green
 		} else if (value < 0.0) {
 			hexColor = "ff0000"; //red
 		}
 		costToCreateOutputLabel.setText(Double.toString(value).replaceAll("[^\\d.]", ""));
 		costToCreateOutputLabel.setTextColor(new Color(hexColor));
 		if (LockUtil.canPerformAction(getPlayer(), "UNLOCK")) {
 			final String name = getPlayer().getName();
 			boolean canUnlock;
 			if (!ownerField.getText().equals(name)) {
 				if (!coowners.contains(name)) {
 					unlockButton.setEnabled(false);
 					unlockButton.setTooltip("You do not have permission, not the owner, or a co-owner and therefore cannot unlock this lock!");
 				}
 				canUnlock = true;
 			} else {
 				canUnlock = true;
 			}
 			if (canUnlock) {
 				unlockButton.setTooltip("Click this to free this lock!");
 				unlockButton.setEnabled(true);
 			}
 		}
 		costToUseField.setText(Double.toString(((BukkitLock) lock).getUseCost()));
 		if (lock instanceof DoorBukkitLock) {
 			closeTimerField.setText(Long.toString(((DoorBukkitLock) lock).getAutocloseTimer()).replaceAll("[^\\d.]", ""));
 		}
 		applyButton.setText("Update");
 		this.setDirty(true);
 	}
 
 	public Location getLocation() {
 		return location;
 	}
 
 	public void setLocation(Location location) {
 		this.location = location;
 	}
 }
