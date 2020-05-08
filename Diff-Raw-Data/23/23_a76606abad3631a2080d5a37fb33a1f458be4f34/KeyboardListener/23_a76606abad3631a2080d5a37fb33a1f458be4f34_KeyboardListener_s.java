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
 
 package com.jpii.navalbattle.gui.listeners;
 
 import java.awt.event.*;
 import java.util.ArrayList;
 
 import com.jpii.navalbattle.debug.DebugWindow;
 import com.jpii.navalbattle.gui.*;
 
 public class KeyboardListener implements KeyListener {
 	
 	ArrayList<Object> windows;
 	
 	public KeyboardListener() {
 		windows = new ArrayList<Object>();
 	}
 	
 	public void add(Object classname){
 		windows.add(classname);
 	}
 	
 	public void remove(Object classname){
 		windows.remove(classname);
 	}
 	
 	public void keyPressed(KeyEvent k) {	
 		if(k.getKeyCode() == KeyEvent.VK_ESCAPE) {
 			WindowCloser.close();
 		}
 		for(int index=0; index<windows.size();index++) {
 			Object window = windows.get(index);
 			if(window instanceof LoginWindow) {
 				LoginWindow l = (LoginWindow) window;
 				if(k.getKeyCode() == KeyEvent.VK_ENTER) {
 					l.login();
 				}
 			}
 			if(window instanceof DebugWindow) {
 				DebugWindow d = (DebugWindow) window;
 				if(k.getKeyCode() == KeyEvent.VK_ENTER) {
 					d.submitCommandRemote();
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void keyReleased(KeyEvent e) {
 		
 	}
 	
 	@Override
 	public void keyTyped(KeyEvent e) {
 		
 	}
 }
