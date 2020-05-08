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
 
 import com.almuramc.aqualock.bukkit.AqualockPlugin;
 import com.almuramc.aqualock.bukkit.display.CachedGeoPopup;
 import com.almuramc.aqualock.bukkit.display.field.PasswordField;
 import com.almuramc.aqualock.bukkit.util.LockUtil;
 
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.Widget;
 
 public class RemoveButton extends GenericButton {
 	private final AqualockPlugin plugin;
 
 	public RemoveButton(AqualockPlugin plugin) {
 		super("Remove");
 		this.plugin = plugin;
 	}
 
 	@Override
 	public void onButtonClick(ButtonClickEvent event) {
 		final CachedGeoPopup panel = (CachedGeoPopup) event.getScreen();
 		String password = "";
 		for (Widget widget : panel.getAttachedWidgets()) {
 			if (widget instanceof PasswordField) {
 				password = ((PasswordField) widget).getText();
 			}
 		}
		if (LockUtil.unlock(getScreen().getPlayer().getName(), password, panel.getLocation())) {
 			((CachedGeoPopup) event.getScreen()).onClose();
 		}
 	}
 }
