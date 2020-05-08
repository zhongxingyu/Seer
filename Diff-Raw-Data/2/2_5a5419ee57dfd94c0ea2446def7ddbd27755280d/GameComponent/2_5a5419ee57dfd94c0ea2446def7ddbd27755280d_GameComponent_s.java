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
 
 package com.jpii.navalbattle.game;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.event.*;
 import java.lang.Thread.State;
 
 import javax.swing.*;
 
 import com.jpii.navalbattle.renderer.Console;
 import com.jpii.navalbattle.renderer.Helper;
 import com.jpii.navalbattle.renderer.RenderConstants;
 import com.jpii.navalbattle.renderer.RepaintType;
 
 
 /**
  * @author MKirkby
  * 
  */
 @SuppressWarnings({ "serial", "unused" })
 public class GameComponent extends JComponent {
 	JFrame frame;
 	Timer ticker;
 	boolean waitingForGen = false;
 	GameBeta game;
 	public GameComponent(JFrame frame) {
 		this.frame = frame;
 		ActionListener al = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (waitingForGen) {
 					if (game.getGenerationComplete() >= 100)
 						waitingForGen = false;
 					repaint();
 				}
 			}
 		};
 		ticker = new Timer(40, al);
 		ticker.start();
 		game = new GameBeta();
 		waitingForGen = true;
 		game.generate();
 	}
 	public void paintComponent(Graphics g) {
 		if (waitingForGen) {
 			g.setColor(new Color(61,64,38));
 			g.fillRect(0,0,800,600);
 			g.setColor(Color.black);
 			g.fillRect(0,290,800,20);
 			g.setColor(Color.green);
			g.fillRect(1,291,(game.getGenerationComplete() * 8)-2,18);
 			g.setColor(Color.white);
 			Font f = new Font("Courier New",0,24);
 			g.setFont(f);
 			String s = "Percent complete: " + game.getGenerationComplete() + "%";
 			int w = g.getFontMetrics(f).stringWidth(s);
 			g.drawString(s, 400 - (w/2), 80);
 			s = "Generating, please wait a while.";
 			w = g.getFontMetrics(f).stringWidth(s);
 			g.drawString(s,400 - (w/2),132);
 		}
 		else {
 			g.setColor(Color.black);
 			g.fillRect(0,0,800,600);
 		}
 	}
 }
