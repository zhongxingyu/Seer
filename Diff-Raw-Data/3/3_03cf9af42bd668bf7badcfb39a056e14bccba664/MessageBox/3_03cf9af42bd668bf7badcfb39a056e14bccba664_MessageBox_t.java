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
 
 package com.jpii.navalbattle.pavo.gui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 
 import javax.swing.ImageIcon;
 
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.PavoHelper;
 import com.jpii.navalbattle.renderer.Helper;
 
 /**
  * @author MKirkby
  *
  */
 public class MessageBox extends com.jpii.navalbattle.pavo.gui.GameWindow {
 	String message = "no msg";
 	MessageBoxIcon icon;
 	private static BufferedImage msg_error = null,msg_warn = null,msg_notify = null,msg_info = null;
 	private MessageBox(String title, String message,MessageBoxIcon icon) {
 		super();
 		this.icon = icon;
 		setTitle(title);
 		this.message = message;
 		String[] lines = message.split("\n");
 		int lg = 0;
 		for (int v = 0; v < lines.length; v++) {
 			if (lines[v].length() > lg) {
 				lg = lines[v].length();
 			}
 		}
 		int w = lg * 10;
 		int h = 125 + (16 * lines.length);
 		setSize(w,h);
 		setLoc((Game.Settings.currentWidth/2)-(w/2),(Game.Settings.currentHeight/2)-(h/2));
 		render();
 	}
 	public static void setMessageBoxErrorIcon(BufferedImage icon) {
 		msg_error = icon;
 	}
 	public static void setMessageBoxWarnIcon(BufferedImage icon) {
 		msg_warn = icon;
 	}
 	public static void setMessageBoxInfoIcon(BufferedImage icon) {
 		msg_info = icon;
 	}
 	public static void setMessageBoxNotifyIcon(BufferedImage icon) {
 		msg_notify = icon;
 	}
 	/**
 	 * Shows a message box on the active Game window, with a title and message.
 	 * @param title The title that the message box should have.
 	 * @param message The message that the message box should have.
 	 */
 	public static void show(String title, String message) {
 		show(title,message,MessageBoxIcon.None);
 	}
 	/**
 	 * Shows a message box on the active Game window, with a title, special icon, and message.
 	 * @param title The title that the message box should have.
 	 * @param message The message that the message box should have.
 	 * @param iconifier The icon to show on the message box.
 	 */
 	public static void show(String title, String message, MessageBoxIcon iconifier) {
 		show(title,message,iconifier,false,true);
 	}
	public static void show(String title, String message, MessageBoxIcon iconifier, boolean blotchBackground) {
		show(title,message,iconifier,blotchBackground,true);
	}
 	public static void closeAllMessageBoxes() {
 		if (WindowManager.Inst == null)
 			return;
 		for (int c = 0; c < WindowManager.Inst.size(); c++) {
 			GameWindow gw = WindowManager.Inst.get(c);
 			if (gw != null && gw instanceof MessageBox) {
 				WindowManager.Inst.remove(gw);
 			}
 		}
 	}
 	/**
 	 * Shows a message box on the active Game window, with a title, special icon, gives the option of hiding the backrouns, and message.
 	 * @param title The title that the message box should have.
 	 * @param message The message that the message box should have.
 	 * @param iconifier The icon to show on the message box.
 	 * @param blotchBackground If set to true, the background will be blocked from user interaction, and blurred out.
 	 */
 	public static void show(String title, String message, MessageBoxIcon iconifier, boolean blotchBackground, boolean onlyOneAllowed) {
 		//WindowManager.Inst.add(new MessageBox(title,message));
 		if (WindowManager.Inst == null)
 			return;
 		
 		MessageBox handle = new MessageBox(title,message,iconifier);
 		handle.setHandle(395428394);
 		if (onlyOneAllowed) {
 			for (int c = 0; c < WindowManager.Inst.size(); c++) {
 				GameWindow gw = WindowManager.Inst.get(c);
 				if (gw != null && gw instanceof MessageBox) {
 					WindowManager.Inst.remove(gw);
 				}
 			}
 		}
 		if (blotchBackground) {
 			WindowManager.Inst.ianOwjej10nJAnin345soaKOEe9201LIQUICK(handle);
 		}
 		else
 			WindowManager.Inst.add(handle);
 	}
 	public void render() {
 		super.render();
 		Graphics2D g = PavoHelper.createGraphics(getBuffer());
 		BufferedImage hs = null;
 		g.setColor(Color.red);
 		if (icon == MessageBoxIcon.Error) {
 			hs = msg_error;
 		}
 		if (icon == MessageBoxIcon.Warning) {
 			hs = msg_warn;
 		}
 		if (icon == MessageBoxIcon.Information) {
 			hs = msg_info;
 		}
 		if (icon == MessageBoxIcon.Notify) {
 			hs = msg_notify;
 		}
 		if (hs != null) {
 			int size = (getHeight()/2);
 			g.drawImage(hs,getWidth()-(size+8), ((size+13)/2), size,size,null);
 		}
 		g.setColor(Color.black);
 		g.setFont(Helper.GUI_GAME_FONT);
 		String[] lines = message.split("\n");
 		for (int v = 0; v < lines.length; v++) {
 			g.drawString(lines[v], 5, 40 + (v*14));
 		}
 		g.setColor(getBackgroundColor().darker().darker());
 		g.fillRoundRect((getWidth()/2)-30, (getHeight()-36), 60, 24, 5,5);
 		g.setColor(Color.black);
 		g.drawRoundRect((getWidth()/2)-30, (getHeight()-36), 60, 24, 5,5);
 		g.drawString("OK",(getWidth()/2)-8, (getHeight()-18));
 	}
 	public boolean checkOtherDown(MouseEvent me) {
 		int mx = me.getX();
 		int my = me.getY();
 		if (mx >= (getWidth()/2)-30+getX() && mx <= (getWidth()/2)+30+getX() && my >= (getHeight()-36)+getY() && my <= (getHeight()-18)+getY()) {
 			onCloseCalled();
 			return true;
 		}
 		return false;
 	}
 	public void onCloseCalled() {
 		getWinMan().remove(this);
 	}
 }
