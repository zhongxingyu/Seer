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
 package com.almuramc.aqualock.bukkit.display.button;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.almuramc.aqualock.bukkit.AqualockPlugin;
 import com.almuramc.aqualock.bukkit.display.AquaPanel;
 import com.almuramc.aqualock.bukkit.display.checkbox.EveryoneCheckbox;
 import com.almuramc.aqualock.bukkit.display.field.CloseTimerField;
 import com.almuramc.aqualock.bukkit.display.field.CoOwnerField;
 import com.almuramc.aqualock.bukkit.display.field.OwnerField;
 import com.almuramc.aqualock.bukkit.display.field.PasswordField;
 import com.almuramc.aqualock.bukkit.display.field.UseCostField;
 import com.almuramc.aqualock.bukkit.display.field.UserField;
 import com.almuramc.aqualock.bukkit.util.LockUtil;
 
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.Widget;
 
 import org.bukkit.Location;
 
 public class ApplyButton extends GenericButton {
 	private final AqualockPlugin plugin;
 
 	public ApplyButton(AqualockPlugin plugin) {
 		super("Apply");
 		this.plugin = plugin;
 	}
 
 	@Override
 	public void onButtonClick(ButtonClickEvent event) {
 		final AquaPanel panel = (AquaPanel) event.getScreen();
 		String owner = "";
 		List<String> coowners = null;
 		List<String> users = null;
 		String password = "";
 		double cost = 0;
 		long timer = 0;
 		for (Widget widget : panel.getAttachedWidgets()) {
 			final Class clazz = widget.getClass();
 			if (clazz.equals(OwnerField.class)) {
 				owner = ((OwnerField) widget).getText();
 			} else if (clazz.equals(PasswordField.class)) {
 				password = ((PasswordField) widget).getText();
 			} else if (clazz.equals(CoOwnerField.class)) {
 				coowners = parseFieldToList(((CoOwnerField) widget).getText());
 			} else if (clazz.equals(UserField.class)) {
 				boolean everyone = false;
 				for (Widget inner : panel.getAttachedWidgets()) {
 					if (inner instanceof EveryoneCheckbox) {
						everyone = ((EveryoneCheckbox) widget).isChecked();
 					}
 				}
 				final ArrayList<String> temp = new ArrayList<>();
 				temp.add("Everyone");
 				users = everyone == true ? temp : parseFieldToList(((UserField) widget).getText());
 			} else if (clazz.equals(CloseTimerField.class)) {
 				try {
 					timer = Long.parseLong(((CloseTimerField) widget).getText(), 10);
 				} catch (Exception e) {
 					//do nothing
 				}
 			} else if (clazz.equals(UseCostField.class)) {
 				try {
 					cost = Double.parseDouble(((UseCostField) widget).getText());
 				} catch (Exception e) {
 					//do nothing
 				}
 			}
 		}
 		final Location loc = panel.getLocation();
 		boolean close = true;
 		if (AqualockPlugin.getRegistry().contains(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
 			if (!LockUtil.update(owner, coowners, users, password, panel.getLocation(), panel.getLocation().getBlock().getData(), cost, timer)) {
 				close = false;
 			}
 		} else {
 			if (!LockUtil.lock(owner, coowners, users, password, panel.getLocation(), panel.getLocation().getBlock().getData(), cost, timer)) {
 				close = false;
 			}
 		}
 		if (close) {
 			panel.close();
 		}
 	}
 
 	public List<String> parseFieldToList(String text) {
 		ArrayList<String> temp = new ArrayList<>();
 		final char[] chars = text.toCharArray();
 		final StringBuilder parsed = new StringBuilder();
 		for (int i = 0; i < chars.length; i++) {
 			if (chars[i] == ',') {
 				temp.add(parsed.toString());
 				parsed.delete(0, parsed.length());
 				continue;
 			}
 			if (chars[i] == ' ') {
 				continue;
 			}
 			if (i == chars.length) {
 				temp.add(parsed.toString());
 				parsed.append(chars[i]);
 				continue;
 			}
 			parsed.append(chars[i]);
 		}
 		return temp;
 	}
 }
