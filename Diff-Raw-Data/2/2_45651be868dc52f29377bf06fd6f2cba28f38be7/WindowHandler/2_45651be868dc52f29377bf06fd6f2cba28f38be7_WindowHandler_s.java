 /*
  * Copyright (C) 2012 JPII and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jpii.navalbattle.data;
 
 import java.util.*;
 import javax.swing.*;
 
 import com.jpii.gamekit.toaster.Toaster;
 import com.jpii.navalbattle.game.SinglePlayerGame;
 import com.jpii.navalbattle.gui.*;
 
 public class WindowHandler {
 	
 	public ArrayList<Window> windows;
 	private static Toaster toasterManager;
 	
 	/**
 	 * Default constructor for <code>WindowConstructor</code>
 	 */
 	public WindowHandler() {
 		toasterManager = new Toaster();
 		windows = new ArrayList<Window>();
 		initArray();
 		windows.get(0).setVisible(true);
 	}
 	
 	/**
 	 * Initialize <code>ArrayList</code> of <code>Windows</code>.
 	 */
 	private void initArray() {
 		windows.add(new LoginWindow());
 		windows.add(new LoggingInWindow());
 		windows.add(new LoginOptionsWindow());
 		windows.add(new MainMenuWindow());
 		windows.add(new SPOptions());
 		windows.add(new HelpWindow());
 		windows.add(new CreditsWindow());
 		windows.add(new SinglePlayerGame());
 	}
 	
 	/**
 	 * Open a new window from <code>String</code>.
 	 * @param a
 	 */
 	public void setNewWindow(String a) {
 		for(int index = 0; index<windows.size(); index++){
 			JFrame temp = (Window) windows.get(index);
 			if(a.toLowerCase().equals( temp.getClass().toString().substring((temp.getClass().toString().lastIndexOf(".")+1)).toLowerCase() )){
 				temp.setVisible(true);
 			}
 			else{
 				temp.setVisible(false);
 			}
 		}
 	}
 	
 	/**
 	 * Add a new <code>Window</code> to the <code>ArrayList</code>
 	 * and make it visible.
 	 * @param w
 	 */
 	public void add(Window w) {
 		windows.add(w);
 		w.setVisible(true);
 	}
 	
 	/**
 	 * Returns current instance of <code>Toaster</code>. Used to send desktop notifications.
 	 * @return toasterManager
 	 */
 	public Toaster getToasterManager() {
 		return toasterManager;
 	}
 	
 	public void killAll() {
 		for(int index = 0; index<windows.size(); index+=0){
 			JFrame temp = windows.get(index);
 			if(temp instanceof SinglePlayerGame){
 				index++;
 			}
 			else{
 				((Window)temp).donewithMe();
 				windows.remove(index);
 			}
 		}
		System.out.println("Done");
 	}
 }
