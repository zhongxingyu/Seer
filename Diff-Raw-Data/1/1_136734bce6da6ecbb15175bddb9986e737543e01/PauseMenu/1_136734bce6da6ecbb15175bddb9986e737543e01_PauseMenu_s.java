 package com.sk83rsplace.arkane.menus;
 
 import org.newdawn.slick.Color;
 
 import com.sk83rsplace.arkane.GUI.Menu;
 import com.sk83rsplace.arkane.GUI.components.ButtonComponent;
 import com.sk83rsplace.arkane.client.Board;
 import com.sk83rsplace.arkane.client.Client;
 
 public class PauseMenu extends Menu {
 	public PauseMenu() {
 		setColor(new Color(0f, 0f, 0f, 0.5f));
 		addComponent(new ButtonComponent("Resume", -1, 128) {
 			public void onClick() {
 				Board.menuStack.pop();
 			}
 		});
 		addComponent(new ButtonComponent("Server Listing", -1, 192) {
 			public void onClick() {
 				Client.connected = false;
 				Board.menuStack.pop();
 				Board.menuStack.add(new ServerListMenu());
 				Board.client = null;
 			}
 		});
 	}
 }
