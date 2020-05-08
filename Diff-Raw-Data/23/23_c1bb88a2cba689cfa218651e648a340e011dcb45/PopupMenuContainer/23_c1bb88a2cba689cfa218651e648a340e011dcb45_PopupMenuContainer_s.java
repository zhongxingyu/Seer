 /*
  * Copyright 2005-2007 jWic group (http://www.jwic.de)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * de.jwic.ecolib.controls.menubutton.MenuContainer
  * Created on 10.11.2012
  * $Id:$
  */
 package de.jwic.ecolib.controls.menucontrols;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Holds a list of menu items.
  * 
  * @author Andrei
  */
public class PopupMenuContainer {
 	private final List<MenuItem> menuItems = new ArrayList<MenuItem>();
 
 	/**
 	 * Adds a menu item to the container.
 	 * 
 	 * @return
 	 */
 	public MenuItem addMenuItem() {
 		MenuItem item = new MenuItem();
 		menuItems.add(item);
 		return item;
 	}
 
 	/**
 	 * @return the menuItems in this container.
 	 */
 	public List<MenuItem> getMenuItems() {
 		return menuItems;
 	}
 
 	/**
 	 * Checks if any of the menu items must render an icon.
 	 * 
 	 * @return
 	 */
 	public boolean hasIcons() {
 		for (MenuItem item : menuItems) {
 			if (item.hasIcon())
 				return true;
 		}
 		return false;
 	}
 
 }
