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
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.util.ArrayList;
 import javax.swing.*;
 import java.beans.*;
 
 import com.jpii.dagen.Engine;
 import com.jpii.dagen.MapType;
 import com.jpii.navalbattle.data.Helper;
 import com.jpii.navalbattle.game.entity.Entity;
 
 /**
  * @author MKirkby
  * 
  */
 @SuppressWarnings("serial")
 public class GameComponent extends JComponent {
 	JFrame frame;
 	ArrayList<Entity> entities;
 	Timer ticker;
 
 	BufferedImage grid, shadow,map;
 
 	int test;
 	JSlider slider;
 
 	Engine eng;
 	
 	public GameComponent(JFrame frame) {
 		this.frame = frame;
 		
 		slider = new JSlider();
 		slider.setForeground(new Color(0,0,0,255));
 		slider.addPropertyChangeListener(new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent arg0) {
 				onZoom();
 			}
 		});
 		slider.setPaintTicks(true);
 		slider.setOrientation(SwingConstants.VERTICAL);
 		slider.setBounds(10, 63, 31, 177);
 		add(slider);
 		ActionListener al = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				tick();
 			}
 		};
 		int w = Toolkit.getDefaultToolkit().getScreenSize().width;
 		int h = Toolkit.getDefaultToolkit().getScreenSize().height;
 		
 		eng = new Engine(w,h);
 		eng.generate(MapType.Hills, (int)(Math.random() * 99999999999.9), 1.0);
 		
 		grid = Helper.genGrid(w, h, 20);
 		shadow = Helper.genInnerShadow(w, h);
 		map = Helper.genMap(eng, w, h, 1);
 
 		entities = new ArrayList<Entity>();
 
 		ticker = new Timer(100, al);
 
 		start();
 
 	}
 
 	public void addEntity(Entity entity) {
 		if (entity != null) {
 			entities.add(entity);
 		}
 	}
 
 	public Entity getEntity(String tag) {
 		for (int x = 0; x < entities.size(); x++) {
 			if (entities.get(x).getTag().toLowerCase() == tag.toLowerCase()) {
 				return entities.get(x);
 			}
 		}
 		return null;
 	}
 
 	public Entity getEntity(int index) {
 		return entities.get(index);
 	}
 
 	private void tick() {
 		for (int x = 0; x < entities.size(); x++) {
 			if (entities.get(x) != null) {
 				entities.get(x).tick();
 			}
 		}
 		test += 1;
 		repaint();
 	}
 	
 	private void onZoom() {
 		int w = Toolkit.getDefaultToolkit().getScreenSize().width;
 		int h = Toolkit.getDefaultToolkit().getScreenSize().height;
 		grid = Helper.genGrid(w,h, getGridSize());
 	}
 	public int getGridSize() {
 		int c = (slider.getValue() * 40)/100;
 		if (c > 5)
 			return c;
 		else
 			return 5;
 	}
 
 	public void paintComponent(Graphics g) {
 		g.setColor(Color.red);
 		g.fillRect(0, 0, getWidth() + 1, getHeight() + 1);
 		
 		g.drawImage(map,0,0,null);
 
 		g.drawImage(grid, 0, 0, null);
 		g.drawImage(shadow, 0, 0, null);
 
 		g.setColor(Color.black);
 		g.drawString("This is just a test.", 100, 100);
 		g.drawString(
 				"In the future, this is where the map, enitities, and GUI would go.",
 				100, 120);
 		g.drawString(
 				"Just to make sure that the ticker/updater is working, here is how many ticks performed:"
 						+ test, 100, 140);
 	}
 
 	public void setTimeTick(int interval) {
 		ticker.setDelay(interval);
 	}
 
 	public int getTimeTick() {
 		return ticker.getDelay();
 	}
 
 	public void start() {
 		ticker.start();
 	}
 
 	public void stop() {
 		ticker.stop();
 	}
 
 	public void repaint() {
 		frame.repaint();
 	}
 }
